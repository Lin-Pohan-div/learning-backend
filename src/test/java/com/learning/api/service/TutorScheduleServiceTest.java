package com.learning.api.service;

import com.learning.api.dto.ScheduleDTO;
import com.learning.api.entity.TutorSchedule;
import com.learning.api.entity.User;
import com.learning.api.repo.TutorScheduleRepo;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.learning.api.enums.UserRole;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TutorScheduleServiceTest {

    @Autowired
    private TutorScheduleService scheduleService;

    @Autowired
    private TutorScheduleRepo scheduleRepo;

    @Autowired
    private UserRepository userRepo;

    private Long tutorId;

    @BeforeEach
    void setUp() {
        User tutor = new User();
        tutor.setName("Schedule Svc Tutor");
        tutor.setEmail("schedulesvc.tutor@example.com");
        tutor.setPassword("hashed");
        tutor.setRole(UserRole.TUTOR);
        tutor.setWallet(0L);
        tutor = userRepo.save(tutor);
        tutorId = tutor.getId();
    }

    private ScheduleDTO.ToggleReq buildReq(int weekday, int hour, String targetStatus) {
        ScheduleDTO.ToggleReq req = new ScheduleDTO.ToggleReq();
        req.setTutorId(tutorId);
        req.setWeekday(weekday);
        req.setHour(hour);
        req.setTargetStatus(targetStatus);
        return req;
    }

    @Test
    void toggleSchedule_available_noExisting_createsRecord() {
        String result = scheduleService.toggleSchedule(buildReq(1, 10, "available"));
        assertEquals("success", result);
        assertTrue(scheduleRepo.findByTutorIdAndWeekdayAndHour(tutorId, 1, 10).isPresent());
    }

    @Test
    void toggleSchedule_inactive_existingRecord_deletesRecord() {
        TutorSchedule existing = new TutorSchedule();
        existing.setTutorId(tutorId);
        existing.setWeekday(2);
        existing.setHour(14);
        existing.setStatus("available");
        scheduleRepo.save(existing);

        String result = scheduleService.toggleSchedule(buildReq(2, 14, "inactive"));
        assertEquals("success", result);
        assertFalse(scheduleRepo.findByTutorIdAndWeekdayAndHour(tutorId, 2, 14).isPresent());
    }

    @Test
    void toggleSchedule_invalidHour_returnsErrorMessage() {
        String result = scheduleService.toggleSchedule(buildReq(1, 8, "available")); // hour < 9
        assertNotEquals("success", result);
        assertTrue(result.contains("格式錯誤"));
    }

    @Test
    void toggleSchedule_invalidWeekday_returnsErrorMessage() {
        String result = scheduleService.toggleSchedule(buildReq(0, 10, "available")); // weekday < 1
        assertNotEquals("success", result);
        assertTrue(result.contains("格式錯誤"));
    }

    @Test
    void getWeeklySchedule_returnsAvailableSlots() {
        TutorSchedule s1 = new TutorSchedule();
        s1.setTutorId(tutorId);
        s1.setWeekday(3);
        s1.setHour(9);
        s1.setStatus("available");
        scheduleRepo.save(s1);

        TutorSchedule s2 = new TutorSchedule();
        s2.setTutorId(tutorId);
        s2.setWeekday(5);
        s2.setHour(15);
        s2.setStatus("available");
        scheduleRepo.save(s2);

        List<ScheduleDTO.Res> result = scheduleService.getWeeklySchedule(tutorId);
        assertEquals(2, result.size());
    }
}
