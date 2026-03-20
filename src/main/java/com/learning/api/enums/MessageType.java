package com.learning.api.enums;

public enum MessageType {
    TEXT(1),
    STICKER(2),
    VOICE(3),
    IMAGE(4),
    VIDEO(5),
    FILE(6);

    private final int value;

    MessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessageType fromValue(int value) {
        for (MessageType type : values()) {
            if (type.value == value) return type;
        }
        throw new IllegalArgumentException("不支援的訊息類型: " + value);
    }

    public boolean isMedia() {
        return this != TEXT;
    }
}
