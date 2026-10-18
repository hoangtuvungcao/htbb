/**
 * Job dò giao dịch bị LỠ webhook (SePay user API) — docs/van-hanh.md.
 * Chỉ chạy khi nạp đang bật VÀ có HTBB_SEPAY_API_TOKEN. Mỗi HTBB_SEPAY_DO_PHUT phút (2–5, mặc định 3)
 * lấy giao dịch tiền vào HTBB_SEPAY_DO_GIO giờ gần nhất (mặc định 6) của đúng tài khoản cấu hình,
 * đưa từng cái qua napXuLyGiaoDich — cùng khoá UNIQUE(sepay, id) với webhook nên giao dịch đã nhận
 * qua webhook bị bỏ qua, không bao giờ cộng hai lần.
 *
 * GET {HTBB_SEPAY_API_URL | https://my.sepay.vn}/userapi/transactions/list
 *     ?account_number=..&transaction_date_min=yyyy-mm-dd HH:MM:SS&limit=200
 *     Authorization: Bearer <token>   (giới hạn 3 lần/giây; 429 thì chờ header x-sepay-userapi-retry-after)
 */
export default defineNitroPlugin(() => {
  if (import.meta.prerender) return
  const n = useHtbbConfig().nap
  // Chạy khi có token; mỗi lượt tự kiểm còn bán gì không (nạp lượng HOẶC Thành viên — công tắc DB đổi lúc chạy).
  if (n.sepayApiToken === '') return
  const goc = ((process.env.HTBB_SEPAY_API_URL ?? process.env.KPAH_SEPAY_API_URL) || 'https://my.sepay.vn').replace(/\/+$/, '') // TUONG-THICH-TEN-CU
  let dangChay = false
  let choDen = 0
  const chay = async () => {
    if (dangChay || Date.now() < choDen) return
    dangChay = true
    try {
      if (!(await napCoBang())) return
      if (!n.bat && !(await tvBanDuoc()).duoc) return
      const tu = gioDb(new Date(Date.now() - n.sepayDoGio * 3600_000))
      const url = `${goc}/userapi/transactions/list?${new URLSearchParams({ account_number: n.accountNo, transaction_date_min: tu, limit: '200' })}`
      const r = await fetch(url, { headers: { 'Authorization': `Bearer ${n.sepayApiToken}`, 'Content-Type': 'application/json' }, signal: AbortSignal.timeout(20_000) })
      if (r.status === 429) {
        choDen = Date.now() + Math.max(5, Number(r.headers.get('x-sepay-userapi-retry-after') ?? 60)) * 1000
        return
      }
      if (!r.ok) {
        console.warn(`[do-sepay] HTTP ${r.status}`)
        return
      }
      const d = await r.json() as { transactions?: Record<string, unknown>[] }
      let moi = 0
      for (const x of d.transactions ?? []) {
        const gd = docSepayApi(x)
        if (!gd) continue
        const kq = await napXuLyGiaoDich('sepay-api', 'sepay', gd)
        if (kq.ketQua !== 'trung') {
          moi++
          await napNhatKy(null, 'job:sepay-api', 'do_giao_dich', timMaDon(gd.maThanhToan) ?? timMaDon(gd.noiDung) ?? '', 0, `GD ${gd.maGd} lỡ webhook -> ${kq.ketQua}`)
        }
      }
      if (moi) console.log(`[do-sepay] xu ly ${moi} giao dich lo webhook`)
    } catch (e: any) {
      console.warn('[do-sepay] loi:', e?.code ?? e?.message ?? e)
    } finally {
      dangChay = false
    }
  }
  setTimeout(chay, 15_000)
  setInterval(chay, n.sepayDoPhut * 60_000)
})
