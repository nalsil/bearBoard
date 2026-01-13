import { check, sleep } from 'k6';
import http from 'k6/http';

/**
 * HTTP GET 요청 헬퍼
 * @param {string} url - 요청 URL
 * @param {object} params - 요청 파라미터 (헤더, 태그 등)
 * @param {string} checkName - 체크 이름
 * @returns {object} - 응답 객체
 */
export function httpGet(url, params = {}, checkName = 'HTTP GET') {
    const response = http.get(url, params);

    // 디버깅: 상태 코드가 200이 아닐 때 로그 출력
    if (response.status !== 200) {
        console.log(`[${checkName}] Status: ${response.status}, URL: ${url}`);
        console.log(`[${checkName}] Body preview: ${response.body.substring(0, 200)}`);
    }

    check(response, {
        [`${checkName}: status is 200`]: (r) => r.status === 200,
        [`${checkName}: response time < 1s`]: (r) => r.timings.duration < 1000,
        [`${checkName}: has body`]: (r) => r.body && r.body.length > 0,
    });

    return response;
}

/**
 * HTTP POST 요청 헬퍼
 * @param {string} url - 요청 URL
 * @param {object} payload - 요청 본문
 * @param {object} params - 요청 파라미터 (헤더, 태그 등)
 * @param {string} checkName - 체크 이름
 * @returns {object} - 응답 객체
 */
export function httpPost(url, payload, params = {}, checkName = 'HTTP POST') {
    const response = http.post(url, payload, params);

    check(response, {
        [`${checkName}: status is 2xx or 3xx`]: (r) =>
            r.status >= 200 && r.status < 400,
        [`${checkName}: response time < 2s`]: (r) => r.timings.duration < 2000,
    });

    return response;
}

/**
 * 페이지 로드 시뮬레이션 (HTML + 정적 리소스)
 * @param {string} pageUrl - 페이지 URL
 * @param {array} staticResources - 정적 리소스 URL 배열
 * @param {string} pageName - 페이지 이름
 */
export function simulatePageLoad(pageUrl, staticResources = [], pageName = 'Page') {
    // HTML 페이지 로드
    const htmlResponse = httpGet(pageUrl, {}, `${pageName} HTML`);

    // 정적 리소스 병렬 로드
    if (staticResources.length > 0) {
        const requests = staticResources.map(url => ['GET', url]);
        const responses = http.batch(requests);

        check(responses, {
            [`${pageName}: all static resources loaded`]: (responses) =>
                responses.every(r => r.status === 200 || r.status === 304),
        });
    }

    return htmlResponse;
}

/**
 * 랜덤 대기 (사용자 행동 시뮬레이션)
 * @param {number} min - 최소 대기 시간 (초)
 * @param {number} max - 최대 대기 시간 (초)
 */
export function randomSleep(min = 1, max = 5) {
    const duration = Math.random() * (max - min) + min;
    sleep(duration);
}

/**
 * 페이지네이션 순회
 * @param {function} urlGenerator - URL 생성 함수 (페이지 번호를 인자로 받음)
 * @param {number} maxPages - 최대 페이지 수
 * @param {string} checkName - 체크 이름
 */
export function browsePagination(urlGenerator, maxPages = 3, checkName = 'Pagination') {
    for (let page = 0; page < maxPages; page++) {
        const url = urlGenerator(page);
        httpGet(url, {}, `${checkName} - Page ${page + 1}`);
        randomSleep(0.5, 2);
    }
}

/**
 * 에러율 계산
 * @param {object} response - HTTP 응답 객체
 * @returns {boolean} - 에러 여부
 */
export function isError(response) {
    return !response || response.status === 0 || response.status >= 400;
}

/**
 * 응답 시간 측정 태그 추가
 * @param {string} name - 메트릭 이름
 * @returns {object} - 태그 객체
 */
export function tagWithName(name) {
    return {
        tags: { name: name }
    };
}

/**
 * 사용자 시나리오: 홈페이지 방문자
 * 메인 → 회사소개 → 제품목록 순으로 탐색
 */
export function visitHomepage(endpoints) {
    // 메인 페이지
    httpGet(endpoints.home(), {}, 'Home Page');
    randomSleep(2, 5);

    // 회사 소개
    httpGet(endpoints.about(), {}, 'About Page');
    randomSleep(1, 3);

    // 제품 목록
    httpGet(endpoints.productList(), {}, 'Product List');
    randomSleep(1, 3);
}

/**
 * 사용자 시나리오: 제품 탐색자
 * 제품목록 → 제품상세 → 다른 제품상세 순으로 탐색
 */
export function browseProducts(endpoints, config) {
    // 제품 목록
    httpGet(endpoints.productList(), {}, 'Product List');
    randomSleep(2, 4);

    // 제품 상세 (샘플 ID)
    httpGet(endpoints.productDetail(config.SAMPLE_PRODUCT_ID), {}, 'Product Detail');
    randomSleep(3, 6);

    // 제품 목록으로 돌아가기
    httpGet(endpoints.productList(), {}, 'Product List - Return');
    randomSleep(1, 2);
}

/**
 * 사용자 시나리오: 게시판 독자
 * 게시판 목록 → 게시글 읽기 순으로 탐색
 */
export function readBoard(endpoints, config) {
    const boardType = config.SAMPLE_BOARD_TYPE;

    // 게시판 목록
    httpGet(endpoints.boardList(boardType), {}, `Board List - ${boardType}`);
    randomSleep(2, 4);

    // 게시글 상세
    httpGet(
        endpoints.postDetail(boardType, config.SAMPLE_POST_ID),
        {},
        `Post Detail - ${boardType}`
    );
    randomSleep(3, 7);
}

/**
 * 사용자 시나리오: FAQ 검색자
 * FAQ 목록 조회
 */
export function browseFaq(endpoints) {
    // FAQ 목록
    httpGet(endpoints.faqList(), {}, 'FAQ List');
    randomSleep(2, 5);

    // 카테고리별 조회 (예시)
    httpGet(endpoints.faqList('general'), {}, 'FAQ List - Category');
    randomSleep(1, 3);
}

/**
 * 사용자 시나리오: QnA 조회자
 * QnA 목록 → QnA 상세 순으로 탐색
 */
export function browseQna(endpoints, config) {
    // QnA 목록
    httpGet(endpoints.qnaList(), {}, 'QnA List');
    randomSleep(2, 4);

    // QnA 상세
    httpGet(endpoints.qnaDetail(config.SAMPLE_QNA_ID), {}, 'QnA Detail');
    randomSleep(2, 5);
}

export default {
    httpGet,
    httpPost,
    simulatePageLoad,
    randomSleep,
    browsePagination,
    isError,
    tagWithName,
    visitHomepage,
    browseProducts,
    readBoard,
    browseFaq,
    browseQna,
};
