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
}

export function useSite() {
  return useFetch<SiteConfig>('/api/site', { key: 'kpah-site', dedupe: 'defer' })
}

/** Link nhóm hỗ trợ, rơi về mục hỗ trợ của trang tải game khi chưa cấu hình. */
export function linkHoTro(site: SiteConfig | null | undefined): string {
  const g = site?.linkGroup?.trim() ?? ''
  return g !== '' && g !== '#' ? g : '/download#support'
}
