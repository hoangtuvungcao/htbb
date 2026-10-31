<script setup lang="ts">
/**
 * Đổi lượng trên trang /account: mỗi Pokémon một thẻ (lượng thường / lượng khoá, form đổi lượng → lượng khoá và
 * lượng → xu với số nhận được hiện trước khi gửi), sổ lệnh sàn lượng ↔ xu (chỉ xem), yêu cầu gần đây (huỷ được khi còn
 * chờ). Luật: shared/utils/doiLuong.ts; API: server/api/doi-luong/*. Game đổi trên RAM (online ngay, offline khi vào game).
 */
import type { LoaiDoi } from '#shared/utils/doiLuong'

interface NhanVat { charname: string, luong?: number, luongKhoa?: number }
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
  yeuCau: { id: number, charname: string, moTa: string, tenTrangThai: string, dangCho: boolean, trangThai: number, ketQua: string }[]
}

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

async function gui(c: NhanVat) {
  const f = form[c.charname]
  if (!f || f.so == null || loiForm(c)) return
  const moTa = f.loai === 'khoa'
    ? `${soVn(f.so)} lượng → ${soVn(nhan(c))} lượng khoá`
    : `${soVn(f.so)} lượng → ${soVn(nhan(c))} xu`
  if (!window.confirm(`Đổi ${moTa} cho ${c.charname}? Không đổi ngược lại được.`)) return
  dangGui.value = c.charname
  try {
    const kq = await api<{ loai: 'success' | 'warning' | 'error', thongBao: string }>('/api/doi-luong', {
      method: 'POST', body: { charname: c.charname, loai: f.loai, so: Math.trunc(Number(f.so)) },
    })
    toast.add({ title: kq.loai === 'error' ? 'Không đổi được' : 'Đổi lượng', description: kq.thongBao, color: kq.loai, icon: 'i-lucide-repeat' })
    f.so = null
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
          <span class="flex flex-wrap gap-3 text-sm">
            <TienTe loai="luong" :gt="c.luong ?? 0" />
            <TienTe loai="luong_khoa" :gt="c.luongKhoa ?? 0" />
          </span>
        </div>
        <template v-if="form[c.charname]">
          <USelect v-model="form[c.charname]!.loai" :items="loaiItems" class="w-full" />
          <div class="flex gap-2">
            <UInput
              v-model.number="form[c.charname]!.so"
              type="number"
              inputmode="numeric"
              min="1"
              placeholder="Số lượng"
              class="min-w-0 flex-1"
            />
            <UButton color="neutral" variant="outline" label="Tối đa" @click="datToiDa(c)" />
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
            label="Đổi"
          />
        </template>
      </form>
    </div>

    <div v-if="tt?.yeuCau.length" class="mt-6">
      <h3 class="font-semibold text-highlighted">
        Yêu cầu gần đây
      </h3>
      <ul class="mt-2 divide-y divide-default text-sm">
        <li v-for="y in tt.yeuCau" :key="y.id" class="flex flex-wrap items-center justify-between gap-2 py-2">
          <span><b>{{ y.charname }}</b>: {{ y.moTa }}</span>
          <span class="flex items-center gap-2">
            <UBadge :color="y.dangCho ? 'warning' : (y.trangThai === 3 ? 'error' : 'neutral')" variant="subtle" :label="y.tenTrangThai" />
            <UButton v-if="y.trangThai === 0 || y.trangThai === 4" size="xs" color="neutral" variant="outline" label="Huỷ" @click="huy(y.id)" />
          </span>
          <span v-if="y.trangThai === 3 && y.ketQua" class="w-full text-xs text-error">{{ y.ketQua }}</span>
        </li>
      </ul>
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
  </div>
</template>
