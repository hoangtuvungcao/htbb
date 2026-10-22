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
      // biểu tượng chung với game (client_jar/tools/tao_logo.py --web)
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/logo-mark.svg' },
        { rel: 'icon', type: 'image/png', sizes: '32x32', href: '/favicon-32.png' },
        { rel: 'alternate icon', href: '/favicon.ico' },
        { rel: 'apple-touch-icon', sizes: '180x180', href: '/apple-touch-icon.png' },
      ],
      script: [
        {
          // Tệp JS chính (entry/vendor) bị WAF trả 429/403 hay mạng rớt -> không dòng Nuxt nào chạy,
          // trang đứng ở bản SSR (menu/form chết). Script inline nhỏ này (CSP cho 'unsafe-inline') tải
          // lại trang một lần nếu sau 2.5 giây app vẫn chưa mount. Cùng khoá/giới hạn với
          // app/plugins/tai-lai-khi-loi-chunk.client.ts (tối đa 2 lần / 5 phút / tab).
          key: 'htbb-tai-lai-js',
          tagPriority: 'critical',
          innerHTML: `(function(){var K='htbb:tai-lai-chunk',hen=false;function mount(){var n=document.getElementById('__nuxt');return !!(n&&n.__vue_app__)}function thu(){if(mount())return;try{var a=JSON.parse(sessionStorage.getItem(K)||'[]').filter(function(t){return Date.now()-t<300000});if(a.length>=2)return;a.push(Date.now());sessionStorage.setItem(K,JSON.stringify(a))}catch(e){return}location.reload()}addEventListener('error',function(e){var t=e.target;if(hen||!t||!t.tagName)return;if((t.tagName==='SCRIPT'&&t.type==='module')||(t.tagName==='LINK'&&/modulepreload/.test(t.rel))){hen=true;setTimeout(thu,2500)}},true)})()`,
        },
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
    // Ảnh / phông tĩnh của game: cache dài ở trình duyệt + CDN (đổi ảnh thì đổi tên tệp hoặc chờ hết hạn 7 ngày).
    // /_nuxt/** (JS/CSS có hash trong tên) Nitro đã tự đặt immutable 1 năm.
    ...Object.fromEntries(['/pokemon/**', '/anh-game/**', '/keyart/**', '/vat-pham/**', '/tien/**', '/chieu/**', '/ban-do/**', '/og/**'].map(r => [r, {
      headers: { 'Cache-Control': 'public, max-age=604800, stale-while-revalidate=86400' },
    }])),
    '/_fonts/**': { headers: { 'Cache-Control': 'public, max-age=31536000, immutable' } },
    ...Object.fromEntries(['/logo.svg', '/logo-gon.svg', '/logo-mark.svg', '/favicon.ico', '/favicon-32.png', '/apple-touch-icon.png'].map(r => [r, {
      headers: { 'Cache-Control': 'public, max-age=86400, stale-while-revalidate=604800' },
    }])),
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
      // Chỉ bộ latin + vietnamese (đủ mọi chữ tiếng Việt) và preload để chữ không nhảy khi phông tải xong.
      { name: 'Nunito', provider: 'google', weights: [400, 600, 700, 800, 900], subsets: ['latin', 'vietnamese'], preload: true },
      { name: 'Baloo 2', provider: 'google', weights: [600, 700, 800], subsets: ['latin', 'vietnamese'], preload: true },
      // Font pixel KHÔNG có tiếng Việt: chỉ dùng cho chữ ASCII ngắn (số dex, "LV").
      { name: 'Press Start 2P', provider: 'google', weights: [400], subsets: ['latin'] },
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

  vite: {
    $client: {
      build: {
        // iPhone/iPad đời cũ: mặc định Vite 8 nhắm Safari 16.4 (cú pháp mới + import map cho
        // "#entry" -> iOS 15 / 16.0-16.3 không chạy được dòng JS nào, trang đứng ở bản SSR, menu/form
        // không phản hồi). Hạ đích để rolldown dịch cú pháp; Nuxt tự tắt import map khi đích < 16.4.
        // (CSS Tailwind v4 vẫn cần Safari 15.4+ cho @layer — iOS 15.4 là sàn thực tế.)
        target: ['es2020', 'safari15', 'ios15', 'chrome100', 'edge100', 'firefox100'],
        rolldownOptions: {
          output: {
            // Mặc định mỗi trang kéo ~80 chunk JS nhỏ trong <1 giây. WAF phía trước (giới hạn
            // request/giây theo IP — mạng 4G dùng chung IP CGNAT) trả 429 cho một phần trong số đó ->
            // "Importing a module script failed" -> trang lỗi "Có lỗi xảy ra". Gom thư viện
            // (node_modules) và component dùng chung thành vài tệp lớn, cache 1 năm (tên có hash).
            codeSplitting: {
              groups: [
                { name: 'vendor', test: /[\\/]node_modules[\\/]/, priority: 20 },
                { name: 'app', test: /[\\/]app[\\/](components|composables|utils)[\\/]/, priority: 10, minShareCount: 2 },
              ],
            },
          },
        },
      },
    },
  },

  typescript: {
    strict: true,
  },
})
