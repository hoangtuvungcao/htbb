/**
 * Gỡ header "x-powered-by: Nuxt" (lộ framework cho kẻ dò lỗ hổng, không có ích cho người dùng).
 * Renderer của Nuxt gắn header này SAU middleware, nên phải gỡ ở hook: 'render:response' (trang
 * render thường / payload) và 'beforeResponse' (trang stream, các route khác) — cả hai chạy trước
 * khi header được gửi đi.
 */
export default defineNitroPlugin((nitroApp) => {
  nitroApp.hooks.hook('render:response', (response) => {
    if (response?.headers) {
      for (const k of Object.keys(response.headers)) {
        if (k.toLowerCase() === 'x-powered-by') delete response.headers[k]
      }
    }
  })
  nitroApp.hooks.hook('beforeResponse', (event) => {
    if (!event.node.res.headersSent) event.node.res.removeHeader('x-powered-by')
  })
})
