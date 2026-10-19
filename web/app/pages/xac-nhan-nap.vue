<script setup lang="ts">
/**
 * Trang xác nhận nạp tiền THỦ CÔNG — dùng khi chưa cấu hình
 * SePay (hoặc muốn kiểm tra tay). Bảo vệ bằng mật khẩu chung HTBB_ADMIN_NAPTIEN_PASSWORD,
 * KHÔNG phải tài khoản admin. Chưa đổi mật khẩu mặc định -> server trả 404 (fail closed).
 *
 * Phiên: token 'naptien' sống 1 giờ, giữ trong sessionStorage (đóng tab là mất).
 * Trong khu quản trị có trang tương đương đầy đủ hơn: /admin/naptien.
 */
definePageMeta({ middleware: 'nap-bat' })
useSeoMeta({ title: 'Xác nhận nạp tiền (Admin)', robots: 'noindex, nofollow' })

interface DonCho {
  id: number
  ma_don: string
  username: string
  charname: string
  ten_goi: string
  so_tien: number
  tao_luc: string
}

const KHOA = 'htbb_naptien'
// Bản DEMO (HTBB_NAP_DEMO=1): server từ chối mọi xác nhận tay — trang chỉ hiện banner + giải thích.
const napDemo = useNapDemo()
const toast = useToast()
const token = ref<string | null>(null)
const matKhau = ref('')
const dangVao = ref(false)
const loiVao = ref('')
const dons = ref<DonCho[]>([])
const dangTai = ref(true)
const dangXacNhan = ref<number | null>(null)
const choXacNhan = ref<DonCho | null>(null)
const maGd = ref('')

function luuToken(t: string | null) {
  token.value = t
  try {
    if (t) sessionStorage.setItem(KHOA, t)
    else sessionStorage.removeItem(KHOA)
  } catch { /* chế độ riêng tư chặn storage */ }
}

function goi<T>(url: string, opts: Parameters<typeof $fetch>[1] = {}) {
  const h = new Headers(opts.headers as HeadersInit)
  if (token.value) h.set('Authorization', `Bearer ${token.value}`)
  return $fetch<T>(url, { ...opts, headers: h } as any) as Promise<T>
}

async function taiDanhSach() {
  dangTai.value = true
  try {
    const r = await goi<{ dons: DonCho[] }>('/api/xac-nhan-nap/don')
    dons.value = r.dons
  } catch (e: any) {
    if (e?.statusCode === 404) {
      // Chưa đặt mật khẩu thật: trang coi như không tồn tại.
      showError({ statusCode: 404, statusMessage: 'Not Found' })
      return
    }
    if (e?.statusCode === 401) {
      luuToken(null)
    } else {
      toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
    }
  } finally {
    dangTai.value = false
  }
}

async function dangNhap() {
  loiVao.value = ''
  dangVao.value = true
  try {
    const r = await $fetch<{ token: string }>('/api/xac-nhan-nap/login', { method: 'POST', body: { password: matKhau.value } })
    luuToken(r.token)
    matKhau.value = ''
    await taiDanhSach()
  } catch (e: any) {
    if (e?.statusCode === 404) {
      showError({ statusCode: 404, statusMessage: 'Not Found' })
      return
    }
    loiVao.value = loiApi(e)
  } finally {
    dangVao.value = false
  }
}

function dangXuat() {
  luuToken(null)
  dons.value = []
}

async function xacNhan(d: DonCho) {
  choXacNhan.value = null
  dangXacNhan.value = d.id
  try {
    const r = await goi<{ message: string }>('/api/xac-nhan-nap/xac-nhan', { method: 'POST', body: { id: d.id, ma_gd: maGd.value.trim() } })
    maGd.value = ''
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
  } catch (e: any) {
    if (e?.statusCode === 401) luuToken(null)
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangXacNhan.value = null
    if (token.value) await taiDanhSach()
  }
}

const columns = [
  { accessorKey: 'id', header: '#' },
  { accessorKey: 'username', header: 'Tài khoản' },
  { accessorKey: 'charname', header: 'Pokémon' },
  { accessorKey: 'ten_goi', header: 'Gói' },
  { accessorKey: 'so_tien', header: 'Số tiền' },
  { accessorKey: 'ma_don', header: 'Mã đơn' },
  { accessorKey: 'tao_luc', header: 'Thời gian' },
  { id: 'hanh_dong', header: '' },
]

onMounted(async () => {
  if ((await napDemo.tai()).demo) {
    dangTai.value = false
    return
  }
  try {
    token.value = sessionStorage.getItem(KHOA)
  } catch { token.value = null }
  // Gọi cả khi chưa có token: server trả 404 nếu trang bị khoá, 401 nếu cần mật khẩu.
  taiDanhSach()
})
</script>

<template>
  <div>
    <SitePageBanner title="Xác nhận nạp tiền (Admin)" eyebrow="Quản trị nạp tiền" />

    <UContainer class="max-w-6xl py-14">
      <template v-if="napDemo.xem.value">
        <NapDemoBanner :chi-admin="napDemo.info.value?.chiAdmin" />
        <UAlert color="neutral" variant="subtle" icon="i-lucide-lock" title="Xác nhận tay bị tắt trong bản DEMO" description="Đơn demo không có tiền thật nên không có gì để xác nhận. Muốn xem màn hình thành công: /admin/naptien › Đơn DEMO › Giả lập thanh toán (chỉ hiển thị, không cộng gì)." />
      </template>
      <div v-else-if="dangTai && !dons.length" class="flex items-center justify-center gap-3 py-20 text-muted">
        <UIcon name="i-lucide-loader-circle" class="size-6 animate-spin" /> Đang tải...
      </div>

      <!-- Chưa đăng nhập -->
      <div v-else-if="!token" class="mx-auto max-w-md">
        <FxReveal>
          <div class="rounded-3xl glass ring-gilded p-8">
            <UAlert
              v-if="loiVao"
              class="mb-6"
              color="error"
              variant="subtle"
              icon="i-lucide-circle-alert"
              :title="loiVao"
              role="alert"
            />
            <form class="space-y-5" @submit.prevent="dangNhap">
              <UFormField label="Mật khẩu admin" name="password" required>
                <UInput
                  v-model="matKhau"
                  type="password"
                  autocomplete="current-password"
                  size="xl"
                  icon="i-lucide-lock-keyhole"
                  class="w-full"
                  autofocus
                  required
                />
              </UFormField>
              <UButton type="submit" size="xl" block :loading="dangVao">
                Vào trang xác nhận
              </UButton>
            </form>
          </div>
        </FxReveal>
      </div>

      <!-- Đã đăng nhập -->
      <template v-else>
        <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
          <p class="text-muted">
            {{ dons.length ? `Có ${dons.length} đơn đang chờ thanh toán.` : 'Không có đơn nào đang chờ thanh toán.' }}
          </p>
          <div class="flex gap-2">
            <UButton color="neutral" variant="outline" icon="i-lucide-refresh-cw" :loading="dangTai" @click="taiDanhSach">
              Tải lại
            </UButton>
            <UButton color="neutral" variant="ghost" icon="i-lucide-log-out" @click="dangXuat">
              Đăng xuất trang admin
            </UButton>
          </div>
        </div>

        <div v-if="dons.length" class="overflow-hidden rounded-2xl glass">
          <UTable :data="dons" :columns="columns">
            <template #so_tien-cell="{ row }">
              <span class="tabular-nums">{{ vnd(row.original.so_tien) }}</span>
            </template>
            <template #ma_don-cell="{ row }">
              <strong class="font-mono text-highlighted">{{ row.original.ma_don }}</strong>
            </template>
            <template #hanh_dong-cell="{ row }">
              <UButton
                size="sm"
                :loading="dangXacNhan === row.original.id"
                @click="choXacNhan = row.original"
              >
                Xác nhận đã nhận tiền
              </UButton>
            </template>
          </UTable>
        </div>
      </template>
    </UContainer>

    <UModal
      :open="!!choXacNhan"
      title="Xác nhận đã nhận tiền?"
      @update:open="(v: boolean) => { if (!v) choXacNhan = null }"
    >
      <template #body>
        <div v-if="choXacNhan" class="space-y-4">
          <p class="text-sm text-muted">
            Chỉ xác nhận khi đã thấy đúng <strong class="text-highlighted">{{ vnd(choXacNhan.so_tien) }}</strong> với nội dung
            <strong class="font-mono text-highlighted">{{ choXacNhan.ma_don }}</strong> trên sao kê ngân hàng.
            Sau khi xác nhận, server game tự cộng <strong class="text-highlighted">{{ choXacNhan.ten_goi }}</strong> vào
            <strong class="text-highlighted">{{ choXacNhan.charname }}</strong> khi Pokémon online.
          </p>
          <UFormField label="Mã giao dịch trên sao kê" hint="bắt buộc — một giao dịch chỉ xác nhận được một đơn">
            <UInput v-model="maGd" class="w-full" placeholder="VD FT26290123456" />
          </UFormField>
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" @click="choXacNhan = null">
            Huỷ
          </UButton>
          <UButton icon="i-lucide-check" :disabled="maGd.trim().length < 3" @click="choXacNhan && xacNhan(choXacNhan)">
            Xác nhận
          </UButton>
        </div>
      </template>
    </UModal>
  </div>
</template>
