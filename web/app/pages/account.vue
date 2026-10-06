<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
useSeoMeta({ title: 'Tài khoản của tôi', robots: 'noindex' })

interface AccountInfo {
  username: string
  activated: boolean
  chars: Array<{ charname: string, lastLv: number, xp: number, class: number }>
}

const api = useApi()
const { data, error, refresh } = useAsyncData<AccountInfo>('kpah-account', () => api('/api/account'), { server: false })

// Phiên hết hạn (401) -> useApi đã xoá token; đưa người chơi về trang đăng nhập.
watch(error, (e: any) => {
  if (e?.statusCode === 401) navigateTo('/login')
})

const columns = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'class', header: 'Lớp' },
  { accessorKey: 'lastLv', header: 'Cấp' },
]
</script>

<template>
  <div>
    <SitePageBanner title="Tài khoản của tôi" eyebrow="Hồ sơ anh hùng" intro="Thông tin tài khoản và các nhân vật của bạn trong thế giới Khí Phách Anh Hùng." />

    <UContainer class="max-w-6xl py-14">
      <UAlert
        v-if="error && (error as any).statusCode !== 401"
        color="error"
        variant="subtle"
        icon="i-lucide-circle-alert"
        title="Chưa tải được thông tin tài khoản"
        :description="loiApi(error)"
        :actions="[{ label: 'Thử lại', color: 'neutral', variant: 'outline', onClick: () => refresh() }]"
        class="mb-8"
      />

      <div class="grid gap-6 lg:grid-cols-[1fr_1.6fr]">
        <!-- Thông tin -->
        <FxReveal>
          <FxTiltCard :max="6" class="h-full rounded-3xl">
            <div class="relative h-full overflow-hidden rounded-3xl glass ring-gilded p-8">
              <div class="pointer-events-none absolute -right-16 -top-16 size-56 rounded-full bg-gold-400/10 blur-3xl" aria-hidden="true" />
              <p class="eyebrow">
                Thông tin
              </p>
              <div class="mt-6 flex items-center gap-4 [transform:translateZ(40px)]">
                <span class="grid size-16 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(249,192,42,.2)]" aria-hidden="true">
                  <UIcon name="i-lucide-user-round" class="size-8" />
                </span>
                <div class="min-w-0">
                  <p class="text-xs tracking-[0.2em] text-dimmed">
                    TÊN TÀI KHOẢN
                  </p>
                  <USkeleton v-if="!data && !error" class="mt-1 h-7 w-36" />
                  <p v-else class="truncate font-serif text-2xl font-bold text-highlighted">
                    {{ data?.username }}
                  </p>
                </div>
              </div>

              <USeparator class="my-6" />

              <div class="flex items-center justify-between gap-4">
                <span class="text-muted">Trạng thái</span>
                <USkeleton v-if="!data && !error" class="h-6 w-28" />
                <UBadge v-else-if="data?.activated" color="success" variant="subtle" icon="i-lucide-badge-check" size="lg">
                  Đã kích hoạt
                </UBadge>
                <UBadge v-else-if="data" color="warning" variant="subtle" icon="i-lucide-clock" size="lg">
                  Chưa kích hoạt
                </UBadge>
              </div>
              <p v-if="data && !data.activated" class="mt-4 text-sm text-muted">
                Vui lòng liên hệ quản trị viên để kích hoạt tài khoản trong 30 ngày.
              </p>

              <div class="mt-8 flex flex-wrap gap-3">
                <UButton to="/download" icon="i-lucide-download">
                  Tải game
                </UButton>
                <UButton to="/kichhoat" color="neutral" variant="outline" icon="i-lucide-badge-check">
                  Kích hoạt
                </UButton>
              </div>
            </div>
          </FxTiltCard>
        </FxReveal>

        <!-- Nhân vật -->
        <FxReveal :delay="0.1">
          <div class="h-full overflow-hidden rounded-3xl glass ring-gilded p-8">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="eyebrow">
                  Nhân vật
                </p>
                <h2 class="section-title mt-2 text-2xl text-highlighted">
                  Những bước chân giang hồ
                </h2>
              </div>
              <UBadge v-if="data?.chars.length" color="neutral" variant="outline" size="lg">
                {{ data.chars.length }} nhân vật
              </UBadge>
            </div>

            <div v-if="!data && !error" class="mt-6 space-y-3">
              <USkeleton v-for="i in 3" :key="i" class="h-10 w-full" />
            </div>
            <UTable
              v-else-if="data?.chars.length"
              :data="data.chars"
              :columns="columns"
              class="mt-6"
              :ui="{ th: 'text-xs uppercase tracking-wider text-dimmed', td: 'text-default' }"
            >
              <template #charname-cell="{ row }">
                <span class="font-semibold text-highlighted">{{ row.original.charname }}</span>
              </template>
              <template #class-cell="{ row }">
                {{ CLASS_NAMES[row.original.class] ?? '—' }}
              </template>
              <template #lastLv-cell="{ row }">
                <span class="num font-semibold text-primary">{{ row.original.lastLv }}</span>
              </template>
            </UTable>
            <div v-else-if="data" class="mt-8 flex flex-col items-center gap-3 rounded-2xl border border-dashed border-default p-10 text-center">
              <UIcon name="i-lucide-swords" class="size-10 text-primary/60" />
              <p class="text-muted">
                Chưa có nhân vật. Hãy vào game và tạo nhân vật đầu tiên của bạn!
              </p>
            </div>
          </div>
        </FxReveal>
      </div>
    </UContainer>
  </div>
</template>
