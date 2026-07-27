@echo off
REM ============================================================
REM  Xem log game server tren VPS theo thoi gian thuc
REM  Ctrl+C de thoat
REM ============================================================
title Log game server VPS
echo Dang ket noi 180.93.57.30 ... (Ctrl+C de thoat)
echo.
ssh root@180.93.57.30 "tail -n 80 -f /opt/kpah/log/game.out"
pause
