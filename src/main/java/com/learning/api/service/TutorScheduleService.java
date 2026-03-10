package com.learning.api.service;

import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TutorScheduleService {

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    // 老師新增排班的商業邏輯
    public String addSchedule(TutorSchedule schedule) {

        // 1. 檢查必填欄位
        if (schedule.getTutorId() == null || schedule.getWeekday() == null || schedule.getHour() == null) {
            return "資料不完整：必須提供老師ID、星期與小時";
        }

        // 2. 檢查時間合不合理
        if (schedule.getWeekday() < 1 || schedule.getWeekday() > 7) {
            return "格式錯誤：星期必須是 1 到 7";
        }
        if (schedule.getHour() < 0 || schedule.getHour() > 23) {
            return "格式錯誤：時間必須是 0 到 23";
        }

        // 3. 防呆檢查：是不是已經排過這個時間了？
        boolean isExist = scheduleRepo.existsByTutorIdAndWeekdayAndHour(
                schedule.getTutorId(), schedule.getWeekday(), schedule.getHour()
        );
        if (isExist) {
            return "排班失敗：您在這個時段已經排過班囉！";
        }

        // 4. 強制把狀態設為「可預約」，然後存檔
        schedule.setStatus("available");
        scheduleRepo.save(schedule);

        return "success";
    }
}