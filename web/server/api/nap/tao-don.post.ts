/**
 * Tạo đơn nạp. Body: { ma_goi, char_id, khoa_idem } -> { ma_don, so_tien }
 * khoa_idem: chuỗi ngẫu nhiên trình duyệt sinh MỖI lần bấm — gửi lại (mạng chập chờn, bấm đúp)
 * nhận lại đúng đơn cũ, không tạo đơn mới. Trần nạp tuần/tháng kiểm trong napTaoDon.
 */
const TAO_DON_TOI_DA = 10
const TAO_DON_TRONG_GIAY = 3600

export default defineEventHandler(async (event) => {
  if (napDemoBat()) return taoDonDemo(event)
  const u = await requireUser(event)
  if (!(await napCoBang())) napThieuBang(true)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const maGoi = str(body?.ma_goi).slice(0, 20)
  const charId = int(body?.char_id, 0)
  const khoaIdem = khoaIdemHopLe(body?.khoa_idem)
  if (!/^[A-Z0-9_]{2,20}$/.test(maGoi)) throw apiLoi(400, 'Gói không hợp lệ.')
  if (charId <= 0) throw apiLoi(400, 'Hãy chọn Pokémon nhận.')
  if (await biChanSpam(event, 'tao_don_nap', TAO_DON_TOI_DA, TAO_DON_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn đã tạo quá nhiều đơn nạp. Hãy thanh toán đơn đang có, hoặc thử lại sau một giờ.')
  }
  let don
  try {
    await napHetHanDonCu()
    don = await napTaoDon(u, maGoi, charId, khoaIdem)
  } catch (e: any) {
    if (e?.statusCode) throw e
    console.error('[nap] tao don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa thể tạo đơn nạp lúc này. Bạn vui lòng thử lại sau ít phút.')
  }
  await ghiLanLam(event, 'tao_don_nap')
  return { ma_don: don.ma_don, so_tien: Number(don.so_tien) }
})

/** DEMO: đơn chỉ trong bộ nhớ (mã DM...), không vào nap_don, không cộng gì. */
async function taoDonDemo(event: import('h3').H3Event) {
  const u = await napDemoRequireUser(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const maGoi = str(body?.ma_goi).slice(0, 20)
  const charId = int(body?.char_id, 0)
  if (charId <= 0) throw apiLoi(400, 'Hãy chọn Pokémon nhận.')
  const goi = (await napDemoGoi()).find(g => g.ma === maGoi)
  if (!goi) throw apiLoi(404, 'Gói không tồn tại hoặc đã ngừng bán.')
  const nv = (await napDemoNhanVat(u.id)).find(n => n.id === charId)
  const don = napDemoKho().tao(u, goi, nv?.charname ?? `Pokémon #${charId}`, khoaIdemHopLe(body?.khoa_idem))
  return { ma_don: don.ma_don, so_tien: don.so_tien, demo: true }
}
