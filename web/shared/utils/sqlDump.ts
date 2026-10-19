/**
 * Đọc dòng của một bảng trong tệp SQL (mysqldump `sql/htbb_day_du.sql` hoặc tệp sinh `du_lieu/sql_nguon/pokemon_*.sql`).
 *
 * Thuần (không fs, không CSDL): nhận chuỗi SQL, trả mảng object { cột: giá trị }. Dùng cho
 *   - test đối chiếu (tests/du-lieu-sql.test.ts): biến đổi dòng SQL phải ra đúng JSON đóng gói;
 *   - không dùng lúc chạy web (lúc chạy đọc thẳng CSDL qua server/utils/duLieuSql.ts).
 *
 * Hỗ trợ: `CREATE TABLE \`t\` (...)` (lấy thứ tự cột), `INSERT [IGNORE] INTO \`t\` [(cột...)] VALUES (...),(...);`,
 * `DELETE FROM \`t\`;` / `DROP TABLE` (xoá dòng đã gom — giống chạy tuần tự). Chuỗi: thoát bằng `\` (mysqldump)
 * và `''`. Số: trả number; NULL: null.
 */

export type DongSql = Record<string, string | number | null>

function docGiaTri(s: string, i: number): [string | number | null, number] {
  while (s[i] === ' ' || s[i] === '\n' || s[i] === '\r' || s[i] === '\t') i++
  if (s[i] === '\'') {
    let o = ''
    i++
    for (;;) {
      const c = s[i]
      if (c === undefined) throw new Error('sqlDump: chuỗi không đóng')
      if (c === '\\') {
        const d = s[i + 1]
        o += d === 'n' ? '\n' : d === 'r' ? '\r' : d === 't' ? '\t' : d === '0' ? '\0' : d === 'Z' ? '\x1a' : d
        i += 2
      }
      else if (c === '\'') {
        if (s[i + 1] === '\'') { o += '\''; i += 2 }
        else { i++; break }
      }
      else { o += c; i++ }
    }
    return [o, i]
  }
  let j = i
  while (j < s.length && s[j] !== ',' && s[j] !== ')') j++
  const tho = s.slice(i, j).trim()
  if (/^null$/i.test(tho)) return [null, j]
  const so = Number(tho)
  if (tho === '' || Number.isNaN(so)) throw new Error(`sqlDump: giá trị lạ "${tho}"`)
  return [so, j]
}

/** Các bộ (v1, v2, ...) sau VALUES, tới dấu `;` ngoài chuỗi. */
function docCacBo(s: string, i: number): [(string | number | null)[][], number] {
  const ra: (string | number | null)[][] = []
  for (;;) {
    while (/\s|,/.test(s[i] ?? '')) i++
    if (s[i] === ';' || i >= s.length) return [ra, i + 1]
    if (s[i] !== '(') throw new Error(`sqlDump: mong "(" ở ${i}`)
    i++
    const bo: (string | number | null)[] = []
    for (;;) {
      const [v, j] = docGiaTri(s, i)
      bo.push(v)
      i = j
      while (s[i] === ' ' || s[i] === '\n') i++
      if (s[i] === ',') { i++; continue }
      if (s[i] === ')') { i++; break }
      throw new Error(`sqlDump: mong "," hoặc ")" ở ${i}`)
    }
    ra.push(bo)
  }
}

/** Dòng của bảng `bang` sau khi "chạy" tuần tự tệp SQL (DROP/DELETE xoá, INSERT thêm). */
export function docBangSql(sql: string, bang: string): DongSql[] {
  const ten = `\`${bang}\``
  let cot: string[] = []
  let dong: DongSql[] = []
  const re = new RegExp(`(CREATE TABLE(?: IF NOT EXISTS)? ${ten} \\(|INSERT(?: IGNORE)? INTO ${ten}|DELETE FROM ${ten}|DROP TABLE IF EXISTS ${ten})`, 'g')
  let m: RegExpExecArray | null
  while ((m = re.exec(sql)) !== null) {
    const loai = m[1]!
    if (loai.startsWith('DROP') || loai.startsWith('DELETE')) {
      dong = []
      continue
    }
    if (loai.startsWith('CREATE')) {
      // Cột: mỗi `ten` đứng đầu một định nghĩa (bỏ PRIMARY KEY / KEY ...), tới ")" đóng ngoài cùng.
      let i = m.index + m[0].length
      let sau = 1
      const dau = i
      while (i < sql.length && sau > 0) {
        if (sql[i] === '(') sau++
        else if (sql[i] === ')') sau--
        else if (sql[i] === '\'') { i++; while (sql[i] !== '\'') i += sql[i] === '\\' ? 2 : 1 }
        i++
      }
      const than = sql.slice(dau, i - 1)
      // Tách theo dấu phẩy ở mức 0.
      const phan: string[] = []
      let buf = ''
      let d = 0
      for (const c of than) {
        if (c === '(') d++
        else if (c === ')') d--
        if (c === ',' && d === 0) { phan.push(buf); buf = '' }
        else buf += c
      }
      phan.push(buf)
      cot = phan.map(p => /^\s*`([^`]+)`/.exec(p)?.[1]).filter((x): x is string => !!x)
      re.lastIndex = i
      continue
    }
    // INSERT
    let i = m.index + m[0].length
    let cotIns = cot
    const mc = /^\s*\(([^)]*)\)\s*VALUES/i.exec(sql.slice(i, i + 4000))
    if (mc) {
      cotIns = [...mc[1]!.matchAll(/`([^`]+)`/g)].map(x => x[1]!)
      i += mc[0].length
    }
    else {
      const mv = /^\s*VALUES/i.exec(sql.slice(i, i + 20))
      if (!mv) throw new Error(`sqlDump: INSERT ${bang} thiếu VALUES`)
      i += mv[0].length
    }
    const [bo, j] = docCacBo(sql, i)
    for (const b of bo) {
      if (b.length !== cotIns.length) throw new Error(`sqlDump: ${bang} có ${b.length} giá trị, ${cotIns.length} cột`)
      dong.push(Object.fromEntries(cotIns.map((c, k) => [c, b[k]!])))
    }
    re.lastIndex = j
  }
  return dong
}
