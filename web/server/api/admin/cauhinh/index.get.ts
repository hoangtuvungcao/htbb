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
  // Bảng đầy đủ mọi khoá (kể cả khoá chưa có dòng nhưng nằm trong danh mục) cho phần "Tất cả khoá".
  const tatCa = new Set([...Object.keys(gt), ...Object.keys(DANH_MUC_CAU_HINH)])
  const dong = [...tatCa].sort().map((khoa) => {
    const m = DANH_MUC_CAU_HINH[khoa]
    return {
      khoa,
      coDong: khoa in gt,
      giaTri: khoa in gt ? gt[khoa]! : null,
      suaBoi: suaBoi[khoa]?.[0] || null,
      suaLuc: suaBoi[khoa]?.[1] || null,
      ten: m?.ten ?? '',
      moTa: m?.moTa ?? (khoa.includes('.') && !khoa.startsWith('sv.') ? 'Khoá đè số liệu dài hạn chưa có trong danh mục — game đọc nếu có dùng (LauDai.so).' : 'Khoá chưa có trong danh mục web.'),
      nhom: m?.nhom ?? 'Chưa phân loại',
      kieu: m?.kieu ?? 'chuoi',
      min: m?.min ?? null,
      max: m?.max ?? null,
      macDinh: m?.macDinh ?? null,
      suaLe: !!m?.suaLe,
      the: m?.the ?? null,
      nguon: m?.nguon ?? '',
    }
  })
  return {
    loiDoc,
    dong,
    form: cauHinhGiaTriForm(gt),
    suaLanCuoi,
    cacThu: CAU_HINH_CAC_THU,
  }
})
