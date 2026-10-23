<script setup lang="ts">
/**
 * Báo cáo "Nghi clone" (chống clone chuyển xu, chủ game duyệt 19/10/2026): người nhận có nhiều người gửi / người gửi
 * dùng chung thiết bị, và các lần bị chặn. API /api/admin/kinh-te/nghi-clone (bảng tob_log_chuyen_gia_tri).
 * Cổng chặn chỉnh ở Cấu hình hệ thống › Chống clone & chuyển xu (chong_clone.*).
 */
interface NguoiNhan { id: number, ten: string, cap: number, soNguoiGui: number, soLan: number, tongXu: number, soNghi: number, soBiChan: number, soMay: number, soIp: number, cungMayNguoiNhan: number, lanCuoi: string | null }
interface MayChung { may: string, soPokemon: number, soTaiKhoan: number, soNguoiNhan: number, tongXu: number, dsGui: string, dsNhan: string }
interface ChiTiet { id: number, luc: string, kieu: string, gui: string, guiCap: number, guiMay: string, guiIp: string, nhan: string, nhanCap: number, xu: number, chiTiet: string, biChan: number, nghi: number, lyDo: string }
interface DuLieu { coBang: boolean, ngay: number, nguoiNhan: NguoiNhan[], mayChung: MayChung[], chiTiet: ChiTiet[] }

const api = useAdminApi()
const ngay = ref(7)
const { data, status, refresh } = useAsyncData('admin-nghi-clone', () => api<DuLieu>('/api/admin/kinh-te/nghi-clone', { query: { ngay: ngay.value } }), { server: false, watch: [ngay] })

const TEN_KIEU: Record<string, string> = {
  giao_dich: 'Giao dịch', ky_gui: 'Ký gửi', thu_cung: 'Thú cưng', cho_trang_tri: 'Chợ trang trí', cho_sinh_thai: 'Chợ sinh thái', thach_dau: 'Thách đấu',
}
const cotNhan = [
  { key: 'ten', label: 'Người nhận', sort: true },
  { key: 'cap', label: 'Cấp', so: true, sort: true, anMobile: true },
  { key: 'soNguoiGui', label: 'Số người gửi', so: true, sort: true },
  { key: 'soMay', label: 'Thiết bị gửi', so: true, sort: true, anMobile: true },
  { key: 'soIp', label: 'IP gửi', so: true, sort: true, anMobile: true },
  { key: 'tongXu', label: 'Tổng xu', so: true, sort: true },
  { key: 'soNghi', label: 'Lần nghi', so: true, sort: true },
  { key: 'soBiChan', label: 'Bị chặn', so: true, sort: true },
  { key: 'lanCuoi', label: 'Lần cuối', anMobile: true },
]
const cotMay = [
  { key: 'may', label: 'Thiết bị (IMEI / mã máy)' },
  { key: 'soPokemon', label: 'Pokémon gửi', so: true, sort: true },
  { key: 'soTaiKhoan', label: 'Tài khoản', so: true, sort: true },
  { key: 'soNguoiNhan', label: 'Người nhận', so: true, sort: true },
  { key: 'tongXu', label: 'Tổng xu', so: true, sort: true },
  { key: 'dsGui', label: 'Pokémon gửi', anMobile: true },
  { key: 'dsNhan', label: 'Gửi cho', anMobile: true },
]
const cotChiTiet = [
  { key: 'luc', label: 'Lúc', sort: true },
  { key: 'kieu', label: 'Đường', giaTri: (r: ChiTiet) => TEN_KIEU[r.kieu] ?? r.kieu },
  { key: 'gui', label: 'Gửi' },
  { key: 'nhan', label: 'Nhận' },
  { key: 'xu', label: 'Xu', so: true, sort: true },
  { key: 'lyDo', label: 'Lý do', giaTri: (r: ChiTiet) => (r.biChan ? r.chiTiet : r.lyDo) },
  { key: 'guiMay', label: 'Thiết bị gửi', anMobile: true },
]
</script>

<template>
  <UCard>
    <template #header>
      <div class="flex flex-wrap items-center justify-between gap-2">
        <div>
          <h2 class="font-semibold text-highlighted">Nghi clone — chuyển xu nhiều-về-một</h2>
          <p class="mt-1 text-xs text-muted">
            Mọi lần chuyển xu / giá trị giữa hai người chơi (giao dịch, ký gửi, thú cưng lấy xu, chợ trang trí, chợ sinh thái) và lần bị chặn.
            "Nghi" = một người nhận có từ 3 người gửi trong ngày, người gửi chung thiết bị / IP, hoặc gửi từ chính thiết bị của người nhận.
            Ngưỡng và cổng chặn ở Cấu hình hệ thống › Chống clone &amp; chuyển xu.
          </p>
        </div>
        <div class="flex items-center gap-2">
          <USelect v-model="ngay" :items="[{ label: '24 giờ', value: 1 }, { label: '7 ngày', value: 7 }, { label: '30 ngày', value: 30 }]" size="sm" class="w-28" />
          <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" size="sm" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
        </div>
      </div>
    </template>
    <UAlert v-if="data && !data.coBang" color="neutral" variant="subtle" icon="i-lucide-info"
      title="Chưa có bảng tob_log_chuyen_gia_tri"
      description="Máy chủ game bản mới tự tạo bảng ở lần chuyển xu đầu tiên; hoặc chạy sql/htbb.sql." />
    <div v-else class="space-y-6">
      <div>
        <h3 class="mb-2 text-sm font-semibold text-highlighted">Người nhận từ nhiều người gửi</h3>
        <AdminBang :rows="data?.nguoiNhan" :cot="cotNhan" :dang-tai="status === 'pending'" ten-csv="nghi-clone-nguoi-nhan" :moi-trang="10" rong-tieu-de="Không có người nhận đáng ngờ">
          <template #cell-ten="{ row }">
            <NuxtLink :to="`/admin/nguoi-choi?q=${encodeURIComponent(row.ten)}`" class="font-medium text-primary hover:underline">{{ row.ten }}</NuxtLink>
            <UBadge v-if="row.cungMayNguoiNhan > 0" color="error" variant="subtle" size="sm" class="ml-1">gửi từ máy người nhận</UBadge>
          </template>
        </AdminBang>
      </div>
      <div>
        <h3 class="mb-2 text-sm font-semibold text-highlighted">Thiết bị có nhiều Pokémon cùng gửi xu</h3>
        <AdminBang :rows="data?.mayChung" :cot="cotMay" :dang-tai="status === 'pending'" ten-csv="nghi-clone-thiet-bi" :moi-trang="10" rong-tieu-de="Không thiết bị nào có từ 2 Pokémon gửi xu" />
      </div>
      <div>
        <h3 class="mb-2 text-sm font-semibold text-highlighted">Lần nghi / bị chặn gần đây</h3>
        <AdminBang :rows="data?.chiTiet" :cot="cotChiTiet" :dang-tai="status === 'pending'" ten-csv="nghi-clone-chi-tiet" :moi-trang="15" rong-tieu-de="Chưa có lần nghi hay bị chặn">
          <template #cell-gui="{ row }">
            {{ row.gui }} <span class="text-xs text-dimmed">c{{ row.guiCap }}</span>
            <UBadge v-if="row.biChan" color="warning" variant="subtle" size="sm" class="ml-1">bị chặn</UBadge>
            <UBadge v-else-if="row.nghi" color="error" variant="subtle" size="sm" class="ml-1">nghi</UBadge>
          </template>
          <template #cell-nhan="{ row }">
            {{ row.nhan }} <span class="text-xs text-dimmed">c{{ row.nhanCap }}</span>
          </template>
        </AdminBang>
      </div>
    </div>
  </UCard>
</template>
