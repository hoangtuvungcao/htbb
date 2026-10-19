<script setup lang="ts">
useSeoMeta({
  title: 'Tải game',
  description: 'Một lần tải game. Mở ra cả một thế giới để khám phá, kết giao và viết nên câu chuyện của bạn.',
})

const { data: site } = await useSite()
const { link: hoTro, coZalo } = useHoTro()

/** Link được cấu hình hay còn để trống / '#'. */
function coLink(url: string | null | undefined): boolean {
  const u = (url ?? '').trim()
  return u !== '' && u !== '#'
}

/** Bản tải: /admin/tai-game (danh mục chung useBanTai — cùng nguồn với Hướng dẫn tân thủ, trang chủ, FAQ). */
const { ds: dsBanTai } = useBanTai()
const MAU: Record<string, { mau: string, anh: string }> = {
  'jar': { mau: 'from-pokeblue-400/25 to-pokeblue-900/5', anh: 'shadow-pokeblue-500/30' },
  'windows': { mau: 'from-pikachu-400/25 to-pikachu-900/5', anh: 'shadow-pikachu-500/30' },
  'linux': { mau: 'from-pikachu-400/20 to-pikachu-900/5', anh: 'shadow-pikachu-500/30' },
  'android': { mau: 'from-grass-400/25 to-grass-900/5', anh: 'shadow-grass-500/30' },
  'ios': { mau: 'from-pokered-400/25 to-pokered-900/5', anh: 'shadow-pokered-500/30' },
}
const platforms = computed(() => dsBanTai.value.map(b => ({ ...b, ...(MAU[b.ma] ?? MAU.windows!) })))

const goiY = ref<string | null>(null)
onMounted(() => { goiY.value = nenTangTheoUa(navigator.userAgent) })
/** Bản gợi ý cho thiết bị này (có thể là "Sắp ra mắt", vd. iPhone). */
const banGoiY = computed(() => platforms.value.find(p => p.ma === goiY.value) ?? null)
const nhanNut = (p: { kieu: string }) => (p.kieu === 'testflight' ? 'Cài qua TestFlight' : p.kieu === 'app-store' ? 'Mở App Store' : p.kieu === 'ipa-manifest' ? 'Cài trên iPhone/iPad' : 'Tải / cài đặt')
const shaMo = ref<string | null>(null)

const steps = computed(() => [
  { so: '01.', tieuDe: 'Tạo tài khoản', mo: 'Đăng ký tài khoản để bắt đầu hành trình của bạn.', link: 'Đăng ký ngay', to: '/register' },
  { so: '02.', tieuDe: 'Tải phiên bản phù hợp', mo: 'Chọn nền tảng và làm theo hướng dẫn cài đặt.', link: 'Chọn bản tải', to: '/download#download' },
  { so: '03.', tieuDe: 'Chọn Pokémon khởi đầu', mo: 'Đăng nhập game, chọn Pokémon của bạn và tìm những người bạn đồng hành.', link: 'Cộng đồng & hỗ trợ', to: hoTro.value },
])

/** Cột: Java (J2ME) · Windows / Linux · Android. iOS chưa phát hành nên chưa có cột. */
const cauHinh = [
  ['Hệ điều hành', 'Điện thoại hỗ trợ Java MIDP 2.0, hoặc J2ME Loader trên Android', 'Windows 10 / 11 (64-bit); Linux x86-64. Có sẵn Java đi kèm', 'Android 8.0 trở lên'],
  ['Bộ nhớ (RAM)', 'Điện thoại Java phổ thông', '2 GB', '2 GB'],
  ['Dung lượng trống', 'Vài MB', '300 MB', '200 MB'],
  ['Kết nối', 'GPRS/3G trở lên', 'Internet ổn định', 'Wi-Fi hoặc 3G/4G ổn định'],
  ['Điều khiển', 'Phím số', 'Bàn phím + chuột', 'Cảm ứng'],
]

const faq = [
  { label: 'Tôi cần làm gì sau khi đăng ký?', slot: 'faq1' as const },
  { label: 'Tôi gặp khó khăn khi cài đặt hoặc đăng nhập?', slot: 'faq2' as const },
]
</script>

<template>
  <div>
    <SitePageBanner
      title="Tải game"
      eyebrow="Miễn phí · không bán sức mạnh"
      intro="Một lần tải game. Mở ra cả một thế giới để khám phá, kết giao và viết nên câu chuyện của bạn."
    />

    <!-- ============ CHỌN NỀN TẢNG ============ -->
    <section id="download" class="scroll-mt-20 py-20" aria-labelledby="download-title">
      <UContainer class="max-w-6xl">
        <FxReveal class="text-center">
          <p class="eyebrow justify-center">
            Chọn nền tảng của bạn
          </p>
          <h2 id="download-title" class="section-title mt-3 text-4xl text-highlighted sm:text-5xl">
            Sẵn sàng cho cuộc phiêu lưu?
          </h2>
          <p class="mx-auto mt-4 max-w-2xl text-muted">
            Chọn phiên bản phù hợp với thiết bị. Bạn có thể tạo tài khoản trước khi cài đặt game.
          </p>
        </FxReveal>

        <!-- Gợi ý theo thiết bị -->
        <div v-if="banGoiY" class="glass mx-auto mt-10 flex max-w-2xl flex-col items-center gap-3 rounded-2xl p-6 text-center sm:flex-row sm:text-left" aria-live="polite">
          <UIcon :name="banGoiY.icon" class="size-10 shrink-0 text-primary" />
          <div class="min-w-0 flex-1">
            <p class="text-sm text-muted">Gợi ý cho thiết bị của bạn</p>
            <p class="font-display text-xl font-bold text-highlighted">{{ banGoiY.ten }}<span v-if="banGoiY.phienBan" class="text-base font-semibold text-muted"> · {{ banGoiY.phienBan }}</span></p>
          </div>
          <UButton v-if="banGoiY.href" :to="banGoiY.href" external size="lg" trailing-icon="i-lucide-download">{{ nhanNut(banGoiY) }}</UButton>
          <UBadge v-else color="warning" variant="subtle" size="lg" icon="i-lucide-hourglass">{{ banGoiY.trangThai === 'sap-ra-mat' ? 'Sắp ra mắt' : 'Đang cập nhật bản tải' }}</UBadge>
        </div>

        <div class="mt-14 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          <FxReveal v-for="(p, i) in platforms" :key="p.ma" :delay="Math.min(i, 5) * 0.08">
            <FxTiltCard :max="12" as="article" class="group h-full rounded-3xl">
              <div class="relative flex h-full flex-col items-center overflow-hidden rounded-3xl glass ring-gilded bg-gradient-to-b p-8 text-center" :class="[p.mau, p.ma === goiY ? 'ring-2 ring-primary' : '']">
                <small class="text-[0.7rem] tracking-[0.25em] text-dimmed">{{ String(i + 1).padStart(2, '0') }} / NỀN TẢNG</small>
                <UBadge v-if="p.trangThai === 'sap-ra-mat'" color="warning" variant="solid" class="absolute right-4 top-4">Sắp ra mắt</UBadge>
                <span
                  class="mt-8 grid size-20 place-items-center rounded-2xl border border-white/10 bg-white/5 text-highlighted shadow-2xl transition-transform duration-500 [transform:translateZ(70px)] group-hover:scale-110"
                  :class="p.anh"
                  aria-hidden="true"
                >
                  <UIcon :name="p.icon" class="size-10" />
                </span>
                <h3 class="mt-8 font-display text-2xl font-bold text-highlighted [transform:translateZ(40px)]">
                  {{ p.ten }}
                </h3>
                <p class="mt-2 text-muted [transform:translateZ(25px)]">
                  {{ p.moTa }}
                </p>
                <ul class="mt-4 flex flex-1 flex-wrap content-start justify-center gap-1.5 [transform:translateZ(20px)]">
                  <li v-for="c in p.chiTiet" :key="c" class="rounded-full bg-elevated px-2.5 py-1 text-xs font-semibold text-toned">
                    {{ c }}
                  </li>
                </ul>
                <p v-if="p.heDieuHanh" class="mt-4 text-xs text-muted">
                  <UIcon name="i-lucide-info" class="size-3.5 align-[-2px]" /> Cần: {{ p.heDieuHanh }}
                </p>
                <p v-if="p.phienBan || p.dungLuong || p.capNhat" class="mt-2 text-xs font-semibold text-dimmed">
                  <span v-if="p.phienBan">Phiên bản {{ p.phienBan }}</span>
                  <span v-if="p.dungLuong"> · {{ p.dungLuong }}</span>
                  <span v-if="p.capNhat"> · cập nhật {{ p.capNhat }}</span>
                </p>
                <details v-if="p.nhatKy" class="mt-2 w-full text-left text-xs text-muted">
                  <summary class="cursor-pointer text-center font-semibold text-primary">Có gì mới</summary>
                  <p class="mt-2 whitespace-pre-line">{{ p.nhatKy }}</p>
                </details>
                <div v-if="p.sha256 && p.href" class="mt-2 w-full text-xs text-muted">
                  <button type="button" class="font-semibold text-primary hover:underline" :aria-expanded="shaMo === p.ma" @click="shaMo = shaMo === p.ma ? null : p.ma">
                    Mã kiểm tra SHA-256
                  </button>
                  <code v-if="shaMo === p.ma" class="mt-1 block break-all rounded bg-elevated p-2 text-left">{{ p.sha256 }}</code>
                </div>
                <div class="mt-8 w-full [transform:translateZ(30px)]">
                  <UButton
                    v-if="p.href"
                    :to="p.href"
                    external
                    block
                    size="lg"
                    trailing-icon="i-lucide-arrow-up-right"
                    class="shadow-[0_10px_30px_rgba(10,26,51,.14)]"
                  >
                    {{ nhanNut(p) }}
                  </UButton>
                  <template v-else-if="p.trangThai === 'sap-ra-mat'">
                    <UBadge color="warning" variant="outline" size="lg" icon="i-lucide-hourglass" class="w-full justify-center py-2">
                      Sắp ra mắt
                    </UBadge>
                    <p v-if="coZalo" class="mt-2 text-xs text-muted">
                      Theo dõi <ULink :to="hoTro" target="_blank" class="font-semibold text-primary hover:underline">nhóm Zalo</ULink> để biết khi có bản.
                    </p>
                  </template>
                  <UBadge v-else color="neutral" variant="outline" size="lg" icon="i-lucide-hourglass" class="w-full justify-center py-2">
                    Đang cập nhật bản tải
                  </UBadge>
                </div>
              </div>
            </FxTiltCard>
          </FxReveal>
        </div>
      </UContainer>
    </section>

    <!-- ============ YÊU CẦU CẤU HÌNH ============ -->
    <section id="yeu-cau" class="scroll-mt-20 pb-20" aria-labelledby="yeu-cau-title">
      <UContainer class="max-w-6xl">
        <h2 id="yeu-cau-title" class="section-title text-3xl text-highlighted">
          Yêu cầu cấu hình (khuyến nghị)
        </h2>
        <p class="mt-2 text-muted">
          Game nhẹ, chạy tốt trên máy cũ. Bảng dưới là mức khuyến nghị để chơi mượt.
        </p>
        <div class="bang-cuon mt-5">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th /><th>Java (J2ME)</th><th>Windows / Linux</th><th>Android</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="r in cauHinh" :key="r[0]">
                <td class="font-semibold text-highlighted">
                  {{ r[0] }}
                </td><td>{{ r[1] }}</td><td>{{ r[2] }}</td><td>{{ r[3] }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <SiteLuuYTuoi class="mt-5" />
        <p class="mt-3 text-sm text-muted">
          Cài xong? Làm theo <NuxtLink to="/huong-dan" class="font-semibold text-primary hover:underline">hướng dẫn tân thủ</NuxtLink> để bắt đầu.
        </p>
      </UContainer>
    </section>

    <!-- ============ HỖ TRỢ ============ -->
    <section id="support" class="scroll-mt-20 pb-20" aria-labelledby="support-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative flex flex-col items-start justify-between gap-6 overflow-hidden rounded-3xl glass ring-gilded p-8 sm:flex-row sm:items-center sm:p-10">
            <div class="pointer-events-none absolute -left-16 -bottom-24 size-72 rounded-full bg-grass-500/15 blur-3xl" aria-hidden="true" />
            <div class="relative flex items-start gap-5">
              <span class="hidden size-14 shrink-0 place-items-center rounded-2xl border border-grass-400/30 bg-grass-500/10 text-grass-300 sm:grid" aria-hidden="true">
                <UIcon name="i-lucide-life-buoy" class="size-7" />
              </span>
              <div>
                <p class="eyebrow">
                  Luôn có bạn bè đồng hành
                </p>
                <h2 id="support-title" class="section-title mt-2 text-3xl text-highlighted">
                  Cần hỗ trợ cài đặt?
                </h2>
                <p class="mt-2 text-muted">
                  Kênh hỗ trợ chính thức là <strong class="text-highlighted">Zalo</strong>: hỏi cách cài đặt, báo lỗi,
                  khiếu nại khoá tài khoản và tìm bạn cùng chơi.
                </p>
                <p class="mt-1 text-xs text-dimmed">
                  Ban quản trị không bao giờ hỏi mật khẩu. Chỉ tin link Zalo hiện trên website này.
                </p>
              </div>
            </div>
            <SiteHoTroZalo v-if="coZalo" kieu="the" class="relative w-full sm:w-auto sm:max-w-md" />
            <UBadge v-else color="neutral" variant="outline" size="lg" icon="i-lucide-hourglass" class="relative">
              Kênh hỗ trợ đang cập nhật
            </UBadge>
          </div>
        </FxReveal>
      </UContainer>
    </section>

    <!-- ============ HÀNH TRÌNH + FAQ ============ -->
    <section id="guide" class="scroll-mt-20 pb-28" aria-labelledby="guide-title">
      <UContainer class="max-w-6xl">
        <FxReveal>
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-8 sm:p-12">
            <div class="pointer-events-none absolute -right-24 -top-24 size-80 rounded-full bg-pikachu-400/10 blur-3xl" aria-hidden="true" />
            <p class="eyebrow">
              Hẹn gặp bạn trong thế giới Pokémon
            </p>
            <h2 id="guide-title" class="section-title mt-3 text-4xl text-highlighted">
              Hành trình bắt đầu từ đây.
            </h2>
            <div class="mt-10 grid gap-8 md:grid-cols-3">
              <div v-for="s in steps" :key="s.so">
                <b class="font-display text-5xl text-primary/30" aria-hidden="true">{{ s.so }}</b>
                <h3 class="mt-2 text-lg font-bold text-highlighted">
                  {{ s.tieuDe }}
                </h3>
                <p class="mt-1 text-muted">
                  {{ s.mo }}
                </p>
                <ULink :to="s.to" :target="s.to.startsWith('https://') ? '_blank' : undefined" class="mt-3 inline-flex items-center gap-1 font-semibold text-primary hover:underline">
                  {{ s.link }} <UIcon name="i-lucide-arrow-up-right" class="size-4" />
                </ULink>
              </div>
            </div>
            <UAccordion :items="faq" class="mt-12" :ui="{ trigger: 'text-base font-semibold' }">
              <template #faq1-body>
                <p class="text-muted">
                  Tải và cài đặt game, sau đó đăng nhập bằng tài khoản vừa tạo. Nếu tài khoản yêu cầu kích hoạt, hãy truy cập
                  <NuxtLink to="/kichhoat" class="text-primary hover:underline">trang kích hoạt</NuxtLink> và làm theo hướng dẫn.
                </p>
              </template>
              <template #faq2-body>
                <p class="text-muted">
                  Tham gia <ULink :to="hoTro" :target="hoTro.startsWith('https://') ? '_blank' : undefined" class="text-primary hover:underline">nhóm hỗ trợ</ULink> và mô tả lỗi để được hướng dẫn. Không chia sẻ mật khẩu tài khoản trong nhóm.
                </p>
              </template>
            </UAccordion>
          </div>
        </FxReveal>
      </UContainer>
    </section>
  </div>
</template>
