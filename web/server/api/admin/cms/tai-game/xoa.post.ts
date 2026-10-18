/** Xoá một tệp trong kho /tai/. Body { ten }. Link trong "Bản tải game" trỏ tới tệp này sẽ hỏng — trang báo trước. */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const ten = typeof b.ten === 'string' ? b.ten : ''
  if (!(await xoaTepGame(ten))) throw apiLoi(404, 'Không có tệp này.')
  await ghiNhatKy(event, 'tai_game_xoa', ten.slice(0, 100), '')
  return { ok: true }
})
