package com.javaworld.core.entity;

import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.Namespace;

public class EntityID implements com.javaworld.adapter.entity.EntityID {
    public final Namespace namespace;
    public final String name;
    public final int id;
    public final EntityType entityType;

    public EntityID(final Namespace namespace, final String name, EntityType entityType, final int id) {
        this.namespace = namespace;
        this.name = name;
        this.id = id;
        this.entityType = entityType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityID entityID &&
                entityID.namespace.id == this.namespace.id && entityID.id == this.id;
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
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }
}
