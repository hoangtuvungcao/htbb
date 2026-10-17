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
const hoTro = computed(() => linkHoTro(site.value))
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
              <p v-else class="mt-4 text-sm text-muted">
                Vui lòng liên hệ quản trị viên để kích hoạt tài khoản trong 30 ngày.
              </p>
            </template>

            <div class="mt-10 flex flex-col gap-3 sm:flex-row sm:justify-center">
              <UButton v-if="data?.activated" to="/download" icon="i-lucide-download" size="lg">
                Tải game
              </UButton>
              <UButton v-else :to="hoTro" icon="i-lucide-life-buoy" size="lg">
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
