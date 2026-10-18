<script setup lang="ts">
/**
 * Chẩn đoán & Lỗi. Bốn nguồn: lỗi Java trong file, lỗi
 * server ghi vào bảng log, dữ liệu lệch, và lỗi của chính trang web.
 *
 * Xem file log: ?file=<tên>[&thutu=cu] — server chỉ nhận tên khớp đúng danh sách
 * cố định (hoặc file <nhân vật>.txt chính server liệt kê được), không nhận đường dẫn.
 */
import type { TableColumn } from '@nuxt/ui'

definePageMeta({ layout: 'admin', middleware: 'admin' })

type Muc = 'ok' | 'canhbao' | 'loi' | 'khong_kiem_duoc'
interface KiemTra { ten: string, muc: Muc, mota: string, cachSua: string, bang: Record<string, string | number>[] }
interface ThongKe { bang: string, aclog: string, nghia: string, n: number, lanCuoi: string }
interface FileLog { ten: string, mota: string, co: boolean, kichThuoc: number, suaLuc: string, docDuoc: boolean }
interface FileHong { ten: string, kichThuoc: number, suaLuc: string, docDuoc: boolean }
interface LoiWeb { thoi_gian: string, admin_user: string, doi_tuong: string, chi_tiet: string }
interface ChanDoanResp {
  kiemTra: KiemTra[]
  demMuc: Record<Muc, number>
  thongKeLoi: ThongKe[]
  coBangNhatKy: boolean
  loiWeb: LoiWeb[]
  serverDir: string
  files: FileLog[]
  luuHong: FileHong[]
}
interface NoiDungFile { ten: string, noiDung: string, co: number, cat: boolean, suaLuc: string, moiTruoc: boolean }

const api = useAdminApi()
const toast = useToast()
const route = useRoute()
const router = useRouter()

const { data, status, error, refresh } = await useAsyncData(
  'admin-chandoan',
  () => api<ChanDoanResp>('/api/admin/chandoan'),
  { server: false },
)

const NHAN_MUC: Record<Muc, { nhan: string, mau: 'error' | 'warning' | 'success' | 'neutral', icon: string }> = {
  loi: { nhan: 'Cần xử lý', mau: 'error', icon: 'i-lucide-octagon-alert' },
  canhbao: { nhan: 'Nên xem', mau: 'warning', icon: 'i-lucide-triangle-alert' },
  ok: { nhan: 'Bình thường', mau: 'success', icon: 'i-lucide-circle-check' },
  khong_kiem_duoc: { nhan: 'Không kiểm được', mau: 'neutral', icon: 'i-lucide-circle-help' },
}

/** Cách sửa mở sẵn cho mục "Cần xử lý", các mục khác bấm để mở. */
const moCachSua = ref<Record<string, boolean>>({})
watch(data, (d) => {
  for (const k of d?.kiemTra ?? []) {
    if (moCachSua.value[k.ten] === undefined) moCachSua.value[k.ten] = k.muc === 'loi'
  }
}, { immediate: true })

function cotBang(bang: KiemTra['bang']): TableColumn<Record<string, string | number>>[] {
  return Object.keys(bang[0] ?? {}).map((k, i) => ({
    id: `c${i}`,
    header: k,
    accessorFn: (r: Record<string, string | number>) => String(r[k] ?? ''),
  }))
}

async function saoChep(s: string) {
  try {
    await navigator.clipboard.writeText(s)
    toast.add({ title: 'Đã sao chép', color: 'success', icon: 'i-lucide-copy' })
  } catch {
    toast.add({ title: 'Trình duyệt chặn sao chép — bôi đen rồi Ctrl+C.', color: 'warning' })
  }
}

// ---------------------------------------------------------------------------
//  Xem cuối file log
// ---------------------------------------------------------------------------
const tenFile = computed(() => (typeof route.query.file === 'string' ? route.query.file : ''))
const moiTruoc = computed(() => route.query.thutu !== 'cu')
const noiDungFile = ref<NoiDungFile | null>(null)
const loiFile = ref('')
const dangDocFile = ref(false)
const khungFile = ref<HTMLElement | null>(null)

// Bấm nhanh sang file khác khi file trước chưa đọc xong: chỉ lượt đọc MỚI NHẤT
// được ghi kết quả, kẻo nội dung file cũ đè lên tiêu đề file mới.
let luotDoc = 0
async function docFile() {
  const luot = ++luotDoc
  noiDungFile.value = null
  loiFile.value = ''
  if (!tenFile.value) {
    dangDocFile.value = false
    return
  }
  const ten = tenFile.value
  dangDocFile.value = true
  try {
    const kq = await api<NoiDungFile>('/api/admin/chandoan/file', {
      query: { ten, thutu: moiTruoc.value ? undefined : 'cu' },
    })
    if (luot !== luotDoc) return
    noiDungFile.value = kq
  } catch (e) {
    if (luot !== luotDoc) return
    loiFile.value = loiApi(e, `Không đọc được file ${ten}.`)
  } finally {
    if (luot === luotDoc) dangDocFile.value = false
  }
  await nextTick()
  khungFile.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}
watch([tenFile, moiTruoc], docFile, { immediate: true })

function xemFile(ten: string, cu = false) {
  router.replace({ query: { file: ten, ...(cu ? { thutu: 'cu' } : {}) } })
}
function dongFile() {
  router.replace({ query: {} })
}

const cotThongKe: TableColumn<ThongKe>[] = [
  { accessorKey: 'aclog', header: 'Loại' },
  { accessorKey: 'nghia', header: 'Nghĩa là gì' },
  { accessorKey: 'n', header: 'Số dòng', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'lanCuoi', header: 'Gần nhất' },
  { id: 'xem', header: '' },
]
const cotFile: TableColumn<FileLog>[] = [
  { accessorKey: 'ten', header: 'File' },
  { accessorKey: 'mota', header: 'Nội dung' },
  { accessorKey: 'kichThuoc', header: 'Cỡ', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'suaLuc', header: 'Sửa lần cuối' },
  { id: 'xem', header: '' },
]
const cotHong: TableColumn<FileHong>[] = [
  { accessorKey: 'ten', header: 'Pokémon' },
  { accessorKey: 'kichThuoc', header: 'Cỡ', meta: { class: { th: 'text-right', td: 'text-right tabular-nums' } } },
  { accessorKey: 'suaLuc', header: 'Sửa lần cuối' },
  { id: 'xem', header: '' },
]
const cotLoiWeb: TableColumn<LoiWeb>[] = [
  { accessorKey: 'thoi_gian', header: 'Lúc' },
  { accessorKey: 'admin_user', header: 'Ai gặp' },
  { accessorKey: 'doi_tuong', header: 'Ở đâu' },
  { accessorKey: 'chi_tiet', header: 'Lỗi' },
]
</script>

<template>
  <AdminPage title="Chẩn đoán & Lỗi">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" label="Kiểm lại" :loading="status === 'pending'" @click="refresh()" />
    </template>

    <p class="text-sm text-muted">
      Bốn nguồn: lỗi Java trong file, lỗi server ghi vào bảng log, dữ liệu lệch, và lỗi của chính trang web.
    </p>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không chạy được chẩn đoán" :description="loiApi(error)" />

    <div v-if="!data && status === 'pending'" class="flex items-center gap-2 text-sm text-muted">
      <UIcon name="i-lucide-loader-circle" class="size-4 animate-spin" /> Đang chạy các phép kiểm tra…
    </div>

    <template v-if="data">
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <AdminStatCard label="Cần xử lý" :value="data.demMuc.loi" note="gây hỏng thật, nên sửa" :mau="data.demMuc.loi > 0 ? 'bad' : 'ok'" icon="i-lucide-octagon-alert" />
        <AdminStatCard label="Nên xem" :value="data.demMuc.canhbao" note="chưa hỏng nhưng đáng để mắt" :mau="data.demMuc.canhbao > 0 ? 'warn' : 'ok'" icon="i-lucide-triangle-alert" />
        <AdminStatCard label="Bình thường" :value="data.demMuc.ok" note="đã kiểm, không có vấn đề" mau="ok" icon="i-lucide-circle-check" />
        <AdminStatCard v-if="data.demMuc.khong_kiem_duoc > 0" label="Không kiểm được" :value="data.demMuc.khong_kiem_duoc" note="thiếu bảng hoặc thiếu quyền" icon="i-lucide-circle-help" />
      </div>

      <!-- Các phép kiểm tra, đã xếp mức nặng lên trước -->
      <div class="space-y-3">
        <UCard
          v-for="k in data.kiemTra"
          :key="k.ten"
          :class="{
            'border-l-4 border-l-error': k.muc === 'loi',
            'border-l-4 border-l-warning': k.muc === 'canhbao',
          }"
          :ui="{ body: 'p-4 sm:p-5 space-y-3' }"
        >
          <div class="flex flex-wrap items-start justify-between gap-2">
            <h3 class="flex items-center gap-2 font-semibold text-highlighted">
              <UIcon :name="NHAN_MUC[k.muc].icon" class="size-5 shrink-0" :class="{ 'text-error': k.muc === 'loi', 'text-warning': k.muc === 'canhbao', 'text-success': k.muc === 'ok', 'text-muted': k.muc === 'khong_kiem_duoc' }" />
              {{ k.ten }}
            </h3>
            <UBadge :color="NHAN_MUC[k.muc].mau" variant="subtle" :label="NHAN_MUC[k.muc].nhan" />
          </div>
          <p class="text-sm text-muted">
            {{ k.mota }}
          </p>

          <div v-if="k.bang.length" class="overflow-x-auto rounded-md border border-default">
            <UTable :data="k.bang" :columns="cotBang(k.bang)" class="w-full" :ui="{ td: 'py-2 text-sm', th: 'py-2' }" />
          </div>

          <UCollapsible v-if="k.cachSua" v-model:open="moCachSua[k.ten]">
            <UButton
              size="sm"
              color="neutral"
              variant="ghost"
              icon="i-lucide-wrench"
              :trailing-icon="moCachSua[k.ten] ? 'i-lucide-chevron-up' : 'i-lucide-chevron-down'"
              label="Cách sửa"
            />
            <template #content>
              <div class="relative mt-2">
                <pre class="overflow-x-auto rounded-md border border-default bg-elevated p-3 pr-12 font-mono text-xs leading-relaxed text-highlighted">{{ k.cachSua }}</pre>
                <UButton
                  class="absolute right-2 top-2"
                  size="xs"
                  color="neutral"
                  variant="outline"
                  icon="i-lucide-copy"
                  aria-label="Sao chép"
                  @click="saoChep(k.cachSua)"
                />
              </div>
            </template>
          </UCollapsible>
        </UCard>
      </div>

      <!-- Lỗi server ghi vào bảng log -->
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Lỗi server ghi vào bảng log · 90 ngày gần nhất
          </h3>
        </template>
        <p v-if="!data.thongKeLoi.length" class="p-4 text-sm text-muted sm:p-6">
          Không có dòng lỗi nào trong 90 ngày qua. (Hoặc máy chủ này chưa có các bảng log đó.)
        </p>
        <UTable v-else :data="data.thongKeLoi" :columns="cotThongKe" class="w-full">
          <template #aclog-cell="{ row }">
            <code>{{ row.original.aclog }}</code>
          </template>
          <template #nghia-cell="{ row }">
            <span class="text-sm text-muted">{{ row.original.nghia }}</span>
          </template>
          <template #n-cell="{ row }">
            {{ so(row.original.n) }}
          </template>
          <template #lanCuoi-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.lanCuoi }}</span>
          </template>
          <template #xem-cell="{ row }">
            <UButton size="xs" color="neutral" variant="outline" label="Xem" :to="{ path: '/admin/log', query: { bang: row.original.bang, loc2: row.original.aclog } }" />
          </template>
        </UTable>
      </UCard>

      <!-- File log của server -->
      <UCard :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <h3 class="font-semibold text-highlighted">
            File log của server
          </h3>
          <p class="mt-1 text-xs text-muted">
            Đọc từ <code>{{ data.serverDir }}</code> (biến môi trường <code>HTBB_SERVER_DIR</code>). Chỉ đọc đúng danh sách tên file viết sẵn
            trong mã nguồn — server không nhận đường dẫn từ trình duyệt, nên không ai lần ra <code>.env</code> hay mật khẩu database được.
          </p>
        </template>
        <UTable :data="data.files" :columns="cotFile" class="w-full">
          <template #ten-cell="{ row }">
            <code class="whitespace-nowrap">{{ row.original.ten }}</code>
          </template>
          <template #mota-cell="{ row }">
            <span class="block min-w-64 whitespace-normal text-sm text-muted">{{ row.original.mota }}</span>
          </template>
          <template #kichThuoc-cell="{ row }">
            <span v-if="row.original.co">{{ so(row.original.kichThuoc) }}</span>
            <span v-else class="text-dimmed">—</span>
          </template>
          <template #suaLuc-cell="{ row }">
            <span class="whitespace-nowrap text-xs text-muted">{{ row.original.suaLuc }}</span>
          </template>
          <template #xem-cell="{ row }">
            <UButton v-if="row.original.co && row.original.docDuoc" size="xs" color="neutral" variant="outline" icon="i-lucide-file-text" label="Xem cuối file" class="whitespace-nowrap" @click="xemFile(row.original.ten)" />
            <span v-else-if="row.original.co" class="whitespace-nowrap text-xs text-warning">không có quyền đọc</span>
            <span v-else class="whitespace-nowrap text-xs text-dimmed">chưa có</span>
          </template>
        </UTable>

        <template v-if="data.luuHong.length" #footer>
          <h3 class="font-semibold text-highlighted">
            Vết lưu Pokémon thất bại
          </h3>
          <p class="mb-3 mt-1 text-xs text-muted">
            Mỗi file dưới đây mang đúng <strong>tên một Pokémon</strong>. Server tạo nó khi đường lưu chính của Pokémon đó ném lỗi
            (<code>DatabaseNhanVat.java:770</code>) — thường là vết duy nhất còn lại của sự cố mất đồ / tụt cấp sau khi thoát game.
            <code>savechar.txt</code> ở bảng trên <em>không</em> phủ đường lưu này.
          </p>
          <UTable :data="data.luuHong" :columns="cotHong" class="w-full">
            <template #ten-cell="{ row }">
              <code class="whitespace-nowrap">{{ row.original.ten }}</code>
            </template>
            <template #kichThuoc-cell="{ row }">
              {{ so(row.original.kichThuoc) }}
            </template>
            <template #suaLuc-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.suaLuc }}</span>
            </template>
            <template #xem-cell="{ row }">
              <UButton v-if="row.original.docDuoc" size="xs" color="neutral" variant="outline" icon="i-lucide-file-text" label="Xem cuối file" @click="xemFile(row.original.ten)" />
              <span v-else class="text-xs text-warning">không có quyền đọc</span>
            </template>
          </UTable>
        </template>
      </UCard>
    </template>

    <!-- Nội dung cuối file -->
    <div ref="khungFile" class="scroll-mt-20">
      <UAlert v-if="loiFile" color="error" variant="subtle" icon="i-lucide-file-x" :title="loiFile" />
      <UCard v-else-if="tenFile && (noiDungFile || dangDocFile)">
        <template #header>
          <div class="flex flex-wrap items-center justify-between gap-2">
            <h3 class="font-semibold text-highlighted">
              Cuối file <code>{{ tenFile }}</code>
            </h3>
            <div class="flex flex-wrap gap-2">
              <UButton
                v-if="moiTruoc"
                size="xs"
                color="neutral"
                variant="outline"
                icon="i-lucide-arrow-down-wide-narrow"
                label="Xem theo thứ tự gốc"
                @click="xemFile(tenFile, true)"
              />
              <UButton
                v-else
                size="xs"
                color="neutral"
                variant="outline"
                icon="i-lucide-arrow-up-wide-narrow"
                label="Xem mới nhất trước"
                @click="xemFile(tenFile)"
              />
              <UButton v-if="noiDungFile" size="xs" color="neutral" variant="outline" icon="i-lucide-copy" label="Sao chép" @click="saoChep(noiDungFile.noiDung)" />
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-x" aria-label="Đóng" @click="dongFile" />
            </div>
          </div>
          <p v-if="noiDungFile" class="mt-1 text-xs text-muted">
            Cỡ {{ so(noiDungFile.co) }} byte · sửa lần cuối {{ noiDungFile.suaLuc }}
            <template v-if="noiDungFile.cat">
              · chỉ hiện khoảng 64 KB cuối
            </template>
            · <strong>{{ noiDungFile.moiTruoc ? 'mới nhất ở trên' : 'theo đúng thứ tự trong file' }}</strong>
          </p>
        </template>
        <div v-if="dangDocFile" class="flex items-center gap-2 text-sm text-muted">
          <UIcon name="i-lucide-loader-circle" class="size-4 animate-spin" /> Đang đọc…
        </div>
        <pre
          v-else-if="noiDungFile"
          class="max-h-[520px] overflow-auto rounded-md border border-default bg-elevated p-3 font-mono text-xs leading-relaxed whitespace-pre text-highlighted"
        >{{ noiDungFile.noiDung || '(trống)' }}</pre>
      </UCard>
    </div>

    <!-- Lỗi của chính trang quản trị -->
    <UCard v-if="data" :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Lỗi của chính trang quản trị
        </h3>
      </template>
      <p v-if="!data.coBangNhatKy" class="p-4 text-sm text-muted sm:p-6">
        Chưa có bảng <code>web_admin_log</code> nên lỗi web chỉ in ra log của tiến trình website (stdout).
      </p>
      <p v-else-if="!data.loiWeb.length" class="p-4 text-sm text-muted sm:p-6">
        Chưa có lỗi nào được ghi. Tốt.
      </p>
      <UTable v-else :data="data.loiWeb" :columns="cotLoiWeb" class="w-full">
        <template #thoi_gian-cell="{ row }">
          <span class="whitespace-nowrap text-xs text-muted">{{ row.original.thoi_gian }}</span>
        </template>
        <template #admin_user-cell="{ row }">
          <span v-if="row.original.admin_user" class="whitespace-nowrap">{{ row.original.admin_user }}</span>
          <span v-else class="text-dimmed">chưa đăng nhập</span>
        </template>
        <template #doi_tuong-cell="{ row }">
          <code class="whitespace-nowrap">{{ row.original.doi_tuong }}</code>
        </template>
        <template #chi_tiet-cell="{ row }">
          <span class="block min-w-64 max-w-2xl whitespace-pre-wrap break-words text-sm">{{ row.original.chi_tiet }}</span>
        </template>
      </UTable>
    </UCard>
  </AdminPage>
</template>
