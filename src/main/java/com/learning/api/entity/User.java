package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Data;
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

    @Column(nullable = false ,name="is_admin")
    private Boolean IsAdmin;

    @Column(nullable=false)
    private Integer wallet;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    

}