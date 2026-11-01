package space.qouve.networking.handler.handlers;


import space.qouve.Main;
import space.qouve.networking.CrayonWebSocketServer;
import space.qouve.networking.client.Client;
import space.qouve.networking.handler.Handler;
import space.qouve.protocol.ClientProtocol;
import space.qouve.protocol.ServerProtocol;

public class CreateTestServerFromCommit implements Handler<ClientProtocol.CreateTestServerFromCommit> {
    @Override
    public void handle(Client client, ClientProtocol.CreateTestServerFromCommit msg) {
        System.out.println(msg);

        client.send(new ServerProtocol.MemoryUsage(client.info().serverId(), msg.userID(), 1000, 2000));
        client.send(new ServerProtocol.TPS(client.info().serverId(), msg.userID(), 20));
        client.send(new ServerProtocol.CpuUsage(client.info().serverId(), msg.userID(), 10, 100));
    }
}
