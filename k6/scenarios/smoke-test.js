/**
 * Smoke Test - 기본 기능 확인
 *
 * 목적: 시스템의 기본 기능이 정상적으로 작동하는지 확인
 * 부하: 최소 (1-2 VU)
 * 기간: 짧음 (1-2분)
 *
 * 사용법:
 *   k6 run scenarios/smoke-test.js
 *   k6 run --env BASE_URL=http://production.com --env COMPANY_CODE=mycompany scenarios/smoke-test.js
 */

import { sleep } from 'k6';
import { config, endpoints } from '../config.js';
import { httpGet, randomSleep } from '../utils/helpers.js';

export const options = {
    // VU(Virtual User) 설정: 최소 부하로 기능 확인
    vus: 1,
    duration: '1m',

    // 성능 임계값
    thresholds: {
        // HTTP 실패율 < 1%
        http_req_failed: ['rate<0.01'],
        // 95% 요청이 1초 이내 응답, 99% 요청이 2초 이내 응답
        http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    },

    // 태그
    tags: {
        testType: 'smoke',
        ...config.TAGS,
    },
};

/**
 * 메인 테스트 함수
 * 모든 공개 엔드포인트를 한 번씩 호출하여 정상 작동 확인
 */
export default function () {
    // 1. 홈페이지
    httpGet(endpoints.home(), {}, 'Smoke - Home');
    sleep(1);

    // 2. 회사 소개
    httpGet(endpoints.about(), {}, 'Smoke - About');
    sleep(1);

    // 3. 제품 목록
    httpGet(endpoints.productList(), {}, 'Smoke - Product List');
    sleep(1);

    // 4. 제품 상세
    httpGet(endpoints.productDetail(), {}, 'Smoke - Product Detail');
    sleep(1);

    // 5. 게시판 목록
    httpGet(endpoints.boardList(), {}, 'Smoke - Board List');
    sleep(1);

    // 6. 게시글 상세
    httpGet(endpoints.postDetail(), {}, 'Smoke - Post Detail');
    sleep(1);

    // 7. FAQ 목록
    httpGet(endpoints.faqList(), {}, 'Smoke - FAQ List');
    sleep(1);

    // 8. QnA 목록
    httpGet(endpoints.qnaList(), {}, 'Smoke - QnA List');
    sleep(1);

    // 9. QnA 상세
    httpGet(endpoints.qnaDetail(), {}, 'Smoke - QnA Detail');
    sleep(1);

    // 10. 유튜브 목록
    httpGet(endpoints.youtubeList(), {}, 'Smoke - YouTube List');
    sleep(1);

    // 11. 유튜브 플레이어
    httpGet(endpoints.youtubePlayer(), {}, 'Smoke - YouTube Player');
    sleep(1);

    // 12. Health Check
    httpGet(`${config.BASE_URL}/actuator/health`, {}, 'Smoke - Health Check');

    randomSleep(1, 2);
}

/**
 * 테스트 설정 단계
 */
export function setup() {
    console.log('='.repeat(60));
    console.log('🧪 Smoke Test - 기본 기능 확인');
    console.log('='.repeat(60));
    console.log(`Base URL: ${config.BASE_URL}`);
    console.log(`Company Code: ${config.COMPANY_CODE}`);
    console.log(`VUs: ${options.vus}`);
    console.log(`Duration: ${options.duration}`);
    console.log('='.repeat(60));
}

/**
 * 테스트 종료 단계
 */
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('✅ Smoke Test 완료');
    console.log('='.repeat(60));
}
