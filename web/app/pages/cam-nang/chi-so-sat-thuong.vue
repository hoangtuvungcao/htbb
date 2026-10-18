<script setup lang="ts">
/** Chỉ số & sát thương — giải thích cho người chơi; khắc hệ/STAB/giai đoạn từ pokedex.json. */
const DONG = [
  { cls: 0, dex: 4, chinh: 'Sức mạnh', tc: 'Khá', hp: 'Cao', loi: 'Cận chiến cân bằng, sát thương ổn định.' },
  { cls: 1, dex: 1, chinh: 'Sức mạnh', tc: 'Rất cao', hp: 'Khá', loi: 'Tấn công cao nhất khi dồn điểm, trụ tốt.' },
  { cls: 2, dex: 133, chinh: 'Tinh thần', tc: 'Cao', hp: 'Thấp', loi: 'Máu thấp, bù bằng năng lượng (MP) dồi dào và chiêu hỗ trợ.' },
  { cls: 3, dex: 7, chinh: 'Sức mạnh', tc: 'Rất cao', hp: 'Khá', loi: 'Thủ chắc, phản công mạnh.' },
  { cls: 4, dex: 172, chinh: 'Nhanh nhẹn', tc: 'Cao', hp: 'Thấp', loi: 'Đánh xa, cơ động; máu thấp nên giữ khoảng cách.' },
].map(d => ({ ...d, loai: loaiTheoDex(d.dex)! }))

const pt = POKEDEX.phanTramGiaiDoan

/** Dòng chỉ số trên món (khớp tên trong game). */
const CHI_SO_DONG: [string, string][] = [
  ['Tấn công / Tấn công %', 'Tăng sát thương mọi đòn.'],
  ['Phòng thủ / Phòng thủ đặc biệt', 'Giảm sát thương vật lý / đặc biệt nhận vào.'],
  ['HP, PP', 'Máu và năng lượng tối đa. Dòng "mỗi điểm +1.000" cộng 1.000 cho mỗi điểm.'],
  ['Chí mạng', '3 điểm chí mạng = 1% tỉ lệ chí mạng. "Chí mạng thêm" cộng thẳng theo %.'],
  ['ST chí mạng', 'Đòn chí mạng mạnh hơn (gốc ×1,5).'],
  ['Chính xác / Né tránh', 'Khả năng đánh trúng / tránh đòn.'],
  ['Hút HP / Hút PP', 'Hồi lại theo % sát thương bạn gây ra.'],
  ['Hồi HP / PP mỗi 5 giây', 'Tự hồi dần khi đang chơi.'],
  ['Xuyên giáp, bỏ qua phòng thủ', 'Bỏ qua một phần phòng thủ của mục tiêu.'],
  ['Giảm ST nhận, phản sát thương', 'Giảm sát thương bị đánh / trả lại một phần cho kẻ đánh.'],
  ['Kháng hệ X, Kháng mọi hệ', 'Giảm sát thương nhận từ chiêu thuộc hệ đó.'],
  ['ST hệ X', 'Tăng sát thương chiêu thuộc hệ đó.'],
  ['Phiến đá hệ', 'Chọn một trong 18 hệ: dòng "theo hệ phiến đá" tăng sát thương hoặc kháng đúng hệ ghi trên tên đá.'],
  ['Gây ngủ / đóng băng / trúng độc / tê liệt / bỏng', 'Tỉ lệ gây trạng thái khi đánh trúng. Dòng "Kháng ..." giảm tỉ lệ bị trúng.'],
  ['Tốc độ đánh / Tốc độ chạy', 'Giảm thời gian hồi chiêu / chạy nhanh hơn.'],
  ['ST lên boss / quái, Tăng EXP', 'Thêm sát thương lên boss hoặc quái thường / thêm EXP nhận được.'],
]

/** Mức tối đa (khớp server khi bật thiết kế dài hạn). */
const GIOI_HAN: [string, string, string][] = [
  ['Tỉ lệ chí mạng', '40%', '30%'],
  ['Đòn chí mạng', '×2', '×1,5'],
  ['Né tránh', '30%', '20%'],
  ['Hút HP', '10%', '5%'],
  ['Giảm sát thương nhận', '50%', '40%'],
  ['Kháng một hệ', '60%', '60%'],
  ['Phản sát thương', '15%', '10%'],
  ['Tốc độ đánh', '+50%', '+50%'],
  ['Tốc độ chạy', '+30%', '+30%'],
  ['ST lên boss / lên quái', '+20% / +20%', '—'],
  ['Gây trạng thái từ đồ', '15%', '7,5%'],
  ['EXP từ trang bị, thú cưng, thời trang', '+30%', '—'],
]

// Máy tính
const heItems = POKEDEX.he.map(h => ({ label: h.ten, value: h.id }))
const tanCong = ref(1200)
const phanTramChieu = ref(150)
const phongThu = ref(300)
const heChieu = ref(1)
const heMucTieu = ref(4)
const stab = ref(true)
const giaiDoan = ref<'1' | '2' | '3' | 'eevee'>('2')
const gdItems = [
  { label: 'Dạng 1 (+0%)', value: '1' },
  { label: `Dạng 2 (+${pt[2]}%)`, value: '2' },
  { label: `Dạng 3 (+${pt[3]}%)`, value: '3' },
  { label: `Nhánh Eevee (+${pt.eevee}%)`, value: 'eevee' },
]
const thuongGd = computed(() => giaiDoan.value === '1' ? 0 : pt[giaiDoan.value as '2' | '3' | 'eevee'])
const buoc = computed(() => {
  const a = Math.round(tanCong.value * (100 + thuongGd.value) / 100)
  const goc = Math.round(a * phanTramChieu.value / 100)
  const he = heSoTrongGame(heChieu.value, [heMucTieu.value])
  const sauHe = Math.round(goc * he / 100)
  const sauStab = Math.round(sauHe * (stab.value ? POKEDEX.stabPhanTram : 100) / 100)
  const cuoi = Math.max(1, sauStab - phongThu.value)
  return { a, goc, he, sauHe, sauStab, cuoi }
})
</script>

<template>
  <WikiShell slug="chi-so-sat-thuong">
    <WikiSection id="chi-so" title="Chỉ số & điểm tiềm năng">
      <p class="van">
        Pokémon của bạn có bốn chỉ số gốc: <strong>Sức mạnh</strong>, <strong>Nhanh nhẹn</strong>, <strong>Tinh thần</strong> và <strong>Sức khoẻ</strong>.
        Mỗi lần lên cấp bạn nhận <strong>+1 vào mọi chỉ số</strong>, <strong>+5 điểm tiềm năng</strong> để tự cộng (mục <em>Bản thân</em> trong menu)
        và <strong>+1 điểm kỹ năng</strong> để nâng chiêu.
      </p>
      <div class="bang-cuon mt-5">
        <table class="bang-wiki">
          <caption>Chỉ số chính quyết định tấn công của từng dòng khởi đầu</caption>
          <thead>
            <tr>
              <th>Dòng</th><th>Chỉ số chính</th><th>
                Tấn công khi dồn điểm
              </th><th>
                Máu
              </th><th>Lối chơi</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="d in DONG" :key="d.cls">
              <td>
                <NuxtLink :to="`/pokedex/${d.dex}`" class="flex items-center gap-2 font-semibold text-highlighted hover:text-primary">
                  <PokeAvatar :dex="d.dex" class="size-7" />{{ d.loai.ten }}
                </NuxtLink>
              </td>
              <td>{{ d.chinh }}</td>
              <td>{{ d.tc }}</td>
              <td>{{ d.hp }}</td>
              <td>{{ d.loi }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <WikiCallout kieu="meo">
        Dồn phần lớn điểm vào chỉ số chính để tăng sát thương, phần còn lại vào <strong>Sức khoẻ</strong> để tăng HP.
        Pokémon máu thấp (Eevee, Pichu) nên có ít nhất ~30% điểm vào Sức khoẻ khi đi PvP.
      </WikiCallout>
      <p class="van">
        Tiến hoá cộng thẳng % vào HP và tấn công: dạng 2 <strong>+{{ pt[2] }}%</strong>, dạng 3 <strong>+{{ pt[3] }}%</strong>,
        nhánh Eevee <strong>+{{ pt.eevee }}%</strong>. Trang bị, ngọc hệ, thưởng bộ và tinh thông cộng thêm sau đó.
      </p>
    </WikiSection>

    <WikiSection id="chi-so-trang-bi" title="Chỉ số trên trang bị" lead="Các dòng chỉ số bạn thấy khi xem một món đồ, thú cưng, thú cưỡi hay skin.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <caption>Ý nghĩa các dòng chỉ số thường gặp</caption>
          <thead>
            <tr><th>Dòng</th><th>Tác dụng</th></tr>
          </thead>
          <tbody>
            <tr v-for="r in CHI_SO_DONG" :key="r[0]">
              <td class="font-semibold text-highlighted">
                {{ r[0] }}
              </td>
              <td>{{ r[1] }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="van">
        Mỗi trạng thái có hệ miễn nhiễm riêng (hệ Lửa không bị bỏng, hệ Điện không bị tê liệt...). Trong PvP, thời gian trạng thái chỉ còn một nửa.
      </p>
    </WikiSection>

    <WikiSection id="gioi-han" title="Giới hạn chỉ số" lead="Để trận đấu luôn công bằng và đồ cũ không bị thổi phồng, một số chỉ số có mức tối đa. Càng gần mức tối đa, cộng thêm càng tăng chậm.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <caption>Mức tối đa của từng chỉ số</caption>
          <thead>
            <tr><th>Chỉ số</th><th>Đánh quái</th><th>PvP</th></tr>
          </thead>
          <tbody>
            <tr v-for="r in GIOI_HAN" :key="r[0]">
              <td class="font-semibold text-highlighted">
                {{ r[0] }}
              </td>
              <td>{{ r[1] }}</td>
              <td>{{ r[2] }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <ul class="cham">
        <li>Mọi lớp giảm sát thương (phòng thủ đặc biệt từ đồ, kháng hệ, thú cưỡi, skin...) cộng lại thì một đòn vẫn còn ít nhất <strong>40%</strong> sát thương. Không ai bất tử.</li>
        <li>Hút HP với boss, Gym và quái tinh anh chỉ còn một phần mười, và mỗi đòn hồi không quá một phần nhỏ HP tối đa.</li>
        <li>Tổng phần trăm cộng thêm vào tấn công, phòng thủ và HP từ thú cưng, thú cưỡi, thời trang, bộ đồ và đặc quyền tối đa <strong>+50%</strong>.</li>
        <li>Mỗi món đồ có mức chỉ số tối đa theo bậc, cấp và mức nâng cấp. Đồ bậc cũ đã nâng +15 không mạnh hơn đồ bậc hiện tại quá <strong>30%</strong>, nên khi lên bậc mới, đổi đồ luôn đáng.</li>
        <li>Nếu một món có chỉ số vượt mức tối đa, phần dư sẽ không được tính. Bảng chỉ số của món hiện đúng con số đang có tác dụng.</li>
      </ul>
    </WikiSection>

    <WikiSection id="cong-thuc" title="Sát thương tính thế nào" lead="Mỗi đòn đánh đi qua các bước sau (đơn giản hoá cho dễ hiểu):">
      <ol class="grid gap-3 md:grid-cols-2">
        <li v-for="(b, i) in [
          ['Tấn công', 'Từ chỉ số chính × hệ số dòng, cộng % tiến hoá, trang bị và hiệu ứng tăng sức mạnh.'],
          ['× % sức mạnh chiêu', 'Đánh thường ~100%; chiêu mạnh 135–450%, nâng chiêu bằng điểm kỹ năng để tăng %.'],
          ['× khắc hệ', 'Hệ của CHIÊU so với hệ mục tiêu: ×2 / ×1,5 / ×1 / ×0,7 / ×0,5 / ×0,3.'],
          ['× STAB', `Chiêu cùng hệ với bạn: ×${(POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN')}.`],
          ['− phòng thủ', 'Trừ phòng thủ của mục tiêu (tối thiểu luôn gây 1 sát thương).'],
          ['Chí mạng & dao động', 'Có tỉ lệ chí mạng (găng đấm, thưởng bộ tăng) và dao động nhỏ ngẫu nhiên.'],
        ]" :key="i" class="flex gap-3 rounded-2xl glass p-4">
          <span class="grid size-8 shrink-0 place-items-center rounded-full border-2 border-[var(--poke-outline)] bg-pokered-600 font-pixel text-[10px] text-white">{{ i + 1 }}</span>
          <span><strong class="font-display text-highlighted">{{ b[0] }}</strong><span class="mt-0.5 block text-sm text-muted">{{ b[1] }}</span></span>
        </li>
      </ol>
      <WikiCallout kieu="vi-sao">
        Khắc hệ áp <strong>trước</strong> khi trừ phòng thủ, nên khi mục tiêu thủ cao, đánh đúng hệ có thể tăng sát thương thực tế nhiều hơn hẳn ×1,5.
        Ngược lại, đánh vào hệ kháng có thể chỉ còn sát thương tối thiểu.
      </WikiCallout>
      <p class="van">
        <strong>PvP:</strong> sát thương giữa người chơi được giảm mạnh so với đánh quái và có trần theo đòn, để một trận không kết thúc chỉ sau một chiêu.
      </p>
    </WikiSection>

    <WikiSection id="may-tinh" title="Máy tính sát thương" lead="Thử nhanh một đòn với mô hình đơn giản ở trên (chưa tính chí mạng và dao động).">
      <div class="grid gap-6 rounded-3xl glass ring-gilded p-5 sm:p-6 lg:grid-cols-[1fr_20rem]">
        <div class="grid gap-4 sm:grid-cols-2">
          <UFormField label="Tấn công gốc">
            <UInputNumber v-model="tanCong" :min="1" :max="999999" class="w-full" />
          </UFormField>
          <UFormField label="Giai đoạn tiến hoá">
            <USelect v-model="giaiDoan" :items="gdItems" class="w-full" />
          </UFormField>
          <UFormField label="% sức mạnh chiêu">
            <UInputNumber v-model="phanTramChieu" :min="1" :max="5000" class="w-full" />
          </UFormField>
          <UFormField label="Phòng thủ mục tiêu">
            <UInputNumber v-model="phongThu" :min="0" :max="999999" class="w-full" />
          </UFormField>
          <UFormField label="Hệ của chiêu">
            <USelect v-model="heChieu" :items="heItems" class="w-full" />
          </UFormField>
          <UFormField label="Hệ của mục tiêu">
            <USelect v-model="heMucTieu" :items="heItems" class="w-full" />
          </UFormField>
          <UCheckbox v-model="stab" label="Có STAB (chiêu cùng hệ với tôi)" class="sm:col-span-2" />
        </div>
        <div class="rounded-2xl bg-screen p-5 text-sm">
          <dl class="space-y-1.5">
            <div class="flex justify-between">
              <dt>Tấn công sau tiến hoá</dt><dd class="num font-semibold">
                {{ soVn(buoc.a) }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt>× chiêu {{ phanTramChieu }}%</dt><dd class="num font-semibold">
                {{ soVn(buoc.goc) }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt>× khắc hệ {{ hienHeSo(buoc.he) }}</dt><dd class="num font-semibold">
                {{ soVn(buoc.sauHe) }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt>× STAB</dt><dd class="num font-semibold">
                {{ soVn(buoc.sauStab) }}
              </dd>
            </div>
            <div class="flex justify-between">
              <dt>− phòng thủ</dt><dd class="num font-semibold">
                −{{ soVn(phongThu) }}
              </dd>
            </div>
          </dl>
          <p class="mt-4 border-t-2 border-dashed border-[var(--ui-border)] pt-3 text-xs font-bold uppercase tracking-wide text-muted">
            Sát thương ước tính
          </p>
          <p class="font-display text-4xl font-extrabold text-highlighted" aria-live="polite">
            {{ soVn(buoc.cuoi) }}
          </p>
        </div>
      </div>
    </WikiSection>

    <WikiSection id="guc-nga" title="Gục ngã & hồi sinh">
      <ul class="cham">
        <li>Hết HP thì Pokémon gục ngã. Bị quái hạ mất một ít EXP của cấp hiện tại (khoảng 0,1%); bị người chơi hạ khi PK mất nhiều hơn (khoảng 1%).</li>
        <li>Hồi sinh tại chỗ tốn xu, giá tăng theo cấp; hoặc về làng hồi sinh miễn phí.</li>
        <li>Mang theo <strong>Thuốc hồi máu</strong> (phím 7) và <strong>Ether</strong> hồi năng lượng (phím 9) — mua ở Kecleon (tạp hoá).</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
