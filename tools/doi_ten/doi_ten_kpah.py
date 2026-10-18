#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Doi ten KPAH -> HTBB tren ca 3 repo (HTBB, htbb-client, HTBB-Web).

    python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py              # CHAY THU (mac dinh): chi in bao cao
    python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --apply      # sua that + doi ten tep/thu muc
    python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --chi-tiet   # in tung dong se doi
    python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --da-doi-repo  # SAU KHI chu repo da chay `gh repo rename`
    python3 -I HTBB/tools/doi_ten/doi_ten_kpah.py --kiem       # chi liet ke cho con sot (sau --apply)

Chay lai bao nhieu lan cung duoc (idempotent):
  * dong co chuoi TUONG-THICH-TEN-CU duoc bo qua (do la cho co y giu ten cu de tuong thich 1 ban);
  * dong ngay sau mot dong chua TUONG-THICH-TEN-CU-DONG-SAU cung duoc bo qua
    (dung cho tep khong cho chu thich cuoi dong: .gitignore, unit systemd);
  * cac chuoi trong BAO_VE khong bao gio bi doi (ten trong bytecode, khoa RMS, ten mien, repo ngoai...);
  * cac mieng va (VA) kiem dau hieu rieng truoc khi chen.

Khong commit, khong push, khong dung toi remote. Doi ten tep bang `git mv` (chi dua vao index).
Xem HTBB/tools/doi_ten/KE_HOACH_DOI_TEN.md.
"""
import argparse
import collections
import io
import json
import tarfile
import time
import os
import re
import subprocess
import sys
import zipfile
from pathlib import Path

GOC = Path(__file__).resolve().parents[3]          # .../pokemon
REPO = {
    'HTBB': GOC / 'HTBB',
    'htbb-client': GOC / 'htbb-client',
    'HTBB-Web': GOC / 'HTBB-Web',
}
DAU = 'TUONG-THICH-TEN-CU'
DAU_SAU = 'TUONG-THICH-TEN-CU-DONG-SAU'

# Tep / thu muc khong dung toi (theo duong dan tuong doi trong repo).
BO_QUA = {
    'HTBB': [r'^\.claude/', r'^tools/doi_ten/', r'^vps/CHUYEN_TEN_HTBB\.md$'],
    'htbb-client': [r'^\.claude/'],
    'HTBB-Web': [r'^\.claude/'],
}

# Tep khong nam trong git (bi .gitignore) nhung dang dung that tren may dev.
TEP_NGOAI_GIT = {
    'HTBB': ['chay_novadb/chay_lap.sh'],
}

# --------------------------------------------------------------------------------------------
# 1. BAO VE: khong bao gio doi. (regex, ly do) - liet ke trong bao cao "con lai co chu y".
# --------------------------------------------------------------------------------------------
BAO_VE = [
    (r'\breal\.UtilKPAH\b|\bUtilKPAH\b', 'lop real.UtilKPAH nam trong bytecode lib/htbb-core.jar (khong co source)'),
    (r'\blogNQSH\b', 'goi logNQSH nam trong bytecode lib/htbb-core.jar'),
    (r'\bkpahmay\b', 'khoa RMS/PlayerPrefs ma may: doi = moi may mat ma, lach duoc cam theo may'),
    (r'\b(?:nqshlogin|nqshIP|nqshQuickSlot|ipnqshauto|ipnqsh|nqshImgPotionNew|nqshImgPotion)\b',
     'khoa RMS da luu tren may nguoi choi (mat khau/IP/o nhanh): doi = mat du lieu da luu'),
    (r'\bkpah-server/nqsh-h2-tai\b|\bkpah-server(?=/)', 'repo GitHub kenh NQSH-H2 cu (APK cu doc cung raw URL nay)'),
    (r'\b(?:sv\.|game\.|www\.)?kpah-private\.site\b', 'ten mien DNS that (doi = mua/tro ten mien moi)'),
    (r'\bg=kpah\b', 'tham so API cua my.teamobi.com (ben ngoai)'),
    (r'\bKPAH_27[67](?:[-_][A-Za-z0-9]+)*(?:\.exe)?', 'ten tep/thu muc goc cua TeaMobi (KPAH_276.exe, KPAH_276_Data...)'),
    (r'\b(?:chay-)?kpah275555(?:[_-][A-Za-z0-9]+)*(?:\.jar|\.bat)?', 'jar J2ME goc 2.7.5 cua TeaMobi tren may chu repo'),
    (r'\bKPAH-VER3\b', 'ten fork goc trong Downloads (lich su)'),
    (r'\bbuild/emu/kpah\.jar\b', 'tep jar goc giai nen de doi chieu (lich su)'),
    (r'\bKhiPhachAnhHung\b|suite-Khi Phach Anh Hung', 'ten tep exe / suite MicroEmulator cua client goc'),
    (r'\bTEN_(?:GAME|SAN_PHAM|TEP)_CU\s*=\s*"[^"]*"', 'ten hien thi khi sv.che_do_pokemon=0 (che do game goc)'),
    (r'indexOf\("KPAH"\)|tên cũ KPAH/NQSH', 'bo loc an ten server cu trong danh sach (chuc nang)'),
    (r"\(\d+,'kpah'\)", "bang bad_word: tu cam trong chat (chan quang cao ten server cu)"),
    (r'"teamobi", "kpah"', 'danh sach ten cam dang ky (chan mao danh ten cu) - nen THEM "htbb", khong bo "kpah"'),
    (r'\bNQSH2\.txt\b', 'URL danh sach server client cu da phat hanh tai ve (/NQSH2.txt)'),
    (r'/var/www/html/kpah\b|(?<!/opt)(?<!/logrotate\.d)(?<!/ssl)(?<!Desktop)/kpah\b(?![-_.])',
     'tien to URL cu http://may/kpah/... (link da phat tan; RedirectMatch ^/kpah/ giu de chuyen huong)'),
]
# Repo GitHub cua chinh chu: chi doi khi da --da-doi-repo (sau `gh repo rename`).
REPO_GITHUB = [
    (r'\bhoanghoa0903/KPAH-Web\b', 'hoanghoa0903/HTBB-Web'),
    (r'\bhoanghoa0903/KPAH\b', 'hoanghoa0903/HTBB'),
    (r'\bKPAH-Web\b', 'HTBB-Web'),
    (r'\bkpah-mod-pb\b', 'htbb-client'),
]

# --------------------------------------------------------------------------------------------
# 2. LUAT DOI (theo thu tu; cu the truoc, chung sau). (nhom, regex, thay, pham vi repo|None)
#    Nhom: 1 CSDL, 2 ENV, 3 DUONG_DAN/systemd, 4 JAR, 5 REPO, 6 MA/CHU_THICH, 7 NGUOI_CHOI_THAY
# --------------------------------------------------------------------------------------------
LUAT = [
    # --- 7. chu nguoi choi / quan tri thay, cau van nhac ten cu (phai chay TRUOC luat chung) ---
    ('7', r' \(HTBB; trước đây là KPAH — "Khí Phách Anh Hùng"\)', ' (HTBB)', None),
    ('7', r' \(HTBB; trước đây là Khí Phách Anh Hùng / KPAH của TeaMobi\)', ' (HTBB)', None),
    ('7', r' \(trước đây Khí Phách Anh Hùng\)', '', None),
    ('7', r'thay cho "Khí Phách Anh Hùng" / tên tạm "KPAH Pokémon" trước đây', 'thay cho tên cũ', None),
    ('7', r' \(trước đây là KPAH\)', '', None),
    ('7', r'vườn KPAH sẵn có', 'vườn có sẵn của game gốc', None),
    ('7', r'bản đồ cá nhân KPAH', 'bản đồ cá nhân của game gốc', None),
    ('7', r'"pronqsh"', '"probaoboi"', None),
    ('7', r'client NQSH-H2 của mình', 'client HTBB chính thức của mình', ['HTBB-Web']),
    ('7', r'client NQSH-H2 chính thức', 'client HTBB chính thức', ['HTBB-Web']),
    ('7', r'bản NQSH-H2 của bạn', 'bản HTBB chính thức của bạn', ['HTBB-Web']),
    ('7', r'NQSH-H2 của mình', 'HTBB chính thức', ['HTBB-Web']),
    ('7', r'HTBB276-NQSH-H2-', 'HTBB276-', ['htbb-client']),
    ('7', r"rootProject\.name = 'nqsh-h2-android'", "rootProject.name = 'htbb-android'", ['htbb-client']),
    ('7', r'unity\.KPAH\.KPAH', 'com.htbb.pokemon', ['htbb-client']),
    ('7', r'# htbb-client — client NQSH-H2 \(Huyền Thoại Bảo Bối\)', '# htbb-client — client Huyền Thoại Bảo Bối', ['htbb-client']),
    ('7', r'"KPAH2-(PKVIP|DOITIEU)"', r'"\1"', None),
    ('7', r'C:/Users/ADMIN/Desktop/KPAH-All/HTBB', 'C:/duong/dan/HTBB', None),

    # --- 4. jar ---
    ('4', r'KPAH2_patched_backup_truoc_capnhat', 'htbb-server_backup_truoc_capnhat', None),
    ('4', r'KPAH2_patched', 'htbb-server', None),
    ('4', r'KPAH2_NEW', 'HTBB_NEW', None),
    ('4', r'KPAH2_TRUOC_', 'htbb-server_TRUOC_', None),
    ('4', r'\bNQSH_5h\.jar\b', 'htbb-core.jar', None),
    ('4', r'\bNQSH_5h\b', 'htbb-core', None),
    ('4', r'\bbuild/nqsh-src\b', 'build/core-src', None),
    ('4', r'\bnqsh-src\b', 'core-src', None),
    ('4', r'\bgiai-ma-nqsh\b', 'giai-ma-core', None),
    ('4', r'project-kpah-nqsh-jar-khoa-ten-package', 'project-htbb-core-jar-khoa-ten-package', None),

    # --- 1. CSDL ---
    ('1', r'\bkpah_db', 'htbb_db', None),            # ca kpah_db_hst, kpah_db.bang, vps_kpah_db_...
    ('1', r'kpah_db\b', 'htbb_db', None),

    # --- 2. bien moi truong ---
    ('2', r'\bKPAH_(?![0-9])', 'HTBB_', None),

    # --- 3. duong dan / dich vu / user he thong ---
    ('3', r'/opt/kpah-web\b', '/opt/htbb-web', None),
    ('3', r'/opt/kpah\b', '/opt/htbb', None),
    ('3', r'/etc/logrotate\.d/kpah\b', '/etc/logrotate.d/htbb', None),
    ('3', r'/etc/ssl/kpah\b', '/etc/ssl/htbb', None),
    ('3', r'\b90-kpah\.cnf\b', '90-htbb.cnf', None),
    ('3', r'\bkpah\.(crt|key)\b', r'htbb.\1', None),
    ('3', r'\bkpah-(game|login|web)\b', r'htbb-\1', None),
    ('3', r'\bkpah-(?=[a-z])', 'htbb-', None),       # kpah-ssl, kpah-tai, kpah-backup, kpah-cloudflare-web...

    # --- 6. con lai: user CSDL/he thong 'kpah', chu thich, ten trong tai lieu ---
    ('6', r'KPAH', 'HTBB', None),
    ('6', r'Kpah', 'Htbb', None),
    ('6', r'kpah', 'htbb', None),
    ('6', r'Khí Phách Anh Hùng', 'Huyền Thoại Bảo Bối', None),
]

# --------------------------------------------------------------------------------------------
# 3. LUAT CA DONG co tuong thich (dong ra mang dau TUONG-THICH-TEN-CU nen lan sau bo qua)
# --------------------------------------------------------------------------------------------
RE_YML_ENV = re.compile(r'^(\s*)KPAH_([A-Z0-9_]+):\s*\$\{\{\s*(secrets|vars)\.KPAH_\2\s*\}\}\s*$')
RE_JS_ENV = re.compile(r'process\.env\.KPAH_([A-Z0-9_]+)')
BIEN_SH = {'DIR', 'OUT', 'THU_MUC', 'GOC', 'USER_SV', 'USER_WEB', 'SV', 'SV_GAME', 'SV_LOGIN', 'SERVICE', 'ENV_FILE'}
RE_SH_GAN = re.compile(
    r'^(?P<ind>\s*)(?P<bien>[A-Z_][A-Z0-9_]*)=(?P<dau>\$\{[A-Z_][A-Z0-9_]*:-)?'
    r'(?P<gt>/opt/kpah(?:-web)?(?:/[\w./-]*)?|/etc/kpah-web\.env|kpah(?:-game|-login|-web)?)'
    r'(?P<cuoi>\})?\s*$')


def doi_chuoi(s, repo, da_doi_repo, dem=None):
    """Ap BAO_VE + LUAT len mot chuoi (khong co dau tuong thich)."""
    bao_ve = list(BAO_VE)
    if da_doi_repo:
        for r_, t in REPO_GITHUB:
            s2, n = re.subn(r_, t, s)
            if n and dem is not None:
                dem['5'] += n
            s = s2
    else:
        bao_ve += [(r_, 'repo GitHub chua doi ten') for r_, _ in REPO_GITHUB]
    giu = []

    def che(m):
        giu.append(m.group(0))
        return '\x00%d\x00' % (len(giu) - 1)
    for r_, _ in bao_ve:
        s = re.sub(r_, che, s)
    for nhom, r_, t, pv in LUAT:
        if pv and repo not in pv:
            continue
        s2, n = re.subn(r_, t, s)
        if n and dem is not None:
            dem[nhom] += n
        s = s2
    return re.sub('\x00(\\d+)\x00', lambda m: giu[int(m.group(1))], s)


def luat_ca_dong(dong, duoi, repo, da_doi_repo, dem):
    """Tra ve dong moi neu la mot trong cac dang can tuong thich, nguoc lai None."""
    tach = dong.rstrip('\r\n')
    nl = dong[len(tach):]
    # GitHub Actions: KPAH_X: ${{ secrets.KPAH_X }}  ->  HTBB_X: ${{ secrets.HTBB_X || secrets.KPAH_X }}
    if duoi in ('.yml', '.yaml'):
        m = RE_YML_ENV.match(tach)
        if m:
            dem['2'] += 1
            ind, ten, loai = m.group(1), m.group(2), m.group(3)
            return '%sHTBB_%s: ${{ %s.HTBB_%s || %s.KPAH_%s }}  # %s%s' % (ind, ten, loai, ten, loai, ten, DAU, nl)
    # JS/TS doc thang process.env.KPAH_X -> (process.env.HTBB_X ?? process.env.KPAH_X)
    if duoi in ('.ts', '.mjs', '.js', '.cjs') and RE_JS_ENV.search(tach):
        moi = RE_JS_ENV.sub(lambda m: '(process.env.HTBB_%s ?? process.env.KPAH_%s)' % (m.group(1), m.group(1)), tach)
        dem['2'] += len(RE_JS_ENV.findall(tach))
        moi = doi_chuoi(moi.replace('process.env.KPAH_', '\x01'), repo, da_doi_repo, dem).replace('\x01', 'process.env.KPAH_')
        return '%s // %s%s' % (moi, DAU, nl)
    # shell: VAR=/opt/kpah..., VAR=kpah-game ... -> gia tri moi + roi ve ten cu neu may chua chuyen
    if duoi in ('.sh', '') and not tach.lstrip().startswith('#'):
        m = RE_SH_GAN.match(tach)
        if m and m.group('bien') in BIEN_SH:
            ind, bien, dau, gt, cuoi = m.group('ind'), m.group('bien'), m.group('dau') or '', m.group('gt'), m.group('cuoi') or ''
            moi = doi_chuoi(gt, repo, da_doi_repo, None)
            if gt.startswith('/opt/'):
                goc_moi = '/opt/htbb-web' if gt.startswith('/opt/kpah-web') else '/opt/htbb'
                kiem = '[ -d %s ]' % goc_moi
                dem['3'] += 1
            elif gt.startswith('/etc/'):
                kiem = '[ -e %s ] || [ ! -e %s ]' % (moi, gt)
                dem['3'] += 1
            elif gt.endswith(('-game', '-login', '-web')) and 'USER' not in bien:
                kiem = 'systemctl cat %s >/dev/null 2>&1' % moi
                dem['3'] += 1
            else:
                kiem = 'id -u %s >/dev/null 2>&1' % moi
                dem['3'] += 1
            return '%s%s=%s%s%s; %s || %s=%s%s%s  # %s: bo sau 1 ban%s' % (
                ind, bien, dau, moi, cuoi, kiem, bien, dau, gt, cuoi, DAU, nl)
    return None


def la_nhi_phan(b):
    return b'\x00' in b[:8192]


def doc_van_ban(p):
    b = p.read_bytes()
    if la_nhi_phan(b):
        return None, None
    try:
        return b.decode('utf-8'), 'utf-8'
    except UnicodeDecodeError:
        return b.decode('latin-1'), 'latin-1'


def ds_tep(repo, goc):
    ra = subprocess.run(['git', '-C', str(goc), 'ls-files', '-z', '--cached', '--others', '--exclude-standard'],
                        capture_output=True, check=True).stdout.decode('utf-8', 'replace').split('\0')
    tep = sorted({t for t in ra if t})
    bo = [re.compile(r_) for r_ in BO_QUA.get(repo, [])]
    tep = [t for t in tep if not any(b.search(t) for b in bo) and (goc / t).is_file() and not (goc / t).is_symlink()]
    for t in TEP_NGOAI_GIT.get(repo, []):
        if (goc / t).is_file():
            tep.append(t)
    return tep


def xu_ly_noi_dung(repo, rel, s, da_doi_repo, dem):
    duoi = os.path.splitext(rel)[1].lower()
    ten = os.path.basename(rel)
    la_gitignore = ten == '.gitignore'
    dong = s.splitlines(keepends=True)
    ra = []
    bo_dong_sau = False
    doi = []
    for i, d in enumerate(dong):
        if bo_dong_sau:
            bo_dong_sau = False
            ra.append(d)
            continue
        if DAU_SAU in d:
            bo_dong_sau = True
            ra.append(d)
            continue
        if DAU in d:
            ra.append(d)
            continue
        moi = luat_ca_dong(d, duoi, repo, da_doi_repo, dem)
        if moi is None:
            moi = doi_chuoi(d, repo, da_doi_repo, dem)
            # .gitignore: mau ten cu GIU LAI 1 ban (tep cu con tren VPS/may dev, khong thi git bao "dirty")
            if la_gitignore and moi != d and not d.lstrip().startswith('#') and d.strip():
                nl = d[len(d.rstrip('\r\n')):] or '\n'
                ra.append(moi if moi.endswith(('\n', '\r')) else moi + nl)
                ra.append('# %s: mau ten cu, bo sau 1 ban%s' % (DAU_SAU, nl))
                ra.append(d if d.endswith(('\n', '\r')) else d + nl)
                doi.append((i + 1, d.rstrip('\r\n'), moi.rstrip('\r\n') + '  (+ giu dong cu)'))
                continue
        if moi != d:
            doi.append((i + 1, d.rstrip('\r\n'), moi.rstrip('\r\n')))
        ra.append(moi)
    return ''.join(ra), doi


# --------------------------------------------------------------------------------------------
# 4. MIENG VA: chen ma tuong thich (moi cai co the danh dau rieng de khong chen 2 lan)
# --------------------------------------------------------------------------------------------
def va_config_ts(s):
    cu = ("function env(ten: string): string | undefined {\n"
          "  const v = process.env[ten]\n"
          "  return v === undefined ? undefined : v\n"
          "}\n")
    moi = ("/**\n"
           " * Doc bien HTBB_*; chua co thi doc ten cu cung hau to (tien to truoc khi doi ten HTBB),\n"
           " * de may dang chay voi file env dat ten theo tien to cu khong hong. Giu MOT ban phat hanh roi bo.\n"
           " */\n"
           "function env(ten: string): string | undefined {\n"
           "  const v = process.env[ten]\n"
           "  if (v !== undefined) return v\n"
           "  return ten.startsWith('HTBB_') ? process.env['KPAH_' + ten.slice(5)] : undefined // %s\n"
           "}\n") % DAU
    if "process.env['KPAH_' + ten.slice(5)]" in s:
        return s, False
    if cu not in s:
        raise SystemExit('va config.ts: khong thay ham env() mau - sua tay')
    return s.replace(cu, moi), True


def va_deploy_web(s):
    neo = '        [ -z "$k" ] && continue\n'
    chen = ('        case "$k" in KPAH_*) k="HTBB_${k#KPAH_}" ;; esac  # %s: nhan ca ten bien cu, ghi ten moi\n' % DAU)
    if 'k="HTBB_${k#KPAH_}"' in s:
        return s, False
    if neo not in s:
        raise SystemExit('va deploy-web.sh: khong thay neo')
    return s.replace(neo, neo + chen, 1), True


DAU_CSDL = '/etc/htbb/da-chuyen-csdl'


def va_sinh_server_ini(s):
    neo = 'if ! mysql --defaults-file="${MOI}.cnf" -N -e "select 1" "$(lay db.name)" >/dev/null 2>&1; then\n'
    chen = (
        '# Doi ten CSDL kpah_db -> htbb_db (vps/CHUYEN_TEN_HTBB.md). Chua chuyen (chua co %s) thi  # %s\n'
        '# van chay kpah_db cu - KHONG tu nhay sang htbb_db chi vi no da ton tai (ban sao co the cu).  # %s\n'
        'if [ ! -e %s ] && mysql --defaults-file="${MOI}.cnf" -N -e "select 1" kpah_db >/dev/null 2>&1; then  # %s\n'
        '    echo "[CANH BAO] chua chuyen CSDL sang htbb_db - tam dung CSDL cu (xem vps/CHUYEN_TEN_HTBB.md)"  # %s\n'
        '    sed -i -E \'s/^(db\\.name[A-Za-z0-9]*)=htbb_db$/\\1=kpah_db/\' "$MOI"  # %s\n'
        'fi\n') % (DAU_CSDL, DAU, DAU, DAU_CSDL, DAU, DAU, DAU)
    if DAU_CSDL in s:
        return s, False
    if neo not in s:
        raise SystemExit('va sinh-server-ini.sh: khong thay neo')
    return s.replace(neo, chen + neo, 1), True


def va_backup_db(s):
    cu = 'for DB in htbb_db account; do\n'
    moi = ('CSDL_GAME=htbb_db; [ -e %s ] || CSDL_GAME=kpah_db  # %s: bo sau 1 ban\n'
           'for DB in "$CSDL_GAME" account; do\n') % (DAU_CSDL, DAU)
    if 'CSDL_GAME=' in s:
        return s, False
    if cu not in s:
        raise SystemExit('va backup_db.sh: khong thay vong for')
    return s.replace(cu, moi, 1), True


def va_unit(ten_cu):
    def f(s):
        if 'Alias=%s' % ten_cu in s:
            return s, False
        neo = 'WantedBy=multi-user.target\n'
        if neo not in s:
            raise SystemExit('va unit: khong thay [Install]')
        chen = ('# %s: `systemctl ... %s` cu van chay sau khi `systemctl enable` (bo sau 1 ban)\n'
                'Alias=%s\n') % (DAU_SAU, ten_cu.replace('.service', ''), ten_cu)
        return s.replace(neo, neo + chen, 1), True
    return f


def va_unit_web(s):
    s, a = va_unit('kpah-web.service')(s)
    cu = 'EnvironmentFile=/etc/htbb-web.env\n'
    moi = ('# %s: may chua chuyen van con /etc/kpah-web.env; file moi (doc sau) de len.\n'
           'EnvironmentFile=-/etc/kpah-web.env\n'
           'EnvironmentFile=-/etc/htbb-web.env\n') % DAU_SAU
    if 'EnvironmentFile=-/etc/kpah-web.env' in s:
        return s, a
    if cu not in s:
        raise SystemExit('va htbb-web.service: khong thay EnvironmentFile')
    return s.replace(cu, moi, 1), True


def va_env_example(s):
    dong = '# Ten cu KPAH_* van duoc doc neu chua co HTBB_* (giu 1 ban phat hanh). %s\n' % DAU
    if dong in s:
        return s, False
    return dong + s, True


def va_android_key(s):
    cu = 'KEY_DIR=${KEY_DIR:-"$HOME/Desktop/HTBB-All/_tmp_build/android-key"}\n'
    moi = ('KEY_DIR=${KEY_DIR:-"$HOME/Desktop/HTBB-All/_tmp_build/android-key"}\n'
           '[ -d "$KEY_DIR" ] || [ ! -d "$HOME/Desktop/KPAH-All/_tmp_build/android-key" ] || KEY_DIR="$HOME/Desktop/KPAH-All/_tmp_build/android-key"  # %s: thu muc cu tren may chu repo\n') % DAU
    if 'Desktop/KPAH-All/_tmp_build' in s:
        return s, False
    if cu not in s:
        raise SystemExit('va android/build.sh: khong thay KEY_DIR')
    return s.replace(cu, moi, 1), True


def va_chay_novadb(s):
    neo = 'if ! m -N -e "show databases" | grep -qx htbb_db; then\n'
    chen = ('# Doi ten kpah_db -> htbb_db: may chua chay tools/doi_ten/chuyen_csdl.sh thi DUNG LAI - nap sql day du  # %s\n'
            '# ben duoi se ghi de ca bang cua CSDL account dang co.  # %s\n'
            'if m -N -e "show databases" | grep -qx kpah_db && ! m -N -e "show databases" | grep -qx htbb_db; then  # %s\n'
            '    echo "Con CSDL ten cu kpah_db - chay truoc: sh tools/doi_ten/chuyen_csdl.sh --apply"; dung; exit 1  # %s\n'
            'fi\n') % (DAU, DAU, DAU, DAU)
    if 'grep -qx kpah_db' in s:
        return s, False
    if neo not in s:
        raise SystemExit('va chay_tren_novadb.sh: khong thay neo')
    return s.replace(neo, chen + neo, 1), True


# (repo, duong dan SAU khi doi ten) -> ham va. Chay SAU buoc doi chuoi.
VA = [
    ('HTBB-Web', 'server/utils/config.ts', va_config_ts),
    ('HTBB-Web', 'deploy/deploy-web.sh', va_deploy_web),
    ('HTBB-Web', 'deploy/htbb-web.service', va_unit_web),
    ('HTBB-Web', '.env.example', va_env_example),
    ('HTBB', 'vps/sinh-server-ini.sh', va_sinh_server_ini),
    ('HTBB', 'vps/backup_db.sh', va_backup_db),
    ('HTBB', 'tools/pokemon/chay_tren_novadb.sh', va_chay_novadb),
    ('HTBB', 'vps/htbb-game.service', va_unit('kpah-game.service')),
    ('HTBB', 'vps/htbb-login.service', va_unit('kpah-login.service')),
    ('htbb-client', 'android/build.sh', va_android_key),
]

# --------------------------------------------------------------------------------------------
# 5. DOI TEN TEP (git mv). (repo, cu, moi)
# --------------------------------------------------------------------------------------------
DOI_TEN = [
    ('HTBB', 'KPAH2_patched.jar', 'htbb-server.jar'),
    ('HTBB', 'lib/NQSH_5h.jar', 'lib/htbb-core.jar'),
    ('HTBB', 'tools/giai-ma-nqsh.bat', 'tools/giai-ma-core.bat'),
    ('HTBB', 'vps/kpah-game.service', 'vps/htbb-game.service'),
    ('HTBB', 'vps/kpah-login.service', 'vps/htbb-login.service'),
    ('HTBB', 'vps/kpah-logrotate.conf', 'vps/htbb-logrotate.conf'),
    ('HTBB', 'vps/kpah-mariadb.cnf', 'vps/htbb-mariadb.cnf'),
    ('htbb-client', 'phat-hanh/kpah-tai.conf', 'phat-hanh/htbb-tai.conf'),
    ('HTBB-Web', 'deploy/apache-kpah-web.conf', 'deploy/apache-htbb-web.conf'),
    ('HTBB-Web', 'deploy/kpah-web.service', 'deploy/htbb-web.service'),
]


# Symlink TEN CU -> ten moi, dua vao git (giu 1 ban phat hanh). Nho vay:
#  * deploy-auto.sh CU (dang chay luc git pull dau tien, chua co ma moi) van `unzip -t KPAH2_patched.jar` duoc;
#  * unit systemd CU (ExecStart ... -jar KPAH2_patched.jar) bat lai duoc o lan bao tri ke tiep;
#  * lenh/script ngoai repo con ghi lib/NQSH_5h.jar (chay_lap.sh, may dev khac) khong gay.
SYMLINK_CU = [
    ('HTBB', 'KPAH2_patched.jar', 'htbb-server.jar'),
    ('HTBB', 'lib/NQSH_5h.jar', 'htbb-core.jar'),
]


def doi_manifest_jar(p, apply):
    """Class-Path trong MANIFEST: lib/NQSH_5h.jar -> lib/htbb-core.jar. Giu nguyen moi entry khac."""
    with zipfile.ZipFile(p) as z:
        mf = z.read('META-INF/MANIFEST.MF').decode('utf-8')
    tron = re.sub(r'\r?\n ', '', mf)        # bo dong noi tiep
    if 'NQSH_5h.jar' not in tron:
        return False
    if not apply:
        return True
    moi_dong = []
    for d in re.split(r'\r?\n', tron.replace('NQSH_5h.jar', 'htbb-core.jar')):
        if not d:
            continue
        b = d.encode('utf-8')
        manh = [b[:72]]
        b = b[72:]
        while b:
            manh.append(b' ' + b[:71])
            b = b[71:]
        moi_dong.extend(manh)
    mf_moi = b'\r\n'.join(moi_dong) + b'\r\n\r\n'
    tam = p.with_suffix('.jar.tam')
    with zipfile.ZipFile(p) as z, zipfile.ZipFile(tam, 'w') as ra:
        for info in z.infolist():
            du_lieu = mf_moi if info.filename == 'META-INF/MANIFEST.MF' else z.read(info.filename)
            ra.writestr(info, du_lieu, compress_type=info.compress_type)
    with zipfile.ZipFile(tam) as z:
        if z.testzip() is not None or b'lib/htbb-core.jar' not in re.sub(rb'\r?\n ', b'', z.read('META-INF/MANIFEST.MF')):
            tam.unlink()
            raise SystemExit('jar moi hong - khong thay')
    os.replace(tam, p)
    return True


# --------------------------------------------------------------------------------------------
def quet_con_lai(repo, goc):
    """Liet ke moi dong con kpah/nqsh/khí phách va phan loai: co dau tuong thich / bao ve / SOT."""
    re_tim = re.compile(r'kpah|kh[ií] ph[aá]ch|khiphach|nqsh', re.I)
    kq = collections.Counter()
    sot = []
    for rel in ds_tep(repo, goc):
        s, _ = doc_van_ban(goc / rel)
        if s is None:
            continue
        dong = s.splitlines()
        sau = False
        for i, d in enumerate(dong):
            truoc_sau = sau
            sau = DAU_SAU in d
            if not re_tim.search(d):
                continue
            if DAU in d or truoc_sau:
                kq['tuong-thich (co dau %s)' % DAU] += 1
                continue
            con = d
            for r_, ly_do in BAO_VE + [(r_, 'repo GitHub chua doi ten') for r_, _ in REPO_GITHUB]:
                if re.search(r_, con):
                    kq[ly_do] += len(re.findall(r_, con))
                    con = re.sub(r_, '', con)
            if re.search(r'kpah|kh[ií] ph[aá]ch|khiphach', con, re.I):
                sot.append('%s/%s:%d: %s' % (repo, rel, i + 1, d.strip()[:160]))
            elif re.search('nqsh', con, re.I):
                kq['NQSH con lai (kenh NQSH-H2 cu / module android/app - xem ke hoach)'] += 1
    for rel in ds_tep(repo, goc):
        if re.search(r'kpah', rel, re.I):
            sot.append('%s/%s: (ten tep)' % (repo, rel))
    for r_, cu, dich in SYMLINK_CU:
        if r_ == repo and (goc / cu).is_symlink():
            kq['symlink tuong thich: %s -> %s' % (cu, dich)] += 1
    return kq, sot


class SaoLuu:
    """Luu ban goc moi tep truoc khi ghi + nhat ky doi ten/symlink -> GOC/doi_ten_sao_luu/truoc_<ts>.tar."""

    def __init__(self, bat):
        self.bat = bat
        self.da = set()
        self.nk = {'doi_ten': [], 'symlink': []}
        self.tar = None
        if bat:
            d = GOC / 'doi_ten_sao_luu'
            d.mkdir(exist_ok=True)
            self.duong = d / ('truoc_%s.tar' % time.strftime('%Y%m%d_%H%M%S'))
            self.tar = tarfile.open(self.duong, 'w')

    def tep(self, repo, rel):
        if not self.bat or (repo, rel) in self.da:
            return
        self.da.add((repo, rel))
        b = (REPO[repo] / rel).read_bytes()
        ti = tarfile.TarInfo('noi_dung/%s/%s' % (repo, rel))
        ti.size = len(b)
        ti.mtime = int(time.time())
        self.tar.addfile(ti, io.BytesIO(b))

    def dong(self):
        if not self.bat:
            return
        b = json.dumps(self.nk, ensure_ascii=False, indent=1).encode('utf-8')
        ti = tarfile.TarInfo('nhat_ky.json')
        ti.size = len(b)
        self.tar.addfile(ti, io.BytesIO(b))
        self.tar.close()
        print('\nSao luu ban goc: %s  (quay lai: --quay-lai %s)' % (self.duong, self.duong))


def quay_lai(duong):
    with tarfile.open(duong) as t:
        nk = json.loads(t.extractfile('nhat_ky.json').read().decode('utf-8'))
        for m in t.getmembers():
            if m.name.startswith('noi_dung/'):
                repo, rel = m.name[len('noi_dung/'):].split('/', 1)
                p = REPO[repo] / rel
                p.write_bytes(t.extractfile(m).read())
                print('  tra lai %s/%s' % (repo, rel))
    for repo, cu in reversed(nk['symlink']):
        subprocess.run(['git', '-C', str(REPO[repo]), 'rm', '-q', '--cached', cu], check=False)
        (REPO[repo] / cu).unlink(missing_ok=True)
        print('  xoa symlink %s/%s' % (repo, cu))
    for repo, cu, moi in reversed(nk['doi_ten']):
        subprocess.run(['git', '-C', str(REPO[repo]), 'mv', moi, cu], check=True)
        print('  git mv %s/%s -> %s' % (repo, moi, cu))
    print('Xong quay lai tu %s' % duong)


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument('--apply', action='store_true', help='sua that (mac dinh chi chay thu)')
    ap.add_argument('--chi-tiet', action='store_true', help='in tung dong se doi')
    ap.add_argument('--da-doi-repo', action='store_true', help='doi ca ten repo GitHub (sau gh repo rename)')
    ap.add_argument('--kiem', action='store_true', help='chi quet cho con sot')
    ap.add_argument('--quay-lai', metavar='TAR', help='tra lai tu ban sao luu cua mot lan --apply')
    a = ap.parse_args()
    if a.quay_lai:
        quay_lai(a.quay_lai)
        return

    if a.kiem:
        tong_sot = []
        tong = collections.Counter()
        for repo, goc in REPO.items():
            kq, sot = quet_con_lai(repo, goc)
            tong.update(kq)
            tong_sot += sot
        print('== Con lai CO CHU Y (khong phai sot):')
        for k, v in tong.most_common():
            print('  %5d  %s' % (v, k))
        print('== SOT (%d):' % len(tong_sot))
        for s in tong_sot:
            print('  ' + s)
        sys.exit(1 if tong_sot else 0)

    sl = SaoLuu(a.apply)
    che_do = 'SUA THAT' if a.apply else 'CHAY THU (khong ghi gi)'
    print('== doi_ten_kpah.py - %s - goc %s' % (che_do, GOC))
    dem = collections.Counter()
    tep_doi = collections.Counter()
    dem_tep_nhom = collections.defaultdict(set)

    # 1) doi ten tep truoc (de mieng va tim dung duong dan moi)
    print('\n-- Doi ten tep (git mv):')
    for repo, cu, moi in DOI_TEN:
        goc = REPO[repo]
        if (goc / moi).exists() and (not (goc / cu).exists() or (goc / cu).is_symlink()):
            print('  [da xong] %s/%s' % (repo, moi))
            continue
        if not (goc / cu).exists():
            print('  [KHONG THAY] %s/%s' % (repo, cu))
            continue
        print('  %s: %s -> %s' % (repo, cu, moi))
        if a.apply:
            subprocess.run(['git', '-C', str(goc), 'mv', cu, moi], check=True)
            sl.nk['doi_ten'].append([repo, cu, moi])
    for repo, cu, dich in SYMLINK_CU:
        p = REPO[repo] / cu
        if p.is_symlink():
            print('  [da co] symlink tuong thich %s/%s -> %s' % (repo, cu, dich))
            continue
        print('  symlink tuong thich (1 ban) %s/%s -> %s' % (repo, cu, dich))
        if a.apply:
            os.symlink(dich, p)
            subprocess.run(['git', '-C', str(REPO[repo]), 'add', cu], check=True)
            sl.nk['symlink'].append([repo, cu])
    jar = REPO['HTBB'] / ('htbb-server.jar' if a.apply or (REPO['HTBB'] / 'htbb-server.jar').exists() else 'KPAH2_patched.jar')
    if jar.exists():
        if a.apply and doi_manifest_jar(jar, False):
            sl.tep('HTBB', jar.name)
        can = doi_manifest_jar(jar, a.apply)
        print('  MANIFEST %s: %s' % (jar.name, ('Class-Path lib/NQSH_5h.jar -> lib/htbb-core.jar' if can else 'da dung')))

    # 2) doi noi dung
    print('\n-- Doi noi dung:')
    for repo, goc in REPO.items():
        for rel in ds_tep(repo, goc):
            p = goc / rel
            s, mh = doc_van_ban(p)
            if s is None:
                continue
            truoc = collections.Counter(dem)
            moi, doi = xu_ly_noi_dung(repo, rel, s, a.da_doi_repo, dem)
            if moi != s:
                tep_doi[repo] += 1
                for k in dem:
                    if dem[k] != truoc.get(k, 0):
                        dem_tep_nhom[k].add('%s/%s' % (repo, rel))
                if a.chi_tiet:
                    print('  %s/%s' % (repo, rel))
                    for so, c, m in doi:
                        print('    %d: - %s\n    %d: + %s' % (so, c[:200], so, m[:200]))
                if a.apply:
                    sl.tep(repo, rel)
                    p.write_bytes(moi.encode(mh))

    # 3) mieng va
    print('\n-- Mieng va tuong thich:')
    for repo, rel, f in VA:
        goc = REPO[repo]
        p = goc / rel
        if not p.exists():
            # chay thu: tep chua doi ten / noi dung chua doi -> mo phong tren ban da doi
            cu = [c for r_, c, m in DOI_TEN if r_ == repo and m == rel]
            p = goc / cu[0] if cu else p
        if not p.exists():
            print('  [KHONG THAY] %s/%s' % (repo, rel))
            continue
        s, mh = doc_van_ban(p)
        if not a.apply:
            s, _ = xu_ly_noi_dung(repo, rel, s, a.da_doi_repo, collections.Counter())
        s2, co = f(s)
        print('  %s %s/%s' % ('chen' if co else '[da co]', repo, rel))
        if a.apply and co:
            sl.tep(repo, rel)
            p.write_bytes(s2.encode(mh))
            tep_doi[repo] += 0

    ten_nhom = {'1': 'CSDL kpah_db', '2': 'bien moi truong KPAH_*', '3': 'duong dan /opt, systemd, apache, user',
                '4': 'jar (KPAH2_patched.jar, NQSH_5h.jar)', '5': 'repo GitHub', '6': 'ma/chu thich/tai lieu (con lai)',
                '7': 'chu nguoi choi/quan tri thay'}
    print('\n-- Tong ket theo nhom (so cho doi / so tep):')
    for k in sorted(ten_nhom):
        print('  (%s) %-42s %6d  / %4d tep' % (k, ten_nhom[k], dem.get(k, 0), len(dem_tep_nhom.get(k, ()))))
    for repo in REPO:
        print('  %-12s %4d tep doi noi dung' % (repo, tep_doi[repo]))
    sl.dong()
    if a.apply:
        print('\nXong. Kiem lai: python3 -I %s --kiem' % Path(__file__).relative_to(GOC))
    else:
        print('\n(Chay thu - chua ghi gi. Them --apply de sua that.)')


if __name__ == '__main__':
    main()
