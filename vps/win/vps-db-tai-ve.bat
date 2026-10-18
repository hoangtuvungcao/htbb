@echo off
REM May VPS: dat bien HTBB_VPS (vd. setx HTBB_VPS root@ten-may) hoac khai Host htbb-vps trong %USERPROFILE%\.ssh\config
if not defined HTBB_VPS set "HTBB_VPS=root@htbb-vps"
setlocal enabledelayedexpansion
title Tai DB tu VPS ve may nay
cd /d "%~dp0"

REM ============================================================
REM  Tai htbb_db + account tu VPS ve may Windows nay, de nap vao
REM  MySQL local (XAMPP) cho giong VPS.
REM
REM  Buoc tiep theo sau khi chay xong: vps-db-nap-vao-local.bat
REM
REM  KHONG chua mat khau nao: SSH dung khoa (hoac hoi tay), con
REM  mysql -u root tren VPS dung unix_socket. Xem README.md.
REM
REM  VI SAO KHONG NEN chi lam "mysqldump | gzip > file":
REM  mysqldump chet giua duong VAN cho ra mot file trong nhu that,
REM  va ma thoat lay tu lenh cuoi cua duong ong (gzip = 0) nen
REM  script tuong da xong. Chinh cai bay nay tung lam backup_db.sh
REM  xoa ban cu sau khi tao ra mot ban dump hong. O day dung HAI
REM  phep kiem doc lap: ma thoat cua ssh, VA dong "Dump completed"
REM  o cuoi file. Thieu mot trong hai la coi nhu that bai.
REM ============================================================

set "VPS=%HTBB_VPS%"
set "DICH=%USERPROFILE%\Desktop\htbb-db-backup"

REM Dat NGOAI thu muc git: ban dump nang ~600MB va chua du lieu nguoi
REM choi that, tuyet doi khong de no lot vao commit.

echo ============================================================
echo   TAI DB TU VPS VE MAY NAY
echo ============================================================
echo   VPS   : %VPS%
echo   Luu ve: %DICH%
echo.
echo   Luu y: ban dump chua du lieu nguoi choi that (ten dang nhap,
echo   nhan vat, nap the). Dung chia se file nay ra ngoai.
echo.

if not exist "%DICH%" mkdir "%DICH%"

REM %DATE%/%TIME% phu thuoc dinh dang vung mien nen khong dung duoc
REM de dat ten file. Lay qua PowerShell cho chac.
for /f "delims=" %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd_HHmmss"') do set "TS=%%i"

echo [1/4] Thu ket noi SSH...
ssh -o ConnectTimeout=20 %VPS% "echo SSH_OK"
if errorlevel 1 (
    echo.
    echo [LOI] Khong SSH duoc vao %VPS%.
    echo       - Neu no hoi mat khau ma bao sai: xem muc "Do phai nhap mat khau
    echo         SSH moi lan" trong README.md ^(dat khoa SSH^).
    echo       - Neu bao timeout: kiem tra mang, hoac IP VPS da doi.
    pause
    exit /b 1
)

echo.
echo [2/4] Xem thong tin DB tren VPS...
REM In ra bang ma va so bang, de lat nua nap vao local biet co khop khong.
echo SELECT table_schema AS db, COUNT(*) AS so_bang FROM information_schema.tables WHERE table_schema IN ('htbb_db','account') GROUP BY table_schema; SELECT SCHEMA_NAME AS db, DEFAULT_CHARACTER_SET_NAME AS bang_ma FROM information_schema.SCHEMATA WHERE SCHEMA_NAME IN ('htbb_db','account'); | ssh %VPS% "mysql -u root --default-character-set=utf8 -t"

echo.
echo [3/4] Tai htbb_db (~600MB, cho vai phut)...
REM  --single-transaction: chup mot anh nhat quan MA KHONG khoa bang. Server
REM     game dang chay that nen tuyet doi khong duoc khoa.
REM  --quick: doc tung dong, khong dom het vao RAM cua VPS.
REM  ssh -C: nen ngay trong duong truyen, khoi phai co gzip tren Windows.
REM  KHONG dung duong ong o day: nho vay ma thoat cua ssh CHINH LA ma thoat
REM     cua mysqldump.
REM  --databases: ban dump mang theo ca dong CREATE DATABASE voi DUNG bang ma
REM     cua VPS, nen luc nap vao local khong phai doan bang ma nua.
REM  CO Y KHONG dung --add-drop-database: file nay nam tren Desktop, nhung ai
REM     do chay nham no NGUOC len VPS la xoa sach DB that. Lenh xoa de trong
REM     vps-db-nap-vao-local.bat, cho no ghim cung 127.0.0.1.
ssh -C %VPS% "mysqldump -u root --single-transaction --quick --default-character-set=utf8mb4 --routines --events --databases htbb_db" > "%DICH%\vps_htbb_db_%TS%.sql"
if errorlevel 1 (
    echo [LOI] Dump htbb_db that bai. File tai ve KHONG dung duoc, dang xoa.
    del /f /q "%DICH%\vps_htbb_db_%TS%.sql" 2>nul
    pause
    exit /b 1
)

echo       Tai account...
ssh -C %VPS% "mysqldump -u root --single-transaction --quick --default-character-set=utf8mb4 --databases account" > "%DICH%\vps_account_%TS%.sql"
if errorlevel 1 (
    echo [LOI] Dump account that bai. File tai ve KHONG dung duoc, dang xoa.
    del /f /q "%DICH%\vps_account_%TS%.sql" 2>nul
    pause
    exit /b 1
)

echo.
echo [4/4] Kiem tra hai ban dump...
REM Phep kiem thu hai, doc lap voi ma thoat: mysqldump chi in dong nay khi da
REM chay HET. Dump bi cat giua duong se khong co no.
set "HONG=0"
for %%F in ("%DICH%\vps_htbb_db_%TS%.sql" "%DICH%\vps_account_%TS%.sql") do (
    powershell -NoProfile -Command "if ((Get-Content -LiteralPath '%%~F' -Tail 5) -match 'Dump completed') { exit 0 } else { exit 1 }"
    if errorlevel 1 (
        echo   [LOI] %%~nxF thieu dong 'Dump completed' -^> ban dump BI CAT giua duong.
        set "HONG=1"
    ) else (
        for %%S in ("%%~F") do echo   OK  %%~nxF  ^(%%~zS byte^)
    )
)
if "!HONG!"=="1" (
    echo.
    echo [LOI] Co ban dump khong tron ven. DUNG nap vao local. Chay lai script nay.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo   XONG. Hai file da nam o:
echo     %DICH%
echo.
echo   Buoc tiep theo: chay vps-db-nap-vao-local.bat
echo   ^(no se tat server local, sao luu DB local, roi nap ban moi^)
echo ============================================================
pause
