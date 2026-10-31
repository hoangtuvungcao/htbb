<script setup lang="ts">
/**
 * Đổi lượng trên trang /account: mỗi Pokémon một thẻ (lượng thường / lượng khoá, form đổi lượng → lượng khoá và
 * lượng → xu với số nhận được hiện trước khi gửi), sổ lệnh sàn lượng ↔ xu (chỉ xem), yêu cầu gần đây (huỷ được khi còn
 * chờ). Luật: shared/utils/doiLuong.ts; API: server/api/doi-luong/*. Game đổi trên RAM (online ngay, offline khi vào game).
 */
import type { LoaiDoi } from '#shared/utils/doiLuong'

interface NhanVat { charname: string, luong?: number, luongKhoa?: number, xu?: number }
const props = defineProps<{ chars: NhanVat[] }>()
const emit = defineEmits<{ daDoi: [] }>()

interface TinhTrang {
  ngay: string
  tiLe: number
  tiGia: { ngay: string, giaTri: number, capTrungVi: number, soNguoi: number, phanTramGio: number } | null
  tranXu: number
  conXu: number
  phiSan: number
  tranSan: number
  conSan: number
  san: { ban: { gia: number, so: number }[], mua: { gia: number, so: number }[], tongLuong24h: number, giaTb24h: number } | null
  yeuCau: {
    id: number
    loai: string
    charname: string
    so: number
    nhan?: number | null
    tiGia?: number | null
    taoLuc: string | null
    moTa: string
    tenTrangThai: string
    dangCho: boolean
    trangThai: number
    ketQua: string
    nguon?: string
  }[]
}

const columnsYeuCau = [
  { accessorKey: 'taoLuc', header: 'Thời gian' },
  { accessorKey: 'charname', header: 'Pokémon' },
  { accessorKey: 'so', header: 'Đổi' },
  { accessorKey: 'nhan', header: 'Nhận được' },
  { accessorKey: 'tiGia', header: 'Tỉ giá' },
  { accessorKey: 'tenTrangThai', header: 'Trạng thái' },
]

function dinhDangThoiGian(s?: string | null): string {
  if (!s) return '—'
  const m = String(s).match(/(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/)
  if (m) {
    const [, , mm, dd, hh, min] = m
    return `${hh}:${min} ${dd}/${mm}`
  }
  return String(s)
}

const trangLs = ref(1)
const moiTrangLs = ref(5)
const luaChonMoiTrangLs = [
  { label: '5 / trang', value: 5 },
  { label: '10 / trang', value: 10 },
  { label: '20 / trang', value: 20 },
  { label: 'Tất cả', value: 100 },
]

const tongLs = computed(() => tt.value?.yeuCau?.length ?? 0)
const dsHienThi = computed(() => {
  const list = tt.value?.yeuCau ?? []
  if (moiTrangLs.value >= list.length) return list
  const start = (trangLs.value - 1) * moiTrangLs.value
  return list.slice(start, start + moiTrangLs.value)
})

watch(tongLs, (m) => {
  const max = Math.max(1, Math.ceil(m / moiTrangLs.value))
  if (trangLs.value > max) trangLs.value = max
})
watch(moiTrangLs, () => {
  trangLs.value = 1
})

interface ModalXacNhan {
  nhanVat: NhanVat
  loai: LoaiDoi
  soLuong: number
  nhanDuoc: number
}
const hopXacNhan = ref<ModalXacNhan | null>(null)

const api = useApi()
const toast = useToast()
const { data: tt, refresh } = useAsyncData<TinhTrang>('htbb-doi-luong', () => api('/api/doi-luong'), { server: false })

const soVn = (n: number | undefined | null) => Math.trunc(Number(n) || 0).toLocaleString('vi-VN')

const form = reactive<Record<string, { loai: LoaiDoi, so: number | null }>>({})
watchEffect(() => {
  for (const c of props.chars) {
    if (!form[c.charname]) form[c.charname] = { loai: 'khoa', so: null }
  }
})
const dangGui = ref('')
const loaiItems = [{ label: 'Lượng → lượng khoá', value: 'khoa' }, { label: 'Lượng → xu', value: 'xu' }]

function dangCho(ten: string) {
  return !!tt.value?.yeuCau.some(y => y.dangCho && y.charname.toLowerCase() === ten.toLowerCase())
}
function loiForm(c: NhanVat): string | null {
  const f = form[c.charname]
  if (!f || !tt.value || f.so == null) return null
  return loiDoi(f.loai, Number(f.so), {
    luong: c.luong ?? 0, tiLe: tt.value.tiLe, tiGia: tt.value.tiGia?.giaTri ?? 0, conXu: tt.value.conXu, dangCho: dangCho(c.charname),
  })
}
function nhan(c: NhanVat): number {
  const f = form[c.charname]
  return f && tt.value ? soNhan(f.loai, Number(f.so) || 0, tt.value.tiLe, tt.value.tiGia?.giaTri ?? 0) : 0
}
function datToiDa(c: NhanVat) {
  const f = form[c.charname]
  if (!f) return
  f.so = Math.max(0, f.loai === 'xu' ? Math.min(c.luong ?? 0, tt.value?.conXu ?? 0) : (c.luong ?? 0)) || null
}

function gui(c: NhanVat) {
  const f = form[c.charname]
  if (!f || f.so == null || loiForm(c) || nhan(c) <= 0) return
  hopXacNhan.value = {
    nhanVat: c,
    loai: f.loai,
    soLuong: Math.trunc(Number(f.so)),
    nhanDuoc: nhan(c),
  }
}

async function xacNhanDoi() {
  if (!hopXacNhan.value) return
  const { nhanVat: c, loai, soLuong } = hopXacNhan.value
  dangGui.value = c.charname
  try {
    const kq = await api<{ loai: 'success' | 'warning' | 'error', thongBao: string }>('/api/doi-luong', {
      method: 'POST', body: { charname: c.charname, loai, so: soLuong },
    })
    toast.add({ title: kq.loai === 'error' ? 'Không đổi được' : 'Đổi lượng', description: kq.thongBao, color: kq.loai, icon: 'i-lucide-repeat' })
    if (form[c.charname]) form[c.charname]!.so = null
    hopXacNhan.value = null
    await refresh()
    emit('daDoi')
  } catch (e) {
    toast.add({ title: 'Không gửi được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangGui.value = ''
  }
}

async function huy(id: number) {
  try {
    await api('/api/doi-luong/huy', { method: 'POST', body: { id } })
    toast.add({ title: 'Đã huỷ yêu cầu.', color: 'success', icon: 'i-lucide-x' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không huỷ được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  }
}
</script>

<template>
  <div class="mt-6 overflow-hidden rounded-3xl glass ring-gilded p-5 sm:p-8">
    <p class="eyebrow">
      Đổi lượng
    </p>
    <h2 class="section-title mt-2 text-2xl text-highlighted">
      Lượng → lượng khoá / xu
    </h2>

    <div class="mt-4 grid gap-3 text-sm sm:grid-cols-2">
      <div class="rounded-2xl border border-default p-4">
        <p class="font-semibold text-highlighted">
          Tỉ lệ: 1 <TienTe loai="luong" /> = {{ tt?.tiLe ?? '…' }} <TienTe loai="luong_khoa" />
        </p>
        <p class="mt-1 text-muted">
          Một chiều, không giới hạn. Lượng khoá không bao giờ đổi ngược ra lượng hay xu.
        </p>
      </div>
      <div class="rounded-2xl border border-default p-4">
        <p class="font-semibold text-highlighted">
          <template v-if="tt?.tiGia">
            Tỉ giá hôm nay: 1 <TienTe loai="luong" /> = <TienTe loai="xu" :gt="tt.tiGia.giaTri" /> (áp dụng chung toàn máy chủ)
          </template>
          <template v-else>
            Lượng → xu: chưa có tỉ giá hôm nay
          </template>
        </p>
        <p class="mt-1 text-muted">
          <template v-if="tt?.tiGia">
            = {{ tt.tiGia.phanTramGio }}% thu nhập 1 giờ ở cấp {{ tt.tiGia.capTrungVi }} (cấp giữa của người chơi hoạt động 7 ngày), tính lúc 00:00.
          </template>
          Tài khoản hôm nay còn đổi được <b class="text-highlighted">{{ soVn(tt?.conXu) }}</b> / {{ soVn(tt?.tranXu) }} lượng → xu.
        </p>
      </div>
    </div>

    <UAlert
      class="mt-4"
      color="info"
      variant="subtle"
      icon="i-lucide-info"
      description="Chỉ dùng lượng THƯỜNG trong người (không dùng lượng trong kho, không dùng lượng khoá). Pokémon đang online: đổi ngay. Đang offline: đổi ngay khi Pokémon vào game — huỷ được trước lúc đó. Trong game: NPC Tổng Quản → Đổi lượng."
    />

    <div class="mt-6 grid gap-4 md:grid-cols-2">
      <form
        v-for="c in chars"
        :key="c.charname"
        class="space-y-3 rounded-2xl border border-default p-4"
        @submit.prevent="gui(c)"
      >
        <div class="flex flex-wrap items-center justify-between gap-2">
          <span class="font-semibold text-highlighted">{{ c.charname }}</span>
          <span class="flex flex-wrap items-center gap-3 text-sm">
            <TienTe loai="xu" :gt="c.xu ?? 0" />
            <TienTe loai="luong" :gt="c.luong ?? 0" />
            <TienTe loai="luong_khoa" :gt="c.luongKhoa ?? 0" />
          </span>
        </div>
        <template v-if="form[c.charname]">
          <USelect v-model="form[c.charname]!.loai" :items="loaiItems" class="w-full" />
          <div class="space-y-1">
            <div class="flex items-center justify-between text-xs text-muted">
              <span>Số lượng ({{ form[c.charname]!.loai === 'xu' ? '→ xu' : '→ lượng khoá' }})</span>
              <span v-if="form[c.charname]!.loai === 'xu' && tt?.conXu != null">
                Còn đổi được: <b class="text-highlighted">{{ soVn(tt.conXu) }}</b> lượng
              </span>
            </div>
            <div class="flex gap-2">
              <UInput
                v-model.number="form[c.charname]!.so"
                type="number"
                inputmode="numeric"
                min="1"
                :placeholder="form[c.charname]!.loai === 'xu' ? 'Số lượng (→ xu)' : 'Số lượng (→ lượng khoá)'"
                class="min-w-0 flex-1"
              />
              <UButton color="neutral" variant="outline" label="Tối đa" @click="datToiDa(c)" />
            </div>
          </div>
          <p v-if="loiForm(c)" class="text-sm text-error">
            {{ loiForm(c) }}
          </p>
          <p v-else-if="nhan(c) > 0" class="text-sm text-muted">
            Nhận: <TienTe :loai="form[c.charname]!.loai === 'khoa' ? 'luong_khoa' : 'xu'" :gt="nhan(c)" ten />
          </p>
          <UButton
            type="submit"
            block
            icon="i-lucide-repeat"
            :loading="dangGui === c.charname"
            :disabled="!form[c.charname]!.so || !!loiForm(c) || nhan(c) <= 0"
            :label="form[c.charname]!.loai === 'xu' ? 'Đổi sang xu' : 'Đổi sang lượng khoá'"
          />
        </template>
      </form>
    </div>

    <div v-if="tt?.yeuCau.length" class="mt-6">
      <div class="flex items-center justify-between gap-4">
        <h3 class="font-semibold text-highlighted">
          Lịch sử đổi lượng gần đây
        </h3>
        <UBadge color="neutral" variant="outline" size="sm">
          {{ tt.yeuCau.length }} yêu cầu
        </UBadge>
      </div>

      <!-- Điện thoại: thẻ gọn gàng -->
      <ul class="mt-3 space-y-3 md:hidden">
        <li v-for="y in dsHienThi" :key="y.id" class="space-y-2 rounded-2xl border border-default p-4">
          <div class="flex items-center justify-between gap-2">
            <span class="font-semibold text-highlighted">{{ y.charname }}</span>
            <span class="text-xs text-muted tabular-nums">{{ dinhDangThoiGian(y.taoLuc) }}</span>
          </div>
          <div class="flex items-center justify-between gap-2">
            <span class="flex items-center gap-1.5 text-sm">
              <TienTe loai="luong" :gt="y.so" />
              <span class="text-muted">→</span>
              <span class="font-semibold text-highlighted">
                +<TienTe :loai="y.loai === 'doi_luong_khoa' ? 'luong_khoa' : 'xu'" :gt="y.nhan ?? 0" />
              </span>
            </span>
            <UBadge :color="y.dangCho ? 'warning' : (y.trangThai === 3 ? 'error' : 'neutral')" variant="subtle" size="sm" :label="y.tenTrangThai" />
          </div>
          <div class="flex items-center justify-between border-t border-default/40 pt-1 text-xs text-muted">
            <span>
              Tỉ giá: 1 <TienTe loai="luong" /> = {{ y.loai === 'doi_luong_khoa' ? '1' : soVn(y.tiGia) }} <TienTe :loai="y.loai === 'doi_luong_khoa' ? 'luong_khoa' : 'xu'" />
            </span>
            <UButton v-if="y.dangCho" size="xs" color="neutral" variant="outline" label="Huỷ" @click="huy(y.id)" />
          </div>
          <p v-if="y.trangThai === 3 && y.ketQua" class="text-xs text-error">{{ y.ketQua }}</p>
        </li>
      </ul>

      <!-- Máy tính / Tablet: bảng chi tiết -->
      <UTable
        :data="dsHienThi"
        :columns="columnsYeuCau"
        class="mt-3 hidden md:block"
        :ui="{ th: 'text-xs uppercase tracking-wider text-dimmed', td: 'text-default' }"
      >
        <template #taoLuc-cell="{ row }">
          <span class="text-xs text-muted tabular-nums">{{ dinhDangThoiGian(row.original.taoLuc) }}</span>
        </template>
        <template #charname-cell="{ row }">
          <span class="font-semibold text-highlighted">{{ row.original.charname }}</span>
        </template>
        <template #so-cell="{ row }">
          <TienTe loai="luong" :gt="row.original.so" />
        </template>
        <template #nhan-cell="{ row }">
          <span class="font-semibold text-highlighted">
            +<TienTe :loai="row.original.loai === 'doi_luong_khoa' ? 'luong_khoa' : 'xu'" :gt="row.original.nhan ?? 0" />
          </span>
        </template>
        <template #tiGia-cell="{ row }">
          <span class="text-xs text-muted">
            1 <TienTe loai="luong" /> = {{ row.original.loai === 'doi_luong_khoa' ? '1' : soVn(row.original.tiGia) }} <TienTe :loai="row.original.loai === 'doi_luong_khoa' ? 'luong_khoa' : 'xu'" />
          </span>
        </template>
        <template #tenTrangThai-cell="{ row }">
          <div class="flex items-center gap-2">
            <UBadge :color="row.original.dangCho ? 'warning' : (row.original.trangThai === 3 ? 'error' : 'neutral')" variant="subtle" size="sm" :label="row.original.tenTrangThai" />
            <UButton v-if="row.original.dangCho" size="xs" color="neutral" variant="outline" label="Huỷ" @click="huy(row.original.id)" />
          </div>
          <p v-if="row.original.trangThai === 3 && row.original.ketQua" class="mt-0.5 text-xs text-error">{{ row.original.ketQua }}</p>
        </template>
      </UTable>

      <!-- Phân trang lịch sử -->
      <div v-if="tongLs > 0" class="mt-4 flex flex-wrap items-center justify-between gap-3 text-xs text-muted">
        <div class="flex items-center gap-2">
          <span>
            Hiển thị {{ (trangLs - 1) * moiTrangLs + 1 }} - {{ Math.min(trangLs * moiTrangLs, tongLs) }} trên tổng số {{ tongLs }}
          </span>
          <USelect v-model.number="moiTrangLs" :items="luaChonMoiTrangLs" size="xs" class="w-28" />
        </div>
        <UPagination v-if="tongLs > moiTrangLs" v-model:page="trangLs" :total="tongLs" :items-per-page="moiTrangLs" />
      </div>
    </div>

    <div class="mt-6 rounded-2xl border border-default p-4">
      <h3 class="font-semibold text-highlighted">
        Sàn đổi lượng ↔ xu (giữa người chơi)
      </h3>
      <p class="mt-1 text-sm text-muted">
        Chỉ lượng thường; lượng khoá không bao giờ lên sàn. Đặt lệnh bán / mua ở NPC Tổng Quản → Đổi lượng → Sàn. Phí {{ tt?.phiSan ?? '…' }}% xu do bên mua trả (bị đốt), lệnh tự huỷ sau 24 giờ.
        Hôm nay tài khoản còn đặt được {{ soVn(tt?.conSan) }} lượng.
      </p>
      <p v-if="tt?.san" class="mt-2 text-sm">
        Giá trung bình 24 giờ:
        <b v-if="tt.san.giaTb24h > 0"><TienTe loai="xu" :gt="tt.san.giaTb24h" /> / lượng ({{ soVn(tt.san.tongLuong24h) }} lượng đã khớp)</b>
        <span v-else class="text-muted">chưa có giao dịch</span>
      </p>
      <div v-if="tt?.san" class="mt-3 grid grid-cols-2 gap-3 text-sm">
        <div>
          <p class="font-semibold text-error">
            Bán (rẻ nhất trước)
          </p>
          <p v-for="m in tt.san.ban" :key="`b${m.gia}`" class="num">
            {{ soVn(m.gia) }} xu × {{ soVn(m.so) }}
          </p>
          <p v-if="!tt.san.ban.length" class="text-muted">
            (trống)
          </p>
        </div>
        <div>
          <p class="font-semibold text-success">
            Mua (cao nhất trước)
          </p>
          <p v-for="m in tt.san.mua" :key="`m${m.gia}`" class="num">
            {{ soVn(m.gia) }} xu × {{ soVn(m.so) }}
          </p>
          <p v-if="!tt.san.mua.length" class="text-muted">
            (trống)
          </p>
        </div>
      </div>
    </div>

    <!-- Hộp thoại xác nhận đổi lượng -->
    <UModal
      :open="!!hopXacNhan"
      title="Xác nhận đổi lượng"
      :dismissible="!dangGui"
      @update:open="(v: boolean) => { if (!v && !dangGui) hopXacNhan = null }"
    >
      <template #body>
        <div v-if="hopXacNhan" class="space-y-4">
          <div class="flex items-center gap-3 rounded-2xl border border-default p-4">
            <span class="grid size-11 place-items-center rounded-xl border border-primary/30 bg-primary/10 text-primary">
              <UIcon name="i-lucide-repeat" class="size-6" />
            </span>
            <div class="min-w-0">
              <p class="font-bold text-highlighted text-base truncate">{{ hopXacNhan.nhanVat.charname }}</p>
              <p class="text-xs text-muted">Đổi tiền tệ cho Pokémon này</p>
            </div>
          </div>

          <div class="rounded-2xl border border-default p-4 space-y-2.5 text-sm">
            <div class="flex items-center justify-between">
              <span class="text-muted">Số lượng trừ:</span>
              <TienTe loai="luong" :gt="hopXacNhan.soLuong" />
            </div>
            <div class="flex items-center justify-between">
              <span class="text-muted">Thực nhận:</span>
              <span class="font-bold text-highlighted text-base">
                +<TienTe :loai="hopXacNhan.loai === 'khoa' ? 'luong_khoa' : 'xu'" :gt="hopXacNhan.nhanDuoc" />
              </span>
            </div>
            <div v-if="hopXacNhan.loai === 'xu' && tt?.tiGia" class="flex items-center justify-between text-xs text-muted pt-2 border-t border-default/50">
              <span>Tỉ giá hôm nay:</span>
              <span>1 <TienTe loai="luong" /> = <TienTe loai="xu" :gt="tt.tiGia.giaTri" /></span>
            </div>
            <div v-else-if="hopXacNhan.loai === 'khoa'" class="flex items-center justify-between text-xs text-muted pt-2 border-t border-default/50">
              <span>Tỉ lệ:</span>
              <span>1 <TienTe loai="luong" /> = 1 <TienTe loai="luong_khoa" /></span>
            </div>
          </div>

          <div class="flex items-start gap-2 rounded-xl bg-amber-500/10 p-3 text-xs text-amber-500">
            <UIcon name="i-lucide-alert-triangle" class="size-4 shrink-0 mt-0.5" />
            <span>Thao tác một chiều: không thể đổi ngược lại sau khi xác nhận.</span>
          </div>
        </div>
      </template>
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton color="neutral" variant="ghost" label="Huỷ" :disabled="!!dangGui" @click="hopXacNhan = null" />
          <UButton
            color="primary"
            icon="i-lucide-check"
            label="Xác nhận đổi"
            :loading="!!dangGui"
            @click="xacNhanDoi"
          />
        </div>
      </template>
    </UModal>
  </div>
</template>
