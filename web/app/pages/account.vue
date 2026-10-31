<script setup lang="ts">
definePageMeta({ middleware: 'auth' })
useSeoMeta({ title: 'Tài khoản của tôi', robots: 'noindex' })

interface AccountInfo {
  username: string
  email: string | null
  activated: boolean
  chars: Array<{
    charname: string, lastLv: number, xp: number, class: number, dex: number,
    xu?: number, khoXu?: number, luong?: number, luongKhoa?: number, khoLuong?: number, khoLuongKhoa?: number,
    mapTen?: string | null, lanCuoi?: string | null
  }>
}

const soVn = (n: number | undefined) => (n ?? 0).toLocaleString('vi-VN')

const api = useApi()
const auth = useAuth()
const { data, error, refresh } = useAsyncData<AccountInfo>('htbb-account', () => api('/api/account'), { server: false })
// Thành viên HTBB (theo tài khoản) — docs/van-hanh.md.
const { data: tv } = useAsyncData<{ coBang: boolean, trangThai: { laThanhVien: boolean, hetHanMs: number | null } | null }>(
  'htbb-tv-cua-toi', () => api('/api/thanh-vien/cua-toi'), { server: false })
const hanTv = computed(() => {
  const h = tv.value?.trangThai?.hetHanMs
  return h === 0 ? 'vĩnh viễn' : (h && h > 0 ? new Date(h).toLocaleDateString('vi-VN') : '')
})

// Phiên hết hạn (401) -> useApi đã xoá token; đưa người chơi về trang đăng nhập.
watch(error, (e: any) => {
  if (e?.statusCode === 401) navigateTo('/login')
})

// ---- Bảo mật: đổi mật khẩu ----
const toast = useToast()
const mk = reactive({ current: '', password: '', confirm: '' })
const mkHien = ref(false)
const mkDangGui = ref(false)
const mkLoi = ref('')

async function doiMatKhau() {
  mkLoi.value = ''
  // Kiểm tra trước đúng như server (doi-mat-khau.post.ts).
  if (mk.current === '') {
    mkLoi.value = 'Vui lòng nhập mật khẩu hiện tại.'
    return
  }
  if (!matKhauGameHopLe(mk.password)) {
    mkLoi.value = LOI_MAT_KHAU_GAME
    return
  }
  if (mk.password !== mk.confirm) {
    mkLoi.value = 'Mật khẩu nhập lại không khớp.'
    return
  }
  if (mk.password === mk.current) {
    mkLoi.value = 'Mật khẩu mới phải khác mật khẩu hiện tại.'
    return
  }
  mkDangGui.value = true
  try {
    const kq = await api<{ ok: boolean, token?: string | null }>('/api/auth/doi-mat-khau', { method: 'POST', body: { ...mk } })
    // Đổi mật khẩu làm mọi token cũ hết hiệu lực: lưu token mới để phiên này không bị đăng xuất
    if (kq?.token && auth.user.value) {
      auth.setSession(kq.token, auth.user.value)
    } else {
      // Không nhận được token mới: phiên cũ đã hết hiệu lực -> đăng nhập lại bằng mật khẩu mới
      auth.clear()
      toast.add({ title: 'Đã đổi mật khẩu.', description: 'Hãy đăng nhập lại bằng mật khẩu mới.', color: 'success', icon: 'i-lucide-shield-check' })
      await navigateTo('/login')
      return
    }
    mk.current = ''
    mk.password = ''
    mk.confirm = ''
    toast.add({ title: 'Đã đổi mật khẩu.', description: 'Lần sau vào game hãy dùng mật khẩu mới.', color: 'success', icon: 'i-lucide-shield-check' })
  } catch (e) {
    mkLoi.value = loiApi(e)
  } finally {
    mkDangGui.value = false
  }
}

// ---- Bảo mật: thêm / đổi email ----
const em = reactive({ email: '', current: '' })
const emHien = ref(false)
const emDangGui = ref(false)
const emLoi = ref('')

async function doiEmail() {
  emLoi.value = ''
  const email = em.email.trim()
  if (email === '') {
    emLoi.value = 'Vui lòng nhập email.'
    return
  }
  if (!dangKyEmailHopLe(email)) {
    emLoi.value = 'Email không hợp lệ.'
    return
  }
  if (em.current === '') {
    emLoi.value = 'Vui lòng nhập mật khẩu hiện tại.'
    return
  }
  emDangGui.value = true
  try {
    const res = await api<{ email: string }>('/api/auth/doi-email', { method: 'POST', body: { email, current: em.current } })
    if (data.value) data.value.email = res.email
    em.email = ''
    em.current = ''
    toast.add({ title: 'Đã lưu email.', description: res.email, color: 'success', icon: 'i-lucide-mail-check' })
  } catch (e) {
    emLoi.value = loiApi(e)
  } finally {
    emDangGui.value = false
  }
}

const columns = [
  { accessorKey: 'charname', header: 'Tên' },
  { accessorKey: 'class', header: 'Pokémon' },
  { accessorKey: 'lastLv', header: 'Cấp' },
  { accessorKey: 'xu', header: 'Xu' },
  { accessorKey: 'luong', header: 'Lượng' },
]
</script>

<template>
  <div>
    <SitePageBanner title="Tài khoản của tôi" eyebrow="Hồ sơ của bạn" :intro="`Thông tin tài khoản và các Pokémon của bạn trong thế giới ${useAppConfig().htbb.tenSite}.`" />

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

      <!-- grid-cols-1 (= minmax(0,1fr)): không có thì cột ngầm định giãn theo bảng Pokémon, tràn ngang trên điện thoại -->
      <div class="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.6fr)]">
        <!-- Thông tin -->
        <FxReveal>
          <FxTiltCard :max="6" class="h-full rounded-3xl">
            <div class="relative h-full overflow-hidden rounded-3xl glass ring-gilded p-5 sm:p-8">
              <div class="pointer-events-none absolute -right-16 -top-16 size-56 rounded-full bg-pikachu-400/10 blur-3xl" aria-hidden="true" />
              <p class="eyebrow">
                Thông tin
              </p>
              <div class="mt-6 flex items-center gap-4 [transform:translateZ(40px)]">
                <span class="grid size-16 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(10,26,51,.14)]" aria-hidden="true">
                  <UIcon name="i-lucide-user-round" class="size-8" />
                </span>
                <div class="min-w-0">
                  <p class="text-xs tracking-[0.2em] text-dimmed">
                    TÊN TÀI KHOẢN
                  </p>
                  <USkeleton v-if="!data && !error" class="mt-1 h-7 w-36" />
                  <p v-else class="truncate font-display text-2xl font-bold text-highlighted">
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
                Kích hoạt (miễn phí) do quản trị viên cấp — xem cách làm ở trang <NuxtLink to="/kichhoat" class="lien-ket">Kích hoạt</NuxtLink>.
              </p>
              <div v-if="tv?.coBang" class="mt-4 flex items-center justify-between gap-4">
                <span class="text-muted">Thành viên HTBB</span>
                <UBadge v-if="tv.trangThai?.laThanhVien" color="success" variant="subtle" icon="i-lucide-badge-check" size="lg" :label="`Hạn ${hanTv}`" />
                <UButton v-else to="/thanh-vien" size="sm" color="neutral" variant="outline" icon="i-lucide-badge-plus" label="Xem / mua" />
              </div>

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
          <div class="h-full overflow-hidden rounded-3xl glass ring-gilded p-5 sm:p-8">
            <div class="flex items-center justify-between gap-4">
              <div>
                <p class="eyebrow">
                  Đội hình
                </p>
                <h2 class="section-title mt-2 text-2xl text-highlighted">
                  Những Pokémon của bạn
                </h2>
              </div>
              <UBadge v-if="data?.chars.length" color="neutral" variant="outline" size="lg">
                {{ data.chars.length }} Pokémon
              </UBadge>
            </div>

            <div v-if="!data && !error" class="mt-6 space-y-3">
              <USkeleton v-for="i in 3" :key="i" class="h-10 w-full" />
            </div>
            <!-- Điện thoại: mỗi Pokémon một thẻ (bảng 6 cột không vừa 360 px) -->
            <ul v-else-if="data?.chars.length" class="mt-6 space-y-3 md:hidden">
              <li v-for="c in data.chars" :key="c.charname" class="rounded-2xl border border-default/60 p-4">
                <div class="flex items-center gap-3">
                  <PokeAvatar :dex="dexHienTai(c.class, c.lastLv, c.dex)" class="size-12 shrink-0" />
                  <div class="min-w-0 flex-1">
                    <p class="truncate font-semibold text-highlighted">{{ c.charname }}</p>
                    <p class="truncate text-sm text-muted">{{ speciesName(c.class, c.lastLv, c.dex) }}</p>
                  </div>
                  <span class="shrink-0 text-sm text-muted">Cấp <span class="num font-semibold text-primary">{{ c.lastLv }}</span></span>
                </div>
                <dl class="mt-3 grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <div>
                    <dt class="text-xs text-dimmed">Xu</dt>
                    <dd class="tabular-nums">{{ soVn(c.xu) }}<span v-if="c.khoXu" class="block text-xs text-muted">kho {{ soVn(c.khoXu) }}</span></dd>
                  </div>
                  <div>
                    <dt class="text-xs text-dimmed">Lượng</dt>
                    <dd class="tabular-nums">{{ soVn(c.luong) }}<span class="block text-xs text-muted">khoá {{ soVn(c.luongKhoa) }}<template v-if="c.khoLuong || c.khoLuongKhoa"> · kho {{ soVn(c.khoLuong) }} / {{ soVn(c.khoLuongKhoa) }}</template></span></dd>
                  </div>
                </dl>
              </li>
            </ul>
            <UTable
              v-if="data?.chars.length"
              :data="data.chars"
              :columns="columns"
              class="mt-6 hidden md:block"
              :ui="{ th: 'text-xs uppercase tracking-wider text-dimmed', td: 'text-default' }"
            >
              <template #charname-cell="{ row }">
                <span class="font-semibold text-highlighted">{{ row.original.charname }}</span>
              </template>
              <template #class-cell="{ row }">
                <span class="inline-flex items-center gap-2">
                  <PokeAvatar :dex="dexHienTai(row.original.class, row.original.lastLv, row.original.dex)" class="size-10" />
                  {{ speciesName(row.original.class, row.original.lastLv, row.original.dex) }}
                </span>
              </template>
              <template #lastLv-cell="{ row }">
                <span class="num font-semibold text-primary">{{ row.original.lastLv }}</span>
              </template>
              <template #xu-cell="{ row }">
                <span class="num">{{ soVn(row.original.xu) }}</span>
                <span v-if="row.original.khoXu" class="block text-xs text-muted">kho {{ soVn(row.original.khoXu) }}</span>
              </template>
              <template #luong-cell="{ row }">
                <span class="num">{{ soVn(row.original.luong) }}</span>
                <span class="block text-xs text-muted">khoá {{ soVn(row.original.luongKhoa) }}<template v-if="row.original.khoLuong || row.original.khoLuongKhoa"> · kho {{ soVn(row.original.khoLuong) }} / {{ soVn(row.original.khoLuongKhoa) }}</template></span>
              </template>
            </UTable>
            <div v-else-if="data" class="mt-8 flex flex-col items-center gap-3 rounded-2xl border border-dashed border-default p-10 text-center">
              <UIcon name="i-lucide-paw-print" class="size-10 text-primary/60" />
              <p class="text-muted">
                Chưa có Pokémon nào. Hãy vào game và chọn Pokémon khởi đầu của bạn!
              </p>
            </div>
          </div>
        </FxReveal>
      </div>

      <!-- Đổi lượng → lượng khoá / xu (game đổi trên RAM; xem components/tai-khoan/DoiLuong.vue) -->
      <FxReveal v-if="data?.chars.length" :delay="0.12">
        <TaiKhoanDoiLuong :chars="data.chars" @da-doi="refresh()" />
      </FxReveal>

      <!-- Bảo mật -->
      <FxReveal :delay="0.15">
        <div class="mt-6 overflow-hidden rounded-3xl glass ring-gilded p-5 sm:p-8">
          <p class="eyebrow">
            Bảo mật
          </p>
          <h2 class="section-title mt-2 text-2xl text-highlighted">
            Giữ tài khoản an toàn
          </h2>

          <div class="mt-6 grid gap-8 lg:grid-cols-2">
            <!-- Đổi mật khẩu -->
            <form class="space-y-4" aria-labelledby="doi-mk-title" @submit.prevent="doiMatKhau">
              <h3 id="doi-mk-title" class="flex items-center gap-2 font-semibold text-highlighted">
                <UIcon name="i-lucide-lock-keyhole" class="size-5 text-primary" /> Đổi mật khẩu
              </h3>
              <UAlert
                v-if="mkLoi"
                color="error"
                variant="subtle"
                icon="i-lucide-circle-alert"
                :description="mkLoi"
                role="alert"
              />
              <UFormField label="Mật khẩu hiện tại" name="current" required>
                <UInput
                  v-model="mk.current"
                  :type="mkHien ? 'text' : 'password'"
                  autocomplete="current-password"
                  size="lg"
                  icon="i-lucide-lock-keyhole"
                  class="w-full"
                  required
                />
              </UFormField>
              <UFormField label="Mật khẩu mới" name="password" required :help="`${PASSWORD_MIN}–${PASSWORD_MAX} ký tự, chỉ gồm chữ cái không dấu và số (game chỉ nhận như vậy).`">
                <UInput
                  v-model="mk.password"
                  :type="mkHien ? 'text' : 'password'"
                  autocomplete="new-password"
                  :minlength="PASSWORD_MIN"
                  :maxlength="PASSWORD_MAX"
                  pattern="[A-Za-z0-9]+"
                  size="lg"
                  icon="i-lucide-key-round"
                  class="w-full"
                  required
                  :ui="{ trailing: 'pe-1' }"
                >
                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="mkHien ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      :aria-label="mkHien ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      :aria-pressed="mkHien"
                      @click="mkHien = !mkHien"
                    />
                  </template>
                </UInput>
              </UFormField>
              <UFormField label="Nhập lại mật khẩu mới" name="confirm" required>
                <UInput
                  v-model="mk.confirm"
                  :type="mkHien ? 'text' : 'password'"
                  autocomplete="new-password"
                  :minlength="PASSWORD_MIN"
                  :maxlength="PASSWORD_MAX"
                  size="lg"
                  icon="i-lucide-key-round"
                  class="w-full"
                  required
                  :color="mk.confirm && mk.confirm !== mk.password ? 'error' : undefined"
                />
              </UFormField>
              <UButton type="submit" :loading="mkDangGui" icon="i-lucide-shield-check">
                Đổi mật khẩu
              </UButton>
            </form>

            <!-- Email -->
            <form class="space-y-4" aria-labelledby="doi-email-title" @submit.prevent="doiEmail">
              <h3 id="doi-email-title" class="flex items-center gap-2 font-semibold text-highlighted">
                <UIcon name="i-lucide-mail" class="size-5 text-primary" /> {{ data?.email ? 'Đổi email' : 'Thêm email' }}
              </h3>
              <USkeleton v-if="!data && !error" class="h-6 w-56" />
              <p v-else-if="data?.email" class="text-sm text-muted">
                Email hiện tại: <strong class="break-all text-highlighted">{{ data.email }}</strong>
              </p>
              <UAlert
                v-else-if="data"
                color="warning"
                variant="subtle"
                icon="i-lucide-mail-warning"
                description="Chưa có email — thêm email để dùng được Quên mật khẩu."
              />
              <UAlert
                v-if="emLoi"
                color="error"
                variant="subtle"
                icon="i-lucide-circle-alert"
                :description="emLoi"
                role="alert"
              />
              <UFormField :label="data?.email ? 'Email mới' : 'Email'" name="email" required>
                <UInput
                  v-model="em.email"
                  type="email"
                  placeholder="email@example.com"
                  autocomplete="email"
                  autocapitalize="none"
                  :spellcheck="false"
                  maxlength="254"
                  size="lg"
                  icon="i-lucide-mail"
                  class="w-full"
                  required
                />
              </UFormField>
              <UFormField label="Mật khẩu hiện tại" name="email-current" required help="Nhập mật khẩu để xác nhận chính bạn đang đổi email.">
                <UInput
                  v-model="em.current"
                  :type="emHien ? 'text' : 'password'"
                  autocomplete="current-password"
                  size="lg"
                  icon="i-lucide-lock-keyhole"
                  class="w-full"
                  required
                  :ui="{ trailing: 'pe-1' }"
                >
                  <template #trailing>
                    <UButton
                      color="neutral"
                      variant="link"
                      size="sm"
                      :icon="emHien ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                      :aria-label="emHien ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                      :aria-pressed="emHien"
                      @click="emHien = !emHien"
                    />
                  </template>
                </UInput>
              </UFormField>
              <UButton type="submit" :loading="emDangGui" icon="i-lucide-mail-check">
                Lưu email
              </UButton>
            </form>
          </div>
        </div>
      </FxReveal>
    </UContainer>
  </div>
</template>
