<script setup lang="ts">
/**
 * Form đăng nhập khu quản trị, hai bước.
 *
 *  Bước 1: tên tài khoản game + mật khẩu  -> POST /api/admin/auth/login
 *  Bước 2: mã 6 số / mã dự phòng          -> POST /api/admin/auth/verify-2fa
 *
 * Token chờ mã ('cho2fa', 5 phút) chỉ nằm trong bộ nhớ của trang: tải lại trang
 * là phải gõ lại mật khẩu — cố ý: trạng thái "đã đúng mật khẩu, chờ mã" càng
 * ngắn thì càng ít cơ hội cho người ngồi vào máy sau.
 */
import type { WebUser } from '~/composables/useAuth'

interface Cho2fa { pending: string, username: string, conLai: number, het: number }

const auth = useAdminAuth()
const api = useAdminApi()
const toast = useToast()

const { data: trangThai } = useAsyncData('admin-auth-status', () => api<{ coAdmin: boolean }>('/api/admin/auth/status'), { server: false })

const form = reactive({ username: '', password: '' })
const ma = ref('')
const hienMatKhau = ref(false)
const dangGui = ref(false)
const loi = ref<string[]>([])
const cho = ref<Cho2fa | null>(null)

// Đồng hồ đếm ngược của phiên chờ mã — hết giờ thì về bước một.
const bayGio = ref(Date.now())
const { pause, resume } = useIntervalFn(() => {
  bayGio.value = Date.now()
  if (cho.value && bayGio.value >= cho.value.het) {
    cho.value = null
    loi.value = ['Phiên chờ mã hai bước đã hết hạn. Hãy đăng nhập lại.']
  }
}, 1000, { immediate: false })
const conGiay = computed(() => (cho.value ? Math.max(0, Math.ceil((cho.value.het - bayGio.value) / 1000)) : 0))
const dongHo = computed(() => `${Math.floor(conGiay.value / 60)}:${String(conGiay.value % 60).padStart(2, '0')}`)
watch(cho, v => (v ? resume() : pause()))

function vao(res: { token: string, admin: WebUser }) {
  cho.value = null
  auth.setSession(res.token, res.admin)
  toast.add({ title: `Chào ${res.admin.username}, đã vào khu quản trị.`, color: 'success', icon: 'i-lucide-shield-check' })
}

async function dangNhap() {
  loi.value = []
  dangGui.value = true
  try {
    const res = await api<{ token: string, admin: WebUser } | { need2fa: true, pending: string, username: string, conLai: number }>(
      '/api/admin/auth/login',
      { method: 'POST', body: { username: form.username, password: form.password } },
    )
    if ('need2fa' in res) {
      form.password = ''
      ma.value = ''
      cho.value = { pending: res.pending, username: res.username, conLai: res.conLai, het: Date.now() + 295_000 }
      bayGio.value = Date.now()
    } else {
      vao(res)
    }
  } catch (e) {
    loi.value = [loiApi(e)]
  } finally {
    dangGui.value = false
  }
}

async function xacMinh() {
  if (!cho.value) return
  loi.value = []
  dangGui.value = true
  try {
    const res = await api<{ token: string, admin: WebUser }>('/api/admin/auth/verify-2fa', {
      method: 'POST',
      body: { pending: cho.value.pending, code: ma.value },
    })
    vao(res)
  } catch (e: any) {
    loi.value = [loiApi(e)]
    if (e?.data?.data?.code === 'restart') cho.value = null
    ma.value = ''
  } finally {
    dangGui.value = false
  }
}

/** "Đăng nhập bằng tài khoản khác" ở bước hai. */
function huy2fa() {
  cho.value = null
  loi.value = []
  form.password = ''
}
</script>

<template>
  <FxTiltCard :max="4" :scale="1" class="rounded-3xl">
    <div class="glass ring-gilded relative overflow-hidden rounded-3xl p-7 shadow-2xl shadow-black/50 sm:p-9">
      <div class="pointer-events-none absolute -right-24 -top-24 size-64 rounded-full bg-gold-400/10 blur-3xl" aria-hidden="true" />
      <div class="pointer-events-none absolute -bottom-28 -left-20 size-64 rounded-full bg-jade-400/10 blur-3xl" aria-hidden="true" />

      <div class="relative [transform:translateZ(30px)]">
        <div class="mb-6 flex items-center justify-between">
          <div class="grid size-14 place-items-center rounded-2xl border border-primary/30 bg-primary/10 text-primary shadow-[0_0_30px_rgba(249,192,42,.25)]">
            <UIcon :name="cho ? 'i-lucide-key-round' : 'i-lucide-shield'" class="size-7" />
          </div>
          <UBadge v-if="cho" color="neutral" variant="subtle" icon="i-lucide-timer" class="tabular-nums">
            {{ dongHo }}
          </UBadge>
        </div>

        <!-- ================= Bước HAI: mã 6 số ================= -->
        <template v-if="cho">
          <p class="eyebrow">
            Bước 2 / 2
          </p>
          <h1 class="section-title mt-3 text-3xl text-highlighted">
            Mã hai bước
          </h1>
          <p class="mt-2 text-sm text-muted">
            Mở ứng dụng xác thực và gõ mã 6 số của <strong class="text-highlighted">{{ cho.username }}</strong>.
          </p>

          <UAlert v-if="loi.length" class="mt-5" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="loi.join(' ')" role="alert" />

          <form class="mt-6 space-y-5" autocomplete="off" @submit.prevent="xacMinh">
            <UFormField label="Mã 6 số" name="ma2b" required>
              <!-- inputmode=numeric để điện thoại mở sẵn bàn số. Không dùng type=number:
                   nó cắt mất số 0 ở đầu và thêm mũi tên tăng giảm. Mã dự phòng cũng gõ vào đây. -->
              <UInput
                v-model="ma"
                inputmode="numeric"
                autocomplete="one-time-code"
                maxlength="12"
                placeholder="123456"
                size="xl"
                icon="i-lucide-hash"
                class="w-full"
                :ui="{ base: 'font-mono tracking-[0.35em] text-lg' }"
                autofocus
                required
              />
            </UFormField>
            <UButton type="submit" size="xl" block :loading="dangGui" icon="i-lucide-log-in">
              Vào
            </UButton>
          </form>

          <p class="mt-5 text-sm text-muted">
            Mất điện thoại? Gõ một trong 8 <strong class="text-default">mã dự phòng</strong> vào ô trên
            ({{ so(cho.conLai) }} mã còn lại) — mỗi mã dùng đúng một lần.
          </p>
          <UAlert v-if="cho.conLai === 0" class="mt-3" color="error" variant="subtle" icon="i-lucide-triangle-alert" title="Đã hết mã dự phòng.">
            <template #description>
              Vào máy chủ bằng SSH và chạy
              <code class="break-all">DELETE FROM web_admin_2fa WHERE admin_user='{{ cho.username }}';</code>
              rồi ghi danh lại.
            </template>
          </UAlert>
          <UButton class="mt-4 px-0" variant="link" color="neutral" icon="i-lucide-arrow-left" @click="huy2fa">
            Đăng nhập bằng tài khoản khác
          </UButton>
        </template>

        <!-- ================= Bước MỘT: mật khẩu ================= -->
        <template v-else>
          <p class="eyebrow">
            Khu quản trị
          </p>
          <h1 class="section-title mt-3 text-3xl text-highlighted">
            Đăng nhập <em class="text-gilded">quản trị</em>
          </h1>
          <p class="mt-2 text-sm text-muted">
            Đăng nhập bằng tài khoản game có quyền admin.
          </p>

          <UAlert v-if="loi.length" class="mt-5" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="loi.join(' ')" role="alert" />

          <UAlert v-if="trangThai && !trangThai.coAdmin" class="mt-5" color="warning" variant="subtle" icon="i-lucide-lock" title="Chưa ai được cấp quyền.">
            <template #description>
              <p>Đặt biến môi trường cho dịch vụ web trên máy chủ:</p>
              <p class="mt-1">
                <code>KPAH_ADMIN_ACCOUNTS=tenTaiKhoanCuaBan</code>
              </p>
              <p class="mt-1">
                Đó là <em>tên tài khoản đăng nhập</em> (cột <code>team_user.username</code>), không phải tên nhân vật.
                Nhiều người thì ngăn nhau bằng dấu phẩy.
              </p>
            </template>
          </UAlert>

          <form class="mt-6 space-y-5" autocomplete="off" @submit.prevent="dangNhap">
            <UFormField label="Tên tài khoản" name="username" required>
              <UInput
                v-model="form.username"
                autocomplete="username"
                autocapitalize="none"
                :spellcheck="false"
                size="xl"
                icon="i-lucide-user-round"
                class="w-full"
                autofocus
                required
              />
            </UFormField>
            <UFormField label="Mật khẩu" name="password" required>
              <UInput
                v-model="form.password"
                :type="hienMatKhau ? 'text' : 'password'"
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
                    :icon="hienMatKhau ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                    :aria-label="hienMatKhau ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'"
                    @click="hienMatKhau = !hienMatKhau"
                  />
                </template>
              </UInput>
            </UFormField>
            <UButton type="submit" size="xl" block :loading="dangGui" trailing-icon="i-lucide-arrow-right">
              Đăng nhập
            </UButton>
          </form>
        </template>

        <div class="mt-7 flex items-center justify-between border-t border-default/60 pt-4 text-xs text-dimmed">
          <NuxtLink to="/" class="inline-flex items-center gap-1 hover:text-primary">
            <UIcon name="i-lucide-house" class="size-3.5" /> Trang chủ
          </NuxtLink>
          <span class="inline-flex items-center gap-1">
            <UIcon name="i-lucide-shield-check" class="size-3.5" /> Mọi lần đăng nhập đều được ghi lại
          </span>
        </div>
      </div>
    </div>
  </FxTiltCard>
</template>
