package com.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.adapter.entity.EntityType;
import com.javaworld.core.entity.Entity;
import lombok.Getter;

public class Player extends Entity implements com.javaworld.adapter.entity.Player {
    @Getter
    private final String name;
    @Getter
    private Entity grab;

    public Player(String name, Vec2 position, float direction) {
        super(EntityType.PLAYER, position, direction);
        this.name = name;
    }
}
