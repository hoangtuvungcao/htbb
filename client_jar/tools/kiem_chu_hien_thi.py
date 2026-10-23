#!/usr/bin/env python3
"""Soát CHỮ HIỆN RA cho người chơi trong mã client chung (J2ME / PC / APK: client_jar/src + client_jar/shared).

    python3 -I client_jar/tools/kiem_chu_hien_thi.py [-v]      # mã thoát 1 = có lỗi

Quét mọi chuỗi "..." (đã giải \\uXXXX, bỏ chú thích, bỏ dòng log / khoá / tài nguyên) và báo:
  thiếu dấu   - từ tiếng Việt viết không dấu ("duoc", "khong", "chon"...) trong chuỗi CÓ dấu, hoặc chuỗi
                không dấu mà có >= 2 từ như thế ("Item Da duoc chon", "Thong bao");
  tiếng Anh   - từ tiếng Anh còn sót ("level", "server", "item", "skin", "focus"... -> "cấp", "máy chủ", ...);
  thương hiệu - tên game / hãng cũ (NRO, NSO, HSO, Làng Lá, TeaMobi, Ngọc Rồng...) - đường dẫn tài nguyên
                ("/nro/khung.png") không tính vì nằm ở dòng getResource / chuỗi bắt đầu bằng "/";
  chữ hoa có dấu - U+1EA0..U+1EF8 viết HOA (font ảnh J2ME không có sẵn; Font.ghep phải ghép) - không thêm mới.

Ngoại lệ (log / gỡ lỗi / dữ liệu, không tới người chơi) ghi rõ trong CHO_PHEP kèm lý do: so khớp theo NỘI DUNG
chuỗi + tên tệp, không theo số dòng. Kiểm tự thử: các mẫu lỗi đã biết phải bị bắt (TU_THU).
"""
import os
import re
import sys

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
THU_MUC = ["src", "shared"]
# bảng dữ liệu / từ điển (chứa từ cũ hoặc tên riêng có chủ đích)
BO_TEP = {"ChuPokemon.java", "TenMapPokemon.java", "TenBanDoBang.java", "IconBang.java", "KhungNroBang.java"}

# dòng không tới người chơi: log, giám sát, khoá giao thức / RMS, tài nguyên, so chuỗi máy chủ gửi
DONG_BO = re.compile(r"System\.(out|err)|printStackTrace|Cout\.println|GiamSat\.(ghi|loi|tick|nhip|xong|daVe)|"
                     r"getResource|createImage|getAppProperty|getProperty|Class\.forName|\.indexOf\(|startsWith\(|"
                     r"endsWith\(|\.equals\(|RMS\.|RecordStore|setRequestProperty|getHeaderField|\"UTF-8\"")

_LIT = re.compile(r'"((?:[^"\\\n]|\\.)*)"')

# Từ tiếng Việt viết không dấu gần như luôn sai (không phải từ hợp lệ khi bỏ dấu, hoặc rất hiếm).
KHONG_DAU = set("""
duoc khong chua nguoi choi chon dang nhan thong cong tien luong nhap dung hien muon phai cuoc
diem loi gui thuoc chieu chien danh doi dong nhieu nhung nhat truoc sau giua tren duoi xuong
khac cung cach viec biet hoac neu thi rang minh chuyen dau vat pham trang thai tinh nang
cuong hoa thuong ngay gio phut giay huong dan nhiem tham gia ket noi dien thoai mang
da het dat ve den ra vao tat bat mo dua doc xem thoat nhac am thanh hieu ung tu dong bao nap mien
""".split())
# Những từ trong KHONG_DAU vẫn có nghĩa khi viết không dấu: chỉ bắt khi chuỗi CÓ dấu (lẫn lộn) hoặc khi đứng cạnh
# một từ chắc chắn sai - không tính một mình.
MO_HO = set("da ve den ra vao tat bat mo dua doc xem thi rang minh dau sau tren dang cong tien dung hien trang thanh "
            "gia danh tinh ngay mang hoa tham bao".split())

TIENG_ANH = ["level", "levels", "server", "servers", "item", "items", "click", "loading", "error",
             "cancel", "focus", "slogan", "please", "success", "failed", "inventory", "quest", "quests", "reward",
             "upgrade", "account", "password", "username", "login", "logout", "register", "connect", "connecting",
             "disconnect", "retry", "select", "selected", "setting", "settings", "close", "back", "next", "done",
             "bid", "miss", "agent", "ranking", "event", "events", "update", "download", "player", "players"]
# (auto / AUTO, pet, skin, online / offline, tab, Top, OK, Menu, Chat, HP / PP / EXP, Lv, PK, PvP, Boss là thuật ngữ game đang dùng -
# docs/thiet-ke-game.md - không tính)
_ANH = re.compile(r"(?<![\w])(" + "|".join(TIENG_ANH) + r")(?![\w])", re.I)

THUONG_HIEU = re.compile(r"(?<![\w])(nro|nso|hso|tso|kpah|nqsh|teamobi|tea mobi|làng lá|lang la|ngọc rồng|ngoc rong|"
                         r"ninja school|hiệp sĩ online|hiep si online|knight age|kpah)(?![\w])", re.I)


def la_hoa_co_dau(c):
    o = ord(c)
    return 0x1EA0 <= o <= 0x1EF8 and o % 2 == 0


# (tệp, đoạn chuỗi) -> lý do. Đoạn = chuỗi con của nội dung (đã giải \\u).
CHO_PHEP = [
    ("Font.java", "ABCDEFGHIJKLMNOPQRSTUVWXYZÁÀẢ", "bảng ký tự của bigFont (font có sẵn chữ hoa có dấu)"),
    ("ThuongHieu.java", "HUYỀN THOẠI BẢO BỐI", "tên game cho bigFont (font chỉ có chữ hoa, có cả chữ hoa có dấu)"),
    ("CreateCharScr.java", "[^a-zA-Z0-9", "biểu thức lọc tên, không vẽ"),
    # Chữ HOA có dấu đang dùng (Font.taoGhep ghép từ chữ hoa gốc + dấu của chữ thường; PC/APK dùng phông thật).
    # client_pc/test/KiemDauTruong kiểm daVe("THẮNG") nên giữ nguyên dạng.
    ("ManTranDauTruong.java", "THẮNG", "thẻ kết quả đấu trường (KiemDauTruong kiểm nguyên văn)"),
    ("DungDo.java", "TIN NỔI BẬT", "nhãn biểu ngữ (Font.ghep)"),
    ("DungDo.java", "TOÀN MÁY CHỦ", "nhãn biểu ngữ (Font.ghep)"),
    # tên lớp / khoá gỡ lỗi
    ("Canvas.java", "canvas", "nhãn đo thời gian khởi động (GiamSat)"),
    ("Canvas.java", "luong nap", "nhãn đo thời gian khởi động (GiamSat)"),
    ("BieuTuong.java", "BieuTuong vector", "nhãn đo"),
    ("ThoiTietVe.java", "ThoiTietVe.veIcon vector", "nhãn đo"),
    ("TienTe.java", "tien vector", "nhãn đo"),
    ("ChuDe.java", "dau X ve net", "nhãn đo"),
    ("GiaoDien.java", "Poke Ball ve cung", "nhãn đo"),
    ("GiaoDien.java", "menu nhanh", "nhãn đo"),
    ("KhoiDong.java", "", "nhãn đo thời gian khởi động (GiamSat)"),
    ("BoNho.java", "truoc goi", "nhãn đo bộ nhớ"),
    ("GameMidlet.java", "khoi dong", "nhãn đo khởi động"),
    ("Session_ME.java", "", "nhãn lỗi mạng ghi vào GiamSat"),
    ("TuNoiLai.java", "thong bao cach", "nhãn log"),
    ("ManTranDauTruong.java", "bang ", "nhãn tự kiểm bố trí (ve() trả lỗi cho test)"),
    ("ManTranDauTruong.java", "nut toc do", "nhãn tự kiểm bố trí"),
    ("ManTranDauTruong.java", "vung an toan", "nhãn tự kiểm bố trí"),
    ("ManTranDauTruong.java", "vung danh nhau", "nhãn tự kiểm bố trí"),
    ("ManTranDauTruong.java", "buc do doi thu", "nhãn tự kiểm bố trí"),
    ("ManTranDauTruong.java", "sprite ben", "nhãn tự kiểm bố trí"),
    ("TaiDanhSach.java", "NQSH", "khoá tên server cũ trong danh sách tải (lọc bỏ, không vẽ)"),
    ("TaiDanhSach.java", "KPAH", "khoá tên server cũ trong danh sách tải (lọc bỏ, không vẽ)"),
    ("SoundMm.java", "VolumeControl", "tên lớp MMAPI"),
    ("LoginScr.java", "GPRS", "kiểu kết nối J2ME (nhãn thiết bị)"),
    ("LoginScr.java", "Wifi", "kiểu kết nối J2ME (nhãn thiết bị)"),
    ("VirTualKeyB.java", "", "nhãn phím bàn phím ảo (ABC / Left / Right...) - tên phím"),
    ("TField.java", "", "kiểu gõ ABC / Abc"),
    ("FilePack.java", "not found", "ngoại lệ nạp tệp (log)"),
    ("GiamSat.java", "", "nhãn giám sát / log gỡ lỗi (không vẽ)"),
    ("LoginScr.java", "Agent: ", "hộp gỡ lỗi chỉ mở bằng tên đăng nhập đặc biệt (doShowAgent)"),
    ("Mystore.java", "", "lớp chết (không nơi nào tạo Mystore)"),
    # khoá chữ bay: SoBay.bat đổi "MISS" -> "Trượt", "CHI MANG" -> dấu chí mạng... (không vẽ nguyên văn)
    ("*", "MISS", "khoá SoBay (vẽ thành \"Trượt\")"),
    ("AttackResult.java", "", "khoá SoBay (miss / CHI MANG / XUYEN GIAP / BAOKICH / HIEU QUA CAO)"),
    # chữ HOA có dấu đang dùng: J2ME Font.taoGhep ghép chữ hoa gốc + dấu (ChupTrongGame -Dchup.j2me đếm ký tự thiếu),
    # PC / APK vẽ bằng phông thật - giữ, KHÔNG thêm mới
    ("MessageHandler.java", "TOP ĐỘI", "tiêu đề danh sách (Font.ghep)"),
    ("MessageHandler.java", "TOP SỨC MẠNH", "tiêu đề danh sách (Font.ghep)"),
    ("MessageHandler.java", "TOP ĐẠI GIA", "tiêu đề danh sách (Font.ghep)"),
    ("MessageHandler.java", "BẠN BÈ", "tiêu đề danh sách (Font.ghep)"),
    ("PauseMenu.java", "BẠN BÈ", "tiêu đề danh sách (Font.ghep)"),
    ("GameScr.java", "NẠP LƯỢNG", "tiêu đề hộp (Font.ghep)"),
    ("GameScr.java", "NẠP XU", "tiêu đề hộp (Font.ghep)"),
    ("GameScr.java", "NHẬP CHỮ", "tiêu đề hộp (Font.ghep)"),
    ("ListScr.java", "TIN NHẮN", "tiêu đề hộp (Font.ghep)"),
    ("ShopOnline.java", "NHẬP GIÁ BÁN", "tiêu đề hộp (Font.ghep)"),
    ("ManNhanVat.java", "SỨC MẠNH", "nhãn thẻ (Font.ghep)"),
    ("ManNhanVat.java", "Ẩn", "viên trạng thái (Font.ghep)"),
    ("ManTuDo.java", "Ẩn/hiện", "tab Tủ đồ (Font.ghep)"),
    ("NgoaiHinhMenu.java", "Ẩn", "menu ngoại hình (Font.ghep)"),
    ("CreateCharScr.java", "KHÔNG DẤU", "nhấn mạnh luật đặt tên (Font.ghep)"),
    ("KhanPk.java", "BỊ đánh", "nhấn mạnh luật PK (Font.ghep)"),
]

# Còn lỗi nhưng thuộc phần VẬT PHẨM / CỬA HÀNG (việc khác đang giữ): in CANH BAO, không tính lỗi. Sửa xong thì xoá dòng.
CHO_SUA = [
    ("ShopOnline.java", "Level:", "chợ: bộ lọc \"Level:\" -> \"Cấp:\""),
    ("ShopOnline.java", "Theo level", "chợ: sắp xếp -> \"Theo cấp\""),
    ("ShopOnline.java", "Bid", "chợ: tab / nút \"Bid\" -> \"Đấu giá\""),
    ("ShopOnline.java", "bid", "chợ: \"giá bid\" -> \"giá đặt\""),
    ("ItemInInventory.java", "Giá bid", "mô tả vật phẩm chợ: \"Giá bid\" -> \"Giá đặt\""),
]

# mẫu lỗi đã biết: kiểm phải bắt (tự thử)
TU_THU = [("Item Da duoc chon", "thiếu dấu"), ("Tab duoc chon la tab", "thiếu dấu"), ("Xin đánh lên level", "tiếng Anh"),
          ("Hãy chọn server khác", "tiếng Anh"), ("Chào mừng tới NRO", "thương hiệu"), ("TOP ĐỘI", "chữ hoa có dấu"),
          ("Bạn chua có đội", "thiếu dấu"), ("Thong bao", "thiếu dấu")]
TU_THU_SACH = ["Đã chọn", "Bạn chưa có đội", "Cấp 10", "Máy chủ đang bận", "OK", "HP", "Pokémon", "Menu", "Top đội",
               "Mua", "Xem", "Trang sau", "Kanto"]


def bo_chu_thich(s):
    """Xoá // và /* */ (giữ số dòng), không đụng vào chuỗi "..." và ký tự '.'."""
    ra = []
    i, n = 0, len(s)
    while i < n:
        c = s[i]
        if c in "\"'":
            j = i + 1
            while j < n and s[j] != c and s[j] != "\n":
                j += 2 if s[j] == "\\" else 1
            ra.append(s[i:j + 1])
            i = j + 1
            continue
        if s.startswith("//", i):
            j = s.find("\n", i)
            i = n if j < 0 else j
            continue
        if s.startswith("/*", i):
            j = s.find("*/", i + 2)
            j = n if j < 0 else j + 2
            ra.append("\n" * s.count("\n", i, j))
            i = j
            continue
        ra.append(c)
        i += 1
    return "".join(ra)


def giai(t):
    t = re.sub(r"\\u+([0-9a-fA-F]{4})", lambda m: chr(int(m.group(1), 16)), t)
    return re.sub(r"\\(.)", lambda m: {"n": "\n", "t": "\t"}.get(m.group(1), m.group(1)), t)


def giong_chu(t):
    """Chuỗi giống chữ người đọc: có dấu, có khoảng trắng giữa chữ, hoặc viết hoa đầu."""
    if not re.search(r"[A-Za-zÀ-ỹ]{2,}", t):
        return False
    if t.startswith("/") or re.search(r"\.(png|bin|txt|mid|wav|ogg|jar|properties)\b", t):
        return False
    return bool(re.search(r"[^\x00-\x7f]", t)) or bool(re.search(r"[A-Za-z] +[A-Za-z]", t)) or t[:1].isupper()


def soat(t):
    """-> danh sách (loại, chi tiết) lỗi của một chuỗi."""
    loi = []
    co_dau = re.search(r"[À-ỹ]", t) is not None
    tu = re.findall(r"[A-Za-zÀ-ỹĐđ]+", t)
    thuong = [w.lower() for w in tu if re.match(r"^[A-Za-z]+$", w)]
    sai = [w for w in thuong if w in KHONG_DAU]
    chac = [w for w in sai if w not in MO_HO]
    if co_dau and chac or not co_dau and len(sai) >= 2 and chac:
        loi.append(("thiếu dấu", "/".join(sorted(set(sai)))))
    for m in _ANH.finditer(t):
        loi.append(("tiếng Anh", m.group(1)))
    for m in THUONG_HIEU.finditer(t):
        loi.append(("thương hiệu", m.group(1)))
    hoa = [c for c in t if la_hoa_co_dau(c)]
    if hoa:
        loi.append(("chữ hoa có dấu", "".join(hoa)))
    return loi


def cho_phep(ten, t, bang=None):
    for tep, doan, _ in (CHO_PHEP if bang is None else bang):
        if tep in (ten, "*") and doan in t:
            return True
    return False


def quet(verbose=False):
    loi, canh, so = [], [], 0
    for g in THU_MUC:
        for dp, _, fs in os.walk(os.path.join(GOC, g)):
            for ten in sorted(fs):
                if not ten.endswith(".java") or ten in BO_TEP:
                    continue
                p = os.path.join(dp, ten)
                s = bo_chu_thich(open(p, encoding="utf-8", errors="replace").read())
                for so_dong, dong in enumerate(s.split("\n"), 1):
                    if DONG_BO.search(dong):
                        continue
                    for m in _LIT.finditer(dong):
                        t = giai(m.group(1))
                        if not giong_chu(t):
                            continue
                        so += 1
                        for loai, ct in soat(t):
                            if cho_phep(ten, t):
                                if verbose:
                                    print("bo qua %s:%d [%s] %r" % (ten, so_dong, loai, t))
                                continue
                            if cho_phep(ten, t, CHO_SUA):
                                canh.append("%s:%d [%s: %s] %r" % (os.path.relpath(p, GOC), so_dong, loai, ct, t))
                                continue
                            loi.append("%s:%d [%s: %s] %r" % (os.path.relpath(p, GOC), so_dong, loai, ct, t))
    return loi, canh, so


def main():
    hong = 0
    for t, loai in TU_THU:
        if loai not in [x[0] for x in soat(t)]:
            print("LOI: tu thu khong bat duoc [%s] %r" % (loai, t))
            hong += 1
    for t in TU_THU_SACH:
        if soat(t):
            print("LOI: tu thu bat oan %r: %r" % (t, soat(t)))
            hong += 1
    loi, canh, so = quet("-v" in sys.argv)
    for l in canh:
        print("CANH BAO (vat pham, cho sua): " + l)
    for l in loi:
        print("LOI: " + l)
    print("kiem_chu_hien_thi: %d chuoi, %d loi, %d canh bao (vat pham), tu thu hong %d" % (so, len(loi), len(canh), hong))
    sys.exit(1 if loi or hong else 0)


if __name__ == "__main__":
    main()
