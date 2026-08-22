#!/bin/sh
# =====================================================================================
#  Dung lai TOAN BO bo anh cua 5 skin linh thu tu 5 bang sprite goc.
# =====================================================================================
#
#  Chay:  sh tools/tao_skin_linh_thu.sh
#
#  Dau vao : map/sprite_goc/bang_*.png   (bang sprite goc, nen trong suot)
#  Dau ra  : map/skinthu/<n>.png         atlas con thu tren ban do
#            map/skinthu/<n>             du lieu 21 frame
#            map/skinthu/avt<n>.png      dai 3 frame cho bang "Trang bi thu"
#            map/icon/<855+n>.png        icon vat pham trong hanh trang
#            map/skinthu_goc/*.png       anh de xem lai, KHONG dung luc chay game
#
#  Sau khi chay xong phai khoi dong lai server: DataGame nap bo anh mot lan luc bat may.
#
# -------------------------------------------------------------------------------------
#  THAM SO CUA TUNG BANG — do bang mat, khong tu dong duoc
# -------------------------------------------------------------------------------------
#  Cac bang sprite nay ve tay / ve bang AI nen moi bang mot kieu: hang khong deu, cac
#  con de len nhau, co bang con ve them bong do va manh yen roi. Da thu ca hai cach tu
#  dong va deu that bai:
#    - cat theo hang/cot trong hoan toan: khong bang nao co hang trong ca
#    - go vung lien thong: cac con ve dinh nhau nen ca bang ra mot vung duy nhat
#  Nen cach lam la: chay "SkinThu xem" de tool cat thu roi mo file _roi.png ra doi
#  chieu, dieu chinh --hang / --socot cho den khi so o dung, sau do doc tung o de xep
#  vao ba huong.
#
#  Cach nhan huong khi xem map/skinthu_goc/<ten>_roi.png:
#    XUONG = nhin thay ro mat, hai mat doi dien nguoi xem
#    LEN   = nhin thay lung / yen / gay, khong thay mat
#    NGANG = than nam ngang, dau o mot ben
#
#  --xuong / --len / --ngang la DANH SACH UNG VIEN, khong phai thu tu frame. Cu liet ke
#  het cac o thuoc huong do (nhieu hon 5 cang tot), tool tu chon ra 5 va tu xep:
#    - hai tu the giong nhau nhat -> luc dung yen (Fhorse 0, 1)
#    - bo ba it lech nhau nhat    -> luc di chuyen (Fhorse 2, 3, 4)
#  Neu khong tim duoc bo nao du giong thi tool tu rut xuong lap lai tu the, va in ra
#  "2 tu the (A-B-A)" hoac "dung [x,x]". Xem chu thich chonTuThe() trong SkinThu.java.
#
#  Lam vay vi cac bang nay khong phai bo frame cua mot hoat anh ma la nhieu buc ve roi
#  rac, moi buc mot co. Cu lay theo thu tu trong bang thi cac tu the lech nhau 14%-48%
#  ve hinh dang va toi 48% ve be ngang — con thu doi ca kich thuoc moi khi doi frame,
#  nhin rat giat. Cho tool chon theo do giong nhau thi con 0%-25%.
# =====================================================================================
set -e
JAVA=${JAVA:-java}
JAVAC=${JAVAC:-javac}
OUT=${OUT:-out_tools}

"$JAVAC" -nowarn -d "$OUT" tools/SkinThu.java tools/KiemTraSkinThu.java

# --- 0 Phuc Thu -----------------------------------------------------------------------
# Bang 716x572. Cot ben phai lech hang so voi ba cot trai nen phai chi dinh --hang tay.
# Huong xuong chi co 3 tu the dung duoc (o 7, 12, 13) va ba tu the do lech nhau kha
# nhieu, nen tool se tu rut ve lap lai tu the. Day la huong con giat nhat trong ca bo
# (21%), do ban than bang sprite chu khong phai do cach cat.
"$JAVA" -cp "$OUT" SkinThu tao map/sprite_goc/bang_phucthu.png 0 855 \
    --alpha=100 --hang=0-145,150-292,296-421,436-572 --socot=4,4,4,3 \
    --xuong=7,12,13 --len=0,1,3,4,8 --ngang=2,5,6,9,10

# --- 1 Gau Truc -----------------------------------------------------------------------
# Bang 820x428, ba hang deu nhau nen tu do hang duoc, chi can chi so o moi hang.
# KHONG liet ke o 8 cho huong ngang: o do co mot manh yen ve roi DINH LIEN vao than con
# gau nen khong loc ra duoc, keo khung bao dai xuong lam con thu bi thu nho va lo lung
# tren mat dat.
"$JAVA" -cp "$OUT" SkinThu tao map/sprite_goc/bang_gautruc.png 1 856 \
    --alpha=100 --socot=5,5,5 \
    --xuong=3,4,7,10,11 --len=0,1,2,5,6 --ngang=9,12,13,14

# --- 2 Tien Loc -----------------------------------------------------------------------
# Bang 476x688. --cao=135: con nay co bo gac rat cao, neu chuan hoa ca gac ve 39px nhu
# cac con khac thi phan THAN chi con hon chuc diem anh, nguoi ngoi lo lung tren ngon
# gac. Phong to 135% thi than vua tam lung, gac vuot len tren dau nguoi ngoi — dung
# dang mot con huou cuoi. Khong len duoc 140%: bo gac lam frame qua rong, 16 anh khong
# xep vua atlas 256x256.
"$JAVA" -cp "$OUT" SkinThu tao map/sprite_goc/bang_tienloc.png 2 857 \
    --alpha=100 --pchang=40 --socot=4,4,4,3,3 --cao=135 \
    --xuong=1,4,9,10,11,13,14 --len=2,7,12,15,16,17 --ngang=0,3,5,6,8

# --- 3 Xich Duong ---------------------------------------------------------------------
# Bang 720x396, ba hang 6 o deu nhau.
"$JAVA" -cp "$OUT" SkinThu tao map/sprite_goc/bang_xichduong.png 3 858 \
    --alpha=100 --socot=6,6,6 \
    --xuong=4,8,9,12,13,14 --len=0,1,2,3,6,7 --ngang=5,10,11,15,16,17

# --- 4 Chien Lang ---------------------------------------------------------------------
# Bang 388x852, sau hang cao thap khac nhau nen chi dinh --hang tay. Hang thu tu (o
# 9,10,11) de tool tu do (--socot phan tu thu 4 = 0) vi ba o hang do rong khac han nhau,
# chia deu se cat vao giua con thu.
"$JAVA" -cp "$OUT" SkinThu tao map/sprite_goc/bang_chienlang.png 4 859 \
    --alpha=100 --hang=8-140,165-300,300-445,450-570,615-725,740-852 --socot=3,3,3,0,2,2 \
    --xuong=2,4,5,6,9 --len=0,1,3,7,8 --ngang=10,12,13,14,15

# --- Kiem tra: chay lai dung thuat toan ve cua client tren file vua sinh ---------------
echo ""
"$JAVA" -cp "$OUT" KiemTraSkinThu
echo ""
echo "Mo map/skinthu_goc/<n>_nhutrongame.png de xem con thu se hien the nao trong game."
echo "Hinh nguoi mau xanh la vi tri nhan vat ngoi, vach do la mat dat."
