package space.qouve.netwoking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.WebSocket;
import space.qouve.netwoking.protokoll.ServerProtokoll;

public class Client {
    private final WebSocket conn;
    private final ObjectMapper mapper;

    public Client(WebSocket conn, ObjectMapper mapper) {
        this.conn = conn;
        this.mapper = mapper;
    }

    public void send(ServerProtokoll msg) {
        try {
            conn.send(mapper.writeValueAsString(msg));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}