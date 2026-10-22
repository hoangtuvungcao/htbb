"""Tiện ích ảnh pixel cho bộ sinh đồ / trang phục (tools/do_trang_phuc). Không cần thư viện ngoài.

Ảnh = (rộng, cao, list hàng; mỗi điểm là int ARGB 0xAARRGGBB, 0 = trong suốt).
Hình vẽ tay bằng LƯỚI KÝ TỰ: mỗi ký tự là một vai màu (role) tra trong bảng màu truyền vào.
Viền 1 px tự sinh (màu viền của bảng) quanh mọi điểm đặc theo 4 hướng - giống tools/pokemon/SinhPhuKien.java
và icon PMD (viền tối, sáng từ trên-trái).
"""
import struct
import zlib


def anh_moi(w, h, nen=0):
    return [w, h, [[nen] * w for _ in range(h)]]


def ghi_png(duong, anh):
    w, h, px = anh
    tho = bytearray()
    for y in range(h):
        tho.append(0)
        for x in range(w):
            c = px[y][x]
            a = (c >> 24) & 0xFF
            tho += bytes(((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, a))

    def khoi(loai, du):
        return struct.pack(">I", len(du)) + loai + du + struct.pack(">I", zlib.crc32(loai + du) & 0xFFFFFFFF)

    du = b"\x89PNG\r\n\x1a\n" + khoi(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    du += khoi(b"IDAT", zlib.compress(bytes(tho), 9)) + khoi(b"IEND", b"")
    with open(duong, "wb") as f:
        f.write(du)
    return du


def doc_png(duong):
    """Đọc PNG RGBA/RGB 8 bit không xen kẽ (đủ cho ảnh do chính bộ sinh ghi + icon PMD)."""
    with open(duong, "rb") as f:
        du = f.read()
    assert du[:8] == b"\x89PNG\r\n\x1a\n", duong
    i = 8
    w = h = 0
    loai_mau = 0
    idat = b""
    bang = []
    trong = None
    while i < len(du):
        n = struct.unpack(">I", du[i:i + 4])[0]
        t = du[i + 4:i + 8]
        d = du[i + 8:i + 8 + n]
        if t == b"IHDR":
            w, h, sau, loai_mau = struct.unpack(">IIBB", d[:10])
            assert sau == 8, "chỉ đọc 8 bit"
        elif t == b"PLTE":
            bang = [tuple(d[k:k + 3]) for k in range(0, len(d), 3)]
        elif t == b"tRNS":
            trong = d
        elif t == b"IDAT":
            idat += d
        i += 12 + n
    tho = zlib.decompress(idat)
    bpp = {6: 4, 2: 3, 3: 1, 0: 1, 4: 2}[loai_mau]
    hang = []
    truoc = bytearray(w * bpp)
    p = 0
    for _ in range(h):
        loc = tho[p]
        dong = bytearray(tho[p + 1:p + 1 + w * bpp])
        p += 1 + w * bpp
        for x in range(len(dong)):
            a = dong[x - bpp] if x >= bpp else 0
            b = truoc[x]
            c = truoc[x - bpp] if x >= bpp else 0
            if loc == 1:
                dong[x] = (dong[x] + a) & 0xFF
            elif loc == 2:
                dong[x] = (dong[x] + b) & 0xFF
            elif loc == 3:
                dong[x] = (dong[x] + (a + b) // 2) & 0xFF
            elif loc == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                du_doan = a if pa <= pb and pa <= pc else (b if pb <= pc else c)
                dong[x] = (dong[x] + du_doan) & 0xFF
        hang.append(dong)
        truoc = dong
    px = []
    for dong in hang:
        r = []
        for x in range(w):
            if loai_mau == 6:
                R, G, B, A = dong[x * 4:x * 4 + 4]
            elif loai_mau == 2:
                R, G, B = dong[x * 3:x * 3 + 3]
                A = 255
            elif loai_mau == 3:
                k = dong[x]
                R, G, B = bang[k]
                A = trong[k] if trong is not None and k < len(trong) else 255
            elif loai_mau == 4:
                R = G = B = dong[x * 2]
                A = dong[x * 2 + 1]
            else:
                R = G = B = dong[x]
                A = 255
            r.append((A << 24 | R << 16 | G << 8 | B) if A else 0)
        px.append(r)
    return [w, h, px]


# ---------------------------------------------------------------- màu

def rgb(c):
    return (c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF


def tu_rgb(r, g, b, a=255):
    r, g, b = (max(0, min(255, int(round(v)))) for v in (r, g, b))
    return a << 24 | r << 16 | g << 8 | b


def tron(c1, c2, t):
    """Trộn hai màu rgb24 (t = phần của c2)."""
    a, b = rgb(c1), rgb(c2)
    return tu_rgb(*(a[i] + (b[i] - a[i]) * t for i in range(3))) & 0xFFFFFF


def sang(c, t):
    return tron(c, 0xFFFFFF, t)


def toi(c, t):
    return tron(c, 0x000000, t)


def ba_sac(goc):
    """(sáng, gốc, tối) từ một màu gốc - bóng kiểu PMD (tối ngả về màu, không xám)."""
    return sang(goc, 0.42), goc, toi(goc, 0.38)


def mau_vien(goc):
    return toi(goc, 0.72)


# ---------------------------------------------------------------- lưới

def ve_luoi(luoi, bang, vien=None, vien_trong=True):
    """Lưới ký tự -> ảnh. bang: ký tự -> rgb24 (None/'.'/' ' = trong suốt).

    vien: rgb24 của viền tự sinh (None = không viền). Viền làm ảnh rộng thêm 1 px mỗi phía.
    Ký tự 'O' trong lưới luôn là viền vẽ tay (lấy bang['O'] hoặc vien).
    """
    dong = [s for s in luoi.strip("\n").split("\n")] if isinstance(luoi, str) else list(luoi)
    h0 = len(dong)
    w0 = max(len(s) for s in dong)
    le = 1 if vien is not None else 0
    w, h = w0 + 2 * le, h0 + 2 * le
    anh = anh_moi(w, h)
    dac = [[False] * w for _ in range(h)]
    for y, s in enumerate(dong):
        for x, ch in enumerate(s):
            if ch in ". ":
                continue
            if ch == "O":
                c = bang.get("O", vien)
            else:
                if ch not in bang:
                    raise KeyError("ky tu la '%s' trong luoi" % ch)
                c = bang[ch]
            if c is None:
                continue
            anh[2][y + le][x + le] = 0xFF000000 | c
            dac[y + le][x + le] = True
    if vien is not None and vien_trong:
        for y in range(h):
            for x in range(w):
                if dac[y][x]:
                    continue
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    X, Y = x + dx, y + dy
                    if 0 <= X < w and 0 <= Y < h and dac[Y][X]:
                        anh[2][y][x] = 0xFF000000 | vien
                        break
    return anh


def dan(dich, nguon, x0, y0):
    """Dán ảnh nguồn lên đích (điểm trong suốt bỏ qua)."""
    for y in range(nguon[1]):
        for x in range(nguon[0]):
            c = nguon[2][y][x]
            if c >> 24 == 0:
                continue
            X, Y = x0 + x, y0 + y
            if 0 <= X < dich[0] and 0 <= Y < dich[1]:
                dich[2][Y][X] = c
    return dich


def lat_ngang(anh):
    w, h, px = anh
    return [w, h, [list(reversed(r)) for r in px]]


def phong(anh, k):
    w, h, px = anh
    return [w * k, h * k, [[px[y // k][x // k] for x in range(w * k)] for y in range(h * k)]]


def hop_bao(anh):
    """(x0, y0, x1, y1) điểm đặc; None nếu rỗng."""
    w, h, px = anh
    xs = [x for y in range(h) for x in range(w) if px[y][x] >> 24]
    ys = [y for y in range(h) for x in range(w) if px[y][x] >> 24]
    if not xs:
        return None
    return min(xs), min(ys), max(xs), max(ys)


def cat(anh, x0, y0, x1, y1):
    return [x1 - x0 + 1, y1 - y0 + 1, [anh[2][y][x0:x1 + 1] for y in range(y0, y1 + 1)]]


def to_nen(anh, c):
    w, h, px = anh
    return [w, h, [[(p if p >> 24 else (0xFF000000 | c)) for p in r] for r in px]]


def so_mau(anh):
    return len({p for r in anh[2] for p in r if p >> 24})


def khac_nhau(a, b):
    """Số điểm khác nhau giữa hai ảnh cùng cỡ."""
    if a[0] != b[0] or a[1] != b[1]:
        return max(a[0] * a[1], b[0] * b[1])
    return sum(1 for y in range(a[1]) for x in range(a[0]) if a[2][y][x] != b[2][y][x])


# ---------------------------------------------------------------- chữ số 3x5 cho ảnh xem trước

SO_3X5 = {
    "0": ["###", "#.#", "#.#", "#.#", "###"], "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": ["###", "..#", "###", "#..", "###"], "3": ["###", "..#", "###", "..#", "###"],
    "4": ["#.#", "#.#", "###", "..#", "..#"], "5": ["###", "#..", "###", "..#", "###"],
    "6": ["###", "#..", "###", "#.#", "###"], "7": ["###", "..#", ".#.", ".#.", ".#."],
    "8": ["###", "#.#", "###", "#.#", "###"], "9": ["###", "#.#", "###", "..#", "###"],
    "+": ["...", ".#.", "###", ".#.", "..."], "-": ["...", "...", "###", "...", "..."],
}


def viet_so(anh, chu, x, y, mau=0xFFFFFF, k=1):
    for ch in chu:
        m = SO_3X5.get(ch)
        if m:
            for yy in range(5):
                for xx in range(3):
                    if m[yy][xx] == "#":
                        for a in range(k):
                            for b in range(k):
                                X, Y = x + xx * k + a, y + yy * k + b
                                if 0 <= X < anh[0] and 0 <= Y < anh[1]:
                                    anh[2][Y][X] = 0xFF000000 | mau
        x += 4 * k
    return x
