/**
 * Lưu nhiều khoá cấu hình (tất cả hoặc không). Body: { thayDoi: { [khoa]: giá trị | null }, lyDo: string, phienBan?: number }
 * null = xoá dòng (máy chủ dùng server.ini / mặc định). Kiểm kiểu / khoảng / lựa chọn (400 kèm `loi` từng khoá),
 * từ chối khoá env / chỉ đọc / thử nghiệm (máy thật), 409 khi phiên bản đã đổi. Chỉ siêu quản trị.
 */
export default defineAdminHandler(async (event, admin) => {
  const b = (await readBody<Record<string, unknown>>(event).catch(() => null)) ?? {}
  const thayDoi = b.thayDoi && typeof b.thayDoi === 'object' && !Array.isArray(b.thayDoi) ? b.thayDoi as Record<string, unknown> : null
  if (!thayDoi || !Object.keys(thayDoi).length) throw apiLoi(400, 'Không có thay đổi nào.')
  if (Object.keys(thayDoi).length > 200) throw apiLoi(400, 'Quá nhiều khoá trong một lần lưu.')
  const lyDo = str(b.lyDo).trim().replace(/\s+/g, ' ').slice(0, 255)
  if (Array.from(lyDo).length < 3) throw apiLoi(400, 'Ghi lý do thay đổi (ít nhất 3 ký tự) — lưu vào lịch sử.')
  const pb = b.phienBan === undefined || b.phienBan === null ? undefined : int(b.phienBan, -1)
  const kq = await chLuu(event, admin, thayDoi, lyDo, 'web', pb)
  return {
    ok: true,
    ...kq,
    message: kq.thayDoi.length
      ? `Đã lưu ${kq.thayDoi.length} khoá (phiên bản ${kq.phienBan}). Máy chủ áp dụng trong ≤ 10 giây${kq.canKhoiDongLai.length ? `; ${kq.canKhoiDongLai.length} khoá cần khởi động lại` : ''}.`
      : 'Không có gì thay đổi.',
  }
})
