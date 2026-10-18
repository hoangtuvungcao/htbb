/**
 * Mua Thành viên HTBB: tạo đơn THANH_VIEN (luồng nạp chung: VietQR + SePay, idempotent, trần nạp).
 * Body: { khoa_idem } -> { ma_don, so_tien }. Công tắc bán riêng (thanh_vien.ban_bat) + giấy phép + cổng an toàn.
 */
import { TV_MA_GOI } from '../../utils/thanhVienThuan'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  if (!(await napCoBang()) || !(await tvCoBang())) tvThieuBang()
  const ban = await tvBanDuoc()
  if (!ban.duoc) throw apiLoi(403, 'Hiện chưa mở bán Thành viên HTBB.', { code: 'tv_tat' })
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  if (await biChanSpam(event, 'tao_don_nap', 10, 3600)) throw apiLoi(429, 'Bạn đã tạo quá nhiều đơn. Thử lại sau một giờ.')
  await napHetHanDonCu()
  const don = await napTaoDon(u, TV_MA_GOI, 0, khoaIdemHopLe(body?.khoa_idem))
  await ghiLanLam(event, 'tao_don_nap')
  return { ma_don: don.ma_don, so_tien: Number(don.so_tien) }
})
