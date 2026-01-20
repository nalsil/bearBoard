#!/bin/bash

# ============================================================
# Bear Application JMeter Performance Test Runner
# ============================================================
# 사용법: ./run-tests.sh [test_type] [options]
#
# test_type: smoke, load, stress, spike, all
#
# 옵션:
#   -h, --host HOST        서버 호스트 (기본값: localhost)
#   -p, --port PORT        서버 포트 (기본값: 8080)
#   -c, --company CODE     회사 코드 (기본값: demo-company)
#   -t, --threads NUM      스레드 수
#   -d, --duration SEC     테스트 지속 시간(초)
#   -r, --rampup SEC       Ramp-up 시간(초)
#   --help                 도움말 표시
#
# 예제:
#   ./run-tests.sh load
#   ./run-tests.sh stress -h localhost -p 8080
#   ./run-tests.sh spike -t 300 -d 120
# ============================================================

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m' # No Color

# 기본값 설정
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JMX_FILE="$SCRIPT_DIR/bear-performance-test.jmx"
RESULTS_DIR="$SCRIPT_DIR/results"
HOST="localhost"
PORT="8080"
COMPANY_CODE="demo-company"
THREADS=0
DURATION=0
RAMPUP=0
PRODUCT_ID="1"
POST_ID="1"
QNA_ID="1"
YOUTUBE_ID="1"

# 테스트 설정
declare -A SMOKE_CONFIG=([threads]=1 [rampup]=1 [duration]=60 [description]="Smoke Test - 기본 기능 확인")
declare -A LOAD_CONFIG=([threads]=10 [rampup]=30 [duration]=300 [description]="Load Test - 일반 부하 테스트")
declare -A STRESS_CONFIG=([threads]=50 [rampup]=60 [duration]=600 [description]="Stress Test - 고부하 스트레스 테스트")
declare -A SPIKE_CONFIG=([threads]=200 [rampup]=10 [duration]=180 [description]="Spike Test - 급격한 트래픽 증가 테스트")

# 도움말 출력
show_help() {
    echo "사용법: $0 [test_type] [options]"
    echo ""
    echo "test_type: smoke, load, stress, spike, all"
    echo ""
    echo "옵션:"
    echo "  -h, --host HOST        서버 호스트 (기본값: localhost)"
    echo "  -p, --port PORT        서버 포트 (기본값: 8080)"
    echo "  -c, --company CODE     회사 코드 (기본값: demo-company)"
    echo "  -t, --threads NUM      스레드 수"
    echo "  -d, --duration SEC     테스트 지속 시간(초)"
    echo "  -r, --rampup SEC       Ramp-up 시간(초)"
    echo "  --help                 도움말 표시"
    echo ""
    echo "예제:"
    echo "  $0 load"
    echo "  $0 stress -h localhost -p 8080"
    echo "  $0 spike -t 300 -d 120"
    exit 0
}

# JMeter 설치 확인
check_jmeter() {
    if ! command -v jmeter &> /dev/null; then
        echo -e "${RED}Error: JMeter가 설치되어 있지 않습니다.${NC}"
        echo "macOS: brew install jmeter"
        echo "Linux: sudo apt-get install jmeter"
        exit 1
    fi
}

# 테스트 실행
run_test() {
    local test_type=$1
    local timestamp=$(date +"%Y%m%d-%H%M%S")
    local result_file="$RESULTS_DIR/${test_type}-test-${timestamp}.jtl"
    local report_dir="$RESULTS_DIR/${test_type}-test-report-${timestamp}"

    # 테스트 설정 가져오기
    local config_name="${test_type^^}_CONFIG"
    declare -n config="$config_name"

    # 사용자 지정 값 또는 기본값 사용
    local thread_count=$([[ $THREADS -gt 0 ]] && echo $THREADS || echo ${config[threads]})
    local rampup_time=$([[ $RAMPUP -gt 0 ]] && echo $RAMPUP || echo ${config[rampup]})
    local test_duration=$([[ $DURATION -gt 0 ]] && echo $DURATION || echo ${config[duration]})

    echo ""
    echo -e "${CYAN}============================================================${NC}"
    echo -e "${CYAN} ${config[description]}${NC}"
    echo -e "${CYAN}============================================================${NC}"
    echo -e "${YELLOW}서버: $HOST:$PORT${NC}"
    echo -e "${YELLOW}회사 코드: $COMPANY_CODE${NC}"
    echo -e "${YELLOW}스레드 수: $thread_count${NC}"
    echo -e "${YELLOW}Ramp-up 시간: $rampup_time 초${NC}"
    echo -e "${YELLOW}테스트 지속 시간: $test_duration 초${NC}"
    echo -e "${CYAN}============================================================${NC}"
    echo ""

    # JMeter 실행
    local jmeter_args=(
        "-n"
        "-t" "$JMX_FILE"
        "-l" "$result_file"
        "-e" "-o" "$report_dir"
        "-Jserver.host=$HOST"
        "-Jserver.port=$PORT"
        "-Jcompany.code=$COMPANY_CODE"
        "-J${test_type}.threads=$thread_count"
        "-J${test_type}.rampup=$rampup_time"
        "-J${test_type}.duration=$test_duration"
        "-Jsample.product.id=$PRODUCT_ID"
        "-Jsample.post.id=$POST_ID"
        "-Jsample.qna.id=$QNA_ID"
        "-Jsample.youtube.id=$YOUTUBE_ID"
    )

    # Load Test 관련 추가 파라미터 (기본 Thread Group용)
    if [[ "$test_type" == "load" || "$test_type" == "smoke" ]]; then
        jmeter_args+=("-Jload.threads=$thread_count")
        jmeter_args+=("-Jload.rampup=$rampup_time")
        jmeter_args+=("-Jload.duration=$test_duration")
    fi

    echo -e "${GREEN}JMeter 테스트 실행 중...${NC}"
    jmeter "${jmeter_args[@]}"

    if [[ $? -eq 0 ]]; then
        echo ""
        echo -e "${GREEN}테스트 완료!${NC}"
        echo -e "${YELLOW}결과 파일: $result_file${NC}"
        echo -e "${YELLOW}HTML 리포트: $report_dir/index.html${NC}"

        # macOS에서 리포트 열기
        if [[ "$OSTYPE" == "darwin"* ]]; then
            read -p "HTML 리포트를 열겠습니까? (y/n): " open_report
            if [[ "$open_report" == "y" || "$open_report" == "Y" ]]; then
                open "$report_dir/index.html"
            fi
        fi
    else
        echo ""
        echo -e "${RED}테스트 실패!${NC}"
        exit 1
    fi
}

# 인자 파싱
if [[ $# -eq 0 ]]; then
    show_help
fi

TEST_TYPE=$1
shift

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)
            HOST="$2"
            shift 2
            ;;
        -p|--port)
            PORT="$2"
            shift 2
            ;;
        -c|--company)
            COMPANY_CODE="$2"
            shift 2
            ;;
        -t|--threads)
            THREADS="$2"
            shift 2
            ;;
        -d|--duration)
            DURATION="$2"
            shift 2
            ;;
        -r|--rampup)
            RAMPUP="$2"
            shift 2
            ;;
        --help)
            show_help
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            ;;
    esac
done

# 결과 디렉토리 생성
mkdir -p "$RESULTS_DIR"

# JMeter 확인
check_jmeter

# 메인 실행
echo ""
echo -e "${MAGENTA}========================================${NC}"
echo -e "${MAGENTA} Bear Application JMeter Performance Test${NC}"
echo -e "${MAGENTA}========================================${NC}"

case $TEST_TYPE in
    smoke|load|stress|spike)
        run_test "$TEST_TYPE"
        ;;
    all)
        for type in smoke load stress spike; do
            run_test "$type"
            if [[ "$type" != "spike" ]]; then
                echo ""
                echo -e "${YELLOW}다음 테스트 시작 전 30초 대기...${NC}"
                sleep 30
            fi
        done
        ;;
    *)
        echo -e "${RED}Unknown test type: $TEST_TYPE${NC}"
        show_help
        ;;
esac

echo ""
echo -e "${GREEN}모든 테스트 완료!${NC}"
