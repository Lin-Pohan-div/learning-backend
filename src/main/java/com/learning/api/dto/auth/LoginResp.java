package com.learning.api.dto.auth;

import lombok.Data;

@Data
public class LoginResp {
    private String token;
    private UserResp user;
}
