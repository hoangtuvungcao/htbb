/** Lịch sử 50 đơn nạp gần nhất của chính tài khoản. -> { dons } */
export default defineEventHandler(async (event) => {
  if (napDemoBat()) {
    const u = await napDemoRequireUser(event)
    return { dons: napDemoKho().cuaToi(u.id).filter(d => d.loai !== 'thanh_vien').map(donDemoCongKhai), demo: true }
  }
  const u = await requireUser(event)
  if (!(await napCoBang())) napThieuBang(true)
  try {
    await napHetHanDonCu()
    return { dons: (await napLichSu(u.id)).map(napDonCongKhai) }
  } catch (e: any) {
    console.error('[nap] lich su loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa tải được lịch sử nạp lúc này.')
  }
})
