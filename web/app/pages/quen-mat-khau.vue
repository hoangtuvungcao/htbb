<script setup lang="ts">
useSeoMeta({
  title: 'Quên mật khẩu',
  description: 'Lấy lại mật khẩu tài khoản Khí Phách Anh Hùng qua email đã đăng ký.',
})

const api = useApi()

const form = reactive({ username: '', email: '' })
const loading = ref(false)
const errors = ref<string[]>([])
/** Câu trả lời của server sau khi gửi (luôn giống nhau dù tài khoản có khớp hay không). */
const thongBao = ref('')

async function submit() {
  errors.value = []
  thongBao.value = ''
  const email = form.email.trim()
  if (form.username.trim() === '' || email === '') {
    errors.value = ['Vui lòng nhập tên tài khoản và email.']
    return
  }
  if (!dangKyEmailHopLe(email)) {
    errors.value = ['Email không hợp lệ.']
    return
  }
  loading.value = true
  try {
    const res = await api<{ message: string }>('/api/auth/quen-mat-khau', {
      method: 'POST',
      body: { username: form.username, email },
    })
    thongBao.value = res.message
  } catch (e) {
    errors.value = [loiApi(e)]
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <SiteAuthShell>
    <div class="mb-6 grid size-14 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(249,192,42,.25)]" aria-hidden="true">
      <UIcon name="i-lucide-key-round" class="size-7" />
    </div>
    <p class="eyebrow">
      Lấy lại tài khoản
    </p>
    <h1 id="forgot-title" class="section-title mt-3 text-4xl text-highlighted">
      Quên<br><em class="text-gilded">mật khẩu?</em>
    </h1>
    <p class="mt-3 text-muted">
      Nhập tên tài khoản và email đã đăng ký. Mật khẩu mới sẽ được gửi tới email của bạn.
    </p>

    <template v-if="thongBao">
      <UAlert
        class="mt-6"
        color="success"
        variant="subtle"
        icon="i-lucide-mail-check"
        title="Đã gửi yêu cầu"
        :description="thongBao"
        role="status"
      />
      <p class="mt-4 text-sm text-muted">
        Đăng nhập bằng mật khẩu mới rồi đổi sang mật khẩu của riêng bạn ở trang Tài khoản (mục Bảo mật).
      </p>
      <div class="mt-6 flex flex-col gap-3">
        <UButton to="/login" size="xl" block label="Đăng nhập" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
        <UButton color="neutral" variant="outline" size="xl" block @click="thongBao = ''">
          Gửi lại yêu cầu khác
        </UButton>
      </div>
    </template>

    <template v-else>
      <UAlert
        v-if="errors.length"
        id="forgot-errors"
        class="mt-6"
        color="error"
        variant="subtle"
        icon="i-lucide-circle-alert"
        title="Chưa gửi được yêu cầu"
        :description="errors.join(' ')"
        role="alert"
      />

      <form class="mt-6 space-y-5" :aria-describedby="errors.length ? 'forgot-errors' : undefined" @submit.prevent="submit">
        <UFormField label="Tên tài khoản" name="username" required>
          <UInput
            :model-value="form.username"
            placeholder="Nhập tên tài khoản"
            autocomplete="username"
            autocapitalize="none"
            :spellcheck="false"
            :maxlength="USERNAME_MAX"
            size="xl"
            icon="i-lucide-user-round"
            class="w-full"
            required
            @update:model-value="(v: string | number) => (form.username = String(v).toLowerCase())"
          />
        </UFormField>
        <UFormField label="Email" name="email" required help="Email bạn đã nhập khi đăng ký hoặc ở trang Tài khoản.">
          <UInput
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            autocomplete="email"
            autocapitalize="none"
            :spellcheck="false"
            maxlength="254"
            size="xl"
            icon="i-lucide-mail"
            class="w-full"
            required
          />
        </UFormField>
        <p class="text-sm text-muted">
          Tài khoản chưa có email? <NuxtLink to="/download#support" class="text-primary hover:underline">Liên hệ quản trị viên ↗</NuxtLink>
        </p>
        <UButton type="submit" size="xl" block :loading="loading" label="Gửi mật khẩu mới" trailing-icon="i-lucide-send" :ui="{ label: 'flex-1 text-center' }" />
      </form>

      <div class="my-6 flex items-center gap-3 text-xs tracking-[0.2em] text-dimmed">
        <USeparator class="flex-1" /> ĐÃ NHỚ MẬT KHẨU? <USeparator class="flex-1" />
      </div>
      <UButton to="/login" color="neutral" variant="outline" size="xl" block>
        Đăng nhập
      </UButton>
    </template>

    <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
      Một tài khoản · Kết nối cùng bằng hữu
    </p>
  </SiteAuthShell>
</template>
