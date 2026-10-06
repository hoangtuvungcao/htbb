/**
 * Đọc bảng cau_hinh cho trang Cấu hình server (chỉ admin).
 * Lỗi đọc không làm vỡ trang: trả về loiDoc để trang hiện "Lỗi đọc dữ liệu: ..." và
 * các ô vẫn điền giá trị mặc định.
 */
export default defineAdminHandler(async () => {
  let gt: Record<string, string> = {}
  let suaBoi: Record<string, [string, string]> = {}
  let loiDoc = ''
  try {
    ({ gt, suaBoi } = await cauHinhDocBang())
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }
  const suaLanCuoi: Record<string, { boi: string, luc: string } | null> = {}
  for (const [nhom, khoas] of Object.entries(CAU_HINH_NHOM)) {
    suaLanCuoi[nhom] = cauHinhSuaMoiNhat(suaBoi, khoas)
  }
  return {
    loiDoc,
    form: cauHinhGiaTriForm(gt),
    suaLanCuoi,
    cacThu: CAU_HINH_CAC_THU,
  }
})
