/** Mọi gói (kể cả đã ngừng bán) để sửa. -> { goi, tyGia } */
export default defineAdminHandler(async () => {
  if (!(await napCoBang())) napThieuBang()
  return { goi: await napDanhSachGoi(false), tyGia: LUONG_MOI_1000_VND }
})
