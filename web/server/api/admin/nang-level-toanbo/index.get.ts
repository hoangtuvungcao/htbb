/** Số liệu trang Nâng Level toàn bộ + 100 lệnh gần nhất (xem server/utils/nangLevelToanBo.ts). */
export default defineAdminHandler(async () => {
  let loiDoc = ''
  let soNhanVat = 0
  let soLenhChoXuLy = 0
  let lenhs: ReturnType<typeof lenhChoGiaoDien>[] = []
  try {
    soNhanVat = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_char WHERE del = 1') ?? 0)

    if (await coBangLenh(dbGame())) {
      soLenhChoXuLy = Number(await qv(dbGame(),
        'SELECT COUNT(*) FROM web_lenh_nhan_vat WHERE loai = ? AND tham_so = ? AND trang_thai IN (?, ?)',
        [LENH_LOAI_DAT_CAP_DO, TANG_LEVEL_TOANBO_DANH_DAU, LENH_CHO, LENH_CHO_DANG_NHAP]) ?? 0)

      const rows = await q<DongLenhNhanVat>(dbGame(),
        'SELECT * FROM web_lenh_nhan_vat WHERE loai = ? AND tham_so = ? ORDER BY id DESC LIMIT 100',
        [LENH_LOAI_DAT_CAP_DO, TANG_LEVEL_TOANBO_DANH_DAU])
      lenhs = rows.map(lenhChoGiaoDien)
    }
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }
  return { capDoToiDa: CAPDO_TOI_DA, soNhanVat, soLenhChoXuLy, lenhs, loiDoc }
})
