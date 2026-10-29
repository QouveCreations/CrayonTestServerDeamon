package space.qouve.networking;

import space.qouve.networking.client.Client;
import space.qouve.protocol.ClientProtocol;

public interface Handler<T extends ClientProtocol> {
    void handle(Client client, T msg);
}