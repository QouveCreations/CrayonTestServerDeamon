package space.qouve.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import space.qouve.models.ServerType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClientProtocol.CreateTestServer.class, name = "createtestserver"),
        @JsonSubTypes.Type(value = ClientProtocol.UpdateTestServerFiles.class, name = "updatetestserverfiles"),
        @JsonSubTypes.Type(value = ClientProtocol.StopServer.class, name = "stopserver"),
        @JsonSubTypes.Type(value = ClientProtocol.RestartServer.class, name = "restartserver"),
        @JsonSubTypes.Type(value = ClientProtocol.UpdateFiles.class, name = "updatefiles"),
        @JsonSubTypes.Type(value = ClientProtocol.GetConsole.class, name = "getconsole"),
        @JsonSubTypes.Type(value = ClientProtocol.SendCommand.class, name = "sendcommand"),
        @JsonSubTypes.Type(value = ClientProtocol.GetMemoryUsage.class, name = "getmemoryusage"),
        @JsonSubTypes.Type(value = ClientProtocol.GetCpuUsage.class, name = "getcpuusage"),
        @JsonSubTypes.Type(value = ClientProtocol.GetTPS.class, name = "gettps"),
        @JsonSubTypes.Type(value = ClientProtocol.GetServers.class, name = "getservers"),
        @JsonSubTypes.Type(value = ClientProtocol.GetDaemonInformation.class, name = "getdaemoninformation"),
})
public sealed interface ClientProtocol {
    String id();

    record CreateTestServer(String id, String userID, String repositoryID, String slug) implements ClientProtocol {}
    record UpdateTestServerFiles(String id, String userID) implements ClientProtocol {}
    record StopServer(String id, String userID) implements ClientProtocol {}
    record RestartServer(String id, String userID) implements ClientProtocol {}
    record GetConsole(String id, String userID) implements ClientProtocol {}
    record SendCommand(String id, String userID, String command) implements ClientProtocol {}

    // Production / staging
    record UpdateFiles(String id, String repositoryID, String commit, ServerType serverType) implements ClientProtocol {}

    // Metrics are pushed every second anyway, these are for one-off reads
    record GetMemoryUsage(String id, String userID) implements ClientProtocol {}
    record GetCpuUsage(String id, String userID) implements ClientProtocol {}
    record GetTPS(String id, String userID) implements ClientProtocol {}

    record GetServers(String id, String userID) implements ClientProtocol {}
    record GetDaemonInformation(String id) implements ClientProtocol {}
}