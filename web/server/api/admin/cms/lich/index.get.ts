export default defineAdminHandler(async () => {
  const coBangLich = await coBangCms('web_su_kien_lich')
  const ds = coBangLich ? await q(dbGame(), 'SELECT * FROM web_su_kien_lich ORDER BY tuan') : []
  return { coBang: coBangLich, ds, macDinh: (await camNangServer()).suKien }
})
