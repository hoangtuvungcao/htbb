@echo off
setlocal enabledelayedexpansion
title Build HTBB_NEW.jar tu source
cd /d "%~dp0"

REM ============================================================
REM  Build HTBB_NEW.jar tu src\  (jar SACH, dung tu dau)
REM
REM  Chay sau khi sua bat ky file .java nao trong src\.
REM  Xong thi chay capnhat.bat de thay vao htbb-server.jar.
REM
REM  Ghi chu lich su: ban cu chong class moi len jar cu ("jar uf") vi so
REM  mat util/Common.class va real/SkinThuNuoi.class - hai class khong co
REM  ma nguon trong src\. Da kiem tra bytecode toan bo 139 class trong jar:
REM  KHONG class nao tham chieu toi hai class do, ke ca dang Class.forName.
REM  Chung la rac mo coi. Ba entry .txt trong jar cung vo dung vi source
REM  khong he goi getResource/getResourceAsStream lan nao - moi file deu
REM  doc bang new File(...) theo thu muc lam viec.
REM  => dung jar sach duoc, va nho vay class xoa di trong src\ khong con bi
REM     ket lai vinh vien trong jar nua.
REM ============================================================

echo ============================================================
echo   BUILD HTBB_NEW.jar TU SOURCE (src\)
echo ============================================================
echo.

REM ---- Tim mot JDK co DU ca javac.exe lan jar.exe ----
REM javac tren PATH thuong la shim cua Oracle (Common Files\Oracle\Java\javapath)
REM chi co java/javac/javaw/jshell, KHONG co jar.exe -> phai do tim JDK that.
set "JDKBIN="
for /f "delims=" %%J in ('where javac 2^>nul') do (
    if not defined JDKBIN (
        for %%D in ("%%~dpJ") do (
            if exist "%%~fD\jar.exe" set "JDKBIN=%%~fD"
        )
    )
)
if not defined JDKBIN (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\jar.exe" set "JDKBIN=%%D\bin"
    )
)
if not defined JDKBIN (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%D\bin\jar.exe" set "JDKBIN=%%D\bin"
    )
)
if not defined JDKBIN (
    for /d %%D in ("C:\Program Files\Microsoft\jdk*") do (
        if exist "%%D\bin\jar.exe" set "JDKBIN=%%D\bin"
    )
)
if not defined JDKBIN (
    echo [LOI] Khong tim thay JDK co ca javac.exe lan jar.exe.
    echo       Cai JDK ^(khuyen nghi 17-21^) roi thu lai, hoac dat bien
    echo       JDKBIN trong file nay tro thang toi thu muc "bin" cua JDK.
    pause
    exit /b 1
)
echo   JDK: %JDKBIN%
echo.

set "OUT=build\classes"
set "MF=build\manifest.mf"
set "LIBCP=lib\jxl-2.6.jar;lib\mysql-connector-java-5.1.49.jar;lib\htbb-core.jar"
set "MAINCLASS=server.TeamServer"

if not exist build mkdir build
if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

echo [1/4] Liet ke file .java...
dir /s /b src\*.java > build\sources.txt
for /f %%C in ('type build\sources.txt ^| find /c /v ""') do set SOCFILE=%%C
echo       %SOCFILE% file.

echo [2/4] Bien dich (javac --release 8)...
REM QUAN TRONG: VPS chay Java 8 (class version 52). Build bang --release cao hon
REM tao class file VPS KHONG CHAY DUOC ("UnsupportedClassVersionError ... class
REM file version 63.0, may nay chi doc duoc toi 52.0") -> server crash-loop,
REM cong 19129 khong bao gio mo. DUNG doi so 8 nay tru khi VPS da nang JDK.
"%JDKBIN%\javac.exe" --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "%LIBCP%" -sourcepath src -d "%OUT%" @build\sources.txt
if errorlevel 1 (
    echo.
    echo [LOI] Bien dich that bai. Xem loi phia tren. KHONG dong goi jar.
    pause
    exit /b 1
)
for /f %%C in ('dir /s /b "%OUT%\*.class" ^| find /c /v ""') do set SOCLASS=%%C
echo       OK - %SOCLASS% class.

echo [3/4] Dong goi HTBB_NEW.jar...
REM Class-Path tro toi lib\ - la duong dan TUONG DOI so voi cho dat jar, tuc
REM /opt/htbb/lib tren VPS (WorkingDirectory=/opt/htbb). Doi ten thu muc lib
REM la server chet ngay luc khoi dong vi khong thay driver MySQL.
> "%MF%" echo Manifest-Version: 1.0
>> "%MF%" echo Main-Class: %MAINCLASS%
>> "%MF%" echo Class-Path: lib/jxl-2.6.jar lib/mysql-connector-java-5.1.49.jar lib/htbb-core.jar
>> "%MF%" echo.

if exist HTBB_NEW.jar del /f /q HTBB_NEW.jar
"%JDKBIN%\jar.exe" cfm HTBB_NEW.jar "%MF%" -C "%OUT%" .
if errorlevel 1 (
    echo [LOI] Dong goi jar that bai.
    pause
    exit /b 1
)

echo [4/4] Kiem tra jar...
REM Ba kiem tra toi thieu truoc khi cho phep dem len server that.
"%JDKBIN%\jar.exe" tf HTBB_NEW.jar > build\entries.txt
if errorlevel 1 (
    echo   [LOI] Jar khong doc duoc.
    pause
    exit /b 1
)
set "MAINPATH=%MAINCLASS:.=/%.class"
findstr /x /c:"%MAINPATH%" build\entries.txt >nul
if errorlevel 1 (
    echo   [LOI] Khong thay Main-Class %MAINCLASS% trong jar. Server se khong khoi dong duoc.
    pause
    exit /b 1
)
for /f %%C in ('findstr /e ".class" build\entries.txt ^| find /c /v ""') do set SOJAR=%%C
if not "%SOJAR%"=="%SOCLASS%" (
    echo   [LOI] Jar co %SOJAR% class nhung bien dich ra %SOCLASS%. Khong khop.
    pause
    exit /b 1
)
echo       OK - %SOJAR% class, Main-Class %MAINCLASS% co mat.

echo.
echo ============================================================
echo   XONG: da tao HTBB_NEW.jar (%SOJAR% class)
echo   Buoc tiep theo:
echo     1. capnhat.bat  -^> thay vao htbb-server.jar (co backup) + chay lai
echo     2. Test server chay on tren may ban
echo     3. git add htbb-server.jar ^&^& git commit ^&^& git push
echo        (BAT BUOC - deploy-auto.sh tren VPS chi pull file .jar co san,
echo         no KHONG tu bien dich .java)
echo ============================================================
pause
