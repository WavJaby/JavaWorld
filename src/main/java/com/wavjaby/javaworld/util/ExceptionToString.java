package com.wavjaby.javaworld.util;

import com.wavjaby.javaworld.server.CompiledResult;

public abstract class ExceptionToString {
    public static String exceptionToString(String classLoaderName, Throwable e) {
        StringBuilder builder = new StringBuilder();
        builder.append(e).append('\n');
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace)
            if (classLoaderName.equals(traceElement.getClassLoaderName()))
                builder.append("    at ").append(traceElement).append('\n');

        return builder.toString();
    }

    public static void exceptionToErrorPrintStream(CompiledResult compiled, Throwable e) {
        compiled.playerApplication.error.println(e);
        StackTraceElement[] trace = e.getStackTrace();
        for (StackTraceElement traceElement : trace)
            if (compiled.classloader.getName().equals(traceElement.getClassLoaderName()))
                compiled.playerApplication.error.println("    at " + traceElement);
    }
}
