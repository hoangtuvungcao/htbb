<script setup lang="ts">
/** Boss & đột kích — boss từ pokedex.json (bang_quai.csv), lịch đột kích từ su_kien_nam.csv. */
const boss = POKEDEX.loai.filter(l => l.vaiTro.includes('boss')).map((l) => {
  const q = l.quai.find(x => x.boss) ?? l.quai[0]
  return { l, cap: q && q.cap > 1 ? q.cap : null, hp: q && q.hp > 1 ? q.hp : null, noi: q ? (q.noi.map(n => n.ten).join(', ') || q.ghiChuNoi) : '' }
}).sort((a, b) => (a.cap ?? 999) - (b.cap ?? 999))
const dotKich = CAM_NANG.suKien.filter(s => s.loai === 'dot_kich')
const fmtHp = (n: number) => n >= 1e6 ? `${soVn(n / 1e6, 1)} triệu` : soVn(n)
</script>

<template>
  <WikiShell slug="boss-dot-kich">
    <WikiSection id="boss" title="Danh sách boss" lead="Boss là Pokémon huyền thoại hoặc cỡ lớn (hiển thị to gấp 1,5–2 lần), hồi sinh sau 1–24 giờ. Ai gây sát thương cũng có phần thưởng rơi ra.">
      <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        <li v-for="b in boss" :key="b.l.dex">
          <NuxtLink :to="`/pokedex/${b.l.dex}`" class="group flex items-center gap-3 overflow-hidden rounded-2xl glass pr-3 transition hover:-translate-y-0.5">
            <PokeSan :dex="b.l.dex" :he="b.l.he" :size="96" class="w-28 shrink-0" />
            <span class="min-w-0 py-2">
              <span class="block font-display text-lg font-bold text-highlighted group-hover:text-primary">{{ b.l.ten }}</span>
              <span class="mt-0.5 flex flex-wrap gap-1"><PokeHeBadge v-for="h in b.l.he" :key="h" :he="h" size="sm" /></span>
              <span class="mt-1 block text-xs text-muted">{{ b.cap ? `Cấp ${b.cap}` : 'Cấp theo sự kiện' }}{{ b.hp ? ` · ${fmtHp(b.hp)} HP` : '' }}</span>
              <span class="block truncate text-xs text-dimmed">{{ b.noi }}</span>
            </span>
          </NuxtLink>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="dot-kich" title="Đột kích huyền thoại" :lead="`${dotKich.length} tuần đột kích mỗi năm: boss thế giới xuất hiện 2 lần mỗi ngày theo giờ hẹn, cần đội từ 5 người.`">
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
