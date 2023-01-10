package org.trotiletre.common;

public enum ManagerTag {
    SCOOTER(0), AUTHENTICATION(1), NOTIFICATION(2);
    public final int tag;

    ManagerTag(int tag){
        this.tag = tag;
    }
    
    public static ManagerTag fromInt(int i){
        return switch (i){
            case 0 -> SCOOTER;
            case 1 -> AUTHENTICATION;
            case 2 -> NOTIFICATION;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }
}
