package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(nullable = false)
    private Integer role; //1:student/2:teacher/3admin

    @Column(nullable=false)
    private Integer wallet=0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}