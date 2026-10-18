@echo off
setlocal enabledelayedexpansion
title Nap DB cua VPS vao MySQL local
cd /d "%~dp0"

REM ============================================================
REM  Nap ban dump vua tai ve (vps-db-tai-ve.bat) vao MySQL local
REM  cua XAMPP, de DB local giong het VPS.
REM
REM  GHI DE TOAN BO htbb_db va account tren may nay. Ban cu duoc
REM  sao luu truoc, khong hoi lai.
REM
REM  Lenh DROP DATABASE nam o day chu khong nam trong file dump:
REM  file dump de tren Desktop, ai chay nham no nguoc len VPS thi
REM  chi mat may bang chu khong xoa ca DB that.
REM ============================================================

set "LUU=%USERPROFILE%\Desktop\htbb-db-backup"
set "MYSQL=C:\xampp\mysql\bin\mysql.exe"
set "MYSQLDUMP=C:\xampp\mysql\bin\mysqldump.exe"

echo ============================================================
echo   NAP DB CUA VPS VAO MYSQL LOCAL
echo ============================================================
echo.

if not exist "%MYSQL%" (
    echo [LOI] Khong thay %MYSQL%
    echo       Sua duong dan trong file nay neu XAMPP nam cho khac.
    pause
    exit /b 1
)

REM ---- Tim ban dump moi nhat ----
set "D_GAME="
set "D_ACC="
for /f "delims=" %%F in ('dir /b /o-d "%LUU%\vps_htbb_db_*.sql" 2^>nul') do if not defined D_GAME set "D_GAME=%LUU%\%%F"
for /f "delims=" %%F in ('dir /b /o-d "%LUU%\vps_account_*.sql" 2^>nul') do if not defined D_ACC set "D_ACC=%LUU%\%%F"

if not defined D_GAME (
    echo [LOI] Khong thay file vps_htbb_db_*.sql trong %LUU%
    echo       Chay vps-db-tai-ve.bat truoc.
    pause
    exit /b 1
)
if not defined D_ACC (
    echo [LOI] Khong thay file vps_account_*.sql trong %LUU%
    pause
    exit /b 1
)

echo   Se nap:
for %%S in ("%D_GAME%") do echo     %%~nxS  ^(%%~zS byte^)
for %%S in ("%D_ACC%")  do echo     %%~nxS  ^(%%~zS byte^)
echo.

REM ---- Phep kiem tron ven, lam LAI o day ----
REM Khong tin ban dump chi vi no ton tai: co the la ban tai ve tu lan truoc bi
REM cat giua duong, hoac bi sua tay. Kiem lai truoc khi xoa DB local.
for %%F in ("%D_GAME%" "%D_ACC%") do (
    powershell -NoProfile -Command "if ((Get-Content -LiteralPath '%%~F' -Tail 5) -match 'Dump completed') { exit 0 } else { exit 1 }"
    if errorlevel 1 (
        echo [LOI] %%~nxF thieu dong 'Dump completed' -^> ban dump BI CAT giua duong.
        echo       KHONG nap. Chay lai vps-db-tai-ve.bat.
        pause
        exit /b 1
    )
)
echo   Hai ban dump tron ven ^(co dong 'Dump completed'^).
echo.

REM ---- Server local phai TAT ----
REM Giong capnhat.bat: cho nguoi dung tat tu te, KHONG tu kill. Kill cung thi
REM hook luu du lieu khong chay, va dang nap DB ma server con ghi vao la hong.
call :CHECKPORT 19129
if "%RUNNING%"=="1" (
    echo   Server game DANG CHAY ^(cong 19129^).
    echo   Hay tat: Admin Panel -^> "Bao Tri", hoac Ctrl+C o cua so console cua no.
    echo   Tat dung cach thi log in ra: CHUAN BI THOAT ... SAVE XONG ... THOAT
    echo   ^(Dung bam nut X: Windows chi cho 5 giay roi cuong che tat, hook luu
    echo    du lieu khong chay kip.^)
    echo.
    echo   VI SAO CHO CHU KHONG TU TAT nhu login server: server game giu trang thai
    echo   nhan vat trong RAM, tat tu te thi no ghi xuong CSDL TRUOC, nen ban sao luu
    echo   o buoc sau moi day du. Kill cung la mat phan chua ghi.
    echo   Neu no duoc bat khong co cua so ^(khong bam "Bao Tri" duoc^) thi tat tay:
    echo         netstat -ano ^| findstr ":19129"     ^(xem PID o cot cuoi^)
    echo         taskkill /PID ^<so_PID^> /F
    echo   - chap nhan mat phan chua ghi. Rieng lan nay it quan trong, vi htbb_db
    echo   sap bi thay het bang ban cua VPS.
    echo.
    echo   Dang cho... ^(Ctrl+C o day de huy^)
    :WAIT_GAME
    timeout /t 1 /nobreak >nul
    call :CHECKPORT 19129
    if "%RUNNING%"=="1" (
        echo|set /p="."
        goto WAIT_GAME
    )
    echo.
    echo   Server game da tat.
)

REM Login server thi TU TAT luon, khong cho nhu server game. Hai ly do:
REM   1. No khong co gi phai luu: CheckLoginSocket.jar chi doi chieu mat khau voi bang
REM      account.team_user roi tra loi, khong giu trang thai gi trong RAM.
REM   2. No thuong duoc bat khong co cua so rieng, nen bao "hay dong cua so cua no" la
REM      loi khuyen vo nghia - dung cai bay nay roi.
call :CHECKPORT 8023
if "%RUNNING%"=="1" (
    echo   Login server dang chay ^(cong 8023^) - tu tat...
    for /f "tokens=5" %%P in ('netstat -ano ^| findstr /C:":8023 " ^| findstr /C:"LISTENING"') do taskkill /PID %%P /F >nul 2>&1
    timeout /t 2 /nobreak >nul
    call :CHECKPORT 8023
    if "%RUNNING%"=="1" (
        echo   [LOI] Van con gi giu cong 8023. Tat tay roi chay lai:
        echo         netstat -ano ^| findstr ":8023"      ^(xem PID o cot cuoi^)
        echo         taskkill /PID ^<so_PID^> /F
        pause
        exit /b 1
    )
    echo   Login server da tat.
)

REM ---- MySQL local phai BAT ----
call :CHECKPORT 3306
if "%RUNNING%"=="0" (
    echo [LOI] MySQL local chua chay. Bat XAMPP MySQL ^(hoac c:\xampp\mysql_start.bat^) roi thu lai.
    pause
    exit /b 1
)

for /f "delims=" %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HHmmss"') do set "TS=%%i"

echo.
echo [1/3] Sao luu DB local hien tai truoc khi ghi de...
if not exist "%LUU%" mkdir "%LUU%"
"%MYSQLDUMP%" -u root --skip-password --single-transaction --quick --default-character-set=utf8mb4 --routines --events --databases htbb_db > "%LUU%\truoc_khi_nap_htbb_db_%TS%.sql"
if errorlevel 1 (
    echo [LOI] Sao luu htbb_db that bai -^> KHONG nap gi ca.
    pause
    exit /b 1
)
"%MYSQLDUMP%" -u root --skip-password --single-transaction --quick --default-character-set=utf8mb4 --databases account > "%LUU%\truoc_khi_nap_account_%TS%.sql"
if errorlevel 1 (
    echo [LOI] Sao luu account that bai -^> KHONG nap gi ca.
    pause
    exit /b 1
)
echo       Ban cu luu tai: %LUU%\truoc_khi_nap_*_%TS%.sql

echo.
echo [2/3] Xoa DB local roi nap ban cua VPS...
REM Phai xoa han DB: neu chi nap de len thi bang nao local CO ma VPS KHONG co
REM se con lai, thanh ra hai ben van khong giong nhau. Ban dump co san dong
REM CREATE DATABASE ^(nho --databases^) nen bang ma se dung y het VPS.
echo DROP DATABASE IF EXISTS htbb_db; DROP DATABASE IF EXISTS account; | "%MYSQL%" -u root --skip-password -h 127.0.0.1
if errorlevel 1 (
    echo [LOI] Khong xoa duoc DB local. Ban cu van con nguyen.
    pause
    exit /b 1
)

"%MYSQL%" -u root --skip-password -h 127.0.0.1 --default-character-set=utf8mb4 < "%D_GAME%"
if errorlevel 1 (
    echo.
    echo [LOI] Nap htbb_db that bai. DB dang do dang!
    echo       Khoi phuc: "%MYSQL%" -u root --skip-password ^< "%LUU%\truoc_khi_nap_htbb_db_%TS%.sql"
    pause
    exit /b 1
)
"%MYSQL%" -u root --skip-password -h 127.0.0.1 --default-character-set=utf8mb4 < "%D_ACC%"
if errorlevel 1 (
    echo.
    echo [LOI] Nap account that bai. DB dang do dang!
    echo       Khoi phuc: "%MYSQL%" -u root --skip-password ^< "%LUU%\truoc_khi_nap_account_%TS%.sql"
    pause
    exit /b 1
)

echo.
echo [3/3] Kiem tra sau khi nap...
echo SELECT table_schema AS db, COUNT(*) AS so_bang FROM information_schema.tables WHERE table_schema IN ('htbb_db','account') GROUP BY table_schema; SELECT COUNT(*) AS so_nhan_vat FROM htbb_db.tob_char; SELECT COUNT(*) AS so_tai_khoan FROM account.team_user; | "%MYSQL%" -u root --skip-password -h 127.0.0.1 --default-character-set=utf8mb4 -t

echo.
echo ============================================================
echo   XONG. DB local gio la ban sao cua VPS.
echo.
echo   Bat lai server local theo dung thu tu:
echo     1. loginServer\run.bat   ^(cong 8023^)
echo     2. run.bat               ^(cong 19129^)
echo.
echo   Dang nhap bang tai khoan THAT tren VPS. Nhan vat thu cu cua may
echo   nay da bi thay; muon lay lai thi nap file truoc_khi_nap_*.sql.
echo ============================================================
pause
exit /b 0

:CHECKPORT
REM Goi THANG bang duong dan day du, KHONG dua vao PATH: neu ai chay file nay tu Git Bash
REM (hay bat ky shell co dat cac lenh Unix len truoc PATH), thi `find` lai la find cua
REM Unix chu khong phai find.exe cua Windows - no coi "/C" la mot duong dan va di quet ca
REM o C:, treo vinh vien. Da dinh that.
REM Cung khong con dem so dong nua: findstr da tra ma thoat 1 khi khong khop, dung luon.
set RUNNING=0
%SystemRoot%\System32\netstat.exe -an | %SystemRoot%\System32\findstr.exe /C:":%~1 " | %SystemRoot%\System32\findstr.exe /C:"LISTENING" >nul 2>&1
if not errorlevel 1 set RUNNING=1
goto :eof
