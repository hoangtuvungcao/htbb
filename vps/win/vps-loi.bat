@echo off
REM ============================================================
REM  Loc nhanh cac dong loi trong log game server tren VPS
REM ============================================================
title Loi trong log VPS
echo === 30 dong loi gan nhat trong game.out ===
ssh root@180.93.57.30 "grep -n -iE 'exception|error|access denied|OutOfMemory|Killed' /opt/htbb/log/game.out | tail -30"
echo.
echo === Log login server ===
ssh root@180.93.57.30 "tail -n 15 /opt/htbb/log/login.out"
echo.
pause
