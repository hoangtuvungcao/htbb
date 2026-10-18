/**
 * Thông tin cho form đăng nhập quản trị (chưa cần đăng nhập): đã cấu hình
 * HTBB_ADMIN_ACCOUNTS chưa, để form hiện khung "Chưa ai được cấp quyền" khi
 * danh sách rỗng. Chỉ lộ đúng một bit đó, không lộ tên nào.
 */
export default defineEventHandler(() => ({
  coAdmin: adminAccounts().length > 0,
}))
