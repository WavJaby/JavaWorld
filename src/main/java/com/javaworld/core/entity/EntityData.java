package com.javaworld.core.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.Namespace;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EntityData extends EntityID {
    private static final Logger logger = Logger.getLogger(EntityData.class.getSimpleName());

    private static final List<List<EntityData>> entities = new ArrayList<>();
    private static final List<Map<String, EntityData>> entityNames = new ArrayList<>();

    private final Constructor<? extends Entity> entityHandler;

    public EntityData(final Namespace namespace, final String name, final int id, EntityType entityType, final Class<? extends Entity> entityHandler) {
        super(namespace, name, entityType, id);
        if (entityType == EntityType.PLAYER) {
            this.entityHandler = null;
            return;
        }

        // Unknown entity
        if (entityHandler == null) {
            this.entityHandler = null;
            logger.log(Level.SEVERE, "Could not find constructor for entity: " + this);
            return;
        }

        Constructor<? extends Entity> entityHandlerConstructor = null;
        try {
            entityHandlerConstructor = entityHandler.getDeclaredConstructor(EntityData.class, Vec2.class, float.class);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Could not find constructor for entity: " + this + " -> " + entityHandler.getName(), e);
        }
        this.entityHandler = entityHandlerConstructor;
    }

    public static synchronized void createNamespaceEntityList() {
        entities.add(new ArrayList<>());
        entityNames.add(new HashMap<>());
    }

    public static synchronized EntityData createEntityData(final Namespace namespace, final String entityName,
                                                           EntityType entityType, final Class<? extends Entity> entityHandler) {
        List<EntityData> namespaceEntities = entities.get(namespace.id);
        EntityData entityData = new EntityData(namespace, entityName, namespaceEntities.size(), entityType, entityHandler);
        namespaceEntities.add(entityData);
        entityNames.get(namespace.id).put(entityName, entityData);
        return entityData;
    }

    public static EntityData getEntityData(final int namespaceId, final String entityName) {
        return entityNames.get(namespaceId).get(entityName);
    }

    public static EntityData getEntityData(int namespaceId, int entityId) {
        return entities.get(namespaceId).get(entityId);
    }

    public static Entity createEntity(EntityData entityData, Vec2 position, float direction) {
        try {
            return entityData.entityHandler.newInstance(entityData, position, direction);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public static EntityID[] getEntities() {
        List<EntityID> out = new ArrayList<>(entityNames.size());
        for (Map<String, EntityData> entityID : entityNames) {
            out.addAll(entityID.values());
        }
        return out.toArray(new EntityID[0]);
    }
}
