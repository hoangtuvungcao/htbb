<script setup lang="ts">
/**
 * Trang quản trị — đăng nhập + bảng tổng quan.
 *
 * Chưa đăng nhập  -> form đăng nhập hai bước (AdminDangNhap).
 * Đã đăng nhập    -> thống kê tài khoản / nhân vật / online / giftcode.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

const auth = useAdminAuth()
auth.hydrate()
const api = useAdminApi()

type Cot = { nhan: string, gt: number }
interface TongQuan {
  loiKetNoi: string | null
  dbAccount: string
  dbGame: string
  baoMat: { chayHttps: boolean, daBat2fa: boolean, conMaDuPhong: number, batBuoc2fa: boolean, chuaGhiDanh: string[], admin: string }
  online: {
    coBang: boolean
    soMau: number | null
    ganNhat: { nhanVat: number, ketNoi: number, phutTruoc: number, thoiGian: string } | null
    cu: boolean
    dinhHomNay: number | null
    dinh7Ngay: number | null
    tbHomNay: number | null
    bdGio: Cot[]
    bdNgay: Cot[]
  }
  taiKhoan: { tong: number | null, khoa: number | null, choEmail: number | null, homNay: number | null, bayNgay: number | null, baMuoiNgay: number | null, chuaKichHoat: number | null, chuaKichHoatUocLuong: boolean }
  nhanVat: { tong: number | null, song: number | null, khoa: number | null, h24: number | null, bayNgay: number | null, baMuoiNgay: number | null }
  giftcode: { tong: number | null, conDung: number | null, luotTong: number | null }
  bdDangKy: Cot[]
  bdHoatDong: Cot[]
  phanBoNghe: Cot[]
  phanBoCap: Cot[]
  topXp: { charname: string, lastLv: number, xp: number, nghe: string }[]
  topLuong: { charname: string, lastLv: number, luong: number, luonglock: number }[]
  topNap: { charname: string, luongnap: number, luongxai: number }[]
  tkMoi: { id: number, username: string, email: string, regdate: string, ban: number }[]
  nhatKyMoi: { admin_user: string, hanh_dong: string, doi_tuong: string, thoi_gian: string }[]
}

const { data, status, error, refresh } = useAsyncData<TongQuan | null>(
  'admin-tong-quan',
  () => (auth.loggedIn.value ? api<TongQuan>('/api/admin/tong-quan') : Promise.resolve(null)),
  { server: false, watch: [() => auth.loggedIn.value] },
)
const dangTai = computed(() => status.value === 'pending')

/** null (bảng chưa có / câu đếm hỏng) -> "—" thay vì vỡ trang. */
const hienSo = (n: number | null | undefined) => (n === null || n === undefined ? '—' : so(n))

// Trình duyệt đang ở HTTPS cũng tính — khi web đứng sau proxy/WAF không gửi X-Forwarded-Proto.
const chayHttps = computed(() => !!data.value?.baoMat.chayHttps || (import.meta.client && window.location.protocol === 'https:'))

const linkNhanVat = (ten: string) => `/admin/nhanvat?q=${encodeURIComponent(ten)}`

const cotPhai = { class: { th: 'text-right', td: 'text-right tabular-nums' } }
const cotTopXp = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'lastLv', header: 'Cấp', meta: cotPhai },
]
const cotTopLuong = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'luong', header: 'Lượng', meta: cotPhai },
]
const cotTopNap = [
  { accessorKey: 'charname', header: 'Nhân vật' },
  { accessorKey: 'luongnap', header: 'Đã nạp', meta: cotPhai },
]
const cotTkMoi = [
  { accessorKey: 'username', header: 'Tài khoản' },
  { accessorKey: 'regdate', header: 'Đăng ký' },
  { accessorKey: 'ban', header: 'Trạng thái' },
]
const cotNhatKy = [
  { accessorKey: 'admin_user', header: 'Người làm' },
  { accessorKey: 'hanh_dong', header: 'Việc' },
  { accessorKey: 'thoi_gian', header: 'Lúc' },
]
</script>

<template>
  <AdminDangNhap v-if="!auth.loggedIn.value" />

  <AdminPage v-else title="Tổng quan">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="dangTai" aria-label="Làm mới" @click="refresh()" />
      <UButton to="/admin/taikhoan" icon="i-lucide-search" color="neutral" variant="outline" size="sm" class="hidden sm:inline-flex">
        Tìm tài khoản
      </UButton>
      <UButton to="/admin/giftcode" icon="i-lucide-gift" size="sm">
        Tạo giftcode
      </UButton>
    </template>

    <div v-if="dangTai && !data" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <USkeleton v-for="i in 8" :key="i" class="h-28 rounded-xl" />
    </div>

    <UAlert v-else-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" title="Không tải được số liệu" :description="loiApi(error)" />

    <UCard v-else-if="data?.loiKetNoi">
      <template #header>
        <h3 class="font-semibold text-highlighted">
          Không kết nối được cơ sở dữ liệu
        </h3>
      </template>
      <p>
        Kiểm tra MySQL/MariaDB đã chạy chưa, và <code>KPAH_DB_HOST</code> / <code>KPAH_DB_USER</code> /
        <code>KPAH_DB_PASS</code> (hoặc <code>server.ini</code> của game server) có đúng không.
      </p>
      <p class="mt-2 text-sm text-muted">
        Chi tiết: {{ data.loiKetNoi }}
      </p>
    </UCard>

    <template v-else-if="data">
      <p class="text-sm text-muted">
        Số liệu đọc trực tiếp từ <code>{{ data.dbAccount }}</code> và <code>{{ data.dbGame }}</code> ngay lúc mở trang.
      </p>

      <!-- ============ Cảnh báo an toàn đăng nhập ============
           Hai lỗ này không hiện ra ở đâu khác: trang vẫn chạy tốt, vẫn đăng nhập
           được, vẫn đẹp. Chúng chỉ lộ ra vào hôm có người lấy được mật khẩu. Nên
           phải nói ngay trên trang đầu, mỗi lần mở, cho tới khi được sửa. -->
      <UAlert v-if="!chayHttps" color="error" variant="subtle" icon="i-lucide-lock-open" title="Trang này đang chạy trên HTTP, không có mã hoá.">
        <template #description>
          <p>
            Mỗi lần bạn đăng nhập, tên và mật khẩu đi qua Internet dưới dạng chữ thường —
            bất kỳ ai trên đường truyền (wifi công cộng, nhà mạng, nhà cung cấp máy chủ)
            đọc được nguyên văn. Đây là cách thông tin đăng nhập admin bị lộ trong thực tế,
            và nó không liên quan gì tới việc đường dẫn <code>/admin</code> có bí mật hay không.
          </p>
          <p class="mt-2">
            Sửa: bật HTTPS trên máy chủ — <code>sudo /opt/kpah/vps/bat-https.sh</code>.
            Xem <NuxtLink to="/admin/chandoan" class="text-primary hover:underline">Chẩn đoán</NuxtLink> để biết máy chủ đang mở những cổng nào.
          </p>
        </template>
      </UAlert>

      <UAlert v-if="!data.baoMat.daBat2fa" color="error" variant="subtle" icon="i-lucide-shield-alert" :title="`Tài khoản ${data.baoMat.admin} chưa bật xác thực hai bước.`">
        <template #description>
          <p>Hiện tại chỉ cần biết mật khẩu là vào được đây — kể cả người không phải bạn.</p>
          <p class="mt-2">
            Bật bằng lệnh chạy trên máy chủ (khoá bí mật không đi qua web, cố ý như vậy):
          </p>
          <p class="mt-1">
            <code>cd &lt;thư mục KPAH-Web&gt; &amp;&amp; node scripts/tao-ma-2buoc.mjs {{ data.baoMat.admin }}</code>
          </p>
          <p v-if="!data.baoMat.batBuoc2fa" class="mt-2">
            Ghi danh xong cho tất cả người trong <code>KPAH_ADMIN_ACCOUNTS</code> thì đặt
            <code>KPAH_ADMIN_2FA_BAT_BUOC=1</code> cho dịch vụ web —
            chưa có dòng đó thì người chưa ghi danh vẫn vào được chỉ bằng mật khẩu.
          </p>
        </template>
      </UAlert>
      <UAlert v-else-if="data.baoMat.conMaDuPhong === 0" color="error" variant="subtle" icon="i-lucide-key-round" title="Đã hết mã dự phòng.">
        <template #description>
          <p>Nếu mất điện thoại lúc này thì bạn phải vào máy chủ bằng SSH mới lấy lại được quyền quản trị.</p>
          <p class="mt-2">
            Sinh bộ mã mới (khoá cũ sẽ bị thay, phải quét lại trên điện thoại):
            <code>node scripts/tao-ma-2buoc.mjs {{ data.baoMat.admin }}</code>
          </p>
        </template>
      </UAlert>
      <UAlert
        v-else-if="data.baoMat.conMaDuPhong <= 2"
        color="warning"
        variant="subtle"
        icon="i-lucide-key-round"
        :title="`Chỉ còn ${so(data.baoMat.conMaDuPhong)} mã dự phòng.`"
        description="Hết mã thì mất điện thoại là phải dùng SSH để vào lại."
      />
      <UAlert
        v-if="data.baoMat.daBat2fa && !data.baoMat.batBuoc2fa && data.baoMat.chuaGhiDanh.length"
        color="warning"
        variant="subtle"
        icon="i-lucide-users"
        title="Hai bước chưa bắt buộc cho cả đội."
      >
        <template #description>
          Còn {{ data.baoMat.chuaGhiDanh.join(', ') }} chưa ghi danh — những tài khoản đó vẫn vào được chỉ bằng mật khẩu.
          Ghi danh xong cho mọi người thì đặt <code>KPAH_ADMIN_2FA_BAT_BUOC=1</code>.
        </template>
      </UAlert>

      <!-- ============ Người chơi online ============ -->
      <section class="space-y-4">
        <h3 class="text-xs font-semibold uppercase tracking-widest text-muted">
          Người chơi online
        </h3>
        <UCard v-if="!data.online.coBang">
          <p>Chưa có bảng <code>tob_log_online</code>, nên chưa vẽ được gì.</p>
          <p class="mt-1.5 text-sm text-muted">
            Server game tự tạo bảng này lúc khởi động (<code>data/GhiSoOnline.java</code>).
            Khởi động lại server một lần là bắt đầu ghi, 5 phút một mẫu, và giữ lại 30 ngày.
            Muốn có bảng ngay thì chạy <code>sql/them_bang_tob_log_online.sql</code>.
          </p>
        </UCard>
        <UCard v-else-if="data.online.soMau === 0">
          <p>
            <strong>Đã có bảng <code>tob_log_online</code> nhưng chưa có một dòng nào.</strong>
            Nghĩa là bảng được tạo bằng tay, còn server game thì chưa hề ghi mẫu.
          </p>
          <p class="mt-1.5 text-sm text-muted">
            Bộ lấy mẫu nằm trong <code>data/GhiSoOnline.java</code> — nó chỉ chạy khi
            <code>KPAH2_patched.jar</code> đã được build lại từ mã nguồn hiện tại và server
            đã khởi động lại. Sửa file <code>.java</code> thôi thì server đang chạy vẫn dùng
            jar cũ và không có gì thay đổi.
          </p>
          <p class="mt-1.5 text-sm text-muted">
            Kiểm tra nhanh xem jar đã có lớp đó chưa:
            <code>jar tf KPAH2_patched.jar | grep GhiSoOnline</code> — không ra dòng nào
            tức là chưa build lại.
          </p>
        </UCard>
        <template v-else>
          <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            <AdminStatCard v-if="!data.online.ganNhat" label="Đang online" value="—" note="chưa ghi được mẫu nào" icon="i-lucide-radio" />
            <!-- Số cũ KHÔNG được trình bày như số hiện tại: mẫu ngừng lại gần như luôn
                 có nghĩa là server game đã tắt. -->
            <AdminStatCard
              v-else-if="data.online.cu"
              label="Đang online"
              value="—"
              :note="`mẫu gần nhất cách đây ${so(data.online.ganNhat.phutTruoc)} phút — server game có thể đang tắt`"
              mau="bad"
              icon="i-lucide-radio"
            />
            <AdminStatCard
              v-else
              label="Đang online"
              :value="data.online.ganNhat.nhanVat"
              :note="`${so(data.online.ganNhat.ketNoi)} kết nối · đo ${so(data.online.ganNhat.phutTruoc)} phút trước`"
              mau="ok"
              icon="i-lucide-radio"
            />
            <AdminStatCard label="Đỉnh hôm nay" :value="hienSo(data.online.dinhHomNay)" note="lúc đông nhất kể từ 0h" icon="i-lucide-trending-up" />
            <AdminStatCard label="Đỉnh 7 ngày" :value="hienSo(data.online.dinh7Ngay)" icon="i-lucide-calendar-range" />
            <AdminStatCard label="Trung bình hôm nay" :value="hienSo(data.online.tbHomNay)" note="bình quân các mẫu 5 phút" icon="i-lucide-gauge" />
          </div>
          <div class="grid gap-4 lg:grid-cols-2">
            <UCard>
              <template #header>
                <h3 class="font-semibold text-highlighted">
                  Online · 24 giờ qua
                </h3>
              </template>
              <AdminBarChart :data="data.online.bdGio" don-vi="người" />
              <p class="mt-3 text-xs text-muted">
                Mỗi cột là <em>đỉnh</em> của giờ đó, không phải trung bình — đỉnh mới là con số
                quyết định tải máy chủ.
              </p>
            </UCard>
            <UCard>
              <template #header>
                <h3 class="font-semibold text-highlighted">
                  Đỉnh online · 14 ngày
                </h3>
              </template>
              <AdminBarChart :data="data.online.bdNgay" don-vi="người" />
              <p class="mt-3 text-xs text-muted">
                Đếm <em>nhân vật đang trong game</em>. Số kết nối luôn cao hơn vì tính cả người
                còn ở màn hình đăng nhập hoặc chọn nhân vật.
              </p>
            </UCard>
          </div>
        </template>
      </section>

      <!-- ============ Tài khoản ============ -->
      <section class="space-y-4">
        <h3 class="text-xs font-semibold uppercase tracking-widest text-muted">
          Tài khoản
        </h3>
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <AdminStatCard label="Tổng tài khoản" :value="hienSo(data.taiKhoan.tong)" icon="i-lucide-users" />
          <AdminStatCard
            label="Đăng ký hôm nay"
            :value="hienSo(data.taiKhoan.homNay)"
            :note="`7 ngày: ${hienSo(data.taiKhoan.bayNgay)} · 30 ngày: ${hienSo(data.taiKhoan.baMuoiNgay)}`"
            mau="ok"
            icon="i-lucide-user-plus"
          />
          <AdminStatCard
            label="Đang bị khoá"
            :value="hienSo(data.taiKhoan.khoa)"
            note="ban = 1, không đăng nhập được"
            :mau="(data.taiKhoan.khoa ?? 0) > 0 ? 'bad' : ''"
            icon="i-lucide-user-x"
          />
          <AdminStatCard
            label="Chưa kích hoạt"
            :value="hienSo(data.taiKhoan.chuaKichHoat)"
            :note="data.taiKhoan.chuaKichHoatUocLuong ? 'ước lượng (không đọc chéo được 2 DB)' : 'chưa có trong 5h_active'"
            mau="warn"
            icon="i-lucide-hourglass"
          />
          <AdminStatCard
            v-if="(data.taiKhoan.choEmail ?? 0) > 0"
            label="Chờ xác nhận email"
            :value="hienSo(data.taiKhoan.choEmail)"
            note="ban = 3"
            mau="warn"
            icon="i-lucide-mail"
          />
        </div>
      </section>

      <!-- ============ Nhân vật ============ -->
      <section class="space-y-4">
        <h3 class="text-xs font-semibold uppercase tracking-widest text-muted">
          Nhân vật
        </h3>
        <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          <AdminStatCard label="Tổng nhân vật" :value="hienSo(data.nhanVat.tong)" :note="`đang chơi được: ${hienSo(data.nhanVat.song)}`" icon="i-lucide-shield" />
          <AdminStatCard
            label="Vào trong 24 giờ"
            :value="hienSo(data.nhanVat.h24)"
            :note="`7 ngày: ${hienSo(data.nhanVat.bayNgay)} · 30 ngày: ${hienSo(data.nhanVat.baMuoiNgay)}`"
            mau="ok"
            icon="i-lucide-log-in"
          />
          <AdminStatCard
            label="Nhân vật bị khoá"
            :value="hienSo(data.nhanVat.khoa)"
            note="biến mất khỏi màn chọn nhân vật"
            :mau="(data.nhanVat.khoa ?? 0) > 0 ? 'bad' : ''"
            icon="i-lucide-shield-off"
          />
          <AdminStatCard
            label="Mã quà còn lượt"
            :value="hienSo(data.giftcode.conDung)"
            :note="`trên tổng ${hienSo(data.giftcode.tong)} mã · đã phát ${hienSo(data.giftcode.luotTong)} lượt`"
            icon="i-lucide-gift"
          />
        </div>
      </section>

      <div class="grid gap-4 lg:grid-cols-2">
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Tài khoản đăng ký · 14 ngày
            </h3>
          </template>
          <AdminBarChart :data="data.bdDangKy" don-vi="tài khoản" />
        </UCard>
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Nhân vật vào game · 14 ngày
            </h3>
          </template>
          <AdminBarChart :data="data.bdHoatDong" don-vi="nhân vật" />
          <p class="mt-3 text-xs text-muted">
            Đếm theo cột <code>lastLog</code> — <em>lần vào cuối cùng</em> của mỗi nhân vật, nên một
            nhân vật chỉ nằm ở đúng một cột. Người chơi vào đều mỗi ngày sẽ luôn đứng ở cột hôm nay.
          </p>
        </UCard>
      </div>

      <div class="grid gap-4 lg:grid-cols-2">
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Phân bố nghề
            </h3>
          </template>
          <AdminHBar :data="data.phanBoNghe" />
        </UCard>
        <UCard>
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Phân bố cấp độ
            </h3>
          </template>
          <AdminHBar :data="data.phanBoCap" />
        </UCard>
      </div>

      <!-- ============ Bảng xếp hạng nhanh ============ -->
      <div class="grid gap-4 lg:grid-cols-3">
        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Cao cấp nhất
            </h3>
          </template>
          <UTable :data="data.topXp" :columns="cotTopXp" empty="Chưa có nhân vật.">
            <template #charname-cell="{ row }">
              <NuxtLink :to="linkNhanVat(row.original.charname)" class="font-medium text-primary hover:underline">
                {{ row.original.charname }}
              </NuxtLink>
              <div class="text-xs text-muted">
                {{ row.original.nghe }}
              </div>
            </template>
          </UTable>
        </UCard>

        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Nhiều lượng nhất
            </h3>
          </template>
          <UTable :data="data.topLuong" :columns="cotTopLuong" empty="Chưa có nhân vật.">
            <template #charname-cell="{ row }">
              <NuxtLink :to="linkNhanVat(row.original.charname)" class="font-medium text-primary hover:underline">
                {{ row.original.charname }}
              </NuxtLink>
            </template>
            <template #luong-cell="{ row }">
              {{ so(row.original.luong) }}
              <div class="text-xs text-muted">
                khoá {{ so(row.original.luonglock) }}
              </div>
            </template>
          </UTable>
        </UCard>

        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Nạp nhiều nhất
            </h3>
          </template>
          <UTable :data="data.topNap" :columns="cotTopNap">
            <template #empty>
              <span class="text-muted">Bảng <code>tob_nap_xai_luong</code> chưa có dữ liệu.</span>
            </template>
            <template #charname-cell="{ row }">
              <NuxtLink :to="linkNhanVat(row.original.charname)" class="font-medium text-primary hover:underline">
                {{ row.original.charname }}
              </NuxtLink>
            </template>
            <template #luongnap-cell="{ row }">
              {{ so(row.original.luongnap) }}
              <div class="text-xs text-muted">
                xài {{ so(row.original.luongxai) }}
              </div>
            </template>
          </UTable>
        </UCard>
      </div>

      <div class="grid gap-4 lg:grid-cols-2">
        <UCard :ui="{ body: 'p-0 sm:p-0' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Tài khoản mới nhất
            </h3>
          </template>
          <UTable :data="data.tkMoi" :columns="cotTkMoi" empty="Chưa có tài khoản nào.">
            <template #username-cell="{ row }">
              <NuxtLink :to="`/admin/taikhoan?id=${row.original.id}`" class="font-medium text-primary hover:underline">
                {{ row.original.username }}
              </NuxtLink>
              <div v-if="row.original.email" class="text-xs text-muted">
                {{ row.original.email }}
              </div>
            </template>
            <template #regdate-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.regdate }}</span>
            </template>
            <template #ban-cell="{ row }">
              <UBadge v-if="row.original.ban === 1" color="error" variant="subtle">
                Bị khoá
              </UBadge>
              <UBadge v-else color="success" variant="subtle">
                Bình thường
              </UBadge>
            </template>
          </UTable>
        </UCard>

        <UCard :ui="{ body: 'p-0 sm:p-0', footer: 'py-3' }">
          <template #header>
            <h3 class="font-semibold text-highlighted">
              Thao tác quản trị gần nhất
            </h3>
          </template>
          <UTable :data="data.nhatKyMoi" :columns="cotNhatKy" empty="Chưa có thao tác nào được ghi.">
            <template #hanh_dong-cell="{ row }">
              <span class="font-mono text-xs">{{ row.original.hanh_dong }}</span>
              <span class="ms-1 text-muted">{{ row.original.doi_tuong }}</span>
            </template>
            <template #thoi_gian-cell="{ row }">
              <span class="whitespace-nowrap text-xs text-muted">{{ row.original.thoi_gian }}</span>
            </template>
          </UTable>
          <template #footer>
            <UButton to="/admin/nhatky" variant="link" trailing-icon="i-lucide-arrow-right" class="px-0">
              Xem toàn bộ nhật ký
            </UButton>
          </template>
        </UCard>
      </div>
    </template>
  </AdminPage>
</template>
