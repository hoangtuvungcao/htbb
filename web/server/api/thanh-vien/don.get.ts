/** Xem 1 đơn THÀNH VIÊN của chính mình (QR, tài khoản nhận, hạn, nút mở app ngân hàng). */
import type { DonNap } from '../../utils/napTien'
import { APP_NGAN_HANG, GIO_HET_HAN_DON, vietQrDeepLink } from '../../utils/napTienThuan'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const maDon = str(getQuery(event).ma_don).toUpperCase()
  const don: DonNap | null = /^HT[A-Z0-9]{8}$/.test(maDon) ? await napLayDon(maDon) : null
  if (!don || Number(don.user_id) !== u.id || don.loai !== 'thanh_vien') throw apiLoi(404, 'Không tìm thấy đơn này.', { code: 'not_found' })
  const n = useHtbbConfig().nap
  const taoMs = new Date(String(don.tao_luc).replace(' ', 'T')).getTime()
  return {
    don: napDonCongKhai(don),
    qr: napQr(Number(don.so_tien), don.ma_don),
    nganHang: { soTaiKhoan: n.accountNo, chuTaiKhoan: n.accountName },
    hetHanMs: Number.isFinite(taoMs) ? taoMs + GIO_HET_HAN_DON * 3600_000 : null,
    bayGioMs: Date.now(),
    moApp: APP_NGAN_HANG.map(a => ({ ten: a.ten, url: vietQrDeepLink(a.app, n.bankBin, n.accountNo, Number(don.so_tien), don.ma_don) })),
  }
})
