@echo off
REM May VPS: dat bien HTBB_VPS (vd. setx HTBB_VPS root@ten-may) hoac khai Host htbb-vps trong %USERPROFILE%\.ssh\config
if not defined HTBB_VPS set "HTBB_VPS=root@htbb-vps"
REM ============================================================
REM  Xem log game server tren VPS theo thoi gian thuc
REM  Ctrl+C de thoat
REM ============================================================
title Log game server VPS
echo Dang ket noi %HTBB_VPS% ... (Ctrl+C de thoat)
echo.
ssh %HTBB_VPS% "tail -n 80 -f /opt/htbb/logs/game.out"
pause
