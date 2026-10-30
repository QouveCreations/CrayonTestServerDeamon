package space.qouve.models;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Server {
    private final String userID;
    private final String repositoryID;
    private ServerStatus status;

    public Server(String userID, String repositoryID, ServerStatus status) {
        this.userID = userID;
        this.repositoryID = repositoryID;
        this.status = status;
    }


    public enum ServerStatus {
        Running, Starting, Stopping, InQue, UnHealthy
    }



}