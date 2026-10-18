<script setup lang="ts">
/**
 * Giảm Level nhiều nhân vật. Gửi lệnh giam_cap_do cho
 * server game qua web_lenh_nhan_vat — không ghi thẳng tob_char (xem server/utils/lenhNhanVat.ts).
 */
import type { DongLenhGiaoDien } from '~/components/admin/nhanvat/BangLenh.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

const api = useAdminApi()
const toast = useToast()

const { data, status, error, refresh } = useAsyncData('admin-giam-level',
  () => api<{ capDoToiDa: number, lenhs: DongLenhGiaoDien[], loi: string }>('/api/admin/giam-level'),
  { server: false })

const tenNhanVat = ref('')
const soCap = ref<number>(1)
const dangGui = ref(false)
const moXacNhan = ref(false)

const soTen = computed(() => new Set(tenNhanVat.value.split(/[,;\r\n]+/).map(s => s.trim().toLowerCase()).filter(Boolean)).size)

function hoiGui() {
  if (soTen.value === 0) {
    toast.add({ title: 'Thiếu tên', description: 'Nhập từ 1 đến 100 tên Pokémon.', color: 'error' })
    return
  }
  moXacNhan.value = true
}

async function gui() {
  dangGui.value = true
  try {
    const kq = await api<{ thongBao: string }>('/api/admin/giam-level', {
      method: 'POST',
      body: { ten_nhan_vat: tenNhanVat.value, so_cap: String(soCap.value ?? '') },
    })
    toast.add({ title: 'Đã gửi lệnh', description: kq.thongBao, color: 'success' })
    tenNhanVat.value = ''
  } catch (e) {
    toast.add({ title: 'Chưa gửi lệnh', description: loiApi(e), color: 'error', duration: 12000 })
  } finally {
    dangGui.value = false
    moXacNhan.value = false
  }
  await refresh()
}

const dangHuy = ref<number | null>(null)
async function huy(l: DongLenhGiaoDien) {
  dangHuy.value = l.id
  try {
    const kq = await api<{ thongBao: string }>('/api/admin/giam-level/huy', { method: 'POST', body: { id_lenh: l.id } })
    toast.add({ title: 'Xong', description: kq.thongBao, color: 'success' })
  } catch (e) {
    toast.add({ title: 'Không huỷ được', description: loiApi(e), color: 'error' })
  } finally {
    dangHuy.value = null
  }
  await refresh()
}
</script>

<template>
  <AdminPage title="Giảm Level">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="outline" label="Cập nhật kết quả" :loading="status === 'pending'" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-xl font-bold text-highlighted">
        Giảm Level nhiều Pokémon
      </h1>
      <p class="text-sm text-muted">
        Giảm cùng số level cho một hoặc nhiều Pokémon.
      </p>
    </div>

    <UCard>
      <form class="space-y-4" @submit.prevent="hoiGui">
        <UFormField label="Tên Pokémon — mỗi dòng một tên, hoặc ngăn bằng dấu phẩy / chấm phẩy (tối đa 100)" required>
          <UTextarea v-model="tenNhanVat" :rows="6" placeholder="nhanvat1&#10;nhanvat2" class="w-full" />
        </UFormField>
        <UFormField label="Số level cần giảm cho mỗi Pokémon" required>
          <UInputNumber v-model="soCap" :min="1" :max="data?.capDoToiDa ?? 90" class="w-40" />
        </UFormField>
        <div class="space-y-1 text-sm text-muted">
          <p>Cấp thấp nhất là 1, EXP về 0% cấp mới. Không đủ cấp sẽ báo lỗi riêng cho Pokémon đó. Tên trùng chỉ xử lý một lần.</p>
          <p>Giữ điểm đã nhận; luyện lại cấp cũ không cộng trùng điểm. Pokémon offline sẽ được giảm khi vào game.</p>
        </div>
        <UButton type="submit" color="error" icon="i-lucide-user-minus" label="Gửi lệnh giảm Level" :loading="dangGui" />
      </form>
    </UCard>

    <section class="space-y-3">
      <h2 class="text-lg font-semibold text-highlighted">
        100 lệnh giảm Level gần nhất
      </h2>
      <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${loiApi(error)}`" />
      <UAlert v-if="data?.loi" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="data.loi" />
      <AdminNhanvatBangLenh
        :lenh="data?.lenhs ?? []"
        :cot="['charname']"
        nhan-yeu-cau="Giảm"
        empty="Chưa có lệnh giảm level."
        :dang-huy="dangHuy"
        @huy="huy"
      />
    </section>

    <AdminNhanvatXacNhan
      v-model:open="moXacNhan"
      :title="`Giảm ${soCap} level cho ${soTen} Pokémon?`"
      description="Pokémon đang online được giảm ngay; offline sẽ được giảm khi vào game."
      nhan-nut="Gửi lệnh giảm Level"
      mau="error"
      :dang-chay="dangGui"
      @xac-nhan="gui"
    />
  </AdminPage>
</template>
