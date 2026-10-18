/**
 * Bật / tắt bảo trì đăng nhập (chỉ siêu quản trị — quy tắc /api/admin/he-thong/** ghi: chỉ siêu QT).
 * Body: { bat: true, cau?: string (≤ 200 ký tự, một dòng), cho?: string[] (tài khoản [a-z0-9_]{3,20}) } | { bat: false }
 * Game kiểm tệp mỗi 5 giây; KHÔNG đá người đang chơi. Ghi lịch sử thay đổi trước/sau.
 */
export default defineAdminHandler(async (event) => {
  const body = await readBody(event).catch(() => null)
  const kq = baoTriKiem(body)
  if (!kq.ok) throw apiLoi(400, kq.loi)
  const truoc = await trangThaiBaoTriTep()
  const sau = kq.bat ? kq.noiDung : null
  await ghiBaoTriTep(sau)
  await ghiThayDoi(event, kq.bat ? 'bat_bao_tri' : 'tat_bao_tri', 'bao_tri.txt',
    truoc.coTep ? { bat: true, ...truoc.noiDung } : { bat: false },
    sau ? { bat: true, ...sau } : { bat: false })
  return {
    ok: true,
    message: kq.bat
      ? 'Đã bật bảo trì. Trong tối đa 5 giây máy chủ từ chối đăng nhập mới (người đang chơi không bị đá).'
      : 'Đã tắt bảo trì. Trong tối đa 5 giây máy chủ mở lại đăng nhập.',
  }
})
