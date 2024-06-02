package com.wavjaby.javaworld.core.entity;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.entity.EntityType;
import com.wavjaby.javaworld.core.Namespace;

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
    private static final List<Map<String, EntityID>> entityIDs = new ArrayList<>();

    private final Constructor<? extends Entity> entityHandler;

    public EntityData(final Namespace namespace, final String name, final int id, final Class<? extends Entity> entityHandler) {
        super(namespace, name, id);
        // Air entity
        if (id == 0) {
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
            entityHandlerConstructor = entityHandler.getDeclaredConstructor(
                    EntityType.class, Vec2.class, float.class);
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Could not find constructor for entity: " + this + " -> " + entityHandler.getName(), e);
        }
        this.entityHandler = entityHandlerConstructor;
    }

    public static synchronized void createNamespaceEntityList() {
        entities.add(new ArrayList<>());
        entityIDs.add(new HashMap<>());
    }

    public static synchronized EntityData createEntityData(final Namespace namespace, final String entityName,
                                                           final Class<? extends Entity> entityHandler) {
        List<EntityData> namespaceEntities = entities.get(namespace.id);
        EntityData entityData = new EntityData(namespace, entityName, namespaceEntities.size(), entityHandler);
        namespaceEntities.add(entityData);
        entityIDs.get(namespace.id).put(entityName, entityData);
        return entityData;
    }

    public static EntityData getEntityData(EntityID entityID) {
        return entities.get(entityID.namespace.id).get(entityID.id);
    }

    public static EntityID getEntityID(final Namespace namespace, final String entityName) {
        return entityIDs.get(namespace.id).get(entityName);
    }

    public static Entity createEntity(EntityData entityData, EntityType type, Vec2 position, float direction) {
        if (entityData.entityHandler == null)
            return null;
        try {
            return entityData.entityHandler.newInstance(type, position, direction);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    public static EntityID[] getEntityIDs() {
        List<EntityID> out = new ArrayList<>(entityIDs.size());
        for (Map<String, EntityID> entityID : entityIDs) {
            out.addAll(entityID.values());
        }
        return out.toArray(new EntityID[0]);
    }
}
