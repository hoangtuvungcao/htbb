/**
 * Chẩn đoán & Lỗi — toàn bộ dữ liệu cho trang, trừ nội dung file log
 * (xem file.get.ts).
 *
 *   GET /api/admin/chandoan
 */
export default defineAdminHandler(async () => {
  const kiemTra = await chanDoanChayKiemTra()
  const demMuc: Record<ChanDoanMuc, number> = { loi: 0, canhbao: 0, ok: 0, khong_kiem_duoc: 0 }
  for (const k of kiemTra) demMuc[k.muc]++

  const coBang = await coBangNhatKy()
  return {
    kiemTra,
    demMuc,
    thongKeLoi: await chanDoanThongKeLoi(),
    coBangNhatKy: coBang,
    loiWeb: coBang ? await chanDoanLoiWeb() : [],
    ...chanDoanDanhSachFile(),
  }
})
