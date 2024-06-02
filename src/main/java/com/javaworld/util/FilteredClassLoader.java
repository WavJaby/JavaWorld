package com.javaworld.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class FilteredClassLoader extends URLClassLoader {
    private final String playerClassName;

    public FilteredClassLoader(URL[] urls, ClassLoader parent, String playerClassName) throws MalformedURLException {
        super(playerClassName, urls, parent);
        this.playerClassName = playerClassName;
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//        System.out.println("loading_: " + name);
        if (classFilter(name))
            return super.loadClass(name, resolve);

        throw new IllegalAccessError("Using '" + name + "' is not allowed");
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
//        System.out.println("loading: " + name);
        if (classFilter(name))
            return super.loadClass(name);

        throw new IllegalAccessError("Using '" + name + "' is not allowed");
    }

    boolean classFilter(String name) {
        if (name.startsWith("java.lang.")) {
            if (name.startsWith("System", 10))
                throw new IllegalAccessError("Using '" + name + "' is not allowed, use 'console' instead");
            else if (name.startsWith("reflect", 10) ||
                    name.startsWith("Process", 10) ||
                    name.startsWith("Thread", 10) ||
                    name.startsWith("Runnable", 10) ||
                    name.startsWith("Security", 10) ||
                    name.startsWith("Runtime", 10) ||
                    name.startsWith("ClassLoader", 10) ||
                    name.startsWith("*", 10))
                return false;
            return true;
        }
        if (name.startsWith(playerClassName) ||
                name.startsWith("com.javaworld.adapter") ||
                name.startsWith("com.almasb.fxgl") ||
                name.startsWith("java.text") ||
                name.startsWith("java.util") ||
                name.equals("java.io.PrintStream") ||
                name.equals("java.io.Serializable")
        ) return true;

        return false;
    }
}
