<script setup lang="ts">
/**
 * Chế độ bảo trì đăng nhập (tệp bao_tri.txt cạnh server.ini — HTBB/src/server/ChiSoMayChu.java):
 * xem trạng thái, bật (câu thông báo + tài khoản vẫn được vào) / tắt. Hỏi xác nhận trước khi đổi.
 * Web không ghi được thư mục game (mặc định trên VPS) -> hiện lệnh làm tay.
 */
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

interface TrangThai {
  thuMuc: string, tep: string, coServerIni: boolean, ghiDuoc: boolean, coTep: boolean
  noiDung: { cau: string, cho: string[] } | null, sua: string | null, cauHienThi: string | null, cauMacDinh: string
  game: { co: boolean, baoTri: boolean | null, cachGiay: number | null }
  gioiHan: { cau: number, soTaiKhoan: number }
  thuMucVps: string
}

const api = useAdminApi()
const toast = useToast()
const { duoc } = useAdminMe()
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)

const { data, status, error, refresh } = useAsyncData('admin-he-thong-bao-tri', () => api<TrangThai>('/api/admin/he-thong/bao-tri'), { server: false })
defineExpose({ refresh })

const cau = ref('')
const cho = ref('')
watch(data, (d) => {
  if (!d) return
  cau.value = d.noiDung?.cau ?? ''
  cho.value = (d.noiDung?.cho ?? []).join(', ')
}, { immediate: true })

const duocGhi = computed(() => duoc('/api/admin/he-thong/bao-tri', 'POST'))
const dsCho = computed(() => cho.value.split(/[,\s]+/).map(s => s.trim().toLowerCase()).filter(Boolean))
const loiNhap = computed(() => {
  const k = baoTriKiem({ bat: true, cau: cau.value, cho: dsCho.value })
  return k.ok ? '' : k.loi
})
const soKyTu = computed(() => Array.from(cau.value.trim()).length)
const dangGui = ref(false)

const ndDuKien = computed(() => ({ cau: cau.value.trim(), cho: dsCho.value }))
const thuMucTay = computed(() => (data.value?.coServerIni ? data.value.thuMuc : data.value?.thuMucVps ?? '/opt/htbb'))
const lenhBat = computed(() => baoTriLenhTay(thuMucTay.value, ndDuKien.value))
const lenhTat = computed(() => baoTriLenhTay(thuMucTay.value, null))

async function gui(bat: boolean) {
  if (bat && loiNhap.value) return
  const ok = await xacNhan.open(bat
    ? {
        title: data.value?.coTep ? 'Cập nhật bảo trì?' : 'Bật bảo trì đăng nhập?',
        description: `Trong tối đa 5 giây máy chủ từ chối MỌI lượt đăng nhập mới${dsCho.value.length ? ` (trừ: ${dsCho.value.join(', ')})` : ''}.\nNgười đang chơi KHÔNG bị đá — muốn đưa hết ra thì dừng máy chủ ở trang Máy chủ game.\n\nThông báo: "${baoTriCauHienThi(cau.value)}"`,
        nut: data.value?.coTep ? 'Cập nhật' : 'Bật bảo trì',
        mau: 'warning',
      }
    : { title: 'Tắt bảo trì?', description: 'Xoá tệp bao_tri.txt — trong tối đa 5 giây mọi người đăng nhập lại được.', nut: 'Mở lại đăng nhập', mau: 'primary' }).result
  if (!ok) return
  dangGui.value = true
  try {
    const r = await api<{ message: string }>('/api/admin/he-thong/bao-tri', { method: 'POST', body: bat ? { bat: true, cau: cau.value.trim(), cho: dsCho.value } : { bat: false } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Chưa đổi được', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangGui.value = false
  }
}

async function chep(s: string) {
  try {
    await navigator.clipboard.writeText(s)
    toast.add({ title: 'Đã chép lệnh', color: 'success', icon: 'i-lucide-clipboard-check' })
  } catch {
    toast.add({ title: 'Không chép được — bôi đen rồi chép tay', color: 'warning' })
  }
}
const lechGame = computed(() => {
  const d = data.value
  if (!d || d.game.baoTri === null || d.game.cachGiay === null || d.game.cachGiay > 90) return false
  return d.game.baoTri !== d.coTep
})
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-wrap items-center gap-2">
        <UIcon name="i-lucide-construction" class="size-5 text-muted" />
        <h2 class="font-semibold text-highlighted">Bảo trì đăng nhập</h2>
        <template v-if="data">
          <UBadge v-if="data.coTep" color="warning" variant="subtle" icon="i-lucide-lock">Đang chặn đăng nhập mới</UBadge>
          <UBadge v-else color="success" variant="subtle" icon="i-lucide-lock-open">Đang mở</UBadge>
        </template>
        <UButton class="ml-auto" size="sm" color="neutral" variant="ghost" icon="i-lucide-refresh-cw" :loading="status === 'pending'" aria-label="Tải lại trạng thái bảo trì" @click="refresh()" />
      </div>
    </template>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không đọc được trạng thái bảo trì" :description="loiApi(error)" />
    <div v-else-if="!data" class="space-y-2">
      <USkeleton class="h-6 w-1/2" />
      <USkeleton class="h-20 w-full" />
    </div>

    <div v-else class="space-y-4 text-sm">
      <dl class="grid gap-x-6 gap-y-2 sm:grid-cols-2">
        <div>
          <dt class="text-muted">Tệp trên máy chủ web</dt>
          <dd class="break-all font-mono text-xs text-highlighted">{{ data.tep }}</dd>
          <dd class="text-xs text-dimmed">{{ data.coTep ? `có — sửa lúc ${new Date(data.sua!).toLocaleString('vi-VN')}` : 'không có (đang mở)' }}</dd>
        </div>
        <div>
          <dt class="text-muted">Game tự báo (tob_chi_so_may_chu)</dt>
          <dd class="text-highlighted">
            <template v-if="!data.game.co">Chưa có bảng chỉ số</template>
            <template v-else-if="data.game.baoTri === null">Chưa có mẫu nào</template>
            <template v-else>{{ data.game.baoTri ? 'Đang bảo trì' : 'Đang mở' }}<span class="text-xs text-dimmed"> · mẫu cách đây {{ data.game.cachGiay }} giây</span></template>
          </dd>
          <dd v-if="data.game.cachGiay !== null && data.game.cachGiay > 90" class="text-xs text-warning">Mẫu đã cũ — máy chủ game có thể đang tắt.</dd>
        </div>
      </dl>

      <UAlert v-if="lechGame" color="warning" variant="subtle" icon="i-lucide-git-compare"
        title="Tệp và cờ game đang khác nhau"
        description="Game đọc tệp mỗi 5 giây và ghi mẫu mỗi 30 giây — vừa đổi thì chờ ít giây. Lệch mãi nghĩa là web và game KHÔNG dùng chung thư mục (web ở máy khác, hoặc HTBB_SERVER_DIR trỏ sai)." />

      <div v-if="data.coTep && data.noiDung" class="rounded-lg border border-default p-3">
        <p class="text-muted">Người chơi đang thấy:</p>
        <p class="font-medium text-highlighted">"{{ data.cauHienThi }}"</p>
        <p class="mt-1 text-xs text-muted">Vẫn được vào: {{ data.noiDung.cho.length ? data.noiDung.cho.join(', ') : 'không ai' }}</p>
      </div>

      <form class="space-y-3" @submit.prevent="gui(true)">
        <UFormField label="Câu thông báo cho người chơi" :help="`Để trống = câu mặc định: “${data.cauMacDinh}”`" :hint="`${soKyTu}/${data.gioiHan.cau}`">
          <UInput v-model="cau" :maxlength="data.gioiHan.cau" :disabled="!duocGhi" placeholder="Bảo trì cập nhật phiên bản, mở lại lúc 15:00" class="w-full" />
        </UFormField>
        <UFormField label="Tài khoản vẫn được đăng nhập" :help="`Tên TÀI KHOẢN (không phải tên Pokémon), cách nhau dấu phẩy, tối đa ${data.gioiHan.soTaiKhoan}. Dùng để GM vào kiểm tra trước khi mở.`">
          <UInput v-model="cho" :disabled="!duocGhi" placeholder="admin, gm01" class="w-full" autocomplete="off" />
        </UFormField>
        <p v-if="loiNhap && (cau || cho)" class="text-xs text-error">{{ loiNhap }}</p>
        <div v-if="duocGhi && data.ghiDuoc" class="flex flex-wrap gap-2">
          <UButton type="submit" color="warning" icon="i-lucide-lock" :loading="dangGui" :disabled="!!loiNhap" :label="data.coTep ? 'Cập nhật bảo trì' : 'Bật bảo trì'" />
          <UButton v-if="data.coTep" color="primary" variant="outline" icon="i-lucide-lock-open" :loading="dangGui" label="Tắt bảo trì" @click="gui(false)" />
        </div>
        <p v-else-if="!duocGhi" class="text-xs text-muted">Chỉ siêu quản trị bật/tắt được bảo trì.</p>
      </form>

      <UAlert v-if="!data.ghiDuoc" color="info" variant="subtle" icon="i-lucide-terminal">
        <template #title>Web không ghi được thư mục game — làm tay trên VPS</template>
        <template #description>
          <p>
            <template v-if="!data.coServerIni">Thư mục web đang trỏ tới (<code>{{ data.thuMuc }}</code>, biến <code>HTBB_SERVER_DIR</code>) không có <code>server.ini</code>, nên web không chắc đó là thư mục game.</template>
            <template v-else>Tiến trình web không có quyền ghi <code>{{ data.thuMuc }}</code>.</template>
            Trên VPS, game chạy ở <code>{{ data.thuMucVps }}</code> bằng user <code>htbb</code> (HTBB/vps/htbb-game.service), còn web chạy bằng <code>htbb-web</code> với
            <code>ReadOnlyPaths=/opt/htbb</code> (deploy/htbb-web.service) — cố ý không cho web ghi vào thư mục game.
            SSH vào VPS rồi chạy:
          </p>
          <div class="mt-2 space-y-2">
            <div>
              <p class="text-xs font-semibold">Bật / cập nhật (theo ô đã nhập ở trên):</p>
              <div class="flex items-start gap-2">
                <pre class="min-w-0 flex-1 overflow-x-auto rounded bg-elevated p-2 text-xs">{{ lenhBat }}</pre>
                <UButton size="xs" color="neutral" variant="outline" icon="i-lucide-copy" aria-label="Chép lệnh bật" @click="chep(lenhBat)" />
              </div>
            </div>
            <div>
              <p class="text-xs font-semibold">Tắt (mở lại đăng nhập):</p>
              <div class="flex items-start gap-2">
                <pre class="min-w-0 flex-1 overflow-x-auto rounded bg-elevated p-2 text-xs">{{ lenhTat }}</pre>
                <UButton size="xs" color="neutral" variant="outline" icon="i-lucide-copy" aria-label="Chép lệnh tắt" @click="chep(lenhTat)" />
              </div>
            </div>
          </div>
          <p class="mt-2 text-xs">Game nhận ra trong 5 giây, không cần khởi động lại. Trạng thái thật xem ở dòng "Game tự báo" phía trên (mẫu 30 giây).</p>
        </template>
      </UAlert>
    </div>
  </UCard>
</template>
