package org.trotiletre.common;

public enum NotificationOperations {
    REGISTER(0), IS_REGISTERED(1), ADD_LOCATION(2), REMOVE(3);
    public final int operationTag;

    NotificationOperations(int operationTag) {
        this.operationTag = operationTag;
    }

    public static NotificationOperations fromInt(int i) {
        return switch (i) {
            case 0 -> REGISTER;
            case 1 -> IS_REGISTERED;
            case 2 -> ADD_LOCATION;
            case 3 -> REMOVE;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }
}