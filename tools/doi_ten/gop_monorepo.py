#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Gop 3 repo (HTBB, htbb-client, HTBB-Web) thanh MOT monorepo htbb/ - GIU lich su git - kem doi ten KPAH -> HTBB.

    python3 -I HTBB/tools/doi_ten/gop_monorepo.py --dich <thu muc moi> [--lam <thu muc tam>] [--sao-luu]

Thu tu (moi buoc in ra; loi la dung):
  1. (--sao-luu) tar 3 repo (ca .git, tru node_modules/build) + novadb-data -> <goc>/sao_luu_gop_<gio>.tar
  2. chep cay lam viec 3 repo sang <lam>/cay/ (rsync, tru tep sinh ra) -> chay doi_ten_kpah.py --apply TREN BAN CHEP
  3. clone --mirror tung repo (chi doc repo goc) -> filter-branch --index-filter (ban_do_gop.LUAT) -> lich su da doi duong dan
  4. tao <dich>: nhanh = HTBB/<nhanh> + merge --allow-unrelated-histories 2 repo kia (main, pokemon/chuyen-doi);
     CHI nhap nhanh pokemon/chuyen-doi cua moi repo -> nhanh main (khong main/nhanh cu). CHI co commit gop.
  5. phu cay lam viec (thay doi chua commit cua 3 repo + doi ten) len <dich> theo ban do - de CHUA COMMIT
  6. sua_sau_gop.py: duong dan, .gitignore/.gitattributes goc, workflow, README goc, bo rac
  7. chep tep cuc bo bi ignore can de chay (server.ini, chay_novadb/, *.txt runtime, pmd_raw*, node_modules = hardlink)
3 repo goc KHONG bi sua (chi doc). Khong push, khong dung remote (remote cua <dich> de trong).
"""
import argparse
import os
import shutil
import subprocess
import sys
import tarfile
import time
from pathlib import Path

DAY = Path(__file__).resolve().parent
sys.path.insert(0, str(DAY))
sys.dont_write_bytecode = True
import ban_do_gop  # noqa: E402

REPO = ['HTBB', 'htbb-client', 'HTBB-Web']
NHANH_NGUON = 'pokemon/chuyen-doi'   # CHI nhap lich su nhanh Pokemon cua moi repo (khong nhap main/nhanh cu)
NHANH_DICH = 'main'                  # nhanh duy nhat cua monorepo
BO_CHEP = ['node_modules', '/build', '.output', '.nuxt', '.gradle', 'android/app/build', 'android/pokemon/build',
           'android/build', 'android/out', 'j2me/out', 'pc-java/out', 'pc/Library', 'pc/Temp', '__pycache__', '/chay_novadb/log']
# Tep/thu muc bi ignore nhung can de chay may dev: (repo, duong dan cu)
CUC_BO = [
    ('HTBB', 'server.ini'), ('HTBB', 'chay_novadb'), ('HTBB', 'AdminChat.txt'), ('HTBB', 'cuoptieu.txt'),
    ('HTBB', 'doitieu.txt'), ('HTBB', 'errorlog.txt'), ('HTBB', 'hack.txt'), ('HTBB', 'matketinfo.txt'),
    ('HTBB', 'nhantieu.txt'), ('HTBB', 'thetieu.txt'), ('HTBB', 'tratieu.txt'), ('HTBB', 'trang_thai.json'),
    ('HTBB', 'loginServer/server.ini'),
    ('HTBB', 'docs/pokemon/map_cu'), ('HTBB', 'docs/pokemon/map_moi/kiem'),
    ('HTBB', 'tools/pokemon/pmd_raw'), ('HTBB', 'tools/pokemon/pmd_raw_icon'), ('HTBB', 'tools/pokemon/pmd_raw_tile_moi'),
    ('htbb-client', 'phat-hanh/may_chu.conf'), ('htbb-client', 'android/local.properties'),
]


def chay(cmd, **kw):
    print('   $ ' + (cmd if isinstance(cmd, str) else ' '.join(map(str, cmd)))[:220], flush=True)
    return subprocess.run(cmd, check=True, shell=isinstance(cmd, str), **kw)


def ra(cmd, **kw):
    return subprocess.run(cmd, check=True, capture_output=True, **kw).stdout


def buoc(n, s):
    print('\n== %s. %s  [%s]' % (n, s, time.strftime('%H:%M:%S')), flush=True)


def sao_luu(goc):
    p = goc / ('sao_luu_gop_%s.tar' % time.strftime('%Y%m%d_%H%M%S'))
    bo = {'node_modules', '.output', '.nuxt', '.gradle', 'build', 'out', 'Library', '__pycache__'}

    def loc(ti):
        return None if os.path.basename(ti.name) in bo else ti
    with tarfile.open(p, 'w') as t:
        for r in REPO + ['novadb-data']:
            t.add(goc / r, arcname=r, filter=loc)
    print('   sao luu: %s (%.0f MB)' % (p, p.stat().st_size / 1e6))


def chep_cay(goc, lam):
    cay = lam / 'cay'
    cay.mkdir(parents=True, exist_ok=True)
    for r in REPO:
        bo = sum((['--exclude', b] for b in BO_CHEP), [])
        chay(['rsync', '-a', '--delete'] + bo + ['%s/%s/' % (goc, r), '%s/%s/' % (cay, r)])
    # doi ten KPAH -> HTBB tren BAN CHEP (script doi_ten nam trong ban chep, GOC cua no = cay/)
    chay([sys.executable, '-I', str(cay / 'HTBB/tools/doi_ten/doi_ten_kpah.py'), '--apply'])
    chay([sys.executable, '-I', str(cay / 'HTBB/tools/doi_ten/doi_ten_kpah.py'), '--kiem'])
    return cay


def viet_lai_lich_su(goc, lam):
    for r in REPO:
        g = lam / ('%s.git' % r)
        if g.exists():
            shutil.rmtree(g)
        chay(['git', 'clone', '-q', '--mirror', '--no-local', str(goc / r), str(g)])
        env = dict(os.environ, FILTER_BRANCH_SQUELCH_WARNING='1')
        loc = '%s -I %s %s' % (sys.executable, DAY / 'loc_lich_su.py', r)
        with open(lam / ('%s.filter.log' % r), 'w') as f:
            subprocess.run(['git', 'filter-branch', '--index-filter', loc, '--tag-name-filter', 'cat', '--', '--all'],
                           cwd=g, env=env, check=True, stdout=f, stderr=subprocess.STDOUT)
        # bo refs/original de clone sau khong keo theo
        for ref in ra(['git', 'for-each-ref', '--format=%(refname)', 'refs/original'], cwd=g).decode().split():
            chay(['git', 'update-ref', '-d', ref], cwd=g)


def tao_monorepo(lam, dich):
    if dich.exists() and any(dich.iterdir()):
        raise SystemExit('%s da co noi dung - chon thu muc trong' % dich)
    dich.mkdir(parents=True, exist_ok=True)
    chay(['git', 'init', '-q', '-b', NHANH_DICH, str(dich)])
    for r in REPO:
        chay(['git', 'fetch', '-q', '--no-tags', str(lam / ('%s.git' % r)),
              '+refs/heads/%s:refs/gop/%s/nguon' % (NHANH_NGUON, r)], cwd=dich)
    ten = 'HTBB-gop'
    env = dict(os.environ, GIT_AUTHOR_NAME=ten, GIT_COMMITTER_NAME=ten,
               GIT_AUTHOR_EMAIL='htbb@localhost', GIT_COMMITTER_EMAIL='htbb@localhost')
    nguoi = ra(['git', 'config', 'user.name'], cwd=dich).decode().strip() if subprocess.run(
        ['git', 'config', 'user.name'], cwd=dich, capture_output=True).returncode == 0 else ''
    if nguoi:
        env = dict(os.environ)
    for nh in [NHANH_DICH]:
        # Commit gop 3 cha: cay = hop 3 cay (duong dan da roi nhau sau ban do) - khong can chien luoc merge.
        cha = [ra(['git', 'rev-parse', 'refs/gop/%s/nguon' % r], cwd=dich).decode().strip() for r in REPO]
        idx = dich / '.git' / 'index.gop'
        if idx.exists():
            idx.unlink()
        e = dict(env, GIT_INDEX_FILE=str(idx))
        for c in cha:
            lst = ra(['git', 'ls-tree', '-r', '-z', '--full-tree', c], cwd=dich)
            moi_dong = []
            for muc in lst.split(b'\0'):
                if muc:
                    dau, duong = muc.split(b'\t', 1)
                    mode, kieu, sha = dau.split()
                    moi_dong.append(mode + b' ' + sha + b'\t' + duong)
            subprocess.run(['git', 'update-index', '--add', '-z', '--index-info'], cwd=dich, env=e,
                           input=b'\0'.join(moi_dong) + b'\0', check=True)
        cay_gop = ra(['git', 'write-tree'], cwd=dich, env=e).decode().strip()
        idx.unlink()
        tn = ('Gop 3 repo vao monorepo htbb (giu lich su nhanh %s cua moi repo)\n\n'
              'HTBB -> server/ sql/ docs/ tools/ vps/ client_pc_ios/ClientUnity/\n'
              'htbb-client -> client_jar/ (+shared/) client_apk/ client_pc/ client_pc_ios/ tools/phat-hanh/ docs/client/\n'
              'HTBB-Web -> web/ sql/web/\n\n'
              'Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>\n') % NHANH_NGUON
        c = ra(['git', 'commit-tree', cay_gop] + sum((['-p', x] for x in cha), []) + ['-m', tn], cwd=dich, env=env).decode().strip()
        chay(['git', 'update-ref', 'refs/heads/%s' % nh, c], cwd=dich)
    for line in ra(['git', 'for-each-ref', '--format=%(refname)', 'refs/gop'], cwd=dich).decode().split():
        chay(['git', 'update-ref', '-d', line], cwd=dich)
    chay(['git', 'symbolic-ref', 'HEAD', 'refs/heads/%s' % NHANH_DICH], cwd=dich)
    chay(['git', 'reset', '-q', '--hard'], cwd=dich)
    chay(['git', 'gc', '-q', '--prune=now'], cwd=dich)


def phu_cay(cay, dich):
    """Dat cay lam viec cua 3 ban chep (da doi ten) len dich theo ban do. Khong dung index cua dich."""
    so = [0, 0]
    for r in REPO:
        g = cay / r
        co = [p for p in ra(['git', 'ls-files', '-z', '-c', '-o', '--exclude-standard'], cwd=g).decode('utf-8', 'surrogateescape').split('\0') if p]
        dau = set(p for p in ra(['git', 'ls-tree', '-r', '-z', '--name-only', 'HEAD'], cwd=g).decode('utf-8', 'surrogateescape').split('\0') if p)
        con = set()
        for p in co:
            nguon = g / p
            if not (nguon.exists() or nguon.is_symlink()):
                continue
            con.add(p)
            d = dich / ban_do_gop.doi(r, p)
            d.parent.mkdir(parents=True, exist_ok=True)
            if d.is_symlink() or d.exists():
                if d.is_dir() and not d.is_symlink():
                    shutil.rmtree(d)
                else:
                    d.unlink()
            if nguon.is_symlink():
                os.symlink(os.readlink(nguon), d)
            else:
                shutil.copy2(nguon, d)
            so[0] += 1
        for p in dau - con:
            d = dich / ban_do_gop.doi(r, p)
            if d.is_symlink() or d.exists():
                d.unlink()
                so[1] += 1
    # thu muc rong con lai sau khi xoa tep (vd. server/web cua trang PHP cu da git rm)
    for goc_d, ds, fs in os.walk(dich, topdown=False):
        if '/.git' in goc_d + '/' or goc_d == str(dich):
            continue
        if not os.listdir(goc_d):
            os.rmdir(goc_d)
    print('   chep %d tep, xoa %d tep (da xoa/doi ten trong cay lam viec)' % tuple(so))


def chep_cuc_bo(goc, cay, dich):
    for r, p in CUC_BO:
        n = cay / r / p                    # ban chep (da doi ten, vd. chay_novadb/chay_lap.sh dung lib/htbb-core.jar)
        if not (n.exists() or n.is_symlink()):
            n = goc / r / p
        if not (n.exists() or n.is_symlink()):
            continue
        d = dich / ban_do_gop.doi(r, p)
        if d.exists():
            continue
        d.parent.mkdir(parents=True, exist_ok=True)
        if n.is_dir():
            shutil.copytree(n, d, symlinks=True, ignore=shutil.ignore_patterns('*.pid', 'log'))
        else:
            shutil.copy2(n, d)
        print('   chep cuc bo %s/%s -> %s' % (r, p, d.relative_to(dich)))
    nm = goc / 'HTBB-Web/node_modules'
    if nm.is_dir() and not (dich / 'web/node_modules').exists():
        chay(['cp', '-al', str(nm), str(dich / 'web/node_modules')])   # hardlink: khong ton cho


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--goc', default=str(DAY.parents[2]), help='thu muc chua 3 repo (mac dinh ~/Downloads/pokemon)')
    ap.add_argument('--dich', required=True, help='thu muc monorepo moi (phai trong)')
    ap.add_argument('--lam', help='thu muc tam (mac dinh <dich>.lam)')
    ap.add_argument('--sao-luu', action='store_true')
    ap.add_argument('--tu-buoc', type=int, default=1)
    a = ap.parse_args()
    goc, dich = Path(a.goc).resolve(), Path(a.dich).resolve()
    lam = Path(a.lam).resolve() if a.lam else Path(str(dich) + '.lam')
    lam.mkdir(parents=True, exist_ok=True)
    if a.sao_luu and a.tu_buoc <= 1:
        buoc(1, 'sao luu')
        sao_luu(goc)
    if a.tu_buoc <= 2:
        buoc(2, 'chep cay lam viec + doi ten KPAH -> HTBB tren ban chep')
        chep_cay(goc, lam)
    cay = lam / 'cay'
    if a.tu_buoc <= 3:
        buoc(3, 'viet lai lich su (filter-branch)')
        viet_lai_lich_su(goc, lam)
    if a.tu_buoc <= 4:
        buoc(4, 'tao monorepo + merge lich su')
        tao_monorepo(lam, dich)
    if a.tu_buoc <= 5:
        buoc(5, 'phu cay lam viec (chua commit)')
        phu_cay(cay, dich)
    if a.tu_buoc <= 6:
        buoc(6, 'sua duong dan sau khi gop')
        chay([sys.executable, '-I', str(DAY / 'sua_sau_gop.py'), str(dich)])
    if a.tu_buoc <= 7:
        buoc(7, 'chep tep cuc bo (ignore)')
        chep_cuc_bo(goc, cay, dich)
    print('\nXong: %s  (git -C %s status | log --graph)' % (dich, dich))


if __name__ == '__main__':
    main()
