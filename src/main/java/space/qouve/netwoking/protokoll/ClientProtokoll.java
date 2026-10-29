package space.qouve.netwoking.protokoll;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClientProtokoll.CreateTestServer.class, name = "command"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetConsole.class, name = "getconsole"),
        @JsonSubTypes.Type(value = ClientProtokoll.StopServer.class, name = "stopserver"),
        @JsonSubTypes.Type(value = ClientProtokoll.RestartServer.class, name = "restartserver"),
        @JsonSubTypes.Type(value = ClientProtokoll.UpdateFiles.class, name = "updatefiles"),
        @JsonSubTypes.Type(value = ClientProtokoll.SendCommand.class, name = "sendcommand"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetMemoryUsage.class, name = "getmemoryusage"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetCpuUsage.class, name = "getcpuusage"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetTPS.class, name = "gettps"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetServers.class, name = "getservers"),
        @JsonSubTypes.Type(value = ClientProtokoll.GetDeamonInformation.class, name = "getdeamoninformation"),
})
public sealed interface ClientProtokoll {
    String id();

    record CreateTestServer(String id, String userID, String repositoryID) implements ClientProtokoll {}
    record GetConsole(String id, String userID) implements ClientProtokoll {}
    record StopServer(String id, String userID) implements ClientProtokoll {}
    record RestartServer(String id, String userID) implements ClientProtokoll {}
    record UpdateFiles(String id, String userID) implements ClientProtokoll {}
    record SendCommand(String id, String userID) implements ClientProtokoll {}

    // Usages will send automaticlly every 1sek, so get is not really needed.
    record GetMemoryUsage(String id, String userID) implements ClientProtokoll {}
    record GetCpuUsage(String id, String userID) implements ClientProtokoll {}
    record GetTPS(String id, String userID) implements ClientProtokoll {}

    record GetServers(String id, String userID) implements ClientProtokoll {}
    record GetDeamonInformation(String id) implements ClientProtokoll {}
}