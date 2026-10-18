/** Nhật ký kiểm toán nạp (nap_nhat_ky) + webhook thô (nap_webhook). Query: ma_don (tuỳ chọn). */
export default defineAdminHandler(async (event) => {
  if (!(await napCoBang())) napThieuBang()
  const maDon = str(getQuery(event).ma_don).toUpperCase().slice(0, 16)
  const loc = /^HT[A-Z0-9]{8}$/.test(maDon)
  const nhatKy = await q(dbGame(),
    `SELECT id, tac_nhan, hanh_dong, ma_don, user_id, chi_tiet, ip, tao_luc FROM nap_nhat_ky ${loc ? 'WHERE ma_don = ?' : ''} ORDER BY id DESC LIMIT 200`,
    loc ? [maDon] : [])
  const webhook = await q(dbGame(),
    `SELECT id, nha_cung_cap, ma_gd, chu_ky_hop_le, so_tien, noi_dung, ma_don, ket_qua, ip, tao_luc FROM nap_webhook ${loc ? 'WHERE ma_don = ?' : ''} ORDER BY id DESC LIMIT 200`,
    loc ? [maDon] : [])
  const soCai = loc
    ? await q(dbGame(), 'SELECT id, loai, so_luong, so_tien, ma_tham_chieu, ghi_chu, tao_luc FROM nap_so_cai WHERE ma_don = ? ORDER BY id', [maDon])
    : []
  return { nhatKy, webhook, soCai }
})
