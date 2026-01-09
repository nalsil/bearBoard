-- Test data insertion script for multi-tenant homepage
-- 멀티테넌트 기업용 홈페이지 테스트 데이터

-- Clear existing data (개발 환경에서만 사용)
-- TRUNCATE TABLE youtube_video, product, qna, faq, post, board, admin, company RESTART IDENTITY CASCADE;

-- 1. Company test data (2개 테스트 기업)
INSERT INTO company (code, name, logo_url, primary_color, secondary_color, description, address, phone, email, latitude, longitude, founded_date, is_active) VALUES
('company-a', '테크솔루션 주식회사', 'https://via.placeholder.com/200x80?text=TechSolution', '#2563EB', '#1E40AF', '혁신적인 기술로 미래를 선도하는 소프트웨어 솔루션 기업입니다. AI, 빅데이터, 클라우드 기술을 활용한 맞춤형 솔루션을 제공합니다.', '서울특별시 강남구 테헤란로 123, 테크타워 15층', '02-1234-5678', 'contact@techsolution.co.kr', 37.498095, 127.027610, '2015-03-15', true),
('company-b', '글로벌무역 주식회사', 'https://via.placeholder.com/200x80?text=GlobalTrade', '#059669', '#047857', '글로벌 네트워크를 바탕으로 한국과 세계를 연결하는 종합 무역 기업입니다. 전자제품, 생활용품, 산업자재 등 다양한 품목을 취급합니다.', '부산광역시 해운대구 센텀중앙로 78, 무역센터 20층', '051-9876-5432', 'info@globaltrade.co.kr', 35.169197, 129.130096, '2008-07-22', true);

-- 2. Admin test data (3명의 관리자)
-- 참고: 실제 운영 환경에서는 BCrypt 해시된 비밀번호 사용 필요
-- 테스트용 평문 비밀번호: "password123" → BCrypt 해시 예시 사용
INSERT INTO admin (username, password_hash, name, email, role, company_id, last_login_at) VALUES
('admin-a', '$2a$10$DXqH3Ft8sqIOBoo9TVc6l.2oVJjm2te/KmiwUvhpYwgYoCe9sGdQ.', '김관리', 'admin-a@techsolution.co.kr', 'ADMIN', 1, '2025-12-25 10:30:00'),
('admin-b', '$2a$10$DXqH3Ft8sqIOBoo9TVc6l.2oVJjm2te/KmiwUvhpYwgYoCe9sGdQ.', '이관리', 'admin-b@globaltrade.co.kr', 'ADMIN', 2, '2025-12-28 14:20:00'),
('superadmin', '$2a$10$DXqH3Ft8sqIOBoo9TVc6l.2oVJjm2te/KmiwUvhpYwgYoCe9sGdQ.', '최슈퍼', 'superadmin@system.com', 'SUPER_ADMIN', NULL, '2025-12-30 09:00:00');

-- 3. Board test data (각 회사당 공지사항, 보도자료, 채용정보 게시판)
INSERT INTO board (company_id, name, type) VALUES
(1, '공지사항', 'notice'),
(1, '보도자료', 'press'),
(1, '채용정보', 'recruit'),
(2, '공지사항', 'notice'),
(2, '보도자료', 'press'),
(2, '채용정보', 'recruit');

-- 4. Post test data (각 게시판당 3-5개 샘플 게시글)
INSERT INTO post (board_id, title, content, author, view_count, file_path, is_hidden) VALUES
-- Company A 공지사항
(1, '[중요] 2025년 신년사 및 경영 방침 안내', '안녕하십니까. 테크솔루션 임직원 여러분께 2025년 새해 인사를 드립니다. 올해는 AI 기술 혁신과 글로벌 시장 확대를 중점으로 추진하겠습니다...', '김대표', 245, NULL, false),
(1, '사내 보안 정책 업데이트 안내', '전 임직원 여러분께 알려드립니다. 2025년 1월 1일부터 강화된 보안 정책이 시행됩니다. 주요 변경사항은 다음과 같습니다: 1) 비밀번호 복잡도 강화...', '보안팀', 156, '/uploads/security-policy-2025.pdf', false),
(1, '동계 휴가 일정 안내', '2025년 설 연휴 및 동계 휴가 일정을 안내드립니다. 설 연휴: 1월 28일(화) ~ 1월 30일(목), 대체 휴무: 1월 31일(금)...', '인사팀', 89, NULL, false),

-- Company A 보도자료
(2, '테크솔루션, AI 기반 물류 최적화 솔루션 \'SmartLogix\' 출시', '테크솔루션(대표 김대표)이 AI 기술을 활용한 물류 최적화 솔루션 SmartLogix를 정식 출시했다고 30일 밝혔다. SmartLogix는 머신러닝 알고리즘을 통해 재고 관리...', '홍보팀', 412, NULL, false),
(2, '제15회 소프트웨어 품질대상 우수상 수상', '테크솔루션이 한국소프트웨어산업협회가 주최한 \'제15회 소프트웨어 품질대상\'에서 우수상을 수상했다. 이번 수상은 자체 개발한 품질관리 시스템...', '홍보팀', 328, '/uploads/award-certificate.pdf', false),

-- Company A 채용정보
(3, '[채용] 백엔드 개발자 (경력 3년 이상)', '테크솔루션에서 백엔드 개발자를 모집합니다. 주요 업무: Spring Boot 기반 RESTful API 설계 및 개발, 마이크로서비스 아키텍처 구축...', '채용팀', 567, NULL, false),
(3, '[채용] AI/ML 엔지니어 (신입/경력)', 'AI/ML 엔지니어를 신입 및 경력 구분 없이 모집합니다. 우대사항: PyTorch/TensorFlow 실무 경험, 논문 게재 경험, Kaggle 수상 경력...', '채용팀', 892, NULL, false),

-- Company B 공지사항
(4, '2025년 상반기 해외 전시회 참가 계획', '글로벌무역 임직원 여러분께 알려드립니다. 2025년 상반기 주요 해외 전시회 참가 일정을 공유드립니다. CES 2025 (미국 라스베이거스), MWC 2025 (스페인 바르셀로나)...', '해외영업팀', 178, NULL, false),
(4, '신규 파트너사 등록 절차 안내', '신규 파트너사 등록을 원하시는 업체는 다음 절차를 따라주시기 바랍니다. 1) 사업자등록증 사본 제출, 2) 회사소개서 및 주요 거래품목 명세...', '파트너십팀', 234, '/uploads/partner-guide.pdf', false),

-- Company B 보도자료
(5, '글로벌무역, 베트남 하노이에 해외지사 설립', '글로벌무역(대표 박대표)이 베트남 하노이에 해외지사를 신규 설립했다고 밝혔다. 이번 지사 설립으로 동남아시아 시장 공략을 본격화할 예정이다...', '홍보실', 456, NULL, false),

-- Company B 채용정보
(6, '[채용] 해외영업 담당자 (영어/중국어 가능자)', '해외영업 담당자를 모집합니다. 자격요건: 영어 또는 중국어 비즈니스 회화 가능, 무역 실무 경험 2년 이상, 해외 출장 가능자...', '인사팀', 623, NULL, false);

-- 5. FAQ test data (각 회사당 5-8개 자주 묻는 질문)
INSERT INTO faq (company_id, category, question, answer, display_order, is_hidden) VALUES
-- Company A FAQ
(1, '서비스 이용', '솔루션 도입 절차가 어떻게 되나요?', '솔루션 도입 절차는 다음과 같습니다: 1) 초기 상담 및 요구사항 분석 → 2) 제안서 작성 및 제출 → 3) 계약 체결 → 4) 시스템 구축 및 커스터마이징 → 5) 테스트 및 검수 → 6) 정식 오픈 및 운영. 평균 소요기간은 3~6개월입니다.', 1, false),
(1, '서비스 이용', '클라우드 서비스와 온프레미스 중 어떤 것을 선택해야 하나요?', '고객사의 보안 정책, 예산, 인프라 현황에 따라 달라집니다. 클라우드는 초기 비용이 낮고 유연한 확장이 가능하며, 온프레미스는 데이터 통제권과 보안성이 높습니다. 무료 컨설팅을 통해 최적의 방안을 제안드립니다.', 2, false),
(1, '기술 지원', '기술 지원은 어떻게 받을 수 있나요?', '기술 지원은 다음 채널을 통해 제공됩니다: 1) 이메일: support@techsolution.co.kr, 2) 전화: 02-1234-5678 (평일 09:00~18:00), 3) 고객 포털 티켓 시스템. 긴급 장애 발생 시 24시간 핫라인(02-1234-9999)을 이용해 주세요.', 3, false),
(1, '기술 지원', 'SLA(서비스 수준 협약) 내용이 궁금합니다.', '표준 SLA는 다음과 같습니다: 시스템 가용성 99.9%, 긴급 장애 대응 시간 1시간 이내, 일반 문의 응답 시간 4시간 이내. 프리미엄 SLA 옵션도 제공하며 상세 내용은 영업팀에 문의해 주세요.', 4, false),
(1, '요금/결제', '라이선스 비용은 어떻게 산정되나요?', '라이선스 비용은 사용자 수, 기능 모듈, 계약 기간에 따라 산정됩니다. 기본 패키지는 월 사용자당 50,000원부터 시작하며, 대량 구매 시 할인 혜택이 적용됩니다.', 5, false),
(1, '요금/결제', '무료 체험판이 있나요?', '네, 모든 솔루션은 30일 무료 체험판을 제공합니다. 제한된 사용자 수(최대 10명) 내에서 모든 기능을 테스트해 보실 수 있습니다. 홈페이지에서 직접 신청 가능합니다.', 6, false),

-- Company B FAQ
(2, '거래 문의', '최소 주문 수량이 있나요?', '품목별로 최소 주문 수량(MOQ)이 다릅니다. 전자제품은 일반적으로 100개 이상, 생활용품은 500개 이상이며, 협의를 통해 조정 가능합니다. 상세 내용은 담당 영업사원에게 문의해 주세요.', 1, false),
(2, '거래 문의', '샘플 제공이 가능한가요?', '네, 신규 거래처의 경우 품질 확인을 위한 샘플을 제공합니다. 샘플 비용은 정식 계약 체결 시 첫 주문 금액에서 차감됩니다.', 2, false),
(2, '배송/물류', '해외 배송 기간은 얼마나 걸리나요?', '배송 국가와 운송 방법에 따라 다릅니다. 항공: 5~7일, 해상: 20~30일 소요됩니다. 긴급 배송(Express)도 가능하며 추가 비용이 발생합니다.', 3, false),
(2, '배송/물류', '통관 절차는 어떻게 진행되나요?', '당사가 통관 대행 서비스를 제공합니다. 필요 서류(상업송장, 패킹리스트, 원산지증명서 등)를 준비해 주시면 전문 관세사가 통관 업무를 대행합니다.', 4, false),
(2, '결제/정산', '결제 조건은 어떻게 되나요?', '기본 결제 조건은 T/T 30% 선입금, 70% 선적 전 입금입니다. 신용도가 검증된 거래처는 L/C 또는 D/A 조건도 협의 가능합니다.', 5, false),
(2, '품질/A/S', '불량품 발생 시 처리 절차는 어떻게 되나요?', '불량품 발생 시 즉시 담당자에게 연락 주시고, 사진 및 수량을 제공해 주세요. 검수 후 교체 또는 환불 처리해 드립니다. 당사 귀책 사유일 경우 배송비도 당사 부담입니다.', 6, false);

-- 6. QnA test data (각 회사당 3-5개 질문 및 일부 답변)
INSERT INTO qna (company_id, question_title, question_body, asker_email, answer_body, answerer_id, is_answered, is_hidden, answered_at) VALUES
-- Company A QnA
(1, 'API 연동 가이드 문서 요청', 'SmartLogix 솔루션의 RESTful API 연동 가이드 문서를 요청드립니다. Swagger 문서나 Postman Collection이 있으면 함께 제공해 주시면 감사하겠습니다.', 'developer@clientcompany.com', 'API 연동 가이드 문서를 이메일로 발송해 드렸습니다. Swagger UI는 https://api.techsolution.co.kr/swagger-ui.html 에서 확인 가능하며, Postman Collection은 첨부 파일로 보내드렸습니다. 추가 문의사항이 있으시면 언제든 연락 주세요.', 1, true, false, '2025-12-28 11:30:00'),
(1, '대용량 데이터 처리 성능 문의', '일 평균 10만 건 이상의 데이터 처리가 필요한데, SmartLogix가 이를 안정적으로 처리할 수 있는지 궁금합니다. 성능 벤치마크 자료가 있을까요?', 'cto@logistics-corp.com', '네, SmartLogix는 시간당 최대 50만 건의 데이터 처리가 가능하도록 설계되었습니다. 실제 고객사 운영 사례에서 일 평균 30만 건 이상을 안정적으로 처리하고 있습니다. 성능 벤치마크 보고서를 이메일로 발송해 드리겠습니다.', 1, true, false, '2025-12-29 14:20:00'),
(1, '온프레미스 설치 시 서버 사양 문의', '온프레미스로 설치하려고 하는데, 권장 서버 사양이 어떻게 되는지 알고 싶습니다. 사용자는 약 100명 예상됩니다.', 'infra@manufacturing.co.kr', NULL, NULL, false, false, NULL),

-- Company B QnA
(2, '유럽 시장 진출 관련 상담 요청', '당사는 가전제품 제조업체이며, 유럽 시장 진출을 계획하고 있습니다. 글로벌무역에서 유럽 바이어 연결 및 수출 대행이 가능한지 문의드립니다.', 'export@homeappliance.com', '유럽 시장 진출 상담 요청 감사합니다. 당사는 독일, 프랑스, 영국 등에 다수의 바이어 네트워크를 보유하고 있으며, 수출 대행 서비스를 제공합니다. 자세한 상담을 위해 다음 주 화요일 오후 2시 미팅을 제안드립니다. 일정 조율 부탁드립니다.', 2, true, false, '2025-12-27 16:45:00'),
(2, '친환경 인증 제품 수입 가능 여부', 'EU의 친환경 인증(Ecolabel)을 받은 생활용품을 수입하고 싶은데, 취급 가능한 품목인지 확인 부탁드립니다.', 'purchasing@eco-retail.com', NULL, NULL, false, false, NULL),
(2, '베트남 지사 방문 일정 협의', '베트남 현지 공장 방문 및 샘플 확인을 위해 하노이 지사 방문을 희망합니다. 방문 가능한 일정을 알려주시면 감사하겠습니다.', 'manager@tradingkorea.com', '베트남 하노이 지사 방문 일정 협의 건입니다. 다음 주 수요일~금요일 중 방문 가능하시면 좋겠습니다. 확정되면 공항 픽업 및 호텔 예약 지원해 드리겠습니다.', 2, true, false, '2025-12-29 10:15:00');

-- 7. YouTube Video test data (각 회사당 3-5개 영상)
INSERT INTO youtube_video (company_id, video_url, title, description, thumbnail_url, display_order, is_hidden) VALUES
-- Company A YouTube
(1, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'SmartLogix 솔루션 소개 영상', 'AI 기반 물류 최적화 솔루션 SmartLogix의 주요 기능과 도입 효과를 소개합니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 1, false),
(1, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '고객 성공 사례: A물류센터 도입 후기', 'SmartLogix 도입 후 업무 효율 40% 향상, 재고 회전율 25% 개선을 달성한 A물류센터 사례를 소개합니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 2, false),
(1, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '2025 테크솔루션 비전 영상', '테크솔루션의 2025년 비전과 핵심 전략을 소개하는 영상입니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 3, false),

-- Company B YouTube
(2, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '글로벌무역 회사 소개', '20년 전통의 종합 무역 기업 글로벌무역의 사업 분야와 글로벌 네트워크를 소개합니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 1, false),
(2, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', '베트남 하노이 지사 오픈 현장', '2025년 1월 신규 오픈한 베트남 하노이 지사의 개소식 및 사무실 투어 영상입니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 2, false),
(2, 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 'CES 2025 참가 후기', 'CES 2025 전시회에서 발굴한 혁신 제품과 신규 파트너사를 소개합니다.', 'https://i.ytimg.com/vi/dQw4w9WgXcQ/hqdefault.jpg', 3, false);

-- 8. Product test data (각 회사당 5-8개 상품/서비스)
INSERT INTO product (company_id, name, category, description, price, image_url, display_order, is_hidden) VALUES
-- Company A Products (솔루션/서비스)
(1, 'SmartLogix Standard', 'AI 솔루션', 'AI 기반 물류 최적화 솔루션 표준 패키지. 재고 관리, 입출고 자동화, 배송 경로 최적화 기능 제공.', 5000000.00, 'https://via.placeholder.com/400x300?text=SmartLogix+Standard', 1, false),
(1, 'SmartLogix Enterprise', 'AI 솔루션', 'SmartLogix 엔터프라이즈 패키지. Standard 기능 + 고급 분석, API 연동, 전담 기술지원 포함.', 12000000.00, 'https://via.placeholder.com/400x300?text=SmartLogix+Enterprise', 2, false),
(1, 'CloudSync Pro', '클라우드 서비스', '실시간 데이터 동기화 클라우드 서비스. 멀티 디바이스 지원, 자동 백업, 99.9% 가용성 보장.', 300000.00, 'https://via.placeholder.com/400x300?text=CloudSync+Pro', 3, false),
(1, 'SecureVault', '보안 솔루션', '기업용 데이터 암호화 및 접근 제어 솔루션. AES-256 암호화, 역할 기반 권한 관리, 감사 로그 제공.', 2500000.00, 'https://via.placeholder.com/400x300?text=SecureVault', 4, false),
(1, '기술 지원 패키지 (연간)', '기술 지원', '전담 엔지니어 배정, 24/7 긴급 지원, 분기별 시스템 점검, 무제한 원격 지원 제공.', 8000000.00, 'https://via.placeholder.com/400x300?text=Support+Package', 5, false),

-- Company B Products (무역 상품)
(2, '갤럭시 스마트폰 (해외 수출용)', '전자제품', '삼성 갤럭시 최신 모델 해외 수출 전용 제품. 글로벌 워런티 지원, 다국어 펌웨어 탑재.', 850000.00, 'https://via.placeholder.com/400x300?text=Galaxy+Export', 1, false),
(2, '4K UHD 스마트 TV 55인치', '전자제품', '55인치 4K UHD 해상도 스마트 TV. HDR10+ 지원, 음성인식 리모컨 포함. 유럽/북미 인증 완료.', 1200000.00, 'https://via.placeholder.com/400x300?text=Smart+TV+55', 2, false),
(2, '친환경 대나무 칫솔 세트 (100개입)', '생활용품', 'EU Ecolabel 인증 친환경 대나무 칫솔. 생분해성 포장재 사용, 비건 인증 완료.', 35000.00, 'https://via.placeholder.com/400x300?text=Bamboo+Toothbrush', 3, false),
(2, '프리미엄 녹차 세트', '식품', '제주산 유기농 녹차 프리미엄 세트. 개별 티백 50개입, 고급 선물 포장. 해외 수출 전용.', 45000.00, 'https://via.placeholder.com/400x300?text=Green+Tea+Set', 4, false),
(2, '산업용 LED 조명 (50W)', '산업자재', '고효율 산업용 LED 조명 50W. IP65 방수 등급, 50,000시간 수명, CE/UL 인증.', 120000.00, 'https://via.placeholder.com/400x300?text=LED+Industrial', 5, false),
(2, '스테인리스 주방용품 세트', '생활용품', '고급 스테인리스 주방용품 10종 세트. 식기세척기 사용 가능, FDA 승인.', 180000.00, 'https://via.placeholder.com/400x300?text=Kitchen+Set', 6, false);
