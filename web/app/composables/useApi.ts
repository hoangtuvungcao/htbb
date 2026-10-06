/**
 * Gọi API của website.
 *
 *   const api = useApi()          // API người chơi: tự gắn token người chơi
 *   const admin = useAdminApi()   // API quản trị: tự gắn token admin + nhận token gia hạn
 *
 *   await api('/api/auth/login', { method: 'POST', body: { username, password } })
 *
 * Lỗi từ server luôn có `data.message` tiếng Việt — lấy bằng loiApi(e).
 */
import type { FetchError } from 'ofetch'

export function loiApi(e: unknown, macDinh = 'Có lỗi xảy ra, vui lòng thử lại.'): string {
  const fe = e as FetchError<{ message?: string }>
  return fe?.data?.message || (fe?.statusCode ? macDinh : 'Không kết nối được tới máy chủ. Kiểm tra mạng rồi thử lại.')
}

export function useApi() {
  const auth = useAuth()
  return $fetch.create({
    onRequest({ options }) {
      auth.hydrate()
      if (auth.token.value) {
        const h = new Headers(options.headers as HeadersInit)
        h.set('Authorization', `Bearer ${auth.token.value}`)
        options.headers = h
      }
    },
    onResponseError({ response }) {
      if (response.status === 401 && response._data?.data?.code === 'not_logged_in') {
        auth.clear()
      }
    },
  })
}

export function useAdminApi() {
  const auth = useAdminAuth()
  // Lấy router ngay lúc setup: trong callback của fetch không còn ngữ cảnh Nuxt.
  const router = useRouter()
  return $fetch.create({
    onRequest({ options }) {
      auth.hydrate()
      if (auth.token.value) {
        const h = new Headers(options.headers as HeadersInit)
        h.set('Authorization', `Bearer ${auth.token.value}`)
        options.headers = h
      }
    },
    onResponse({ response }) {
      const t = response.headers.get('x-kpah-token')
      if (t) auth.refresh(t)
    },
    onResponseError({ response }) {
      if (response.status === 401 && response._data?.data?.code === 'admin_required') {
        auth.clear()
        if (import.meta.client && window.location.pathname !== '/admin') {
          router.push('/admin')
        }
      }
    },
  })
}
