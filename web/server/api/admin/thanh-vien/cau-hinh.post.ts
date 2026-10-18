/**
 * Lưu cấu hình Thành viên (Siêu quản trị): bat, banBat, gia, thoiHanNgay, chucNang[], mienCap, thongBao.
 * Ghi cau_hinh (server game đọc lại sau ≤ 10 giây) + đồng bộ gói THANH_VIEN; ghi nhật ký admin.
 */
import { docCauHinhTv, kiemCauHinhTv, type CauHinhTv } from '../../../utils/thanhVienThuan'

export default defineAdminHandler(async (event, admin) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const c: CauHinhTv = {
    bat: b?.bat === true,
    banBat: b?.banBat === true,
    gia: int(b?.gia, -1),
    thoiHanNgay: int(b?.thoiHanNgay, -1),
    chucNang: Array.isArray(b?.chucNang) ? (b!.chucNang as unknown[]).map(x => String(x)) : [],
    mienCap: int(b?.mienCap, -1),
    thongBao: str(b?.thongBao).trim(),
    khChanDangNhap: b?.khChanDangNhap === true,
    khChucNang: Array.isArray(b?.khChucNang) ? (b!.khChucNang as unknown[]).map(x => String(x)) : [],
    khThongBao: str(b?.khThongBao).trim(),
    khTvLaKh: b?.khTvLaKh !== false,
  }
  const loi = kiemCauHinhTv(c)
  if (loi) throw apiLoi(400, loi)
  const cu = await tvCauHinh(true)
  await tvLuuCauHinh(admin.username, docCauHinhTv({
    'thanh_vien.bat': c.bat ? '1' : '0', 'thanh_vien.ban_bat': c.banBat ? '1' : '0', 'thanh_vien.gia': String(c.gia),
    'thanh_vien.thoi_han_ngay': String(c.thoiHanNgay), 'thanh_vien.chuc_nang': c.chucNang.join(','), 'thanh_vien.mien_cap': String(c.mienCap),
    'thanh_vien.thong_bao': c.thongBao,
    'kich_hoat.chan_dang_nhap': c.khChanDangNhap ? '1' : '0', 'kich_hoat.chuc_nang': c.khChucNang.join(','),
    'kich_hoat.thong_bao': c.khThongBao, 'kich_hoat.thanh_vien_la_kich_hoat': c.khTvLaKh ? '1' : '0',
  }))
  await ghiNhatKy(event, 'cau_hinh_thanh_vien', 'thanh_vien', `bắt buộc ${cu.bat ? 1 : 0}->${c.bat ? 1 : 0}, bán ${cu.banBat ? 1 : 0}->${c.banBat ? 1 : 0}, giá ${cu.gia}->${c.gia}, hạn ${c.thoiHanNgay} ngày, miễn cấp ${c.mienCap}, khoá: ${c.chucNang.join(',')}; kích hoạt: chặn đăng nhập ${c.khChanDangNhap ? 1 : 0}, khoá ${c.khChucNang.join(',')}, thành viên=kích hoạt ${c.khTvLaKh ? 1 : 0}`)
  const ban = await tvBanDuoc()
  return { ok: true, message: c.banBat && !ban.duoc ? `Đã lưu — nhưng CHƯA bán được: ${ban.lyDo.join('; ')}` : 'Đã lưu. Server game áp dụng sau ≤ 10 giây.' }
})
