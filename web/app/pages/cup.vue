<script setup lang="ts">
/**
 * Cúp PvP tuần (0.9.7): cúp tuần này (xoay vòng mỗi thứ Hai 04:00), luật, lịch thi đấu, bảng điểm Thuỵ Sĩ và nhà vô
 * địch các tuần trước. Dữ liệu: GET /api/cup (máy chủ game ghi tob_cup_dang_ky + tob_su_kien_lich). Tin "Sắp / Đang /
 * Đã kết thúc" của cúp tự đăng ở chuyên mục Sự kiện (server/plugins/tin-su-kien.ts).
 */

useSeoMeta({
  title: 'Cúp PvP tuần',
  description: 'Cúp PvP xoay vòng mỗi tuần của Huyền Thoại Bảo Bối: Cúp Nhí, Cúp hệ, Cúp Đôi, Cúp Không trang bị — đăng ký trong game, Chủ nhật 20:00 thi đấu Thuỵ Sĩ tự ghép cặp.',
})

interface Doi { doiId: number, ten: string, cap: number, diem: number, thang: number, hoa: number, thua: number, hang: number }
interface VoDich { tuan: number, maLoai: string, ten: string, diem: number, mo: string }
interface KetQua { bayGio: string | null, tuan: number, mo: string, dong: string, cup: { ten: string, moTa: string, phanThuong: string } | null, bang: Doi[], soDoi: number, voDich: VoDich[] }

const { data, pending } = await useFetch<KetQua>('/api/cup', { key: 'htbb-cup' })
const TEN_LOAI: Record<string, string> = {
  nhi: 'Cúp Nhí', he_lua: 'Cúp hệ Lửa', he_nuoc: 'Cúp hệ Nước', he_co: 'Cúp hệ Cỏ', he_dien: 'Cúp hệ Điện', doi: 'Cúp Đôi', thuan: 'Cúp Không trang bị',
}
const khoang = computed(() => data.value?.mo ? `${gioDep(msTuChuoi(data.value.mo))} → ${gioDep(msTuChuoi(data.value.dong))}` : '')
</script>

<template>
  <div>
    <SitePageBanner
      title="Cúp PvP tuần"
      eyebrow="Đấu trường xoay vòng"
      intro="Mỗi tuần một thể thức mới. Chỉ số chuẩn hoá theo cấp, tự ghép cặp cùng điểm, thưởng danh hiệu có hạn — không ai mạnh lên nhờ thắng."
    />
    <section class="relative py-12 sm:py-16" aria-labelledby="cup-tuan-nay">
      <FxGlowOrbs />
      <UContainer class="relative max-w-5xl space-y-8">
        <div class="glass rounded-2xl p-6">
          <h2 id="cup-tuan-nay" class="font-display text-2xl font-bold text-highlighted">
            {{ data?.cup?.ten ?? 'Cúp tuần này' }}
          </h2>
          <p v-if="khoang" class="mt-1 text-sm text-muted">
            {{ khoang }} (giờ Việt Nam)
          </p>
          <p v-if="data?.cup?.moTa" class="mt-3 text-sm leading-relaxed">
            {{ data.cup.moTa }}
          </p>
          <p v-else-if="!pending" class="mt-3 text-sm text-muted">
            Tuần này chưa có lịch cúp (hoặc đang tạm đóng).
          </p>
          <p v-if="data?.cup?.phanThuong" class="mt-3 text-sm">
            <span class="font-semibold text-highlighted">Phần thưởng:</span> {{ data.cup.phanThuong }}
          </p>
          <ul class="cham mt-4 text-sm text-muted">
            <li>Đăng ký trong game: Sổ tay huấn luyện → Cúp PvP tuần (Cúp Đôi: đội trưởng tổ đội 2 người đăng ký cả đội).</li>
            <li>Mỗi thiết bị / tài khoản chỉ dự bằng một Pokémon mỗi tuần; hai bên cùng thiết bị / mạng không bao giờ gặp nhau.</li>
            <li>Trận cúp không làm mất EXP, không tính điểm PK hay thang PvP mùa. Vắng mặt lúc ghép vòng thì không được ghép.</li>
          </ul>
        </div>

        <div class="glass rounded-2xl p-6">
          <h2 class="font-display text-xl font-bold text-highlighted">
            Bảng điểm tuần này
            <span class="text-sm font-normal text-muted">({{ data?.soDoi ?? 0 }} đội)</span>
          </h2>
          <p v-if="!data?.bang?.length" class="mt-3 text-sm text-muted">
            Chưa có ai đăng ký.
          </p>
          <div v-else class="mt-4 overflow-x-auto">
            <table class="w-full text-left text-sm">
              <thead class="text-xs uppercase text-muted">
                <tr>
                  <th class="py-2 pr-3">
                    #
                  </th>
                  <th class="py-2 pr-3">
                    Pokémon
                  </th>
                  <th class="py-2 pr-3 text-right">
                    Điểm
                  </th>
                  <th class="py-2 text-right">
                    Thắng - hoà - thua
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(d, i) in data.bang" :key="d.doiId" class="border-t border-default">
                  <td class="py-2 pr-3 font-semibold">
                    {{ d.hang > 0 ? d.hang : i + 1 }}
                  </td>
                  <td class="py-2 pr-3 break-words">
                    {{ d.ten }} <span class="text-xs text-muted">cấp {{ d.cap }}</span>
                  </td>
                  <td class="py-2 pr-3 text-right font-semibold">
                    {{ d.diem }}
                  </td>
                  <td class="py-2 text-right text-muted">
                    {{ d.thang }} - {{ d.hoa }} - {{ d.thua }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div v-if="data?.voDich?.length" class="glass rounded-2xl p-6">
          <h2 class="font-display text-xl font-bold text-highlighted">
            Nhà vô địch các tuần trước
          </h2>
          <ul class="mt-4 grid gap-3 sm:grid-cols-2">
            <li v-for="v in data.voDich" :key="v.tuan" class="rounded-xl bg-elevated p-3">
              <p class="text-xs text-muted">
                {{ TEN_LOAI[v.maLoai] ?? v.maLoai }} · {{ gioDep(msTuChuoi(v.mo)) }}
              </p>
              <p class="font-semibold text-highlighted break-words">
                {{ v.ten }} <span class="text-sm font-normal text-muted">{{ v.diem }} điểm</span>
              </p>
            </li>
          </ul>
        </div>
      </UContainer>
    </section>
  </div>
</template>
