<script setup lang="ts">
/**
 * Trang ghi công (việc W3, HTBB/docs/pokemon/KE_HOACH_CHUYEN_DOI.md mục 3.3).
 * Bắt buộc theo CC BY-NC 4.0: ghi tên người vẽ từng sprite/chân dung PMDCollab.
 *
 * Nguồn: public/pokemon/GHI_CONG.txt — chép lại từ
 * HTBB/ClientUnity/Assets/Resources/poke/GHI_CONG.txt mỗi khi client đóng gói lại sprite.
 * Đọc lúc build (?raw) nên không phụ thuộc file tĩnh khi chạy.
 */
import ghiCongTho from '~~/public/pokemon/GHI_CONG.txt?raw'

const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Ghi công',
  description: `Ghi công tác giả sprite và chân dung Pokémon (PMDCollab SpriteCollab, CC BY-NC 4.0) dùng trong ${tenSite}.`,
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

const taiNguyen = [
  { ten: 'PMDCollab SpriteCollab', url: 'https://github.com/PMDCollab/SpriteCollab', dung: 'Sprite động & chân dung Pokémon (game + website)', gp: 'CC BY-NC 4.0 — ghi công từng tác giả bên dưới' },
  { ten: 'PMDCollab RawAsset', url: 'https://github.com/PMDCollab/RawAsset', dung: 'Hiệu ứng chiêu (particle), icon vật phẩm, vật thể trên bản đồ', gp: 'Không ghi giấy phép; phần lớn trích từ Pokémon Mystery Dungeon (© Nintendo / Chunsoft) — chỉ dùng phi thương mại' },
  { ten: 'Kenney — Tiny Town', url: 'https://kenney.nl/assets/tiny-town', dung: 'Tile bản đồ: cỏ, đất, cây, nhà, hàng rào, biển', gp: 'CC0 1.0' },
  { ten: 'Kenney — Tiny Dungeon', url: 'https://kenney.nl/assets/tiny-dungeon', dung: 'Tile hang động, đồ nội thất, rương', gp: 'CC0 1.0' },
  { ten: 'Ninja Adventure (Pixel-boy & AAA)', url: 'https://pixel-boy.itch.io/ninja-adventure-asset-pack', dung: 'Sàn gỗ, tường và đồ trong nhà', gp: 'CC0 1.0' },
  { ten: 'Phông Nunito, Baloo 2, Press Start 2P', url: 'https://fonts.google.com', dung: 'Chữ trên website (tự host)', gp: 'SIL Open Font License 1.1' },
  { ten: 'Lucide Icons', url: 'https://lucide.dev', dung: 'Biểu tượng giao diện website', gp: 'ISC' },
]

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
          </div>
        </FxReveal>

        <FxReveal :delay="0.03">
          <div class="mt-10">
            <h2 class="section-title text-2xl text-highlighted">
              Tài nguyên khác
            </h2>
            <div class="bang-cuon mt-4">
              <table class="bang-wiki">
                <thead>
                  <tr>
                    <th>Tài nguyên</th><th>Dùng cho</th><th>Giấy phép</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="r in taiNguyen" :key="r.ten">
                    <td class="font-semibold text-highlighted">
                      <ULink :to="r.url" target="_blank" rel="noopener" class="hover:text-primary hover:underline">
                        {{ r.ten }}
                      </ULink>
                    </td>
                    <td>{{ r.dung }}</td>
                    <td>{{ r.gp }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            <p class="mt-3 text-sm text-muted">
              Âm thanh: client hiện chưa phát nhạc/âm thanh. Khi bổ sung, dự án chỉ dùng nhạc và hiệu ứng âm thanh <strong class="text-highlighted">CC0</strong> có nguồn rõ ràng
              (sẽ liệt kê tại đây) — không dùng nhạc gốc của dòng game Pokémon.
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
              <PokeAvatar v-if="l.anh" :src="l.anh" class="size-12" />
              <span v-else class="grid size-12 place-items-center rounded-lg bg-screen text-xs text-dimmed" aria-hidden="true">#</span>
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
