<script setup lang="ts">
/**
 * Bản tải game: mỗi nền tảng (Android APK, PC Windows, PC Linux, macOS, Java .jar, iOS) có bật/tắt,
 * trạng thái "Sẵn sàng / Sắp ra mắt", kiểu link (tệp / TestFlight / App Store / manifest .ipa),
 * phiên bản, dung lượng, SHA-256, hệ điều hành tối thiểu, nhật ký thay đổi.
 * Tệp có thể tải thẳng lên kho /tai/ của website (ghi theo luồng, tự tính SHA-256) hoặc dán link ngoài.
 * Lưu vào web_cai_dat khoá 'taiGame' (server kiểm lại mọi trường: shared/utils/taiGame.ts loiBanTai).
 */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'
import type { BanTai } from '#shared/utils/taiGame'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const me = useAdminMe()
const toast = useToast()
const auth = useAdminAuth()
const api = useAdminApi()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const { f: fRef, luu, dangLuu, coBang, daThayDoi } = await useCaiDatAdmin('taiGame')
const f = fRef as Ref<{ ban: BanTai[] }>
const duocGhi = computed(() => me.duocGhiNoiDung.value)

const LUA_TRANG_THAI = [{ label: 'Sẵn sàng tải', value: 'san-sang' }, { label: 'Sắp ra mắt', value: 'sap-ra-mat' }]
const luaKieu = (ma: string) => (nenTang(ma)?.kieu ?? ['tep']).map(k => ({ label: TEN_KIEU_LINK[k], value: k }))
const loi = (b: BanTai) => loiBanTai(b)
const coLoi = computed(() => f.value.ban.some(b => !!loi(b)))

function doi(i: number, buoc: number) {
  const ds = f.value.ban
  const j = i + buoc
  if (j < 0 || j >= ds.length) return
  ;[ds[i], ds[j]] = [ds[j]!, ds[i]!]
}

// ---- kho tệp /tai/
interface Tep { ten: string, url: string, byte: number, sha256: string, luc: string }
const { data: kho, pending: dangTaiKho, error: loiKho, refresh: taiKho } = await useAsyncData('admin-tai-game-tep',
  () => api<{ ds: Tep[], toiDaMb: number }>('/api/admin/cms/tai-game/tep'), { server: false })

const dangGui = ref<string | null>(null)
const tienDo = ref(0)
/** Gửi tệp theo luồng (XHR để có % tiến độ). Xong -> điền url/dung lượng/SHA-256 vào bản tải `b`. */
async function taiLen(b: BanTai | null, e: Event) {
  const input = e.target as HTMLInputElement
  const tep = input.files?.[0]
  input.value = ''
  if (!tep) return
  const ten = tenTepGameAnToan(tep.name)
  if (!ten) { toast.add({ title: 'Đuôi tệp không được nhận (.apk .zip .jar .jad .exe .gz .tgz .AppImage .ipa .plist).', color: 'error' }); return }
  let ghiDe = false
  if (kho.value?.ds.some(t => t.ten === ten)) {
    if (!(await xacNhan.open({ title: `Ghi đè ${ten}?`, description: 'Đã có tệp cùng tên trong kho. Người đang tải bản cũ có thể bị lỗi giữa chừng.', nut: 'Ghi đè', mau: 'warning' }).result)) return
    ghiDe = true
  }
  dangGui.value = b?.ma ?? '_kho'
  tienDo.value = 0
  try {
    const kq = await new Promise<Tep>((ok, hong) => {
      const x = new XMLHttpRequest()
      x.open('POST', `/api/admin/cms/tai-game/tai-len?ten=${encodeURIComponent(ten)}${ghiDe ? '&ghiDe=1' : ''}`)
      x.setRequestHeader('Content-Type', 'application/octet-stream')
      if (auth.token.value) x.setRequestHeader('Authorization', `Bearer ${auth.token.value}`)
      x.upload.onprogress = (ev) => { if (ev.lengthComputable) tienDo.value = Math.round(ev.loaded * 100 / ev.total) }
      x.onload = () => {
        const t = x.getResponseHeader('x-htbb-token')
        if (t) auth.refresh(t)
        let j: any = null
        try { j = JSON.parse(x.responseText) } catch { /* không phải JSON */ }
        if (x.status >= 200 && x.status < 300 && j) ok(j)
        else hong(new Error(j?.message || j?.data?.message || `Lỗi ${x.status}`))
      }
      x.onerror = () => hong(new Error('Mất kết nối khi tải lên.'))
      x.send(tep)
    })
    toast.add({ title: `Đã tải lên ${kq.ten}`, description: `SHA-256 ${kq.sha256.slice(0, 16)}…`, color: 'success' })
    if (b) {
      b.url = kq.url
      b.kieu = b.kieu === 'ipa-manifest' && kq.ten.endsWith('.plist') ? 'ipa-manifest' : nenTang(b.ma)!.kieu.includes('tep') ? 'tep' : b.kieu
      b.dungLuong = dungLuongDeDoc(kq.byte)
      b.sha256 = kq.sha256
      b.capNhat = new Date().toLocaleDateString('vi-VN')
      b.trangThai = 'san-sang'
      b.hien = true
      toast.add({ title: 'Đã điền link, dung lượng, SHA-256 — nhớ bấm Lưu.', color: 'info' })
    }
    await taiKho()
  }
  catch (err: any) { toast.add({ title: err?.message ?? 'Tải lên thất bại', color: 'error' }) }
  finally { dangGui.value = null }
}

function dungTep(b: BanTai, t: Tep) {
  b.url = t.url
  b.dungLuong = dungLuongDeDoc(t.byte)
  b.sha256 = t.sha256
  if (b.ma === 'ios' && t.ten.endsWith('.plist')) b.kieu = 'ipa-manifest'
}

async function xoaTep(t: Tep) {
  const dangDung = f.value.ban.filter(b => b.url === t.url).map(b => b.ten)
  if (!(await xacNhan.open({
    title: `Xoá ${t.ten}?`,
    description: dangDung.length ? `Tệp đang được dùng cho: ${dangDung.join(', ')}. Link tải sẽ hỏng cho tới khi bạn đổi link.` : 'Không bản tải nào đang dùng tệp này.',
    nut: 'Xoá', mau: 'error',
  }).result)) return
  try { await api('/api/admin/cms/tai-game/xoa', { method: 'POST', body: { ten: t.ten } }); await taiKho(); toast.add({ title: 'Đã xoá', color: 'success' }) }
  catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
}
const luaTep = computed(() => (kho.value?.ds ?? []).map(t => ({ label: `${t.ten} · ${dungLuongDeDoc(t.byte)}`, value: t.ten })))
</script>

<template>
  <AdminPage title="Bản tải game">
    <template #right>
      <UBadge v-if="daThayDoi" color="warning" variant="subtle">Chưa lưu</UBadge>
      <UButton to="/download" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Xem trang Tải game" />
      <UButton icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!duocGhi || coLoi" @click="luu" />
    </template>
    <AdminCmsChuaCoBang v-if="!coBang" bang="web_cai_dat" />
    <UAlert v-if="!duocGhi" color="info" variant="subtle" icon="i-lucide-eye" title="Chế độ chỉ đọc" description="Chỉ Siêu quản trị và Biên tập sửa được bản tải." />
    <UAlert
      color="neutral" variant="subtle" icon="i-lucide-info"
      description="Thẻ hiện trên trang Tải game theo thứ tự dưới đây. “Sắp ra mắt” hiện thẻ có nhãn chờ, không có nút tải. Nút chính trên trang tự chọn theo thiết bị của người xem. Link trống thì dùng biến môi trường HTBB_LINK_* (nếu có)."
    />

    <div class="grid gap-4 xl:grid-cols-2">
      <UCard v-for="(b, i) in f.ban" :key="b.ma" :class="loi(b) ? 'ring-2 ring-error' : ''">
        <template #header>
          <div class="flex flex-wrap items-center gap-2">
            <UIcon :name="nenTang(b.ma)?.icon ?? 'i-lucide-box'" class="size-5 text-primary" />
            <b class="text-highlighted">{{ b.ten }}</b>
            <UBadge size="sm" :color="b.trangThai === 'san-sang' ? 'success' : 'warning'" variant="subtle">{{ b.trangThai === 'san-sang' ? 'Sẵn sàng' : 'Sắp ra mắt' }}</UBadge>
            <div class="ml-auto flex items-center gap-1">
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-up" :aria-label="`Đưa ${b.ten} lên`" :disabled="!duocGhi || i === 0" @click="doi(i, -1)" />
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-down" :aria-label="`Đưa ${b.ten} xuống`" :disabled="!duocGhi || i === f.ban.length - 1" @click="doi(i, 1)" />
              <USwitch v-model="b.hien" :disabled="!duocGhi" label="Hiện" />
            </div>
          </div>
        </template>
        <div class="grid gap-3 sm:grid-cols-2">
          <UFormField label="Tên hiển thị"><UInput v-model="b.ten" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="Trạng thái"><USelect v-model="b.trangThai" :items="LUA_TRANG_THAI" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="Kiểu link"><USelect v-model="b.kieu" :items="luaKieu(b.ma)" :disabled="!duocGhi || luaKieu(b.ma).length < 2" class="w-full" /></UFormField>
          <UFormField label="Phiên bản"><UInput v-model="b.phienBan" placeholder="1.0.3" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField
            :label="b.kieu === 'testflight' ? 'Link TestFlight' : b.kieu === 'app-store' ? 'Link App Store' : b.kieu === 'ipa-manifest' ? 'URL manifest.plist (https)' : 'Link tải'"
            class="sm:col-span-2"
            :help="b.kieu === 'tep' ? 'https://… hoặc tệp trong kho của website (/tai/…)' : b.kieu === 'ipa-manifest' ? 'iOS chỉ cài qua https; manifest trỏ tới tệp .ipa' : ''"
          >
            <div class="flex flex-wrap gap-2">
              <UInput v-model="b.url" :placeholder="b.kieu === 'testflight' ? 'https://testflight.apple.com/join/…' : b.kieu === 'app-store' ? 'https://apps.apple.com/…' : 'https://… hoặc /tai/ten-tep.zip'" :disabled="!duocGhi" class="w-full" />
              <USelect v-if="luaTep.length && (b.kieu === 'tep' || b.kieu === 'ipa-manifest')" :items="luaTep" placeholder="Chọn tệp trong kho" :disabled="!duocGhi" class="min-w-0 flex-1" @update:model-value="(v: any) => { const t = kho?.ds.find(x => x.ten === v); if (t) dungTep(b, t) }" />
              <label v-if="duocGhi && (b.kieu === 'tep' || b.kieu === 'ipa-manifest')" class="inline-flex">
                <input type="file" class="sr-only" :disabled="!!dangGui" @change="taiLen(b, $event)">
                <span class="inline-flex cursor-pointer items-center gap-1.5 rounded-md border border-default px-2.5 py-1.5 text-sm font-medium hover:bg-elevated focus-within:outline-2 focus-within:outline-primary">
                  <UIcon :name="dangGui === b.ma ? 'i-lucide-loader-circle' : 'i-lucide-upload'" class="size-4" :class="dangGui === b.ma ? 'animate-spin' : ''" />
                  {{ dangGui === b.ma ? `${tienDo}%` : 'Tải tệp lên' }}
                </span>
              </label>
            </div>
          </UFormField>
          <UFormField label="Dung lượng"><UInput v-model="b.dungLuong" placeholder="85 MB" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="Ngày cập nhật"><UInput v-model="b.capNhat" placeholder="18/10/2026" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="Hệ điều hành tối thiểu" class="sm:col-span-2"><UInput v-model="b.heDieuHanh" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="SHA-256 (tự điền khi tải tệp lên)" class="sm:col-span-2"><UInput v-model="b.sha256" placeholder="64 ký tự hex" :disabled="!duocGhi" class="w-full font-mono text-xs" /></UFormField>
          <UFormField label="Mô tả ngắn" class="sm:col-span-2"><UInput v-model="b.moTa" :disabled="!duocGhi" class="w-full" /></UFormField>
          <UFormField label="Có gì mới (mỗi dòng một ý)" class="sm:col-span-2"><UTextarea v-model="b.nhatKy" :rows="3" :disabled="!duocGhi" class="w-full" /></UFormField>
        </div>
        <p v-if="loi(b)" class="mt-3 text-sm text-error" role="alert">{{ loi(b) }}</p>
      </UCard>
    </div>

    <UCard>
      <template #header>
        <div class="flex flex-wrap items-center gap-2">
          <div>
            <b>Kho tệp game (/tai/)</b>
            <p class="text-sm text-muted">Tối đa {{ kho?.toiDaMb ?? 1024 }} MB mỗi tệp. Có proxy phía trước thì nới giới hạn kích thước thân request cho đường dẫn tải lên.</p>
          </div>
          <label v-if="duocGhi" class="ml-auto inline-flex">
            <input type="file" class="sr-only" :disabled="!!dangGui" @change="taiLen(null, $event)">
            <span class="inline-flex cursor-pointer items-center gap-1.5 rounded-md bg-primary px-3 py-1.5 text-sm font-semibold text-inverted focus-within:outline-2 focus-within:outline-primary">
              <UIcon :name="dangGui === '_kho' ? 'i-lucide-loader-circle' : 'i-lucide-upload'" class="size-4" :class="dangGui === '_kho' ? 'animate-spin' : ''" />
              {{ dangGui === '_kho' ? `${tienDo}%` : 'Tải tệp lên kho' }}
            </span>
          </label>
        </div>
      </template>
      <AdminBang
        :rows="kho?.ds" :dang-tai="dangTaiKho" :loi="loiKho" row-key="ten" ten-csv="kho-tep-game"
        :cot="[
          { key: 'ten', label: 'Tệp', sort: true },
          { key: 'byte', label: 'Dung lượng', sort: true, so: true },
          { key: 'sha256', label: 'SHA-256', anMobile: true },
          { key: 'luc', label: 'Tải lên lúc', sort: true, anMobile: true },
          { key: 'thaoTac', label: '' },
        ]"
        rong-tieu-de="Chưa có tệp nào" rong-mo-ta="Tải APK / zip / jar lên đây hoặc dán link ngoài vào từng nền tảng." @thu-lai="taiKho"
      >
        <template #cell-ten="{ row }"><a :href="row.url" class="font-semibold text-primary hover:underline">{{ row.ten }}</a></template>
        <template #cell-byte="{ row }">{{ dungLuongDeDoc(row.byte) }}</template>
        <template #cell-sha256="{ row }"><code class="text-xs">{{ row.sha256 ? `${row.sha256.slice(0, 16)}…` : '—' }}</code></template>
        <template #cell-luc="{ row }">{{ new Date(row.luc).toLocaleString('vi-VN') }}</template>
        <template #cell-thaoTac="{ row }">
          <UButton v-if="duocGhi" size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" :aria-label="`Xoá ${row.ten}`" @click="xoaTep(row as Tep)" />
        </template>
      </AdminBang>
    </UCard>
  </AdminPage>
</template>
