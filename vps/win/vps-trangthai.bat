@echo off
REM ============================================================
REM  Xem nhanh tinh trang server tren VPS: service, cong, RAM, thread, DB
REM ============================================================
title Trang thai server VPS
echo === Service ===
ssh root@180.93.57.30 "systemctl is-active kpah-game kpah-login mariadb apache2 2>/dev/null"
echo.
echo === Cong dang mo (19129 = game, 8023 = login) ===
ssh root@180.93.57.30 "ss -lntp | grep -E ':(19129|8023|80|3306)'"
echo.
echo === RAM ===
ssh root@180.93.57.30 "free -h | head -3"
echo.
echo === So thread cua game server ===
ssh root@180.93.57.30 "ls /proc/$(pgrep -f KPAH2_patched.jar)/task 2>/dev/null | wc -l"
echo.
echo === Nhan vat trong DB ===
ssh root@180.93.57.30 "mysql -u root -t -e 'SELECT id,charname,userId,ban FROM kpah_db.tob_char;'"
echo.
echo === Tai khoan ===
ssh root@180.93.57.30 "mysql -u root -t -e 'SELECT id,username,ban FROM account.team_user;'"
echo.
echo === Ban jar dang chay ===
ssh root@180.93.57.30 "cd /opt/kpah && git log -1 --format='%%h %%s'"
echo.
pause
