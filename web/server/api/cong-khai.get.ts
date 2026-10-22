/** Nội dung sửa được của website (cài đặt, trang chủ, menu, tải game) — CSDL trộn mặc định.
 *  Chỉ trả trường dùng để hiển thị: bỏ cấu hình nội bộ (nguồn trạng thái máy chủ, số bản BAN của tệp tải —
 *  admin đọc số bản qua /api/admin/cms/cai-dat). */
export default defineEventHandler(async (event) => {
  setHeader(event, 'Cache-Control', 'public, max-age=15')
  const c = await cmsCongKhai()
  return {
    ...c,
    site: { ...c.site, trangThaiUrl: '' },
    taiGame: { ban: c.taiGame.ban.map(({ ban: _ban, ...b }) => b) },
  }
})
