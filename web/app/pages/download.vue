<script setup lang="ts">
/**
 * Tải game: 5 bản (Java J2ME, Windows, Linux, Android, iOS) từ /admin/tai-game (useBanTai — cùng nguồn với
 * Hướng dẫn tân thủ, trang chủ). Mỗi bản một hàng: tên, yêu cầu, cách cài, nút tải; mã SHA-256 nằm gọn trong
 * "Kiểm tra tệp" cho ai cần đối chiếu.
 */
useSeoMeta({
  title: 'Tải game',
  description: 'Tải Huyền Thoại Bảo Bối miễn phí cho Android, Windows, Linux và điện thoại Java. Tạo tài khoản rồi đăng nhập để chọn Pokémon khởi đầu.',
})

const { link: hoTro, coZalo } = useHoTro()
const { ds: platforms } = useBanTai()

const goiY = ref<string | null>(null)
/** Đang xem bằng iPhone/iPad: các bản .jar/.apk/.zip KHÔNG chạy được trên máy này. */
const laIos = ref(false)
onMounted(() => {
  laIos.value = laThietBiIos(navigator.userAgent, navigator.maxTouchPoints)
  goiY.value = laIos.value ? 'ios' : nenTangTheoUa(navigator.userAgent)
})
/** Bản gợi ý cho thiết bị này (có thể là "Sắp ra mắt", vd. iPhone). */
const banGoiY = computed(() => platforms.value.find(p => p.ma === goiY.value) ?? null)
/** iPhone/iPad mà bản iOS chưa phát hành: báo rõ thay vì mời tải bản không cài được. */
const iosChuaCo = computed(() => laIos.value && !banGoiY.value?.href)
/** Bản này không cài được trên máy đang xem (iOS mở bản Java/Windows/Linux/Android). */
const khongChoMayNay = (p: { ma: string }) => laIos.value && p.ma !== 'ios'
const nhanNut = (p: { kieu: string }) => (p.kieu === 'testflight' ? 'Cài qua TestFlight' : p.kieu === 'app-store' ? 'Mở App Store' : p.kieu === 'ipa-manifest' ? 'Cài trên iPhone/iPad' : 'Tải về')

/** Cột: Java (J2ME) · Windows / Linux · Android. */
const cauHinh = [
  ['Hệ điều hành', 'Điện thoại Java MIDP 2.0, hoặc J2ME Loader trên Android', 'Windows 10 / 11 (64-bit); Linux 64-bit. Có sẵn Java đi kèm', 'Android 8.0 trở lên'],
  ['Bộ nhớ (RAM)', 'Điện thoại Java phổ thông', '2 GB', '2 GB'],
  ['Dung lượng trống', 'Vài MB', '300 MB', '200 MB'],
  ['Kết nối', 'GPRS/3G trở lên', 'Internet ổn định', 'Wi-Fi hoặc 3G/4G'],
  ['Điều khiển', 'Phím số', 'Bàn phím + chuột', 'Cảm ứng'],
]
</script>

<template>
  <div>
    <SitePageBanner
      title="Tải game"
      eyebrow="Miễn phí"
      intro="Chọn bản cho thiết bị của bạn. Tạo tài khoản trước rồi đăng nhập trong game để chọn Pokémon khởi đầu."
    />

    <section id="download" class="scroll-mt-20 py-10 sm:py-14" aria-labelledby="download-title">
      <UContainer class="max-w-5xl">
        <h2 id="download-title" class="sr-only">
          Chọn nền tảng
        </h2>

        <!-- Gợi ý theo thiết bị -->
        <div v-if="banGoiY" class="khung ring-gilded mb-6 flex flex-col items-center gap-4 p-5 text-center sm:flex-row sm:text-left" aria-live="polite">
          <img src="/logo-mark.svg" width="56" height="56" alt="" class="size-14 shrink-0">
          <div class="min-w-0 flex-1">
            <p class="text-xs font-extrabold uppercase tracking-wider text-primary">
              Gợi ý cho thiết bị của bạn
            </p>
            <p class="font-display text-2xl font-extrabold text-highlighted">
              {{ banGoiY.ten }}
            </p>
            <p v-if="iosChuaCo" class="mt-1 text-sm text-muted">
              <strong class="text-highlighted">Bản iOS sắp ra mắt; hiện có thể chơi bằng PC/Android.</strong>
              iPhone/iPad không cài được tệp .jar, .apk hay bản Windows/Linux bên dưới — hãy tải trên máy tính hoặc điện thoại Android.
              <template v-if="coZalo">
                Theo dõi <ULink :to="hoTro" target="_blank" class="font-semibold text-primary hover:underline">nhóm Zalo</ULink> để biết khi có bản iOS.
              </template>
            </p>
          </div>
          <UButton v-if="banGoiY.href" :to="banGoiY.href" external size="xl" class="btn-pikachu px-6" icon="i-lucide-download">
            {{ nhanNut(banGoiY) }}
          </UButton>
          <span v-else class="rounded-md border-2 border-[var(--khung-vien)] bg-elevated px-3 py-2 font-display font-bold text-toned">
            {{ banGoiY.sapRaMat ? 'Sắp ra mắt' : 'Đang cập nhật bản tải' }}
          </span>
        </div>

        <ul class="space-y-4">
          <li v-for="p in platforms" :id="`ban-${p.ma}`" :key="p.ma" class="khung overflow-hidden" :class="p.ma === goiY ? 'ring-gilded' : ''">
            <div class="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:p-5">
              <span class="o-slot size-14 shrink-0 text-highlighted" aria-hidden="true">
                <UIcon :name="p.icon" class="size-7" />
              </span>
              <div class="min-w-0 flex-1">
                <h3 class="flex flex-wrap items-center gap-2 font-display text-xl font-extrabold text-highlighted">
                  {{ p.ten }}
                  <span v-if="p.sapRaMat" class="rounded bg-pikachu-400 px-1.5 py-0.5 text-[11px] font-extrabold uppercase text-[#261a00]">Sắp ra mắt</span>
                </h3>
                <p class="mt-0.5 text-sm text-muted">
                  {{ p.moTa }}
                </p>
                <p class="mt-1 text-xs font-semibold text-dimmed">
                  <span v-if="p.heDieuHanh">Cần {{ p.heDieuHanh }}</span>
                  <span v-if="p.phienBan"> · Phiên bản {{ p.phienBan }}</span>
                  <span v-if="p.dungLuong"> · {{ p.dungLuong }}</span>
                  <span v-if="p.capNhat"> · {{ p.capNhat }}</span>
                </p>
              </div>
              <div class="shrink-0 sm:w-48">
                <template v-if="p.href && khongChoMayNay(p)">
                  <p class="mb-1.5 text-center text-xs font-bold text-warning">
                    Không chạy trên iPhone/iPad
                  </p>
                  <UButton :to="p.href" external block size="md" color="neutral" variant="outline" icon="i-lucide-monitor-smartphone">
                    Tải cho máy khác
                  </UButton>
                </template>
                <UButton v-else-if="p.href" :to="p.href" external block size="lg" class="btn-pikachu" icon="i-lucide-download">
                  {{ nhanNut(p) }}
                </UButton>
                <p v-else class="rounded-md border-2 border-dashed border-[var(--ui-border-accented)] px-3 py-2 text-center text-sm font-bold text-muted">
                  {{ p.sapRaMat ? 'Sắp ra mắt' : 'Đang cập nhật' }}
                </p>
              </div>
            </div>
            <div v-if="p.buoc.length || p.nhatKy || (p.sha256 && p.href) || (p.sapRaMat && coZalo)" class="flex flex-wrap gap-x-6 gap-y-2 border-t-2 border-[var(--ui-border-muted)] bg-muted/50 px-4 py-2.5 text-sm sm:px-5">
              <details v-if="p.buoc.length" class="group min-w-0">
                <summary class="cursor-pointer font-semibold text-primary">
                  Cách cài
                </summary>
                <ol class="mt-2 list-decimal space-y-1 pl-5 text-muted">
                  <li v-for="b in p.buoc" :key="b">
                    {{ b }}
                  </li>
                </ol>
              </details>
              <details v-if="p.nhatKy" class="min-w-0">
                <summary class="cursor-pointer font-semibold text-primary">
                  Có gì mới
                </summary>
                <p class="mt-2 whitespace-pre-line text-muted">
                  {{ p.nhatKy }}
                </p>
              </details>
              <details v-if="p.sha256 && p.href" class="min-w-0">
                <summary class="cursor-pointer font-semibold text-dimmed">
                  Kiểm tra tệp
                </summary>
                <p class="mt-2 text-xs text-muted">
                  Mã SHA-256 của tệp tải về (để đối chiếu tệp không bị sửa):
                </p>
                <code class="mt-1 block break-all rounded bg-elevated p-2 text-xs">{{ p.sha256 }}</code>
              </details>
              <p v-if="p.sapRaMat && coZalo" class="text-muted">
                Theo dõi <ULink :to="hoTro" target="_blank" class="font-semibold text-primary hover:underline">nhóm Zalo</ULink> để biết khi có bản.
              </p>
            </div>
          </li>
        </ul>

        <p class="mt-6 text-sm text-muted">
          Chưa có tài khoản? <NuxtLink to="/register" class="font-semibold text-primary hover:underline">Đăng ký</NuxtLink> ·
          Cài xong, xem <NuxtLink to="/huong-dan" class="font-semibold text-primary hover:underline">hướng dẫn tân thủ</NuxtLink>.
        </p>
      </UContainer>
    </section>

    <!-- ============ YÊU CẦU CẤU HÌNH ============ -->
    <section id="yeu-cau" class="scroll-mt-20 pb-12" aria-labelledby="yeu-cau-title">
      <UContainer class="max-w-5xl">
        <h2 id="yeu-cau-title" class="tieu-de-muc text-2xl sm:text-3xl">
          Cấu hình khuyến nghị
        </h2>
        <div class="bang-cuon mt-4">
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
      </UContainer>
    </section>

    <!-- ============ HỖ TRỢ ============ -->
    <section id="support" class="scroll-mt-20 pb-6" aria-labelledby="support-title">
      <UContainer class="max-w-5xl">
        <div class="khung flex flex-col gap-5 p-5 sm:flex-row sm:items-center sm:p-7">
          <img :src="anhChanDung(113)" width="40" height="40" alt="" class="pixelated size-16 shrink-0 rounded-md border-2 border-[var(--khung-vien)]">
          <div class="min-w-0 flex-1">
            <h2 id="support-title" class="tieu-de-muc text-2xl">
              Cần hỗ trợ cài đặt?
            </h2>
            <p class="mt-1 text-muted">
              Hỏi cách cài, báo lỗi, khiếu nại khoá tài khoản qua <strong class="text-highlighted">Zalo</strong>.
              Ban quản trị không bao giờ hỏi mật khẩu — chỉ tin link Zalo trên website này.
            </p>
          </div>
          <SiteHoTroZalo v-if="coZalo" kieu="the" class="w-full sm:w-auto sm:max-w-md" />
          <span v-else class="text-sm font-semibold text-dimmed">Kênh hỗ trợ đang cập nhật</span>
        </div>
      </UContainer>
    </section>
  </div>
</template>
