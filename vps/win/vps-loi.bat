@echo off
REM May VPS: dat bien HTBB_VPS (vd. setx HTBB_VPS root@ten-may) hoac khai Host htbb-vps trong %USERPROFILE%\.ssh\config
if not defined HTBB_VPS set "HTBB_VPS=root@htbb-vps"
REM ============================================================
REM  Loc nhanh cac dong loi trong log game server tren VPS
REM ============================================================
title Loi trong log VPS
echo === 30 dong loi gan nhat trong game.out ===
ssh %HTBB_VPS% "grep -n -iE 'exception|error|access denied|OutOfMemory|Killed' /opt/htbb/logs/game.out | tail -30"
echo.
echo === Log login server ===
ssh %HTBB_VPS% "tail -n 15 /opt/htbb/logs/login.out"
echo.
pause
