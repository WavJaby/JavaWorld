package com.javaworld.core.update;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.entity.EntityData;
import com.javaworld.core.jwentities.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityUpdate {
    public final UpdateType type;
    public final int entitySerial;
    public final int namespaceId;
    public final int entityId;
    public final Vec2 entityPosition;
    public final float entityDirection;
    public final String playerName;

    public EntityUpdate(UpdateType type, Entity entity) {
        this.type = type;
        this.entitySerial = entity.getSerial();
        this.namespaceId = entity.entityData.namespace.id;
        this.entityId = entity.entityData.id;
        this.entityPosition = entity.getPosition();
        this.entityDirection = entity.getDirection();
        if (entity instanceof Player player)
            playerName = player.getName();
        else playerName = null;
    }

    public EntityUpdate(UpdateType type, EntityUpdate entity) {
        this.type = type;
        this.entitySerial = entity.entitySerial;
        this.namespaceId = entity.namespaceId;
        this.entityId = entity.entityId;
        this.entityPosition = entity.entityPosition;
        this.entityDirection = entity.entityDirection;
        this.playerName = entity.playerName;
    }

    public Entity toEntity() {
        EntityData entityData = EntityData.getEntityData(namespaceId, entityId);
        if (entityData.entityType == EntityType.PLAYER)
            return new Player(playerName, entityPosition, entityDirection);
        return EntityData.createEntity(entityData, entityPosition, entityDirection);
    }
}
