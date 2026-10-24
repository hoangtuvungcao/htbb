#!/bin/sh
# Kiểm tải J2ME thật (0.9.7.1): jar J2ME trong MicroEmulator, heap nhỏ, RMS giới hạn, socket chia mảnh nhỏ, cache rỗng,
# tự đăng nhập -> tạo / chọn Pokémon -> vào thế giới. Cần server dev đang chạy (docs/build.md) và tài khoản trống
# mật khẩu 123456 (mỗi lần chạy một tài khoản mới: cache server-side không ảnh hưởng, nhưng tên Pokémon sinh từ tk).
#
#   sh client_jar/tools/kiem_tai_j2me/chay.sh <jar> <cổng server> <tài khoản> [heap KB, mặc định 7168] [giây chờ]
#   Biến: W, H (cỡ màn, 240x320), MANH (byte tối đa mỗi lần socket trả, 512), RMS / RMS_BAN_GHI (byte, 0 = không giới
#         hạn), CAT_SAU (máy chủ "im" sau N byte ở kết nối đầu -> phải hiện hộp lỗi), THU_LAI=1 (bấm Thử lại),
#         DUNG_MS (canh màn tải nhanh để kiểm), CONG_PROXY (29600).
# Thoát 0 = vào thế giới (hoặc, với MONG_LOI=1, hiện hộp lỗi đúng thay vì quay mãi).
D=$(cd "$(dirname "$0")" && pwd); J=$(cd "$D/../.." && pwd)
[ $# -ge 3 ] || { sed -n '2,12p' "$0"; exit 2; }
B=${TMPDIR:-/tmp}/kiem_tai_j2me_lop; rm -rf "$B"; mkdir -p "$B"
javac -nowarn -encoding UTF-8 -cp "$J/tools/microemulator.jar" -d "$B" "$D/KiemTaiJ2me.java" || exit 2
HEAP=${4:-7168}
# 0.9.15: JVM gia lap het heap (7 MB) co the khong xu ly duoc ca SIGTERM -> treo mai (lan thu 24/10/2026 treo 5 gio).
# Luon chay duoi timeout co KILL: qua (giay cho + 120) s thi giet.
exec timeout -k 10 $(( ${5:-150} + 120 )) java -Xms${HEAP}k -Xmx${HEAP}k -Djava.awt.headless=true -Dw=${W:-240} -Dh=${H:-320} -Dmanh=${MANH:-512} \
  -Drms=${RMS:-0} -DrmsBanGhi=${RMS_BAN_GHI:-0} -DcatSau=${CAT_SAU:-0} -DthuLai=${THU_LAI:+true} -DmongLoi=${MONG_LOI:+true} \
  -DdungMs=${DUNG_MS:-0} -DcongProxy=${CONG_PROXY:-29600} \
  -cp "$J/tools/microemulator.jar:$B" KiemTaiJ2me "$1" "$2" "$3" "${5:-150}"
