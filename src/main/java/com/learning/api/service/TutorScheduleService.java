package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.learning.api.repo.TutorRepo;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TutorScheduleService {

    @Autowired
    private TutorScheduleRepo scheduleRepo;
    @Autowired 
    private TutorRepo tutorRepo;

    @Transactional
    public String toggleSchedule(ScheduleDTO.ToggleReq req) {
        if (req.getWeekday() < 1 || req.getWeekday() > 7 || req.getHour() < 9 || req.getHour() > 21) {
            return "格式錯誤：時間範圍需在 9~21 點之間。";
        }
    }else
    {
        // 【目標：改為休息 (取消開放)】
        // 直接把這筆紀錄從資料庫刪除，保持資料表極致精簡
        existingSlotOpt.ifPresent(slot -> scheduleRepo.delete(slot));
    }

        Optional<TutorSchedule> existingSlotOpt = scheduleRepo.findByTutorIdAndWeekdayAndHour(
                req.getTutorId(), req.getWeekday(), req.getHour()
        );

        if (Boolean.TRUE.equals(req.getIsAvailable())) {
            // 【目標：改為開放】
            if (existingSlotOpt.isEmpty()) {
                TutorSchedule newSlot = new TutorSchedule();
                newSlot.setTutorId(req.getTutorId());
                newSlot.setWeekday(req.getWeekday());
                newSlot.setHour(req.getHour());
                newSlot.setIsAvailable(true); // 對齊 DB
                scheduleRepo.save(newSlot);
            }
        } else {
            // 【目標：改為休息】刪除常態模板
            existingSlotOpt.ifPresent(slot -> scheduleRepo.delete(slot));
        }
        return "success";
    }

    public List<ScheduleDTO.Res> getWeeklySchedule(Long tutorId) {
        return scheduleRepo.findByTutorId(tutorId)
                .stream()
                .map(s -> new ScheduleDTO.Res(s.getId(), s.getWeekday(), s.getHour(), s.getIsAvailable()))
                .collect(Collectors.toList());
    }
}
