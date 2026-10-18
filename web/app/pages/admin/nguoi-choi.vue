<script setup lang="ts">
/**
 * Trung tâm người chơi: tìm tài khoản/nhân vật, hồ sơ (cấp, loài, xu/lượng, Pokédex, túi đồ, IP,
 * lần cuối vào game), khoá có lý do + thời hạn, cấm chat, gửi thư, đổi mật khẩu.
 */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const toast = useToast()
const me = useAdminMe()
const route = useRoute()
const router = useRouter()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const duocLam = computed(() => ['sieu_quan_tri', 'ho_tro'].includes(me.vaiTro.value))

const tim = ref('')
const { data: kq } = await useAsyncData('admin-nc-tim', () => tim.value.trim().length >= 2 ? api<{ taiKhoan: any[], nhanVat: any[] }>('/api/admin/nguoi-choi/tim', { query: { q: tim.value.trim() } }) : Promise.resolve({ taiKhoan: [], nhanVat: [] }), { server: false, watch: [refDebounced(tim, 300)] })

const u = computed(() => (typeof route.query.u === 'string' ? route.query.u : ''))
const { data: hs, refresh } = await useAsyncData('admin-nc-hs', () => u.value ? api<any>('/api/admin/nguoi-choi/chi-tiet', { query: { u: u.value } }) : Promise.resolve(null), { server: false, watch: [u] })
function mo(ten: string) { router.push({ query: { u: ten } }) }

const fKhoa = reactive({ lyDo: '', soGio: 24 })
const HAN = [{ label: '1 giờ', value: 1 }, { label: '24 giờ', value: 24 }, { label: '3 ngày', value: 72 }, { label: '7 ngày', value: 168 }, { label: '30 ngày', value: 720 }, { label: 'Vĩnh viễn', value: 0 }]
async function goi(url: string, body: Record<string, unknown>, hoi?: { title: string, nut: string, mau?: 'error' | 'warning' | 'primary' }) {
  if (hoi && !(await xacNhan.open({ ...hoi }).result)) return
  try {
    const r = await api<{ message?: string }>(url, { method: 'POST', body })
    toast.add({ title: r.message ?? 'Xong', color: 'success' })
    refresh()
  }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
const khoa = () => goi('/api/admin/nguoi-choi/khoa', { username: u.value, ...fKhoa }, { title: `Khoá ${u.value}?`, nut: 'Khoá', mau: 'error' })
const moKhoa = () => goi('/api/admin/nguoi-choi/mo-khoa', { username: u.value }, { title: `Mở khoá ${u.value}?`, nut: 'Mở khoá' })

const fChat = reactive<Record<number, { soGio: number, lyDo: string }>>({})
const fThu = reactive<Record<number, string>>({})
const camChat = (c: any, soGio: number) => goi('/api/admin/nguoi-choi/cam-chat', { charId: c.id, soGio, lyDo: fChat[c.id]?.lyDo ?? '' })
const guiThu = async (c: any) => { await goi('/api/admin/nguoi-choi/gui-thu', { charname: c.charname, noiDung: fThu[c.id] ?? '' }); fThu[c.id] = '' }
watch(hs, (v) => {
  for (const c of v?.nhanVat ?? []) { fChat[c.id] ??= { soGio: 24, lyDo: '' }; fThu[c.id] ??= '' }
}, { immediate: true })

const mk = reactive({ a: '', b: '' })
const doiMk = () => goi(`/api/admin/taikhoan/${hs.value?.tk.id}`, { viec: 'doi_mat_khau', matkhau: mk.a, matkhau2: mk.b }, { title: `Đặt mật khẩu mới cho ${u.value}?`, nut: 'Đổi', mau: 'warning' })
const so = (n: unknown) => Number(n ?? 0).toLocaleString('vi-VN')
</script>

<template>
  <AdminPage title="Trung tâm người chơi">
    <UInput v-model="tim" icon="i-lucide-search" size="lg" placeholder="Gõ tên tài khoản hoặc tên Pokémon (ít nhất 2 ký tự)..." class="w-full" autofocus />
    <div v-if="kq && (kq.taiKhoan.length || kq.nhanVat.length)" class="grid gap-4 lg:grid-cols-2">
      <UCard>
        <template #header><b>Tài khoản ({{ kq.taiKhoan.length }})</b></template>
        <ul class="divide-y divide-default">
          <li v-for="t in kq.taiKhoan" :key="t.id" class="flex items-center gap-2 py-1.5">
            <button type="button" class="font-semibold hover:text-primary" @click="mo(t.username)">{{ t.username }}</button>
            <UBadge v-if="Number(t.ban) === 1" color="error" variant="subtle" size="sm">Khoá</UBadge>
            <span class="ml-auto text-xs text-dimmed">{{ t.regdate }}</span>
          </li>
        </ul>
      </UCard>
      <UCard>
        <template #header><b>Pokémon ({{ kq.nhanVat.length }})</b></template>
        <ul class="divide-y divide-default">
          <li v-for="n in kq.nhanVat" :key="n.id" class="flex items-center gap-2 py-1.5">
            <PokeAvatar :dex="dexHienTai(n.class, n.lastLv, n.dex)" class="size-7" />
            <button type="button" class="font-semibold hover:text-primary" :disabled="!n.chu" @click="mo(n.chu)">{{ n.charname }}</button>
            <span class="text-xs text-muted">Lv {{ n.lastLv }} · {{ speciesName(n.class, n.lastLv, n.dex) }}</span>
            <span class="ml-auto text-xs text-dimmed">TK: {{ n.chu }}</span>
          </li>
        </ul>
      </UCard>
    </div>

    <template v-if="hs">
      <UCard>
        <div class="flex flex-wrap items-center gap-3">
          <UIcon name="i-lucide-user-round" class="size-8 text-primary" />
          <div>
            <p class="text-xl font-bold">{{ hs.tk.username }} <UBadge v-if="hs.tk.laAdmin" color="error" variant="subtle">Quản trị</UBadge></p>
            <p class="text-sm text-muted">#{{ hs.tk.id }} · đăng ký {{ hs.tk.regdate ?? '?' }} · email {{ hs.tk.email || '—' }}</p>
          </div>
          <UBadge :color="Number(hs.tk.ban) === 1 ? 'error' : 'success'" size="lg" class="ml-auto">{{ Number(hs.tk.ban) === 1 ? 'Đang bị khoá' : 'Hoạt động' }}</UBadge>
        </div>
      </UCard>

      <div class="grid gap-4 xl:grid-cols-3">
        <UCard v-if="duocLam && !hs.tk.laAdmin">
          <template #header><b>Khoá / mở tài khoản</b></template>
          <template v-if="Number(hs.tk.ban) !== 1">
            <UFormField label="Lý do"><UInput v-model="fKhoa.lyDo" placeholder="Dùng bot, lừa đảo..." class="w-full" /></UFormField>
            <UFormField label="Thời hạn" class="mt-2"><USelect v-model="fKhoa.soGio" :items="HAN" class="w-full" /></UFormField>
            <UButton class="mt-3" color="error" icon="i-lucide-lock" label="Khoá" :disabled="fKhoa.lyDo.length < 3" @click="khoa" />
          </template>
          <UButton v-else color="success" icon="i-lucide-lock-open" label="Mở khoá ngay" @click="moKhoa" />
          <p class="mt-2 text-xs text-dimmed">Hết hạn thì website tự mở khoá (kiểm mỗi 5 phút). Người đang online chơi tiếp tới khi thoát.</p>
        </UCard>
        <UCard v-if="duocLam">
          <template #header><b>Đặt mật khẩu mới</b></template>
          <UInput v-model="mk.a" type="password" placeholder="Mật khẩu mới" class="w-full" autocomplete="new-password" />
          <UInput v-model="mk.b" type="password" placeholder="Nhập lại" class="mt-2 w-full" autocomplete="new-password" />
          <UButton class="mt-3" color="warning" icon="i-lucide-key-round" label="Đổi mật khẩu" :disabled="!mk.a || mk.a !== mk.b" @click="doiMk" />
        </UCard>
        <UCard>
          <template #header><b>Máy & IP đăng nhập</b></template>
          <ul class="space-y-1 text-sm">
            <li v-for="m in hs.may" :key="`${m.ip}${m.lan_cuoi}`" class="flex justify-between gap-2"><span class="font-mono">{{ m.ip || '?' }}</span><span class="text-xs text-dimmed">{{ m.lan_cuoi }} · {{ m.so_lan }} lần</span></li>
            <li v-if="!hs.may.length" class="text-muted">Chưa có dữ liệu.</li>
          </ul>
        </UCard>
      </div>

      <UCard v-for="c in hs.nhanVat" :key="c.id">
        <template #header>
          <div class="flex flex-wrap items-center gap-3">
            <PokeAvatar :dex="c.dexHienTai" class="size-10" />
            <div>
              <b class="text-lg">{{ c.charname }}</b>
              <p class="text-sm text-muted">Lv {{ c.lastLv }} · {{ c.loai }} · vào game lần cuối {{ c.lastLog || '?' }}</p>
            </div>
            <UBadge v-if="c.online" color="success" variant="subtle">Đang online</UBadge>
            <UBadge v-if="c.camChatDen && c.camChatDen > new Date().toISOString().slice(0, 19).replace('T', ' ')" color="warning" variant="subtle">Cấm chat tới {{ c.camChatDen }}</UBadge>
            <UButton class="ml-auto" size="xs" color="neutral" variant="outline" :to="`/admin/nhanvat?id=${c.id}`" icon="i-lucide-backpack" label="Túi đồ & lệnh" />
          </div>
        </template>
        <div class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-7">
          <div><p class="text-xs text-muted">Xu</p><b>{{ so(c.gold) }}</b></div>
          <div><p class="text-xs text-muted">Lượng</p><b>{{ so(c.luong) }}</b></div>
          <div><p class="text-xs text-muted">Lượng khoá</p><b>{{ so(c.luonglock) }}</b></div>
          <div><p class="text-xs text-muted">Kho xu / lượng</p><b>{{ so(c.khoXu) }} / {{ so(c.khoLuong) }}</b></div>
          <div><p class="text-xs text-muted">Pokédex đã hạ</p><b>{{ c.pokedexDaHa }} loài</b></div>
          <div><p class="text-xs text-muted">Vật phẩm</p><b>{{ c.soVatPham.mac }} mặc · {{ c.soVatPham.tui }} túi · {{ c.soVatPham.ruong }} rương</b></div>
          <div><p class="text-xs text-muted">Thời gian chơi (ghi nhận)</p><b>{{ so(c.totalTimePlay) }}</b></div>
        </div>
        <div v-if="duocLam" class="mt-4 grid gap-4 border-t border-default pt-4 lg:grid-cols-2">
          <div>
            <p class="text-sm font-semibold">Cấm chat <span class="font-normal text-dimmed">(chỉ khi offline)</span></p>
            <div v-if="fChat[c.id]" class="mt-2 flex flex-wrap gap-2">
              <USelect v-model="fChat[c.id]!.soGio" :items="HAN.filter(h => h.value > 0)" class="w-32" />
              <UInput v-model="fChat[c.id]!.lyDo" placeholder="Lý do" class="min-w-40 flex-1" />
              <UButton color="warning" icon="i-lucide-message-square-off" label="Cấm" :disabled="(fChat[c.id]!.lyDo ?? '').length < 3" @click="camChat(c, fChat[c.id]!.soGio)" />
              <UButton color="neutral" variant="outline" label="Bỏ cấm" @click="camChat(c, 0)" />
            </div>
          </div>
          <div>
            <p class="text-sm font-semibold">Gửi thư trong game</p>
            <div class="mt-2 flex gap-2">
              <UInput v-model="fThu[c.id]" placeholder="Nội dung (tối đa 800 ký tự)" :maxlength="800" class="flex-1" />
              <UButton icon="i-lucide-send" label="Gửi" :disabled="!fThu[c.id]" @click="guiThu(c)" />
            </div>
          </div>
        </div>
      </UCard>

      <UCard v-if="hs.khoa.length">
        <template #header><b>Lịch sử khoá / cấm chat</b></template>
        <table class="bang-wiki">
          <thead><tr><th>Loại</th><th>Lý do</th><th>Hết hạn</th><th>Bởi</th><th>Lúc</th><th>Đã mở</th></tr></thead>
          <tbody>
            <tr v-for="k in hs.khoa" :key="k.id">
              <td>{{ k.loai === 'khoa' ? 'Khoá TK' : 'Cấm chat' }}</td><td>{{ k.ly_do }}</td><td>{{ k.het_han ?? 'Vĩnh viễn' }}</td><td>{{ k.admin_user }}</td><td class="text-xs">{{ k.tao_luc }}</td><td class="text-xs">{{ k.mo_luc ?? '—' }}</td>
            </tr>
          </tbody>
        </table>
      </UCard>
    </template>
  </AdminPage>
</template>
