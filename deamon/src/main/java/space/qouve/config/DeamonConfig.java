package space.qouve.config;

public record DeamonConfig(int port, int maxServers, String gitBasePath, String prodSecret, String stagingSecret, String webeditorSecret) {

}