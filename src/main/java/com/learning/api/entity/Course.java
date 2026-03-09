package com.learning.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "courses")
@Getter
@Setter
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_id", nullable = false)
    private Long tutorId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false)
    private Integer subject;

    @Column(nullable = false)
    private Integer level;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}