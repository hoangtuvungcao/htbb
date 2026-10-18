<script setup lang="ts">
/**
 * Boss & đột kích — boss từ pokedex.json (bang_quai.csv), lịch đột kích từ su_kien_nam.csv,
 * boss thế giới theo tuần từ du_lieu/web/boss_the_gioi.csv + giờ triệu hồi trong LauDaiJob.java.
 */
const boss = POKEDEX.loai.filter(l => l.vaiTro.includes('boss')).map((l) => {
  const q = l.quai.find(x => x.boss) ?? l.quai[0]
  return { l, cap: q && q.cap > 1 ? q.cap : null, hp: q && q.hp > 1 ? q.hp : null, noi: q ? (q.noi.map(n => n.ten).join(', ') || q.ghiChuNoi) : '' }
}).sort((a, b) => (a.cap ?? 999) - (b.cap ?? 999))
const dotKich = CAM_NANG.suKien.filter(s => s.loai === 'dot_kich')
const BTG = CAM_NANG_ND.bossTheGioi
/** Tuần ISO (game chọn boss theo SuKienLauDai.tuanIso). */
function tuanIso(d: Date): number {
  const t = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()))
  const thu = t.getUTCDay() || 7
  t.setUTCDate(t.getUTCDate() + 4 - thu)
  return Math.ceil(((t.getTime() - Date.UTC(t.getUTCFullYear(), 0, 1)) / 86400000 + 1) / 7)
}
const tuanNay = Math.min(52, tuanIso(new Date()))
const bossTuan = computed(() => [0, 1, 2, 3].map(i => BTG.lich.find(b => b.tuan === ((tuanNay - 1 + i) % 52) + 1)).filter(Boolean) as typeof BTG.lich)
const heTheoTen = new Map(POKEDEX.he.map(h => [h.ten.toLocaleLowerCase('vi'), h.id]))
const loaiTheoTen = new Map(POKEDEX.loai.map(l => [l.ten.toLocaleLowerCase('vi'), l]))
const fmtHp = (n: number) => n >= 1e6 ? `${soVn(n / 1e6, 1)} triệu` : soVn(n)
</script>

<template>
  <WikiShell slug="boss-dot-kich">
    <WikiSection id="boss" title="Danh sách boss" lead="Boss là Pokémon huyền thoại hoặc cỡ lớn, hiển thị to hơn quái thường và hồi sinh sau một khoảng thời gian. Ai gây sát thương cũng có phần thưởng rơi ra.">
      <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="b in boss" :key="b.l.dex">
          <NuxtLink :to="`/pokedex/${b.l.dex}`" class="group flex h-full items-center gap-3 overflow-hidden rounded-2xl glass pr-3 transition hover:-translate-y-0.5">
            <PokeSan :dex="b.l.dex" :he="b.l.he" :size="96" class="w-28 shrink-0" />
            <span class="min-w-0 py-2">
              <span class="block break-words font-display text-base font-bold text-highlighted group-hover:text-primary">{{ b.l.ten }}</span>
              <span class="mt-0.5 flex flex-wrap gap-1"><PokeHeBadge v-for="h in b.l.he" :key="h" :he="h" size="sm" /></span>
              <span class="mt-1 block text-xs text-muted">{{ b.cap ? `Cấp ${b.cap}` : 'Cấp theo sự kiện' }}{{ b.hp ? ` · ${fmtHp(b.hp)} HP` : '' }}</span>
              <span class="line-clamp-2 block text-xs text-dimmed" :title="b.noi">{{ b.noi }}</span>
            </span>
          </NuxtLink>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="boss-the-gioi" title="Boss thế giới hằng tuần" :lead="`Mỗi thứ Bảy lúc ${BTG.gioThuBay || '20:00'}, một boss thế giới xuất hiện cho cả máy chủ. Thưởng chia theo mức đóng góp sát thương; hạng cao nhận hộp vàng, người tham gia nhận hộp tham gia.`">
      <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
        <li v-for="(b, i) in bossTuan" :key="b.tuan" class="rounded-2xl glass p-4" :class="i === 0 ? 'ring-2 ring-primary' : ''">
          <p class="text-xs font-bold uppercase tracking-wide text-dimmed">
            {{ i === 0 ? 'Tuần này' : `Tuần ${b.tuan}` }}
          </p>
          <NuxtLink v-if="loaiTheoTen.get(b.nam1.toLocaleLowerCase('vi'))" :to="`/pokedex/${loaiTheoTen.get(b.nam1.toLocaleLowerCase('vi'))!.dex}`" class="mt-1 flex items-center gap-2 font-display text-lg font-bold text-highlighted hover:text-primary">
            <PokeAvatar :dex="loaiTheoTen.get(b.nam1.toLocaleLowerCase('vi'))!.dex" class="size-8" /> {{ b.nam1 }}
          </NuxtLink>
          <p v-else class="mt-1 font-display text-lg font-bold text-highlighted">
            {{ b.nam1 }}
          </p>
          <PokeHeBadge v-if="heTheoTen.get(b.he.toLocaleLowerCase('vi')) != null" :he="heTheoTen.get(b.he.toLocaleLowerCase('vi'))!" size="sm" class="mt-2" />
        </li>
      </ul>
      <ul class="cham mt-4">
        <li>Boss nổi giận sau {{ BTG.phutNoiGian }} phút — dồn sát thương sớm.</li>
        <li>Trong các tuần đột kích, boss thế giới xuất hiện thêm mỗi ngày lúc {{ BTG.gioDotKich.join(' và ') }}.</li>
        <li>Năm thứ hai có danh sách boss riêng ở một số tuần.</li>
      </ul>
    </WikiSection>

    <WikiSection id="dot-kich" title="Đột kích huyền thoại" :lead="`${dotKich.length} tuần đột kích mỗi năm: boss thế giới xuất hiện thêm theo giờ hẹn mỗi ngày.`">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th class="so">
                Tuần
              </th><th>Năm 1</th><th>Năm 2</th><th>Cách chơi</th><th>Phần thưởng</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in dotKich" :key="s.tuan">
              <td class="so">
                {{ s.tuan }}
              </td>
              <td class="font-semibold text-highlighted">
                {{ s.ten1 }}
              </td>
              <td>{{ s.ten2 }}</td>
              <td>{{ s.coChe.startsWith('Như tuần') ? dotKich[0]?.coChe : s.coChe }}</td>
              <td>{{ s.phanThuong }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <p class="mt-3 text-sm text-muted">
        Nguyên liệu đột kích là vật phẩm <strong>khoá</strong> (không bán được), dùng để chế tạo và nâng cấp.
      </p>
    </WikiSection>

    <WikiSection id="chuan-bi" title="Chuẩn bị">
      <ul class="cham">
        <li>Xem hệ của boss trên trang Pokédex và mang Pokémon/chiêu <NuxtLink to="/cam-nang/he-khac-che" class="lien-ket">khắc hệ</NuxtLink>.</li>
        <li>Mang đủ thuốc hồi máu và Ether; boss đánh lan nên đứng tản ra.</li>
        <li>Đội cần ít nhất một Pokémon chịu đòn và một Pokémon có chiêu hồi máu/hồi sinh (Eevee có Phước lành hồi sinh).</li>
        <li>Đến sớm vài phút trước giờ hẹn — boss thế giới xuất hiện cùng lúc cho cả máy chủ.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
