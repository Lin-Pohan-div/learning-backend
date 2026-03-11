package com.learning.api.repo;

import com.learning.api.entity.TutorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TutorScheduleRepo extends JpaRepository<TutorSchedule, Long> {

    // 找出某位老師所有的排班時段
    List<TutorSchedule> findByTutorId(Long tutorId);

    // 檢查該老師在「星期幾的幾點」是不是已經排過班了 (防呆機制)
    boolean existsByTutorIdAndWeekdayAndHour(Long tutorId, Byte weekday, Byte hour);
}