#!/bin/sh
# Sinh lai TAT CA anh cua 5 skin thu cuoi tu 5 bang sprite goc.
#
# Chuoi tool:
#   ChuanBiBangSprite  bang sprite (nhieu con dinh nhau, da tach nen) -> bang chuan
#                      3 hang x 5 frame  (hang 0 xuong / 1 len / 2 ngang)
#   TaoEffThuCuoi      bang chuan -> map/d_effect/<id>.png + <id>  (con thu chay tren ban do)
#   TaoAvtThuTuBang    bang chuan -> map/avtathu/<id>.png  (anh 3 frame trong bang Trang bi thu)
#   TaoIconSkin        anh avtathu -> map/icon/<id>.png    (icon vat pham trong hanh trang)
#
# Tham so cua tung bang do bang tay: so hang, so o moi hang, va frame nao thuoc huong nao.
# Cach do: chay ChuanBiBangSprite mot lan, mo <ten>_bang.png (bang co danh so) ra xem, roi
# dien vao --socot va --lay. Nhan dang huong: thay mat = xuong, thay yen/lung = len, than
# nam ngang = ngang. Xem PHAN J trong HUONG_DAN_BUILD_VPS.md.
#
# Chay:  sh tools/tao_skin_thu.sh
set -e
JAVA=${JAVA:-"/c/Program Files/Java/jdk-26.0.1/bin/java.exe"}
JAVAC=${JAVAC:-"/c/Program Files/Java/jdk-26.0.1/bin/javac.exe"}
OUT=${OUT:-out_tools}
# Chieu cao con thu tren ban do (px). Rong Bang cao 44, giu bang no cho khoi lech.
CAO=${CAO:-44}
# Chieu cao mot frame trong bang Trang bi thu
CAO_AVT=${CAO_AVT:-48}
# Thu muc chua 5 bang sprite goc (anh PNG da tach nen, thuong da phong 4x)
NGUON=${NGUON:-.}

"$JAVAC" -nowarn -d "$OUT" tools/CatSpriteSheet.java tools/TaoDataSprite.java \
    tools/TaoEffThuCuoi.java tools/ChuanBiBangSprite.java tools/TaoAvtThuTuBang.java \
    tools/TaoIconSkin.java

# --- Buoc 1: cat frame, xep thanh bang chuan 3 hang x 5 frame ---
# <anh> <ten> <so hang> <so o moi hang> <frame cua tung huong: xuong|len|ngang>
B() { "$JAVA" -cp "$OUT" ChuanBiBangSprite "$NGUON/$1" "$2" --hang="$3" --socot="$4" \
        --lay="$5" --mongcot=3 --khe=1; }

B 02.png phucthu   4 4,4,4,3      "7,12,13,14,12|0,1,3,4,8|2,5,6,9,10"
B 04.png gautruc   3 5,5,5        "3,4,7,10,11|0,1,2,5,6|8,9,12,13,14"
B 03.png tienloc   5 4,4,4,3,3    "1,4,9,10,13|2,7,12,15,16|0,3,5,6,8"
B 05.png xichduong 3 6,6,6        "0,1,2,3,6|4,8,9,12,13|5,10,11,15,16"
B 01.png chienlang 6 3,3,3,3,2,2  "2,3,7,8,2|0,1,4,5,9|10,12,13,14,15"

# --- Buoc 2: sprite con thu chay tren ban do (map/d_effect) ---
# --khonglat vi bang nay ve con thu nhin sang TRAI san (client tu lat khi di phai).
# Bang nao ve nhin phai thi bo --khonglat di.
E() { "$JAVA" -cp "$OUT" TaoEffThuCuoi map/sprite_moi/$1.png "$2" \
        --xuong=0 --len=1 --ngang=2 --nguong=-1 --cao="$CAO" --khonglat \
        --cotxuong=5 --cotlen=5 --cotngang=5; }

E phucthu   159
E gautruc   197
E tienloc   198
E xichduong 199
E chienlang 212

# --- Buoc 3: anh 3 frame cho bang Trang bi thu (map/avtathu) ---
# Lay hang 0 (nhin truoc), ba frame 0-2-4. PHAI dung 3 frame: client chi chay {0,1,2}
# khi numFrame = 3, khac 3 la chi ve frame 0 va 1 voi nhip cham gap doi.
A() { "$JAVA" -cp "$OUT" TaoAvtThuTuBang map/sprite_moi/$1.png 0 0,2,4 "$CAO_AVT" map/avtathu/$2.png; }

A phucthu   21
A gautruc   22
A tienloc   23
A xichduong 24
A chienlang 25

# --- Buoc 4: icon vat pham trong hanh trang (map/icon) ---
# Id icon phai con trong va < 855; 815..819 la 5 id trong da chon, khop cot xstart
# trong sql/skin_thu_item.sql.
I() { "$JAVA" -cp "$OUT" TaoIconSkin map/avtathu/$1.png "$2" --frame=3; }

I 21 815
I 22 816
I 23 817
I 24 818
I 25 819

echo ""
echo "== File du lieu frame PHAI giong nhau tung byte giua ca 5 con =="
echo "   (khac nhau la client con giu du lieu cu se ve ra khoi vun -> phai bat tat app)"
md5sum map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212
if [ "$(md5sum map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212 | awk '{print $1}' | sort -u | wc -l)" != "1" ]; then
    echo "LOI: du lieu khong giong nhau. Xem PHAN J muc 4 trong HUONG_DAN_BUILD_VPS.md."
    exit 1
fi
echo "OK"
