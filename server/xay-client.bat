@echo off
setlocal enabledelayedexpansion
rem ===================================================================
rem  Xay client Unity cho tung nen tang.
rem
rem  Dung:
rem    xay-client.bat taonguon   - tao tai nguyen tu ban client J2ME
rem    xay-client.bat chuanbi    - tao canh + dat cau hinh du an
rem    xay-client.bat windows    - xuat file .exe
rem    xay-client.bat webgl      - xuat ban chay trong trinh duyet
rem    xay-client.bat android    - xuat file .apk   (can cai module Android)
rem    xay-client.bat ios        - xuat du an Xcode (can cai module iOS + may Mac)
rem
rem  KHONG co lenh pause o cuoi: de chay duoc tu kich ban tu dong.
rem ===================================================================

set "GOC=%~dp0"
if "%GOC:~-1%"=="\" set "GOC=%GOC:~0,-1%"
set "DUAN=%GOC%\ClientUnity"

rem --- Tim Unity. Dat bien moi truong UNITY_EXE de dung ban khac. ---
if not defined UNITY_EXE set "UNITY_EXE=C:\Program Files\Unity\Hub\Editor\6000.5.9f1\Editor\Unity.exe"
if not exist "%UNITY_EXE%" (
    echo LOI: khong thay Unity tai "%UNITY_EXE%"
    echo Dat bien UNITY_EXE tro toi Unity.exe cua ban roi chay lai.
    exit /b 2
)

rem --- Tim JDK cho buoc tao tai nguyen. ---
if not defined JAVA_BIN set "JAVA_BIN=C:\Program Files\Java\jdk-26.0.1\bin"

set "VIEC=%~1"
if "%VIEC%"=="" goto :huongdan

if /i "%VIEC%"=="taonguon" goto :taonguon
if /i "%VIEC%"=="chuanbi"  set "HAM=XayDung.ChuanBi"    & goto :chayunity
if /i "%VIEC%"=="windows"  set "HAM=XayDung.XayWindows" & goto :chayunity
if /i "%VIEC%"=="webgl"    set "HAM=XayDung.XayWebGL"   & goto :chayunity
if /i "%VIEC%"=="android"  set "HAM=XayDung.XayAndroid" & goto :chayunity
if /i "%VIEC%"=="ios"      set "HAM=XayDung.XayIOS"     & goto :chayunity
goto :huongdan

rem -------------------------------------------------------------------
:taonguon
if "%~2"=="" (
    echo LOI: thieu duong dan toi thu muc client J2ME da giai nen.
    echo Vi du: xay-client.bat taonguon C:\Users\ADMIN\Downloads\KPAH_276-1
    exit /b 2
)
if not exist "%~2" (
    echo LOI: khong thay thu muc "%~2"
    exit /b 2
)
echo [1/2] Bien dich cong cu chuyen tai nguyen...
if not exist "%GOC%\build\tools-classes" mkdir "%GOC%\build\tools-classes"
"%JAVA_BIN%\javac.exe" --release 8 -encoding UTF-8 -nowarn ^
    -d "%GOC%\build\tools-classes" "%GOC%\tools\ChuyenTaiNguyen.java"
if errorlevel 1 exit /b 1

echo [2/2] Chuyen tai nguyen...
"%JAVA_BIN%\java.exe" -cp "%GOC%\build\tools-classes" ChuyenTaiNguyen ^
    "%~2" "%DUAN%\Assets\Scripts\Game" "%DUAN%\Assets\Resources\res"
exit /b %errorlevel%

rem -------------------------------------------------------------------
:chayunity
echo Chay Unity: %HAM%
echo (lan dau se lau vi Unity phai nhap toan bo tai nguyen)
"%UNITY_EXE%" -batchmode -quit -nographics ^
    -projectPath "%DUAN%" -executeMethod %HAM% ^
    -logFile "%GOC%\build\unity-%VIEC%.log"
set MA=%errorlevel%
if not "%MA%"=="0" (
    echo.
    echo THAT BAI ^(ma %MA%^). Xem nhat ky: build\unity-%VIEC%.log
    echo Cac dong loi:
    findstr /i /c:"error" /c:"Exception" "%GOC%\build\unity-%VIEC%.log"
    exit /b %MA%
)
echo Xong. Ket qua trong ClientUnity\Build\
exit /b 0

rem -------------------------------------------------------------------
:huongdan
echo.
echo   xay-client.bat taonguon ^<thu-muc-J2ME^>   tao tai nguyen game
echo   xay-client.bat chuanbi                    tao canh + cau hinh
echo   xay-client.bat windows^|webgl^|android^|ios  xuat ban cai dat
echo.
echo   Lan dau lam theo thu tu: taonguon -^> chuanbi -^> windows
echo.
exit /b 2
