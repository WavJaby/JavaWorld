package com.javaworld.core.jwentities;

import com.almasb.fxgl.core.math.Vec2;
import com.javaworld.core.entity.Entity;
import com.javaworld.core.entity.EntityData;
import lombok.Getter;

public class Player extends Entity implements com.javaworld.adapter.entity.Player {
    public static EntityData entityData;
    @Getter
    private final String name;
    @Getter
    private Entity grab;

    public Player(String name, Vec2 position, float direction) {
        super(entityData, position, direction);
        this.name = name;
    }
}
