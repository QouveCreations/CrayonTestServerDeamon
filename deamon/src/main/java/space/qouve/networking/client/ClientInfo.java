package space.qouve.networking.client;

import space.qouve.networking.ServerType;

public record ClientInfo(ServerType type, String serverId, String version) {}