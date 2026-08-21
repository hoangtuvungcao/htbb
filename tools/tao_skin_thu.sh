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
# 0 = giu nguyen kich thuoc pixel goc (khong resample -> khong nhoe vien).
# Dat so duong chi khi frame that su to hon muc muon co.
CAO=${CAO:-0}
# Chieu cao mot frame trong bang Trang bi thu
CAO_AVT=${CAO_AVT:-0}
# Thu muc chua 5 bang sprite goc (anh PNG da tach nen, thuong da phong 4x)
NGUON=${NGUON:-map/sprite_goc}

"$JAVAC" -nowarn -d "$OUT" tools/CatSpriteSheet.java tools/TaoDataSprite.java \
    tools/TaoEffThuCuoi.java tools/ChuanBiBangSprite.java tools/TaoAvtThuTuBang.java \
    tools/TaoIconSkin.java

# --- Buoc 1: cat frame, xep thanh bang chuan 3 hang x 5 frame ---
# <anh> <ten> <so hang> <so o moi hang> <frame cua tung huong: xuong|len|ngang>
B() { "$JAVA" -cp "$OUT" ChuanBiBangSprite "$NGUON/$1" "$2" --hang="$3" --socot="$4" \
        --lay="$5" --mongcot=3 --khe=1; }

B bang_phucthu.png phucthu   4 4,4,4,3      "7,12,13,14,12|0,1,3,4,8|2,5,6,9,10"
B bang_gautruc.png gautruc   3 5,5,5        "3,4,7,10,11|0,1,2,5,6|8,9,12,13,14"
B bang_tienloc.png tienloc   5 4,4,4,3,3    "1,4,9,10,13|2,7,12,15,16|0,3,5,6,8"
B bang_xichduong.png xichduong 3 6,6,6        "0,1,2,3,6|4,8,9,12,13|5,10,11,15,16"
B bang_chienlang.png chienlang 6 3,3,3,3,2,2  "2,3,7,8,2|0,1,4,5,9|10,12,13,14,15"

# --- Buoc 2: sprite con thu chay tren ban do (map/d_effect) ---
# --khonglat vi bang nay ve con thu nhin sang TRAI san (client tu lat khi di phai).
# Bang nao ve nhin phai thi bo --khonglat di.
# Tham so thu 3 (tuy chon) = --thoxuong=x,y,z rieng cho con thu do; bo trong thi dung
# mac dinh {10,8,4} cua TaoEffThuCuoi (do tu Rong Bang / heo bang, than to).
E() { local thoxuong="$3"; "$JAVA" -cp "$OUT" TaoEffThuCuoi map/sprite_moi/$1.png "$2" \
        --xuong=0 --len=1 --ngang=2 --nguong=-1 --cao="$CAO" --khonglat \
        --cotxuong=5 --cotlen=5 --cotngang=5 ${thoxuong:+--thoxuong=$thoxuong}; }

# Phuc Thu: than chi cao ~34px (nho hon Rong Bang/heo bang nhieu) nen mac dinh {10,8,4}
# lam nhan vat ngoi sat DINH con thu, gan nhu troi lung lo phia tren (than con thu bi
# nhan vat che het). Do do tu anh d_effect/159 (top=30,bottom=63 trong o 64px) + muc
# nang nhan vat khi cuoi (Char.dy_horse_tool, cot xuong/len/ngang) -> nhac nhan vat ngoi
# ~65% sau vao than con thu. THU NGHIEM DAU TIEN, can anh xem lai trong game roi bao
# lai de chinh tiep, khong the kiem chung tu day.
E phucthu   159 -10,-2,-3
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
# trong vps/db_new_server.sql PHAN 2F.
I() { "$JAVA" -cp "$OUT" TaoIconSkin map/avtathu/$1.png "$2" --frame=3; }

I 21 815
I 22 816
I 23 817
I 24 818
I 25 819

echo ""
echo "== Kich thuoc file du lieu frame (phai giong nhau: cung luoi 64x64, cung so o) =="
# Truoc day ca 5 con dung chung THO_XUONG nen file du lieu giong nhau TUNG BYTE — do
# la ly do PHAN J muc 4 (HUONG_DAN_BUILD_VPS.md) dung md5sum lam kiem tra nhanh. Tu khi
# Phuc Thu (159) dung --thoxuong rieng (than qua nho, nhan vat ngoi troi len dinh con
# thu) thi 159 KHONG con giong 4 con kia nua — do la CO Y, khong phai loi tao sprite.
# Ly do an toan van giu duoc: hai lop chan khoi vun (goi anh -51 day lai luc doi skin,
# va luoi o 64x64 CO DINH giu vi tri o khong doi) hoat dong THEO TUNG ID rieng, khong
# phu thuoc 5 file co giong byte nhau hay khong. Nen chi con kiem tra CO CAU TRUC giu
# nguyen (cung do dai file — WB/HB/CAO_BONG/so o KHONG doi), khong con doi bang byte.
ls -l map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212
soDoDaiKhacNhau=$(for f in map/d_effect/159 map/d_effect/197 map/d_effect/198 map/d_effect/199 map/d_effect/212; do
    wc -c < "$f"
done | sort -u | wc -l)
if [ "$soDoDaiKhacNhau" != "1" ]; then
    echo "LOI: do dai file khac nhau -> co con dung so o/so anh con khac (WB/HB/CAO_BONG/so"
    echo "phan moi frame bi doi). Xem PHAN J muc 4 trong HUONG_DAN_BUILD_VPS.md."
    exit 1
fi
echo "OK"
