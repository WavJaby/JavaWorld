package com.wavjaby.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.block.Block;
import com.wavjaby.javaworld.adapter.block.BlockID;
import com.wavjaby.javaworld.adapter.entity.Entity;
import com.wavjaby.javaworld.core.Chunk;
import com.wavjaby.javaworld.core.ServerWorld;
import com.wavjaby.javaworld.core.block.BlockData;
import lombok.Getter;

import static com.wavjaby.javaworld.adapter.block.Chunk.CHUNK_SIZE_SHIFT;

public class Self extends Player implements com.wavjaby.javaworld.adapter.Self {
    @Getter
    Vec2 dest = null;

    public Self(String name) {
        super(name, new Vec2(0, 0), 0);
    }

    @Override
    public void moveTo(Vec2 position) {
        dest = position;
    }

    @Override
    public Entity[] getEntities() {
        return ((ServerWorld) world).getEntities();
    }

    @Override
    public Block[][] getBlocks() {
        Chunk chunk = ((ServerWorld) world).getChunk((int) position.x >> CHUNK_SIZE_SHIFT, (int) position.y >> CHUNK_SIZE_SHIFT);
        if (chunk == null)
            return new Block[0][0];

        return chunk.getBlocks();
    }

    @Override
    public Block[] getBlocks(int z) {
        Chunk chunk = ((ServerWorld) world).getChunk((int) position.x >> CHUNK_SIZE_SHIFT, (int) position.y >> CHUNK_SIZE_SHIFT);
        if (chunk == null)
            return new Block[0];
        return chunk.getBlocks(z);
    }

    @Override
    public BlockID[] getBlockIDs() {
        return BlockData.getBlockIDs();
    }
}
