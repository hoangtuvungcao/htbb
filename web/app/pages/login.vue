<script setup lang="ts">
useSeoMeta({
  title: 'Đăng nhập',
  description: 'Đăng nhập Khí Phách Anh Hùng để quản lý tài khoản và tiếp tục hành trình cùng bằng hữu.',
})

const auth = useAuth()
const api = useApi()
const toast = useToast()

const form = reactive({ username: '', password: '' })
const showPass = ref(false)
const loading = ref(false)
const errors = ref<string[]>([])

async function submit() {
  errors.value = []
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
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <SiteAuthShell>
    <div class="mb-6 grid size-14 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-2xl text-primary shadow-[0_0_30px_rgba(249,192,42,.25)]" aria-hidden="true">
      ⚔
    </div>
    <p class="eyebrow">
      Tiếp nối hành trình
    </p>
    <h1 id="login-title" class="section-title mt-3 text-4xl text-highlighted">
      Chào mừng<br><em class="text-gilded">anh hùng trở lại.</em>
    </h1>
    <p class="mt-3 text-muted">
      Đăng nhập để quản lý tài khoản và sẵn sàng trở lại giang hồ.
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
      <p class="text-sm text-muted">
        Cần hỗ trợ tài khoản? <NuxtLink to="/download#support" class="text-primary hover:underline">Xem kênh hỗ trợ ↗</NuxtLink>
      </p>
      <!-- Dùng prop label (không dùng slot) để ui.label áp vào: nút block đẩy icon sang
           mép phải bằng ms-auto, chữ phải tự giãn ra mới nằm giữa được. -->
      <UButton type="submit" size="xl" block :loading="loading" label="Đăng nhập" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
    </form>

    <div class="my-6 flex items-center gap-3 text-xs tracking-[0.2em] text-dimmed">
      <USeparator class="flex-1" /> LẦN ĐẦU ĐẾN GIANG HỒ? <USeparator class="flex-1" />
    </div>
    <UButton to="/register" color="neutral" variant="outline" size="xl" block>
      Tạo tài khoản mới
    </UButton>
    <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
      Một tài khoản · Kết nối cùng bằng hữu
    </p>
  </SiteAuthShell>
</template>
