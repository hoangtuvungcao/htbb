/**
 * Xem 1 đơn nạp của chính mình (vừa tạo, hoặc quay lại bằng link ?ma_don=...).
 * Query: ma_don -> { don, qr, nganHang }
 */
import type { DonNap } from '../../utils/napTien'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const maDon = str(getQuery(event).ma_don).toUpperCase()
  let don: DonNap | null = null
  try {
    don = /^HT[A-Z0-9]{8}$/.test(maDon) ? await napLayDon(maDon) : null
  } catch (e: any) {
    console.error('[nap] doc don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa tải được đơn nạp lúc này. Bạn vui lòng thử lại sau ít phút.')
  }
  // Không phân biệt "không có" với "của người khác" — không cho dò mã đơn người khác.
  if (!don || Number(don.user_id) !== u.id) throw apiLoi(404, 'Không tìm thấy đơn nạp này.', { code: 'not_found' })
  const n = useHtbbConfig().nap
  return {
    don: napDonCongKhai(don),
    qr: napQr(Number(don.so_tien), don.ma_don),
    nganHang: { bin: n.bankBin, soTaiKhoan: n.accountNo, chuTaiKhoan: n.accountName },
  }
})
