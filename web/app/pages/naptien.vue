<script setup lang="ts">
/**
 * Nạp tiền (docs/van-hanh.md): chọn nhân vật nhận + gói (lượng / Thẻ tháng / Vé mùa)
 * -> tạo đơn -> QR VietQR. Trang tự hỏi lại trạng thái; cổng thanh toán báo tiền vào (hoặc admin
 * xác nhận) thì server game tự cộng vào nhân vật trong ~15 giây khi nhân vật online.
 * Không bán sức mạnh: lượng chỉ mua trang trí + tiện ích + tăng tốc (chỉ EXP, chung trần với bản khoá) trong game.
 */
definePageMeta({ middleware: ['auth', 'nap-bat'] })
useSeoMeta({
  title: 'Nạp tiền',
  robots: 'noindex',
  description: 'Nạp lượng, Thẻ tháng, Vé mùa bằng chuyển khoản VietQR. Tỷ giá cố định, chỉ bán trang trí, tiện ích và vật phẩm tăng tốc (chung giới hạn với bản kiếm được khi chơi), có trần nạp bảo vệ người chơi.',
})

interface Goi { ma: string, ten: string, loai: string, gia_vnd: number, luong: number, mo_ta: string }
interface NhanVat { id: number, charname: string, lastLv: number }
interface Tran { tuan: number, thang: number, tuDat: number, cho: number, choLuc: string | null, daTuan: number, daThang: number }
interface DanhMuc { goi: Goi[], nhanVat: NhanVat[], tran: Tran, khoaNap: boolean, noLuong: number, mua: { so: number, conNgay: number } | null, tyGia: number }
interface DonHienThi {
  ma_don: string
  ma_goi: string
  loai: string
  ten_goi: string
  so_tien: number
  luong: number
  charname: string
  trang_thai: string
  tao_luc: string
  thanh_toan_luc: string | null
  cong_luc: string | null
}

const api = useApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()

const maDon = computed(() => (typeof route.query.ma_don === 'string' ? route.query.ma_don : ''))
const dm = ref<DanhMuc | null>(null)
const charId = ref<number | undefined>(undefined)
const don = ref<DonHienThi | null>(null)
const qr = ref('')
const nganHang = ref<{ bin: string, soTaiKhoan: string, chuTaiKhoan: string } | null>(null)
const moApp = ref<{ ten: string, url: string }[]>([])
const hetHanMs = ref<number | null>(null)
const lechMs = ref(0)
const bayGio = ref(Date.now())
let dongHo: ReturnType<typeof setInterval> | null = null
const conLai = computed(() => {
  if (!hetHanMs.value) return ''
  const ms = hetHanMs.value - (bayGio.value + lechMs.value)
  if (ms <= 0) return 'đã quá hạn (tiền đến muộn vẫn được nhận)'
  const h = Math.floor(ms / 3600_000)
  const m = Math.floor((ms % 3600_000) / 60_000)
  const sec = Math.floor((ms % 60_000) / 1000)
  return `${h}:${String(m).padStart(2, '0')}:${String(sec).padStart(2, '0')}`
})
const lichSu = ref<DonHienThi[]>([])
const loi = ref('')
const dangTai = ref(false)
const dangTao = ref<string | null>(null)
const tranMoi = ref<number | undefined>(undefined)
const dangDatTran = ref(false)

const goiLuong = computed(() => dm.value?.goi.filter(g => g.loai === 'luong') ?? [])
const goiKhac = computed(() => dm.value?.goi.filter(g => g.loai !== 'luong') ?? [])
const chuaThanhToan = computed(() => !!don.value && ['cho_thanh_toan', 'that_bai'].includes(don.value.trang_thai))
const dangChoVaoGame = computed(() => don.value?.trang_thai === 'da_thanh_toan')
const nhanVatItems = computed(() => (dm.value?.nhanVat ?? []).map(n => ({ label: `${n.charname} (cấp ${n.lastLv})`, value: n.id })))

async function taiDanhMuc() {
  try {
    dm.value = await api<DanhMuc>('/api/nap/goi')
    if (charId.value === undefined && dm.value.nhanVat.length) charId.value = dm.value.nhanVat[0]!.id
  } catch (e) {
    loi.value = loiApi(e)
  }
}

async function taiLichSu() {
  try {
    lichSu.value = (await api<{ dons: DonHienThi[] }>('/api/nap/lich-su')).dons
  } catch { /* không chặn trang */ }
}

// --- Hỏi lại trạng thái: 4 giây một lần (tới khi đã cộng), lỗi mạng giãn ra 8 giây ---
let hen: ReturnType<typeof setTimeout> | null = null
function dungHoi() {
  if (hen) clearTimeout(hen)
  hen = null
}
function henHoi(ms: number) {
  dungHoi()
  hen = setTimeout(hoiTrangThai, ms)
}
async function hoiTrangThai() {
  const ma = don.value?.ma_don
  if (!ma || don.value?.trang_thai === 'da_cong') return
  try {
    const r = await api<{ trang_thai: string, cong_luc: string | null }>('/api/nap/trang-thai', { query: { ma_don: ma } })
    if (don.value?.ma_don !== ma) return
    const cu = don.value.trang_thai
    don.value.trang_thai = r.trang_thai
    don.value.cong_luc = r.cong_luc
    if (cu !== r.trang_thai && r.trang_thai === 'da_thanh_toan') {
      toast.add({ title: 'Đã nhận được thanh toán!', description: `Đang chuyển vào Pokémon ${don.value.charname} (cần đang online trong game).`, color: 'success', icon: 'i-lucide-party-popper' })
    }
    if (cu !== r.trang_thai && r.trang_thai === 'da_cong') {
      toast.add({ title: `Quà đã vào ${don.value.charname}!`, color: 'success', icon: 'i-lucide-check' })
      taiLichSu()
      return
    }
    if (['cho_thanh_toan', 'that_bai', 'da_thanh_toan'].includes(r.trang_thai)) henHoi(4000)
  } catch (e: any) {
    if (e?.statusCode === 401) {
      loi.value = loiApi(e)
      return
    }
    henHoi(8000)
  }
}

async function taiDon(ma: string) {
  dungHoi()
  don.value = null
  if (!ma) return
  dangTai.value = true
  try {
    const r = await api<{ don: DonHienThi, qr: string, nganHang: { bin: string, soTaiKhoan: string, chuTaiKhoan: string }, hetHanMs: number | null, bayGioMs: number, moApp: { ten: string, url: string }[] }>('/api/nap/don', { query: { ma_don: ma } })
    don.value = r.don
    qr.value = r.qr
    nganHang.value = r.nganHang
    moApp.value = r.moApp ?? []
    hetHanMs.value = r.hetHanMs
    lechMs.value = r.bayGioMs - Date.now()
    henHoi(4000)
  } catch (e) {
    loi.value = loiApi(e)
  } finally {
    dangTai.value = false
  }
}

function khoaIdem(): string {
  const a = new Uint8Array(16)
  crypto.getRandomValues(a)
  return Array.from(a, b => b.toString(16).padStart(2, '0')).join('')
}

async function taoDon(g: Goi) {
  if (!charId.value) {
    toast.add({ title: 'Hãy chọn Pokémon nhận trước', color: 'warning', icon: 'i-lucide-user' })
    return
  }
  dangTao.value = g.ma
  try {
    const r = await api<{ ma_don: string }>('/api/nap/tao-don', { method: 'POST', body: { ma_goi: g.ma, char_id: charId.value, khoa_idem: khoaIdem() } })
    await router.push({ query: { ma_don: r.ma_don } })
  } catch (e) {
    toast.add({ title: 'Chưa tạo được đơn nạp', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangTao.value = null
  }
}

async function datTran() {
  if (tranMoi.value === undefined) return
  dangDatTran.value = true
  try {
    const r = await api<{ ngay: boolean, hieuLucLuc: string | null }>('/api/nap/tran', { method: 'POST', body: { tran: tranMoi.value } })
    toast.add({
      title: r.ngay ? 'Đã hạ trần nạp tháng' : `Trần mới có hiệu lực lúc ${r.hieuLucLuc}`,
      description: r.ngay ? undefined : 'Nâng / bỏ trần cần chờ 72 giờ để bạn kịp suy nghĩ lại.',
      color: 'success',
      icon: 'i-lucide-shield-check',
    })
    await taiDanhMuc()
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangDatTran.value = false
  }
}

async function chep(s: string) {
  try {
    await navigator.clipboard.writeText(s)
    toast.add({ title: `Đã chép ${s}`, color: 'success', icon: 'i-lucide-copy-check' })
  } catch { /* trình duyệt chặn clipboard */ }
}

const tranThang = computed(() => {
  const t = dm.value?.tran
  if (!t) return 0
  return t.tuDat > 0 ? Math.min(t.thang, t.tuDat) : t.thang
})

watch(maDon, ma => taiDon(ma), { immediate: true })
onMounted(() => {
  taiDanhMuc()
  taiLichSu()
  dongHo = setInterval(() => { bayGio.value = Date.now() }, 1000)
})
onBeforeUnmount(() => {
  dungHoi()
  if (dongHo) clearInterval(dongHo)
})

const cotLichSu = [
  { accessorKey: 'ma_don', header: 'Mã đơn' },
  { accessorKey: 'ten_goi', header: 'Gói' },
  { accessorKey: 'charname', header: 'Pokémon nhận' },
  { accessorKey: 'so_tien', header: 'Số tiền', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'trang_thai', header: 'Trạng thái' },
  { accessorKey: 'tao_luc', header: 'Tạo lúc' },
]
</script>

<template>
  <div>
    <SitePageBanner
      title="Nạp tiền"
      eyebrow="Ủng hộ máy chủ"
      intro="Tỷ giá cố định 10 lượng / 1.000 đ. Lượng chỉ mua được trang trí, tiện ích và vật phẩm tăng tốc luyện cấp (chỉ EXP, chung giới hạn 60 điểm/ngày, 210/tuần với bản khoá kiếm được khi chơi) — không bán sức mạnh, không quay may rủi."
    />

    <UContainer class="max-w-6xl py-14">
      <UAlert v-if="loi" class="mb-8" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="loi" role="alert" />

      <!-- ============ ĐANG XEM 1 ĐƠN ============ -->
      <div v-if="maDon && (don || dangTai)" class="mx-auto max-w-xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-6 sm:p-10">
            <div v-if="dangTai" class="flex items-center justify-center gap-3 py-24 text-muted">
              <UIcon name="i-lucide-loader-circle" class="size-6 animate-spin" /> Đang tải đơn nạp...
            </div>

            <template v-else-if="don">
              <p class="eyebrow">
                Đơn {{ don.ma_don }} · nhận: {{ don.charname }}
              </p>
              <h2 class="section-title mt-3 text-3xl text-highlighted">
                {{ don.ten_goi }} · <span class="text-gilded">{{ vnd(don.so_tien) }}</span>
              </h2>

              <!-- Chờ thanh toán -->
              <div v-if="chuaThanhToan" class="mt-6">
                <div class="mx-auto w-fit rounded-2xl bg-white p-3 shadow-[0_20px_60px_rgba(10,26,51,.14)]">
                  <img :src="qr" alt="Mã QR chuyển khoản VietQR" class="block size-64 sm:size-72" width="288" height="288">
                </div>
                <p class="mt-4 text-center text-sm text-muted">
                  Quét bằng app ngân hàng bất kỳ hỗ trợ VietQR — số tiền và nội dung đã điền sẵn.
                </p>
                <p v-if="conLai" class="mt-1 text-center text-sm" role="timer">
                  Thời hạn thanh toán: <strong class="tabular-nums text-highlighted">{{ conLai }}</strong>
                </p>
                <div v-if="moApp.length" class="mt-4">
                  <p class="text-center text-xs text-muted">
                    Đang dùng điện thoại? Mở thẳng app ngân hàng (đã điền sẵn):
                  </p>
                  <div class="mt-2 flex flex-wrap justify-center gap-2">
                    <UButton v-for="a in moApp" :key="a.ten" :to="a.url" external target="_blank" size="xs" color="neutral" variant="outline" :label="a.ten" />
                  </div>
                </div>
                <dl class="mt-6 divide-y divide-default/60 overflow-hidden rounded-xl border border-default/60 text-sm">
                  <div class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Số tiền
                    </dt>
                    <dd class="font-bold text-highlighted tabular-nums">
                      {{ vnd(don.so_tien) }}
                    </dd>
                  </div>
                  <div class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Nội dung CK
                    </dt>
                    <dd class="flex items-center gap-2">
                      <strong class="font-mono text-base tracking-wider text-primary">{{ don.ma_don }}</strong>
                      <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-copy" aria-label="Chép nội dung chuyển khoản" @click="chep(don.ma_don)" />
                    </dd>
                  </div>
                  <div v-if="nganHang" class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Tài khoản
                    </dt>
                    <dd class="text-right text-highlighted">
                      {{ nganHang.soTaiKhoan }} · {{ nganHang.chuTaiKhoan }}
                    </dd>
                  </div>
                </dl>
                <UAlert
                  class="mt-6"
                  color="warning"
                  variant="subtle"
                  icon="i-lucide-triangle-alert"
                  :description="`Chuyển ĐÚNG số tiền và GIỮ NGUYÊN nội dung ${don.ma_don}. Chuyển thiếu sẽ không được xác nhận tự động. Đơn hết hạn sau 24 giờ (tiền đến muộn vẫn được nhận).`"
                />
                <p class="mt-4 flex items-center justify-center gap-2 text-sm text-muted" role="status" aria-live="polite">
                  <span class="relative flex size-2.5">
                    <span class="absolute inline-flex size-full animate-ping rounded-full bg-primary/70" />
                    <span class="relative inline-flex size-2.5 rounded-full bg-primary" />
                  </span>
                  Đang chờ thanh toán... trang sẽ tự cập nhật.
                </p>
              </div>

              <!-- Đã thanh toán / đã cộng / khác -->
              <div v-else class="mt-8 text-center" role="status" aria-live="polite">
                <div class="mx-auto grid size-20 place-items-center rounded-full border border-success/40 bg-success/10 text-success">
                  <UIcon :name="dangChoVaoGame ? 'i-lucide-hourglass' : 'i-lucide-check'" class="size-10" />
                </div>
                <h3 class="mt-6 font-display text-2xl font-bold text-highlighted">
                  {{ TEN_TRANG_THAI_NAP[don.trang_thai] ?? don.trang_thai }}
                </h3>
                <p v-if="dangChoVaoGame" class="mt-3 text-muted">
                  Vào game bằng Pokémon <strong class="text-highlighted">{{ don.charname }}</strong> — quà tự vào trong khoảng 15 giây.
                  Thẻ tháng / Vé mùa: nhận thưởng ở <strong class="text-highlighted">NPC Tổng Quản › Cửa hàng &amp; Nạp</strong>.
                </p>
                <p v-else-if="don.trang_thai === 'da_cong'" class="mt-3 text-muted">
                  Đã cộng vào {{ don.charname }} lúc {{ don.cong_luc }}.
                </p>
              </div>

              <UButton to="/naptien" class="mt-8" color="neutral" variant="outline" icon="i-lucide-arrow-left">
                Chọn gói khác
              </UButton>
            </template>
          </div>
        </FxReveal>
      </div>

      <!-- ============ DANH MỤC ============ -->
      <template v-else>
        <div v-if="maDon" class="mb-8 text-center">
          <UButton to="/naptien" color="neutral" variant="outline" icon="i-lucide-arrow-left">
            Chọn gói khác
          </UButton>
        </div>

        <UAlert v-if="dm?.khoaNap" class="mb-6" color="error" variant="subtle" icon="i-lucide-lock" title="Tài khoản tạm khoá nạp" description="Có giao dịch bị hoàn/khiếu nại. Liên hệ ban quản trị." />
        <UAlert v-if="dm && dm.noLuong > 0" class="mb-6" color="warning" variant="subtle" icon="i-lucide-undo-2" :title="`Còn nợ thu hồi ${dm.noLuong} lượng`" description="Do một giao dịch đã hoàn tiền. Lượng nạp mới sẽ trừ nợ trước." />

        <div class="mb-8 grid gap-4 md:grid-cols-[1fr_auto] md:items-end">
          <UFormField label="Pokémon nhận" :hint="dm && !dm.nhanVat.length ? 'Tài khoản chưa có Pokémon nào' : undefined">
            <USelect v-model="charId" :items="nhanVatItems" placeholder="Chọn Pokémon" class="w-full md:w-80" />
          </UFormField>
          <p v-if="dm" class="text-sm text-muted">
            Đã nạp tuần này {{ vnd(dm.tran.daTuan) }} / {{ vnd(Math.min(dm.tran.tuan, tranThang)) }} ·
            tháng này {{ vnd(dm.tran.daThang) }} / {{ vnd(tranThang) }}
          </p>
        </div>

        <h2 class="section-title text-2xl text-highlighted">
          Gói lượng
        </h2>
        <div class="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
          <div v-for="g in goiLuong" :key="g.ma" class="flex flex-col rounded-2xl border border-default/70 bg-elevated/40 p-5">
            <div class="font-display text-3xl font-extrabold text-gilded">
              {{ so(g.luong) }}
            </div>
            <div class="text-sm text-muted">
              lượng
            </div>
            <div class="mt-3 text-lg font-bold text-highlighted tabular-nums">
              {{ vnd(g.gia_vnd) }}
            </div>
            <UButton class="mt-4" block :loading="dangTao === g.ma" :disabled="!!dangTao || dm?.khoaNap" trailing-icon="i-lucide-qr-code" @click="taoDon(g)">
              Nạp
            </UButton>
          </div>
        </div>

        <div class="mt-10 grid gap-6 md:grid-cols-2">
          <div v-for="g in goiKhac" :key="g.ma" class="flex flex-col rounded-2xl border border-default/70 bg-elevated/40 p-6">
            <div class="flex items-center justify-between gap-3">
              <h3 class="font-display text-2xl font-bold text-highlighted">
                {{ g.ten }}
              </h3>
              <UBadge color="primary" variant="subtle" :label="TEN_LOAI_GOI[g.loai] ?? g.loai" />
            </div>
            <div class="mt-2 text-xl font-bold text-gilded tabular-nums">
              {{ vnd(g.gia_vnd) }}
            </div>
            <p class="mt-3 flex-1 text-sm text-toned">
              {{ g.mo_ta }}
            </p>
            <p v-if="g.loai === 've_mua' && dm?.mua" class="mt-2 text-xs text-muted">
              Áp dụng cho mùa {{ dm.mua.so }} (còn {{ dm.mua.conNgay }} ngày). Nhánh cao cấp cho đúng lượng vật liệu như nhánh miễn phí.
            </p>
            <UButton class="mt-5" block :loading="dangTao === g.ma" :disabled="!!dangTao || dm?.khoaNap" trailing-icon="i-lucide-qr-code" @click="taoDon(g)">
              Mua {{ g.ten }}
            </UButton>
          </div>
        </div>

        <div class="mt-12 grid gap-6 lg:grid-cols-[2fr_1fr]">
          <UCard>
            <template #header>
              <b>Lịch sử nạp</b>
            </template>
            <UTable :data="lichSu" :columns="cotLichSu" empty="Chưa có đơn nào.">
              <template #ma_don-cell="{ row }">
                <NuxtLink :to="{ query: { ma_don: row.original.ma_don } }" class="font-mono lien-ket">
                  {{ row.original.ma_don }}
                </NuxtLink>
              </template>
              <template #so_tien-cell="{ row }">
                {{ vnd(row.original.so_tien) }}
              </template>
              <template #trang_thai-cell="{ row }">
                <UBadge :color="MAU_TRANG_THAI_NAP[row.original.trang_thai] ?? 'neutral'" variant="subtle" :label="TEN_TRANG_THAI_NAP[row.original.trang_thai] ?? row.original.trang_thai" />
              </template>
            </UTable>
          </UCard>

          <UCard>
            <template #header>
              <b>Tự giới hạn chi tiêu</b>
            </template>
            <p class="text-sm text-muted">
              Trần mặc định: {{ vnd(dm?.tran.tuan ?? 0) }}/tuần, {{ vnd(dm?.tran.thang ?? 0) }}/tháng.
              Bạn có thể đặt trần tháng thấp hơn — hạ có hiệu lực ngay, nâng/bỏ phải chờ 72 giờ.
            </p>
            <p v-if="dm?.tran.tuDat" class="mt-2 text-sm">
              Trần tự đặt: <strong>{{ vnd(dm.tran.tuDat) }}</strong>/tháng
            </p>
            <p v-if="dm?.tran.choLuc" class="mt-1 text-xs text-warning">
              Đang chờ đổi thành {{ dm.tran.cho ? vnd(dm.tran.cho) : 'mặc định' }} lúc {{ dm.tran.choLuc }}.
            </p>
            <form class="mt-4 flex gap-2" @submit.prevent="datTran">
              <UInputNumber v-model="tranMoi" :min="0" :step="50000" placeholder="VD: 200000 (0 = bỏ trần)" class="flex-1" />
              <UButton type="submit" :loading="dangDatTran" icon="i-lucide-shield-check">
                Đặt
              </UButton>
            </form>
          </UCard>
        </div>

        <p class="mt-10 text-sm text-muted">
          Trước khi nạp, hãy đọc <NuxtLink to="/dieu-khoan-thanh-toan" class="lien-ket">Điều khoản thanh toán</NuxtLink>
          và <NuxtLink to="/chinh-sach-hoan-tien" class="lien-ket">Chính sách hoàn tiền</NuxtLink>.
          Lượng/vật phẩm không quy đổi ngược ra tiền. Giao dịch đồ trang trí giữa người chơi chỉ qua Chợ trang trí trong game (thuế 7%).
        </p>
      </template>
    </UContainer>
  </div>
</template>
