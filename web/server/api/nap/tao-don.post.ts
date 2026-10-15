/**
 * Tạo đơn nạp mốc mới cho người chơi đang đăng nhập.
 * Body: { moc: 1 | 2 | 3 } -> { ma_don, amount, moc }
 */

/** Mỗi lần bấm là một dòng mới trong tob_nap_moc. Người thật đổi ý vài lần là cùng;
 *  quá 10 đơn một giờ thì chỉ có thể là bấm tự động. */
const TAO_DON_TOI_DA = 10
const TAO_DON_TRONG_GIAY = 3600

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  const moc = int(body?.moc, 0)
  if (![1, 2, 3].includes(moc)) {
    throw apiLoi(400, 'Mốc nạp không hợp lệ.')
  }
  if (await biChanSpam(event, 'tao_don_nap', TAO_DON_TOI_DA, TAO_DON_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã tạo quá nhiều đơn nạp. Hãy thanh toán đơn đang có, hoặc thử lại sau một giờ.')
  }

  let tao
  try {
    tao = await napMocTaoDon(u.id, moc)
  } catch (e: any) {
    console.error('[nap] tao don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa thể tạo đơn nạp lúc này. Bạn vui lòng thử lại sau ít phút.')
  }
  await ghiLanLam(event, 'tao_don_nap')
  return tao
})
