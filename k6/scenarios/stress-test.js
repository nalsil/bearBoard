/**
 * Stress Test - 한계 테스트
 *
 * 목적: 시스템의 한계점과 장애 복구 능력 확인
 * 부하: 높음 (50-200+ VU)
 * 기간: 중간 (10-15분)
 *
 * 단계:
 *   1. Ramp-up: 0 → 50 VU (2분)
 *   2. Normal Load: 50 VU (3분)
 *   3. Stress: 50 → 100 VU (2분)
 *   4. High Stress: 100 → 200 VU (2분)
 *   5. Peak: 200 VU 유지 (3분)
 *   6. Recovery: 200 → 0 VU (2분)
 *
 * 사용법:
 *   k6 run scenarios/stress-test.js
 *   k6 run --env BASE_URL=http://staging.com scenarios/stress-test.js
 *
 * 주의: 프로덕션 환경에서는 신중하게 실행할 것!
 */

import { sleep } from 'k6';
import { config, endpoints } from '../config.js';
import {
    httpGet,
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
        { duration: '2m', target: 50 },   // Ramp-up
        { duration: '3m', target: 50 },   // Normal load
        { duration: '2m', target: 100 },  // Stress
        { duration: '2m', target: 200 },  // High stress
        { duration: '3m', target: 200 },  // Peak stress
        { duration: '2m', target: 0 },    // Recovery
    ],

    // 성능 임계값 (Stress Test에서는 더 관대하게 설정)
    thresholds: {
        // HTTP 실패율 < 5% (스트레스 상황에서는 일부 실패 허용)
        http_req_failed: ['rate<0.05'],
        // 95% 요청이 2초 이내 응답, 99% 요청이 5초 이내, 평균 < 1초
        http_req_duration: ['p(95)<2000', 'p(99)<5000', 'avg<1000'],
    },

    // 태그
    tags: {
        testType: 'stress',
        ...config.TAGS,
    },
};

/**
 * 메인 테스트 함수
 * 다양한 사용자 시나리오를 혼합하여 실행
 */
export default function () {
    const scenario = Math.random();

    try {
        if (scenario < 0.25) {
            // 25%: 홈페이지 방문자
            visitHomepage(endpoints);
        } else if (scenario < 0.50) {
            // 25%: 제품 탐색자
            browseProducts(endpoints, config);
        } else if (scenario < 0.70) {
            // 20%: 게시판 독자
            readBoard(endpoints, config);
        } else if (scenario < 0.85) {
            // 15%: FAQ 검색자
            browseFaq(endpoints);
        } else {
            // 15%: QnA 조회자
            browseQna(endpoints, config);
        }
    } catch (error) {
        console.error(`Error in scenario: ${error.message}`);
    }

    // 스트레스 상황에서는 대기 시간 짧게
    randomSleep(1, 3);
}

/**
 * 테스트 설정 단계
 */
export function setup() {
    console.log('='.repeat(60));
    console.log('⚠️  Stress Test - 한계 테스트');
    console.log('='.repeat(60));
    console.log(`Base URL: ${config.BASE_URL}`);
    console.log(`Company Code: ${config.COMPANY_CODE}`);
    console.log('');
    console.log('⚠️  주의: 이 테스트는 시스템에 높은 부하를 가합니다!');
    console.log('    프로덕션 환경에서는 신중하게 실행하세요.');
    console.log('');
    console.log('Stages:');
    options.stages.forEach((stage, index) => {
        console.log(`  ${index + 1}. ${stage.duration} → ${stage.target} VU`);
    });
    console.log('');
    console.log('관찰 포인트:');
    console.log('  - 어느 시점에서 응답 시간이 급격히 증가하는가?');
    console.log('  - 에러율이 급증하는 VU 수는?');
    console.log('  - 시스템 리소스 사용률 (CPU, Memory, DB Connections)');
    console.log('  - 부하 감소 후 시스템 복구 시간');
    console.log('='.repeat(60));
}

/**
 * 테스트 종료 단계
 */
export function teardown(data) {
    console.log('='.repeat(60));
    console.log('✅ Stress Test 완료');
    console.log('='.repeat(60));
    console.log('분석 항목:');
    console.log('  1. 시스템 한계점 (Breaking Point)');
    console.log('  2. 에러 발생 시점 및 원인');
    console.log('  3. 복구 시간 및 안정성');
    console.log('  4. 리소스 병목 지점');
    console.log('  5. Auto-scaling 설정 최적화');
    console.log('='.repeat(60));
}
