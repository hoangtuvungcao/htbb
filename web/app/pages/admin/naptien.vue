<script setup lang="ts">
/**
 * Đơn nạp mốc (khu quản trị), cho admin đã đăng nhập bằng tài khoản game —
 * đầy đủ hơn trang /xac-nhan-nap dùng mật khẩu chung: xem mọi đơn (lọc theo trạng thái / mốc / tìm), xác nhận tay
 * đơn đang chờ. Mỗi lần xác nhận ghi nhật ký 'xac_nhan_nap_moc'.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

interface DonAdmin {
  id: number
  userID: number
  moc: number
  amount: number
  ma_don: string
  phuong_hoang: number
  trang_thai: string
  created_at: string
  paid_at: string | null
  claimed_at: string | null
  username: string | null
}
interface KetQua {
  dons: DonAdmin[]
  tong: number
  page: number
  perPage: number
  thongKe: Record<string, { so: number, tien: number }>
  sepay: boolean
}

const api = useAdminApi()
const toast = useToast()

const TAT_CA = 'tat_ca'
const trangThai = ref<string>('cho_thanh_toan')
const moc = ref<string>(TAT_CA)
const tim = ref('')
const timDaGui = ref('')
const page = ref(1)

const data = ref<KetQua | null>(null)
const dangTai = ref(false)
const loi = ref('')
const dangXacNhan = ref<number | null>(null)
const choXacNhan = ref<DonAdmin | null>(null)

const locTrangThai = [
  { label: 'Mọi trạng thái', value: TAT_CA },
  { label: 'Chờ thanh toán', value: 'cho_thanh_toan' },
  { label: 'Đã thanh toán', value: 'da_thanh_toan' },
  { label: 'Đã nhận quà', value: 'da_nhan' },
]
const locMoc = [
  { label: 'Mọi mốc', value: TAT_CA },
  ...([1, 2, 3] as const).map(m => ({ label: `Mốc ${m} · ${vnd(MOC_NAP[m].gia)}`, value: String(m) })),
]

const mauTrangThai: Record<string, 'warning' | 'success' | 'neutral'> = {
  cho_thanh_toan: 'warning',
  da_thanh_toan: 'success',
  da_nhan: 'neutral',
}

async function tai() {
  dangTai.value = true
  loi.value = ''
  try {
    data.value = await api<KetQua>('/api/admin/naptien/don', {
      query: {
        trang_thai: trangThai.value === TAT_CA ? '' : trangThai.value,
        moc: moc.value === TAT_CA ? '' : moc.value,
        q: timDaGui.value,
        page: page.value,
      },
    })
  } catch (e) {
    loi.value = loiApi(e)
  } finally {
    dangTai.value = false
  }
}

function guiTim() {
  timDaGui.value = tim.value.trim()
  if (page.value !== 1) page.value = 1
  else tai()
}

watch([trangThai, moc], () => {
  if (page.value !== 1) page.value = 1
  else tai()
})
watch(page, tai)
onMounted(tai)

async function xacNhan(d: DonAdmin) {
  choXacNhan.value = null
  dangXacNhan.value = d.id
  try {
    const r = await api<{ message: string }>('/api/admin/naptien/xac-nhan', { method: 'POST', body: { id: d.id } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangXacNhan.value = null
    await tai()
  }
}

const tk = computed(() => data.value?.thongKe ?? {})
const tienDaThu = computed(() => (tk.value.da_thanh_toan?.tien ?? 0) + (tk.value.da_nhan?.tien ?? 0))

const columns = [
  { accessorKey: 'id', header: '#' },
  { accessorKey: 'username', header: 'Tài khoản' },
  { accessorKey: 'moc', header: 'Mốc' },
  { accessorKey: 'amount', header: 'Số tiền', meta: { class: { th: 'text-right', td: 'num' } } },
  { accessorKey: 'ma_don', header: 'Mã đơn' },
  { accessorKey: 'trang_thai', header: 'Trạng thái' },
  { accessorKey: 'created_at', header: 'Tạo lúc' },
  { accessorKey: 'paid_at', header: 'Thanh toán lúc' },
  { id: 'hanh_dong', header: '' },
]
</script>

<template>
  <AdminPage title="Đơn nạp mốc">
    <template #right>
      <UButton color="neutral" variant="outline" icon="i-lucide-refresh-cw" :loading="dangTai" @click="tai">
        Tải lại
      </UButton>
    </template>

    <UAlert
      v-if="data && !data.sepay"
      color="warning"
      variant="subtle"
      icon="i-lucide-triangle-alert"
      title="Chưa cấu hình SePay"
      description="Đơn KHÔNG được xác nhận tự động. Kiểm tra tiền vào tài khoản TPBank rồi bấm xác nhận từng đơn bên dưới (đặt KPAH_SEPAY_WEBHOOK_APIKEY để bật tự động)."
    />
    <UAlert v-if="loi" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="loi" />

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <AdminStatCard
        label="Chờ thanh toán"
        :value="tk.cho_thanh_toan?.so ?? 0"
        :note="vnd(tk.cho_thanh_toan?.tien ?? 0)"
        :mau="(tk.cho_thanh_toan?.so ?? 0) > 0 ? 'warn' : ''"
        icon="i-lucide-hourglass"
      />
      <AdminStatCard label="Đã thanh toán (chưa nhận quà)" :value="tk.da_thanh_toan?.so ?? 0" :note="vnd(tk.da_thanh_toan?.tien ?? 0)" icon="i-lucide-banknote" />
      <AdminStatCard label="Đã nhận quà" :value="tk.da_nhan?.so ?? 0" :note="vnd(tk.da_nhan?.tien ?? 0)" icon="i-lucide-gift" />
      <AdminStatCard label="Tổng tiền đã thu" :value="vnd(tienDaThu)" icon="i-lucide-wallet" />
    </div>

    <UCard>
      <form class="flex flex-wrap items-end gap-3" @submit.prevent="guiTim">
        <UFormField label="Trạng thái">
          <USelect v-model="trangThai" :items="locTrangThai" class="w-48" />
        </UFormField>
        <UFormField label="Mốc">
          <USelect v-model="moc" :items="locMoc" class="w-52" />
        </UFormField>
        <UFormField label="Tìm" hint="mã đơn, tên tài khoản hoặc #id" class="min-w-64 flex-1">
          <UInput v-model="tim" icon="i-lucide-search" placeholder="KPX7F3AQ2B / hoahv / #12" class="w-full" />
        </UFormField>
        <UButton type="submit" icon="i-lucide-search">
          Lọc
        </UButton>
      </form>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <UTable :data="data?.dons ?? []" :columns="columns" :loading="dangTai" empty="Không có đơn nào khớp bộ lọc.">
        <template #username-cell="{ row }">
          <span class="font-medium text-highlighted">{{ row.original.username ?? `userID ${row.original.userID}` }}</span>
        </template>
        <template #moc-cell="{ row }">
          Mốc {{ row.original.moc }}
          <span v-if="row.original.moc === 3 && row.original.phuong_hoang >= 0" class="text-xs text-muted">
            · {{ row.original.phuong_hoang === 1 ? 'Phượng Hoàng Băng' : 'Phượng Hoàng' }}
          </span>
        </template>
        <template #amount-cell="{ row }">
          {{ vnd(row.original.amount) }}
        </template>
        <template #ma_don-cell="{ row }">
          <span class="font-mono">{{ row.original.ma_don }}</span>
        </template>
        <template #trang_thai-cell="{ row }">
          <UBadge
            :color="mauTrangThai[row.original.trang_thai] ?? 'neutral'"
            variant="subtle"
            :label="TEN_TRANG_THAI_DON[row.original.trang_thai] ?? row.original.trang_thai"
          />
        </template>
        <template #paid_at-cell="{ row }">
          <span class="text-muted">{{ row.original.paid_at ?? '—' }}</span>
        </template>
        <template #hanh_dong-cell="{ row }">
          <UButton
            v-if="row.original.trang_thai === 'cho_thanh_toan'"
            size="sm"
            :loading="dangXacNhan === row.original.id"
            @click="choXacNhan = row.original"
          >
            Xác nhận đã nhận tiền
          </UButton>
        </template>
      </UTable>
    </UCard>

    <div v-if="data && data.tong > data.perPage" class="flex justify-center">
      <UPagination v-model:page="page" :total="data.tong" :items-per-page="data.perPage" />
    </div>

    <UModal
      :open="!!choXacNhan"
      title="Xác nhận đã nhận tiền?"
      @update:open="(v: boolean) => { if (!v) choXacNhan = null }"
    >
      <template #body>
        <p v-if="choXacNhan" class="text-sm text-muted">
          Chỉ xác nhận khi đã thấy đúng <strong class="text-highlighted">{{ vnd(choXacNhan.amount) }}</strong> với nội dung
          <strong class="font-mono text-highlighted">{{ choXacNhan.ma_don }}</strong> trong tài khoản TPBank.
          Sau khi xác nhận, tài khoản <strong class="text-highlighted">{{ choXacNhan.username ?? `userID ${choXacNhan.userID}` }}</strong>
          nhận được quà mốc {{ choXacNhan.moc }} tại NPC Tổng Quản. Thao tác được ghi vào nhật ký.
        </p>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" @click="choXacNhan = null">
            Huỷ
          </UButton>
          <UButton icon="i-lucide-check" @click="choXacNhan && xacNhan(choXacNhan)">
            Xác nhận
          </UButton>
        </div>
      </template>
    </UModal>
  </AdminPage>
</template>
