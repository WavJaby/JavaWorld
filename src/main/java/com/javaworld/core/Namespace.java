package com.javaworld.core;

import com.javaworld.core.block.BlockData;
import com.javaworld.core.entity.EntityData;

import java.util.ArrayList;
import java.util.List;

public class Namespace {
    public static List<Namespace> namespaceList = new ArrayList<>();

    public final int id;
    public final String name;

    private Namespace(final String name, int id) {
        this.name = name;
        this.id = id;
    }

    public static synchronized Namespace addNamespace(final String name) {
        final Namespace namespace = new Namespace(name, namespaceList.size());
        namespaceList.add(namespace);
        // Add block list for this namespace
        BlockData.createNamespaceBlockList();
        EntityData.createNamespaceEntityList();
        return namespace;
    }

    public static Namespace getNamespace(final String name) {
        for (final Namespace namespace : namespaceList) {
            if (namespace.name.equals(name))
                return namespace;
        }
        return null;
    }
}
