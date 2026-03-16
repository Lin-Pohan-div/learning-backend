package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.TutorSchedule;
/* import com.learning.api.repo.TutorScheduleRepo; */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TutorScheduleService {

/*     @Autowired
    private TutorScheduleRepo scheduleRepo; */

    /**
     * 切換常態時段狀態 (優化版：休息就刪除，開放才新增)
     */
    /* @Transactional
    public String toggleSchedule(ScheduleDTO.ToggleReq req) {
        // 1. 範圍檢查 (星期 1-7, 時間 9-21)
        if (req.getWeekday() < 1 || req.getWeekday() > 7 || req.getHour() < 9 || req.getHour() > 21) {
            return "格式錯誤：時間範圍需在 9~21 點之間。";
        } */

        // 2. 尋找該老師在該時段是否已有紀錄
/*         Optional<TutorSchedule> existingSlotOpt = scheduleRepo.findByTutorIdAndWeekdayAndHour(
                req.getTutorId(), req.getWeekday(), req.getHour()
        ); */

        // 3. 邏輯判斷
       /*  if ("available".equals(req.getTargetStatus())) {
            // 【目標：改為開放】
            // 如果目前沒紀錄，才需要新增
            if (existingSlotOpt.isEmpty()) {
                TutorSchedule newSlot = new TutorSchedule();
                newSlot.setTutorId(req.getTutorId());
                newSlot.setWeekday(req.getWeekday());
                newSlot.setHour(req.getHour());
                newSlot.setStatus("available");
                scheduleRepo.save(newSlot);
            }
        } else {
            // 【目標：改為休息 (取消開放)】
            // 直接把這筆紀錄從資料庫刪除，保持資料表極致精簡
            existingSlotOpt.ifPresent(slot -> scheduleRepo.delete(slot));
        }

        return "success"; */
    }

    /**
     * 取得老師的一週課表模板
     */
/*     public List<ScheduleDTO.Res> getWeeklySchedule(Long tutorId) {
        // 即使資料被刪除了也沒關係，前端對應不到資料的格子就會自動顯示為「休息」
        return scheduleRepo.findByTutorId(tutorId)
                .stream()
                .map(s -> new ScheduleDTO.Res(s.getId(), s.getWeekday(), s.getHour(), s.getStatus()))
                .collect(Collectors.toList());
    } */
