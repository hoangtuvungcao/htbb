<script setup lang="ts">
/** Sự kiện năm — lịch 52 tuần từ su_kien_nam.csv (qua cam-nang.json). */
const lich = useLichSuKien()
const SK = computed(() => lich.value)
const nam = ref<1 | 2>(1)
const loaiChon = ref<string | null>(null)
const tuanNay = ref(tuanTrongNam(new Date()))
onMounted(() => { tuanNay.value = tuanTrongNam(new Date()) })

const dangDienRa = computed(() => suKienTuTuan(tuanNay.value, 2, SK.value))
const tenSk = (s: SuKienTuan) => (nam.value === 1 ? s.ten1 : s.ten2)
const thang = computed(() => Array.from({ length: 12 }, (_, i) => ({
  so: i + 1,
  tuan: SK.value.filter(s => s.thang === i + 1),
})))
const dem = computed(() => Object.fromEntries(Object.keys(LOAI_SU_KIEN).map(k => [k, SK.value.filter(s => s.loai === k).length])))
const moTaLoai: Record<string, string> = {
  le_vn: 'Tết, Trung Thu, 8/3, 20/10, Giáng sinh… phiên bản Pokémon của các dịp lễ Việt Nam.',
  tuan_he: 'Quái một hệ xuất hiện ×2 và chiêu hệ đó +10% — cơ hội săn Pokédex và đá.',
  cong_dong: '3 giờ vàng: một dòng khởi đầu xuất hiện dày đặc, tỉ lệ shiny ×5.',
  pokemon: 'Tuần Shiny, Lễ hội Pokédex, Bùng phát quần thể.',
  dot_kich: 'Boss huyền thoại xuất hiện 2 lần/ngày, cần đội từ 5 người.',
  pvp: 'Chốt bậc mùa PvP và phát thưởng.',
  chien_vung: 'Thứ Bảy 20h: ba vùng Kanto/Johto/Hoenn giành cờ.',
  mua: 'Sự kiện theo mùa: Hè, Halloween, Black Friday…',
}
</script>

<template>
  <WikiShell slug="su-kien">
    <p class="van">
      Mỗi tuần trong năm đều có một sự kiện. Lịch lặp lại hằng năm, năm thứ hai đổi chủ đề (ví dụ Ngày cộng đồng Charmander → Squirtle).
      Phần thưởng sự kiện chủ yếu là <strong>đồ trang trí không chỉ số</strong> và vật phẩm khoá — vui mà không làm lệch sức mạnh.
    </p>

    <WikiSection id="dang-dien-ra" title="Đang diễn ra">
      <ClientOnly>
        <ol class="grid gap-3 md:grid-cols-3">
          <li v-for="(s, i) in dangDienRa" :key="s.tuan" class="relative overflow-hidden rounded-2xl glass p-4" :class="i === 0 ? 'ring-gilded' : ''">
            <span class="absolute inset-y-0 left-0 w-1.5" :style="{ background: LOAI_SU_KIEN[s.loai]?.mau }" />
            <p class="text-xs font-bold uppercase tracking-wide" :class="i === 0 ? 'text-primary' : 'text-dimmed'">
              {{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }} · tuần {{ s.tuan }}
            </p>
            <p class="mt-1 font-display text-lg font-extrabold text-highlighted">
              {{ s.ten1 }}
            </p>
            <p class="mt-1 text-sm text-muted">
              {{ s.coChe }}
            </p>
            <p v-if="s.phanThuong" class="mt-2 text-xs text-toned">
              <UIcon name="i-lucide-gift" class="mr-1 inline size-3.5 align-[-2px]" />{{ s.phanThuong }}
            </p>
          </li>
        </ol>
        <template #fallback>
          <USkeleton class="h-32 w-full" />
        </template>
      </ClientOnly>
      <p class="mt-2 text-xs text-dimmed">
        Tuần tính theo lịch dương (tuần 1 = 1–7/1). Tết Nguyên Đán đi theo âm lịch nên có thể lệch vài tuần; ngày giờ chính xác xem thông báo trong game.
      </p>
    </WikiSection>

    <WikiSection id="lich" title="Lịch cả năm">
      <div class="flex flex-wrap items-center gap-2">
        <UButton :color="nam === 1 ? 'primary' : 'neutral'" :variant="nam === 1 ? 'solid' : 'outline'" size="sm" @click="nam = 1">
          Năm 1
        </UButton>
        <UButton :color="nam === 2 ? 'primary' : 'neutral'" :variant="nam === 2 ? 'solid' : 'outline'" size="sm" @click="nam = 2">
          Năm 2
        </UButton>
        <span class="mx-2 h-5 w-px bg-[var(--ui-border)]" />
        <button
          v-for="(v, k) in LOAI_SU_KIEN"
          :key="k"
          type="button"
          class="flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold transition"
          :class="loaiChon === k ? 'border-[var(--poke-outline)] bg-elevated text-highlighted' : 'border-default text-toned hover:bg-elevated'"
          :aria-pressed="loaiChon === k"
          @click="loaiChon = loaiChon === k ? null : k"
        >
          <span class="size-2.5 rounded-full" :style="{ background: v.mau }" /> {{ v.ten }} <span class="text-dimmed">{{ dem[k] }}</span>
        </button>
      </div>
      <div class="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <section v-for="t in thang" :key="t.so" class="rounded-2xl glass p-3" :aria-label="`Tháng ${t.so}`">
          <h3 class="px-1 font-display text-sm font-extrabold uppercase tracking-wider text-dimmed">
            Tháng {{ t.so }}
          </h3>
          <ul class="mt-2 space-y-1.5">
            <li
              v-for="s in t.tuan"
              :key="s.tuan"
              class="rounded-xl px-2 py-1.5 transition"
              :class="[
                loaiChon && loaiChon !== s.loai ? 'opacity-35' : '',
                s.tuan === tuanNay ? 'bg-pikachu-100 ring-2 ring-pikachu-400 dark:bg-pikachu-950/40' : 'hover:bg-elevated',
              ]"
            >
              <details class="group">
                <summary class="flex cursor-pointer list-none items-start gap-2 text-sm">
                  <span class="mt-1 size-2.5 shrink-0 rounded-full" :style="{ background: LOAI_SU_KIEN[s.loai]?.mau }" />
                  <span class="min-w-0 flex-1">
                    <span class="font-semibold text-highlighted">{{ tenSk(s) }}</span>
                    <span v-if="s.phan" class="text-dimmed"> ({{ s.phan }})</span>
                    <span class="block text-[11px] text-dimmed">Tuần {{ s.tuan }} · {{ LOAI_SU_KIEN[s.loai]?.ten }}{{ s.tuan === tuanNay ? ' · đang diễn ra' : '' }}</span>
                  </span>
                  <UIcon name="i-lucide-chevron-down" class="mt-1 size-4 text-dimmed transition group-open:rotate-180" />
                </summary>
                <p class="mt-1.5 pl-4.5 text-xs text-muted">
                  {{ s.coChe }}
                </p>
                <p v-if="s.phanThuong" class="mt-1 pl-4.5 text-xs text-toned">
                  🎁 {{ s.phanThuong }}
                </p>
              </details>
            </li>
          </ul>
        </section>
      </div>
    </WikiSection>

    <WikiSection id="loai" title="Các loại sự kiện">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li v-for="(v, k) in LOAI_SU_KIEN" :key="k" class="flex gap-3 rounded-2xl glass p-4">
          <span class="grid size-10 shrink-0 place-items-center rounded-xl text-white" :style="{ background: v.mau }">
            <UIcon :name="v.icon" class="size-5" />
          </span>
          <span>
            <strong class="font-display text-highlighted">{{ v.ten }}</strong> <span class="text-xs text-dimmed">· {{ dem[k] }} tuần/năm</span>
            <span class="block text-sm text-muted">{{ moTaLoai[k] }}</span>
          </span>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
