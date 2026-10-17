<script setup lang="ts">
/** Trang cẩm nang tạo trong khu quản trị (/admin/trang) — nội dung Markdown trong CSDL. */
const route = useRoute()
const slug = computed(() => String(route.params.slug ?? ''))
const { data } = await useFetch<{ ten: string } | null>(() => `/api/cam-nang-db/${slug.value}`, { key: `trang-db-${slug.value}`, default: () => null })
if (!data.value) {
  throw createError({ statusCode: 404, statusMessage: 'Không có trang cẩm nang này', fatal: true })
}
</script>

<template>
  <WikiShell :slug="slug" />
</template>
