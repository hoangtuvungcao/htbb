#!/usr/bin/env python3
"""KIỂM KÊ TRANG BỊ + ICON (trước / sau đồ họ mới) -> du_lieu/KIEM_KE_TRANG_BI.md. Chạy lại được, đừng sửa tay tệp ra.

Đọc: sql/htbb_day_du.sql (data_item gốc), du_lieu/noi_dung_trang_bi.csv, du_lieu/bang_icon.csv, du_lieu/noi_dung_bo.csv,
du_lieu/do_moi_trang_bi.csv (đồ họ mới), server/map/icon_pokemon + server/map/icon_do_moi.
Chạy: python3 -I tools/do_trang_phuc/kiem_ke.py
"""
import collections
import csv
import hashlib
import os
import sys

DAY = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, DAY)
import sinh  # noqa: E402
import danh_muc as DM  # noqa: E402

GOC = sinh.GOC
TEN_O = {0: "Khăn quàng", 1: "Đai", 2: "Băng đô", 3: "Đồ cầm tay Charmander", 4: "Đồ cầm tay Bulbasaur",
         5: "Đồ cầm tay Eevee", 6: "Đồ cầm tay Squirtle", 7: "Đồ cầm tay Pichu", 8: "Huy hiệu", 9: "Vòng cổ",
         10: "Lông vũ", 11: "Găng", 12: "Phiến đá hệ", 19: "Ruy băng"}
DAI = [("1–19", 0, 1), ("20–39", 2, 3), ("40–59", 4, 5), ("60–79", 6, 7), ("80–98", 8, 10)]
TU_KIEM_HIEP = ["kiếm", "đao", "giáp", "áo", "quần", "giày", "nhẫn", "ngọc bội", "phi phong", "dây chuyền", "nón",
                "thương", "cung", "trượng", "quạt"]


def bac(level):
    return 10 if level >= 98 else max(0, min(9, level // 10))


def md5(p):
    return hashlib.md5(open(p, "rb").read()).hexdigest()


def main():
    mon = sinh.doc_data_item()
    trang_bi = {i: r for i, r in mon.items() if r[2] in TEN_O}
    moi = list(csv.DictReader(open(os.path.join(GOC, "du_lieu", "do_moi_trang_bi.csv"), encoding="utf8")))
    goc_csv = list(csv.DictReader(open(os.path.join(GOC, "du_lieu", "noi_dung_trang_bi.csv"), encoding="utf8")))
    o = []
    w = o.append
    w("# Kiểm kê trang bị, bộ và icon (đồ họ mới + trang phục, 0.9.8)\n\n")
    w("> **Tệp sinh** bởi `tools/do_trang_phuc/kiem_ke.py` - đừng sửa tay. Danh mục đồ mới: `tools/do_trang_phuc/danh_muc.py`.\n\n")
    # ---------------------------------------------------------------- 1. trước
    w("## 1. Hiện trạng trước khi thêm (data_item trong `sql/htbb_day_du.sql`)\n\n")
    w("- %d mẫu trang bị nhân vật (type 0..12, 19) trong `data_item`; %d dòng \"chuẩn\" ô × bậc ở `noi_dung_trang_bi.csv` "
      "(14 ô × 11 bậc, đồ cầm tay tách 5 dòng Pokémon). Độ hiếm là thuộc tính của **từng món** (`colorName`), không phải "
      "của mẫu: mọi ô × bậc đều ra được Thường / Hiếm / Quý / Sử thi (Huyền thoại = Sử thi + 3 lỗ từ chế tạo).\n"
      % (len(trang_bi), len(goc_csv)))
    # trùng tên
    ten = collections.Counter(r[1] for r in trang_bi.values())
    trung = [(t, n) for t, n in ten.items() if n > 1]
    w("- **Trùng tên**: %d tên dùng cho nhiều mẫu (di sản kiếm hiệp chia mẫu theo giới tính / style / cấp 1-4-9), "
      "tổng %d mẫu. Ví dụ: %s.\n" % (len(trung), sum(n for _, n in trung),
                                      ", ".join("\"%s\" ×%d" % x for x in sorted(trung, key=lambda x: -x[1])[:6])))
    # icon theo ô
    w("- **Icon dùng chung giữa các bậc** (xstart giống nhau cho cả 11 bậc của một ô):\n\n")
    w("| Ô | Số mẫu | Số icon khác nhau | Icon (xstart) |\n|---|---|---|---|\n")
    for t in sorted(TEN_O):
        rows = [r for r in trang_bi.values() if r[2] == t]
        xs = collections.Counter(r[20] for r in rows)
        w("| %s | %d | %d | %s |\n" % (TEN_O[t], len(rows), len(xs), ", ".join("%d×%d" % (k, v) for k, v in xs.most_common(4))))
    cu = [r for r in trang_bi.values() if r[20] < 14000]
    w("\n- **Icon kiếm hiệp còn sót** (xstart < 14000, dải icon gốc game cũ): %d mẫu trang bị%s.\n"
      % (len(cu), (" (" + ", ".join("%d %s" % (r[0], r[1]) for r in cu[:8]) + ")") if cu else ""))
    kh = [r for r in trang_bi.values() if any(x in r[1].lower().split() for x in TU_KIEM_HIEP)]
    w("- **Tên kiếm hiệp còn sót** trong mẫu trang bị: %d%s.\n" % (len(kh), (" (" + ", ".join(r[1] for r in kh[:6]) + ")") if kh else ""))
    # icon ảnh trùng nội dung
    thu = os.path.join(GOC, "server", "map", "icon_pokemon")
    hs = collections.defaultdict(list)
    for f in os.listdir(thu):
        if f.endswith(".png"):
            hs[md5(os.path.join(thu, f))].append(int(f[:-4]))
    trung_anh = [sorted(v) for v in hs.values() if len(v) > 1]
    w("- **Ảnh icon trùng hệt nhau** trong `map/icon_pokemon` (cùng nội dung, khác số): %d nhóm%s. Icon PMDCollab "
      "(CC BY-NC) - không dùng cho đồ mới.\n" % (len(trung_anh), (" (" + "; ".join(",".join(map(str, g)) for g in trung_anh[:5]) + ")") if trung_anh else ""))
    # bộ
    bo = list(csv.DictReader(open(os.path.join(GOC, "du_lieu", "noi_dung_bo.csv"), encoding="utf8")))
    w("- **Bộ**: chỉ có %d dòng bộ bậc (11 bộ × 4/7/11 món, cùng thưởng +5%% HP / +5%% tấn công / +3%% chí mạng mọi bậc). "
      "Chưa có bộ theo hệ (đặc tả 4.4 \"Bộ hệ\") hay theo vùng; không món nào mang tên / hình Pokémon.\n" % len(bo))
    w("- **Trang trí**: 8 bảng màu trang phục + 7 kiểu phụ kiện vẽ theo trang bị (`NgoaiHinh`); cửa hàng lượng "
      "(`nap_cua_hang`) có skin / hào quang nhưng **không hình nào hiện trên Pokémon** và không có ô ngân sách chỉ số.\n\n")
    # khoảng trống theo dải cấp x ô
    w("### 1.1 Khoảng trống theo dải cấp × ô (số mẫu có tên/hình riêng ngoài món gốc)\n\n")
    w("| Ô | " + " | ".join("cấp " + d[0] for d in DAI) + " |\n|---|" + "---|" * len(DAI) + "\n")
    for t in sorted(TEN_O):
        cells = []
        for _, b0, b1 in DAI:
            ten_rieng = {r[1] for r in trang_bi.values() if r[2] == t and b0 <= bac(r[6]) <= b1}
            cells.append("%d tên, 0 họ" % len(ten_rieng))
        w("| %s | %s |\n" % (TEN_O[t], " | ".join(cells)))
    w("\nMỗi ô × dải chỉ có 1-2 tên chung (\"Khăn quàng bạc\"), không có lựa chọn ngang nào (hệ, vùng), nên người chơi "
      "không có lý do đổi đồ trong cùng bậc ngoài độ hiếm.\n\n")
    # ---------------------------------------------------------------- 2. sau
    w("## 2. Sau khi thêm\n\n")
    so_he = sum(1 for r in moi if r["loai"] == "he")
    so_vung = len(moi) - so_he
    w("- **%d món mới** (`data_item` %s..%s, `sql/nang_cap.sql` (khối `them_do_trang_phuc.sql`)): %d món 18 bộ hệ (6 ô × 5 bậc) + %d món 3 bộ "
      "huy chương vùng (8 ô × 11 bậc). Mỗi món là bản sao chỉ số của món gốc cùng ô + bậc (không sức mạnh dọc mới).\n"
      % (len(moi), moi[0]["id_item"], moi[-1]["id_item"], so_he, so_vung))
    w("- **%d icon mới** 16x16 vẽ tay (`server/map/icon_do_moi`, id ảnh 15000+n), không trùng nhau: " % len([f for f in os.listdir(os.path.join(GOC, "server", "map", "icon_do_moi")) if f.endswith(".png")]))
    hs2 = collections.defaultdict(list)
    for f in os.listdir(os.path.join(GOC, "server", "map", "icon_do_moi")):
        if f.endswith(".png"):
            hs2[md5(os.path.join(GOC, "server", "map", "icon_do_moi", f))].append(f)
    w("%d nhóm trùng nội dung.\n" % sum(1 for v in hs2.values() if len(v) > 1))
    w("\n| Ô | " + " | ".join("cấp " + d[0] for d in DAI) + " |\n|---|" + "---|" * len(DAI) + "\n")
    for t in sorted(TEN_O):
        cells = []
        for _, b0, b1 in DAI:
            n = sum(1 for r in moi if int(r["type"]) == t and b0 <= int(r["bac"]) <= b1)
            ho = {r["ma_ho"] for r in moi if int(r["type"]) == t and b0 <= int(r["bac"]) <= b1}
            cells.append("%d món, %d họ" % (n, len(ho)))
        w("| %s | %s |\n" % (TEN_O[t], " | ".join(cells)))
    w("\n- Dải cấp 1–19: bộ hệ bắt đầu ở bậc bạc (cấp 20) theo đặc tả (người mới dùng món gốc + bộ vùng Kanto); mọi ô "
      "khác có ít nhất một họ mới ở mọi dải.\n")
    w("- Độ hiếm: không đổi cơ chế - món họ mới rơi với đúng độ hiếm của món gốc mà nó thay (Thường..Sử thi, "
      "Huyền thoại qua chế tạo), khung độ hiếm PNG mới `client_jar/res/pk/khung_hiem.png` (6 bậc, Sử thi+ lấp lánh).\n")
    w("- Trang trí: %d trang phục, %d hào quang, %d skin thú cưỡi, %d thẻ bạn đồng hành (`data_trang_phuc`).\n\n"
      % (len(DM.TRANG_PHUC), len(DM.NGUON_HAO_QUANG), len(DM.NGUON_SKIN_THU), len(DM.NGUON_THE)))
    open(os.path.join(GOC, "du_lieu", "KIEM_KE_TRANG_BI.md"), "w", encoding="utf8").write("".join(o))
    print("-> du_lieu/KIEM_KE_TRANG_BI.md")


if __name__ == "__main__":
    main()
