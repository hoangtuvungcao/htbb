<script setup lang="ts">
import type { NuxtError } from '#app'

/**
 * Trang lỗi (404 / 403 / 500): nền key art của game, hộp thoại kiểu game, hai lối ra. Không hiện chi tiết kỹ thuật
 * của lỗi (stack, đường dẫn máy chủ) — chỉ mã lỗi và một câu cho người chơi.
 */
const props = defineProps<{ error: NuxtError }>()

const ma = computed(() => props.error.statusCode || 500)
const la404 = computed(() => ma.value === 404)
/** 403 do web tự báo (vd. nạp tiền đã tắt): hiện đúng lời giải thích. */
const la403 = computed(() => ma.value === 403)
useSeoMeta({ title: () => (la404.value ? 'Không tìm thấy trang' : 'Có lỗi xảy ra'), robots: 'noindex' })

function veTrangChu() {
  clearError({ redirect: '/' })
}
</script>

<template>
  <UApp>
    <div class="relative isolate grid min-h-dvh place-items-center overflow-hidden bg-[#120f24] p-4 text-center">
      <img src="/keyart/nen_ngang.jpg" alt="" width="1760" height="800" class="pixelated absolute inset-0 -z-10 size-full object-cover object-bottom opacity-45">
      <div class="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_center,rgb(18_15_36/.55),#120f24_85%)]" aria-hidden="true" />
      <div class="w-full max-w-lg">
        <button type="button" class="mx-auto block" aria-label="Về trang chủ" @click="veTrangChu">
          <img src="/logo-gon.svg" alt="" width="796" height="409" class="h-16 w-auto">
        </button>
        <p class="mt-6 font-display text-[6rem] font-extrabold leading-none text-gilded sm:text-[7rem]">
          {{ ma }}
        </p>
        <div class="dialog-box mt-6 text-left">
          <div class="flex items-start gap-4">
            <img :src="anhChanDung(la404 ? 133 : 143)" width="40" height="40" alt="" class="pixelated size-14 shrink-0 rounded-md border-2 border-[#241c3f]">
            <div>
              <h1 class="font-display text-xl font-extrabold text-highlighted">
                {{ la404 ? 'Lạc vào bụi cỏ cao rồi!' : la403 ? 'Lối này đang đóng.' : 'Có lỗi xảy ra.' }}
              </h1>
              <p class="mt-1 text-muted">
                {{ la404
                  ? 'Trang bạn tìm không tồn tại hoặc đã được dời đi nơi khác.'
                  : la403
                    ? (error.statusMessage || 'Bạn không có quyền xem trang này.')
                    : 'Vui lòng thử lại sau ít phút.' }}
              </p>
            </div>
          </div>
        </div>
        <div class="mt-8 flex flex-wrap justify-center gap-3">
          <UButton size="lg" class="btn-pikachu px-6" @click="veTrangChu">
            Về trang chủ
          </UButton>
          <UButton size="lg" class="btn-game px-6" to="/download" @click="clearError()">
            Tải game
          </UButton>
        </div>
      </div>
    </div>
  </UApp>
</template>
