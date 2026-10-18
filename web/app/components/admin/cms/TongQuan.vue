<script setup lang="ts">
/** Khối tổng quan nội dung trên bảng điều khiển: người online, tài khoản mới/ngày, sự kiện tuần, tin mới. */
const api = useAdminApi()
const { data } = useAsyncData('admin-cms-tq', () => api<{ noiDung: Record<string, number | null>, tkMoi: { ngay: string, so: number }[], online: number | null, tinMoi: { id: number, tieu_de: string, trang_thai: number, dang_luc: string | null, sua_luc: string }[], coBangCms: boolean }>('/api/admin/cms/tong-quan'), { server: false })
const lich = useLichSuKien()
const suKien = computed(() => suKienTuTuan(tuanTrongNam(new Date()), 1, lich.value))
const nhanTin = (t: { trang_thai: number, dang_luc?: string | null }) => Number(t.trang_thai) !== 1 ? ['Nháp', 'neutral'] as const : t.dang_luc && new Date(t.dang_luc.replace(' ', 'T')) > new Date() ? ['Hẹn giờ', 'info'] as const : ['Đã đăng', 'success'] as const
const maxTk = computed(() => Math.max(1, ...(data.value?.tkMoi ?? []).map(x => x.so)))
</script>

<template>
  <div class="space-y-4">
    <AdminCmsChuaCoBang v-if="data && !data.coBangCms" />
    <div class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <UCard>
        <p class="text-sm text-muted">Đang online</p>
        <p class="text-3xl font-bold">{{ data?.online ?? '—' }}</p>
        <NuxtLink to="/admin/online" class="text-xs text-primary hover:underline">Ai đang ở đâu →</NuxtLink>
      </UCard>
      <UCard>
        <p class="text-sm text-muted">Tài khoản mới (14 ngày)</p>
        <div class="mt-2 flex h-14 items-end gap-0.5" role="img" :aria-label="(data?.tkMoi ?? []).map(x => `${x.ngay}: ${x.so}`).join(', ')">
          <span v-for="x in data?.tkMoi ?? []" :key="x.ngay" class="flex-1 rounded-t bg-primary/70" :style="{ height: `${Math.max(6, (x.so / maxTk) * 100)}%` }" :title="`${x.ngay}: ${x.so}`" />
        </div>
        <p class="mt-1 text-xs text-dimmed">Tổng {{ (data?.tkMoi ?? []).reduce((s, x) => s + x.so, 0) }}</p>
      </UCard>
      <UCard>
        <p class="text-sm text-muted">Sự kiện tuần này</p>
        <p class="font-bold">{{ suKien[0]?.ten1 ?? '—' }}</p>
        <p class="text-xs text-muted">Tuần sau: {{ suKien[1]?.ten1 ?? '—' }}</p>
        <NuxtLink to="/admin/lich-su-kien" class="text-xs text-primary hover:underline">Sửa lịch →</NuxtLink>
      </UCard>
      <UCard>
        <p class="text-sm text-muted">Nội dung</p>
        <p class="text-sm"><b>{{ data?.noiDung.tin ?? 0 }}</b> tin ({{ data?.noiDung.tinNhap ?? 0 }} nháp) · <b>{{ data?.noiDung.trang ?? 0 }}</b> trang · <b>{{ data?.noiDung.faq ?? 0 }}</b> FAQ · <b>{{ data?.noiDung.anh ?? 0 }}</b> ảnh</p>
        <div class="mt-2 flex flex-wrap gap-1">
          <UButton size="xs" to="/admin/tin-tuc/moi" icon="i-lucide-plus" label="Viết tin" />
          <UButton size="xs" color="neutral" variant="outline" to="/admin/trang-chu" label="Trang chủ" />
        </div>
      </UCard>
    </div>
    <AdminKinhTeNgay />
    <UCard v-if="data?.tinMoi?.length">
      <template #header><b>Tin sửa gần đây</b></template>
      <ul class="divide-y divide-default text-sm">
        <li v-for="t in data.tinMoi" :key="t.id" class="flex items-center gap-2 py-1.5">
          <NuxtLink :to="`/admin/tin-tuc/${t.id}`" class="font-semibold hover:text-primary">{{ t.tieu_de }}</NuxtLink>
          <UBadge :color="nhanTin(t)[1]" variant="subtle" size="sm">{{ nhanTin(t)[0] }}</UBadge>
          <span class="ml-auto text-xs text-dimmed">{{ t.sua_luc }}</span>
        </li>
      </ul>
    </UCard>
  </div>
</template>
