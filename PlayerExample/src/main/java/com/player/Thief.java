package com.player;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.PlayerApplication;
import com.javaworld.adapter.Self;
import com.javaworld.adapter.entity.Entity;

import java.util.Arrays;

public class Thief extends PlayerApplication {
    enum State {
        MOVE_R,
        MOVE_L,
        STEAL,
        PLANT,
    }

    State state = State.MOVE_R;
    Entity tree;

    @Override
    public void init(Self self) {
        self.moveTo(new Vec2(0.5, 2.5));
    }

    @Override
    public void gameUpdate(Self self) {
        if (state == State.MOVE_R || state == State.MOVE_L) {
            if (self.isMoving()) return;
            if ((tree = findTree(self)) == null) {
                // Move to next position
                state = getMoveDir(self);
                self.moveTo(self.getPosition().add(state == State.MOVE_R ? 1 : -1, 0));
            } else {
                // Steal tree
                state = State.STEAL;
                self.moveTo(tree.getPosition());
            }
        } else if (state == State.STEAL) {
            if (self.isMoving()) return;
            self.grabEntity(tree);
            // Move down
            self.moveTo(self.getPosition().add(0, 4));
            state = State.PLANT;
        } else if (state == State.PLANT) {
            if (self.isMoving()) return;
            // Replant tree
            self.putEntity();
            self.moveTo(self.getPosition().add(0, -4));
            state = State.MOVE_R;
            state = getMoveDir(self);
        }
    }

    private Entity findTree(Self self) {
        return Arrays.stream(self.getEntities()).filter(e -> "tree".equals(e.getEntityID().getName())).findFirst().orElse(null);
    }

    private State getMoveDir(Self self) {
        if (self.getPosition().x > 31)
            return State.MOVE_L;
        else if (self.getPosition().x < 1)
            return State.MOVE_R;
        return state;
    }
}