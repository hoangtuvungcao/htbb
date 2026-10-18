<script setup lang="ts">
/**
 * Nâng Level toàn bộ nhân vật. Xếp hàng lệnh
 * dat_cap_do (đánh dấu tham_so = 'toanbo') cho MỌI nhân vật del = 1; server game tự xử lý
 * dần ở nền. Xem server/utils/nangLevelToanBo.ts.
 */
import type { DongLenhGiaoDien } from '~/components/admin/nhanvat/BangLenh.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })

interface SoLieu { capDoToiDa: number, soNhanVat: number, soLenhChoXuLy: number, lenhs: DongLenhGiaoDien[], loiDoc: string }

const api = useAdminApi()
const toast = useToast()

const { data, status, error, refresh } = useAsyncData('admin-nang-level-toanbo',
  () => api<SoLieu>('/api/admin/nang-level-toanbo'), { server: false })

const capDich = ref<number>(90)
watch(() => data.value?.capDoToiDa, (v) => {
  if (v) capDich.value = v
}, { once: true })

const moXacNhan = ref(false)
const dangGui = ref(false)

async function gui() {
  dangGui.value = true
  try {
    const kq = await api<{ thongBao: string }>('/api/admin/nang-level-toanbo', {
      method: 'POST', body: { cap_do_dich: String(capDich.value ?? '') },
    })
    toast.add({ title: 'Đã xếp hàng đợi', description: kq.thongBao, color: 'success', duration: 12000 })
  } catch (e) {
    toast.add({ title: 'Không thực hiện được', description: loiApi(e), color: 'error' })
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
    const kq = await api<{ thongBao: string }>('/api/admin/nang-level-toanbo/huy', { method: 'POST', body: { id_lenh: l.id } })
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
  <AdminPage title="Nâng Level toàn server">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="outline" label="Cập nhật số liệu" :loading="status === 'pending'" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-xl font-bold text-highlighted">
        Nâng Level toàn bộ nhân vật
      </h1>
      <p class="text-sm text-muted">
        Đưa MỌI nhân vật đang hoạt động trên server lên ĐÚNG một cấp độ chỉ định — dùng cho sự kiện / bù đắp toàn server.
      </p>
    </div>

    <UAlert v-if="error" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${loiApi(error)}`" />
    <UAlert v-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-alert" :title="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <div class="grid gap-4 sm:grid-cols-2">
      <AdminStatCard label="Nhân vật sẽ được nâng level" :value="data?.soNhanVat ?? '…'" icon="i-lucide-users" />
      <AdminStatCard
        label="Lệnh đang chờ server xử lý"
        :value="data?.soLenhChoXuLy ?? '…'"
        :mau="(data?.soLenhChoXuLy ?? 0) > 0 ? 'warn' : ''"
        icon="i-lucide-hourglass"
      />
    </div>

    <UCard>
      <template #header>
        <h2 class="font-semibold text-highlighted">
          Nâng level cho tất cả
        </h2>
      </template>
      <form class="space-y-4" @submit.prevent="moXacNhan = true">
        <UFormField label="Cấp độ muốn đưa tất cả nhân vật lên" required>
          <UInputNumber v-model="capDich" :min="1" :max="data?.capDoToiDa ?? 90" class="w-40" />
        </UFormField>
        <p class="text-sm text-muted">
          Nhân vật đang online được nâng NGAY (kèm điểm cơ bản/kỹ năng của các cấp vừa lên, giống lên cấp tự nhiên);
          nhân vật offline sẽ được nâng khi vào game. Nhân vật đã ở cấp này trở lên sẽ báo lỗi riêng cho nhân vật
          đó (không bị hạ cấp), không ảnh hưởng đến các nhân vật khác.
        </p>
        <UButton type="submit" color="error" icon="i-lucide-trending-up" label="Nâng Level cho tất cả" :loading="dangGui" />
      </form>
    </UCard>

    <section class="space-y-3">
      <h2 class="text-lg font-semibold text-highlighted">
        100 lệnh nâng level toàn bộ gần nhất
      </h2>
      <AdminNhanvatBangLenh
        :lenh="data?.lenhs ?? []"
        :cot="['charname']"
        nhan-yeu-cau="Cấp đích"
        empty="Chưa có lệnh nâng level toàn bộ."
        :dang-huy="dangHuy"
        @huy="huy"
      />
    </section>

    <AdminNhanvatXacNhan
      v-model:open="moXacNhan"
      :title="`Đưa toàn bộ ${data?.soNhanVat ?? 0} nhân vật đang hoạt động lên đúng cấp ${capDich}?`"
      description="Lệnh được xếp hàng cho server game xử lý dần ở nền. Nhân vật đã ở cấp này trở lên được giữ nguyên."
      nhan-nut="Nâng Level cho tất cả"
      mau="error"
      :dang-chay="dangGui"
      @xac-nhan="gui"
    />
  </AdminPage>
</template>
