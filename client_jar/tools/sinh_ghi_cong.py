"""Sinh tệp ghi công người chơi đọc được (GHI_CONG.txt) cho cả 3 client từ ghi công gốc của HTBB.

    python3 -I client_jar/tools/sinh_ghi_cong.py <thu muc poke cua HTBB Unity> [ra1 ra2 ...]
    vd: python3 -I client_jar/tools/sinh_ghi_cong.py ../HTBB/ClientUnity/Assets/Resources/poke \
            client_jar/res/GHI_CONG.txt client_pc_ios/Assets/Resources/ghicong/GHI_CONG.txt

Đọc (trong thư mục poke): GHI_CONG.txt (PMDCollab SpriteCollab: người vẽ theo từng loài, sinh bởi DongGoiSprite),
GHI_CONG_MAP.txt (bộ tile bản đồ), GHI_CONG_VAT_THE.txt (hình vật thể). Tên người vẽ: SpriteCollab ghi bằng mã
Discord (<@!123...>); đổi sang tên hiển thị bằng client_jar/tools/pmdcollab_credit_names.txt (bản sao của
https://github.com/PMDCollab/SpriteCollab/blob/master/credit_names.txt). Âm thanh: client_jar/res/LICENSE-audio.txt.

CC BY-NC 4.0 bắt buộc ghi công trong MỌI bản phân phối: tệp ra được đóng vào jar (client_jar/res -> /GHI_CONG.txt),
APK (client_jar/res là assets) và bản PC (Resources/ghicong + chép cạnh .exe). Màn "Ghi công" trong game đọc tệp này.

Định dạng ra (UTF-8): dòng "# ..." = tiêu đề mục, dòng trống = cách đoạn, còn lại = chữ thường.
Chỉ dùng chữ font game vẽ được (Latin + tiếng Việt); ký tự khác bị bỏ.
"""
import os
import re
import sys
import unicodedata

GOC = os.path.dirname(os.path.abspath(__file__))
TEN_DISCORD = os.path.join(GOC, "pmdcollab_credit_names.txt")
AM_THANH = os.path.join(GOC, "..", "res", "LICENSE-audio.txt")

# chữ tiếng Việt font game có; ngoài ra chỉ ASCII in được
VIET = set("àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ")
VIET |= {c.upper() for c in VIET}


def sach(s):
    # "~" có trong bảng chữ font jar nhưng ảnh font thiếu hàng đó (vẽ là ném lỗi) -> "-"
    s = s.replace("　", " ").replace("〜", "-").replace("～", "-").replace("~", "-")
    ra = []
    for c in s:
        if 32 <= ord(c) < 127 or c in VIET:
            ra.append(c)
        else:
            # bỏ dấu ngoại (ñ -> n, é giữ), ký tự không phải chữ Latin thì bỏ
            d = unicodedata.normalize("NFKD", c)
            d = "".join(x for x in d if 32 <= ord(x) < 127)
            ra.append(d)
    return re.sub(r"\s+", " ", "".join(ra)).strip()


def doc_ten():
    ten = {}
    if not os.path.exists(TEN_DISCORD):
        return ten
    with open(TEN_DISCORD, encoding="utf-8") as f:
        for i, dong in enumerate(f):
            p = dong.rstrip("\n").split("\t")
            if i == 0 or len(p) < 2:
                continue
            ten[p[1].strip()] = sach(p[0])
    return ten


def ten_nguoi_ve(ma, ten):
    t = ten.get(ma, "")
    if t:
        return t
    if ma == "CHUNSOFT":
        return "CHUNSOFT"
    m = re.match(r"<@!?(\d+)>", ma)
    if m:
        return "Discord " + m.group(1)
    return sach(ma) or "?"


def doc_sprite(duong, ten):
    """[(dex, [người vẽ theo thứ tự, không trùng])], phần hiệu ứng chiêu (dòng sau HET loài)."""
    loai = []
    hien = None
    duoi = []
    with open(duong, encoding="utf-8") as f:
        dong = [d.rstrip("\n") for d in f]
    i = 0
    while i < len(dong):
        d = dong[i]
        m = re.match(r"^== (\d{4})", d)
        if m:
            hien = (m.group(1), [])
            loai.append(hien)
        elif hien is not None and "\t" in d:
            p = d.split("\t")
            if len(p) >= 2:
                n = ten_nguoi_ve(p[1].strip(), ten)
                if n not in hien[1]:
                    hien[1].append(n)
        elif d.startswith("Hieu ung chieu"):
            duoi = dong[i:]
            break
        i += 1
    return loai, duoi


def main():
    if len(sys.argv) < 3:
        raise SystemExit(__doc__)
    poke = sys.argv[1]
    ten = doc_ten()
    loai, chieu = doc_sprite(os.path.join(poke, "GHI_CONG.txt"), ten)
    tat_ca = {}
    for _, ds in loai:
        for n in ds:
            tat_ca[n] = tat_ca.get(n, 0) + 1
    nguoi = sorted(tat_ca, key=lambda n: (n != "CHUNSOFT", n.lower()))

    r = []
    a = r.append
    a("# Huyền Thoại Bảo Bối - Ghi công")
    a("Game fan phi thương mại. Không liên kết, không được bảo trợ bởi Nintendo, Creatures, GAME FREAK hay The Pokémon Company.")
    a("Pokémon (c) Nintendo / Creatures / GAME FREAK / The Pokémon Company.")
    a("")
    a("# Hình Pokémon")
    a("PMDCollab SpriteCollab - https://github.com/PMDCollab/SpriteCollab")
    a("Giấy phép CC BY-NC 4.0 (phải ghi công, không dùng cho mục đích thương mại): https://creativecommons.org/licenses/by-nc/4.0/")
    a("Hình đã được thu nhỏ / cắt khung / đổi định dạng để chạy trên điện thoại.")
    a("Nhân vật dựa trên Pokémon Mystery Dungeon (Chunsoft / Spike Chunsoft).")
    a("Chân dung Pokémon và biểu tượng ứng dụng (chân dung Pikachu) cũng lấy từ SpriteCollab.")
    a("")
    a("# Người vẽ (%d)" % len(nguoi))
    a(", ".join(nguoi))
    a("")
    a("# Người vẽ theo từng loài")
    for dex, ds in loai:
        a("#%s: %s" % (dex, ", ".join(ds) if ds else "?"))
    a("")
    a("# Hiệu ứng chiêu và vật thể")
    a("PMDCollab RawAsset - https://github.com/PMDCollab/RawAsset (thư mục Particle, Object, Item).")
    a("Trích từ Pokémon Mystery Dungeon gốc (Chunsoft / Nintendo), chỉ dùng phi thương mại.")
    if chieu:
        for d in chieu:
            if d.startswith("File goc:"):
                a("Hiệu ứng: " + sach(d[len("File goc:"):]))
    vt = os.path.join(poke, "GHI_CONG_VAT_THE.txt")
    if os.path.exists(vt):
        with open(vt, encoding="utf-8") as f:
            for d in f:
                m = re.search(r"File goc: (.*)", d)
                if m:
                    a("Vật thể: " + sach(m.group(1)))
    a("")
    a("# Bản đồ")
    a("Ninja Adventure - Asset Pack, tác giả Pixel-boy và AAA - CC0 1.0")
    a("https://pixel-boy.itch.io/ninja-adventure-asset-pack")
    a("Mép chuyển tiếp, nền / vách hang, biển hiệu: vẽ bằng code trong dự án HTBB (CC0).")
    a("")
    a("# Âm thanh")
    a("Kenney (www.kenney.nl) - CC0 1.0: Interface Sounds, Impact Sounds, RPG Audio, Digital Audio, Music Jingles.")
    a("Nhạc nền chiptune gốc, tự soạn cho dự án (CC0) - không dùng nhạc Pokémon.")
    a("")
    a("# Giao diện và mã nguồn")
    a("Nhóm phát triển Huyền Thoại Bảo Bối (HTBB). Chữ tiếng Việt, giao diện kiểu Pokémon: vẽ bằng code.")
    a("Logo và key art màn tải / đăng nhập: dựng bằng code (client_jar/tools/tao_logo.py, tao_banner.py) từ sprite PMDCollab (Charmander, Bulbasaur, Eevee, Squirtle, Pichu, Lapras, Butterfree, Mareep, Rayquaza - ghi công ở trên) và tile Ninja Adventure. Chữ logo: font Baloo 2 (Ek Type) - SIL Open Font License 1.1.")
    a("")
    a("Cảm ơn các hoạ sĩ PMDCollab và cộng đồng đã chia sẻ tác phẩm!")
    noi_dung = "\n".join(r) + "\n"
    for ra in sys.argv[2:]:
        os.makedirs(os.path.dirname(os.path.abspath(ra)), exist_ok=True)
        with open(ra, "w", encoding="utf-8", newline="\n") as f:
            f.write(noi_dung)
        print("OK %s (%d loài, %d người vẽ, %d byte)" % (ra, len(loai), len(nguoi), len(noi_dung.encode("utf-8"))))


main()
