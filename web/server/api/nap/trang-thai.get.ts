/**
 * Trạng thái 1 đơn nạp — trang /naptien hỏi lại vài giây một lần.
 * Chỉ chủ đơn mới xem được. Query: ma_don -> { trang_thai }
 */
export default defineEventHandler(async (event) => {
  const u = requireUser(event)
  const maDon = str(getQuery(event).ma_don)
  let don: DonNapMoc | null = null
  try {
    don = maDon !== '' && maDon.length <= 20 ? await napMocLayDon(maDon) : null
  } catch (e: any) {
    console.error('[nap] doc trang thai loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa kiểm tra được đơn nạp lúc này.')
  }

  // Không phân biệt "không có" với "của người khác" — không cho dò mã đơn người khác.
  if (!don || Number(don.userID) !== u.id) {
    throw apiLoi(404, 'Không tìm thấy đơn nạp này.', { code: 'not_found' })
  }
  return { trang_thai: don.trang_thai }
})
