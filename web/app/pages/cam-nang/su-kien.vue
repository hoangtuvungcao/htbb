<script setup lang="ts">
/**
 * Sự kiện năm — lễ hội theo NGÀY THẬT (máy chủ game ghi tob_su_kien_lich, có âm lịch; /api/su-kien) + bảng chủ đề
 * 52 tuần Pokémon (data_su_kien_nam / cam-nang.json). Tuần tính như máy chủ: tuần ISO, đổi lúc 04:00 thứ Hai.
 */
const lich = useLichSuKien()
const SK = computed(() => lich.value)
const nam = ref<1 | 2>(1)
const loaiChon = ref<string | null>(null)
const tuanNay = ref(tuanTrongNam(new Date()))
onMounted(() => { tuanNay.value = tuanTrongNam(new Date()) })

const dangDienRa = computed(() => suKienTuTuan(tuanNay.value, 2, SK.value))

// Lịch thật theo ngày (máy chủ game ghi tob_su_kien_lich, Tết / Giỗ Tổ / Vu Lan / Trung Thu theo âm lịch).
const skNam = useSuKienNam(45)
const leHoiNam = computed(() => skNam.ds.value
  .filter(l => l.nhom !== 'Tuần Pokémon' && !SU_KIEN_PHU[l.ma] && msTuChuoi(l.dong) > skNam.bay.value - 86_400_000)
  .slice(0, 24))
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
  dot_kich: 'Boss thế giới xuất hiện thêm 2 lần mỗi ngày (12:30 và 20:30), thưởng theo mức đóng góp.',
  pvp: 'Chốt bậc mùa PvP và phát thưởng.',
  chien_vung: 'Thứ Bảy 20h: ba vùng Kanto/Johto/Hoenn giành cờ.',
  mua: 'Sự kiện theo mùa: Hè, Halloween, Black Friday…',
}
</script>

<template>
  <WikiShell slug="su-kien">
    <p class="van">
      Lịch sự kiện chạy <strong>tự động cả năm</strong>: các dịp lễ (Tết, Giỗ Tổ, Vu Lan, Trung Thu, Halloween, Noel…) tự mở đúng
      ngày thật mỗi năm — kể cả ngày âm lịch — và website tự đăng thông báo. Xen giữa là các <strong>tuần Pokémon</strong> (tuần hệ,
      Ngày cộng đồng, Tuần Shiny, đột kích…), năm thứ hai đổi chủ đề. Phần thưởng chủ yếu là <strong>đồ trang trí không chỉ số</strong>
      và vật phẩm khoá — vui mà không làm lệch sức mạnh.
    </p>

    <WikiSection v-if="skNam.coDuLieu.value" id="lich-that" title="Lịch lễ hội 12 tháng tới">
      <p class="van">
        Mỗi dịp lễ tự mở và đóng đúng ngày (giờ Việt Nam, đổi trạng thái lúc 04:00 — áp dụng ở lần bảo trì sáng sớm).
        Tết Nguyên Đán, Giỗ Tổ Hùng Vương, Vu Lan và Trung Thu tính theo âm lịch. Website đăng tin trước 3 ngày, lúc mở và lúc kết thúc.
      </p>
      <ClientOnly>
        <ol class="mt-3 grid gap-2 md:grid-cols-2">
          <li v-for="l in leHoiNam" :key="`${l.ma}-${l.nam}-${l.lan}`" class="rounded-2xl glass p-3">
            <p class="text-xs font-bold uppercase tracking-wide" :class="msTuChuoi(l.mo) <= skNam.bay.value ? 'text-primary' : 'text-dimmed'">
              <template v-if="msTuChuoi(l.mo) <= skNam.bay.value">Đang diễn ra · còn {{ demNguoc(msTuChuoi(l.dong) - skNam.bay.value) }}</template>
              <template v-else>Bắt đầu sau {{ demNguoc(msTuChuoi(l.mo) - skNam.bay.value) }}</template>
            </p>
            <p class="font-display text-base font-extrabold text-highlighted">
              {{ l.ten }} <span class="text-xs font-normal text-dimmed">· {{ l.ngayLe }}</span>
            </p>
            <p class="text-xs text-muted">{{ khoangThoiGian(l) }}</p>
            <p class="mt-1 text-sm text-muted">{{ l.moTa }}</p>
            <p v-if="l.phanThuong" class="mt-1 text-xs text-toned">
              <UIcon name="i-lucide-gift" class="mr-1 inline size-3.5 align-[-2px]" />{{ l.phanThuong }}
            </p>
          </li>
        </ol>
        <template #fallback>
          <USkeleton class="h-32 w-full" />
        </template>
      </ClientOnly>
    </WikiSection>

    <WikiSection id="dang-dien-ra" title="Tuần này">
      <ClientOnly>
        <ol class="grid gap-3 md:grid-cols-3">
          <li v-for="(s, i) in dangDienRa" :key="s.tuan" class="relative overflow-hidden rounded-2xl glass p-4" :class="i === 0 ? 'ring-gilded' : ''">
            <span class="absolute inset-y-0 left-0 w-1.5" :style="{ background: LOAI_SU_KIEN[s.loai]?.mau }" />
            <p class="text-xs font-bold uppercase tracking-wide" :class="i === 0 ? 'text-primary' : 'text-dimmed'">
              {{ i === 0 ? 'Tuần này' : i === 1 ? 'Tuần sau' : 'Sắp tới' }} · tuần {{ s.tuan }}
            </p>
            <p class="mt-1 font-display text-lg font-extrabold text-highlighted">
              {{ s.ten1 }}<template v-if="s.phan"> ({{ s.phan }})</template>
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
        Tuần Pokémon tính theo tuần ISO, đổi tuần lúc 04:00 thứ Hai (giờ Việt Nam) — trùng với máy chủ. Ô "lễ Việt Nam" trong bảng
        chỉ là vị trí gần đúng; ngày giờ thật của các dịp lễ xem mục Lịch lễ hội ở trên.
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
