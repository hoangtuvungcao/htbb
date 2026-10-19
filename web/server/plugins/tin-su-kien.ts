/**
 * Tin tự động theo lịch sự kiện năm (chủ game duyệt 19/10/2026): mỗi 10 phút đọc tob_su_kien_lich và đăng vào
 * web_tin_tuc (chuyên mục "Sự kiện"):
 *   - "Sắp diễn ra: <tên>"  — 3 ngày trước khi mở (tuần Pokémon: 1 ngày);
 *   - "Đang diễn ra: <tên>" — lúc mở;
 *   - "Đã kết thúc: <tên>"  — lúc đóng (trong 2 ngày; tuần Pokémon không có).
 * Mỗi tin một slug cố định (su-kien-<ma>-<nam>-<kieu>) và INSERT IGNORE, nên chạy lại / nhiều tiến trình không đăng
 * trùng; admin sửa bài ở /admin/tin-tuc thì không bị ghi đè. Muốn ẩn một tin tự động: chuyển bài sang "Nháp"
 * (đừng xoá — xoá trong lúc giai đoạn còn hiệu lực thì lần quét sau đăng lại). Giờ đăng (dang_luc) = đúng mốc nếu trễ
 * ít. Logic: shared/utils/suKienNam.ts.
 * Tắt: HTBB_TIN_SU_KIEN=0.
 */
import { chuoiTuMs, gioDangTin, msTuChuoi, suKienKem, tinCanDang, vietTin } from '../../shared/utils/suKienNam'

export const TAC_GIA_TIN_SU_KIEN = 'Lịch sự kiện'

export async function dangTinSuKien(): Promise<number> {
  if (!(await coBang(dbGame(), 'web_tin_tuc'))) return 0
  const bayGio = await suKienBayGio()
  const bay = msTuChuoi(bayGio)
  const ds = await docLanSuKien(chuoiTuMs(bay - 3 * 86_400_000), chuoiTuMs(bay + 4 * 86_400_000))
  let n = 0
  for (const l of ds) {
    const can = tinCanDang(l, bay)
    if (!can) continue
    const tin = vietTin(l, can.kieu, suKienKem(l, ds))
    const r = await exec(dbGame(),
      `INSERT IGNORE INTO web_tin_tuc (slug, tieu_de, tom_tat, anh_bia, loai, the, noi_dung, trang_thai, dang_luc, ghim, tac_gia, tao_luc, sua_luc)
       VALUES (?, ?, ?, '', ?, ?, ?, 1, ?, 0, ?, NOW(), NOW())`,
      [tin.slug, tin.tieuDe, tin.tomTat, tin.loai, tin.the, tin.noiDung, chuoiTuMs(gioDangTin(can.han, bay)), TAC_GIA_TIN_SU_KIEN])
    if (Number(r.affectedRows)) n++
  }
  if (n) xoaDemCms()
  return n
}

export default defineNitroPlugin(() => {
  if (import.meta.prerender) return
  if (process.env.HTBB_TIN_SU_KIEN === '0') return
  const chay = async () => {
    try {
      const n = await dangTinSuKien()
      if (n) console.log(`[tin-su-kien] đã đăng ${n} tin sự kiện`)
    }
    catch { /* CSDL chưa sẵn sàng / chưa có bảng: thử lại lần sau */ }
  }
  setTimeout(chay, 20_000)
  setInterval(chay, 10 * 60_000).unref?.()
})
