#!/bin/sh
# Sinh sql/htbb_day_du.sql: CSDL DAY DU (account + kpah_db) cho server Huyen Thoai Bao Boi.
#
# Dung mot MariaDB/MySQL TRONG (khong co du lieu that) de dung lai tu dau theo DUNG thu tu
# duoi day, roi mysqldump ra MOT file. Server moi chi can: mysql < sql/htbb_day_du.sql
#
# Chay o thu muc HTBB/:
#   MYSQL="mysql -uroot -h127.0.0.1 -P3306" MYSQLDUMP="mysqldump -uroot -h127.0.0.1 -P3306" \
#     sh tools/pokemon/xuat_sql_day_du.sh
#
# CANH BAO: script XOA roi tao lai hai database `account` va `kpah_db` tren may chu chi dinh.
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

$MYSQL -e "DROP DATABASE IF EXISTS account; CREATE DATABASE account CHARACTER SET utf8;
DROP DATABASE IF EXISTS kpah_db; CREATE DATABASE kpah_db CHARACTER SET utf8;"
$MYSQL --default-character-set=utf8 account < loginServer/account.sql > /dev/null
$MYSQL --default-character-set=utf8 kpah_db < sql/gamedb7_clean.sql
$MYSQL --default-character-set=utf8 kpah_db < vps/db_new_server.sql > /dev/null
for f in $MIGRATION; do
    $MYSQL -f --default-character-set=utf8 kpah_db < sql/$f.sql 2>&1 | grep -v "ERROR 106[02]" || true
done
for f in $POKEMON; do
    $MYSQL --default-character-set=utf8 kpah_db < sql/$f.sql
done

{
    echo "-- Huyen Thoai Bao Boi (HTBB) - CSDL DAY DU cho server moi (account + kpah_db)."
    echo "-- SINH TU tools/pokemon/xuat_sql_day_du.sh - KHONG SUA TAY. Gom: gamedb7_clean.sql + vps/db_new_server.sql"
    echo "-- + moi migration trong sql/ + moi file sql/pokemon_*.sql (thu tu xem script). Nap: mysql < sql/htbb_day_du.sql"
    $MYSQLDUMP --default-character-set=utf8 --databases account kpah_db --skip-lock-tables \
        --no-tablespaces --skip-comments --skip-dump-date
} > "$RA"
echo "Da ghi $RA ($(wc -c < "$RA") byte, $(grep -c '^CREATE TABLE' "$RA") bang)"
