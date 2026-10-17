/** robots.txt: chặn khu quản trị/trang cá nhân, trỏ tới sitemap. */
export default defineEventHandler((event) => {
  const goc = getRequestURL(event, { xForwardedHost: true, xForwardedProto: true }).origin
  setHeader(event, 'Content-Type', 'text/plain; charset=utf-8')
  return [
    'User-agent: *',
    'Disallow: /admin',
    'Disallow: /api/',
    'Disallow: /account',
    'Disallow: /naptien',
    'Disallow: /xac-nhan-nap',
    'Disallow: /kichhoat',
    'Disallow: /logout',
    '',
    `Sitemap: ${goc}/sitemap.xml`,
    '',
  ].join('\n')
})
