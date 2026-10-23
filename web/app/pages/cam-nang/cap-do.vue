<script setup lang="ts">
const { camNang: CAM_NANG } = await useCamNang() // số liệu CSDL game, dự phòng JSON
/** Cấp độ, năng lượng & tăng tốc — bảng cấp + năng lượng từ CSDL game (data_cap_do_lau_dai, data_kinh_te_lau_dai), còn lại cam-nang.json; bảng tăng tốc khớp com.htbb.pokemon.TangToc. */
const C = CAM_NANG.capDo
const nl = C.nangLuong
const gioNgay = nl.phutNgay / 60
const tt = C.tinhThong

/** Giờ chơi hiệu dụng tới cấp tối đa. */
const gioToiDa = C.bang.find(r => r.cap === C.capToiDa)?.gioTichLuy ?? 0

/** Vật phẩm tăng tốc (khớp com.htbb.pokemon.TangToc — đổi bên game thì sửa ở đây). Chung trần 60 điểm/ngày, 210/tuần. */
const TANG_TOC = [
  { ten: 'Vé EXP x1,5 (30 phút)', diem: 15, tacDung: 'EXP hạ quái ×1,5 trong 30 phút đánh quái (đứng yên không mất)', gioiHan: '60 phút vé/ngày' },
  { ten: 'Vé EXP x2 (30 phút)', diem: 30, tacDung: 'EXP hạ quái ×2 trong 30 phút đánh quái', gioiHan: 'chung 60 phút vé/ngày' },
  { ten: 'Bùa thể lực', diem: 45, tacDung: '+60 phút năng lượng 100% hôm nay (chỉ EXP)', gioiHan: '1/ngày' },
  { ten: 'Phiếu ủy thác luyện tập (1 giờ)', diem: 12, tacDung: 'Thoát game, Pokémon tự luyện: 20% EXP 1 giờ đánh quái', gioiHan: '3 giờ/ngày (+1 giờ với Thẻ tháng)' },
  { ten: 'Kẹo hiếm', diem: 30, tacDung: '30 phút EXP, trừ 30 phút năng lượng 100%', gioiHan: '2/tuần' },
]

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
        <b>Không</b><span>giới hạn cấp theo mùa</span>
      </div>
      <div class="the-so">
        <b>{{ hienGio(gioNgay) }}</b><span>tốc độ 100% mỗi ngày</span>
      </div>
      <div class="the-so">
        <b>×{{ (C.batKip.heSoPhanTram / 100).toLocaleString('vi-VN') }}</b><span>EXP khi bị tụt lại</span>
      </div>
    </div>

    <WikiSection id="tran-cap" title="Không giới hạn cấp theo mùa" :lead="`Cấp ${C.capToiDa} mở cho mọi người ngay từ đầu — không còn trần cấp theo mùa. Bù lại, lên cấp chậm: tổng ≈ ${hienGio(gioToiDa)} đánh quái hiệu dụng từ cấp 1 tới ${C.capToiDa}, và năng lượng huấn luyện giới hạn số giờ đầy đủ mỗi ngày.`">
      <ul class="cham">
        <li>Người chơi 1–2 giờ/ngày tới cấp {{ C.capToiDa }} sau khoảng <strong>15–18 tháng</strong>; người cày 5–7 giờ/ngày khoảng <strong>9–12 tháng</strong>.</li>
        <li>Nội dung mở theo <strong>cấp và cốt truyện</strong>: vùng Hoenn cần cấp {{ CAP_VAO_HOENN }} và xong chương 52 (kết năm 1, sau khi trở thành Nhà vô địch); chuỗi huyền thoại mở từng bước theo cấp.</li>
        <li>Sự kiện tuần, boss đột kích, mùa PvP và vé mùa vẫn chạy theo lịch.</li>
      </ul>
      <WikiCallout kieu="meo">
        Ở cấp tối đa, EXP không mất đi: nó chuyển sang <NuxtLink to="#tinh-thong" class="lien-ket">điểm tinh thông</NuxtLink>.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="tang-toc" title="Vật phẩm tăng tốc" lead="Muốn lên cấp nhanh hơn? Dùng vé EXP, bùa thể lực hoặc ủy thác luyện tập. Chúng chỉ cho EXP — không cho xu, đồ, đá hay chỉ số — và không dùng được ở cấp tối đa.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead><tr><th>Vật phẩm</th><th>Tác dụng</th><th>Điểm</th><th>Giới hạn riêng</th></tr></thead>
          <tbody>
            <tr v-for="t in TANG_TOC" :key="t.ten"><td>{{ t.ten }}</td><td>{{ t.tacDung }}</td><td>{{ t.diem }}</td><td>{{ t.gioiHan }}</td></tr>
          </tbody>
        </table>
      </div>
      <ul class="cham mt-4">
        <li>Mọi vật phẩm tăng tốc dùng chung giới hạn <strong>60 điểm mỗi ngày</strong> và <strong>210 điểm mỗi tuần</strong> (khoảng 3,5 giờ EXP).</li>
        <li><strong>Bản khoá</strong> kiếm được khi chơi: xong 2 nhiệm vụ tuần (2 vé x1,5 + 1 phiếu ủy thác), lịch đăng nhập ngày 3 (vé x1,5) và ngày 7 (bùa thể lực), vé mùa miễn phí, đổi lượng khoá ở Kecleon.</li>
        <li><strong>Bản cao cấp</strong> (cửa hàng lượng) có cùng công dụng và cùng giới hạn — chỉ tiết kiệm thời gian, không mạnh hơn người cùng cấp.</li>
      </ul>
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

    <WikiSection id="tinh-thong" title="Tinh thông" lead="Phần thưởng cho người đã đạt cấp tối đa.">
      <ul class="cham">
        <li>Ở cấp tối đa, mỗi lần tích đủ <strong>{{ tt.phanTramCap }}%</strong> EXP của cấp liền trước → nhận <strong>1 điểm tinh thông</strong>.</li>
        <li>Tổng tối đa <strong>{{ tt.toiDa }} điểm</strong>. Vật phẩm tăng tốc không dùng được ở cấp tối đa.</li>
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
            <tr v-for="r in bang" :key="r.cap" :class="r.cap % 10 === 0 ? 'noi-bat' : ''">
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
        Hàng tô vàng = mốc mỗi 10 cấp.
      </p>
    </WikiSection>
  </WikiShell>
</template>
