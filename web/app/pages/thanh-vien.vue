<script setup lang="ts">
/**
 * Thành viên Huyền Thoại Bảo Bối (HTBB/docs/pokemon/THANH_VIEN.md). Danh sách quyền lợi sinh từ cấu hình
 * admin (/api/thanh-vien/thong-tin). Mua: đơn THANH_VIEN qua luồng nạp chung (VietQR + SePay), cấp ngay khi
 * tiền vào, theo TÀI KHOẢN (mọi nhân vật). Không tăng sức mạnh, không cho tiền tệ trong game.
 */
useSeoMeta({
  title: 'Thành viên HTBB',
  description: 'Thành viên Huyền Thoại Bảo Bối: mở giao dịch, chat thế giới, chợ, đội... cho mọi Pokémon của tài khoản. Không bán sức mạnh.',
})

interface ThongTin {
  batBuoc: boolean
  dangBan: boolean
  gia: number
  thoiHanNgay: number
  mienCap: number
  chucNang: { ma: string, ten: string, cho: string, khoa: boolean }[]
}
interface DonTv { ma_don: string, so_tien: number, trang_thai: string, tao_luc: string, cong_luc: string | null }
interface CuaToi { coBang: boolean, trangThai: { laThanhVien: boolean, hetHanMs: number | null, capLuc: string | null } | null, dons: DonTv[] }

const api = useApi()
const auth = useAuth()
const toast = useToast()
const route = useRoute()
const router = useRouter()

const { data: tt } = await useFetch<ThongTin>('/api/thanh-vien/thong-tin', { key: 'tv-thong-tin' })
const cuaToi = ref<CuaToi | null>(null)
const maDon = computed(() => (typeof route.query.ma_don === 'string' ? route.query.ma_don : ''))
const don = ref<DonTv | null>(null)
const qr = ref('')
const nganHang = ref<{ soTaiKhoan: string, chuTaiKhoan: string } | null>(null)
const moApp = ref<{ ten: string, url: string }[]>([])
const dangMua = ref(false)

const khoa = computed(() => tt.value?.chucNang.filter(c => c.khoa) ?? [])
const hanText = computed(() => (tt.value?.thoiHanNgay ? `${tt.value.thoiHanNgay} ngày` : 'vĩnh viễn'))

function hanCuaToi(ms: number | null | undefined): string {
  if (ms === null || ms === undefined) return ''
  if (ms === 0) return 'vĩnh viễn'
  if (ms < 0) return 'đã thu hồi'
  return new Date(ms).toLocaleString('vi-VN')
}

async function taiCuaToi() {
  auth.hydrate()
  if (!auth.loggedIn.value) return
  try {
    cuaToi.value = await api<CuaToi>('/api/thanh-vien/cua-toi')
  } catch { /* bỏ qua */ }
}

let hen: ReturnType<typeof setTimeout> | null = null
async function taiDon(ma: string) {
  if (hen) clearTimeout(hen)
  if (!ma) {
    don.value = null
    return
  }
  try {
    const r = await api<{ don: DonTv, qr: string, nganHang: { soTaiKhoan: string, chuTaiKhoan: string }, moApp: { ten: string, url: string }[] }>('/api/thanh-vien/don', { query: { ma_don: ma } })
    const cu = don.value?.trang_thai
    don.value = r.don
    qr.value = r.qr
    nganHang.value = r.nganHang
    moApp.value = r.moApp
    if (r.don.trang_thai === 'da_cong') {
      if (cu && cu !== 'da_cong') toast.add({ title: 'Đã là Thành viên HTBB!', color: 'success', icon: 'i-lucide-badge-check' })
      taiCuaToi()
      return
    }
    if (['cho_thanh_toan', 'that_bai', 'da_thanh_toan'].includes(r.don.trang_thai)) hen = setTimeout(() => taiDon(ma), 4000)
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error' })
  }
}

function khoaIdem(): string {
  const a = new Uint8Array(16)
  crypto.getRandomValues(a)
  return Array.from(a, b => b.toString(16).padStart(2, '0')).join('')
}

async function mua() {
  auth.hydrate()
  if (!auth.loggedIn.value) return navigateTo('/login?next=/thanh-vien')
  dangMua.value = true
  try {
    const r = await api<{ ma_don: string }>('/api/thanh-vien/mua', { method: 'POST', body: { khoa_idem: khoaIdem() } })
    await router.push({ query: { ma_don: r.ma_don } })
  } catch (e) {
    toast.add({ title: 'Chưa tạo được đơn', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangMua.value = false
  }
}

watch(maDon, ma => taiDon(ma))
onMounted(() => {
  taiCuaToi()
  if (maDon.value) taiDon(maDon.value)
})
onBeforeUnmount(() => { if (hen) clearTimeout(hen) })

const faq = computed(() => [
  { q: 'Thành viên có làm Pokémon mạnh hơn không?', a: 'Không. Thành viên không cho chỉ số, EXP, lượng hay xu. Chỉ mở các tính năng cộng đồng để hạn chế tài khoản phụ / clone phá kinh tế.' },
  { q: 'Áp dụng cho Pokémon nào?', a: 'Cho TÀI KHOẢN — mọi Pokémon của tài khoản đều dùng được.' },
  { q: 'Khi nào bắt buộc?', a: tt.value?.batBuoc ? 'Đang bắt buộc với các tính năng bên dưới.' : 'Trong giai đoạn thử nghiệm KHÔNG bắt buộc. Khi máy chủ mở chính thức, ban quản trị sẽ thông báo trước.' },
  { q: 'Thanh toán thế nào?', a: 'Chuyển khoản VietQR đúng số tiền và nội dung (mã đơn). Tiền vào là kích hoạt ngay, không cần vào game.' },
  { q: 'Hoàn tiền?', a: 'Theo Chính sách hoàn tiền. Khi hoàn, quyền Thành viên của lần mua đó bị thu hồi.' },
  ...(tt.value?.mienCap ? [{ q: 'Người mới?', a: `Pokémon dưới cấp ${tt.value.mienCap} được dùng thử miễn phí mọi tính năng.` }] : []),
])
</script>

<template>
  <div>
    <SitePageBanner
      title="Thành viên Huyền Thoại Bảo Bối"
      eyebrow="Cộng đồng sạch"
      :intro="`Mở giao dịch, chat thế giới, chợ, đội... cho mọi Pokémon của tài khoản. ${tt ? vnd(tt.gia) : ''} · ${hanText}. Không bán sức mạnh.`"
    />
    <UContainer class="max-w-5xl py-12">
      <UAlert v-if="tt && !tt.batBuoc" class="mb-6" color="info" variant="subtle" icon="i-lucide-flask-conical" title="Đang thử nghiệm: Thành viên CHƯA bắt buộc" description="Mọi tính năng đang mở cho tất cả. Khi mở chính thức, các tính năng bên dưới sẽ cần Thành viên." />

      <div class="grid gap-6 md:grid-cols-[1fr_22rem]">
        <UCard>
          <template #header>
            <b>Thành viên mở các tính năng</b>
          </template>
          <ul class="space-y-2">
            <li v-for="c in khoa" :key="c.ma" class="flex gap-2">
              <UIcon name="i-lucide-check" class="mt-1 size-4 shrink-0 text-success" />
              <span><b>{{ c.ten }}</b> <span class="text-sm text-muted">— {{ c.cho }}</span></span>
            </li>
            <li v-if="!khoa.length" class="text-muted">
              Chưa có tính năng nào cần Thành viên.
            </li>
          </ul>
          <p class="mt-4 text-sm text-muted">
            Áp dụng cho mọi Pokémon của tài khoản.{{ tt?.mienCap ? ` Pokémon dưới cấp ${tt.mienCap} dùng thử miễn phí.` : '' }}
            Không cho chỉ số, EXP, lượng hay xu.
          </p>
        </UCard>

        <UCard>
          <div class="text-center">
            <div class="font-display text-4xl font-extrabold text-gilded">
              {{ tt ? vnd(tt.gia) : '…' }}
            </div>
            <div class="text-sm text-muted">
              {{ hanText }}
            </div>
            <div v-if="cuaToi?.trangThai" class="mt-4">
              <UBadge v-if="cuaToi.trangThai.laThanhVien" color="success" variant="subtle" size="lg" icon="i-lucide-badge-check" :label="`Thành viên · hạn ${hanCuaToi(cuaToi.trangThai.hetHanMs)}`" />
              <UBadge v-else color="neutral" variant="subtle" size="lg" label="Chưa là Thành viên" />
            </div>
            <UButton class="mt-5" block size="lg" :loading="dangMua" :disabled="!tt?.dangBan || (cuaToi?.trangThai?.laThanhVien && cuaToi.trangThai.hetHanMs === 0)" icon="i-lucide-qr-code" @click="mua">
              {{ cuaToi?.trangThai?.laThanhVien ? 'Gia hạn' : 'Mua Thành viên' }}
            </UButton>
            <p v-if="tt && !tt.dangBan" class="mt-2 text-xs text-muted">
              Hiện chưa mở bán.
            </p>
          </div>
        </UCard>
      </div>

      <!-- Đơn đang thanh toán -->
      <UCard v-if="don" class="mx-auto mt-8 max-w-xl">
        <template #header>
          <b>Đơn {{ don.ma_don }} · {{ vnd(don.so_tien) }}</b>
        </template>
        <div v-if="['cho_thanh_toan', 'that_bai'].includes(don.trang_thai)" class="text-center">
          <img :src="qr" alt="Mã QR chuyển khoản VietQR" class="mx-auto size-64 rounded-xl bg-white p-2" width="256" height="256">
          <p class="mt-3 text-sm">
            Nội dung CK: <strong class="font-mono text-primary">{{ don.ma_don }}</strong>
            <span v-if="nganHang"> · TK {{ nganHang.soTaiKhoan }} ({{ nganHang.chuTaiKhoan }})</span>
          </p>
          <div v-if="moApp.length" class="mt-3 flex flex-wrap justify-center gap-2">
            <UButton v-for="a in moApp" :key="a.ten" :to="a.url" external target="_blank" size="xs" color="neutral" variant="outline" :label="a.ten" />
          </div>
          <p class="mt-3 text-sm text-muted" role="status">
            Đang chờ thanh toán… trang tự cập nhật.
          </p>
        </div>
        <div v-else class="text-center" role="status">
          <UIcon :name="don.trang_thai === 'da_cong' ? 'i-lucide-badge-check' : 'i-lucide-info'" class="size-12 text-success" />
          <p class="mt-2 font-display text-xl font-bold">
            {{ don.trang_thai === 'da_cong' ? 'Đã kích hoạt Thành viên!' : (TEN_TRANG_THAI_NAP[don.trang_thai] ?? don.trang_thai) }}
          </p>
          <p v-if="don.trang_thai === 'da_cong'" class="text-sm text-muted">
            Áp dụng cho mọi Pokémon trong ≤ 30 giây, không cần đăng nhập lại.
          </p>
        </div>
      </UCard>

      <UCard class="mt-8">
        <template #header>
          <b>Câu hỏi thường gặp</b>
        </template>
        <dl class="space-y-3">
          <div v-for="f in faq" :key="f.q">
            <dt class="font-semibold">
              {{ f.q }}
            </dt>
            <dd class="text-sm text-muted">
              {{ f.a }}
            </dd>
          </div>
        </dl>
        <p class="mt-4 text-sm text-muted">
          <NuxtLink to="/dieu-khoan-thanh-toan" class="lien-ket">Điều khoản thanh toán</NuxtLink> ·
          <NuxtLink to="/chinh-sach-hoan-tien" class="lien-ket">Chính sách hoàn tiền</NuxtLink>
        </p>
      </UCard>
    </UContainer>
  </div>
</template>
