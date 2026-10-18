/**
 * Cổng thanh toán GIẢ để thử trên máy dev (KHÔNG gọi ngân hàng / cổng thật nào).
 * Chỉ mở khi KPAH_NAP_SANDBOX=1 VÀ có KPAH_NAP_MOCK_SECRET; ngoài ra trả 404 như không tồn tại.
 * Ký: X-HTBB-Signature: t=<giây>,v1=<hex HMAC-SHA256(secret, t + "." + thân thô)>.
 * Thân: { ma_gd, so_tien, noi_dung } — đi qua đúng đường napXuLyGiaoDich như SePay/Casso.
 */
export default defineEventHandler(async (event) => {
  const n = useHtbbConfig().nap
  if (!n.sandbox || n.mockSecret === '') throw createError({ statusCode: 404, statusMessage: 'Not Found' })
  const raw = (await readRawBody(event, 'utf8').catch(() => undefined)) ?? ''
  if (!xacThucMock(getRequestHeader(event, 'x-htbb-signature'), raw, n.mockSecret)) {
    setResponseStatus(event, 401)
    return { success: false, message: 'invalid signature' }
  }
  let d: Record<string, unknown> | null = null
  try {
    const p = JSON.parse(raw)
    d = p && typeof p === 'object' ? p : null
  } catch { d = null }
  const gd = d ? docMock(d) : null
  if (!gd) return { success: false, message: 'invalid payload' }
  return napXuLyGiaoDich(event, 'mock', gd)
})
