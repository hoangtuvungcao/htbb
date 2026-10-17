<script lang="ts">
/**
 * Hoạt ảnh sprite PMD của 5 Pokémon khởi đầu (dải khung + JSON do
 * scripts/sinh-anim-khoi-dau.mjs cắt từ atlas của game vào public/pokemon/anim/).
 *
 *   <PokeSprite :dex="4" dong="walk" :scale="3" />
 *
 * Chạy hoàn toàn bằng CSS: mỗi khung là một mốc keyframes đổi background-position với
 * hàm steps(1) — giữ đúng thời lượng riêng của từng khung (đơn vị 1/60 s như AnimData.xml).
 * Người bật "giảm chuyển động" chỉ thấy khung đầu.
 */
import a1 from '../../../public/pokemon/anim/1.json'
import a4 from '../../../public/pokemon/anim/4.json'
import a7 from '../../../public/pokemon/anim/7.json'
import a133 from '../../../public/pokemon/anim/133.json'
import a172 from '../../../public/pokemon/anim/172.json'

export interface DaiKhung { w: number, h: number, khung: number, thoiLuong: number[] }
export type DongTac = 'idle' | 'walk'

export const ANIM_KHOI_DAU: Record<number, Record<DongTac, DaiKhung>> = { 1: a1, 4: a4, 7: a7, 133: a133, 172: a172 }

/** Keyframes cho một dải: % tích luỹ theo thời lượng từng khung. */
function keyframes(ten: string, d: DaiKhung): string {
  const tong = d.thoiLuong.reduce((s, x) => s + x, 0)
  let t = 0
  const moc = d.thoiLuong.map((x, i) => {
    const p = (t / tong) * 100
    t += x
    const x0 = d.khung > 1 ? (i / (d.khung - 1)) * 100 : 0
    return `${p.toFixed(3)}%{background-position:${x0.toFixed(4)}% 0}`
  })
  return `@keyframes ${ten}{${moc.join('')}100%{background-position:0% 0}}`
}
</script>

<script setup lang="ts">
const props = withDefaults(defineProps<{
  dex: number
  dong?: DongTac
  scale?: number
  /** Nhãn cho trình đọc màn hình; bỏ trống = trang trí. */
  label?: string
  /** Lệch pha (giây) để nhiều sprite không nhún cùng lúc. */
  delay?: number
}>(), { dong: 'idle', scale: 3, delay: 0 })

const d = computed(() => ANIM_KHOI_DAU[props.dex]?.[props.dong])
const ten = computed(() => `poke-${props.dex}-${props.dong}`)

// Mỗi (dex, động tác) chỉ cần một khối keyframes; key giống nhau thì useHead gộp lại.
useHead(() => ({
  style: d.value ? [{ key: ten.value, innerHTML: keyframes(ten.value, d.value) }] : [],
}))

const style = computed(() => {
  const x = d.value
  if (!x) return {}
  const tong = x.thoiLuong.reduce((s, v) => s + v, 0) / 60
  // Hệ số phóng lấy từ biến CSS --s của phần tử cha nếu có (đổi theo màn hình),
  // không thì dùng prop `scale`. Luôn là số nguyên để pixel không bị nhoè.
  const s = `var(--s, ${props.scale})`
  return {
    width: `calc(${x.w}px * ${s})`,
    height: `calc(${x.h}px * ${s})`,
    backgroundImage: `url(/pokemon/anim/${props.dex}-${props.dong}.png)`,
    backgroundSize: `calc(${x.w * x.khung}px * ${s}) calc(${x.h}px * ${s})`,
    backgroundRepeat: 'no-repeat',
    animation: x.khung > 1 ? `${ten.value} ${tong.toFixed(3)}s steps(1, end) ${-props.delay}s infinite` : undefined,
  }
})
</script>

<template>
  <span
    v-if="d"
    class="pixelated inline-block"
    :style="style"
    :role="label ? 'img' : undefined"
    :aria-label="label"
    :aria-hidden="label ? undefined : 'true'"
  />
</template>
