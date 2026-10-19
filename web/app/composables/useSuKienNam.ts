/**
 * Lịch sự kiện năm thật (API /api/su-kien, máy chủ game ghi tob_su_kien_lich) + đồng hồ đếm ngược theo giờ CSDL.
 * Dùng ở trang chủ (mục "Sự kiện") và Cẩm nang › Sự kiện. Rỗng khi máy chủ game chưa chạy bản có lịch năm.
 */
import type { SuKienLan } from '#shared/utils/suKienNam'
import { dangVaSapToi, msTuChuoi } from '#shared/utils/suKienNam'

export function useSuKienNam(soNgaySapToi = 30) {
  const { data } = useFetch<{ bayGio: string | null, ds: SuKienLan[] }>('/api/su-kien', {
    key: 'htbb-su-kien-nam', dedupe: 'defer', default: () => ({ bayGio: null, ds: [] }),
  })
  // Lệch giữa đồng hồ CSDL và máy người xem, để đếm ngược đúng giờ máy chủ.
  const lech = computed(() => {
    const t = msTuChuoi(data.value?.bayGio ?? '')
    return Number.isFinite(t) ? t - Date.now() : 0
  })
  const bay = ref(Date.now())
  let h: ReturnType<typeof setInterval> | undefined
  onMounted(() => {
    bay.value = Date.now() + lech.value
    h = setInterval(() => { bay.value = Date.now() + lech.value }, 30_000)
  })
  onBeforeUnmount(() => clearInterval(h))
  watch(lech, (l) => { if (import.meta.client) bay.value = Date.now() + l })
  const ds = computed(() => data.value?.ds ?? [])
  const nhom = computed(() => dangVaSapToi(ds.value, bay.value, soNgaySapToi))
  return { ds, bay, dang: computed(() => nhom.value.dang), sap: computed(() => nhom.value.sap), coDuLieu: computed(() => ds.value.length > 0) }
}
