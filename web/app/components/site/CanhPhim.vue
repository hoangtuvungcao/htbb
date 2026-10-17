<script setup lang="ts">
/**
 * Cảnh "trailer" ở trang chủ: đồng cỏ cuộn ngang, 5 Pokémon khởi đầu chạy sang phải (sprite PMD
 * thật, hướng RIGHT), thỉnh thoảng một con dừng lại tung chiêu vào Pokémon hoang dã; hộp thoại
 * kiểu game đổi câu theo nhịp. Toàn CSS + một bộ đếm nhỏ; "giảm chuyển động" thì đứng yên.
 */
const DOAN = [
  { dex: 4, delay: 0 },
  { dex: 1, delay: 0.5 },
  { dex: 172, delay: 0.2 },
  { dex: 7, delay: 0.8 },
  { dex: 133, delay: 0.35 },
]
const HOANG_DA = [10, 27, 19, 194, 43]
const CAU = [
  'Một buổi sáng ở Pallet… bạn thức dậy trong hình hài một Pokémon!',
  'Cùng bạn bè lập đội, băng qua Rừng Viridian.',
  'Đánh đúng hệ: Nước khắc Lửa, Điện khắc Nước, Cỏ khắc Đất!',
  'Charmander đã tiến hoá thành Charmeleon!',
  'Thứ Bảy 20h: Thế chiến vùng — Kanto, Johto, Hoenn giành cờ.',
  'Huyền thoại đang thức giấc trên đỉnh Núi Silver…',
]
/** Hộp cố định theo khung đi (walkR) để đổi sang khung đánh không làm xô hàng. */
function hop(dex: number) {
  const a = ANIM[String(dex)]!.walkR!
  return { width: `calc(${a.w}px * var(--s))`, height: `calc(${a.h}px * var(--s))` }
}
const i = ref(0)
const danh = ref(0)
let t: ReturnType<typeof setInterval> | undefined
onMounted(() => {
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  t = setInterval(() => {
    i.value = (i.value + 1) % CAU.length
    danh.value = (danh.value + 1) % DOAN.length
  }, 3600)
})
onBeforeUnmount(() => clearInterval(t))
</script>

<template>
  <div class="canh relative isolate h-64 overflow-hidden rounded-3xl ring-gilded sm:h-72" role="img" aria-label="Năm Pokémon khởi đầu chạy qua đồng cỏ">
    <!-- trời + đồi -->
    <div class="absolute inset-0 -z-10 bg-[linear-gradient(180deg,var(--poke-sky-top),var(--poke-sky-bottom)_70%)]" />
    <div class="doi absolute inset-x-0 bottom-16 -z-10 h-24 opacity-90" />
    <div class="co absolute inset-x-0 bottom-0 -z-10 h-20 bg-grass-400 dark:bg-grass-800" />
    <div class="grass-edge !bottom-[78px] !h-6 -z-10 !bg-grass-400 dark:!bg-grass-800" />
    <!-- Pokémon hoang dã đứng trong cỏ -->
 <span v-for="(d, k) in HOANG_DA" :key="d" class="hoang absolute bottom-[4.6rem]" :class="k % 2 ? 'max-sm:hidden' : ''" :style="{ left: `${12 + k * 19}%`, animationDelay: `${-k * 2.4}s` }" aria-hidden="true">
      <PokeSprite :dex="d" :scale="2" :delay="k * 0.4" />
    </span>
    <!-- đoàn khởi đầu chạy sang phải -->
    <div class="doan absolute bottom-1 left-0 flex items-end gap-1 [--s:2] sm:gap-3 sm:[--s:3]" aria-hidden="true">
      <span v-for="(p, k) in DOAN" :key="p.dex" class="relative" :style="hop(p.dex)">
        <span class="absolute left-1/2 h-2 w-[60%] -translate-x-1/2 -translate-y-1/2 rounded-[50%] bg-black/20" :style="{ top: `calc(50% + ${ANIM[String(p.dex)]!.walkR!.chanY}px * var(--s))` }" />
        <PokeSprite :dex="p.dex" :dong="danh === k ? 'danhR' : 'walkR'" :delay="p.delay" class="!absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2" />
      </span>
    </div>
    <!-- hộp thoại -->
    <div class="absolute inset-x-3 top-3 sm:inset-x-auto sm:left-6 sm:top-6 sm:max-w-md">
      <div class="dialog-box !py-3 text-sm sm:text-base" aria-live="polite">
        <p :key="i" class="cau">
          {{ CAU[i] }}
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.doi {
  background:
    radial-gradient(60% 120% at 20% 100%, var(--color-grass-300) 60%, transparent 61%),
    radial-gradient(50% 110% at 70% 100%, var(--color-grass-300) 60%, transparent 61%);
  background-size: 900px 100%;
  animation: cuon 40s linear infinite;
}
.dark .doi {
  background:
    radial-gradient(60% 120% at 20% 100%, var(--color-grass-900) 60%, transparent 61%),
    radial-gradient(50% 110% at 70% 100%, var(--color-grass-900) 60%, transparent 61%);
  background-size: 900px 100%;
}
.co {
  background-image: repeating-linear-gradient(90deg, transparent 0 22px, rgb(0 0 0 / .05) 22px 44px);
  animation: cuon-co 6s linear infinite;
}
.doan { animation: chay 22s linear infinite; }
.hoang { animation: lui 14s linear infinite; }
@keyframes cuon { to { background-position: -900px 0; } }
@keyframes cuon-co { to { background-position: -88px 0; } }
@keyframes chay {
  from { transform: translateX(-60%); }
  to { transform: translateX(110vw); }
}
@keyframes lui {
  from { transform: translateX(0); }
  50% { transform: translateX(-6vw); }
  to { transform: translateX(0); }
}
.cau { animation: hien .35s ease-out; }
@keyframes hien { from { opacity: .2; transform: translateY(4px); } }
@media (prefers-reduced-motion: reduce) {
  .doan { animation: none; transform: translateX(30%); }
}
</style>
