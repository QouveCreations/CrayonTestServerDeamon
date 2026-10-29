package space.qouve;

import space.qouve.netwoking.CrayonWebSocketServer;
import space.qouve.netwoking.Dispatcher;

public class Main {
    static void main() {
        Dispatcher dispatcher = new Dispatcher();
        CrayonWebSocketServer socketServer = new CrayonWebSocketServer(6666, dispatcher);
        socketServer.start();


    }
}
