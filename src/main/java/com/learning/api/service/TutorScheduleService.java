package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.Tutor;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorRepo;
import com.learning.api.repo.TutorScheduleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. 驗證時間格式是否合法（星期 1~7，小時 9~21）
        if (req.getWeekday() < 1 || req.getWeekday() > 7 || req.getHour() < 9 || req.getHour() > 21) {
            return "格式錯誤：時間範圍需在 9~21 點之間。";
        }

        // 2. 查詢這個老師在「星期幾的幾點」是否已有紀錄
        Optional<TutorSchedule> existingSlotOpt = scheduleRepo.findByTutorIdAndWeekdayAndHour(
                req.getTutorId(), req.getWeekday(), req.getHour()
        );

        if (Boolean.TRUE.equals(req.getIsAvailable())) {
            // 【目標：開放時段】

            // 3. 只有在「原本沒有紀錄」的情況下才新增，避免重複
            if (existingSlotOpt.isEmpty()) {
                TutorSchedule newSlot = new TutorSchedule();
                // 改成（需要先查出 Tutor 物件再設定）
                Tutor tutor = tutorRepo.findById(req.getTutorId())
                        .orElseThrow(() -> new IllegalArgumentException("找不到老師"));
                newSlot.setTutor(tutor);
                newSlot.setWeekday(req.getWeekday());  // 設定星期幾
                newSlot.setHour(req.getHour());        // 設定幾點
                newSlot.setIsAvailable(true);          // 標記為開放
                scheduleRepo.save(newSlot);            // 寫入資料庫
            }
            // 4. 如果已經存在就不動（避免重複新增）

        } else {
            // 【目標：關閉時段】

            // 5. 只有在「原本有紀錄」的情況下才刪除
            //    使用 scheduleRepo::delete 是方法引用，等同於 slot -> scheduleRepo.delete(slot)
            existingSlotOpt.ifPresent(scheduleRepo::delete);
        }

        return "success";
    }

    // 取得老師的整週課表，回傳 DTO 格式給前端
    public List<ScheduleDTO.Res> getWeeklySchedule(Long tutorId) {
        return scheduleRepo.findByTutorId(tutorId)
                .stream()
                // 6. 把每個 TutorSchedule entity 轉成 ScheduleDTO.Res，避免回傳原始 entity
                .map(s -> new ScheduleDTO.Res(s.getId(), s.getWeekday(), s.getHour(), s.getIsAvailable()))
                .collect(Collectors.toList());
    }
}