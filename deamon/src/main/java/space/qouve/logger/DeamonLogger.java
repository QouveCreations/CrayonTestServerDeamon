package space.qouve.logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.logging.*;

public final class DeamonLogger {

    private static final Logger LOGGER = Logger.getLogger("deamon");

    static {
        try {
            Files.createDirectories(Path.of("logs"));

            LogFormatter formatter = new LogFormatter();

            FileHandler fileHandler = new FileHandler("logs/deamon-%g.log", 5_000_000, 10, true);
            fileHandler.setFormatter(formatter);
            fileHandler.setLevel(Level.ALL);

            StreamHandler console = new StreamHandler(System.out, formatter) {
                @Override
                public synchronized void publish(LogRecord record) {
                    super.publish(record);
                    flush();
                }
            };
            console.setLevel(Level.INFO);

            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(fileHandler);
            LOGGER.addHandler(console);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DeamonLogger() {}

    public static Logger get() {
        return LOGGER;
    }
}