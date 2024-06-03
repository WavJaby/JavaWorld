package com.javaworld.core.update;

import com.javaworld.core.block.Block;
import com.javaworld.core.block.BlockData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlockUpdate {
    public final UpdateType type;
    public final int blockX;
    public final int blockY;
    public final int blockZ;
    public final int namespaceId;
    public final int blockId;

    public BlockUpdate(UpdateType updateType, Block block, BlockData newBlockData) {
        type = updateType;
        blockX = block.x;
        blockY = block.y;
        blockZ = block.z;
        namespaceId = newBlockData.namespace.id;
        blockId = newBlockData.id;
    }
}
