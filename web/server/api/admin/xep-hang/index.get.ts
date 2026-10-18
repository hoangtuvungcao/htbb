/**
 * Cài đặt bảng xếp hạng công khai (/admin/xep-hang): cài đặt đang dùng + danh mục bảng (nguồn game,
 * bảng CSDL có đủ không) + giới hạn. Lưu: POST /api/admin/xep-hang/luu (chỉ siêu quản trị).
 */
import { XH_DANH_MUC, XH_GIOI_HAN } from '../../../../shared/utils/xepHangThuan'

export default defineAdminHandler(async () => {
  const coBangCaiDat = await coBangCms('web_cai_dat')
  const caiDat = await xhDocCaiDatTuDb()
  const tho = await xhDocCaiDatTho()
  const danhMuc = []
  for (const b of XH_DANH_MUC) {
    danhMuc.push({ ma: b.ma, ten: b.ten, icon: b.icon, nhom: b.nhom, moTa: b.moTa, nguon: b.nguon, trongGame: b.trongGame,
      gioiHanGame: b.gioiHanGame, bang: b.bang, khaDung: await xhBangKhaDung(b) })
  }
  return {
    coBang: coBangCaiDat,
    tuDb: tho != null,
    caiDat,
    danhMuc,
    gioiHan: XH_GIOI_HAN,
    napBat: !!useHtbbConfig().nap.bat,
    muaHienTai: await xhMuaHienTai(),
  }
})
