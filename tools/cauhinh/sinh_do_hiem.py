#!/usr/bin/env python3
"""Sinh mau do hiem tu tools/cauhinh/do_hiem.json (NGUON DUY NHAT, de xuat 4 - chu game duyet 19/10/2026).

Ra (KHONG sua tay):
  1. server/src/com/htbb/pokemon/DoHiemSinh.java  - may chu (ten bac, colorName -> bac, danh hieu -> bac, ten / mau bac trang bi)
  2. client_jar/src/game/pokemon/DoHiemSinh.java  - client J2ME / APK / PC (token mau ChuDe.mauHiem / nenHiem / sangHiem, phu kien)
  3. web/shared/utils/doHiem.gen.ts               - web (chip danh hieu, tooltip do)
  4. sql/nang_cap.sql, CHI khoi "-- >>> them_do_hiem.sql" - data_do_hiem, data_do_hiem_vat_pham, data_bac_trang_bi
                                                    + mau data_danh_hieu_do_hiem (idempotent; mot tep SQL, sql/README.md)
  5. tools/pokemon/SinhPhuKien.java               - CHI khoi giua hai dong moc (bang mau phu kien + bac)

Chay:  python3 tools/cauhinh/sinh_do_hiem.py [--kiem]   (--kiem: ma thoat 1 neu lech). Sau khi doi mau: ap khoi SQL vao
CSDL (nang_cap.sql) roi xuat lai sql/htbb_day_du.sql (vps/xuat_csdl_day_du.sh).
"""
import json
import os
import re
import sys

GOC = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
NGUON = os.path.join(GOC, 'tools', 'cauhinh', 'do_hiem.json')
RA_SERVER = os.path.join(GOC, 'server', 'src', 'com', 'htbb', 'pokemon', 'DoHiemSinh.java')
RA_CLIENT = os.path.join(GOC, 'client_jar', 'src', 'game', 'pokemon', 'DoHiemSinh.java')
RA_TS = os.path.join(GOC, 'web', 'shared', 'utils', 'doHiem.gen.ts')
RA_SQL = os.path.join(GOC, 'sql', 'nang_cap.sql')
MOC_SQL = '-- >>> them_do_hiem.sql\n'
RA_PHU_KIEN = os.path.join(GOC, 'tools', 'pokemon', 'SinhPhuKien.java')
MOC_DAU = '    // SINH TU tools/cauhinh/do_hiem.json: BAT DAU (khong sua tay, chay python3 tools/cauhinh/sinh_do_hiem.py)\n'
MOC_CUOI = '    // SINH TU tools/cauhinh/do_hiem.json: KET THUC\n'
HEX = re.compile(r'^#[0-9A-F]{6}$')
MA_BAC = ['thuong', 'hiem', 'quy', 'su_thi', 'huyen_thoai', 'trang_tri']


def doc():
    with open(NGUON, encoding='utf-8') as f:
        d = json.load(f)
    loi = lambda m: sys.exit('do_hiem.json: ' + m)
    bac = d['bac']
    if [b['ma'] for b in bac] != MA_BAC:
        loi('bac phai dung thu tu ' + ', '.join(MA_BAC) + ' (id = vi tri; client cu doc hang phu kien theo so nay)')
    for b in bac:
        for k in ('mau', 'nhat', 'sang'):
            if not HEX.match(b[k]):
                loi('%s.%s khong phai #RRGGBB in hoa' % (b['ma'], k))
        if len(b['phuKien']) != 5 or not all(HEX.match(x) for x in b['phuKien']):
            loi(b['ma'] + '.phuKien can 5 mau #RRGGBB')
    vi = {b['ma']: i for i, b in enumerate(bac)}
    mvp = {}
    for k, v in d['mauVatPham'].items():
        if not re.match(r'^\d{1,2}$', k) or v not in vi:
            loi('mauVatPham %s -> %s' % (k, v))
        mvp[int(k)] = vi[v]
    d['_mvp'] = [mvp.get(i, 0) for i in range(max(mvp) + 1)]
    d['_dh'] = [vi[x] for x in d['danhHieu']]
    bt = []
    for x in d['bacTrangBi']:
        if 'theoHiem' in x:
            bt.append(bac[vi[x['theoHiem']]]['phuKien'][2])
        elif HEX.match(x.get('mau', '')):
            bt.append(x['mau'])
        else:
            loi('bacTrangBi %s thieu mau / theoHiem' % x.get('ten'))
    if len(bt) != 11:
        loi('bacTrangBi can 11 bac')
    d['_bt'] = bt
    return d


def j(s):
    return json.dumps(s, ensure_ascii=False)


def h(c):
    return '0x' + c[1:]


def mang_so(ds, thut='        '):
    return ('\n' + thut).join(', '.join(str(x) for x in ds[i:i + 12]) for i in range(0, len(ds), 12))


def sinh_server(d):
    b = d['bac']
    o = ['package com.htbb.pokemon;', '',
         '/**',
         ' * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.',
         ' * Do hiem chung (6 bac) cho vat pham, danh hieu, phu kien ngoai hinh; ten bac trang bi. Mau RGB cung nguon voi',
         ' * client (game.pokemon.DoHiemSinh) va web (shared/utils/doHiem.gen.ts).',
         ' */',
         'public final class DoHiemSinh {', '',
         '    private DoHiemSinh() {', '    }', '']
    for i, x in enumerate(b):
        o.append('    public static final int %s = %d;' % (x['ma'].upper(), i))
    o.append('    public static final int SO_BAC = %d;' % len(b))
    o.append('')
    o.append('    public static final String[] MA = {%s};' % ', '.join(j(x['ma']) for x in b))
    o.append('    public static final String[] TEN = {%s};' % ', '.join(j(x['ten']) for x in b))
    o.append('    /** Mau chu / vien tren nen sang (0xRRGGBB). */')
    o.append('    public static final int[] MAU = {%s};' % ', '.join(h(x['mau']) for x in b))
    o.append('    /** Vien chay / lap lanh. */')
    o.append('    public static final boolean[] VIEN_DONG = {%s};' % ', '.join('true' if x['vienDong'] else 'false' for x in b))
    o.append('')
    o.append('    /** Item.colorName -> bac (ngoai bang = THUONG). */')
    o.append('    static final int[] BAC_VAT_PHAM = {%s};' % ', '.join(str(x) for x in d['_mvp']))
    o.append('    /** data_danh_hieu.do_hiem -> bac. */')
    o.append('    static final int[] BAC_DANH_HIEU = {%s};' % ', '.join(str(x) for x in d['_dh']))
    o.append('')
    o.append('    /** Ten 11 bac trang bi (NgoaiHinh). */')
    o.append('    public static final String[] TEN_BAC_TRANG_BI = {%s};' % ', '.join(j(x['ten']) for x in d['bacTrangBi']))
    o.append('    public static final int[] MAU_BAC_TRANG_BI = {%s};' % ', '.join(h(x) for x in d['_bt']))
    o.append('')
    o.append('    /** Bac cua vat pham theo colorName. */')
    o.append('    public static int bacVatPham(final int colorName) {')
    o.append('        return colorName >= 0 && colorName < BAC_VAT_PHAM.length ? BAC_VAT_PHAM[colorName] : THUONG;')
    o.append('    }')
    o.append('')
    o.append('    /** Bac cua do hiem danh hieu (0..3). */')
    o.append('    public static int bacDanhHieu(final int doHiem) {')
    o.append('        return doHiem >= 0 && doHiem < BAC_DANH_HIEU.length ? BAC_DANH_HIEU[doHiem] : THUONG;')
    o.append('    }')
    o.append('')
    o.append('    public static String ten(final int bac) {')
    o.append('        return bac >= 0 && bac < TEN.length ? TEN[bac] : TEN[THUONG];')
    o.append('    }')
    o.append('')
    o.append('    /** Ten bac cua tung colorName (thu tu giu nguyen). */')
    o.append('    public static String[] tenVatPham(final int[] colorName) {')
    o.append('        final String[] r = new String[colorName.length];')
    o.append('        for (int i = 0; i < r.length; i++) {')
    o.append('            r[i] = ten(bacVatPham(colorName[i]));')
    o.append('        }')
    o.append('        return r;')
    o.append('    }')
    o.append('}')
    return '\n'.join(o) + '\n'


def sinh_client(d):
    b = d['bac']
    o = ['package game.pokemon;', '',
         '/**',
         ' * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.',
         ' * Bang mau do hiem chung (MOT nguon voi may chu va web). Ve qua token chu de ChuDe.mauHiem / nenHiem / sangHiem,',
         ' * khung o do KieuO.mauHiem, phu kien PhuKien. J2ME: chi mang int / String.',
         ' */',
         'public final class DoHiemSinh {', '',
         '    private DoHiemSinh() {', '    }', '']
    for i, x in enumerate(b):
        o.append('    public static final int %s = %d;' % (x['ma'].upper(), i))
    o.append('    public static final int SO_BAC = %d;' % len(b))
    o.append('')
    o.append('    public static final String[] TEN = {%s};' % ', '.join(j(x['ten']) for x in b))
    o.append('    /** chu / vien tren nen sang */')
    o.append('    public static final int[] MAU = {%s};' % ', '.join(h(x['mau']) for x in b))
    o.append('    /** nen nhat cung tong */')
    o.append('    public static final int[] NHAT = {%s};' % ', '.join(h(x['nhat']) for x in b))
    o.append('    /** chu tren nen toi / the gioi */')
    o.append('    public static final int[] SANG = {%s};' % ', '.join(h(x['sang']) for x in b))
    o.append('    public static final boolean[] VIEN_DONG = {%s};' % ', '.join('true' if x['vienDong'] else 'false' for x in b))
    o.append('    /** bang mau phu kien [bac] = {vien, toi, goc, sang, diem} */')
    o.append('    public static final int[][] PHU_KIEN = {')
    for x in b:
        o.append('        {%s},' % ', '.join(h(c) for c in x['phuKien']))
    o.append('    };')
    o.append('    /** Item.colorName -> bac (ngoai bang = THUONG) */')
    o.append('    public static final int[] BAC_VAT_PHAM = {%s};' % ', '.join(str(x) for x in d['_mvp']))
    o.append('    /** do hiem danh hieu 0..3 -> bac */')
    o.append('    public static final int[] BAC_DANH_HIEU = {%s};' % ', '.join(str(x) for x in d['_dh']))
    o.append('    public static final String[] TEN_BAC_TRANG_BI = {%s};' % ', '.join(j(x['ten']) for x in d['bacTrangBi']))
    o.append('    public static final int[] MAU_BAC_TRANG_BI = {%s};' % ', '.join(h(x) for x in d['_bt']))
    o.append('')
    o.append('    public static int bacVatPham(int colorName) {')
    o.append('        return colorName >= 0 && colorName < BAC_VAT_PHAM.length ? BAC_VAT_PHAM[colorName] : THUONG;')
    o.append('    }')
    o.append('')
    o.append('    public static int bacDanhHieu(int doHiem) {')
    o.append('        return doHiem >= 0 && doHiem < BAC_DANH_HIEU.length ? BAC_DANH_HIEU[doHiem] : THUONG;')
    o.append('    }')
    o.append('')
    o.append('    static int kep(int bac) {')
    o.append('        return bac >= 0 && bac < SO_BAC ? bac : THUONG;')
    o.append('    }')
    o.append('}')
    return '\n'.join(o) + '\n'


def sinh_ts(d):
    b = d['bac']
    o = ['/**',
         ' * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.',
         ' * Do hiem chung (cung mau voi game: server com.htbb.pokemon.DoHiemSinh, client game.pokemon.DoHiemSinh).',
         ' */', '',
         'export interface BacHiem { id: number, ma: string, ten: string, mau: string, nhat: string, sang: string, vienDong: boolean }',
         '',
         'export const BAC_HIEM: readonly BacHiem[] = [']
    for i, x in enumerate(b):
        o.append('  { id: %d, ma: %s, ten: %s, mau: %s, nhat: %s, sang: %s, vienDong: %s },' % (
            i, j(x['ma']), j(x['ten']), j(x['mau']), j(x['nhat']), j(x['sang']), 'true' if x['vienDong'] else 'false'))
    o.append(']')
    o.append('')
    o.append('/** Item.colorName -> id bac (ngoai bang = 0 Thuong). */')
    o.append('export const BAC_VAT_PHAM: readonly number[] = [%s]' % ', '.join(str(x) for x in d['_mvp']))
    o.append('/** data_danh_hieu.do_hiem (0..3) -> id bac. */')
    o.append('export const BAC_DANH_HIEU: readonly number[] = [%s]' % ', '.join(str(x) for x in d['_dh']))
    o.append('export const BAC_TRANG_BI: readonly { ten: string, mau: string }[] = [')
    for x, m in zip(d['bacTrangBi'], d['_bt']):
        o.append('  { ten: %s, mau: %s },' % (j(x['ten']), j(m)))
    o.append(']')
    o.append('')
    o.append('export function bacHiem(id: number): BacHiem {')
    o.append('  return BAC_HIEM[id] ?? BAC_HIEM[0]!')
    o.append('}')
    o.append('')
    o.append('export function bacVatPham(colorName: number): BacHiem {')
    o.append('  return bacHiem(BAC_VAT_PHAM[colorName] ?? 0)')
    o.append('}')
    o.append('')
    o.append('export function bacDanhHieu(doHiem: number): BacHiem {')
    o.append('  return bacHiem(BAC_DANH_HIEU[doHiem] ?? 0)')
    o.append('}')
    return '\n'.join(o) + '\n'


def sq(s):
    return "'" + str(s).replace('\\', '\\\\').replace("'", "''") + "'"


def sinh_sql(d):
    b = d['bac']
    o = ['-- DO HIEM CHUNG (de xuat 4, chu game duyet 19/10/2026) - SINH TU tools/cauhinh/do_hiem.json boi',
         '-- tools/cauhinh/sinh_do_hiem.py, KHONG SUA TAY. Idempotent (MariaDB 10.3, utf8): chay lai bao nhieu lan cung duoc.',
         '-- Mau trong game / web doc ban sinh trong ma nguon (cung nguon); bang nay de tra cuu, admin va cong cu ngoai.',
         '--   (khoi nay nam trong sql/nang_cap.sql phan 11)', '',
         'CREATE TABLE IF NOT EXISTS `data_do_hiem` (',
         '  `id` TINYINT NOT NULL,',
         "  `ma` VARCHAR(16) NOT NULL DEFAULT '',",
         "  `ten` VARCHAR(32) NOT NULL DEFAULT '',",
         "  `mau` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu / vien tren nen sang',",
         "  `nhat` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'nen nhat cung tong',",
         "  `sang` VARCHAR(7) NOT NULL DEFAULT '' COMMENT 'chu tren nen toi / the gioi',",
         "  `phu_kien` VARCHAR(40) NOT NULL DEFAULT '' COMMENT '5 tong phu kien: vien,toi,goc,sang,diem',",
         '  `vien_dong` TINYINT NOT NULL DEFAULT 0,',
         '  PRIMARY KEY (`id`)',
         ') ENGINE=InnoDB DEFAULT CHARSET=utf8;', '',
         'INSERT INTO `data_do_hiem` (`id`,`ma`,`ten`,`mau`,`nhat`,`sang`,`phu_kien`,`vien_dong`) VALUES']
    rows = []
    for i, x in enumerate(b):
        rows.append('(%d,%s,%s,%s,%s,%s,%s,%d)' % (i, sq(x['ma']), sq(x['ten']), sq(x['mau']), sq(x['nhat']), sq(x['sang']),
                                                  sq(','.join(x['phuKien'])), 1 if x['vienDong'] else 0))
    o.append(',\n'.join(rows))
    o.append('ON DUPLICATE KEY UPDATE `ma`=VALUES(`ma`), `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `nhat`=VALUES(`nhat`),'
             ' `sang`=VALUES(`sang`), `phu_kien`=VALUES(`phu_kien`), `vien_dong`=VALUES(`vien_dong`);')
    o.append('')
    o.append('-- Item.colorName (data_item.colortype) -> bac; ma khong co dong = Thuong.')
    o.append('CREATE TABLE IF NOT EXISTS `data_do_hiem_vat_pham` (')
    o.append('  `color_name` TINYINT NOT NULL,')
    o.append('  `bac` TINYINT NOT NULL DEFAULT 0,')
    o.append('  PRIMARY KEY (`color_name`)')
    o.append(') ENGINE=InnoDB DEFAULT CHARSET=utf8;')
    o.append('')
    o.append('INSERT INTO `data_do_hiem_vat_pham` (`color_name`,`bac`) VALUES')
    o.append(','.join('(%d,%d)' % (i, x) for i, x in enumerate(d['_mvp'])))
    o.append('ON DUPLICATE KEY UPDATE `bac`=VALUES(`bac`);')
    o.append('')
    o.append('-- 11 bac trang bi theo cap (vong sang / vien Pokemon).')
    o.append('CREATE TABLE IF NOT EXISTS `data_bac_trang_bi` (')
    o.append('  `bac` TINYINT NOT NULL,')
    o.append("  `ten` VARCHAR(32) NOT NULL DEFAULT '',")
    o.append("  `mau` VARCHAR(7) NOT NULL DEFAULT '',")
    o.append('  PRIMARY KEY (`bac`)')
    o.append(') ENGINE=InnoDB DEFAULT CHARSET=utf8;')
    o.append('')
    o.append('INSERT INTO `data_bac_trang_bi` (`bac`,`ten`,`mau`) VALUES')
    o.append(',\n'.join('(%d,%s,%s)' % (i, sq(x['ten']), sq(m)) for i, (x, m) in enumerate(zip(d['bacTrangBi'], d['_bt']))))
    o.append('ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`);')
    o.append('')
    o.append('-- Mau danh hieu (them_danh_hieu.sql tao bang; cot mau chi cho web) = mau bac chung.')
    o.append('CREATE TABLE IF NOT EXISTS `data_danh_hieu_do_hiem` (')
    o.append('  `id` TINYINT NOT NULL,')
    o.append("  `ten` VARCHAR(32) NOT NULL DEFAULT '',")
    o.append("  `mau` VARCHAR(7) NOT NULL DEFAULT '',")
    o.append('  `thu_tu` INT NOT NULL DEFAULT 0,')
    o.append('  PRIMARY KEY (`id`)')
    o.append(') ENGINE=InnoDB DEFAULT CHARSET=utf8;')
    o.append('')
    o.append('INSERT INTO `data_danh_hieu_do_hiem` (`id`,`ten`,`mau`,`thu_tu`) VALUES')
    o.append(','.join('(%d,%s,%s,%d)' % (i, sq(b[x]['ten']), sq(b[x]['mau']), i) for i, x in enumerate(d['_dh'])))
    o.append('ON DUPLICATE KEY UPDATE `ten`=VALUES(`ten`), `mau`=VALUES(`mau`), `thu_tu`=VALUES(`thu_tu`);')
    return '\n'.join(o) + '\n'


def khoi_phu_kien(d):
    b = d['bac']
    o = ['    static final int[][] HIEM = {']
    for x in b:
        o.append('        {%s},   // %s' % (', '.join(h(c) for c in x['phuKien']), x['ma']))
    o.append('    };')
    o.append('    static final String[] TEN_HIEM = {%s};' % ', '.join(j(x['ten']) for x in b))
    o.append('    static final int[] MAU_BAC = {%s};' % ', '.join(h(x) for x in d['_bt']))
    o.append('    static final String[] TEN_BAC = {%s};' % ', '.join(j(x['ten']) for x in d['bacTrangBi']))
    return '\n'.join(o) + '\n'


def ghep_phu_kien(cu, d):
    a = cu.find(MOC_DAU)
    b = cu.find(MOC_CUOI)
    if a < 0 or b < a:
        return None
    return cu[:a + len(MOC_DAU)] + khoi_phu_kien(d) + cu[b:]


def ghep_nang_cap(cu, sql):
    """Thay khoi tu dong MOC_SQL toi dong "-- >>> " ke tiep (hoac het tep) cua nang_cap.sql bang SQL sinh."""
    a = cu.find('\n' + MOC_SQL)
    if a < 0:
        return None
    a += 1
    b = cu.find('\n-- >>> ', a + 1)
    duoi = '' if b < 0 else cu[b + 1:]
    return cu[:a] + MOC_SQL + sql + duoi


def tep_ra():
    """[(duong, noi dung)] cho main() / --kiem."""
    d = doc()
    ra = [(RA_SERVER, sinh_server(d)), (RA_CLIENT, sinh_client(d))]
    with open(RA_SQL, encoding='utf-8') as f:
        nc = ghep_nang_cap(f.read(), sinh_sql(d))
    if nc is None:
        sys.exit('sql/nang_cap.sql thieu dong moc ' + MOC_SQL.strip())
    ra.append((RA_SQL, nc))
    if os.path.isdir(os.path.dirname(RA_TS)):
        ra.append((RA_TS, sinh_ts(d)))
    if os.path.exists(RA_PHU_KIEN):
        with open(RA_PHU_KIEN, encoding='utf-8') as f:
            pk = ghep_phu_kien(f.read(), d)
        if pk is None:
            sys.exit('tools/pokemon/SinhPhuKien.java thieu dong moc do_hiem.json')
        ra.append((RA_PHU_KIEN, pk))
    return ra


def main():
    kiem = '--kiem' in sys.argv
    lech = []
    for duong, nd in tep_ra():
        cu = None
        if os.path.exists(duong):
            with open(duong, encoding='utf-8') as f:
                cu = f.read()
        if cu == nd:
            continue
        if kiem:
            lech.append(duong)
        else:
            with open(duong, 'w', encoding='utf-8') as f:
                f.write(nd)
            print('da ghi', os.path.relpath(duong, GOC))
    if lech:
        print('TEP SINH LECH voi do_hiem.json (chay python3 tools/cauhinh/sinh_do_hiem.py):')
        for x in lech:
            print('  ', x)
        sys.exit(1)


if __name__ == '__main__':
    main()
