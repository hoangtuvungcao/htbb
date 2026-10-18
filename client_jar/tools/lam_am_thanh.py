#!/usr/bin/env python3
"""
Sinh am thanh cho ban jar J2ME (Huyen Thoai Bao Boi): res/am/*.wav (SFX) + res/am/*.mid (nhac nen)
va ban nhe res_nhe/am (chi SFX, it hon 60 KB).

    python3 -P j2me/tools/lam_am_thanh.py <thu_muc_tai_ve> j2me/res/am j2me/res_nhe/am

<thu_muc_tai_ve>: noi giai nen cac goi Kenney (CC0, https://kenney.nl):
    kenney_interface-sounds/  kenney_impact-sounds/  kenney_rpg-audio/
    kenney_digital-audio/     kenney_music-jingles/
(moi goi co License.txt "Creative Commons Zero, CC0"; ghi cong o res/LICENSE-audio.txt).

- SFX: cat im lang dau/cuoi, cat ngan, chuan hoa am luong, vuot nho cuoi, doi sang WAV PCM
  8 bit khong dau, mono, 11025 Hz (jingle 16000 Hz; ban nhe 8000 Hz) - nho va moi may MIDP 2 deu doc duoc.
- Tieng gam boss: TU TONG HOP (nhieu loc + quet tan so), khong lay tu dau.
- Nhac nen: nhac chiptune GOC soan bang ma o day (khong phai nhac Pokemon), ghi MIDI format 0,
  moi bai 8..16 nhip, lap vo han (Player.setLoopCount(-1) o client). CC0 nhu phan con lai.
Can: ffmpeg, numpy.
"""
import os
import random
import struct
import subprocess
import sys
import wave

import numpy as np

SR = 11025
SR_NHE = 8000

# ten trong jar (khop AmThanh.java) -> (goi, tep, cat_toi_da_giay, tan_so, am_luong)
SFX = [
    ("click", "kenney_interface-sounds", "click_001.ogg", 0.12, SR, 0.8),
    ("xacnhan", "kenney_interface-sounds", "confirmation_001.ogg", 0.30, SR, 0.8),
    ("huy", "kenney_interface-sounds", "back_001.ogg", 0.10, SR, 0.8),
    ("mo", "kenney_interface-sounds", "open_001.ogg", 0.16, SR, 0.8),
    ("danh", "kenney_impact-sounds", "impactPunch_medium_000.ogg", 0.22, SR, 1.0),
    ("phep", "kenney_digital-audio", "zapTwoTone.ogg", 0.28, SR, 0.85),
    ("chimang", "kenney_impact-sounds", "impactPunch_heavy_001.ogg", 0.32, SR, 1.0),
    ("hieuqua", "kenney_digital-audio", "phaserUp3.ogg", 0.28, SR, 0.8),
    ("ithieuqua", "kenney_impact-sounds", "impactGeneric_light_000.ogg", 0.14, SR, 0.7),
    ("truot", "kenney_rpg-audio", "knifeSlice2.ogg", 0.25, SR, 0.8),
    ("bidanh", "kenney_impact-sounds", "impactSoft_heavy_001.ogg", 0.22, SR, 0.9),
    ("chet", "kenney_digital-audio", "phaserDown2.ogg", 0.30, SR, 0.85),
    ("nhat", "kenney_interface-sounds", "pluck_001.ogg", 0.12, SR, 0.8),
    ("vang", "kenney_rpg-audio", "handleCoins2.ogg", 0.30, SR, 0.8),
    ("dichchuyen", "kenney_digital-audio", "phaseJump1.ogg", 0.40, SR, 0.8),
    ("hoimau", "kenney_digital-audio", "powerUp2.ogg", 0.40, SR, 0.8),
    ("canhbao", "kenney_digital-audio", "lowThreeTone.ogg", 0.45, SR, 0.8),
    ("lencap", "kenney_music-jingles", "8-Bit jingles/jingles_NES12.ogg", 0.95, 16000, 0.85),
    ("tienhoa", "kenney_music-jingles", "8-Bit jingles/jingles_NES00.ogg", 1.60, 16000, 0.85),
    ("nhiemvu", "kenney_music-jingles", "8-Bit jingles/jingles_NES04.ogg", 0.70, 16000, 0.85),
]
# ban nhe: chi cac tieng chien dau + giao dien chinh (it hon 60 KB)
NHE = ["click", "xacnhan", "huy", "mo", "danh", "phep", "chimang", "hieuqua", "truot", "bidanh",
       "chet", "nhat", "vang", "hoimau", "lencap", "boss"]


def doc(path, sr):
    raw = subprocess.run(["ffmpeg", "-v", "error", "-i", path, "-ac", "1", "-ar", str(sr), "-f", "f32le", "-"],
                         capture_output=True, check=True).stdout
    return np.frombuffer(raw, dtype=np.float32).astype(np.float64)


def lam_sach(x, sr, toi_da, am_luong):
    nguong = 0.02 * (np.abs(x).max() or 1)
    idx = np.where(np.abs(x) > nguong)[0]
    if len(idx):
        x = x[max(0, idx[0] - int(0.003 * sr)): idx[-1] + 1]
    x = x[: int(toi_da * sr)]
    n = len(x)
    fade = min(n // 3, int(0.04 * sr))
    if fade > 0:
        x[-fade:] *= np.linspace(1, 0, fade)
    dinh = np.abs(x).max() or 1
    return x / dinh * am_luong


def ghi_wav(path, x, sr):
    u8 = np.clip(np.round(x * 127 + 128), 0, 255).astype(np.uint8)
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(1)
        w.setframerate(sr)
        w.writeframes(u8.tobytes())


def gam_boss(sr=SR):
    """Tieng gam: rang cua tan so thap quet xuong + nhieu loc, rung bien do."""
    rng = np.random.default_rng(7)
    n = int(0.75 * sr)
    t = np.arange(n) / sr
    f = 120 - 55 * t / t[-1]
    pha = 2 * np.pi * np.cumsum(f) / sr
    rang = 2 * ((pha / (2 * np.pi)) % 1) - 1
    nhieu = rng.standard_normal(n)
    k = 6
    nhieu = np.convolve(nhieu, np.ones(k) / k, mode="same")
    rung = 0.75 + 0.25 * np.sin(2 * np.pi * 23 * t)
    vo = np.minimum(1, t / 0.06) * np.minimum(1, (t[-1] - t) / 0.25)
    x = (0.65 * rang + 0.55 * nhieu) * rung * vo
    x = np.tanh(2.2 * x)
    return x / np.abs(x).max() * 0.95


# ------------------------------------------------------------------------------- MIDI

TPQ = 48


def vlq(n):
    b = [n & 0x7F]
    n >>= 7
    while n:
        b.insert(0, (n & 0x7F) | 0x80)
        n >>= 7
    return bytes(b)


class Bai:
    def __init__(self, tempo):
        self.ev = []  # (tick, thu_tu, bytes)
        self.tempo = tempo

    def note(self, kenh, t, dai, not_, luc):
        t0 = int(round(t * TPQ))
        t1 = int(round((t + dai) * TPQ)) - 1
        self.ev.append((t0, 1, bytes([0x90 | kenh, not_, luc])))
        self.ev.append((max(t1, t0 + 1), 0, bytes([0x80 | kenh, not_, 0])))

    def nhac_cu(self, kenh, prog, vol=100):
        self.ev.append((0, -1, bytes([0xC0 | kenh, prog])))
        self.ev.append((0, -1, bytes([0xB0 | kenh, 7, vol])))

    def ghi(self, path, tong_phach):
        us = int(60_000_000 / self.tempo)
        ds = [(0, -2, b"\xff\x51\x03" + us.to_bytes(3, "big"))] + self.ev
        ds.sort(key=lambda e: (e[0], e[1]))
        out = bytearray()
        cu = 0
        trang_thai = None
        for t, _, b in ds:
            out += vlq(t - cu)
            cu = t
            # running status cho gon
            if b[0] < 0xF0 and b[0] == trang_thai:
                out += b[1:]
            else:
                out += b
                trang_thai = b[0] if b[0] < 0xF0 else None
        het = int(tong_phach * TPQ)
        out += vlq(max(0, het - cu)) + b"\xff\x2f\x00"
        with open(path, "wb") as f:
            f.write(b"MThd" + struct.pack(">IHHH", 6, 0, 1, TPQ))
            f.write(b"MTrk" + struct.pack(">I", len(out)) + out)


THANG = {
    "major": [0, 2, 4, 5, 7, 9, 11],
    "minor": [0, 2, 3, 5, 7, 8, 10],
    "dorian": [0, 2, 3, 5, 7, 9, 10],
    "hijaz": [0, 1, 4, 5, 7, 8, 10],
    "harm": [0, 2, 3, 5, 7, 8, 11],
}


def not_thang(goc, thang, bac):
    q, r = divmod(bac, 7)
    return goc + 12 * q + THANG[thang][r]


def soan(path, goc, thang, tempo, hop_am, prog_giai_dieu, prog_bass, nhip_giai_dieu, kieu_bass,
         trong, seed, prog_dem=None, quang_giai_dieu=12, luc=96, kieu_dem="arp"):
    """
    hop_am: bac (0..6) cua hop am moi o nhip (4 phach). Giai dieu: cau A (4 nhip) - B - A - B' ket ve chu am.
    """
    rng = random.Random(seed)
    b = Bai(tempo)
    b.nhac_cu(0, prog_giai_dieu, 100)
    b.nhac_cu(1, prog_bass, 96)
    if prog_dem is not None:
        b.nhac_cu(2, prog_dem, 70)
    so_nhip = len(hop_am)

    def chord_tones(bac):
        return [bac, bac + 2, bac + 4]

    # ---- giai dieu: sinh cau 4 nhip roi lap co bien tau
    def sinh_cau(nhip_dau, ket):
        cau = []
        bac_cu = 7 + hop_am[nhip_dau % so_nhip]
        for k in range(4):
            i = nhip_dau + k
            hb = hop_am[i % so_nhip]
            nhip = rng.choice(nhip_giai_dieu)
            if k == 3:
                nhip = [2, 2] if ket else rng.choice([[1, 1, 2], [2, 1, 1], [1.5, 0.5, 2]])
            t = 0
            for j, d in enumerate(nhip):
                manh = (t == 0 or t == 2)
                if manh:
                    ung = [x + 7 for x in chord_tones(hb)] + [x for x in chord_tones(hb)]
                    bac = min(ung, key=lambda x: abs(x - bac_cu) + rng.random() * 1.5)
                else:
                    bac = bac_cu + rng.choice([-2, -1, -1, 1, 1, 2])
                if ket and k == 3 and j == len(nhip) - 1:
                    bac = 7
                bac = max(2, min(quang_giai_dieu, bac))
                nghi = (not manh) and rng.random() < 0.12
                cau.append((i * 4 + t, d, None if nghi else bac))
                bac_cu = bac
                t += d
        return cau

    giai_dieu = []
    for p in range(0, so_nhip, 8):
        a = sinh_cau(p, False)
        bb = sinh_cau(p + 4, p + 8 >= so_nhip)
        giai_dieu += a + bb
    for t, d, bac in giai_dieu:
        if bac is None:
            continue
        b.note(0, t, d * 0.92, not_thang(goc + 12, thang, bac), luc)

    # ---- bass
    for i, hb in enumerate(hop_am):
        r = not_thang(goc - 12, thang, hb)
        nam = not_thang(goc - 12, thang, hb + 4)
        t0 = i * 4
        if kieu_bass == "nua":
            b.note(1, t0, 1.8, r, 100)
            b.note(1, t0 + 2, 1.8, nam, 90)
        elif kieu_bass == "tam":
            for k in range(8):
                b.note(1, t0 + k * 0.5, 0.45, r if k % 4 != 3 else nam, 100 if k % 2 == 0 else 80)
        elif kieu_bass == "dai":
            b.note(1, t0, 3.9, r, 90)
        elif kieu_bass == "nhay":
            for k, (dt, n) in enumerate([(0, r), (1, nam), (2, r + 12), (3, nam)]):
                b.note(1, t0 + dt, 0.9, n, 96 - 8 * (k % 2))

    # ---- dem (arpeggio / hop am giu)
    if prog_dem is not None:
        for i, hb in enumerate(hop_am):
            ns = [not_thang(goc, thang, x) for x in chord_tones(hb)]
            t0 = i * 4
            if kieu_dem == "arp":
                for k in range(8):
                    b.note(2, t0 + k * 0.5, 0.45, ns[[0, 1, 2, 1][k % 4]], 64)
            else:
                for n in ns:
                    b.note(2, t0, 3.9, n, 52)

    # ---- trong (kenh 10)
    if trong:
        for i in range(so_nhip):
            t0 = i * 4
            for k in range(8):
                tt = t0 + k * 0.5
                if trong == "nhe":
                    if k in (0, 4):
                        b.note(9, tt, 0.25, 36, 70)
                    if k % 2 == 1:
                        b.note(9, tt, 0.2, 42, 40)
                elif trong == "manh":
                    if k in (0, 3, 4):
                        b.note(9, tt, 0.25, 36, 100)
                    if k in (2, 6):
                        b.note(9, tt, 0.25, 38, 95)
                    b.note(9, tt, 0.2, 42, 55)
                elif trong == "tabla":
                    if k in (0, 3, 6):
                        b.note(9, tt, 0.25, 60 if k else 61, 80)
                    if k in (2, 5):
                        b.note(9, tt, 0.2, 70, 50)
            if trong == "manh" and i % 4 == 3:
                b.note(9, t0 + 3.5, 0.25, 49, 90)
    b.ghi(path, so_nhip * 4)


def soan_nhac(ra):
    # prog GM: 80 square, 81 saw, 79 ocarina, 73 flute, 10 music box, 68 oboe, 38 synth bass, 33 finger bass,
    # 89 warm pad, 46 harp, 12 marimba, 48 strings
    I, ii, iii, IV, V, vi = 0, 1, 2, 3, 4, 5
    soan(os.path.join(ra, "n_tieude.mid"), 60, "major", 116, [I, V, vi, IV] * 2 + [IV, V, iii, vi, ii, V, I, I],
         80, 38, [[1, 0.5, 0.5, 2], [1, 1, 1, 1], [0.5, 0.5, 1, 2]], "nhay", "manh", 11, prog_dem=48, kieu_dem="giu")
    soan(os.path.join(ra, "n_thitran.mid"), 65, "major", 100, [I, vi, IV, V, I, iii, IV, V, IV, V, iii, vi, ii, V, I, I],
         79, 33, [[1, 1, 2], [1.5, 0.5, 1, 1], [1, 1, 1, 1]], "nua", "nhe", 21, prog_dem=46)
    soan(os.path.join(ra, "n_duong.mid"), 67, "major", 132, [I, IV, V, I, vi, IV, ii, V, I, IV, V, vi, IV, V, I, I],
         80, 38, [[0.5, 0.5, 1, 1, 1], [1, 0.5, 0.5, 2], [0.5] * 4 + [2]], "tam", "manh", 31, prog_dem=12)
    soan(os.path.join(ra, "n_rung.mid"), 62, "dorian", 96, [0, 3, 0, 6, 0, 3, 4, 0, 5, 3, 6, 0, 3, 6, 4, 0],
         73, 33, [[1, 1, 2], [2, 1, 1], [1, 0.5, 0.5, 2]], "nua", "nhe", 41, prog_dem=12)
    soan(os.path.join(ra, "n_hang.mid"), 57, "minor", 78, [0, 5, 3, 4, 0, 5, 1, 4],
         89, 38, [[2, 2], [3, 1], [2, 1, 1]], "dai", None, 51, prog_dem=46, quang_giai_dieu=10, luc=80)
    soan(os.path.join(ra, "n_tuyet.mid"), 64, "minor", 90, [0, 5, 2, 6, 0, 3, 4, 4],
         10, 33, [[1, 1, 2], [1, 1, 1, 1], [2, 2]], "nua", "nhe", 61, prog_dem=89, kieu_dem="giu", luc=84)
    soan(os.path.join(ra, "n_samac.mid"), 62, "hijaz", 108, [0, 1, 0, 6, 0, 1, 3, 0],
         68, 33, [[1, 0.5, 0.5, 2], [0.5, 0.5, 1, 1, 1], [1, 1, 2]], "nhay", "tabla", 71)
    soan(os.path.join(ra, "n_dauchien.mid"), 57, "harm", 150, [0, 5, 3, 4, 0, 5, 1, 4, 5, 3, 0, 4, 5, 1, 4, 0],
         80, 38, [[0.5, 0.5, 1, 0.5, 0.5, 1], [1, 0.5, 0.5, 1, 1], [0.5] * 8], "tam", "manh", 81, prog_dem=81)
    soan(os.path.join(ra, "n_boss.mid"), 50, "harm", 160, [0, 0, 5, 4, 0, 0, 1, 4] * 2,
         81, 38, [[0.5, 0.5, 0.5, 0.5, 1, 1], [1, 0.5, 0.5, 0.5, 0.5, 1], [0.5] * 8], "tam", "manh", 91, prog_dem=48,
         kieu_dem="giu", luc=110)


def main():
    if len(sys.argv) < 4:
        print(__doc__)
        sys.exit(1)
    goc, ra, ra_nhe = sys.argv[1:4]
    os.makedirs(ra, exist_ok=True)
    os.makedirs(ra_nhe, exist_ok=True)
    for f in os.listdir(ra_nhe):
        os.remove(os.path.join(ra_nhe, f))
    for ten, goi, tep, toi_da, sr, al in SFX:
        x = lam_sach(doc(os.path.join(goc, goi, "Audio", tep), sr), sr, toi_da, al)
        ghi_wav(os.path.join(ra, "s_" + ten + ".wav"), x, sr)
    ghi_wav(os.path.join(ra, "s_boss.wav"), gam_boss(), SR)
    soan_nhac(ra)
    # ban nhe: cung tieng nhung 8000 Hz (jingle len cap cat ngan hon)
    for ten, goi, tep, toi_da, sr, al in SFX:
        if ten in NHE:
            x = lam_sach(doc(os.path.join(goc, goi, "Audio", tep), SR_NHE), SR_NHE, min(toi_da, 0.7), al)
            ghi_wav(os.path.join(ra_nhe, "s_" + ten + ".wav"), x, SR_NHE)
    ghi_wav(os.path.join(ra_nhe, "s_boss.wav"), gam_boss(SR_NHE)[: int(0.5 * SR_NHE)], SR_NHE)
    for d in (ra, ra_nhe):
        tong = sum(os.path.getsize(os.path.join(d, f)) for f in os.listdir(d))
        print(d, len(os.listdir(d)), "tep", tong, "byte")


if __name__ == "__main__":
    main()
