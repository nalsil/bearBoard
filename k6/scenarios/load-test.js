/**
 * Load Test - 일반 부하 테스트
 *
 * 목적: 정상 트래픽 상황에서 시스템 성능 측정
 * 부하: 중간 (10-50 VU)
 * 기간: 중간 (5-10분)
 *
 * 단계:
 *   1. Ramp-up: 0 → 50 VU (2분)
 *   2. Steady: 50 VU 유지 (5분)
 *   3. Ramp-down: 50 → 0 VU (1분)
 *
 * 사용법:
 *   k6 run scenarios/load-test.js
 *   k6 run --env BASE_URL=http://production.com scenarios/load-test.js
 */

import { sleep } from 'k6';
import { config, endpoints } from '../config.js';
import {
    visitHomepage,
    browseProducts,
    readBoard,
    browseFaq,
    browseQna,
    randomSleep,
} from '../utils/helpers.js';

export const options = {
    // 부하 단계별 설정
    stages: [
        { duration: '2m', target: 20 },  // Ramp-up: 0 → 20 VU
        { duration: '5m', target: 50 },  // Steady: 20 → 50 VU
        { duration: '2m', target: 50 },  // Steady: 50 VU 유지
        { duration: '1m', target: 0 },   // Ramp-down: 50 → 0 VU
    ],

    // 성능 임계값
    thresholds: {
        // HTTP 실패율 < 1%
        http_req_failed: ['rate<0.01'],
        // 95% 요청이 500ms 이내 응답, 99% 요청이 1초 이내, 평균 < 300ms
        http_req_duration: ['p(95)<500', 'p(99)<1000', 'avg<300'],
        // 요청 처리율 > 100 req/s
        http_reqs: ['rate>100'],
    },

    // 태그
    tags: {
        testType: 'load',
        ...config.TAGS,
    },
};

/**
 * 메인 테스트 함수
 * 실제 사용자 행동 패턴 시뮬레이션
 */
export default function () {
    // VU별로 다른 시나리오 실행 (가중치 기반)
    const scenario = Math.random();

    if (scenario < 0.3) {
        // 30%: 홈페이지 방문자
        visitHomepage(endpoints);
    } else if (scenario < 0.6) {
        // 30%: 제품 탐색자
        browseProducts(endpoints, config);
    } else if (scenario < 0.8) {
        // 20%: 게시판 독자
        readBoard(endpoints, config);
    } else if (scenario < 0.9) {
        // 10%: FAQ 검색자
        browseFaq(endpoints);
    } else {
        // 10%: QnA 조회자
        browseQna(endpoints, config);
    }

    // 사용자 대기 시간 (다음 액션까지)
    randomSleep(3, 8);
}

/**
 * 테스트 설정 단계
 */
export function setup() {
    console.log('='.repeat(60));
    console.log('📊 Load Test - 일반 부하 테스트');
    console.log('='.repeat(60));
    console.log(`Base URL: ${config.BASE_URL}`);
    console.log(`Company Code: ${config.COMPANY_CODE}`);
    console.log('Stages:');
    options.stages.forEach((stage, index) => {
        console.log(`  ${index + 1}. ${stage.duration} → ${stage.target} VU`);
    });
    console.log('User Scenarios:');
    console.log('  - 30% Homepage Visitors');
    console.log('  - 30% Product Browsers');
    console.log('  - 20% Board Readers');
    console.log('  - 10% FAQ Searchers');
    console.log('  - 10% QnA Browsers');
    console.log('='.repeat(60));
}

/**
 * 테스트 종료 단계
 */
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('✅ Load Test 완료');
    console.log('='.repeat(60));
    console.log('다음 단계:');
    console.log('  1. 결과 분석: k6 cloud로 시각화');
    console.log('  2. 성능 개선: 응답 시간이 임계값을 초과한 엔드포인트 확인');
    console.log('  3. 용량 계획: 처리량 기반 서버 리소스 계산');
    console.log('='.repeat(60));
}
