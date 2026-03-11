-- phpMyAdmin SQL Dump
-- version 5.1.2
-- https://www.phpmyadmin.net/
--
-- 主機： localhost:3306
-- 產生時間： 2026-03-11 00:58:29
-- 伺服器版本： 5.7.24
-- PHP 版本： 8.3.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 資料庫: `learningv3`
--

-- --------------------------------------------------------

--
-- 資料表結構 `bookings`
--

CREATE TABLE `bookings` (
  `id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `tutor_id` bigint(20) NOT NULL,
  `student_id` bigint(20) NOT NULL,
  `date` date NOT NULL,
  `hour` tinyint(4) NOT NULL COMMENT '上課小時(9~21)',
  `slot_locked` tinyint(4) DEFAULT '1' COMMENT '1=時段鎖定 NULL=取消釋放',
  `status` tinyint(4) NOT NULL COMMENT '1=排程中 2=完成 3=取消'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `chat_messages`
--

CREATE TABLE `chat_messages` (
  `id` bigint(20) NOT NULL,
  `order_id` bigint(20) NOT NULL,
  `role` tinyint(4) NOT NULL COMMENT '1=學生 2=老師',
  `message` varchar(1000) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `courses`
--

CREATE TABLE `courses` (
  `id` bigint(20) NOT NULL,
  `tutor_id` bigint(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `subject` tinyint(4) NOT NULL COMMENT '1=英文 2=程式',
  `level` tinyint(4) NOT NULL COMMENT '1=初級 2=中級 3=高級',
  `description` varchar(1000) DEFAULT NULL,
  `price` int(11) NOT NULL,
  `is_active` tinyint(4) NOT NULL DEFAULT '1' COMMENT '1=上架 0=下架'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `lesson_feedback`
--

CREATE TABLE `lesson_feedback` (
  `id` bigint(20) NOT NULL,
  `booking_id` bigint(20) NOT NULL,
  `rating` tinyint(4) NOT NULL COMMENT '1~5分',
  `comment` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `orders`
--

CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `unit_price` int(11) NOT NULL COMMENT '原價快照',
  `discount_price` int(11) NOT NULL COMMENT '折扣價',
  `lesson_count` int(11) NOT NULL COMMENT '購買堂數',
  `lesson_used` int(11) NOT NULL DEFAULT '0' COMMENT '已使用堂數',
  `status` tinyint(4) NOT NULL COMMENT '1=pending 2=deal 3=complete'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `reviews`
--

CREATE TABLE `reviews` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `course_id` bigint(20) NOT NULL,
  `focus_score` tinyint(4) NOT NULL,
  `comprehension_score` int(11) NOT NULL,
  `confidence_score` int(11) NOT NULL,
  `comment` varchar(1000) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `tutors`
--

CREATE TABLE `tutors` (
  `id` bigint(20) NOT NULL,
  `title` varchar(50) DEFAULT NULL,
  `intro` varchar(1000) DEFAULT NULL,
  `certificate_1` varchar(500) DEFAULT NULL,
  `certificate_name_1` varchar(40) NOT NULL,
  `certificate_2` varchar(500) NOT NULL,
  `certificate_name_2` int(11) NOT NULL,
  `video_url_1` varchar(500) DEFAULT NULL,
  `video_url_2` varchar(500) DEFAULT NULL,
  `bank_code` varchar(10) DEFAULT NULL,
  `bank_account` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `tutor_schedules`
--

CREATE TABLE `tutor_schedules` (
  `id` bigint(20) NOT NULL,
  `tutor_id` bigint(20) NOT NULL,
  `weekday` tinyint(4) NOT NULL COMMENT '1=星期一 ... 7=星期日',
  `hour` tinyint(4) NOT NULL COMMENT '上課小時(9~21)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `users`
--

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(64) NOT NULL,
  `birthday` date DEFAULT NULL,
  `role` tinyint(4) NOT NULL COMMENT '1=學生 2=老師 3=管理者',
  `wallet` bigint(20) NOT NULL DEFAULT '0' COMMENT '錢包餘額(快取)',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- 資料表結構 `wallet_logs`
--

CREATE TABLE `wallet_logs` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `transaction_type` tinyint(4) NOT NULL COMMENT '1=儲值 2=購課 3=授課收入 4=退款 5=提現 6=平台贈點',
  `amount` bigint(20) NOT NULL COMMENT '金額(+入帳 -扣款)',
  `related_type` tinyint(4) DEFAULT NULL COMMENT '1=order 2=booking 3=bank',
  `related_id` bigint(20) DEFAULT NULL,
  `merchant_trade_no` varchar(100) DEFAULT NULL COMMENT '金流交易編號',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 已傾印資料表的索引
--

--
-- 資料表索引 `bookings`
--
ALTER TABLE `bookings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tutor_id` (`tutor_id`,`date`,`hour`,`slot_locked`),
  ADD UNIQUE KEY `student_id` (`student_id`,`date`,`hour`,`slot_locked`),
  ADD KEY `idx_lessons_tutor_date` (`tutor_id`,`date`,`hour`),
  ADD KEY `idx_lessons_booking` (`order_id`),
  ADD KEY `idx_lessons_student` (`student_id`,`date`,`hour`);

--
-- 資料表索引 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_chat_booking_time` (`order_id`,`created_at`);

--
-- 資料表索引 `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tutor_id` (`tutor_id`,`name`);

--
-- 資料表索引 `lesson_feedback`
--
ALTER TABLE `lesson_feedback`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_feedback_lesson` (`booking_id`);

--
-- 資料表索引 `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_booking_user` (`user_id`),
  ADD KEY `fk_booking_course` (`course_id`);

--
-- 資料表索引 `reviews`
--
ALTER TABLE `reviews`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `user_id` (`user_id`,`course_id`),
  ADD KEY `fk_review_course` (`course_id`);

--
-- 資料表索引 `tutors`
--
ALTER TABLE `tutors`
  ADD PRIMARY KEY (`id`);

--
-- 資料表索引 `tutor_schedules`
--
ALTER TABLE `tutor_schedules`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tutor_id` (`tutor_id`,`weekday`,`hour`);

--
-- 資料表索引 `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- 資料表索引 `wallet_logs`
--
ALTER TABLE `wallet_logs`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `merchant_trade_no` (`merchant_trade_no`),
  ADD KEY `idx_wallet_user_time` (`user_id`,`created_at`);

--
-- 在傾印的資料表使用自動遞增(AUTO_INCREMENT)
--

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `bookings`
--
ALTER TABLE `bookings`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `chat_messages`
--
ALTER TABLE `chat_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `courses`
--
ALTER TABLE `courses`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `lesson_feedback`
--
ALTER TABLE `lesson_feedback`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `orders`
--
ALTER TABLE `orders`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `reviews`
--
ALTER TABLE `reviews`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `tutor_schedules`
--
ALTER TABLE `tutor_schedules`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `users`
--
ALTER TABLE `users`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 使用資料表自動遞增(AUTO_INCREMENT) `wallet_logs`
--
ALTER TABLE `wallet_logs`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- 已傾印資料表的限制式
--

--
-- 資料表的限制式 `bookings`
--
ALTER TABLE `bookings`
  ADD CONSTRAINT `fk_lesson_booking` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  ADD CONSTRAINT `fk_lesson_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `fk_lesson_tutor` FOREIGN KEY (`tutor_id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `chat_messages`
--
ALTER TABLE `chat_messages`
  ADD CONSTRAINT `fk_chat_booking` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`);

--
-- 資料表的限制式 `courses`
--
ALTER TABLE `courses`
  ADD CONSTRAINT `fk_course_tutor` FOREIGN KEY (`tutor_id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `lesson_feedback`
--
ALTER TABLE `lesson_feedback`
  ADD CONSTRAINT `fk_feedback_lesson` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`);

--
-- 資料表的限制式 `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `fk_booking_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  ADD CONSTRAINT `fk_booking_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `reviews`
--
ALTER TABLE `reviews`
  ADD CONSTRAINT `fk_review_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`id`),
  ADD CONSTRAINT `fk_review_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `tutors`
--
ALTER TABLE `tutors`
  ADD CONSTRAINT `fk_tutor_user` FOREIGN KEY (`id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `tutor_schedules`
--
ALTER TABLE `tutor_schedules`
  ADD CONSTRAINT `fk_schedule_tutor` FOREIGN KEY (`tutor_id`) REFERENCES `users` (`id`);

--
-- 資料表的限制式 `wallet_logs`
--
ALTER TABLE `wallet_logs`
  ADD CONSTRAINT `fk_wallet_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
