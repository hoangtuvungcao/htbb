<script setup lang="ts">
/**
 * Tạo NHIỀU tài khoản đăng nhập cùng một lượt.
 * Mỗi dòng `tenTaiKhoan` hoặc `tenTaiKhoan:matKhau` (trống mật khẩu thì tự sinh).
 *
 * Mật khẩu chỉ hiện ĐÚNG MỘT LẦN ở đây — cột team_user.password chỉ lưu bản băm một
 * chiều qua PASSWORD(), tải lại trang là mất, không xem lại được nữa.
 */
import type { TableColumn } from '@nuxt/ui'

definePageMeta({ layout: 'admin', middleware: 'admin' })

/** Phải khớp TK_TAO_HANG_LOAT_TOI_DA ở server (server vẫn kiểm lại). */
const TOI_DA = 200

interface Hang { dong: number, username: string, matkhau: string, trangthai: string, loi: boolean }
interface KetQua { ketQua: Hang[], tongDong: number, soTao: number }

const api = useAdminApi()
// Vai trò Chỉ xem thấy trang (đọc được) nhưng không gửi được: khoá nút thay vì để bấm rồi nhận 403.
const me = useAdminMe()
const duocGhi = computed(() => me.duoc('/api/admin/tao-hang-loat'))
const toast = useToast()

const danhSach = ref('')
const tuKichHoat = ref(true)
const dangTao = ref(false)
const kq = ref<KetQua | null>(null)

const daTao = computed(() => (kq.value?.ketQua ?? []).filter(h => !h.loi))
const vanBanDaTao = computed(() => daTao.value.map(h => `${h.username}:${h.matkhau}`).join('\n'))

async function tao() {
  dangTao.value = true
  try {
    kq.value = await api<KetQua>('/api/admin/tao-hang-loat', {
      method: 'POST',
      body: { danh_sach: danhSach.value, tu_kich_hoat: tuKichHoat.value },
    })
    toast.add({
      title: `Đã tạo ${kq.value.soTao}/${kq.value.tongDong} tài khoản`,
      color: kq.value.soTao === kq.value.tongDong ? 'success' : 'warning',
      icon: 'i-lucide-check',
    })
  } catch (e) {
    toast.add({ title: 'Chưa tạo', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangTao.value = false
  }
}

async function saoChep() {
  try {
    await navigator.clipboard.writeText(vanBanDaTao.value)
    toast.add({ title: 'Đã sao chép danh sách', color: 'success', icon: 'i-lucide-copy' })
  } catch {
    toast.add({ title: 'Trình duyệt chặn sao chép — bấm vào ô rồi Ctrl+C.', color: 'warning' })
  }
}

const cot: TableColumn<Hang>[] = [
  { accessorKey: 'dong', header: 'Dòng', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'username', header: 'Tài khoản' },
  { accessorKey: 'matkhau', header: 'Mật khẩu' },
  { accessorKey: 'trangthai', header: 'Trạng thái' },
]
</script>

<template>
  <AdminPage title="Tạo tài khoản hàng loạt">
    <UAlert
      v-if="!duocGhi"
      color="neutral"
      variant="subtle"
      icon="i-lucide-eye"
      title="Chế độ chỉ xem"
      description="Vai trò của bạn xem được trang này nhưng không gửi được lệnh — chỉ Siêu quản trị thao tác."
    />
    <p class="text-sm text-muted">
      Mỗi dòng một tài khoản, tối đa {{ TOI_DA }} dòng một lượt.
    </p>

    <UCard>
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Danh sách cần tạo
        </h3>
      </template>
      <form class="space-y-4" @submit.prevent="tao">
        <UFormField>
          <template #label>
            Mỗi dòng: <code>tenTaiKhoan</code> hoặc <code>tenTaiKhoan:matKhau</code> — để trống mật khẩu thì tự sinh ngẫu nhiên.
            Dòng bắt đầu bằng <code>#</code> bị bỏ qua.
          </template>
          <UTextarea
            v-model="danhSach"
            :rows="10"
            autoresize
            :maxrows="20"
            class="w-full"
            :ui="{ base: 'font-mono' }"
            :placeholder="'banA\nbanB:matkhaurieng\n# dòng này là ghi chú, bỏ qua'"
          />
        </UFormField>
        <UCheckbox v-model="tuKichHoat">
          <template #label>
            Kích hoạt ngay (thêm vào <code>5h_active</code>) — bỏ chọn nếu tài khoản phải tự kích hoạt trên trang chủ.
          </template>
        </UCheckbox>
        <UButton type="submit" icon="i-lucide-user-plus" label="Tạo tài khoản" :disabled="!duocGhi" :loading="dangTao" />
      </form>
    </UCard>

    <UCard v-if="kq">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Kết quả: đã tạo {{ kq.soTao }}/{{ kq.tongDong }} tài khoản
        </h3>
      </template>

      <div v-if="daTao.length" class="mb-4 space-y-3">
        <UAlert
          color="warning"
          variant="subtle"
          icon="i-lucide-triangle-alert"
          title="Sao chép ngay danh sách dưới đây — mật khẩu chỉ hiện được lúc này."
        >
          <template #description>
            Cột <code>team_user.password</code> chỉ lưu bản băm một chiều qua <code>PASSWORD()</code>,
            tải lại trang là mất, không xem lại được nữa (chỉ có thể đặt mật khẩu MỚI ở trang Tài khoản).
          </template>
        </UAlert>
        <UFormField label="Tài khoản vừa tạo (tenTaiKhoan:matKhau)">
          <UTextarea
            :model-value="vanBanDaTao"
            readonly
            :rows="Math.min(15, Math.max(3, daTao.length))"
            class="w-full"
            :ui="{ base: 'font-mono' }"
            @focus="(e: FocusEvent) => (e.target as HTMLTextAreaElement).select()"
          />
        </UFormField>
        <UButton size="sm" color="neutral" variant="outline" icon="i-lucide-copy" label="Sao chép" @click="saoChep" />
      </div>

      <UTable :data="kq.ketQua" :columns="cot">
        <template #username-cell="{ row }">
          <strong class="text-highlighted">{{ row.original.username }}</strong>
        </template>
        <template #matkhau-cell="{ row }">
          <code v-if="row.original.matkhau" class="whitespace-nowrap">{{ row.original.matkhau }}</code>
          <span v-else class="text-dimmed">—</span>
        </template>
        <template #trangthai-cell="{ row }">
          <UBadge :color="!row.original.loi ? 'success' : (row.original.trangthai.startsWith('Lỗi') ? 'error' : 'warning')" variant="subtle" :label="row.original.trangthai" class="whitespace-normal" />
        </template>
      </UTable>
    </UCard>
  </AdminPage>
</template>
