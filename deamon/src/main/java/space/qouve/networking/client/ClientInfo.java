package space.qouve.networking.client;

import space.qouve.models.ServerType;

public record ClientInfo(ServerType type, String serverId, String version) {}