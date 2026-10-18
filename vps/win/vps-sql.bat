@echo off
REM ============================================================
REM  Chay mot cau SQL tren DB cua VPS, khong can mo cong 3306
REM
REM  Cach dung (nho dat cau SQL trong dau ngoac kep):
REM     vps-sql "SELECT id,charname,userId FROM htbb_db.tob_char;"
REM     vps-sql "SELECT COUNT(*) FROM htbb_db.tob_char;"
REM     vps-sql "SELECT username,ban FROM account.team_user;"
REM
REM  Cau SQL duoc gui qua stdin nen khong phai lo escape dau nhay.
REM ============================================================
setlocal
if "%~1"=="" (
    echo Cach dung: vps-sql "SELECT id,charname FROM htbb_db.tob_char;"
    exit /b 1
)
echo %~1 | ssh root@180.93.57.30 "mysql -u root --default-character-set=utf8 -t"
endlocal
