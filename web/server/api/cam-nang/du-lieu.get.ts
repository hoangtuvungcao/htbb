/**
 * Số liệu cẩm nang đọc từ CSDL game (phần đè lên JSON đóng gói): { camNang, noiDung, nguon }.
 * Mục nào bảng thiếu/trống thì vắng mặt -> trang giữ bản JSON (app/composables/useCamNang.ts).
 */
export default defineEventHandler(async (event) => {
  setHeader(event, 'Cache-Control', 'public, max-age=300')
  return await camNangDeSql()
})
