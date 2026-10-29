package space.qouve.networking.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import space.qouve.logger.DeamonLogger;
import space.qouve.protocol.ServerProtocol;

import java.util.logging.Level;

public class Client {
    private final WebSocket conn;
    private final ClientInfo info;
    private final ObjectMapper mapper;

    public Client(WebSocket conn, ClientInfo info, ObjectMapper mapper) {
        this.conn = conn;
        this.info = info;
        this.mapper = mapper;
    }

    public ClientInfo info() {
        return info;
    }

    public boolean isOpen() {
        return conn.isOpen();
    }

    public void send(ServerProtocol msg) {
        if (!conn.isOpen()) return;
        try {
            conn.send(mapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            DeamonLogger.get().log(Level.SEVERE,
                    "Massage to " + info.serverId() + " cannot sertilized", e);
        }
    }

    public void close(int code, String reason) {
        conn.close(code, reason);
    }
}