@echo off
REM May VPS: dat bien HTBB_VPS (vd. setx HTBB_VPS root@ten-may) hoac khai Host htbb-vps trong %USERPROFILE%\.ssh\config
if not defined HTBB_VPS set "HTBB_VPS=root@htbb-vps"
REM ============================================================
REM  Xem nhanh tinh trang server tren VPS: service, cong, RAM, thread, DB
REM ============================================================
title Trang thai server VPS
echo === Service ===
ssh %HTBB_VPS% "systemctl is-active htbb-game htbb-login htbb-web mariadb nginx 2>/dev/null"
echo.
echo === Cong dang mo (19129 = game, 8023 = login, 3000 = web Nuxt, 80/443 = nginx) ===
ssh %HTBB_VPS% "ss -lntp | grep -E ':(19129|8023|80|443|3000|3306)'"
echo.
echo === RAM ===
ssh %HTBB_VPS% "free -h | head -3"
echo.
echo === So thread cua game server ===
ssh %HTBB_VPS% "ls /proc/$(pgrep -f htbb-server.jar)/task 2>/dev/null | wc -l"
echo.
echo === Nhan vat trong DB ===
ssh %HTBB_VPS% "mysql -u root -t -e 'SELECT id,charname,userId,ban FROM htbb_db.tob_char;'"
echo.
echo === Tai khoan ===
ssh %HTBB_VPS% "mysql -u root -t -e 'SELECT id,username,ban FROM account.team_user;'"
echo.
echo === Ban jar dang chay ===
ssh %HTBB_VPS% "cd /opt/htbb && git log -1 --format='%%h %%s'"
echo.
pause
