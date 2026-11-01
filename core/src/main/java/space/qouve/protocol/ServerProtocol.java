package space.qouve.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import space.qouve.models.Server;
import space.qouve.models.ServerType;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServerProtocol.Ack.class, name = "ack"),
        @JsonSubTypes.Type(value = ServerProtocol.ErrorResponse.class, name = "error"),
        @JsonSubTypes.Type(value = ServerProtocol.UserInQueue.class, name = "userinqueue"),
        @JsonSubTypes.Type(value = ServerProtocol.CreatingServer.class, name = "creatingserver"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerStarted.class, name = "serverstarted"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerRestarting.class, name = "serverrestarting"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerStopped.class, name = "serverstopped"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerCrashed.class, name = "servercrashed"),
        @JsonSubTypes.Type(value = ServerProtocol.FilesUpdated.class, name = "filesupdated"),
        @JsonSubTypes.Type(value = ServerProtocol.TestServerFilesUpdated.class, name = "testserverfilesupdated"),
        @JsonSubTypes.Type(value = ServerProtocol.NewConsoleLine.class, name = "newconsoleline"),
        @JsonSubTypes.Type(value = ServerProtocol.ConsoleHistory.class, name = "consolehistory"),
        @JsonSubTypes.Type(value = ServerProtocol.MemoryUsage.class, name = "memoryusage"),
        @JsonSubTypes.Type(value = ServerProtocol.CpuUsage.class, name = "cpuusage"),
        @JsonSubTypes.Type(value = ServerProtocol.DestroyTime.class, name = "destroytime"),
        @JsonSubTypes.Type(value = ServerProtocol.TPS.class, name = "tps"),
        @JsonSubTypes.Type(value = ServerProtocol.Servers.class, name = "servers"),
        @JsonSubTypes.Type(value = ServerProtocol.DaemonInformation.class, name = "daemoninformation"),
        @JsonSubTypes.Type(value = ServerProtocol.UpdateFiles.class, name = "updatefiles"),
})
public sealed interface ServerProtocol {
    String id();

    record Ack(String id) implements ServerProtocol {}
    record ErrorResponse(String id, String code, String message) implements ServerProtocol {}

    record UserInQueue(String id, String userID, int place) implements ServerProtocol {}
    record CreatingServer(String id, String userID) implements ServerProtocol {}
    record ServerStarted(String id, String userID) implements ServerProtocol {}
    record ServerRestarting(String id, String userID) implements ServerProtocol {}
    record ServerStopped(String id, String userID) implements ServerProtocol {}
    record ServerCrashed(String id, String userID, List<String> errorLines) implements ServerProtocol {}

    record FilesUpdated(String id, String repositoryID, ServerType serverType) implements ServerProtocol {}
    record TestServerFilesUpdated(String id, String userID) implements ServerProtocol {}

    record NewConsoleLine(String id, String userID, String line) implements ServerProtocol {}
    record ConsoleHistory(String id, String userID, List<String> lines) implements ServerProtocol {}

    // MB
    record MemoryUsage(String id, String userID, int usage, int max) implements ServerProtocol {}
    // %
    record CpuUsage(String id, String userID, int usage, int max) implements ServerProtocol {}
    // Time bevor destroy int = minutes (-1 for infinite)
    record DestroyTime(String id, String userID, int time) implements ServerProtocol {}
    // 0.0 to 20.0
    record TPS(String id, String userID, double tps) implements ServerProtocol {}

    record Servers(String id, String userID, List<Server> servers) implements ServerProtocol {}
    record DaemonInformation(String id, DaemonInfo daemonInformation) implements ServerProtocol {}
    record DaemonInfo(String version, long uptimeSeconds, int runningServers, int maxServers) {}

    // Sends the repo as zip to the clients (MC-Server clients).
    record UpdateFiles(String id, String slug, byte[] file) {}
}