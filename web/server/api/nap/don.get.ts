/**
 * Xem 1 đơn nạp của chính mình (vừa tạo, hoặc quay lại bằng link ?ma_don=...).
 * Query: ma_don -> { don, qr, nganHang }
 */
import type { DonNap } from '../../utils/napTien'
import { APP_NGAN_HANG, GIO_HET_HAN_DON, vietQrDeepLink } from '../../utils/napTienThuan'

export default defineEventHandler(async (event) => {
  if (napDemoBat()) {
    // DEMO: không QR thật, không link mở app ngân hàng, số TK minh hoạ.
    const u = await napDemoRequireUser(event)
    return napDemoChiTiet(napDemoDonCuaToi(u.id, str(getQuery(event).ma_don).toUpperCase()))
  }
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
  // Hạn thanh toán: tao_luc + 24 giờ (tao_luc là giờ máy chủ, gửi kèm "bây giờ" để trình duyệt tự bù lệch đồng hồ).
  const taoMs = new Date(String(don.tao_luc).replace(' ', 'T')).getTime()
  return {
    don: napDonCongKhai(don),
    qr: napQr(Number(don.so_tien), don.ma_don),
    nganHang: { bin: n.bankBin, soTaiKhoan: n.accountNo, chuTaiKhoan: n.accountName },
    hetHanMs: Number.isFinite(taoMs) ? taoMs + GIO_HET_HAN_DON * 3600_000 : null,
    bayGioMs: Date.now(),
    moApp: APP_NGAN_HANG.map(a => ({ ten: a.ten, url: vietQrDeepLink(a.app, n.bankBin, n.accountNo, Number(don!.so_tien), don!.ma_don) })),
  }
})
