package com.wavjaby.javaworld.core;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.entity.EntityType;
import com.wavjaby.javaworld.core.entity.Entity;
import com.wavjaby.javaworld.core.jwentities.Player;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EntityUpdate {
    public enum UpdateType {
        CREATE,
        REMOVE,
        UPDATE,
        ;

        public static UpdateType valueOf(int order) {
            return switch (order) {
                case 0 -> UpdateType.CREATE;
                case 1 -> UpdateType.REMOVE;
                case 2 -> UpdateType.UPDATE;
                default -> throw new IllegalStateException("Unexpected value: " + order);
            };
        }
    }

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
