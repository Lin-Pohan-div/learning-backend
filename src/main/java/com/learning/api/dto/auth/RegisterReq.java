package com.learning.api.dto.auth;

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
    @Min(1)
    @Max(2)
    private Integer role; //1:student/2:teacher
}
