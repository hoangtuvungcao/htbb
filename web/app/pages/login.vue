<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Đăng nhập',
  description: `Đăng nhập ${tenSite} để quản lý tài khoản và tiếp tục chuyến phiêu lưu cùng bạn bè.`,
})

const auth = useAuth()
const api = useApi()
const toast = useToast()

const form = reactive({ username: '', password: '' })
const showPass = ref(false)
const loading = ref(false)
const errors = ref<string[]>([])
/** Đăng nhập bị từ chối vì tài khoản bị khoá -> chỉ đường khiếu nại qua Zalo. */
const biKhoa = ref(false)
const { coZalo, link: linkHoTro } = useHoTro()

async function submit() {
  errors.value = []
  biKhoa.value = false
  loading.value = true
  try {
    const res = await api<{ token: string, user: WebUser }>('/api/auth/login', {
      method: 'POST',
      body: { username: form.username, password: form.password },
    })
    auth.setSession(res.token, res.user)
    toast.add({ title: `Đăng nhập thành công. Chào mừng ${res.user.username}!`, color: 'success', icon: 'i-lucide-party-popper' })
    await navigateTo('/account')
  } catch (e) {
    errors.value = [loiApi(e)]
    biKhoa.value = (e as { data?: { code?: string } })?.data?.code === 'tai_khoan_bi_khoa'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <SiteAuthShell>
    <PokeBall wiggle class="mb-6 size-14" />
    <p class="eyebrow">
      Tiếp nối hành trình
    </p>
    <h1 id="login-title" class="section-title mt-3 text-4xl text-highlighted">
      Chào mừng<br><em class="text-gilded">bạn trở lại.</em>
    </h1>
    <p class="mt-3 text-muted">
      Đăng nhập để quản lý tài khoản và sẵn sàng trở lại thế giới Pokémon.
    </p>

    <UAlert
      v-if="errors.length"
      id="login-errors"
      class="mt-6"
      color="error"
      variant="subtle"
      icon="i-lucide-circle-alert"
      title="Đăng nhập chưa thành công"
      :description="errors.join(' ')"
      role="alert"
    />
    <SiteHoTroZalo v-if="biKhoa" kieu="the" nhan="Khiếu nại qua Zalo" class="mt-3" />

    <form class="mt-6 space-y-5" :aria-describedby="errors.length ? 'login-errors' : undefined" @submit.prevent="submit">
      <UFormField label="Tên tài khoản" name="username" required>
        <UInput
          v-model="form.username"
          placeholder="Nhập tên tài khoản"
          autocomplete="username"
          autocapitalize="none"
          :spellcheck="false"
          size="xl"
          icon="i-lucide-user-round"
          class="w-full"
          required
        />
      </UFormField>
      <UFormField label="Mật khẩu" name="password" required>
        <UInput
          v-model="form.password"
          :type="showPass ? 'text' : 'password'"
          placeholder="Nhập mật khẩu"
          autocomplete="current-password"
          size="xl"
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
              :icon="showPass ? 'i-lucide-eye-off' : 'i-lucide-eye'"
              :aria-label="showPass ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
              :aria-pressed="showPass"
              @click="showPass = !showPass"
            />
          </template>
        </UInput>
      </UFormField>
      <p class="text-end text-sm">
        <NuxtLink to="/quen-mat-khau" class="text-primary hover:underline">Quên mật khẩu?</NuxtLink>
      </p>
      <p class="text-sm text-muted">
        Cần hỗ trợ tài khoản? <ULink :to="linkHoTro" :target="coZalo ? '_blank' : undefined" class="text-primary hover:underline">{{ coZalo ? 'Nhắn Zalo hỗ trợ' : 'Xem kênh hỗ trợ' }} ↗</ULink>
      </p>
      <!-- Dùng prop label (không dùng slot) để ui.label áp vào: nút block đẩy icon sang
           mép phải bằng ms-auto, chữ phải tự giãn ra mới nằm giữa được. -->
      <UButton type="submit" size="xl" block class="btn-game" :loading="loading" label="Đăng nhập" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
    </form>

    <div class="my-6 flex items-center gap-3 text-xs tracking-[0.2em] text-dimmed">
      <USeparator class="flex-1" /> LẦN ĐẦU ĐẾN THẾ GIỚI POKÉMON? <USeparator class="flex-1" />
    </div>
    <UButton to="/register" color="neutral" variant="outline" size="xl" block>
      Tạo tài khoản mới
    </UButton>
    <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
      Một tài khoản · Kết nối cùng bạn bè
    </p>
  </SiteAuthShell>
</template>
