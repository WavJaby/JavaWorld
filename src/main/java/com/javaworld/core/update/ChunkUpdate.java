package com.javaworld.core.update;

import com.javaworld.core.block.BlockData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChunkUpdate {
    public final int chunkX, chunkY;
    public final BlockData[][] blocks;
}
