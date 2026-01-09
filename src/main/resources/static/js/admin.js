// 관리자 페이지 공통 JavaScript

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin page loaded');

    // 자동 알림 메시지 숨김 (3초 후)
    hideAlertAfterDelay();

    // 폼 제출 시 버튼 비활성화 (중복 제출 방지)
    preventDoubleSubmit();
});

/**
 * 알림 메시지 자동 숨김
 */
function hideAlertAfterDelay() {
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => {
                alert.style.display = 'none';
            }, 500);
        }, 3000);
    });
}

/**
 * 폼 중복 제출 방지
 */
function preventDoubleSubmit() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitButtons = form.querySelectorAll('button[type="submit"]');
            submitButtons.forEach(button => {
                button.disabled = true;
                button.style.opacity = '0.6';
                button.textContent = '처리 중...';
            });
        });
    });
}

/**
 * 삭제 확인 대화상자
 */
function confirmDelete(message) {
    return confirm(message || '정말 삭제하시겠습니까?');
}

/**
 * 텍스트 영역 자동 높이 조절
 */
function autoResizeTextarea(textarea) {
    textarea.style.height = 'auto';
    textarea.style.height = textarea.scrollHeight + 'px';
}

// 모든 텍스트 영역에 자동 높이 조절 적용
document.querySelectorAll('textarea').forEach(textarea => {
    textarea.addEventListener('input', function() {
        autoResizeTextarea(this);
    });
    // 초기 로드 시에도 적용
    autoResizeTextarea(textarea);
});
