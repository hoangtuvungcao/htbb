/**
 * Công bố chính sách HTBB_AUTO_ACTIVATE cho server game: ghi htbb_db.cau_hinh khoá 'web.autoActivate'
 * = '1' / '0' lúc web khởi động (và mỗi giờ, phòng bảng bị dọn).
 *
 * VÌ SAO: đăng ký trong game (lệnh 39, HTBB/src/real/cmd/DangKyTrongGame.java) phải kích hoạt
 * 5h_active ĐÚNG như web. Server game không đọc được /etc/htbb-web.env (chứa mật khẩu, user khác),
 * nên web tự công bố giá trị vào bảng chung. server.ini sv.dangKyTuKichHoat=web (mặc định) đọc khoá
 * này; đặt 1/0 thì server game dùng giá trị cứng và bỏ qua khoá.
 *
 * Khoá 'web.*' KHÔNG nằm trong whitelist CAU_HINH_K nên trang Cấu hình không sửa được nó (đúng ý:
 * nguồn thật là biến môi trường).
 */
export default defineNitroPlugin(() => {
  if (import.meta.prerender) return
  const chay = async () => {
    try {
      if (!(await coBang(dbGame(), 'cau_hinh'))) return
      const v = useHtbbConfig().site.autoActivate ? '1' : '0'
      await exec(dbGame(),
        `INSERT INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES ('web.autoActivate', ?, 'web', NOW())
         ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), sua_boi = VALUES(sua_boi), sua_luc = VALUES(sua_luc)`,
        [v])
    }
    catch { /* CSDL chưa sẵn sàng: thử lại lần sau */ }
  }
  setTimeout(chay, 5_000)
  setInterval(chay, 60 * 60_000).unref?.()
})
