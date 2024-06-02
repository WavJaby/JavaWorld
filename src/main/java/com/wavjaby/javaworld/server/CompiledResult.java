package com.wavjaby.javaworld.server;

import com.wavjaby.javaworld.adapter.PlayerApplication;
import com.wavjaby.javaworld.util.FilteredClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static com.wavjaby.javaworld.server.LiveCompiler.deleteTempFolder;

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
    public final ByteArrayOutputStream out;
    public final ByteArrayOutputStream err;
    public final boolean success;
    public final ErrorCode code;
    public final String message;

    public CompiledResult(ErrorCode code, String message) {
        this.success = false;
        this.code = code;
        this.message = message;

        this.classloader = null;
        this.playerApplication = null;
        this.out = null;
        this.err = null;
        this.tempDir = null;
    }

    public CompiledResult(String message, FilteredClassLoader classloader, PlayerApplication playerApplication,
                          ByteArrayOutputStream out, ByteArrayOutputStream err, File tempDir) {
        this.success = true;
        this.code = null;
        this.message = message;
        this.classloader = classloader;
        this.playerApplication = playerApplication;
        this.out = out;
        this.err = err;
        this.tempDir = tempDir;
    }

    @Override
    public void close() throws IOException {
        if (out != null) out.close();
        if (classloader != null) classloader.close();
        deleteTempFolder(this.tempDir);
    }
}
