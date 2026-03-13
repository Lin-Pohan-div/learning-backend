package com.learning.api.repo;

import com.learning.api.entity.TutorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TutorScheduleRepo extends JpaRepository<TutorSchedule, Long> {

    // 查詢老師的「整週常態課表模板」
    List<TutorSchedule> findByTutorId(Long tutorId);

    // 精準尋找老師在「星期幾的幾點」的紀錄 (用來檢查要 Update 還是 Insert)
    Optional<TutorSchedule> findByTutorIdAndWeekdayAndHour(Long tutorId, Integer weekday, Integer hour);
}