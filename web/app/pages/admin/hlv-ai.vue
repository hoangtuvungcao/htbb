<script setup lang="ts">
/**
 * Bot / Huấn luyện viên AI: bot lấp máy chủ chơi như người (tên hiện tiền tố hlv_ai.tien_to_ten + tên, mặc định "ai "). Trang này chỉnh nhóm cấu hình hlv_ai
 * (lưu qua /api/admin/cau-hinh-he-thong/luu - có lịch sử, hoàn tác ở trang Cấu hình hệ thống), xem bot đang online và
 * kinh tế của bot (máy chủ game ghi: tob_hlv_ai_kinh_te_ngay / tob_hlv_ai_moc / tob_hlv_ai_chuyen).
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const toast = useToast()

interface Bot { taiKhoan: string, charId: number, ten: string, lop: number, tinhCach: string, cap: number, capMucTieu: number, mapId: number, tenMap: string, hoatDong: string, xu: number, online: boolean, vaoLuc: string | null, raLuc: string | null, capNhat: string | null }
interface Ngay { ngay: string, xuTao: number, xuCat: number, xuDot: number, xuTieu: number, xuNhanNguoi: number, xuTraNguoi: number, giaTriChoNguoi: number, soChuyen: number, soChan: number, vatHuy: number, tongXuBot: number, tongXuNguoi: number, soBot: number, soBotOnline: number }
interface Moc { luc: string, tongXuBot: number, tongXuNguoi: number, soBotOnline: number, soNguoiOnline: number }
interface Chuyen { luc: string, huong: number, kieu: string, bot: string, nguoi: string, xu: number, giaTri: number, biChan: boolean, chiTiet: string }
interface CapTheoNguoi { ngay: string, thap: number, cao: number, soNguoi: number, tbNguoi: number, caoNhatNguoi: number, tbTop: number, tbBot: number, caoNhatBot: number, botNang: number, capNhat: string | null }
interface KetQua { coBang: boolean, cauHinh: Record<string, { giaTri: string, macDinh: string, daLuu: boolean }>, capTheoNguoi: CapTheoNguoi | null, bot: Bot[], ngay: Ngay[], moc: Moc[], chuyen: Chuyen[] }

const { data, status, error, refresh } = useAsyncData('admin-hlv-ai', () => api<KetQua>('/api/admin/hlv-ai'), { server: false })
const hen = setInterval(() => { if (!dangSua.value) refresh() }, 30000)
onBeforeUnmount(() => clearInterval(hen))

const LOP = ['Charmander', 'Bulbasaur', 'Eevee', 'Squirtle', 'Pichu']
const TINH_CACH: Record<string, string> = { cay: 'Cày cấp', pvp: 'PvP', buon: 'Buôn bán', xa_hoi: 'Xã hội', thuong: 'Chơi thường' }
const VIEC: Record<string, string> = {
  nhiem_vu: 'Nhiệm vụ', cay: 'Cày quái', treo: 'Treo máy (Tự động)', gym: 'Gym', boss: 'Boss', pk: 'PK', dau_truong: 'Đấu trường',
  pho_ban: 'Phó bản', doi: 'Đội', chat: 'Chat', ban_be: 'Kết bạn', giao_dich: 'Giao dịch', cho: 'Chợ ký gửi',
  sinh_thai: 'Hệ sinh thái (câu, đào, nấu)', vuon: 'Vườn', nha_tre: 'Nhà trẻ', bat: 'Bắt Pokémon', nang_cap: 'Nâng cấp / đồ', su_kien: 'Khám phá, danh hiệu',
}

// ---------------------------------------------------------------- form
const dangSua = ref(false)
const f = reactive({
  bat: false, soLuong: 30, lich: Array.from({ length: 24 }, () => 50), lop: [20, 20, 20, 20, 20], tinhCach: [30, 15, 15, 20, 20],
  capThap: 10, capCao: 60, capTheoNguoi: true, banDo: '', viec: [] as string[],
  heSo: 60, tranXu: 300000, tranTui: 60, giaBan: 100, chuyenNguoi: 50000, chuyenTong: 500000, doiBot: 3, tienTo: 'ai ',
})
const lyDo = ref('')
const dangLuu = ref(false)

const so = (s: string | undefined, md: number) => { const n = Number(String(s ?? '').trim()); return Number.isFinite(n) ? n : md }
const ds = (s: string | undefined, n: number, md: number) => { const a = String(s ?? '').split(',').map(x => Number(x.trim())); return a.length === n && a.every(Number.isFinite) ? a : Array.from({ length: n }, () => md) }

function napForm() {
  const c = data.value?.cauHinh ?? {}
  const g = (k: string) => c[k]?.giaTri
  f.bat = g('hlv_ai.bat') === '1'
  f.soLuong = so(g('hlv_ai.so_luong'), 30)
  f.lich = ds(g('hlv_ai.lich_gio'), 24, 50)
  f.lop = ds(g('hlv_ai.ty_le_lop'), 5, 20)
  f.tinhCach = ds(g('hlv_ai.ty_le_tinh_cach'), 5, 20)
  const cap = ds(g('hlv_ai.cap_muc_tieu'), 2, 0)
  f.capThap = cap[0] ?? 10
  f.capCao = cap[1] ?? 60
  f.capTheoNguoi = g('hlv_ai.cap_theo_nguoi') !== '0'
  f.banDo = g('hlv_ai.ban_do') ?? ''
  f.viec = String(g('hlv_ai.hoat_dong') ?? '').split(',').map(x => x.trim()).filter(Boolean)
  f.heSo = so(g('hlv_ai.he_so_thu_nhap'), 60)
  f.tranXu = so(g('hlv_ai.tran_xu'), 300000)
  f.doiBot = so(g('hlv_ai.doi_bot_toi_da'), 3)
  f.tienTo = g('hlv_ai.tien_to_ten') ?? 'ai '
  f.tranTui = so(g('hlv_ai.tran_vat_pham'), 60)
  f.giaBan = so(g('hlv_ai.gia_ban_toi_thieu'), 100)
  f.chuyenNguoi = so(g('hlv_ai.chuyen_moi_nguoi_ngay'), 50000)
  f.chuyenTong = so(g('hlv_ai.chuyen_tong_ngay'), 500000)
}
watch(data, () => { if (!dangSua.value) napForm() }, { immediate: true })

function giaTriForm(): Record<string, string> {
  const viecDung = Object.keys(VIEC).filter(v => f.viec.includes(v))
  return {
    'hlv_ai.bat': f.bat ? '1' : '0',
    'hlv_ai.so_luong': String(Math.trunc(f.soLuong)),
    'hlv_ai.lich_gio': f.lich.map(x => Math.trunc(x)).join(','),
    'hlv_ai.ty_le_lop': f.lop.map(x => Math.trunc(x)).join(','),
    'hlv_ai.ty_le_tinh_cach': f.tinhCach.map(x => Math.trunc(x)).join(','),
    'hlv_ai.cap_muc_tieu': `${Math.trunc(f.capThap)},${Math.trunc(f.capCao)}`,
    'hlv_ai.cap_theo_nguoi': f.capTheoNguoi ? '1' : '0',
    'hlv_ai.ban_do': f.banDo.split(',').map(x => x.trim()).filter(Boolean).join(','),
    'hlv_ai.hoat_dong': viecDung.join(','),
    'hlv_ai.he_so_thu_nhap': String(Math.trunc(f.heSo)),
    'hlv_ai.tran_xu': String(Math.trunc(f.tranXu)),
    'hlv_ai.tran_vat_pham': String(Math.trunc(f.tranTui)),
    'hlv_ai.gia_ban_toi_thieu': String(Math.trunc(f.giaBan)),
    'hlv_ai.chuyen_moi_nguoi_ngay': String(Math.trunc(f.chuyenNguoi)),
    'hlv_ai.chuyen_tong_ngay': String(Math.trunc(f.chuyenTong)),
    'hlv_ai.doi_bot_toi_da': String(Math.trunc(f.doiBot)),
    'hlv_ai.tien_to_ten': f.tienTo,
  }
}

const loiTienTo = computed(() => loiTienToHlvAi(f.tienTo))
const xemTruoc = computed(() => ['minhanh', 'BeoMap', 'Pika99'].map(t => tenHienThiHlvAi(t, true, loiTienTo.value ? null : f.tienTo)))

async function luu(chiBat?: boolean) {
  if (chiBat === undefined && loiTienTo.value) { toast.add({ title: loiTienTo.value, color: 'error', icon: 'i-lucide-circle-alert' }); return }
  const moi = giaTriForm()
  if (chiBat !== undefined) moi['hlv_ai.bat'] = chiBat ? '1' : '0'
  const cu = data.value?.cauHinh ?? {}
  const thayDoi: Record<string, string> = {}
  for (const [k, v] of Object.entries(moi)) if ((cu[k]?.giaTri ?? '') !== v && (chiBat === undefined || k === 'hlv_ai.bat')) thayDoi[k] = v
  if (!Object.keys(thayDoi).length) { toast.add({ title: 'Không có gì thay đổi.', color: 'neutral' }); return }
  const ly = chiBat !== undefined 
    ? (chiBat ? 'Bật Huấn luyện viên AI' : 'Tắt Huấn luyện viên AI') 
    : (lyDo.value.trim().length >= 3 ? lyDo.value.trim() : 'Cập nhật cấu hình bot')
  dangLuu.value = true
  try {
    const kq = await api<{ message: string }>('/api/admin/cau-hinh-he-thong/luu', { method: 'POST', body: { thayDoi, lyDo: ly } })
    toast.add({ title: kq.message, color: 'success', icon: 'i-lucide-check' })
    dangSua.value = false
    lyDo.value = ''
    await refresh()
  }
  catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  }
  finally {
    dangLuu.value = false
  }
}

// ---------------------------------------------------------------- số liệu
const online = computed(() => (data.value?.bot ?? []).filter(b => b.online))
const tong = computed(() => data.value?.bot.length ?? 0)
const ngay = computed(() => data.value?.ngay ?? [])
const moc = computed(() => data.value?.moc ?? [])
const homNay = computed(() => ngay.value.at(-1))
const tongPhanTram = (a: number[]) => a.reduce((s, x) => s + Math.max(0, x), 0)
const fmt = (n: number) => n.toLocaleString('vi-VN')
const loc = ref('')
const dsOnline = computed(() => online.value.filter(b => !loc.value || `${b.ten} ${b.tenMap} ${b.hoatDong}`.toLowerCase().includes(loc.value.toLowerCase())))
const demViec = computed(() => {
  const m: Record<string, number> = {}
  for (const b of online.value) m[b.hoatDong] = (m[b.hoatDong] ?? 0) + 1
  return Object.entries(m).sort((a, b) => b[1] - a[1])
})
const gioHienTai = new Date().getHours()
</script>

<template>
  <AdminPage title="Bot / Huấn luyện viên AI">
    <template #right>
      <UBadge :color="f.bat ? 'success' : 'neutral'" variant="subtle" :icon="f.bat ? 'i-lucide-play' : 'i-lucide-pause'">
        <span class="hidden sm:inline">{{ f.bat ? 'Đang bật' : 'Đang tắt' }} · </span>{{ online.length }}/{{ tong }}<span class="hidden sm:inline"> online</span>
      </UBadge>
      <UButton v-if="!f.bat" color="success" icon="i-lucide-play" label="Bật" :loading="dangLuu" @click="luu(true)" />
      <UButton v-else color="error" variant="soft" icon="i-lucide-square" label="Tắt" :loading="dangLuu" @click="luu(false)" />
      <UButton color="neutral" variant="ghost" icon="i-lucide-refresh-cw" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <UAlert v-if="error" color="error" :title="loiApi(error)" class="mb-4" />
    <UAlert v-else-if="data && !data.coBang" color="warning" icon="i-lucide-database" class="mb-4"
      title="CSDL chưa có bảng tob_hlv_ai_* - chạy nâng cấp CSDL (vps/nang_cap_csdl.sh, khối them_hlv_ai.sql)." />

    <p class="mb-4 text-sm text-muted">
      Bot chơi qua giao thức thật như người chơi (tiến trình riêng <code>htbb-hlv-ai</code> trên VPS, không tự bật), tên luôn có
      tiền tố <b>"{{ chuanTienToHlvAi(f.tienTo) }}"</b> (đổi ở ô <i>Tiền tố tên bot</i> bên dưới). Bot không nạp, không mua gói, không chuyển / nhận lượng, không nhập giftcode. Máy chủ giữ lạm phát bằng hệ số
      thu nhập, trần xu / túi, giá rao tối thiểu và hạn mức bot → người thật mỗi ngày.
    </p>

    <!-- ===================================================== kinh tế -->
    <UCard class="mb-4">
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <b>Kinh tế của bot</b>
          <span v-if="homNay" class="text-sm text-muted">Hôm nay ({{ homNay.ngay }}): tạo {{ fmt(homNay.xuTao) }} · đốt {{ fmt(homNay.xuDot) }} · tiêu {{ fmt(homNay.xuTieu) }} · bot → người {{ fmt(homNay.giaTriChoNguoi) }} / {{ fmt(f.chuyenTong) }} · chặn {{ homNay.soChan }}</span>
        </div>
      </template>
      <p v-if="!ngay.length && !moc.length" class="text-sm text-muted">Chưa có số liệu - máy chủ game ghi khi có bot chơi.</p>
      <div v-else class="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
        <AdminBieuDoDuong tieu-de="Tổng xu đang giữ (ảnh chụp 10 phút)" :nhan="moc.map(m => m.luc.slice(5, 16))"
          :chuoi="[{ ten: 'Bot', gt: moc.map(m => m.tongXuBot) }, { ten: 'Người thật', gt: moc.map(m => m.tongXuNguoi) }]" />
        <AdminBieuDoDuong tieu-de="Xu bot theo ngày" :nhan="ngay.map(d => d.ngay.slice(5))"
          :chuoi="[{ ten: 'Tạo', gt: ngay.map(d => d.xuTao) }, { ten: 'Đốt (trần)', gt: ngay.map(d => d.xuDot) }, { ten: 'Tiêu', gt: ngay.map(d => d.xuTieu) }]" />
        <AdminBieuDoDuong tieu-de="Giá trị bot → người thật / ngày" :nhan="ngay.map(d => d.ngay.slice(5))"
          :chuoi="[{ ten: 'Giá trị', gt: ngay.map(d => d.giaTriChoNguoi) }]" :nguong="{ gt: f.chuyenTong, nhan: 'Hạn mức ngày' }" />
        <AdminBieuDoDuong tieu-de="Online (ảnh chụp 10 phút)" :nhan="moc.map(m => m.luc.slice(5, 16))"
          :chuoi="[{ ten: 'Bot', gt: moc.map(m => m.soBotOnline) }, { ten: 'Người thật', gt: moc.map(m => m.soNguoiOnline) }]" />
      </div>
      <details v-if="data?.chuyen.length" class="mt-4 text-sm">
        <summary class="cursor-pointer font-semibold text-primary">Chuyển giá trị bot ↔ người gần đây ({{ data.chuyen.length }})</summary>
        <div class="mt-2 max-h-80 overflow-auto">
          <table class="bang-wiki">
            <thead><tr><th>Lúc</th><th>Hướng</th><th>Kiểu</th><th>Bot</th><th>Người</th><th class="so">Xu</th><th class="so">Giá trị</th><th>Chặn</th><th>Chi tiết</th></tr></thead>
            <tbody>
              <tr v-for="(c, i) in data.chuyen" :key="i">
                <td>{{ c.luc.slice(5, 16) }}</td><td>{{ c.huong === 1 ? 'bot → người' : 'người → bot' }}</td><td>{{ c.kieu }}</td><td>{{ c.bot }}</td><td>{{ c.nguoi }}</td>
                <td class="so">{{ fmt(c.xu) }}</td><td class="so">{{ fmt(c.giaTri) }}</td><td>{{ c.biChan ? 'chặn' : '' }}</td><td>{{ c.chiTiet }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </details>
    </UCard>

    <!-- ===================================================== bot online -->
    <UCard class="mb-4">
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <b>Bot đang online ({{ online.length }})</b>
          <span class="text-xs text-muted">{{ demViec.map(([v, n]) => `${VIEC[v] ?? v} ${n}`).join(' · ') }}</span>
          <UInput v-model="loc" size="sm" icon="i-lucide-search" placeholder="Lọc tên / bản đồ / việc" class="w-full sm:ml-auto sm:w-56" />
        </div>
      </template>
      <p v-if="!dsOnline.length" class="text-sm text-muted">Không có bot online.</p>
      <!-- Điện thoại: mỗi bot một dòng thẻ (bảng 8 cột phải cuộn ngang) -->
      <ul v-if="dsOnline.length" class="max-h-[32rem] divide-y divide-default overflow-y-auto text-sm sm:hidden">
        <li v-for="b in dsOnline" :key="b.taiKhoan" class="py-2">
          <div class="flex items-baseline justify-between gap-2">
            <span class="min-w-0 truncate font-semibold text-highlighted">{{ b.ten }}</span>
            <span class="shrink-0 tabular-nums">cấp {{ b.cap }} / {{ b.capMucTieu }}</span>
          </div>
          <p class="mt-0.5 text-xs text-muted">
            {{ VIEC[b.hoatDong] ?? b.hoatDong }} · {{ b.tenMap || b.mapId }}
          </p>
          <p class="text-xs text-dimmed">
            {{ LOP[b.lop] ?? b.lop }} · {{ TINH_CACH[b.tinhCach] ?? b.tinhCach }} · {{ fmt(b.xu) }} xu<template v-if="b.vaoLuc"> · vào {{ b.vaoLuc.slice(11, 16) }}</template>
          </p>
        </li>
      </ul>
      <div v-if="dsOnline.length" class="hidden max-h-96 overflow-auto sm:block">
        <table class="bang-wiki">
          <thead><tr><th>Tên</th><th class="so">Cấp / mục tiêu</th><th>Dòng</th><th>Tính cách</th><th>Bản đồ</th><th>Đang làm</th><th class="so">Xu</th><th>Vào lúc</th></tr></thead>
          <tbody>
            <tr v-for="b in dsOnline" :key="b.taiKhoan">
              <td>{{ b.ten }}</td><td class="so">{{ b.cap }} / {{ b.capMucTieu }}</td><td>{{ LOP[b.lop] ?? b.lop }}</td><td>{{ TINH_CACH[b.tinhCach] ?? b.tinhCach }}</td>
              <td>{{ b.tenMap || b.mapId }}</td><td>{{ VIEC[b.hoatDong] ?? b.hoatDong }}</td><td class="so">{{ fmt(b.xu) }}</td><td>{{ b.vaoLuc?.slice(11, 16) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>

    <!-- ===================================================== cấu hình -->
    <UCard>
      <template #header>
        <div class="flex flex-wrap items-center gap-3">
          <b>Cấu hình</b>
          <span class="text-xs text-muted">Lưu vào bảng cau_hinh (nhóm hlv_ai) - dịch vụ bot đọc lại mỗi 20 giây, máy chủ game ≤ 10 giây. Lịch sử / hoàn tác: trang Cấu hình hệ thống.</span>
        </div>
      </template>
      <div class="grid grid-cols-1 gap-6 lg:grid-cols-2" @input="dangSua = true" @change="dangSua = true">
        <section class="space-y-3">
          <h3 class="font-semibold">Số lượng và lịch</h3>
          <UFormField label="Số Huấn luyện viên AI (giờ 100% = ngần này bot online)">
            <UInput v-model.number="f.soLuong" type="number" :min="0" :max="500" class="w-32" />
          </UFormField>
          <div>
            <div class="mb-1 text-sm">Lịch online theo giờ (% số bot) - giờ hiện tại: {{ gioHienTai }}h</div>
            <div class="flex h-24 items-end gap-0.5 rounded border border-default p-1">
              <div v-for="(v, g) in f.lich" :key="g" class="flex-1 rounded-t" :class="g === gioHienTai ? 'bg-primary' : 'bg-primary/40'" :style="{ height: `${Math.max(2, Math.min(100, v))}%` }" :title="`${g}h: ${v}%`" />
            </div>
            <div class="mt-2 grid grid-cols-4 gap-1 min-[400px]:grid-cols-6 sm:grid-cols-12">
              <UInput v-for="(_, g) in f.lich" :key="g" v-model.number="f.lich[g]" type="number" :min="0" :max="100" size="xs">
                <template #leading><span class="text-[10px] text-muted">{{ g }}h</span></template>
              </UInput>
            </div>
          </div>
          <h3 class="pt-2 font-semibold">Dòng khởi đầu (trọng số) - tổng {{ tongPhanTram(f.lop) }}</h3>
          <div class="grid grid-cols-3 gap-2 sm:grid-cols-5">
            <UFormField v-for="(t, i) in LOP" :key="t" :label="t" class="min-w-0" :ui="{ label: 'truncate' }"><UInput v-model.number="f.lop[i]" type="number" :min="0" :max="100" size="sm" /></UFormField>
          </div>
          <h3 class="pt-2 font-semibold">Tính cách (trọng số) - tổng {{ tongPhanTram(f.tinhCach) }}</h3>
          <div class="grid grid-cols-3 gap-2 sm:grid-cols-5">
            <UFormField v-for="(t, i) in Object.values(TINH_CACH)" :key="t" :label="t" class="min-w-0" :ui="{ label: 'truncate' }"><UInput v-model.number="f.tinhCach[i]" type="number" :min="0" :max="100" size="sm" /></UFormField>
          </div>
          <h3 class="pt-2 font-semibold">Tên bot</h3>
          <UFormField label="Tiền tố tên bot (1–12 ký tự, phải có AI hoặc BOT; được dính liền tên)" :error="loiTienTo ?? undefined">
            <UInput v-model="f.tienTo" :maxlength="12" class="w-48" placeholder="ai " />
          </UFormField>
          <p class="text-sm">Xem trước: <span v-for="t in xemTruoc" :key="t" class="mr-3 rounded bg-elevated px-2 py-0.5 font-mono">{{ t }}</span></p>
          <h3 class="pt-2 font-semibold">Cấp mục tiêu và bản đồ</h3>
          <UCheckbox :model-value="f.capTheoNguoi" @update:model-value="(x: boolean | 'indeterminate') => { dangSua = true; f.capTheoNguoi = x === true }" label="Cấp mục tiêu theo người chơi thật (mỗi ngày: thấp = 25% người thật hoạt động 7 ngày, cao = trung bình top 10 trừ 2)" />
          <p v-if="f.capTheoNguoi" class="text-sm text-muted">
            <template v-if="data?.capTheoNguoi">
              Đang dùng <b>cấp {{ data.capTheoNguoi.thap }}–{{ data.capTheoNguoi.cao }}</b> theo người chơi thật
              ({{ data.capTheoNguoi.soNguoi }} người hoạt động 7 ngày: trung bình cấp {{ data.capTheoNguoi.tbNguoi }}, cao nhất {{ data.capTheoNguoi.caoNhatNguoi }},
              top 10 trung bình {{ data.capTheoNguoi.tbTop }}; bot trung bình {{ data.capTheoNguoi.tbBot }}, cao nhất {{ data.capTheoNguoi.caoNhatBot }};
              {{ data.capTheoNguoi.botNang }} bot được nâng / chặn mục tiêu hôm nay - tính ngày {{ data.capTheoNguoi.ngay }}).
              Bot không cày quá cấp {{ data.capTheoNguoi.cao }}; ô "cao" dưới đây là trần.
            </template>
            <template v-else>Dịch vụ bot chưa tính (chưa chạy hôm nay, hoặc chưa có người chơi thật hoạt động 7 ngày - khi đó dùng khoảng cố định dưới đây).</template>
          </p>
          <div class="flex flex-wrap gap-3">
            <UFormField :label="f.capTheoNguoi ? 'Cấp mục tiêu thấp (khi chưa có người thật)' : 'Cấp mục tiêu thấp'"><UInput v-model.number="f.capThap" type="number" :min="1" :max="200" class="w-24" /></UFormField>
            <UFormField :label="f.capTheoNguoi ? 'Cấp mục tiêu cao (trần)' : 'Cấp mục tiêu cao'"><UInput v-model.number="f.capCao" type="number" :min="1" :max="200" class="w-24" /></UFormField>
            <UFormField label="Bãi luyện ưu tiên (id bản đồ, dấu phẩy; trống = rải đều)" class="grow"><UInput v-model="f.banDo" placeholder="vd 1,2,5" /></UFormField>
          </div>
        </section>
        <section class="space-y-3">
          <h3 class="font-semibold">Hoạt động được bật</h3>
          <div class="grid grid-cols-2 gap-1 sm:grid-cols-3">
            <UCheckbox v-for="(t, v) in VIEC" :key="v" :model-value="f.viec.includes(v)" :label="t"
              @update:model-value="(x: boolean | 'indeterminate') => { dangSua = true; f.viec = x === true ? [...f.viec, v] : f.viec.filter(y => y !== v) }" />
          </div>
          <h3 class="pt-2 font-semibold">Chống lạm phát (máy chủ áp theo cờ bot của tài khoản)</h3>
          <div class="grid grid-cols-2 gap-3">
            <UFormField label="Hệ số thu nhập xu (%)" help="Xu từ quái / nhiệm vụ / thưởng bot nhận = % người thật"><UInput v-model.number="f.heSo" type="number" :min="0" :max="100" /></UFormField>
            <UFormField label="Trần xu mỗi bot" help="Phần vượt bị đốt"><UInput v-model.number="f.tranXu" type="number" :min="0" /></UFormField>
            <UFormField label="Trần ô túi mỗi bot" help="Ô thừa bị huỷ mỗi phút"><UInput v-model.number="f.tranTui" type="number" :min="0" :max="500" /></UFormField>
            <UFormField label="Giá rao tối thiểu (% trung vị)" help="Bot không bán phá giá"><UInput v-model.number="f.giaBan" type="number" :min="50" :max="1000" /></UFormField>
            <UFormField label="Bot → mỗi người thật / ngày (xu)" help="0 = cấm"><UInput v-model.number="f.chuyenNguoi" type="number" :min="0" /></UFormField>
            <UFormField label="Bot → người thật / ngày, cả máy chủ"><UInput v-model.number="f.chuyenTong" type="number" :min="0" /></UFormField>
            <UFormField label="Số đội do bot làm chủ tối đa" help="Bot cấp 50 lập đội miễn 100 triệu xu khi còn dưới số này"><UInput v-model.number="f.doiBot" type="number" :min="0" :max="50" /></UFormField>
          </div>
          <div class="flex flex-wrap items-end gap-2 pt-2">
            <UFormField label="Lý do thay đổi (ghi lịch sử)" class="grow" help="Để trống sẽ tự động ghi 'Cập nhật cấu hình bot'"><UInput v-model="lyDo" placeholder="vd: đổi tên bot, tăng bot giờ tối..." /></UFormField>
            <UButton icon="i-lucide-save" label="Lưu cấu hình" :loading="dangLuu" :disabled="!!loiTienTo" @click="luu()" />
            <UButton color="neutral" variant="ghost" label="Bỏ thay đổi" @click="dangSua = false; napForm()" />
          </div>
        </section>
      </div>
    </UCard>
  </AdminPage>
</template>
