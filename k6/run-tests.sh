#!/bin/bash

# k6 테스트 실행 헬퍼 스크립트
# 사용법: ./run-tests.sh [smoke|load|stress|spike|all] [options]

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 기본값 설정
TEST_TYPE="${1:-smoke}"
BASE_URL="${BASE_URL:-http://localhost:8080}"
COMPANY_CODE="${COMPANY_CODE:-company-a}"
SAMPLE_PRODUCT_ID="${SAMPLE_PRODUCT_ID:-1}"
SAMPLE_POST_ID="${SAMPLE_POST_ID:-1}"
SAMPLE_BOARD_TYPE="${SAMPLE_BOARD_TYPE:-notice}"

# 배너 출력
print_banner() {
    echo -e "${BLUE}"
    echo "╔════════════════════════════════════════════════════════════╗"
    echo "║          k6 Load & Stress Testing Suite                   ║"
    echo "║          Bear Application Performance Tests                ║"
    echo "╚════════════════════════════════════════════════════════════╝"
    echo -e "${NC}"
}

# 사용법 출력
print_usage() {
    echo -e "${YELLOW}사용법:${NC}"
    echo "  ./run-tests.sh [test-type] [options]"
    echo ""
    echo -e "${YELLOW}Test Types:${NC}"
    echo "  smoke   - 기본 기능 확인 (1 VU, 1분)"
    echo "  load    - 일반 부하 테스트 (10-50 VU, 10분)"
    echo "  stress  - 한계 테스트 (50-200 VU, 14분)"
    echo "  spike   - 급격한 트래픽 증가 (10-500 VU, 7분)"
    echo "  all     - 모든 테스트 순차 실행"
    echo ""
    echo -e "${YELLOW}환경 변수:${NC}"
    echo "  BASE_URL            - 애플리케이션 URL (기본: http://localhost:8080)"
    echo "  COMPANY_CODE        - 기업 코드 (기본: company-a)"
    echo "  SAMPLE_PRODUCT_ID   - 샘플 제품 ID (기본: 1)"
    echo "  SAMPLE_POST_ID      - 샘플 게시글 ID (기본: 1)"
    echo "  SAMPLE_BOARD_TYPE   - 샘플 게시판 타입 (기본: notice)"
    echo ""
    echo -e "${YELLOW}예시:${NC}"
    echo "  ./run-tests.sh smoke"
    echo "  BASE_URL=http://staging.example.com COMPANY_CODE=mycompany ./run-tests.sh load"
    echo "  ./run-tests.sh all"
}

# 애플리케이션 상태 확인
check_application() {
    echo -e "${BLUE}🔍 애플리케이션 상태 확인...${NC}"

    if curl -sf "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 애플리케이션이 실행 중입니다.${NC}"
        return 0
    else
        echo -e "${RED}❌ 애플리케이션에 연결할 수 없습니다.${NC}"
        echo -e "${YELLOW}URL: $BASE_URL/actuator/health${NC}"
        echo ""
        echo "다음을 확인해주세요:"
        echo "  1. 애플리케이션이 실행 중인지 확인"
        echo "  2. BASE_URL이 올바른지 확인"
        echo "  3. 방화벽/네트워크 설정 확인"
        return 1
    fi
}

# 테스트 설정 출력
print_config() {
    echo -e "${BLUE}⚙️  테스트 설정:${NC}"
    echo "  Base URL:         $BASE_URL"
    echo "  Company Code:     $COMPANY_CODE"
    echo "  Product ID:       $SAMPLE_PRODUCT_ID"
    echo "  Post ID:          $SAMPLE_POST_ID"
    echo "  Board Type:       $SAMPLE_BOARD_TYPE"
    echo ""
}

# 단일 테스트 실행
run_test() {
    local test_type=$1
    local script_file="scenarios/${test_type}-test.js"

    echo -e "${BLUE}🚀 ${test_type^} Test 실행 중...${NC}"
    echo ""

    if [ ! -f "$script_file" ]; then
        echo -e "${RED}❌ 테스트 파일을 찾을 수 없습니다: $script_file${NC}"
        return 1
    fi

    # k6 실행
    k6 run \
        --env BASE_URL="$BASE_URL" \
        --env COMPANY_CODE="$COMPANY_CODE" \
        --env SAMPLE_PRODUCT_ID="$SAMPLE_PRODUCT_ID" \
        --env SAMPLE_POST_ID="$SAMPLE_POST_ID" \
        --env SAMPLE_BOARD_TYPE="$SAMPLE_BOARD_TYPE" \
        "$script_file"

    local exit_code=$?

    if [ $exit_code -eq 0 ]; then
        echo ""
        echo -e "${GREEN}✅ ${test_type^} Test 완료${NC}"
        return 0
    else
        echo ""
        echo -e "${RED}❌ ${test_type^} Test 실패 (종료 코드: $exit_code)${NC}"
        return 1
    fi
}

# 모든 테스트 실행
run_all_tests() {
    local failed_tests=()

    echo -e "${BLUE}📋 모든 테스트를 순차적으로 실행합니다...${NC}"
    echo ""

    # 1. Smoke Test
    if run_test "smoke"; then
        echo -e "${GREEN}1/4 Smoke Test ✅${NC}"
    else
        failed_tests+=("smoke")
        echo -e "${RED}1/4 Smoke Test ❌${NC}"
        echo -e "${YELLOW}⚠️  기본 기능 테스트 실패. 추가 테스트를 건너뜁니다.${NC}"
        return 1
    fi

    echo ""
    echo -e "${YELLOW}다음 테스트까지 10초 대기...${NC}"
    sleep 10

    # 2. Load Test
    if run_test "load"; then
        echo -e "${GREEN}2/4 Load Test ✅${NC}"
    else
        failed_tests+=("load")
        echo -e "${RED}2/4 Load Test ❌${NC}"
    fi

    echo ""
    echo -e "${YELLOW}다음 테스트까지 15초 대기...${NC}"
    sleep 15

    # 3. Stress Test
    echo -e "${YELLOW}⚠️  Stress Test는 시스템에 높은 부하를 가합니다.${NC}"
    read -p "계속하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if run_test "stress"; then
            echo -e "${GREEN}3/4 Stress Test ✅${NC}"
        else
            failed_tests+=("stress")
            echo -e "${RED}3/4 Stress Test ❌${NC}"
        fi
    else
        echo -e "${YELLOW}Stress Test를 건너뜁니다.${NC}"
    fi

    echo ""
    echo -e "${YELLOW}다음 테스트까지 20초 대기...${NC}"
    sleep 20

    # 4. Spike Test
    echo -e "${YELLOW}⚠️  Spike Test는 순간적으로 매우 높은 부하를 가합니다.${NC}"
    read -p "계속하시겠습니까? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        if run_test "spike"; then
            echo -e "${GREEN}4/4 Spike Test ✅${NC}"
        else
            failed_tests+=("spike")
            echo -e "${RED}4/4 Spike Test ❌${NC}"
        fi
    else
        echo -e "${YELLOW}Spike Test를 건너뜁니다.${NC}"
    fi

    # 결과 요약
    echo ""
    echo -e "${BLUE}════════════════════════════════════════${NC}"
    echo -e "${BLUE}테스트 결과 요약${NC}"
    echo -e "${BLUE}════════════════════════════════════════${NC}"

    if [ ${#failed_tests[@]} -eq 0 ]; then
        echo -e "${GREEN}✅ 모든 테스트가 성공했습니다!${NC}"
        return 0
    else
        echo -e "${RED}❌ 실패한 테스트: ${failed_tests[*]}${NC}"
        return 1
    fi
}

# 메인 실행
main() {
    print_banner

    # k6 설치 확인
    if ! command -v k6 &> /dev/null; then
        echo -e "${RED}❌ k6가 설치되어 있지 않습니다.${NC}"
        echo ""
        echo "설치 방법:"
        echo "  macOS:   brew install k6"
        echo "  Windows: choco install k6"
        echo "  Linux:   https://k6.io/docs/getting-started/installation/"
        exit 1
    fi

    # 도움말 표시
    if [ "$TEST_TYPE" = "help" ] || [ "$TEST_TYPE" = "-h" ] || [ "$TEST_TYPE" = "--help" ]; then
        print_usage
        exit 0
    fi

    # 설정 출력
    print_config

    # 애플리케이션 상태 확인
    if ! check_application; then
        exit 1
    fi

    echo ""

    # 테스트 실행
    case "$TEST_TYPE" in
        smoke)
            run_test "smoke"
            ;;
        load)
            run_test "load"
            ;;
        stress)
            echo -e "${YELLOW}⚠️  Stress Test는 시스템에 높은 부하를 가합니다.${NC}"
            read -p "계속하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                run_test "stress"
            else
                echo -e "${YELLOW}테스트가 취소되었습니다.${NC}"
                exit 0
            fi
            ;;
        spike)
            echo -e "${YELLOW}⚠️  Spike Test는 순간적으로 매우 높은 부하를 가합니다.${NC}"
            read -p "계속하시겠습니까? (y/N): " -n 1 -r
            echo
            if [[ $REPLY =~ ^[Yy]$ ]]; then
                run_test "spike"
            else
                echo -e "${YELLOW}테스트가 취소되었습니다.${NC}"
                exit 0
            fi
            ;;
        all)
            run_all_tests
            ;;
        *)
            echo -e "${RED}❌ 알 수 없는 테스트 유형: $TEST_TYPE${NC}"
            echo ""
            print_usage
            exit 1
            ;;
    esac

    exit $?
}

# 스크립트 실행
main
