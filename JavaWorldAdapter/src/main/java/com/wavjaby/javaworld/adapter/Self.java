package com.wavjaby.javaworld.adapter;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.block.Block;
import com.wavjaby.javaworld.adapter.block.BlockID;
import com.wavjaby.javaworld.adapter.entity.Entity;
import com.wavjaby.javaworld.adapter.entity.Player;

public interface Self extends Player {
    void moveTo(Vec2 position);

    Entity[] getEntities();

    Block[][] getBlocks();

    Block[] getBlocks(int z);

    BlockID[] getBlockIDs();
}
