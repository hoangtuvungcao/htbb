/** Danh sách người có quyền vào khu quản trị (biến môi trường + bảng vai trò). Chỉ siêu quản trị. */
export default defineAdminHandler(async () => {
  const coBangVt = await coBangCms('web_admin_vai_tro')
  const ds = coBangVt ? await q(dbGame(), 'SELECT * FROM web_admin_vai_tro ORDER BY username') : []
  return { coBang: coBangVt, env: adminAccounts(), ds, vaiTro: TEN_VAI_TRO_ADMIN }
})
