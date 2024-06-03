package com.javaworld.core.update;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.jwentities.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityUpdate {
    public final UpdateType type;
    public final int entitySerial;
    public final Vec2 entityPosition;
    public final float entityDirection;
    public final EntityType entityType;
    public final String playerName;

    public EntityUpdate(UpdateType type, Entity entity) {
        this.type = type;
        this.entitySerial = entity.getSerial();
        this.entityPosition = entity.getPosition();
        this.entityDirection = entity.getDirection();
        this.entityType = entity.type;
        if (entity instanceof Player player)
            playerName = player.getName();
        else playerName = null;
    }
}
