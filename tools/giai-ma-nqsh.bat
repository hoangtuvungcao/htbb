@echo off
setlocal enabledelayedexpansion
title Giai ma nguon NQSH_5h.jar
cd /d "%~dp0\.."

REM ============================================================
REM  Dich nguoc phan CODE GAME trong lib\NQSH_5h.jar ra doc duoc.
REM
REM  Vi sao can: NQSH_5h.jar chua 878 class DA BIEN DICH, KHONG co ma nguon,
REM  trong do 236 class la code game (141 real, 73 data, 8 io, 4 server...).
REM  Do la phan logic khong ai doc duoc. File nay giai ma chung ra de DOC,
REM  KHONG phai de bien dich vao server.
REM
REM  Ket qua ra build\nqsh-src\ (da gitignore, khong commit).
REM
REM  LUU Y: 642 class con lai trong jar la ban sao thua cua jxl va
REM  mysql-connector - da co san trong lib\, khong can giai ma.
REM ============================================================

set "OUT=build\nqsh-src"
set "CFR=build\cfr.jar"
set "CFRURL=https://repo1.maven.org/maven2/org/benf/cfr/0.152/cfr-0.152.jar"

if not exist build mkdir build

if not exist "%CFR%" (
    echo Tai CFR ^(trinh dich nguoc^)...
    powershell -NoProfile -Command "try { Invoke-WebRequest -Uri '%CFRURL%' -OutFile '%CFR%' -TimeoutSec 120 } catch { exit 1 }"
    if errorlevel 1 (
        echo [LOI] Khong tai duoc CFR. Kiem tra mang, hoac tai tay tu:
        echo       %CFRURL%
        echo       roi luu vao %CFR%
        pause
        exit /b 1
    )
)

if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

echo Dang dich nguoc code game trong NQSH_5h.jar...
java -jar "%CFR%" lib\NQSH_5h.jar ^
     --jarfilter "^(data|real|io|server|common|util|logNQSH)\..*" ^
     --extraclasspath "lib\jxl-2.6.jar;lib\mysql-connector-java-5.1.49.jar" ^
     --outputdir "%OUT%" --silent true
if errorlevel 1 (
    echo [LOI] Dich nguoc that bai.
    pause
    exit /b 1
)

for /f %%C in ('dir /s /b "%OUT%\*.java" 2^>nul ^| find /c /v ""') do set SOFILE=%%C
echo.
echo ============================================================
echo   XONG: %SOFILE% file .java trong %OUT%
echo.
echo   Day la ma nguon DE DOC. KHONG dua vao src\ va KHONG bien dich
echo   vao server - xem README.md muc "Go phu thuoc NQSH_5h.jar" de
echo   biet vi sao.
echo ============================================================
pause
