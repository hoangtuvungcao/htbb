/** Số liệu trang Reset toàn server (xem server/utils/resetToanBo.ts). */
export default defineAdminHandler(async () => {
  let loiDoc = ''
  let soNhanVat = 0
  let soTaiKhoan = 0
  let soLenhChoXuLy = 0
  const dsAdmin = adminAccounts()

  try {
    const idsAdmin = await resetIdTaiKhoanAdmin()
    const [dk, th] = resetDieuKienNgoaiAdmin(idsAdmin)

    soNhanVat = Number(await qv(dbGame(), `SELECT COUNT(*) FROM tob_char${dk}`, th) ?? 0)
    soTaiKhoan = Number(await qv(dbGame(), `SELECT COUNT(DISTINCT userId) FROM tob_char${dk}`, th) ?? 0)

    if (await coBangLenh(dbGame())) {
      soLenhChoXuLy = Number(await qv(dbGame(),
        'SELECT COUNT(*) FROM web_lenh_nhan_vat WHERE loai = ? AND trang_thai = ?',
        [LENH_LOAI_XOA_NHAN_VAT, LENH_CHO]) ?? 0)
    }
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }

  return { soNhanVat, soTaiKhoan, soLenhChoXuLy, dsAdmin, cumTu: RESET_TOANBO_CUM_TU, loiDoc }
})
