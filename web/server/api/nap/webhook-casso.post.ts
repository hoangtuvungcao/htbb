/**
 * Webhook Casso (https://casso.vn) v2 — ký HMAC-SHA512 ở header X-Casso-Signature (t=..,v1=..)
 * bằng KPAH_CASSO_WEBHOOK_SECRET (biến môi trường máy chủ). Sai chữ ký / lệch giờ > 5 phút = 401.
 * ĐỐI CHIẾU tài liệu Casso hiện hành trước khi bật (HTBB/docs/pokemon/NAP_TIEN.md mục 6).
 * Thân gồm một (v2) hoặc nhiều (v1) giao dịch — xử lý từng cái qua napXuLyGiaoDich.
 */
export default defineEventHandler(async (event) => {
  setResponseHeader(event, 'Content-Type', 'application/json; charset=utf-8')
  const secret = useHtbbConfig().nap.cassoSecret
  if (secret === '') return { success: false, message: 'Casso chưa được cấu hình' }
  const raw = (await readRawBody(event, 'utf8').catch(() => undefined)) ?? ''
  let d: Record<string, unknown> | null = null
  try {
    const p = JSON.parse(raw)
    d = p && typeof p === 'object' ? p : null
  } catch { d = null }
  if (!d || !xacThucCasso(getRequestHeader(event, 'x-casso-signature'), d.data, secret)) {
    setResponseStatus(event, 401)
    return { success: false, message: 'invalid signature' }
  }
  try {
    const kq = []
    for (const gd of docCasso(d)) kq.push(await napXuLyGiaoDich(event, 'casso', gd))
    return { success: true, message: kq.map(k => k.message).join('; ') || 'no incoming transaction' }
  } catch (e: any) {
    console.error('[webhook-casso] loi database:', e?.code ?? e)
    setResponseStatus(event, 500)
    return { success: false, message: 'server error' }
  }
})
