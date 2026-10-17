@echo off
title Cap nhat server HTBB
setlocal enabledelayedexpansion

cd /d "%~dp0"

echo ============================================================
echo   CAP NHAT SERVER HTBB
echo ============================================================
echo.

if not exist "HTBB_NEW.jar" (
  echo [LOI] Khong thay HTBB_NEW.jar trong thu muc nay.
  pause
  exit /b 1
)

REM Nhan dien server bang CONG 19129 dang lang nghe, KHONG dung tasklist java.exe
REM vi VS Code / cong cu khac cung chay java.exe -> se cho nham vo tan.
call :CHECKPORT
if "%RUNNING%"=="0" (
  echo Server dang tat. Cap nhat luon.
  goto COPY
)

echo   Server DANG CHAY ^(cong 19129 dang mo^).
echo.
echo   Hay tat server:
echo     - Admin Panel  -^>  nut "Bao Tri"     ^(khuyen dung^)
echo     - Cua so console  -^>  Ctrl+C
echo.
echo   Dung bam nut X: Windows chi cho 5 giay roi cuong che tat.
echo.
echo   Dang cho server tat (bam Ctrl+C o day de huy)...

:WAIT
timeout /t 1 /nobreak >nul
call :CHECKPORT
if "%RUNNING%"=="1" (
  echo|set /p="."
  goto WAIT
)
echo.
echo   Server da tat.
timeout /t 3 /nobreak >nul

:COPY
echo.
echo Dang thay jar...
if exist "KPAH2_patched.jar" (
  copy /Y "KPAH2_patched.jar" "KPAH2_patched_backup_truoc_capnhat.jar" >nul
  echo   Ban cu luu tai: KPAH2_patched_backup_truoc_capnhat.jar
)
copy /Y "HTBB_NEW.jar" "KPAH2_patched.jar" >nul
if errorlevel 1 (
  echo [LOI] Copy that bai, file co the dang bi khoa.
  pause
  exit /b 1
)
echo   Da cap nhat KPAH2_patched.jar

echo.
echo ============================================================
echo   XONG. Dang khoi dong server...
echo ============================================================
timeout /t 2 /nobreak >nul
start "" run.bat
exit /b 0

:CHECKPORT
set RUNNING=0
for /f %%A in ('netstat -an ^| findstr /C:":19129" ^| findstr /C:"LISTENING" ^| find /C /V ""') do set CNT=%%A
if not "%CNT%"=="0" set RUNNING=1
goto :eof