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
import type { CotBang } from '~/components/admin/Bang.vue'

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
const me = useAdminMe()

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

const duocKhoa = computed(() => me.duoc('/api/admin/nhanvat/1/khoa'))
const columns = computed<CotBang[]>(() => [
  { key: 'charname', label: 'Pokémon', sort: true },
  { key: 'lastLv', label: 'Cấp', sort: true, so: true },
  { key: 'luong', label: 'Lượng', sort: true, so: true, anMobile: true },
  { key: 'gold', label: 'Xu', sort: true, so: true, anMobile: true },
  { key: 'lastLog', label: 'Vào lần cuối', sort: true, anMobile: true },
  { key: 'trang_thai', label: 'Trạng thái', giaTri: (r: DongNV) => nhan(r).label },
  { key: 'thao_tac', label: '' },
])
</script>

<template>
  <AdminPage title="Pokémon người chơi">
    <template #right>
      <form class="flex items-center gap-2" @submit.prevent="timKiem">
        <UInput v-model="oTim" icon="i-lucide-search" placeholder="Tên Pokémon hoặc ID tài khoản" maxlength="64" class="w-40 sm:w-72" />
        <UButton type="submit" label="Tìm" />
      </form>
    </template>

    <AdminNhanvatChiTiet v-if="idChiTiet" :id="idChiTiet" @ve-danh-sach="veDanhSach" />

    <template v-else>
      <p class="text-sm text-muted">
        Khoá / mở khoá từng Pokémon. Muốn chặn cả tài khoản thì làm ở trang
        <NuxtLink to="/admin/taikhoan" class="text-primary hover:underline">Tài khoản</NuxtLink>.
        Tìm theo tên Pokémon, hoặc gõ số để tra theo ID tài khoản.
      </p>

      <UCard :ui="{ body: 'p-3 sm:p-4' }">
        <template #header>
          <div class="space-y-1">
            <h2 class="font-semibold text-highlighted">
              {{ tim === '' ? 'Danh sách Pokémon' : `Kết quả cho “${tim}”` }}
            </h2>
            <p class="text-sm text-muted">
              Bấm <strong class="text-highlighted">tên Pokémon</strong> để mở trang quản lý (cấp độ, đồ đang mặc, hành trang, rương, lệnh).
              <strong class="text-highlighted">Hồ sơ</strong> mở Trung tâm người chơi (tài khoản, IP, Pokédex, nhiệm vụ, lịch sử).
            </p>
          </div>
        </template>

        <AdminBang :rows="data?.ds" :cot="columns" :dang-tai="status === 'pending'" :loi="error" ten-csv="nhan-vat" row-key="id"
          :moi-trang="50" rong-tieu-de="Không có Pokémon nào khớp" @thu-lai="refresh">
          <template #cell-charname="{ row }">
            <span class="flex items-center gap-2">
              <PokeAvatar :dex="dexHienTai(row.class, row.lastLv, row.dex)" class="size-7" />
              <span>
                <NuxtLink :to="{ query: { ...(tim ? { q: tim } : {}), id: row.id } }" class="font-semibold text-primary hover:underline">
                  {{ row.charname }}
                </NuxtLink>
                <span class="block text-xs text-muted">
                  {{ speciesName(row.class, row.lastLv, row.dex) }} · tài khoản #{{ row.userId }}
                </span>
              </span>
            </span>
          </template>
          <template #cell-lastLog="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.lastLog }}</span>
          </template>
          <template #cell-trang_thai="{ row }">
            <UBadge :color="nhan(row).color" variant="subtle" :label="nhan(row).label" />
          </template>
          <template #cell-thao_tac="{ row }">
            <div class="flex justify-end gap-1">
              <UButton size="xs" color="primary" variant="soft" icon="i-lucide-contact" label="Hồ sơ" :to="{ path: '/admin/nguoi-choi', query: { u: `#${row.userId}`, nv: String(row.id) } }" />
              <UButton
                v-if="duocKhoa"
                size="xs"
                :color="Number(row.ban) !== 0 ? 'primary' : 'error'"
                :variant="Number(row.ban) !== 0 ? 'solid' : 'soft'"
                :label="Number(row.ban) !== 0 ? 'Mở khoá' : 'Khoá'"
                @click="hoiKhoa(row)"
              />
            </div>
          </template>
        </AdminBang>

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
      :title="xn.nv ? `${Number(xn.nv.ban) !== 0 ? 'Mở khoá' : 'Khoá'} Pokémon ${xn.nv.charname}?` : ''"
      :nhan-nut="xn.nv && Number(xn.nv.ban) !== 0 ? 'Mở khoá' : 'Khoá'"
      :mau="xn.nv && Number(xn.nv.ban) !== 0 ? 'primary' : 'error'"
      :dang-chay="dangKhoa"
      @xac-nhan="khoaMo"
    />
  </AdminPage>
</template>
