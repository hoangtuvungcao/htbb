<script setup lang="ts">
/**
 * Hiện dần khi cuộn tới (một lần). Dùng motion-v.
 *
 *   <FxReveal :delay="0.1">...</FxReveal>
 *
 * Người dùng bật "giảm chuyển động" thì nội dung hiện ngay, không hiệu ứng.
 */
const props = withDefaults(defineProps<{ delay?: number, y?: number, as?: string }>(), { delay: 0, y: 28, as: 'div' })
const reduced = usePreferredReducedMotion()

// Gom thành một object rồi v-bind: kiểu props của <Motion> (motion-v) không nhận
// `as` dạng string khi truyền rời từng thuộc tính.
const motionProps = computed(() => ({
  as: props.as,
  initial: { opacity: 0, y: props.y, filter: 'blur(6px)' },
  whileInView: { opacity: 1, y: 0, filter: 'blur(0px)' },
  inViewOptions: { once: true, amount: 0.15 },
  transition: { duration: 0.7, delay: props.delay, ease: [0.2, 0.8, 0.2, 1] },
}) as Record<string, any>)
</script>

<template>
  <component :is="as" v-if="reduced === 'reduce'">
    <slot />
  </component>
  <Motion v-else v-bind="motionProps">
    <slot />
  </Motion>
</template>
