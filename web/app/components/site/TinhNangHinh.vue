<script setup lang="ts">
/**
 * Hình minh hoạ THẬT cho một thẻ "Có gì trong game" ở trang chủ — chọn theo liên kết của thẻ (CMS sửa được chữ,
 * hình đi theo trang đích): chân dung PMD của 5 Pokémon khởi đầu, bảng khắc hệ mini, hàng tiến hoá Eevee, boss
 * huyền thoại, icon tiền tệ thật, hoặc ảnh chụp trong game. Không khớp trang nào thì dùng icon cũ của thẻ.
 */
const props = defineProps<{ to: string, icon?: string }>()

const kieu = computed(() => {
  const t = props.to.split('#')[0]!
  if (t === '/gioi-thieu' || t === '/huong-dan') return 'khoi-dau'
  if (t === '/cam-nang/he-khac-che') return 'he'
  if (t === '/cam-nang/tien-hoa') return 'eevee'
  if (t === '/cam-nang/boss-dot-kich') return 'boss'
  if (t === '/cam-nang/pvp') return 'pvp'
  if (t === '/cam-nang/kinh-te') return 'tien'
  if (t === '/cam-nang/tu-dong') return 'anh:tu-dong'
  if (t === '/cam-nang/thu-cuoi-pet') return 'anh:thu-cuoi'
  if (t === '/cam-nang/ban-do') return 'anh:the-gioi'
  if (t === '/pokedex' || t === '/cam-nang/pokedex-shiny') return 'anh:pokedex'
  if (t === '/cam-nang/su-kien') return 'su-kien'
  return ''
})
const maAnh = computed(() => (kieu.value.startsWith('anh:') ? kieu.value.slice(4) : ''))

// Lửa / Nước / Cỏ đánh nhau: hệ số dùng trong game (đã làm dịu), đúng bảng khắc hệ 18×18.
const HE_MINI = [1, 2, 4]
const oHe = HE_MINI.map(a => HE_MINI.map(d => heSoTrongGame(a, [d])))
const BOSS = [145, 384, 382, 383]
// Lễ hội trong lịch năm tự động (LichNam.java): ngày dương hoặc âm lịch.
const LE = [
  { ten: 'Tết Nguyên Đán', ngay: '1/1 âm', mau: 'bg-pokered-500 text-white' },
  { ten: 'Giỗ Tổ', ngay: '10/3 âm', mau: 'bg-pikachu-400 text-navy-950' },
  { ten: 'Vu Lan', ngay: '15/7 âm', mau: 'bg-[#127f77] text-white' },
  { ten: 'Trung Thu', ngay: '15/8 âm', mau: 'bg-pikachu-400 text-navy-950' },
  { ten: 'Halloween', ngay: '31/10', mau: 'bg-[#705898] text-white' },
  { ten: 'Noel', ngay: '25/12', mau: 'bg-[#2f7d32] text-white' },
]
</script>

<template>
  <div class="hinh relative grid h-36 place-items-center overflow-hidden rounded-2xl" :class="maAnh ? '' : 'bg-screen'" aria-hidden="true">
    <!-- 5 Pokémon khởi đầu -->
    <div v-if="kieu === 'khoi-dau'" class="flex items-end justify-center gap-1.5 px-3">
      <img v-for="(d, k) in DEX_KHOI_DAU" :key="d" :src="anhChanDung(d)" width="40" height="40" alt="" loading="lazy" class="pixelated rounded-2xl bg-white shadow-sm ring-2 ring-white" :class="k === 2 ? 'size-20' : 'size-14'">
    </div>
    <!-- bảng khắc hệ mini -->
    <div v-else-if="kieu === 'he'" class="grid grid-cols-[auto_repeat(3,2.6rem)] items-center gap-1 text-[11px] font-extrabold">
      <span class="text-[10px] text-dimmed">đánh ↓</span>
      <PokeHeBadge v-for="d in HE_MINI" :key="`c${d}`" :he="d" ngan size="sm" />
      <template v-for="(a, r) in HE_MINI" :key="`r${a}`">
        <PokeHeBadge :he="a" ngan size="sm" />
        <span
          v-for="(x, c) in oHe[r]"
          :key="c"
          class="grid h-6 place-items-center rounded-md"
          :class="x > 100 ? 'bg-[#127f77] text-white' : x < 100 ? 'bg-pokered-100 text-pokered-700' : 'bg-white/70 text-muted'"
        >×{{ (x / 100).toLocaleString('vi-VN') }}</span>
      </template>
    </div>
    <!-- Eevee + 8 nhánh -->
    <div v-else-if="kieu === 'eevee'" class="flex flex-col items-center gap-2">
      <img :src="anhChanDung(133)" width="40" height="40" alt="" loading="lazy" class="pixelated size-14 rounded-2xl bg-white ring-2 ring-white">
      <div class="grid grid-cols-8 gap-1 px-2">
        <img v-for="d in NHANH_EEVEE" :key="d" :src="anhChanDung(d)" width="40" height="40" alt="" loading="lazy" class="pixelated size-9 rounded-xl bg-white ring-1 ring-white">
      </div>
    </div>
    <!-- boss huyền thoại -->
    <div v-else-if="kieu === 'boss'" class="flex items-center justify-center gap-2 px-3">
      <img v-for="d in BOSS" :key="d" :src="anhChanDung(d)" width="40" height="40" alt="" loading="lazy" class="pixelated size-16 rounded-2xl bg-white ring-2 ring-white">
    </div>
    <!-- PvP -->
    <div v-else-if="kieu === 'pvp'" class="flex items-center justify-center gap-3">
      <img :src="anhChanDung(6)" width="40" height="40" alt="" loading="lazy" class="pixelated size-20 rounded-2xl bg-white ring-2 ring-white">
      <span class="grid size-10 place-items-center rounded-full bg-pokered-500 font-display text-sm font-extrabold text-white shadow">VS</span>
      <img :src="anhChanDung(9)" width="40" height="40" alt="" loading="lazy" class="pixelated size-20 rounded-2xl bg-white ring-2 ring-white">
    </div>
    <!-- lịch lễ hội -->
    <ul v-else-if="kieu === 'su-kien'" class="grid grid-cols-2 gap-1.5 px-3">
      <li v-for="l in LE" :key="l.ten" class="flex items-center gap-1.5 rounded-full bg-white px-1 py-0.5 pr-2.5 text-[11px] font-bold text-navy-900 shadow-sm">
        <span class="rounded-full px-1.5 py-0.5 text-[10px]" :class="l.mau">{{ l.ngay }}</span>{{ l.ten }}
      </li>
    </ul>
    <!-- tiền tệ -->
    <div v-else-if="kieu === 'tien'" class="flex items-center justify-center gap-4">
      <img src="/tien/xu_64.png" width="64" height="64" alt="" loading="lazy" class="size-14">
      <img src="/tien/luong_khoa_64.png" width="64" height="64" alt="" loading="lazy" class="size-14">
      <img src="/tien/luong_64.png" width="64" height="64" alt="" loading="lazy" class="size-14">
    </div>
    <SiteAnhGame v-else-if="maAnh" :ma="maAnh" sizes="(min-width: 1024px) 380px, (min-width: 640px) 50vw, 100vw" class="absolute inset-0 size-full" />
    <UIcon v-else :name="icon || 'i-lucide-sparkles'" class="size-12 text-primary" />
  </div>
</template>
