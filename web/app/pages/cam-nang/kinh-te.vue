<script setup lang="ts">
/** Kinh tế & tiền tệ — chỉ những gì người chơi cần biết (số liệu từ cam-nang.json). */
const K = CAM_NANG.kinhTe
const T = CAM_NANG.trangBi
const tenSite = useAppConfig().htbb.tenSite

const tongDangNhap = K.lichDangNhap.reduce((s, x) => s + x, 0)
</script>

<template>
  <WikiShell slug="kinh-te">
    <WikiCallout kieu="luu-y" title="Không nạp tiền, không bán sức mạnh">
      {{ tenSite }} là dự án fan phi thương mại: không có nạp tiền, không có cửa hàng bán chỉ số. Mọi thứ trong game đều kiếm được bằng cách chơi.
    </WikiCallout>

    <WikiSection id="tien" title="Ba loại tiền">
      <div class="grid gap-4 md:grid-cols-3">
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <span class="grid size-9 place-items-center rounded-full bg-pikachu-400 text-navy-950"><UIcon name="i-lucide-coins" class="size-5" /></span> Xu
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-grass-700 dark:text-grass-400">
            Giao dịch được
          </p>
          <p class="mt-3 text-sm text-muted">
            Tiền chính. Rơi từ quái (khoảng {{ K.tiLeRoiXu }}% số con), nhiệm vụ, phân rã đồ. Dùng để nâng cấp, mua thuốc, sửa đồ, bay, khảm ngọc, mua bán ở chợ.
          </p>
        </div>
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <span class="grid size-9 place-items-center rounded-full bg-pokeblue-400 text-white"><UIcon name="i-lucide-lock" class="size-5" /></span> Lượng khoá
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-pokered-600 dark:text-pokered-300">
            Không giao dịch được
          </p>
          <p class="mt-3 text-sm text-muted">
            Nhận từ nhiệm vụ, đăng nhập 7 ngày ({{ K.lichDangNhap.join(' · ') }} — tổng {{ tongDangNhap }}/tuần), thành tựu, sự kiện. Dùng mua bùa bảo hộ, vé, đồ tiện ích.
          </p>
        </div>
        <div class="rounded-3xl glass p-5">
          <p class="flex items-center gap-2 font-display text-xl font-extrabold text-highlighted">
            <span class="grid size-9 place-items-center rounded-full bg-pokered-500 text-white"><UIcon name="i-lucide-gem" class="size-5" /></span> Lượng
          </p>
          <p class="mt-1 text-xs font-bold uppercase tracking-wide text-grass-700 dark:text-grass-400">
            Giao dịch được · hiếm
          </p>
          <p class="mt-3 text-sm text-muted">
            Không mua được bằng tiền thật. Chỉ có từ bảng xếp hạng tuần, thành tựu và top sự kiện — phần thưởng cho người chơi giỏi.
          </p>
        </div>
      </div>
      <div class="bang-cuon mt-5 max-w-md">
        <table class="bang-wiki">
          <caption>Lượng thưởng theo bảng xếp hạng tuần</caption>
          <thead>
            <tr>
              <th>Hạng</th><th class="so">
                Lượng
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(x, i) in K.luongXepHangTuan" :key="x.top">
              <td>{{ i === 0 ? 'Hạng 1' : `Top ${x.top}` }}</td><td class="so font-bold text-highlighted">
                {{ x.luong }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="kiem" title="Kiếm tiền">
      <ul class="cham">
        <li><strong>Đánh quái gần cấp mình</strong> — nguồn xu chính, tỉ lệ thuận với cấp (xem cột "Xu / giờ" ở <NuxtLink to="/cam-nang/cap-do#bang-exp" class="lien-ket">bảng EXP</NuxtLink>). Chịu giới hạn <NuxtLink to="/cam-nang/cap-do#nang-luong" class="lien-ket">năng lượng huấn luyện</NuxtLink>.</li>
        <li><strong>Nhiệm vụ</strong> trả xu + EXP theo "giờ thu nhập" ở cấp của nhiệm vụ, nên luôn đáng làm dù bạn cấp nào.</li>
        <li><strong>Sự kiện</strong>: chỉ {{ K.suKienPhanXu }}% giá trị phần thưởng là xu; phần lớn là vật phẩm khoá, đồ trang trí, vật phẩm hết hạn.</li>
        <li><strong>Giftcode</strong>: tối đa {{ K.giftcodeToiDaThang }} mã/tháng, giá trị nhỏ — không có mã "phá game".</li>
      </ul>
    </WikiSection>

    <WikiSection id="tieu" title="Tiêu tiền" lead="Những 'hố hút' giữ cho xu không mất giá. Chi phí tính theo thu nhập ở cấp của bạn, nên người mới không bị đắt và người giàu vẫn có chỗ tiêu.">
      <ul class="cham">
        <li><strong>Nâng cấp trang bị</strong>: chi phí tăng theo bậc và mức (từ {{ soVn(T.chiPhiNangCap[0]?.[0] ?? 0) }} tới {{ soVn(T.chiPhiNangCap.at(-1)?.at(-1) ?? 0) }} xu/lần) — xem <NuxtLink to="/cam-nang/trang-bi#chi-phi" class="lien-ket">bảng chi phí</NuxtLink>.</li>
        <li><strong>Thuốc</strong> khoảng {{ K.tieuHaoPhanTram }}% và <strong>sửa đồ</strong> khoảng {{ K.suaDoPhanTram }}% thu nhập mỗi giờ chơi.</li>
        <li><strong>Bay (Pidgeot)</strong>: mỗi lần ~{{ K.dichChuyenPhanTramGio }}% thu nhập 1 giờ.</li>
        <li><strong>Ngọc hệ</strong>, học chiêu, đồ trang trí bằng xu (không chỉ số).</li>
      </ul>
    </WikiSection>

    <WikiSection id="cho" title="Chợ & giao dịch">
      <div class="grid gap-3 sm:grid-cols-3">
        <div class="the-so">
          <b>{{ K.thueCho }}%</b><span>thuế khi bán được hàng ở chợ</span>
        </div>
        <div class="the-so">
          <b>{{ K.phiDangBan }}%</b><span>phí đăng bán (mất kể cả khi không bán được)</span>
        </div>
        <div class="the-so">
          <b>{{ K.tranGiaoDichGio }} giờ</b><span>thu nhập: trần chuyển xu trực tiếp / ngày</span>
        </div>
      </div>
      <WikiCallout kieu="vi-sao">
        Giới hạn chuyển xu trực tiếp giúp chống mua bán xu bằng tiền thật và chống lừa đảo. Mua bán vật phẩm qua chợ vẫn tự do.
      </WikiCallout>
    </WikiSection>

    <WikiSection id="lam-phat" title="Vì sao giá cả ổn định?" lead="Mục tiêu: đồng xu bạn kiếm hôm nay vẫn có giá trị sau nhiều tháng chơi.">
      <ul class="cham">
        <li>Chi phí (nâng cấp, thuốc, bay, ngọc) <strong>tăng theo cấp</strong> giống như thu nhập — người mới không bị đắt, người chơi lâu năm vẫn có chỗ tiêu xu.</li>
        <li>Không có xu hay lượng "từ trên trời": không nạp tiền, không giftcode khổng lồ. Sự kiện thưởng chủ yếu vật phẩm khoá và đồ trang trí.</li>
        <li>Năng lượng huấn luyện giới hạn số giờ đánh quái hiệu quả mỗi ngày, nên treo máy cả ngày không làm xu mất giá.</li>
        <li>Đồ trang trí bằng xu là nơi để người chơi giàu tiêu tiền mà không ảnh hưởng sức mạnh của người khác.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
