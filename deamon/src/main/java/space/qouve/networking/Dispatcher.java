package space.qouve.networking;

import space.qouve.networking.client.Client;
import space.qouve.protocol.ClientProtocol;
import space.qouve.protocol.ServerProtocol;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private final Map<Class<? extends ClientProtocol>, Handler<?>> handlers = new HashMap<>();

    public <T extends ClientProtocol> void register(Class<T> type, Handler<T> handler) {
        handlers.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(Client client, ClientProtocol msg) {
        Handler<ClientProtocol> handler = (Handler<ClientProtocol>) handlers.get(msg.getClass());
        if (handler == null) {
            client.send(new ServerProtocol.Error(msg.id(), "UNKNOWN_TYPE", "no handler"));
            return;
        }
        handler.handle(client, msg);
    }
}