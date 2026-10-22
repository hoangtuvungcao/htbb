/**
 * Chi tiết một loài: chỉ số nền, cả họ tiến hoá (cạnh + điều kiện), 4 chiêu bạn đồng hành, nơi sống, nhóm trứng,
 * trạng thái sprite + ghi công. Số Pokédex sai -> 400; chưa có dữ liệu -> 404.
 */
import { ketQuaChiTietLoai } from '~~/shared/utils/loaiDuLieu'

export default defineEventHandler(async (event) => {
  let kq
  try {
    kq = await ketQuaChiTietLoai(getRouterParam(event, 'dex'), truyLoai)
  }
  catch (e: any) {
    console.error('[pokedex] chi tiet:', e?.code ?? e?.message ?? e)
    throw apiLoi(503, 'Pokédex tạm thời không đọc được, thử lại sau.')
  }
  if (kq.ma !== 200) throw apiLoi(kq.ma, kq.thongBao ?? 'Lỗi')
  setHeader(event, 'Cache-Control', 'public, max-age=300')
  return kq.duLieu
})
