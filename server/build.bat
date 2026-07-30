@echo off
setlocal enabledelayedexpansion
title Build KPAH2_NEW.jar tu source
cd /d "%~dp0"

echo ============================================================
echo   BUILD KPAH2_NEW.jar TU SOURCE (src\)
echo ============================================================
echo.
echo   Chay sau khi ban sua bat ky file .java nao trong src\.
echo   Sau khi build xong, chay capnhat.bat de thay vao KPAH2_patched.jar
echo   (co backup tu dong) va khoi dong lai server.
echo.

REM ---- Tim javac.exe + jar.exe (cung mot JDK) ----
set "JAVAC="
set "JARTOOL="
for /f "delims=" %%J in ('where javac 2^>nul') do (
    if not defined JAVAC set "JAVAC=%%J"
)
if not defined JAVAC (
    echo [LOI] Khong tim thay javac trong PATH. Can cai JDK ^(khuyen nghi JDK 17-21^)
    echo       va them vao PATH, hoac cai lai Java.
    pause
    exit /b 1
)
for %%J in ("%JAVAC%") do set "JDK_BIN=%%~dpJ"
if exist "%JDK_BIN%jar.exe" set "JARTOOL=%JDK_BIN%jar.exe"

REM javac tren PATH doi khi la shim khong co jar.exe cung thu muc (vi du
REM Common Files\Oracle\Java\javapath cua Java 8 cu) -> do tim trong
REM cac thu muc JDK pho bien tren Windows.
if not defined JARTOOL (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\jar.exe" set "JARTOOL=%%D\bin\jar.exe"
    )
)
if not defined JARTOOL (
    for /d %%D in ("C:\Program Files\Eclipse Adoptium\jdk*") do (
        if exist "%%D\bin\jar.exe" set "JARTOOL=%%D\bin\jar.exe"
    )
)
if not defined JARTOOL (
    echo [LOI] Tim thay javac tai:
    echo         %JAVAC%
    echo       nhung khong thay jar.exe cung thu muc, va khong tu do duoc trong
    echo       C:\Program Files\Java\jdk* hay C:\Program Files\Eclipse Adoptium\jdk*.
    echo       Mo thu muc "bin" cua JDK ban dang cai, kiem tra co jar.exe khong,
    echo       roi sua bien JARTOOL trong file build.bat nay tro thang toi do.
    pause
    exit /b 1
)

echo   javac: %JAVAC%
echo   jar:   %JARTOOL%
echo.

set "OUT=build\ant-classes"
set "MANIFEST=build\manifest.mf"
set "LIBCP=libs\jxl-2.6.jar;libs\mysql-connector-java-5.1.49.jar;libs\NQSH_5h.jar"

if exist "%OUT%" rmdir /s /q "%OUT%"
mkdir "%OUT%"

echo Dang liet ke file .java...
dir /s /b src\*.java > build\sources.txt
if not exist build\sources.txt (
    echo [LOI] Khong liet ke duoc file .java.
    pause
    exit /b 1
)

echo Dang bien dich (javac --release 8, khop dung Java 8 dang chay tren VPS)...
REM QUAN TRONG: VPS chay Java 8 (openjdk-8-jre-headless, xem HUONG_DAN_BUILD_VPS.md
REM phan B1: "jar server bien dich o class version 52 = Java 8"). Build bang
REM --release cao hon (vi du 17, 19) tao class file VPS KHONG CHAY DUOC:
REM "UnsupportedClassVersionError ... class file version 63.0, may nay chi doc
REM duoc toi 52.0" -> server crash-loop, cong 19129 khong bao gio mo. DUNG doi so nay
REM tru khi VPS da nang cap JDK va ban chac chan.
"%JAVAC%" --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "%LIBCP%" -sourcepath src -d "%OUT%" @build\sources.txt
if errorlevel 1 (
    echo.
    echo [LOI] Bien dich that bai. Xem loi phia tren. KHONG dong goi jar.
    pause
    exit /b 1
)
echo   Bien dich xong, khong loi.

echo.
echo Dang copy file tai nguyen (khong phai .java/.form)...
robocopy src "%OUT%" /S /XF *.java *.form /NFL /NDL /NJH /NJS /NP >nul

echo.
echo Dang dong goi KPAH2_NEW.jar...
REM QUAN TRONG: KHONG tao jar moi hoan toan tu %OUT%. Trong KPAH2_patched.jar
REM hien tai co class util/Common.class KHONG CO MA NGUON trong src/ (xem
REM HUONG_DAN_BUILD_VPS.md PHAN I) - dong jar moi tu dau se lam mat class do
REM va server crash luc chay toi cho dung no. Phai CHONG lop moi len jar cu
REM (jar uf = update, giu lai entry cu khong bi ghi de).
if not exist KPAH2_patched.jar (
    echo [LOI] Khong thay KPAH2_patched.jar de chong len. Dung lai de khong lam mat class cu.
    pause
    exit /b 1
)
if exist KPAH2_NEW.jar del /f /q KPAH2_NEW.jar
copy /Y KPAH2_patched.jar KPAH2_NEW.jar >nul
"%JARTOOL%" uf KPAH2_NEW.jar -C "%OUT%" .
if errorlevel 1 (
    echo [LOI] Cap nhat jar that bai.
    pause
    exit /b 1
)

echo Dang kiem tra jar khong bi mat class nao so voi ban cu...
"%JARTOOL%" tf KPAH2_patched.jar | findstr /e ".class" > build\classes_cu.txt
"%JARTOOL%" tf KPAH2_NEW.jar | findstr /e ".class" > build\classes_moi.txt

if exist build\check_classes.ps1 del /f /q build\check_classes.ps1
echo $cu = Get-Content 'build\classes_cu.txt' >> build\check_classes.ps1
echo $moi = Get-Content 'build\classes_moi.txt' >> build\check_classes.ps1
echo $mat = $cu ^| Where-Object { $_ -notin $moi } >> build\check_classes.ps1
echo if ($mat) { >> build\check_classes.ps1
echo Write-Host "  [CANH BAO] Cac class sau bi MAT so voi jar cu:" >> build\check_classes.ps1
echo $mat ^| ForEach-Object { Write-Host "    $_" } >> build\check_classes.ps1
echo exit 1 >> build\check_classes.ps1
echo } else { >> build\check_classes.ps1
echo Write-Host "  OK: khong mat class nao so voi jar cu." >> build\check_classes.ps1
echo exit 0 >> build\check_classes.ps1
echo } >> build\check_classes.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File build\check_classes.ps1
if errorlevel 1 (
    echo   [DUNG LAI] Co class bi mat, KPAH2_NEW.jar co the lam server crash. Kiem tra lai truoc khi dung.
    pause
    exit /b 1
)

echo.
echo ============================================================
echo   XONG: da tao KPAH2_NEW.jar
echo   Buoc tiep theo:
echo     1. Chay capnhat.bat  -^> thay vao KPAH2_patched.jar (co backup) + khoi dong lai
echo     2. Test server chay on tren may ban
echo     3. git add KPAH2_patched.jar ^&^& git commit ^&^& git push
echo        (day la buoc BAT BUOC de VPS nhan duoc ban build moi - deploy-auto.sh
echo         tren VPS CHI pull file .jar co san, KHONG tu bien dich .java)
echo ============================================================
pause
