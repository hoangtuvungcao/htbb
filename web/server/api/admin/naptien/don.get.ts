/**
 * Danh sách đơn nạp mốc cho khu quản trị.
 * Query: trang_thai ('' | cho_thanh_toan | da_thanh_toan | da_nhan), moc (0-3),
 *        q (mã đơn, tên tài khoản hoặc số #id đơn), page (từ 1)
 * -> { dons, tong, page, perPage, thongKe, sepay }
 */
const MOI_TRANG = 50
const TRANG_THAI = ['cho_thanh_toan', 'da_thanh_toan', 'da_nhan']

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const trangThai = str(query.trang_thai)
  const moc = int(query.moc, 0)
  const tim = str(query.q).slice(0, 50)
  const page = Math.max(1, int(query.page, 1))

  if (!(await coBang(dbGame(), 'tob_nap_moc'))) {
    throw apiLoi(409, 'Chưa có bảng tob_nap_moc — chạy PHẦN 2I của vps/db_new_server.sql trước.', { code: 'thieu_bang' })
  }

  const where: string[] = []
  const params: unknown[] = []
  if (TRANG_THAI.includes(trangThai)) {
    where.push('trang_thai = ?')
    params.push(trangThai)
  }
  if ([1, 2, 3].includes(moc)) {
    where.push('moc = ?')
    params.push(moc)
  }
  if (tim !== '') {
    const m = tim.match(/^#?(\d+)$/)
    if (/^KP/i.test(tim)) {
      where.push('ma_don LIKE ?')
      // Thoát % và _ để chúng là ký tự thường, không phải ký tự đại diện của LIKE.
      params.push(`${tim.toUpperCase().replace(/[\\%_]/g, c => `\\${c}`)}%`)
    } else if (m) {
      where.push('(id = ? OR userID = ?)')
      params.push(Number(m[1]), Number(m[1]))
    } else {
      // Tên tài khoản nằm ở DB account — tra id trước rồi lọc theo userID.
      const uid = await qv<number>(dbAccount(), 'SELECT id FROM team_user WHERE username = ? LIMIT 1', [tim.toLowerCase()])
      where.push('userID = ?')
      params.push(uid === null ? -1 : Number(uid))
    }
  }
  const w = where.length ? `WHERE ${where.join(' AND ')}` : ''
  const offset = Math.trunc((page - 1) * MOI_TRANG)

  const tong = Number(await qv(dbGame(), `SELECT COUNT(*) FROM tob_nap_moc ${w}`, params) ?? 0)
  const dons = await q<DonNapMoc>(
    dbGame(),
    `SELECT ${NAP_MOC_COT} FROM tob_nap_moc ${w} ORDER BY created_at DESC, id DESC LIMIT ${MOI_TRANG} OFFSET ${offset}`,
    params,
  )

  // Số liệu toàn bảng (không theo bộ lọc).
  const tk = await q<{ trang_thai: string, so: number, tien: number }>(
    dbGame(),
    'SELECT trang_thai, COUNT(*) AS so, COALESCE(SUM(amount), 0) AS tien FROM tob_nap_moc GROUP BY trang_thai',
  )
  const thongKe: Record<string, { so: number, tien: number }> = {}
  for (const r of tk) thongKe[r.trang_thai] = { so: Number(r.so), tien: Number(r.tien) }

  return {
    dons: await napMocGanTen(dons),
    tong,
    page,
    perPage: MOI_TRANG,
    thongKe,
    sepay: useHtbbConfig().nap.sepayApiKey !== '',
  }
})
