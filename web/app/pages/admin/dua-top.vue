<script setup lang="ts">
/**
 * Phát quà đua top.
 *
 * Bộ chọn vật phẩm cùng cơ chế với trang Giftcode (chỉ GHÉP CHUỖI rồi chèn vào ô của
 * hạng đang chọn ở "Điền vào hạng"); khác đúng một chỗ: ở đây có 10 ô đích nên có thêm
 * ô chọn hạng — mỗi ô phần thưởng cũng tự đưa hạng của nó lên làm đích khi được bấm vào.
 * Chuỗi vẫn đi qua kiemTraItem() phía máy chủ y như khi gõ tay lúc lưu.
 */
import type { DuaTopNhom, DuaTopNhomLuaChon } from '#shared/utils/duaTop'
import type { TruongThuong } from '#shared/utils/thuong'

definePageMeta({ layout: 'admin', middleware: 'admin' })

const api = useAdminApi()
// Vai trò chỉ đọc chỉ xem danh sách — không thấy khung tạo đợt / nút huỷ (server cũng chặn, 403).
const me = useAdminMe()
const duocGhi = computed(() => me.duoc('/api/admin/dua-top/luu-dot', 'POST'))
const toast = useToast()

interface DuLieu {
  soHang: number
  loiDoc: string
  dangCho: DuaTopDongView[]
  daNhan: DuaTopDongView[]
  dmuc: DuaTopNhom[]
  truong: Record<string, TruongThuong[]>
  chenNhanh: { loai: string, cuPhap: string, moTa: string, viDu: string }[]
}
interface DuaTopDongView {
  id: number
  vi_tri: number
  charname: string
  item: string
  item_chon: string
  admin_user: string
  tao_luc: string
  nhan_luc: string | null
  moTa: string[]
  luaChon: DuaTopNhomLuaChon[]
}

const { data, status, error, refresh } = await useAsyncData('admin-dua-top', () => api<DuLieu>('/api/admin/dua-top'), { server: false })

const soHang = computed(() => data.value?.soHang ?? DUA_TOP_SO_HANG)

// ---------------------------------------------------------------------------
//  Biểu mẫu 10 hạng
// ---------------------------------------------------------------------------
const taoHangTrong = () => Array.from({ length: DUA_TOP_SO_HANG }, () => ({ ten: '', item: '', itemchon: '' }))
const hang = ref(taoHangTrong())
const hangDich = ref(1)
const loiLuu = ref<string[]>([])
const dangLuu = ref(false)

async function luuDot() {
  dangLuu.value = true
  loiLuu.value = []
  try {
    const r = await api<{ message: string }>('/api/admin/dua-top/luu-dot', { method: 'POST', body: { hang: hang.value } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    hang.value = taoHangTrong()
    await refresh()
  } catch (e: any) {
    loiLuu.value = Array.isArray(e?.data?.data?.loi) ? e.data.data.loi : []
    toast.add({ title: 'Chưa lưu', description: loiLuu.value.length ? 'Chưa lưu gì cả, sửa lỗi rồi lưu lại.' : loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLuu.value = false
  }
}

// ---------------------------------------------------------------------------
//  Bộ chọn vật phẩm
// ---------------------------------------------------------------------------
const maNhom = ref('')
const tim = ref('')
const idChon = ref('')
const giaTri = ref<Record<string, string>>({})

watch(() => data.value?.dmuc, (dm) => {
  if (dm?.length && !dm.some(n => n.ma === maNhom.value)) maNhom.value = dm[0]!.ma
}, { immediate: true })

const nhom = computed(() => data.value?.dmuc.find(n => n.ma === maNhom.value) ?? null)
const truong = computed<TruongThuong[]>(() => (nhom.value ? data.value?.truong[nhom.value.loai] ?? [] : []))
const truongHien = computed(() => truong.value.filter(t => !t.an))

const mucLoc = computed(() => {
  const n = nhom.value
  if (!n) return []
  const tu = tim.value.trim().toLowerCase()
  return tu === '' ? n.muc : n.muc.filter(m => m.ten.toLowerCase().includes(tu) || m.id.includes(tu))
})
const demMuc = computed(() => {
  const tu = tim.value.trim().toLowerCase()
  return tu === '' ? `(${mucLoc.value.length})` : `(${mucLoc.value.length} khớp “${tu}”)`
})

// Đổi nhóm: đặt lại các ô về mặc định. Đổi danh sách: chọn sẵn món đầu tiên.
watch(nhom, (n) => {
  const g: Record<string, string> = {}
  for (const t of data.value?.truong[n?.loai ?? ''] ?? []) g[t.ma] = t.macDinh
  giaTri.value = g
}, { immediate: true })
watch(mucLoc, (ds) => {
  idChon.value = ds[0]?.id ?? ''
}, { immediate: true })

const chuoi = computed(() => (nhom.value && idChon.value ? dmGhepChuoi(nhom.value.loai, idChon.value, giaTri.value) : ''))

const optHang = computed(() => Array.from({ length: soHang.value }, (_, i) => ({ label: `Hạng ${i + 1}`, value: i + 1 })))
const optNhom = computed(() => (data.value?.dmuc ?? []).map(n => ({ label: n.ten, value: n.ma })))

function oDich() {
  return hang.value[hangDich.value - 1]!
}

function themCoDinh() {
  if (chuoi.value === '') return
  const o = oDich()
  o.item = dmNoiThem(o.item, chuoi.value)
}

/* "Cho người chơi tự chọn": thêm ĐÚNG món đang chọn vào ô tự chọn, dạng "nhãn=chuỗi",
   nối cách nhau bằng "|" — mỗi đoạn là MỘT lựa chọn trong menu NPC lúc người chơi nhận
   thưởng (xem real/SuKienDuaTop.doChonThuong). */
function themTuChon() {
  if (chuoi.value === '' || !nhom.value) return
  const m = nhom.value.muc.find(x => x.id === idChon.value)
  const nhan = duaTopLamSachNhan(m?.ten || idChon.value)
  const o = oDich()
  o.itemchon = duaTopNoiLuaChon(o.itemchon, `${nhan}=${chuoi.value}`)
}

function themCaNhom() {
  if (!nhom.value) return
  const doans = duaTopToHopCaNhom(nhom.value, truong.value, giaTri.value)
  if (!doans?.length) return
  const o = oDich()
  o.itemchon = duaTopNoiLuaChon(o.itemchon, doans.join('|'))
}

// Chèn mẫu có sẵn vào đúng ô của hạng đang chọn — tắt JavaScript thì chỉ mất nút tiện
// lợi này, vẫn gõ tay được.
function chenMau(viDu: string) {
  const o = oDich()
  o.item = dmNoiThem(o.item, viDu)
}

// ---------------------------------------------------------------------------
//  Huỷ dòng đang chờ
// ---------------------------------------------------------------------------
const moHuy = ref(false)
const dongHuy = ref<DuaTopDongView | null>(null)
const dangHuy = ref(false)

function hoiHuy(r: DuaTopDongView) {
  dongHuy.value = r
  moHuy.value = true
}

async function huy() {
  if (!dongHuy.value) return
  dangHuy.value = true
  try {
    const r = await api<{ message: string }>('/api/admin/dua-top/xoa', { method: 'POST', body: { id: dongHuy.value.id } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    moHuy.value = false
    await refresh()
  } catch (e) {
    toast.add({ title: 'Không huỷ được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangHuy.value = false
  }
}
</script>

<template>
  <AdminPage title="Đua top">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" @click="refresh()">
        Tải lại
      </UButton>
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">
        Phát quà đua top
      </h1>
      <p class="mt-1 max-w-4xl text-muted">
        {{ soHang }} hạng. Điền tên Pokémon + chọn phần thưởng cho hạng nào cần phát — để trống hạng
        không dùng. Pokémon được điền tên sẽ thấy mục "Nhận thưởng đua top" ở NPC Tổng Quản, chỉ đúng người đó
        mới nhận được đúng phần thưởng của mình.
      </p>
    </div>

    <UAlert
      v-if="error && !data"
      color="error"
      variant="subtle"
      icon="i-lucide-circle-x"
      title="Không tải được dữ liệu đua top"
      :description="loiApi(error)"
    />
    <UAlert v-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-x" :title="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <UCard v-if="duocGhi">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Tạo đợt phát quà mới
        </h3>
      </template>

      <div class="space-y-6">
        <!-- ============ Bộ chọn vật phẩm ============ -->
        <div v-if="data?.dmuc.length" class="space-y-4 rounded-lg border border-default bg-elevated/30 p-4">
          <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <strong class="text-highlighted">Chọn vật phẩm</strong>
            <span class="text-xs text-muted">chọn hạng muốn điền ở ô bên dưới rồi chọn vật phẩm, bấm Thêm</span>
          </div>

          <div class="grid gap-4 md:grid-cols-[10rem_1fr_1fr]">
            <UFormField label="Điền vào hạng">
              <USelect v-model="hangDich" :items="optHang" class="w-full" />
            </UFormField>
            <UFormField label="Nhóm">
              <USelect v-model="maNhom" :items="optNhom" class="w-full" />
            </UFormField>
            <UFormField label="Tìm theo tên hoặc id">
              <UInput v-model="tim" icon="i-lucide-search" placeholder="ví dụ: trứng, cấp 6, 848" class="w-full" />
            </UFormField>
          </div>

          <div class="grid gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
            <UFormField>
              <template #label>
                Vật phẩm <span class="text-xs text-muted">{{ demMuc }}</span>
              </template>
              <div class="h-56 overflow-y-auto rounded-md border border-default bg-default" role="listbox">
                <button
                  v-for="m in mucLoc"
                  :key="m.id"
                  type="button"
                  role="option"
                  :aria-selected="m.id === idChon"
                  class="block w-full px-3 py-1.5 text-left text-sm hover:bg-elevated"
                  :class="m.id === idChon ? 'bg-primary/15 text-highlighted' : 'text-toned'"
                  @click="idChon = m.id"
                >
                  <span class="num text-muted">{{ m.id }}</span> — {{ m.ten }}
                </button>
                <p v-if="!mucLoc.length" class="px-3 py-2 text-sm text-dimmed">
                  không có món nào khớp
                </p>
              </div>
            </UFormField>
            <div class="space-y-3">
              <UFormField v-for="t in truongHien" :key="t.ma" :label="t.nhan">
                <USelect
                  v-if="t.chon"
                  v-model="giaTri[t.ma]"
                  :items="t.chon.map(c => ({ label: c.label, value: c.value }))"
                  class="w-full"
                />
                <UInput v-else v-model="giaTri[t.ma]" type="number" :min="t.minSo ?? '1'" class="w-full" />
              </UFormField>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2">
            <UButton icon="i-lucide-plus" :disabled="chuoi === ''" @click="themCoDinh">
              Thêm vào phần thưởng của hạng đã chọn
            </UButton>
            <UButton icon="i-lucide-list-plus" color="neutral" variant="outline" :disabled="chuoi === ''" @click="themTuChon">
              Thêm vào "cho người chơi tự chọn"
            </UButton>
            <UButton icon="i-lucide-layers" color="neutral" variant="outline" @click="themCaNhom">
              Thêm CẢ NHÓM vào "cho người chơi tự chọn"
            </UButton>
            <code class="rounded bg-elevated px-2 py-1 text-xs text-highlighted">{{ chuoi === '' ? '—' : chuoi }}</code>
          </div>
          <p v-if="nhom?.ghiChu" class="text-xs text-muted">
            {{ nhom.ghiChu }}
          </p>
          <p class="text-xs text-muted">
            "Cho người chơi tự chọn": Pokémon bấm "Nhận thưởng đua top" ở NPC Tổng Quản sẽ
            thấy một menu để TỰ CHỌN 1 trong các món đã thêm vào đây, thay vì admin chọn sẵn một món cố định.
            Nút "Thêm CẢ NHÓM" đưa HẾT các tổ hợp có thể có của nhóm đang chọn thành các lựa chọn trong một lần
            bấm — không chỉ duyệt hết các món (vd 5 mặt nạ), mà còn duyệt hết CẢ các ô "Hệ"/"Kháng"... đang là
            dropdown nhiều lựa chọn (vd Ruy băng kháng ra đủ 2 Hệ × 6 Kháng = 12 lựa chọn); ô số lượng/thời hạn
            thì giữ nguyên giá trị đang đặt cho mọi lựa chọn.
          </p>
        </div>

        <!-- ============ Chèn nhanh ============ -->
        <div v-if="data?.chenNhanh.length" class="space-y-3 rounded-lg border border-default bg-elevated/30 p-4">
          <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
            <strong class="text-highlighted">Chèn nhanh — thú cưỡi, ruy băng kháng, đồ cầm tay thời trang/bảo vật, Gậy Ánh Trăng, mặt nạ</strong>
            <span class="text-xs text-muted">không có danh sách id để chọn, bấm Chèn rồi tự sửa số ngày/phút trong ô của hạng đang chọn ở "Điền vào hạng" bên trên</span>
          </div>
          <div class="max-h-80 overflow-auto rounded-md border border-default">
            <table class="w-full text-sm">
              <thead class="sticky top-0 bg-elevated text-left text-muted">
                <tr>
                  <th class="px-3 py-2 font-medium">
                    Cú pháp
                  </th>
                  <th class="px-3 py-2 font-medium">
                    Là gì
                  </th>
                  <th class="px-3 py-2 font-medium">
                    Mẫu chèn
                  </th>
                  <th class="px-3 py-2" />
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in data.chenNhanh" :key="c.loai" class="border-t border-default">
                  <td class="px-3 py-1.5 whitespace-nowrap">
                    <code>{{ c.cuPhap }}</code>
                  </td>
                  <td class="px-3 py-1.5 text-xs text-muted">
                    {{ c.moTa }}
                  </td>
                  <td class="px-3 py-1.5 whitespace-nowrap">
                    <code>{{ c.viDu }}</code>
                  </td>
                  <td class="px-3 py-1.5 text-right whitespace-nowrap">
                    <UButton size="xs" color="neutral" variant="outline" @click="chenMau(c.viDu)">
                      Chèn
                    </UButton>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <p class="text-xs text-muted">
          Xem đầy đủ cú pháp gõ tay (61 loại) tại trang <ULink to="/admin/giftcode" class="text-primary">
            Giftcode
          </ULink>.
        </p>

        <!-- ============ 10 hạng ============ -->
        <form class="space-y-4" @submit.prevent="luuDot">
          <div class="overflow-x-auto rounded-lg border border-default">
            <table class="w-full min-w-[56rem] text-sm">
              <thead class="bg-elevated/50 text-left text-muted">
                <tr>
                  <th class="px-3 py-2 text-right font-medium">
                    Hạng
                  </th>
                  <th class="px-3 py-2 font-medium">
                    Tên Pokémon
                  </th>
                  <th class="px-3 py-2 font-medium">
                    Phần thưởng cố định
                  </th>
                  <th class="px-3 py-2 font-medium">
                    Cho người chơi tự chọn (tuỳ chọn)
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="(h, i) in hang"
                  :key="i"
                  class="border-t border-default align-top"
                  :class="hangDich === i + 1 ? 'bg-primary/5' : ''"
                  @focusin="hangDich = i + 1"
                >
                  <td class="px-3 py-2 text-right whitespace-nowrap">
                    <strong class="text-highlighted">Top {{ i + 1 }}</strong>
                  </td>
                  <td class="w-56 px-3 py-2">
                    <UInput v-model="h.ten" maxlength="50" placeholder="tên Pokémon" class="w-full" />
                  </td>
                  <td class="px-3 py-2">
                    <UTextarea v-model="h.item" :rows="2" autoresize placeholder="ITEM:848:1000,GEM:73:100:-1" class="w-full font-mono text-xs" />
                  </td>
                  <td class="px-3 py-2">
                    <UTextarea v-model="h.itemchon" :rows="2" autoresize placeholder="Mặt nạ Pumpkaboo=MATNA:0:_:0|Mặt nạ Gengar=MATNA:4:_:0" class="w-full font-mono text-xs" />
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <UAlert v-if="loiLuu.length" color="error" variant="subtle" icon="i-lucide-circle-x" title="Chưa lưu gì cả, sửa lỗi rồi lưu lại:">
            <template #description>
              <ul class="list-disc space-y-1 pl-5">
                <li v-for="(l, i) in loiLuu" :key="i">
                  {{ l }}
                </li>
              </ul>
            </template>
          </UAlert>

          <div class="flex justify-end">
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu">
              Lưu đợt phát quà
            </UButton>
          </div>
        </form>
      </div>
    </UCard>

    <!-- ============ Đang chờ nhận ============ -->
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Đang chờ nhận ({{ data?.dangCho.length ?? 0 }})
        </h3>
      </template>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-elevated/50 text-left text-muted">
            <tr>
              <th class="px-4 py-2 text-right font-medium">
                Hạng
              </th>
              <th class="px-4 py-2 font-medium">
                Pokémon
              </th>
              <th class="px-4 py-2 font-medium">
                Phần thưởng
              </th>
              <th class="px-4 py-2 font-medium">
                Người tạo
              </th>
              <th class="px-4 py-2 text-right font-medium">
                Tạo lúc
              </th>
              <th class="px-4 py-2" />
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in data?.dangCho ?? []" :key="r.id" class="border-t border-default align-top">
              <td class="px-4 py-2 text-right whitespace-nowrap">
                <strong class="text-highlighted">Top {{ r.vi_tri }}</strong>
              </td>
              <td class="px-4 py-2">
                <ULink :to="{ path: '/admin/nhanvat', query: { q: r.charname } }" class="text-primary">
                  {{ r.charname }}
                </ULink>
              </td>
              <td class="px-4 py-2">
                <AdminDuaTopPhanThuong :dong="r" />
              </td>
              <td class="px-4 py-2 text-toned">
                {{ r.admin_user }}
              </td>
              <td class="num px-4 py-2 text-right whitespace-nowrap text-muted">
                {{ r.tao_luc }}
              </td>
              <td class="px-4 py-2 text-right">
                <UButton v-if="duocGhi" size="xs" color="error" variant="soft" icon="i-lucide-x" @click="hoiHuy(r)">
                  Huỷ
                </UButton>
              </td>
            </tr>
            <tr v-if="!data?.dangCho.length">
              <td colspan="6" class="px-4 py-4 text-muted">
                Không có ai đang chờ nhận.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>

    <!-- ============ Đã nhận ============ -->
    <UCard :ui="{ body: 'p-0 sm:p-0' }">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Đã nhận (mới nhất 100 dòng)
        </h3>
      </template>
      <div class="overflow-x-auto">
        <table class="w-full text-sm">
          <thead class="bg-elevated/50 text-left text-muted">
            <tr>
              <th class="px-4 py-2 text-right font-medium">
                Hạng
              </th>
              <th class="px-4 py-2 font-medium">
                Pokémon
              </th>
              <th class="px-4 py-2 font-medium">
                Phần thưởng
              </th>
              <th class="px-4 py-2 text-right font-medium">
                Nhận lúc
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in data?.daNhan ?? []" :key="r.id" class="border-t border-default align-top">
              <td class="px-4 py-2 text-right whitespace-nowrap text-toned">
                Top {{ r.vi_tri }}
              </td>
              <td class="px-4 py-2">
                <ULink :to="{ path: '/admin/nhanvat', query: { q: r.charname } }" class="text-primary">
                  {{ r.charname }}
                </ULink>
              </td>
              <td class="px-4 py-2">
                <AdminDuaTopPhanThuong :dong="r" />
              </td>
              <td class="num px-4 py-2 text-right whitespace-nowrap text-muted">
                {{ r.nhan_luc }}
              </td>
            </tr>
            <tr v-if="!data?.daNhan.length">
              <td colspan="4" class="px-4 py-4 text-muted">
                Chưa ai nhận thưởng.
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </UCard>

    <UModal
      v-model:open="moHuy"
      title="Huỷ phần thưởng"
      :description="dongHuy ? `Huỷ phần thưởng hạng ${dongHuy.vi_tri} của ${dongHuy.charname}?` : ''"
    >
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton label="Thôi" color="neutral" variant="ghost" @click="moHuy = false" />
          <UButton label="Huỷ phần thưởng" color="error" :loading="dangHuy" @click="huy" />
        </div>
      </template>
    </UModal>
  </AdminPage>
</template>
