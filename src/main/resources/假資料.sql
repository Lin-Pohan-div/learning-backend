-- ============================================================
-- learningv3 假資料 Seed
-- 插入順序：users → tutors → courses → tutor_schedules
--           → orders → bookings → wallet_logs
--           → lesson_feedback → reviews → chat_messages
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ============================================================
-- 1. users  (role: 1=學生 2=老師 3=管理者)
-- ============================================================
INSERT INTO `users`
  (`id`, `name`, `email`, `password`, `birthday`, `role`, `wallet`, `created_at`, `updated_at`)
VALUES
  (1, '王小明', 'ming@example.com',   SHA2('password123', 256), '2000-05-15', 1, 2000, '2025-08-01 09:00:00', '2025-08-01 09:00:00'),
  (2, '李美玲', 'meiling@example.com', SHA2('password123', 256), '1999-11-20', 1, 5000, '2025-08-05 10:30:00', '2025-08-05 10:30:00'),
  (3, '陳大雄', 'hero@example.com',   SHA2('password123', 256), '2001-03-08', 1,  500, '2025-09-01 08:00:00', '2025-09-01 08:00:00'),
  (4, '張文華', 'jason@example.com',  SHA2('tutor456', 256),    '1988-07-22', 2,    0, '2025-07-10 14:00:00', '2025-07-10 14:00:00'),
  (5, '林淑芬', 'sophie@example.com', SHA2('tutor456', 256),    '1991-02-14', 2,    0, '2025-07-15 11:00:00', '2025-07-15 11:00:00'),
  (6, '系統管理員', 'admin@example.com', SHA2('admin789', 256), '1985-01-01', 3,    0, '2025-07-01 00:00:00', '2025-07-01 00:00:00');

-- ============================================================
-- 2. tutors  (id = users.id)
-- ============================================================
INSERT INTO `tutors`
  (`id`, `applt_date`, `title`, `avatar_url`, `intro`, `education`,
   `certificate_1`, `certificate_name_1`,
   `certificate_2`, `certificate_name_2`,
   `video_url_1`, `video_url_2`, `bank_code`, `bank_account`)
VALUES
  (4, '2025-07-12',
   '英文 & 程式雙棲家教',
   'https://drive.google.com/file/d/1DXU3uHxuMw2IvxwI6i0Sp5FLRMziZeq_/view?usp=drive_link',
   '擁有 8 年教學經驗，專精 TOEIC 與 Python 程式設計，學生平均進步 150 分。',
   '國立臺灣大學資訊工程學系',
   'https://drive.google.com/file/d/1GyM8OkWUQ_s_LZe07njf7P03NiDcFnBG/view?usp=drive_link', 'TOEIC 金色證書',
   'https://drive.google.com/file/d/1-yT3YxcuMpdF4xML_hPUxT5W4F-7aNWp/view?usp=drive_link', 1001,
   'https://www.youtube.com/watch?v=JqfpOX76a-k',
   'https://www.youtube.com/watch?v=647wycnUx04&list=PLQn99bzkJv9w41g62mZGxqh-09wMzkX5t',
   '004', '12345678901234'),

  (5, '2025-07-18',
   '全美語英文家教',
   'https://drive.google.com/file/d/1Vu5PPw9Q5QyT9oM7eHMoh4khCvIehELL/view?usp=drive_link',
   '旅居美國 10 年，提供純英語沉浸式教學，擅長口說與寫作提升。',
   'University of Washington 英語文學碩士',
   'https://drive.google.com/file/d/1GyM8OkWUQ_s_LZe07njf7P03NiDcFnBG/view?usp=drive_link', 'TEFL 國際認證',
   'https://drive.google.com/file/d/1-yT3YxcuMpdF4xML_hPUxT5W4F-7aNWp/view?usp=drive_link', 1002,
   'https://drive.google.com/file/d/11vlhLQ0hjYCt7lZ3AsFVJLQUYQf757EF/view?usp=drive_link',
   NULL,
   '013', '98765432109876');

-- ============================================================
-- 3. courses  (subject: 1=英文 2=程式)
-- ============================================================
INSERT INTO `courses`
  (`id`, `tutor_id`, `name`, `subject`, `description`, `price`, `is_active`)
VALUES
  (1, 4, 'TOEIC 900+ 衝刺班',    1, '系統性攻克 TOEIC 聽力與閱讀，目標 900 分以上。每堂 50 分鐘，含完整題型解析。', 800, 1),
  (2, 4, 'Python 程式入門',        2, '從零開始學 Python，涵蓋基礎語法、函式、OOP 與實作小專案。', 900, 1),
  (3, 5, '英文口說全英語沉浸課',  1, '純英語授課，針對日常對話與商務英語口說，每堂皆有錄音回顧。', 1000, 1),
  (4, 5, '英文寫作精修班',        1, '從段落結構到學術論文寫作，老師提供詳盡批改與回饋。', 850, 0);

-- ============================================================
-- 4. tutor_schedules  (weekday: 1=一 ... 7=日, hour: 9~21)
-- ============================================================
INSERT INTO `tutor_schedules` (`id`, `tutor_id`, `weekday`, `hour`)
VALUES
  -- 張文華：週二、四、六 上午 10~12 / 下午 14~16
  (1, 4, 2, 10), (2, 4, 2, 11), (3, 4, 2, 14), (4, 4, 2, 15),
  (5, 4, 4, 10), (6, 4, 4, 11), (7, 4, 4, 14), (8, 4, 4, 15),
  (9, 4, 6, 10), (10, 4, 6, 11),
  -- 林淑芬：週一、三、五 上午 09~11 / 下午 19~21
  (11, 5, 1,  9), (12, 5, 1, 10), (13, 5, 1, 19), (14, 5, 1, 20),
  (15, 5, 3,  9), (16, 5, 3, 10), (17, 5, 3, 19), (18, 5, 3, 20),
  (19, 5, 5,  9), (20, 5, 5, 10);

-- ============================================================
-- 5. orders  (status: 1=pending 2=deal 3=complete)
-- ============================================================
INSERT INTO `orders`
  (`id`, `user_id`, `course_id`, `unit_price`, `discount_price`, `lesson_count`, `lesson_used`, `status`)
VALUES
  (1, 1, 1,  800,  720, 8, 3, 2),   -- 王小明 買 TOEIC班 8堂，已上3堂
  (2, 1, 2,  900,  900, 4, 1, 2),   -- 王小明 買 Python 4堂，已上1堂
  (3, 2, 3, 1000,  900, 6, 6, 3),   -- 李美玲 買 口說班 6堂，全數完成
  (4, 2, 1,  800,  720, 4, 0, 1),   -- 李美玲 買 TOEIC班 4堂，pending
  (5, 3, 3, 1000, 1000, 3, 1, 2);   -- 陳大雄 買 口說班 3堂，已上1堂

-- ============================================================
-- 6. wallet_logs  (transaction_type: 1=儲值 2=購課 3=授課收入 4=退款 5=提現)
-- ============================================================
INSERT INTO `wallet_logs`
  (`id`, `user_id`, `transaction_type`, `amount`, `related_type`, `related_id`,
   `merchant_trade_no`, `created_at`)
VALUES
  -- 王小明儲值
  (1,  1, 1,  5000, NULL, NULL, 'ECPay20250802001', '2025-08-02 10:00:00'),
  -- 王小明購課 order 1 & 2
  (2,  1, 2, -5760, 1, 1, NULL, '2025-08-02 10:05:00'),
  (3,  1, 2, -3600, 1, 2, NULL, '2025-08-02 10:06:00'),
  -- 李美玲儲值
  (4,  2, 1, 10000, NULL, NULL, 'ECPay20250806001', '2025-08-06 09:00:00'),
  -- 李美玲購課 order 3
  (5,  2, 2, -5400, 1, 3, NULL, '2025-08-06 09:05:00'),
  -- 陳大雄儲值
  (6,  3, 1,  3000, NULL, NULL, 'ECPay20250902001', '2025-09-02 08:30:00'),
  -- 陳大雄購課 order 5
  (7,  3, 2, -3000, 1, 5, NULL, '2025-09-02 08:35:00'),
  -- 張文華授課收入 (order 1 已完成3堂)
  (8,  4, 3,  2160, 2, 1, NULL, '2025-09-15 23:00:00'),
  -- 林淑芬授課收入 (order 3 6堂全完成)
  (9,  5, 3,  5400, 2, 3, NULL, '2025-10-01 23:00:00'),
  -- 林淑芬提現
  (10, 5, 5, -5000, 3, NULL, NULL, '2025-10-03 14:00:00'),
  -- 平台贈點給王小明
  (11, 1, 6,   200, NULL, NULL, NULL, '2025-10-10 10:00:00');

-- ============================================================
-- 7. bookings  (status: 1=排程中 2=完成 3=取消)
-- ============================================================
INSERT INTO `bookings`
  (`id`, `order_id`, `tutor_id`, `student_id`, `date`, `hour`, `slot_locked`, `status`)
VALUES
  -- order 1 (王小明 + 張文華 TOEIC): 3堂已完成 + 1堂排程中
  (1, 1, 4, 1, '2025-09-02', 10, 1, 2),
  (2, 1, 4, 1, '2025-09-09', 10, 1, 2),
  (3, 1, 4, 1, '2025-09-16', 10, 1, 2),
  (4, 1, 4, 1, '2025-09-23', 10, 1, 1),
  -- order 2 (王小明 + 張文華 Python): 1堂已完成
  (5, 2, 4, 1, '2025-09-04', 14, 1, 2),
  -- order 3 (李美玲 + 林淑芬 口說): 6堂全部完成
  (6,  3, 5, 2, '2025-08-11',  9, 1, 2),
  (7,  3, 5, 2, '2025-08-13',  9, 1, 2),
  (8,  3, 5, 2, '2025-08-18',  9, 1, 2),
  (9,  3, 5, 2, '2025-08-20',  9, 1, 2),
  (10, 3, 5, 2, '2025-08-25',  9, 1, 2),
  (11, 3, 5, 2, '2025-08-27',  9, 1, 2),
  -- order 5 (陳大雄 + 林淑芬 口說): 1堂已完成
  (12, 5, 5, 3, '2025-09-08', 19, 1, 2);

-- ============================================================
-- 8. lesson_feedback
-- ============================================================
INSERT INTO `lesson_feedback`
  (`id`, `booking_id`, `rating`, `comment`,
   `focus_score`, `comprehension_score`, `confidence_score`, `created_at`)
VALUES
  (1,  1, 5, '老師解析清楚，聽力技巧大有進步！',          90, 85, 80, '2025-09-02 11:30:00'),
  (2,  2, 4, '閱讀方法很實用，需要多加練習。',             85, 80, 75, '2025-09-09 11:30:00'),
  (3,  3, 5, '今天學了長題型破題技巧，非常受用。',         95, 90, 88, '2025-09-16 11:30:00'),
  (4,  5, 4, 'Python 函式概念清晰，作業有點難。',          80, 78, 72, '2025-09-04 15:30:00'),
  (5,  6, 5, '全英語環境讓我進步很快！',                   88, 85, 82, '2025-08-11 10:30:00'),
  (6,  7, 5, '口說練習很有趣，老師很有耐心。',             90, 88, 86, '2025-08-13 10:30:00'),
  (7,  8, 4, '今天話題較難，但收穫豐富。',                 82, 80, 79, '2025-08-18 10:30:00'),
  (8,  9, 5, '語速提升明顯，信心大增！',                   92, 90, 91, '2025-08-20 10:30:00'),
  (9, 10, 5, '商務情境模擬超實用。',                        95, 93, 92, '2025-08-25 10:30:00'),
  (10,11, 5, '最後一堂，感謝老師這段時間的陪伴！',         97, 95, 96, '2025-08-27 10:30:00'),
  (11,12, 3, '課程內容不錯，但時間有點趕。',               75, 70, 68, '2025-09-08 20:30:00');

-- ============================================================
-- 9. reviews  (一位學生對同一課程只能評一次)
-- ============================================================
INSERT INTO `reviews`
  (`id`, `user_id`, `course_id`, `focus_score`, `comprehension_score`,
   `confidence_score`, `comment`)
VALUES
  (1, 1, 1, 5, 5, 4, '張老師講解仔細，讓我的 TOEIC 從 750 進步到 895，非常推薦！'),
  (2, 1, 2, 4, 4, 4, 'Python 課程由淺入深，適合完全零基礎的學生。'),
  (3, 2, 3, 5, 5, 5, '林老師讓我愛上說英文！六堂課後口說流暢度大幅提升，強烈推薦。'),
  (4, 3, 3, 4, 3, 4, '課程內容豐富，老師耐心，稍微覺得進度有點快。');

-- ============================================================
-- 10. chat_messages
-- ============================================================
INSERT INTO `chat_messages`
  (`id`, `order_id`, `role`, `message`, `created_at`, `message_type`, `media_url`)
VALUES
  -- order 1 (王小明 ↔ 張文華)
  (1, 1, 1, '張老師您好，請問第一堂課需要準備什麼？',            '2025-08-28 15:00:00', 1, NULL),
  (2, 1, 2, '您好！準備一本筆記本即可，教材我會在課前寄給您。',  '2025-08-28 15:10:00', 1, NULL),
  (3, 1, 1, '好的謝謝老師！',                                     '2025-08-28 15:12:00', 1, NULL),
  (4, 1, 2, '這是本週的課前練習題，請先做看看 :)',                '2025-09-01 20:00:00', 1, 'https://drive.google.com/file/d/1s7jhFkLaUWVpxymXdOCOCSCoQMrn_aRh/view?usp=drive_link'),
  (5, 1, 1, '老師我做完了，附上我的答案！',                       '2025-09-01 22:30:00', 1, 'https://drive.google.com/file/d/1s7jhFkLaUWVpxymXdOCOCSCoQMrn_aRh/view?usp=drive_link'),
  -- order 2 (王小明 ↔ 張文華 Python)
  (6, 2, 1, '老師，我對 list comprehension 還是不太懂。',         '2025-09-06 11:00:00', 1, NULL),
  (7, 2, 2, '沒問題，我整理了一份範例文件，你參考看看！',         '2025-09-06 11:30:00', 1, 'https://drive.google.com/file/d/1sv0r312GKteSJqBKchr1xNJpiGqXXccr/view?usp=drive_link'),
  -- order 3 (李美玲 ↔ 林淑芬)
  (8,  3, 1, 'Hi Sophie, I am so excited for the first class!',    '2025-08-10 10:00:00', 1, NULL),
  (9,  3, 2, 'Hi! Me too! See you tomorrow at 9 AM. 😊',           '2025-08-10 10:15:00', 1, NULL),
  (10, 3, 1, 'Thank you for today's class! It was amazing.',        '2025-08-11 10:50:00', 1, NULL),
  (11, 3, 2, 'You did great! Here is a recording of today's session.', '2025-08-11 11:00:00', 1, 'https://drive.google.com/file/d/1vOG-KTVS5HzO9hfo2QkL5ztEMUigS3L4/view?usp=drive_link'),
  -- order 5 (陳大雄 ↔ 林淑芬)
  (12, 5, 1, '老師好，我英文口說不太好，希望可以多練習。',        '2025-09-07 16:00:00', 1, NULL),
  (13, 5, 2, '沒問題！我們會從基礎對話開始慢慢建立信心。',        '2025-09-07 16:20:00', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
COMMIT;

-- ============================================================
-- 驗證查詢 (可自行執行確認資料正確性)
-- ============================================================
-- SELECT u.name, COUNT(b.id) AS 已完成堂數
-- FROM users u
-- JOIN bookings b ON b.student_id = u.id AND b.status = 2
-- GROUP BY u.id;

-- SELECT c.name, AVG(r.focus_score) AS 專注平均, AVG(r.comprehension_score) AS 理解平均
-- FROM reviews r JOIN courses c ON c.id = r.course_id
-- GROUP BY c.id;