// 공통 JavaScript - 모든 페이지에서 사용

// 페이지 로드 완료 시 실행
document.addEventListener('DOMContentLoaded', function() {
    console.log('Page loaded successfully');

    // 모바일 메뉴 토글 (추후 추가)
    initMobileMenu();

    // 이미지 로드 에러 처리
    handleImageErrors();

    // 외부 링크 새 창으로 열기
    handleExternalLinks();
});

/**
 * 모바일 메뉴 초기화
 */
function initMobileMenu() {
    // TODO: 모바일 메뉴 토글 기능 구현
}

/**
 * 이미지 로드 에러 처리
 */
function handleImageErrors() {
    const images = document.querySelectorAll('img');
    images.forEach(function(img) {
        img.addEventListener('error', function() {
            this.style.display = 'none';
            console.warn('Image failed to load:', this.src);
        });
    });
}

/**
 * 외부 링크 새 창으로 열기
 */
function handleExternalLinks() {
    const links = document.querySelectorAll('a[href^="http"]');
    links.forEach(function(link) {
        const currentDomain = window.location.hostname;
        const linkDomain = new URL(link.href).hostname;

        if (currentDomain !== linkDomain) {
            link.setAttribute('target', '_blank');
            link.setAttribute('rel', 'noopener noreferrer');
        }
    });
}

/**
 * 페이지 상단으로 스크롤
 */
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

/**
 * 특정 요소로 스크롤
 * @param {string} selector - CSS 선택자
 */
function scrollToElement(selector) {
    const element = document.querySelector(selector);
    if (element) {
        element.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
    }
}

/**
 * 간단한 알림 표시
 * @param {string} message - 표시할 메시지
 * @param {string} type - 알림 타입 (success, error, info)
 */
function showNotification(message, type = 'info') {
    // TODO: 더 나은 알림 UI 구현
    alert(message);
}

/**
 * 날짜 포맷 함수
 * @param {Date} date - 포맷할 날짜
 * @param {string} format - 포맷 문자열
 * @returns {string} 포맷된 날짜 문자열
 */
function formatDate(date, format = 'YYYY-MM-DD') {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day);
}
