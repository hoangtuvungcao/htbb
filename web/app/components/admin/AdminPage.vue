<script setup lang="ts">
/**
 * Khung một trang admin: thanh trên (nút menu, chỉ đường "Nhóm / Tiêu đề", vùng
 * nút bên phải) + vùng nội dung cuộn được, rộng tối đa 1360px.
 *
 *   <AdminPage title="Tài khoản">
 *     <template #right><UButton .../></template>
 *     ...nội dung...
 *   </AdminPage>
 */
const props = defineProps<{ title: string }>()
const route = useRoute()

const nhom = computed(() => mucMenuCua(route.path)?.nhom ?? '')

useHead({ title: () => props.title })
</script>

<template>
  <UDashboardPanel :id="`admin-${title}`">
    <template #header>
      <UDashboardNavbar :ui="{ right: 'gap-2' }">
        <template #leading>
          <UDashboardSidebarCollapse />
        </template>
        <template #title>
          <span class="flex items-center gap-2 text-sm">
            <span v-if="nhom" class="text-muted">{{ nhom }}</span>
            <span v-if="nhom" class="text-dimmed">/</span>
            <b class="text-highlighted">{{ title }}</b>
          </span>
        </template>
        <template #right>
          <slot name="right" />
        </template>
      </UDashboardNavbar>
      <slot name="toolbar" />
    </template>

    <template #body>
      <div class="mx-auto w-full max-w-[1360px] space-y-6">
        <AdminNhatKyBanner />
        <slot />
      </div>
      <p class="mx-auto mt-10 max-w-[1360px] text-center text-xs text-dimmed">
        Khu quản trị · mọi thao tác đều được ghi lại kèm tên người thực hiện và địa chỉ IP.
      </p>
    </template>
  </UDashboardPanel>
</template>
