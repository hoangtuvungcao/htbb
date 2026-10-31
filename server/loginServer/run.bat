@echo off
title Login server HTBB (cong 8023)
cd /d "%~dp0"

REM Ten file PHAI dung hoa/thuong: CheckLoginSocket.jar
REM Ban cu ghi "Checkloginsocket.jar" - chay duoc tren Windows vi Windows khong phan biet
REM hoa/thuong, nhung ai chep dong lenh nay sang Linux la an "Unable to access jarfile".
REM vps/htbb-login.service dung dung ten, nen production khong bao gio dinh loi nay.

REM 31/10/2026: login server HTBB (htbb-login.jar, ma nguon server\src\com\htbb\dangnhap, build: sh build.sh).
REM QUAY LUI ve ban cu: dat bien truoc khi chay  set HTBB_LOGIN_JAR=CheckLoginSocket.jar
if "%HTBB_LOGIN_JAR%"=="" set HTBB_LOGIN_JAR=htbb-login.jar
java -Dfile.encoding=UTF-8 -jar %HTBB_LOGIN_JAR%
