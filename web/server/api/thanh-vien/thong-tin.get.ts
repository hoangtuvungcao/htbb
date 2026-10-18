/**
 * Thông tin công khai về Thành viên HTBB (trang /thanh-vien): đang bắt buộc chưa, đang bán chưa, giá,
 * thời hạn, danh sách tính năng (sinh từ cấu hình admin). Không cần đăng nhập, không lộ bí mật.
 */
import { TV_CHUC_NANG } from '../../utils/thanhVienThuan'

export default defineEventHandler(async () => {
  const c = await tvCauHinh()
  const ban = await tvBanDuoc().catch(() => ({ duoc: false, lyDo: [] as string[] }))
  return {
    batBuoc: c.bat,
    dangBan: ban.duoc,
    gia: c.gia,
    thoiHanNgay: c.thoiHanNgay,
    mienCap: c.mienCap,
    chucNang: TV_CHUC_NANG.map(x => ({ ma: x.ma, ten: x.ten, cho: x.cho, khoa: c.chucNang.includes(x.ma) })),
    kichHoat: {
      chanDangNhap: c.khChanDangNhap,
      chucNang: TV_CHUC_NANG.filter(x => c.khChucNang.includes(x.ma)).map(x => ({ ma: x.ma, ten: x.ten })),
      thanhVienLaKichHoat: c.khTvLaKh,
    },
  }
})
