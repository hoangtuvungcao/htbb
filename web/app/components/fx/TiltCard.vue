<script setup lang="ts">
/**
 * Thẻ nghiêng 3D theo con trỏ, kèm vệt sáng chạy theo chuột.
 *
 *   <FxTiltCard :max="12" glare class="p-6">...</FxTiltCard>
 *
 * Tắt nghiêng khi người dùng bật "giảm chuyển động" hoặc dùng màn hình cảm ứng.
 * Nội dung con có thể "nổi" lên bằng class `[transform:translateZ(40px)]`.
 */
const props = withDefaults(defineProps<{
  max?: number
  glare?: boolean
  scale?: number
  as?: string
}>(), { max: 10, glare: true, scale: 1.02, as: 'div' })

const el = ref<HTMLElement>()
const rx = ref(0)
const ry = ref(0)
const gx = ref(50)
const gy = ref(50)
const hover = ref(false)
const reduced = usePreferredReducedMotion()

function move(e: PointerEvent) {
  if (reduced.value === 'reduce' || e.pointerType === 'touch' || !el.value) return
  const r = el.value.getBoundingClientRect()
  const px = (e.clientX - r.left) / r.width
  const py = (e.clientY - r.top) / r.height
  ry.value = (px - 0.5) * 2 * props.max
  rx.value = -(py - 0.5) * 2 * props.max
  gx.value = px * 100
  gy.value = py * 100
  hover.value = true
}

function leave() {
  rx.value = 0
  ry.value = 0
  hover.value = false
}

const style = computed(() => ({
  transform: `perspective(900px) rotateX(${rx.value}deg) rotateY(${ry.value}deg) scale(${hover.value ? props.scale : 1})`,
  transition: hover.value ? 'transform 80ms linear' : 'transform 600ms cubic-bezier(.2,.8,.2,1)',
}))
</script>

<template>
  <component
    :is="as"
    ref="el"
    class="relative [transform-style:preserve-3d] will-change-transform"
    :style="style"
    @pointermove="move"
    @pointerleave="leave"
  >
    <slot />
    <div
      v-if="glare"
      class="pointer-events-none absolute inset-0 rounded-[inherit] transition-opacity duration-300"
      :class="hover ? 'opacity-100' : 'opacity-0'"
      :style="{ background: `radial-gradient(circle at ${gx}% ${gy}%, rgba(255,229,138,.22), transparent 55%)` }"
      aria-hidden="true"
    />
  </component>
</template>
