/**
 * Kênh hỗ trợ chính thức = Zalo (nhóm / OA). Nguồn: /admin/cai-dat -> "Hỗ trợ qua Zalo"
 * (cms.site.hoTroZalo); CMS chưa đặt thì dùng HTBB_LINK_GROUP nếu đó là link Zalo hợp lệ.
 * Chưa có link -> coZalo = false: các nút Zalo ẩn, link hỗ trợ rơi về /download#support.
 */
export function useHoTro() {
  const cms = useCongKhai()
  const { data: site } = useSite()
  const zalo = computed(() => {
    const z = cms.value.site.hoTroZalo ?? CMS_MAC_DINH.site.hoTroZalo
    const url = lienKetZalo(z.url) || lienKetZalo(site.value?.linkGroup)
    return {
      url,
      anhQr: url ? anhTrongSite(z.anhQr) : '',
      ten: z.ten || CMS_MAC_DINH.site.hoTroZalo.ten,
      moTa: z.moTa,
    }
  })
  const coZalo = computed(() => zalo.value.url !== '')
  /** Dùng cho chữ "nhóm hỗ trợ" trong bài: Zalo nếu có, không thì mục hỗ trợ của trang Tải game. */
  const link = computed(() => zalo.value.url || '/download#support')
  return { zalo, coZalo, link }
}
