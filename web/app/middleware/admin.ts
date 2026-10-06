/**
 * Trang admin cần đăng nhập. Chưa đăng nhập -> về /admin (form đăng nhập).
 * Đây chỉ là tiện cho giao diện; quyền thật được server kiểm ở MỌI API admin.
 */
export default defineNuxtRouteMiddleware((to) => {
  if (import.meta.server) return
  const auth = useAdminAuth()
  auth.hydrate()
  if (!auth.loggedIn.value && to.path !== '/admin') {
    return navigateTo('/admin')
  }
})
