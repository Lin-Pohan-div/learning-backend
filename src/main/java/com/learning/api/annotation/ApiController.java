package com.learning.api.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 組合注解：@RestController
 * 搭配 @RequestMapping 使用；CORS 由 SecurityConfig 全域處理
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface ApiController {
}
