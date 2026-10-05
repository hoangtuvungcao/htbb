/**
 * Webhook nhận thông báo biến động số dư từ SePay (https://sepay.vn) khi tài khoản
 * TPBank nhận được tiền.
 *
 * Cấu hình: SePay > Cấu hình > Webhooks trỏ tới https://<tên-miền>/api/nap/webhook-sepay,
 * đặt API Key ở đó và dán y hệt vào KPAH_SEPAY_WEBHOOK_APIKEY. Để trống thì KHÔNG
 * xác nhận tự động — admin dùng /admin/naptien hoặc /xac-nhan-nap.
 *
 * Luôn trả HTTP 200 (kể cả khi không khớp đơn nào) để SePay không lặp lại gửi mãi;
 * chỉ 401 khi sai API key. Riêng lỗi database (MySQL tắt...) thì trả 500 — lúc đó
 * để SePay gửi lại là điều MONG MUỐN, kẻo tiền đã vào mà đơn không bao giờ được xác nhận.
 *
 * Chống trùng: UPDATE ... WHERE trang_thai='cho_thanh_toan' — gửi lặp bao nhiêu lần
 * cũng chỉ lần đầu có tác dụng.
 */
export default defineEventHandler(async (event) => {
  setResponseHeader(event, 'Content-Type', 'application/json; charset=utf-8')
  const respond = (success: boolean, message = '') => {
    setResponseStatus(event, 200)
    return { success, message }
  }

  const apiKey = useKpahConfig().nap.sepayApiKey
  if (apiKey === '') {
    // Chưa cấu hình SePay -> bỏ qua toàn bộ webhook, dùng xác nhận thủ công.
    return respond(false, 'SePay chưa được cấu hình')
  }

  const goiLen = napMocDocApiKey(getRequestHeader(event, 'authorization'))
  if (goiLen === null || !napMocSoKhoa(apiKey, goiLen)) {
    setResponseStatus(event, 401)
    return { success: false, message: 'unauthorized' }
  }

  const raw = (await readRawBody(event, 'utf8').catch(() => undefined)) ?? ''
  const pt = napMocPhanTichWebhook(raw)
  if (pt.loai === 'tra_loi') return respond(pt.success, pt.message)

  try {
    const don = await napMocLayDon(pt.maDon)
    const qd = napMocQuyetDinhWebhook(don, pt.maDon, pt.amount)
    if (qd.capNhat && don) await napMocDanhDauDaThanhToan(don.id)
    return respond(qd.success, qd.message)
  } catch (e: any) {
    console.error('[webhook-sepay] loi database:', e?.code ?? e)
    setResponseStatus(event, 500)
    return { success: false, message: 'server error' }
  }
})
