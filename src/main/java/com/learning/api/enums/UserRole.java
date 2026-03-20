package com.learning.api.enums;

public enum UserRole {
    STUDENT(1),
    TUTOR(2),
    ADMIN(3);

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static UserRole fromCode(int code) {
        return switch (code) {
            case 1 -> STUDENT;
            case 2 -> TUTOR;
            case 3 -> ADMIN;
            default -> throw new IllegalArgumentException("Unknown role code: " + code);
        };
    }
}

