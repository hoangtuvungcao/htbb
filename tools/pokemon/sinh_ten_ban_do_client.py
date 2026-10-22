"""Sinh bảng TÊN BẢN ĐỒ cho client (0.9.5) từ SQL - nguồn duy nhất là bảng data_map_ten (sql/nang_cap.sql phan 5,
trang /admin/ten-hien). Client J2ME / PC / APK không đọc CSDL nên tên được đóng vào mã lúc build:

    python3 -I tools/pokemon/sinh_ten_ban_do_client.py         # ghi client_jar/src/game/pokemon/TenBanDoBang.java
    python3 -I tools/pokemon/sinh_ten_ban_do_client.py --kiem  # mã thoát 1 nếu tệp Java lệch SQL

Sửa tên trên web thì chạy lại lệnh này rồi build client (server đổi ngay, client theo bản phát hành kế tiếp).
"""
import os
import re
import sys

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
SQL = os.path.join(GOC, "sql", "nang_cap.sql")  # phan 5: data_map_ten
RA = os.path.join(GOC, "client_jar", "src", "game", "pokemon", "TenBanDoBang.java")


def doc():
    s = open(SQL, encoding="utf-8").read()
    i = s.index("INSERT INTO `data_map_ten`")
    j = s.index(";", s.index("VALUES", i))
    ds = []
    for m in re.finditer(r"\((\d+),'((?:[^'\\]|\\.|'')*)'", s[i:j]):
        ds.append((int(m.group(1)), m.group(2).replace("''", "'").replace("\\'", "'")))
    ds.sort()
    return ds


def java(ds):
    def q(t):
        return '"' + t.replace("\\", "\\\\").replace('"', '\\"') + '"'
    ids = ", ".join(str(i) for i, _ in ds)
    tens = ",\n        ".join(", ".join(q(t) for _, t in ds[k:k + 6]) for k in range(0, len(ds), 6))
    return '''package game.pokemon;

/**
 * TÊN BẢN ĐỒ người chơi thấy - SINH TỰ ĐỘNG từ SQL data_map_ten (tools/pokemon/sinh_ten_ban_do_client.py), KHÔNG SỬA
 * TAY. Tilemap.setNameMap / bản đồ vùng / xa phu dùng bảng này thay tên viết cứng cũ.
 */
public final class TenBanDoBang {
    static final short[] ID = {%s};
    static final String[] TEN = {
        %s
    };

    private TenBanDoBang() {
    }

    /** Tên bản đồ id (SQL); null = không có trong bảng. */
    public static String ten(int id) {
        int lo = 0, hi = ID.length - 1;
        while (lo <= hi) {
            int m = (lo + hi) >>> 1;
            if (ID[m] < id) {
                lo = m + 1;
            } else if (ID[m] > id) {
                hi = m - 1;
            } else {
                return TEN[m];
            }
        }
        return null;
    }

    /** Tên bản đồ id, không có thì tên dự phòng (đã đổi qua TenMapPokemon). */
    public static String ten(int id, String duPhong) {
        String t = ten(id);
        return t != null && t.length() > 0 ? t : TenMapPokemon.doiHien(duPhong);
    }
}
''' % (ids, tens)


def main():
    ds = doc()
    out = java(ds)
    if "--kiem" in sys.argv:
        cu = open(RA, encoding="utf-8").read() if os.path.exists(RA) else ""
        if cu != out:
            print("LOI: TenBanDoBang.java lệch SQL data_map_ten - chạy lại tools/pokemon/sinh_ten_ban_do_client.py")
            sys.exit(1)
        print("TenBanDoBang: %d ban do, khop SQL" % len(ds))
        return
    open(RA, "w", encoding="utf-8").write(out)
    print("ghi", os.path.relpath(RA, GOC), len(ds), "ban do")


if __name__ == "__main__":
    main()
