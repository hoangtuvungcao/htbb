<script setup lang="ts">
/**
 * Cảnh 3D ở đầu trang chủ: Linh Kiếm lơ lửng giữa vầng trăng vàng, vòng thiên
 * văn xoay chậm, tinh thể ngọc bay quanh và đom đóm vàng. Kéo chuột để nghiêng
 * cảnh (parallax).
 *
 * TresCanvas của @tresjs/nuxt chỉ chạy phía client nên không cần <ClientOnly>.
 * Người bật "giảm chuyển động" (prefers-reduced-motion) sẽ thấy cảnh đứng yên.
 */
const reduced = usePreferredReducedMotion()
</script>

<template>
  <div class="absolute inset-0" aria-hidden="true">
    <TresCanvas
      :alpha="true"
      :clear-alpha="0"
      :antialias="true"
      :dpr="[1, 2]"
    >
      <TresPerspectiveCamera :position="[0, 0.2, 9]" :fov="42" :look-at="[0, 0, 0]" />
      <ThreeHeroContent :still="reduced === 'reduce'" />
    </TresCanvas>
  </div>
</template>
