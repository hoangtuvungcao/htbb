/**
 * Lịch sự kiện: CSDL nếu admin đã nhập, không thì dữ liệu sinh từ su_kien_nam.csv.
 * Tách khỏi useCongKhai.ts để khung trang (layout) không kéo theo cam-nang.json.
 */
export function useLichSuKien() {
  const { data } = useFetch<SuKienTuan[] | null>('/api/su-kien-lich', { key: 'htbb-lich', dedupe: 'defer', default: () => null })
  return computed<SuKienTuan[]>(() => (data.value && data.value.length ? data.value : CAM_NANG.suKien))
}

