/** Ghi yêu cầu BẬT server ngay. Watcher trên VPS áp dụng ở lần chạy kế tiếp (≤ ~1 phút). */
export default defineAdminHandler(async (event, admin) => {
  try {
    await exec(dbGame(), `UPDATE dieu_khien_server SET muon_chay=1, gio_hen=NULL, da_ap_dung=0,
                          yeu_cau_boi=?, yeu_cau_luc=NOW() WHERE id=1`, [admin.username])
  } catch (e: any) {
    throw apiLoi(500, `Không thao tác được (có thể chưa có bảng dieu_khien_server): ${e?.message ?? e}`)
  }
  await ghiNhatKy(event, 'server_bat', 'kpah-game', 'bật ngay')
  return { ok: true, message: 'Đã ghi yêu cầu BẬT server. Watcher trên VPS sẽ áp dụng trong vòng 1 phút.' }
})
