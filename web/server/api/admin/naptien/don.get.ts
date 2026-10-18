/**
 * Danh sách đơn nạp (khu quản trị). Query: trang_thai, loai, q (mã đơn / tài khoản / nhân vật / #id), page.
 * -> { dons, tong, page, perPage }
 */
import type { DonNap } from '../../../utils/napTien'
import { LOAI_GOI, TRANG_THAI_NAP } from '../../../utils/napTienThuan'

const MOI_TRANG = 50

export default defineAdminHandler(async (event) => {
  if (!(await napCoBang())) napThieuBang()
  const query = getQuery(event)
  const trangThai = str(query.trang_thai)
  const loai = str(query.loai)
  const tim = str(query.q).trim().slice(0, 50)
  const page = Math.max(1, int(query.page, 1))
  await napHetHanDonCu()
  const where: string[] = []
  const params: unknown[] = []
  if ((TRANG_THAI_NAP as readonly string[]).includes(trangThai)) {
    where.push('trang_thai = ?')
    params.push(trangThai)
  }
  if ((LOAI_GOI as readonly string[]).includes(loai)) {
    where.push('loai = ?')
    params.push(loai)
  }
  if (tim !== '') {
    const m = tim.match(/^#?(\d+)$/)
    if (/^HT/i.test(tim)) {
      where.push('ma_don LIKE ?')
      params.push(`${tim.toUpperCase().replace(/[\\%_]/g, c => `\\${c}`)}%`)
    } else if (m) {
      where.push('(id = ? OR user_id = ? OR char_id = ?)')
      params.push(Number(m[1]), Number(m[1]), Number(m[1]))
    } else {
      where.push('(username = ? OR charname = ?)')
      params.push(tim.toLowerCase(), tim.toLowerCase())
    }
  }
  const w = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const tong = Number(await qv(dbGame(), `SELECT COUNT(*) FROM nap_don ${w}`, params) ?? 0)
  const dons = await q<DonNap>(dbGame(),
    `SELECT ${NAP_DON_COT} FROM nap_don ${w} ORDER BY id DESC LIMIT ${MOI_TRANG} OFFSET ${Math.trunc((page - 1) * MOI_TRANG)}`, params)
  return { dons, tong, page, perPage: MOI_TRANG }
})
