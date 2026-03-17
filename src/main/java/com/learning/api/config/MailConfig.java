package com.learning.api.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class MailConfig {

    // Keep startup resilient in local/dev when mail settings are not provided.
    @Value("${spring.mail.username:no-reply@local.dev}")
    private String from;

}