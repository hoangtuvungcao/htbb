/**
 * Bản DEMO nạp tiền / Thành viên (HTBB_NAP_DEMO=1, server/utils/napDemoThuan.ts).
 * Server quyết định ai được xem (HTBB_NAP_DEMO_CHI_ADMIN) theo token người chơi, nên chỉ hỏi được ở
 * trình duyệt. Không được xem -> { demo: false } (giao diện như khi nạp tắt).
 */
export interface NapDemoInfo {
  demo: boolean
  chiAdmin?: boolean
  banner?: string
  nganHang?: { bin: string, soTaiKhoan: string, chuTaiKhoan: string }
}

export function useNapDemo() {
  const api = useApi()
  const auth = useAuth()
  const info = useState<NapDemoInfo | null>('htbb-nap-demo', () => null)
  const choAi = useState<string | null>('htbb-nap-demo-ai', () => null)

  /** Tải (một lần cho mỗi người đăng nhập). Phía server luôn trả { demo: false }. */
  async function tai(): Promise<NapDemoInfo> {
    if (!import.meta.client) return { demo: false }
    auth.hydrate()
    const ai = auth.user.value?.username ?? ''
    if (info.value && choAi.value === ai) return info.value
    try {
      info.value = await api<NapDemoInfo>('/api/nap-demo')
    } catch {
      info.value = { demo: false }
    }
    choAi.value = ai
    return info.value
  }

  return { info, tai, xem: computed(() => !!info.value?.demo) }
}
