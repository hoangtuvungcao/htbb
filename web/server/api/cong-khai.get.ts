/** Nội dung sửa được của website (cài đặt, trang chủ, menu, tải game) — CSDL trộn mặc định.
 *  Chỉ trả trường dùng để hiển thị: bỏ cấu hình nội bộ (nguồn trạng thái máy chủ). */
export default defineEventHandler(async (event) => {
  setHeader(event, 'Cache-Control', 'public, max-age=15')
  const c = await cmsCongKhai()
  return { ...c, site: { ...c.site, trangThaiUrl: '' } }
})
