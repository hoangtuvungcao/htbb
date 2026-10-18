/**
 * Người chơi tự đặt trần nạp tháng (bảo vệ bản thân). Body: { tran } (VND, 0 = bỏ trần tự đặt).
 * HẠ có hiệu lực ngay; NÂNG / bỏ chỉ có hiệu lực sau 72 giờ. -> { ngay, hieuLucLuc }
 */
export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (!(await napCoBang())) napThieuBang()
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const tran = int(body?.tran, -1)
  if (tran < 0 || tran > TRAN_CUNG_THANG_VND || (tran > 0 && tran < 10000)) {
    throw apiLoi(400, `Trần phải là 0 (bỏ) hoặc từ 10.000 đến ${TRAN_CUNG_THANG_VND.toLocaleString('vi-VN')} đ.`)
  }
  return napDatTranTuDat(u.id, tran)
})
