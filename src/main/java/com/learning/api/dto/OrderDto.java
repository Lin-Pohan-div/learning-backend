package com.learning.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

public class OrderDto {

    /**
     * 新增訂單
     * { "userId": 3, "courseId": 5, "lessonCount": 10 }
     */
    @Getter @Setter
    public static class Req {
        private Long userId;        // 之後補 JWT 後移除
        private Long courseId;

        @Min(1)
        private Integer lessonCount; // >= 10 自動享 95 折
    }

    /**
     * 修改訂單 (lessonCount / lessonUsed 選填)
     * { "lessonCount": 15, "lessonUsed": 4 }
     */
    @Getter @Setter
    public static class UpdateReq {
        @Min(1)
        private Integer lessonCount; // 選填：調整購買堂數（須 >= 已使用堂數）

        @Min(0)
        private Integer lessonUsed;  // 選填：更新已使用堂數（須 <= lessonCount）
    }

    /**
     * 更新訂單狀態
     * { "status": 2 }   // 1=pending 2=deal 3=complete
     */
    @Getter @Setter
    public static class StatusReq {
        @Min(1) @Max(3)
        private Integer status;
    }

    /**
     * 訂單回應
     * { "id":1, "userId":3, "courseId":5, "unitPrice":700,
     *   "discountPrice":665, "lessonCount":10, "lessonUsed":3, "status":2 }
     */
    @Getter @Setter
    public static class Resp {
        private Long id;
        private Long userId;
        private Long courseId;
        private Integer unitPrice;
        private Integer discountPrice;
        private Integer lessonCount;
        private Integer lessonUsed;
        private Integer status;
    }

    /**
     * 訂單餘額
     * { "userId": 3, "courseId": 5 }
     */
    @Getter @Setter
    public static class Remaining {
        private Long userId;
        private Long courseId;
        private Integer remaining;
    }
}
