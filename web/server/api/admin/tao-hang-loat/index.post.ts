/**
 * Tạo NHIỀU tài khoản (account.team_user) cùng một lượt.
 * Body: { danh_sach: string, tu_kich_hoat: boolean }
 *
 * Mỗi dòng: `tenTaiKhoan` hoặc `tenTaiKhoan:matKhau` (trống mật khẩu thì tự sinh). Xử lý
 * TỪNG DÒNG ĐỘC LẬP — một dòng sai/trùng chỉ bị bỏ qua, không làm hỏng cả lượt (ghi log
 * từng dòng, không gộp một transaction).
 *
 * KHÔNG dùng chặn spam (biChanSpam/ghiLanLam) như form đăng ký công khai: admin đã đăng
 * nhập. Thay vào đó giới hạn cứng số dòng một lượt để tránh dán nhầm tệp khổng lồ.
 *
 * Mật khẩu chỉ trả về ĐÚNG MỘT LẦN trong phản hồi này — cột team_user.password chỉ lưu
 * bản băm một chiều qua PASSWORD(), không đọc lại nguyên văn được nữa.
 */
export default defineAdminHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const raw = typeof body?.danh_sach === 'string' ? body.danh_sach : ''
  const tuKichHoat = body?.tu_kich_hoat === true || body?.tu_kich_hoat === 1 || body?.tu_kich_hoat === '1'

  const dong = tkTachDongTao(raw)
  if (!dong.length) {
    throw apiLoi(400, 'Chưa nhập tài khoản nào (mỗi dòng một tài khoản, dạng "tenTaiKhoan" hoặc "tenTaiKhoan:matKhau").')
  }
  if (dong.length > TK_TAO_HANG_LOAT_TOI_DA) {
    throw apiLoi(400, `Chỉ được tạo tối đa ${TK_TAO_HANG_LOAT_TOI_DA} tài khoản một lượt (đang nhập ${dong.length} dòng). Chia nhỏ ra rồi nhập nhiều lượt.`)
  }

  const ketQua = tkChuanBiTao(dong)
  const acc = dbAccount()

  for (const hang of ketQua) {
    if (hang.loi) continue // đã bị bỏ qua ở bước kiểm (sai tên, trùng trong lô, sai mật khẩu)
    const matKhau = hang.matkhau
    hang.matkhau = ''
    hang.loi = true
    try {
      if ((await qv(acc, 'SELECT 1 FROM team_user WHERE username = ? LIMIT 1', [hang.username])) !== null) {
        hang.trangthai = 'Bỏ qua — tên đã tồn tại trong hệ thống'
        continue
      }
      // Câu INSERT giống hệt đường đăng ký: PASSWORD() để khớp login server.
      const ins = await exec(acc, TK_SQL_INSERT_TEAM_USER, [hang.username, matKhau])
      const userId = Number(ins.insertId)

      let daKichHoat = false
      if (tuKichHoat) daKichHoat = await activateAccount(userId, hang.username)

      // KHÔNG ghi mật khẩu vào nhật ký — nguyên tắc "không ghi lại nội dung" như đổi mật khẩu.
      await ghiNhatKy(event, 'tao_tai_khoan_hang_loat', hang.username,
        `id ${userId}${daKichHoat ? ', đã kích hoạt' : ', chưa kích hoạt'}`)

      hang.matkhau = matKhau
      hang.trangthai = `Đã tạo (#${userId})${daKichHoat ? ', đã kích hoạt' : ''}`
      hang.loi = false
    } catch (e: any) {
      hang.trangthai = `Lỗi: ${e?.message ?? e}`
    }
  }

  return {
    ketQua,
    tongDong: ketQua.length,
    soTao: ketQua.filter(h => !h.loi).length,
  }
})
