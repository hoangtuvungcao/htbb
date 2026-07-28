#!/bin/sh
# Sinh lai ca 5 sprite skin thu cuoi.
#
# Tham so cua tung con da do bang tay tren bang sprite goc, dung ghi nho lai:
#   --botrai   bo cot chu nhan (Idle/Run/Left/Right/Up/Down) o le trai bang
#   --xuong    hang nhin truoc (chay xuong)   --oxuong  khoang o dung trong hang do
#   --len      hang nhin sau  (chay len)      --olen
#   --ngang    hang nhin ngang               --ongang
#   --khonglat bang nay ve con thu nhin sang TRAI san, khong phai lat
#   --cao      chieu cao chung cho ca 5 con
#
# Vi sao phai chi dinh khoang o: bang sprite AI nhieu khi don hai goc nhin vao cung
# mot hang. Tien loc hang "Run Down" thuc ra la 3 con nhin truoc roi 3 con nhin sau;
# gau truc thi ba con cuoi hang bi ve dau teo. Lay ca hang thi frame chay bi sai.
#
# Chay:  sh tools/tao_skin_thu.sh
set -e
JAVA=${JAVA:-"/c/Program Files/Java/jdk-26.0.1/bin/java.exe"}
JAVAC=${JAVAC:-"/c/Program Files/Java/jdk-26.0.1/bin/javac.exe"}
OUT=${OUT:-out_tools}
CAO=${CAO:-44}

"$JAVAC" -nowarn -d "$OUT" tools/CatSpriteSheet.java tools/TaoDataSprite.java tools/TaoEffThuCuoi.java

T() { "$JAVA" -cp "$OUT" TaoEffThuCuoi "$@" --khonglat --cao="$CAO"; }

T map/sprite_goc/phucthu.png   159 --botrai=480 --xuong=6 --len=5 --ngang=3
T map/sprite_goc/gautruc.png   197 --botrai=460 --xuong=6 --oxuong=0-2 --len=5 --olen=0-2 --ngang=3
T map/sprite_goc/tienloc.png   198 --botrai=20  --xuong=8 --oxuong=0-2 --len=6 --olen=1-5 --ngang=3 --ongang=1-4
T map/sprite_goc/xichduong.png 199 --botrai=140 --xuong=6 --len=5 --ngang=3
T map/sprite_goc/chienlang.png 212 --botrai=200 --xuong=5 --len=4 --ngang=2

echo ""
echo "== File du lieu frame PHAI giong nhau tung byte giua ca 5 con =="
md5sum map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212
if [ "$(md5sum map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212 | awk '{print $1}' | sort -u | wc -l)" != "1" ]; then
    echo "LOI: du lieu khong giong nhau -> se phai bat moi nguoi tat app. Xem PHAN J muc 4."
    exit 1
fi
echo "OK"
