/**
 * Nội dung sửa được từ khu quản trị (CSDL) cho trang công khai — luôn có giá trị: lỗi / chưa có
 * bảng thì dùng mặc định (shared/utils/cmsMacDinh.ts). Một lần tải dùng chung mọi component.
 */
export function useCongKhai() {
  const { data } = useFetch<CmsCongKhai>('/api/cong-khai', {
    key: 'htbb-cong-khai',
    dedupe: 'defer',
    default: () => CMS_MAC_DINH,
  })
  return computed<CmsCongKhai>(() => data.value ?? CMS_MAC_DINH)
}

/** Lịch sự kiện: CSDL nếu admin đã nhập, không thì dữ liệu sinh từ su_kien_nam.csv. */
export function useLichSuKien() {
  const { data } = useFetch<SuKienTuan[] | null>('/api/su-kien-lich', { key: 'htbb-lich', dedupe: 'defer', default: () => null })
  return computed<SuKienTuan[]>(() => (data.value && data.value.length ? data.value : CAM_NANG.suKien))
}

/** Trang cẩm nang trong CSDL (ghi đè + trang mới). */
export function useTrangDb() {
  const { data } = useFetch<TrangDbCongKhai[]>('/api/cam-nang-db', { key: 'htbb-trang-db', dedupe: 'defer', default: () => [] })
  return computed<TrangDbCongKhai[]>(() => data.value ?? [])
}

export interface TrangDbCongKhai {
  slug: string
  ten: string
  nhom: string
  icon: string
  moTa: string
  tuKhoa: string
  thuTu: number
  hien: boolean
  coSan: boolean
  muc: { id: string, ten: string }[]
}

/** Danh mục cẩm nang sau khi trộn CSDL: trang có sẵn (có thể bị đổi tên/ẩn/xếp lại) + trang mới. */
export function useDanhMucCamNang() {
  const db = useTrangDb()
  return computed<TrangCamNang[]>(() => {
    const theoSlug = new Map(db.value.map(t => [t.slug, t]))
    const ra: (TrangCamNang & { thuTu: number })[] = []
    DS_CAM_NANG.forEach((p, i) => {
      const o = theoSlug.get(p.slug)
      if (o && !o.hien) return
      ra.push({ ...p, ...(o ? { ten: o.ten || p.ten, moTa: o.moTa || p.moTa, nhom: o.nhom || p.nhom, icon: o.icon || p.icon, tuKhoa: `${p.tuKhoa ?? ''} ${o.tuKhoa}`, muc: [...(p.muc ?? []), ...o.muc] } : {}), thuTu: o ? o.thuTu : i * 10 })
    })
    for (const t of db.value) {
      if (t.coSan || !t.hien) continue
      ra.push({ slug: t.slug, ten: t.ten, nhom: (NHOM_CAM_NANG as readonly string[]).includes(t.nhom) ? t.nhom : 'Thế giới', icon: t.icon, moTa: t.moTa, tuKhoa: t.tuKhoa, muc: t.muc, thuTu: t.thuTu })
    }
    return ra.sort((a, b) => a.thuTu - b.thuTu)
  })
}
