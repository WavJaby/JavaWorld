package com.player;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.Self;
import com.javaworld.adapter.World;

import java.text.DecimalFormat;

public class Main extends PlayerApplication {
    private static final DecimalFormat formatter = new DecimalFormat("0.0");
    private long time;

    int i = 0;

    enum State {
        MOVE,
        HOE,
        PLANT,
    }

    State state = State.MOVE;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getWorldTime();
        self.moveTo(new Vec2(0.5, 5));
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getWorldTime() - time >= 20) {
            time = world.getWorldTime();
            console.println(vectorFormat(self.getPosition()) + " " + self.isMoving() + " " + self.isHoeingBlock() + " " + self.isPlanting());
        }

        if (state == State.MOVE) {
            if (!self.isMoving() && !self.isPlanting()) {
                self.moveTo(new Vec2(++i + 0.5f, 0));
                state = State.HOE;
            }
        } else if (state == State.HOE) {
            if (!self.isMoving()) {
                self.hoeBlock();
                state = State.PLANT;
            }
        } else if (state == State.PLANT) {
            if (!self.isHoeingBlock()) {
                self.plantTree();
                state = State.MOVE;
            }
        }
    }

    private String vectorFormat(Vec2 pos) {
        return "[" + formatter.format(pos.x) + ", " + formatter.format(pos.y) + "]";
    }
}