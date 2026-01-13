@echo off
REM k6 테스트 실행 헬퍼 스크립트 (Windows)
REM 사용법: run-tests.bat [smoke|load|stress|spike|all]

setlocal enabledelayedexpansion

REM 기본값 설정
set TEST_TYPE=%1
if "%TEST_TYPE%"=="" set TEST_TYPE=smoke

if not defined BASE_URL set BASE_URL=http://localhost:8080
if not defined COMPANY_CODE set COMPANY_CODE=company-a
if not defined SAMPLE_PRODUCT_ID set SAMPLE_PRODUCT_ID=1
if not defined SAMPLE_POST_ID set SAMPLE_POST_ID=1
if not defined SAMPLE_BOARD_TYPE set SAMPLE_BOARD_TYPE=notice

REM 배너 출력
echo.
echo ================================================================
echo           k6 Load ^& Stress Testing Suite
echo           Bear Application Performance Tests
echo ================================================================
echo.

REM 도움말
if "%TEST_TYPE%"=="help" goto :show_help
if "%TEST_TYPE%"=="-h" goto :show_help
if "%TEST_TYPE%"=="--help" goto :show_help

REM k6 설치 확인
where k6 >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] k6가 설치되어 있지 않습니다.
    echo.
    echo 설치 방법:
    echo   choco install k6
    echo   또는
    echo   scoop install k6
    echo.
    exit /b 1
)

REM 설정 출력
echo [설정]
echo   Base URL:         %BASE_URL%
echo   Company Code:     %COMPANY_CODE%
echo   Product ID:       %SAMPLE_PRODUCT_ID%
echo   Post ID:          %SAMPLE_POST_ID%
echo   Board Type:       %SAMPLE_BOARD_TYPE%
echo.

REM 애플리케이션 상태 확인
echo [체크] 애플리케이션 상태 확인...
curl -sf "%BASE_URL%/actuator/health" >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 애플리케이션에 연결할 수 없습니다.
    echo URL: %BASE_URL%/actuator/health
    echo.
    echo 다음을 확인해주세요:
    echo   1. 애플리케이션이 실행 중인지 확인
    echo   2. BASE_URL이 올바른지 확인
    echo   3. 방화벽/네트워크 설정 확인
    exit /b 1
)
echo [OK] 애플리케이션이 실행 중입니다.
echo.

REM 테스트 실행
if "%TEST_TYPE%"=="smoke" goto :run_smoke
if "%TEST_TYPE%"=="load" goto :run_load
if "%TEST_TYPE%"=="stress" goto :run_stress
if "%TEST_TYPE%"=="spike" goto :run_spike
if "%TEST_TYPE%"=="all" goto :run_all

echo [ERROR] 알 수 없는 테스트 유형: %TEST_TYPE%
echo.
goto :show_help

:run_smoke
echo [실행] Smoke Test 시작...
echo.
k6 run ^
    --env BASE_URL=%BASE_URL% ^
    --env COMPANY_CODE=%COMPANY_CODE% ^
    --env SAMPLE_PRODUCT_ID=%SAMPLE_PRODUCT_ID% ^
    --env SAMPLE_POST_ID=%SAMPLE_POST_ID% ^
    --env SAMPLE_BOARD_TYPE=%SAMPLE_BOARD_TYPE% ^
    scenarios/smoke-test.js

if %errorlevel% equ 0 (
    echo.
    echo [OK] Smoke Test 완료
) else (
    echo.
    echo [ERROR] Smoke Test 실패
    exit /b 1
)
goto :end

:run_load
echo [실행] Load Test 시작...
echo.
k6 run ^
    --env BASE_URL=%BASE_URL% ^
    --env COMPANY_CODE=%COMPANY_CODE% ^
    --env SAMPLE_PRODUCT_ID=%SAMPLE_PRODUCT_ID% ^
    --env SAMPLE_POST_ID=%SAMPLE_POST_ID% ^
    --env SAMPLE_BOARD_TYPE=%SAMPLE_BOARD_TYPE% ^
    scenarios/load-test.js

if %errorlevel% equ 0 (
    echo.
    echo [OK] Load Test 완료
) else (
    echo.
    echo [ERROR] Load Test 실패
    exit /b 1
)
goto :end

:run_stress
echo [경고] Stress Test는 시스템에 높은 부하를 가합니다.
set /p CONFIRM="계속하시겠습니까? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo 테스트가 취소되었습니다.
    goto :end
)
echo.
echo [실행] Stress Test 시작...
echo.
k6 run ^
    --env BASE_URL=%BASE_URL% ^
    --env COMPANY_CODE=%COMPANY_CODE% ^
    --env SAMPLE_PRODUCT_ID=%SAMPLE_PRODUCT_ID% ^
    --env SAMPLE_POST_ID=%SAMPLE_POST_ID% ^
    --env SAMPLE_BOARD_TYPE=%SAMPLE_BOARD_TYPE% ^
    scenarios/stress-test.js

if %errorlevel% equ 0 (
    echo.
    echo [OK] Stress Test 완료
) else (
    echo.
    echo [ERROR] Stress Test 실패
    exit /b 1
)
goto :end

:run_spike
echo [경고] Spike Test는 순간적으로 매우 높은 부하를 가합니다.
set /p CONFIRM="계속하시겠습니까? (y/N): "
if /i not "%CONFIRM%"=="y" (
    echo 테스트가 취소되었습니다.
    goto :end
)
echo.
echo [실행] Spike Test 시작...
echo.
k6 run ^
    --env BASE_URL=%BASE_URL% ^
    --env COMPANY_CODE=%COMPANY_CODE% ^
    --env SAMPLE_PRODUCT_ID=%SAMPLE_PRODUCT_ID% ^
    --env SAMPLE_POST_ID=%SAMPLE_POST_ID% ^
    --env SAMPLE_BOARD_TYPE=%SAMPLE_BOARD_TYPE% ^
    scenarios/spike-test.js

if %errorlevel% equ 0 (
    echo.
    echo [OK] Spike Test 완료
) else (
    echo.
    echo [ERROR] Spike Test 실패
    exit /b 1
)
goto :end

:run_all
echo [실행] 모든 테스트를 순차적으로 실행합니다...
echo.

REM 1. Smoke Test
echo [1/4] Smoke Test 실행 중...
call :run_smoke
if %errorlevel% neq 0 (
    echo [ERROR] Smoke Test 실패. 추가 테스트를 건너뜁니다.
    exit /b 1
)

echo.
echo 다음 테스트까지 10초 대기...
timeout /t 10 /nobreak >nul
echo.

REM 2. Load Test
echo [2/4] Load Test 실행 중...
call :run_load

echo.
echo 다음 테스트까지 15초 대기...
timeout /t 15 /nobreak >nul
echo.

REM 3. Stress Test
echo [3/4] Stress Test 실행 가능
echo [경고] Stress Test는 시스템에 높은 부하를 가합니다.
set /p CONFIRM="계속하시겠습니까? (y/N): "
if /i "%CONFIRM%"=="y" (
    call :run_stress
) else (
    echo Stress Test를 건너뜁니다.
)

echo.
echo 다음 테스트까지 20초 대기...
timeout /t 20 /nobreak >nul
echo.

REM 4. Spike Test
echo [4/4] Spike Test 실행 가능
echo [경고] Spike Test는 순간적으로 매우 높은 부하를 가합니다.
set /p CONFIRM="계속하시겠습니까? (y/N): "
if /i "%CONFIRM%"=="y" (
    call :run_spike
) else (
    echo Spike Test를 건너뜁니다.
)

echo.
echo ================================================================
echo 모든 테스트 완료
echo ================================================================
goto :end

:show_help
echo 사용법:
echo   run-tests.bat [test-type]
echo.
echo Test Types:
echo   smoke   - 기본 기능 확인 (1 VU, 1분)
echo   load    - 일반 부하 테스트 (10-50 VU, 10분)
echo   stress  - 한계 테스트 (50-200 VU, 14분)
echo   spike   - 급격한 트래픽 증가 (10-500 VU, 7분)
echo   all     - 모든 테스트 순차 실행
echo.
echo 환경 변수:
echo   BASE_URL            - 애플리케이션 URL (기본: http://localhost:8080)
echo   COMPANY_CODE        - 기업 코드 (기본: demo-company)
echo   SAMPLE_PRODUCT_ID   - 샘플 제품 ID (기본: 1)
echo   SAMPLE_POST_ID      - 샘플 게시글 ID (기본: 1)
echo   SAMPLE_BOARD_TYPE   - 샘플 게시판 타입 (기본: notice)
echo.
echo 예시:
echo   run-tests.bat smoke
echo   set BASE_URL=http://staging.example.com ^&^& set COMPANY_CODE=mycompany ^&^& run-tests.bat load
echo   run-tests.bat all
echo.
goto :end

:end
endlocal
