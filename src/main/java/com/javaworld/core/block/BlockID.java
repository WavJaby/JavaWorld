package com.javaworld.core.block;

import com.javaworld.core.Namespace;

public class BlockID implements com.javaworld.adapter.block.BlockID {
    public final Namespace namespace;
    public final String name;
    public final int id;

    public BlockID(final Namespace namespace, final String name, final int id) {
        this.namespace = namespace;
        this.name = name;
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BlockID blockID &&
                blockID.namespace.id == this.namespace.id && blockID.id == this.id;
    }

    @Override
    public int hashCode() {
        return (this.namespace.id << 16) | this.id;
    }

    @Override
    public String toString() {
        return namespace.name + ":" + name;
    }

    @Override
    public String getFullName() {
        return namespace.name + ":" + name;
    }

    @Override
    public int getId() {
        return id;
    }
}
