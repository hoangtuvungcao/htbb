#!/usr/bin/env python3
"""Dựng video giới thiệu từ các đoạn quay bằng client thật (tools/video/KichBanVideo.java).

    python3 -I tools/video/dung_video.py <kich_ban_dung.json>

Tệp JSON:
  {
    "doan": "<thư mục doan/ chứa các .mp4 đã quay>",
    "chu_de": "<thư mục biển chữ đề .png (ChuDeVideo)>",
    "ra": "<thư mục ra>", "ten": "video_htbb_2phut",
    "nhac": "<tệp nhạc đã chuẩn bị (wav, đúng độ dài)>",
    "xfade": 0.5,
    "logo_doc": "<png logo cho dải trên video dọc>" (tuỳ chọn),
    "chan_doc": "<png biển chân video dọc>" (tuỳ chọn),
    "doan_list": [ {"tep": "c1_mo_dau", "tu": 0.0, "den": 4.0, "toc": 1.0,
                    "chu": "c1", "chu_tu": 0.3, "chu_dai": 3.2,
                    "chu_vi_tri": "duoi" | "giua" | "tren" | "to" | "y250"}, ... ]
  }
Ra: <ten>_1080p.mp4 (16:9, chữ đề giữa dưới), <ten>_doc.mp4 (9:16 1080x1920: khung đầy đủ HUD giữa màn, nền mờ,
chữ đề ở dải trên), cùng nhạc. H.264 yuv420p CRF 18, +faststart, AAC 192k.
"""
import json
import os
import subprocess
import sys

FPS = 30


def chay(lenh):
    print('+', ' '.join(lenh[:6]), '...' if len(lenh) > 6 else '')
    subprocess.run(lenh, check=True)


def dai(tep):
    r = subprocess.run(['ffprobe', '-v', 'error', '-show_entries', 'format=duration', '-of', 'csv=p=0', tep],
                       check=True, capture_output=True, text=True)
    return float(r.stdout.strip())


def main():
    kb = json.load(open(sys.argv[1], encoding='utf-8'))
    ra = kb['ra']
    tam = os.path.join(ra, 'tam_dung')
    os.makedirs(tam, exist_ok=True)
    xf = float(kb.get('xfade', 0.5))
    ds = kb['doan_list']
    # 1) cắt từng đoạn (sạch, không chữ), chuẩn 1920x1080 30 khung, tốc độ
    sach = []
    for i, d in enumerate(ds):
        out = os.path.join(tam, '%02d.mp4' % i)
        toc = float(d.get('toc', 1.0))
        vf = 'setpts=(PTS-STARTPTS)/%g,fps=%d,scale=1920:1080:flags=lanczos,format=yuv420p' % (toc, FPS)
        if d.get('mo_dau'):
            vf += ',fade=t=in:st=0:d=%g' % d['mo_dau']
        if d.get('mo_cuoi'):
            dd = (float(d['den']) - float(d['tu'])) / toc
            vf += ',fade=t=out:st=%g:d=%g' % (dd - d['mo_cuoi'], d['mo_cuoi'])
        chay(['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-ss', str(d['tu']), '-to', str(d['den']),
              '-i', d['tep'] if os.path.isabs(d['tep']) else os.path.join(kb['doan'], d['tep'] + '.mp4'), '-an', '-vf', vf, '-c:v', 'libx264', '-preset', 'medium',
              '-crf', '14', out])
        sach.append(out)
    # 2) nối bằng hoà tan (xfade), tính mốc bắt đầu từng đoạn trên trục thời gian chung
    dais = [dai(t) for t in sach]
    bd = [0.0]
    for i in range(1, len(sach)):
        bd.append(bd[-1] + dais[i - 1] - xf)
    tong = bd[-1] + dais[-1]
    print('tong %.2f s' % tong)
    lenh = ['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y']
    for t in sach:
        lenh += ['-i', t]
    f = []
    truoc = '[0:v]'
    for i in range(1, len(sach)):
        ra_n = '[v%d]' % i
        f.append('%s[%d:v]xfade=transition=fade:duration=%g:offset=%.3f%s' % (truoc, i, xf, bd[i], ra_n))
        truoc = ra_n
    sach_tong = os.path.join(tam, 'sach.mp4')
    if len(sach) == 1:
        lenh += ['-map', '0:v']
    else:
        lenh += ['-filter_complex', ';'.join(f), '-map', truoc]
    lenh += ['-c:v', 'libx264', '-preset', 'medium', '-crf', '14', '-pix_fmt', 'yuv420p', sach_tong]
    chay(lenh)
    # 3) mốc chữ đề
    chu = []
    for i, d in enumerate(ds):
        if d.get('chu'):
            t0 = bd[i] + float(d.get('chu_tu', 0.4))
            chu.append((d['chu'], t0, float(d.get('chu_dai', 3.0)), d.get('chu_vi_tri', 'duoi')))
    nhac = kb['nhac']

    def lop_chu(doc):
        """filter overlay chữ đề (mờ vào / ra 0.35 s)."""
        vao = []
        fl = []
        for j, (ten, t0, dd, vt) in enumerate(chu):
            png = os.path.join(kb['chu_de'], ten + ('_doc.png' if doc or vt == 'to' else '.png'))
            vao += ['-loop', '1', '-t', '%.3f' % (t0 + dd + 0.5), '-i', png]
        return vao

    for doc in (False, True):
        ten_ra = os.path.join(ra, kb['ten'] + ('_doc.mp4' if doc else '_1080p.mp4'))
        lenh = ['ffmpeg', '-hide_banner', '-loglevel', 'error', '-y', '-i', sach_tong, '-i', nhac]
        lenh += lop_chu(doc)
        so = 2
        fl = []
        if doc:
            # nền: chính khung hình phóng phủ, làm mờ + tối; giữa: khung đầy đủ HUD 1080x608
            fl.append('[0:v]split=2[nen0][fg0]')
            fl.append('[nen0]scale=3413:1920,crop=1080:1920,boxblur=24:2,eq=brightness=-0.18:saturation=1.1[nen]')
            fl.append('[fg0]scale=1080:608:flags=lanczos[fg]')
            fl.append('[nen][fg]overlay=0:656[b0]')
            cur = '[b0]'
            phu = []
            if kb.get('logo_doc'):
                lenh += ['-loop', '1', '-t', '%.3f' % tong, '-i', kb['logo_doc']]
                phu.append(('logo', len(chu) + so))
            if kb.get('chan_doc'):
                lenh += ['-loop', '1', '-t', '%.3f' % tong, '-i', kb['chan_doc']]
                phu.append(('chan', len(chu) + so + len(phu)))
            for loai, idx in phu:
                o = '[p%s]' % loai
                if loai == 'logo':
                    fl.append('%s[%d:v]overlay=(W-w)/2:70%s' % (cur, idx, o))
                else:
                    fl.append('%s[%d:v]overlay=(W-w)/2:1330%s' % (cur, idx, o))
                cur = o
        else:
            cur = '[0:v]'
        for j, (ten, t0, dd, vt) in enumerate(chu):
            idx = so + j
            a = '[c%d]' % j
            fl.append('[%d:v]format=rgba,fade=t=in:st=%.3f:d=0.35:alpha=1,fade=t=out:st=%.3f:d=0.35:alpha=1%s'
                      % (idx, t0, t0 + dd - 0.35, a))
            o = '[o%d]' % j
            if doc:
                pos = '(W-w)/2:%d' % (360 if not kb.get('logo_doc') else 350)
            elif vt == 'giua':
                pos = '(W-w)/2:(H-h)/2'
            elif vt == 'tren':
                pos = '(W-w)/2:150'
            elif str(vt).startswith('y'):
                pos = '(W-w)/2:%d' % int(vt[1:])   # "y250": mép trên biển ở y=250 (tránh chữ trong khung)
            else:
                pos = '(W-w)/2:H-h-170'
            fl.append('%s%soverlay=%s:enable=\'between(t,%.3f,%.3f)\'%s' % (cur, a, pos, t0, t0 + dd, o))
            cur = o
        fl.append('%sformat=yuv420p[v]' % cur)
        lenh += ['-filter_complex', ';'.join(fl), '-map', '[v]', '-map', '1:a', '-t', '%.3f' % tong,
                 '-c:v', 'libx264', '-preset', 'slow', '-crf', '18', '-pix_fmt', 'yuv420p', '-r', str(FPS),
                 '-c:a', 'aac', '-b:a', '192k', '-movflags', '+faststart', ten_ra]
        chay(lenh)
        print('RA', ten_ra, '%.2f s' % dai(ten_ra))
    json.dump({'tong': tong, 'bat_dau': bd, 'chu': chu}, open(os.path.join(tam, 'truc.json'), 'w'), indent=1)


if __name__ == '__main__':
    main()
