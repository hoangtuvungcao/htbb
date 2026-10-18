#!/usr/bin/env python3
"""Nén lại PNG trong res/pk (và res_nhe/pk) KHÔNG MẤT ĐIỂM ẢNH nào - cần Pillow.

    python3 client_jar/tools/toi_uu_png.py client_jar/res/pk [client_jar/res_nhe/pk ...]

- Ảnh RGBA mà alpha chỉ 0/255 và <= 255 màu (sprite PMD gần như đều thế) -> PNG bảng màu (màu 3) có
  tRNS, độ sâu bit nhỏ nhất đủ chứa (1/2/4/8 bit, MIDP 2.0 đọc được; jar đã dùng sẵn 4 bit).
- Ảnh bảng màu sẵn: bỏ màu thừa, giảm độ sâu bit, nén zlib mức cao nhất, bỏ chunk phụ (text, gAMA...).
- Chỉ ghi khi file mới NHỎ HƠN và giải mã lại GIỐNG HỆT từng điểm (alpha chuẩn 0/255) so với ảnh gốc.
DongGoiPokeJ2me có thể ghi lại ảnh RGBA - chạy lại công cụ này sau mỗi lần đóng gói.
"""
import io
import os
import sys

from PIL import Image


def chuan(im):
    """RGBA, điểm trong suốt (alpha < 128) thành (0,0,0,0) để so sánh."""
    a = im.convert('RGBA')
    px = a.load()
    w, h = a.size
    out = []
    for y in range(h):
        for x in range(w):
            r, g, b, al = px[x, y]
            out.append((0, 0, 0, 0) if al < 128 else (r, g, b, 255))
    return out


def bang_mau(im):
    """Ảnh P (bảng màu tối thiểu, chỉ số 0 = trong suốt nếu có) hoặc None nếu không chuyển được."""
    a = im.convert('RGBA')
    px = list(a.getdata()) if not hasattr(a, 'get_flattened_data') else list(a.get_flattened_data())
    if any(0 < p[3] < 255 for p in px):
        return None
    co_trong = any(p[3] == 0 for p in px)
    mau = []
    chi_so = {}
    if co_trong:
        mau.append((0, 0, 0))
    for p in px:
        if p[3] == 0:
            continue
        k = p[:3]
        if k not in chi_so:
            chi_so[k] = len(mau)
            mau.append(k)
            if len(mau) > 256:
                return None
    du = [0 if p[3] == 0 else chi_so[p[:3]] for p in px]
    out = Image.new('P', a.size)
    flat = []
    for c in mau:
        flat.extend(c)
    out.putpalette(flat + [0] * (768 - len(flat)))
    out.putdata(du)
    return out, len(mau), co_trong


def ghi(out, so_mau, co_trong):
    bits = 1 if so_mau <= 2 else 2 if so_mau <= 4 else 4 if so_mau <= 16 else 8
    b = io.BytesIO()
    kw = {'optimize': True, 'bits': bits}
    if co_trong:
        kw['transparency'] = 0
    out.save(b, 'PNG', **kw)
    return b.getvalue()


def xu_ly(duong):
    goc = open(duong, 'rb').read()
    im = Image.open(io.BytesIO(goc))
    im.load()
    r = bang_mau(im)
    if r is None:
        return 0
    moi = ghi(*r)
    if len(moi) >= len(goc):
        return 0
    if chuan(Image.open(io.BytesIO(moi))) != chuan(im):
        print('BO QUA (khac diem anh):', duong)
        return 0
    open(duong, 'wb').write(moi)
    return len(goc) - len(moi)


def main():
    tong = 0
    so = 0
    for thu_muc in sys.argv[1:]:
        for ten in sorted(os.listdir(thu_muc)):
            if ten.endswith('.png'):
                d = xu_ly(os.path.join(thu_muc, ten))
                if d > 0:
                    tong += d
                    so += 1
    print('OK: %d anh nho hon, tiet kiem %d KB' % (so, tong // 1024))


if __name__ == '__main__':
    main()
