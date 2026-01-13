/**
 * k6 Load Test Configuration
 *
 * 공통 설정 및 환경 변수 관리
 */

// 환경 변수에서 설정 가져오기 (기본값 제공)
export const config = {
    // 기본 URL 설정
    BASE_URL: __ENV.BASE_URL || 'http://localhost:8080',

    // 테스트할 기업 코드 (실제 DB에 존재하는 코드로 변경 필요)
    COMPANY_CODE: __ENV.COMPANY_CODE || 'demo-company',

    // 샘플 데이터 ID (실제 DB에 존재하는 ID로 변경 필요)
    SAMPLE_PRODUCT_ID: __ENV.SAMPLE_PRODUCT_ID || '1',
    SAMPLE_POST_ID: __ENV.SAMPLE_POST_ID || '1',
    SAMPLE_FAQ_ID: __ENV.SAMPLE_FAQ_ID || '1',
    SAMPLE_QNA_ID: __ENV.SAMPLE_QNA_ID || '1',
    SAMPLE_YOUTUBE_ID: __ENV.SAMPLE_YOUTUBE_ID || '1',
    SAMPLE_BOARD_TYPE: __ENV.SAMPLE_BOARD_TYPE || 'notice',

    // 성능 임계값 설정
    THRESHOLDS: {
        // HTTP 요청 실패율 < 1%
        http_req_failed: ['rate<0.01'],
        // 95% 요청이 500ms 이내 응답, 99% 요청이 1000ms 이내 응답
        http_req_duration: ['p(95)<500', 'p(99)<1000'],
    },

    // 태그 설정 (결과 분석용)
    TAGS: {
        testType: __ENV.TEST_TYPE || 'load',
        environment: __ENV.ENVIRONMENT || 'local',
    }
};

/**
 * 엔드포인트 경로 생성 헬퍼
 */
export function buildUrl(path) {
    const companyPath = path.startsWith('/') ? path : `/${path}`;
    return `${config.BASE_URL}/${config.COMPANY_CODE}${companyPath}`;
}

/**
 * 정적 리소스 URL
 */
export const staticUrls = {
    css: `${config.BASE_URL}/css/style.css`,
    js: `${config.BASE_URL}/js/main.js`,
    health: `${config.BASE_URL}/actuator/health`,
};

/**
 * 공개 엔드포인트 URL 생성
 */
export const endpoints = {
    // Home
    home: () => buildUrl(''),
    about: () => buildUrl('/about'),

    // Products
    productList: (category = null) => {
        const url = buildUrl('/products');
        return category ? `${url}?category=${category}` : url;
    },
    productDetail: (id) => buildUrl(`/products/${id || config.SAMPLE_PRODUCT_ID}`),

    // Board
    boardList: (boardType, page = 0) =>
        buildUrl(`/board/${boardType || config.SAMPLE_BOARD_TYPE}?page=${page}`),
    postDetail: (boardType, postId) =>
        buildUrl(`/board/${boardType || config.SAMPLE_BOARD_TYPE}/${postId || config.SAMPLE_POST_ID}`),

    // FAQ
    faqList: (category = null, keyword = null) => {
        let url = buildUrl('/faq');
        const params = [];
        if (category) params.push(`category=${category}`);
        if (keyword) params.push(`keyword=${keyword}`);
        return params.length > 0 ? `${url}?${params.join('&')}` : url;
    },

    // QnA
    qnaList: (page = 0) => buildUrl(`/qna?page=${page}`),
    qnaDetail: (id) => buildUrl(`/qna/${id || config.SAMPLE_QNA_ID}`),
    qnaForm: () => buildUrl('/qna/new'),

    // YouTube
    youtubeList: () => buildUrl('/youtube'),
    youtubePlayer: (id) => buildUrl(`/youtube/${id || config.SAMPLE_YOUTUBE_ID}`),
};

export default config;
