package space.qouve.protocol;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClientProtocol.CreateTestServer.class, name = "command"),
        @JsonSubTypes.Type(value = ClientProtocol.GetConsole.class, name = "getconsole"),
        @JsonSubTypes.Type(value = ClientProtocol.StopServer.class, name = "stopserver"),
        @JsonSubTypes.Type(value = ClientProtocol.RestartServer.class, name = "restartserver"),
        @JsonSubTypes.Type(value = ClientProtocol.UpdateFiles.class, name = "updatefiles"),
        @JsonSubTypes.Type(value = ClientProtocol.SendCommand.class, name = "sendcommand"),
        @JsonSubTypes.Type(value = ClientProtocol.GetMemoryUsage.class, name = "getmemoryusage"),
        @JsonSubTypes.Type(value = ClientProtocol.GetCpuUsage.class, name = "getcpuusage"),
        @JsonSubTypes.Type(value = ClientProtocol.GetTPS.class, name = "gettps"),
        @JsonSubTypes.Type(value = ClientProtocol.GetServers.class, name = "getservers"),
        @JsonSubTypes.Type(value = ClientProtocol.GetDeamonInformation.class, name = "getdeamoninformation"),
})
public sealed interface ClientProtocol {
    String id();

    record CreateTestServer(String id, String userID, String repositoryID, String slug) implements ClientProtocol {}
    record GetConsole(String id, String userID) implements ClientProtocol {}
    record StopServer(String id, String userID) implements ClientProtocol {}
    record RestartServer(String id, String userID) implements ClientProtocol {}
    record UpdateFiles(String id, String userID) implements ClientProtocol {}
    record SendCommand(String id, String userID) implements ClientProtocol {}

    // Usages will send automaticlly every 1sek, so get is not really needed.
    record GetMemoryUsage(String id, String userID) implements ClientProtocol {}
    record GetCpuUsage(String id, String userID) implements ClientProtocol {}
    record GetTPS(String id, String userID) implements ClientProtocol {}

    record GetServers(String id, String userID) implements ClientProtocol {}
    record GetDeamonInformation(String id) implements ClientProtocol {}
}