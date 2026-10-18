@echo off
REM May VPS: dat bien HTBB_VPS (vd. setx HTBB_VPS root@ten-may) hoac khai Host htbb-vps trong %USERPROFILE%\.ssh\config
if not defined HTBB_VPS set "HTBB_VPS=root@htbb-vps"
REM ============================================================
REM  Mo duong ham SSH de xem DB cua VPS bang cong cu tren Windows
REM  (HeidiSQL / MySQL Workbench / DBeaver / mysql.exe cua XAMPP)
REM
REM  127.0.0.1:3307 (may nay)  ->  127.0.0.1:3306 (VPS)
REM  Dung cong 3307 vi 3306 da bi MySQL cua XAMPP chiem.
REM
REM  DE CUA SO NAY MO trong luc lam viec. Dong cua so = dong duong ham.
REM
REM  Thong tin ket noi trong HeidiSQL:
REM     Network type : MariaDB or MySQL (TCP/IP)
REM     Hostname/IP  : 127.0.0.1
REM     Port         : 3307
REM     User         : htbb
REM     Password     : (mat khau DB tren VPS)
REM
REM  Hoac bang dong lenh:
REM     "C:\xampp\mysql\bin\mysql.exe" -h 127.0.0.1 -P 3307 -u htbb -p htbb_db
REM
REM  Cong 3306 tren VPS VAN DONG voi internet - duong ham di trong SSH nen an toan.
REM ============================================================
title Duong ham DB VPS - DE MO
echo Dang mo duong ham 127.0.0.1:3307  ->  VPS 127.0.0.1:3306
echo Ket noi bang: host 127.0.0.1 / port 3307 / user htbb
echo.
echo GIU CUA SO NAY MO. Ctrl+C de dong duong ham.
echo.
ssh -N -L 3307:127.0.0.1:3306 %HTBB_VPS%
pause
