// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2026-10-01',
  devtools: { enabled: false },

  modules: ['@nuxt/ui', '@vueuse/nuxt', 'motion-v/nuxt'],

  css: ['~/assets/css/main.css'],

  app: {
    head: {
      htmlAttrs: { lang: 'vi' },
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        {
          name: 'description',
          content:
            'MMORPG nơi chính bạn là một Pokémon: chọn 1 trong 5 Pokémon khởi đầu, lên cấp, tiến hoá và phiêu lưu cùng bạn bè. Tải game và đăng ký tài khoản.',
        },
      ],
      // biểu tượng chung với game (htbb-client/j2me/tools/tao_logo.py --web)
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/logo-mark.svg' },
        { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/favicon-32.png' },
        { rel: 'alternate icon', href: '/favicon.ico' },
        { rel: 'apple-touch-icon', sizes: '180x180', href: '/apple-touch-icon.png' },
      ],
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
    // Trang ghi công đổi tên: giữ link cũ (301) cho bên ngoài đã trỏ tới.
    '/ghi-cong': { redirect: { to: '/giay-phep', statusCode: 301 } },
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
      // Cả hai đều có bộ chữ tiếng Việt (subset "vietnamese" trên Google Fonts).
      { name: 'Nunito', provider: 'google', weights: [400, 600, 700, 800, 900] },
      { name: 'Baloo 2', provider: 'google', weights: [500, 600, 700, 800] },
      // Font pixel KHÔNG có tiếng Việt: chỉ dùng cho chữ ASCII ngắn (số dex, "LV").
      { name: 'Press Start 2P', provider: 'google', weights: [400] },
    ],
  },

  // Mặc định sáng (trời xanh, cỏ xanh); vẫn có chế độ tối (navy) qua nút đổi màu ở đầu trang.
  colorMode: {
    preference: 'light',
    fallback: 'light',
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
