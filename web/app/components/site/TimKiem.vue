<script setup lang="ts">
/**
 * Tìm kiếm phía trình duyệt (không gọi server): trang chính, cẩm nang (cả từng mục), Pokémon, tin tức.
 * Mở bằng nút kính lúp, Ctrl/⌘+K hoặc phím "/". Không phân biệt dấu tiếng Việt.
 */
interface KetQua { nhom: string, ten: string, mo?: string, to: string, icon: string, khoa: string, dex?: number, anh?: string }

const mo = ref(false)
const q = ref('')
const chon = ref(0)

const TRANG: KetQua[] = [
  { nhom: 'Trang', ten: 'Trang chủ', to: '/', icon: 'i-lucide-house' },
  { nhom: 'Trang', ten: 'Giới thiệu game', mo: 'Thế giới, cốt truyện, điểm độc đáo', to: '/gioi-thieu', icon: 'i-lucide-info' },
  { nhom: 'Trang', ten: 'Hướng dẫn tân thủ', mo: 'Tải, cài, tạo tài khoản, điều khiển, 30 phút đầu', to: '/huong-dan', icon: 'i-lucide-compass' },
  { nhom: 'Trang', ten: 'Điều khiển phím & cảm ứng', to: '/huong-dan#dieu-khien', icon: 'i-lucide-keyboard' },
  { nhom: 'Trang', ten: 'Giao diện trong game (HUD)', to: '/huong-dan#giao-dien', icon: 'i-lucide-layout-dashboard' },
  { nhom: 'Trang', ten: 'Cẩm nang cơ chế', to: '/cam-nang', icon: 'i-lucide-library' },
  { nhom: 'Trang', ten: 'Pokédex', to: '/pokedex', icon: 'i-lucide-book-open' },
  { nhom: 'Trang', ten: 'Tải game', mo: 'Java J2ME (.jar), Windows (.exe), Linux, Android APK, iOS sắp ra mắt, cấu hình', to: '/download', icon: 'i-lucide-download' },
  { nhom: 'Trang', ten: 'Bảng xếp hạng', to: '/ranking', icon: 'i-lucide-trophy' },
  { nhom: 'Trang', ten: 'Tin tức', to: '/news', icon: 'i-lucide-newspaper' },
  { nhom: 'Trang', ten: 'Điều khoản sử dụng', to: '/dieu-khoan', icon: 'i-lucide-scale' },
  { nhom: 'Trang', ten: 'Quyền riêng tư', to: '/quyen-rieng-tu', icon: 'i-lucide-shield' },
  { nhom: 'Trang', ten: 'Đăng ký tài khoản', to: '/register', icon: 'i-lucide-user-plus' },
].map(x => ({ ...x, khoa: boDau(`${x.ten} ${x.mo ?? ''}`) }))

const danhMuc = useDanhMucCamNang()
const CAM = computed<KetQua[]>(() => danhMuc.value.flatMap(p => [
  { nhom: 'Cẩm nang', ten: p.ten, mo: p.moTa, to: `/cam-nang/${p.slug}`, icon: p.icon, khoa: boDau(`${p.ten} ${p.moTa} ${p.tuKhoa ?? ''}`) },
  ...(p.muc ?? []).map(m => ({ nhom: 'Cẩm nang', ten: m.ten, mo: p.ten, to: `/cam-nang/${p.slug}#${m.id}`, icon: 'i-lucide-hash', khoa: boDau(`${m.ten} ${p.ten}`) })),
]))
// Pokédex (~90 KB) chỉ tải khi mở hộp tìm kiếm lần đầu — không nằm trong gói JS của mọi trang.
const POKE = shallowRef<KetQua[]>([])
async function napPokedex() {
  if (POKE.value.length) return
  const { POKEDEX, tenHe, anhLoaiDex } = await import('~~/shared/utils/pokedex')
  POKE.value = POKEDEX.loai.map(l => ({ nhom: 'Pokémon', ten: l.ten, mo: `#${String(l.dex).padStart(3, '0')} · ${l.he.map(tenHe).join('/')}`, to: `/pokedex/${l.dex}`, icon: 'i-lucide-book-open', dex: l.dex, anh: anhLoaiDex(l), khoa: boDau(`${l.ten} ${l.dex} ${l.he.map(tenHe).join(' ')}`) }))
}
const { data: dsTin } = useFetch<TinTomTatWeb[]>('/api/tin-tuc', { key: 'htbb-tin', default: () => [], server: false, lazy: true })
const TIN = computed<KetQua[]>(() => (dsTin.value ?? []).map(a => ({ nhom: 'Tin tức', ten: a.tieuDe, mo: a.loai, to: `/news/${a.slug}`, icon: 'i-lucide-newspaper', khoa: boDau(`${a.tieuDe} ${a.tomTat} ${a.the.join(' ')}`) })))
const TAT_CA = computed(() => [...TRANG, ...CAM.value, ...POKE.value, ...TIN.value])

const ketQua = computed(() => {
  const tu = boDau(q.value.trim()).split(/\s+/).filter(Boolean)
  if (!tu.length) return [...TRANG.slice(1, 4), ...CAM.value.filter(c => !c.to.includes('#')).slice(0, 5)]
  return TAT_CA.value
    .map(x => ({ x, diem: tu.every(t => x.khoa.includes(t)) ? (boDau(x.ten).startsWith(tu[0]!) ? 2 : 1) : 0 }))
    .filter(r => r.diem > 0)
    .sort((a, b) => b.diem - a.diem)
    .slice(0, 24)
    .map(r => r.x)
})
const nhomKq = computed(() => {
  const ra: { nhom: string, ds: { x: KetQua, i: number }[] }[] = []
  ketQua.value.forEach((x, i) => {
    let g = ra.find(r => r.nhom === x.nhom)
    if (!g) { g = { nhom: x.nhom, ds: [] }; ra.push(g) }
    g.ds.push({ x, i })
  })
  return ra
})
watch(q, () => { chon.value = 0 })
watch(mo, (v) => { if (v) { q.value = ''; chon.value = 0; napPokedex() } })

function di(x?: KetQua) {
  if (!x) return
  mo.value = false
  navigateTo(x.to)
}
function phim(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') { chon.value = Math.min(ketQua.value.length - 1, chon.value + 1); e.preventDefault() }
  else if (e.key === 'ArrowUp') { chon.value = Math.max(0, chon.value - 1); e.preventDefault() }
  else if (e.key === 'Enter') { di(ketQua.value[chon.value]); e.preventDefault() }
}

defineShortcuts({
  meta_k: () => { mo.value = true },
  '/': () => { mo.value = true },
})
</script>

<template>
  <UModal v-model:open="mo" title="Tìm kiếm" description="Tìm trang, cẩm nang, Pokémon, tin tức" :ui="{ content: 'sm:max-w-xl', header: 'sr-only', body: 'p-0 sm:p-0' }">
    <!-- Đầu trang chật: chỉ một nút kính lúp; bấm (hoặc Ctrl/⌘+K, "/") mở ô tìm kiếm. -->
    <span class="flex">
      <UButton color="neutral" variant="ghost" icon="i-lucide-search" aria-label="Tìm kiếm" title="Tìm kiếm (Ctrl+K)" aria-keyshortcuts="Control+K" />
    </span>
    <template #body>
      <div class="border-b border-default p-3">
        <UInput v-model="q" autofocus icon="i-lucide-search" size="xl" variant="none" placeholder="Gõ: khắc hệ, nâng cấp, Pikachu, sự kiện..." class="w-full" aria-label="Từ khoá tìm kiếm" @keydown="phim" />
      </div>
      <div class="max-h-[60vh] overflow-y-auto p-2" role="listbox">
        <div v-for="g in nhomKq" :key="g.nhom" class="mb-2">
          <p class="px-2 py-1 text-xs font-extrabold uppercase tracking-wider text-dimmed">
            {{ g.nhom }}
          </p>
          <NuxtLink
            v-for="{ x, i } in g.ds"
            :key="x.to"
            :to="x.to"
            role="option"
            :aria-selected="chon === i"
            class="flex items-center gap-3 rounded-xl px-2 py-2"
            :class="chon === i ? 'bg-elevated' : 'hover:bg-elevated/60'"
            @mouseenter="chon = i"
            @click="mo = false"
          >
            <span v-if="x.anh" class="inline-grid size-8 shrink-0 place-items-center overflow-hidden rounded-full bg-elevated"><img :src="x.anh" width="40" height="40" alt="" loading="lazy" decoding="async" class="pixelated size-full object-cover"></span>
            <span v-else class="grid size-8 shrink-0 place-items-center rounded-lg bg-elevated text-primary"><UIcon :name="x.icon" class="size-4" /></span>
            <span class="min-w-0">
              <span class="block truncate font-semibold text-highlighted">{{ x.ten }}</span>
              <span v-if="x.mo" class="block truncate text-xs text-muted">{{ x.mo }}</span>
            </span>
            <UIcon v-if="chon === i" name="i-lucide-corner-down-left" class="ml-auto size-4 text-dimmed" />
          </NuxtLink>
        </div>
        <p v-if="!ketQua.length" class="p-6 text-center text-sm text-muted">
          Không tìm thấy "{{ q }}". Thử từ khác hoặc xem <NuxtLink to="/cam-nang" class="text-primary" @click="mo = false">mục lục cẩm nang</NuxtLink>.
        </p>
      </div>
    </template>
  </UModal>
</template>
