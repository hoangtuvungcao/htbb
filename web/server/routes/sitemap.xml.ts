/**
 * sitemap.xml sinh từ danh mục trang (cẩm nang, Pokédex, tin tức) — không cần module ngoài.
 * Địa chỉ gốc lấy theo request (đứng sau proxy thì nhớ chuyển tiếp Host / X-Forwarded-Proto).
 */
export default defineEventHandler(async (event) => {
  const goc = getRequestURL(event, { xForwardedHost: true, xForwardedProto: true }).origin
  const trang = [
    '/', '/gioi-thieu', '/huong-dan', '/cam-nang', '/pokedex', '/download', '/news', '/ranking',
    '/ghi-cong', '/dieu-khoan', '/quyen-rieng-tu', '/register',
    ...DS_CAM_NANG.map(p => `/cam-nang/${p.slug}`),
    ...(await dsTinCongKhai()).map(t => `/news/${t.slug}`),
    ...(await dsTrangDb()).filter(t => t.hien && !t.coSan).map(t => `/cam-nang/${t.slug}`),
    ...POKEDEX.loai.map(l => `/pokedex/${l.dex}`),
  ]
  const uutien = (p: string) => (p === '/' ? '1.0' : p.startsWith('/pokedex/') || p.startsWith('/news/') ? '0.5' : '0.8')
  const xml = `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${
    trang.map(p => `  <url><loc>${goc}${p}</loc><priority>${uutien(p)}</priority></url>`).join('\n')
  }\n</urlset>\n`
  setHeader(event, 'Content-Type', 'application/xml; charset=utf-8')
  setHeader(event, 'Cache-Control', 'public, max-age=3600')
  return xml
})
