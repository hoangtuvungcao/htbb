#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Doi ten LOP / TEP / GOI con mang khai niem kiem hiep (KPAH) sang ten game Pokemon, theo doi_ten_lop.tsv.
Chay tren MONOREPO (bo cuc ban_do_gop.py: server/, client_jar/, client_apk/, client_pc_ios/, docs/, tools/).

    python3 -I doi_ten_lop.py --goc <monorepo>              # CHAY THU: in se doi gi
    python3 -I doi_ten_lop.py --goc <monorepo> --apply      # git mv (tep chua theo doi: mv) + thay ten trong ma/tai lieu
    python3 -I doi_ten_lop.py --goc <monorepo> --quay-lai <tar>

Chi dung dong trang_thai=DOI (thanh_phan server | client | chung; chung + loai tep = doi ten mot tep theo duong dan, thay ten tep moi noi). Dong GIU (khoa jar, tai nguyen, CSDL, goi tin) chi de tra cuu.
Thay ten: dung TU DOC LAP (khong dinh chu/so/_/$ hai ben) - setIdThanThu, CharThanthu... khong bi dung.
Pham vi: server -> server/ ; client -> client_jar/ client_apk/ client_pc_ios/ ; ca hai -> docs/ tools/ sql/
(ten trung o 2 thanh phan ma ten moi khac nhau: docs/tools/sql dung ten cua server)
(tru tools/doi_ten/ va cac ten mo ho trong BO_TAI_LIEU). Unity: .cs di kem .cs.meta (giu GUID).
Lam lai khong sao: tep cu khong con + tep moi da co = bo qua; thay chuoi lan 2 = khong doi gi.
"""
import argparse
import io
import json
import os
import re
import subprocess
import tarfile
import time
from pathlib import Path

DAY = Path(__file__).resolve().parent
PHAM_VI = {'server': ['server'], 'client': ['client_jar', 'client_apk', 'client_pc_ios']}
CHUNG = ['docs', 'tools', 'sql']   # sql: ghi chu do tools/ sinh ra phai khop ban sinh lai
BO_QUA = [re.compile(r'^tools/doi_ten/'), re.compile(r'^client_apk/app/'), re.compile(r'(^|/)(build|out|node_modules|Library|Temp|\.gradle)/')]
# ten trung voi ten khac o thanh phan kia (vd method client GameService.doRideAnimal) -> khong thay trong docs/tools
BO_TAI_LIEU = {'doRideAnimal'}
DUOI = {'.java', '.cs', '.py', '.sh', '.md', '.txt', '.gradle', '.pro', '.mjs', '.ts', '.vue', '.bat', '.yml', '.json', '.csv', '.html', '.xml', '.sql', '.tmpl', '.rsp', '.asmdef'}


def doc_bang():
    ds = []
    with open(DAY / 'doi_ten_lop.tsv', encoding='utf-8') as f:
        next(f)
        for d in f:
            tp, cu, moi, tt, loai, ly = d.rstrip('\n').split('\t')
            if tt == 'DOI':
                ds.append((tp, cu, moi, loai))
    ds.sort(key=lambda x: -len(x[1]))       # ten dai truoc (Skill_Kiem_TypeAEO truoc Skill_Kiem_Type...)
    return ds


def git_ls(goc):
    r = subprocess.run(['git', 'ls-files', '-z', '-c', '-o', '--exclude-standard'], cwd=goc, capture_output=True)
    return [p for p in r.stdout.decode('utf-8', 'surrogateescape').split('\0') if p] if r.returncode == 0 else None


def theo_doi(goc, p):
    return subprocess.run(['git', 'ls-files', '--error-unmatch', p], cwd=goc, capture_output=True).returncode == 0


def tim_tep_lop(goc, tep, tp, cu, loai):
    """Cac tep/thu muc can doi ten cho mot dong: <cu>.java|.cs (+.meta) trong pham vi; goi = thu muc."""
    ra = []
    if loai == 'tep':                    # dong 'chung': duong dan day du tu goc monorepo
        return [(cu, MOI[(tp, cu)])] if (goc / cu).exists() else []
    for p in tep:
        if not any(p.startswith(d + '/') for d in PHAM_VI[tp]) or any(b.search(p) for b in BO_QUA):
            continue
        ten = os.path.basename(p)
        if loai == 'lop' and ten in (cu + '.java', cu + '.cs', cu + '.cs.meta'):
            ra.append((p, os.path.join(os.path.dirname(p), ten.replace(cu, MOI[(tp, cu)], 1))))
    if loai == 'goi':
        for d in PHAM_VI[tp]:
            for r_, ds, _ in os.walk(goc / d):
                if os.path.basename(r_) == cu and any(f.endswith('.java') for f in os.listdir(r_)):
                    rel = os.path.relpath(r_, goc)
                    ra.append((rel, os.path.join(os.path.dirname(rel), MOI[(tp, cu)])))
                ds[:] = [x for x in ds if x not in ('build', 'out', '.gradle', 'Library')]
    return ra


MOI = {}


class SaoLuu:
    def __init__(self, goc, bat):
        self.bat, self.da, self.nk = bat, set(), {'mv': []}
        if bat:
            d = goc.parent / 'doi_ten_sao_luu'
            d.mkdir(exist_ok=True)
            self.duong = d / ('lop_truoc_%s.tar' % time.strftime('%Y%m%d_%H%M%S'))
            self.tar = tarfile.open(self.duong, 'w')

    def tep(self, rel, b):
        if self.bat and rel not in self.da:
            self.da.add(rel)
            ti = tarfile.TarInfo('noi_dung/' + rel)
            ti.size = len(b)
            self.tar.addfile(ti, io.BytesIO(b))

    def dong(self):
        if self.bat:
            b = json.dumps(self.nk, ensure_ascii=False).encode()
            ti = tarfile.TarInfo('nhat_ky.json')
            ti.size = len(b)
            self.tar.addfile(ti, io.BytesIO(b))
            self.tar.close()
            print('Sao luu: %s' % self.duong)


def quay_lai(goc, duong):
    with tarfile.open(duong) as t:
        nk = json.loads(t.extractfile('nhat_ky.json').read())
        for m in t.getmembers():                      # noi dung goc (luu theo duong dan SAU khi mv)
            if m.name.startswith('noi_dung/'):
                (goc / m.name[9:]).write_bytes(t.extractfile(m).read())
        for cu, moi, git in reversed(nk['mv']):
            if git:
                subprocess.run(['git', 'mv', moi, cu], cwd=goc, check=True)
            else:
                os.rename(goc / moi, goc / cu)
    print('Da tra lai tu', duong)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--goc', required=True)
    ap.add_argument('--apply', action='store_true')
    ap.add_argument('--quay-lai')
    ap.add_argument('--chi-tiet', action='store_true')
    a = ap.parse_args()
    goc = Path(a.goc).resolve()
    if a.quay_lai:
        return quay_lai(goc, a.quay_lai)
    bang = doc_bang()
    for tp, cu, moi, loai in bang:
        MOI[(tp, cu)] = moi
    tep = git_ls(goc)
    if tep is None:
        raise SystemExit('%s khong phai repo git' % goc)
    sl = SaoLuu(goc, a.apply)

    # 1) doi ten tep / thu muc (lop -> tep cung ten; goi -> thu muc)
    so_mv = 0
    for tp, cu, moi, loai in bang:
        for p, q in tim_tep_lop(goc, tep, tp, cu, loai):
            if not (goc / p).exists():
                print('  [DA XOA] %s khong con trong cay lam viec - bo qua' % p)
                continue
            if (goc / q).exists():
                print('  [XUNG DOT] %s da ton tai - bo qua %s' % (q, p))
                continue
            so_mv += 1
            if a.chi_tiet or not a.apply:
                print('  mv %s -> %s' % (p, q))
            if a.apply:
                git = theo_doi(goc, p) if loai == 'lop' else True
                if git:
                    subprocess.run(['git', 'mv', p, q], cwd=goc, check=True)
                else:
                    os.rename(goc / p, goc / q)
                sl.nk['mv'].append([p, q, git])
    if a.apply:
        tep = git_ls(goc)

    # 2) thay ten trong noi dung
    luat = {}
    for tp, cu, moi, loai in bang:
        if loai == 'tep':                # tep: thay ten tep (vd skin-linh-thu.md) trong moi lien ket
            cu, moi = os.path.basename(cu), os.path.basename(moi)
        rx = re.compile(r'(?<![A-Za-z0-9_$])' + re.escape(cu) + r'(?![A-Za-z0-9_$])')
        luat.setdefault(tp, []).append((rx, moi, cu))
    so_tep = so_cho = 0
    for p in tep:
        if any(b.search(p) for b in BO_QUA):
            continue
        f = goc / p
        if f.is_symlink() or not f.is_file() or os.path.splitext(p)[1].lower() not in DUOI:
            continue
        top = p.split('/', 1)[0]
        ds = []
        for tp, dirs in PHAM_VI.items():
            if top in dirs:
                ds = luat.get(tp, [])
        if top in CHUNG:
            sv = {x[2] for x in luat.get('server', [])}
            ds = [x for tp in luat for x in luat[tp] if tp != 'chung' and x[2] not in BO_TAI_LIEU and (tp == 'server' or x[2] not in sv)]
            ds.sort(key=lambda x: -len(x[2]))
        if top in CHUNG or any(top in d for d in PHAM_VI.values()):
            ds = ds + luat.get('chung', [])
        if not ds:
            continue
        b = f.read_bytes()
        if b'\0' in b[:8192]:
            continue
        try:
            s = b.decode('utf-8'); mh = 'utf-8'
        except UnicodeDecodeError:
            s = b.decode('latin-1'); mh = 'latin-1'
        s2, n = s, 0
        for rx, moi, cu in ds:
            s2, k = rx.subn(moi, s2)
            n += k
        if n:
            so_tep += 1
            so_cho += n
            if a.chi_tiet:
                print('  %s: %d cho' % (p, n))
            if a.apply:
                sl.tep(p, b)
                f.write_bytes(s2.encode(mh))
    sl.dong()
    print('%s: %d tep/thu muc doi ten, %d cho thay trong %d tep' % ('DA LAM' if a.apply else 'CHAY THU', so_mv, so_cho, so_tep))


if __name__ == '__main__':
    main()
