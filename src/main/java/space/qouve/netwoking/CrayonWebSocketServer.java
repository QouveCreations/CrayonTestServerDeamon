package space.qouve.netwoking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import space.qouve.netwoking.protokoll.ClientProtokoll;
import space.qouve.netwoking.protokoll.ServerProtokoll;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CrayonWebSocketServer extends WebSocketServer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Dispatcher dispatcher;
    private final Map<WebSocket, Client> clients = new ConcurrentHashMap<>();

    public CrayonWebSocketServer(int port, Dispatcher dispatcher) {
        super(new InetSocketAddress(port));
        this.dispatcher = dispatcher;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.put(conn, new Client(conn, mapper));
        System.out.println("Client connected");
    }

    @Override
    public void onMessage(WebSocket conn, String raw) {
        Client client = clients.get(conn);
        ClientProtokoll msg;
        try {
            msg = mapper.readValue(raw, ClientProtokoll.class);
        } catch (JsonProcessingException e) {
            client.send(new ServerProtokoll.Error(null, "BAD_REQUEST", "invalid message"));
            return;
        }
        dispatcher.dispatch(client, msg);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        System.out.println("Client disconnected");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("Server running on port " + getPort());
    }
}