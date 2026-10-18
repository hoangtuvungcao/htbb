/**
 * Lưu một nhóm cấu hình (mỗi thẻ trên trang là một nhóm).
 * Body: { viec: 'luu_drop' | 'luu_exp_quai' | 'luu_tieu' | 'luu_chiemthanh' | 'luu_lichhen' | 'luu_khac' | 'luu_exp_phu', ...các ô }
 *
 * Token bearer thay cho CSRF (xem server/utils/admin.ts). Chỉ ghi bảng cau_hinh — KHÔNG
 * ghi server.ini; server game tự đọc lại bảng mỗi 10 giây.
 * Nhật ký: ghiThayDoi (trước/sau từng khoá) — xem ở Lịch sử thay đổi.
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

  const truoc = await cauHinhDocKhoa(Object.keys(tt.m))
  // Lỗi CSDL để defineAdminHandler ghi 'loi_web' vào nhật ký và báo lại cho admin.
  await cauHinhLuu(tt.m, admin.username)
  const sau = Object.fromEntries(Object.entries(tt.m).map(([k, v]) => [k, String(v)]))
  await ghiThayDoi(event, 'sua_cau_hinh', `cau_hinh:${tt.doiTuong}`, truoc, sau)
  return { ok: true, message: tt.thongBao }
})
