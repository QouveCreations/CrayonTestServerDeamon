package space.qouve.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import space.qouve.models.Server;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServerProtocol.Ack.class,   name = "ack"),
        @JsonSubTypes.Type(value = ServerProtocol.Error.class, name = "error"),
        @JsonSubTypes.Type(value = ServerProtocol.CreatingServer.class, name = "creatingserver"),
        @JsonSubTypes.Type(value = ServerProtocol.UserInQue.class, name = "userinque"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerStopped.class, name = "serverstopped"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerCrashed.class, name = "servercrashed"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerStarted.class, name = "serverstarted"),
        @JsonSubTypes.Type(value = ServerProtocol.NewConsoleLine.class, name = "newconsoleline"),
        @JsonSubTypes.Type(value = ServerProtocol.ConsoleHistory.class, name = "consolehistory"),
        @JsonSubTypes.Type(value = ServerProtocol.ServerRestarting.class, name = "serverrestarting"),
        @JsonSubTypes.Type(value = ServerProtocol.FilesUpdated.class, name = "filesupdated"),
        @JsonSubTypes.Type(value = ServerProtocol.MemoryUsage.class, name = "memoryysage"),
        @JsonSubTypes.Type(value = ServerProtocol.CpuUsage.class, name = "cpuusage"),
        @JsonSubTypes.Type(value = ServerProtocol.TPS.class, name = "tps"),
        @JsonSubTypes.Type(value = ServerProtocol.Servers.class, name = "servers"),
        @JsonSubTypes.Type(value = ServerProtocol.DeamonInformation.class, name = "deamoninformation"),
})
public sealed interface ServerProtocol {

    record Ack(String id) implements ServerProtocol {}
    record Error(String id, String code, String message) implements ServerProtocol {}
    record CreatingServer(String id, String userID) implements ServerProtocol {}
    record UserInQue(String id, String userID, int place) implements ServerProtocol {}
    record ServerStopped(String id, String userID) implements ServerProtocol {}
    record ServerCrashed(String id, String userID, List<String> errorLines) implements ServerProtocol {}
    record ServerStarted(String id, String userID) implements ServerProtocol {}
    record NewConsoleLine(String id, String userID, String line) implements ServerProtocol {}
    record ConsoleHistory(String id, String userID, List<String> lines) implements ServerProtocol {}
    record ServerRestarting(String id, String userID) implements ServerProtocol {}
    record FilesUpdated(String id, String userID) implements ServerProtocol {}
    // Usage in MB
    record MemoryUsage(String id, String userID, int usage) implements ServerProtocol {}
    // Usaage in %
    record CpuUsage(String id, String userID, int usage) implements ServerProtocol {}
    // TPS 0.0 to 20.0
    record TPS(String id, String userID, int tps) implements ServerProtocol {}
    record Servers(String id, String userID, List<Server> servers) implements ServerProtocol {}
    record DeamonInformation(String id, String userID, DeamonInformation deamonInformation) implements ServerProtocol {}
}