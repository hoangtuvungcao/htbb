import { gcChuanHoaMa, gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
/** Mã an toàn + giới hạn + ngân sách tháng đã dùng. coBang=false -> trang dùng giftcode kiểu cũ. */
export default defineAdminHandler(async () => {
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
    ds.push({ ...r, giaTri: chuan ? gcGiaTri(chuan) : null, moTa: chuan ? await gcMoTa(chuan) : ['(phần thưởng không hợp lệ — server sẽ từ chối)'] })
  }
  return { coBang: true, gioiHan, thang, nganSach: { daDung, luot: luotThang, toiDa: gioiHan.nganSachThang }, ds }
})
