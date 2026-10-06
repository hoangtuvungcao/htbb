/**
 * Đọc trạng thái điều khiển server.
 * Bảng `dieu_khien_server` chỉ có MỘT dòng (id=1), watcher trên VPS ghi phần thực tế.
 */
export default defineAdminHandler(async () => {
  let coBang = true
  let loiDoc = ''
  let tt: DieuKhienDong | null = null
  try {
    tt = await q1<DieuKhienDong>(
      dbGame(),
      `SELECT muon_chay, gio_hen, da_ap_dung, trang_thai_thuc_te, lan_kiem_cuoi,
              yeu_cau_boi, yeu_cau_luc,
              TIMESTAMPDIFF(SECOND, lan_kiem_cuoi, NOW()) AS giay_tu_lan_kiem,
              (gio_hen IS NOT NULL AND gio_hen > NOW()) AS dang_hen_gio
         FROM dieu_khien_server WHERE id=1`,
    )
  } catch (e: any) {
    coBang = false
    loiDoc = String(e?.message ?? e)
  }

  return {
    coBang,
    loiDoc,
    tt,
    watcherSong: dieuKhienWatcherSong(tt),
    dangCho: dieuKhienDangCho(tt),
    xacNhanTat: DIEU_KHIEN_XAC_NHAN_TAT,
    nguongImLangGiay: DIEU_KHIEN_NGUONG_IM_LANG_GIAY,
  }
})
