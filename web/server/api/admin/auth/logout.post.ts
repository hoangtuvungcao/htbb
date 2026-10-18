/**
 * Thoát khỏi khu quản trị.
 *
 * Chỉ "đóng" phiên ADMIN — token người chơi (localStorage) không bị đụng tới, người
 * đang đăng nhập trang công khai bằng cùng trình duyệt vẫn ở nguyên đó.
 *
 * Token ký HMAC không có trạng thái phía máy chủ nên không thu hồi được: trình
 * duyệt xoá token khỏi sessionStorage, còn bản sao (nếu có ai chép được) tự hết
 * hạn sau HTBB_ADMIN_IDLE_TIMEOUT giây. Phiên đã hết hạn thì API trả 401 và
 * không ghi nhật ký — trình duyệt vẫn tự xoá token của mình.
 */
export default defineAdminHandler(async (event, admin) => {
  await ghiNhatKy(event, 'dang_xuat', admin.username, 'rời trang quản trị')
  return { ok: true, message: 'Đã thoát khỏi khu quản trị.' }
})
