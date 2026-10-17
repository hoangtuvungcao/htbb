<script setup lang="ts">
/** Nhiệm vụ — mọi danh sách từ nhiem_vu_moi.csv (qua cam-nang.json). Ít tiết lộ cốt truyện: chỉ tên cung/chương. */
const N = CAM_NANG.nhiemVu
const cungNam1 = N.cung.filter(c => c.nam === 1)
const cungNam2 = N.cung.filter(c => c.nam === 2)
const soChuongNam1 = cungNam1.reduce((s, c) => s + c.chuong.length, 0)
const moCung = ref<string | null>(cungNam1[0]?.ma ?? null)

const LOAI = [
  { k: 'huong_dan', ten: 'Hướng dẫn', icon: 'i-lucide-graduation-cap', mo: 'Dẫn bạn làm quen game trong ~30 phút đầu.' },
  { k: 'chinh', ten: 'Chính tuyến', icon: 'i-lucide-book-marked', mo: 'Cốt truyện Kanto → Johto → Hoenn, chia cung và chương.' },
  { k: 'phu', ten: 'Phụ tuyến', icon: 'i-lucide-house', mo: 'Chuỗi 5 bước ở mỗi thị trấn, nhận danh hiệu thị trấn.' },
  { k: 'tuyen', ten: 'Tuyến đường', icon: 'i-lucide-route', mo: 'Trinh sát, dọn đường, tìm kho báu trên các tuyến.' },
  { k: 'ngay', ten: 'Hằng ngày', icon: 'i-lucide-sun', mo: `Tối đa ${N.gioiHanNgay} nhiệm vụ mỗi ngày, theo bậc cấp.` },
  { k: 'tuan', ten: 'Hằng tuần', icon: 'i-lucide-calendar-range', mo: `Tối đa ${N.gioiHanTuan} nhiệm vụ mỗi tuần, thưởng lớn.` },
  { k: 'pokedex', ten: 'Nghiên cứu Pokédex', icon: 'i-lucide-book-open', mo: 'Gặp và hạ từng loài.' },
  { k: 'doi', ten: 'Đội', icon: 'i-lucide-users', mo: 'Cả đội cùng tiến độ, mỗi tuần.' },
  { k: 'su_pham', ten: 'Sư phạm', icon: 'i-lucide-hand-heart', mo: 'Thầy dẫn trò, cả hai nhận thưởng.' },
  { k: 'hang_bi_an', ten: 'Hầm ngục', icon: 'i-lucide-castle', mo: 'Các chặng Hầm Ngục Bí Ẩn.' },
  { k: 'quay_lai', ten: 'Quay lại', icon: 'i-lucide-undo-2', mo: 'Chỉ hiện khi bạn vắng từ 14 ngày.' },
  { k: 'thanh_tuu', ten: 'Thành tựu', icon: 'i-lucide-award', mo: 'Danh hiệu, không cộng chỉ số.' },
]
const TEN_KIEU_TT: Record<string, string> = { cap: 'Đạt cấp', pokedex: 'Pokédex', chuong: 'Chương chính tuyến', nang_cap: 'Nâng cấp +', phu: 'Nhiệm vụ phụ', ngay: 'Ngày đăng nhập', pvp: 'Thắng PvP', boss: 'Hạ boss', doi: 'Đội', bo: 'Bộ trang bị', su_kien: 'Sự kiện' }
</script>

<template>
  <WikiShell slug="nhiem-vu">
    <WikiSection id="tong-quan" title="Tổng quan">
      <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="l in LOAI" :key="l.k" class="flex gap-3 rounded-2xl glass p-4">
          <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-pokeblue-500 text-white"><UIcon :name="l.icon" class="size-5" /></span>
          <span>
            <strong class="font-display text-highlighted">{{ l.ten }}</strong>
            <span class="ml-1 text-xs font-bold text-dimmed">{{ N.demLoai[l.k] ?? 0 }}</span>
            <span class="block text-sm text-muted">{{ l.mo }}</span>
          </span>
        </li>
      </ul>
      <h3 class="mt-8 font-display text-xl font-bold text-highlighted">
        Hướng dẫn tân thủ ({{ N.huongDan.length }} bước)
      </h3>
      <ol class="mt-3 grid gap-2 sm:grid-cols-2">
        <li v-for="(q, i) in N.huongDan" :key="i" class="flex items-center gap-3 rounded-xl bg-elevated/60 px-3 py-2 text-sm">
          <span class="grid size-7 shrink-0 place-items-center rounded-full bg-pokered-500 font-pixel text-[9px] text-white">{{ i + 1 }}</span>
          <span><strong class="text-highlighted">{{ q.ten }}</strong> <span class="text-muted">— {{ q.npc }}<template v-if="q.mucTieu">, {{ q.soLuong }} {{ q.mucTieu }}</template></span></span>
        </li>
      </ol>
    </WikiSection>

    <WikiSection id="chinh-tuyen" title="Chính tuyến" :lead="`Năm đầu có ${cungNam1.length} cung × 4 chương = ${soChuongNam1} chương, đi từ Pallet (Kanto) tới đỉnh Núi Silver (Johto). Mỗi chương 4 bước và kết thúc bằng một thử thách.`">
      <WikiCallout kieu="vi-sao" title="Vé chương">
        Mở một chương cần <strong>1 vé chương</strong>. Mỗi tuần nhận {{ N.veChuongTuan }} vé, tích tối đa {{ N.veChuongToiDa }} (khởi đầu có sẵn {{ N.veChuongBanDau }}).
        Nhờ vậy cốt truyện trải đều cả năm: người chơi bận rộn không bị bỏ lại, người chơi chăm vẫn còn phụ tuyến, Pokédex, PvP để làm.
      </WikiCallout>
      <div class="space-y-2">
        <div v-for="c in cungNam1" :key="c.ma" class="overflow-hidden rounded-2xl glass">
          <button type="button" class="flex w-full items-center gap-3 px-4 py-3 text-left" :aria-expanded="moCung === c.ma" @click="moCung = moCung === c.ma ? null : c.ma">
            <span class="grid size-9 shrink-0 place-items-center rounded-lg bg-navy-800 font-pixel text-[10px] text-pikachu-300">{{ c.ma }}</span>
            <span class="min-w-0 flex-1">
              <span class="block font-display font-bold text-highlighted">{{ c.ten }}</span>
              <span class="text-xs text-muted">{{ c.vung }} · cấp {{ c.capTu }}–{{ c.capDen }}</span>
            </span>
            <UIcon name="i-lucide-chevron-down" class="size-5 text-dimmed transition" :class="moCung === c.ma ? 'rotate-180' : ''" />
          </button>
          <ol v-if="moCung === c.ma" class="grid gap-2 border-t border-default px-4 py-3 sm:grid-cols-2">
            <li v-for="ch in c.chuong" :key="ch.so" class="text-sm">
              <span class="text-dimmed">Chương {{ ch.so }}:</span> <strong class="text-highlighted">{{ ch.ten }}</strong>
              <span class="block text-xs text-muted">{{ ch.diaDanh }} · từ cấp {{ ch.cap }}</span>
            </li>
          </ol>
        </div>
      </div>
      <h3 class="mt-8 font-display text-xl font-bold text-highlighted">
        Năm 2: Hoenn & liên vùng (dàn ý)
      </h3>
      <p class="mt-1 text-sm text-muted">
        Mở khi trần cấp vượt 91. Tên cung dự kiến:
      </p>
      <ul class="mt-3 flex flex-wrap gap-2">
        <li v-for="c in cungNam2" :key="c.ma">
          <UBadge color="neutral" variant="outline">
            {{ c.ma }} · {{ c.ten }}
          </UBadge>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="phu-tuyen" title="Phụ tuyến & tuyến đường">
      <p class="van">
        Mỗi thị trấn có chuỗi <strong>5 bước</strong>; xong bước cuối nhận danh hiệu thị trấn. Các tuyến đường có chuỗi 3 bước (trinh sát → dọn đường → kho báu).
      </p>
      <div class="mt-4 grid gap-4 lg:grid-cols-2">
        <div class="bang-cuon">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Thị trấn</th><th>Vùng</th><th class="so">
                  Từ cấp
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in N.phu" :key="p.ten">
                <td class="font-semibold text-highlighted">
                  {{ p.ten.replace(/^Thị trấn\s+/, '') }}
                </td><td>{{ p.vung }}</td><td class="so">
                  {{ p.capTu }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="bang-cuon self-start">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Tuyến đường</th><th>Vùng</th><th class="so">
                  Từ cấp
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in N.tuyen" :key="p.ten">
                <td class="font-semibold text-highlighted">
                  {{ p.ten }}
                </td><td>{{ p.vung }}</td><td class="so">
                  {{ p.capTu }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </WikiSection>

    <WikiSection id="ngay-tuan" title="Ngày & tuần">
      <div class="grid gap-4 md:grid-cols-2">
        <div class="rounded-2xl glass p-4">
          <p class="font-display font-bold text-highlighted">
            Bảng nhiệm vụ ngày — tối đa {{ N.gioiHanNgay }}/ngày
          </p>
          <p class="mt-1 text-sm text-muted">
            Nhận ở Blaziken (Trung tâm Pokémon). Nội dung đổi theo bậc cấp của bạn. Ví dụ:
          </p>
          <p class="mt-2 flex flex-wrap gap-1.5">
            <UBadge v-for="t in N.ngay.slice(0, 10)" :key="t" color="neutral" variant="soft" size="sm">
              {{ t }}
            </UBadge>
          </p>
        </div>
        <div class="rounded-2xl glass p-4">
          <p class="font-display font-bold text-highlighted">
            Bảng nhiệm vụ tuần — tối đa {{ N.gioiHanTuan }}/tuần
          </p>
          <p class="mt-1 text-sm text-muted">
            Làm mới thứ Hai 05:00. Thưởng tương đương ~1 giờ EXP và nhiều lượng khoá. Ví dụ:
          </p>
          <p class="mt-2 flex flex-wrap gap-1.5">
            <UBadge v-for="t in N.tuan.slice(0, 8)" :key="t" color="neutral" variant="soft" size="sm">
              {{ t }}
            </UBadge>
          </p>
        </div>
      </div>
      <WikiCallout kieu="meo">
        Dưới cấp {{ CAM_NANG.capDo.tanThu.duoiCap }}, thưởng nhiệm vụ được ×{{ (CAM_NANG.capDo.tanThu.heSoPhanTram / 100).toLocaleString('vi-VN') }} — làm hướng dẫn và chính tuyến trước để tiến hoá lần đầu thật nhanh.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="thanh-tuu" title="Thành tựu" :lead="`${N.thanhTuu.length} thành tựu cho danh hiệu và lượng khoá/lượng. Danh hiệu chỉ để khoe, không cộng chỉ số.`">
      <ul class="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="t in N.thanhTuu" :key="t.ten" class="flex items-center gap-3 rounded-xl bg-elevated/60 px-3 py-2">
          <UIcon name="i-lucide-award" class="size-5 shrink-0 text-pikachu-600 dark:text-pikachu-300" />
          <span class="min-w-0 text-sm">
            <strong class="block truncate text-highlighted">{{ t.ten }}</strong>
            <span class="text-xs text-muted">{{ TEN_KIEU_TT[t.kieu] ?? t.kieu }}: {{ t.soLuong }}</span>
          </span>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
