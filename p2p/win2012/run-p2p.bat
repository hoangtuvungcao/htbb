@echo off
setlocal
title HTBB game server + Peerova P2P (cong game 19129, P2P UDP/TCP sv.p2p_port)
cd /d "%~dp0"

REM ============================================================
REM  Chay login server + game server CO Peerova P2P nhung (nhanh p2p, ban thu Windows Server 2012).
REM  Dat file nay trong thu muc server\ (canh htbb-server.jar). Can:
REM    - Java 8 64-bit (hoac 11/17/21 64-bit) - peerova_jni.dll la 64-bit, Java 32-bit KHONG nap duoc
REM    - p2p\peerova_jni.dll (ban win7 = chay tren Server 2012 / 2012 R2 / 8 / 7 / 10 / 11)
REM    - server.ini co sv.p2p=1 (xem server.ini.p2p.example), loginServer\server.ini
REM  Ticket P2P: in ra cua so game + ghi vao p2p\ticket.txt + nut "Ticket P2P" trong AdminPanel.
REM  Xem docs\p2p-win2012.md.
REM ============================================================

set "JAVA=java"
if exist "%~dp0jre\bin\java.exe" set "JAVA=%~dp0jre\bin\java.exe"
if not "%HTBB_JAVA%"=="" set "JAVA=%HTBB_JAVA%"

"%JAVA%" -version >nul 2>&1
if errorlevel 1 (
    echo [LOI] Khong chay duoc java. Cai Java 8 64-bit ^(Temurin 8 x64^) hoac dat HTBB_JAVA=C:\...\bin\java.exe
    pause
    exit /b 1
)
"%JAVA%" -XshowSettings:properties -version 2>&1 | findstr /c:"sun.arch.data.model = 64" >nul
if errorlevel 1 (
    echo [LOI] Java dang dung KHONG phai 64-bit. peerova_jni.dll la 64-bit - cai Java 64-bit.
    "%JAVA%" -version
    pause
    exit /b 1
)
if not exist "p2p\peerova_jni.dll" (
    echo [LOI] Thieu p2p\peerova_jni.dll ^(lay tu p2p\out\dist\windows-x86_64-win7\ cua may build^).
    pause
    exit /b 1
)
if not exist "server.ini" (
    echo [LOI] Thieu server.ini ^(chep tu vps\server.ini.linux.example, sua db.* va sv.pathLog, them khoi server.ini.p2p.example^).
    pause
    exit /b 1
)
findstr /b /c:"sv.p2p=1" server.ini >nul
if errorlevel 1 echo [CANH BAO] server.ini chua co dong sv.p2p=1 - game server se chay KHONG co P2P.
if not exist "logs" mkdir logs

REM Login server (cong 8023, chi noi bo - KHONG mo ra internet)
start "HTBB login server" /d "%~dp0loginServer" "%JAVA%" -Xmx128m -Dfile.encoding=UTF-8 -jar htbb-login.jar
timeout /t 5 /nobreak >nul

REM Game server (Peerova khoi dong ngay truoc vong accept, in TICKET)
"%JAVA%" -Xms256m -Xmx1024m -Dfile.encoding=UTF-8 -Dpeerova.jni.path="%~dp0p2p\peerova_jni.dll" -jar htbb-server.jar
echo Game server da dung.
pause
