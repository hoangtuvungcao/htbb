/** Sửa một tuần. Body { tuan, thang, ten1, ten2, phan, loai, coChe, soNgay, phanThuong, mocGiuChan }. */
export default defineAdminHandler(async (event) => {
  await canBangCms('web_su_kien_lich')
  const b = await bodyObj(event)
  const tuan = int(b.tuan)
  if (tuan < 1 || tuan > 53) throw apiLoi(400, 'Tuần phải từ 1 đến 53.')
  const loai = str(b.loai)
  if (!Object.keys(LOAI_SU_KIEN).includes(loai)) throw apiLoi(400, 'Loại sự kiện không hợp lệ.')
  const du = [tuan, Math.max(1, Math.min(12, int(b.thang, 1))), chuoi(b.ten1, 'Tên năm 1', 200, 1), chuoi(b.ten2, 'Tên năm 2', 200),
    chuoi(b.phan, 'Phần', 10), loai, chuoi(b.coChe, 'Cơ chế', 500), Math.max(1, Math.min(60, int(b.soNgay, 7))),
    chuoi(b.phanThuong, 'Phần thưởng', 300), chuoi(b.mocGiuChan, 'Mốc', 100)] as const
  const cu = await q1(dbGame(), 'SELECT * FROM web_su_kien_lich WHERE tuan = ? LIMIT 1', [tuan])
  if (cu) {
    await exec(dbGame(), 'UPDATE web_su_kien_lich SET thang=?, ten1=?, ten2=?, phan=?, loai=?, co_che=?, so_ngay=?, phan_thuong=?, moc=?, sua_luc=NOW() WHERE tuan=?',
      [...du.slice(1), tuan])
  }
  else {
    await exec(dbGame(), 'INSERT INTO web_su_kien_lich (tuan, thang, ten1, ten2, phan, loai, co_che, so_ngay, phan_thuong, moc, sua_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())', [...du])
  }
  xoaDemCms()
  await ghiThayDoi(event, 'cms_sua_lich', `lich:tuan${tuan}`, cu, du)
  return { ok: true }
})
