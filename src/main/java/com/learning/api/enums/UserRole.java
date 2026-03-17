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

    public static UserRole fromCode(int value) {
        for (UserRole role : values()) {
            if (role.code == value) return role;
        }
        throw new IllegalArgumentException("未知的 role 值: " + value);
    }
}