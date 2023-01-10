package org.trotiletre.common;

public enum CommunicationTags {
    SCOOTER_MAN(0), AUTHENTICATION_MAN(1), NOTIFICATION_MAN(2), NOTIFICATION(3);
    public final int tag;

    CommunicationTags(int tag){
        this.tag = tag;
    }
    
    public static CommunicationTags fromInt(int i){
        return switch (i){
            case 0 -> SCOOTER_MAN;
            case 1 -> AUTHENTICATION_MAN;
            case 2 -> NOTIFICATION_MAN;
            case 3 -> NOTIFICATION;
            default -> throw new IllegalStateException("Unexpected value: " + i);
        };
    }
}
