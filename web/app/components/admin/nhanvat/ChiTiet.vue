<script setup lang="ts">
/**
 * Trang chi tiết một nhân vật: thông tin, khoá/mở khoá,
 * đặt cấp độ / buff level kèm %, lệnh đã gửi, xoá vĩnh viễn, và bốn bảng vật phẩm.
 * Mọi thao tác đổi dữ liệu game (cấp độ, xoá đồ, xoá nhân vật) đi qua bảng lệnh cho server
 * game — xem server/utils/lenhNhanVat.ts.
 */
import type { DongLenhGiaoDien } from './BangLenh.vue'
import type { VatPhamGiaoDien } from './BangVatPham.vue'

const props = defineProps<{ id: number }>()
const emit = defineEmits<{ veDanhSach: [] }>()

interface ChiTietNhanVat {
  ct: {
    id: number, charname: string, userId: number, lastLv: number, class: number, dex: number, xp: number | string
    gold: number, luong: number, luonglock: number, ban: number, del: number, lastLog: string
    idserver: number, idClan: number, nhack: number, blacklist: number, tichluy: number, topNap: number
  }
  chuTk: string
  chuLaAdmin: boolean
  capDoToiDa: number
  loiVatPham: string
  vatPham: { mac: VatPhamGiaoDien[], thoiTrang: VatPhamGiaoDien[], tui: VatPhamGiaoDien[], ruong: VatPhamGiaoDien[] }
  lenh: DongLenhGiaoDien[]
}
interface KetQua { loai: 'success' | 'warning' | 'error', thongBao: string, daXoa?: boolean }

const api = useAdminApi()
const toast = useToast()

const { data, status, error, refresh } = useAsyncData(
  () => `admin-nhanvat-${props.id}`,
  () => api<ChiTietNhanVat>(`/api/admin/nhanvat/${props.id}`),
  { server: false, watch: [() => props.id] },
)

const ct = computed(() => data.value?.ct)
const capDo = ref<number>(1)
const capDoBuff = ref<number>(1)
const phanTramBuff = ref<number>(0)
const tenXacNhan = ref('')
watch(ct, (v) => {
  if (!v) return
  capDo.value = Number(v.lastLv)
  capDoBuff.value = Number(v.lastLv)
  phanTramBuff.value = 0
  tenXacNhan.value = ''
}, { immediate: true })

function bao(kq: KetQua) {
  toast.add({
    title: kq.loai === 'error' ? 'Không thực hiện được' : kq.loai === 'warning' ? 'Lưu ý' : 'Xong',
    description: kq.thongBao,
    color: kq.loai,
    duration: kq.loai === 'success' ? 6000 : 12000,
  })
}

// ---- Hộp xác nhận dùng chung ----
const xn = reactive({
  open: false,
  title: '',
  description: '',
  nhanNut: 'Xác nhận',
  mau: 'primary' as 'primary' | 'error' | 'warning',
  viec: null as null | (() => Promise<void>),
})
const dangChay = ref(false)

function hoi(title: string, description: string, viec: () => Promise<void>, opts: { nhanNut?: string, mau?: 'primary' | 'error' | 'warning' } = {}) {
  Object.assign(xn, { open: true, title, description, viec, nhanNut: opts.nhanNut ?? 'Xác nhận', mau: opts.mau ?? 'primary' })
}

async function chay() {
  if (!xn.viec) return
  dangChay.value = true
  try {
    await xn.viec()
  } finally {
    dangChay.value = false
    xn.open = false
  }
}

async function goi(duong: string, body: Record<string, unknown>) {
  try {
    const kq = await api<KetQua>(`/api/admin/nhanvat/${props.id}/${duong}`, { method: 'POST', body })
    bao(kq)
    if (kq.daXoa) {
      emit('veDanhSach')
      return
    }
  } catch (e) {
    bao({ loai: 'error', thongBao: loiApi(e) })
  }
  await refresh()
}

// ---- Thao tác ----
function khoaMo() {
  const v = ct.value
  if (!v) return
  const khoa = Number(v.ban) !== 0
  hoi(`${khoa ? 'Mở khoá' : 'Khoá'} nhân vật ${v.charname}?`, '',
    () => goi('khoa', { viec: khoa ? 'mo_khoa' : 'khoa' }),
    { nhanNut: khoa ? 'Mở khoá' : 'Khoá', mau: khoa ? 'primary' : 'error' })
}

function datCapDo() {
  const v = ct.value
  if (!v) return
  hoi(`Đặt cấp độ ${capDo.value} cho ${v.charname}?`,
    'Nhân vật đang online sẽ được cộng ngay; offline thì cộng ngay khi họ vào game. Chỉ NÂNG cấp, không hạ.',
    () => goi('cap-do', { viec: 'dat_cap_do', cap_do: capDo.value }),
    { nhanNut: 'Đặt cấp độ', mau: 'error' })
}

function buffLevel() {
  const v = ct.value
  if (!v) return
  // Bước 0,1 cộng dồn trong ô số dễ ra 0.30000000000000004 — làm tròn trước khi hiện.
  const pt = (Math.round(Number(phanTramBuff.value ?? 0) * 10) / 10).toLocaleString('vi-VN')
  hoi(`Buff ${capDoBuff.value} (${pt}%) cho ${v.charname}?`,
    'Nhân vật đang online sẽ được cộng ngay; offline thì cộng ngay khi họ vào game. Chỉ NÂNG cấp, không hạ.',
    () => goi('cap-do', { viec: 'buff_level', cap_do: capDoBuff.value, phan_tram: phanTramBuff.value ?? 0 }),
    { nhanNut: 'Buff level', mau: 'error' })
}

const dangHuy = ref<number | null>(null)
function huyLenh(l: DongLenhGiaoDien) {
  hoi(`Huỷ lệnh #${l.id}?`, l.mo_ta, async () => {
    dangHuy.value = l.id
    try {
      await goi('huy-lenh', { id_lenh: l.id })
    } finally {
      dangHuy.value = null
    }
  }, { nhanNut: 'Huỷ lệnh' })
}

const dangXoa = ref<string | null>(null)
function xoaVatPham(vp: VatPhamGiaoDien, cot: 'equip' | 'inven' | 'bag') {
  const tenMon = (vp.ten ?? `#${vp.template}`) + (vp.plus > 0 ? ` +${vp.plus}` : '')
  hoi(`Xoá vĩnh viễn ${tenMon} khỏi nhân vật?`,
    'Nhân vật đang online thì server gỡ ngay trong game; offline thì xoá trong CSDL.',
    async () => {
      dangXoa.value = `${cot}:${vp._i}`
      try {
        await goi('xoa-vat-pham', { cot, vi_tri: vp._i, template: vp.template, plus: vp.plus })
      } finally {
        dangXoa.value = null
      }
    }, { nhanNut: 'Xoá', mau: 'error' })
}

function xoaNhanVat() {
  const v = ct.value
  if (!v || tenXacNhan.value !== v.charname) return
  hoi(`XOÁ VĨNH VIỄN ${v.charname}?`, 'Không thể hoàn tác.',
    () => goi('xoa', { xac_nhan_ten: tenXacNhan.value }),
    { nhanNut: 'Xoá vĩnh viễn', mau: 'error' })
}

const trangThai = computed(() => {
  const v = ct.value
  if (!v) return null
  if (Number(v.ban) !== 0) return { label: 'Bị khoá', color: 'error' as const }
  if (Number(v.del) !== 1) return { label: 'Đã xoá', color: 'warning' as const }
  return { label: 'Bình thường', color: 'success' as const }
})
</script>

<template>
  <div class="space-y-6">
    <UButton icon="i-lucide-arrow-left" color="neutral" variant="ghost" label="Về danh sách" class="-ml-2" @click="emit('veDanhSach')" />

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="loiApi(error)" />
    <div v-else-if="status === 'pending' && !data" class="grid gap-4 lg:grid-cols-[2fr_1fr]">
      <USkeleton class="h-96" />
      <USkeleton class="h-48" />
    </div>

    <template v-if="data && ct">
      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <UCard>
          <template #header>
            <div class="flex flex-wrap items-center gap-3">
              <h2 class="text-lg font-bold text-highlighted">
                {{ ct.charname }}
              </h2>
              <UBadge v-if="trangThai" :color="trangThai.color" variant="subtle" :label="trangThai.label" />
              <div class="ml-auto">
                <UButton
                  :color="Number(ct.ban) !== 0 ? 'primary' : 'error'"
                  :variant="Number(ct.ban) !== 0 ? 'solid' : 'soft'"
                  :icon="Number(ct.ban) !== 0 ? 'i-lucide-lock-open' : 'i-lucide-lock'"
                  :label="Number(ct.ban) !== 0 ? 'Mở khoá' : 'Khoá'"
                  size="sm"
                  @click="khoaMo"
                />
              </div>
            </div>
          </template>

          <dl class="grid grid-cols-[max-content_1fr] gap-x-6 gap-y-2 text-sm">
            <dt class="text-muted">ID Pokémon</dt><dd class="tabular-nums">{{ ct.id }}</dd>
            <dt class="text-muted">Tài khoản</dt>
            <dd>
              <template v-if="data.chuTk !== ''">
                <NuxtLink :to="`/admin/taikhoan?id=${ct.userId}`" class="font-medium text-primary hover:underline">{{ data.chuTk }}</NuxtLink>
                <span class="ml-1 text-xs text-muted">(#{{ ct.userId }})</span>
                <UBadge v-if="data.chuLaAdmin" class="ml-2" color="neutral" variant="outline" size="sm" label="quản trị" />
              </template>
              <span v-else class="text-muted">không tìm thấy tài khoản #{{ ct.userId }}</span>
            </dd>
            <dt class="text-muted">Pokémon</dt><dd>{{ speciesName(ct.class, ct.lastLv, ct.dex) }} <span class="text-muted">(dòng {{ tenNghe(ct.class) }})</span></dd>
            <dt class="text-muted">Cấp độ</dt>
            <dd><b class="tabular-nums">{{ ct.lastLv }}</b> <span class="text-xs text-muted">({{ so(ct.xp) }} kinh nghiệm)</span></dd>
            <dt class="text-muted">Xu</dt><dd class="tabular-nums">{{ so(ct.gold) }}</dd>
            <dt class="text-muted">Lượng</dt>
            <dd class="tabular-nums">{{ so(ct.luong) }} <span class="text-xs text-muted">+ {{ so(ct.luonglock) }} khoá</span></dd>
            <dt class="text-muted">Tích luỹ</dt>
            <dd class="tabular-nums">{{ so(ct.tichluy) }} <span class="text-xs text-muted">· top nạp {{ so(ct.topNap) }}</span></dd>
            <dt class="text-muted">Vào lần cuối</dt><dd>{{ ct.lastLog }}</dd>
            <dt class="text-muted">Máy chủ</dt><dd class="tabular-nums">{{ ct.idserver }}</dd>
            <dt class="text-muted">Đội</dt>
            <dd><span v-if="Number(ct.idClan) === -1" class="text-muted">không có</span><span v-else class="tabular-nums">{{ ct.idClan }}</span></dd>
            <dt class="text-muted">Cờ nghi vấn</dt>
            <dd class="flex flex-wrap gap-1.5">
              <UBadge :color="Number(ct.nhack) > 0 ? 'warning' : 'neutral'" variant="subtle" size="sm" :label="`nhack ${ct.nhack}`" />
              <UBadge :color="Number(ct.blacklist) > 0 ? 'warning' : 'neutral'" variant="subtle" size="sm" :label="`blacklist ${ct.blacklist}`" />
            </dd>
          </dl>

          <UAlert v-if="Number(ct.ban) !== 0" class="mt-5" color="warning" variant="subtle" icon="i-lucide-triangle-alert" title="Nhân vật này đang bị khoá.">
            <template #description>
              <p>
                Nếu bạn không phải người khoá nó thì rất có thể server tự khoá: khi lưu nhân vật
                thất bại (ví dụ hành trang quá dài so với sức chứa của cột <code>inven</code>),
                khối bắt lỗi trong <code>Database.java</code> tự đặt <code>ban = 1</code>.
                Xem <code>vps/sql-giftcode.md</code>, mục “CẠM BẪY ĐÃ NỔ MỘT LẦN”.
              </p>
              <p class="mt-1">
                Mở khoá lấy lại được nhân vật, nhưng <em>không</em> lấy lại phần dữ liệu đã mất giữa lúc lưu hỏng.
              </p>
            </template>
          </UAlert>

          <USeparator class="my-6" />

          <!-- Đặt cấp độ -->
          <section class="space-y-3">
            <h3 class="font-semibold text-highlighted">
              Đặt cấp độ
            </h3>
            <div class="flex flex-wrap items-end gap-3">
              <UFormField :label="`Cấp độ mới (hiện tại: ${ct.lastLv})`">
                <UInputNumber v-model="capDo" :min="1" :max="data.capDoToiDa" class="w-40" />
              </UFormField>
              <UButton color="error" icon="i-lucide-trending-up" label="Đặt cấp độ" @click="datCapDo" />
            </div>
            <UAlert color="info" variant="subtle" icon="i-lucide-info" title="Dùng được cả khi nhân vật đang online.">
              <template #description>
                <p>
                  Trang web không ghi thẳng CSDL nữa mà gửi một lệnh vào bảng <code>web_lenh_nhan_vat</code>;
                  server game đọc mỗi giây và tự xử lý: nhân vật <strong>đang online</strong> được cộng ngay trong
                  game (đúng như lên cấp tự nhiên: có điểm cơ bản, điểm kỹ năng, hồi đầy HP/MP) rồi lưu lại tức thì;
                  nhân vật <strong>offline</strong> thì lệnh nằm chờ và server cộng ngay khi họ vào game. Kết quả hiện
                  ngay sau khi bấm.
                </p>
                <p class="mt-2">
                  <strong>Chỉ nâng cấp.</strong> Cấp/% mới phải cao hơn mức hiện tại, nếu không server từ chối và báo
                  lỗi — hạ cấp để lại điểm cơ bản/kỹ năng vượt mức, chốt chống gian lận lúc đăng nhập sẽ xoá điểm và
                  kỹ năng của người chơi. Server game đang tắt thì lệnh nằm chờ và được thực hiện khi server chạy lại —
                  không cần bấm lại; theo dõi hoặc huỷ ở bảng "Lệnh đã gửi" bên dưới.
                </p>
              </template>
            </UAlert>
          </section>

          <!-- Buff level kèm % -->
          <section class="mt-6 space-y-3">
            <h3 class="font-semibold text-highlighted">
              Buff level kèm %
            </h3>
            <div class="flex flex-wrap items-end gap-3">
              <UFormField label="Cấp độ">
                <UInputNumber v-model="capDoBuff" :min="1" :max="data.capDoToiDa" class="w-40" />
              </UFormField>
              <UFormField label="% vào cấp">
                <UInputNumber v-model="phanTramBuff" :min="0" :max="99.9" :step="0.1" :format-options="{ maximumFractionDigits: 1 }" class="w-40" />
              </UFormField>
              <UButton color="error" icon="i-lucide-zap" label="Buff level" @click="buffLevel" />
            </div>
            <p class="text-sm text-muted">
              Giống hệt "Đặt cấp độ" nhưng cho nhập thêm % tiến độ vào cấp đó (thay vì luôn là 0%) — cùng tính
              năng "Buff EXP → Lên Level/Phần trăm" đã có sẵn trong AdminPanel (ứng dụng chạy trên server).
              Cũng đi qua bảng lệnh như "Đặt cấp độ" ở trên nên dùng được cả khi nhân vật đang online lẫn offline.
            </p>
          </section>

          <!-- Lệnh đã gửi -->
          <section class="mt-6 space-y-3">
            <h3 class="font-semibold text-highlighted">
              Lệnh đã gửi
            </h3>
            <AdminNhanvatBangLenh :lenh="data.lenh" empty="Chưa có lệnh nào cho nhân vật này." :dang-huy="dangHuy" @huy="huyLenh" />
            <p v-if="data.lenh.length" class="text-xs text-muted">
              "Chờ vào game" = nhân vật đang offline, server sẽ cộng ngay khi họ đăng nhập.
              "Chờ server" quá lâu = server game đang tắt; lệnh sẽ chạy khi server lên.
            </p>
          </section>

          <!-- Xoá vĩnh viễn -->
          <section class="mt-8 space-y-3 rounded-lg border border-error/40 bg-error/5 p-4">
            <h3 class="font-semibold text-error">
              Xoá vĩnh viễn
            </h3>
            <p class="text-sm text-muted">
              <strong class="text-highlighted">KHÔNG THỂ HOÀN TÁC.</strong> Khác hẳn "Khoá" ở trên (chỉ ẩn đi, mở lại
              được) — xoá vĩnh viễn xoá hẳn dòng <code>tob_char</code> này khỏi cơ sở dữ liệu, cùng với thú nuôi, trang
              trại, đá quý, bạn bè, quest riêng của nhân vật. Nhân vật đang online sẽ bị đẩy ra khỏi game trước khi xoá.
              Nhật ký giao dịch/log vẫn được giữ lại để tra cứu. Nếu Pokémon đang trong một đội, hãy tự rời đội
              trước — trang này không tự gỡ tên khỏi danh sách thành viên đội.
            </p>
            <div class="flex flex-wrap items-end gap-3">
              <UFormField :label="`Gõ đúng tên “${ct.charname}” để xác nhận`" class="min-w-60">
                <UInput v-model="tenXacNhan" autocomplete="off" class="w-full" />
              </UFormField>
              <UButton color="error" icon="i-lucide-trash-2" label="Xoá vĩnh viễn" :disabled="tenXacNhan !== ct.charname" @click="xoaNhanVat" />
            </div>
          </section>
        </UCard>

        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Khoá nhân vật làm gì
            </h3>
          </template>
          <ul class="list-disc space-y-2 pl-5 text-sm text-muted">
            <li>Nhân vật <strong class="text-highlighted">biến mất khỏi màn hình chọn nhân vật</strong> — người chơi thường hiểu nhầm là bị xoá.</li>
            <li>Bị loại khỏi mọi bảng xếp hạng.</li>
            <li>Tài khoản <strong class="text-highlighted">vẫn đăng nhập được</strong> và vẫn chơi được các nhân vật khác.</li>
            <li>
              Người đang online <strong class="text-highlighted">không bị đá ra</strong> — việc khoá ghi thẳng cột
              <code>ban</code>, không đi qua bảng lệnh như "Đặt cấp độ". Khoá chỉ ăn từ lần vào sau.
            </li>
          </ul>
        </UCard>
      </div>

      <UAlert v-if="data.loiVatPham" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Không đọc được vật phẩm: ${data.loiVatPham}`" />

      <UCard>
        <template #header>
          <div class="flex items-center gap-2">
            <UIcon name="i-lucide-backpack" class="size-5 text-muted" />
            <h3 class="font-semibold text-highlighted">
              Vật phẩm
            </h3>
          </div>
        </template>
        <div class="space-y-6">
          <p class="text-sm text-muted">
            Máy chủ này <strong class="text-highlighted">không có bảng <code>tob_item</code></strong> — trang bị, hành trang
            và rương được ghi thẳng vào ba cột <code>equip</code>, <code>inven</code>, <code>bag</code> của
            <code>tob_char</code>. Đây là ảnh chụp <em>lần lưu gần nhất</em>: người chơi đang online thì đồ thật nằm
            trong RAM của server, chỉ khớp sau khi họ thoát hoặc server tự lưu.
          </p>
          <UAlert color="info" variant="subtle" icon="i-lucide-info" title="Nút &quot;Xoá&quot; dùng được cả khi nhân vật đang online.">
            <template #description>
              Giống "Đặt cấp độ", lệnh xoá đi qua bảng lệnh cho server game: nhân vật đang online thì server tìm đúng món
              đó trong RAM, gỡ ra và lưu ngay; offline thì server xoá trong CSDL rồi đối chiếu lại một lần nữa khi nhân
              vật vào game. Server báo lỗi nếu món đồ đã đổi so với bảng đang xem (tải lại trang rồi bấm lại). Mọi thao
              tác xoá đều được ghi lại ở trang <NuxtLink to="/admin/log" class="text-primary hover:underline">Log</NuxtLink>
              và bảng "Lệnh đã gửi".
            </template>
          </UAlert>

          <div class="grid gap-6 xl:grid-cols-2">
            <div class="space-y-2">
              <h4 class="font-semibold">
                Đang mặc <span class="text-sm font-normal text-muted">({{ data.vatPham.mac.length }})</span>
              </h4>
              <AdminNhanvatBangVatPham :ds="data.vatPham.mac" trong="Không mặc gì." cot="equip" :dang-xoa="dangXoa" @xoa="xoaVatPham" />
            </div>
            <div class="space-y-2">
              <h4 class="font-semibold">
                Thời trang <span class="text-sm font-normal text-muted">({{ data.vatPham.thoiTrang.length }})</span>
              </h4>
              <AdminNhanvatBangVatPham :ds="data.vatPham.thoiTrang" trong="Không có." cot="inven" :dang-xoa="dangXoa" @xoa="xoaVatPham" />
            </div>
          </div>

          <div class="space-y-2">
            <h4 class="font-semibold">
              Hành trang <span class="text-sm font-normal text-muted">({{ data.vatPham.tui.length }})</span>
            </h4>
            <AdminNhanvatBangVatPham :ds="data.vatPham.tui" trong="Túi rỗng." cot="inven" :dang-xoa="dangXoa" @xoa="xoaVatPham" />
          </div>

          <div class="space-y-2">
            <h4 class="font-semibold">
              Rương <span class="text-sm font-normal text-muted">({{ data.vatPham.ruong.length }})</span>
            </h4>
            <AdminNhanvatBangVatPham :ds="data.vatPham.ruong" trong="Rương rỗng." cot="bag" :dang-xoa="dangXoa" @xoa="xoaVatPham" />
          </div>

          <p class="text-xs text-muted">
            Đá quý và nguyên liệu <em>không</em> nằm trong ba cột này nên không hiện ở đây — chúng ở bảng riêng.
          </p>
        </div>
      </UCard>
    </template>

    <AdminNhanvatXacNhan
      v-model:open="xn.open"
      :title="xn.title"
      :description="xn.description"
      :nhan-nut="xn.nhanNut"
      :mau="xn.mau"
      :dang-chay="dangChay"
      @xac-nhan="chay"
    />
  </div>
</template>
