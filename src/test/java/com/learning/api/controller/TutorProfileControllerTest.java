package com.learning.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.api.entity.User;
import com.learning.api.entity.Tutor;
import com.learning.api.repo.TutorRepository;
import com.learning.api.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.learning.api.enums.UserRole;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TutorProfileControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User testTutor;
    private Long savedTutorId;

    @BeforeEach
    void setUp() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testTutor = new User();
        testTutor.setName("Test Teacher");
        testTutor.setEmail("teacher_profile_test@example.com");
        testTutor.setPassword("hashedpassword");
        testTutor.setRole(UserRole.TUTOR);
        testTutor.setWallet(0L);
        testTutor = userRepository.save(testTutor);
        savedTutorId = testTutor.getId();
    }

    // ===================== GET =====================

    @Test
    void get_noProfileExists_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value(containsString("找不到")));
    }

    @Test
    void get_afterCreate_shouldReturn200WithData() throws Exception {
        Map<String, Object> body = buildFullProfileBody(testTutor.getId());

        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTutor.getId()))
                .andExpect(jsonPath("$.title").value("資深英文老師"))
                .andExpect(jsonPath("$.intro").value("我有十年教學經驗"))
                .andExpect(jsonPath("$.education").value("國立台灣大學 碩士"))
                .andExpect(jsonPath("$.certificate1").value("https://example.com/cert1.jpg"))
                .andExpect(jsonPath("$.certificateName1").value("TESOL 國際英語教師認證"))
                .andExpect(jsonPath("$.bankCode").value("822"))
                .andExpect(jsonPath("$.bankAccount").value("123456789"));
    }

    // ===================== POST =====================

    @Test
    void post_missingTutorId_shouldReturn400() throws Exception {
        Map<String, Object> body = Map.of("title", "老師標題");

        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void post_nonExistentUser_shouldReturn404() throws Exception {
        Map<String, Object> body = Map.of("tutorId", 999999L, "title", "不存在的老師");

        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value(containsString("找不到")));
    }

    @Test
    void post_validData_shouldReturn201() throws Exception {
        Map<String, Object> body = buildFullProfileBody(testTutor.getId());

        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.msg").value(containsString("成功")));
    }

    @Test
    void post_profileAlreadyExists_shouldReturn409() throws Exception {
        Map<String, Object> body = buildFullProfileBody(testTutor.getId());

        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());

        // 再次建立應回 409
        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value(containsString("已存在")));
    }

    // ===================== PUT =====================

    @Test
    void put_validRequest_withAllFields_shouldReturn200() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "Updated Prof",
                "intro", "I have been teaching English for 10 years.",
                "certificate", "https://cert.example.com/1.pdf",
                "video", "https://video.example.com/intro.mp4"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(containsString("個人檔案更新成功！")));
    }

    @Test
    void put_missingTutorId_shouldReturn400() throws Exception {
        // Map.of 不接受 null，用 HashMap 省略 tutorId
        Map<String, Object> body = new HashMap<>();
        body.put("name", "No ID Prof");
        body.put("intro", "Some intro");

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("必須提供老師 ID"));
    }

    @Test
    void put_nonExistingTutorId_shouldReturn404() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", 999999L,
                "name", "Nobody",
                "intro", "test"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("找不到該名老師"));
    }

    @Test
    void put_withNameUpdate_shouldUpdateUserName() throws Exception {
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "name", "New Name",
                "intro", "intro text"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(containsString("成功")));
    }

    @Test
    void put_updatesAllFields_shouldReturn200AndPersist() throws Exception {
        // 先建立
        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildFullProfileBody(testTutor.getId()))))
                .andExpect(status().isCreated());

        // 更新
        Map<String, Object> updated = new HashMap<>();
        updated.put("tutorId", testTutor.getId());
        updated.put("title", "更新後標題");
        updated.put("education", "哈佛大學 博士");
        updated.put("bankCode", "007");
        updated.put("bankAccount", "987654321");

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());

        // 確認更新生效
        mockMvc.perform(get("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("更新後標題"))
                .andExpect(jsonPath("$.education").value("哈佛大學 博士"))
                .andExpect(jsonPath("$.bankCode").value("007"));
    }

    // ===================== DELETE =====================

    @Test
    void delete_nonExistentProfile_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value(containsString("找不到")));
    }

    @Test
    void delete_existingProfile_shouldReturn200() throws Exception {
        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildFullProfileBody(testTutor.getId()))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value(containsString("成功")));
    }

    @Test
    void delete_thenGet_shouldReturn404() throws Exception {
        mockMvc.perform(post("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildFullProfileBody(testTutor.getId()))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/teacher/profile/{tutorId}", testTutor.getId()))
                .andExpect(status().isNotFound());
    }

    // ===================== Helper =====================

    private Map<String, Object> buildFullProfileBody(Long tutorId) {
        Map<String, Object> body = new HashMap<>();
        body.put("tutorId", tutorId);
        body.put("name", "王小明");
        body.put("title", "資深英文老師");
        body.put("avatar", "https://example.com/avatar.jpg");
        body.put("intro", "我有十年教學經驗");
        body.put("education", "國立台灣大學 碩士");
        body.put("certificate1", "https://example.com/cert1.jpg");
        body.put("certificateName1", "TESOL 國際英語教師認證");
        body.put("certificate2", "https://example.com/cert2.jpg");
        body.put("certificateName2", "劍橋英語教學認證 CELTA");
        body.put("videoUrl1", "https://example.com/intro.mp4");
        body.put("videoUrl2", "https://example.com/demo.mp4");
        body.put("bankCode", "822");
        body.put("bankAccount", "123456789");
        return body;
    }

    @Test
    void put_withoutName_shouldNotOverwriteUserName() throws Exception {
        // 不傳 name，只傳 intro
        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "intro", "intro only"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        String name = userRepository.findById(savedTutorId).orElseThrow().getName();
        assertThat(name).isEqualTo("Test Teacher");
    }

    @Test
    void put_upsertsTutorRow_shouldCreateTutorRecord() throws Exception {
        assertThat(tutorRepository.existsById(savedTutorId)).isFalse();

        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "intro", "Teaching since 2010"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        Tutor created = tutorRepository.findById(savedTutorId).orElseThrow();
        assertThat(created.getIntro()).isEqualTo("Teaching since 2010");
    }

    @Test
    void put_updatesExistingTutorRow_shouldOverwriteIntro() throws Exception {
        // 預先建立 tutors 列
        Tutor existing = new Tutor();
        existing.setUser(userRepository.findById(savedTutorId).orElseThrow());
        existing.setIntro("Old intro");
        tutorRepository.save(existing);

        Map<String, Object> body = Map.of(
                "tutorId", savedTutorId,
                "intro", "New intro"
        );

        mockMvc.perform(put("/api/teacher/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        Tutor updated = tutorRepository.findById(savedTutorId).orElseThrow();
        assertThat(updated.getIntro()).isEqualTo("New intro");
    }
}
