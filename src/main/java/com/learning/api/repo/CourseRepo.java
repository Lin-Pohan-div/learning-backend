package com.learning.api.repo;

import com.learning.api.dto.CourseSearchDTO;
import com.learning.api.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    /**
     * 透過老師的 ID 尋找該名老師開設的所有課程
     * Spring Data JPA 會自動解析為：SELECT * FROM courses WHERE tutor_id = ?
     */
    List<Course> findByTutorId(Long tutorId);

    /**
     * (選填) 如果你想確保顯示順序，例如按價格從低到高
     */
    List<Course> findByTutorIdOrderByPriceAsc(Long tutorId);

    // 正確的：透過關聯的 Tutor entity 主鍵查詢
    List<Course> findByTutor_Id(Long tutorId);

    // =========================================================================
    // 暫時用Service處理 此處註解 JPQL：動態組裝前端需要的課程卡片 (結合 Course, Tutor, User 三張表)
    // =========================================================================
    //    @Query("SELECT new com.learning.api.dto.CourseSearchDTO(" +
    //            "c.id, t.id, u.name, t.avatar, t.title, c.name, c.subject, c.description, c.price) " +
    //            "FROM Course c JOIN c.tutor t JOIN t.user u " +
    //            "WHERE c.isActive = true")
    //    List<CourseSearchDTO> findAllActiveCourseCards();

}