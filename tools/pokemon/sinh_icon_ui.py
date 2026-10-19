"""Sinh BỘ ICON GIAO DIỆN PNG (0.9.5) cho client HTBB - ảnh hoàn chỉnh kiểu game (khối có độ sáng, ánh, viền tối
mềm, bóng đổ), render OFFLINE từ thiết kế vector của game.pokemon.BieuTuong (một nguồn hình dạng duy nhất).

    APP_IMAGE=0 sh client_pc/build.sh            # cần lớp đã biên dịch (đọc BieuTuong.HINH)
    python3 tools/pokemon/sinh_icon_ui.py [thư mục lớp, mặc định client_pc/out/build/cls]

Ra (client_jar/res/ui/, đưa vào jar / APK / PC):
  ui_96.png    tấm ghép icon 96x96 / ô, 8 cột, ô i = BieuTuong id i (ô 0 = Poké Ball)
  nut_96.png   da nút 9-slice 96x96: cột 0 thường, 1 nhấn, 2 tắt, 3 tròn thường, 4 tròn nhấn

Ảnh là ĐỘ SÁNG + ALPHA (xám): client TÔ MÀU lúc chạy (game.pokemon.AnhUI): độ sáng 0,55 = đúng màu yêu cầu, sáng
hơn -> pha trắng, tối hơn -> pha màu viền đậm. Nhờ vậy một ảnh dùng được cho ô menu (icon màu trên ô nhạt), nút HUD
(icon trắng trên đĩa màu), ô trang bị (bóng mờ xám)... Lỗ (màu NEN của thiết kế) = trong suốt.
Giấy phép: tự vẽ cho dự án, dùng tự do (CC0) - docs/giay-phep.md.
"""
import json
import os
import subprocess
import sys
import tempfile

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

GOC = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
RA = os.path.join(GOC, "client_jar", "res", "ui")
O = 96          # cạnh ô ảnh ra
SS = 4          # siêu mẫu (vẽ 384 rồi thu)
COT = 8
# giá trị độ sáng theo vai màu của thiết kế
V_CHINH, V_TOI, V_NHAT, V_TRANG = 0.55, 0.36, 0.76, 0.98
V_VIEN = 0.10

DUMP = r'''
public class DumpHinh {
    public static void main(String[] a) throws Exception {
        java.lang.reflect.Field f = Class.forName("game.pokemon.BieuTuong").getDeclaredField("HINH");
        f.setAccessible(true);
        float[][] h = (float[][]) f.get(null);
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < h.length; i++) {
            if (i > 0) b.append(",");
            if (h[i] == null) { b.append("null"); continue; }
            b.append("[");
            for (int k = 0; k < h[i].length; k++) { if (k > 0) b.append(","); b.append(h[i][k]); }
            b.append("]");
        }
        System.out.println(b.append("]"));
    }
}
'''


def doc_hinh(cls):
    d = tempfile.mkdtemp(prefix="sinh_icon_ui_")
    open(os.path.join(d, "DumpHinh.java"), "w").write(DUMP)
    subprocess.check_call(["javac", "-nowarn", "-cp", cls, "-d", d, os.path.join(d, "DumpHinh.java")])
    out = subprocess.check_output(["java", "-cp", cls + os.pathsep + d, "DumpHinh"])
    return json.loads(out.decode())


class Ve:
    """Vẽ lệnh BieuTuong lên mặt nạ độ sáng / alpha (float, siêu mẫu)."""

    def __init__(self, n, le, co):
        self.n = n
        self.le = le          # lề (px siêu mẫu) quanh hộp 100
        self.k = co / 100.0
        self.L = np.zeros((n, n), np.float32)
        self.A = np.zeros((n, n), np.float32)
        self.v = V_CHINH
        self.lo = False

    def p(self, x, y):
        return (self.le + x * self.k, self.le + y * self.k)

    def mat(self):
        return Image.new("L", (self.n, self.n), 0)

    def ap(self, m):
        a = np.asarray(m, np.float32) / 255.0
        if self.lo:
            self.A *= (1 - a)
        else:
            self.L = self.L * (1 - a) + self.v * a
            self.A = np.maximum(self.A, a)

    def tron(self, cx, cy, r):
        m = self.mat()
        x0, y0 = self.p(cx - r, cy - r)
        x1, y1 = self.p(cx + r, cy + r)
        ImageDraw.Draw(m).ellipse([x0, y0, x1, y1], fill=255)
        self.ap(m)

    def vong(self, cx, cy, r, w):
        m = self.mat()
        x0, y0 = self.p(cx - r, cy - r)
        x1, y1 = self.p(cx + r, cy + r)
        ImageDraw.Draw(m).ellipse([x0, y0, x1, y1], outline=255, width=max(1, int(w * self.k)))
        self.ap(m)

    def hcn(self, x, y, w, h, r):
        m = self.mat()
        x0, y0 = self.p(x, y)
        x1, y1 = self.p(x + w, y + h)
        ImageDraw.Draw(m).rounded_rectangle([x0, y0, x1, y1], radius=max(0, r * self.k), fill=255)
        self.ap(m)

    def net(self, pts, w):
        m = self.mat()
        dr = ImageDraw.Draw(m)
        ww = max(1, int(w * self.k))
        q = [self.p(x, y) for x, y in pts]
        dr.line(q, fill=255, width=ww, joint="curve")
        r = ww / 2.0
        for x, y in q:
            dr.ellipse([x - r, y - r, x + r, y + r], fill=255)
        self.ap(m)

    def da_giac(self, pts):
        m = self.mat()
        ImageDraw.Draw(m).polygon([self.p(x, y) for x, y in pts], fill=255)
        self.ap(m)

    def cung(self, cx, cy, r, a0, ext, w):
        m = self.mat()
        x0, y0 = self.p(cx - r, cy - r)
        x1, y1 = self.p(cx + r, cy + r)
        # Java2D / J2ME: góc ngược chiều kim đồng hồ từ 3 giờ; PIL: cùng chiều kim đồng hồ
        s, e = -(a0 + ext), -a0
        dr = ImageDraw.Draw(m)
        if w <= 0:
            dr.pieslice([x0, y0, x1, y1], s, e, fill=255)
        else:
            dr.arc([x0, y0, x1, y1], s, e, fill=255, width=max(1, int(w * self.k)))
        self.ap(m)


def chay(ve, h):
    i = 0
    while i < len(h):
        lenh = int(h[i])
        i += 1
        if lenh == 5:
            m = int(h[i])
            i += 1
            ve.lo = m == 4
            ve.v = {0: V_CHINH, 1: V_TRANG, 2: V_TOI, 3: V_NHAT}.get(m, V_CHINH)
        elif lenh == 0:
            ve.tron(h[i], h[i + 1], h[i + 2])
            i += 3
        elif lenh == 1:
            ve.hcn(h[i], h[i + 1], h[i + 2], h[i + 3], h[i + 4])
            i += 5
        elif lenh == 2:
            ve.net([(h[i], h[i + 1]), (h[i + 2], h[i + 3])], h[i + 4])
            i += 5
        elif lenh == 4:
            ve.vong(h[i], h[i + 1], h[i + 2], h[i + 3])
            i += 4
        elif lenh == 6:
            ve.cung(h[i], h[i + 1], h[i + 2], h[i + 3], h[i + 4], h[i + 5])
            i += 6
        elif lenh == 3:
            n = int(h[i])
            i += 1
            ve.da_giac([(h[i + 2 * q], h[i + 2 * q + 1]) for q in range(n)])
            i += 2 * n
        elif lenh == 7:
            n = int(h[i])
            w = h[i + 1]
            i += 2
            ve.net([(h[i + 2 * q], h[i + 2 * q + 1]) for q in range(n)], w)
            i += 2 * n
        else:
            break


def pokeball(ve):
    """Poké Ball (ô 0): nửa trên đậm (tô màu đỏ lúc chạy), nửa dưới trắng, đai + nút giữa."""
    ve.v = V_CHINH
    ve.tron(50, 50, 44)
    m = ve.mat()
    x0, y0 = ve.p(6, 6)
    x1, y1 = ve.p(94, 94)
    ImageDraw.Draw(m).pieslice([x0, y0, x1, y1], 0, 180, fill=255)
    ve.v = V_TRANG
    ve.ap(m)
    ve.v = V_VIEN + 0.02
    ve.hcn(6, 45, 88, 10, 0)
    ve.tron(50, 50, 17)
    ve.v = V_TRANG
    ve.tron(50, 50, 11)
    ve.v = 0.86
    ve.tron(50, 50, 6)


def lam_dep(L, A, n):
    """Khối + ánh + viền mềm + bóng đổ (mảng float n x n) -> (L, A)."""
    yy = np.linspace(0, 1, n, dtype=np.float32)[:, None] * np.ones((1, n), np.float32)
    xx = np.linspace(0, 1, n, dtype=np.float32)[None, :] * np.ones((n, 1), np.float32)
    trong = A > 0.01
    # khối: sáng trên, tối dưới (giữ màu "chính" ở giữa), chỉ áp vùng có màu (không áp phần trắng / lỗ)
    khoi = 1.0 + 0.28 * (0.45 - yy)
    L2 = np.where(L < 0.9, L * khoi, L)
    # gờ: mép trong tối nhẹ (mặt nạ bị làm mờ -> gần mép < 1)
    m = Image.fromarray((A * 255).astype(np.uint8))
    mo = np.asarray(m.filter(ImageFilter.GaussianBlur(n * 0.018)), np.float32) / 255.0
    L2 = L2 * (0.86 + 0.14 * np.clip((mo - 0.5) * 2, 0, 1))
    # ánh: vệt sáng góc trên trái, mờ dần
    anh = np.clip(1.0 - np.sqrt(((xx - 0.36) / 0.42) ** 2 + ((yy - 0.24) / 0.26) ** 2), 0, 1) ** 1.6
    L2 = np.where(trong, L2 + (1 - L2) * 0.45 * anh * (L2 < 0.9), L2)
    # viền tối mềm bên ngoài
    md = m.filter(ImageFilter.MaxFilter(int(n * 0.045) | 1)).filter(ImageFilter.GaussianBlur(n * 0.006))
    Av = np.asarray(md, np.float32) / 255.0
    # bóng đổ: viền dời xuống, mờ
    bo = md.transform(md.size, Image.AFFINE, (1, 0, 0, 0, 1, -int(n * 0.035))).filter(ImageFilter.GaussianBlur(n * 0.02))
    Ab = np.asarray(bo, np.float32) / 255.0 * 0.38
    # ghép: bóng (đen) < viền (tối) < thân
    Lr = np.zeros_like(L2)
    Ar = Ab.copy()
    Lr = Lr * (1 - Av) + V_VIEN * Av
    Ar = Av + Ar * (1 - Av)
    Lr = Lr * (1 - A) + np.clip(L2, 0, 1) * A
    Ar = A + Ar * (1 - A)
    return Lr, Ar


def thu(L, A, o):
    """Siêu mẫu -> o x o, tiền nhân alpha cho thu nhỏ đúng."""
    pm = np.dstack([L * A, A])
    im = Image.fromarray((np.clip(pm, 0, 1) * 255).astype(np.uint8), "LA")
    r = np.asarray(im.resize((o, o), Image.LANCZOS), np.float32) / 255.0
    a = r[:, :, 1]
    l = np.where(a > 0.003, r[:, :, 0] / np.maximum(a, 0.003), 0)
    return np.dstack([np.clip(l, 0, 1), a])


def icon(h, la_bong=False):
    n = O * SS
    ve = Ve(n, n * 0.05, n * 0.90)
    if la_bong:
        pokeball(ve)
    else:
        chay(ve, h)
    L, A = lam_dep(ve.L, ve.A, n)
    return thu(L, A, O)


def da_nut(kieu):
    """Da nút 9-slice (96 x 96): 0 thường, 1 nhấn, 2 tắt, 3 tròn thường, 4 tròn nhấn."""
    n = O * SS
    yy = np.linspace(0, 1, n, dtype=np.float32)[:, None] * np.ones((1, n), np.float32)
    m = Image.new("L", (n, n), 0)
    pad = n * 0.06
    tron = kieu >= 3
    dr = ImageDraw.Draw(m)
    box = [pad, pad, n - pad, n - pad * (1.0 if kieu in (1, 4) else 1.5)]
    if tron:
        dr.ellipse(box, fill=255)
    else:
        dr.rounded_rectangle(box, radius=n * 0.25, fill=255)
    A = np.asarray(m, np.float32) / 255.0
    nhan = kieu in (1, 4)
    if kieu == 2:
        L = np.full((n, n), 0.58, np.float32)
    elif nhan:
        L = 0.47 - 0.05 * (0.5 - yy)            # nhấn: tối hơn, sáng dưới (lõm)
        L = L - 0.12 * np.clip(1 - yy / 0.25, 0, 1) ** 2   # bóng trong mép trên
    else:
        L = 0.565 + 0.10 * (0.5 - yy)           # thường: giữa = đúng màu, sáng trên, đậm dưới (lồi)
        L = L + 0.12 * np.clip(1 - np.abs(yy - 0.17) / 0.12, 0, 1)   # vệt ánh trên
    mo = np.asarray(m.filter(ImageFilter.GaussianBlur(n * 0.012)), np.float32) / 255.0
    L = L * (0.88 + 0.12 * np.clip((mo - 0.5) * 2, 0, 1))
    md = m.filter(ImageFilter.MaxFilter(int(n * 0.03) | 1))
    Av = np.asarray(md, np.float32) / 255.0
    off = int(n * (0.012 if nhan else 0.04))
    bo = md.transform(md.size, Image.AFFINE, (1, 0, 0, 0, 1, -off)).filter(ImageFilter.GaussianBlur(n * 0.02))
    Ab = np.asarray(bo, np.float32) / 255.0 * (0.25 if kieu == 2 else 0.4)
    Lr = V_VIEN * Av
    Ar = Av + Ab * (1 - Av)
    Lr = Lr * (1 - A) + np.clip(L, 0, 1) * A
    Ar = A + Ar * (1 - A)
    return thu(Lr, Ar, O)


def luu_tam(ds, cot, ten):
    hang = (len(ds) + cot - 1) // cot
    tam = np.zeros((hang * O, cot * O, 2), np.float32)
    for i, o in enumerate(ds):
        if o is None:
            continue
        r, c = divmod(i, cot)
        tam[r * O:(r + 1) * O, c * O:(c + 1) * O] = o
    im = Image.fromarray((np.clip(tam, 0, 1) * 255 + 0.5).astype(np.uint8), "LA").convert("RGBA")
    # RGBA rồi bảng 256 màu (sai số trung bình ~1/255): Java ImageIO coi PNG xám là xám TUYẾN TÍNH (sáng lên khi đọc)
    im = im.quantize(256, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE)
    p = os.path.join(RA, ten)
    im.save(p, optimize=True)
    print("%s: %d o %dx%d, %d byte" % (ten, len(ds), O, O, os.path.getsize(p)))


def main():
    cls = sys.argv[1] if len(sys.argv) > 1 else os.path.join(GOC, "client_pc", "out", "build", "cls")
    hinh = doc_hinh(cls)
    os.makedirs(RA, exist_ok=True)
    ds = []
    for i, h in enumerate(hinh):
        ds.append(icon(None, True) if i == 0 else (icon(h) if h else None))
    luu_tam(ds, COT, "ui_96.png")
    luu_tam([da_nut(k) for k in range(5)], 5, "nut_96.png")


main()
