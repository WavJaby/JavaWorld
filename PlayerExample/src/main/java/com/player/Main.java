package com.player;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.Self;
import com.javaworld.adapter.World;

import java.text.DecimalFormat;

public class Main extends PlayerApplication {
    private static final DecimalFormat formatter = new DecimalFormat("0.0");
    private long time;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getTime();
        self.moveTo(new Vec2(5, 0));
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getTime() - time > 1000) {
            time = world.getTime();
            console.println(vectorFormat(self.getPosition()) + " " + self.isMoving());

//            StringBuilder builder = new StringBuilder();
//            Block[] blocks = self.getBlocks(0);
//            for (int i = 0; i < blocks.length; i++) {
//                if (i != 0 && i % Chunk.CHUNK_SIZE == 0)
//                    builder.append('\n');
//                builder.append(blocks[i].getBlockID().getId());
//            }
//            console.println(builder);
        }
    }

    private String vectorFormat(Vec2 pos) {
        return "[" + formatter.format(pos.x) + ", " + formatter.format(pos.y) + "]";
    }
}