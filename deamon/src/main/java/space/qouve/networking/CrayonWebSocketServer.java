package space.qouve.networking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import space.qouve.logger.DeamonLogger;
import space.qouve.models.ServerType;
import space.qouve.networking.client.Client;
import space.qouve.networking.client.ClientInfo;
import space.qouve.networking.handler.Dispatcher;
import space.qouve.protocol.ClientProtocol;
import space.qouve.protocol.ServerProtocol;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class CrayonWebSocketServer extends WebSocketServer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Dispatcher dispatcher;
    private final TokenAuth auth;

    private final Map<WebSocket, Client> clients = new ConcurrentHashMap<>();
    private final Map<String, Client> clientsById = new ConcurrentHashMap<>();

    public CrayonWebSocketServer(int port, Dispatcher dispatcher, TokenAuth auth) {
        super(new InetSocketAddress(port));
        this.dispatcher = dispatcher;
        this.auth = auth;
    }

    @Override
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(
            WebSocket conn, Draft draft, ClientHandshake req) throws InvalidDataException {

        ServerType type = ServerType.parse(req.getFieldValue("X-Server-Type"));
        String id       = req.getFieldValue("X-Server-Id");
        String token    = req.getFieldValue("X-Token");

        if (type == null)
            throw reject("Unknow Server-Typ");
        if (!type.isValidId(id))
            throw reject("Unknow Server-ID for Typ " + type);
        if (!auth.isValid(type, id, token))
            throw reject("Unauthorized");
        if (clientsById.containsKey(id))
            throw reject("Server-ID is already connected");

        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, req);
    }

    private InvalidDataException reject(String reason) {
        DeamonLogger.get().warning("Verbindung abgelehnt: " + reason);
        return new InvalidDataException(CloseFrame.POLICY_VALIDATION, reason);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake h) {
        ClientInfo info = new ClientInfo(
                ServerType.parse(h.getFieldValue("X-Server-Type")),
                h.getFieldValue("X-Server-Id"),
                h.getFieldValue("X-Version"));

        Client client = new Client(conn, info, mapper);

        if (clientsById.putIfAbsent(info.serverId(), client) != null) {
            conn.close(CloseFrame.POLICY_VALIDATION, "Server-ID is already connected");
            return;
        }
        clients.put(conn, client);
        DeamonLogger.get().info("Connected: " + info.type() + " / " + info.serverId());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Client client = clients.remove(conn);
        if (client != null) {
            clientsById.remove(client.info().serverId(), client);
            DeamonLogger.get().info("disconnected: " + client.info().serverId());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String raw) {
        Client client = clients.get(conn);
        if (client == null) {
            conn.close(CloseFrame.POLICY_VALIDATION, "Not authenticated");
            return;
        }

        ClientProtocol msg;
        try {
            msg = mapper.readValue(raw, ClientProtocol.class);
        } catch (JsonProcessingException e) {
            client.send(new ServerProtocol.ErrorResponse(null, "BAD_REQUEST", "invalid message"));
            return;
        }
        dispatcher.dispatch(client, msg);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        DeamonLogger.get().log(Level.SEVERE, "WebSocket-Error", ex);
    }

    @Override
    public void onStart() {
        DeamonLogger.get().info("Server started on port " + getPort());
    }

    public Client getClient(String serverId) {
        return clientsById.get(serverId);
    }
}