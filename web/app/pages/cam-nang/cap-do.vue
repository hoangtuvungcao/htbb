<script setup lang="ts">
/** Cấp độ & trần cấp mùa — mọi số từ cam-nang.json (ThongSoLauDai.java + bang_cap_do_lau_dai.csv). */
const C = CAM_NANG.capDo
const nl = C.nangLuong
const gioNgay = nl.phutNgay / 60
const tt = C.tinhThong

/** Giờ chơi hiệu dụng để đạt mỗi trần cấp. */
const tranCap = C.tranCap.map(t => ({ ...t, gio: C.bang.find(r => r.cap === t.cap)?.gioTichLuy ?? 0 }))

const xemTatCa = ref(false)
const MOC = [1, 5, 10, 16, 20, 25, 30, 36, 40, 50, 60, 70, 80, 90, 91, 95, 100]
const bang = computed(() => (xemTatCa.value ? C.bang : C.bang.filter(r => MOC.includes(r.cap))))
const capTim = ref<number | null>(null)
const dongTim = computed(() => C.bang.find(r => r.cap === capTim.value))

const thanhMet = [
  { nhan: `${hienGio(gioNgay)} đầu mỗi ngày`, tocDo: 100, rong: nl.phutNgay },
  ...nl.bacMet.map(b => ({ nhan: `${hienGio(b.phut / 60)} tiếp theo`, tocDo: b.tocDo, rong: b.phut })),
]
const tongMet = thanhMet.reduce((s, b) => s + b.rong, 0)
</script>

<template>
  <WikiShell slug="cap-do">
    <div class="grid grid-cols-2 gap-3 sm:grid-cols-4">
      <div class="the-so">
        <b>{{ C.capToiDa }}</b><span>cấp tối đa</span>
      </div>
      <div class="the-so">
        <b>{{ C.tranCap[0]!.cap }}</b><span>trần cấp lúc khai trương</span>
      </div>
      <div class="the-so">
        <b>{{ hienGio(gioNgay) }}</b><span>tốc độ 100% mỗi ngày</span>
      </div>
      <div class="the-so">
        <b>×{{ (C.batKip.heSoPhanTram / 100).toLocaleString('vi-VN') }}</b><span>EXP khi bị tụt lại</span>
      </div>
    </div>

    <WikiSection id="tran-cap" title="Trần cấp theo mùa" lead="Cấp tối đa không mở hết ngay từ đầu: trần cấp nâng dần theo số ngày kể từ khi máy chủ khai trương. Người chơi chăm chỉ không bỏ quá xa người chơi nhẹ nhàng, còn nội dung mới luôn có người cùng khám phá.">
      <ol class="relative grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="(t, i) in tranCap" :key="t.ngay" class="flex items-center gap-4 rounded-2xl glass p-4">
          <span class="grid size-14 shrink-0 place-items-center rounded-xl bg-screen font-pixel text-sm text-pokered-600 dark:text-pikachu-300">{{ t.cap }}</span>
          <span>
            <strong class="block font-display text-highlighted">{{ i === 0 ? 'Khai trương' : `Mùa ${i + 1}` }}</strong>
            <span class="text-sm text-muted">{{ thangTuKhaiTruong(t.ngay) }}</span>
            <span class="block text-xs text-dimmed">≈ {{ hienGio(t.gio) }} luyện cấp hiệu dụng để chạm trần</span>
          </span>
        </li>
      </ol>
      <WikiCallout kieu="meo">
        Chạm trần cấp rồi thì EXP không mất đi: nó chuyển sang <NuxtLink to="#tinh-thong" class="lien-ket">điểm tinh thông</NuxtLink>.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="nang-luong" title="Năng lượng huấn luyện" lead="Mỗi ngày, những giờ đánh quái đầu tiên cho đủ EXP và xu; chơi quá lâu thì tốc độ giảm dần. Cơ chế này giữ sức khoẻ cho người chơi và khiến treo máy/bot không có lợi.">
      <div class="rounded-3xl glass p-5">
        <div class="flex h-10 overflow-hidden rounded-full border-2 border-[var(--poke-outline)]" role="img" :aria-label="thanhMet.map(b => `${b.nhan}: ${b.tocDo}%`).join(', ')">
          <div
            v-for="b in thanhMet"
            :key="b.nhan"
            class="grid place-items-center text-xs font-bold"
            :class="b.tocDo === 100 ? 'bg-grass-400 text-navy-950' : b.tocDo >= 50 ? 'bg-pikachu-300 text-navy-950' : 'bg-pokered-300 text-navy-950'"
            :style="{ width: `${(b.rong / tongMet) * 100}%` }"
          >
            {{ b.tocDo }}%
          </div>
        </div>
        <ul class="mt-4 grid gap-2 text-sm sm:grid-cols-3">
          <li v-for="b in thanhMet" :key="b.nhan">
            <strong class="text-highlighted">{{ b.tocDo }}% EXP & xu</strong> — {{ b.nhan }}
          </li>
        </ul>
        <p class="mt-3 text-sm text-muted">
          Sau đó: 0% EXP và 0 xu từ quái (nhiệm vụ, sự kiện vẫn nhận bình thường).
        </p>
      </div>
      <ul class="cham mt-5">
        <li>Ngày nào không chơi hết, phần 100% được <strong>tích lại</strong> (nghỉ ngơi), tối đa <strong>{{ hienGio(nl.phutTichToiDa / 60) }}</strong>.</li>
        <li>Nhiệm vụ, sự kiện, boss và Pokédex không bị năng lượng giới hạn.</li>
      </ul>
    </WikiSection>

    <WikiSection id="bat-kip" title="Bắt kịp & tân thủ">
      <ul class="cham">
        <li>
          <strong>Bắt kịp:</strong> nếu cấp của bạn thấp hơn cấp trung vị của máy chủ quá {{ C.batKip.khoangCap }} cấp,
          EXP từ quái được nhân <strong>×{{ (C.batKip.heSoPhanTram / 100).toLocaleString('vi-VN') }}</strong>.
        </li>
        <li>
          <strong>Tân thủ:</strong> dưới cấp {{ C.tanThu.duoiCap }}, thưởng nhiệm vụ được nhân
          <strong>×{{ (C.tanThu.heSoPhanTram / 100).toLocaleString('vi-VN') }}</strong> — lần tiến hoá đầu tiên đến rất nhanh.
        </li>
        <li>Quái thấp hơn bạn nhiều cấp cho ít EXP hơn; nên luyện ở khu có quái gần cấp mình (xem <NuxtLink to="/cam-nang/ban-do#luyen-cap" class="lien-ket">khu luyện cấp</NuxtLink>).</li>
      </ul>
    </WikiSection>

    <WikiSection id="tinh-thong" title="Tinh thông" lead="Phần thưởng cho người đã chạm trần cấp của mùa.">
      <ul class="cham">
        <li>Ở trần cấp, mỗi lần tích đủ <strong>{{ tt.phanTramCap }}%</strong> EXP của cấp liền trước trần → nhận <strong>1 điểm tinh thông</strong>.</li>
        <li>Mỗi mùa nhận tối đa <strong>{{ tt.diemMoiMua }} điểm</strong>; tổng tối đa <strong>{{ tt.toiDa }} điểm</strong>.</li>
        <li>Mỗi điểm: <strong>+{{ (tt.phanNghinMoiDiem / 10).toLocaleString('vi-VN') }}%</strong> tấn công và HP (tối đa +{{ ((tt.phanNghinMoiDiem * tt.toiDa) / 10).toLocaleString('vi-VN') }}%).</li>
      </ul>
    </WikiSection>

    <WikiSection id="bang-exp" title="Bảng EXP" lead="Giờ hiệu dụng = số giờ đánh quái cùng cấp ở tốc độ 100%. Thời gian thực tế phụ thuộc trang bị, đội và nhiệm vụ.">
      <div class="mb-4 flex flex-wrap items-end gap-3">
        <UFormField label="Tra nhanh cấp">
          <UInputNumber v-model="capTim" :min="1" :max="C.capToiDa" placeholder="1–100" class="w-36" />
        </UFormField>
        <p v-if="dongTim" class="rounded-xl bg-screen px-3 py-2 text-sm">
          Cấp {{ dongTim.cap }}: cần <strong>{{ soVn(dongTim.expLenCap) }}</strong> EXP để lên cấp,
          ≈ {{ soVn(Math.ceil(dongTim.expLenCap / Math.max(1, dongTim.expMoiQuai))) }} quái cùng cấp,
          tổng ≈ {{ hienGio(dongTim.gioTichLuy) }} từ cấp 1.
        </p>
        <USwitch v-model="xemTatCa" label="Hiện đủ 100 cấp" class="ml-auto" />
      </div>
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th class="so">
                Cấp
              </th><th class="so">
                EXP lên cấp
              </th><th class="so">
                EXP / quái
              </th><th class="so">
                Quái / giờ
              </th><th class="so">
                Xu / giờ
              </th><th class="so">
                Giờ cho cấp này
              </th><th class="so">
                Tổng giờ từ cấp 1
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in bang" :key="r.cap" :class="C.tranCap.some(t => t.cap === r.cap) ? 'noi-bat' : ''">
              <td class="so font-bold text-highlighted">
                {{ r.cap }}
              </td>
              <td class="so">
                {{ r.expLenCap ? soVn(r.expLenCap) : '—' }}
              </td>
              <td class="so">
                {{ soVn(r.expMoiQuai) }}
              </td>
              <td class="so">
                {{ r.quaiMoiGio }}
              </td>
              <td class="so">
                {{ soVn(r.xuMoiGio) }}
              </td>
              <td class="so">
                {{ r.expLenCap ? hienGio(r.gioCap) : '—' }}
              </td>
              <td class="so">
                {{ hienGio(r.gioTichLuy) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="mt-2 text-xs text-dimmed">
        Hàng tô vàng = mốc trần cấp theo mùa.
      </p>
    </WikiSection>
  </WikiShell>
</template>
