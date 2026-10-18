/**
 * Trang cần đăng nhập người chơi. Chưa đăng nhập -> /login.
 * Quyền thật do server kiểm ở API; đây chỉ để chuyển trang cho tiện.
 */
export default defineNuxtRouteMiddleware(() => {
  if (import.meta.server) return
  const auth = useAuth()
  auth.hydrate()
  if (!auth.loggedIn.value) {
    return navigateTo('/login')
  }
})
