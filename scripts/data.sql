-- KHUYẾN KHÍCH EXECUTE TỪNG ĐOẠN NHÉ!!!

-- 1. Insert Seasons (Bảng gốc)
INSERT INTO seasons (id, semester, year) VALUES
(gen_random_uuid(), 'SPRING', 2026),
(gen_random_uuid(), 'SUMMER', 2026);

-- 2. Insert Events (Lấy season_id bằng Subquery)
INSERT INTO events (id, name, description, start_time, end_time, teams_limit, status, season_id, open_for_registration) VALUES
(
    gen_random_uuid(),
    'SEAL Hackathon Spring 2026',
    'Mastering Domain-Specific AI RAG Systems',
    '2026-04-11 14:00:00+07',
    '2026-04-12 18:00:00+07',
    50,
    'DRAFT',
    (SELECT id FROM seasons WHERE semester = 'SPRING' AND year = 2026), -- Tìm lại ID của season vừa tạo
    false
);

-- 3. Insert Tracks (Lấy event_id bằng Subquery)
INSERT INTO tracks (id, name, description, event_id) VALUES
(gen_random_uuid(), 'Requirement & Architecture', 'Track A', (SELECT id FROM events WHERE name = 'SEAL Hackathon Spring 2026')),
(gen_random_uuid(), 'TESTING, EXECUTION AND REPORTING APP', 'Track B', (SELECT id FROM events WHERE name = 'SEAL Hackathon Spring 2026')),
(gen_random_uuid(), 'TESTING, EXECUTION AND REPORTING APP', 'Track C', (SELECT id FROM events WHERE name = 'SEAL Hackathon Spring 2026'));

-- 4. Rounds
INSERT INTO rounds (id, name, description, start_time, end_time, event_id) VALUES
(gen_random_uuid(), 'Round 1', 'Giai đoạn 1 & 2', '2026-04-12 07:00:00+07', '2026-04-12 15:30:00+07', (SELECT id FROM events WHERE name = 'SEAL Hackathon Spring 2026')),
(gen_random_uuid(), 'Round 2', 'The Grand Finale', '2026-04-12 15:30:00+07', '2026-04-12 17:00:00+07', (SELECT id FROM events WHERE name = 'SEAL Hackathon Spring 2026'));


-- ==============================================================================
-- ĐỢT 2: TIÊU CHÍ CHẤM ĐIỂM VÀ MAPPING (CRITERIA & ROUND_CRITERIA)
-- ==============================================================================

-- 1. Criteria
INSERT INTO criteria (id, name, weight) VALUES
-- Vòng 1
(gen_random_uuid(), 'Tính đúng đắn & Hoàn thiện chức năng', 30),
(gen_random_uuid(), 'Ứng dụng AI trong giải pháp', 25),
(gen_random_uuid(), 'Thiết kế & Kiến trúc phần mềm', 15),
(gen_random_uuid(), 'Thuyết trình & Demo', 20),
(gen_random_uuid(), 'Teamwork & Tinh thần làm việc', 10),
-- Vòng 2
(gen_random_uuid(), 'Độ hoàn thiện & Chất lượng sản phẩm', 25),
(gen_random_uuid(), 'Sáng tạo & Khả năng đổi mới', 25),
(gen_random_uuid(), 'Tính ứng dụng & Khả năng triển khai', 20),
(gen_random_uuid(), 'Trình bày & Demo sản phẩm', 20),
(gen_random_uuid(), 'Làm việc nhóm & Trả lời phản biện', 10);

-- 2. Map Criteria vào Rounds
INSERT INTO round_criteria (round_id, criteria_id) VALUES
-- Vòng 1
((SELECT id FROM rounds WHERE name = 'Round 1'), (SELECT id FROM criteria WHERE name = 'Tính đúng đắn & Hoàn thiện chức năng')),
((SELECT id FROM rounds WHERE name = 'Round 1'), (SELECT id FROM criteria WHERE name = 'Ứng dụng AI trong giải pháp')),
((SELECT id FROM rounds WHERE name = 'Round 1'), (SELECT id FROM criteria WHERE name = 'Thiết kế & Kiến trúc phần mềm')),
((SELECT id FROM rounds WHERE name = 'Round 1'), (SELECT id FROM criteria WHERE name = 'Thuyết trình & Demo')),
((SELECT id FROM rounds WHERE name = 'Round 1'), (SELECT id FROM criteria WHERE name = 'Teamwork & Tinh thần làm việc')),
-- Vòng 2
((SELECT id FROM rounds WHERE name = 'Round 2'), (SELECT id FROM criteria WHERE name = 'Độ hoàn thiện & Chất lượng sản phẩm')),
((SELECT id FROM rounds WHERE name = 'Round 2'), (SELECT id FROM criteria WHERE name = 'Sáng tạo & Khả năng đổi mới')),
((SELECT id FROM rounds WHERE name = 'Round 2'), (SELECT id FROM criteria WHERE name = 'Tính ứng dụng & Khả năng triển khai')),
((SELECT id FROM rounds WHERE name = 'Round 2'), (SELECT id FROM criteria WHERE name = 'Trình bày & Demo sản phẩm')),
((SELECT id FROM rounds WHERE name = 'Round 2'), (SELECT id FROM criteria WHERE name = 'Làm việc nhóm & Trả lời phản biện'));


-- ==============================================================================
-- ĐỢT 3: TẠO TÀI KHOẢN (COORDINATOR, LECTURERS / JUDGES)
-- ==============================================================================

-- 1. Coordinator
INSERT INTO coordinator (id, full_name, role, email, password_hash) VALUES
(gen_random_uuid(), 'Coordinator', 'COORDINATOR', 'admin@seal.edu.vn', '123456');

-- 2. Lecturers (Giám khảo)
INSERT INTO lecturer (id, full_name, role, email, password_hash) VALUES
-- Group A
(gen_random_uuid(), 'Nguyễn Văn Chiến', 'LECTURER', 'chiennv@seal.edu.vn', 'hash'),
(gen_random_uuid(), 'Phạm Thanh Trí', 'LECTURER', 'tript@seal.edu.vn', 'hash'),
-- Group B
(gen_random_uuid(), 'Nguyễn Minh Sang', 'LECTURER', 'sangnm@seal.edu.vn', 'hash'),
(gen_random_uuid(), 'Nguyễn Ngọc Lâm', 'LECTURER', 'lamnn@seal.edu.vn', 'hash'),
-- Group C
(gen_random_uuid(), 'Lê Thị Quỳnh Chi', 'LECTURER', 'chiltq@seal.edu.vn', 'hash'),
(gen_random_uuid(), 'Đỗ Phúc Thịnh', 'LECTURER', 'thinhdp@seal.edu.vn', 'hash'),
-- Chung Kết
(gen_random_uuid(), 'Trần Thiên Phúc', 'LECTURER', 'phuctt@seal.edu.vn', 'hash'),
(gen_random_uuid(), 'Phạm Thái Dương', 'LECTURER', 'duongpt@seal.edu.vn', 'hash'),
(gen_random_uuid(), 'Lâm Hữu Khánh Phương', 'LECTURER', 'phuonglhk@seal.edu.vn', 'hash');

-- 3. Mapping Giám khảo vào Track
-- Group A (Track A)
INSERT INTO tracks_judges (judged_tracks_id, judges_id)
SELECT t.id, l.id
FROM tracks t CROSS JOIN lecturer l
WHERE t.description = 'Track A'
  AND l.email IN ('chiennv@seal.edu.vn', 'tript@seal.edu.vn');

-- Group B (Track B)
INSERT INTO tracks_judges (judged_tracks_id, judges_id)
SELECT t.id, l.id
FROM tracks t CROSS JOIN lecturer l
WHERE t.description = 'Track B'
  AND l.email IN ('sangnm@seal.edu.vn', 'lamnn@seal.edu.vn');

-- Group C (Track C)
INSERT INTO tracks_judges (judged_tracks_id, judges_id)
SELECT t.id, l.id
FROM tracks t CROSS JOIN lecturer l
WHERE t.description = 'Track C'
  AND l.email IN ('chiltq@seal.edu.vn', 'thinhdp@seal.edu.vn');


-- ==============================================================================
-- ĐỢT 4: SINH VIÊN VÀ ĐỘI THI (Xử lý Circular Dependency cho 6 đội Chung kết)
-- ==============================================================================

-- 1. Tạo 6 Sinh viên Leader (để team_id = NULL)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id) VALUES
(gen_random_uuid(), 'Leader Slothub', 'STUDENT', 'lead_slothub@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180001', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader THE ORCA', 'STUDENT', 'lead_orca@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180002', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Aqua team', 'STUDENT', 'lead_aqua@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT001', 'ĐH Bách Khoa', NULL),
(gen_random_uuid(), 'Leader RAGnarok', 'STUDENT', 'lead_rag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180003', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Red Team Gang', 'STUDENT', 'lead_red@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180004', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader WORKA GANG', 'STUDENT', 'lead_worka@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT002', 'ĐH KHTN', NULL);

-- 2. Tạo 6 Đội thi (Lấy leader_id từ mã sinh viên)
-- Track A: Slothub
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Slothub', 'Giải pháp y tế', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'SE180001'
  AND t.description = 'Track A';

-- Track A: THE ORCA
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'THE ORCA', 'Tích hợp DuckDB', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'SE180002'
  AND t.description = 'Track A';

-- Track B: Aqua team
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Aqua team', 'Kiến trúc Cache', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'EXT001'
  AND t.description = 'Track B';

-- Track B: RAGnarok
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'RAGnarok', 'Giao diện UI/UX', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'SE180003'
  AND t.description = 'Track B';

-- Track C: Red Team Gang
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Red Team Gang', 'Ứng dụng GraphDB', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'SE180004'
  AND t.description = 'Track C';

-- Track C: WORKA GANG
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'WORKA GANG', 'Hệ thống Marketing', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t
WHERE e.name = 'SEAL Hackathon Spring 2026'
  AND s.student_id = 'EXT002'
  AND t.description = 'Track C';

-- 3. Update lại team_id cho 6 Leader
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'Slothub') WHERE student_id = 'SE180001';
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'THE ORCA') WHERE student_id = 'SE180002';
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'Aqua team') WHERE student_id = 'EXT001';
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'RAGnarok') WHERE student_id = 'SE180003';
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'Red Team Gang') WHERE student_id = 'SE180004';
UPDATE student SET team_id = (SELECT id FROM teams WHERE name = 'WORKA GANG') WHERE student_id = 'EXT002';

-- 4. Bổ sung Members (Mỗi đội 2 thành viên) để đội hợp lệ (>=3 thành viên)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id) VALUES
-- Team Slothub
(gen_random_uuid(), 'Member 1 Slothub', 'STUDENT', 'mem1_slot@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180011', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'Slothub')),
(gen_random_uuid(), 'Member 2 Slothub', 'STUDENT', 'mem2_slot@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180012', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'Slothub')),
-- Team THE ORCA
(gen_random_uuid(), 'Member 1 ORCA', 'STUDENT', 'mem1_orca@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180021', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'THE ORCA')),
(gen_random_uuid(), 'Member 2 ORCA', 'STUDENT', 'mem2_orca@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180022', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'THE ORCA')),
-- Team Aqua team
(gen_random_uuid(), 'Member 1 Aqua', 'STUDENT', 'mem1_aqua@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT011', 'ĐH Bách Khoa', (SELECT id FROM teams WHERE name = 'Aqua team')),
(gen_random_uuid(), 'Member 2 Aqua', 'STUDENT', 'mem2_aqua@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT012', 'ĐH Bách Khoa', (SELECT id FROM teams WHERE name = 'Aqua team')),
-- Team RAGnarok
(gen_random_uuid(), 'Member 1 RAG', 'STUDENT', 'mem1_rag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180031', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'RAGnarok')),
(gen_random_uuid(), 'Member 2 RAG', 'STUDENT', 'mem2_rag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180032', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'RAGnarok')),
-- Team Red Team Gang
(gen_random_uuid(), 'Member 1 Red', 'STUDENT', 'mem1_red@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180041', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'Red Team Gang')),
(gen_random_uuid(), 'Member 2 Red', 'STUDENT', 'mem2_red@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180042', 'Đại học FPT', (SELECT id FROM teams WHERE name = 'Red Team Gang')),
-- Team WORKA GANG
(gen_random_uuid(), 'Member 1 WORKA', 'STUDENT', 'mem1_worka@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT021', 'ĐH KHTN', (SELECT id FROM teams WHERE name = 'WORKA GANG')),
(gen_random_uuid(), 'Member 2 WORKA', 'STUDENT', 'mem2_worka@gmail.com', 'hash', 'EXTERNAL', 'ACTIVE', 'EXT022', 'ĐH KHTN', (SELECT id FROM teams WHERE name = 'WORKA GANG'));


-- ==============================================================================
-- BỔ SUNG 19 LEADER CHO CÁC ĐỘI CÒN LẠI (team_id = NULL)
-- ==============================================================================
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id) VALUES
-- Track A (6 đội)
(gen_random_uuid(), 'Leader Đẹp trai', 'STUDENT', 'lead_deptrai@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180101', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Epoch 0', 'STUDENT', 'lead_epoch@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180102', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader food enjoyer', 'STUDENT', 'lead_food@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180103', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader NEWBIES', 'STUDENT', 'lead_newbies@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180104', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader ORTT', 'STUDENT', 'lead_ortt@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180105', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Try', 'STUDENT', 'lead_try@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180106', 'Đại học FPT', NULL),

-- Track B (6 đội)
(gen_random_uuid(), 'Leader 5 anh em', 'STUDENT', 'lead_5ae@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180201', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader APX', 'STUDENT', 'lead_apx@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180202', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader FULI', 'STUDENT', 'lead_fuli@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180203', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader NGUHANHSON', 'STUDENT', 'lead_nguhanhson@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180204', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader VAIK', 'STUDENT', 'lead_vaik@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180205', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader WhaleDone', 'STUDENT', 'lead_whale@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180206', 'Đại học FPT', NULL),

-- Track C (7 đội)
(gen_random_uuid(), 'Leader 404NotFound', 'STUDENT', 'lead_404@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180301', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader BitMindz', 'STUDENT', 'lead_bitmindz@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180302', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader KQL', 'STUDENT', 'lead_kql@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180303', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader LearningAgent', 'STUDENT', 'lead_learning@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180304', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Passion Ducks', 'STUDENT', 'lead_passion@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180305', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader Underrated', 'STUDENT', 'lead_underrated@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180306', 'Đại học FPT', NULL),
(gen_random_uuid(), 'Leader YAG', 'STUDENT', 'lead_yag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180307', 'Đại học FPT', NULL);

-- ==============================================================================
-- BỔ SUNG 19 ĐỘI CÒN LẠI (Sử dụng INSERT INTO ... SELECT)
-- ==============================================================================

-- TRACK A
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Đẹp trai có gì sai', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180101' AND t.description = 'Track A';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Epoch 0', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180102' AND t.description = 'Track A';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'food enjoyer', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180103' AND t.description = 'Track A';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'NEWBIES', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180104' AND t.description = 'Track A';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'ORTT', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180105' AND t.description = 'Track A';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Try', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180106' AND t.description = 'Track A';

-- TRACK B
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), '5 anh em siêu nhân', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180201' AND t.description = 'Track B';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'APX', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180202' AND t.description = 'Track B';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'FULI', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180203' AND t.description = 'Track B';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'NGUHANHSON', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180204' AND t.description = 'Track B';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'VAIK', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180205' AND t.description = 'Track B';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'WhaleDone', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180206' AND t.description = 'Track B';

-- TRACK C
INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), '404NotFound', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180301' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'BitMindz', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180302' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'KQL', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180303' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'LearningAgent', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180304' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Passion Ducks', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180305' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'Underrated', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180306' AND t.description = 'Track C';

INSERT INTO teams (id, name, description, team_status, event_id, leader_id, track_id)
SELECT gen_random_uuid(), 'YAG', 'Team vòng loại', 'ACTIVE', e.id, s.id, t.id
FROM events e, student s, tracks t WHERE e.name = 'SEAL Hackathon Spring 2026' AND s.student_id = 'SE180307' AND t.description = 'Track C';

-- ==============================================================================
-- UPDATE TEAM_ID NGƯỢC LẠI CHO CÁC LEADER ĐỂ KHÔNG BỊ LỖI KHOÁ NGOẠI
-- ==============================================================================
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180101' AND t.name = 'Đẹp trai có gì sai';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180102' AND t.name = 'Epoch 0';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180103' AND t.name = 'food enjoyer';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180104' AND t.name = 'NEWBIES';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180105' AND t.name = 'ORTT';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180106' AND t.name = 'Try';

UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180201' AND t.name = '5 anh em siêu nhân';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180202' AND t.name = 'APX';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180203' AND t.name = 'FULI';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180204' AND t.name = 'NGUHANHSON';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180205' AND t.name = 'VAIK';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180206' AND t.name = 'WhaleDone';

UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180301' AND t.name = '404NotFound';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180302' AND t.name = 'BitMindz';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180303' AND t.name = 'KQL';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180304' AND t.name = 'LearningAgent';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180305' AND t.name = 'Passion Ducks';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180306' AND t.name = 'Underrated';
UPDATE student s SET team_id = t.id FROM teams t WHERE s.student_id = 'SE180307' AND t.name = 'YAG';


-- ==============================================================================
-- BỔ SUNG THÀNH VIÊN CHO 19 ĐỘI CÒN LẠI
-- ==============================================================================

-- ---------------------------------------------------------
-- TRACK A
-- ---------------------------------------------------------
-- Đẹp trai có gì sai (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Đẹp trai', 'STUDENT', 'm1_deptrai@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180111', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Đẹp trai có gì sai' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Đẹp trai', 'STUDENT', 'm2_deptrai@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180112', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Đẹp trai có gì sai' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Đẹp trai', 'STUDENT', 'm3_deptrai@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180113', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Đẹp trai có gì sai';

-- Epoch 0 (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Epoch', 'STUDENT', 'm1_epoch@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180121', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Epoch 0' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Epoch', 'STUDENT', 'm2_epoch@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180122', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Epoch 0';

-- food enjoyer (+4 thành viên -> Tổng 5)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Food', 'STUDENT', 'm1_food@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180131', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'food enjoyer' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Food', 'STUDENT', 'm2_food@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180132', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'food enjoyer' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Food', 'STUDENT', 'm3_food@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180133', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'food enjoyer' UNION ALL
SELECT gen_random_uuid(), 'Mem 4 Food', 'STUDENT', 'm4_food@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180134', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'food enjoyer';

-- NEWBIES (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 NEWBIES', 'STUDENT', 'm1_newbies@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180141', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'NEWBIES' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 NEWBIES', 'STUDENT', 'm2_newbies@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180142', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'NEWBIES';

-- ORTT (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 ORTT', 'STUDENT', 'm1_ortt@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180151', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'ORTT' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 ORTT', 'STUDENT', 'm2_ortt@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180152', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'ORTT' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 ORTT', 'STUDENT', 'm3_ortt@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180153', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'ORTT';

-- Try (+4 thành viên -> Tổng 5)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Try', 'STUDENT', 'm1_try@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180161', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Try' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Try', 'STUDENT', 'm2_try@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180162', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Try' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Try', 'STUDENT', 'm3_try@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180163', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Try' UNION ALL
SELECT gen_random_uuid(), 'Mem 4 Try', 'STUDENT', 'm4_try@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180164', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Try';


-- ---------------------------------------------------------
-- TRACK B
-- ---------------------------------------------------------
-- 5 anh em siêu nhân (+4 thành viên -> Tổng 5 đúng với tên đội)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Anh em 2', 'STUDENT', 'm2_5ae@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180211', 'Đại học FPT', t.id FROM teams t WHERE t.name = '5 anh em siêu nhân' UNION ALL
SELECT gen_random_uuid(), 'Anh em 3', 'STUDENT', 'm3_5ae@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180212', 'Đại học FPT', t.id FROM teams t WHERE t.name = '5 anh em siêu nhân' UNION ALL
SELECT gen_random_uuid(), 'Anh em 4', 'STUDENT', 'm4_5ae@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180213', 'Đại học FPT', t.id FROM teams t WHERE t.name = '5 anh em siêu nhân' UNION ALL
SELECT gen_random_uuid(), 'Anh em 5', 'STUDENT', 'm5_5ae@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180214', 'Đại học FPT', t.id FROM teams t WHERE t.name = '5 anh em siêu nhân';

-- APX (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 APX', 'STUDENT', 'm1_apx@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180221', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'APX' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 APX', 'STUDENT', 'm2_apx@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180222', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'APX';

-- FULI (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 FULI', 'STUDENT', 'm1_fuli@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180231', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'FULI' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 FULI', 'STUDENT', 'm2_fuli@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180232', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'FULI' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 FULI', 'STUDENT', 'm3_fuli@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180233', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'FULI';

-- NGUHANHSON (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 NHS', 'STUDENT', 'm1_nhs@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180241', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'NGUHANHSON' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 NHS', 'STUDENT', 'm2_nhs@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180242', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'NGUHANHSON' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 NHS', 'STUDENT', 'm3_nhs@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180243', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'NGUHANHSON';

-- VAIK (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 VAIK', 'STUDENT', 'm1_vaik@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180251', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'VAIK' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 VAIK', 'STUDENT', 'm2_vaik@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180252', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'VAIK';

-- WhaleDone (+4 thành viên -> Tổng 5)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Whale', 'STUDENT', 'm1_whale@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180261', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'WhaleDone' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Whale', 'STUDENT', 'm2_whale@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180262', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'WhaleDone' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Whale', 'STUDENT', 'm3_whale@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180263', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'WhaleDone' UNION ALL
SELECT gen_random_uuid(), 'Mem 4 Whale', 'STUDENT', 'm4_whale@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180264', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'WhaleDone';


-- ---------------------------------------------------------
-- TRACK C
-- ---------------------------------------------------------
-- 404NotFound (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 404', 'STUDENT', 'm1_404@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180311', 'Đại học FPT', t.id FROM teams t WHERE t.name = '404NotFound' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 404', 'STUDENT', 'm2_404@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180312', 'Đại học FPT', t.id FROM teams t WHERE t.name = '404NotFound' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 404', 'STUDENT', 'm3_404@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180313', 'Đại học FPT', t.id FROM teams t WHERE t.name = '404NotFound';

-- BitMindz (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Bit', 'STUDENT', 'm1_bit@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180321', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'BitMindz' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Bit', 'STUDENT', 'm2_bit@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180322', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'BitMindz';

-- KQL (+4 thành viên -> Tổng 5)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 KQL', 'STUDENT', 'm1_kql@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180331', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'KQL' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 KQL', 'STUDENT', 'm2_kql@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180332', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'KQL' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 KQL', 'STUDENT', 'm3_kql@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180333', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'KQL' UNION ALL
SELECT gen_random_uuid(), 'Mem 4 KQL', 'STUDENT', 'm4_kql@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180334', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'KQL';

-- LearningAgent (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Learning', 'STUDENT', 'm1_learning@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180341', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'LearningAgent' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Learning', 'STUDENT', 'm2_learning@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180342', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'LearningAgent';

-- Passion Ducks (+3 thành viên -> Tổng 4)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Ducks', 'STUDENT', 'm1_ducks@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180351', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Passion Ducks' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Ducks', 'STUDENT', 'm2_ducks@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180352', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Passion Ducks' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Ducks', 'STUDENT', 'm3_ducks@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180353', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Passion Ducks';

-- Underrated (+4 thành viên -> Tổng 5)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 Under', 'STUDENT', 'm1_under@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180361', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Underrated' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 Under', 'STUDENT', 'm2_under@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180362', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Underrated' UNION ALL
SELECT gen_random_uuid(), 'Mem 3 Under', 'STUDENT', 'm3_under@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180363', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Underrated' UNION ALL
SELECT gen_random_uuid(), 'Mem 4 Under', 'STUDENT', 'm4_under@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180364', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'Underrated';

-- YAG (+2 thành viên -> Tổng 3)
INSERT INTO student (id, full_name, role, email, password_hash, student_type, student_status, student_id, school_name, team_id)
SELECT gen_random_uuid(), 'Mem 1 YAG', 'STUDENT', 'm1_yag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180371', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'YAG' UNION ALL
SELECT gen_random_uuid(), 'Mem 2 YAG', 'STUDENT', 'm2_yag@fpt.edu.vn', 'hash', 'FPT', 'ACTIVE', 'SE180372', 'Đại học FPT', t.id FROM teams t WHERE t.name = 'YAG';