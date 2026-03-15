package com.learning.api.config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class MailConfig {

    @Value("${spring.mail.username:}")
    private String from;

}