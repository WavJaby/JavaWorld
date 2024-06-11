package com.javaworld.client;

import com.javaworld.core.block.BlockData;
import com.javaworld.core.block.BlockState;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.update.ChunkUpdate;

public interface ClientGameEvent {
    void entityCreate(Entity e);

    void entityUpdate(Entity e);

    void entityRemove(Entity e);

    void blockCreate(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState);

    void blockRemove(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState);

    void blockUpdate(int blockX, int blockY, int blockZ, BlockData blockData, BlockState blockState);

    void chunkInit(ChunkUpdate chunkUpdate);

    void playerScoreUpdate(String[] playerNames, int[] playerScore);

    void playerLog(String log);

    void playerError(String log);
}
