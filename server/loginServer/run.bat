@echo off
title Login server HTBB (cong 8023)
cd /d "%~dp0"

REM Ten file PHAI dung hoa/thuong: CheckLoginSocket.jar
REM Ban cu ghi "Checkloginsocket.jar" - chay duoc tren Windows vi Windows khong phan biet
REM hoa/thuong, nhung ai chep dong lenh nay sang Linux la an "Unable to access jarfile".
REM vps/kpah-login.service dung dung ten, nen production khong bao gio dinh loi nay.

java -jar CheckLoginSocket.jar
