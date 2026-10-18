/**
 * Trạng thái bảo trì đăng nhập: tệp bao_tri.txt (có không, câu thông báo, tài khoản được vào),
 * web có ghi được không, và cờ bao_tri game tự báo trong tob_chi_so_may_chu (mẫu 30 giây).
 */
export default defineAdminHandler(async () => {
  const [tep, game] = await Promise.all([trangThaiBaoTriTep(), baoTriTheoGame().catch(() => ({ co: false, baoTri: null, cachGiay: null }))])
  return {
    ...tep,
    cauHienThi: tep.noiDung ? baoTriCauHienThi(tep.noiDung.cau) : null,
    cauMacDinh: BAO_TRI_CAU_MAC_DINH,
    game,
    gioiHan: { cau: BAO_TRI_CAU_TOI_DA, soTaiKhoan: BAO_TRI_SO_TK_TOI_DA },
    // Thư mục game thật trên VPS (HTBB/vps/htbb-game.service: WorkingDirectory=/opt/htbb, User=htbb).
    thuMucVps: '/opt/htbb',
  }
})
