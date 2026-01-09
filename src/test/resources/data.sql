-- Minimal test data for H2

-- Companies
INSERT INTO company (code, name, is_active) VALUES
('company-a', '테크솔루션 주식회사', true),
('company-b', '글로벌무역 주식회사', true);

-- Boards
INSERT INTO board (company_id, name, type) VALUES
(1, '공지사항', 'notice'),
(1, '보도자료', 'press'),
(2, '공지사항', 'notice');

-- Posts
INSERT INTO post (board_id, title, content, author, view_count, is_hidden) VALUES
(1, '테스트 게시글 1', '테스트 내용 1', '작성자1', 10, false),
(1, '테스트 게시글 2', '테스트 내용 2', '작성자2', 20, false),
(1, '테스트 게시글 3', '테스트 내용 3', '작성자3', 30, false);
