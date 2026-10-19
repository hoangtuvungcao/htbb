<script setup lang="ts">
/**
 * Bật / tắt sự kiện trong game (bảng htbb_db.tob_su_kien), và hẹn giờ mở/đóng.
 *
 * Danh sách sự kiện do GAME SERVER đăng lên mỗi lần khởi động. Trang này chỉ ghi
 * bat / sua_boi / sua_luc / mo_luc / dong_luc; server ghi phần còn lại. Đổi xong server
 * tự gom thay đổi, báo trong game rồi tự khởi động lại (chỉ cần ghi CSDL).
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

interface SuKien {
  ma: string
  bat: number
  ten: string | null
  nhom: string | null
  mo_ta: string | null
  dang_bat: number
  sua_boi: string | null
  sua_luc: string | null
  may_chu_luc: string | null
  mo_luc: string | null
  dong_luc: string | null
}

interface DuLieu {
  coBang: boolean
  coCotLich: boolean
  loiDoc: string
  bayGio: string
  maCongTong: string
  tong: number
  theoNhom: { nhom: string, ds: SuKien[] }[]
  choKhoiDong: { ma: string, ten: string | null, bat: number }[]
  anhChupHong: { ma: string, ten: string | null }[]
  lichSapToi: { ma: string, ten: string, ve: 'mo' | 'dong', moc: string }[]
  mayChuLuc: string | null
  soDangBat: number
  soSeBat: number
  congTongTat: boolean
  tuKhoiDongLai: boolean | null
  gomGiay: number
  baoTruocGiay: number
}

const api = useAdminApi()
// Vai trò chỉ đọc (Hỗ trợ / Chỉ xem / Biên tập) không thấy công tắc và nút — server cũng chặn (403).
const me = useAdminMe()
const duocGhi = computed(() => me.duoc('/api/admin/sukien/bat-tat', 'POST'))
const toast = useToast()

const { data, status, error, refresh } = useAsyncData('admin-sukien', () => api<DuLieu>('/api/admin/sukien'), { server: false })

/** Ô nhập lịch của từng sự kiện — nạp lại mỗi lần đọc dữ liệu. */
const lichNhap = reactive<Record<string, { mo: string, dong: string }>>({})
const dangLam = reactive<Record<string, boolean>>({})
const timKiem = ref('')
const chiLech = ref(false)

/** 'Y-m-d H:i:s' của CSDL -> giá trị ô datetime-local ('Y-m-dTH:i'). */
function mocChoO(v: string | null | undefined): string {
  return v ? v.replace(' ', 'T').slice(0, 16) : ''
}

/**
 * Giá trị trong CSDL ở lần đọc trước. Mỗi thao tác đều đọc lại cả bảng; ô nào người dùng
 * đang gõ dở (khác giá trị đã đọc) thì giữ nguyên, để bật/tắt sự kiện khác hay lưu lịch
 * lỗi không xoá mất mốc vừa gõ. Ô chưa đụng tới thì theo giá trị mới của CSDL.
 */
const lichGoc: Record<string, { mo: string, dong: string }> = {}

watch(data, (d) => {
  if (!d) return
  for (const g of d.theoNhom) {
    for (const s of g.ds) {
      const moi = { mo: mocChoO(s.mo_luc), dong: mocChoO(s.dong_luc) }
      const cu = lichGoc[s.ma]
      const o = lichNhap[s.ma]
      if (!o || !cu || (o.mo === cu.mo && o.dong === cu.dong)) lichNhap[s.ma] = { ...moi }
      lichGoc[s.ma] = moi
    }
  }
}, { immediate: true })

const nhomHienThi = computed(() => {
  const t = timKiem.value.trim().toLowerCase()
  return (data.value?.theoNhom ?? [])
    .map(g => ({
      ...g,
      ds: g.ds.filter((s) => {
        if (chiLech.value && Number(s.bat) === Number(s.dang_bat)) return false
        if (!t) return true
        return `${s.ten ?? ''} ${s.ma} ${s.mo_ta ?? ''}`.toLowerCase().includes(t)
      }),
    }))
    .filter(g => g.ds.length > 0)
})

/** Gọi một thao tác rồi đọc lại bảng. Trả về true nếu thao tác thành công. */
async function goi(khoa: string, url: string, body: Record<string, unknown> = {}): Promise<boolean> {
  dangLam[khoa] = true
  let ok = false
  try {
    const kq = await api<{ message: string }>(url, { method: 'POST', body })
    toast.add({ title: kq.message, color: 'success', icon: 'i-lucide-check' })
    ok = true
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangLam[khoa] = false
    await refresh()
  }
  return ok
}

function doiBat(s: SuKien, bat: boolean) {
  goi(`bat:${s.ma}`, '/api/admin/sukien/bat-tat', { ma: s.ma, viec: bat ? 'bat' : 'tat' })
}

function luuLich(s: SuKien) {
  const o = lichNhap[s.ma] ?? { mo: '', dong: '' }
  goi(`lich:${s.ma}`, '/api/admin/sukien/lich', { ma: s.ma, mo_luc: o.mo, dong_luc: o.dong })
}

// ---- Hộp xác nhận cho thao tác khó gỡ (xoá lịch, tắt hết) ----
const xacNhan = ref<{ title: string, description: string, label: string, run: () => unknown } | null>(null)
const moXacNhan = computed({
  get: () => xacNhan.value !== null,
  set: (v: boolean) => { if (!v) xacNhan.value = null },
})
function chay() {
  const x = xacNhan.value
  xacNhan.value = null
  x?.run()
}

function hoiXoaLich(s: SuKien) {
  xacNhan.value = {
    title: 'Xoá lịch hẹn?',
    description: `Xoá lịch hẹn của sự kiện "${s.ten ?? s.ma}"?`,
    label: 'Xoá lịch',
    run: async () => {
      if (await goi(`xoa:${s.ma}`, '/api/admin/sukien/xoa-lich', { ma: s.ma })) lichNhap[s.ma] = { mo: '', dong: '' }
    },
  }
}

function hoiTatHet() {
  xacNhan.value = {
    title: 'Tắt hết sự kiện?',
    description: 'Tắt toàn bộ sự kiện đang đặt bật?',
    label: 'Tắt hết',
    run: () => goi('tat_het', '/api/admin/sukien/tat-het'),
  }
}

const lichCot = [
  { accessorKey: 'moc', header: 'Lúc' },
  { accessorKey: 've', header: 'Việc' },
  { accessorKey: 'ten', header: 'Sự kiện' },
]
</script>

<template>
  <AdminPage title="Sự kiện">
    <template #right>
      <UButton
        icon="i-lucide-refresh-cw"
        color="neutral"
        variant="ghost"
        :loading="status === 'pending'"
        aria-label="Tải lại"
        @click="refresh()"
      />
      <UButton
        v-if="duocGhi && data?.coBang && data.soSeBat > 0"
        icon="i-lucide-power-off"
        color="error"
        variant="soft"
        label="Tắt hết"
        :loading="dangLam.tat_het"
        @click="hoiTatHet"
      />
    </template>

    <p class="max-w-4xl text-sm text-muted">
      Bật / tắt từng sự kiện trong game, hoặc <strong class="text-highlighted">hẹn giờ</strong> mở và đóng.
      Trước đây mốc thời gian của mọi sự kiện được ghi cứng trong mã nguồn (toàn năm 2015–2024, nên đều đã
      tắt vĩnh viễn); nay chúng đọc thẳng từ bảng này nên <strong class="text-highlighted">bật lúc nào cũng được</strong>.
      Đổi xong server tự khởi động lại.
    </p>

    <div v-if="!data && status === 'pending'" class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <USkeleton v-for="i in 4" :key="i" class="h-24" />
    </div>

    <UAlert
      v-else-if="!data && error"
      color="error"
      variant="subtle"
      icon="i-lucide-circle-x"
      title="Không tải được danh sách sự kiện."
      :description="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'neutral', variant: 'outline', onClick: () => { refresh() } }]"
    />

    <UAlert
      v-else-if="data && !data.coBang"
      color="warning"
      variant="subtle"
      icon="i-lucide-database"
      title="Chưa đọc được bảng tob_su_kien."
    >
      <template #description>
        <p>Server tự tạo bảng này lần đầu khởi động sau khi cập nhật. Muốn tạo trước thì chạy:</p>
        <p class="mt-1">
          <code>mysql -u root htbb_db &lt; sql/them_bang_su_kien.sql</code>
        </p>
        <p class="mt-1 text-xs">
          Lỗi đọc được: {{ data.loiDoc }}
        </p>
      </template>
    </UAlert>

    <UAlert
      v-else-if="data && data.tong === 0"
      color="warning"
      variant="subtle"
      icon="i-lucide-inbox"
      title="Bảng đã có nhưng chưa có dòng nào."
    >
      <template #description>
        <p>
          Danh sách sự kiện do <em>game server</em> đăng lên mỗi lần khởi động, không phải do trang web tạo.
          Hãy khởi động lại server một lần rồi quay lại đây.
        </p>
        <p class="mt-1 text-xs">
          Nếu khởi động lại rồi mà vẫn trống thì server đang chạy bản jar cũ (chưa có <code>real/SuKien</code>) — build và cập nhật lại jar.
        </p>
      </template>
    </UAlert>

    <template v-else-if="data">
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <AdminStatCard
          label="Đang chạy trên server"
          :value="`${so(data.soDangBat)} / ${so(data.tong)}`"
          :note="data.mayChuLuc ? `tính lúc server khởi động ${data.mayChuLuc}` : ''"
          icon="i-lucide-server"
        />
        <AdminStatCard
          label="Đang đặt bật"
          :value="`${so(data.soSeBat)} / ${so(data.tong)}`"
          note="sẽ có hiệu lực sau lần khởi động tới"
          icon="i-lucide-toggle-right"
        />
        <AdminStatCard
          label="Chờ khởi động lại"
          :value="so(data.choKhoiDong.length)"
          :note="data.choKhoiDong.length > 0 ? 'thiết lập chưa vào game' : 'không có gì lệch'"
          :mau="data.choKhoiDong.length > 0 ? 'warn' : 'ok'"
          icon="i-lucide-rotate-cw"
        />
        <AdminStatCard
          label="Đang hẹn lịch"
          :value="so(data.lichSapToi.length)"
          :note="data.lichSapToi.length ? `gần nhất ${data.lichSapToi[0]!.moc}` : 'chưa hẹn mốc nào'"
          icon="i-lucide-calendar-clock"
        />
      </div>

      <UAlert
        v-if="!data.coCotLich"
        color="warning"
        variant="subtle"
        icon="i-lucide-calendar-x"
        title="Bảng chưa có hai cột lịch hẹn."
      >
        <template #description>
          <p>
            Bảng này được tạo trước ngày 30/08/2026 nên còn thiếu <code>mo_luc</code> và <code>dong_luc</code>.
            Phần bật/tắt bên dưới vẫn dùng bình thường, chỉ chưa hẹn giờ được.
          </p>
          <p class="mt-1 text-xs">
            Khởi động lại server một lần là xong — server tự thêm cột (<code>DatabaseSuKien.themCotThieu</code>). Hoặc chạy tay:
            <code>ALTER TABLE tob_su_kien ADD COLUMN mo_luc DATETIME NULL;</code>
            <code>ALTER TABLE tob_su_kien ADD COLUMN dong_luc DATETIME NULL;</code>
          </p>
        </template>
      </UAlert>

      <UAlert
        v-if="data.choKhoiDong.length"
        color="warning"
        variant="subtle"
        icon="i-lucide-hourglass"
        :title="`Có ${so(data.choKhoiDong.length)} sự kiện đang chờ vào game:`"
      >
        <template #description>
          <div class="flex flex-wrap gap-1.5">
            <UBadge v-for="d in data.choKhoiDong" :key="d.ma" color="warning" variant="outline">
              {{ d.ten }} {{ d.bat === 1 ? '→ bật' : '→ tắt' }}
            </UBadge>
          </div>
          <template v-if="data.tuKhoiDongLai === true">
            <p class="mt-2">
              <strong>Server sẽ tự khởi động lại.</strong> Nó đợi {{ so(data.gomGiay) }} giây không còn thay đổi nào nữa,
              báo trong game {{ so(data.baoTruocGiay) }} giây, rồi lưu dữ liệu và khởi động lại.
              Bạn không phải làm gì thêm — cứ đặt hết những gì cần đặt rồi để đó.
            </p>
            <p class="mt-1 text-xs">
              Đổi thêm một sự kiện nữa là đồng hồ gom chạy lại từ đầu, nên cả chùm thay đổi chỉ tốn <em>một</em> lần
              khởi động lại. Sau khi bật máy, server im 3 phút đầu mới bắt đầu tính — chỗ này cố ý, vì systemd chỉ cho
              khởi động 5 lần trong 300 giây, quá thì unit chết hẳn và không tự bật lại.
            </p>
          </template>
          <p v-else-if="data.tuKhoiDongLai === false" class="mt-2">
            <strong>Tự khởi động lại đang TẮT</strong> (<code>sv.suKienTuKhoiDongLai=0</code> trong <code>server.ini</code>).
            Phải tự khởi động lại: <code>systemctl restart htbb-game</code>.
          </p>
          <p v-else class="mt-2 text-xs">
            Không đọc được <code>server.ini</code> (web có thể nằm khác máy với game server) nên không biết tự khởi động
            lại đang bật hay tắt. Nếu tắt thì chạy tay: <code>systemctl restart htbb-game</code>.
          </p>
          <p class="mt-1 text-xs">
            Vì sao phải khởi động lại: bảng shop, <code>Map.isSale</code> và danh sách NPC được dựng <em>một lần</em> lúc
            bật máy theo trạng thái sự kiện, đổi giữa chừng thì được nửa này hỏng nửa kia.
          </p>
        </template>
      </UAlert>

      <UAlert
        v-if="data.anhChupHong.length"
        color="error"
        variant="subtle"
        icon="i-lucide-camera-off"
        :title="`Có ${so(data.anhChupHong.length)} dòng lệch mà không phải do ai sửa.`"
      >
        <template #description>
          <p>
            Cột <code>dang_bat</code> khác <code>bat</code> nhưng <code>sua_luc</code> lại cũ hơn lần khởi động gần nhất —
            nghĩa là lần bật máy vừa rồi server không ghi được ảnh chụp trạng thái.
          </p>
          <div class="mt-2 flex flex-wrap gap-1.5">
            <UBadge v-for="d in data.anhChupHong" :key="d.ma" color="error" variant="outline">
              {{ d.ten }}
            </UBadge>
          </div>
          <p class="mt-2 text-xs">
            Server <strong>cố tình không</strong> tự khởi động lại vì chuyện này: lệch kiểu đó không tự hết, nên nếu lấy nó
            làm cớ thì server sẽ khởi động lại vô hạn cho tới khi systemd bỏ cuộc và nằm luôn. Xem log <code>[SUKIEN]</code>
            lúc bật máy trong <ULink to="/admin/chandoan" class="underline">Chẩn đoán</ULink>, thường là lỗi kết nối CSDL.
          </p>
        </template>
      </UAlert>

      <UAlert
        v-if="data.congTongTat && data.soSeBat > 0"
        color="warning"
        variant="subtle"
        icon="i-lucide-door-closed"
        title="Cổng tổng đang tắt."
      >
        <template #description>
          <p>
            Hơn 70 chỗ trong mã nguồn hỏi <code>isFinishAllSuKien()</code> <em>trước</em> khi hỏi từng sự kiện. Để cổng
            tổng tắt thì nhiều nội dung của sự kiện bạn vừa bật sẽ không hiện ra.
          </p>
          <p class="mt-1 text-xs">
            Muốn chạy sự kiện cũ thì bật <strong>“Cổng tổng cho mọi sự kiện”</strong> ở nhóm đầu tiên.
          </p>
        </template>
      </UAlert>

      <UCard v-if="data.lichSapToi.length" :ui="{ body: 'p-0 sm:p-0' }">
        <template #header>
          <div class="flex flex-wrap items-center justify-between gap-2">
            <h3 class="font-semibold text-highlighted">
              Lịch sắp tới
            </h3>
            <span class="text-xs text-muted">giờ của CSDL, hiện là {{ data.bayGio }}</span>
          </div>
        </template>
        <UTable :data="data.lichSapToi" :columns="lichCot">
          <template #moc-cell="{ row }">
            <span class="whitespace-nowrap tabular-nums">{{ row.original.moc }}</span>
          </template>
          <template #ve-cell="{ row }">
            <UBadge :color="row.original.ve === 'mo' ? 'success' : 'neutral'" variant="subtle">
              {{ row.original.ve === 'mo' ? 'mở' : 'đóng' }}
            </UBadge>
          </template>
        </UTable>
      </UCard>

      <div class="flex flex-wrap items-center gap-3">
        <UInput v-model="timKiem" icon="i-lucide-search" placeholder="Tìm theo tên, mã, mô tả…" class="w-full sm:w-72" />
        <USwitch v-model="chiLech" label="Chỉ hiện sự kiện đang lệch" />
      </div>

      <section v-for="g in nhomHienThi" :key="g.nhom" class="space-y-3">
        <div class="flex items-center gap-2">
          <h3 class="text-base font-semibold text-highlighted">
            {{ g.nhom === '' ? 'Khác' : g.nhom }}
          </h3>
          <UBadge color="neutral" variant="subtle" size="sm">
            {{ g.ds.length }}
          </UBadge>
        </div>

        <div class="grid gap-4 md:grid-cols-2 2xl:grid-cols-3">
          <UCard
            v-for="s in g.ds"
            :key="s.ma"
            class="flex flex-col transition-colors"
            :class="[
              Number(s.bat) === 1 ? 'border-primary/40 bg-primary/5' : 'bg-elevated/30',
              s.ma === data.maCongTong ? 'ring-1 ring-primary/50' : '',
            ]"
            :ui="{ body: 'flex-1 p-4 sm:p-4', footer: 'p-4 sm:px-4 sm:py-3' }"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <div class="font-semibold text-highlighted">
                  {{ s.ten ?? s.ma }}
                </div>
                <code class="text-xs text-dimmed">{{ s.ma }}</code>
              </div>
              <USwitch
                :model-value="Number(s.bat) === 1"
                :loading="dangLam[`bat:${s.ma}`]"
                :disabled="!duocGhi || dangLam[`bat:${s.ma}`]"
                :aria-label="Number(s.bat) === 1 ? `Tắt ${s.ten}` : `Bật ${s.ten}`"
                size="lg"
                @update:model-value="(v: boolean) => doiBat(s, v)"
              />
            </div>

            <p v-if="s.mo_ta" class="mt-2 text-sm text-muted">
              {{ s.mo_ta }}
            </p>

            <div class="mt-3 flex flex-wrap items-center gap-1.5 text-xs">
              <span class="text-dimmed">Trên server</span>
              <UBadge :color="Number(s.dang_bat) === 1 ? 'success' : 'neutral'" variant="subtle" size="sm">
                {{ Number(s.dang_bat) === 1 ? 'đang bật' : 'tắt' }}
              </UBadge>
              <span class="ml-2 text-dimmed">Đặt</span>
              <UBadge
                :color="Number(s.bat) !== Number(s.dang_bat) ? 'warning' : (Number(s.bat) === 1 ? 'success' : 'neutral')"
                variant="subtle"
                size="sm"
              >
                {{ Number(s.bat) === 1 ? 'bật' : 'tắt' }}
              </UBadge>
              <span v-if="Number(s.bat) !== Number(s.dang_bat)" class="text-warning">chờ khởi động lại</span>
            </div>

            <div v-if="data.coCotLich" class="mt-4 rounded-lg border border-default p-3">
              <div class="mb-2 flex items-center gap-1.5 text-xs font-medium text-muted">
                <UIcon name="i-lucide-calendar-clock" class="size-3.5" />
                Lịch hẹn
              </div>
              <div class="grid gap-2 sm:grid-cols-2">
                <UFormField label="mở" size="xs">
                  <UInput v-model="lichNhap[s.ma]!.mo" type="datetime-local" size="sm" class="w-full" />
                </UFormField>
                <UFormField label="đóng" size="xs">
                  <UInput v-model="lichNhap[s.ma]!.dong" type="datetime-local" size="sm" class="w-full" />
                </UFormField>
              </div>
              <div v-if="duocGhi" class="mt-2 flex justify-end gap-2">
                <UButton
                  v-if="s.mo_luc || s.dong_luc"
                  label="Xoá lịch"
                  color="neutral"
                  variant="ghost"
                  size="xs"
                  :loading="dangLam[`xoa:${s.ma}`]"
                  @click="hoiXoaLich(s)"
                />
                <UButton
                  label="Lưu lịch"
                  color="primary"
                  variant="soft"
                  size="xs"
                  icon="i-lucide-save"
                  :loading="dangLam[`lich:${s.ma}`]"
                  @click="luuLich(s)"
                />
              </div>
            </div>

            <template #footer>
              <div class="text-xs text-dimmed">
                <template v-if="s.sua_luc">
                  Sửa lần cuối {{ s.sua_luc }} · {{ s.sua_boi }}
                </template>
                <template v-else>
                  Sửa lần cuối —
                </template>
              </div>
            </template>
          </UCard>
        </div>
      </section>

      <p v-if="!nhomHienThi.length" class="text-sm text-muted">
        Không có sự kiện nào khớp bộ lọc.
      </p>

      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Nên biết trước khi bật một sự kiện cũ
          </h3>
        </template>
        <dl class="grid gap-x-8 gap-y-4 text-sm md:grid-cols-2">
          <div>
            <dt class="font-medium text-highlighted">
              Bật cổng tổng trước
            </dt>
            <dd class="mt-1 text-muted">
              Rất nhiều nội dung sự kiện nằm sau <code>if (!Char.isFinishAllSuKien())</code>. Bỏ qua bước này là bật xong mà không thấy gì.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Sự kiện cũ có thể cần vật phẩm không còn trong bảng dữ liệu
            </dt>
            <dd class="mt-1 text-muted">
              Các bản sự kiện 2015–2016 tham chiếu id vật phẩm, NPC và bản đồ của thời đó. Bật xong nên tự vào game kiểm tra
              NPC tương ứng trước khi thông báo cho người chơi.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Tỉ lệ rơi đồ sự kiện đặt riêng
            </dt>
            <dd class="mt-1 text-muted">
              <code>sv.tileItemSuKien</code> trong <code>server.ini</code> nhân vào mọi tỉ lệ rơi vật phẩm sự kiện. Đang để thấp
              thì bật sự kiện lên người chơi vẫn gần như không nhặt được gì.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Sự kiện World Cup có cửa sổ thời gian riêng
            </dt>
            <dd class="mt-1 text-muted">
              Hai dòng <em>World Cup</em> ở đây đè lên cửa sổ <code>sv.worldCup*</code> trong <code>server.ini</code>. Lần đầu
              chạy sự kiện này còn phải chạy <code>sql/them_su_kien_world_cup.sql</code> một lần.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Lịch hẹn chỉ chạy một lần
            </dt>
            <dd class="mt-1 text-muted">
              Mốc nào chạy xong thì bị xoá khỏi bảng, không lặp lại năm sau. Sự kiện theo mùa thì mỗi năm đặt lại một lần.
              Mốc để trống nghĩa là không tự làm gì — hẹn mở mà không hẹn đóng thì sự kiện chạy tới khi bạn tự tắt.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Giờ ở đây là giờ của máy chủ CSDL
            </dt>
            <dd class="mt-1 text-muted">
              Không phải giờ trên máy bạn. Đang là <code>{{ data.bayGio }}</code>. Lệch giờ thì mốc hẹn sẽ chạy sớm hoặc muộn
              đúng bằng khoảng lệch đó.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Server tự khởi động lại, nhưng không tức thì
            </dt>
            <dd class="mt-1 text-muted">
              Sau khi đổi, server đợi {{ so(data.gomGiay) }} giây không còn thay đổi nào nữa rồi mới báo trong game
              {{ so(data.baoTruocGiay) }} giây và tắt. Bật liền mấy sự kiện vẫn chỉ tốn một lần khởi động lại. Ba phút đầu
              sau khi bật máy thì nó không làm gì cả — để hai lần khởi động lại luôn cách nhau trên 5 phút, là cửa sổ mà
              systemd đếm số lần khởi động.
            </dd>
          </div>
          <div>
            <dt class="font-medium text-highlighted">
              Mọi thao tác ở đây đều vào nhật ký
            </dt>
            <dd class="mt-1 text-muted">
              Xem lại ở mục <ULink to="/admin/nhatky" class="text-primary underline">Nhật ký admin</ULink>.
            </dd>
          </div>
        </dl>
      </UCard>
    </template>

    <UModal v-model:open="moXacNhan" :title="xacNhan?.title ?? ''" :description="xacNhan?.description ?? ''">
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton label="Thôi" color="neutral" variant="ghost" @click="moXacNhan = false" />
          <UButton :label="xacNhan?.label ?? 'Đồng ý'" color="error" @click="chay" />
        </div>
      </template>
    </UModal>
  </AdminPage>
</template>
