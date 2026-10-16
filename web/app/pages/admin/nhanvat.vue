<script setup lang="ts">
/**
 * Trang quản trị Nhân vật.
 *   /admin/nhanvat            : 30 nhân vật vào gần nhất, phân trang ?trang=
 *   /admin/nhanvat?q=<tên|số> : tìm theo tên, hoặc số = userId / id nhân vật (tối đa 50)
 *   /admin/nhanvat?id=<id>    : trang chi tiết (các trang khác liên kết theo kiểu này)
 * Tìm ra ĐÚNG MỘT nhân vật thì mở luôn trang chi tiết (motId) — thiếu bước này thì bấm tên
 * nhân vật ở trang Log (liên kết ?q=ten) chỉ ra một bảng đúng một dòng, không có phần vật
 * phẩm, và người xem tưởng trang không hề có. Bốn bảng Đang mặc / Thời trang / Hành trang /
 * Rương nằm trong AdminNhanvatChiTiet (app/components/admin/nhanvat/ChiTiet.vue).
 */
import type { TableColumn } from '@nuxt/ui'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface DongNV {
  id: number, charname: string, userId: number, lastLv: number, class: number, dex: number
  gold: number, luong: number, ban: number, del: number, lastLog: string
}
interface KetQuaDs { q: string, trang: number, conTrangSau: boolean, ds: DongNV[], motId: number | null }

const api = useAdminApi()
const route = useRoute()
const router = useRouter()
const toast = useToast()

const idXem = computed(() => {
  const n = Number(route.query.id)
  return Number.isInteger(n) && n > 0 ? n : 0
})
const tim = computed(() => (typeof route.query.q === 'string' ? route.query.q.trim() : ''))
const trang = computed(() => Math.max(1, Number(route.query.trang) || 1))
const oTim = ref(tim.value)
watch(tim, v => (oTim.value = v))

const { data, status, error, refresh } = useAsyncData(
  'admin-nhanvat-ds',
  // Đang xem chi tiết bằng ?id= thì không cần tải danh sách.
  () => (idXem.value
    ? Promise.resolve(null)
    : api<KetQuaDs>('/api/admin/nhanvat', { query: { q: tim.value || undefined, trang: trang.value } })),
  { server: false, watch: [tim, trang, idXem] },
)

// Tìm ra đúng một nhân vật -> mở thẳng trang chi tiết (giữ ?q để "Về danh sách" quay lại kết quả).
const idChiTiet = computed(() => idXem.value || (tim.value !== '' && data.value?.q === tim.value ? data.value?.motId ?? 0 : 0))

function timKiem() {
  router.push({ query: oTim.value.trim() ? { q: oTim.value.trim() } : {} })
}

function veDanhSach() {
  // Mở từ kết quả tìm đúng một người thì bỏ luôn q, nếu không sẽ lại tự mở chi tiết.
  const q = idXem.value && tim.value ? { q: tim.value } : {}
  router.push({ query: data.value?.motId && !idXem.value ? {} : q })
}

function sangTrang(t: number) {
  router.push({ query: t > 1 ? { trang: String(t) } : {} })
}

// ---- Khoá / mở khoá ngay trên bảng ----
const xn = reactive({ open: false, nv: null as DongNV | null })
const dangKhoa = ref(false)
function hoiKhoa(nv: DongNV) {
  xn.nv = nv
  xn.open = true
}
async function khoaMo() {
  const nv = xn.nv
  if (!nv) return
  dangKhoa.value = true
  const khoa = Number(nv.ban) !== 0
  try {
    const kq = await api<{ thongBao: string }>(`/api/admin/nhanvat/${nv.id}/khoa`, { method: 'POST', body: { viec: khoa ? 'mo_khoa' : 'khoa' } })
    toast.add({ title: 'Xong', description: kq.thongBao, color: 'success' })
  } catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error' })
  } finally {
    dangKhoa.value = false
    xn.open = false
  }
  await refresh()
}

function nhan(nv: DongNV) {
  if (Number(nv.ban) !== 0) return { label: 'Bị khoá', color: 'error' as const }
  if (Number(nv.del) !== 1) return { label: 'Đã xoá', color: 'warning' as const }
  return { label: 'Bình thường', color: 'success' as const }
}

const columns: TableColumn<DongNV>[] = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'lastLv', header: 'Cấp', meta: { class: { th: 'text-right', td: 'text-right' } } },
  { accessorKey: 'luong', header: 'Lượng', meta: { class: { th: 'text-right', td: 'text-right' } } },
  { accessorKey: 'gold', header: 'Xu', meta: { class: { th: 'text-right', td: 'text-right' } } },
  { accessorKey: 'lastLog', header: 'Vào lần cuối' },
  { id: 'trang_thai', header: 'Trạng thái' },
  { id: 'thao_tac', header: '' },
]
</script>

<template>
  <AdminPage title="Nhân vật">
    <template #right>
      <form class="flex items-center gap-2" @submit.prevent="timKiem">
        <UInput v-model="oTim" icon="i-lucide-search" placeholder="Tên nhân vật hoặc ID tài khoản" class="w-56 sm:w-72" />
        <UButton type="submit" label="Tìm" />
      </form>
    </template>

    <AdminNhanvatChiTiet v-if="idChiTiet" :id="idChiTiet" @ve-danh-sach="veDanhSach" />

    <template v-else>
      <p class="text-sm text-muted">
        Khoá / mở khoá từng nhân vật. Muốn chặn cả tài khoản thì làm ở trang
        <NuxtLink to="/admin/taikhoan" class="text-primary hover:underline">Tài khoản</NuxtLink>.
        Tìm theo tên nhân vật, hoặc gõ số để tra theo ID tài khoản.
      </p>

      <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${loiApi(error)}`" />

      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <div class="space-y-1">
            <h2 class="font-semibold text-highlighted">
              {{ tim === '' ? 'Danh sách nhân vật' : `Kết quả cho “${tim}”` }}
            </h2>
            <p class="text-sm text-muted">
              Bấm <strong class="text-highlighted">tên nhân vật</strong> để mở trang chi tiết — ở đó mới có
              <strong class="text-highlighted">đồ đang mặc, thời trang, hành trang và rương</strong> của nhân vật đó.
              Bảng này chỉ liệt kê, không kèm vật phẩm.
            </p>
          </div>
        </template>

        <UTable :data="data?.ds ?? []" :columns="columns" :loading="status === 'pending'" empty="Không có nhân vật nào khớp.">
          <template #charname-cell="{ row }">
            <NuxtLink :to="{ query: { ...(tim ? { q: tim } : {}), id: row.original.id } }" class="font-semibold text-primary hover:underline">
              {{ row.original.charname }}
            </NuxtLink>
            <div class="text-xs text-muted">
              {{ speciesName(row.original.class, row.original.lastLv, row.original.dex) }} · tài khoản #{{ row.original.userId }}
            </div>
          </template>
          <template #lastLv-cell="{ row }">
            <span class="tabular-nums">{{ row.original.lastLv }}</span>
          </template>
          <template #luong-cell="{ row }">
            <span class="tabular-nums">{{ so(row.original.luong) }}</span>
          </template>
          <template #gold-cell="{ row }">
            <span class="tabular-nums">{{ so(row.original.gold) }}</span>
          </template>
          <template #lastLog-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lastLog }}</span>
          </template>
          <template #trang_thai-cell="{ row }">
            <UBadge :color="nhan(row.original).color" variant="subtle" :label="nhan(row.original).label" />
          </template>
          <template #thao_tac-cell="{ row }">
            <UButton
              size="xs"
              :color="Number(row.original.ban) !== 0 ? 'primary' : 'error'"
              :variant="Number(row.original.ban) !== 0 ? 'solid' : 'soft'"
              :label="Number(row.original.ban) !== 0 ? 'Mở khoá' : 'Khoá'"
              @click="hoiKhoa(row.original)"
            />
          </template>
        </UTable>

        <template #footer>
          <div v-if="tim === ''" class="flex items-center justify-between gap-3 text-sm text-muted">
            <UButton v-if="trang > 1" color="neutral" variant="outline" size="sm" icon="i-lucide-chevron-left" label="Trang trước" @click="sangTrang(trang - 1)" />
            <span v-else />
            <span>Trang {{ trang }} · {{ data?.ds.length ?? 0 }} dòng</span>
            <UButton v-if="data?.conTrangSau" color="neutral" variant="outline" size="sm" trailing-icon="i-lucide-chevron-right" label="Trang sau" @click="sangTrang(trang + 1)" />
            <span v-else />
          </div>
          <p v-else-if="(data?.ds.length ?? 0) >= 50" class="text-sm text-muted">
            Chỉ hiện 50 kết quả đầu — gõ cụ thể hơn để thu hẹp.
          </p>
        </template>
      </UCard>
    </template>

    <AdminNhanvatXacNhan
      v-model:open="xn.open"
      :title="xn.nv ? `${Number(xn.nv.ban) !== 0 ? 'Mở khoá' : 'Khoá'} nhân vật ${xn.nv.charname}?` : ''"
      :nhan-nut="xn.nv && Number(xn.nv.ban) !== 0 ? 'Mở khoá' : 'Khoá'"
      :mau="xn.nv && Number(xn.nv.ban) !== 0 ? 'primary' : 'error'"
      :dang-chay="dangKhoa"
      @xac-nhan="khoaMo"
    />
  </AdminPage>
</template>
