package com.learning.api.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.learning.api.entity.TutorSchedule;


@Repository
public interface TutorScheduleRepo extends JpaRepository<TutorSchedule, Long> {
    // 透過 Spring Data 的命名慣例自動生成查詢：找特定老師、按星期排序、再按小時排序
    List<TutorSchedule> findByTutorIdOrderByWeekdayAscHourAsc(Long tutorId);
    
	// 方法名必須對應實體變數名 user，Spring 會自動提取其 ID 進行查詢
    List<TutorSchedule> findByTutorId(Long tutorId);
}
