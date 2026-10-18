/**
 * Lịch sự kiện: CSDL nếu admin đã nhập (web_su_kien_lich), không thì lịch 52 tuần của game (data_su_kien_nam,
 * dự phòng JSON sinh từ su_kien_nam.csv).
 * Tách khỏi useCongKhai.ts để khung trang (layout) không kéo theo cam-nang.json.
 */
export function useLichSuKien() {
  const { data } = useFetch<SuKienTuan[] | null>('/api/su-kien-lich', { key: 'htbb-lich', dedupe: 'defer', default: () => null })
  const game = useSuKienCamNang()
  return computed<SuKienTuan[]>(() => (data.value && data.value.length ? data.value : game.value))
}
