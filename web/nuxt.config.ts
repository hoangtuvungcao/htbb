// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2026-10-01',
  devtools: { enabled: false },

  modules: ['@nuxt/ui', '@tresjs/nuxt', '@vueuse/nuxt', 'motion-v/nuxt'],

  css: ['~/assets/css/main.css'],

  app: {
    head: {
      htmlAttrs: { lang: 'vi' },
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        {
          name: 'description',
          content:
            'Khám phá Khí Phách Anh Hùng: năm môn phái, bang hội và hành trình kiếm hiệp cùng bằng hữu. Tải game và đăng ký tài khoản.',
        },
      ],
      link: [{ rel: 'icon', type: 'image/svg+xml', href: '/logo-mark.svg' }],
    },
  },

  // Khu quản trị không cần SEO và phụ thuộc token trong trình duyệt -> chỉ render phía client.
  // Trang công khai vẫn SSR để tải nhanh và có SEO.
  routeRules: {
    // Không cho trang khác nhúng website vào iframe (chống lừa bấm vào nút admin),
    // không cho trình duyệt đoán kiểu nội dung, không gửi đường dẫn đầy đủ ra ngoài.
    '/**': {
      headers: {
        'X-Frame-Options': 'SAMEORIGIN',
        'X-Content-Type-Options': 'nosniff',
        'Referrer-Policy': 'strict-origin-when-cross-origin',
      },
    },
    '/admin': { ssr: false, headers: { 'X-Robots-Tag': 'noindex, nofollow' } },
    '/admin/**': { ssr: false, headers: { 'X-Robots-Tag': 'noindex, nofollow' } },
    '/xac-nhan-nap': { ssr: false, headers: { 'X-Robots-Tag': 'noindex, nofollow' } },
    // Trang cần đăng nhập người chơi: token nằm trong trình duyệt nên render phía client.
    '/account': { ssr: false },
    '/kichhoat': { ssr: false },
    '/naptien': { ssr: false },
    '/logout': { ssr: false },
    '/api/**': { headers: { 'Cache-Control': 'no-store' } },
  },

  // Icon được đóng gói sẵn (không gọi API Iconify lúc chạy) — máy chủ game có thể không ra được Internet.
  icon: {
    serverBundle: 'local',
    clientBundle: { scan: true },
  },

  // Không tải font từ Google lúc chạy: tự host khi build.
  fonts: {
    families: [
      { name: 'Be Vietnam Pro', provider: 'google', weights: [400, 500, 600, 700, 800] },
      { name: 'Noto Serif', provider: 'google', weights: [600, 700, 800], styles: ['normal', 'italic'] },
    ],
  },

  colorMode: {
    preference: 'dark',
    fallback: 'dark',
  },

  ui: {
    theme: {
      colors: ['primary', 'secondary', 'success', 'info', 'warning', 'error', 'neutral'],
    },
  },

  nitro: {
    compressPublicAssets: true,
  },

  typescript: {
    strict: true,
  },
})
