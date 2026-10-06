/**
 * Lưu một nhóm cấu hình (mỗi thẻ trên trang là một nhóm).
 * Body: { viec: 'luu_drop' | 'luu_exp_quai' | 'luu_tieu' | 'luu_chiemthanh' | 'luu_lichhen' | 'luu_khac' | 'luu_exp_phu', ...các ô }
 *
 * Token bearer thay cho CSRF (xem server/utils/admin.ts). Chỉ ghi bảng cau_hinh — KHÔNG
 * ghi server.ini; server game tự đọc lại bảng mỗi 10 giây.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = (await readBody<Record<string, unknown>>(event).catch(() => null)) ?? {}
  const viec = str(body.viec)

  let tt: CauHinhLuuKetQua
  try {
    tt = cauHinhDungThaoTac(viec, body)
  } catch (e: any) {
    throw apiLoi(400, String(e?.message ?? e))
  }

  // Lỗi CSDL để defineAdminHandler ghi 'loi_web' vào nhật ký và báo lại cho admin.
  await cauHinhLuu(tt.m, admin.username)
  await ghiNhatKy(event, 'sua_cau_hinh', tt.doiTuong, cauHinhJson(tt.m))
  return { ok: true, message: tt.thongBao }
})
