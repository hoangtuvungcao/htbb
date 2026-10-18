/**
 * Webhook SePay (https://sepay.vn) báo tiền vào tài khoản ngân hàng.
 *
 * Cấu hình: SePay > Webhooks trỏ tới https://<tên-miền>/api/nap/webhook-sepay, kiểu xác thực
 * "API Key", dán y hệt vào KPAH_SEPAY_WEBHOOK_APIKEY (CHỈ trong biến môi trường máy chủ, không
 * bao giờ vào bundle trình duyệt). Để trống = không xác nhận tự động (admin xác nhận tay).
 *
 * Luôn 200 khi đã xử lý (kể cả không khớp đơn) để SePay không gửi lại mãi; 401 khi sai khoá;
 * 500 khi lỗi database (để SePay GỬI LẠI — tiền đã vào thì đơn phải được xác nhận).
 * Gọi lặp: UNIQUE(nha_cung_cap, ma_gd) ở nap_webhook + chốt trạng thái ở napXacNhanThanhToan.
 */
export default defineEventHandler(async (event) => {
  setResponseHeader(event, 'Content-Type', 'application/json; charset=utf-8')
  const apiKey = useHtbbConfig().nap.sepayApiKey
  if (apiKey === '') {
    setResponseStatus(event, 200)
    return { success: false, message: 'SePay chưa được cấu hình' }
  }
  if (!xacThucSepay(getRequestHeader(event, 'authorization'), apiKey)) {
    setResponseStatus(event, 401)
    return { success: false, message: 'unauthorized' }
  }
  const raw = (await readRawBody(event, 'utf8').catch(() => undefined)) ?? ''
  let d: Record<string, unknown> | null = null
  try {
    const p = JSON.parse(raw)
    d = p && typeof p === 'object' ? p : null
  } catch { d = null }
  if (!d) return { success: false, message: 'invalid payload' }
  const gd = docSepay(d)
  if (gd === 'bo_qua') return { success: true, message: 'ignored (not incoming)' }
  if (!gd) return { success: false, message: 'missing transaction id' }
  try {
    return await napXuLyGiaoDich(event, 'sepay', gd)
  } catch (e: any) {
    console.error('[webhook-sepay] loi database:', e?.code ?? e)
    setResponseStatus(event, 500)
    return { success: false, message: 'server error' }
  }
})
