<script setup lang="ts">
/**
 * Trung tâm người chơi.
 *   /admin/nguoi-choi?q=…&kieu=…        tìm theo tài khoản / nhân vật / email / IP / mã máy
 *   /admin/nguoi-choi?u=<tài khoản>     hồ sơ đầy đủ (tab), &nv=<id nhân vật> mở sẵn nhân vật
 * Các trang Tài khoản / Nhân vật / Cấm theo máy liên kết về đây bằng ?u=.
 */
import type { CotBang } from '~/components/admin/Bang.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface NvTim { id: number, charname: string, lastLv: number, ban: number, loai: string, dex: number, lastLog: string, online: boolean | null }
interface DongTim {
  id: number, username: string, email: string, regdate: string, ban: number, tuGame: boolean, laAdmin: boolean
  nhanVat: NvTim[], capCao: number, dangOnline: boolean, lanCuoi: string, khop: string[]
}

const api = useAdminApi()
const route = useRoute()
const router = useRouter()

const KIEU = [
  { label: 'Tất cả', value: 'tat_ca' },
  { label: 'Tên tài khoản', value: 'tai_khoan' },
  { label: 'Tên Pokémon', value: 'nhan_vat' },
  { label: 'Email', value: 'email' },
  { label: 'IP', value: 'ip' },
  { label: 'Mã máy', value: 'ma_may' },
]
const qUrl = computed(() => (typeof route.query.q === 'string' ? route.query.q.trim() : ''))
const kieuUrl = computed(() => (typeof route.query.kieu === 'string' && KIEU.some(k => k.value === route.query.kieu) ? route.query.kieu : 'tat_ca'))
const u = computed(() => (typeof route.query.u === 'string' ? route.query.u.trim() : ''))

const tim = ref(qUrl.value)
const kieu = ref(kieuUrl.value)
watch(qUrl, v => (tim.value = v))
watch(kieuUrl, v => (kieu.value = v))

function timKiem() {
  const q = tim.value.trim()
  router.push({ query: q ? { q, ...(kieu.value !== 'tat_ca' ? { kieu: kieu.value } : {}) } : {} })
}
watch(kieu, () => { if (tim.value.trim().length >= 2) timKiem() })

const { data, status, error, refresh } = useAsyncData(
  'admin-nc-tim',
  () => (u.value || qUrl.value.length < 2
    ? Promise.resolve(null)
    : api<{ ketQua: DongTim[], gioiHan: boolean }>('/api/admin/nguoi-choi/tim', { query: { q: qUrl.value, kieu: kieuUrl.value } })),
  { server: false, watch: [qUrl, kieuUrl, u] },
)

const cot: CotBang[] = [
  { key: 'username', label: 'Tài khoản', sort: true },
  { key: 'nhanVat', label: 'Pokémon', giaTri: r => r.nhanVat.map((n: NvTim) => n.charname).join(' ') },
  { key: 'capCao', label: 'Cấp cao nhất', sort: true, so: true, anMobile: true },
  { key: 'trangThai', label: 'Trạng thái', giaTri: r => `${r.dangOnline ? 'online ' : ''}${r.ban === 1 ? 'khoá' : ''}` },
  { key: 'lanCuoi', label: 'Vào lần cuối', sort: true, anMobile: true },
  { key: 'khop', label: 'Khớp theo', giaTri: r => r.khop.join(', '), anMobile: true },
]
const mo = (ten: string) => router.push({ query: { u: ten } })
</script>

<template>
  <AdminPage title="Trung tâm người chơi">
    <template v-if="u">
      <UButton icon="i-lucide-arrow-left" color="neutral" variant="link" label="Tìm người chơi khác" class="self-start px-0" @click="router.push({ query: {} })" />
      <AdminNguoichoiHoSo :key="u" :u="u" />
    </template>

    <template v-else>
      <form class="flex flex-col gap-2 sm:flex-row" role="search" @submit.prevent="timKiem">
        <USelect v-model="kieu" :items="KIEU" class="sm:w-44" aria-label="Tìm theo" />
        <UInput v-model="tim" icon="i-lucide-search" size="lg" class="flex-1" autofocus maxlength="64"
          placeholder="Tên tài khoản, tên Pokémon, email, IP hoặc mã máy (ít nhất 2 ký tự)" aria-label="Từ khoá" />
        <UButton type="submit" size="lg" label="Tìm" :disabled="tim.trim().length < 2" />
      </form>

      <UCard v-if="!qUrl" class="bg-elevated/40">
        <div class="flex items-start gap-3 text-sm text-muted">
          <UIcon name="i-lucide-info" class="mt-0.5 size-5 shrink-0 text-primary" />
          <div class="space-y-1">
            <p>Gõ tên tài khoản hoặc tên Pokémon để mở hồ sơ. Chọn <b>IP</b> hoặc <b>Mã máy</b> để xem mọi tài khoản đã đăng nhập từ đó (lấy theo đầu chuỗi).</p>
            <p>Số thuần được hiểu thêm là ID tài khoản / ID Pokémon.</p>
          </div>
        </div>
      </UCard>

      <AdminBang v-else :rows="data?.ketQua" :cot="cot" :dang-tai="status === 'pending'" :loi="error" ten-csv="tim-nguoi-choi"
        row-key="id" rong-tieu-de="Không tìm thấy người chơi nào" rong-mo-ta="Thử kiểu tìm khác, hoặc gõ ít ký tự hơn." @thu-lai="refresh">
        <template #cell-username="{ row }">
          <button type="button" class="text-left font-semibold text-primary hover:underline" @click="mo(row.username)">{{ row.username }}</button>
          <div class="flex flex-wrap gap-1 text-xs text-muted">
            <span>#{{ row.id }}</span>
            <span v-if="row.email" class="break-all">· {{ row.email }}</span>
            <UBadge v-if="row.laAdmin" size="sm" color="neutral" variant="outline" label="quản trị" />
          </div>
        </template>
        <template #cell-nhanVat="{ row }">
          <div class="flex flex-col gap-1">
            <span v-for="n in row.nhanVat" :key="n.id" class="flex items-center gap-2">
              <PokeAvatar :dex="n.dex" class="size-6" />
              <span class="font-medium">{{ n.charname }}</span>
              <span class="text-xs text-muted">Lv {{ n.lastLv }} · {{ n.loai }}</span>
              <span v-if="n.online === true" class="size-2 rounded-full bg-success" title="Đang online" />
            </span>
            <span v-if="!row.nhanVat.length" class="text-xs text-dimmed">Chưa có Pokémon</span>
          </div>
        </template>
        <template #cell-trangThai="{ row }">
          <div class="flex flex-wrap gap-1">
            <UBadge v-if="row.dangOnline" color="success" variant="subtle" label="Online" />
            <UBadge v-if="row.ban === 1" color="error" variant="subtle" label="Bị khoá" />
            <UBadge v-else-if="row.ban === 3" color="warning" variant="subtle" label="Chờ email" />
            <UBadge v-else-if="!row.dangOnline" color="neutral" variant="subtle" label="Bình thường" />
          </div>
        </template>
        <template #cell-lanCuoi="{ row }">
          <span class="whitespace-nowrap text-xs text-muted">{{ row.lanCuoi || '—' }}</span>
        </template>
        <template #cell-khop="{ row }">
          <span class="text-xs text-muted">{{ row.khop.join(' · ') }}</span>
        </template>
      </AdminBang>
      <p v-if="data?.gioiHan" class="text-sm text-muted">Chỉ hiện 50 tài khoản đầu — gõ cụ thể hơn để thu hẹp.</p>
    </template>
  </AdminPage>
</template>
