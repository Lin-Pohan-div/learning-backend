package com.learning.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
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
/*     @JsonIgnore // Course 實體與 Tutor 實體之間存在雙向關聯，避免無限迴圈 */
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 課程名稱，如「雅思衝刺」

    /**
     * 科目代碼：
     * 年級課程 — 11: 低年級, 12: 中年級, 13: 高年級
     * 檢定升學 — 21: GEPT, 22: YLE, 23: 國中先修
     * 其他 — 31: 其他
     */

    @Column(nullable = false)
    private Integer subject;

    @Column(length = 1000)
    private String description; // 課程介紹

    @Column(nullable = false)
    private Integer price; // 單堂價格（元）

    // 👉 修正：對齊 DB 的 is_active，並建議使用 Boolean 物件
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
