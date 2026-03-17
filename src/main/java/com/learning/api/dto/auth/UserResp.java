package com.learning.api.dto.auth;

import com.learning.api.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserResp {
    private Long id;

    private String name;

    private String email;

    private LocalDate birthday;

    private UserRole role;

    private Integer wallet;

    private Instant createdAt;

    private Instant updatedAt;
}
