<script setup lang="ts">
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Cẩm nang cơ chế',
  description: `Cẩm nang ${tenSite}: hệ & khắc hệ, chỉ số, cấp độ, tiến hoá, trang bị, kinh tế, Pokédex, PvP, boss, sự kiện, nhiệm vụ và bản đồ.`,
})

const tim = ref('')
const danhMuc = useDanhMucCamNang()
const nhom = computed(() => {
  const t = boDau(tim.value.trim())
  return NHOM_CAM_NANG.map(n => ({
    ten: n,
    trang: danhMuc.value.filter(p => p.nhom === n && (!t || boDau(`${p.ten} ${p.moTa} ${p.tuKhoa ?? ''} ${(p.muc ?? []).map(m => m.ten).join(' ')}`).includes(t))),
  })).filter(n => n.trang.length)
})

const C = CAM_NANG
const nhanh = [
  { so: String(POKEDEX.he.length), nhan: 'hệ Pokémon', to: '/cam-nang/he-khac-che' },
  { so: String(C.capDo.capToiDa), nhan: 'cấp tối đa', to: '/cam-nang/cap-do' },
  { so: `+${C.trangBi.nangCap.length}`, nhan: 'mức nâng cấp', to: '/cam-nang/trang-bi' },
  { so: String(C.suKien.length), nhan: 'tuần Pokémon / năm', to: '/cam-nang/su-kien' },
  { so: String(C.nhiemVu.cung.filter(c => c.nam === 1).reduce((s, c) => s + c.chuong.length, 0)), nhan: 'chương chính tuyến năm 1', to: '/cam-nang/nhiem-vu' },
  { so: String(POKEDEX.loai.length), nhan: 'loài trong Pokédex', to: '/pokedex' },
]
</script>

<template>
  <div>
    <SitePageBanner
      title="Cẩm nang cơ chế"
      eyebrow="Wiki"
      :intro="`Mọi luật chơi của ${tenSite} — giải thích dễ hiểu, kèm bảng số liệu chi tiết.`"
    />
    <UContainer class="max-w-7xl py-12 sm:py-16">
      <div class="rounded-3xl glass ring-gilded p-4 sm:p-6">
        <UInput
          v-model="tim"
          icon="i-lucide-search"
          size="xl"
          placeholder="Tìm trong cẩm nang: khắc hệ, nâng cấp, shiny, thuế chợ..."
          class="w-full"
          type="search"
          aria-label="Tìm trong cẩm nang"
        />
        <ul class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
          <li v-for="x in nhanh" :key="x.nhan">
            <NuxtLink :to="x.to" class="block rounded-2xl bg-screen p-3 text-center transition hover:-translate-y-0.5">
              <b class="block font-display text-2xl font-extrabold text-pokered-600 dark:text-pikachu-300">{{ x.so }}</b>
              <span class="text-xs font-semibold text-muted">{{ x.nhan }}</span>
            </NuxtLink>
          </li>
        </ul>
      </div>

      <div v-for="n in nhom" :key="n.ten" class="mt-12">
        <h2 class="section-title flex items-center gap-3 text-2xl text-highlighted">
          <PokeBall class="size-6" /> {{ n.ten }}
        </h2>
        <ul class="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <li v-for="p in n.trang" :key="p.slug">
            <NuxtLink :to="`/cam-nang/${p.slug}`" class="the-go group flex h-full flex-col p-5 hover:-translate-y-1">
              <span class="flex items-center gap-3">
                <SiteHinhCamNang :slug="p.slug" :icon="p.icon" class="size-12" />
                <span class="font-display text-xl font-bold text-highlighted group-hover:text-primary">{{ p.ten }}</span>
              </span>
              <span class="mt-3 flex-1 text-sm text-muted">{{ p.moTa }}</span>
              <span v-if="p.muc?.length" class="mt-3 flex flex-wrap gap-1.5">
                <span v-for="m in p.muc.slice(0, 4)" :key="m.id" class="rounded-full bg-elevated px-2 py-0.5 text-[11px] font-semibold text-toned">{{ m.ten }}</span>
              </span>
            </NuxtLink>
          </li>
        </ul>
      </div>
      <div v-if="!nhom.length" class="mt-12 flex flex-col items-center gap-4 text-center">
        <PokeBall wiggle class="size-14" />
        <p class="dialog-box no-arrow max-w-md">
          Không tìm thấy trang nào. Thử từ khoá khác, ví dụ "đá", "PvP", "xu".
        </p>
      </div>
    </UContainer>
  </div>
</template>
