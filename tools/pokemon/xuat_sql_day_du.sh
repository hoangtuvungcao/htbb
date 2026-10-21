#!/bin/sh
# Sinh sql/htbb_day_du.sql: CSDL DAY DU (account + htbb_db) cho server Huyen Thoai Bao Boi.
#
# Dung mot MariaDB/MySQL TRONG (khong co du lieu that) de dung lai tu dau theo DUNG thu tu
# duoi day, roi mysqldump ra MOT file. Server moi chi can: mysql < sql/htbb_day_du.sql
#
# Chay o thu muc HTBB/:
#   MYSQL="mysql -uroot -h127.0.0.1 -P3306" MYSQLDUMP="mysqldump -uroot -h127.0.0.1 -P3306" \
#     sh tools/pokemon/xuat_sql_day_du.sh
#
# CANH BAO: script XOA roi tao lai hai database `account` va `htbb_db` tren may chu chi dinh.
# Khong bao gio tro no vao CSDL that.
set -e
MYSQL=${MYSQL:-mysql -uroot}
MYSQLDUMP=${MYSQLDUMP:-mysqldump -uroot}
RA=sql/htbb_day_du.sql

# Migration cu: loi "da co" (1060 trung cot, 1062 trung khoa) la binh thuong vi seed da gom san.
MIGRATION="them_bang_2buoc them_bang_cam_may them_bang_dieu_khien_server them_bang_su_kien
them_bang_tob_log_online them_bang_tob_top_thuong them_bang_web_admin_log them_bang_web_chan_spam
them_bang_web_lenh_nhan_vat them_cot_kho_xu_luong them_khoa_duy_nhat_giftcode_log
them_phi_phong_vo_hinh them_su_kien_world_cup skin_linh_thu ten_mo_ta_skin_linh_thu
sua_type_skin_linh_thu xoa_su_kien_trung_lap"
# Pokemon: PHAI dung thu tu nay (pokemon_doi_chu cuoi cung).
POKEMON="them_cot_pokemon them_bang_pokedex pokemon_du_lieu_quai pokemon_doi_ten_vat_pham
pokemon_doi_ten_thu pokemon_vat_pham_eevee pokemon_icon_vat_pham pokemon_doi_chu"
# Thiet ke dai han - giai doan 2 (docs/thiet-ke-game.md): bang so lieu (staging)
# + bang moi cua game + vo hieu giftcode tt (GD2 1.10). Chay SAU POKEMON.
# pokemon_nhiem_vu: noi dung nhiem vu (hoi thoai, goi y, nhanh, an) - tools/pokemon/sinh_nhiem_vu.py.
LAU_DAI="pokemon_lau_dai pokemon_nhiem_vu them_bang_lau_dai vo_hieu_giftcode_tt"
# Noi dung day du 1-2 nam (docs/thiet-ke-game.md, sinh boi tools/pokemon/sinh_noi_dung.py):
# bang staging data_noi_dung_* + data_item id 1001..1199. Chay SAU LAU_DAI.
NOI_DUNG="pokemon_noi_dung"
# Nap tien (docs/van-hanh.md): bang nap_* (don, so cai, vi, cua hang, the thang, ve mua...).
# Tao bang KHONG bat gi (sv.nap_tien=0, HTBB_NAP_BAT=false mac dinh). Chay SAU NOI_DUNG.
NAP_TIEN="pokemon_nap_tien pokemon_thanh_vien"  # thanh_vien SAU nap_tien (dung nap_goi)
# He sinh thai game (docs/thiet-ke-game.md, sinh boi tools/pokemon/sinh_sql_he_sinh_thai.py):
# bang the gioi data_hst_* + bang nguoi choi tob_hst_* (khong bat gi: sv.he_sinh_thai=0 mac dinh).
HE_SINH_THAI="pokemon_he_sinh_thai"
# GD2b vat pham dung / do giu / bua bao ho (sinh boi tools/pokemon/sinh_vat_pham_lau_dai.py):
# data_item type 28 id 1201..1260 + data_vat_pham_lau_dai. Chi co tac dung khi sv.lau_dai=1.
VAT_PHAM_LAU_DAI="pokemon_vat_pham_lau_dai"
# Cau hinh trung tam (docs/cau-hinh.md, sinh boi tools/cauhinh/sinh_cau_hinh.py): bang phien ban /
# lich su / xac nhan / hieu luc / yeu cau khoi dong lai + danh muc khoa. KHONG ghi gia tri nao vao cau_hinh.
CAU_HINH_TT="pokemon_cau_hinh_trung_tam"
# Chi so trang bi (docs/thiet-ke-game.md, sinh boi tools/pokemon/SinhSqlChiSo.java): ten / don vi / mo ta
# 198 dong data_attribute (giu id) + kep ngan sach data_item (LEAST, chi giam). Chay SAU noi dung + vat pham (moi
# dong data_item da co) va SAU pokemon_doi_chu (ten moi thang). Idempotent.
CHI_SO="pokemon_chi_so"
# 0.9.8 bo trang bi / ngoc he / che tao / moc neo / thu phuc (docs/thiet-ke-game.md 4.2-4.3, 7.7): bang data_* moi + cot
# moi cua data_noi_dung_che_tao / tob_pokedex. Chay SAU noi dung + chi so. Idempotent.
BO_NGOC_BAT="them_bo_ngoc_che_tao_bat"
# Thuat ngu chuan (docs/thiet-ke-game.md, sinh boi tools/pokemon/sinh_sql_thuat_ngu.py): UPDATE doi thuat
# ngu cu con sot trong du lieu goc. Chay CUOI CUNG. BO_THUAT_NGU=1 bo buoc nay (de sinh lai chinh file do).
THUAT_NGU="pokemon_thuat_ngu"
[ "${BO_THUAT_NGU:-0}" = "1" ] && THUAT_NGU=""

$MYSQL -e "DROP DATABASE IF EXISTS account; CREATE DATABASE account CHARACTER SET utf8;
DROP DATABASE IF EXISTS htbb_db; CREATE DATABASE htbb_db CHARACTER SET utf8;"
$MYSQL --default-character-set=utf8 account < server/loginServer/account.sql > /dev/null
$MYSQL --default-character-set=utf8 htbb_db < sql/gamedb7_clean.sql
# Admin trong ban dump co mat khau NGAU NHIEN (khong ai biet): sau khi nap, dat lai mat khau admin
# (docs/trien-khai.md). Khong bao gio commit mat khau that vao repo.
MK_NGAU_NHIEN=$(od -An -N12 -tx1 /dev/urandom | tr -d ' \n')
sed "s/^SET @admin_pass = .*/SET @admin_pass = '$MK_NGAU_NHIEN';/" vps/db_new_server.sql \
    | $MYSQL --default-character-set=utf8 htbb_db > /dev/null
for f in $MIGRATION; do
    $MYSQL -f --default-character-set=utf8 htbb_db < sql/$f.sql 2>&1 | grep -v "ERROR 106[02]" || true
done
for f in $POKEMON; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $LAU_DAI; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $NOI_DUNG; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $NAP_TIEN; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $HE_SINH_THAI; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $VAT_PHAM_LAU_DAI; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $CAU_HINH_TT; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $CHI_SO; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $BO_NGOC_BAT; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done
for f in $THUAT_NGU; do
    $MYSQL --default-character-set=utf8 htbb_db < sql/$f.sql
done

{
    echo "-- Huyen Thoai Bao Boi (HTBB) - CSDL DAY DU cho server moi (account + htbb_db)."
    echo "-- SINH TU tools/pokemon/xuat_sql_day_du.sh - KHONG SUA TAY. Gom: gamedb7_clean.sql + vps/db_new_server.sql"
    echo "-- + moi migration trong sql/ + moi file sql/pokemon_*.sql (thu tu xem script). Nap: mysql < sql/htbb_day_du.sql"
    $MYSQLDUMP --default-character-set=utf8 --databases account htbb_db --skip-lock-tables \
        --no-tablespaces --skip-comments --skip-dump-date
} > "$RA"
echo "Da ghi $RA ($(wc -c < "$RA") byte, $(grep -c '^CREATE TABLE' "$RA") bang)"
