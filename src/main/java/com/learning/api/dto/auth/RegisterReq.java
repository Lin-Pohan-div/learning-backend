package com.learning.api.dto.auth;

import com.learning.api.enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterReq {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8)
    private String password;

    @Past
    private LocalDate birthday;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserRole role; //1:student/2:teacher
}
