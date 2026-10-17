<script setup lang="ts">
/**
 * Hộp chọn ảnh từ thư viện (+ tải ảnh mới). Dùng qua useOverlay():
 *   const chonAnh = useOverlay().create(AdminCmsChonAnh)
 *   const url = await chonAnh.open().result   // '' nếu huỷ
 */
const emit = defineEmits<{ close: [string] }>()
const api = useAdminApi()
const toast = useToast()
const { data, refresh } = useAsyncData('admin-media-chon', () => api<{ coBang: boolean, ds: { id: number, url: string, ten_goc: string, rong: number, cao: number }[] }>('/api/admin/cms/media'), { server: false })
const dangTai = ref(false)
async function taiLen(e: Event) {
  const f = (e.target as HTMLInputElement).files
  if (!f?.length) return
  const fd = new FormData()
  for (const x of f) fd.append('file', x)
  dangTai.value = true
  try {
    const r = await api<{ ds: { url: string }[] }>('/api/admin/cms/media/tai-len', { method: 'POST', body: fd })
    toast.add({ title: `Đã tải ${r.ds.length} ảnh`, color: 'success' })
    await refresh()
    if (r.ds.length === 1) emit('close', r.ds[0]!.url)
  }
  catch (err) { toast.add({ title: loiApi(err), color: 'error' }) }
  finally { dangTai.value = false }
}
</script>

<template>
  <UModal title="Chọn ảnh" :ui="{ content: 'sm:max-w-3xl' }" @update:open="(v: boolean) => { if (!v) emit('close', '') }">
    <template #body>
      <AdminCmsChuaCoBang v-if="data && !data.coBang" bang="web_media" />
      <template v-else>
        <label class="flex cursor-pointer items-center justify-center gap-2 rounded-xl border-2 border-dashed border-default p-4 text-sm font-semibold text-muted hover:border-primary">
          <UIcon :name="dangTai ? 'i-lucide-loader-circle' : 'i-lucide-upload'" class="size-5" :class="dangTai ? 'animate-spin' : ''" />
          Tải ảnh mới (PNG, JPEG, GIF, WebP · tối đa 5 MB)
          <input type="file" accept="image/png,image/jpeg,image/gif,image/webp" multiple class="sr-only" @change="taiLen">
        </label>
        <ul class="mt-4 grid max-h-[55vh] grid-cols-3 gap-2 overflow-y-auto sm:grid-cols-5">
          <li v-for="a in data?.ds ?? []" :key="a.id">
            <button type="button" class="group block w-full overflow-hidden rounded-lg border border-default hover:border-primary" :title="a.ten_goc" @click="emit('close', a.url)">
              <img :src="a.url" alt="" loading="lazy" class="aspect-square w-full object-cover">
            </button>
          </li>
        </ul>
        <p v-if="!data?.ds?.length" class="mt-4 text-center text-sm text-muted">
          Thư viện trống.
        </p>
      </template>
    </template>
  </UModal>
</template>
