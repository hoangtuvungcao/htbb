<script setup lang="ts">
/**
 * Giấy phép & nguồn tài nguyên (/giay-phep; /ghi-cong chuyển hướng 301 về đây — nuxt.config.ts).
 * Bắt buộc theo CC BY-NC 4.0: ghi tên người vẽ từng sprite/chân dung PMDCollab. Trang gọn, chỉ có một
 * link nhỏ ở chân trang (không nằm trong menu, cẩm nang, trang chủ).
 *
 * Nguồn: public/pokemon/GHI_CONG.txt — chép lại từ
 * client_pc_ios/Assets/Resources/poke/GHI_CONG.txt mỗi khi client đóng gói lại sprite.
 * Giấy phép từng thành phần: docs/giay-phep.md.
 * Đọc lúc build (?raw) nên không phụ thuộc file tĩnh khi chạy.
 */
import ghiCongTho from '~~/public/pokemon/GHI_CONG.txt?raw'

const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Giấy phép & nguồn tài nguyên',
  description: `Giấy phép và ghi công tài nguyên dùng trong ${tenSite}: sprite PMDCollab (CC BY-NC 4.0), tile và âm thanh Kenney / Ninja Adventure (CC0), phông chữ OFL, bộ âm thanh MIDI TimGM6mb (GPL-2), biểu tượng.`,
})

const ghiCong = docGhiCong(ghiCongTho)

const dsLoai = ghiCong.loai.map(l => ({
  ...l,
  ten: loaiTheoDex(l.dex)?.ten ?? tenLoai(l.dex),
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
  { ten: 'Kenney — Interface / Impact / RPG / Digital Audio, Music Jingles', url: 'https://kenney.nl/assets', dung: 'Tiếng hiệu ứng trong game', gp: 'CC0 1.0' },
  { ten: 'game-icons.net (Lorc, Delapouite, Skoll, sbed, darkzaitzev)', url: 'https://game-icons.net', dung: 'Hình trên icon chiêu (game, trang Cẩm nang Chiêu thức) — đã sửa đổi thành huy hiệu pixel', gp: 'CC BY 3.0' },
  { ten: 'Nhạc nền chiptune', url: 'https://creativecommons.org/publicdomain/zero/1.0/', dung: 'Nhạc nền trong game (sáng tác riêng cho dự án, không dùng nhạc Pokémon)', gp: 'CC0 1.0' },
  { ten: 'TimGM6mb (Tim Brechbill, David Bolton)', url: 'https://github.com/musescore/musescore-old', dung: 'Bộ âm thanh MIDI của bản PC Java (tệp riêng, đi kèm nguyên vẹn)', gp: 'GNU GPL v2' },
  { ten: 'Phông Nunito, Baloo 2, Press Start 2P, Noto Sans', url: 'https://fonts.google.com', dung: 'Chữ trên website (tự host) và trong bản PC', gp: 'SIL Open Font License 1.1' },
  { ten: 'Eclipse Temurin JRE', url: 'https://adoptium.net', dung: 'Java đi kèm bản PC Windows (bản Java)', gp: 'GPL v2 + Classpath Exception' },
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
  <UContainer class="max-w-4xl py-12 sm:py-16">
    <nav aria-label="Đường dẫn" class="text-sm text-muted">
      <NuxtLink to="/" class="hover:text-primary hover:underline">Trang chủ</NuxtLink> / <span aria-current="page">Giấy phép & nguồn tài nguyên</span>
    </nav>
    <h1 class="section-title mt-4 text-3xl text-highlighted sm:text-4xl">
      Giấy phép & nguồn tài nguyên
    </h1>

    <ul class="mt-6 space-y-2 text-sm text-muted">
      <li>
        Sprite và chân dung Pokémon:
        <ULink to="https://github.com/PMDCollab/SpriteCollab" target="_blank" rel="noopener" class="text-primary hover:underline">PMDCollab SpriteCollab</ULink>,
        giấy phép <ULink to="https://creativecommons.org/licenses/by-nc/4.0/" target="_blank" rel="noopener" class="text-primary hover:underline">CC BY-NC 4.0</ULink>
        (ghi công, không thương mại) — danh sách tác giả bên dưới. Một số hình gốc do CHUNSOFT vẽ cho Pokémon Mystery Dungeon.
      </li>
      <li>Vì vậy máy chủ này <strong class="text-highlighted">phi thương mại</strong>.</li>
      <li>
        Pokémon và mọi tên, hình liên quan © Nintendo / Creatures Inc. / GAME FREAK inc. / The Pokémon Company. Dự án fan
        <strong class="text-highlighted">không chính thức</strong>, không liên quan và không được các công ty trên bảo trợ.
      </li>
    </ul>

    <h2 class="mt-10 font-display text-xl font-bold text-highlighted">Tài nguyên</h2>
    <div class="bang-cuon mt-3">
      <table class="bang-wiki">
        <thead><tr><th>Tài nguyên</th><th>Dùng cho</th><th>Giấy phép</th></tr></thead>
        <tbody>
          <tr v-for="r in taiNguyen" :key="r.ten">
            <td class="font-semibold text-highlighted">
              <ULink :to="r.url" target="_blank" rel="noopener" class="hover:text-primary hover:underline">{{ r.ten }}</ULink>
            </td>
            <td>{{ r.dung }}</td>
            <td>{{ r.gp }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    <p class="mt-2 text-xs text-dimmed">
      Mỗi bản game có kèm tệp ghi công và giấy phép đầy đủ (GHI_CONG.txt, giấy phép phông chữ và bộ âm thanh MIDI).
    </p>

    <h2 class="mt-10 font-display text-xl font-bold text-highlighted">Người vẽ sprite ({{ nguoiVe.length }})</h2>
    <p class="mt-2 break-words text-xs leading-relaxed text-muted">
      {{ nguoiVe.map(([ten, n]) => `${ten} (${n})`).join(', ') }}.
      Tên dạng "Discord #số" là tài khoản Discord trên máy chủ PMDCollab.
    </p>

    <details class="mt-8 rounded-xl border border-default p-4">
      <summary class="cursor-pointer font-semibold text-highlighted">Ghi công theo từng loài ({{ dsLoai.length }})</summary>
      <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm số dex, tên loài, người vẽ..." aria-label="Tìm trong danh sách ghi công" class="mt-4 w-full sm:w-72" />
      <div class="bang-cuon mt-3">
        <table class="bang-wiki">
          <thead><tr><th>Loài</th><th>Người vẽ</th><th>Giấy phép · ngày</th></tr></thead>
          <tbody>
            <template v-for="l in loaiLoc" :key="l.ma">
              <tr v-for="(d, j) in l.dong" :key="`${l.ma}-${j}`">
                <td v-if="j === 0" :rowspan="l.dong.length" class="font-semibold text-highlighted">
                  #{{ l.ma.replace(/s$/, '') }}<template v-if="!l.ten.startsWith('#')"> · {{ l.ten }}</template><template v-if="l.shiny"> (shiny)</template>
                </td>
                <td>{{ d.nguoiVe }}<span v-if="d.dongTac.length" class="text-xs text-muted"> — {{ d.dongTac.join(', ') }}</span></td>
                <td class="text-xs">{{ tenGiayPhep(d.giayPhep) }} · {{ d.ngay }}</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <p v-if="!loaiLoc.length" class="mt-4 text-center text-muted">Không có loài nào khớp.</p>
    </details>
  </UContainer>
</template>
