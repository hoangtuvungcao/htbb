<script setup lang="ts">
/**
 * Quản lý mã quà tặng (bảng kpah_db.giftcode + giftcode_log).
 *
 * Bốn điều server game làm, quyết định mọi thứ trên trang này (DatabaseGiftCode.java):
 *   1. Chỉ tìm mã `type = 0` -> type khác 0 là mã vô hình với người chơi.
 *   2. `limit_use` là số lượt CÒN LẠI; 0 = hết lượt; số âm = không giới hạn (không trừ).
 *   3. Chặn nhận lại theo TÀI KHOẢN (giftcode_log.userId) — mỗi tài khoản một lần.
 *   4. Cột `expire` không bao giờ được đọc — muốn hết hạn thì bấm "Tắt".
 *
 * Tên mã và chuỗi phần thưởng bị lọc ký tự chặt vì server ghép thẳng chúng vào SQL.
 * Kiểm tra ở trang này (kiemTraItem chạy ngay trong trình duyệt) chỉ để tiện — máy chủ
 * chạy lại đúng hàm đó trước khi ghi.
 */
import type { TableColumn } from '@nuxt/ui'
import AdminGiftcodeXacNhan from '~/components/admin/giftcode/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface MaGiftcode {
  id: number
  giftcode: string
  xu: number
  luong: number
  luongLock: number
  item: string
  limit_use: number
  type: number
  daPhat: number
  moTa: string[]
}
interface DongLog {
  id: number
  giftcode: string
  player: string
  userId: number
  item: string
  xu: number
  luong: number
  luongK: number
  moTa: string[]
}
interface Nhom { ma: string, ten: string, loai: string, ghiChu: string, muc: { id: number, ten: string }[] }

const api = useAdminApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()
const xacNhan = useOverlay().create(AdminGiftcodeXacNhan)

const qs = (k: string) => (typeof route.query[k] === 'string' ? (route.query[k] as string).trim() : '')
const tab = computed(() => (qs('tab') === 'log' ? 'log' : 'ma'))
const suaId = computed(() => Math.max(0, Math.trunc(Number(qs('sua')) || 0)))

function baoLoi(tieuDe: string, e: unknown) {
  toast.add({ title: tieuDe, description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
}
function baoXong(msg: string) {
  toast.add({ title: msg, color: 'success', icon: 'i-lucide-check' })
}

// ---------------------------------------------------------------------------
//  Danh sách mã
// ---------------------------------------------------------------------------
const { data: dsData, status: dsStatus, error: dsError, refresh: dsRefresh } = await useAsyncData(
  'admin-giftcode-ds',
  () => api<{ dsMa: MaGiftcode[] }>('/api/admin/giftcode'),
  { server: false, immediate: tab.value === 'ma' },
)
const dsMa = computed(() => dsData.value?.dsMa ?? [])

const { data: dmData, error: dmError, refresh: dmRefresh } = await useAsyncData(
  'admin-giftcode-dm',
  () => api<{ dmuc: Nhom[] }>('/api/admin/giftcode/danh-muc'),
  { server: false, immediate: tab.value === 'ma' },
)

watch(tab, (t) => {
  if (t === 'ma') {
    if (!dsData.value) dsRefresh()
    if (!dmData.value) dmRefresh()
  }
})

// ---------------------------------------------------------------------------
//  Biểu mẫu tạo / sửa
// ---------------------------------------------------------------------------
interface Form { id: number, giftcode: string, xu: number, luong: number, luongLock: number, limit_use: number, type: number, item: string }
const formTrong = (): Form => ({ id: 0, giftcode: '', xu: 0, luong: 0, luongLock: 0, limit_use: 100, type: 0, item: '' })
const form = reactive<Form>(formTrong())
const dangSuaTen = ref('')
const dangLuu = ref(false)
const oForm = useTemplateRef<HTMLElement>('oForm')

async function napMaSua(id: number) {
  if (id <= 0) {
    Object.assign(form, formTrong())
    dangSuaTen.value = ''
    return
  }
  try {
    const r = await api<{ ma: Omit<MaGiftcode, 'daPhat' | 'moTa'> }>(`/api/admin/giftcode/${id}`)
    Object.assign(form, r.ma)
    dangSuaTen.value = r.ma.giftcode
    nextTick(() => oForm.value?.scrollIntoView({ behavior: 'smooth', block: 'start' }))
  } catch (e) {
    baoLoi('Không mở được mã để sửa', e)
    router.replace({ query: { ...route.query, sua: undefined } })
  }
}
watch(suaId, napMaSua, { immediate: true })

const trangThaiItems = [
  { label: 'Đang bật (type 0)', value: 0 },
  { label: 'Ẩn — người chơi không dùng được', value: 1 },
]

const tenMaHopLe = computed(() => form.giftcode === '' || GIFTCODE_TEN_RE.test(form.giftcode.trim()))

/** Kiểm tra cú pháp NGAY khi gõ — cùng hàm máy chủ chạy lúc lưu. */
const kiem = computed(() => {
  const [chuan, loi] = kiemTraItem(form.item)
  return { chuan, loi, soPhan: chuan === '' ? 0 : chuan.split(',').length }
})

/** Bản dịch "người chơi sẽ nhận" — hỏi máy chủ (cần tên trong data_item), trễ 400ms. */
const xemTruoc = ref<string[]>([])
const dangXem = ref(false)
watchDebounced(() => kiem.value.chuan, async (chuan) => {
  if (chuan === '') {
    xemTruoc.value = []
    return
  }
  dangXem.value = true
  try {
    const r = await api<{ moTa: string[] }>('/api/admin/giftcode/kiem-tra', { method: 'POST', body: { item: chuan } })
    xemTruoc.value = r.moTa
  } catch {
    xemTruoc.value = []
  } finally {
    dangXem.value = false
  }
}, { debounce: 400, immediate: true })

function chenVao(s: string) {
  form.item = dmNoiThem(form.item, s)
  toast.add({ title: 'Đã thêm vào phần thưởng', description: s, color: 'neutral', icon: 'i-lucide-plus', duration: 1800 })
}

function huySua() {
  router.replace({ query: { ...route.query, sua: undefined } })
  Object.assign(form, formTrong())
  dangSuaTen.value = ''
}

async function luu() {
  if (dangLuu.value) return
  dangLuu.value = true
  try {
    const r = await api<{ message: string }>('/api/admin/giftcode/luu', {
      method: 'POST',
      body: { ...form, id: form.id > 0 ? form.id : undefined },
    })
    baoXong(r.message)
    // Lưu xong quay về biểu mẫu trống (bỏ ?sua=) để lần bấm tiếp theo không ghi đè nhầm mã vừa sửa.
    if (suaId.value > 0) await router.replace({ query: { ...route.query, sua: undefined } })
    Object.assign(form, formTrong())
    dangSuaTen.value = ''
    await dsRefresh()
  } catch (e) {
    baoLoi('Chưa lưu được', e)
  } finally {
    dangLuu.value = false
  }
}

// ---------------------------------------------------------------------------
//  Thao tác trên từng mã
// ---------------------------------------------------------------------------
const dangLam = ref<string>('')
const daChon = ref<number[]>([])
watch(dsMa, (ds) => {
  const con = new Set(ds.map(d => d.id))
  daChon.value = daChon.value.filter(id => con.has(id))
})
const chonTatCa = computed<boolean | 'indeterminate'>({
  get: () => (daChon.value.length === 0 ? false : (daChon.value.length === dsMa.value.length ? true : 'indeterminate')),
  set: (v) => { daChon.value = v === true ? dsMa.value.map(d => d.id) : [] },
})
function doiChon(id: number, v: boolean | 'indeterminate') {
  daChon.value = v === true ? [...new Set([...daChon.value, id])] : daChon.value.filter(x => x !== id)
}

async function goi(khoa: string, url: string, body: Record<string, unknown>) {
  dangLam.value = khoa
  try {
    const r = await api<{ message: string }>(url, { method: 'POST', body })
    baoXong(r.message)
    return true
  } catch (e) {
    baoLoi('Không thao tác được', e)
    return false
  } finally {
    dangLam.value = ''
  }
}

async function tat(gc: MaGiftcode) {
  const ok = await xacNhan.open({
    title: `Tắt mã ${gc.giftcode}?`,
    description: 'Đặt số lượt còn lại về 0. Muốn bật lại thì sửa mã và đặt số lượt mới.',
    nut: 'Tắt mã',
    mau: 'warning',
  }).result
  if (ok && await goi(`tat${gc.id}`, '/api/admin/giftcode/tat', { id: gc.id })) await dsRefresh()
}

async function xoa(gc: MaGiftcode) {
  const ok = await xacNhan.open({
    title: `XOÁ HẲN mã ${gc.giftcode}?`,
    description: 'Không hoàn lại được. Nhật ký ai đã nhận vẫn còn trong giftcode_log.',
    nut: 'Xoá hẳn',
    mau: 'error',
  }).result
  if (ok && await goi(`xoa${gc.id}`, '/api/admin/giftcode/xoa', { id: gc.id })) {
    if (form.id === gc.id) huySua()
    await dsRefresh()
  }
}

async function xoaNhieu() {
  if (!daChon.value.length) return
  const ten = dsMa.value.filter(d => daChon.value.includes(d.id)).map(d => d.giftcode)
  const ok = await xacNhan.open({
    title: `XOÁ HẲN ${ten.length} mã đã chọn?`,
    description: `Không hoàn lại được.\n${ten.join(', ')}`,
    nut: `Xoá ${ten.length} mã`,
    mau: 'error',
  }).result
  if (ok && await goi('xoaNhieu', '/api/admin/giftcode/xoa-nhieu', { ids: daChon.value })) {
    if (daChon.value.includes(form.id)) huySua()
    daChon.value = []
    await dsRefresh()
  }
}

function trangThai(gc: MaGiftcode) {
  const bat = gc.type === 0
  if (bat && gc.limit_use !== 0) return { label: 'Dùng được', color: 'success' as const }
  if (!bat) return { label: 'Ẩn', color: 'neutral' as const }
  return { label: 'Hết lượt', color: 'warning' as const }
}

const so_ = { th: 'text-right', td: 'text-right tabular-nums' }
const cotMa: TableColumn<MaGiftcode>[] = [
  { id: 'chon', header: '' },
  { accessorKey: 'id', header: 'ID', meta: { class: so_ } },
  { accessorKey: 'giftcode', header: 'Mã' },
  { accessorKey: 'xu', header: 'Xu', meta: { class: so_ } },
  { accessorKey: 'luong', header: 'Lượng', meta: { class: so_ } },
  { accessorKey: 'luongLock', header: 'Lượng khoá', meta: { class: so_ } },
  { accessorKey: 'item', header: 'Phần thưởng' },
  { accessorKey: 'limit_use', header: 'Còn lượt', meta: { class: so_ } },
  { accessorKey: 'daPhat', header: 'Đã phát', meta: { class: so_ } },
  { id: 'trangThai', header: 'Trạng thái' },
  { id: 'nut', header: '' },
]

// ---------------------------------------------------------------------------
//  Bảng tham khảo các loại phần thưởng
// ---------------------------------------------------------------------------
const timLoai = ref('')
const bangLoai = computed(() => {
  const tu = timLoai.value.trim().toLowerCase()
  return Object.entries(loaiThuong())
    .map(([loai, [, cuPhap, moTa, viDu]]) => ({ loai, cuPhap, moTa, viDu }))
    .filter(r => tu === '' || `${r.cuPhap} ${r.moTa}`.toLowerCase().includes(tu))
})
const cotLoai: TableColumn<{ loai: string, cuPhap: string, moTa: string, viDu: string }>[] = [
  { accessorKey: 'cuPhap', header: 'Cú pháp' },
  { accessorKey: 'moTa', header: 'Là gì' },
  { accessorKey: 'viDu', header: 'Ví dụ' },
  { id: 'nut', header: '' },
]

// ---------------------------------------------------------------------------
//  Tab "Ai đã nhận"
// ---------------------------------------------------------------------------
const logMa = computed(() => qs('ma'))
const logPlayer = computed(() => qs('player'))
const logTrang = computed(() => Math.max(1, Math.trunc(Number(qs('trang')) || 1)))
const locMa = ref(logMa.value)
const locPlayer = ref(logPlayer.value)
watch([logMa, logPlayer], ([a, b]) => {
  locMa.value = a
  locPlayer.value = b
})

const { data: logData, status: logStatus, error: logError, refresh: logRefresh } = await useAsyncData(
  'admin-giftcode-log',
  () => (tab.value === 'log'
    ? api<{ rows: DongLog[], trang: number, conTrangSau: boolean }>('/api/admin/giftcode/log', {
        query: { ma: logMa.value || undefined, player: logPlayer.value || undefined, trang: logTrang.value },
      })
    : Promise.resolve(null)),
  { server: false, watch: [tab, logMa, logPlayer, logTrang] },
)

function locLog() {
  router.push({
    query: { tab: 'log', ma: locMa.value.trim() || undefined, player: locPlayer.value.trim() || undefined },
  })
}
function denTrang(t: number) {
  router.push({ query: { ...route.query, tab: 'log', trang: t > 1 ? String(t) : undefined } })
}

async function choNhanLai(r: DongLog) {
  const ok = await xacNhan.open({
    title: `Cho tài khoản của ${r.player} nhận lại mã ${r.giftcode}?`,
    description: 'Xoá mọi dòng của TÀI KHOẢN đó (theo userId, không chỉ riêng nhân vật này) với mã này trong giftcode_log. Không cộng lại lượt cho mã.',
    nut: 'Cho nhận lại',
    mau: 'primary',
  }).result
  if (ok && await goi(`lai${r.id}`, '/api/admin/giftcode/cho-nhan-lai', { ma: r.giftcode, userId: r.userId, player: r.player })) {
    await logRefresh()
  }
}

const cotLog: TableColumn<DongLog>[] = [
  { accessorKey: 'id', header: 'ID', meta: { class: so_ } },
  { accessorKey: 'giftcode', header: 'Mã' },
  { accessorKey: 'player', header: 'Nhân vật' },
  { accessorKey: 'xu', header: 'Xu', meta: { class: so_ } },
  { accessorKey: 'luong', header: 'Lượng', meta: { class: so_ } },
  { accessorKey: 'luongK', header: 'Lượng khoá', meta: { class: so_ } },
  { accessorKey: 'item', header: 'Phần thưởng' },
  { id: 'nut', header: '' },
]
</script>

<template>
  <AdminPage title="Giftcode">
    <template #right>
      <UFieldGroup size="sm">
        <UButton
          :to="{ path: '/admin/giftcode' }"
          icon="i-lucide-ticket"
          label="Danh sách mã"
          :color="tab === 'ma' ? 'primary' : 'neutral'"
          :variant="tab === 'ma' ? 'solid' : 'outline'"
        />
        <UButton
          :to="{ path: '/admin/giftcode', query: { tab: 'log' } }"
          icon="i-lucide-history"
          label="Ai đã nhận"
          :color="tab === 'log' ? 'primary' : 'neutral'"
          :variant="tab === 'log' ? 'solid' : 'outline'"
        />
      </UFieldGroup>
    </template>

    <p class="text-sm text-muted">
      Tạo mã, nạp thêm lượt, tắt mã, và xem ai đã nhận gì.
    </p>

    <!-- =============================================================== MÃ -->
    <template v-if="tab === 'ma'">
      <div ref="oForm" class="scroll-mt-20">
        <UCard>
          <template #header>
            <div class="flex flex-wrap items-center justify-between gap-2">
              <h3 class="flex items-center gap-2 font-semibold text-highlighted">
                <UIcon :name="form.id > 0 ? 'i-lucide-pencil' : 'i-lucide-gift'" class="size-4 text-primary" />
                {{ form.id > 0 ? `Sửa mã “${dangSuaTen}”` : 'Tạo mã mới' }}
              </h3>
              <UBadge v-if="form.id > 0" color="neutral" variant="subtle" :label="`#${form.id}`" />
            </div>
          </template>

          <form id="formGiftcode" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" @submit.prevent="luu">
            <UFormField
              label="Tên mã"
              help="Chỉ chữ, số, gạch dưới. Không phân biệt hoa thường khi nhập trong game."
              :error="tenMaHopLe ? false : 'Chỉ chữ, số, gạch dưới, dài 2–50 ký tự.'"
              required
            >
              <UInput v-model="form.giftcode" maxlength="50" placeholder="tanthu2026" icon="i-lucide-ticket" class="w-full" />
            </UFormField>
            <UFormField label="Xu">
              <UInput v-model.number="form.xu" type="number" min="0" class="w-full" />
            </UFormField>
            <UFormField label="Lượng">
              <UInput v-model.number="form.luong" type="number" min="0" class="w-full" />
            </UFormField>
            <UFormField label="Lượng khoá">
              <UInput v-model.number="form.luongLock" type="number" min="0" class="w-full" />
            </UFormField>
            <UFormField label="Số lượt còn lại">
              <UInput v-model.number="form.limit_use" type="number" class="w-full" />
              <template #help>
                <strong class="text-toned">-1 = không giới hạn</strong> (số âm không bị trừ). 0 = mã đã tắt.
              </template>
            </UFormField>
            <UFormField label="Trạng thái">
              <USelect v-model="form.type" :items="trangThaiItems" value-key="value" class="w-full" />
            </UFormField>
            <!-- Enter trong các ô trên = lưu. Bộ chọn nằm NGOÀI form này nên
                 Enter trong ô tìm vật phẩm không bao giờ lưu nhầm mã. -->
            <button type="submit" class="sr-only" tabindex="-1" aria-hidden="true" />
          </form>

          <div class="mt-5">
            <UAlert
              v-if="dmError"
              color="warning"
              variant="subtle"
              icon="i-lucide-triangle-alert"
              title="Không tải được danh mục cho bộ chọn — vẫn gõ tay được như trước."
              :description="loiApi(dmError)"
            />
            <USkeleton v-else-if="!dmData" class="h-36" />
            <AdminGiftcodeBoChon v-else-if="dmData.dmuc.length" :dmuc="dmData.dmuc" @them="chenVao" />
          </div>

          <UFormField label="Phần thưởng vật phẩm" class="mt-5">
            <UTextarea
              v-model="form.item"
              :rows="3"
              autoresize
              :maxrows="10"
              placeholder="ITEM:848:1000,TRANGPHUC:833:1:30"
              class="w-full font-mono"
              :color="kiem.loi.length ? 'error' : 'primary'"
              :highlight="kiem.loi.length > 0"
            />
            <template #help>
              Nhiều phần thưởng ngăn nhau bằng dấu phẩy. Để trống nếu chỉ cho xu / lượng.
              Cú pháp được kiểm tra trước khi lưu — xem bảng bên dưới.
              Vẫn gõ hoặc dán tay được như trước; bộ chọn chỉ ghép chuỗi giúp bạn.
            </template>
          </UFormField>

          <!-- Kiểm tra trực tiếp -->
          <div class="mt-3 space-y-2">
            <UAlert
              v-if="kiem.loi.length"
              color="error"
              variant="subtle"
              icon="i-lucide-circle-x"
              title="Phần thưởng chưa hợp lệ — máy chủ sẽ từ chối lưu"
            >
              <template #description>
                <ul class="list-disc space-y-0.5 pl-4">
                  <li v-for="(l, i) in kiem.loi" :key="i">
                    {{ l }}
                  </li>
                </ul>
              </template>
            </UAlert>
            <div v-else-if="kiem.soPhan > 0" class="rounded-lg border border-success/30 bg-success/5 p-3 text-sm">
              <p class="flex flex-wrap items-center gap-2 text-success">
                <UIcon name="i-lucide-circle-check" class="size-4" />
                Cú pháp hợp lệ — {{ kiem.soPhan }} phần thưởng.
                <span v-if="kiem.chuan !== form.item.trim()" class="text-xs text-muted">
                  Sẽ lưu dạng chuẩn: <code class="text-highlighted">{{ kiem.chuan }}</code>
                </span>
              </p>
              <ul v-if="xemTruoc.length" class="mt-2 list-disc space-y-0.5 pl-6 text-xs text-muted" :class="dangXem ? 'opacity-60' : ''">
                <li v-for="(d, i) in xemTruoc" :key="i" :class="d.startsWith('? ') ? 'text-warning' : ''">
                  {{ d }}
                </li>
              </ul>
            </div>
            <p v-if="kiem.chuan.length > 255" class="text-xs text-error">
              Chuỗi dài {{ kiem.chuan.length }} ký tự — cột item chỉ chứa được 255.
            </p>
          </div>

          <div class="mt-5 flex flex-wrap items-center gap-2">
            <UButton
              :icon="form.id > 0 ? 'i-lucide-save' : 'i-lucide-plus'"
              :label="form.id > 0 ? 'Lưu thay đổi' : 'Tạo mã'"
              :loading="dangLuu"
              :disabled="kiem.loi.length > 0 || !tenMaHopLe || form.giftcode.trim() === ''"
              @click="luu"
            />
            <UButton v-if="form.id > 0" color="neutral" variant="ghost" label="Huỷ" @click="huySua" />
          </div>

          <UAlert class="mt-6" color="neutral" variant="soft" icon="i-lucide-lightbulb" title="Nhớ ba điều:">
            <template #description>
              <ul class="mt-1 space-y-1">
                <li>
                  · Mỗi <em>tài khoản</em> chỉ nhận một mã một lần, dù có bao nhiêu nhân vật (chặn theo
                  <code>giftcode_log.userId</code>, không còn theo tên nhân vật).
                </li>
                <li>· Cột <code>expire</code> trong bảng không có tác dụng gì cả; muốn hết hạn thì tự bấm “Tắt”.</li>
                <li>
                  · Mã cho quá nhiều món một lúc dễ làm hành trang phình to; trước đây từng có nhân vật
                  bị server tự khoá vì lưu hỏng. Xem <code>vps/sql-giftcode.md</code>.
                </li>
              </ul>
            </template>
          </UAlert>
        </UCard>
      </div>

      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <h3 class="font-semibold text-highlighted">
              Danh sách mã ({{ dsMa.length }})
            </h3>
            <div class="flex items-center gap-3">
              <span class="text-sm text-muted">
                {{ daChon.length === 0 ? 'chưa chọn mã nào' : `đang chọn ${daChon.length} mã` }}
              </span>
              <UButton
                color="error"
                variant="soft"
                size="sm"
                icon="i-lucide-trash-2"
                label="Xoá các mã đã chọn"
                :disabled="daChon.length === 0"
                :loading="dangLam === 'xoaNhieu'"
                @click="xoaNhieu"
              />
            </div>
          </div>
        </template>

        <UAlert v-if="dsError" class="m-4" color="error" variant="subtle" icon="i-lucide-circle-x" :title="`Lỗi đọc dữ liệu: ${loiApi(dsError)}`" />
        <UTable
          v-else
          :data="dsMa"
          :columns="cotMa"
          :loading="dsStatus === 'pending' && !dsData"
          empty="Chưa có mã nào."
          sticky="header"
          class="max-h-[70vh]"
        >
          <template #chon-header>
            <UCheckbox v-model="chonTatCa" aria-label="Chọn tất cả" />
          </template>
          <template #chon-cell="{ row }">
            <UCheckbox
              :model-value="daChon.includes(row.original.id)"
              :aria-label="`Chọn ${row.original.giftcode}`"
              @update:model-value="(v: boolean | 'indeterminate') => doiChon(row.original.id, v)"
            />
          </template>
          <template #giftcode-cell="{ row }">
            <strong class="text-highlighted">{{ row.original.giftcode }}</strong>
          </template>
          <template #xu-cell="{ row }">
            {{ so(row.original.xu) }}
          </template>
          <template #luong-cell="{ row }">
            {{ so(row.original.luong) }}
          </template>
          <template #luongLock-cell="{ row }">
            {{ so(row.original.luongLock) }}
          </template>
          <template #item-cell="{ row }">
            <AdminGiftcodePhanThuong :item="row.original.item" :mo-ta="row.original.moTa" />
          </template>
          <template #limit_use-cell="{ row }">
            {{ row.original.limit_use < 0 ? '∞' : so(row.original.limit_use) }}
          </template>
          <template #daPhat-cell="{ row }">
            <NuxtLink
              :to="{ path: '/admin/giftcode', query: { tab: 'log', ma: row.original.giftcode } }"
              class="text-primary hover:underline"
            >
              {{ so(row.original.daPhat) }}
            </NuxtLink>
          </template>
          <template #trangThai-cell="{ row }">
            <UBadge :color="trangThai(row.original).color" variant="subtle" :label="trangThai(row.original).label" />
          </template>
          <template #nut-cell="{ row }">
            <div class="flex justify-end gap-1.5 whitespace-nowrap">
              <UButton
                size="xs"
                color="neutral"
                variant="outline"
                icon="i-lucide-pencil"
                label="Sửa"
                :to="{ path: '/admin/giftcode', query: { sua: String(row.original.id) } }"
              />
              <UButton
                v-if="row.original.limit_use !== 0"
                size="xs"
                color="neutral"
                variant="outline"
                icon="i-lucide-power-off"
                label="Tắt"
                :loading="dangLam === `tat${row.original.id}`"
                @click="tat(row.original)"
              />
              <UButton
                size="xs"
                color="error"
                variant="soft"
                icon="i-lucide-trash-2"
                label="Xoá"
                :loading="dangLam === `xoa${row.original.id}`"
                @click="xoa(row.original)"
              />
            </div>
          </template>
        </UTable>
      </UCard>

      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <div class="flex flex-wrap items-start justify-between gap-3">
            <div class="max-w-3xl">
              <h3 class="font-semibold text-highlighted">
                Các loại phần thưởng server hiểu
              </h3>
              <p class="mt-1 text-xs text-muted">
                Rút thẳng từ <code>src/data/DatabaseGiftCode.java</code>. Phần ghi <code>_</code> là tham số
                server không dùng tới nhưng <strong>vẫn phải có</strong> — thiếu là server ném lỗi giữa chừng,
                người chơi nhận nửa phần quà mà mã không bị trừ lượt. Bấm <strong>Chèn</strong> để nối ví dụ
                vào ô "Phần thưởng vật phẩm" phía trên.
              </p>
            </div>
            <UInput v-model="timLoai" icon="i-lucide-search" placeholder="Lọc theo tên loại / mô tả" size="sm" class="w-64" />
          </div>
        </template>
        <UTable :data="bangLoai" :columns="cotLoai" empty="Không có loại nào khớp." sticky="header" class="max-h-[60vh]">
          <template #cuPhap-cell="{ row }">
            <code class="whitespace-nowrap text-xs text-highlighted">{{ row.original.cuPhap }}</code>
          </template>
          <template #moTa-cell="{ row }">
            <p class="min-w-64 max-w-xl whitespace-normal text-xs text-muted">
              {{ row.original.moTa }}
            </p>
          </template>
          <template #viDu-cell="{ row }">
            <code class="whitespace-nowrap text-xs">{{ row.original.viDu }}</code>
          </template>
          <template #nut-cell="{ row }">
            <UButton size="xs" color="neutral" variant="outline" icon="i-lucide-corner-down-left" label="Chèn" @click="chenVao(row.original.viDu)" />
          </template>
        </UTable>
      </UCard>
    </template>

    <!-- =============================================================== AI ĐÃ NHẬN -->
    <UCard v-else :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <div class="space-y-3">
          <h3 class="font-semibold text-highlighted">
            Ai đã nhận mã
          </h3>
          <form class="flex flex-wrap items-end gap-2" @submit.prevent="locLog">
            <UFormField label="Mã">
              <UInput v-model="locMa" placeholder="để trống = tất cả" icon="i-lucide-ticket" />
            </UFormField>
            <UFormField label="Tên nhân vật">
              <UInput v-model="locPlayer" placeholder="để trống = tất cả" icon="i-lucide-user" />
            </UFormField>
            <UButton type="submit" icon="i-lucide-filter" label="Lọc" />
            <UButton
              v-if="logMa !== '' || logPlayer !== ''"
              :to="{ path: '/admin/giftcode', query: { tab: 'log' } }"
              color="neutral"
              variant="ghost"
              label="Bỏ lọc"
            />
          </form>
        </div>
      </template>

      <UAlert v-if="logError" class="m-4" color="error" variant="subtle" icon="i-lucide-circle-x" :title="`Lỗi đọc dữ liệu: ${loiApi(logError)}`" />
      <UTable
        v-else
        :data="logData?.rows ?? []"
        :columns="cotLog"
        :loading="logStatus === 'pending'"
        empty="Chưa có ai nhận mã nào khớp bộ lọc."
      >
        <template #giftcode-cell="{ row }">
          <NuxtLink :to="{ path: '/admin/giftcode', query: { tab: 'log', ma: row.original.giftcode } }" class="text-primary hover:underline">
            {{ row.original.giftcode }}
          </NuxtLink>
        </template>
        <template #player-cell="{ row }">
          <NuxtLink :to="{ path: '/admin/nhanvat', query: { q: row.original.player } }" class="text-primary hover:underline">
            {{ row.original.player }}
          </NuxtLink>
        </template>
        <template #xu-cell="{ row }">
          {{ so(row.original.xu) }}
        </template>
        <template #luong-cell="{ row }">
          {{ so(row.original.luong) }}
        </template>
        <template #luongK-cell="{ row }">
          {{ so(row.original.luongK) }}
        </template>
        <template #item-cell="{ row }">
          <AdminGiftcodePhanThuong :item="row.original.item" :mo-ta="row.original.moTa" />
        </template>
        <template #nut-cell="{ row }">
          <div class="flex justify-end">
            <UButton
              size="xs"
              color="neutral"
              variant="outline"
              icon="i-lucide-rotate-ccw"
              label="Cho nhận lại"
              :loading="dangLam === `lai${row.original.id}`"
              @click="choNhanLai(row.original)"
            />
          </div>
        </template>
      </UTable>

      <template #footer>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-2">
            <UButton
              v-if="logTrang > 1"
              size="sm"
              color="neutral"
              variant="outline"
              icon="i-lucide-chevron-left"
              label="Trang trước"
              @click="denTrang(logTrang - 1)"
            />
            <span class="text-sm text-muted">Trang {{ logTrang }}</span>
            <UButton
              v-if="logData?.conTrangSau"
              size="sm"
              color="neutral"
              variant="outline"
              trailing-icon="i-lucide-chevron-right"
              label="Trang sau"
              @click="denTrang(logTrang + 1)"
            />
          </div>
          <p class="max-w-2xl text-xs text-muted">
            “Cho nhận lại” xoá mọi dòng của <strong>tài khoản đó</strong> (theo <code>userId</code>, không
            chỉ riêng nhân vật đang hiện ở dòng này) với mã này trong <code>giftcode_log</code> — đúng thứ
            server dùng để chặn nhận trùng. Nó <strong>không</strong> cộng lại lượt cho mã.
          </p>
        </div>
      </template>
    </UCard>
  </AdminPage>
</template>
