/**
 * CHẾ ĐỘ DEMO (HTBB_NAP_DEMO=1): đơn demo trong bộ nhớ + webhook bị từ chối gần đây.
 * Đơn demo không nằm trong nap_don, không bao giờ được cộng. -> { demo, dons, webhook }
 */
export default defineAdminHandler(() => {
  const n = useHtbbConfig().nap
  if (!n.demo) return { demo: false, dons: [], webhook: [] }
  const kho = napDemoKho()
  return {
    demo: true,
    dons: kho.tatCa().slice(0, 200).map(d => ({ ...donDemoCongKhai(d), username: d.username, gia_lap_boi: d.gia_lap_boi })),
    webhook: kho.webhook(),
  }
})
