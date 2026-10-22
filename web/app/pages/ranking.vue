<script setup lang="ts">
/**
 * Bảng xếp hạng công khai. Danh sách bảng + bộ lọc: GET /api/xep-hang (theo cài đặt /admin/xep-hang).
 * Dữ liệu từng bảng: GET /api/xep-hang/<mã>?<bộ lọc> — server đệm theo "Làm mới (giây)".
 * Bảng "Bảng Top trong game" chép đúng truy vấn/thứ tự của game. Chỉ có tên nhân vật / tên đội.
 * Trạng thái bảng + bộ lọc nằm trên URL (?bang=pvp&mua=0) để chia sẻ được.
 */
import type { RankRow } from '~/components/site/RankBoard.vue'
import { bacDanhHieu } from '~~/shared/utils/doHiem.gen'

useSeoMeta({
  title: 'Bảng xếp hạng',
  description: 'Bảng xếp hạng Huyền Thoại Bảo Bối: sức mạnh, đại gia, công trạng, đội, Pokédex, PvP mùa giải, huy hiệu Gym, Hầm Ngục Bí Ẩn và sự kiện — cập nhật liên tục từ máy chủ.',
})

interface LuaChon { gt: string, nhan: string }
interface BangCK { ma: string, ten: string, icon: string, nhom: string, moTa: string, trongGame: boolean, boLoc: Array<{ khoa: string, nhan: string, lua: LuaChon[], macDinh: string }> }
interface DanhSach { bang: BangCK[], moiTrang: number, lamMoiGiay: number, loi?: boolean }
interface Dong { hang: number, ten: string, loai: string, dex: number, shiny: boolean, vung: number, giaTri: number | null, phu: string, bac?: string, danhHieu?: { ten: string, doHiem: number } }
interface KetQua { ma: string, ten: string, laDoi: boolean, cot: { ten: string, giaTri: string, phu: string }, dong: Dong[], loc: Record<string, string>, ghiChu: string, capNhatLuc: string, loi: boolean }

const route = useRoute()
const router = useRouter()

const { data: ds, error: loiDs } = await useFetch<DanhSach>('/api/xep-hang', { key: 'htbb-xep-hang-ds' })

const dsBang = computed(() => ds.value?.bang ?? [])
const maBang = computed(() => {
  const q = typeof route.query.bang === 'string' ? route.query.bang : ''
  return dsBang.value.some(b => b.ma === q) ? q : dsBang.value[0]?.ma ?? ''
})
const bang = computed(() => dsBang.value.find(b => b.ma === maBang.value))

/** Bộ lọc hiện tại: từ URL, sai thì mặc định. */
const loc = computed<Record<string, string>>(() => {
  const ra: Record<string, string> = {}
  for (const l of bang.value?.boLoc ?? []) {
    const v = typeof route.query[l.khoa] === 'string' ? String(route.query[l.khoa]) : ''
    ra[l.khoa] = l.lua.some(x => x.gt === v) ? v : l.macDinh
  }
  return ra
})

function chonBang(ma: string) {
  router.replace({ query: { bang: ma } })
}
function chonLoc(khoa: string, gt: string) {
  router.replace({ query: { ...route.query, bang: maBang.value, [khoa]: gt } })
}

const { data: kq, pending, error: loiKq, refresh } = await useFetch<KetQua>(
  () => `/api/xep-hang/${maBang.value || '_'}`,
  { query: loc, key: 'htbb-xep-hang-bang', immediate: !!maBang.value, watch: [maBang, loc] },
)

// Phân trang phía trình duyệt (server đã cắt theo "Số dòng tối đa").
const trang = ref(1)
watch([maBang, loc], () => { trang.value = 1 })
const moiTrang = computed(() => ds.value?.moiTrang ?? 10)
const tatCaDong = computed(() => kq.value?.dong ?? [])
const dongTrang = computed(() => {
  // Trang 1 có bục: 3 + moiTrang dòng bảng; các trang sau mỗi trang moiTrang dòng.
  const n = tatCaDong.value.length
  if (trang.value === 1) return tatCaDong.value.slice(0, Math.min(n, 3 + moiTrang.value))
  const bd = 3 + moiTrang.value * (trang.value - 1)
  return tatCaDong.value.slice(bd, bd + moiTrang.value)
})
const soTrang = computed(() => Math.max(1, Math.ceil(Math.max(0, tatCaDong.value.length - 3) / moiTrang.value)))

const VUNG = ['Kanto', 'Johto', 'Hoenn']
const rows = computed<RankRow[]>(() => dongTrang.value.map((d) => {
  const nhan: string[] = []
  if (d.vung >= 0 && d.vung <= 2) nhan.push(VUNG[d.vung]!)
  if (d.shiny) nhan.push('Shiny')
  if (d.bac) nhan.push(d.bac)
  // 0.9.7 danh hiệu đang đeo (data_danh_hieu): chip màu độ hiếm chung (cùng màu trong game)
  const bac = d.danhHieu?.ten ? bacDanhHieu(d.danhHieu.doHiem) : null
  const danhHieu = bac ? { ten: d.danhHieu!.ten, mau: bac.mau, nhat: bac.nhat } : undefined
  const sub = [d.loai, d.phu].filter(Boolean).join(' · ')
  return { name: d.ten, sub, anh: d.dex ? anhPokemon(d.dex) : '', value: d.giaTri, hang: d.hang, nhan, danhHieu }
}))

const capNhat = computed(() => {
  const t = kq.value?.capNhatLuc
  if (!t) return ''
  const d = new Date(t)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit', day: '2-digit', month: '2-digit' })
})

const nhomBang = computed(() => {
  const m = new Map<string, BangCK[]>()
  for (const b of dsBang.value) m.set(b.nhom, [...(m.get(b.nhom) ?? []), b])
  return [...m.entries()]
})
const luaBang = computed(() => dsBang.value.map(b => ({ label: b.ten, value: b.ma, icon: b.icon })))
</script>

<template>
  <div>
    <SitePageBanner
      title="Bảng xếp hạng"
      eyebrow="Bảng vàng Pokémon"
      intro="Cùng các bảng với menu “Bảng Top” trong game, cập nhật liên tục."
    />

    <section class="relative py-12 sm:py-16" aria-labelledby="xh-tieu-de">
      <FxGlowOrbs />
      <UContainer class="relative max-w-6xl">
        <h2 id="xh-tieu-de" class="sr-only">
          Chọn bảng xếp hạng
        </h2>

        <div v-if="loiDs || ds?.loi || !dsBang.length" class="glass mx-auto max-w-xl rounded-2xl p-10 text-center">
          <PokeBall class="mx-auto size-12 opacity-60 grayscale" />
          <p class="mt-4 font-semibold text-highlighted">
            {{ loiDs || ds?.loi ? 'Tạm thời chưa tải được bảng xếp hạng.' : 'Chưa có bảng xếp hạng nào đang mở.' }}
          </p>
          <p class="mt-1 text-sm text-muted">
            Vui lòng quay lại sau ít phút.
          </p>
        </div>

        <div v-else class="grid gap-8 lg:grid-cols-[15rem_1fr]">
          <!-- Chọn bảng: danh sách nhóm trên máy tính, ô chọn trên điện thoại -->
          <nav aria-label="Các bảng xếp hạng" class="hidden lg:block">
            <div class="glass sticky top-24 space-y-5 rounded-2xl p-4">
              <div v-for="[nhom, ds2] in nhomBang" :key="nhom">
                <p class="px-2 text-xs font-bold uppercase tracking-wider text-muted">
                  {{ nhom }}
                </p>
                <ul class="mt-1.5 space-y-0.5">
                  <li v-for="b in ds2" :key="b.ma">
                    <button
                      type="button"
                      class="flex w-full items-center gap-2 rounded-lg px-2 py-1.5 text-left text-sm font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-primary"
                      :class="b.ma === maBang ? 'bg-primary text-inverted' : 'text-toned hover:bg-elevated'"
                      :aria-current="b.ma === maBang ? 'page' : undefined"
                      @click="chonBang(b.ma)"
                    >
                      <UIcon :name="b.icon" class="size-4 shrink-0" />
                      <span class="truncate">{{ b.ten }}</span>
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          </nav>

          <div class="min-w-0">
            <div class="lg:hidden">
              <UFormField label="Bảng xếp hạng">
                <USelect :model-value="maBang" :items="luaBang" class="w-full" @update:model-value="(v: any) => chonBang(String(v))" />
              </UFormField>
            </div>

            <header v-if="bang" class="mt-6 lg:mt-0">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="font-display text-3xl font-extrabold text-highlighted">
                  {{ bang.ten }}
                </h3>
                <UBadge v-if="bang.trongGame" color="success" variant="subtle" icon="i-lucide-gamepad-2">
                  Giống bảng trong game
                </UBadge>
              </div>
              <p class="mt-2 max-w-3xl text-sm text-muted">
                {{ bang.moTa }}
              </p>
            </header>

            <div v-if="bang?.boLoc.length" class="mt-5 flex flex-wrap gap-3">
              <UFormField v-for="l in bang.boLoc" :key="l.khoa" :label="l.nhan" class="min-w-40">
                <USelect
                  :model-value="loc[l.khoa]"
                  :items="l.lua.map(x => ({ label: x.nhan, value: x.gt }))"
                  class="w-full"
                  @update:model-value="(v: any) => chonLoc(l.khoa, String(v))"
                />
              </UFormField>
            </div>

            <div class="mt-4 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-muted" aria-live="polite">
              <span v-if="pending" class="inline-flex items-center gap-1"><UIcon name="i-lucide-loader-circle" class="size-3.5 animate-spin" /> Đang tải…</span>
              <span v-else-if="capNhat">Cập nhật lúc {{ capNhat }} · làm mới mỗi {{ ds?.lamMoiGiay ?? 60 }} giây</span>
              <span v-if="kq?.ghiChu">{{ kq.ghiChu }}</span>
              <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-refresh-cw" label="Tải lại" class="ml-auto" :loading="pending" @click="() => refresh()" />
            </div>

            <div class="mt-6">
              <SiteRankBoard
                :key="`${maBang}-${JSON.stringify(loc)}-${trang}`"
                :rows="rows"
                :buc="trang === 1"
                :name-label="kq?.cot.ten || (kq?.laDoi ? 'Đội' : 'Pokémon')"
                :value-label="kq?.cot.giaTri || 'Điểm'"
                :error="!!loiKq || !!kq?.loi"
                error-text="Tạm thời chưa tải được bảng này. Thử lại sau ít phút."
                empty-text="Chưa có ai trên bảng này — hãy là người đầu tiên!"
              />
            </div>

            <div v-if="soTrang > 1" class="mt-6 flex justify-center">
              <UPagination v-model:page="trang" :total="Math.max(0, tatCaDong.length - 3)" :items-per-page="moiTrang" />
            </div>
          </div>
        </div>
      </UContainer>
    </section>
  </div>
</template>
