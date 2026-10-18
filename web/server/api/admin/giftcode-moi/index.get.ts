import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Mã an toàn + giới hạn + ngân sách tháng đã dùng. coBang=false -> trang dùng giftcode kiểu cũ. */
/** Mã quà cá nhân ("qr…", tạo ở Trung tâm người chơi) là bí mật của người nhận: vai trò khác siêu quản trị chỉ thấy bản che. */
function cheMa(ma: unknown): string {
  const s = String(ma ?? '')
  return s.toLowerCase().startsWith('qr') && s.length > 6 ? `${s.slice(0, 4)}${'•'.repeat(Math.max(4, s.length - 6))}${s.slice(-2)}` : s
}

export default defineAdminHandler(async (_event, admin) => {
  if (!(await coBang(dbGame(), 'tob_giftcode'))) return { coBang: false }
  const gioiHan = await gcGioiHan()
  const thang = gcThangNay()
  const coLog = await coBang(dbGame(), 'tob_giftcode_log')
  const daDung = coLog ? Number(await qv(dbGame(), 'SELECT COALESCE(SUM(gia_tri), 0) FROM tob_giftcode_log WHERE thang = ?', [thang])) : 0
  const luotThang = coLog ? Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_giftcode_log WHERE thang = ?', [thang])) : 0
  const rows = await q<Record<string, any>>(dbGame(), 'SELECT * FROM tob_giftcode ORDER BY tao_luc DESC LIMIT 500')
  const ds = []
  for (const r of rows) {
    const chuan = gcChuanHoaThuong(r.phan_thuong)
    ds.push({ ...r, ma: admin.vaiTro === 'sieu_quan_tri' ? r.ma : cheMa(r.ma), giaTri: chuan ? gcGiaTri(chuan) : null, moTa: chuan ? await gcMoTa(chuan) : ['(phần thưởng không hợp lệ — server sẽ từ chối)'] })
  }
  return { coBang: true, gioiHan, thang, nganSach: { daDung, luot: luotThang, toiDa: gioiHan.nganSachThang }, ds }
})
