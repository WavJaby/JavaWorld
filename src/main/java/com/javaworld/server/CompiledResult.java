package com.javaworld.server;

import com.javaworld.adapter.PlayerApplication;
import com.javaworld.util.FilteredClassLoader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static com.javaworld.server.LiveCompiler.deleteTempFolder;

public class CompiledResult implements Closeable {

    public enum ErrorCode {
        COMPILE_FAILED,
        BAD_SOURCECODE,
        SERVER_ERROR,
        METHOD_NOT_ALLOWED_IN_COMPILE,
    }

    private final File tempDir;

    public final FilteredClassLoader classloader;
    public final PlayerApplication playerApplication;
    public final boolean success;
    public final ErrorCode code;
    public final String message;

    public CompiledResult(ErrorCode code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;

        this.classloader = null;
        this.playerApplication = null;
        this.tempDir = null;
    }

    public CompiledResult(String message, FilteredClassLoader classloader, PlayerApplication playerApplication, File tempDir) {
        this.success = true;
        this.code = null;
        this.message = message;
        this.classloader = classloader;
        this.playerApplication = playerApplication;
        this.tempDir = tempDir;
    }

    @Override
    public void close() throws IOException {
        if (classloader != null) classloader.close();
        deleteTempFolder(this.tempDir);
    }
}
