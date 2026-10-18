#!/usr/bin/env python3
"""Sinh danh muc cau hinh trung tam tu tools/cauhinh/danh_muc.json (NGUON DUY NHAT).

Ra 4 tep (KHONG sua tay, chay lai script nay sau khi sua danh_muc.json):
  1. server/src/com/htbb/cauhinh/DanhMucSinh.java - may chu game (kiem kieu/khoang, ap dung ngay/khoi dong lai)
  2. sql/pokemon_cau_hinh_trung_tam.sql            - bang + danh muc (idempotent, chay lai bao nhieu lan cung duoc)
  3. web/shared/utils/cauHinhTrungTam.gen.ts       - ban sao cho web
  4. docs/cau-hinh.md                              - CHI phan bang giua hai dong danh dau MOC_DAU / MOC_CUOI;
                                                     phan con lai cua tai lieu viet tay, script khong dung toi

Chay o goc monorepo:     python3 tools/cauhinh/sinh_cau_hinh.py
Kiem dong bo (CI/test):  python3 tools/cauhinh/sinh_cau_hinh.py --kiem   (ma thoat 1 neu tep sinh lech)
"""
import json
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
NGUON = os.path.join(GOC, 'tools', 'cauhinh', 'danh_muc.json')
RA_JAVA = os.path.join(GOC, 'server', 'src', 'com', 'htbb', 'cauhinh', 'DanhMucSinh.java')
RA_SQL = os.path.join(GOC, 'sql', 'pokemon_cau_hinh_trung_tam.sql')
RA_TS = os.path.join(GOC, 'web', 'shared', 'utils', 'cauHinhTrungTam.gen.ts')
RA_MD = os.path.join(GOC, 'docs', 'cau-hinh.md')
MOC_DAU = '<!-- SINH TU tools/cauhinh/danh_muc.json: BAT DAU (khong sua tay, chay python3 tools/cauhinh/sinh_cau_hinh.py) -->'
MOC_CUOI = '<!-- SINH TU tools/cauhinh/danh_muc.json: KET THUC -->'

KIEU = {'co', 'so', 'chon', 'chuoi', 'ds_chuoi', 'ds_so', 'ngay', 'ngay_gio', 'nhip'}
AP_DUNG = {'ngay', 'khoi_dong_lai'}
BANG = {'cau_hinh', 'nap_cau_hinh', 'env'}
DUNG_O = {'server', 'web', 'ca_hai'}


def doc():
    with open(NGUON, encoding='utf-8') as f:
        d = json.load(f)
    nhom = {n['ma'] for n in d['nhom']}
    thay = set()
    for k in d['khoa']:
        ten = k['khoa']
        loi = lambda m: sys.exit('danh_muc.json: %s: %s' % (ten, m))
        if ten in thay:
            loi('trung khoa')
        thay.add(ten)
        if not re.match(r'^[A-Za-z0-9_.]{1,64}$', ten):
            loi('ten khoa sai (chi chu, so, _ . toi da 64)')
        if k['nhom'] not in nhom:
            loi('nhom la ' + k['nhom'])
        if k['kieu'] not in KIEU:
            loi('kieu la ' + k['kieu'])
        if k.get('apDung', 'ngay') not in AP_DUNG:
            loi('apDung la')
        if k.get('bang', 'cau_hinh') not in BANG:
            loi('bang la')
        if k.get('dungO', 'server') not in DUNG_O:
            loi('dungO la')
        if k['kieu'] == 'chon' and not k.get('luaChon'):
            loi('kieu chon can luaChon')
        if 'mau' in k:
            re.compile(k['mau'])
        if len(k.get('macDinh', '')) > 255:
            loi('macDinh qua 255')
    return d


def j(s):
    """Chuoi Java/TS (JSON escape la tap con hop le cua ca hai)."""
    return json.dumps(s, ensure_ascii=False)


def sinh_java(d):
    o = []
    o.append('package com.htbb.cauhinh;')
    o.append('')
    o.append('/**')
    o.append(' * SINH TU tools/cauhinh/danh_muc.json boi tools/cauhinh/sinh_cau_hinh.py - KHONG SUA TAY.')
    o.append(' * Danh muc moi khoa cau hinh trung tam (xem {@link CauHinhTrungTam}).')
    o.append(' */')
    o.append('final class DanhMucSinh {')
    o.append('')
    o.append('    private DanhMucSinh() {')
    o.append('    }')
    o.append('')
    o.append('    /** {ma, ten} theo thu tu hien thi. */')
    o.append('    static final String[][] NHOM = {')
    for n in d['nhom']:
        o.append('        {%s, %s},' % (j(n['ma']), j(n['ten'])))
    o.append('    };')
    o.append('')
    o.append('    static DinhNghiaKhoa[] tatCa() {')
    o.append('        return new DinhNghiaKhoa[]{')
    for k in d['khoa']:
        dong = 'new DinhNghiaKhoa(%s, %s, %s, %s, %s, %s)' % (
            j(k['khoa']), j(k['nhom']), j(k['kieu']), j(k.get('macDinh', '')),
            'true' if k.get('apDung', 'ngay') == 'ngay' else 'false', j(k.get('bang', 'cau_hinh')))
        them = []
        them.append('.nhan(%s, %s)' % (j(k['ten']), j(k.get('moTa', ''))))
        if 'min' in k or 'max' in k:
            them.append('.khoang(%s, %s)' % (
                ('%dL' % k['min']) if 'min' in k else 'Long.MIN_VALUE',
                ('%dL' % k['max']) if 'max' in k else 'Long.MAX_VALUE'))
        if k.get('buoc'):
            them.append('.buoc(%dL)' % k['buoc'])
        if k.get('luaChon'):
            them.append('.luaChon(new String[]{%s})' % ', '.join(j(x[0]) for x in k['luaChon']))
        if k.get('soPhanTu'):
            them.append('.soPhanTu(%d)' % k['soPhanTu'])
        if k.get('mau'):
            them.append('.mau(%s)' % j(k['mau']))
        if k.get('doDai'):
            them.append('.doDai(%d)' % k['doDai'])
        if k.get('hoa'):
            them.append('.hoa()')
        if k.get('nhay'):
            them.append('.nhay()')
        if k.get('khoaProd'):
            them.append('.khoaProd()')
        if k.get('chiDoc'):
            them.append('.chiDoc()')
        o.append('            ' + dong)
        for t in them:
            o.append('                    ' + t)
        o[-1] += ','
    o.append('        };')
    o.append('    }')
    o.append('}')
    return '\n'.join(o) + '\n'


def sq(s):
    return "'" + str(s).replace('\\', '\\\\').replace("'", "''") + "'"


def sinh_sql(d):
    o = []
    o.append('-- CAU HINH TRUNG TAM (docs/cau-hinh.md) - SINH TU tools/cauhinh/danh_muc.json boi')
    o.append('-- tools/cauhinh/sinh_cau_hinh.py, KHONG SUA TAY. Idempotent: chay lai bao nhieu lan cung duoc.')
    o.append('-- KHONG ghi gia tri vao cau_hinh: thieu dong = may chu dung server.ini roi mac dinh (CSDL > ini > mac dinh).')
    o.append('-- May chu game cung tu tao cac bang nay luc khoi dong (com.htbb.cauhinh.CauHinhTrungTam).')
    o.append('')
    o.append('CREATE TABLE IF NOT EXISTS `cau_hinh` (')
    o.append('  `khoa` varchar(64) NOT NULL,')
    o.append("  `gia_tri` varchar(255) NOT NULL DEFAULT '',")
    o.append('  `sua_boi` varchar(50) DEFAULT NULL,')
    o.append('  `sua_luc` datetime DEFAULT NULL,')
    o.append('  PRIMARY KEY (`khoa`)')
    o.append(') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;')
    o.append('')
    for ddl in DDL:
        o.append(ddl + ';')
        o.append('')
    o.append("INSERT IGNORE INTO `cau_hinh_phien_ban` (`id`, `phien_ban`) VALUES (1, 1);")
    o.append("INSERT IGNORE INTO `cau_hinh_khoi_dong_lai` (`id`, `trang_thai`) VALUES (1, '');")
    o.append('')
    o.append('-- Danh muc (chi de tra cuu / cong cu ngoai; may chu va web dung ban sinh trong ma nguon).')
    o.append('DELETE FROM `cau_hinh_danh_muc`;')
    for k in d['khoa']:
        gioi = {}
        for f in ('min', 'max', 'buoc', 'soPhanTu', 'mau', 'doDai'):
            if f in k:
                gioi[f] = k[f]
        if k.get('luaChon'):
            gioi['luaChon'] = [x[0] for x in k['luaChon']]
        o.append('INSERT INTO `cau_hinh_danh_muc` (`khoa`, `nhom`, `ten`, `mo_ta`, `kieu`, `mac_dinh`, `gioi_han`, `ap_dung`,'
                 ' `nhay`, `khoa_prod`, `chi_doc`, `bang`, `dung_o`, `nguon`) VALUES (%s);' % ', '.join([
                     sq(k['khoa']), sq(k['nhom']), sq(k['ten']), sq(k.get('moTa', '')[:500]), sq(k['kieu']),
                     sq(k.get('macDinh', '')), sq(json.dumps(gioi, ensure_ascii=False)[:500]), sq(k.get('apDung', 'ngay')),
                     '1' if k.get('nhay') else '0', '1' if k.get('khoaProd') else '0', '1' if k.get('chiDoc') else '0',
                     sq(k.get('bang', 'cau_hinh')), sq(k.get('dungO', 'server')), sq(k.get('nguon', '')[:120])]))
    return '\n'.join(o) + '\n'


# Cung DDL voi CauHinhTrungTam.taoBang() (test/KiemCauHinhTrungTam doi chieu ten bang).
DDL = [
    'CREATE TABLE IF NOT EXISTS `cau_hinh_phien_ban` (`id` TINYINT NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0,'
    ' `sua_luc` DATETIME NULL, `sua_boi` VARCHAR(50) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
    'CREATE TABLE IF NOT EXISTS `cau_hinh_lich_su` (`id` BIGINT NOT NULL AUTO_INCREMENT, `khoa` VARCHAR(64) NOT NULL,'
    " `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `gia_tri_cu` VARCHAR(255) NULL, `gia_tri_moi` VARCHAR(255) NULL,"
    " `nguoi_sua` VARCHAR(50) NULL, `nguon` VARCHAR(20) NOT NULL DEFAULT '', `ly_do` VARCHAR(255) NOT NULL DEFAULT '',"
    ' `phien_ban` BIGINT NOT NULL DEFAULT 0, `luc` DATETIME NOT NULL, PRIMARY KEY (`id`), KEY `idx_chls_khoa` (`khoa`),'
    ' KEY `idx_chls_luc` (`luc`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
    'CREATE TABLE IF NOT EXISTS `cau_hinh_ap_dung` (`may_chu` VARCHAR(40) NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0,'
    ' `ap_dung_luc` DATETIME NULL, `khoi_dong_luc` DATETIME NULL, `nhip_luc` DATETIME NULL, `so_khoa` INT NOT NULL DEFAULT 0,'
    ' `so_loi` INT NOT NULL DEFAULT 0, `loi` TEXT NULL, `cho_khoi_dong_lai` TEXT NULL, `thay_doi_cuoi` TEXT NULL,'
    ' PRIMARY KEY (`may_chu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
    'CREATE TABLE IF NOT EXISTS `cau_hinh_hieu_luc` (`may_chu` VARCHAR(40) NOT NULL, `khoa` VARCHAR(64) NOT NULL,'
    " `gia_tri` VARCHAR(255) NULL, `nguon` VARCHAR(12) NOT NULL DEFAULT '', `cho_khoi_dong_lai` VARCHAR(255) NULL,"
    ' `loi` VARCHAR(255) NULL, `cap_nhat_luc` DATETIME NULL, PRIMARY KEY (`may_chu`, `khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
    "CREATE TABLE IF NOT EXISTS `cau_hinh_khoi_dong_lai` (`id` TINYINT NOT NULL, `trang_thai` VARCHAR(12) NOT NULL DEFAULT '',"
    " `giay` INT NOT NULL DEFAULT 60, `ly_do` VARCHAR(200) NOT NULL DEFAULT '', `yeu_cau_boi` VARCHAR(50) NULL,"
    ' `yeu_cau_luc` DATETIME NULL, `bat_dau_luc` DATETIME NULL, `thoat_luc` DATETIME NULL, `xong_luc` DATETIME NULL,'
    ' `may_chu` VARCHAR(40) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
    'CREATE TABLE IF NOT EXISTS `cau_hinh_danh_muc` (`khoa` VARCHAR(64) NOT NULL, `nhom` VARCHAR(20) NOT NULL,'
    " `ten` VARCHAR(160) NOT NULL DEFAULT '', `mo_ta` VARCHAR(500) NOT NULL DEFAULT '', `kieu` VARCHAR(12) NOT NULL,"
    " `mac_dinh` VARCHAR(255) NOT NULL DEFAULT '', `gioi_han` VARCHAR(500) NOT NULL DEFAULT '', `ap_dung` VARCHAR(16) NOT NULL,"
    ' `nhay` TINYINT NOT NULL DEFAULT 0, `khoa_prod` TINYINT NOT NULL DEFAULT 0, `chi_doc` TINYINT NOT NULL DEFAULT 0,'
    " `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `dung_o` VARCHAR(10) NOT NULL DEFAULT 'server',"
    " `nguon` VARCHAR(120) NOT NULL DEFAULT '', PRIMARY KEY (`khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4",
]


def sinh_ts(d):
    o = []
    o.append('/**')
    o.append(' * SINH TU HTBB/tools/cauhinh/danh_muc.json boi HTBB/tools/cauhinh/sinh_cau_hinh.py — KHÔNG SỬA TAY.')
    o.append(' * Bản sao web của danh mục cấu hình trung tâm (máy chủ game: com.htbb.cauhinh.DanhMucSinh).')
    o.append(' * Hàm kiểm / so sánh thuần: shared/utils/cauHinhTrungTam.ts.')
    o.append(' */')
    o.append('')
    o.append("export type KieuTT = 'co' | 'so' | 'chon' | 'chuoi' | 'ds_chuoi' | 'ds_so' | 'ngay' | 'ngay_gio' | 'nhip'")
    o.append("export type BangTT = 'cau_hinh' | 'nap_cau_hinh' | 'env'")
    o.append('')
    o.append('export interface KhoaTT {')
    o.append('  khoa: string')
    o.append('  nhom: string')
    o.append('  ten: string')
    o.append('  moTa: string')
    o.append('  kieu: KieuTT')
    o.append('  macDinh: string')
    o.append('  min?: number')
    o.append('  max?: number')
    o.append('  buoc?: number')
    o.append('  luaChon?: [string, string][]')
    o.append('  soPhanTu?: number')
    o.append('  mau?: string')
    o.append('  doDai?: number')
    o.append('  /** Chuẩn hoá chữ hoa trước khi kiểm (mức nhật ký). */')
    o.append('  hoa?: boolean')
    o.append("  /** 'ngay' = áp dụng ≤ 10 giây; 'khoi_dong_lai' = chỉ đổi khi máy chủ khởi động lại. */")
    o.append("  apDung: 'ngay' | 'khoi_dong_lai'")
    o.append('  nhay?: boolean')
    o.append('  khoaProd?: boolean')
    o.append('  chiDoc?: boolean')
    o.append('  bang: BangTT')
    o.append("  dungO: 'server' | 'web' | 'ca_hai'")
    o.append('  nguon: string')
    o.append('}')
    o.append('')
    o.append('export const NHOM_TT: { ma: string, ten: string, moTa: string }[] = [')
    for n in d['nhom']:
        o.append('  { ma: %s, ten: %s, moTa: %s },' % (j(n['ma']), j(n['ten']), j(n.get('moTa', ''))))
    o.append(']')
    o.append('')
    o.append('export const DANH_MUC_TT: KhoaTT[] = [')
    for k in d['khoa']:
        f = ['khoa: %s' % j(k['khoa']), 'nhom: %s' % j(k['nhom']), 'ten: %s' % j(k['ten']), 'moTa: %s' % j(k.get('moTa', '')),
             'kieu: %s' % j(k['kieu']), 'macDinh: %s' % j(k.get('macDinh', ''))]
        for x in ('min', 'max', 'buoc', 'soPhanTu', 'doDai'):
            if x in k:
                f.append('%s: %d' % (x, k[x]))
        if k.get('luaChon'):
            f.append('luaChon: [%s]' % ', '.join('[%s, %s]' % (j(a), j(b)) for a, b in k['luaChon']))
        if k.get('mau'):
            f.append('mau: %s' % j(k['mau']))
        for x in ('hoa', 'nhay', 'khoaProd', 'chiDoc'):
            if k.get(x):
                f.append('%s: true' % x)
        f.append('apDung: %s' % j(k.get('apDung', 'ngay')))
        f.append('bang: %s' % j(k.get('bang', 'cau_hinh')))
        f.append('dungO: %s' % j(k.get('dungO', 'server')))
        f.append('nguon: %s' % j(k.get('nguon', '')))
        o.append('  { ' + ', '.join(f) + ' },')
    o.append(']')
    return '\n'.join(o) + '\n'


TEN_KIEU = {'co': 'cờ 0/1', 'so': 'số', 'chon': 'lựa chọn', 'chuoi': 'chuỗi', 'ds_chuoi': 'danh sách chuỗi',
            'ds_so': 'danh sách số', 'ngay': 'ngày', 'ngay_gio': 'ngày giờ', 'nhip': 'nhịp số/giây'}


def o_md(s):
    """Mot o bang markdown: bo xuong dong, thoat dau |."""
    return ' '.join(str(s).split()).replace('|', '\\|')


def sinh_md_bang(d):
    """Phan bang cua docs/cau-hinh.md (giua MOC_DAU va MOC_CUOI)."""
    o = [MOC_DAU, '']
    o.append('Tổng cộng **%d khoá** trong **%d nhóm**. Cột *Áp dụng*: **ngay** = máy chủ nhận trong vài giây; '
             '**khởi động lại** = chỉ đổi khi máy chủ khởi động lại. Cột *Ở*: máy chủ game, web hay cả hai.'
             % (len(d['khoa']), len(d['nhom'])))
    o.append('')
    for n in d['nhom']:
        ds = [k for k in d['khoa'] if k['nhom'] == n['ma']]
        if not ds:
            continue
        o.append('### %s (`%s`)' % (n['ten'], n['ma']))
        o.append('')
        if n.get('moTa'):
            o.append(o_md(n['moTa']))
            o.append('')
        o.append('| Khoá | Tên | Kiểu | Mặc định | Giới hạn | Áp dụng | Ở | Mô tả |')
        o.append('|---|---|---|---|---|---|---|---|')
        for k in ds:
            gioi = []
            if 'min' in k or 'max' in k:
                gioi.append('%s..%s' % (k.get('min', ''), k.get('max', '')))
            if k.get('soPhanTu'):
                gioi.append('%d phần tử' % k['soPhanTu'])
            if k.get('luaChon'):
                gioi.append(', '.join('`%s`' % x[0] for x in k['luaChon']))
            if k.get('doDai'):
                gioi.append('≤ %d ký tự' % k['doDai'])
            ghi = []
            if k.get('bang', 'cau_hinh') != 'cau_hinh':
                ghi.append('bảng `%s`' % k['bang'])
            if k.get('khoaProd'):
                ghi.append('máy thật chỉ nhận mặc định')
            if k.get('chiDoc'):
                ghi.append('chỉ đọc')
            mo_ta = o_md(k.get('moTa', ''))
            if ghi:
                mo_ta = (mo_ta + ' (' + '; '.join(ghi) + ')').strip()
            mac_dinh = k.get('macDinh', '')
            o.append('| `%s` | %s | %s | %s | %s | %s | %s | %s |' % (
                k['khoa'], o_md(k['ten']), TEN_KIEU.get(k['kieu'], k['kieu']),
                ('`%s`' % o_md(mac_dinh)) if mac_dinh != '' else '*(trống)*',
                o_md('; '.join(gioi)), 'ngay' if k.get('apDung', 'ngay') == 'ngay' else 'khởi động lại',
                {'server': 'game', 'web': 'web', 'ca_hai': 'cả hai'}[k.get('dungO', 'server')], mo_ta))
        o.append('')
    o.append(MOC_CUOI)
    return '\n'.join(o)


def ghep_md(cu, d):
    """Thay phan giua hai moc trong tai lieu viet tay. Thieu moc -> None."""
    a = cu.find(MOC_DAU)
    b = cu.find(MOC_CUOI)
    if a < 0 or b < a:
        return None
    return cu[:a] + sinh_md_bang(d) + cu[b + len(MOC_CUOI):]


def main():
    if '-h' in sys.argv or '--help' in sys.argv:
        print(__doc__)
        return
    d = doc()
    ra = [(RA_JAVA, sinh_java(d)), (RA_SQL, sinh_sql(d))]
    if os.path.isdir(os.path.dirname(RA_TS)):
        ra.append((RA_TS, sinh_ts(d)))
    if os.path.exists(RA_MD):
        with open(RA_MD, encoding='utf-8') as f:
            md = ghep_md(f.read(), d)
        if md is None:
            sys.exit('docs/cau-hinh.md thieu dong moc:\n  %s\n  %s' % (MOC_DAU, MOC_CUOI))
        ra.append((RA_MD, md))
    kiem = '--kiem' in sys.argv
    lech = []
    for duong, nd in ra:
        cu = None
        if os.path.exists(duong):
            with open(duong, encoding='utf-8') as f:
                cu = f.read()
        if cu == nd:
            continue
        if kiem:
            lech.append(duong)
        else:
            os.makedirs(os.path.dirname(duong), exist_ok=True)
            with open(duong, 'w', encoding='utf-8') as f:
                f.write(nd)
            print('da ghi', os.path.relpath(duong, GOC))
    if lech:
        print('TEP SINH LECH voi danh_muc.json (chay python3 tools/cauhinh/sinh_cau_hinh.py):')
        for x in lech:
            print('  ', x)
        sys.exit(1)
    print('%d khoa, %d nhom' % (len(d['khoa']), len(d['nhom'])))


if __name__ == '__main__':
    main()
