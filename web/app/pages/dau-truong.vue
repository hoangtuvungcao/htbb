<script setup lang="ts">
/**
 * Đấu trường Bảo Bối 3v3 (D12): bảng TUẦN (điểm kiếm được tuần cúp này) + bảng MÙA (top 100). Dữ liệu: GET /api/dau-truong
 * (máy chủ game ghi tob_dau_truong_*). Luật chi tiết: /cam-nang/dau-truong. Logic thuần: shared/utils/dauTruong.ts.
 */
import { DAU_TRUONG } from '../../shared/utils/dauTruong'

useSeoMeta({
  title: 'Đấu trường Bảo Bối',
  description: 'Bảng xếp hạng tuần và mùa của Đấu trường Bảo Bối 3v3: đội 3 Pokémon từ Hộp PC, chuẩn hoá cấp 50, đấu đội phòng thủ của người khác theo lượt.',
})

interface DongMua { idChar: number, ten: string, diem: number, thang: number, thua: number, soTran: number, hang: number, bac: string }
interface DongTuan { idChar: number, ten: string, tran: number, thang: number, diemDoi: number, luong: number, hang: number }
interface KetQua { bayGio: string | null, mua: number, tuan: number, mo: string, dong: string, bangMua: DongMua[], bangTuan: DongTuan[], soNguoi: number }

const { data } = await useFetch<KetQua>('/api/dau-truong', { key: 'htbb-dau-truong' })
const tab = ref<'tuan' | 'mua'>('tuan')
const khoang = computed(() => data.value?.mo ? `${gioDep(msTuChuoi(data.value.mo))} → ${gioDep(msTuChuoi(data.value.dong))}` : '')
</script>

<template>
  <div>
    <SitePageBanner
      title="Đấu trường Bảo Bối"
      eyebrow="Đấu đội 3v3"
      :intro="`Ba Pokémon từ Hộp PC, chuẩn hoá cấp ${DAU_TRUONG.capChuanHoa}: chỉ số cá thể, tính cách và bộ chiêu mới là thứ quyết định. Đấu đội phòng thủ của người khác bất cứ lúc nào, ${DAU_TRUONG.luotNgay} trận mỗi ngày.`"
    />
    <section class="relative py-12 sm:py-16" aria-labelledby="dt-bang">
      <FxGlowOrbs />
      <UContainer class="relative max-w-5xl space-y-8">
        <div class="glass rounded-2xl p-6">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <h2 id="dt-bang" class="font-display text-2xl font-bold text-highlighted">
              {{ tab === 'tuan' ? 'Bảng tuần này' : `Bảng mùa ${(data?.mua ?? 0) + 1}` }}
            </h2>
            <div class="flex gap-2" role="tablist">
              <UButton :variant="tab === 'tuan' ? 'solid' : 'soft'" role="tab" :aria-selected="tab === 'tuan'" @click="tab = 'tuan'">
                Tuần
              </UButton>
              <UButton :variant="tab === 'mua' ? 'solid' : 'soft'" role="tab" :aria-selected="tab === 'mua'" @click="tab = 'mua'">
                Mùa
              </UButton>
            </div>
          </div>
          <p v-if="tab === 'tuan' && khoang" class="mt-1 text-sm text-muted">
            {{ khoang }} (giờ Việt Nam) · xếp theo điểm mùa kiếm được trong tuần
          </p>
          <p v-else-if="tab === 'mua'" class="mt-1 text-sm text-muted">
            {{ data?.soNguoi ?? 0 }} huấn luyện viên đã đấu mùa này · cuối mùa trao danh hiệu cho người đủ {{ DAU_TRUONG.tranToiThieuMua }} trận
          </p>

          <div v-if="tab === 'tuan'" class="mt-4 overflow-x-auto">
            <p v-if="!data?.bangTuan?.length" class="text-sm text-muted">
              Tuần này chưa có trận nào.
            </p>
            <table v-else class="w-full text-left text-sm">
              <thead class="text-xs uppercase text-muted">
                <tr>
                  <th class="py-2 pr-3">#</th>
                  <th class="py-2 pr-3">Huấn luyện viên</th>
                  <th class="py-2 pr-3 text-right">Điểm tuần</th>
                  <th class="py-2 text-right">Thắng / trận</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="d in data.bangTuan" :key="d.idChar" class="border-t border-default">
                  <td class="py-2 pr-3 font-semibold">{{ d.hang }}</td>
                  <td class="py-2 pr-3 break-words">{{ d.ten }}</td>
                  <td class="py-2 pr-3 text-right font-semibold">{{ d.diemDoi > 0 ? '+' : '' }}{{ d.diemDoi }}</td>
                  <td class="py-2 text-right text-muted">{{ d.thang }} / {{ d.tran }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div v-else class="mt-4 overflow-x-auto">
            <p v-if="!data?.bangMua?.length" class="text-sm text-muted">
              Mùa này chưa có ai đấu.
            </p>
            <table v-else class="w-full text-left text-sm">
              <thead class="text-xs uppercase text-muted">
                <tr>
                  <th class="py-2 pr-3">#</th>
                  <th class="py-2 pr-3">Huấn luyện viên</th>
                  <th class="py-2 pr-3 text-right">Điểm</th>
                  <th class="py-2 pr-3">Bậc</th>
                  <th class="py-2 text-right">Thắng – thua</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="d in data.bangMua" :key="d.idChar" class="border-t border-default">
                  <td class="py-2 pr-3 font-semibold">{{ d.hang }}</td>
                  <td class="py-2 pr-3 break-words">{{ d.ten }}</td>
                  <td class="py-2 pr-3 text-right font-semibold">{{ d.diem }}</td>
                  <td class="py-2 pr-3 text-muted">{{ d.bac }}</td>
                  <td class="py-2 text-right text-muted">{{ d.thang }} – {{ d.thua }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="glass rounded-2xl p-6">
          <h2 class="font-display text-xl font-bold text-highlighted">
            Tóm tắt luật
          </h2>
          <ul class="cham mt-3 text-sm text-muted">
            <li>Vào: NPC Đấu trường ở thị trấn → "Đấu trường Bảo Bối" (cấp {{ DAU_TRUONG.capToiThieu }}+, bản game mới).</li>
            <li>Mỗi ngày {{ DAU_TRUONG.luotNgay }} trận tấn công; mỗi lượt chọn chiêu trong {{ DAU_TRUONG.giayLuot }} giây, hết giờ máy chọn giúp.</li>
            <li>Thắng: +{{ DAU_TRUONG.luongThang }} lượng khoá, tối đa {{ DAU_TRUONG.tranLuongTuan }} mỗi tuần. Không có xu.</li>
            <li>Không ghép tài khoản cùng thiết bị / mạng; gặp lại cùng đối thủ trong 24 giờ thì điểm giảm dần ×1 → ×1/2 → ×1/4 → 0.</li>
          </ul>
          <p class="mt-3 text-sm">
            Chi tiết công thức, chiêu và trạng thái: <NuxtLink to="/cam-nang/dau-truong" class="lien-ket">Cẩm nang Đấu trường Bảo Bối</NuxtLink>.
          </p>
        </div>
      </UContainer>
    </section>
  </div>
</template>
