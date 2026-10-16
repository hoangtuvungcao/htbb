<script setup lang="ts">
useSeoMeta({
  title: 'Quên mật khẩu',
  description: 'Lấy lại mật khẩu tài khoản KPAH Pokémon qua email đã đăng ký.',
})

const api = useApi()

const form = reactive({ username: '', email: '' })
const loading = ref(false)
const errors = ref<string[]>([])
/** Câu trả lời của server sau khi gửi (luôn giống nhau dù tài khoản có khớp hay không). */
const thongBao = ref('')

// Bước 2: nhập mã trong email + mật khẩu mới
const b2 = reactive({ ma: '', password: '', confirm: '' })
const b2Loading = ref(false)
const b2Loi = ref('')
const daDoi = ref(false)

async function datLai() {
  b2Loi.value = ''
  if (b2.ma.trim() === '') {
    b2Loi.value = 'Vui lòng nhập mã xác nhận trong email.'
    return
  }
  if (!matKhauGameHopLe(b2.password)) {
    b2Loi.value = LOI_MAT_KHAU_GAME
    return
  }
  if (b2.password !== b2.confirm) {
    b2Loi.value = 'Mật khẩu nhập lại không khớp.'
    return
  }
  b2Loading.value = true
  try {
    await api('/api/auth/dat-lai-mat-khau', {
      method: 'POST',
      body: { username: form.username, ma: b2.ma, password: b2.password, confirm: b2.confirm },
    })
    daDoi.value = true
  } catch (e) {
    b2Loi.value = loiApi(e)
  } finally {
    b2Loading.value = false
  }
}

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
      Nhập tên tài khoản và email đã đăng ký. Mã xác nhận sẽ được gửi tới email của bạn — nhập mã đó cùng mật khẩu mới.
    </p>

    <template v-if="daDoi">
      <UAlert
        class="mt-6"
        color="success"
        variant="subtle"
        icon="i-lucide-shield-check"
        title="Đã đặt lại mật khẩu"
        description="Đăng nhập website và vào game bằng mật khẩu mới."
        role="status"
      />
      <div class="mt-6">
        <UButton to="/login" size="xl" block label="Đăng nhập" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
      </div>
    </template>

    <template v-else-if="thongBao">
      <UAlert
        class="mt-6"
        color="success"
        variant="subtle"
        icon="i-lucide-mail-check"
        title="Đã gửi yêu cầu"
        :description="thongBao"
        role="status"
      />
      <UAlert v-if="b2Loi" class="mt-4" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="b2Loi" role="alert" />
      <form class="mt-6 space-y-5" @submit.prevent="datLai">
        <UFormField label="Mã xác nhận (trong email)" name="ma" required>
          <UInput
            v-model="b2.ma"
            placeholder="VD: K7QM2XHP"
            autocomplete="one-time-code"
            autocapitalize="characters"
            :spellcheck="false"
            maxlength="8"
            size="xl"
            icon="i-lucide-mail"
            class="w-full"
            required
          />
        </UFormField>
        <UFormField label="Mật khẩu mới" name="password" required>
          <UInput v-model="b2.password" type="password" autocomplete="new-password" size="xl" icon="i-lucide-lock" class="w-full" required />
        </UFormField>
        <UFormField label="Nhập lại mật khẩu mới" name="confirm" required>
          <UInput v-model="b2.confirm" type="password" autocomplete="new-password" size="xl" icon="i-lucide-lock" class="w-full" required />
        </UFormField>
        <UButton type="submit" size="xl" block :loading="b2Loading" label="Đặt lại mật khẩu" trailing-icon="i-lucide-key-round" :ui="{ label: 'flex-1 text-center' }" />
      </form>
      <div class="mt-4">
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
        <UButton type="submit" size="xl" block :loading="loading" label="Gửi mã xác nhận" trailing-icon="i-lucide-send" :ui="{ label: 'flex-1 text-center' }" />
      </form>

      <div class="my-6 flex items-center gap-3 text-xs tracking-[0.2em] text-dimmed">
        <USeparator class="flex-1" /> ĐÃ NHỚ MẬT KHẨU? <USeparator class="flex-1" />
      </div>
      <UButton to="/login" color="neutral" variant="outline" size="xl" block>
        Đăng nhập
      </UButton>
    </template>

    <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
      Một tài khoản · Kết nối cùng bạn bè
    </p>
  </SiteAuthShell>
</template>
