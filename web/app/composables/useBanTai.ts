/**
 * Danh sách bản tải game dùng chung cho MỌI nơi liệt kê nền tảng (Tải game, Hướng dẫn tân thủ,
 * trang chủ, FAQ, chân trang): CMS /admin/tai-game (shared/utils/taiGame.ts) + link biến môi trường
 * HTBB_LINK_* khi CMS để trống. Bật iOS / đổi trạng thái ở admin là mọi trang đổi theo.
 */
export function useBanTai() {
  const cms = useCongKhai()
  const { data: site } = useSite()
  const linkEnv = (ma: string): string => {
    const s = site.value
    const v = ({ android: s?.linkAndroid, windows: s?.linkPc, jar: s?.linkJar, ios: s?.linkIos } as Record<string, string | undefined>)[ma] ?? ''
    return v && v !== '#' ? v : ''
  }
  const ds = computed(() => cms.value.taiGame.ban
    .filter(b => b.hien)
    .map((b) => {
      const n = nenTang(b.ma)
      const env = linkEnv(b.ma)
      const ban = !hrefCaiDat(b) && b.kieu === 'tep' && env ? { ...b, url: env } : b
      const href = ban.trangThai === 'san-sang' ? hrefCaiDat(ban) : ''
      return { ...ban, href, sanSang: !!href, sapRaMat: ban.trangThai === 'sap-ra-mat', icon: n?.icon ?? 'i-lucide-box', chiTiet: n?.chiTiet ?? [], buoc: buocCaiDat(ban) }
    }))
  /** "Java (J2ME), Windows, Linux và Android" — chỉ các bản đã/đang phát hành (không tính "sắp ra mắt"). */
  const tenDangCo = computed(() => noiDanhSach(ds.value.filter(b => !b.sapRaMat).map(b => b.ten)))
  const tenSapRaMat = computed(() => noiDanhSach(ds.value.filter(b => b.sapRaMat).map(b => b.ten)))
  return { ds, tenDangCo, tenSapRaMat }
}

export function noiDanhSach(ds: string[]): string {
  if (ds.length <= 1) return ds[0] ?? ''
  return `${ds.slice(0, -1).join(', ')} và ${ds[ds.length - 1]}`
}
