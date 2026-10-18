/**
 * Ai đã nhận mã (giftcode_log). Query: ?ma=&player=&trang=
 * 40 dòng một trang; lấy dư 1 dòng để biết có trang sau hay không, thay vì đếm COUNT(*)
 * trên một bảng có thể rất lớn.
 */
const LOG_MOI_TRANG = 40

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const ma = str(query.ma)
  const player = str(query.player)
  const trang = Math.max(1, Math.min(1_000_000, gcSoNguyen(query.trang) || 1))

  const dieuKien: string[] = []
  const thamSo: unknown[] = []
  if (ma !== '') { dieuKien.push('giftcode = ?'); thamSo.push(ma) }
  if (player !== '') { dieuKien.push('player = ?'); thamSo.push(player) }
  const where = dieuKien.length ? `WHERE ${dieuKien.join(' AND ')}` : ''

  // LIMIT / OFFSET ghép thẳng bằng số nguyên đã ép kiểu (hằng số do mã tính ra).
  const lay = Math.trunc(LOG_MOI_TRANG + 1)
  const bo = Math.trunc((trang - 1) * LOG_MOI_TRANG)
  const rows = await q(dbGame(),
    `SELECT id, giftcode, player, userId, item, xu, luong, luongK
       FROM giftcode_log ${where} ORDER BY id DESC LIMIT ${lay} OFFSET ${bo}`, thamSo)
  let conTrangSau = false
  if (rows.length > LOG_MOI_TRANG) {
    conTrangSau = true
    rows.pop()
  }
  const moTa = await gcMoTaNhieu(rows.map(r => String(r.item ?? '')))
  return {
    trang,
    conTrangSau,
    rows: rows.map((r, i) => ({
      id: Number(r.id),
      giftcode: String(r.giftcode ?? ''),
      player: String(r.player ?? ''),
      userId: Number(r.userId ?? 0),
      item: String(r.item ?? ''),
      xu: Number(r.xu ?? 0),
      luong: Number(r.luong ?? 0),
      luongK: Number(r.luongK ?? 0),
      moTa: moTa[i] ?? [],
    })),
  }
})
