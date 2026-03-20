package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.enums.UserRole;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.security.JwtService;
import com.learning.api.security.SecurityUser;

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
    private JwtService jwtService;

    // @Transactional
    // public String toggleSchedule(ScheduleDTO.ToggleReq req) {
    //     Optional<TutorSchedule> existingSlotOpt = scheduleRepo.findByTutorIdAndWeekdayAndHour(
    //             req.getTutorId(), req.getWeekday(), req.getHour());
    //     if (req.getWeekday() < 1 || req.getWeekday() > 7 || req.getHour() < 9 || req.getHour() > 21) {
    //         return "格式錯誤：時間範圍需在 9~21 點之間。";
    //     } else {
    //         // 【目標：改為休息 (取消開放)】
    //         // 直接把這筆紀錄從資料庫刪除，保持資料表極致精簡
    //         existingSlotOpt.ifPresent(slot -> scheduleRepo.delete(slot));
    //     }

    //     if (Boolean.TRUE.equals(req.getIsAvailable())) {
    //         // 【目標：改為開放】
    //         if (existingSlotOpt.isEmpty()) {
    //             TutorSchedule newSlot = new TutorSchedule();
    //             newSlot.setId(req.getTutorId());
    //             newSlot.setWeekday(req.getWeekday());
    //             newSlot.setHour(req.getHour());
    //             newSlot.setIsAvailable(true); // 對齊 DB
    //             scheduleRepo.save(newSlot);
    //         }
    //     } else {
    //         // 【目標：改為休息】刪除常態模板
    //         existingSlotOpt.ifPresent(slot -> scheduleRepo.delete(slot));
    //     }
    //     return "success";
    // }
     @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired
    private TutorRepo tutorRepo;

    @Transactional
    public String batchToggle(ScheduleDTO.BatchToggleReq req) {

        for (ScheduleDTO.Slot slot : req.getSlots()) {

            Integer weekday = slot.getWeekday();
            Integer hour = slot.getHour();

            // 1️⃣ 基本驗證（🔥你要求的 1~7 / 9~21）
            if (weekday == null || weekday < 1 || weekday > 7 ||
                hour == null || hour < 9 || hour > 21) {
            return "格式錯誤：時間範圍需在 9~21 點之間。";
            }

            Optional<TutorSchedule> existingSlotOpt =
                scheduleRepo.findByTutorIdAndWeekdayAndHour(
                    req.getTutorId(), weekday, hour
                );

            if (Boolean.TRUE.equals(slot.getIsAvailable())) {
                // ✅ 開放 → 沒有才新增
                if (existingSlotOpt.isEmpty()) {
                    TutorSchedule newSlot = new TutorSchedule();

                    newSlot.setTutor(tutorRepo.getReferenceById(req.getTutorId()));
                    newSlot.setWeekday(weekday);
                    newSlot.setHour(hour);
                    newSlot.setIsAvailable(true);

                    scheduleRepo.save(newSlot);
                }

            } else {
                // ❌ 關閉 → 有就刪除
                existingSlotOpt.ifPresent(scheduleRepo::delete);
            }
           
        } 
        return "success";
    }

@Transactional
public String batchToggle(ScheduleDTO.BatchToggleReq req, SecurityUser me) {
    UserRole role = me.getUser().getRole();  
    Long tutorId = me.getUser().getId();



    // 只允許老師操作自己的課表
    if (role != UserRole.TUTOR) {
        return "不是老師無法操作";
    }


    for (ScheduleDTO.Slot slot : req.getSlots()) {
        Integer weekday = slot.getWeekday();
        Integer hour = slot.getHour();

        if (weekday == null || weekday < 1 || weekday > 7 ||
            hour == null || hour < 9 || hour > 21) {
            return "格式錯誤：時間範圍需在 9~21 點之間。";
        }

        Optional<TutorSchedule> existingSlotOpt =
            scheduleRepo.findByTutorIdAndWeekdayAndHour(
                tutorId, weekday, hour
            );

        if (Boolean.TRUE.equals(slot.getIsAvailable())) {
            if (existingSlotOpt.isEmpty()) {
                TutorSchedule newSlot = new TutorSchedule();
                newSlot.setTutor(tutorRepo.getReferenceById(tutorId));
                newSlot.setWeekday(weekday);
                newSlot.setHour(hour);
                newSlot.setIsAvailable(true);
                scheduleRepo.save(newSlot);
            }
        } else {
            existingSlotOpt.ifPresent(scheduleRepo::delete);
        }
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
