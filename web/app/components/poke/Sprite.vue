<script lang="ts">
/**
 * Hoạt ảnh sprite PMD (nền trong suốt). Dải khung + kích thước do scripts/sinh-anim-khoi-dau.mjs
 * cắt từ atlas của game vào public/pokemon/anim/ và shared/data/sprite-anim.json:
 *   - mọi loài: `idle` (đứng, nhìn xuống)
 *   - 5 Pokémon khởi đầu: thêm `walk`, `walkR` (đi sang phải), `danhR` (đánh sang phải)
 *
 *   <PokeSprite :dex="4" dong="walk" :scale="3" />
 *   <PokeSprite :dex="25" :fit="96" />          tự chọn hệ số phóng NGUYÊN lớn nhất vừa 96px
 *
 * Chạy hoàn toàn bằng CSS: mỗi khung là một mốc keyframes đổi background-position với
 * hàm steps(1) — giữ đúng thời lượng riêng của từng khung (đơn vị 1/60 s như AnimData.xml).
 * Người bật "giảm chuyển động" chỉ thấy khung đầu.
 *
 * 0.9.8: ảnh dải khung TẢI TRỄ qua hàng đợi chung (app/composables/useAnhTre.ts - chỉ khi vào khung nhìn, tối đa 6 ảnh
 * cùng lúc, thử lại khi 429): trước đó giữ chỗ trống cùng kích thước. `tinh` = đứng yên ở khung đầu, chỉ chuyển động
 * khi rê chuột vào thẻ (.group:hover) - lưới Pokédex.
 */
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
  /** Ô vuông (px) cần vừa: tự chọn hệ số phóng nguyên lớn nhất (≥ 1). Bỏ trống = dùng --s / scale. */
  fit?: number
  shiny?: boolean
  /** Nhãn cho trình đọc màn hình; bỏ trống = trang trí. */
  label?: string
  /** Lệch pha (giây) để nhiều sprite không nhún cùng lúc. */
  delay?: number
  /** Đứng yên khung đầu, chỉ động khi rê chuột vào thẻ cha (.group) - lưới nhiều thẻ. */
  tinh?: boolean
}>(), { dong: 'idle', scale: 3, delay: 0, tinh: false })

const khoa = computed(() => khoaSprite(props.dex, props.shiny))
const d = computed(() => (khoa.value ? ANIM[khoa.value]?.[props.dong] ?? ANIM[khoa.value]?.idle : undefined))
const dongThat = computed(() => (khoa.value && ANIM[khoa.value]?.[props.dong] ? props.dong : 'idle'))
const ten = computed(() => `poke-${khoa.value}-${dongThat.value}`)
const duongAnh = computed(() => (khoa.value ? `/pokemon/anim/${khoa.value}-${dongThat.value}.png` : null))
const el = ref<HTMLElement>()
const daTai = useAnhTre(el, () => duongAnh.value)

// Mỗi (loài, động tác) chỉ cần một khối keyframes; key giống nhau thì useHead gộp lại.
useHead(() => ({
  style: d.value && d.value.khung > 1 ? [{ key: ten.value, innerHTML: keyframes(ten.value, d.value) }] : [],
}))

const style = computed(() => {
  const x = d.value
  if (!x) return {}
  const tong = x.thoiLuong.reduce((s, v) => s + v, 0) / 60
  // Hệ số phóng luôn là số nguyên để pixel không bị nhoè.
  const s = props.fit
    ? String(Math.max(1, Math.floor(props.fit / Math.max(x.w, x.h))))
    : `var(--s, ${props.scale})`
  return {
    width: `calc(${x.w}px * ${s})`,
    height: `calc(${x.h}px * ${s})`,
    backgroundImage: daTai.value ? `url(${duongAnh.value})` : undefined,
    backgroundSize: `calc(${x.w * x.khung}px * ${s}) calc(${x.h}px * ${s})`,
    backgroundRepeat: 'no-repeat',
    animation: x.khung > 1 && daTai.value ? `${ten.value} ${tong.toFixed(3)}s steps(1, end) ${props.tinh ? 0 : -props.delay}s infinite` : undefined,
  }
})
</script>

<template>
  <span
    v-if="d"
    ref="el"
    class="pixelated inline-block shrink-0 motion-reduce:!animate-none"
    :class="tinh ? 'poke-tinh' : ''"
    :data-anh="daTai ? 'da-tai' : 'cho'"
    :style="style"
    :role="label ? 'img' : undefined"
    :aria-label="label"
    :aria-hidden="label ? undefined : 'true'"
  />
</template>

<style>
/* Lưới: đứng yên ở khung đầu, động khi rê vào thẻ */
.poke-tinh { animation-play-state: paused !important; }
.group:hover .poke-tinh, .poke-tinh:hover { animation-play-state: running !important; }
</style>
