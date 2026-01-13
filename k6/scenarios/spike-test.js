/**
 * Spike Test - 급격한 트래픽 증가 테스트
 *
 * 목적: 갑작스런 트래픽 급증 상황에서 시스템의 안정성 확인
 * 부하: 순간적으로 매우 높음 (0 → 500+ VU)
 * 기간: 짧음 (5-7분)
 *
 * 시나리오:
 *   - 마케팅 이벤트, SNS 바이럴, 뉴스 보도 등으로 인한 급격한 트래픽 증가
 *   - 정상 → 스파이크 → 정상 패턴 반복
 *
 * 단계:
 *   1. Normal: 10 VU (1분)
 *   2. Spike 1: 10 → 300 VU (30초)
 *   3. Spike Hold: 300 VU (1분)
 *   4. Recovery 1: 300 → 10 VU (30초)
 *   5. Normal: 10 VU (1분)
 *   6. Spike 2: 10 → 500 VU (30초)
 *   7. Spike Hold: 500 VU (1분)
 *   8. Recovery 2: 500 → 0 VU (1분)
 *
 * 사용법:
 *   k6 run scenarios/spike-test.js
 *   k6 run --env BASE_URL=http://staging.com scenarios/spike-test.js
 */

import { sleep } from 'k6';
import { config, endpoints } from '../config.js';
import {
    httpGet,
    visitHomepage,
    browseProducts,
    readBoard,
    randomSleep,
} from '../utils/helpers.js';

export const options = {
    // 스파이크 패턴 설정
    stages: [
        { duration: '1m', target: 10 },   // Normal load
        { duration: '30s', target: 300 }, // Spike 1
        { duration: '1m', target: 300 },  // Spike hold
        { duration: '30s', target: 10 },  // Recovery 1
        { duration: '1m', target: 10 },   // Normal load
        { duration: '30s', target: 500 }, // Spike 2 (더 큰 스파이크)
        { duration: '1m', target: 500 },  // Spike hold
        { duration: '1m', target: 0 },    // Recovery 2
    ],

    // 성능 임계값
    thresholds: {
        // Spike 상황에서는 일부 실패 허용
        http_req_failed: ['rate<0.10'],
        // 95% 요청이 3초 이내 응답, 99% 요청이 10초 이내
        http_req_duration: ['p(95)<3000', 'p(99)<10000'],
    },

    // 태그
    tags: {
        testType: 'spike',
        ...config.TAGS,
    },
};

/**
 * 메인 테스트 함수
 * 스파이크 상황에서 가장 많이 접근하는 페이지 중심으로 테스트
 */
export default function () {
    const scenario = Math.random();

    try {
        if (scenario < 0.50) {
            // 50%: 홈페이지 방문자 (바이럴 시 가장 많이 증가)
            httpGet(endpoints.home(), {}, 'Spike - Home');
            randomSleep(1, 2);

            // 일부 사용자는 회사 소개로 이동
            if (Math.random() < 0.3) {
                httpGet(endpoints.about(), {}, 'Spike - About');
                randomSleep(1, 2);
            }
        } else if (scenario < 0.80) {
            // 30%: 제품 탐색 (이벤트 제품 페이지 접근)
            httpGet(endpoints.productList(), {}, 'Spike - Product List');
            randomSleep(1, 2);

            httpGet(endpoints.productDetail(), {}, 'Spike - Product Detail');
            randomSleep(1, 2);
        } else {
            // 20%: 게시판 (이벤트 공지 확인)
            httpGet(endpoints.boardList(), {}, 'Spike - Board List');
            randomSleep(1, 2);

            httpGet(endpoints.postDetail(), {}, 'Spike - Post Detail');
            randomSleep(1, 2);
        }
    } catch (error) {
        console.error(`Error in spike scenario: ${error.message}`);
    }

    // 스파이크 상황에서는 매우 짧은 대기 시간
    sleep(1);
}

/**
 * 테스트 설정 단계
 */
export function setup() {
    console.log('='.repeat(60));
    console.log('⚡ Spike Test - 급격한 트래픽 증가 테스트');
    console.log('='.repeat(60));
    console.log(`Base URL: ${config.BASE_URL}`);
    console.log(`Company Code: ${config.COMPANY_CODE}`);
    console.log('');
    console.log('⚡ 시뮬레이션 시나리오:');
    console.log('    SNS 바이럴, 뉴스 보도, 마케팅 이벤트 등으로');
    console.log('    순간적으로 트래픽이 50배 이상 증가하는 상황');
    console.log('');
    console.log('Spike Patterns:');
    console.log('  1. Normal (10 VU) → Spike 1 (300 VU) → Recovery');
    console.log('  2. Normal (10 VU) → Spike 2 (500 VU) → Recovery');
    console.log('');
    console.log('관찰 포인트:');
    console.log('  - 스파이크 발생 시 시스템 응답 속도');
    console.log('  - 에러율 및 타임아웃 발생 여부');
    console.log('  - Auto-scaling 반응 시간');
    console.log('  - 트래픽 감소 후 복구 시간');
    console.log('  - 큐잉 시스템 동작 여부');
    console.log('='.repeat(60));
}

/**
 * 테스트 종료 단계
 */
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('✅ Spike Test 완료');
    console.log('='.repeat(60));
    console.log('분석 항목:');
    console.log('  1. 스파이크 발생 시 시스템 응답 시간 변화');
    console.log('  2. 에러율 및 실패한 요청 수');
    console.log('  3. Auto-scaling 효과 (있는 경우)');
    console.log('  4. 캐싱 전략의 효과');
    console.log('  5. CDN 및 로드 밸런서 성능');
    console.log('  6. 데이터베이스 연결 풀 설정 적정성');
    console.log('');
    console.log('권장 조치:');
    console.log('  - Rate Limiting 설정');
    console.log('  - 캐싱 전략 강화');
    console.log('  - Auto-scaling 정책 조정');
    console.log('  - CDN 활용 확대');
    console.log('='.repeat(60));
}
