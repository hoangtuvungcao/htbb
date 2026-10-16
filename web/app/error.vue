<script setup lang="ts">
import type { NuxtError } from '#app'

const props = defineProps<{ error: NuxtError }>()

const la404 = computed(() => props.error.statusCode === 404)
/** 403 do web tự báo (vd. nạp tiền đã tắt): hiện đúng lời giải thích. */
const la403 = computed(() => props.error.statusCode === 403)
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
          {{ la404 ? 'Lạc vào bụi cỏ cao rồi.' : la403 ? 'Lối này đang đóng.' : 'Một Pokémon hoang dã đang quậy phá máy chủ.' }}
        </h1>
        <p class="mt-3 text-muted">
          {{ la404
            ? 'Trang bạn tìm không tồn tại hoặc đã được dời đi nơi khác.'
            : la403
              ? (error.statusMessage || 'Bạn không có quyền xem trang này.')
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
