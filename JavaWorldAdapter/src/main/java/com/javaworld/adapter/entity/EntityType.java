package com.javaworld.adapter.entity;

public enum EntityType {
    PLAYER,
    PLANT,
    ITEM,
    ENTITY,
    ;

    public static EntityType valueOf(int order) {
        return switch (order) {
            case 0 -> EntityType.PLAYER;
            case 1 -> EntityType.PLANT;
            case 2 -> EntityType.ITEM;
            case 3 -> EntityType.ENTITY;
            default -> throw new IllegalStateException("Unexpected value: " + order);
        };
    }
}
