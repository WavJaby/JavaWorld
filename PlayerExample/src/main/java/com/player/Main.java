package com.player;

import com.almasb.fxgl.core.math.Vec2;
import com.wavjaby.javaworld.adapter.PlayerApplication;
import com.wavjaby.javaworld.adapter.Self;
import com.wavjaby.javaworld.adapter.World;
import com.wavjaby.javaworld.adapter.block.Block;
import com.wavjaby.javaworld.adapter.block.Chunk;

public class Main extends PlayerApplication {
    long time;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getTime();
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getTime() - time > 1000) {
            time = world.getTime();

            self.moveTo(new Vec2(5, 5));
            console.println(self.getPosition() + " " + self.isMoving());

            StringBuilder builder = new StringBuilder();
            Block[][] blocks = self.getBlocks();
            for (int i = 0; i < blocks[0].length; i++) {
                if (i != 0 && i % Chunk.CHUNK_SIZE == 0)
                    builder.append('\n');
                builder.append(blocks[0][i].getBlockID().getId());
            }
            console.println(builder);
        }
    }
}