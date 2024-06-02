package com.wavjaby.javaworld.logging;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        String err = "\n";
        if (record.getThrown() != null)
            err += record.getThrown().toString() + "\n";
        Instant instant = Instant.ofEpochMilli(record.getMillis());


        return record.getLevel().getName() + " " +
                record.getLoggerName() + ":" +
                formatter.format(instant.atZone(ZoneId.systemDefault())) + ": " +
                record.getMessage() +
                err;
    }
}
