package space.qouve;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import space.qouve.config.DeamonConfig;
import space.qouve.logger.DeamonLogger;
import space.qouve.networking.CrayonWebSocketServer;
import space.qouve.networking.handler.Dispatcher;
import space.qouve.networking.TokenAuth;
import space.qouve.networking.handler.handlers.CreateTestServerFromCommit;
import space.qouve.protocol.ClientProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Main {
    private static final Logger log = DeamonLogger.get();
    private static final Path CONFIG_PATH = Path.of("config.yml");
    private static DeamonConfig config;
    private static Dispatcher dispatcher;
    private static TokenAuth auth;
    private static CrayonWebSocketServer socketServer;

    static void main() throws IOException {
        config = loadConfig();

        log.info("Starting NetWoking Server...");
        dispatcher = new Dispatcher();
        dispatcher.register(ClientProtocol.CreateTestServerFromCommit.class, new CreateTestServerFromCommit());
        auth = new TokenAuth(config.prodSecret(), config.stagingSecret(), config.webeditorSecret());
        socketServer = new CrayonWebSocketServer(config.port(), dispatcher, auth);
        socketServer.start();
        log.info("NetWoking Server Started");
    }

    private static DeamonConfig loadConfig() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            try (InputStream in = Main.class.getResourceAsStream("/config.yml")) {
                Files.copy(in, CONFIG_PATH);
            }
            log.info("config.yml not found, creating default config.");
        }
        ObjectMapper mapper = new YAMLMapper();
        return mapper.readValue(CONFIG_PATH.toFile(), DeamonConfig.class);
    }

    public static DeamonConfig getConfig() {
        return config;
    }

    public static Dispatcher getDispatcher() {
        return dispatcher;
    }

    public static TokenAuth getAuth() {
        return auth;
    }

    public static CrayonWebSocketServer getSocketServer() {
        return socketServer;
    }
}