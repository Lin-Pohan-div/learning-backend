package com.learning.api.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Integer> {

    @Override
    public Integer convertToDatabaseColumn(UserRole role) {
        if (role == null) return null;
        return role.getCode();
    }

    @Override
    public UserRole convertToEntityAttribute(Integer value) {
        if (value == null) return null;
        return UserRole.fromCode(value);
    }
}
