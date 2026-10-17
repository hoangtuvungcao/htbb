/** Chép bộ FAQ tĩnh hiện có (trang /cam-nang/faq) vào CSDL để sửa tiếp. Chỉ chạy khi bảng đang trống. */
export default defineAdminHandler(async (event) => {
  await canBangCms('web_faq')
  if (Number(await qv(dbGame(), 'SELECT COUNT(*) FROM web_faq')) > 0) throw apiLoi(409, 'Bảng FAQ đã có dữ liệu.')
  let i = 0
  for (const n of FAQ_MAC_DINH) {
    for (const h of n.hoi) {
      i += 10
      await exec(dbGame(), 'INSERT INTO web_faq (nhom, cau_hoi, tra_loi, thu_tu, hien, sua_luc) VALUES (?, ?, ?, ?, 1, NOW())', [n.ten, h.label, h.content, i])
    }
  }
  xoaDemCms()
  await ghiThayDoi(event, 'cms_nhap_faq', 'faq', null, `${i / 10} câu`)
  return { ok: true, so: i / 10 }
})
