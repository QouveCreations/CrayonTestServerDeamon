package space.qouve.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(ZoneId.systemDefault());

    @Override
    public String format(LogRecord r) {
        StringBuilder sb = new StringBuilder();

        sb.append(TIME.format(Instant.ofEpochMilli(r.getMillis())))
                .append(" [").append(r.getLevel().getName()).append("] ")
                .append(shortName(r.getLoggerName()))
                .append(" - ")
                .append(formatMessage(r))
                .append(System.lineSeparator());

        // Stacktrace anhängen, falls eine Exception mitgegeben wurde
        if (r.getThrown() != null) {
            StringWriter sw = new StringWriter();
            r.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append(sw);
        }

        return sb.toString();
    }

    private String shortName(String name) {
        if (name == null) return "root";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1) : name;
    }
}