/**
 * Lớp đệm SQL cho NovaDB (https://github.com/hoangtuvungcao/NovaDB) — CSDL nền SQLite có cổng
 * giả lập MySQL. Dùng bản NovaDB tải về nguyên gốc, nên những chỗ cổng MySQL của nó còn thiếu
 * được vá ở đây, ngay trước khi câu lệnh gửi đi. Y hệt `HTBB/src/data/NovaDBSql.java` (game +
 * login server) — sửa một bên thì sửa cả bên kia để ba tiến trình thấy cùng một giờ, cùng một
 * cách so tên.
 *
 *   - NOW()/CURDATE()/CURRENT_TIMESTAMP của NovaDB luôn trả giờ UTC → đổi thành
 *     datetime('now', +lệch múi giờ của máy). VPS đặt Asia/Ho_Chi_Minh nên = +7h.
 *   - DATE_SUB/DATE_ADD(CURDATE(), INTERVAL n DAY) trả 'yyyy-MM-dd 00:00:00' thay vì
 *     'yyyy-MM-dd' → so chuỗi với cột DATE lệch 1 ngày. Đổi thẳng thành date(...).
 *   - UNIX_TIMESTAMP(x)/FROM_UNIXTIME(x) coi x là UTC → bù lệch múi giờ.
 *   - Không có PASSWORD() → '*' + HEX(SHA1(SHA1(x))), khớp hàm của MariaDB/login server.
 *   - Không có TIMESTAMPDIFF → hiệu số strftime('%s').
 *   - '=' của SQLite phân biệt hoa/thường → cột chữ trong CREATE TABLE / ALTER TABLE được gắn
 *     COLLATE NOCASE (trừ cột _bin / BINARY). Chỉ gộp hoa thường chữ ASCII.
 *
 * Chỉ bật khi server tự xưng là NovaDB (VERSION() chứa "NovaDB"), nên vẫn chạy được với MariaDB.
 */
import type { Pool, PoolConnection } from 'mysql2/promise'

const lechGiay = -new Date().getTimezoneOffset() * 60
const LECH = (lechGiay >= 0 ? '+' : '') + lechGiay
const GIO_HIEN_TAI = `(datetime('now','${LECH} seconds'))`
const NGAY_HIEN_TAI = `(date('now','${LECH} seconds'))`

const CAN_XEM = ['NOW', 'CURDATE', 'CURRENT_', 'SYSDATE', 'LOCALTIME', 'PASSWORD',
  'TIMESTAMPDIFF', 'UNIX_TIMESTAMP', 'FROM_UNIXTIME', 'SUBSTRING_INDEX', 'CREATE', 'ALTER']

const nho = new Map<string, string>()
const NHO_TOI_DA = 2000

const enum Loai { Tu, Chuoi, Khoang, Dau }
interface Tok { loai: Loai, s: string }

const hoa = (t: Tok) => t.s.toUpperCase()
const la = (t: Tok | undefined, dau: string) => !!t && t.loai === Loai.Dau && t.s === dau
const laTu = (t: Tok | undefined, tu: string) => !!t && t.loai === Loai.Tu && t.s.toUpperCase() === tu

function laKyTuTu(c: string): boolean {
  return /[\p{L}\p{N}_$]/u.test(c)
}

/** Tách token: từ, chuỗi '..' ".." `..` (giữ nguyên), khoảng trắng/chú thích, dấu. */
function tach(s: string): Tok[] {
  const t: Tok[] = []
  const n = s.length
  let i = 0
  while (i < n) {
    const c = s[i]!
    let j = i + 1
    if (c === '\'' || c === '"' || c === '`') {
      while (j < n) {
        const d = s[j]
        if (d === '\\' && c !== '`') { j += 2; continue }
        if (d === c) {
          if (s[j + 1] === c) { j += 2; continue }
          j++
          break
        }
        j++
      }
      t.push({ loai: Loai.Chuoi, s: s.slice(i, Math.min(j, n)) })
    } else if (/\s/.test(c)) {
      while (j < n && /\s/.test(s[j]!)) j++
      t.push({ loai: Loai.Khoang, s: s.slice(i, j) })
    } else if ((c === '-' && s[j] === '-') || c === '#') {
      while (j < n && s[j] !== '\n') j++
      t.push({ loai: Loai.Khoang, s: s.slice(i, j) })
    } else if (c === '/' && s[j] === '*') {
      const k = s.indexOf('*/', j + 1)
      j = k < 0 ? n : k + 2
      t.push({ loai: Loai.Khoang, s: s.slice(i, j) })
    } else if (laKyTuTu(c)) {
      while (j < n && laKyTuTu(s[j]!)) j++
      t.push({ loai: Loai.Tu, s: s.slice(i, j) })
    } else {
      t.push({ loai: Loai.Dau, s: c })
    }
    i = j
  }
  return t
}

function keTiep(t: Tok[], i: number, het: number): number {
  for (let k = i; k < het; k++) if (t[k]!.loai !== Loai.Khoang) return k
  return -1
}

function truocDo(t: Tok[], a: number, i: number): number {
  let p = i - 1
  while (p >= a && t[p]!.loai === Loai.Khoang) p--
  return p
}

function dongNgoac(t: Tok[], mo: number, het: number): number {
  let sau = 0
  for (let k = mo; k < het; k++) {
    if (la(t[k], '(')) sau++
    else if (la(t[k], ')')) {
      sau--
      if (sau === 0) return k
    }
  }
  return -1
}

function tachPhay(t: Tok[], a: number, b: number): [number, number][] {
  const p: [number, number][] = []
  let sau = 0
  let dau = a
  for (let k = a; k < b; k++) {
    if (la(t[k], '(')) sau++
    else if (la(t[k], ')')) sau--
    else if (sau === 0 && la(t[k], ',')) {
      p.push([dau, k])
      dau = k + 1
    }
  }
  p.push([dau, b])
  return p
}

function gon(t: Tok[], a: number, b: number): string {
  let s = ''
  for (let k = a; k < b; k++) if (t[k]!.loai !== Loai.Khoang) s += hoa(t[k]!)
  return s
}

/** CREATE [TEMPORARY] TABLE ... hoặc ALTER TABLE ... */
function laDdlBang(t: Tok[]): boolean {
  const a = keTiep(t, 0, t.length)
  if (a < 0) return false
  let b = keTiep(t, a + 1, t.length)
  if (b < 0) return false
  if (laTu(t[a], 'ALTER')) return laTu(t[b], 'TABLE')
  if (laTu(t[a], 'CREATE')) {
    if (laTu(t[b], 'TEMPORARY')) b = keTiep(t, b + 1, t.length)
    return b >= 0 && laTu(t[b], 'TABLE')
  }
  return false
}

const boNhay = (s: string) => (s.length > 1 && s.startsWith('`') && s.endsWith('`') ? s.slice(1, -1) : s)

/**
 * ALTER TABLE t ... ADD [UNIQUE] KEY|INDEX tên → tên "t_tên". Tên index của SQLite là duy nhất
 * trong cả CSDL (MySQL: trong từng bảng). Tiền tố "<bảng>_" là đúng quy ước NovaDB tự đặt cho KEY
 * trong CREATE TABLE, và DROP INDEX tên của NovaDB tự tìm cả dạng có tiền tố.
 */
function doiTenIndex(t: Tok[]): void {
  const a = keTiep(t, 0, t.length)
  if (a < 0 || !laTu(t[a], 'ALTER')) return
  const b = keTiep(t, a + 1, t.length)
  const c = b < 0 ? -1 : keTiep(t, b + 1, t.length)
  if (c < 0 || t[c]!.loai === Loai.Dau) return
  const bang = boNhay(t[c]!.s)
  for (let i = c + 1; i < t.length; i++) {
    if (!laTu(t[i], 'ADD')) continue
    let k = keTiep(t, i + 1, t.length)
    if (k >= 0 && laTu(t[k], 'UNIQUE')) k = keTiep(t, k + 1, t.length)
    if (k < 0 || !(laTu(t[k], 'KEY') || laTu(t[k], 'INDEX'))) continue
    const ten = keTiep(t, k + 1, t.length)
    if (ten < 0 || t[ten]!.loai === Loai.Dau) continue
    const goc = boNhay(t[ten]!.s)
    if (!goc.startsWith(bang + '_')) t[ten] = { loai: Loai.Chuoi, s: `\`${bang}_${goc}\`` }
  }
}

function laOnUpdate(t: Tok[], a: number, i: number): boolean {
  const p = truocDo(t, a, i)
  if (p < a || !laTu(t[p], 'UPDATE')) return false
  const q = truocDo(t, a, p)
  return q >= a && laTu(t[q], 'ON')
}

/** DATE_SUB/DATE_ADD(CURDATE(), INTERVAL <số nguyên> DAY|WEEK|MONTH|YEAR) → date(...). */
function ngayCongTru(t: Tok[], a: number, b: number, tru: boolean): string | null {
  const p = tachPhay(t, a, b)
  if (p.length !== 2) return null
  const g = gon(t, p[0]![0], p[0]![1])
  if (g !== 'CURDATE()' && g !== 'CURRENT_DATE()' && g !== 'CURRENT_DATE') return null
  const tu = t.slice(p[1]![0], p[1]![1]).filter(x => x.loai !== Loai.Khoang)
  if (tu.length !== 3 || !laTu(tu[0], 'INTERVAL') || !/^\d+$/.test(tu[1]!.s)) return null
  let so = Number(tu[1]!.s)
  let dv: string
  switch (hoa(tu[2]!)) {
    case 'DAY': dv = 'days'; break
    case 'WEEK': so *= 7; dv = 'days'; break
    case 'MONTH': dv = 'months'; break
    case 'YEAR': dv = 'years'; break
    default: return null
  }
  return `(date('now','${LECH} seconds','${tru ? '-' : '+'}${so} ${dv}'))`
}

const CHIA: Record<string, number> = { SECOND: 1, MINUTE: 60, HOUR: 3600, DAY: 86400, WEEK: 604800 }

/** TIMESTAMPDIFF(đơn_vị, a, b) = b - a theo đơn vị, cắt phần lẻ về 0 như MySQL. */
function timestampDiff(t: Tok[], a: number, b: number, ddl: boolean): string | null {
  const p = tachPhay(t, a, b)
  if (p.length !== 3) return null
  const chia = CHIA[gon(t, p[0]![0], p[0]![1])]
  if (!chia) return null
  const x = viet(t, p[1]![0], p[1]![1], ddl)
  const y = viet(t, p[2]![0], p[2]![1], ddl)
  return `(CAST((CAST(strftime('%s',${y}) AS INTEGER)-CAST(strftime('%s',${x}) AS INTEGER))/${chia} AS SIGNED))`
}

/**
 * SUBSTRING_INDEX(x, 'c', -1) = phần sau dấu 'c' cuối cùng (cả x nếu không có 'c'); SQLite
 * không có hàm này. rtrim(x, <mọi ký tự của x trừ 'c'>) cắt cụt phần đuôi đó, còn lại tiền tố
 * kết thúc bằng 'c'. Chỉ dạng -1 với một ký tự (xepHang.ts) — chỉ web dùng, nên không có
 * trong NovaDBSql.java.
 */
function substringIndexCuoi(t: Tok[], a: number, b: number, ddl: boolean): string | null {
  const p = tachPhay(t, a, b)
  if (p.length !== 3 || gon(t, p[2]![0], p[2]![1]) !== '-1') return null
  const kyTu = t.slice(p[1]![0], p[1]![1]).filter(x => x.loai !== Loai.Khoang)
  if (kyTu.length !== 1 || !/^'[^'\\]'$/.test(kyTu[0]!.s)) return null
  const x = viet(t, p[0]![0], p[0]![1], ddl)
  const c = kyTu[0]!.s
  // CHAR_LENGTH chứ không phải length: cổng MySQL dịch LENGTH thành số byte như MySQL
  return `substr(${x}, CHAR_LENGTH(rtrim(${x}, replace(${x}, ${c}, '')))+1)`
}

const KIEU_CHU = new Set(['CHAR', 'VARCHAR', 'TEXT', 'TINYTEXT', 'MEDIUMTEXT', 'LONGTEXT'])
const KHONG_PHAI_TEN_COT = new Set(['ADD', 'COLUMN', 'MODIFY', 'TABLE', 'EXISTS', 'AS', 'KEY', 'INDEX'])

/** Kiểu cột luôn đứng ngay sau tên cột; "text" sau '(' / ',' / ADD là tên cột, không phải kiểu. */
function truocLaTenCot(t: Tok[], a: number, truoc: number): boolean {
  if (truoc < a) return false
  const p = t[truoc]!
  if (p.loai === Loai.Chuoi) return p.s.startsWith('`')
  return p.loai === Loai.Tu && !KHONG_PHAI_TEN_COT.has(hoa(p))
}

/** Kiểu cột chữ trong DDL: gắn COLLATE NOCASE, trừ cột đã khai báo _bin / BINARY. */
function kieuChu(t: Tok[], i: number, b: number, out: string[]): number {
  out.push(t[i]!.s)
  let j = i + 1
  const k = keTiep(t, j, b)
  if (k >= 0 && la(t[k], '(')) {
    const d = dongNgoac(t, k, b)
    if (d < 0) return j
    for (let q = j; q <= d; q++) out.push(t[q]!.s)
    j = d + 1
  }
  // Bỏ qua CHARACTER SET x / CHARSET x đứng ngay sau kiểu
  let q = j
  const z = keTiep(t, q, b)
  if (z >= 0 && laTu(t[z], 'CHARACTER')) {
    const z2 = keTiep(t, z + 1, b)
    const z3 = z2 >= 0 ? keTiep(t, z2 + 1, b) : -1
    if (z2 >= 0 && laTu(t[z2], 'SET') && z3 >= 0) q = z3 + 1
  } else if (z >= 0 && laTu(t[z], 'CHARSET')) {
    const z2 = keTiep(t, z + 1, b)
    if (z2 >= 0) q = z2 + 1
  }
  const c = keTiep(t, q, b)
  if (c >= 0 && laTu(t[c], 'BINARY')) return j
  if (c >= 0 && laTu(t[c], 'COLLATE')) {
    const ten = keTiep(t, c + 1, b)
    if (ten >= 0 && t[ten]!.loai === Loai.Tu && !hoa(t[ten]!).endsWith('_BIN')) {
      for (let r = j; r < ten; r++) out.push(t[r]!.s)
      out.push('NOCASE')
      return ten + 1
    }
    return j
  }
  out.push(' COLLATE NOCASE')
  return j
}

function viet(t: Tok[], a: number, b: number, ddl: boolean): string {
  const out: string[] = []
  let i = a
  while (i < b) {
    const x = t[i]!
    if (x.loai !== Loai.Tu) {
      out.push(x.s)
      i++
      continue
    }
    const w = hoa(x)
    const k = keTiep(t, i + 1, b)
    const dong = k >= 0 && la(t[k], '(') ? dongNgoac(t, k, b) : -1
    const truoc = truocDo(t, a, i)
    // Cột tên "now"/"password"... đứng sau dấu chấm (bảng.cột) thì không phải hàm
    if (truoc >= a && la(t[truoc], '.')) {
      out.push(x.s)
      i++
      continue
    }

    if (w === 'NOW' || w === 'SYSDATE' || w === 'CURRENT_TIMESTAMP' || w === 'LOCALTIME' || w === 'LOCALTIMESTAMP') {
      // ON UPDATE CURRENT_TIMESTAMP: NovaDB tự bỏ mệnh đề này, giữ nguyên cho nó nhận ra
      if (!laOnUpdate(t, a, i)) {
        if (dong > 0) { out.push(GIO_HIEN_TAI); i = dong + 1; continue }
        if (w !== 'NOW' && w !== 'SYSDATE') { out.push(GIO_HIEN_TAI); i++; continue }
      }
    } else if (w === 'CURDATE' || w === 'CURRENT_DATE') {
      if (dong > 0) { out.push(NGAY_HIEN_TAI); i = dong + 1; continue }
      if (w === 'CURRENT_DATE') { out.push(NGAY_HIEN_TAI); i++; continue }
    } else if ((w === 'DATE_SUB' || w === 'DATE_ADD') && dong > 0) {
      const r = ngayCongTru(t, k + 1, dong, w === 'DATE_SUB')
      if (r) { out.push(r); i = dong + 1; continue }
    } else if (w === 'PASSWORD' && dong > 0) {
      out.push(`CONCAT('*',UPPER(SHA1(UNHEX(SHA1(${viet(t, k + 1, dong, ddl)})))))`)
      i = dong + 1
      continue
    } else if (w === 'TIMESTAMPDIFF' && dong > 0) {
      const r = timestampDiff(t, k + 1, dong, ddl)
      if (r) { out.push(r); i = dong + 1; continue }
    } else if (w === 'UNIX_TIMESTAMP' && dong > 0 && keTiep(t, k + 1, dong) >= 0) {
      out.push(`(CAST(strftime('%s',${viet(t, k + 1, dong, ddl)}) AS INTEGER)-(${LECH}))`)
      i = dong + 1
      continue
    } else if (w === 'FROM_UNIXTIME' && dong > 0 && tachPhay(t, k + 1, dong).length === 1) {
      out.push(`(datetime(${viet(t, k + 1, dong, ddl)},'unixepoch','${LECH} seconds'))`)
      i = dong + 1
      continue
    } else if (w === 'SUBSTRING_INDEX' && dong > 0) {
      const r = substringIndexCuoi(t, k + 1, dong, ddl)
      if (r) { out.push(r); i = dong + 1; continue }
    } else if (ddl && KIEU_CHU.has(w) && truocLaTenCot(t, a, truoc)) {
      i = kieuChu(t, i, b, out)
      continue
    }
    out.push(x.s)
    i++
  }
  return out.join('')
}

/** Viết lại một câu SQL MySQL cho NovaDB. Câu không dùng tới gì ở trên thì trả nguyên văn. */
export function chuyenSqlNovaDB(sql: string): string {
  const daNho = nho.get(sql)
  if (daNho !== undefined) return daNho
  const H = sql.toUpperCase()
  let kq = sql
  if (CAN_XEM.some(s => H.includes(s))) {
    const t = tach(sql)
    const ddl = laDdlBang(t)
    if (ddl) doiTenIndex(t)
    kq = viet(t, 0, t.length, ddl)
  }
  if (nho.size < NHO_TOI_DA) nho.set(sql, kq)
  return kq
}

type HamQuery = (sql: any, values?: any) => Promise<any>

const hai = (n: number) => String(n).padStart(2, '0')
function ngayGioMay(d: Date): string {
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())} `
    + `${hai(d.getHours())}:${hai(d.getMinutes())}:${hai(d.getSeconds())}`
}

/** Bọc query/execute của pool (và của connection lấy từ pool) để SQL đi qua chuyenSqlNovaDB. */
export function bocPoolNovaDB(pool: Pool): Pool {
  const queryGoc = pool.query.bind(pool) as HamQuery
  // Hỏi VERSION() một lần, bằng hàm gốc (chưa bọc). Lỗi kết nối thì hỏi lại lần sau.
  let laNova: Promise<boolean> | null = null
  const kiemTra = () => {
    laNova ??= queryGoc({ sql: 'SELECT VERSION()', rowsAsArray: true })
      .then(([rows]: any) => String(rows?.[0]?.[0] ?? '').includes('NovaDB'))
      .catch((e: unknown) => { laNova = null; throw e })
    return laNova
  }
  const doiSql = (sql: any) => {
    if (typeof sql === 'string') return chuyenSqlNovaDB(sql)
    if (sql && typeof sql.sql === 'string') return { ...sql, sql: chuyenSqlNovaDB(sql.sql) }
    return sql
  }
  // mysql2 gửi Date kèm mili giây; MySQL tự cắt theo cột DATETIME, NovaDB (SQLite) lưu nguyên chuỗi
  // → so sánh chuỗi ngày giờ lệch. Đổi sẵn thành 'YYYY-MM-DD HH:MM:SS' giờ máy (như mysql2 làm).
  const doiThamSo = (values: any) => !Array.isArray(values)
    ? values
    : values.map(v => v instanceof Date ? ngayGioMay(v) : v)
  const boc = (goc: HamQuery): HamQuery => async (sql, values) => {
    if (!(await kiemTra())) return goc(sql, values)
    return goc(doiSql(sql), doiThamSo(values))
  }

  pool.query = boc(queryGoc) as any
  pool.execute = boc(pool.execute.bind(pool) as HamQuery) as any
  const layGoc = pool.getConnection.bind(pool)
  const daBoc = new WeakSet<PoolConnection>()
  pool.getConnection = async () => {
    const conn = await layGoc()
    if (!daBoc.has(conn)) {
      conn.query = boc(conn.query.bind(conn) as HamQuery) as any
      conn.execute = boc(conn.execute.bind(conn) as HamQuery) as any
      daBoc.add(conn)
    }
    return conn
  }
  return pool
}
