export default defineAdminHandler(async () => {
  if (!(await coBangCms('web_media'))) return { coBang: false, ds: [], toiDa: MEDIA_TOI_DA }
  const ds = await q(dbGame(), 'SELECT * FROM web_media ORDER BY id DESC LIMIT 500')
  return { coBang: true, ds: ds.map(r => ({ ...r, url: `/uploads/${r.ten_file}` })), toiDa: MEDIA_TOI_DA }
})
