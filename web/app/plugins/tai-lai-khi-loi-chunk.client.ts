/**
 * Một tệp JS của trang không tải được (WAF trả 429/403 khi điện thoại tải dồn nhiều tệp cùng lúc,
 * mạng 4G chập chờn, vừa deploy bản mới) -> Safari báo "Importing a module script failed." và Nuxt
 * hiện trang "Có lỗi xảy ra" dù chỉ cần tải lại là xong. Bắt lỗi đó, đợi một nhịp cho WAF/mạng hồi
 * rồi tải lại trang — tối đa 2 lần / 5 phút / mỗi tab để không lặp vô hạn khi lỗi thật.
 */
const LOI_TAI_MODULE = /Importing a module script failed|Failed to fetch dynamically imported module|error loading dynamically imported module|Unable to preload|Failed to load module script/i
const KHOA = 'htbb:tai-lai-chunk'
const TOI_DA = 2
const CUA_SO_MS = 5 * 60 * 1000

function laLoiTaiModule(err: unknown): boolean {
  const e = err as { message?: unknown, cause?: unknown } | null | undefined
  const msg = String(e?.message ?? err ?? '')
  if (LOI_TAI_MODULE.test(msg)) return true
  return e?.cause ? laLoiTaiModule(e.cause) : false
}

export default defineNuxtPlugin({
  name: 'htbb:tai-lai-khi-loi-chunk',
  enforce: 'pre',
  setup(nuxtApp) {
    let daHen = false
    const thu = (err: unknown) => {
      if (daHen || !laLoiTaiModule(err)) return
      let lan: number[] = []
      try {
        lan = (JSON.parse(sessionStorage.getItem(KHOA) || '[]') as number[]).filter(t => Date.now() - t < CUA_SO_MS)
      } catch {}
      if (lan.length >= TOI_DA) return
      daHen = true
      try {
        sessionStorage.setItem(KHOA, JSON.stringify([...lan, Date.now()]))
      } catch {}
      setTimeout(() => window.location.reload(), 1500)
    }
    nuxtApp.hook('app:chunkError', ({ error }) => thu(error))
    nuxtApp.hook('app:error', thu)
    nuxtApp.hook('vue:error', thu)
    window.addEventListener('unhandledrejection', ev => thu(ev.reason))
  },
})
