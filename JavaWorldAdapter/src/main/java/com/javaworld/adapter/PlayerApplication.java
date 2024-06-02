package com.javaworld.adapter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public abstract class PlayerApplication {
    public final ByteArrayOutputStream out;
    public final ByteArrayOutputStream err;
    public final PrintStream console;
    public final PrintStream error;

    public PlayerApplication() {
        out = new ByteArrayOutputStream();
        err = new ByteArrayOutputStream();
        console = new PrintStream(out);
        error = new PrintStream(err);
    }

    public boolean isNotInterrupt() {
        if (Thread.currentThread().isInterrupted())
            throw new RuntimeException("Code execution timed out");
        return true;
    }

    public abstract void init(Self self);

    public abstract void gameUpdate(Self self);
}
