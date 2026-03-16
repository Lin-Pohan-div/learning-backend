package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.repo.TutorScheduleRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorScheduleServiceTest {

    @Mock private TutorScheduleRepo scheduleRepo;

    @InjectMocks
    private TutorScheduleService scheduleService;

    // ── 測試資料工廠 ──────────────────────────────────────────────────────────

    private ScheduleDTO.ToggleReq makeToggleReq(Long tutorId, int weekday, int hour, String targetStatus) {
        ScheduleDTO.ToggleReq req = new ScheduleDTO.ToggleReq();
        req.setTutorId(tutorId);
        req.setWeekday(weekday);
        req.setHour(hour);
        req.setTargetStatus(targetStatus);
        return req;
    }

    private TutorSchedule makeSchedule(Long id, Long tutorId, int weekday, int hour) {
        TutorSchedule s = new TutorSchedule();
        s.setId(id);
        s.setTutorId(tutorId);
        s.setWeekday(weekday);
        s.setHour(hour);
        s.setStatus("available");
        return s;
    }

    // ── toggleSchedule - 格式驗證 ─────────────────────────────────────────────

    @Test
    void toggle_weekdayZero_returnsFormatError() {
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 0, 10, "available"));
        assertThat(result).isNotEqualTo("success");
    }

    @Test
    void toggle_weekdayEight_returnsFormatError() {
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 8, 10, "available"));
        assertThat(result).isNotEqualTo("success");
    }

    @Test
    void toggle_hourEight_returnsFormatError() {
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 8, "available"));
        assertThat(result).isNotEqualTo("success");
    }

    @Test
    void toggle_hourTwentyTwo_returnsFormatError() {
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 22, "available"));
        assertThat(result).isNotEqualTo("success");
    }

    @Test
    void toggle_borderWeekdayOne_isValid() {
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 1, 9)).thenReturn(Optional.empty());
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 9, "available"));
        assertThat(result).isEqualTo("success");
    }

    @Test
    void toggle_borderHourTwentyOne_isValid() {
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 3, 21)).thenReturn(Optional.empty());
        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 3, 21, "available"));
        assertThat(result).isEqualTo("success");
    }

    // ── toggleSchedule - 開放 (available) ────────────────────────────────────

    @Test
    void toggle_toAvailable_slotNotExists_createsNewSlot() {
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 1, 10)).thenReturn(Optional.empty());

        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 10, "available"));

        assertThat(result).isEqualTo("success");
        verify(scheduleRepo).save(any(TutorSchedule.class));
    }

    @Test
    void toggle_toAvailable_slotAlreadyExists_noDuplicate() {
        TutorSchedule existing = makeSchedule(1L, 1L, 1, 10);
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 1, 10)).thenReturn(Optional.of(existing));

        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 10, "available"));

        assertThat(result).isEqualTo("success");
        verify(scheduleRepo, never()).save(any());
    }

    // ── toggleSchedule - 關閉 (inactive/其他) ────────────────────────────────

    @Test
    void toggle_toInactive_slotExists_deletesSlot() {
        TutorSchedule existing = makeSchedule(1L, 1L, 1, 10);
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 1, 10)).thenReturn(Optional.of(existing));

        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 10, "inactive"));

        assertThat(result).isEqualTo("success");
        verify(scheduleRepo).delete(existing);
    }

    @Test
    void toggle_toInactive_slotNotExists_noOp() {
        when(scheduleRepo.findByTutorIdAndWeekdayAndHour(1L, 1, 10)).thenReturn(Optional.empty());

        String result = scheduleService.toggleSchedule(makeToggleReq(1L, 1, 10, "inactive"));

        assertThat(result).isEqualTo("success");
        verify(scheduleRepo, never()).delete(any(TutorSchedule.class));
    }

    // ── getWeeklySchedule ─────────────────────────────────────────────────────

    @Test
    void getWeeklySchedule_returnsScheduleList() {
        TutorSchedule s1 = makeSchedule(1L, 1L, 1, 10);
        TutorSchedule s2 = makeSchedule(2L, 1L, 3, 14);
        when(scheduleRepo.findByTutorId(1L)).thenReturn(List.of(s1, s2));

        List<ScheduleDTO.Res> result = scheduleService.getWeeklySchedule(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getWeekday()).isEqualTo(1);
        assertThat(result.get(0).getHour()).isEqualTo(10);
        assertThat(result.get(0).getStatus()).isEqualTo("available");
        assertThat(result.get(1).getWeekday()).isEqualTo(3);
    }

    @Test
    void getWeeklySchedule_noSchedules_returnsEmptyList() {
        when(scheduleRepo.findByTutorId(99L)).thenReturn(List.of());

        List<ScheduleDTO.Res> result = scheduleService.getWeeklySchedule(99L);

        assertThat(result).isEmpty();
    }
}
