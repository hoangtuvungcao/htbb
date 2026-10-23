<script setup lang="ts">
/**
 * Bật/tắt server game (htbb-game) từ xa, không cần SSH/CLI — một khối của trang /admin/he-thong
 * (trước là trang riêng /admin/server, nay gộp để còn MỘT trang "Máy chủ & bảo trì").
 *
 * Trang này KHÔNG tự bật/tắt gì: nó chỉ ghi "trạng thái mong muốn" vào bảng
 * `dieu_khien_server`; watcher vps/dieu-khien-server.sh (cron của root, mỗi phút) đọc
 * bảng rồi gọi systemctl. Riêng "Khởi động lại ngay" dùng cờ 5h_systems.restart mà server
 * đang sống tự poll mỗi 5 giây.
 *
 * Tắt/Bật làm VĂNG TẤT CẢ người chơi đang online, nên Tắt bắt gõ đúng cụm xác nhận.
 */
interface TrangThai {
  muon_chay: number
  gio_hen: string | null
  da_ap_dung: number
  trang_thai_thuc_te: string | null
  lan_kiem_cuoi: string | null
  yeu_cau_boi: string | null
  yeu_cau_luc: string | null
  giay_tu_lan_kiem: number | null
  dang_hen_gio: number | null
}

interface DuLieu {
  coBang: boolean
  loiDoc: string
  tt: TrangThai | null
  watcherSong: boolean
  dangCho: boolean
  xacNhanTat: string
  nguongImLangGiay: number
}

const api = useAdminApi()
const toast = useToast()
// Vai trò Chỉ xem thấy tình trạng nhưng không thấy nút bật/tắt (server cũng chặn, 403).
const me = useAdminMe()
const duocGhi = computed(() => me.duoc('/api/admin/server/bat-ngay', 'POST'))

const { data, status, error, refresh } = useAsyncData('admin-server', () => api<DuLieu>('/api/admin/server'), { server: false })

// Nhịp tim watcher thay đổi mỗi phút — tự đọc lại cho khỏi phải bấm.
useIntervalFn(() => { if (document.visibilityState === 'visible') refresh() }, 30_000)

const gioHen = ref('')
const xacNhanGo = ref('')
const dangLam = reactive<Record<string, boolean>>({})

async function goi(khoa: string, url: string, body: Record<string, unknown> = {}) {
  dangLam[khoa] = true
  try {
    const kq = await api<{ message: string }>(url, { method: 'POST', body })
    toast.add({ title: kq.message, color: 'success', icon: 'i-lucide-check' })
    if (khoa === 'tat') xacNhanGo.value = ''
    if (khoa === 'hen') gioHen.value = ''
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangLam[khoa] = false
    await refresh()
  }
}

const tt = computed(() => data.value?.tt ?? null)
const muonChay = computed(() => Number(tt.value?.muon_chay) === 1)
const nhanXacNhan = computed(() => `Gõ đúng cụm "${data.value?.xacNhanTat ?? ''}" để xác nhận`)

// ---- Hộp xác nhận: Tắt và Khởi động lại đều làm văng người chơi đang online ----
const xacNhan = ref<{ title: string, description: string, label: string, run: () => void } | null>(null)
const moXacNhan = computed({
  get: () => xacNhan.value !== null,
  set: (v: boolean) => { if (!v) xacNhan.value = null },
})
function chay() {
  const x = xacNhan.value
  xacNhan.value = null
  x?.run()
}

function hoiTat() {
  xacNhan.value = {
    title: 'TẮT SERVER?',
    description: 'TẮT SERVER — văng tất cả người chơi đang online và giữ tắt cho tới khi bật lại?',
    label: 'Tắt server ngay',
    run: () => goi('tat', '/api/admin/server/tat-ngay', { xac_nhan: xacNhanGo.value }),
  }
}

function hoiRestart() {
  xacNhan.value = {
    title: 'Khởi động lại server?',
    description: 'Khởi động lại server ngay? Người chơi đang online sẽ bị văng ra trong ít giây.',
    label: 'Khởi động lại ngay',
    run: () => goi('restart', '/api/admin/server/restart-ngay'),
  }
}
</script>

<template>
  <section id="dieu-khien" class="scroll-mt-20 space-y-4">
    <div class="flex items-center justify-between gap-2">
      <div>
        <h2 class="text-lg font-semibold text-highlighted">Bật / tắt / khởi động lại máy chủ game</h2>
        <p class="text-sm text-muted">
          Điều khiển service <code>htbb-game</code> trên VPS từ xa, không cần SSH/CLI.
        </p>
      </div>
      <UButton
        icon="i-lucide-refresh-cw"
        color="neutral"
        variant="ghost"
        :loading="status === 'pending'"
        aria-label="Tải lại trạng thái máy chủ"
        @click="refresh()"
      />
    </div>

    <USkeleton v-if="!data && status === 'pending'" class="h-48" />

    <UAlert
      v-else-if="!data && error"
      color="error"
      variant="subtle"
      icon="i-lucide-circle-x"
      title="Không tải được trạng thái server."
      :description="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'neutral', variant: 'outline', onClick: () => { refresh() } }]"
    />

    <UAlert
      v-else-if="data && !data.coBang"
      color="warning"
      variant="subtle"
      icon="i-lucide-database"
      title="Chưa có bảng dữ liệu."
    >
      <template #description>
        <p>Chạy một lần trên máy chủ:</p>
        <p class="mt-1">
          <code>sudo bash /opt/htbb/vps/nang_cap_csdl.sh</code>
        </p>
        <p class="mt-1 text-xs">
          Lỗi đọc được: {{ data.loiDoc }}
        </p>
      </template>
    </UAlert>

    <UAlert
      v-else-if="data && !tt"
      color="warning"
      variant="subtle"
      icon="i-lucide-database"
      title="Bảng dieu_khien_server chưa có dòng id=1."
      description="Chạy lại sql/htbb.sql để thêm dòng mặc định."
    />

    <template v-else-if="data && tt">
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Tình trạng hiện tại
          </h3>
        </template>
        <dl class="grid gap-x-8 gap-y-4 text-sm sm:grid-cols-[max-content_1fr]">
          <dt class="text-muted">
            Trạng thái mong muốn
          </dt>
          <dd>
            <UBadge :color="muonChay ? 'success' : 'error'" variant="subtle">
              {{ muonChay ? 'Đang chạy' : 'Đã tắt' }}
            </UBadge>
          </dd>

          <dt class="text-muted">
            Trạng thái thực tế (watcher báo cáo)
          </dt>
          <dd class="flex flex-wrap items-center gap-2">
            <span v-if="tt.trang_thai_thuc_te === null" class="text-dimmed">chưa có báo cáo nào</span>
            <UBadge v-else-if="tt.trang_thai_thuc_te === 'active'" color="success" variant="subtle">
              active (đang chạy)
            </UBadge>
            <UBadge v-else color="error" variant="subtle">
              {{ tt.trang_thai_thuc_te }}
            </UBadge>
            <span v-if="tt.lan_kiem_cuoi !== null" class="text-xs text-dimmed">
              — lúc {{ tt.lan_kiem_cuoi }} ({{ so(Math.max(0, Number(tt.giay_tu_lan_kiem ?? 0))) }}s trước)
            </span>
          </dd>

          <dt class="text-muted">
            Watcher (canh gác trên VPS)
          </dt>
          <dd>
            <UBadge :color="data.watcherSong ? 'success' : 'warning'" variant="subtle">
              {{ data.watcherSong ? 'đang sống' : 'im lặng / chưa cài' }}
            </UBadge>
          </dd>

          <template v-if="data.dangCho">
            <dt class="text-muted">
              Đang chờ xử lý
            </dt>
            <dd>
              <p>
                <template v-if="Number(tt.dang_hen_gio) === 1">
                  Sẽ <strong class="text-highlighted">{{ muonChay ? 'BẬT' : 'TẮT' }}</strong> lúc
                  <strong class="text-highlighted">{{ tt.gio_hen }}</strong>
                </template>
                <template v-else>
                  Sẽ <strong class="text-highlighted">{{ muonChay ? 'BẬT' : 'TẮT' }}</strong> ngay khi watcher chạy lần tiếp theo
                </template>
                <span class="text-muted">(yêu cầu bởi {{ tt.yeu_cau_boi ?? '' }} lúc {{ tt.yeu_cau_luc ?? '' }})</span>
              </p>
              <UButton
                v-if="duocGhi"
                class="mt-2"
                label="Huỷ yêu cầu này"
                color="neutral"
                variant="outline"
                size="xs"
                icon="i-lucide-x"
                :loading="dangLam.huy"
                @click="goi('huy', '/api/admin/server/huy-lenh')"
              />
            </dd>
          </template>
        </dl>
      </UCard>

      <UAlert
        v-if="!data.watcherSong"
        color="warning"
        variant="subtle"
        icon="i-lucide-radio-tower"
        title="Watcher chưa cài hoặc đã dừng."
      >
        <template #description>
          <p>
            Nút Bật/Tắt bên dưới vẫn ghi được yêu cầu vào database, nhưng sẽ KHÔNG có gì xảy ra cho tới khi cài xong tiến
            trình canh gác này trên VPS (chỉ cần làm một lần):
          </p>
          <p class="mt-1">
            <code>sudo crontab -e</code>
          </p>
          <p class="mt-1">
            Thêm dòng:
          </p>
          <p class="mt-1">
            <code>* * * * * /opt/htbb/vps/dieu-khien-server.sh &gt;&gt;/opt/htbb/log/dieu-khien-server.log 2&gt;&amp;1</code>
          </p>
          <p class="mt-1 text-xs">
            Chi tiết/an toàn: xem chú thích đầu file <code>vps/dieu-khien-server.sh</code>.
          </p>
        </template>
      </UAlert>

      <div v-if="duocGhi" class="grid gap-6 lg:grid-cols-2">
        <UCard>
          <template #header>
            <h3 class="flex items-center gap-2 font-semibold text-highlighted">
              <UIcon name="i-lucide-power" class="size-4 text-success" />
              Bật server
            </h3>
          </template>
          <p class="text-sm text-muted">
            Áp dụng ngay lần watcher chạy tiếp theo (chậm nhất ~1 phút).
          </p>
          <UButton
            class="mt-3"
            label="Bật server ngay"
            color="primary"
            icon="i-lucide-play"
            :loading="dangLam.bat"
            @click="goi('bat', '/api/admin/server/bat-ngay')"
          />

          <USeparator class="my-6" />

          <h4 class="font-semibold text-highlighted">
            Đặt giờ tự bật
          </h4>
          <p class="mt-1 text-sm text-muted">
            Dùng khi bạn biết trước sẽ không có mặt đúng lúc cần bật.
          </p>
          <form class="mt-3 flex flex-wrap items-end gap-3" @submit.prevent="goi('hen', '/api/admin/server/hen-gio', { gio_hen_dat: gioHen })">
            <UFormField label="Giờ bật (theo giờ hệ thống VPS)" required>
              <UInput v-model="gioHen" type="datetime-local" required />
            </UFormField>
            <UButton type="submit" label="Đặt lịch" color="primary" variant="soft" icon="i-lucide-calendar-clock" :loading="dangLam.hen" />
          </form>
        </UCard>

        <UCard class="border-error/30">
          <template #header>
            <h3 class="flex items-center gap-2 font-semibold text-error">
              <UIcon name="i-lucide-power-off" class="size-4" />
              Tắt server
            </h3>
          </template>
          <UAlert
            color="error"
            variant="subtle"
            icon="i-lucide-triangle-alert"
            title="Văng TẤT CẢ người chơi đang online."
            description="Server sẽ ở trạng thái tắt hẳn cho tới khi có ai đó bấm Bật lại (ở đây hoặc bằng CLI) — khác với Khởi động lại, KHÔNG tự bật lại."
          />
          <form class="mt-4 space-y-3" @submit.prevent="hoiTat">
            <UFormField :label="nhanXacNhan">
              <UInput v-model="xacNhanGo" autocomplete="off" class="w-full sm:w-72" />
            </UFormField>
            <UButton
              type="submit"
              label="Tắt server ngay"
              color="error"
              icon="i-lucide-power-off"
              :disabled="xacNhanGo !== data.xacNhanTat"
              :loading="dangLam.tat"
            />
          </form>

          <USeparator class="my-6" />

          <h4 class="font-semibold text-highlighted">
            Khởi động lại ngay (không tắt hẳn)
          </h4>
          <p class="mt-1 text-sm text-muted">
            Cơ chế khác, có sẵn từ trước, KHÔNG cần watcher — chỉ dùng được khi server đang chạy: server tự lưu dữ liệu rồi
            tự khởi động lại (~10-15 giây).
          </p>
          <UButton
            class="mt-3"
            label="Khởi động lại ngay"
            color="neutral"
            variant="outline"
            icon="i-lucide-rotate-cw"
            :loading="dangLam.restart"
            @click="hoiRestart"
          />
        </UCard>
      </div>
    </template>

    <UModal v-model:open="moXacNhan" :title="xacNhan?.title ?? ''" :description="xacNhan?.description ?? ''">
      <template #footer>
        <div class="flex w-full justify-end gap-2">
          <UButton label="Thôi" color="neutral" variant="ghost" @click="moXacNhan = false" />
          <UButton :label="xacNhan?.label ?? 'Đồng ý'" color="error" @click="chay" />
        </div>
      </template>
    </UModal>
  </section>
</template>
