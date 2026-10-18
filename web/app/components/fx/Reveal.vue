<script setup lang="ts">
/**
 * Hiện dần khi cuộn tới (một lần). Dùng motion-v.
 *
 *   <FxReveal :delay="0.1">...</FxReveal>
 *
 * Người dùng bật "giảm chuyển động" thì nội dung hiện ngay, không hiệu ứng.
 *
 * SSR / chưa có JS: nội dung LUÔN hiện (không render trạng thái ẩn opacity 0 từ server). Sau khi
 * hydrate, khối nào đang nằm trong màn hình thì giữ nguyên (không nháy); khối nằm dưới màn hình
 * mới chuyển sang <Motion> để hiện dần khi cuộn tới.
 */
const props = withDefaults(defineProps<{ delay?: number, y?: number, as?: string }>(), { delay: 0, y: 28, as: 'div' })
const reduced = usePreferredReducedMotion()
const el = ref<HTMLElement | null>(null)
const hieuUng = ref(false)
onMounted(() => {
  if (reduced.value === 'reduce') return
  const r = el.value?.getBoundingClientRect()
  // Đã thấy trên màn hình (hoặc không đo được) -> giữ nguyên, không ẩn rồi hiện lại.
  if (!r || r.top < window.innerHeight) return
  hieuUng.value = true
})

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
  <Motion v-if="hieuUng" v-bind="motionProps">
    <slot />
  </Motion>
  <component :is="as" v-else ref="el">
    <slot />
  </component>
</template>
