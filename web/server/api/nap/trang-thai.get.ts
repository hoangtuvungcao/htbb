/**
 * Trạng thái 1 đơn nạp — trang /naptien hỏi lại vài giây một lần. Chỉ chủ đơn xem được.
 * Query: ma_don -> { trang_thai, cong_luc }
 */
import type { DonNap } from '../../utils/napTien'

export default defineEventHandler(async (event) => {
  if (napDemoBat()) {
    const u = await napDemoRequireUser(event)
    const d = napDemoDonCuaToi(u.id, str(getQuery(event).ma_don).toUpperCase())
    return { trang_thai: d.trang_thai, cong_luc: d.cong_luc, demo: true }
  }
  const u = await requireUser(event)
  const maDon = str(getQuery(event).ma_don).toUpperCase()
  let don: DonNap | null = null
  try {
    don = /^HT[A-Z0-9]{8}$/.test(maDon) ? await napLayDon(maDon) : null
  } catch (e: any) {
    console.error('[nap] doc trang thai loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa kiểm tra được đơn nạp lúc này.')
  }
  if (!don || Number(don.user_id) !== u.id) throw apiLoi(404, 'Không tìm thấy đơn nạp này.', { code: 'not_found' })
  return { trang_thai: don.trang_thai, cong_luc: don.cong_luc }
})
