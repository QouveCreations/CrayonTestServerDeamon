package space.qouve.models;

public class Server {
    public String userID;
    public ServerStatus status;

    public enum ServerStatus {
        Running, Starting, Stopping, InQue, UnHealthy
    }

}