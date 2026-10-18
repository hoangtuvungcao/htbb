/**
 * Đăng nhập quản trị — Bước HAI: đã đúng mật khẩu, đang chờ mã 6 số.
 *
 * Body: { pending, code } -> { token, admin }
 *  - `pending` là token 'cho2fa' (5 phút) do /api/admin/auth/login trả về.
 *  - `code` là mã 6 số của ứng dụng xác thực, HOẶC một mã dự phòng "abcd-efgh".
 *
 * Lỗi có `code: 'restart'` = phải quay lại bước một (hết hạn / thiết lập đổi).
 */
function loiCsdl(e: any): never {
  console.error('[htbb-admin] xac thuc hai buoc that bai:', e?.code ?? '', e?.message ?? e)
  // Không đọc/ghi được web_admin_2fa thì từ chối: cho vào lúc này là bỏ qua
  // chống dùng lại mã (hoặc để mã dự phòng dùng được nhiều lần).
  throw apiLoi(503, 'Cơ sở dữ liệu đang lỗi nên chưa kiểm tra được mã. Thử lại sau ít phút.')
}

export default defineEventHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  const cho = docToken(str(body?.pending), 'cho2fa')
  if (!cho) {
    throw apiLoi(401, 'Phiên chờ mã hai bước đã hết hạn. Hãy đăng nhập lại.', { code: 'restart' })
  }
  if (await dangBiChanDoSai(event)) {
    throw apiLoi(429, 'Đã gõ sai quá nhiều lần. Chờ 15 phút rồi thử lại.')
  }

  const maGoVao = str(body?.code)
  const ten = cho.u
  let d: Awaited<ReturnType<typeof hbDong>>
  try {
    d = await hbDong(ten)
  } catch (e) {
    loiCsdl(e)
  }

  if (d === null || Number(d.kich_hoat) !== 1) {
    // Khoá bị gỡ giữa lúc đang đăng nhập (ai đó chạy DELETE để cứu tài khoản).
    // Bắt đầu lại từ đầu chứ không cho đi tiếp — phiên chờ này được tạo dựa trên
    // một điều kiện giờ không còn đúng.
    throw apiLoi(409, 'Thiết lập hai bước của tài khoản này vừa thay đổi. Hãy đăng nhập lại.', { code: 'restart' })
  }
  // Tên bị gỡ khỏi HTBB_ADMIN_ACCOUNTS trong 5 phút chờ: chặn ngay, không cấp
  // token admin cho người vừa bị rút quyền.
  if (!(await vaiTroCua(ten))) {
    throw apiLoi(403, 'Tài khoản này không có quyền vào trang quản trị.', { code: 'restart' })
  }

  const buoc = hbKiemMa(String(d.khoa_bimat), maGoVao, Number(d.buoc_da_dung))
  let ghiDuoc = false
  if (buoc !== null) {
    try {
      ghiDuoc = await hbGhiBuocDaDung(ten, buoc)
    } catch (e) {
      loiCsdl(e)
    }
  }
  if (ghiDuoc) {
    return await hbVaoDuoc(event, { id: cho.id, username: ten }, 'mã hai bước')
  }

  // Không phải mã 6 số thì thử xem có phải mã dự phòng không.
  const chuoiBam = String(d.ma_du_phong ?? '')
  const conLai = buoc === null ? hbDungMaDuPhong(chuoiBam, maGoVao) : null
  if (conLai !== null) {
    let gachDuoc = false
    try {
      gachDuoc = await hbGhiMaDuPhong(ten, conLai, chuoiBam)
    } catch (e) {
      loiCsdl(e)
    }
    if (gachDuoc) {
      // Ghi TRƯỚC khi mở phiên admin, nên cột admin_user để trống — tên tài
      // khoản nằm ở cột đối tượng.
      await ghiNhatKy(event, '2fa_ma_du_phong', ten, `dùng một mã dự phòng, còn lại ${hbDemMaDuPhong(conLai)}`)
      return await hbVaoDuoc(event, { id: cho.id, username: ten }, 'mã dự phòng')
    }
  }

  // SAI MÃ = MỘT LẦN GÕ SAI. Bắt buộc phải đếm: mã chỉ có 6 chữ số, tức một
  // triệu khả năng. Không tính vào bộ đếm khoá thì một script dò được cả dải
  // trong vài giờ, và 2FA thành đồ trang trí.
  await ghiNhatKyDangNhapThatBai(event, ten, 'sai mã hai bước')
  throw apiLoi(401, 'Mã không đúng. Kiểm tra lại đồng hồ điện thoại, hoặc dùng một mã dự phòng.')
})
