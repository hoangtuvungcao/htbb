#!/bin/sh
# Kiem toan bo monorepo sau khi gop: test server, jar J2ME + KiemPokeDuLieu, APK, bien dich C# (PC + ClientUnity), web.
#   sh tools/doi_ten/kiem_tat_ca.sh <monorepo> <thu muc ket qua> [<thu muc server goc de so sanh test>]
# Thu muc server goc (vd. ban chep HTBB truoc khi gop, cung thoi diem): so danh sach test HONG - phai TRUNG KHOP.
# Ma thoat 0 = tat ca dat. Tung buoc ghi log vao <ket qua>/<buoc>.log.
[ $# -ge 2 ] || { echo "dung: sh kiem_tat_ca.sh <monorepo> <thu muc ket qua> [server goc]"; exit 2; }
M=$(cd "$1" && pwd -P); RA=$2; GOC_SV=${3:-}
mkdir -p "$RA"; RA=$(cd "$RA" && pwd -P)
JAVA8=${JAVA8_HOME:-/usr/lib/jvm/java-8-openjdk-amd64}
NODE_BIN=${NODE_BIN:-$HOME/.nvm/versions/node/v22.22.2/bin}
UNITY_DLL=${UNITY_DLL:-"$HOME/.steam/debian-installation/steamapps/common/VTube Studio/VTube Studio_Data/Managed"}
LOI=""
ghi() { printf '%-28s %s\n' "$1" "$2" | tee -a "$RA/tong_ket.txt"; }
: > "$RA/tong_ket.txt"

test_sv() {  # $1 thu muc server, $2 ra, $3 jar loi
    d=$1; o=$2; LIB=lib/jxl-2.6.jar:lib/mysql-connector-java-5.1.49.jar:$3
    rm -rf "$o"; mkdir -p "$o"
    (cd "$d" && find src test -name '*.java' > "$o/s.txt" &&
     javac --release 8 -encoding UTF-8 -nowarn -Xlint:none -cp "$LIB" -sourcepath src:test -d "$o/cls" @"$o/s.txt" > "$o/javac.log" 2>&1 &&
     timeout 1200 java -cp "$o/cls:$LIB" Kiem > "$o/kiem.log" 2>&1)
    grep '^\s*HONG ' "$o/kiem.log" 2>/dev/null | sort -u > "$o/hong.txt"
    grep 'HONG:' "$o/kiem.log" 2>/dev/null || { [ -s "$o/javac.log" ] && tail -3 "$o/javac.log"; echo "(khong co dong tong)"; }
}

# 1. test server
kq=$(test_sv "$M/server" "$RA/server" lib/htbb-core.jar)
if [ -n "$GOC_SV" ]; then
    lj=lib/htbb-core.jar; [ -f "$GOC_SV/$lj" ] || lj=lib/NQSH_5h.jar
    kq0=$(test_sv "$GOC_SV" "$RA/server_goc" $lj)
    if diff "$RA/server_goc/hong.txt" "$RA/server/hong.txt" > "$RA/server_khac.diff"; then
        ghi "test server" "OK $kq (trung khop ban goc: $kq0)"
    else
        ghi "test server" "LECH: $kq vs goc $kq0 - xem $RA/server_khac.diff"; LOI="$LOI server"
    fi
else
    ghi "test server" "$kq"
fi

# 2. jar J2ME + KiemPokeDuLieu
if (cd "$M" && env JAVA8_HOME="$JAVA8" MAY_CHU=127.0.0.1:19129 MAY_CHU_CHINH=127.0.0.1:19129 HTBB_ASSETS=pc sh client_jar/build.sh 97) > "$RA/jar.log" 2>&1; then
    ghi "jar J2ME" "OK $(cd "$M/client_jar/out" && ls HTBB-v97*.jar | tr '\n' ' ')"
else ghi "jar J2ME" "LOI (xem jar.log)"; LOI="$LOI jar"; fi
CP="out/build/cls:out/build/dg:tools/midp.jar:tools/microemulator.jar:out/build/api"
if (cd "$M/client_jar" && rm -rf out/kpdl && mkdir -p out/kpdl && javac -nowarn -encoding UTF-8 -d out/kpdl -cp "$CP" tools/KiemPokeDuLieu.java &&
    java -Djava.awt.headless=true -cp "out/kpdl:$CP:res" KiemPokeDuLieu ../client_pc_ios/Assets res) > "$RA/kpdl.log" 2>&1 && tail -1 "$RA/kpdl.log" | grep -q '^OK'; then
    ghi "KiemPokeDuLieu" "OK"
else ghi "KiemPokeDuLieu" "LOI (xem kpdl.log)"; LOI="$LOI kpdl"; fi

# 2b. client Java desktop (client_pc/, port tu code jar)
if [ -f "$M/client_pc/build.sh" ]; then
    if (cd "$M" && timeout 1800 sh client_pc/build.sh) > "$RA/pc_java.log" 2>&1; then
        ghi "client PC Java" "$(grep '^OK' "$RA/pc_java.log" | head -1 | cut -c1-80)"
    else ghi "client PC Java" "LOI (xem pc_java.log)"; LOI="$LOI pc_java"; fi
fi

# 3. APK (debug, Gradle offline)
if (cd "$M" && MAY_CHU=10.0.2.2:19129 timeout 2400 sh client_apk/build-pokemon.sh) > "$RA/apk.log" 2>&1; then
    ghi "APK" "$(tail -1 "$RA/apk.log")"
else ghi "APK" "LOI (xem apk.log)"; LOI="$LOI apk"; fi

# 4. C# (khong co Unity tren may: bien dich thu bang dotnet + UnityEngine*.dll)
if command -v dotnet >/dev/null && ls "$UNITY_DLL"/UnityEngine*.dll >/dev/null 2>&1; then
    for t in pc:client_pc_ios/Assets/Scripts cu:client_pc_ios/ClientUnity/Assets/Scripts; do
        ten=${t%%:*}; src=$M/${t#*:}; d=$RA/cs_$ten; mkdir -p "$d"
        [ -d "$src" ] || { ghi "C# $ten" "bo qua (khong co $src)"; continue; }
        cat > "$d/cs_$ten.csproj" <<EOF
<Project Sdk="Microsoft.NET.Sdk"><PropertyGroup><TargetFramework>netstandard2.1</TargetFramework>
<EnableDefaultCompileItems>false</EnableDefaultCompileItems><LangVersion>9.0</LangVersion><Nullable>disable</Nullable>
<NoWarn>CS0162;CS0168;CS0219;CS0414;CS0649;CS0169;CS0108;CS0114;CS0618;CS0672;CS1717;CS0675;CS0665;CS0252;CS0253;CS0659;CS0661;CS0660;CS1998;CS8632</NoWarn>
<AllowUnsafeBlocks>true</AllowUnsafeBlocks><GenerateAssemblyInfo>false</GenerateAssemblyInfo><OutputType>Library</OutputType></PropertyGroup>
<ItemGroup><Compile Include="$src/**/*.cs" /><Reference Include="$UNITY_DLL/UnityEngine*.dll" /></ItemGroup></Project>
EOF
        if (cd "$d" && timeout 900 dotnet build -nologo -v q) > "$d/build.log" 2>&1; then ghi "C# $ten" "OK"
        else ghi "C# $ten" "LOI (xem cs_$ten/build.log)"; LOI="$LOI cs_$ten"; fi
    done
else ghi "C#" "bo qua (khong co dotnet / UnityEngine.dll)"; fi

# 5. web
if (cd "$M/web" && PATH="$NODE_BIN:$PATH" && export PATH && timeout 1200 npx nuxt build) > "$RA/web_build.log" 2>&1; then
    ghi "web build" "OK"
else ghi "web build" "LOI (xem web_build.log)"; LOI="$LOI web_build"; fi
(cd "$M/web" && PATH="$NODE_BIN:$PATH" && export PATH && npm test) > "$RA/web_test.log" 2>&1
ghi "web test" "$(grep -E '^# (pass|fail)' "$RA/web_test.log" | tr '\n' ' ')"
# web test: so voi ban goc bang tay (agent khac co the dang sua do)

# 6. con sot ten cu (bo dong co dau tuong thich)
n=$(cd "$M" && git grep -Iil 'kpah' -- ':!tools/doi_ten' ':!vps/CHUYEN_TEN_HTBB.md' ':!.claude' 2>/dev/null | wc -l)
ghi "tep con chu kpah" "$n (gom dong TUONG-THICH-TEN-CU + ten bao ve; xem doi_ten_kpah.py --kiem tren ban chep)"

[ -z "$LOI" ] && { echo "TAT CA DAT"; exit 0; }
echo "HONG:$LOI"; exit 1
