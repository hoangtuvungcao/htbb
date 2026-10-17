import { join } from 'node:path'

/**
 * Đọc bảng sự kiện cho trang /admin/sukien, kèm phân loại và cấu hình tự khởi động lại.
 *
 * Danh sách sự kiện do GAME SERVER đăng lên mỗi lần khởi động, không phải web tạo.
 */
export default defineAdminHandler(async () => {
  let coBang = true
  let coCotLich = true
  let loiDoc = ''
  let dong: SuKienDong[] = []

  try {
    try {
      dong = await q<SuKienDong>(dbGame(), `SELECT ma, bat, ten, nhom, mo_ta, dang_bat, sua_boi, sua_luc,
                                                   may_chu_luc, mo_luc, dong_luc
                                              FROM ${SU_KIEN_BANG}`)
    } catch {
      // Bảng có từ trước 30/08/2026 thì chưa có hai cột lịch. Server tự ALTER lúc khởi
      // động (DatabaseSuKien.themCotThieu), nhưng cho tới lúc đó trang vẫn phải dùng được
      // — không thì cập nhật jar xong là mất luôn cả nút bật/tắt đang chạy tốt.
      coCotLich = false
      dong = await q<SuKienDong>(dbGame(), `SELECT ma, bat, ten, nhom, mo_ta, dang_bat, sua_boi, sua_luc,
                                                   may_chu_luc, NULL AS mo_luc, NULL AS dong_luc
                                              FROM ${SU_KIEN_BANG}`)
    }
  } catch (e: any) {
    coBang = false
    loiDoc = String(e?.message ?? e)
    dong = []
  }

  // Đọc thẳng server.ini của game server. Web có thể nằm khác máy với game server — lúc đó
  // không đọc được và trang nói thẳng là không biết, không đoán bừa là "đang bật".
  const cauHinh = suKienCauHinhKhoiDong(htbbDocIni(join(useHtbbConfig().serverDir, 'server.ini')))
  const pl = suKienPhanLoai(dong)

  return {
    coBang,
    coCotLich,
    loiDoc,
    bayGio: await suKienBayGio(),
    maCongTong: SU_KIEN_MA_CONG_TONG,
    tong: dong.length,
    theoNhom: suKienTheoNhom(dong),
    choKhoiDong: pl.choKhoiDong.map(d => ({ ma: d.ma, ten: d.ten, bat: Number(d.bat) })),
    anhChupHong: pl.anhChupHong.map(d => ({ ma: d.ma, ten: d.ten })),
    lichSapToi: pl.lichSapToi,
    mayChuLuc: pl.mayChuLuc,
    soDangBat: pl.soDangBat,
    soSeBat: pl.soSeBat,
    congTongTat: pl.congTongTat,
    ...cauHinh,
  }
})
