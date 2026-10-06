/**
 * Xem 1 đơn nạp của chính mình (vừa tạo, hoặc quay lại bằng link cũ ?ma_don=...).
 * Query: ma_don -> { don: {...}, qr }
 */
export default defineEventHandler(async (event) => {
  const u = requireUser(event)
  const maDon = str(getQuery(event).ma_don)
  let don: DonNapMoc | null = null
  try {
    don = maDon !== '' && maDon.length <= 20 ? await napMocLayDon(maDon) : null
  } catch (e: any) {
    console.error('[nap] doc don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa tải được đơn nạp lúc này. Bạn vui lòng thử lại sau ít phút.')
  }

  // Không phân biệt "không có" với "của người khác" — không cho dò mã đơn người khác.
  if (!don || Number(don.userID) !== u.id) {
    throw apiLoi(404, 'Không tìm thấy đơn nạp này.', { code: 'not_found' })
  }
  return {
    don: {
      ma_don: don.ma_don,
      moc: Number(don.moc),
      amount: Number(don.amount),
      trang_thai: don.trang_thai,
      created_at: don.created_at,
      paid_at: don.paid_at,
    },
    qr: napMocQr(Number(don.amount), don.ma_don),
  }
})
