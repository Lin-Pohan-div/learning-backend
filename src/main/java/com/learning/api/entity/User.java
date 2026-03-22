package com.learning.api.entity;
import com.learning.api.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.time.LocalDate;


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

    @Column(nullable = false, length = 255)
    private String password;

    private LocalDate birthday;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable=false)
    private Integer wallet=0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}