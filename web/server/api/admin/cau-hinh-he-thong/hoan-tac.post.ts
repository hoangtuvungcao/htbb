/**
 * Hoàn tác một dòng lịch sử: ghi lại giá trị CŨ của dòng đó (qua đúng luật kiểm + lịch sử mới).
 * Body: { id, lyDo }. Khoá nhạy cảm (đã che trong lịch sử) không hoàn tác được. Chỉ siêu quản trị.
 */
export default defineAdminHandler(async (event, admin) => {
  const b = (await readBody<Record<string, unknown>>(event).catch(() => null)) ?? {}
  const id = int(b.id, 0)
  if (id <= 0) throw apiLoi(400, 'Thiếu mã dòng lịch sử.')
  await chTaoBang()
  const d = await q1<{ id: number, khoa: string, gia_tri_cu: string | null, gia_tri_moi: string | null }>(dbGame(),
    'SELECT id, khoa, gia_tri_cu, gia_tri_moi FROM cau_hinh_lich_su WHERE id = ?', [id])
  if (!d) throw apiLoi(404, 'Không có dòng lịch sử này.')
  const k = khoaTT(d.khoa)
  if (!k) throw apiLoi(400, 'Khoá không còn trong danh mục — không hoàn tác được.')
  if (k.nhay) throw apiLoi(400, 'Khoá nhạy cảm: giá trị cũ đã bị che, sửa tay ở trang.')
  const ghiChu = str(b.lyDo).trim()
  const lyDo = (ghiChu ? `${ghiChu} (hoàn tác #${id})` : `Hoàn tác #${id}`).slice(0, 255)
  const kq = await chLuu(event, admin, { [d.khoa]: d.gia_tri_cu }, lyDo, 'web:hoan_tac')
  return { ok: true, ...kq, message: kq.thayDoi.length ? `Đã hoàn tác ${k.ten} về ${hienThiTT(k, d.gia_tri_cu)}.` : 'Giá trị hiện tại đã giống giá trị cũ.' }
})
