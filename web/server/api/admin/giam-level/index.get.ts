/** 100 lệnh giảm Level gần nhất (trang /admin/giam-level). */
export default defineAdminHandler(async () => {
  let lenhs: ReturnType<typeof lenhChoGiaoDien>[] = []
  let loi = ''
  try {
    if (await coBangLenh(dbGame())) {
      const rows = await q<DongLenhNhanVat>(dbGame(),
        'SELECT * FROM web_lenh_nhan_vat WHERE loai = ? ORDER BY id DESC LIMIT 100', [LENH_LOAI_GIAM_CAP_DO])
      lenhs = rows.map(lenhChoGiaoDien)
    }
  } catch (e: any) {
    loi = String(e?.message ?? e)
  }
  return { capDoToiDa: CAPDO_TOI_DA, lenhs, loi }
})
