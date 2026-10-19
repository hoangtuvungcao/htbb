<script setup lang="ts">
/**
 * Nhiệm vụ — danh sách từ nhiem_vu_moi.csv (cam-nang.json); tóm tắt chương (một câu, không lộ kết)
 * và số bước từ tools/luu_tru_sinh_du_lieu/du_lieu/web/nhiem_vu_tom_tat.json (cam-nang-noi-dung.json).
 */
const N = CAM_NANG.nhiemVu
const cungNam1 = N.cung.filter(c => c.nam === 1)
const cungNam2 = N.cung.filter(c => c.nam === 2)
const soChuongNam1 = cungNam1.reduce((s, c) => s + c.chuong.length, 0)
const moCung = ref<string | null>(cungNam1[0]?.ma ?? null)
const TT = CAM_NANG_ND.cotTruyen
const buocPhuDs = N.phu.map(p => p.soBuoc)
const buocPhu = Math.min(...buocPhuDs) === Math.max(...buocPhuDs) ? String(buocPhuDs[0]) : `${Math.min(...buocPhuDs)}–${Math.max(...buocPhuDs)}`
const chuongTT = new Map(TT.chuong.map(c => [c.so, c]))
const soBuoc = TT.chuong.filter(c => c.nam === 1).map(c => c.buoc.length)
const buocTu = Math.min(...soBuoc)
const buocDen = Math.max(...soBuoc)
const chuongMoiCung = Math.max(...cungNam1.map(c => c.chuong.length))
const nam = [{ so: 1, ten: 'Năm 1: Kanto → Johto', ds: cungNam1 }, { so: 2, ten: 'Năm 2: Hoenn & liên vùng', ds: cungNam2 }]

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
  { k: 'hang_bi_an', ten: 'Hầm ngục', icon: 'i-lucide-castle', mo: 'Các chặng của Hầm ngục bí ẩn.' },
  { k: 'quay_lai', ten: 'Quay lại', icon: 'i-lucide-undo-2', mo: 'Chỉ hiện khi bạn vắng từ 14 ngày.' },
  { k: 'thanh_tuu', ten: 'Thành tựu', icon: 'i-lucide-award', mo: 'Danh hiệu, không cộng chỉ số.' },
]
const TEN_KIEU_TT: Record<string, string> = { cap: 'Đạt cấp', pokedex: 'Pokédex', chuong: 'Chương chính tuyến', nang_cap: 'Nâng cấp lên +', phu: 'Nhiệm vụ phụ', ngay: 'Ngày đăng nhập', pvp: 'Thắng PvP', boss: 'Hạ boss', doi: 'Đội', bo: 'Bộ trang bị', su_kien: 'Sự kiện', gym: 'Huy hiệu Gym', cau_ca: 'Câu cá', dao_dat: 'Khảo cổ', nau_an: 'Nấu ăn', ap_trung: 'Ấp trứng', alpha: 'Hạ quái alpha', kham_pha: 'Khám phá', su_pham: 'Sư phạm', che_tao: 'Chế tạo', shiny: 'Gặp Shiny', ham_nguc: 'Hầm ngục', cau_do: 'Câu đố', huyen_thoai: 'Huyền thoại', an: 'Bí ẩn', ngay_xong: 'Nhiệm vụ ngày', tuan_xong: 'Nhiệm vụ tuần', lua_chon: 'Lựa chọn' }
</script>

<template>
  <WikiShell slug="nhiem-vu">
    <WikiSection id="tong-quan" title="Tổng quan">
      <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="l in LOAI.filter(x => (N.demLoai[x.k] ?? 0) > 0)" :key="l.k" class="flex gap-3 rounded-2xl glass p-4">
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
          <span class="grid size-7 shrink-0 place-items-center rounded-full bg-pokered-600 font-pixel text-[9px] text-white">{{ i + 1 }}</span>
          <span><strong class="text-highlighted">{{ q.ten }}</strong> <span class="text-muted">— {{ q.npc }}<template v-if="q.yeuCau">: {{ q.yeuCau }}</template></span></span>
        </li>
      </ol>
    </WikiSection>

    <WikiSection id="chinh-tuyen" title="Chính tuyến" :lead="`Năm đầu có ${cungNam1.length} cung × ${chuongMoiCung} chương = ${soChuongNam1} chương, đi từ Pallet (Kanto) tới đỉnh Núi Silver (Johto). Mỗi chương ${buocTu === buocDen ? buocTu : `${buocTu}–${buocDen}`} bước và kết thúc bằng một thử thách.`">
      <WikiCallout kieu="vi-sao" title="Vé chương">
        Mở một chương cần <strong>1 vé chương</strong>. Mỗi tuần nhận {{ N.veChuongTuan }} vé, tích tối đa {{ N.veChuongToiDa }} (khởi đầu có sẵn {{ N.veChuongBanDau }}).
        Nhờ vậy cốt truyện trải đều cả năm: người chơi bận rộn không bị bỏ lại, người chơi chăm vẫn còn phụ tuyến, Pokédex, PvP để làm.
      </WikiCallout>
      <div v-for="n in nam" :key="n.so" class="mt-6 first:mt-0">
        <h3 class="font-display text-xl font-bold text-highlighted">
          {{ n.ten }}
        </h3>
        <p v-if="n.so === 2" class="mt-1 text-sm text-muted">
          Mở sau khi xong cốt truyện năm 1 (chương 52) — lên tàu đi Hoenn cần cấp {{ POKEDEX.hoenn.capVao }}+, các chương năm 2 cần cấp 91 trở lên.
        </p>
        <div class="mt-3 space-y-2">
          <div v-for="c in n.ds" :key="c.ma" class="overflow-hidden rounded-2xl glass">
            <button type="button" class="flex w-full items-center gap-3 px-4 py-3 text-left" :aria-expanded="moCung === c.ma" :aria-controls="`cung-${c.ma}`" @click="moCung = moCung === c.ma ? null : c.ma">
              <span class="grid size-9 shrink-0 place-items-center rounded-lg bg-navy-800 font-pixel text-[10px] text-pikachu-300" aria-hidden="true">{{ c.ma }}</span>
              <span class="min-w-0 flex-1">
                <span class="block font-display font-bold text-highlighted">{{ c.ten }}</span>
                <span class="text-xs text-muted">{{ c.vung }} · cấp {{ c.capTu === c.capDen ? c.capTu : `${c.capTu}–${c.capDen}` }}</span>
              </span>
              <UIcon name="i-lucide-chevron-down" class="size-5 text-dimmed transition" :class="moCung === c.ma ? 'rotate-180' : ''" aria-hidden="true" />
            </button>
            <ol v-if="moCung === c.ma" :id="`cung-${c.ma}`" class="grid gap-3 border-t border-default px-4 py-3 sm:grid-cols-2">
              <li v-for="ch in c.chuong" :key="ch.so" class="text-sm">
                <span class="text-dimmed">Chương {{ ch.so }}:</span> <strong class="text-highlighted">{{ chuongTT.get(ch.so)?.buoc[0] ?? ch.ten }}</strong>
                <span class="block text-xs text-muted">{{ chuongTT.get(ch.so)?.diaDanh || ch.diaDanh }} · từ cấp {{ ch.cap }}<template v-if="chuongTT.get(ch.so)"> · {{ chuongTT.get(ch.so)!.buoc.length }} bước</template></span>
                <span v-if="chuongTT.get(ch.so)?.tomTat" class="mt-1 block text-toned">{{ chuongTT.get(ch.so)!.tomTat }}</span>
                <span v-if="chuongTT.get(ch.so)?.huyHieu" class="mt-1 inline-flex items-center gap-1 text-xs font-bold text-warning"><UIcon name="i-lucide-medal" class="size-3.5" aria-hidden="true" /> {{ chuongTT.get(ch.so)!.huyHieu }}</span>
              </li>
            </ol>
          </div>
        </div>
      </div>
    </WikiSection>

    <WikiSection id="phu-tuyen" :title="N.tuyen.length ? 'Phụ tuyến & tuyến đường' : 'Phụ tuyến'">
      <p class="van">
        {{ N.phu.length }} chuỗi nhiệm vụ phụ ở các thị trấn, mỗi chuỗi {{ buocPhu }} bước; làm hết chuỗi của một thị trấn nhận danh hiệu thị trấn.<template v-if="N.tuyen.length"> Các tuyến đường có chuỗi trinh sát → dọn đường → kho báu.</template>
      </p>
      <div class="mt-4 grid gap-4" :class="N.tuyen.length ? 'lg:grid-cols-2' : ''">
        <details class="group rounded-2xl glass">
          <summary class="flex cursor-pointer items-center gap-2 px-4 py-3 font-semibold text-highlighted">
            <UIcon name="i-lucide-house" class="size-4 text-primary" aria-hidden="true" /> Danh sách {{ N.phu.length }} chuỗi phụ tuyến
            <UIcon name="i-lucide-chevron-down" class="ml-auto size-4 transition group-open:rotate-180" aria-hidden="true" />
          </summary>
        <div class="bang-cuon m-2">
          <table class="bang-wiki">
            <thead>
              <tr>
                <th>Chuỗi</th><th>Vùng</th><th class="so">
                  Từ cấp
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="p in N.phu" :key="p.ten">
                <td class="font-semibold text-highlighted">
                  {{ p.ten.replace(/^Thị trấn\s+/, '') }} <span class="text-xs font-normal text-muted">({{ p.soBuoc }} bước)</span>
                </td><td>{{ p.vung }}</td><td class="so">
                  {{ p.capTu }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        </details>
        <div v-if="N.tuyen.length" class="bang-cuon self-start">
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
      <details class="group rounded-2xl glass">
        <summary class="flex cursor-pointer items-center gap-2 px-4 py-3 font-semibold text-highlighted">
          <UIcon name="i-lucide-award" class="size-4 text-primary" aria-hidden="true" /> Xem {{ N.thanhTuu.length }} thành tựu
          <UIcon name="i-lucide-chevron-down" class="ml-auto size-4 transition group-open:rotate-180" aria-hidden="true" />
        </summary>
      <ul class="grid gap-2 p-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="t in N.thanhTuu" :key="t.ten" class="flex items-center gap-3 rounded-xl bg-elevated/60 px-3 py-2">
          <UIcon name="i-lucide-award" class="size-5 shrink-0 text-pikachu-600 dark:text-pikachu-300" />
          <span class="min-w-0 text-sm">
            <strong class="block text-highlighted">{{ t.ten }}</strong>
            <span class="text-xs text-muted">{{ TEN_KIEU_TT[t.kieu] ?? t.kieu.replace(/_/g, ' ') }}{{ t.kieu === 'nang_cap' ? '' : ':' }} {{ t.soLuong }}</span>
          </span>
        </li>
      </ul>
      </details>
    </WikiSection>
  </WikiShell>
</template>
