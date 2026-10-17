<script setup lang="ts">
/** Menu đầu trang + các cột chân trang. */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const me = useAdminMe()
const { f: fRef, luu, khoiPhuc, dangLuu, tuDb, coBang, daThayDoi } = await useCaiDatAdmin('menu')
const f = fRef as Ref<CmsMenu>
function doi<T>(ds: T[], i: number, d: number) {
  const j = i + d
  if (j < 0 || j >= ds.length) return
  const t = ds[i]!
  ds[i] = ds[j]!
  ds[j] = t
}
</script>

<template>
  <AdminPage title="Menu & chân trang">
    <template #right>
      <UBadge v-if="daThayDoi" color="warning" variant="subtle">Chưa lưu</UBadge>
      <UButton v-if="tuDb" color="neutral" variant="outline" icon="i-lucide-rotate-ccw" label="Mặc định" :disabled="!me.duocGhiNoiDung.value" @click="khoiPhuc" />
      <UButton icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" @click="luu" />
    </template>
    <AdminCmsChuaCoBang v-if="!coBang" bang="web_cai_dat" />
    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <div><b>Menu đầu trang</b><p class="text-sm text-muted">Mục trỏ tới /cam-nang tự có danh sách con.</p></div>
          <UButton size="sm" icon="i-lucide-plus" label="Thêm mục" @click="f.dauTrang.push({ label: 'Mục mới', to: '/', icon: 'i-lucide-link' })" />
        </div>
      </template>
      <div v-for="(l, i) in f.dauTrang" :key="i" class="mt-2 grid gap-2 sm:grid-cols-[1fr_1fr_12rem_auto]">
        <UInput v-model="l.label" placeholder="Chữ" />
        <UInput v-model="l.to" placeholder="/duong-dan" />
        <UInput v-model="l.icon" placeholder="i-lucide-..." :icon="l.icon || undefined" />
        <div class="flex gap-1">
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-up" aria-label="Lên" @click="doi(f.dauTrang, i, -1)" />
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-down" aria-label="Xuống" @click="doi(f.dauTrang, i, 1)" />
          <UButton size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="f.dauTrang.splice(i, 1)" />
        </div>
      </div>
    </UCard>
    <div class="grid gap-4 lg:grid-cols-2">
      <UCard v-for="(c, ci) in f.chanTrang" :key="ci">
        <template #header>
          <div class="flex items-center gap-2">
            <UInput v-model="c.ten" class="flex-1" />
            <UButton size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá cột" @click="f.chanTrang.splice(ci, 1)" />
          </div>
        </template>
        <div v-for="(l, i) in c.links" :key="i" class="mt-2 grid grid-cols-[1fr_1fr_auto] gap-2">
          <UInput v-model="l.label" placeholder="Chữ" />
          <UInput v-model="l.to" placeholder="/duong-dan" />
          <UButton size="xs" color="error" variant="ghost" icon="i-lucide-x" aria-label="Xoá" @click="c.links.splice(i, 1)" />
        </div>
        <UButton class="mt-2" size="xs" color="neutral" variant="outline" icon="i-lucide-plus" label="Thêm liên kết" @click="c.links.push({ label: 'Liên kết', to: '/' })" />
      </UCard>
    </div>
    <UButton v-if="f.chanTrang.length < 6" color="neutral" variant="outline" icon="i-lucide-plus" label="Thêm cột chân trang" @click="f.chanTrang.push({ ten: 'Cột mới', links: [] })" />
  </AdminPage>
</template>
