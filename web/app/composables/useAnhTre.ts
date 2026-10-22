/**
 * Ảnh Pokémon TẢI TRỄ: chỉ khi phần tử vào (gần) khung nhìn mới xếp URL vào hàng đợi dùng chung (tối đa 6 ảnh tải
 * cùng lúc, thử lại lùi dần khi 429 / lỗi mạng — shared/utils/hangDoiAnh.ts). Trả `daTai` = true khi ảnh đã nằm trong
 * bộ nhớ đệm trình duyệt: lúc đó mới gắn background-image / src, trước đó giữ chỗ (bóng / Poké Ball mờ).
 *
 *   const el = ref<HTMLElement>()
 *   const daTai = useAnhTre(el, () => `/pokemon/anim/25-idle.png`)
 *
 * SSR: luôn false (HTML server không kéo ảnh), client nối sau khi gắn.
 */
import { taoHangDoiAnh, type HangDoiAnh } from '~~/shared/utils/hangDoiAnh'

let hangDoi: HangDoiAnh | null = null
let quanSat: IntersectionObserver | null = null
const choThay = new WeakMap<Element, () => void>()

function hang(): HangDoiAnh {
  if (!hangDoi) {
    hangDoi = taoHangDoiAnh({
      toiDa: 6,
      thuLai: 5,
      tai: url => new Promise<void>((ok, loi) => {
        const img = new Image()
        img.decoding = 'async'
        img.onload = () => ok()
        img.onerror = () => loi(new Error(`tai anh loi: ${url}`))
        img.src = url
      }),
    })
  }
  return hangDoi
}

function mat(): IntersectionObserver | null {
  if (!quanSat && typeof IntersectionObserver !== 'undefined') {
    quanSat = new IntersectionObserver((ds) => {
      for (const d of ds) {
        if (!d.isIntersecting) continue
        quanSat?.unobserve(d.target)
        const f = choThay.get(d.target)
        choThay.delete(d.target)
        f?.()
      }
    }, { rootMargin: '200px 0px' })
  }
  return quanSat
}

/** Kiểm thử / trang chi tiết: tải ngay URL (vẫn qua hàng đợi giới hạn song song). */
export function taiAnhQuaHang(url: string): Promise<void> {
  return hang().tai(url)
}

export function useAnhTre(el: Ref<Element | null | undefined>, url: () => string | null | undefined) {
  const daTai = ref(false)
  if (import.meta.server) return daTai
  let huy = false
  const nap = () => {
    const u = url()
    if (!u) return
    if (hang().daCo(u)) {
      daTai.value = true
      return
    }
    hang().tai(u).then(() => { if (!huy && url() === u) daTai.value = true }).catch(() => { /* giữ chỗ */ })
  }
  const theoDoi = (e: Element | null | undefined) => {
    if (!e) return
    const o = mat()
    if (!o) {
      nap()
      return
    }
    choThay.set(e, nap)
    o.observe(e)
  }
  onMounted(() => theoDoi(el.value))
  watch(url, () => {
    daTai.value = false
    const u = url()
    if (u && hang().daCo(u)) daTai.value = true
    else theoDoi(el.value)
  })
  onBeforeUnmount(() => {
    huy = true
    if (el.value) {
      quanSat?.unobserve(el.value)
      choThay.delete(el.value)
    }
  })
  return daTai
}
