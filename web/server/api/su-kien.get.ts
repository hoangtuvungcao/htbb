/**
 * Lịch sự kiện công khai (trang chủ "Sự kiện", cẩm nang): mọi lần diễn ra từ 60 ngày trước tới 13 tháng sau, đã áp
 * ghi đè của admin (lần bị "tắt năm nay" bị bỏ). Giờ là giờ Việt Nam; `bayGio` = NOW() của CSDL để trình duyệt đếm
 * ngược theo cùng đồng hồ với máy chủ game. Nguồn: tob_su_kien_lich (máy chủ game ghi theo com.htbb.pokemon.LichNam).
 */
import { chuoiTuMs, msTuChuoi } from '../../shared/utils/suKienNam'

export default defineEventHandler(async () => quaDem('su-kien-nam', 60, async () => {
  try {
    const bayGio = await suKienBayGio()
    const bay = msTuChuoi(bayGio)
    const ds = await docLanSuKien(chuoiTuMs(bay - 60 * 86_400_000), chuoiTuMs(bay + 400 * 86_400_000))
    return { bayGio, ds: ds.filter(l => Number(l.ghiDe) !== 1) }
  }
  catch {
    return { bayGio: null, ds: [] }
  }
}))
