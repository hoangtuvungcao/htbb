#!/usr/bin/env node
/**
 * Sinh HTBB/src/data/QuyTacDangKyDuLieu.java từ shared/data/quy-tac-dang-ky.json — để server game
 * (đăng ký trong game, lệnh 39) dùng ĐÚNG bộ quy tắc của web mà không cần thư viện JSON lúc chạy
 * (server deploy chỉ có class, classpath không có org.json).
 *
 * Chạy:  node scripts/sinh-quy-tac-dang-ky-java.mjs [đường/dẫn/tới/HTBB]   (mặc định ../HTBB)
 * Sau khi sửa JSON PHẢI chạy lại; npm test và test/Kiem.java của HTBB so mã băm SHA256 và báo nếu quên.
 */
import { createHash } from 'node:crypto'
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const goc = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const htbb = resolve(process.argv[2] ?? join(goc, '..'))
const raw = readFileSync(join(goc, 'shared/data/quy-tac-dang-ky.json'))
const d = JSON.parse(raw.toString('utf8'))
const sha = createHash('sha256').update(raw).digest('hex')

/** Chuỗi Java: chỉ ASCII trong tệp nguồn (\\uXXXX cho chữ có dấu) để không phụ thuộc -encoding. */
const js = (s) => '"' + [...String(s)].map((ch) => {
  const c = ch.codePointAt(0)
  if (ch === '"' || ch === '\\') return '\\' + ch
  if (c >= 32 && c < 127) return ch
  if (c > 0xFFFF) {
    const h = Math.floor((c - 0x10000) / 0x400) + 0xD800
    const l = ((c - 0x10000) % 0x400) + 0xDC00
    return `\\u${h.toString(16).padStart(4, '0')}\\u${l.toString(16).padStart(4, '0')}`
  }
  return `\\u${c.toString(16).padStart(4, '0')}`
}).join('') + '"'
const mang = (a) => `{${a.map(js).join(', ')}}`

const tb = Object.entries(d.thongBao)
const so = Object.entries(d.thayTheSo)

const out = `package data;

/**
 * TEP SINH TU DONG - KHONG SUA TAY.
 * Nguon: HTBB-Web/shared/data/quy-tac-dang-ky.json (quy tac dang ky CHUNG cho web + game).
 * Sinh lai: cd HTBB-Web && node scripts/sinh-quy-tac-dang-ky-java.mjs
 * Logic dung du lieu nay: data.QuyTacDangKy (ban dich 1-1 cua HTBB-Web/shared/dang-ky/kiem-tra.ts).
 */
public final class QuyTacDangKyDuLieu {

    /** SHA-256 cua tep JSON nguon luc sinh. Kiem thu hai ben so gia tri nay de bat lech. */
    public static final String SHA256 = "${sha}";
    public static final int PHIEN_BAN = ${Number(d.phienBan)};

    public static final int TEN_MIN = ${Number(d.tenTaiKhoan.min)};
    public static final int TEN_MAX = ${Number(d.tenTaiKhoan.max)};
    public static final int MAT_KHAU_MIN = ${Number(d.matKhau.min)};
    public static final int MAT_KHAU_MAX = ${Number(d.matKhau.max)};
    public static final String[] MAT_KHAU_CAM = ${mang(d.matKhau.cam)};
    public static final int EMAIL_TOI_DA = ${Number(d.email.toiDa)};
    public static final boolean EMAIL_BAT_BUOC_WEB = ${Boolean(d.email.batBuocWeb)};
    public static final boolean EMAIL_BAT_BUOC_GAME = ${Boolean(d.email.batBuocGame)};

    public static final String[] TEN_QUAN_TRI_BANG = ${mang(d.tenQuanTri.bang)};
    public static final String[] TEN_QUAN_TRI_BAT_DAU = ${mang(d.tenQuanTri.batDau)};
    public static final String[] TEN_QUAN_TRI_CHUA = ${mang(d.tenQuanTri.chua)};
    public static final String[] TU_CAM = ${mang(d.tuCam)};
    public static final String THAY_THE_SO_TU = ${js(so.map(([k]) => k).join(''))};
    public static final String THAY_THE_SO_THANH = ${js(so.map(([, v]) => v).join(''))};

    public static final int HAN_MUC_MOI_GIO = ${Number(d.hanMuc.moiGio)};
    public static final int HAN_MUC_MOI_NGAY = ${Number(d.hanMuc.moiNgay)};

    public static final String[] THONG_BAO_MA = ${mang(tb.map(([k]) => k))};
    public static final String[] THONG_BAO_CHU = ${mang(tb.map(([, v]) => v))};

    /** Bo ca chung {u, p, c, e, maLoiWeb (phay), maLoiGame (phay)} - chay ca o npm test lan test/Kiem.java. */
    public static final String[][] THU_NGHIEM = {
${d.thuNghiem.map(t => `        {${[t.u, t.p, t.c, t.e, t.web.join(','), t.game.join(',')].map(js).join(', ')}}`).join(',\n')}
    };

    private QuyTacDangKyDuLieu() {
    }
}
`
const dich = join(htbb, 'server/src/data/QuyTacDangKyDuLieu.java')
if (!existsSync(dirname(dich))) {
  console.error(`Khong thay ${dirname(dich)} - truyen duong dan repo HTBB lam tham so.`)
  process.exit(1)
}
writeFileSync(dich, out)
console.log(`Da sinh ${dich} (sha256 ${sha.slice(0, 12)}...)`)
