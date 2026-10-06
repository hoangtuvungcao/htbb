<script setup lang="ts">
/**
 * Reset toàn server về trắng. KHÔNG THỂ HOÀN TÁC.
 * Hai lớp xác nhận để không thể bấm nhầm: gõ đúng cụm "XOA TOAN BO" mới bật nút, rồi một hộp hỏi lại
 * kèm số nhân vật / tài khoản. Server kiểm lại cụm xác nhận. Xem server/utils/resetToanBo.ts.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

interface SoLieu {
  soNhanVat: number, soTaiKhoan: number, soLenhChoXuLy: number
  dsAdmin: string[], cumTu: string, loiDoc: string
}

const api = useAdminApi()
const toast = useToast()

const { data, status, error, refresh } = useAsyncData('admin-reset-toanbo',
  () => api<SoLieu>('/api/admin/reset-toanbo'), { server: false })

const xacNhan = ref('')
const moXacNhan = ref(false)
const dangGui = ref(false)
const dungCum = computed(() => !!data.value && xacNhan.value === data.value.cumTu)

async function gui() {
  dangGui.value = true
  try {
    const kq = await api<{ thongBao: string }>('/api/admin/reset-toanbo', { method: 'POST', body: { xac_nhan: xacNhan.value } })
    toast.add({ title: 'Đã xếp hàng đợi', description: kq.thongBao, color: 'success', duration: 15000 })
    xacNhan.value = ''
  } catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error' })
  } finally {
    dangGui.value = false
    moXacNhan.value = false
  }
  await refresh()
}
</script>

<template>
  <AdminPage title="Reset toàn server">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="outline" label="Cập nhật số liệu" :loading="status === 'pending'" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-xl font-bold text-error">
        Reset toàn server về trắng
      </h1>
      <p class="text-sm text-muted">
        Xoá vĩnh viễn nhân vật của MỌI tài khoản (trừ quản trị) — dùng khi cần làm mới toàn bộ server.
      </p>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${loiApi(error)}`" />
    <UAlert v-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <UCard>
      <template #header>
        <h2 class="font-semibold text-highlighted">
          Tình trạng hiện tại
        </h2>
      </template>
      <div class="grid gap-4 sm:grid-cols-3">
        <AdminStatCard label="Nhân vật sẽ bị xoá" :value="data?.soNhanVat ?? '…'" icon="i-lucide-shield" />
        <AdminStatCard label="Tài khoản bị ảnh hưởng" :value="data?.soTaiKhoan ?? '…'" icon="i-lucide-user-round" />
        <AdminStatCard
          label="Lệnh xoá đang chờ server xử lý"
          :value="data?.soLenhChoXuLy ?? '…'"
          :mau="(data?.soLenhChoXuLy ?? 0) > 0 ? 'warn' : ''"
          icon="i-lucide-hourglass"
        />
      </div>
      <div class="mt-4 flex flex-wrap items-center gap-2 text-sm">
        <span class="text-muted">Tài khoản quản trị được giữ lại:</span>
        <template v-if="data?.dsAdmin.length">
          <UBadge v-for="a in data.dsAdmin" :key="a" color="neutral" variant="outline" :label="a" />
        </template>
        <span v-else class="text-muted">(ADMIN_ACCOUNTS đang rỗng)</span>
      </div>
      <p class="mt-3 text-xs text-muted">
        Bấm "Cập nhật số liệu" để xem tiến độ sau khi đã gửi lệnh — hai số đầu sẽ giảm dần khi server xử lý xong
        từng nhân vật (khoảng 1 nhân vật/giây).
      </p>
    </UCard>

    <UCard class="ring-error/40">
      <template #header>
        <h2 class="font-semibold text-error">
          Xoá tất cả
        </h2>
      </template>
      <UAlert color="error" variant="subtle" icon="i-lucide-octagon-alert" title="KHÔNG THỂ HOÀN TÁC.">
        <template #description>
          Xoá vĩnh viễn toàn bộ <strong>{{ so(data?.soNhanVat) }} nhân vật</strong> của
          <strong>{{ so(data?.soTaiKhoan) }} tài khoản</strong> (cùng dữ liệu riêng: thú nuôi, trang trại, đá quý...)
          và đưa cả {{ so(data?.soTaiKhoan) }} tài khoản đó về <strong>CHƯA KÍCH HOẠT</strong>. Các tài khoản trong
          <code>ADMIN_ACCOUNTS</code> được giữ nguyên, không tài khoản nào bị xoá — chỉ nhân vật bị xoá và mất kích hoạt,
          ai đăng nhập lại đều thấy tài khoản trắng như vừa đăng ký và phải bấm "Kích hoạt ngay" lại mới vào game được.
          Nhân vật đang online sẽ bị đẩy ra khỏi game trước khi xoá. Nhật ký giao dịch/log vẫn được giữ lại để tra cứu.
        </template>
      </UAlert>

      <p v-if="data && data.soNhanVat === 0" class="mt-4 text-sm text-muted">
        Không có nhân vật nào ngoài tài khoản quản trị — chưa cần reset.
      </p>
      <form v-else-if="data" class="mt-4 flex flex-wrap items-end gap-3" @submit.prevent="dungCum && (moXacNhan = true)">
        <UFormField :label="`Gõ đúng cụm “${data.cumTu}” để xác nhận`" class="min-w-64">
          <UInput v-model="xacNhan" autocomplete="off" class="w-full" />
        </UFormField>
        <UButton type="submit" color="error" icon="i-lucide-rotate-ccw" label="Reset toàn server" :disabled="!dungCum" :loading="dangGui" />
      </form>
    </UCard>

    <AdminNhanvatXacNhan
      v-model:open="moXacNhan"
      :title="`RESET TOÀN SERVER — xoá vĩnh viễn ${so(data?.soNhanVat)} nhân vật của ${so(data?.soTaiKhoan)} tài khoản?`"
      description="Không thể hoàn tác."
      nhan-nut="Reset toàn server"
      mau="error"
      :dang-chay="dangGui"
      @xac-nhan="gui"
    />
  </AdminPage>
</template>
