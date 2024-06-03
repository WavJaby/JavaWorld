package com.javaworld.core.update;

public enum UpdateType {
    CREATE,
    REMOVE,
    UPDATE,
    ;

    public static UpdateType valueOf(int order) {
        return switch (order) {
            case 0 -> CREATE;
            case 1 -> REMOVE;
            case 2 -> UPDATE;
            default -> throw new IllegalStateException("Unexpected value: " + order);
        };
    }
}
