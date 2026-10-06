<script setup lang="ts">
useSeoMeta({
  title: 'Đăng ký',
  description: 'Tạo tài khoản Khí Phách Anh Hùng và bắt đầu hành trình cùng bằng hữu.',
})

const api = useApi()
const toast = useToast()

const form = reactive({ username: '', password: '', confirm: '', email: '' })
const showPass = ref(false)
const showConfirm = ref(false)
const loading = ref(false)
const errors = ref<string[]>([])
/** Tài khoản vừa tạo thành công (hiện kết quả ngay trên trang, không chuyển trang). */
const daDangKy = ref<{ username: string, autoActivate: boolean } | null>(null)

async function submit() {
  errors.value = []
  loading.value = true
  try {
    const res = await api<{ username: string, autoActivate: boolean }>('/api/auth/register', {
      method: 'POST',
      body: { ...form },
    })
    daDangKy.value = res
    form.password = ''
    form.confirm = ''
    toast.add({ title: 'Đăng ký thành công!', description: `Tài khoản: ${res.username}`, color: 'success', icon: 'i-lucide-party-popper' })
  } catch (e: any) {
    const ds = e?.data?.data?.errors
    errors.value = Array.isArray(ds) && ds.length ? ds.map(String) : [loiApi(e)]
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <SiteAuthShell>
    <div class="mb-6 grid size-14 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(249,192,42,.25)]" aria-hidden="true">
      <UIcon name="i-lucide-scroll-text" class="size-7" />
    </div>
    <p class="eyebrow">
      Khởi đầu một huyền thoại
    </p>
    <h1 id="register-title" class="section-title mt-3 text-4xl text-highlighted">
      Gia nhập<br><em class="text-gilded">giang hồ.</em>
    </h1>
    <p class="mt-3 text-muted">
      Tạo tài khoản của bạn. Cùng bằng hữu viết nên một hành trình đáng nhớ.
    </p>

    <template v-if="daDangKy">
      <UAlert
        class="mt-6"
        color="success"
        variant="subtle"
        icon="i-lucide-circle-check-big"
        title="Đăng ký thành công!"
        role="status"
      >
        <template #description>
          <p>Tài khoản: <strong class="text-highlighted">{{ daDangKy.username }}</strong>.</p>
          <p class="mt-1">
            <template v-if="daDangKy.autoActivate">
              Tài khoản đã được kích hoạt, bạn có thể vào game ngay.
            </template>
            <template v-else>
              Vui lòng liên hệ quản trị viên để kích hoạt tài khoản trước khi vào game.
            </template>
          </p>
        </template>
      </UAlert>
      <div class="mt-6 flex flex-col gap-3">
        <UButton to="/login" size="xl" block label="Đăng nhập ngay" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
        <UButton to="/download" color="neutral" variant="outline" size="xl" block icon="i-lucide-download">
          Tải game
        </UButton>
      </div>
    </template>

    <template v-else>
      <UAlert
        v-if="errors.length"
        id="register-errors"
        class="mt-6"
        color="error"
        variant="subtle"
        icon="i-lucide-circle-alert"
        title="Chưa thể tạo tài khoản"
        role="alert"
      >
        <template #description>
          <ul class="list-disc space-y-0.5 ps-4">
            <li v-for="err in errors" :key="err">
              {{ err }}
            </li>
          </ul>
        </template>
      </UAlert>

      <form class="mt-6 space-y-5" :aria-describedby="errors.length ? 'register-errors' : undefined" @submit.prevent="submit">
        <UFormField label="Tên tài khoản" name="username" required :help="`${USERNAME_MIN}–${USERNAME_MAX} ký tự, chỉ gồm chữ thường và số.`">
          <UInput
            :model-value="form.username"
            placeholder="Tên tài khoản của bạn"
            autocomplete="username"
            autocapitalize="none"
            :spellcheck="false"
            :minlength="USERNAME_MIN"
            :maxlength="USERNAME_MAX"
            pattern="[a-z0-9]+"
            size="xl"
            icon="i-lucide-user-round"
            class="w-full"
            required
            @update:model-value="(v: string | number) => (form.username = String(v).toLowerCase())"
          />
        </UFormField>
        <UFormField label="Mật khẩu" name="password" required :help="`Sử dụng từ ${PASSWORD_MIN}–${PASSWORD_MAX} ký tự.`">
          <UInput
            v-model="form.password"
            :type="showPass ? 'text' : 'password'"
            placeholder="Tạo mật khẩu"
            autocomplete="new-password"
            :minlength="PASSWORD_MIN"
            :maxlength="PASSWORD_MAX"
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
        <UFormField label="Nhập lại mật khẩu" name="confirm" required>
          <UInput
            v-model="form.confirm"
            :type="showConfirm ? 'text' : 'password'"
            placeholder="Nhập lại mật khẩu vừa tạo"
            autocomplete="new-password"
            :minlength="PASSWORD_MIN"
            :maxlength="PASSWORD_MAX"
            size="xl"
            icon="i-lucide-lock-keyhole"
            class="w-full"
            required
            :color="form.confirm && form.confirm !== form.password ? 'error' : undefined"
            :ui="{ trailing: 'pe-1' }"
          >
            <template #trailing>
              <UButton
                color="neutral"
                variant="link"
                size="sm"
                :icon="showConfirm ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                :aria-label="showConfirm ? 'Ẩn mật khẩu nhập lại' : 'Hiện mật khẩu nhập lại'"
                :aria-pressed="showConfirm"
                @click="showConfirm = !showConfirm"
              />
            </template>
          </UInput>
        </UFormField>
        <UFormField label="Email" name="email" hint="Không bắt buộc">
          <UInput
            v-model="form.email"
            type="email"
            placeholder="email@example.com"
            autocomplete="email"
            size="xl"
            icon="i-lucide-mail"
            class="w-full"
          />
        </UFormField>
        <UButton type="submit" size="xl" block :loading="loading" label="Tạo tài khoản" trailing-icon="i-lucide-arrow-up-right" :ui="{ label: 'flex-1 text-center' }" />
      </form>

      <div class="my-6 flex items-center gap-3 text-xs tracking-[0.2em] text-dimmed">
        <USeparator class="flex-1" /> ĐÃ CÓ TÀI KHOẢN? <USeparator class="flex-1" />
      </div>
      <UButton to="/login" color="neutral" variant="outline" size="xl" block>
        Đăng nhập
      </UButton>
    </template>

    <p class="mt-6 text-center text-xs tracking-widest text-dimmed">
      Một tài khoản · Bắt đầu hành trình của bạn
    </p>
  </SiteAuthShell>
</template>
