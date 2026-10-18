/**
 * robots.txt. Địa chỉ gốc lấy từ HTBB_SITE_URL (server/utils/config.ts) — không theo Host của request.
 * Chưa có tên miền thật (trống / localhost / IP) -> chặn toàn bộ, tránh bị lập chỉ mục bằng IP.
 */
export default defineEventHandler((event) => {
  const { siteUrl, choIndex } = useHtbbConfig().site
  setHeader(event, 'Content-Type', 'text/plain; charset=utf-8')
  if (!choIndex) {
    return [
      '# Chưa cấu hình tên miền (HTBB_SITE_URL) -> không cho lập chỉ mục.',
      'User-agent: *',
      'Disallow: /',
      '',
      `Sitemap: ${siteUrl}/sitemap.xml`,
      '',
    ].join('\n')
  }
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
    `Sitemap: ${siteUrl}/sitemap.xml`,
    '',
  ].join('\n')
})
