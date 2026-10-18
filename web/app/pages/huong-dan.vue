<script setup lang="ts">
/**
 * Hướng dẫn tân thủ: từng bước từ tải game tới 30 phút đầu. Cơ chế chi tiết nằm ở /cam-nang/*.
 * Số liệu: pokedex.json (khởi đầu, tiến hoá, điều khiển) + cam-nang.json (nhiệm vụ hướng dẫn, chính tuyến).
 */
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Hướng dẫn tân thủ',
  description: `Bắt đầu ${tenSite} từng bước: tải & cài game (Android, PC, Java, iOS sắp ra mắt), tạo tài khoản, chọn Pokémon khởi đầu, điều khiển, giao diện và 30 phút đầu tiên.`,
})
const { data: site } = await useSite()
const hoTro = useHoTro().link

const muc = [
  { id: 'tai-game', ten: 'Tải & cài đặt' },
  { id: 'tai-khoan', ten: 'Tạo tài khoản' },
  { id: 'khoi-dau', ten: 'Chọn Pokémon khởi đầu' },
  { id: 'dieu-khien', ten: 'Điều khiển' },
  { id: 'giao-dien', ten: 'Giao diện trong game' },
  { id: '30-phut', ten: '30 phút đầu tiên' },
  { id: 'tiep-theo', ten: 'Tiếp theo làm gì?' },
]

// Nền tảng + bước cài: danh mục chung (shared/utils/taiGame.ts qua useBanTai) — admin bật iOS là thẻ đổi theo.
const { ds: NEN_TANG } = useBanTai()

// Khởi đầu
const MO_KD: Record<number, { vai: string, mo: string }> = {
  0: { vai: 'Cận chiến cân bằng', mo: 'Dễ chơi nhất cho người mới. Tiến hoá thành Charizard (Lửa/Bay).' },
  1: { vai: 'Trâu bò, sát thương cao', mo: 'Chịu đòn tốt, chiêu Cỏ/Độc mạnh khi dồn Sức mạnh.' },
  2: { vai: 'Linh hoạt, hỗ trợ', mo: `Cấp ${POKEDEX.capNhanhEevee} chọn 1 trong 8 nhánh. Có chiêu hồi sinh đồng đội.` },
  3: { vai: 'Phòng thủ, phản công', mo: 'Mai cứng, chiêu Nước khắc nhiều quái Lửa/Đất/Đá.' },
  4: { vai: 'Đánh xa, cơ động', mo: 'Giữ khoảng cách, bắn điện. Máu thấp nên cần né.' },
}
const khoiDau = [0, 1, 3, 4, 2].map((cls) => {
  const k = POKEDEX.khoiDau.find(x => x.class === cls)!
  const l = loaiTheoDex(k.dex)!
  return { ...k, l, ...MO_KD[cls]! }
})

const DIEU_KHIEN = [
  { viec: 'Di chuyển', dt: 'Phím mũi tên / 2·4·6·8', pc: 'Phím mũi tên', cam: 'Cần điều khiển ảo hoặc chạm vào bản đồ' },
  { viec: 'Đánh thường / xác nhận', dt: 'Phím 5 / phím giữa', pc: 'Enter hoặc 5', cam: 'Nút đánh' },
  { viec: 'Dùng chiêu', dt: 'Phím 1 · 3 (và 7 · 9 nếu gán)', pc: 'Phím số 1 · 3 · 7 · 9', cam: 'Nút chiêu quanh nút đánh' },
  { viec: 'Đổi bảng chiêu', dt: 'Phím 0', pc: 'Phím 0', cam: 'Nút đổi bảng' },
  { viec: 'Thuốc hồi máu / Ether', dt: 'Phím 7 / phím 9', pc: 'Phím 7 / 9', cam: 'Ô thuốc trên thanh chiêu' },
  { viec: 'Mở menu', dt: 'Phím chọn trái', pc: 'F1', cam: 'Nút menu' },
  { viec: 'Đổi mục tiêu', dt: 'Phím chọn phải', pc: 'F2 / Tab', cam: 'Chạm vào mục tiêu' },
  { viec: 'Chat', dt: 'Phím #', pc: 'Nút chat', cam: 'Nút chat' },
  { viec: 'Tương tác người chơi', dt: 'Phím * (mời nhóm, giao dịch, kết bạn, xuống thú)', pc: 'Chạm/nhấp vào người chơi', cam: 'Chạm vào người chơi' },
]
const meo = POKEDEX.huongDanGame.filter(t => !/^Phím/.test(t))

const N = CAM_NANG.nhiemVu
const hd = N.huongDan
const cung1 = N.cung[0]
/** Lần tiến hoá đầu của từng dòng, gom theo cấp: "Pichu cấp 10, Charmander/Bulbasaur/Squirtle cấp 16". */
const tienHoaDau = (() => {
  const theoCap = new Map<number, string[]>()
  for (const k of POKEDEX.khoiDau) {
    const l = loaiTheoDex(k.dex)
    const cap = Number(String(l?.tienHoa?.buoc[1]?.dieuKien ?? '').match(/\d+/)?.[0] ?? 0)
    if (l && cap) theoCap.set(cap, [...(theoCap.get(cap) ?? []), l.ten])
  }
  return [...theoCap.entries()].sort((a, b) => a[0] - b[0]).map(([cap, ten]) => `${ten.join('/')} cấp ${cap}`).join(', ')
})()
</script>

<template>
  <div>
    <SitePageBanner
      title="Hướng dẫn tân thủ"
      eyebrow="Bắt đầu"
      :intro="`Từ lúc tải game tới khi Pokémon của bạn tiến hoá lần đầu — từng bước, không bỏ sót.`"
    />

    <UContainer class="max-w-7xl py-12 sm:py-16">
      <div class="grid gap-10 lg:grid-cols-[14rem_minmax(0,1fr)]">
        <nav aria-label="Mục lục" class="lg:sticky lg:top-24 lg:self-start">
          <p class="eyebrow">
            Các bước
          </p>
          <ol class="mt-3 flex flex-wrap gap-2 lg:flex-col lg:gap-1">
            <li v-for="(m, i) in muc" :key="m.id">
              <a :href="`#${m.id}`" class="flex items-center gap-2 rounded-full px-3 py-1.5 font-display text-sm font-semibold text-muted ring-1 ring-default hover:bg-elevated hover:text-primary lg:ring-0">
                <span class="grid size-5 place-items-center rounded-full bg-pokered-600 font-pixel text-[8px] text-white">{{ i + 1 }}</span> {{ m.ten }}
              </a>
            </li>
          </ol>
          <UButton to="/cam-nang" color="primary" variant="soft" icon="i-lucide-library" class="mt-6 hidden lg:inline-flex">
            Cẩm nang cơ chế
          </UButton>
        </nav>

        <div class="wiki min-w-0">
          <WikiSection id="tai-game" title="1. Tải & cài đặt" lead="Chọn bản phù hợp thiết bị. Mọi bản dùng chung một tài khoản và một thế giới.">
            <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              <div v-for="n in NEN_TANG" :key="n.ma" class="rounded-3xl glass p-5">
                <p class="flex flex-wrap items-center gap-2 font-display text-lg font-extrabold text-highlighted">
                  <span class="grid size-9 place-items-center rounded-xl bg-pikachu-400 text-navy-950"><UIcon :name="n.icon" class="size-5" /></span>{{ n.ten }}
                  <UBadge v-if="n.sapRaMat" color="warning" variant="subtle" size="sm">Sắp ra mắt</UBadge>
                </p>
                <p v-if="n.heDieuHanh" class="mt-1 text-xs text-muted">Cần: {{ n.heDieuHanh }}</p>
                <p v-if="n.sapRaMat && !n.buoc.length" class="mt-3 text-sm text-toned">
                  Bản {{ n.ten }} đang được chuẩn bị. Khi phát hành, hướng dẫn cài (TestFlight / App Store) sẽ hiện ở đây và trên trang Tải game.
                </p>
                <ol v-else class="mt-3 space-y-2 text-sm text-toned">
                  <li v-for="(b, i) in n.buoc" :key="i" class="flex gap-2">
                    <span class="font-bold text-primary">{{ i + 1 }}.</span><span>{{ b }}</span>
                  </li>
                </ol>
              </div>
            </div>
            <div class="mt-5 flex flex-wrap gap-3">
              <UButton to="/download" class="btn-game" trailing-icon="i-lucide-download">
                Đến trang Tải game
              </UButton>
              <UButton to="/download#yeu-cau" color="neutral" variant="outline" icon="i-lucide-cpu">
                Yêu cầu cấu hình
              </UButton>
            </div>
          </WikiSection>

          <WikiSection id="tai-khoan" title="2. Tạo tài khoản">
            <ol class="grid gap-3 md:grid-cols-3">
              <li class="rounded-2xl glass p-4 text-sm">
                <strong class="font-display text-base text-highlighted">Đăng ký trên website</strong>
                <p class="mt-1 text-muted">
                  Vào <NuxtLink to="/register" class="lien-ket">Đăng ký</NuxtLink>, điền tên tài khoản, mật khẩu theo yêu cầu hiển thị dưới ô nhập, và email để lấy lại mật khẩu khi quên.
                </p>
              </li>
              <li class="rounded-2xl glass p-4 text-sm">
                <strong class="font-display text-base text-highlighted">Kích hoạt (nếu cần)</strong>
                <p class="mt-1 text-muted">
                  Nếu máy chủ yêu cầu, đăng nhập website và vào trang <NuxtLink to="/kichhoat" class="lien-ket">Kích hoạt</NuxtLink>.
                </p>
              </li>
              <li class="rounded-2xl glass p-4 text-sm">
                <strong class="font-display text-base text-highlighted">Đăng nhập trong game</strong>
                <p class="mt-1 text-muted">
                  Mở game, chọn máy chủ, nhập đúng tài khoản/mật khẩu vừa tạo.
                </p>
              </li>
            </ol>
            <WikiCallout kieu="luu-y">
              Không chia sẻ mật khẩu với ai — kể cả người xưng là quản trị viên. Game không bán gì, nên mọi lời mời "nạp tiền nhận quà" đều là lừa đảo.
            </WikiCallout>
          </WikiSection>

          <WikiSection id="khoi-dau" title="3. Chọn Pokémon khởi đầu" lead="Pokémon bạn chọn chính là bạn trong suốt hành trình. Không đổi được về sau, nhưng mỗi tài khoản có thể tạo nhiều Pokémon.">
            <ul class="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
              <li v-for="k in khoiDau" :key="k.dex">
                <NuxtLink :to="`/pokedex/${k.dex}`" class="group flex h-full flex-col overflow-hidden rounded-3xl glass transition hover:-translate-y-1">
                  <PokeSan :dex="k.dex" :he="k.l.he" :size="140" class="w-full" />
                  <span class="flex flex-1 flex-col p-4">
                    <span class="font-display text-xl font-extrabold text-highlighted group-hover:text-primary">{{ k.l.ten }}</span>
                    <span class="mt-1 flex gap-1"><PokeHeBadge v-for="h in k.l.he" :key="h" :he="h" size="sm" /></span>
                    <span class="mt-2 text-xs font-bold uppercase tracking-wide text-primary">{{ k.vai }}</span>
                    <span class="mt-1 text-sm text-muted">{{ k.mo }}</span>
                  </span>
                </NuxtLink>
              </li>
            </ul>
            <p class="mt-4 text-sm text-muted">
              So sánh chỉ số chính ở <NuxtLink to="/cam-nang/chi-so-sat-thuong" class="lien-ket">Chỉ số & sát thương</NuxtLink>, các mốc tiến hoá ở
              <NuxtLink to="/cam-nang/tien-hoa" class="lien-ket">Tiến hoá</NuxtLink>.
            </p>
          </WikiSection>

          <WikiSection id="dieu-khien" title="4. Điều khiển" lead="Bố cục phím theo điện thoại phím số; bản PC dùng bàn phím tương ứng, bản Android có nút cảm ứng.">
            <div class="bang-cuon">
              <table class="bang-wiki">
                <thead>
                  <tr>
                    <th>Việc</th><th>Điện thoại phím / Java</th><th>PC</th><th>Cảm ứng (Android)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="d in DIEU_KHIEN" :key="d.viec">
                    <td class="font-semibold text-highlighted">
                      {{ d.viec }}
                    </td><td>{{ d.dt }}</td><td>{{ d.pc }}</td><td>{{ d.cam }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </WikiSection>

          <WikiSection id="giao-dien" title="5. Giao diện trong game">
            <SiteHudMinhHoa />
          </WikiSection>

          <WikiSection id="30-phut" title="6. 30 phút đầu tiên" :lead="`Chuỗi hướng dẫn ${hd.length} bước dẫn bạn đi khắp Pallet — làm xong bạn sẽ ở khoảng cấp 8, đủ hiểu mọi thứ cơ bản.`">
            <ol class="relative space-y-3 border-l-2 border-dashed border-[var(--ui-border)] pl-6">
              <li v-for="(q, i) in hd" :key="i" class="relative">
                <span class="absolute -left-[35px] grid size-7 place-items-center rounded-full border-2 border-white bg-pokered-600 font-pixel text-[9px] text-white shadow">{{ i + 1 }}</span>
                <p class="font-display font-bold text-highlighted">
                  {{ q.ten }}
                </p>
                <p class="text-sm text-muted">
                  {{ q.yeuCau || `Gặp ${q.npc} ở ${q.diaDanh}` }}
                </p>
              </li>
              <li class="relative">
                <span class="absolute -left-[35px] grid size-7 place-items-center rounded-full border-2 border-white bg-grass-500 text-white shadow"><UIcon name="i-lucide-flag" class="size-3.5" /></span>
                <p class="font-display font-bold text-highlighted">
                  Bắt đầu chính tuyến: {{ cung1?.ten }}
                </p>
                <p class="text-sm text-muted">
                  Tiếp tục cốt truyện ở {{ cung1?.chuong[0]?.diaDanh }}; lần tiến hoá đầu tiên: {{ tienHoaDau }}; Eevee chọn nhánh bằng đá tiến hoá từ cấp {{ POKEDEX.capNhanhEevee }}.
                </p>
              </li>
            </ol>
            <WikiCallout kieu="meo" title="Mẹo cho người mới">
              <ul class="cham !gap-1.5">
                <li v-for="m in meo.slice(0, 8)" :key="m">
                  {{ m }}
                </li>
              </ul>
            </WikiCallout>
          </WikiSection>

          <WikiSection id="tiep-theo" title="7. Tiếp theo làm gì?">
            <ul class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              <li v-for="x in [
                { to: '/cam-nang/he-khac-che', icon: 'i-lucide-flame', t: 'Học khắc hệ', m: 'Đánh đúng hệ = nhanh gấp đôi.' },
                { to: '/cam-nang/nhiem-vu', icon: 'i-lucide-scroll-text', t: 'Theo chính tuyến', m: 'EXP và xu đều đặn mỗi tuần.' },
                { to: '/cam-nang/ban-do#luyen-cap', icon: 'i-lucide-map', t: 'Tìm khu luyện cấp', m: 'Quái gần cấp bạn nhất.' },
                { to: '/cam-nang/trang-bi', icon: 'i-lucide-shield', t: 'Nâng trang bị', m: 'Nâng cấp ở Machamp bằng đá nâng cấp và xu.' },
                { to: '/cam-nang/doi-su-pham', icon: 'i-lucide-users', t: 'Tìm đội & thầy', m: 'Trò được +20% EXP.' },
                { to: '/cam-nang/su-kien', icon: 'i-lucide-calendar-days', t: 'Xem sự kiện tuần này', m: 'Mỗi tuần một sự kiện.' },
              ]" :key="x.to">
                <NuxtLink :to="x.to" class="group flex h-full gap-3 rounded-2xl glass p-4 transition hover:-translate-y-0.5">
                  <span class="grid size-10 shrink-0 place-items-center rounded-xl bg-pikachu-400 text-navy-950"><UIcon :name="x.icon" class="size-5" /></span>
                  <span><strong class="font-display text-highlighted group-hover:text-primary">{{ x.t }}</strong><span class="block text-sm text-muted">{{ x.m }}</span></span>
                </NuxtLink>
              </li>
            </ul>
            <p class="mt-6 text-sm text-muted">
              Gặp khó khăn? Hỏi trong <ULink :to="hoTro" :target="hoTro.startsWith('https://') ? '_blank' : undefined" class="lien-ket">nhóm hỗ trợ</ULink> hoặc đọc <NuxtLink to="/cam-nang/faq" class="lien-ket">câu hỏi thường gặp</NuxtLink>.
            </p>
          </WikiSection>
        </div>
      </div>
    </UContainer>
  </div>
</template>
