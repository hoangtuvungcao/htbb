/**
 * Nhập lịch 52 tuần từ dữ liệu game (bảng data_su_kien_nam; CSDL không đọc được thì bản cam-nang.json lúc build).
 * Body { ghiDe?: boolean } — mặc định chỉ thêm tuần còn thiếu, không đè tuần đã sửa tay.
 */
export default defineAdminHandler(async (event) => {
  await canBangCms('web_su_kien_lich')
  const ghiDe = (await bodyObj(event).catch(() => ({} as Record<string, unknown>))).ghiDe === true
  let them = 0
  for (const s of (await camNangServer()).suKien) {
    const co = await qv(dbGame(), 'SELECT tuan FROM web_su_kien_lich WHERE tuan = ? LIMIT 1', [s.tuan])
    if (co && !ghiDe) continue
    if (co) await exec(dbGame(), 'DELETE FROM web_su_kien_lich WHERE tuan = ?', [s.tuan])
    await exec(dbGame(), 'INSERT INTO web_su_kien_lich (tuan, thang, ten1, ten2, phan, loai, co_che, so_ngay, phan_thuong, moc, sua_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())',
      [s.tuan, s.thang, s.ten1, s.ten2, s.phan, s.loai, s.coChe, s.soNgay, s.phanThuong, s.mocGiuChan])
    them++
  }
  xoaDemCms()
  await ghiThayDoi(event, 'cms_nhap_lich', 'lich', null, `${them} tuần${ghiDe ? ' (ghi đè)' : ''}`)
  return { ok: true, them }
})
