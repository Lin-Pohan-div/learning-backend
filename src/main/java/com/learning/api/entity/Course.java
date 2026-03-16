package com.learning.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
<<<<<<< HEAD
=======

>>>>>>> upstream/feature/view
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "subject", nullable = false)
    private Integer subject; // 11低年級 12中年級 13高年級 21GEPT 22YLE 23國中先修 31其他 (開頭 1: 年級課程 2檢定與升學 3其他)
    
/*     @Column(name = "level")
    private Integer level; */
    
    @Column(nullable = false, length = 200)
    private String name; // 課程名稱，如「雅思衝刺」

    /**
     * 科目代碼：
     * 年級課程 — 11: 低年級, 12: 中年級, 13: 高年級
     * 檢定升學 — 21: GEPT, 22: YLE, 23: 國中先修
     * 其他     — 31: 其他
     */
    
    @Column(nullable = false)
    private Integer subject;

    @Column(length = 1000)
    private String description; // 課程介紹

    @Column(nullable = false)
    private Integer price; // 單堂價格（元）

    @Column(name = "is_active", nullable = false)
<<<<<<< HEAD
    private Boolean active;
    /* private Boolean Active; */
=======
    private Integer active = 1; // 1: 上架, 0: 下架
>>>>>>> upstream/feature/view
}