package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 64)
    private String password;

    private LocalDate birthday;

    @Column(nullable = false)
    private Integer role;

    @Column(name = "is_admin", nullable = false)
    private Byte isAdmin;

    @Column(nullable = false)
    private Integer wallet;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
}