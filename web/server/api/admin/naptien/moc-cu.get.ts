/** Đơn nạp MỐC CŨ (tob_nap_moc) — chỉ xem để đối soát; hệ thống mới không bán mốc nữa. */
import type { DonNapMoc } from '../../../utils/napMoc'

export default defineAdminHandler(async () => {
  if (!(await coBang(dbGame(), 'tob_nap_moc'))) return { dons: [] }
  const dons = await q<DonNapMoc>(dbGame(), `SELECT ${NAP_MOC_COT} FROM tob_nap_moc ORDER BY id DESC LIMIT 200`)
  return { dons: await napMocGanTen(dons) }
})
