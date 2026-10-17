@echo off
setlocal enabledelayedexpansion
title Chay kiem thu HTBB
cd /d "%~dp0"

REM ============================================================
REM  Chay bo kiem thu trong test\
REM
REM  Test KHONG nam trong src\ va KHONG dung thu vien ngoai. Ly do:
REM  build.bat gom toan bo src\, va lib\ nam tren Class-Path cua jar deploy -
REM  de test o hai cho do la day ca test len server that. Chay file nay khong
REM  dung toi KPAH2_patched.jar, khong doi mot byte nao cua ban deploy.
REM
REM  Ma thoat: 0 = tat ca dat, 1 = co test hong.
REM ============================================================

echo ============================================================
echo   KIEM THU HTBB
echo ============================================================
echo.

set "JDKBIN="
for /f "delims=" %%J in ('where javac 2^>nul') do (
    if not defined JDKBIN for %%D in ("%%~dpJ") do if exist "%%~fD\jar.exe" set "JDKBIN=%%~fD"
)
if not defined JDKBIN for /d %%D in ("C:\Program Files\Java\jdk*") do if exist "%%D\bin\jar.exe" set "JDKBIN=%%D\bin"
if not defined JDKBIN for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do if exist "%%D\bin\jar.exe" set "JDKBIN=%%D\bin"
if not defined JDKBIN (
    echo [LOI] Khong tim thay JDK. Cai JDK roi thu lai.
    pause
    exit /b 1
)

set "LIBCP=lib\jxl-2.6.jar;lib\mysql-connector-java-5.1.49.jar;lib\NQSH_5h.jar"
set "OUTSRC=build\test-classes-src"
set "OUTTEST=build\test-classes"

if not exist build mkdir build
if exist "%OUTSRC%" rmdir /s /q "%OUTSRC%"
if exist "%OUTTEST%" rmdir /s /q "%OUTTEST%"
mkdir "%OUTSRC%"
mkdir "%OUTTEST%"

echo [1/3] Bien dich src\ ...
dir /s /b src\*.java > build\sources-test.txt
"%JDKBIN%\javac.exe" --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "%LIBCP%" -sourcepath src -d "%OUTSRC%" @build\sources-test.txt
if errorlevel 1 (
    echo [LOI] src\ khong bien dich duoc. Sua loi truoc khi chay test.
    pause
    exit /b 1
)

echo [2/3] Bien dich test\ ...
dir /s /b test\*.java > build\sources-kiem.txt
"%JDKBIN%\javac.exe" --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "%OUTSRC%;%LIBCP%" -d "%OUTTEST%" @build\sources-kiem.txt
if errorlevel 1 (
    echo [LOI] test\ khong bien dich duoc.
    pause
    exit /b 1
)

echo [3/3] Chay...
echo.
"%JDKBIN%\java.exe" -Dfile.encoding=UTF-8 -cp "%OUTTEST%;%OUTSRC%;%LIBCP%" Kiem
set RC=%ERRORLEVEL%

echo.
if "%RC%"=="0" (
    echo   ^>^>^> TAT CA KIEM TRA DAT
) else (
    echo   ^>^>^> CO KIEM TRA HONG - xem o tren
)
pause
exit /b %RC%
