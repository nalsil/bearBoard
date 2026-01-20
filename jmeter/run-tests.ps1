<#
.SYNOPSIS
    Bear Application JMeter Performance Test Runner
.DESCRIPTION
    JMeter 성능 테스트를 실행하는 PowerShell 스크립트입니다.
.PARAMETER TestType
    테스트 유형: load, stress, spike, all
.PARAMETER Host
    서버 호스트 (기본값: localhost)
.PARAMETER Port
    서버 포트 (기본값: 8080)
.PARAMETER CompanyCode
    테스트할 회사 코드 (기본값: demo-company)
.PARAMETER Threads
    스레드 수 (기본값: 테스트 유형별 기본값)
.PARAMETER Duration
    테스트 지속 시간(초) (기본값: 테스트 유형별 기본값)
.EXAMPLE
    .\run-tests.ps1 -TestType load
    .\run-tests.ps1 -TestType stress -Host localhost -Port 8080
    .\run-tests.ps1 -TestType spike -Threads 300 -Duration 120
#>

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("load", "stress", "spike", "smoke", "all")]
    [string]$TestType,

    [string]$Host = "localhost",
    [int]$Port = 8080,
    [string]$CompanyCode = "demo-company",
    [int]$Threads = 0,
    [int]$Duration = 0,
    [int]$Rampup = 0,
    [string]$ProductId = "1",
    [string]$PostId = "1",
    [string]$QnaId = "1",
    [string]$YoutubeId = "1"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JmxFile = Join-Path $ScriptDir "bear-performance-test.jmx"
$ResultsDir = Join-Path $ScriptDir "results"

# 결과 디렉토리 생성
if (!(Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir | Out-Null
}

# JMeter 설치 확인
$jmeterPath = Get-Command jmeter -ErrorAction SilentlyContinue
if (!$jmeterPath) {
    Write-Error "JMeter가 설치되어 있지 않습니다. 'choco install jmeter'로 설치해주세요."
    exit 1
}

# 테스트 설정
$testConfigs = @{
    "smoke" = @{
        threads = 1
        rampup = 1
        duration = 60
        description = "Smoke Test - 기본 기능 확인"
    }
    "load" = @{
        threads = 10
        rampup = 30
        duration = 300
        description = "Load Test - 일반 부하 테스트"
    }
    "stress" = @{
        threads = 50
        rampup = 60
        duration = 600
        description = "Stress Test - 고부하 스트레스 테스트"
    }
    "spike" = @{
        threads = 200
        rampup = 10
        duration = 180
        description = "Spike Test - 급격한 트래픽 증가 테스트"
    }
}

function Run-Test {
    param(
        [string]$Type
    )

    $config = $testConfigs[$Type]
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $resultFile = Join-Path $ResultsDir "$Type-test-$timestamp.jtl"
    $reportDir = Join-Path $ResultsDir "$Type-test-report-$timestamp"

    # 사용자 지정 값 또는 기본값 사용
    $threadCount = if ($Threads -gt 0) { $Threads } else { $config.threads }
    $rampupTime = if ($Rampup -gt 0) { $Rampup } else { $config.rampup }
    $testDuration = if ($Duration -gt 0) { $Duration } else { $config.duration }

    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host " $($config.description)" -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host "서버: $Host`:$Port" -ForegroundColor Yellow
    Write-Host "회사 코드: $CompanyCode" -ForegroundColor Yellow
    Write-Host "스레드 수: $threadCount" -ForegroundColor Yellow
    Write-Host "Ramp-up 시간: $rampupTime 초" -ForegroundColor Yellow
    Write-Host "테스트 지속 시간: $testDuration 초" -ForegroundColor Yellow
    Write-Host "============================================================" -ForegroundColor Cyan
    Write-Host ""

    # JMeter 실행
    $jmeterArgs = @(
        "-n",
        "-t", $JmxFile,
        "-l", $resultFile,
        "-e", "-o", $reportDir,
        "-Jserver.host=$Host",
        "-Jserver.port=$Port",
        "-Jcompany.code=$CompanyCode",
        "-J$Type.threads=$threadCount",
        "-J$Type.rampup=$rampupTime",
        "-J$Type.duration=$testDuration",
        "-Jsample.product.id=$ProductId",
        "-Jsample.post.id=$PostId",
        "-Jsample.qna.id=$QnaId",
        "-Jsample.youtube.id=$YoutubeId"
    )

    # Load Test 관련 추가 파라미터 (기본 Thread Group용)
    if ($Type -eq "load" -or $Type -eq "smoke") {
        $jmeterArgs += "-Jload.threads=$threadCount"
        $jmeterArgs += "-Jload.rampup=$rampupTime"
        $jmeterArgs += "-Jload.duration=$testDuration"
    }

    Write-Host "JMeter 테스트 실행 중..." -ForegroundColor Green
    & jmeter $jmeterArgs

    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "테스트 완료!" -ForegroundColor Green
        Write-Host "결과 파일: $resultFile" -ForegroundColor Yellow
        Write-Host "HTML 리포트: $reportDir\index.html" -ForegroundColor Yellow

        # 리포트 열기 여부 확인
        $openReport = Read-Host "HTML 리포트를 열겠습니까? (y/n)"
        if ($openReport -eq "y" -or $openReport -eq "Y") {
            Start-Process (Join-Path $reportDir "index.html")
        }
    } else {
        Write-Host ""
        Write-Host "테스트 실패!" -ForegroundColor Red
        exit 1
    }
}

# 메인 실행
Write-Host ""
Write-Host "========================================" -ForegroundColor Magenta
Write-Host " Bear Application JMeter Performance Test" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta

if ($TestType -eq "all") {
    foreach ($type in @("smoke", "load", "stress", "spike")) {
        Run-Test -Type $type
        if ($type -ne "spike") {
            Write-Host ""
            Write-Host "다음 테스트 시작 전 30초 대기..." -ForegroundColor Yellow
            Start-Sleep -Seconds 30
        }
    }
} else {
    Run-Test -Type $TestType
}

Write-Host ""
Write-Host "모든 테스트 완료!" -ForegroundColor Green
