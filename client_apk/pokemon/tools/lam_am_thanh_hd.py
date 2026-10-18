#!/usr/bin/env python3
"""
Âm thanh CHẤT LƯỢNG CAO cho bản APK Huyền Thoại Bảo Bối (chỉ Android; bản jar giữ WAV 8 bit 11 kHz + MIDI).

    python3 -P client_apk/pokemon/tools/lam_am_thanh_hd.py <thu_muc_tai_ve_kenney> client_apk/pokemon/src/main/assets/am_hd

- SFX: cùng danh sách / cùng nguồn Kenney (CC0) với client_jar/tools/lam_am_thanh.py (bảng SFX), cắt + chuẩn
  hoá như bản jar nhưng giữ 44,1 kHz, 16 bit -> OGG Vorbis (assets/am_hd/s_<ten>.ogg). Tiếng gầm boss tổng
  hợp lại ở 44,1 kHz (gam_boss).
- Nhạc nền: đọc đúng các bài MIDI gốc của jar (client_jar/res/am/n_*.mid, nhạc chiptune soạn bằng mã - CC0) và
  tự TỔNG HỢP ở 44,1 kHz stereo bằng bộ tạo âm chiptune viết ở đây (xung vuông / răng cưa chống răng cưa
  polyBLEP, tam giác, sáo, hộp nhạc, nền đệm, trống nhiễu) + vang nhẹ -> OGG lặp liền mạch đúng một vòng bài
  (assets/am_hd/n_<ten>.ogg). Không dùng soundfont nên không vướng giấy phép mẫu âm.
Cần: ffmpeg (libvorbis), numpy.
"""
import os
import struct
import subprocess
import sys

import numpy as np

DAY = os.path.dirname(os.path.abspath(__file__))
GOC = os.path.normpath(os.path.join(DAY, "..", "..", ".."))
sys.path.insert(0, os.path.join(GOC, "j2me", "tools"))
import lam_am_thanh as jar  # noqa: E402  (bảng SFX, gam_boss, lam_sach)

SR = 44100


def ghi_ogg(path, x, kenh=1, q=4):
    """x: float [-1,1], shape (n,) hoặc (n,2)."""
    x = np.clip(x, -1, 1)
    pcm = (x * 32767).astype("<i2").tobytes()
    subprocess.run(["ffmpeg", "-v", "error", "-y", "-f", "s16le", "-ar", str(SR), "-ac", str(kenh), "-i", "-",
                    "-c:a", "libvorbis", "-q:a", str(q), path], input=pcm, check=True)


# ------------------------------------------------------------------------------------------- SFX
def tim(goc, tep):
    """Tệp trong gói Kenney (có gói để trong thư mục con Audio/...)."""
    p = os.path.join(goc, tep)
    if os.path.exists(p):
        return p
    ten = os.path.basename(tep)
    for d, _, ds in os.walk(goc):
        if ten in ds and (os.sep + os.path.dirname(tep) in d + os.sep or not os.path.dirname(tep)
                          or os.path.dirname(tep) in d):
            return os.path.join(d, ten)
    for d, _, ds in os.walk(goc):
        if ten in ds:
            return os.path.join(d, ten)
    raise SystemExit("thiếu " + p)


def lam_sfx(tai_ve, ra):
    for ten, goi, tep, toi_da, _sr, am in jar.SFX:
        src = tim(os.path.join(tai_ve, goi), tep)
        x = jar.doc(src, SR)
        x = jar.lam_sach(x, SR, toi_da * 1.25, am)   # giữ đuôi dài hơn chút (bản jar cắt ngắn cho nhẹ)
        ghi_ogg(os.path.join(ra, "s_%s.ogg" % ten), x, 1, 5)
    ghi_ogg(os.path.join(ra, "s_boss.ogg"), jar.gam_boss(SR), 1, 5)


# ------------------------------------------------------------------------------------------- MIDI
def doc_midi(path):
    d = open(path, "rb").read()
    assert d[:4] == b"MThd"
    tpq = struct.unpack(">H", d[12:14])[0]
    i = 14
    assert d[i:i + 4] == b"MTrk"
    n = struct.unpack(">I", d[i + 4:i + 8])[0]
    tr = d[i + 8:i + 8 + n]
    p = 0
    t = 0
    st = None
    tempo = 500000
    prog = {}
    vol = {}
    on = {}
    notes = []
    het = 0

    def vlq():
        nonlocal p
        v = 0
        while True:
            b = tr[p]
            p += 1
            v = (v << 7) | (b & 0x7F)
            if b < 0x80:
                return v

    while p < len(tr):
        t += vlq()
        b = tr[p]
        if b == 0xFF:
            typ = tr[p + 1]
            p += 2
            ln = vlq()
            dat = tr[p:p + ln]
            p += ln
            if typ == 0x51:
                tempo = int.from_bytes(dat, "big")
            elif typ == 0x2F:
                het = t
                break
            continue
        if b >= 0x80:
            st = b
            p += 1
        kind, ch = st & 0xF0, st & 0x0F
        if kind in (0x80, 0x90, 0xB0):
            a, v = tr[p], tr[p + 1]
            p += 2
            if kind == 0x90 and v > 0:
                on[(ch, a)] = (t, v)
            elif kind in (0x80, 0x90):
                if (ch, a) in on:
                    t0, vv = on.pop((ch, a))
                    notes.append((t0, t, ch, a, vv))
            elif kind == 0xB0 and a == 7:
                vol[ch] = v
        elif kind == 0xC0:
            prog[ch] = tr[p]
            p += 1
        elif kind == 0xD0:
            p += 1
        else:
            p += 2
    giay_tick = tempo / 1e6 / tpq
    return notes, prog, vol, het * giay_tick, giay_tick


def blep(t, dt):
    """polyBLEP chống răng cưa cho dạng sóng gián đoạn (t: pha 0..1)."""
    y = np.zeros_like(t)
    m = t < dt
    x = t[m] / dt
    y[m] = x + x - x * x - 1
    m2 = t > 1 - dt
    x = (t[m2] - 1) / dt
    y[m2] = x * x + x + x + 1
    return y


def dang_song(prog, f, n, rng):
    tt = np.arange(n) / SR
    vib = 1 + 0.004 * np.sin(2 * np.pi * 5.2 * tt) * np.minimum(1, tt / 0.25)
    pha = np.cumsum(f * vib / SR) % 1.0
    dt = f / SR
    if prog in (80,):          # xung vuông 50% (lead chiptune)
        y = np.where(pha < 0.5, 1.0, -1.0) + blep(pha, dt) - blep((pha + 0.5) % 1, dt)
        return 0.55 * y
    if prog in (81,):          # răng cưa
        y = 2 * pha - 1 - blep(pha, dt)
        return 0.45 * y
    if prog in (38, 33, 34, 39):   # bass: tam giác + sub
        tri = 2 * np.abs(2 * pha - 1) - 1
        return 0.8 * tri + 0.25 * np.sin(2 * np.pi * pha)
    if prog in (79, 73, 68):   # ocarina / sáo / oboe: sin + bồi âm + hơi thở
        y = np.sin(2 * np.pi * pha) + 0.18 * np.sin(4 * np.pi * pha) + (0.12 if prog == 68 else 0.05) * np.sin(6 * np.pi * pha)
        return 0.7 * y + 0.02 * rng.standard_normal(n)
    if prog in (10, 12, 46, 8, 9, 11, 13):   # hộp nhạc / marimba / đàn hạc: tắt nhanh
        tat = np.exp(-tt * (5.0 if prog == 10 else 7.0))
        y = np.sin(2 * np.pi * pha) + 0.3 * np.sin(2 * np.pi * pha * (4.0 if prog == 12 else 3.0)) * np.exp(-tt * 14)
        return 0.75 * y * tat
    # nền / dây (48, 89, ...): 2 răng cưa lệch nhẹ, lọc thông thấp
    pha2 = np.cumsum(f * 1.006 / SR) % 1.0
    y = (2 * pha - 1 - blep(pha, dt)) + (2 * pha2 - 1 - blep(pha2, dt))
    k = 24
    y = np.convolve(y, np.ones(k) / k, mode="same")
    return 0.32 * y


def vo_bao(n, prog, dai):
    a = int(SR * (0.06 if prog in (48, 89) else 0.006))
    r = int(SR * (0.25 if prog in (48, 89) else 0.06))
    e = np.ones(n)
    a = min(a, n)
    if a > 0:
        e[:a] = np.linspace(0, 1, a)
    gl = int(dai * SR)
    if gl < n:
        e[gl:] *= np.exp(-np.arange(n - gl) / max(1, r) * 3)
    sus = 0.85 if prog in (80, 81) else 1.0
    return e * sus


def trong(nota, n, rng):
    tt = np.arange(n) / SR
    if nota in (35, 36):       # trống cái
        f = 55 + 90 * np.exp(-tt * 30)
        return 0.9 * np.sin(2 * np.pi * np.cumsum(f) / SR) * np.exp(-tt * 9)
    if nota in (38, 40):       # trống con
        nz = rng.standard_normal(n)
        return (0.45 * nz + 0.3 * np.sin(2 * np.pi * 190 * tt)) * np.exp(-tt * 18)
    nz = rng.standard_normal(n)  # chũm chọe
    nz = nz - np.concatenate([[0], nz[:-1]])
    return 0.22 * nz * np.exp(-tt * (40 if nota in (42, 44) else 12))


def tong_hop(path):
    notes, prog, vol, dai_bai, gt = doc_midi(path)
    rng = np.random.default_rng(12345)
    duoi = 2.0
    N = int((dai_bai + duoi) * SR)
    L = np.zeros(N)
    Rr = np.zeros(N)
    pan = {0: 0.0, 1: -0.15, 2: 0.3, 9: 0.1}
    for t0, t1, ch, nota, luc in notes:
        s = t0 * gt
        dai = max(0.03, (t1 - t0) * gt)
        p = prog.get(ch, 80)
        if ch == 9:
            n = int(SR * 0.5)
            y = trong(nota, n, rng)
        else:
            n = int(SR * (dai + (0.6 if p in (48, 89) else 0.25)))
            f = 440.0 * 2 ** ((nota - 69) / 12)
            y = dang_song(p, f, n, rng) * vo_bao(n, p, dai)
        g = (luc / 127.0) * (vol.get(ch, 100) / 127.0) * (0.55 if ch == 2 else 0.8)
        i0 = int(s * SR)
        i1 = min(N, i0 + len(y))
        y = y[: i1 - i0] * g
        pp = pan.get(ch, 0)
        L[i0:i1] += y * (1 - pp) * 0.5
        Rr[i0:i1] += y * (1 + pp) * 0.5
    # vang nhẹ (2 comb lệch trái / phải)
    for buf, tre in ((L, 0.083), (Rr, 0.097)):
        d = int(tre * SR)
        for _ in range(3):
            buf[d:] += 0.22 * buf[:-d]
    # lặp liền mạch: phần đuôi sau điểm kết bài cộng vòng về đầu
    M = int(dai_bai * SR)
    for buf in (L, Rr):
        buf[: N - M] += buf[M:N]
    x = np.stack([L[:M], Rr[:M]], axis=1)
    x = np.tanh(1.4 * x / (np.abs(x).max() or 1)) * 0.92
    return x


def lam_nhac(ra):
    am = os.path.join(GOC, "j2me", "res", "am")
    for ten in sorted(os.listdir(am)):
        if ten.startswith("n_") and ten.endswith(".mid"):
            x = tong_hop(os.path.join(am, ten))
            ghi_ogg(os.path.join(ra, ten[:-4] + ".ogg"), x, 2, 3)
            print("nhac", ten, "%.1fs" % (len(x) / SR))


def main():
    tai_ve, ra = sys.argv[1], sys.argv[2]
    os.makedirs(ra, exist_ok=True)
    lam_sfx(tai_ve, ra)
    lam_nhac(ra)
    with open(os.path.join(ra, "GHI_CONG.txt"), "w", encoding="utf-8") as f:
        f.write("Tiếng hiệu ứng: Kenney (kenney.nl) - CC0 1.0; làm lại 44,1 kHz OGG bởi lam_am_thanh_hd.py.\n"
                "Nhạc nền: nhạc chiptune gốc của HTBB (client_jar/tools/lam_am_thanh.py, CC0) tổng hợp 44,1 kHz stereo.\n")
    print("OK", ra)


main()
