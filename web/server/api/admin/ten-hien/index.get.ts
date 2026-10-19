/** Danh sách tên NPC + tên bản đồ (data_npc / data_map_ten) cho trang /admin/ten-hien. */
export default defineAdminHandler(async () => {
  return await docTenHien()
})
