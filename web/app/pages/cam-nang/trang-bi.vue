<script setup lang="ts">
/** Trang bị & nâng cấp — mọi số từ cam-nang.json (ThongSoLauDai.java: TI_LE_NC, CHI_PHI_NC, ...). */
const T = CAM_NANG.trangBi
const bacChon = ref(3)
const bacItems = T.bac.map(b => ({ label: `${b.ten[0]!.toUpperCase()}${b.ten.slice(1)} (cấp ${b.cap})`, value: b.bac }))

const dong = computed(() => {
  const chiPhi = T.chiPhiNangCap[bacChon.value] ?? []
  let tongXu = 0
  let tongDa = 0
  return T.nangCap.map((n, i) => {
    const kyVong = soLanThuKyVong(n.tiLe, T.pityChia)
    const xu = chiPhi[i] ?? 0
    tongXu += kyVong * xu
    tongDa += kyVong * n.da
    return { ...n, kyVong, xu, tongXu, tongDa, them: Math.ceil(n.tiLe / T.pityChia) }
  })
})
const chiSoCong = (plus: number) => T.nangCap.filter(n => n.plus <= plus).reduce((s, n) => s + n.chiSoPhanTram, 0)
const ngoc = T.ngocHe
const giaNgoc = Array.from({ length: ngoc.capToiDa }, (_, i) => ({ cap: i + 1, gio: (ngoc.giaGioPhanTram / 100) * (ngoc.tangPhanTram / 100) ** i }))
</script>

<template>
  <WikiShell slug="trang-bi">
    <p class="van">
      Trang bị <strong>không đổi hình dạng</strong> Pokémon của bạn — chúng chỉ cộng chỉ số. Có {{ T.soO }} ô, {{ T.bac.length }} bậc theo cấp,
      nâng cấp từ +1 tới +{{ T.nangCap.length }} với cơ chế bù may mắn nên ai kiên trì cũng lên được.
    </p>

    <WikiSection id="o" title="Ô trang bị">
      <ul class="grid gap-3 sm:grid-cols-2">
        <li v-for="o in T.o" :key="o.ten" class="flex gap-3 rounded-2xl glass p-4">
          <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-pikachu-400 text-navy-950 ring-2 ring-[color-mix(in_oklab,var(--poke-outline)_70%,transparent)]">
            <UIcon :name="o.loai === 'vat-cam' ? 'i-lucide-gem' : o.loai === 'huy-hieu' ? 'i-lucide-badge' : o.loai === 'ruy-bang' ? 'i-lucide-ribbon' : 'i-lucide-shirt'" class="size-5" />
          </span>
          <span>
            <strong class="font-display text-highlighted">{{ o.ten }}</strong>
            <span class="block text-sm text-muted">{{ o.vai }}</span>
            <span class="block text-xs text-dimmed">{{ o.soMon }} món trong game</span>
          </span>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="bac" title="Bậc trang bị" :lead="`Mỗi bậc mở theo cấp; vài bậc cao mở theo mùa cùng trần cấp. Mỗi bậc chỉ ${T.soMonMuaMoiBac} món bán ở cửa hàng, phần còn lại đến từ nhiệm vụ, quái rơi và chế tạo.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bậc</th><th class="so">
                Cấp đeo
              </th><th>Mở khi</th><th class="so">
                Giá tham khảo / món
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in T.bac" :key="b.bac">
              <td class="font-semibold capitalize text-highlighted">
                {{ b.ten }}
              </td>
              <td class="so">
                {{ b.cap }}
              </td>
              <td>{{ b.ngayMo ? thangTuKhaiTruong(b.ngayMo) : 'Ngay từ đầu' }}</td>
              <td class="so">
                {{ soVn(b.gia) }} xu
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <WikiCallout kieu="meo">
        Đổi sang món bậc cao hơn cùng ô, bạn <strong>giữ lại cấp nâng</strong> trừ {{ T.giuCapKhiDoiBac }} (ví dụ +10 → +{{ 10 - T.giuCapKhiDoiBac }}).
        Không phải nâng lại từ đầu.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="nang-cap" title="Nâng cấp +1..+15">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>+{{ T.nangCap.length }}</b><span>mức tối đa · +{{ chiSoCong(T.nangCap.length) }}% chỉ số món</span>
        </div>
        <div class="the-so">
          <b>Bù may mắn</b><span>mỗi lần trượt liên tiếp: tỉ lệ +⅓ tỉ lệ gốc</span>
        </div>
        <div class="the-so">
          <b>từ +{{ T.matCapTu }}</b><span>trượt thì tụt 1 cấp (trừ khi có Bùa bảo hộ)</span>
        </div>
      </div>
      <div class="bang-cuon mt-5">
        <table class="bang-wiki">
          <caption>Tỉ lệ gốc, nguyên liệu và bảo hộ cho từng mức</caption>
          <thead>
            <tr>
              <th>Lên</th><th class="so">
                Tỉ lệ gốc
              </th><th class="so">
                Bù / lần trượt
              </th><th class="so">
                Đá nâng cấp
              </th><th class="so">
                Bùa (lượng khoá)
              </th><th class="so">
                Chỉ số món
              </th><th>Trượt thì</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="n in dong" :key="n.plus">
              <td class="font-bold text-highlighted">
                +{{ n.plus }}
              </td>
              <td class="so">
                <span class="inline-flex items-center gap-2">
                  <span class="hidden h-1.5 w-10 overflow-hidden rounded-full bg-elevated xl:inline-block"><span class="block h-full rounded-full bg-grass-500" :style="{ width: `${n.tiLe}%` }" /></span>
                  {{ n.tiLe }}%
                </span>
              </td>
              <td class="so">
                {{ n.tiLe >= 100 ? '—' : `+${n.them}%` }}
              </td>
              <td class="so">
                {{ n.da }}
              </td>
              <td class="so">
                {{ n.buaLuongKhoa || '—' }}
              </td>
              <td class="so">
                +{{ chiSoCong(n.plus) }}%
              </td>
              <td class="whitespace-nowrap">
                {{ n.tiLe >= 100 ? '—' : n.plus >= T.matCapTu ? 'Tụt 1 cấp' : 'Giữ nguyên' }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <WikiCallout kieu="vi-sao" title="Cơ chế bù may mắn hoạt động thế nào?">
        Mỗi lần trượt <strong>liên tiếp</strong> ở cùng một mức, tỉ lệ lần sau tăng thêm ⌈tỉ lệ gốc / {{ T.pityChia }}⌉%.
        Ví dụ +15 gốc {{ T.nangCap[14]?.tiLe }}%: lần 2 là {{ (T.nangCap[14]?.tiLe ?? 0) + Math.ceil((T.nangCap[14]?.tiLe ?? 0) / T.pityChia) }}%, lần 3 là
        {{ (T.nangCap[14]?.tiLe ?? 0) + 2 * Math.ceil((T.nangCap[14]?.tiLe ?? 0) / T.pityChia) }}%… nên không ai "đen" mãi được. Thành công thì bộ đếm về 0.
        Bùa bảo hộ chỉ giữ cấp khi trượt, không làm tăng tỉ lệ.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="chi-phi" title="Chi phí & số lần thử" lead="Chi phí xu mỗi lần thử tăng theo bậc món và theo mức. Bảng dưới tính số lần thử kỳ vọng (đã gồm bù may mắn, chưa tính tụt cấp).">
      <UFormField label="Bậc món" class="max-w-xs">
        <USelect v-model="bacChon" :items="bacItems" class="w-full" />
      </UFormField>
      <div class="bang-cuon mt-4">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Lên</th><th class="so">
                Xu / lần
              </th><th class="so">
                Lần thử kỳ vọng
              </th><th class="so">
                Xu kỳ vọng (cộng dồn)
              </th><th class="so">
                Đá kỳ vọng (cộng dồn)
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="n in dong" :key="n.plus">
              <td class="font-bold text-highlighted">
                +{{ n.plus }}
              </td>
              <td class="so">
                {{ soVn(n.xu) }}
              </td>
              <td class="so">
                {{ soVn(n.kyVong, 2) }}
              </td>
              <td class="so">
                {{ soVn(Math.round(n.tongXu)) }}
              </td>
              <td class="so">
                {{ soVn(n.tongDa, 1) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <ul class="cham mt-5">
        <li>Đá nâng cấp rơi từ quái chênh cấp không quá 5 với bạn: <strong>{{ (T.daRoiPhanNghin / 10).toLocaleString('vi-VN') }}%</strong> mỗi con.</li>
        <li>Phân rã một món trùng → nhận <strong>{{ T.daTuPhanRa }} đá</strong> và một ít xu.</li>
        <li>Bùa bảo hộ mua bằng lượng khoá (kiếm từ nhiệm vụ, đăng nhập) — không cần tiền thật.</li>
      </ul>
    </WikiSection>

    <WikiSection id="ngoc-he" title="Ngọc hệ" :lead="`Mỗi món có ô khảm 1 viên ngọc hệ, cấp 1–${ngoc.capToiDa}; mỗi cấp ngọc +1% chỉ số của món. Giá mỗi cấp tăng ×${(ngoc.tangPhanTram / 100).toLocaleString('vi-VN')} so với cấp trước.`">
      <div class="flex flex-wrap gap-2">
        <span v-for="g in giaNgoc" :key="g.cap" class="rounded-xl bg-elevated px-3 py-2 text-center text-sm">
          <b class="block font-display text-highlighted">Cấp {{ g.cap }}</b>
          <span class="text-xs text-muted">≈ {{ hienGio(g.gio) }} thu nhập</span>
        </span>
      </div>
      <p class="mt-2 text-xs text-dimmed">
        "Giờ thu nhập" = lượng xu kiếm được trong 1 giờ đánh quái ở cấp của bạn — giá luôn tương xứng dù bạn cấp thấp hay cao.
      </p>
    </WikiSection>

    <WikiSection id="bo" title="Thưởng bộ" lead="Mặc nhiều món cùng bậc nhận thêm thưởng:">
      <ul class="grid gap-3 sm:grid-cols-3">
        <li v-for="b in T.boTrangBi" :key="b.soMon" class="the-so">
          <b>{{ b.soMon }} món</b><span>+{{ b.phanTram }}% {{ b.chiSo }}</span>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
