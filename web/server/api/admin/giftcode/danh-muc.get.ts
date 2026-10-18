/**
 * Danh mục cho bộ chọn vật phẩm (~1350 lựa chọn: vật phẩm, thuốc, ngọc, thú, danh hiệu).
 * CHỈ ĐỌC và chỉ cho admin (defineAdminHandler): danh mục tự nó không bí mật, nhưng mọi
 * đường vào trang quản trị đều phải qua cùng một cửa xác thực.
 */
export default defineAdminHandler(async () => ({ dmuc: await gcDanhMuc() }))
