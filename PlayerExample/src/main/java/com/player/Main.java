package com.player;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.Self;
import com.javaworld.adapter.World;

import java.text.DecimalFormat;

public class Main extends PlayerApplication {
    private static final DecimalFormat formatter = new DecimalFormat("0.0");
    private long time;

    enum State {
        MOVE,
        HOE,
        PLANT,
    }

    State state = State.MOVE;
    boolean moveR = true;

    @Override
    public void init(Self self) {
        World world = self.getWorld();
        time = world.getWorldTime();
        self.moveTo(new Vec2(0.5, 2.5));
    }

    @Override
    public void gameUpdate(Self self) {
        World world = self.getWorld();
        if (world.getWorldTime() - time >= 20) {
            time = world.getWorldTime();
            console.println(vectorFormat(self.getPosition()) + " " + self.isMoving() + " " + self.isHoeingBlock() + " " + self.isPlanting());
        }

        if (state == State.MOVE) {
            // Move to next position
            if (!self.isMoving() && !self.isPlanting()) {
                Vec2 next = self.getPosition().add(moveR ? 1 : -1, 0);
                if (next.x > 31 || next.x < 1) {
                    moveR = !moveR;
                    self.moveTo(self.getPosition().add(0, 1));
                    return;
                }
                self.moveTo(next);
                state = State.HOE;
            }
        } else if (state == State.HOE) {
            // Hoeing block
            if (!self.isMoving()) {
                self.hoeBlock();
                state = State.PLANT;
            }
        } else if (state == State.PLANT) {
            // Plant tree
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