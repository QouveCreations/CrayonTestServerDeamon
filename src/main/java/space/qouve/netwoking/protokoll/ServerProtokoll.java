package space.qouve.netwoking.protokoll;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import space.qouve.models.Server;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ServerProtokoll.Ack.class,   name = "ack"),
        @JsonSubTypes.Type(value = ServerProtokoll.Error.class, name = "error"),
        @JsonSubTypes.Type(value = ServerProtokoll.CreatingServer.class, name = "creatingserver"),
        @JsonSubTypes.Type(value = ServerProtokoll.UserInQue.class, name = "userinque"),
        @JsonSubTypes.Type(value = ServerProtokoll.ServerStopped.class, name = "serverstopped"),
        @JsonSubTypes.Type(value = ServerProtokoll.ServerCrashed.class, name = "servercrashed"),
        @JsonSubTypes.Type(value = ServerProtokoll.ServerStarted.class, name = "serverstarted"),
        @JsonSubTypes.Type(value = ServerProtokoll.NewConsoleLine.class, name = "newconsoleline"),
        @JsonSubTypes.Type(value = ServerProtokoll.ConsoleHistory.class, name = "consolehistory"),
        @JsonSubTypes.Type(value = ServerProtokoll.ServerRestarting.class, name = "serverrestarting"),
        @JsonSubTypes.Type(value = ServerProtokoll.FilesUpdated.class, name = "filesupdated"),
        @JsonSubTypes.Type(value = ServerProtokoll.MemoryUsage.class, name = "memoryysage"),
        @JsonSubTypes.Type(value = ServerProtokoll.CpuUsage.class, name = "cpuusage"),
        @JsonSubTypes.Type(value = ServerProtokoll.TPS.class, name = "tps"),
        @JsonSubTypes.Type(value = ServerProtokoll.Servers.class, name = "servers"),
        @JsonSubTypes.Type(value = ServerProtokoll.DeamonInformation.class, name = "deamoninformation"),
})
public sealed interface ServerProtokoll {

    record Ack(String id) implements ServerProtokoll {}
    record Error(String id, String code, String message) implements ServerProtokoll {}
    record CreatingServer(String id, String userID) implements ServerProtokoll {}
    record UserInQue(String id, String userID, int place) implements ServerProtokoll {}
    record ServerStopped(String id, String userID) implements ServerProtokoll {}
    record ServerCrashed(String id, String userID, List<String> errorLines) implements ServerProtokoll {}
    record ServerStarted(String id, String userID) implements ServerProtokoll {}
    record NewConsoleLine(String id, String userID, String line) implements ServerProtokoll {}
    record ConsoleHistory(String id, String userID, List<String> lines) implements ServerProtokoll {}
    record ServerRestarting(String id, String userID) implements ServerProtokoll {}
    record FilesUpdated(String id, String userID) implements ServerProtokoll {}
    // Usage in MB
    record MemoryUsage(String id, String userID, int usage) implements ServerProtokoll {}
    // Usaage in %
    record CpuUsage(String id, String userID, int usage) implements ServerProtokoll {}
    // TPS 0.0 to 20.0
    record TPS(String id, String userID, int tps) implements ServerProtokoll {}
    record Servers(String id, String userID, List<Server> servers) implements ServerProtokoll {}
    record DeamonInformation(String id, String userID, DeamonInformation deamonInformation) implements ServerProtokoll {}
}