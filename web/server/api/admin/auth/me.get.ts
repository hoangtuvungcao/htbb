/** Admin đang đăng nhập + bảng nhật ký đã có chưa (cho banner cảnh báo). */
export default defineAdminHandler(async (_event, admin) => ({
  admin,
  coBangNhatKy: await coBangNhatKy(),
}))
