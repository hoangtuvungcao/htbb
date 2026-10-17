<script setup lang="ts">
/** Bảng kết quả trong game (chỉ đọc): chiếm thành, chiến trường, quà online, giftcode... */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const chon = ref('')
const { data } = await useAsyncData('admin-bang-game', () => api<{ bang: { ten: string, moTa: string }[], dong?: Record<string, unknown>[] }>('/api/admin/bang-game', { query: { bang: chon.value || undefined } }), { server: false, watch: [chon] })
const cot = computed(() => Object.keys(data.value?.dong?.[0] ?? {}))
</script>

<template>
  <AdminPage title="Kết quả trong game">
    <UAlert color="neutral" variant="subtle" icon="i-lucide-eye" description="Chỉ đọc. Danh sách gồm các bảng kết quả có trong CSDL game (chiếm thành tuần, quà online hằng ngày... xuất hiện khi máy chủ tạo bảng)." />
    <div class="flex flex-wrap gap-2">
      <UButton v-for="b in data?.bang ?? []" :key="b.ten" :color="chon === b.ten ? 'primary' : 'neutral'" :variant="chon === b.ten ? 'solid' : 'outline'" size="sm" :label="b.moTa" @click="chon = b.ten" />
    </div>
    <UCard v-if="chon" :ui="{ body: 'p-0 sm:p-0' }">
      <div class="overflow-x-auto">
        <table class="bang-wiki">
          <thead><tr><th v-for="c in cot" :key="c">{{ c }}</th></tr></thead>
          <tbody>
            <tr v-for="(r, i) in data?.dong ?? []" :key="i">
              <td v-for="c in cot" :key="c" class="text-xs">{{ r[c] }}</td>
            </tr>
            <tr v-if="!data?.dong?.length"><td class="text-muted">Bảng trống.</td></tr>
          </tbody>
        </table>
      </div>
    </UCard>
  </AdminPage>
</template>
