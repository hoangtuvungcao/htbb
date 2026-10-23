/**
 * Chữ người chơi thấy trên web (trang, thông báo lỗi API, dữ liệu cẩm nang) — rà soát 23/10/2026:
 *   1. không nhắc tên game khác (NRO / NSO / HSO / TeaMobi / Làng Lá / Ngọc Rồng / Ninja School) ở CẢ app, server, shared
 *      (tests/khong-ten-game-khac.test.ts chỉ quét trang công khai + shared/data);
 *   2. không lọt chỗ trống / chữ máy: "undefined", "null", "NaN", "{x}" trong chuỗi tiếng Việt;
 *   3. chuỗi đã có dấu thì không còn từ tiếng Việt viết KHÔNG dấu (khong, duoc, nguoi... — gõ sót) hay chữ Anh sót
 *      (error, failed, loading, submit...);
 *   4. số luật chơi trang cẩm nang ghi tay == nguồn thật (danh mục cấu hình, sql/htbb.sql, mã máy chủ):
 *      Ô thú, cấp vào Hoenn / khoá bản đồ + cửa ải, Tự động đánh, hạn mức đăng ký open test.
 * Chuỗi = '...', "...", `...` (bỏ ${...}) và chữ giữa thẻ trong <template> (bỏ {{ ... }}); chú thích mã bị bỏ.
 * Ngoại lệ ghi rõ trong các danh sách CHO_* bên dưới (giữ nhỏ). Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { docBangSql } from '../shared/utils/sqlDump.ts'
import { htbbSql } from './htbbSql.ts'
import { O_THU } from '../shared/utils/oThu.ts'
import { DANH_MUC_TT as DS_TT } from '../shared/utils/cauHinhTrungTam.gen.ts'

const goc = (p: string) => fileURLToPath(new URL(p, import.meta.url))
const WEB = goc('..')
const doc = (p: string) => readFileSync(goc(p), 'utf8')
// pokedex.ts / camNangNoiDung.ts import JSON kiểu Vite (không chạy thẳng trong node) -> đọc tệp.
const POKEDEX = JSON.parse(doc('../shared/data/pokedex.json')) as { hoenn: { capVao: number } }
const CAM_NANG_ND = JSON.parse(doc('../shared/data/cam-nang-noi-dung.json')) as { cotTruyen: { chuong: { so: number, nam: number, cap: number }[] } }
const CAP_VAO_HOENN = Number(/export const CAP_VAO_HOENN = (\d+)/.exec(doc('../shared/utils/pokedex.ts'))?.[1])
const QUY_TAC_DANG_KY = JSON.parse(doc('../shared/data/quy-tac-dang-ky.json')) as { hanMuc: { moiGio: number, moiNgay: number } }

// ------------------------------------------------------------------ tách chuỗi hiển thị

function tep(thu: string): string[] {
  const ra: string[] = []
  for (const t of readdirSync(thu)) {
    if (t === 'node_modules' || t.startsWith('.')) continue
    const p = join(thu, t)
    if (statSync(p).isDirectory()) ra.push(...tep(p))
    else if (/\.(vue|ts|mjs|json)$/.test(t) && !/\.gen\.ts$/.test(t)) ra.push(p)
  }
  return ra
}

const trang = (m: string) => m.replace(/[^\n]/g, ' ')
/** Bỏ chú thích nhưng GIỮ số dòng. */
function boChuThich(s: string): string {
  return s.replace(/<!--[\s\S]*?-->/g, trang).replace(/\/\*[\s\S]*?\*\//g, trang)
    .replace(/(^|[^:"'`\\])\/\/[^\n]*/g, (m, a: string) => a + trang(m.slice(a.length)))
}
const boBieuThuc = (v: string) => v.replace(/\$\{[^}]*\}/g, ' ').replace(/\{\{[\s\S]*?\}\}/g, ' ')

interface Chuoi { tep: string, dong: number, v: string }

function chuoiTrong(f: string): Chuoi[] {
  const s = boChuThich(readFileSync(f, 'utf8').normalize('NFC'))
  const ra: Chuoi[] = []
  const them = (i: number, v: string) => ra.push({ tep: f.slice(WEB.length), dong: s.slice(0, i).split('\n').length, v: boBieuThuc(v) })
  if (f.endsWith('.json')) {
    for (const m of s.matchAll(/"((?:[^"\\\n]|\\.)*)"/g)) them(m.index, m[1]!)
    return ra
  }
  let ma = s
  if (f.endsWith('.vue')) {
    const t = /<template>([\s\S]*)<\/template>/.exec(s)
    if (t) {
      const dau = t.index + '<template>'.length
      for (const m of t[1]!.matchAll(/>([^<>]+)</g)) them(dau + m.index, m[1]!)
      for (const m of t[1]!.matchAll(/\{\{([\s\S]*?)\}\}|\s[:@]?[\w.-]+="([^"]*)"/g)) {
        const e = m[1] ?? m[2]!
        if (m[2] !== undefined && !/^\s*:|^\s*@/.test(m[0].trimStart()) && !/['`]/.test(e)) them(dau + m.index, e)
        for (const n of e.matchAll(/'((?:[^'\\]|\\.)*)'|`((?:[^`\\]|\\.)*)`/g)) them(dau + m.index, n[1] ?? n[2]!)
      }
      ma = s.slice(0, t.index) + trang(t[0]) + s.slice(t.index + t[0].length)
    }
  }
  for (const m of ma.matchAll(/'((?:[^'\\\n]|\\.)*)'|"((?:[^"\\\n]|\\.)*)"|`((?:[^`\\]|\\.)*)`/g)) them(m.index, m[1] ?? m[2] ?? m[3]!)
  return ra
}

const TAT_CA = ['app', 'server', 'shared'].flatMap(g => tep(join(WEB, g))).flatMap(chuoiTrong)
/** Có dấu tiếng Việt = chuỗi người đọc (bỏ được tên khoá, SQL, đường dẫn...). */
const DAU = /[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ]/i
const CO_DAU = TAT_CA.filter(c => DAU.test(c.v) && !/\b(SELECT|INSERT|UPDATE|DELETE|WHERE)\b/.test(c.v))
const hien = (c: Chuoi) => `${c.tep}:${c.dong}: ${c.v.trim().replace(/\s+/g, ' ').slice(0, 140)}`

test('bộ tách chuỗi đọc được đủ (tự kiểm)', () => {
  assert.ok(TAT_CA.length > 20000, `chỉ ${TAT_CA.length} chuỗi`)
  assert.ok(CO_DAU.some(c => c.tep.endsWith('tai-khoan.vue') && c.v.includes('Hai cách đăng ký')), 'thuộc tính title=')
  assert.ok(CO_DAU.some(c => c.tep.endsWith('tai-khoan.vue') && c.v.includes('Mở trang')), 'chữ giữa thẻ')
})

// ------------------------------------------------------------------ 1. tên game khác

const CAM_TEN = /(^|[^A-Za-zÀ-ỹ])(NRO|NSO|HSO|TeaMobi|Teamobi)([^A-Za-zÀ-ỹ]|$)|Làng Lá|Ngọc Rồng|Ninja School|ngoc rong online/i
/** /giay-phep: ghi công nguồn gốc mã; quy-tac-dang-ky.json: danh sách tên CẤM đặt (không hiển thị). */
const CHO_TEN = [/giay-phep\.vue$/, /quy-tac-dang-ky\.json$/]

test('không nhắc tên game khác trong mọi chuỗi web (app + server + shared, cả admin)', () => {
  const loi = TAT_CA.filter(c => CAM_TEN.test(c.v) && !CHO_TEN.some(r => r.test(c.tep))).map(hien)
  assert.deepEqual(loi, [], loi.join('\n'))
})

// ------------------------------------------------------------------ 2. chỗ trống / chữ máy lọt ra

/** Thẻ {..} được thay lúc chạy: quy-tac-dang-ky.json {min}/{max} (dangKyThongBao), FAQ {nen_tang}/{nen_tang_sap}. */
const THE_HOP_LE = new Set(['min', 'max', 'nen_tang', 'nen_tang_sap'])

test('chuỗi tiếng Việt không lọt "undefined" / "null" / "NaN" / thẻ {x} chưa thay', () => {
  const loi: string[] = []
  for (const c of CO_DAU) {
    if (/(^|[^\w.$-])(undefined|null|NaN)([^\w-]|$)/.test(c.v)) loi.push(hien(c))
    for (const m of c.v.matchAll(/\{([a-zA-Z_]\w*)\}/g)) if (!THE_HOP_LE.has(m[1]!)) loi.push(hien(c))
  }
  assert.deepEqual(loi, [], loi.join('\n'))
})

// ------------------------------------------------------------------ 3. gõ sót không dấu / chữ Anh

const KHONG_DAU = ['khong', 'duoc', 'nguoi', 'nhiem', 'thuong', 'luong', 'tien', 'diem', 'phut', 'giay', 'truoc', 'duong',
  'them', 'het', 'mot', 'tren', 'duoi', 'nhieu', 'muon', 'chon', 'chua', 'vui long', 'tai khoan', 'mat khau', 'dang nhap',
  'dang ky', 'thu lai', 'cap nhat', 'xu ly', 'hop le', 'ky tu', 'hanh trang', 'kich hoat']
const RE_KHONG_DAU = new RegExp(`(^|[^A-Za-z0-9_À-ỹ./#:$-])(${KHONG_DAU.join('|')})([^A-Za-z0-9_À-ỹ:]|$)`, 'i')
/** Cụm chữ HOA admin phải gõ đúng để xác nhận (không dấu cho dễ gõ). */
const CHO_KHONG_DAU = [/Gõ XOA DA CHON để xác nhận/]
const RE_ANH = /(^|[^A-Za-z_.$/-])(error|errors|failed|fail|loading|submit|success|please|not found|unauthorized|forbidden|click here|TODO|FIXME|lorem)([^A-Za-z_-]|$)/i

test('chuỗi đã có dấu không còn từ tiếng Việt gõ không dấu', () => {
  const loi = CO_DAU.filter(c => RE_KHONG_DAU.test(c.v) && !CHO_KHONG_DAU.some(r => r.test(c.v))).map(hien)
  assert.deepEqual(loi, [], loi.join('\n'))
})

test('chuỗi tiếng Việt không sót chữ Anh (error, failed, loading, submit...)', () => {
  const loi = CO_DAU.filter(c => RE_ANH.test(c.v)).map(hien)
  assert.deepEqual(loi, [], loi.join('\n'))
})

test('bộ bắt chữ tự kiểm: bắt chữ sót, không bắt chữ đúng', () => {
  assert.ok(RE_KHONG_DAU.test('Đăng nhập khong thành công'))
  assert.ok(RE_KHONG_DAU.test('Bạn duoc tặng 5 lượng'))
  assert.ok(!RE_KHONG_DAU.test('Không được để trống'))
  assert.ok(!RE_KHONG_DAU.test("{ key: 'luong', label: 'Lượng' }".split("'")[3]!))
  assert.ok(RE_ANH.test('Tải thất bại (failed)'))
  assert.ok(!RE_ANH.test('Đã lưu'))
})

// ------------------------------------------------------------------ 4. số luật chơi == nguồn thật

const SQL = htbbSql()
const bang = (t: string) => docBangSql(SQL, t)
const danhMucSql = new Map(bang('cau_hinh_danh_muc').map(r => [String(r.khoa), String(r.mac_dinh)]))
const macDinh = (k: string) => {
  const g = DS_TT.find(x => x.khoa === k)
  assert.ok(g, `danh mục cấu hình thiếu khoá ${k}`)
  assert.equal(danhMucSql.get(k), g.macDinh, `${k}: sql/htbb.sql cau_hinh_danh_muc != cauHinhTrungTam.gen.ts`)
  return Number(g.macDinh)
}
const javaHang = (tepJava: string, ten: string): number | null => {
  const p = goc(`../../server/src/${tepJava}`)
  if (!existsSync(p)) return null
  const m = new RegExp(`static final int ${ten} = (\\d+);`).exec(readFileSync(p, 'utf8'))
  assert.ok(m, `${tepJava}: không thấy ${ten}`)
  return Number(m[1])
}

test('Ô thú trên cẩm nang (shared/utils/oThu.ts) == cấu hình + Vé mở ô thú + OThu.java', () => {
  assert.equal(O_THU.coBan, macDinh('thu.o_co_ban'))
  assert.equal(O_THU.toiDa, macDinh('thu.o_toi_da'))
  assert.equal(O_THU.xuThaNgay, macDinh('thu.tha_xu_ngay'))
  const ve = bang('nap_cua_hang').find(r => r.ma === 've_mo_o_thu')
  assert.ok(ve, 'nap_cua_hang thiếu dòng ve_mo_o_thu (khối them_quan_ly_thu.sql)')
  assert.equal(Number(ve.gia_luong), O_THU.giaVeLuong, 'giá Vé mở ô thú (lượng)')
  assert.equal(Number(ve.gioi_han), O_THU.toiDa - O_THU.coBan, 'giới hạn mua vé = tối đa - cơ bản')
  for (const [ten, gt] of [['O_CO_BAN', O_THU.coBan], ['O_TOI_DA', O_THU.toiDa], ['O_MOI_VE', O_THU.oMoiVe]] as const) {
    const j = javaHang('com/htbb/pokemon/OThu.java', ten)
    if (j !== null) assert.equal(j, gt, `OThu.${ten}`)
  }
  const vue = doc('../app/pages/cam-nang/thu-cuoi-pet.vue')
  assert.match(vue, /id="o-thu"/)
  assert.ok(!/\b(20|60) ô\b|10 lượng/.test(vue), 'thu-cuoi-pet.vue: số Ô thú phải đọc từ O_THU, không gõ tay')
})

test('cấp vào Hoenn trên cẩm nang == khoá bản đồ Hoenn (data_ban_do_mo_khoa, cửa C52.7) == chương 52', () => {
  const hoenn = bang('data_ban_do_mo_khoa').filter(r => r.ma_nhiem_vu === 'C52.7')
  assert.ok(hoenn.length >= 20, `chỉ ${hoenn.length} bản đồ Hoenn khoá theo C52.7`)
  assert.equal(CAP_VAO_HOENN, Math.min(...hoenn.map(r => Number(r.cap_toi_thieu))))
  assert.equal(CAP_VAO_HOENN, CAM_NANG_ND.cotTruyen.chuong.find(c => c.nam === 1 && c.so === 52)?.cap)
  assert.ok(CAP_VAO_HOENN >= POKEDEX.hoenn.capVao, 'cổng tàu không được chặn cao hơn khoá bản đồ')
  // Trang người chơi: không ghi cấp cổng tàu (84) như thể đủ để vào Hoenn.
  const loi = ['gioi-thieu.vue', 'cam-nang/ban-do.vue', 'cam-nang/cap-do.vue', 'cam-nang/cau-ca.vue', 'cam-nang/nhiem-vu.vue']
    .filter(p => /hoenn\.capVao|Hoenn[^.\n]{0,40}cấp 84/.test(doc(`../app/pages/${p}`)))
  assert.deepEqual(loi, [])
})

test('"12 cửa ải" + ví dụ cửa ải trên cẩm nang Bản đồ == data_ban_do_mo_khoa', () => {
  const ds = bang('data_ban_do_mo_khoa')
  const cuaAi = new Set(ds.map(r => String(r.ma_nhiem_vu)).filter(m => /^CA-\d+\./.test(m)).map(m => m.split('.')[0]))
  const vue = doc('../app/pages/cam-nang/ban-do.vue')
  const so = /(\d+) cửa ải<\/strong>/.exec(vue)
  assert.ok(so, 'ban-do.vue: không thấy "N cửa ải"')
  assert.equal(Number(so[1]), cuaAi.size)
  const viDu = /cửa ải «([^»]+)» mở ([^.]+?)\)/.exec(vue)
  assert.ok(viDu, 'ban-do.vue: không thấy ví dụ cửa ải')
  const mo = ds.filter(r => r.ten_nhiem_vu === `Cửa ải: ${viDu[1]}`).map(r => String(r.ghi_chu))
  for (const ten of viDu[2]!.split(/ và |, /)) assert.ok(mo.includes(ten.trim()), `cửa ải «${viDu[1]}» không mở ${ten}`)
})

test('Tự động đánh: 5 giờ / +2 giờ thẻ tháng / 15 ô trên cẩm nang == danh mục cấu hình', () => {
  const vue = doc('../app/pages/cam-nang/tu-dong.vue')
  const so = (re: RegExp) => Number(re.exec(vue)?.[1])
  assert.equal(so(/<b>(\d+) giờ<\/b><span>mỗi ngày/), macDinh('tu_dong.gio_moi_ngay'))
  assert.equal(so(/<b>\+(\d+) giờ<\/b><span>khi có thẻ tháng/), macDinh('tu_dong.gio_them_the_thang'))
  assert.equal(so(/<b>(\d+) ô<\/b><span>vùng đánh/), macDinh('tu_dong.ban_kinh') / 16)
})

test('hạn mức đăng ký: mặc định = quy-tac-dang-ky.json, open test (mo_han_muc_dang_ky.sql) = 6 / giờ, 30 / 24 giờ; trang không ghi số cố định', () => {
  assert.equal(macDinh('dang_ky.toi_da_gio'), QUY_TAC_DANG_KY.hanMuc.moiGio)
  assert.equal(macDinh('dang_ky.toi_da_ngay'), QUY_TAC_DANG_KY.hanMuc.moiNgay)
  const khoi = SQL.slice(SQL.indexOf('-- >>> mo_han_muc_dang_ky.sql'))
  assert.match(khoi, /\('dang_ky\.toi_da_gio', '6'/)
  assert.match(khoi, /\('dang_ky\.toi_da_ngay', '30'/)
  // Admin đổi được lúc chạy: trang người chơi không được ghi "3 tài khoản / giờ"...
  for (const p of ['cam-nang/tai-khoan.vue', 'register.vue', 'huong-dan.vue']) {
    assert.ok(!/\d+ tài khoản (mỗi|\/) ?(giờ|ngày)/.test(doc(`../app/pages/${p}`)), p)
  }
})

test('FAQ mặc định không phủ nhận tính năng đang bật (thu phục, Tự động đánh)', () => {
  // faqMacDinh.ts import không đuôi (Nuxt) -> đọc như chữ.
  const chu = doc('../shared/utils/faqMacDinh.ts')
  if (macDinh('bat.bat') === 1) assert.ok(!/không bắt Pokémon|không thu phục/i.test(chu), 'thu phục đang bật mặc định')
  if (macDinh('tu_dong.bat') === 1) assert.ok(!/treo máy tự động[^.]*bị khoá/i.test(chu), 'Tự động đánh chính thức đang bật')
})
