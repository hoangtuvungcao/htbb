<script setup lang="ts">
/**
 * Trang phục & trang trí (0.9.8): ngân sách chỉ số theo ô + danh mục trang phục, hào quang, skin thú cưỡi, thẻ bạn
 * đồng hành. Dữ liệu: /api/do-trang-phuc (CSDL, rơi về shared/data/danh-muc-do-trang-phuc.json).
 */
import macDinh from '~~/shared/data/danh-muc-do-trang-phuc.json'
import { TEN_DO_HIEM, duongIcon, moTaNguon, phanTram, tongNganSach } from '~~/shared/utils/doTrangPhuc.mjs'

const { data } = await useFetch('/api/do-trang-phuc', { key: 'do-trang-phuc', default: () => macDinh })
const dm = computed(() => data.value ?? macDinh)

/** Màu độ hiếm hình (khớp khung độ hiếm trong game). */
const MAU_HIEM = ['#8a94a6', '#2f9a3c', '#2a6fd0', '#8a3cd8', '#e07010']
const TEN_NHOM_NGUON: Record<string, string> = { mien_phi: 'Miễn phí', su_kien: 'Sự kiện', thanh_tuu: 'Thành tựu', cua_hang: 'Cửa hàng', che_tao: 'Chế tạo' }
const MAU_NGUON: Record<string, 'success' | 'info' | 'warning' | 'primary' | 'neutral'> = { mien_phi: 'success', su_kien: 'info', thanh_tuu: 'warning', cua_hang: 'primary', che_tao: 'neutral' }

const bangNganSach = computed(() => [
  ...dm.value.oTrangTri.map(o => ({ ten: o.ten, pn: o.phanNghin, dong: o.dong.map(d => d.ten).join(', ') })),
  { ten: 'Danh hiệu', pn: dm.value.nganSachDanhHieu, dong: 'theo danh hiệu đang đeo' },
])
const tong = computed(() => tongNganSach(dm.value))
const theoO = computed(() => dm.value.oTrangTri.map(o => ({
  ...o,
  ds: dm.value.trangTri.filter(t => t.o === o.o),
})))
</script>

<template>
  <WikiShell slug="trang-phuc">
    <p class="van">
      {{ dm.trangTri.length }} món trang trí: trang phục hiện thẳng trên Pokémon của bạn (mũ, khăn, áo choàng, cánh, đồ cầm tay),
      hào quang dưới chân, skin thú cưỡi và thẻ cho bạn đồng hành. Mặc, thử trước và chọn dòng chỉ số ở
      <strong>Hồ sơ → tab Ngoại hình</strong> (Tủ đồ trang trí) — đây là chỗ duy nhất mặc / tháo đồ trang trí.
    </p>

    <WikiCallout kieu="luu-y" title="Món của cửa hàng lượng cũ đã vào Tủ đồ (0.9.8)">
      Skin Mũ rơm, Khăn quàng mùa đông, Kính râm, Nơ xinh, Hào quang Lửa / Nước và Skin thú cưỡi Vân mây / Cầu vồng mua trước
      đây nay là món Tủ đồ cùng ô (Trang phục Mũ rơm…, Hào quang Lửa thiêng / Sóng nước, Yên vân mây / Yên cầu vồng nhạt): bạn có
      ngay trong Tủ đồ, không mất gì. Màu tên, khung tên, biểu cảm và trang trí nhà không còn công dụng nên ngừng bán; ai đã
      có vẫn giữ.
    </WikiCallout>

    <WikiCallout kieu="vi-sao" title="Lượng chỉ mua hình, chỉ số theo ô">
      Món nào cũng có chút chỉ số, nhưng chỉ số gắn vào <strong>ô</strong> chứ không gắn vào món: món miễn phí, món sự kiện, món
      thành tựu hay món mua bằng lượng ở cùng ô cho đúng một mức, và bạn tự chọn dòng chỉ số ở Tủ đồ (đổi miễn phí 1 lần mỗi ngày).
      Ô nào cũng có món miễn phí sớm, sở hữu nhiều không cộng dồn, không nâng cấp / khảm / tẩy được.
    </WikiCallout>

    <WikiSection id="ngan-sach" title="Ngân sách chỉ số" :lead="`Tổng trang trí tối đa +${phanTram(dm.tranTong)}% (kẹp cứng) gồm cả danh hiệu và dòng chỉ số của thẻ đặt tên Pokémon: danh hiệu và các ô Tủ đồ tính trước, thẻ đặt tên lấy phần còn lại. Tất cả nằm trong trần chỉ số chung của mọi nguồn.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Ô</th><th class="so">
                Chỉ số
              </th><th>Dòng chọn được</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in bangNganSach" :key="r.ten">
              <td class="font-semibold text-highlighted">
                {{ r.ten }}
              </td>
              <td class="so">
                +{{ phanTram(r.pn) }}%
              </td>
              <td class="text-sm">
                {{ r.dong }}
              </td>
            </tr>
            <tr>
              <td class="font-bold">
                Tổng
              </td>
              <td class="so font-bold">
                +{{ phanTram(tong) }}%
              </td>
              <td class="text-sm text-muted">
                trần {{ phanTram(dm.tranTong) }}%
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection v-for="o in theoO" :id="o.o.replace(/_/g, '-')" :key="o.o" :title="o.ten" :lead="`${o.ds.length} món · +${phanTram(o.phanNghin)}% khi đang mặc một món bất kỳ ở ô này.`">
      <ul class="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <li v-for="t in o.ds" :key="t.ma" class="the-go flex gap-3 p-3">
          <span class="grid size-14 shrink-0 place-items-center rounded-2xl bg-screen ring-2" :style="{ '--tw-ring-color': MAU_HIEM[t.doHiem] ?? MAU_HIEM[0] }">
            <img :src="duongIcon(t.icon)" width="16" height="16" :alt="t.ten" loading="lazy" class="pixelated size-12">
          </span>
          <span class="min-w-0">
            <strong class="block font-display text-highlighted" :style="{ color: t.doHiem > 0 ? MAU_HIEM[t.doHiem] : undefined }">{{ t.ten }}</strong>
            <span class="mt-0.5 flex flex-wrap items-center gap-1">
              <UBadge :color="MAU_NGUON[t.nguon] ?? 'neutral'" variant="subtle" size="sm">{{ TEN_NHOM_NGUON[t.nguon] ?? t.nguon }}</UBadge>
              <span class="text-xs text-dimmed">{{ TEN_DO_HIEM[t.doHiem] }}</span>
            </span>
            <span class="mt-1 block text-xs text-muted">{{ moTaNguon(t) }}</span>
            <span v-if="t.moTa" class="block text-xs text-dimmed">{{ t.moTa }}</span>
          </span>
        </li>
      </ul>
    </WikiSection>

    <WikiCallout kieu="meo" title="Lịch sự kiện">
      Trang phục sự kiện mở theo lịch năm (Tết, Valentine, Hè, Trung Thu, Halloween, Giáng sinh…): trong thời gian sự kiện, hạ đủ
      Pokémon hoang dã là nhận được, năm sau quay lại. Xem <NuxtLink to="/cam-nang/su-kien" class="lien-ket">Sự kiện</NuxtLink>.
    </WikiCallout>
  </WikiShell>
</template>
