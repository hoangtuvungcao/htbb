<script setup lang="ts">
/**
 * Chỉ số VPS (toàn máy, không riêng game): CPU tổng + từng nhân, load, uptime, RAM, swap, dung lượng từng hệ tệp, đọc/ghi đĩa,
 * mạng từng card, top tiến trình, trạng thái dịch vụ systemd. Web tự lấy mẫu mỗi 2 giây (server/utils/vpsLayMau.ts);
 * trang hỏi GET /api/admin/chi-so-vps?sau=<mốc> mỗi 2 giây (chỉ khi tab đang hiện) để vẽ trực tiếp 30 phút;
 * khoảng 6 giờ / 24 giờ / 7 ngày đọc GET /api/admin/chi-so-vps/lich-su (gộp mỗi phút, bảng tob_chi_so_vps).
 * Ngưỡng màu: shared/utils/vpsNguong.ts. Số nào hệ điều hành không cung cấp (vd load trên Windows) -> "không hỗ trợ…".
 */
import type { DiemVps, MauVps } from '~~/server/utils/vpsThuan'
import { NGUONG_VPS, mucCpu, mucDia, mucRamSan, mucSwap, mucTai, type MucVps } from '~~/shared/utils/vpsNguong'

const api = useAdminApi()
const KHONG_HT = 'không hỗ trợ trên hệ điều hành này'

interface TraVe { dangLayMau: boolean, chuKyMs: number, batDau: number, moiNhat: MauVps | null, diem: DiemVps[] }
interface DiemLs {
  t: number, cpuTb: number | null, cpuMax: number | null, tai1: number | null, ramTongMb: number, ramDungMb: number, ramSanMb: number
  swapTongMb: number | null, swapDungMb: number | null, swapVaoS: number | null, swapRaS: number | null
  docBs: number | null, ghiBs: number | null, vaoBs: number | null, raBs: number | null, goiVaoS: number | null, goiRaS: number | null, diaMaxPt: number | null
}
interface LichSu { bangChuaCo: boolean, gio: number, bucketGiay: number, may: string, homNay: { mangVaoB: number, mangRaB: number, diaDocB: number, diaGhiB: number, soPhut: number } | null, diem: DiemLs[] }

const moiNhat = ref<MauVps | null>(null)
const dangLayMau = ref(true)
const diem = ref<DiemVps[]>([])
const loi = ref<unknown>(null)
const daTai = ref(false)
let dangHoi = false

async function hoi() {
  if (dangHoi) return
  dangHoi = true
  try {
    const sau = diem.value.at(-1)?.t ?? 0
    const r = await api<TraVe>('/api/admin/chi-so-vps', { query: sau ? { sau } : {} })
    dangLayMau.value = r.dangLayMau
    moiNhat.value = r.moiNhat
    if (r.diem.length) diem.value = [...diem.value, ...r.diem].slice(-900)
    loi.value = null
  }
  catch (e) { loi.value = e }
  finally { dangHoi = false; daTai.value = true }
}

const khoang = ref<'truc_tiep' | 1 | 6 | 24 | 168>('truc_tiep')
const { data: ls, status: lsStatus, refresh: lsTaiLai } = useAsyncData('admin-vps-lich-su',
  () => api<LichSu>('/api/admin/chi-so-vps/lich-su', { query: { gio: khoang.value === 'truc_tiep' ? 24 : khoang.value } }), { server: false, watch: [khoang] })

onMounted(hoi)
useIntervalFn(() => { if (document.visibilityState === 'visible') hoi() }, 2000)
useIntervalFn(() => { if (document.visibilityState === 'visible') lsTaiLai() }, 60_000)

// ---- định dạng ----
const kieuByte = (b: number | null | undefined, giay = false) => {
  if (b === null || b === undefined || !Number.isFinite(b)) return '—'
  const d = ['B', 'KB', 'MB', 'GB', 'TB']
  let v = b
  let i = 0
  while (Math.abs(v) >= 1024 && i < d.length - 1) { v /= 1024; i++ }
  return `${v.toLocaleString('vi-VN', { maximumFractionDigits: v < 10 && i > 0 ? 1 : 0 })} ${d[i]}${giay ? '/s' : ''}`
}
const mb = (v: number | null | undefined) => (v === null || v === undefined ? '—' : v >= 1024 ? `${(v / 1024).toLocaleString('vi-VN', { maximumFractionDigits: 1 })} GB` : `${so(v)} MB`)
const thapPhan = (v: number | null | undefined, n = 1) => (v === null || v === undefined ? '—' : v.toLocaleString('vi-VN', { maximumFractionDigits: n }))
function thoiGianChay(g: number | null) {
  if (g === null) return KHONG_HT
  const n = Math.floor(g / 86400)
  const h = Math.floor((g % 86400) / 3600)
  const p = Math.floor((g % 3600) / 60)
  return n > 0 ? `${n} ngày ${h} giờ` : h > 0 ? `${h} giờ ${p} phút` : `${p} phút`
}
const hai = (n: number) => String(n).padStart(2, '0')
function gioPhut(t: number, giay = false, ngay = false) {
  const d = new Date(t)
  const hm = `${hai(d.getHours())}:${hai(d.getMinutes())}${giay ? `:${hai(d.getSeconds())}` : ''}`
  return ngay ? `${hai(d.getDate())}/${hai(d.getMonth() + 1)} ${hm}` : hm
}
function tuLuc(t: number | null) {
  if (t === null) return ''
  const g = Math.max(0, Math.round((Date.now() - t) / 1000))
  return `${gioPhut(t, false, g > 86400)} (${thoiGianChay(g)} trước)`
}

// ---- ô số liệu ----
const m = computed(() => moiNhat.value)
const tongMang = computed(() => {
  const ds = (m.value?.mang ?? []).filter(n => !n.ao)
  return { vao: ds.reduce((t, n) => t + n.vaoBs, 0), ra: ds.reduce((t, n) => t + n.raBs, 0), goiVao: ds.reduce((t, n) => t + n.goiVaoS, 0), goiRa: ds.reduce((t, n) => t + n.goiRaS, 0) }
})
/** Card thật luôn hiện; card ảo chỉ hiện khi đang có lưu lượng (máy có Docker có hàng chục veth). */
const mangHien = computed(() => (m.value?.mang ?? []).filter(n => !n.ao || n.vaoBs + n.raBs > 0))
const swapPt = computed(() => (m.value?.swap && m.value.swap.tongMb > 0 ? Math.round((m.value.swap.dungMb / m.value.swap.tongMb) * 100) : null))
const diaDayNhat = computed(() => [...(m.value?.dia ?? [])].sort((a, b) => b.pt - a.pt)[0] ?? null)
const ramPt = computed(() => (m.value && m.value.ram.tongMb > 0 ? Math.round((m.value.ram.dungMb / m.value.ram.tongMb) * 100) : 0))
const chuCache = (v: number | null) => (v === null ? '' : ` · cache ${mb(v)}`)
const tom = computed(() => {
  const x = m.value
  if (!x) return []
  const muc = (s: MucVps) => s
  return [
    { label: 'CPU toàn máy', value: x.cpu === null ? '…' : `${thapPhan(x.cpu)}%`, note: `${x.soNhan} nhân`, mau: muc(x.cpu === null ? '' : mucCpu(x.cpu)), icon: 'i-lucide-cpu' },
    { label: 'Tải (1 / 5 / 15 phút)', value: x.tai ? x.tai.map(v => thapPhan(v, 2)).join(' / ') : '—', note: x.tai ? `ngưỡng ${thapPhan(x.soNhan * NGUONG_VPS.taiMoiNhan)} (nhân × ${NGUONG_VPS.taiMoiNhan})` : KHONG_HT, mau: muc(x.tai ? mucTai(x.tai[0], x.soNhan) : ''), icon: 'i-lucide-gauge' },
    { label: 'RAM còn dùng được', value: mb(x.ram.sanMb), note: `đã dùng ${mb(x.ram.dungMb)} / ${mb(x.ram.tongMb)} (${ramPt.value}%)${chuCache(x.ram.bufCacheMb)}`, mau: muc(mucRamSan(x.ram.sanMb)), icon: 'i-lucide-memory-stick' },
    { label: 'Swap đã dùng', value: x.swap ? (x.swap.tongMb > 0 ? `${swapPt.value}%` : 'không có swap') : '—', note: x.swap ? `${mb(x.swap.dungMb)} / ${mb(x.swap.tongMb)} · vào ${thapPhan(x.swap.vaoTrangS)} / ra ${thapPhan(x.swap.raTrangS)} trang/s` : KHONG_HT, mau: muc(x.swap ? mucSwap(x.swap.dungMb, x.swap.tongMb) : ''), icon: 'i-lucide-arrow-left-right' },
    { label: 'Đĩa đầy nhất', value: diaDayNhat.value ? `${thapPhan(diaDayNhat.value.pt)}%` : '—', note: diaDayNhat.value ? `${diaDayNhat.value.diem} · còn ${mb(diaDayNhat.value.tongMb - diaDayNhat.value.dungMb)}` : KHONG_HT, mau: muc(diaDayNhat.value ? mucDia(diaDayNhat.value.pt) : ''), icon: 'i-lucide-hard-drive' },
    { label: 'Mạng vào / ra', value: x.mang ? `${kieuByte(tongMang.value.vao, true)}` : '—', note: x.mang ? `ra ${kieuByte(tongMang.value.ra, true)} · ${so(tongMang.value.goiVao)} / ${so(tongMang.value.goiRa)} gói/s` : KHONG_HT, mau: muc(''), icon: 'i-lucide-network' },
    { label: 'Đĩa đọc / ghi', value: x.diaIo ? kieuByte(x.diaIo.docBs, true) : '—', note: x.diaIo ? `ghi ${kieuByte(x.diaIo.ghiBs, true)}` : KHONG_HT, mau: muc(''), icon: 'i-lucide-database' },
    { label: 'Máy chạy liên tục', value: x.uptime === null ? '—' : thoiGianChay(x.uptime), note: `${x.may} · ${x.heDieuHanh}`, mau: muc(''), icon: 'i-lucide-clock' },
  ] as { label: string, value: string, note: string, mau: MucVps, icon: string }[]
})

// ---- biểu đồ ----
const laTrucTiep = computed(() => khoang.value === 'truc_tiep')
const bd = computed(() => {
  if (laTrucTiep.value) {
    const d = diem.value
    return {
      nhan: d.map(p => gioPhut(p.t, true)),
      cpu: [{ ten: 'CPU', gt: d.map(p => p.cpu) }],
      tai: [{ ten: 'Tải 1 phút', gt: d.map(p => p.tai1) }],
      ram: [{ ten: 'Đã dùng', gt: d.map(p => p.ramDungMb) }, { ten: 'Còn dùng được', gt: d.map(p => p.ramSanMb) }],
      swap: [{ ten: 'Swap dùng', gt: d.map(p => p.swapDungMb) }],
      mang: [{ ten: 'Vào', gt: d.map(p => p.vaoBs) }, { ten: 'Ra', gt: d.map(p => p.raBs) }],
      dia: [{ ten: 'Đọc', gt: d.map(p => p.docBs) }, { ten: 'Ghi', gt: d.map(p => p.ghiBs) }],
    }
  }
  const d = ls.value?.diem ?? []
  const dai = (ls.value?.gio ?? 0) > 24
  return {
    nhan: d.map(p => gioPhut(p.t, false, dai)),
    cpu: [{ ten: 'Trung bình', gt: d.map(p => p.cpuTb) }, { ten: 'Cao nhất', gt: d.map(p => p.cpuMax) }],
    tai: [{ ten: 'Tải 1 phút', gt: d.map(p => p.tai1) }],
    ram: [{ ten: 'Đã dùng (cao nhất)', gt: d.map(p => p.ramDungMb) }, { ten: 'Còn dùng được (thấp nhất)', gt: d.map(p => p.ramSanMb) }],
    swap: [{ ten: 'Swap dùng', gt: d.map(p => p.swapDungMb) }],
    mang: [{ ten: 'Vào', gt: d.map(p => p.vaoBs) }, { ten: 'Ra', gt: d.map(p => p.raBs) }],
    dia: [{ ten: 'Đọc', gt: d.map(p => p.docBs) }, { ten: 'Ghi', gt: d.map(p => p.ghiBs) }],
  }
}) as ComputedRef<Record<'cpu' | 'tai' | 'ram' | 'swap' | 'mang' | 'dia', { ten: string, gt: number[] }[]> & { nhan: string[] }>
const nguongTai = computed(() => (m.value?.tai ? { gt: Math.round(m.value.soNhan * NGUONG_VPS.taiMoiNhan * 100) / 100, nhan: `nhân × ${NGUONG_VPS.taiMoiNhan}` } : null))

const mauTrangThai = (s: string) => (s === 'active' ? 'success' : s === 'failed' ? 'error' : s === 'activating' || s === 'deactivating' || s === 'reloading' ? 'warning' : 'neutral')
const chuTrangThai: Record<string, string> = {
  active: 'đang chạy', failed: 'LỖI, đã dừng', inactive: 'đã tắt', activating: 'đang khởi động', deactivating: 'đang tắt', reloading: 'đang nạp lại', khong_cai: 'chưa cài', unknown: 'không rõ',
}
const mauMuc = (s: MucVps) => (s === 'bad' ? 'text-error' : s === 'warn' ? 'text-warning' : '')
const mauThanh = (s: MucVps) => (s === 'bad' ? 'bg-error' : s === 'warn' ? 'bg-warning' : 'bg-info')

defineExpose({ refresh: () => { hoi(); lsTaiLai() } })
</script>

<template>
  <div class="space-y-4">
    <div class="flex flex-wrap items-end justify-between gap-2">
      <p class="text-sm text-muted">
        Toàn bộ máy chủ (game, staging, bot AI, MariaDB, web, nginx). Web tự lấy mẫu mỗi 2 giây, giữ 30 phút trong bộ nhớ và ghi mỗi phút một dòng
        (giữ 7 ngày). Trực tiếp: tự cập nhật mỗi 2 giây.
      </p>
      <USelect v-model="khoang" :items="[{ label: '30 phút (trực tiếp)', value: 'truc_tiep' }, { label: '1 giờ', value: 1 }, { label: '6 giờ', value: 6 }, { label: '24 giờ', value: 24 }, { label: '7 ngày', value: 168 }]"
        size="sm" class="w-44" aria-label="Khoảng thời gian biểu đồ VPS" />
    </div>

    <UAlert v-if="loi" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được chỉ số VPS" :description="loiApi(loi)" />
    <UAlert v-else-if="daTai && !dangLayMau" color="warning" variant="subtle" icon="i-lucide-pause-circle" title="Bộ lấy mẫu VPS đang tắt"
      description="HTBB_CHI_SO_VPS=0 trong môi trường của web. Bỏ dòng đó (hoặc đặt 1) rồi khởi động lại web để bật." />

    <!-- điện thoại: một cột (giá trị như "2,01 / 3,68 / 7,93" không vừa nửa màn 360 px) -->
    <div v-if="m" class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <AdminStatCard v-for="o in tom" :key="o.label" :label="o.label" :value="o.value" :note="o.note" :mau="o.mau" :icon="o.icon" />
    </div>
    <div v-else-if="!loi" class="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
      <USkeleton v-for="i in 8" :key="i" class="h-24 rounded-xl" />
    </div>

    <UCard v-if="m">
      <template #header>
        <h2 class="font-semibold text-highlighted">CPU từng nhân</h2>
      </template>
      <p v-if="!m.cpuMoiNhan" class="text-sm text-muted">Đang đo (cần hai lần đọc cách nhau 2 giây)…</p>
      <div v-else class="grid gap-2 sm:grid-cols-2">
        <div v-for="(v, i) in m.cpuMoiNhan" :key="i" class="flex items-center gap-2 text-sm">
          <span class="w-14 shrink-0 text-muted">Nhân {{ i }}</span>
          <div class="h-2.5 flex-1 overflow-hidden rounded bg-elevated" role="meter" :aria-valuenow="v" aria-valuemin="0" aria-valuemax="100" :aria-label="`Nhân ${i}`">
            <div class="h-full rounded" :class="mauThanh(mucCpu(v))" :style="{ width: `${Math.min(100, v)}%` }" />
          </div>
          <span class="w-14 shrink-0 text-right tabular-nums" :class="mauMuc(mucCpu(v))">{{ thapPhan(v) }}%</span>
        </div>
      </div>
    </UCard>

    <UCard>
      <template #header>
        <h2 class="font-semibold text-highlighted">
          Biểu đồ {{ laTrucTiep ? `30 phút gần nhất (${so(diem.length)} điểm, 2 giây/điểm)` : `${khoang === 168 ? '7 ngày' : `${khoang} giờ`} (mỗi điểm gộp ${Math.round((ls?.bucketGiay ?? 60) / 60)} phút)` }}
        </h2>
        <p v-if="!laTrucTiep" class="text-xs text-muted">CPU, tải, tốc độ: trung bình trong khoảng gộp; RAM dùng / swap: cao nhất; RAM còn: thấp nhất.</p>
      </template>
      <UAlert v-if="!laTrucTiep && ls?.bangChuaCo" color="neutral" variant="subtle" icon="i-lucide-info" title="Chưa có bảng tob_chi_so_vps"
        description="Web tự tạo bảng khi ghi phút đầu tiên (cần quyền CREATE trên htbb_db), hoặc chạy sql/htbb.sql (vps/nang_cap_csdl.sh)." />
      <div v-else-if="!laTrucTiep && lsStatus === 'pending' && !ls" class="grid gap-8 md:grid-cols-2">
        <USkeleton v-for="i in 4" :key="i" class="h-40" />
      </div>
      <div v-else class="grid gap-8 md:grid-cols-2">
        <AdminBieuDoDuong tieu-de="CPU toàn máy (%)" :nhan="bd.nhan" :chuoi="bd.cpu" don-vi="%" :nguong="{ gt: NGUONG_VPS.cpuPt, nhan: `${NGUONG_VPS.cpuPt}%` }" />
        <AdminBieuDoDuong v-if="m?.tai !== null" tieu-de="Tải máy (load average)" :nhan="bd.nhan" :chuoi="bd.tai" :nguong="nguongTai" />
        <AdminBieuDoDuong tieu-de="RAM (MB)" :nhan="bd.nhan" :chuoi="bd.ram" don-vi=" MB" :nguong="{ gt: NGUONG_VPS.ramSanMb, nhan: `còn ${NGUONG_VPS.ramSanMb} MB` }" />
        <AdminBieuDoDuong v-if="m?.swap !== null" tieu-de="Swap đã dùng (MB)" :nhan="bd.nhan" :chuoi="bd.swap" don-vi=" MB" />
        <AdminBieuDoDuong v-if="m?.mang !== null" tieu-de="Mạng (byte/giây, không tính loopback)" :nhan="bd.nhan" :chuoi="bd.mang" don-vi=" B/s" />
        <AdminBieuDoDuong v-if="m?.diaIo !== null" tieu-de="Đĩa đọc / ghi (byte/giây)" :nhan="bd.nhan" :chuoi="bd.dia" don-vi=" B/s" />
      </div>
    </UCard>

    <div v-if="m" class="grid gap-4 xl:grid-cols-2">
      <UCard>
        <template #header>
          <h2 class="font-semibold text-highlighted">Dung lượng đĩa</h2>
          <p class="text-xs text-muted">Vàng &gt; {{ NGUONG_VPS.diaPt }}%, đỏ &gt; {{ NGUONG_VPS.diaPtDo }}%.</p>
        </template>
        <p v-if="!m.dia" class="text-sm text-muted">{{ KHONG_HT }}</p>
        <ul v-else class="space-y-3">
          <li v-for="d in m.dia" :key="d.diem" class="text-sm">
            <div class="flex flex-wrap items-baseline justify-between gap-x-2">
              <span class="font-mono font-semibold text-highlighted">{{ d.diem }} <span class="font-sans text-xs font-normal text-muted">{{ d.loai }}</span></span>
              <span class="tabular-nums" :class="mauMuc(mucDia(d.pt))">{{ mb(d.dungMb) }} / {{ mb(d.tongMb) }} · {{ thapPhan(d.pt) }}%</span>
            </div>
            <div class="mt-1 h-2.5 overflow-hidden rounded bg-elevated" role="meter" :aria-valuenow="d.pt" aria-valuemin="0" aria-valuemax="100" :aria-label="d.diem">
              <div class="h-full rounded" :class="mauThanh(mucDia(d.pt))" :style="{ width: `${Math.min(100, d.pt)}%` }" />
            </div>
          </li>
        </ul>
        <div v-if="m.diaIo?.thietBi.length" class="mt-4 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted">
          <span v-for="t in m.diaIo.thietBi" :key="t.ten"><span class="font-mono">{{ t.ten }}</span>: đọc {{ kieuByte(t.docBs, true) }} · ghi {{ kieuByte(t.ghiBs, true) }}</span>
        </div>
      </UCard>

      <UCard>
        <template #header>
          <h2 class="font-semibold text-highlighted">Mạng</h2>
          <p v-if="ls?.homNay" class="text-xs text-muted">
            Hôm nay (từ 0 giờ, {{ so(ls.homNay.soPhut) }} phút đã ghi): vào {{ kieuByte(ls.homNay.mangVaoB) }} · ra {{ kieuByte(ls.homNay.mangRaB) }}
            · đĩa đọc {{ kieuByte(ls.homNay.diaDocB) }} · ghi {{ kieuByte(ls.homNay.diaGhiB) }}
          </p>
        </template>
        <p v-if="!m.mang" class="text-sm text-muted">{{ KHONG_HT }}</p>
        <div v-else class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="text-left text-xs text-muted">
              <tr><th class="py-1 pr-2 font-medium">Card</th><th class="py-1 pr-2 text-right font-medium">Vào</th><th class="py-1 pr-2 text-right font-medium">Ra</th><th class="hidden py-1 text-right font-medium sm:table-cell">Gói vào / ra /s</th></tr>
            </thead>
            <tbody>
              <tr v-for="n in mangHien" :key="n.ten" class="border-t border-default" :class="n.ao ? 'text-muted' : ''">
                <td class="py-1 pr-2 font-mono">{{ n.ten }}<span v-if="n.ao" class="ml-1 font-sans text-xs text-dimmed">(ảo, không cộng tổng)</span></td>
                <td class="py-1 pr-2 text-right tabular-nums">{{ kieuByte(n.vaoBs, true) }}</td>
                <td class="py-1 pr-2 text-right tabular-nums">{{ kieuByte(n.raBs, true) }}</td>
                <td class="hidden py-1 text-right tabular-nums sm:table-cell">{{ thapPhan(n.goiVaoS) }} / {{ thapPhan(n.goiRaS) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </UCard>
    </div>

    <UCard v-if="m">
      <template #header>
        <h2 class="font-semibold text-highlighted">Dịch vụ</h2>
        <p class="text-xs text-muted">{{ m.heDieuHanh === 'linux' ? 'systemctl show (10 giây/lần).' : 'Get-Service (5 giây/lần).' }}</p>
      </template>
      <p v-if="!m.dichVu" class="text-sm text-muted">{{ m.heDieuHanh === 'linux' ? 'Không đọc được systemctl trên máy này.' : KHONG_HT }}</p>
      <ul v-else class="grid gap-2 sm:grid-cols-2 xl:grid-cols-4">
        <li v-for="d in m.dichVu" :key="d.ten" class="rounded-lg border border-default p-3 text-sm">
          <div class="flex items-center justify-between gap-2">
            <span class="truncate font-mono text-highlighted">{{ d.ten.replace(/\.service$/, '') }}</span>
            <UBadge :color="mauTrangThai(d.trangThai)" variant="subtle" size="sm">{{ chuTrangThai[d.trangThai] ?? d.trangThai }}</UBadge>
          </div>
          <p v-if="d.tu" class="mt-1 text-xs text-muted">từ {{ tuLuc(d.tu) }}</p>
        </li>
      </ul>
    </UCard>

    <UCard v-if="m">
      <template #header>
        <h2 class="font-semibold text-highlighted">Tiến trình (theo RAM)</h2>
        <p class="text-xs text-muted">Dịch vụ HTBB, MariaDB, nginx luôn hiện; thêm top theo RAM và theo CPU. CPU % tính trên một nhân (bận hết {{ m.soNhan }} nhân = {{ m.soNhan * 100 }}%).</p>
      </template>
      <p v-if="!m.tienTrinh" class="text-sm text-muted">{{ m.heDieuHanh === 'win32' ? 'Đang chờ PowerShell (5 giây/lần)…' : KHONG_HT }}</p>
      <ul v-if="m.tienTrinh" class="divide-y divide-default text-sm sm:hidden">
        <li v-for="p in m.tienTrinh" :key="p.pid" class="py-2">
          <div class="flex items-baseline justify-between gap-2">
            <span class="min-w-0 truncate font-medium text-highlighted">{{ p.ten }}</span>
            <span class="shrink-0 tabular-nums">{{ mb(p.rssMb) }} · {{ thapPhan(p.cpu) }}%</span>
          </div>
          <p class="mt-0.5 truncate text-xs text-muted">
            <span class="font-mono">{{ p.lenh }}</span> · PID {{ p.pid }}<template v-if="p.nguoi"> · {{ p.nguoi }}</template>
          </p>
        </li>
      </ul>
      <div v-if="m.tienTrinh" class="hidden overflow-x-auto sm:block">
        <table class="w-full text-sm">
          <thead class="text-left text-xs text-muted">
            <tr>
              <th class="py-1 pr-2 font-medium">Tên</th><th class="hidden py-1 pr-2 font-medium sm:table-cell">PID</th><th class="hidden py-1 pr-2 font-medium md:table-cell">Người dùng</th>
              <th class="py-1 pr-2 text-right font-medium">RAM</th><th class="py-1 text-right font-medium">CPU</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in m.tienTrinh" :key="p.pid" class="border-t border-default">
              <td class="py-1 pr-2">
                <span class="font-medium text-highlighted">{{ p.ten }}</span>
                <span v-if="p.ten !== p.lenh" class="ml-1 font-mono text-xs text-muted">{{ p.lenh }}</span>
                <span v-if="p.donVi" class="ml-1 hidden font-mono text-xs text-dimmed lg:inline">{{ p.donVi }}</span>
              </td>
              <td class="hidden py-1 pr-2 font-mono tabular-nums sm:table-cell">{{ p.pid }}</td>
              <td class="hidden py-1 pr-2 md:table-cell">{{ p.nguoi ?? '—' }}</td>
              <td class="py-1 pr-2 text-right tabular-nums">{{ mb(p.rssMb) }}</td>
              <td class="py-1 text-right tabular-nums">{{ thapPhan(p.cpu) }}%</td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>
  </div>
</template>
