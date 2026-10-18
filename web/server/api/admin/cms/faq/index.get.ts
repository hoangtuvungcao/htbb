export default defineAdminHandler(async () => {
  if (!(await coBangCms('web_faq'))) return { coBang: false, ds: [] }
  return { coBang: true, ds: await q(dbGame(), 'SELECT * FROM web_faq ORDER BY thu_tu, id') }
})
