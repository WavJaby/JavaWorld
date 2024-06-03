package com.javaworld.data;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.update.EntityUpdate;
import com.javaworld.core.update.UpdateType;
import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Serializable
@AllArgsConstructor
public class WorldEntityUpdate extends WorldEntityUpdateSerializer {
    public final byte[] updateType;
    public final int[] entitySerials;
    // (x,y)[]
    public final float[] entityPositions;
    public final float[] entityDirection;
    public final byte[] entityType;
    public final String[] playerName;

    public WorldEntityUpdate(List<EntityUpdate> entities) {
        updateType = new byte[entities.size()];
        entitySerials = new int[entities.size()];
        entityPositions = new float[entities.size() * 2];
        entityDirection = new float[entities.size()];
        entityType = new byte[entities.size()];
        List<String> playerName = new ArrayList<>();

        for (int i = 0; i < entities.size(); i++) {
            EntityUpdate update = entities.get(i);
            updateType[i] = (byte) update.type.ordinal();

            entitySerials[i] = update.entitySerial;
            Vec2 entityPos = update.entityPosition;
            entityPositions[i << 1] = entityPos.x;
            entityPositions[(i << 1) + 1] = entityPos.y;
            entityDirection[i] = update.entityDirection;
            entityType[i] = (byte) update.entityType.ordinal();
            if (update.playerName != null)
                playerName.add(update.playerName);
        }

        this.playerName = playerName.toArray(new String[0]);
    }

    public List<EntityUpdate> toEntityUpdates() {
        List<EntityUpdate> entities = new ArrayList<>();
        int playerNameIndex = 0;
        for (int i = 0; i < entitySerials.length; i++) {
            EntityType type = EntityType.valueOf(entityType[i]);
            entities.add(new EntityUpdate(
                    UpdateType.valueOf(updateType[i]),
                    entitySerials[i],
                    new Vec2(entityPositions[i << 1], entityPositions[(i << 1) + 1]),
                    entityDirection[i],
                    type,
                    type == EntityType.PLAYER ? playerName[playerNameIndex++] : null
            ));
        }
        return entities;
    }
}
