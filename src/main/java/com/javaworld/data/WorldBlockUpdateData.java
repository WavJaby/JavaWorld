package com.javaworld.data;

import com.javaworld.core.update.BlockUpdate;
import com.javaworld.core.update.UpdateType;
import com.wavjaby.serializer.processor.Serializable;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Serializable
@AllArgsConstructor
public class WorldBlockUpdateData extends WorldBlockUpdateDataSerializer {
    public final byte[] updateType;
    public final int[] blockData;

    public WorldBlockUpdateData(List<BlockUpdate> blocks) {
        blockData = new int[blocks.size() * 5];
        updateType = new byte[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            BlockUpdate update = blocks.get(i);
            updateType[i] = (byte) update.type.ordinal();
            blockData[i * 5] = update.blockX;
            blockData[i * 5 + 1] = update.blockY;
            blockData[i * 5 + 2] = update.blockZ;
            blockData[i * 5 + 3] = update.namespaceId;
            blockData[i * 5 + 4] = update.blockId;
        }
    }

    public List<BlockUpdate> toBlockUpdates() {
        List<BlockUpdate> blocks = new ArrayList<>();
        for (int i = 0; i < updateType.length; i++) {
            blocks.add(new BlockUpdate(
                    UpdateType.valueOf(updateType[i]),
                    blockData[i * 5],
                    blockData[i * 5 + 1],
                    blockData[i * 5 + 2],
                    blockData[i * 5 + 3],
                    blockData[i * 5 + 4]
            ));
        }
        return blocks;
    }
}
