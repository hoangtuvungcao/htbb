<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{ error: NuxtError }>()

const la404 = computed(() => props.error.statusCode === 404)
useSeoMeta({ title: () => (la404.value ? 'Không tìm thấy trang' : 'Có lỗi xảy ra') })

function veTrangChu() {
  clearError({ redirect: '/' })
}
</script>

<template>
  <UApp>
    <div class="relative grid min-h-dvh place-items-center overflow-hidden bg-pattern p-6 text-center">
      <FxGlowOrbs />
      <div class="relative max-w-lg">
        <p class="font-serif text-[7rem] font-extrabold leading-none text-gilded">
          {{ error.statusCode || 500 }}
        </p>
        <h1 class="section-title mt-4 text-3xl text-highlighted">
          {{ la404 ? 'Lạc bước giang hồ rồi.' : 'Giang hồ đang nổi sóng.' }}
        </h1>
        <p class="mt-3 text-muted">
          {{ la404
            ? 'Trang bạn tìm không tồn tại hoặc đã được dời đi nơi khác.'
            : 'Máy chủ gặp sự cố khi xử lý yêu cầu. Bạn vui lòng thử lại sau ít phút.' }}
        </p>
        <div class="mt-8 flex justify-center gap-3">
          <UButton size="lg" icon="i-lucide-house" @click="veTrangChu">
            Về trang chủ
          </UButton>
          <UButton size="lg" color="neutral" variant="outline" to="/download#support" @click="clearError()">
            Kênh hỗ trợ
          </UButton>
        </div>
      </div>
    </div>
  </UApp>
</template>
