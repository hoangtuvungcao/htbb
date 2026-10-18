/**
 * Webhook SePay (docs.sepay.vn/tich-hop-webhooks.html) — NẠP TỰ ĐỘNG.
 *
 * SePay POST JSON { id, gateway, transactionDate, accountNumber, code, content, transferType,
 * transferAmount, accumulated, subAccount, referenceCode, description }, header
 * `Authorization: Apikey <KPAH_SEPAY_WEBHOOK_APIKEY>` (khoá CHỈ ở biến môi trường máy chủ).
 * SePay coi là thành công khi HTTP 200/201 + {"success": true} trong 30 giây; ngược lại gửi lại
 * (Fibonacci, tối đa 7 lần / 5 giờ). Vì vậy:
 *   - mọi giao dịch ĐÃ GHI SỔ (khớp, trùng, thiếu, dư, sai tài khoản, không mã đơn) -> 200 {success:true};
 *   - sai khoá -> 401; lỗi CSDL -> 500 (để SePay GỬI LẠI — tiền đã vào thì đơn phải được xác nhận).
 * Gọi lặp: UNIQUE(sepay, id) ở nap_webhook + chốt trạng thái ở napXacNhanThanhToan.
 */
export default defineEventHandler(async (event) => {
  setResponseHeader(event, 'Content-Type', 'application/json; charset=utf-8')
  const apiKey = useHtbbConfig().nap.sepayApiKey
  if (apiKey === '') {
    setResponseStatus(event, 503)
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
  if (!d) {
    setResponseStatus(event, 400)
    return { success: false, message: 'invalid payload' }
  }
  const gd = docSepay(d)
  if (gd === 'bo_qua') return { success: true, message: 'ignored (not incoming)' }
  if (!gd) {
    setResponseStatus(event, 400)
    return { success: false, message: 'missing transaction id' }
  }
  try {
    const kq = await napXuLyGiaoDich(event, 'sepay', gd)
    return { success: kq.success, message: kq.message }
  } catch (e: any) {
    console.error('[webhook-sepay] loi database:', e?.code ?? e)
    setResponseStatus(event, 500)
    return { success: false, message: 'server error' }
  }
})
