/** Các đơn nạp đang chờ thanh toán (trang /xac-nhan-nap, mật khẩu chung). */
export default defineEventHandler(async (event) => {
  napMocRequireTrangAdmin(event)
  if (!(await napCoBang())) napThieuBang()
  try {
    await napHetHanDonCu()
    const dons = await q(dbGame(),
      "SELECT id, ma_don, username, charname, ten_goi, so_tien, tao_luc FROM nap_don WHERE trang_thai = 'cho_thanh_toan' ORDER BY id DESC LIMIT 200")
    return { dons }
  } catch (e: any) {
    console.error('[xac-nhan-nap] doc don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa đọc được danh sách đơn. Kiểm tra MySQL rồi thử lại.')
  }
})
