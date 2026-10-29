package space.qouve.netwoking;

import space.qouve.netwoking.protokoll.ClientProtokoll;
import space.qouve.netwoking.protokoll.ServerProtokoll;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private final Map<Class<? extends ClientProtokoll>, Handler<?>> handlers = new HashMap<>();

    public <T extends ClientProtokoll> void register(Class<T> type, Handler<T> handler) {
        handlers.put(type, handler);
    }

    @SuppressWarnings("unchecked")
    public void dispatch(Client client, ClientProtokoll msg) {
        Handler<ClientProtokoll> handler = (Handler<ClientProtokoll>) handlers.get(msg.getClass());
        if (handler == null) {
            client.send(new ServerProtokoll.Error(msg.id(), "UNKNOWN_TYPE", "no handler"));
            return;
        }
        handler.handle(client, msg);
    }
}