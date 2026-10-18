<script setup lang="ts">
/**
 * Key art "Huyền Thoại Bảo Bối" - cùng cảnh với màn đăng nhập trong game (J2ME / Android / PC), do
 * htbb-client/j2me/tools/tao_banner.py --web sinh vào public/keyart/ (khung gốc 880x400; màn dọc 360x640 ở doc/).
 *
 * Hai tầng, cùng MỘT hình học "cover, neo đáy giữa" (giống object-fit: cover; object-position: 50% 100%):
 *  1. Ảnh tĩnh ghép sẵn (nen_ngang.jpg / nen_doc.jpg) - có ngay từ HTML server (SSR), không cần JS / CSS
 *     ngoài (style inline), nên lúc trang đang tải hay JS lỗi vẫn thấy đủ cảnh, không bao giờ là mảng navy trống.
 *  2. Sau khi hydrate VÀ mọi ảnh lớp đã tải + decode(): các lớp pixel art đặt bằng px tính từ kích thước
 *     khung chứa (ResizeObserver) - một hệ số chung cho mọi lớp nên luôn khớp nhau; mây trôi, Rayquaza lượn,
 *     parallax theo chuột, đom đóm. Lớp hiện dần đè lên ảnh tĩnh (trùng khít), lỗi tải lớp nào thì giữ ảnh tĩnh.
 * prefers-reduced-motion: chỉ ảnh tĩnh / lớp đứng yên, không parallax, không hoạt ảnh.
 */
const props = withDefaults(defineProps<{ khoiDau?: boolean }>(), { khoiDau: true })

type KieuLop = { ten: string, sau: number, lap?: boolean, bay?: boolean, muot?: boolean }
const LOP: KieuLop[] = [
  { ten: 'troi', sau: 0 },
  { ten: 'may', sau: 0.08, lap: true },
  { ten: 'huyen_thoai', sau: 0.12, bay: true },
  { ten: 'nui', sau: 0.2 },
  { ten: 'rung', sau: 0.4 },
  { ten: 'dat', sau: 0.7 },
  { ten: 'khoi_dau', sau: 0.85 },
  { ten: 'khung', sau: 1 },
  { ten: 'anh_sang', sau: 0, muot: true },
]
const BO = {
  ngang: { w: 880, h: 400, thuMuc: '/keyart/', nen: '/keyart/nen_ngang.jpg' },
  doc: { w: 360, h: 640, thuMuc: '/keyart/doc/', nen: '/keyart/nen_doc.jpg' },
} as const
type TenBo = keyof typeof BO

// ảnh tĩnh tải sớm nhất có thể (trước hàng trăm module JS khi chạy dev)
useHead({
  link: [
    { rel: 'preload', as: 'image', href: BO.ngang.nen, media: '(min-aspect-ratio: 4/5)', fetchpriority: 'high', key: 'keyart-ngang' },
    { rel: 'preload', as: 'image', href: BO.doc.nen, media: '(max-aspect-ratio: 4/5)', fetchpriority: 'high', key: 'keyart-doc' },
  ],
})

const goc = ref<HTMLElement>()
const kich = ref({ w: 0, h: 0 })
const bo = computed<TenBo>(() => (kich.value.w > 0 && kich.value.w / Math.max(1, kich.value.h) < 0.8 ? 'doc' : 'ngang'))
const lop = computed(() => LOP.filter(l => props.khoiDau || l.ten !== 'khoi_dau'))
/** bộ lớp nào đã tải xong hết (chỉ phía trình duyệt) */
const daTai = ref<Record<string, boolean>>({})
const giamChuyenDong = ref(false)
const mx = ref(0)

/** khung cover neo đáy giữa, tính bằng px */
const khung = computed(() => {
  const b = BO[bo.value]
  const { w, h } = kich.value
  const s = Math.max(w / b.w, h / b.h) || 1
  const fw = b.w * s
  const fh = b.h * s
  return { s, w: fw, h: fh, x: (w - fw) / 2, y: h - fh }
})
const hienLop = computed(() => kich.value.w > 0 && daTai.value[bo.value])

function styleLop(l: KieuLop) {
  const k = khung.value
  const dx = giamChuyenDong.value ? 0 : -mx.value * l.sau * k.w * 0.014
  return {
    position: 'absolute' as const,
    left: `${k.x}px`,
    top: `${k.y}px`,
    width: `${k.w}px`,
    height: `${k.h}px`,
    maxWidth: 'none',
    transform: `translate3d(${dx.toFixed(1)}px,0,0)`,
    transition: 'transform .6s ease-out',
    imageRendering: l.muot ? undefined : ('pixelated' as const),
  }
}

/** hai ảnh mây nối đuôi trong một dải rộng gấp đôi, trôi sang trái hết một bề rộng rồi lặp */
let hoatAnh: Animation[] = []
function chayHoatAnh() {
  hoatAnh.forEach(a => a.cancel())
  hoatAnh = []
  if (giamChuyenDong.value) return
  const g = goc.value
  if (!g) return
  for (const el of Array.from(g.querySelectorAll<HTMLElement>('[data-may]'))) {
    if (el?.animate) hoatAnh.push(el.animate([{ transform: 'translateX(0)' }, { transform: 'translateX(-50%)' }], { duration: 140000, iterations: Infinity }))
  }
  for (const el of Array.from(g.querySelectorAll<HTMLElement>('[data-bay]'))) {
    if (el?.animate) hoatAnh.push(el.animate([{ translate: '0 0' }, { translate: '0.6% -1.2%' }, { translate: '0 0' }], { duration: 9000, iterations: Infinity, easing: 'ease-in-out' }))
  }
}

function napBo(t: TenBo) {
  if (daTai.value[t] !== undefined) return
  daTai.value[t] = false
  const b = BO[t]
  Promise.all(lop.value.map((l) => {
    const im = new Image()
    im.src = `${b.thuMuc}${l.ten}.png`
    return im.decode ? im.decode() : new Promise((ok, loi) => { im.onload = ok; im.onerror = loi })
  })).then(() => {
    daTai.value[t] = true
    nextTick(chayHoatAnh)
  }).catch(() => { /* giữ ảnh tĩnh */ })
}

let ro: ResizeObserver | undefined
let raf = 0
function chuot(e: PointerEvent) {
  if (e.pointerType === 'touch' || giamChuyenDong.value) return
  const v = Math.max(-1, Math.min(1, (e.clientX / Math.max(1, window.innerWidth)) * 2 - 1))
  cancelAnimationFrame(raf)
  raf = requestAnimationFrame(() => { mx.value = v })
}
onMounted(() => {
  const mq = window.matchMedia?.('(prefers-reduced-motion: reduce)')
  giamChuyenDong.value = !!mq?.matches
  mq?.addEventListener?.('change', (e) => { giamChuyenDong.value = e.matches; chayHoatAnh() })
  const el = goc.value
  if (!el) return
  const do_ = () => { kich.value = { w: el.clientWidth, h: el.clientHeight } }
  do_()
  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver(do_)
    ro.observe(el)
  }
  window.addEventListener('pointermove', chuot, { passive: true })
})
watch(bo, t => napBo(t), { immediate: false })
watch(kich, (k) => { if (k.w > 0) napBo(bo.value) })
watch([hienLop, bo], () => hienLop.value && nextTick(chayHoatAnh))
onBeforeUnmount(() => {
  ro?.disconnect()
  window.removeEventListener('pointermove', chuot)
  cancelAnimationFrame(raf)
  hoatAnh.forEach(a => a.cancel())
})

const domDom = Array.from({ length: 14 }, (_, i) => ({
  x: (i * 37 + 11) % 100,
  y: 62 + ((i * 23) % 28),
  tre: -((i * 0.73) % 4),
  lau: 3 + (i % 4),
}))
const NEN_STYLE = {
  position: 'absolute', inset: '0', width: '100%', height: '100%', maxWidth: 'none',
  objectFit: 'cover', objectPosition: '50% 100%',
} as const
</script>

<template>
  <!-- style inline: bố cục đúng cả khi CSS / JS chưa tải -->
  <div
    ref="goc"
    class="keyart pointer-events-none"
    style="position:absolute;inset:0;overflow:hidden;background:#3a2a6e linear-gradient(180deg,#12163d 0%,#553384 45%,#de666b 62%,#2a4a32 70%,#1a3528 100%)"
    aria-hidden="true"
  >
    <!-- tầng 1: ảnh tĩnh ghép sẵn (SSR) -->
    <picture>
      <source media="(max-aspect-ratio: 4/5)" :srcset="BO.doc.nen">
      <img :src="BO.ngang.nen" alt="" :style="NEN_STYLE" fetchpriority="high" decoding="async">
    </picture>
    <!-- tầng 2: lớp parallax (chỉ phía trình duyệt, sau khi ảnh lớp đã decode) -->
    <ClientOnly>
      <div v-if="hienLop" :key="bo" class="keyart-lop" style="position:absolute;inset:0">
        <template v-for="l in lop" :key="l.ten">
          <div v-if="l.lap" :style="{ ...styleLop(l), overflow: 'hidden' }">
            <div data-may style="display:flex;width:200%;height:100%">
              <img :src="`${BO[bo].thuMuc}${l.ten}.png`" alt="" style="width:50%;height:100%;flex:none;image-rendering:pixelated;max-width:none">
              <img :src="`${BO[bo].thuMuc}${l.ten}.png`" alt="" style="width:50%;height:100%;flex:none;image-rendering:pixelated;max-width:none">
            </div>
          </div>
          <div v-else-if="l.bay" :style="styleLop(l)">
            <img data-bay :src="`${BO[bo].thuMuc}${l.ten}.png`" alt="" style="width:100%;height:100%;image-rendering:pixelated;max-width:none">
          </div>
          <img v-else :src="`${BO[bo].thuMuc}${l.ten}.png`" alt="" :style="styleLop(l)">
        </template>
        <div v-if="!giamChuyenDong" :style="{ position: 'absolute', left: `${khung.x}px`, top: `${khung.y}px`, width: `${khung.w}px`, height: `${khung.h}px` }">
          <span
            v-for="(d, i) in domDom"
            :key="i"
            class="dom"
            :style="{ left: `${d.x}%`, top: `${d.y}%`, animationDelay: `${d.tre}s`, animationDuration: `${d.lau}s` }"
          />
        </div>
      </div>
    </ClientOnly>
  </div>
</template>

<style scoped>
.keyart-lop {
  animation: hien 0.5s ease-out both;
}
@keyframes hien {
  from { opacity: 0; }
  to { opacity: 1; }
}
.dom {
  position: absolute;
  width: 0.35%;
  min-width: 3px;
  aspect-ratio: 1;
  background: #fff3a8;
  box-shadow: 0 0 6px 2px rgb(255 220 120 / 0.7);
  opacity: 0;
  animation: dom 4s ease-in-out infinite;
}
@keyframes dom {
  0%, 100% { opacity: 0; transform: translateY(0); }
  40%, 60% { opacity: 0.95; transform: translateY(-40%); }
}
@media (prefers-reduced-motion: reduce) {
  .keyart-lop, .dom { animation: none; }
  .dom { display: none; }
}
</style>
