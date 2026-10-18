/**
 * Phiên đăng nhập của người chơi và của admin, giữ trong trình duyệt.
 *
 * Không dùng cookie vì Nova WAF nuốt Cookie/Set-Cookie (xem server/utils/token.ts).
 *  - Người chơi: localStorage (giữ 7 ngày, hết hạn do server quyết định).
 *  - Admin: sessionStorage — đóng tab/trình duyệt là mất phiên; thêm vào đó
 *    server tự hết hạn sau 60 phút không thao tác.
 *
 * Token đã quá hạn (đọc mốc `exp` trong phần dữ liệu của token) bị bỏ ngay khi
 * nạp lại, để đầu trang không hiện "đã đăng nhập" với một token server sẽ từ chối.
 * Việc này chỉ cho giao diện gọn: chữ ký và hạn thật luôn do server kiểm.
 */
export interface WebUser {
  id: number
  username: string
}

const USER_KEY = 'htbb_user'
const ADMIN_KEY = 'htbb_admin'

interface Saved<T> { token: string, info: T }

/** Mốc hết hạn (giây) ghi trong token, hoặc null nếu không đọc được. */
function hetHanLuc(token: string): number | null {
  try {
    const du = token.split('.')[0] ?? ''
    const json = atob(du.replace(/-/g, '+').replace(/_/g, '/'))
    const exp = (JSON.parse(json) as { exp?: unknown }).exp
    return typeof exp === 'number' ? exp : null
  } catch {
    return null
  }
}

function doc<T>(store: Storage | undefined, key: string): Saved<T> | null {
  if (!store) return null
  try {
    const raw = store.getItem(key)
    const s = raw ? JSON.parse(raw) as Saved<T> : null
    if (!s || typeof s.token !== 'string') return null
    const exp = hetHanLuc(s.token)
    if (exp !== null && exp * 1000 < Date.now()) {
      store.removeItem(key)
      return null
    }
    return s
  } catch {
    return null
  }
}

function ghi<T>(store: Storage | undefined, key: string, v: Saved<T> | null) {
  if (!store) return
  try {
    if (v) store.setItem(key, JSON.stringify(v))
    else store.removeItem(key)
  } catch { /* chế độ riêng tư chặn storage: bỏ qua */ }
}

const local = () => (import.meta.client ? window.localStorage : undefined)
const session = () => (import.meta.client ? window.sessionStorage : undefined)

export function useAuth() {
  const token = useState<string | null>('htbb-user-token', () => null)
  const user = useState<WebUser | null>('htbb-user', () => null)
  const ready = useState<boolean>('htbb-user-ready', () => false)

  function hydrate() {
    if (!import.meta.client || ready.value) return
    const s = doc<WebUser>(local(), USER_KEY)
    token.value = s?.token ?? null
    user.value = s?.info ?? null
    ready.value = true
  }

  function setSession(t: string, info: WebUser) {
    token.value = t
    user.value = info
    ghi(local(), USER_KEY, { token: t, info })
  }

  function clear() {
    token.value = null
    user.value = null
    ghi(local(), USER_KEY, null)
  }

  return { token, user, ready, hydrate, setSession, clear, loggedIn: computed(() => !!user.value) }
}

export function useAdminAuth() {
  const token = useState<string | null>('htbb-admin-token', () => null)
  const admin = useState<WebUser | null>('htbb-admin', () => null)
  const ready = useState<boolean>('htbb-admin-ready', () => false)

  function hydrate() {
    if (!import.meta.client || ready.value) return
    const s = doc<WebUser>(session(), ADMIN_KEY)
    token.value = s?.token ?? null
    admin.value = s?.info ?? null
    ready.value = true
  }

  function setSession(t: string, info: WebUser) {
    token.value = t
    admin.value = info
    ghi(session(), ADMIN_KEY, { token: t, info })
  }

  /** Server cấp token mới sau mỗi thao tác (gia hạn trượt). */
  function refresh(t: string) {
    if (!admin.value) return
    token.value = t
    ghi(session(), ADMIN_KEY, { token: t, info: admin.value })
  }

  function clear() {
    token.value = null
    admin.value = null
    ghi(session(), ADMIN_KEY, null)
  }

  return { token, admin, ready, hydrate, setSession, refresh, clear, loggedIn: computed(() => !!admin.value) }
}

/** Thông tin admin đang đăng nhập + vai trò (server trả ở /api/admin/auth/me). */
export function useAdminMe() {
  const api = useAdminApi()
  const { data, refresh } = useAsyncData('admin-me', () => api<{ admin: { username: string, vaiTro: VaiTroAdmin }, coBangNhatKy: boolean, coBangCms: boolean }>('/api/admin/auth/me'), { server: false })
  const vaiTro = computed<VaiTroAdmin>(() => data.value?.admin?.vaiTro ?? 'xem')
  return { data, refresh, vaiTro, coBangCms: computed(() => data.value?.coBangCms ?? true), duocGhiNoiDung: computed(() => ['sieu_quan_tri', 'bien_tap'].includes(vaiTro.value)),
    /** Vai trò hiện tại có gọi được API này không (cùng ma trận với server — chỉ để ẩn nút). */
    duoc: (path: string, method = 'POST') => !!data.value && kiemQuyenAdmin(path, method, vaiTro.value) }
}
