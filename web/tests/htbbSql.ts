/**
 * MỘT tệp CSDL `sql/htbb.sql` (sql/README.md) cho test: phần A "CSDL đầy đủ" (schema + dữ liệu thiết kế, như bản dump
 * đầy đủ cũ) và phần B "nâng cấp CSDL đang chạy" (từng là sql/nang_cap.sql), ngăn bởi dòng `-- ######## B. NANG CAP`.
 * Bản Java cùng việc: com.htbb.dulieu.DocDumpSql.phanDayDu / phanNangCap.
 */
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

export const MOC_NANG_CAP = '-- ######## B. NANG CAP'

let toanVan: string | undefined

/** Toàn văn sql/htbb.sql. */
export function htbbSql(): string {
  toanVan ??= readFileSync(fileURLToPath(new URL('../../sql/htbb.sql', import.meta.url)), 'utf8')
  return toanVan
}

/** Phần A (CSDL đầy đủ): dòng bảng thiết kế như khi nạp vào CSDL trống (docBangSql đọc được). */
export function phanDayDu(): string {
  const s = htbbSql()
  const i = s.indexOf(`\n${MOC_NANG_CAP}`)
  return i < 0 ? s : s.slice(0, i + 1)
}

/** Phần B (nâng cấp CSDL đang chạy) - từ dòng MOC_NANG_CAP tới hết tệp. */
export function phanNangCap(): string {
  const s = htbbSql()
  const i = s.indexOf(`\n${MOC_NANG_CAP}`)
  return i < 0 ? '' : s.slice(i + 1)
}
