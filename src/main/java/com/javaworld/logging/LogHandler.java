package com.javaworld.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

public class LogHandler extends StreamHandler {
    public LogHandler() {
        super(System.out, new LogFormatter());
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getLevel() == Level.SEVERE && record.getThrown() != null) {
            record.getThrown().printStackTrace(System.err);
            System.err.flush();
            return false;
        }
        return super.isLoggable(record);
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public void close() {
        flush();
    }
}
