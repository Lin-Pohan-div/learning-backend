package com.learning.api.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserRole role) {
        return role == null ? null : role.getCode();  // ← 用 getCode()
    }

    @Override
    public UserRole convertToEntityAttribute(Integer value) {
        return value == null ? null : UserRole.fromCode(value);  // ← 用 fromCode()
    }
}