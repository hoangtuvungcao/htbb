/** Cấu hình công khai (link tải game, nhóm hỗ trợ...) — tải một lần, dùng chung. */
export interface SiteConfig {
  name: string
  tagline: string
  linkAndroid: string
  linkPc: string
  linkIos: string
  linkJar: string
  linkGroup: string
  autoActivate: boolean
  /** Nạp tiền đang bật (HTBB_NAP_BAT). Tắt = ẩn menu Nạp và trang /naptien, /xac-nhan-nap. */
  napBat: boolean
  /** Địa chỉ gốc từ HTBB_SITE_URL (mặc định http://localhost:3000) — canonical, OG, JSON-LD. */
  siteUrl: string
  /** false = chưa có tên miền thật -> noindex, nofollow. */
  choIndex: boolean
  /** Đã cấu hình gửi mail -> có trang Quên mật khẩu; chưa thì chỉ hiện kênh hỗ trợ. */
  quenMatKhau?: boolean
}

export function useSite() {
  return useFetch<SiteConfig>('/api/site', { key: 'htbb-site', dedupe: 'defer' })
}

/** Địa chỉ gốc của website (một nguồn: HTBB_SITE_URL qua /api/site; SSR và trình duyệt như nhau). */
export function useGocSite() {
  const { data } = useSite()
  return computed(() => data.value?.siteUrl || GOC_SITE_MAC_DINH)
}
