/**
 * Log game — đọc một trong các bảng log mà game server tự ghi.
 *
 *   GET /api/admin/log?bang=tob_log_all_item&loc=<nhân vật>&loc2=<loại>&trang=1
 *
 * Chỉ ĐỌC. `bang` lạ thì lùi về bảng mặc định (không bao giờ ghép tên bảng từ
 * người dùng). Bảng thiếu trên máy chủ này -> trả `loiDoc` để trang hiện thông báo,
 * không coi là lỗi trang web.
 */
export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  let bang = str(query.bang)
  if (!Object.prototype.hasOwnProperty.call(LOG_GAME_BANG, bang)) bang = LOG_GAME_MAC_DINH
  const ch = LOG_GAME_BANG[bang]!
  const loc = str(query.loc)
  const loc2 = str(query.loc2)
  const trang = Math.min(1_000_000, Math.max(1, int(query.trang, 1)))

  // Bảng theo kỳ (tob_log_use_luong_<tuần>_<tháng>_<năm>): liệt kê bảng có thật,
  // ?ky= chọn một (phải nằm trong danh sách), mặc định kỳ mới nhất.
  let dsKy: { bang: string, nhan: string }[] = []
  let ky = ''
  if (ch.theoKy) {
    try {
      const ten = await q<{ t: string }>(dbGame(),
        'SELECT table_name AS t FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE ?',
        [`${bang.replace(/_/g, '\\_')}\\_%`])
      dsKy = logGameDsKy(bang, ch.theoKy, ten.map(r => String(r.t)))
    } catch { /* không đọc được information_schema: dùng bảng mẫu */ }
    const chon = str(query.ky)
    ky = dsKy.some(d => d.bang === chon) ? chon : (dsKy[0]?.bang ?? '')
  }

  const { bangThat, sql, thamSo } = logGameCauSql(bang, loc, loc2, trang, ky || undefined)

  let dong: Record<string, unknown>[] = []
  let conTrangSau = false
  let loiDoc = ''
  try {
    dong = await q(dbGame(), sql, thamSo)
    if (dong.length > LOG_GAME_MOI_TRANG) {
      conTrangSau = true
      dong.pop()
    }
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }

  return {
    bang,
    bangThat,
    dsKy,
    ky,
    dsBang: Object.entries(LOG_GAME_BANG).map(([key, v]) => ({ key, ten: v.ten })),
    cauHinh: {
      ten: ch.ten,
      mota: ch.mota,
      cot: Object.entries(ch.cot).map(([key, nhan]) => ({ key, nhan, so: LOG_GAME_COT_SO.includes(key) })),
      loc: ch.loc,
      loc2: ch.loc2,
      nhanLoc: ch.loc !== null ? (ch.cot[ch.loc] ?? 'Lọc') : null,
      nhanLoc2: ch.loc2 !== null ? (ch.cot[ch.loc2] ?? 'Lọc 2') : null,
    },
    loc,
    loc2,
    trang,
    dong,
    conTrangSau,
    loiDoc,
  }
})
