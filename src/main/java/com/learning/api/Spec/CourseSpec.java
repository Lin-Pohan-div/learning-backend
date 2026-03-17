package com.learning.api.Spec;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.learning.api.entity.Course;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

public class CourseSpec {
    public static Specification<Course> filterCourses(
            String teacherName, String courseName, Integer subjectCategory, Integer subject, String priceRange, 
            Integer weekday, String timeSlot) {

        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. 強制過濾：只顯示上架課程
            predicates.add(builder.equal(root.get("active"), 1));

            // 2. 老師姓名模糊搜尋 (Join User 表)
            if (teacherName != null && !teacherName.isEmpty()) {
            	predicates.add(builder.like(
                        root.join("tutor").join("user").get("name"), 
                        "%" + teacherName + "%"
                    ));
            }

            // 3. 課程名稱模糊搜尋
            if (courseName != null && !courseName.isEmpty()) {
                predicates.add(builder.like(root.get("name"), "%" + courseName + "%"));
            }

            // 4. 科目邏輯修改：
            // 如果選了具體科目 (如 11, 21)
            if (subject != null) {
                predicates.add(builder.equal(root.get("subject"), subject));
            } 
            // 如果只選了大類別 (如 10 代表年級課程, 20 代表檢定升學)
            else if (subjectCategory != null) {
                // 搜尋該開頭的代碼，例如 10~19 之間
                predicates.add(builder.between(root.get("subject"), subjectCategory, subjectCategory + 9));
            }

            // 5. 價格區間過濾 (格式: "min-max")
            if (priceRange != null && priceRange.contains("-")) {
                String[] parts = priceRange.split("-");
                try {
                    int min = Integer.parseInt(parts[0]);
                    int max = Integer.parseInt(parts[1]);
                    predicates.add(builder.between(root.get("price"), min, max));
                } catch (NumberFormatException e) {
                    // 價格格式錯誤則忽略
                }
            }
            // 6. 加入每週時間搜尋邏輯
            if (weekday != null || (timeSlot != null && !timeSlot.isEmpty())) {
                // 路徑：Course -> Tutor -> TutorSchedule
                Join<Object, Object> scheduleJoin = root.join("tutor").join("schedules");

                if (weekday != null) {
                    predicates.add(builder.equal(scheduleJoin.get("weekday"), weekday));
                }
                if (timeSlot != null && !timeSlot.isEmpty()) {
                    switch (timeSlot) {
                        case "morning": // 9-12
                            predicates.add(builder.between(scheduleJoin.get("hour"), 9, 12));
                            break;
                        case "afternoon": // 13-16
                            predicates.add(builder.between(scheduleJoin.get("hour"), 13, 16));
                            break;
                        case "evening": // 17-20
                            predicates.add(builder.between(scheduleJoin.get("hour"), 17, 20));
                            break;
                    }
                }
                // 避免同一個課程因為多個時段而重複
                query.distinct(true);
            }

            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
