#!/usr/bin/env python3
"""
Tieng hieu ung CHAT LUONG CAO cho ban PC (client_pc/res/am_hq/s_*.wav): WAV PCM 16 bit, mono, 44100 Hz.

    python3 -P client_pc/tools/am_thanh_hq.py <thu_muc_tai_ve_kenney> client_pc/res/am_hq

Cung BANG TIENG va cung nguon CC0 (Kenney) voi client_jar/tools/lam_am_thanh.py (doc bang SFX + ham cat/chuan hoa
tu chinh tep do - mot noi khai bao), chi khac "ho so HQ": 44.1 kHz / 16 bit thay 11 kHz / 8 bit, giu dai hon
(x1.5, jingle x1.3), vuot cuoi 60 ms. Ban PC phat s_*.wav o day thay tieng 8 bit trong jar (MIDlet.moTaiNguyen:
/am/s_x.wav -> /am_hq/s_x.wav neu co). Can: ffmpeg, numpy. Ghi cong: client_jar/res/LICENSE-audio.txt (CC0).
"""
import importlib.util
import os
import sys
import wave

import numpy as np

SR = 44100
GOC = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location(
    "lam_am_thanh", os.path.join(GOC, "..", "..", "j2me", "tools", "lam_am_thanh.py"))
lat = importlib.util.module_from_spec(spec)
spec.loader.exec_module(lat)


def ghi16(path, x, sr):
    x = np.asarray(x, dtype=np.float64)
    n = len(x)
    fade = min(n // 3, int(0.06 * sr))
    if fade > 0:
        x[-fade:] *= np.linspace(1, 0, fade) ** 1.5
    s16 = np.clip(np.round(x * 32000), -32768, 32767).astype("<i2")
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(sr)
        w.writeframes(s16.tobytes())


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        sys.exit(1)
    nguon, ra = sys.argv[1:3]
    os.makedirs(ra, exist_ok=True)
    for ten, goi, tep, toi_da, sr, al in lat.SFX:
        dai = toi_da * (1.3 if sr != lat.SR else 1.5)
        x = lat.lam_sach(lat.doc(os.path.join(nguon, goi, "Audio", tep), SR), SR, dai, al * 0.92)
        ghi16(os.path.join(ra, "s_" + ten + ".wav"), x, SR)
    ghi16(os.path.join(ra, "s_boss.wav"), lat.gam_boss(SR) * 0.92, SR)
    tong = sum(os.path.getsize(os.path.join(ra, f)) for f in os.listdir(ra))
    print(ra, len(os.listdir(ra)), "tep", tong, "byte")


if __name__ == "__main__":
    main()
