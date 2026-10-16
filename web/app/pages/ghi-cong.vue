<script setup lang="ts">
/**
 * Trang ghi công (việc W3, KPAH/docs/pokemon/KE_HOACH_CHUYEN_DOI.md mục 3.3).
 * Bắt buộc theo CC BY-NC 4.0: ghi tên người vẽ từng sprite/chân dung PMDCollab.
 *
 * Nguồn: public/pokemon/GHI_CONG.txt — chép lại từ
 * KPAH/ClientUnity/Assets/Resources/poke/GHI_CONG.txt mỗi khi client đóng gói lại sprite.
 * Đọc lúc build (?raw) nên không phụ thuộc file tĩnh khi chạy.
 */
import ghiCongTho from '~~/public/pokemon/GHI_CONG.txt?raw'

useSeoMeta({
  title: 'Ghi công',
  description: 'Ghi công tác giả sprite và chân dung Pokémon (PMDCollab SpriteCollab, CC BY-NC 4.0) dùng trong KPAH Pokémon.',
})

const ghiCong = docGhiCong(ghiCongTho)

const dsLoai = ghiCong.loai.map(l => ({
  ...l,
  ten: tenLoai(l.dex),
  anh: l.shiny ? '' : anhPokemon(l.dex),
  dong: l.dong.map(d => ({ ...d, ngay: d.thoiDiem.slice(0, 10) })),
}))

/** Mọi người vẽ, xếp theo số lần đóng góp. */
const nguoiVe = (() => {
  const dem = new Map<string, number>()
  for (const l of ghiCong.loai) for (const d of l.dong) dem.set(d.nguoiVe, (dem.get(d.nguoiVe) ?? 0) + 1)
  return [...dem.entries()].sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0]))
})()

const tim = ref('')
const loaiLoc = computed(() => {
  const t = tim.value.trim().toLowerCase()
  if (!t) return dsLoai
  return dsLoai.filter(l => l.ma.includes(t) || l.ten.toLowerCase().includes(t)
    || l.dong.some(d => d.nguoiVe.toLowerCase().includes(t)))
})
</script>

<template>
  <div>
    <SitePageBanner
      title="Ghi công"
      eyebrow="Giấy phép & tác giả"
      intro="Hình Pokémon trong game và trên website do cộng đồng PMDCollab vẽ. Cảm ơn từng người đã đóng góp."
    />

    <section class="py-16" aria-labelledby="giay-phep-title">
      <UContainer class="max-w-5xl">
        <FxReveal>
          <div class="rounded-3xl glass ring-gilded p-8">
            <h2 id="giay-phep-title" class="section-title text-3xl text-highlighted">
              Nguồn và giấy phép
            </h2>
            <ul class="mt-6 space-y-3 text-muted">
              <li>
                Sprite và chân dung Pokémon lấy từ
                <ULink to="https://github.com/PMDCollab/SpriteCollab" target="_blank" rel="noopener" class="text-primary hover:underline">PMDCollab SpriteCollab</ULink>,
                dùng theo giấy phép
                <ULink to="https://creativecommons.org/licenses/by-nc/4.0/" target="_blank" rel="noopener" class="text-primary hover:underline">Creative Commons BY-NC 4.0</ULink>
                (ghi công, <strong class="text-highlighted">không thương mại</strong>). Một số hình gốc do CHUNSOFT vẽ cho dòng game Pokémon Mystery Dungeon.
              </li>
              <li>
                Vì vậy máy chủ này <strong class="text-highlighted">phi thương mại</strong>: không bán vật phẩm, không nhận nạp tiền.
              </li>
              <li>
                Pokémon và mọi tên, hình liên quan © Nintendo / Creatures Inc. / GAME FREAK inc. / The Pokémon Company.
                Đây là dự án fan <strong class="text-highlighted">không chính thức</strong>, không liên quan và không được các công ty trên bảo trợ.
              </li>
              <li>
                Người vẽ ghi dạng "Discord #số" là tài khoản Discord trên máy chủ PMDCollab (danh sách gốc chỉ lưu mã số).
              </li>
            </ul>
            <p v-if="ghiCong.loiDan.length" class="mt-6 whitespace-pre-line rounded-2xl bg-ink-950/40 p-4 font-mono text-xs text-dimmed">
              {{ ghiCong.loiDan.join('\n') }}
            </p>
          </div>
        </FxReveal>

        <FxReveal :delay="0.05">
          <div class="mt-10">
            <h2 class="section-title text-2xl text-highlighted">
              Người vẽ ({{ nguoiVe.length }})
            </h2>
            <ul class="mt-4 flex flex-wrap gap-2">
              <li v-for="[ten, n] in nguoiVe" :key="ten">
                <UBadge color="neutral" variant="outline">
                  {{ ten }} <span class="text-dimmed">· {{ n }}</span>
                </UBadge>
              </li>
            </ul>
          </div>
        </FxReveal>

        <div class="mt-12 flex flex-wrap items-end justify-between gap-4">
          <h2 class="section-title text-2xl text-highlighted">
            Theo từng loài ({{ dsLoai.length }})
          </h2>
          <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm số dex, tên loài, người vẽ..." class="w-full sm:w-72" />
        </div>

        <div class="mt-6 grid gap-4 md:grid-cols-2">
          <article v-for="l in loaiLoc" :key="l.ma" class="rounded-2xl glass p-5">
            <header class="flex items-center gap-3">
              <img v-if="l.anh" :src="l.anh" width="40" height="40" alt="" class="pixelated size-12 rounded-lg bg-ink-950/60">
              <span v-else class="grid size-12 place-items-center rounded-lg bg-ink-950/60 text-xs text-dimmed" aria-hidden="true">#</span>
              <div>
                <h3 class="font-semibold text-highlighted">
                  #{{ l.ma.replace(/s$/, '') }}
                  <span v-if="!l.ten.startsWith('#')">· {{ l.ten }}</span>
                </h3>
                <UBadge v-if="l.shiny" color="primary" variant="subtle" size="sm">
                  Shiny
                </UBadge>
              </div>
            </header>
            <ul class="mt-4 space-y-2 text-sm">
              <li v-for="(d, j) in l.dong" :key="j" class="border-t border-default/50 pt-2">
                <div class="flex flex-wrap items-baseline justify-between gap-x-3">
                  <strong class="text-toned">{{ d.nguoiVe }}</strong>
                  <span class="text-xs text-dimmed">{{ d.ngay }} · {{ tenGiayPhep(d.giayPhep) }}</span>
                </div>
                <p v-if="d.dongTac.length" class="mt-1 text-xs text-muted">
                  {{ d.dongTac.join(', ') }}
                </p>
              </li>
            </ul>
          </article>
        </div>
        <p v-if="!loaiLoc.length" class="mt-6 text-center text-muted">
          Không có loài nào khớp.
        </p>
      </UContainer>
    </section>
  </div>
</template>
