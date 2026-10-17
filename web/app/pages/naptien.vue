<script setup lang="ts">
/**
 * Nạp tiền theo mốc.
 * Chọn mốc -> tạo đơn -> hiện QR VietQR (TPBank). Trang tự hỏi lại trạng thái vài
 * giây một lần; SePay báo tiền vào (hoặc admin xác nhận tay) thì chuyển sang "đã nhận".
 * Quà KHÔNG phát trên web: người chơi vào game gặp NPC Tổng Quản để nhận.
 */
definePageMeta({ middleware: ['auth', 'nap-bat'] })
useSeoMeta({
  title: 'Nạp tiền theo mốc',
  description: 'Chọn mốc nạp tân thủ, quét mã QR chuyển khoản TPBank và nhận quà tại NPC Tổng Quản trong game.',
})

interface DonHienThi {
  ma_don: string
  moc: number
  amount: number
  trang_thai: string
  created_at: string
  paid_at: string | null
}

const api = useApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()

const maDon = computed(() => (typeof route.query.ma_don === 'string' ? route.query.ma_don : ''))
const don = ref<DonHienThi | null>(null)
const qr = ref('')
const loi = ref('')
const dangTai = ref(false)
const dangTao = ref<number | null>(null)
const daXong = computed(() => !!don.value && don.value.trang_thai !== 'cho_thanh_toan')

const mocList = (Object.entries(MOC_NAP) as [string, (typeof MOC_NAP)[1]][]).map(([k, v]) => ({ moc: Number(k), ...v }))
const mocTrangTri: Record<number, { nhan: string, mau: string, vien: string, ky: string }> = {
  1: { nhan: 'Khởi đầu', mau: 'from-grass-400/25 to-grass-900/5', vien: 'hover:border-grass-400/60', ky: '✦' },
  2: { nhan: 'Đồng hành', mau: 'from-sky-400/25 to-sky-900/5', vien: 'hover:border-sky-400/60', ky: '✧' },
  3: { nhan: 'Huyền thoại', mau: 'from-pikachu-400/30 to-pikachu-900/10', vien: 'hover:border-pikachu-300/70', ky: '❖' },
}

// --- Hỏi lại trạng thái: 4 giây một lần, lỗi mạng thì giãn ra 6 giây ---
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
  if (!ma || daXong.value) return
  try {
    const r = await api<{ trang_thai: string }>('/api/nap/trang-thai', { query: { ma_don: ma } })
    if (don.value?.ma_don !== ma) return
    if (r.trang_thai && r.trang_thai !== 'cho_thanh_toan') {
      don.value.trang_thai = r.trang_thai
      toast.add({ title: 'Đã nhận được thanh toán!', description: 'Vào game gặp NPC Tổng Quản để nhận quà.', color: 'success', icon: 'i-lucide-party-popper' })
    } else {
      henHoi(4000)
    }
  } catch (e: any) {
    // Hết phiên đăng nhập thì thôi hỏi (hỏi tiếp cũng chỉ nhận 401); báo để người chơi đăng nhập lại.
    if (e?.statusCode === 401) {
      loi.value = loiApi(e)
      return
    }
    henHoi(6000)
  }
}

async function taiDon(ma: string) {
  dungHoi()
  loi.value = ''
  don.value = null
  if (!ma) return
  dangTai.value = true
  try {
    const r = await api<{ don: DonHienThi, qr: string }>('/api/nap/don', { query: { ma_don: ma } })
    don.value = r.don
    qr.value = r.qr
    if (!daXong.value) henHoi(4000)
  } catch (e) {
    loi.value = loiApi(e)
  } finally {
    dangTai.value = false
  }
}

async function taoDon(moc: number) {
  dangTao.value = moc
  loi.value = ''
  try {
    const r = await api<{ ma_don: string }>('/api/nap/tao-don', { method: 'POST', body: { moc } })
    await router.push({ query: { ma_don: r.ma_don } })
  } catch (e) {
    toast.add({ title: 'Chưa tạo được đơn nạp', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangTao.value = null
  }
}

async function chepMa() {
  if (!don.value) return
  try {
    await navigator.clipboard.writeText(don.value.ma_don)
    toast.add({ title: `Đã chép nội dung ${don.value.ma_don}`, color: 'success', icon: 'i-lucide-copy-check' })
  } catch { /* trình duyệt chặn clipboard */ }
}

watch(maDon, ma => taiDon(ma), { immediate: true })
onBeforeUnmount(dungHoi)
</script>

<template>
  <div>
    <SitePageBanner
      title="Nạp tiền theo mốc"
      eyebrow="Quà tân thủ"
      intro="Chọn 1 trong 3 mốc tân thủ bên dưới, quét mã QR để chuyển khoản qua TPBank. Sau khi thanh toán thành công, vào game gặp NPC Tổng Quản để nhận quà."
    />

    <UContainer class="max-w-6xl py-14">
      <UAlert
        v-if="loi"
        class="mb-8"
        color="error"
        variant="subtle"
        icon="i-lucide-circle-alert"
        :title="loi"
        role="alert"
      />

      <!-- ============ ĐANG XEM 1 ĐƠN ============ -->
      <div v-if="maDon && (don || dangTai)" class="mx-auto max-w-xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-6 sm:p-10">
            <div class="pointer-events-none absolute -right-24 -top-24 size-72 rounded-full bg-pikachu-400/10 blur-3xl" aria-hidden="true" />

            <div v-if="dangTai" class="flex items-center justify-center gap-3 py-24 text-muted">
              <UIcon name="i-lucide-loader-circle" class="size-6 animate-spin" /> Đang tải đơn nạp...
            </div>

            <template v-else-if="don">
              <p class="eyebrow">
                Đơn nạp của bạn
              </p>
              <h2 class="section-title mt-3 text-3xl text-highlighted">
                Mốc {{ don.moc }} · <span class="text-gilded">{{ vnd(don.amount) }}</span>
              </h2>

              <!-- Chờ thanh toán -->
              <div v-if="!daXong" class="mt-6">
                <FxTiltCard :max="6" :scale="1.01" class="mx-auto w-fit rounded-2xl">
                  <div class="rounded-2xl bg-white p-3 shadow-[0_20px_60px_rgba(10,26,51,.14)]">
                    <img :src="qr" alt="Mã QR chuyển khoản TPBank" class="block size-64 sm:size-72" width="288" height="288">
                  </div>
                </FxTiltCard>
                <p class="mt-4 text-center text-sm text-muted">
                  Quét mã bằng app TPBank (hoặc app ngân hàng bất kỳ hỗ trợ VietQR).
                </p>

                <dl class="mt-6 divide-y divide-default/60 overflow-hidden rounded-xl border border-default/60 text-sm">
                  <div class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Số tiền
                    </dt>
                    <dd class="font-bold text-highlighted tabular-nums">
                      {{ vnd(don.amount) }}
                    </dd>
                  </div>
                  <div class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Nội dung CK
                    </dt>
                    <dd class="flex items-center gap-2">
                      <strong class="font-mono text-base tracking-wider text-primary">{{ don.ma_don }}</strong>
                      <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-copy" aria-label="Chép nội dung chuyển khoản" @click="chepMa" />
                    </dd>
                  </div>
                  <div class="flex items-center justify-between gap-4 px-4 py-3">
                    <dt class="text-muted">
                      Ngân hàng
                    </dt>
                    <dd class="text-highlighted">
                      TPBank
                    </dd>
                  </div>
                </dl>

                <UAlert
                  class="mt-6"
                  color="warning"
                  variant="subtle"
                  icon="i-lucide-triangle-alert"
                  :description="`Chuyển khoản đúng số tiền và giữ nguyên nội dung ${don.ma_don} để hệ thống tự nhận ra đơn của bạn.`"
                />
                <p class="mt-4 flex items-center justify-center gap-2 text-sm text-muted" role="status" aria-live="polite">
                  <span class="relative flex size-2.5">
                    <span class="absolute inline-flex size-full animate-ping rounded-full bg-primary/70" />
                    <span class="relative inline-flex size-2.5 rounded-full bg-primary" />
                  </span>
                  Đang chờ thanh toán... trang sẽ tự cập nhật.
                </p>
              </div>

              <!-- Đã thanh toán -->
              <div v-else class="mt-8 text-center" role="status" aria-live="polite">
                <div class="mx-auto grid size-20 place-items-center rounded-full border border-success/40 bg-success/10 text-success shadow-[0_0_50px_rgba(32,224,191,.3)]">
                  <UIcon name="i-lucide-check" class="size-10" />
                </div>
                <h3 class="mt-6 font-display text-2xl font-bold text-highlighted">
                  Đã nhận được thanh toán!
                </h3>
                <p class="mt-3 text-muted">
                  Vào game, gặp <strong class="text-highlighted">NPC Tổng Quản</strong>, chọn <strong class="text-highlighted">"Nhận mốc nạp"</strong> để nhận quà.
                </p>
                <UBadge class="mt-4" color="success" variant="subtle" :label="TEN_TRANG_THAI_DON[don.trang_thai] ?? don.trang_thai" />
              </div>

              <UButton to="/naptien" class="mt-8" color="neutral" variant="outline" icon="i-lucide-arrow-left">
                Chọn mốc khác
              </UButton>
            </template>
          </div>
        </FxReveal>
      </div>

      <!-- ============ CHỌN MỐC ============ -->
      <template v-else>
        <div v-if="maDon" class="mb-8 text-center">
          <UButton to="/naptien" color="neutral" variant="outline" icon="i-lucide-arrow-left">
            Chọn mốc khác
          </UButton>
        </div>
        <div class="grid gap-6 md:grid-cols-3">
          <FxReveal v-for="(m, i) in mocList" :key="m.moc" :delay="i * 0.1">
            <FxTiltCard :max="10" as="article" class="group h-full rounded-2xl">
              <div
                class="relative flex h-full flex-col overflow-hidden rounded-2xl border border-default/70 bg-gradient-to-br p-7 transition-colors duration-300"
                :class="[mocTrangTri[m.moc]?.mau, mocTrangTri[m.moc]?.vien, m.moc === 3 ? 'ring-gilded' : '']"
              >
                <div class="flex items-center justify-between">
                  <small class="text-[0.7rem] tracking-[0.2em] text-dimmed">0{{ m.moc }} / {{ mocTrangTri[m.moc]?.nhan?.toUpperCase() }}</small>
                  <UBadge v-if="m.moc === 3" color="primary" variant="subtle" label="Giá trị nhất" />
                </div>
                <span class="mt-6 font-display text-5xl text-primary/80 [transform:translateZ(60px)]" aria-hidden="true">{{ mocTrangTri[m.moc]?.ky }}</span>
                <h3 class="mt-4 font-display text-2xl font-bold text-highlighted [transform:translateZ(40px)]">
                  Mốc {{ m.moc }}
                </h3>
                <div class="mt-1 font-display text-4xl font-extrabold text-gilded [transform:translateZ(50px)]">
                  {{ vnd(m.gia) }}
                </div>
                <ul class="mt-6 flex-1 space-y-2.5 text-sm text-toned [transform:translateZ(25px)]">
                  <li v-for="it in m.items" :key="it" class="flex gap-2.5">
                    <UIcon name="i-lucide-gem" class="mt-0.5 size-4 shrink-0 text-secondary" />
                    <span>{{ it }}</span>
                  </li>
                </ul>
                <UButton
                  class="mt-8 [transform:translateZ(30px)]"
                  size="lg"
                  block
                  :loading="dangTao === m.moc"
                  :disabled="dangTao !== null && dangTao !== m.moc"
                  trailing-icon="i-lucide-qr-code"
                  @click="taoDon(m.moc)"
                >
                  Nạp mốc {{ m.moc }}
                </UButton>
              </div>
            </FxTiltCard>
          </FxReveal>
        </div>
      </template>
    </UContainer>
  </div>
</template>
