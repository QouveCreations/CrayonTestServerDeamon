package space.qouve.netwoking;

import space.qouve.netwoking.protokoll.ClientProtokoll;

public interface Handler<T extends ClientProtokoll> {
    void handle(Client client, T msg);
}