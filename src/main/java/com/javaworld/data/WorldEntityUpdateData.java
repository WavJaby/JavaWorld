package com.javaworld.data;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.entity.EntityData;
import com.javaworld.core.update.EntityUpdate;
import com.javaworld.core.update.UpdateType;
import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Serializable
@AllArgsConstructor
public class WorldEntityUpdateData extends WorldEntityUpdateDataSerializer {
    public final byte[] updateType;
    public final int[] entitySerials;
    // (x,y,dir)[]
    public final float[] entityPositions;
    public final int[] entityId;
    public final String[] playerName;

    public WorldEntityUpdateData(List<EntityUpdate> entities) {
        updateType = new byte[entities.size()];
        entitySerials = new int[entities.size()];
        entityPositions = new float[entities.size() * 3];
        entityId = new int[entities.size() * 2];
        List<String> playerName = new ArrayList<>();

        for (int i = 0; i < entities.size(); i++) {
            EntityUpdate update = entities.get(i);
            updateType[i] = (byte) update.type.ordinal();

            entitySerials[i] = update.entitySerial;
            Vec2 entityPos = update.entityPosition;
            entityPositions[i * 3] = entityPos.x;
            entityPositions[(i * 3) + 1] = entityPos.y;
            entityPositions[(i * 3) + 2] = update.entityDirection;
            entityId[(i << 1)] = update.namespaceId;
            entityId[(i << 1) + 1] = update.entityId;

            if (update.playerName != null)
                playerName.add(update.playerName);
        }

        this.playerName = playerName.toArray(new String[0]);
    }

    public List<EntityUpdate> toEntityUpdates() {
        List<EntityUpdate> entities = new ArrayList<>();
        int playerNameIndex = 0;
        for (int i = 0; i < entitySerials.length; i++) {
            EntityData entityData = EntityData.getEntityData(entityId[(i << 1)], entityId[(i << 1) + 1]);
            entities.add(new EntityUpdate(
                    UpdateType.valueOf(updateType[i]),
                    entitySerials[i],
                    entityData.namespace.id,
                    entityData.id,
                    new Vec2(entityPositions[i * 3], entityPositions[i * 3 + 1]),
                    entityPositions[i * 3 + 2],
                    entityData.entityType == EntityType.PLAYER ? playerName[playerNameIndex++] : null
            ));
        }
        return entities;
    }
}
