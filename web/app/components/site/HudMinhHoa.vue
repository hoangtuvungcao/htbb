<script setup lang="ts">
/**
 * Ảnh minh hoạ giao diện trong game (HUD) có đánh số — vẽ bằng HTML/CSS + sprite thật,
 * dùng tạm thay ảnh chụp màn hình. Muốn dùng ảnh chụp thật: đặt `anh` (đường dẫn trong public/),
 * các chấm số vẫn đặt theo % nên chỉnh `CHAM` cho khớp.
 */
defineProps<{ anh?: string }>()
interface ChamHud { so: number, ten: string, mo: string, x: number, y: number }
const CHAM: ChamHud[] = [
  { so: 1, ten: 'Chân dung, HP & năng lượng', mo: 'Thanh đỏ là HP, thanh xanh là năng lượng (MP) để dùng chiêu. Hết HP là gục ngã.', x: 17, y: 5 },
  { so: 2, ten: 'Cấp & thanh EXP', mo: 'Đầy thanh là lên cấp: +5 điểm tiềm năng, +1 điểm kỹ năng.', x: 42, y: 5 },
  { so: 3, ten: 'Bản đồ nhỏ', mo: 'Vị trí của bạn, NPC và cổng sang bản đồ khác.', x: 80, y: 5 },
  { so: 4, ten: 'Nhiệm vụ đang theo dõi', mo: 'Mục tiêu hiện tại và tiến độ. Chạm để tự tìm đường.', x: 72, y: 36 },
  { so: 5, ten: 'Mục tiêu', mo: 'Tên, cấp, hệ và HP của Pokémon bạn đang nhắm. Phím chọn phải để đổi mục tiêu.', x: 57, y: 27 },
  { so: 6, ten: 'Thanh chiêu', mo: 'Phím tắt 1 · 3 · 5 · 7 · 9. Phím 0 đổi bảng chiêu. 7/9 thường gán thuốc.', x: 60, y: 91 },
  { so: 7, ten: 'Khung chat', mo: 'Kênh khu vực, đội, thế giới. Phím # để chat.', x: 34, y: 70 },
  { so: 8, ten: 'Menu', mo: 'Hành trang, Bản thân (cộng điểm), chiêu, Pokédex, đội, cài đặt.', x: 7, y: 90 },
]
</script>

<template>
  <figure>
    <div class="relative aspect-[16/9] w-full overflow-hidden rounded-2xl border-2 border-[color-mix(in_oklab,var(--poke-outline)_70%,transparent)] bg-[linear-gradient(180deg,#9fd77a,#6fbf4a)] shadow-[0_4px_0_color-mix(in_oklab,var(--poke-outline)_60%,transparent)]">
      <img v-if="anh" :src="anh" alt="Ảnh chụp giao diện trong game" class="absolute inset-0 size-full object-cover">
      <template v-else>
        <!-- nền "bản đồ" -->
        <div class="absolute inset-0 opacity-30 [background:repeating-linear-gradient(45deg,transparent_0_18px,rgb(0_0_0/.06)_18px_36px)]" aria-hidden="true" />
        <div class="absolute left-[8%] top-[48%] h-[16%] w-[60%] rounded-full bg-[#d9a066]/80" aria-hidden="true" />
        <div class="absolute right-[18%] top-[56%] h-[26%] w-[20%] rounded-[40%] bg-[#5fa8e8]/80" aria-hidden="true" />
        <span class="absolute left-[30%] top-[38%] -translate-x-1/2" aria-hidden="true"><PokeSprite :dex="4" :scale="3" /></span>
        <span class="absolute left-[58%] top-[36%] -translate-x-1/2" aria-hidden="true"><PokeSprite :dex="10" :scale="3" /></span>
        <!-- HUD -->
        <div class="absolute left-[3%] top-[4%] flex items-center gap-2 rounded-xl bg-navy-950/75 p-1.5 pr-3" aria-hidden="true">
          <PokeAvatar :dex="4" class="size-8 sm:size-10" />
          <div class="w-20 space-y-1 sm:w-32">
            <div class="h-1.5 rounded-full bg-white/20 sm:h-2"><div class="h-full w-4/5 rounded-full bg-pokered-500" /></div>
            <div class="h-1.5 rounded-full bg-white/20 sm:h-2"><div class="h-full w-3/5 rounded-full bg-pokeblue-400" /></div>
          </div>
        </div>
        <div class="absolute left-[28%] top-[5%] hidden items-center gap-2 rounded-full bg-navy-950/75 px-2 py-1 font-pixel text-[9px] text-pikachu-300 sm:flex" aria-hidden="true">
          LV 12 <span class="h-1.5 w-24 rounded-full bg-white/20"><span class="block h-full w-2/3 rounded-full bg-pikachu-400" /></span>
        </div>
        <div class="absolute right-[3%] top-[4%] size-[16%] rounded-lg border-2 border-white/70 bg-grass-700/80" aria-hidden="true">
          <span class="absolute left-1/2 top-1/2 size-1.5 rounded-full bg-pokered-500" />
        </div>
        <div class="absolute right-[3%] top-[34%] hidden w-[24%] rounded-lg bg-navy-950/70 p-2 text-[10px] leading-tight text-white sm:block" aria-hidden="true">
          <b class="text-pikachu-300">Nhiệm vụ</b><br>Bài học tấn công: 6/10 Caterpie
        </div>
        <div class="absolute left-[50%] top-[25%] rounded-full bg-navy-950/75 px-2 py-0.5 text-[10px] text-white" aria-hidden="true">
          Caterpie · LV 2
        </div>
        <div class="absolute left-[3%] top-[68%] hidden w-[30%] rounded-lg bg-navy-950/60 p-2 text-[10px] leading-tight text-white/90 sm:block" aria-hidden="true">
          [Khu vực] Pikachu: ai đi Rừng Viridian không?
        </div>
        <div class="absolute bottom-[4%] left-1/2 flex -translate-x-1/2 gap-1.5" aria-hidden="true">
          <span v-for="k in [1, 3, 5, 7, 9]" :key="k" class="grid size-7 place-items-center rounded-lg border-2 border-white/80 bg-navy-900/80 font-pixel text-[8px] text-white sm:size-9">{{ k }}</span>
        </div>
        <div class="absolute bottom-[4%] left-[3%] grid size-8 place-items-center rounded-lg bg-pikachu-400 text-navy-950 sm:size-9" aria-hidden="true">
          <UIcon name="i-lucide-menu" class="size-4" />
        </div>
      </template>
      <!-- Chấm số -->
      <span
        v-for="c in CHAM"
        :key="c.so"
        class="absolute z-10 grid size-6 -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full border-2 border-white bg-pokered-500 font-display text-xs font-extrabold text-white shadow-md"
        :style="{ left: `${c.x}%`, top: `${c.y}%` }"
        aria-hidden="true"
      >{{ c.so }}</span>
    </div>
    <figcaption class="mt-2 text-xs text-dimmed">
      {{ anh ? 'Ảnh chụp giao diện trong game.' : 'Ảnh minh hoạ bố cục giao diện (vị trí thực tế có thể khác đôi chút giữa PC, Android và Java). Ảnh chụp thật sẽ được cập nhật.' }}
    </figcaption>
    <ol class="mt-4 grid gap-2 sm:grid-cols-2">
      <li v-for="c in CHAM" :key="c.so" class="flex gap-3 rounded-xl bg-elevated/60 p-3 text-sm">
        <span class="grid size-6 shrink-0 place-items-center rounded-full bg-pokered-500 font-display text-xs font-extrabold text-white">{{ c.so }}</span>
        <span><strong class="text-highlighted">{{ c.ten }}</strong><span class="block text-muted">{{ c.mo }}</span></span>
      </li>
    </ol>
  </figure>
</template>
