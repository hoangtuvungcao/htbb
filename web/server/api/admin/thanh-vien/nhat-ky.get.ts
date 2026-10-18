/** Nhật ký cấp / gia hạn / thu hồi thành viên (200 dòng gần nhất, lọc theo tài khoản). */
export default defineAdminHandler(async (event) => {
  if (!(await tvCoBang())) tvThieuBang()
  const ten = str(getQuery(event).tai_khoan).trim().toLowerCase().slice(0, 50)
  return {
    ds: await q(dbGame(),
      `SELECT id, username, hanh_dong, ngay, het_han_cu, het_han_moi, ma_don, tac_nhan, ly_do, ip, tao_luc FROM tv_nhat_ky ${ten ? 'WHERE username = ?' : ''} ORDER BY id DESC LIMIT 200`,
      ten ? [ten] : []),
  }
})
