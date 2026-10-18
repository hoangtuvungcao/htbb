/**
 * Trạng thái + thao tác của "Cấu hình hệ thống" (trang /admin/cau-hinh-he-thong và khối nhúng
 * AdminHethongCauHinhNhom ở các trang Nạp tiền / Giftcode…). Một nguồn: GET /api/admin/cau-hinh-he-thong.
 * Chỉ giữ CÁC KHOÁ ĐÃ SỬA trong `sua` — khoá không chạm không bao giờ được gửi lên.
 */
export interface HieuLucTT { giaTri: string | null, nguon: string, cho: string | null, loi: string | null, luc: string | null }
export interface MayChuTT {
  may_chu: string, phien_ban: number, ap_dung_luc: string | null, khoi_dong_luc: string | null, nhip_luc: string | null
  so_khoa: number, so_loi: number, loi: string | null, cho_khoi_dong_lai: string | null, thay_doi_cuoi: string | null
}
export interface DuLieuTT {
  loiDoc: string
  giaTri: Record<string, string | null>
  suaBoi: Record<string, { boi: string | null, luc: string | null }>
  env: Record<string, string>
  nap: { bat: boolean, yeuCauBat: boolean, daCoGiayPhep: boolean, congAnToan: boolean, loiCauHinh: string[], lyDoTat: string[] }
  hieuLuc: Record<string, HieuLucTT>
  mayChu: MayChuTT[]
  giayTuNhip: number | null
  phienBan: { phien_ban: number, sua_luc: string | null, sua_boi: string | null }
  khoiDongLai: { trang_thai: string, giay: number, ly_do: string, yeu_cau_boi: string | null, yeu_cau_luc: string | null, bat_dau_luc: string | null, thoat_luc: string | null, xong_luc: string | null, may_chu: string | null } | null
  quyen: { ghi: boolean, choPhepThuNghiem: boolean }
  /** Danh mục máy chủ web đang dùng (CSDL cau_hinh_danh_muc + cau_hinh_nhom, hoặc bản sinh). */
  danhMuc?: KhoaTT[]
  nhom?: NhomTT[]
  nguonDanhMuc?: 'sql' | 'gen'
}

let danhMucCuoi = ''

export function useCauHinhTT(khoaAsyncData = 'admin-cau-hinh-tt') {
  const api = useAdminApi()
  const toast = useToast()
  const { data, status, error, refresh } = useAsyncData(khoaAsyncData, () => api<DuLieuTT>('/api/admin/cau-hinh-he-thong'), { server: false })
  const sua = ref<Record<string, string | null>>({})
  const dangLuu = ref(false)
  /** Tăng khi danh mục từ API thay bản sinh: computed nào đọc danhMucTT()/khoaTT() phụ thuộc vào đây. */
  const phienDanhMuc = useState('htbb-tt-danh-muc', () => 0)
  watch(data, (d) => {
    if (!d?.danhMuc?.length || !d.nhom?.length) return
    const chuoi = JSON.stringify([d.danhMuc, d.nhom])
    if (chuoi === danhMucCuoi) return
    danhMucCuoi = chuoi
    if (datDanhMucTT({ ds: d.danhMuc, nhom: d.nhom })) phienDanhMuc.value++
  }, { immediate: true, flush: 'sync' })

  /** Giá trị đang lưu trong CSDL (null = chưa có dòng). Khoá env: cờ từ biến môi trường. */
  function luu(k: KhoaTT): string | null {
    if (k.bang === 'env') return data.value?.env[k.khoa] ?? null
    return data.value?.giaTri[k.khoa] ?? null
  }
  /** Giá trị máy chủ đang chạy (cau_hinh_hieu_luc); khoá web/env: chính giá trị lưu hoặc mặc định. */
  function dangChay(k: KhoaTT): string {
    const h = data.value?.hieuLuc[k.khoa]
    if (h && h.giaTri !== null) return h.giaTri
    return luu(k) ?? k.macDinh
  }
  /** Giá trị hiện trên ô nhập. */
  function hienTai(k: KhoaTT): string {
    if (Object.prototype.hasOwnProperty.call(sua.value, k.khoa)) return sua.value[k.khoa] ?? dangChayKhongDb(k)
    return luu(k) ?? dangChayKhongDb(k)
  }
  /** Khi không có dòng CSDL: máy chủ dùng ini / mặc định — hiển thị đúng thứ đó. */
  function dangChayKhongDb(k: KhoaTT): string {
    const h = data.value?.hieuLuc[k.khoa]
    if (h && h.giaTri !== null && h.nguon !== 'db' && !h.cho) return h.giaTri
    return k.macDinh
  }
  function datGiaTri(k: KhoaTT, v: string | null) {
    const goc = luu(k)
    const m = { ...sua.value }
    if (v !== null && v === goc) delete m[k.khoa]
    else if (v === null && goc === null) delete m[k.khoa]
    else m[k.khoa] = v
    sua.value = m
  }
  function boSua(khoa?: string) {
    if (!khoa) { sua.value = {}; return }
    const m = { ...sua.value }
    delete m[khoa]
    sua.value = m
  }
  const loiNhap = computed<Record<string, string>>(() => {
    void phienDanhMuc.value
    const ra: Record<string, string> = {}
    for (const [khoa, v] of Object.entries(sua.value)) {
      const k = khoaTT(khoa)
      if (!k || v === null) continue
      const r = kiemGiaTriTT(k, v)
      if (!r.ok) ra[khoa] = r.loi
    }
    return ra
  })
  const soSanh = computed(() => {
    void phienDanhMuc.value
    const dang: Record<string, string | null> = {}
    for (const khoa of Object.keys(sua.value)) {
      const k = khoaTT(khoa)
      if (k) dang[khoa] = luu(k)
    }
    return soSanhTT(dang, sua.value, data.value?.quyen.choPhepThuNghiem ?? false)
  })
  const soThayDoi = computed(() => soSanh.value.thayDoi.length)

  async function luuThayDoi(lyDo: string, chiKhoa?: string[]): Promise<boolean> {
    const thayDoi: Record<string, string | null> = {}
    for (const d of soSanh.value.thayDoi) {
      if (!chiKhoa || chiKhoa.includes(d.khoa)) thayDoi[d.khoa] = d.moi
    }
    if (!Object.keys(thayDoi).length) return false
    dangLuu.value = true
    try {
      const r = await api<{ message: string }>('/api/admin/cau-hinh-he-thong/luu', {
        method: 'POST', body: { thayDoi, lyDo, phienBan: data.value?.phienBan.phien_ban },
      })
      toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
      for (const k of Object.keys(thayDoi)) boSua(k)
      await refresh()
      return true
    } catch (e) {
      toast.add({ title: 'Chưa lưu được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
      return false
    } finally {
      dangLuu.value = false
    }
  }

  /** Máy chủ chính (nhịp gần nhất) + trạng thái áp dụng so với phiên bản CSDL. */
  const mayChinh = computed(() => data.value?.mayChu[0] ?? null)
  const trangThaiApDung = computed<{ mau: 'success' | 'warning' | 'error' | 'neutral', chu: string }>(() => {
    const d = data.value
    if (!d) return { mau: 'neutral', chu: 'Đang tải…' }
    const m = mayChinh.value
    if (!m) return { mau: 'error', chu: 'Chưa có máy chủ game nào xác nhận cấu hình (máy chủ chưa chạy bản có cấu hình trung tâm?)' }
    if (d.giayTuNhip === null || d.giayTuNhip > 90) return { mau: 'error', chu: `Máy chủ ${m.may_chu} không phản hồi (${d.giayTuNhip ?? '?'} giây không có nhịp)` }
    if (Number(m.phien_ban) >= d.phienBan.phien_ban) return { mau: 'success', chu: `Máy chủ ${m.may_chu} đã áp dụng phiên bản ${m.phien_ban}` }
    return { mau: 'warning', chu: `Máy chủ ${m.may_chu} đang ở phiên bản ${m.phien_ban} — sắp áp dụng phiên bản ${d.phienBan.phien_ban} (≤ 10 giây)` }
  })

  return { data, status, error, refresh, phienDanhMuc, sua, dangLuu, luu, dangChay, hienTai, datGiaTri, boSua, loiNhap, soSanh, soThayDoi, luuThayDoi, mayChinh, trangThaiApDung }
}
