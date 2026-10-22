<script setup lang="ts">
/**
 * Hướng dẫn tân thủ: từng bước từ tải game tới 30 phút đầu. Cơ chế chi tiết nằm ở /cam-nang/*.
 * Số liệu: pokedex.json (khởi đầu, tiến hoá, điều khiển) + cam-nang.json (nhiệm vụ hướng dẫn, chính tuyến).
 */
const tenSite = useAppConfig().htbb.tenSite
useSeoMeta({
  title: 'Hướng dẫn tân thủ',
  description: `Bắt đầu ${tenSite} từng bước: tải & cài game (Java J2ME, Windows, Linux, Android; iOS sắp ra mắt), tạo tài khoản, chọn Pokémon khởi đầu, điều khiển, giao diện và 30 phút đầu tiên.`,
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

/**
 * Điều khiển kiểu NRO (0.9.5+): chạm / bấm để đi tới hoặc chọn mục tiêu, nút chiêu to góc phải dưới,
 * hai nút thuốc HP / PP, nút Tự động. Java (J2ME) vẫn dùng phím số.
 * Nguồn: client_jar GameScr (phím #: menu nhanh), client_apk hd/Hud.java (hàng nút, N_HP / N_PP).
 */
const DIEU_KHIEN = [
  { viec: 'Di chuyển', dt: 'Phím mũi tên / 2·4·6·8', cam: 'Chạm vào chỗ muốn tới trên bản đồ' },
  { viec: 'Chọn mục tiêu', dt: 'Phím chọn phải (đổi mục tiêu)', cam: 'Chạm vào quái, NPC hoặc người chơi' },
  { viec: 'Đánh thường / nói chuyện / xác nhận', dt: 'Phím 5 / phím giữa', cam: 'Nút đánh lớn (chạm NPC để nói chuyện)' },
  { viec: 'Dùng chiêu', dt: 'Phím 1 · 3 (và 7 · 9 nếu gán)', cam: '4 nút chiêu quanh nút đánh' },
  { viec: 'Đổi bảng chiêu', dt: 'Phím 0', cam: 'Nút đổi bảng' },
  { viec: 'Thuốc HP / PP (Ether)', dt: 'Phím 7 / phím 9', cam: '2 nút thuốc nhanh HP / PP cạnh cụm chiêu' },
  { viec: 'Tự động đánh', dt: 'Phím # → Tự động đánh', cam: 'Viên AUTO bật / tắt; bánh răng cạnh AUTO (hoặc Menu → Tự động đánh) mở bảng cài đặt' },
  { viec: 'Mở menu', dt: 'Phím chọn trái', cam: 'Nút Poké Ball (Menu)' },
  { viec: 'Chat', dt: 'Phím # → Chat (menu nhanh)', cam: 'Nút chat (bong bóng)' },
  { viec: 'Nhập giftcode', dt: 'Menu → Thư/Quà → Nhập giftcode', cam: 'Menu → Thư/Quà → Nhập giftcode' },
  { viec: 'Tương tác người chơi', dt: 'Phím * (mời nhóm, giao dịch, kết bạn, xuống thú)', cam: 'Chạm vào người chơi' },
]
/**
 * Phím tắt bản PC (Java) — NƠI DUY NHẤT trên web liệt kê phím PC. Trong game các nút không ghi phím;
 * danh sách đầy đủ (theo phím đã đổi) ở Cài đặt (F10) → thẻ "Phím tắt", hoặc bấm F1.
 * Phím mặc định: client_pc/src/htbb/pc/BanPhim.java (MAC_DINH) — đổi ở đó thì sửa cả bảng này.
 */
const PHIM_PC: { nhom: string, ds: { phim: string[], viec: string }[] }[] = [
  {
    nhom: 'Di chuyển & tương tác',
    ds: [
      { phim: ['W A S D', 'Mũi tên'], viec: 'Đi' },
      { phim: ['Enter'], viec: 'Hành động theo ngữ cảnh: nói chuyện với NPC, đánh, nhặt đồ, tương tác · tiếp / xác nhận lời thoại' },
      { phim: ['Chuột trái'], viec: 'Làm mọi thứ: đi tới, chọn, nói chuyện, đánh, tiếp lời thoại, bấm nút' },
      { phim: ['Chuột phải'], viec: 'Menu ngữ cảnh (tuỳ chọn)' },
      { phim: ['Lăn chuột'], viec: 'Cuộn danh sách' },
    ],
  },
  {
    nhom: 'Chiến đấu (đổi được ở Cài đặt → Đổi phím)',
    ds: [
      { phim: ['Space'], viec: 'Đánh thường' },
      { phim: ['1 – 5'], viec: 'Dùng chiêu 1 – 5 trên thanh chiêu' },
      { phim: ['Q', 'E'], viec: 'Thuốc HP / thuốc PP (Ether)' },
      { phim: ['Tab'], viec: 'Đổi mục tiêu' },
      { phim: ['Shift + Tab'], viec: 'Mục tiêu trước' },
      { phim: ['R'], viec: 'Đổi bộ chiêu' },
    ],
  },
  {
    nhom: 'Giao diện',
    ds: [
      { phim: ['T', '/'], viec: 'Mở chat (đang gõ: Enter gửi tin)' },
      { phim: ['Esc'], viec: 'Menu · quay lại · đóng hộp thoại' },
      { phim: ['M'], viec: 'Menu nhanh' },
      { phim: ['P'], viec: 'Tổ đội' },
      { phim: ['F3', 'F2'], viec: 'Phím mềm trái / phải' },
      { phim: ['Ctrl + V'], viec: 'Dán chữ vào ô nhập' },
    ],
  },
  {
    nhom: 'Hệ thống',
    ds: [
      { phim: ['F10'], viec: 'Cài đặt' },
      { phim: ['F1'], viec: 'Mở trang Phím tắt trong game' },
      { phim: ['F11', 'Alt + Enter'], viec: 'Toàn màn hình' },
      { phim: ['F12'], viec: 'Chụp màn hình' },
    ],
  },
]
const meo = POKEDEX.huongDanGame.filter(t => !/^Phím/.test(t))

const N = CAM_NANG.nhiemVu
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
              Không chia sẻ mật khẩu với ai — kể cả người xưng là quản trị viên. Game không bán sức mạnh; mọi lời mời "nạp qua người trung gian", "bán xu, bán lượng" đều là lừa đảo.
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

          <WikiSection id="dieu-khien" title="4. Điều khiển" lead="Điều khiển kiểu chạm-là-làm: chạm (hoặc bấm chuột) để đi tới, chọn mục tiêu, nói chuyện với NPC. Bản Java dùng phím số; bản PC có bộ phím tắt riêng (bảng bên dưới).">
            <figure class="mb-6 grid items-center gap-4 md:grid-cols-[minmax(0,1.3fr)_minmax(0,1fr)]">
              <div class="overflow-hidden rounded-3xl shadow-[0_14px_30px_-18px_rgb(10_26_51/.55)]">
                <SiteAnhGame ma="tu-dong" sizes="(min-width: 1024px) 560px, 100vw" alt="Ảnh trong game: bảng cài đặt Tự động đánh" />
              </div>
              <figcaption class="text-sm text-muted">
                <strong class="block font-display text-base text-highlighted">Tự động đánh có bảng cài đặt riêng</strong>
                Chọn chiêu, ngưỡng uống thuốc HP / PP, nhặt đồ, vùng đánh và tự hồi sinh. Giờ tự động mỗi ngày có hạn —
                xem <NuxtLink to="/cam-nang/tu-dong" class="lien-ket">Tự động đánh</NuxtLink>.
              </figcaption>
            </figure>
            <div class="bang-cuon">
              <table class="bang-wiki">
                <thead>
                  <tr>
                    <th>Việc</th><th>Điện thoại phím / Java</th><th>Cảm ứng (Android)</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="d in DIEU_KHIEN" :key="d.viec">
                    <td class="font-semibold text-highlighted">
                      {{ d.viec }}
                    </td><td>{{ d.dt }}</td><td>{{ d.cam }}</td>
                  </tr>
                </tbody>
              </table>
            </div>

            <h3 id="phim-tat-pc" class="mt-8 font-display text-xl font-bold text-highlighted">
              Phím tắt bản PC
            </h3>
            <p class="mt-1 text-sm text-muted">
              Chỉ cần chuột trái là chơi được hết; bàn phím cho nhanh tay. Bản đồ không thu phóng (lăn chuột chỉ cuộn
              danh sách). Nút trong game chỉ ghi tên lệnh, không ghi phím — xem lại bất cứ lúc nào: bấm <UKbd value="F1" />
              (hoặc Cài đặt <UKbd value="F10" /> → thẻ "Phím tắt"); đổi phím ở thẻ "Đổi phím".
            </p>
            <div v-for="n in PHIM_PC" :key="n.nhom" class="bang-cuon mt-4">
              <table class="bang-wiki">
                <thead>
                  <tr>
                    <th>Phím</th><th>{{ n.nhom }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="d in n.ds" :key="d.viec">
                    <td class="whitespace-nowrap">
                      <template v-for="(p, i) in d.phim" :key="p">
                        <span v-if="i" class="text-muted"> / </span><UKbd :value="p" />
                      </template>
                    </td><td>{{ d.viec }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </WikiSection>

          <WikiSection id="giao-dien" title="5. Giao diện trong game">
            <SiteHudMinhHoa />
          </WikiSection>

          <WikiSection id="30-phut" title="6. 30 phút đầu tiên" :lead="`Hướng dẫn tân thủ ${TAN_THU.length} bước trong làng khởi đầu (Pewter ở Kanto, Blackthorn ở Johto — cùng các bước). Đi theo dấu nhiệm vụ trên đầu NPC; mở Nhiệm vụ rồi bấm Đi tới để tự tìm đường.`">
            <ol class="relative space-y-3 border-l-2 border-dashed border-[var(--ui-border)] pl-6">
              <li v-for="(q, i) in TAN_THU" :key="i" class="relative">
                <span class="absolute -left-[35px] grid size-7 place-items-center rounded-full border-2 border-white bg-pokered-600 font-pixel text-[9px] text-white shadow">{{ i + 1 }}</span>
                <p class="font-display font-bold text-highlighted">
                  {{ q.ten }}
                </p>
                <p class="text-sm text-muted">
                  {{ q.viec }}
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
            <WikiCallout kieu="luu-y" title="Đã quen chơi?">
              Lúc vào làng lần đầu, chọn <strong>Bỏ qua hướng dẫn (đã quen chơi)</strong>: bạn vẫn nhận đủ thưởng của các bước còn lại
              và gặp Trưởng làng Xatu để vào cốt truyện. Không làm lại được. Công dụng từng NPC trong làng xem ở Sổ tay → Hướng dẫn tân thủ.
            </WikiCallout>
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
                { to: '/cam-nang/trang-bi', icon: 'i-lucide-shield', t: 'Nâng trang bị', m: 'Nâng cấp ở Machamp bằng đá nâng cấp và xu. Đồ đã mặc bị khoá, không trao đổi được.' },
                { to: '/cam-nang/doi-su-pham', icon: 'i-lucide-users', t: 'Tìm đội & thầy', m: 'Trò được +20% EXP.' },
                { to: '/cam-nang/su-kien', icon: 'i-lucide-calendar-days', t: 'Xem lịch sự kiện', m: 'Lễ hội theo ngày thật, tuần Pokémon.' },
              ]" :key="x.to">
                <NuxtLink :to="x.to" class="the-go group flex h-full gap-3 p-4 hover:-translate-y-0.5">
                  <SiteHinhCamNang :slug="x.to.replace('/cam-nang/', '').split('#')[0]!" :icon="x.icon" class="size-11" />
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
