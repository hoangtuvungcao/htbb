<script setup lang="ts">
/**
 * Lịch sự kiện năm (tự động) trên /admin/sukien: mỗi lần diễn ra (năm trước .. năm sau) của mọi sự kiện có lịch, theo
 * ngày thật giờ Việt Nam (máy chủ game: com.htbb.pokemon.LichNam, bảng tob_su_kien_lich). Ghi đè MỘT năm: tắt năm nay,
 * đổi giờ, mở ngay, về theo lịch; hoặc chuyển cả sự kiện sang thủ công / tự động.
 */
import { demNguoc, gioDep, msTuChuoi } from '#shared/utils/suKienNam'

interface Lan {
  ma: string, nam: number, lan: number, ten: string, nhom: string, ngayLe: string, lichMo: string, lichDong: string,
  mo: string, dong: string, ghiDe: number, suaBoi: string | null, suaLuc: string | null, bat: number, dangBat: number, tuDong: number
}
interface DuLieu { coLich: boolean, coTuDong?: boolean, bayGio: string, ds: Lan[] }

const props = defineProps<{ duocGhi: boolean }>()
const emit = defineEmits<{ doi: [] }>()
const api = useAdminApi()
const toast = useToast()
const { data, status, refresh } = useAsyncData('admin-sukien-lich-nam', () => api<DuLieu>('/api/admin/sukien/lich-nam'), { server: false })

const chiSapToi = ref(true)
const bay = computed(() => msTuChuoi(data.value?.bayGio ?? ''))
const ds = computed(() => (data.value?.ds ?? []).filter(l => !chiSapToi.value || msTuChuoi(l.dong) > bay.value - 2 * 86_400_000))
const dangLam = reactive<Record<string, boolean>>({})
const sua = reactive<Record<string, { mo: string, dong: string }>>({})
const khoa = (l: Lan) => `${l.ma}:${l.nam}:${l.lan}`

function trangThai(l: Lan): { nhan: string, mau: 'success' | 'info' | 'neutral' | 'warning' | 'error' } {
  if (l.ghiDe === 1) return { nhan: 'Tắt năm nay', mau: 'error' }
  const mo = msTuChuoi(l.mo)
  const dong = msTuChuoi(l.dong)
  if (bay.value >= dong) return { nhan: 'Đã kết thúc', mau: 'neutral' }
  if (bay.value >= mo) return { nhan: `Đang diễn ra · còn ${demNguoc(dong - bay.value)}`, mau: 'success' }
  return { nhan: `Còn ${demNguoc(mo - bay.value)}`, mau: 'info' }
}
const choO = (v: string) => v.replace(' ', 'T').slice(0, 16)

async function goi(k: string, url: string, body: Record<string, unknown>) {
  dangLam[k] = true
  try {
    const kq = await api<{ message: string }>(url, { method: 'POST', body })
    toast.add({ title: kq.message, color: 'success', icon: 'i-lucide-check' })
    delete sua[k]
  }
  catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  }
  finally {
    dangLam[k] = false
    await refresh()
    emit('doi')
  }
}
const ghiDe = (l: Lan, kieu: string, them: Record<string, unknown> = {}) =>
  goi(khoa(l), '/api/admin/sukien/ghi-de', { ma: l.ma, nam: l.nam, lan: l.lan, kieu, ...them })
const doiTuDong = (l: Lan, tuDong: boolean) => goi(`td:${l.ma}`, '/api/admin/sukien/tu-dong', { ma: l.ma, tuDong: tuDong ? 1 : 0 })
function moSua(l: Lan) {
  sua[khoa(l)] = { mo: choO(l.mo), dong: choO(l.dong) }
}
defineExpose({ refresh })
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 class="font-semibold text-highlighted">Lịch năm (tự động)</h2>
          <p class="mt-1 max-w-3xl text-xs text-muted">
            Mọi sự kiện có lịch tự bật / tắt đúng ngày thật (giờ Việt Nam; Tết, Giỗ Tổ, Vu Lan, Trung Thu theo âm lịch).
            Mốc mở / đóng lúc 04:00 nên được áp dụng ở lần bảo trì hằng ngày (04:30–05:30) — không khởi động lại thêm.
            Ghi đè chỉ cho <strong>một năm</strong>; bật / tắt / hẹn giờ tay ở bảng dưới thì sự kiện chuyển sang thủ công.
            Website tự đăng tin "Sắp diễn ra" (3 ngày trước), "Đang diễn ra" và "Đã kết thúc".
          </p>
        </div>
        <div class="flex items-center gap-2">
          <USwitch v-model="chiSapToi" label="Chỉ đang / sắp tới" size="sm" />
          <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" size="sm" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
        </div>
      </div>
    </template>
    <UAlert v-if="data && !data.coLich" color="warning" variant="subtle" icon="i-lucide-calendar-x"
      title="Chưa có bảng tob_su_kien_lich"
      description="Máy chủ game bản mới tự tạo và ghi lịch lúc khởi động; hoặc chạy du_lieu/sql_nguon/them_lich_su_kien_nam.sql." />
    <div v-else class="divide-y divide-default">
      <div v-for="l in ds" :key="khoa(l)" class="flex flex-col gap-2 py-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="min-w-0">
          <p class="font-medium text-highlighted">
            {{ l.ten }} <span class="text-xs text-dimmed">{{ l.ma }} · {{ l.nam }}<template v-if="l.lan"> #{{ l.lan + 1 }}</template></span>
            <UBadge :color="trangThai(l).mau" variant="subtle" size="sm" class="ml-1">{{ trangThai(l).nhan }}</UBadge>
            <UBadge v-if="!l.tuDong" color="warning" variant="outline" size="sm" class="ml-1">Thủ công</UBadge>
            <UBadge v-if="l.ghiDe === 2" color="warning" variant="subtle" size="sm" class="ml-1">Đã đổi giờ</UBadge>
          </p>
          <p class="text-sm text-muted">
            {{ gioDep(msTuChuoi(l.mo)) }} → {{ gioDep(msTuChuoi(l.dong)) }}
            <template v-if="l.ghiDe === 2"> <span class="text-xs text-dimmed">(lịch: {{ gioDep(msTuChuoi(l.lichMo)) }} → {{ gioDep(msTuChuoi(l.lichDong)) }})</span></template>
            <template v-if="l.ngayLe"> · {{ l.ngayLe }}</template>
          </p>
          <p v-if="l.suaBoi" class="text-xs text-dimmed">Ghi đè bởi {{ l.suaBoi }} lúc {{ l.suaLuc }}</p>
          <div v-if="sua[khoa(l)]" class="mt-2 flex flex-wrap items-end gap-2">
            <UFormField label="Mở" size="sm"><UInput v-model="sua[khoa(l)]!.mo" type="datetime-local" size="sm" /></UFormField>
            <UFormField label="Đóng" size="sm"><UInput v-model="sua[khoa(l)]!.dong" type="datetime-local" size="sm" /></UFormField>
            <UButton size="sm" label="Lưu giờ năm nay" :loading="dangLam[khoa(l)]" @click="ghiDe(l, 'doi', { mo: sua[khoa(l)]!.mo, dong: sua[khoa(l)]!.dong })" />
            <UButton size="sm" color="neutral" variant="ghost" label="Huỷ" @click="delete sua[khoa(l)]" />
          </div>
        </div>
        <div v-if="props.duocGhi" class="flex shrink-0 flex-wrap gap-1">
          <UButton v-if="l.ghiDe !== 0" size="xs" color="neutral" variant="outline" icon="i-lucide-rotate-ccw" label="Theo lịch" :loading="dangLam[khoa(l)]" @click="ghiDe(l, 'theo_lich')" />
          <UButton v-if="l.ghiDe !== 1 && msTuChuoi(l.dong) > bay" size="xs" color="error" variant="soft" icon="i-lucide-calendar-x" label="Tắt năm nay" :loading="dangLam[khoa(l)]" @click="ghiDe(l, 'tat')" />
          <UButton v-if="msTuChuoi(l.mo) > bay && msTuChuoi(l.dong) > bay" size="xs" color="success" variant="soft" icon="i-lucide-play" label="Mở ngay" :loading="dangLam[khoa(l)]" @click="ghiDe(l, 'bat_ngay')" />
          <UButton v-if="msTuChuoi(l.dong) > bay" size="xs" color="neutral" variant="soft" icon="i-lucide-calendar-clock" label="Đổi giờ" @click="moSua(l)" />
          <UButton v-if="data?.coTuDong !== false" size="xs" color="neutral" variant="ghost" :icon="l.tuDong ? 'i-lucide-hand' : 'i-lucide-calendar-check'"
            :label="l.tuDong ? 'Chuyển thủ công' : 'Tự động theo lịch'" :loading="dangLam[`td:${l.ma}`]" @click="doiTuDong(l, !l.tuDong)" />
        </div>
      </div>
      <p v-if="!ds.length && status !== 'pending'" class="py-6 text-center text-sm text-muted">Không có lần diễn ra nào.</p>
    </div>
  </UCard>
</template>
