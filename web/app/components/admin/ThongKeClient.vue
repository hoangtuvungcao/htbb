<script setup lang="ts">
/**
 * Lịch sử đăng nhập theo client (PC / Android / jar) + danh sách tài khoản từng dùng BẢN NGOÀI
 * (không phải client HTBB chính thức của mình) để soát và khoá. Đặt dưới trang "Ai đang ở đâu".
 * Dữ liệu: server/api/admin/client.get.ts (bảng tob_client_dang_nhap, server game ghi mỗi lần
 * đăng nhập thành công — data/GhiClient.java bên repo HTBB).
 */
const api = useAdminApi()

type Loai = 'pc' | 'android' | 'jar' | 'khac'
interface DongClient {
  tai_khoan: string, loai: Loai, platf: string, phien_ban: string, ban_jar: number, chinh_chu: number
  so_lan: number, lan_dau: string, lan_cuoi: string
}
interface KetQua {
  theoLoai: { loai: Loai, tatCa: number, banNgoai: number, ngay1: number, ngay7: number, ngay30: number }[]
  tongTaiKhoan: number
  jarTheoMay: { ten: string, sl: number }[]
  jarTheoBan: { ten: string, sl: number }[]
  banNgoai: DongClient[]
  ganDay: DongClient[]
  soBotAn: number
  bangChuaCo: boolean
  loiDoc: string
}

const TEN: Record<Loai, string> = { pc: 'PC', android: 'Android', jar: 'Jar (J2ME)', khac: 'Khác' }
const ICON: Record<Loai, string> = { pc: 'i-lucide-monitor', android: 'i-lucide-smartphone', jar: 'i-lucide-cpu', khac: 'i-lucide-circle-help' }

const anBot = ref(true)
const { data, status, error, refresh } = useAsyncData<KetQua>(
  'admin-client',
  () => api<KetQua>('/api/admin/client', { query: { anBot: anBot.value ? '1' : '0' } }),
  { server: false, watch: [anBot] },
)
const dangTai = computed(() => status.value === 'pending')
const loaiHienThi = computed(() => (data.value?.theoLoai ?? []).filter(r => r.loai !== 'khac' || r.tatCa > 0))

const cotPhai = { class: { th: 'text-right', td: 'text-right tabular-nums' } }
const cotTheoLoai = [
  { accessorKey: 'loai', header: 'Client' },
  { accessorKey: 'ngay1', header: '24 giờ', meta: cotPhai },
  { accessorKey: 'ngay7', header: '7 ngày', meta: cotPhai },
  { accessorKey: 'ngay30', header: '30 ngày', meta: cotPhai },
  { accessorKey: 'tatCa', header: 'Từ trước tới nay', meta: cotPhai },
  { accessorKey: 'banNgoai', header: 'Trong đó bản ngoài', meta: cotPhai },
]
const cotTaiKhoan = [
  { accessorKey: 'tai_khoan', header: 'Tài khoản' },
  { accessorKey: 'loai', header: 'Client' },
  { accessorKey: 'platf', header: 'Máy / phiên bản' },
  { accessorKey: 'so_lan', header: 'Số lần vào', meta: cotPhai },
  { accessorKey: 'lan_dau', header: 'Lần đầu' },
  { accessorKey: 'lan_cuoi', header: 'Lần cuối' },
  { id: 'khoa', header: '' },
]
const linkTaiKhoan = (tk: string) => `/admin/taikhoan?q=${encodeURIComponent(tk)}`
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-center justify-between gap-3 pt-4">
      <h2 class="text-lg font-semibold text-highlighted">
        Client người chơi
      </h2>
      <div class="flex items-center gap-3">
        <USwitch v-model="anBot" label="Ẩn bot thử tải" />
        <UButton icon="i-lucide-refresh-cw" color="neutral" variant="outline" size="sm" :loading="dangTai" @click="refresh()">
          Làm mới
        </UButton>
      </div>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="loiApi(error)" />
    <UAlert v-else-if="data?.bangChuaCo" color="warning" variant="subtle" icon="i-lucide-database">
      <template #description>
        Chưa có bảng <code>tob_client_dang_nhap</code> — server game tự tạo ở lần đăng nhập thành công đầu
        tiên sau khi chạy bản có tính năng này.
      </template>
    </UAlert>
    <UAlert v-else-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-alert" :description="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <!-- Bản ngoài: để soát và khoá -->
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <div class="space-y-1">
          <h3 class="flex items-center gap-2 font-semibold text-highlighted">
            <UIcon name="i-lucide-shield-alert" class="size-5 text-error" />
            Tài khoản dùng bản ngoài
            <UBadge :color="(data?.banNgoai.length ?? 0) > 0 ? 'error' : 'neutral'" variant="subtle">
              {{ so(data?.banNgoai.length ?? 0) }}
            </UBadge>
          </h3>
          <p class="text-sm text-muted">
            Từng đăng nhập bằng client KHÔNG phải bản HTBB chính thức của bạn (client gốc TeaMobi hoặc client lạ — không
            gửi mã máy). Bấm "Khoá / xem" để mở tài khoản và khoá. Client bị sửa tay vẫn có thể giả làm bản của
            bạn, nên đây là danh sách để soát, không phải bằng chứng tuyệt đối.
          </p>
        </div>
      </template>
      <UTable :data="data?.banNgoai ?? []" :columns="cotTaiKhoan" :loading="dangTai">
        <template #empty>
          <span class="text-muted">Chưa có ai dùng bản ngoài.</span>
        </template>
        <template #tai_khoan-cell="{ row }">
          <NuxtLink :to="linkTaiKhoan(row.original.tai_khoan)" class="whitespace-nowrap font-medium text-primary hover:underline">
            {{ row.original.tai_khoan }}
          </NuxtLink>
        </template>
        <template #loai-cell="{ row }">
          <UBadge :icon="ICON[row.original.loai] ?? ICON.khac" color="neutral" variant="subtle">
            {{ TEN[row.original.loai] ?? row.original.loai }}
          </UBadge>
        </template>
        <template #platf-cell="{ row }">
          <span class="whitespace-nowrap text-muted">{{ row.original.platf || '?' }} · client {{ row.original.phien_ban || '?' }}</span>
        </template>
        <template #lan_dau-cell="{ row }">
          <span class="whitespace-nowrap text-muted">{{ row.original.lan_dau }}</span>
        </template>
        <template #lan_cuoi-cell="{ row }">
          <span class="whitespace-nowrap">{{ row.original.lan_cuoi }}</span>
        </template>
        <template #khoa-cell="{ row }">
          <UButton :to="linkTaiKhoan(row.original.tai_khoan)" size="xs" color="error" variant="outline" icon="i-lucide-lock">
            Khoá / xem
          </UButton>
        </template>
      </UTable>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Số tài khoản đã vào game theo client
          <span class="text-sm font-normal text-muted">
            ({{ so(data?.tongTaiKhoan ?? 0) }} tài khoản; một tài khoản dùng nhiều loại được đếm ở mỗi loại{{ data?.soBotAn ? `; đã ẩn ${so(data.soBotAn)} dòng bot` : '' }})
          </span>
        </h3>
      </template>
      <UTable :data="loaiHienThi" :columns="cotTheoLoai" :loading="dangTai">
        <template #loai-cell="{ row }">
          <span class="flex items-center gap-2 font-medium">
            <UIcon :name="ICON[row.original.loai]" class="size-4 text-muted" />{{ TEN[row.original.loai] }}
          </span>
        </template>
        <template #banNgoai-cell="{ row }">
          <span :class="row.original.banNgoai > 0 ? 'font-semibold text-error' : 'text-muted'">{{ so(row.original.banNgoai) }}</span>
        </template>
      </UTable>
    </UCard>

    <UCard v-if="data?.jarTheoMay.length">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Jar — 30 ngày gần nhất
        </h3>
      </template>
      <div class="space-y-3 text-sm">
        <div class="flex flex-wrap items-center gap-1.5">
          <span class="text-muted">Theo máy:</span>
          <UBadge v-for="m in data.jarTheoMay" :key="m.ten" color="neutral" variant="subtle">
            {{ m.ten }} <span class="ms-1 tabular-nums text-highlighted">{{ m.sl }}</span>
          </UBadge>
        </div>
        <div class="flex flex-wrap items-center gap-1.5">
          <span class="text-muted">Theo bản:</span>
          <UBadge v-for="m in data.jarTheoBan" :key="m.ten" color="neutral" variant="subtle">
            {{ m.ten }} <span class="ms-1 tabular-nums text-highlighted">{{ m.sl }}</span>
          </UBadge>
        </div>
      </div>
    </UCard>

    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Đăng nhập gần đây <span class="text-sm font-normal text-muted">(200 dòng mới nhất)</span>
        </h3>
      </template>
      <UTable :data="data?.ganDay ?? []" :columns="cotTaiKhoan" :loading="dangTai">
        <template #empty>
          <span class="text-muted">Chưa có ai đăng nhập từ khi có tính năng này.</span>
        </template>
        <template #tai_khoan-cell="{ row }">
          <NuxtLink :to="linkTaiKhoan(row.original.tai_khoan)" class="whitespace-nowrap font-medium text-primary hover:underline">
            {{ row.original.tai_khoan }}
          </NuxtLink>
        </template>
        <template #loai-cell="{ row }">
          <span class="flex items-center gap-1.5 whitespace-nowrap">
            <UBadge :icon="ICON[row.original.loai] ?? ICON.khac" color="neutral" variant="subtle">
              {{ TEN[row.original.loai] ?? row.original.loai }}
            </UBadge>
            <UBadge v-if="Number(row.original.chinh_chu) === 1" color="success" variant="subtle">Bản của bạn</UBadge>
            <UBadge v-else color="error" variant="solid">Bản ngoài</UBadge>
          </span>
        </template>
        <template #platf-cell="{ row }">
          <span class="whitespace-nowrap text-muted">
            <template v-if="row.original.loai === 'jar'">{{ row.original.platf || '?' }}{{ row.original.ban_jar > 0 ? ` · v${row.original.ban_jar}` : '' }}</template>
            <template v-else>client {{ row.original.phien_ban }}</template>
          </span>
        </template>
        <template #lan_dau-cell="{ row }">
          <span class="whitespace-nowrap text-muted">{{ row.original.lan_dau }}</span>
        </template>
        <template #lan_cuoi-cell="{ row }">
          <span class="whitespace-nowrap">{{ row.original.lan_cuoi }}</span>
        </template>
        <template #khoa-cell>
          <span />
        </template>
      </UTable>
    </UCard>
  </div>
</template>
