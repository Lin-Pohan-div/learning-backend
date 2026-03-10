package com.learning.api.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "discount_price", nullable = false)
    private Integer discountPrice;

    @Column(name = "lesson_count", nullable = false)
    private Integer lessonCount;

    @Column(name = "lesson_used", nullable = false)
<<<<<<< HEAD
    private Integer lessonused;
    
    @Column(name = "status", nullable = false)
    private Byte status;
=======
    private Integer lessonUsed;
    
    @Column(name = "status", nullable = false)
    private Integer status;
>>>>>>> 62f94148526ad4d50cad68cd7af090cfea14d7c0
}