/**
 * Lưu cài đặt bảng xếp hạng vào web_cai_dat (khoá 'xepHang'). Giá trị được nắn bằng xhChuanHoaCaiDat
 * (khoá lạ bỏ, số kẹp trong giới hạn); mẫu tên loại trừ sai cú pháp -> báo lỗi 400 thay vì lặng lẽ bỏ.
 * Ghi trước/sau vào web_admin_thay_doi, xoá bộ đệm để trang công khai đổi ngay.
 */
import { XH_KHOA_CAI_DAT, xhChuanHoaCaiDat, xhTachMau } from '../../../../shared/utils/xepHangThuan'

export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_cai_dat')
  const b = await bodyObj(event)
  const raw = (b.caiDat && typeof b.caiDat === 'object' ? b.caiDat : {}) as Record<string, any>
  const lt = (raw.loaiTru && typeof raw.loaiTru === 'object' ? raw.loaiTru : {}) as Record<string, unknown>
  const sai = [...xhTachMau(lt.tenNhanVat).loi, ...xhTachMau(lt.taiKhoan).loi]
  if (sai.length) {
    throw apiLoi(400, `Mẫu tên không hợp lệ: ${sai.slice(0, 5).join(', ')}. Chỉ chữ không dấu, số, gạch dưới (tối đa 30 ký tự), có thể kết thúc bằng * để lọc theo tiền tố.`)
  }
  const moi = xhChuanHoaCaiDat(raw)
  const json = JSON.stringify(moi)
  if (json.length > 100_000) throw apiLoi(400, 'Cài đặt quá lớn.')
  const cu = await qv<string>(dbGame(), 'SELECT gia_tri FROM web_cai_dat WHERE khoa = ? LIMIT 1', [XH_KHOA_CAI_DAT])
  await exec(dbGame(),
    `INSERT INTO web_cai_dat (khoa, gia_tri, cap_nhat_luc, cap_nhat_boi) VALUES (?, ?, NOW(), ?)
     ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), cap_nhat_luc = VALUES(cap_nhat_luc), cap_nhat_boi = VALUES(cap_nhat_boi)`,
    [XH_KHOA_CAI_DAT, json, admin.username])
  xhXoaDem()
  await ghiThayDoi(event, 'xep_hang_cai_dat', `cai_dat:${XH_KHOA_CAI_DAT}`, cu ?? '(mặc định)', json)
  return { ok: true, caiDat: moi }
})
