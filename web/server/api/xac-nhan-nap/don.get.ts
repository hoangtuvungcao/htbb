/** Các đơn đang chờ thanh toán (trang /xac-nhan-nap, mật khẩu chung). */
export default defineEventHandler(async (event) => {
  napMocRequireTrangAdmin(event)
  try {
    return { dons: await napMocDonDangCho() }
  } catch (e: any) {
    console.error('[admin-naptien] doc don loi:', e?.code ?? e)
    throw apiLoi(503, 'Chưa đọc được danh sách đơn. Kiểm tra MySQL rồi thử lại.')
  }
})
