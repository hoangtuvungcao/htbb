/** Tạo / sửa một câu hỏi. Body { id?, nhom, cauHoi, traLoi, thuTu, hien }. */
export default defineAdminHandler(async (event) => {
  await canBangCms('web_faq')
  const b = await bodyObj(event)
  const id = int(b.id)
  const du = [chuoi(b.nhom, 'Nhóm', 60) || 'Chung', chuoi(b.cauHoi, 'Câu hỏi', 300, 1), chuoi(b.traLoi, 'Trả lời', 20_000, 1),
    Math.max(-9999, Math.min(9999, int(b.thuTu, 100))), b.hien === false || int(b.hien, 1) === 0 ? 0 : 1] as const
  let cu: unknown = null
  let moiId = id
  if (id > 0) {
    cu = await q1(dbGame(), 'SELECT * FROM web_faq WHERE id = ? LIMIT 1', [id])
    if (!cu) throw apiLoi(404, 'Không tìm thấy câu hỏi.')
    await exec(dbGame(), 'UPDATE web_faq SET nhom=?, cau_hoi=?, tra_loi=?, thu_tu=?, hien=?, sua_luc=NOW() WHERE id=?', [...du, id])
  }
  else {
    moiId = Number((await exec(dbGame(), 'INSERT INTO web_faq (nhom, cau_hoi, tra_loi, thu_tu, hien, sua_luc) VALUES (?, ?, ?, ?, ?, NOW())', [...du])).insertId)
  }
  xoaDemCms()
  await ghiThayDoi(event, id > 0 ? 'cms_sua_faq' : 'cms_tao_faq', `faq:${moiId}`, cu, du)
  return { ok: true, id: moiId }
})
