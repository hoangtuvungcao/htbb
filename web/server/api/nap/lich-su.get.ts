/** Lịch sử 50 đơn nạp gần nhất của chính tài khoản. -> { dons } */
export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (!(await napCoBang())) napThieuBang()
  try {
    await napHetHanDonCu()
    return { dons: (await napLichSu(u.id)).map(napDonCongKhai) }
  } catch (e: any) {
    console.error('[nap] lich su loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa tải được lịch sử nạp lúc này.')
  }
})
