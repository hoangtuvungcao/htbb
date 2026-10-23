<script setup lang="ts">
/**
 * Kích hoạt tài khoản. Chỉ XEM trạng thái: kích hoạt do quản
 * trị viên cấp, người chơi không tự gia hạn được.
 */
definePageMeta({ middleware: 'auth' })
useSeoMeta({ title: 'Kích hoạt tài khoản', robots: 'noindex' })

const api = useApi()
// Dùng chung API với trang Tài khoản (cùng câu kiểm tra 5h_active).
const { data, error, refresh } = useAsyncData<{ username: string, activated: boolean }>(
  'htbb-kichhoat',
  () => api('/api/account'),
  { server: false },
)
watch(error, (e: any) => {
  if (e?.statusCode === 401) navigateTo('/login')
})
const { data: site } = await useSite()
// Thành viên HTBB khác với kích hoạt: kích hoạt = được đăng nhập (5h_active, admin cấp); Thành viên = mở các
// tính năng cộng đồng khi máy chủ bắt buộc (mua trên web). docs/van-hanh.md.
const { data: tv } = useAsyncData<{ coBang: boolean, trangThai: { laThanhVien: boolean, hetHanMs: number | null } | null }>(
  'htbb-tv-kichhoat', () => api('/api/thanh-vien/cua-toi'), { server: false })
// Chưa kích hoạt VẪN chơi được (trừ khi admin bật kich_hoat.chan_dang_nhap); chỉ khoá vài tính năng.
const { data: tt } = useFetch<{ kichHoat: { chanDangNhap: boolean, chucNang: { ma: string, ten: string }[], thanhVienLaKichHoat: boolean } }>(
  '/api/thanh-vien/thong-tin', { key: 'tv-thong-tin-kh', server: false })
const choChoi = computed(() => !!data.value?.activated || tt.value?.kichHoat.chanDangNhap === false)
const hoTro = useHoTro().link
</script>

<template>
  <section class="relative overflow-hidden py-16 sm:py-24">
    <FxGlowOrbs />
    <UContainer class="relative max-w-xl">
      <FxReveal>
        <FxTiltCard :max="8" class="rounded-3xl">
          <div class="relative overflow-hidden rounded-3xl glass ring-gilded p-8 text-center sm:p-12">
            <div class="pointer-events-none absolute -left-20 -top-20 size-64 rounded-full bg-grass-500/10 blur-3xl" aria-hidden="true" />
            <div
              class="mx-auto grid size-20 place-items-center rounded-full border [transform:translateZ(60px)]"
              :class="data?.activated
                ? 'border-success/40 bg-success/10 text-success shadow-[0_0_40px_rgba(7,196,166,.3)]'
                : 'border-warning/40 bg-warning/10 text-warning shadow-[0_0_40px_rgba(10,26,51,.14)]'"
              aria-hidden="true"
            >
              <UIcon :name="data?.activated ? 'i-lucide-shield-check' : 'i-lucide-shield-alert'" class="size-10" />
            </div>
            <p class="eyebrow mt-8 justify-center">
              {{ useAppConfig().htbb.tenSite }}
            </p>
            <h1 class="section-title mt-3 text-3xl text-highlighted sm:text-4xl">
              Kích hoạt tài khoản
            </h1>
            <p class="mt-3 text-muted">
              Tài khoản:
              <USkeleton v-if="!data && !error" class="inline-block h-5 w-24 align-middle" />
              <strong v-else class="text-highlighted">{{ data?.username }}</strong>
            </p>

            <UAlert
              v-if="error && (error as any).statusCode !== 401"
              class="mt-6 text-left"
              color="error"
              variant="subtle"
              icon="i-lucide-circle-alert"
              title="Chưa tải được trạng thái kích hoạt"
              :description="loiApi(error)"
              :actions="[{ label: 'Thử lại', color: 'neutral', variant: 'outline', onClick: () => refresh() }]"
            />

            <template v-if="data">
              <div class="mt-8">
                <UBadge v-if="data.activated" color="success" variant="subtle" size="xl" icon="i-lucide-badge-check">
                  Đã kích hoạt
                </UBadge>
                <UBadge v-else color="warning" variant="subtle" size="xl" icon="i-lucide-clock">
                  Chưa kích hoạt
                </UBadge>
              </div>
              <p v-if="data.activated" class="mt-4 text-muted">
                Tài khoản của bạn đã sẵn sàng để đăng nhập vào game.
              </p>
              <template v-else>
                <p v-if="tt && !tt.kichHoat.chanDangNhap" class="mt-4 text-sm text-muted">
                  Bạn <strong class="text-highlighted">vẫn đăng nhập và chơi bình thường</strong>.
                  <template v-if="tt.kichHoat.chucNang.length">
                    Chỉ tạm khoá: {{ tt.kichHoat.chucNang.map(c => c.ten).join(', ') }}.
                  </template>
                  Kích hoạt (miễn phí) do quản trị viên cấp — liên hệ hỗ trợ.
                  <template v-if="tt.kichHoat.thanhVienLaKichHoat">
                    Thành viên HTBB cũng được tính là đã kích hoạt.
                  </template>
                </p>
                <p v-else class="mt-4 text-sm text-muted">
                  Kích hoạt (miễn phí) do quản trị viên cấp: bấm <strong class="text-highlighted">Liên hệ hỗ trợ</strong> và gửi tên tài khoản (không gửi mật khẩu).
                </p>
              </template>
              <p v-if="tv?.coBang" class="mt-4 text-sm">
                Thành viên HTBB:
                <strong v-if="tv.trangThai?.laThanhVien" class="text-success">đang hiệu lực</strong>
                <NuxtLink v-else to="/thanh-vien" class="lien-ket">chưa có — xem quyền lợi</NuxtLink>
              </p>
            </template>

            <div class="mt-10 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <UButton v-if="choChoi" to="/download" icon="i-lucide-download" size="lg">
                Tải game
              </UButton>
              <UButton v-if="!data?.activated" :to="hoTro" :target="hoTro.startsWith('https://') ? '_blank' : undefined" icon="i-lucide-life-buoy" size="lg" :color="choChoi ? 'neutral' : undefined" :variant="choChoi ? 'outline' : undefined">
                Liên hệ hỗ trợ
              </UButton>
              <UButton to="/account" color="neutral" variant="outline" size="lg" icon="i-lucide-user-round">
                Tài khoản của tôi
              </UButton>
            </div>
          </div>
        </FxTiltCard>
      </FxReveal>
    </UContainer>
  </section>
</template>
