<script setup lang="ts">
/**
 * Bản đồ thế giới — danh sách bản đồ từ thiet_ke_map.csv, khu luyện cấp + quái từ pokedex.json.
 * Ảnh xem trước (public/ban-do/<id>.webp, đã thu nhỏ) chỉ có khi chạy `dong-bo-cam-nang.mjs --anh-map`.
 * Bản đồ Hoenn (năm 2) lấy tên + quái từ BanDoHoenn.java.
 */
const LOAI_MAP: Record<string, { ten: string, icon: string, mau: string }> = {
  thi_tran: { ten: 'Thị trấn', icon: 'i-lucide-house', mau: '#e3350d' },
  duong: { ten: 'Tuyến đường', icon: 'i-lucide-route', mau: '#b8a038' },
  dong_co: { ten: 'Đồng cỏ', icon: 'i-lucide-sprout', mau: '#5aad34' },
  rung: { ten: 'Rừng', icon: 'i-lucide-trees', mau: '#366a21' },
  ho: { ten: 'Hồ', icon: 'i-lucide-waves', mau: '#3d7dca' },
  bien: { ten: 'Biển', icon: 'i-lucide-sailboat', mau: '#2c64ad' },
  nui: { ten: 'Núi', icon: 'i-lucide-mountain', mau: '#705848' },
  hang: { ten: 'Hang động', icon: 'i-lucide-tent', mau: '#3a4d6b' },
  hang_cat: { ten: 'Hang cát', icon: 'i-lucide-tent', mau: '#e0c068' },
  sa_mac: { ten: 'Sa mạc', icon: 'i-lucide-sun', mau: '#c8a038' },
  tuyet: { ten: 'Vùng tuyết', icon: 'i-lucide-snowflake', mau: '#5fa8c8' },
  trong_nha: { ten: 'Trong nhà', icon: 'i-lucide-door-open', mau: '#a06104' },
  thanh: { ten: 'Thành / lãnh địa', icon: 'i-lucide-castle', mau: '#7038f8' },
  dau_truong: { ten: 'Đấu trường', icon: 'i-lucide-swords', mau: '#c03028' },
  tau: { ten: 'Tàu', icon: 'i-lucide-ship', mau: '#5f98e2' },
}
const capVung = (v: string) => {
  const ds = CAM_NANG.nhiemVu.cung.filter(c => c.vung === v)
  return ds.length ? `Cấp ${Math.min(...ds.map(c => c.capTu))}–${Math.max(...ds.map(c => c.capDen))} của cốt truyện.` : ''
}
const capHoenn = Math.min(...CAM_NANG_ND.cotTruyen.chuong.filter(c => c.vung === 'Hoenn').map(c => c.cap))
const VUNG = [
  { ten: 'Kanto', mo: `Vùng khởi đầu: đồng cỏ Pallet, Rừng Viridian, Núi Mặt Trăng, tàu S.S. Anne và đảo lửa Cinnabar. ${capVung('Kanto')}` },
  { ten: 'Johto', mo: `Vùng cổ kính: tháp chuông Ecruteak, Rừng Ilex, Hồ Phẫn Nộ, làng rồng Blackthorn và Đường Victory dẫn tới Núi Silver. ${capVung('Johto')}` },
  { ten: 'Hoenn', mo: `Vùng biển nhiệt đới mở trong năm thứ hai (${POKEDEX.hoenn.soBanDo} bản đồ): rừng Petalburg, núi lửa Chimney, Thác Meteor, Tháp Trời. Vào qua cổng bắc Cinnabar (cần cấp ${POKEDEX.hoenn.capVao}+); cốt truyện Hoenn từ cấp ${capHoenn}.` },
]
const thiTran = CAM_NANG.nhiemVu.phu
const luyenCap = [...POKEDEX.map].sort((a, b) => a.capThap - b.capThap || a.id - b.id)
const L = (dex: number) => loaiTheoDex(dex)

const loaiChon = ref<string | null>(null)
const ngoaiTroi = CAM_NANG.banDo.filter(m => m.loai !== 'trong_nha')
const trongNha = CAM_NANG.banDo.filter(m => m.loai === 'trong_nha')
const dsMap = computed(() => ngoaiTroi.filter(m => !loaiChon.value || m.loai === loaiChon.value))
const capCuaMap = (id: number) => POKEDEX.map.find(m => m.id === id)
const coAnh = CAM_NANG.banDo.some(m => m.anh)
</script>

<template>
  <WikiShell slug="ban-do">
    <WikiSection id="vung" title="Các vùng">
      <div class="grid gap-4 md:grid-cols-3">
        <div v-for="v in VUNG" :key="v.ten" class="rounded-3xl glass p-5">
          <span class="route-sign">{{ v.ten }}</span>
          <p class="mt-3 text-sm text-muted">
            {{ v.mo }}
          </p>
          <p class="mt-3 flex flex-wrap gap-1.5">
            <UBadge v-for="t in thiTran.filter(x => x.vung === v.ten)" :key="t.ten" color="neutral" variant="soft" size="sm">
              {{ t.ten.replace(/^Thị trấn\s+/, '') }}
            </UBadge>
          </p>
        </div>
      </div>
      <ul class="cham mt-5">
        <li>Di chuyển nhanh: nhờ <strong>Pidgeot</strong> (Pidgeot (Bay)) ở thị trấn đưa tới bản đồ khác — mỗi lần tốn xu bằng {{ CAM_NANG.kinhTe.dichChuyenPhanTramGio }}% thu nhập một giờ ở cấp của bạn.</li>
        <li>Ra đảo: gặp <strong>Lapras</strong> (hoa tiêu) ở bến tàu mua vé và đi thuyền.</li>
        <li>NPC trong thị trấn: Chansey bán thuốc, Kecleon tạp hoá, Bronzong bán trang bị, Machamp rèn/sửa/chế tạo, Kangaskhan giữ đồ — giá xem ở <NuxtLink to="/cam-nang/cua-hang" class="lien-ket">Cửa hàng</NuxtLink>.</li>
        <li>Muốn câu cá? Chỉ câu được ở hồ — xem <NuxtLink to="/cam-nang/cau-ca#ban-do" class="lien-ket">bản đồ có hồ</NuxtLink>.</li>
      </ul>
    </WikiSection>

    <WikiSection id="luyen-cap" title="Khu luyện cấp" lead="Pokémon hoang dã theo khu vực, xếp theo cấp. Chọn nơi có quái gần cấp bạn để lên cấp nhanh, rơi đá và an toàn.">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Khu vực</th><th class="so">
                Cấp quái
              </th><th>Pokémon hoang dã</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in luyenCap" :key="m.id">
              <td class="whitespace-nowrap font-semibold text-highlighted">
                {{ m.ten }}
              </td>
              <td class="so whitespace-nowrap">
                {{ m.capThap === m.capCao ? m.capThap : `${m.capThap}–${m.capCao}` }}
              </td>
              <td>
                <ul class="flex flex-wrap gap-1" :aria-label="`Pokémon ở ${m.ten}`">
                  <li v-for="d in m.dex" :key="d">
                    <NuxtLink :to="`/pokedex/${d}`" class="flex items-center gap-1 rounded-full bg-elevated py-0.5 pl-0.5 pr-2 text-xs font-semibold hover:text-primary">
                      <PokeAvatar :dex="d" class="size-5" /> {{ L(d)?.ten }}
                    </NuxtLink>
                  </li>
                </ul>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="danh-sach" title="Mọi bản đồ" :lead="`${ngoaiTroi.length} bản đồ ngoài trời và ${trongNha.length} khu trong nhà (Trung tâm Pokémon, Poké Mart, nhà kho, xưởng, cửa hàng).`">
      <div class="flex flex-wrap gap-1.5">
        <button
          v-for="(v, k) in LOAI_MAP"
          v-show="k !== 'trong_nha' && ngoaiTroi.some(m => m.loai === k)"
          :key="k"
          type="button"
          class="flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold transition"
          :class="loaiChon === k ? 'border-[var(--poke-outline)] bg-elevated text-highlighted' : 'border-default text-toned hover:bg-elevated'"
          :aria-pressed="loaiChon === k"
          @click="loaiChon = loaiChon === k ? null : k"
        >
          <UIcon :name="v.icon" class="size-3.5" :style="{ color: v.mau }" /> {{ v.ten }}
        </button>
      </div>
      <ul class="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
        <li v-for="m in dsMap" :key="m.id" class="overflow-hidden rounded-2xl glass">
          <img v-if="m.anh" :src="m.anh" :width="m.rong || 640" :height="m.cao || 480" :alt="`Xem trước bản đồ ${m.ten}`" loading="lazy" decoding="async" class="aspect-[4/3] w-full bg-elevated object-cover">
          <div v-else class="grid aspect-[4/3] place-items-center" :style="{ background: `linear-gradient(160deg, color-mix(in oklab, ${LOAI_MAP[m.loai]?.mau} 22%, var(--poke-card)), color-mix(in oklab, ${LOAI_MAP[m.loai]?.mau} 8%, var(--poke-card)))` }">
            <UIcon :name="LOAI_MAP[m.loai]?.icon ?? 'i-lucide-map'" class="size-10 opacity-70" :style="{ color: LOAI_MAP[m.loai]?.mau }" />
          </div>
          <div class="p-3">
            <p class="truncate font-display font-bold text-highlighted" :title="m.ten">
              {{ m.ten }}
            </p>
            <p class="text-xs text-muted">
              {{ LOAI_MAP[m.loai]?.ten }}<template v-if="m.vung">
                · {{ m.vung }}
              </template><template v-if="capCuaMap(m.id)">
                · cấp {{ capCuaMap(m.id)!.capThap }}–{{ capCuaMap(m.id)!.capCao }}
              </template>
            </p>
          </div>
        </li>
      </ul>
      <p v-if="!coAnh" class="mt-3 text-xs text-dimmed">
        Ảnh xem trước bản đồ đang được vẽ lại theo phong cách mới và sẽ cập nhật sau.
      </p>
    </WikiSection>
  </WikiShell>
</template>
