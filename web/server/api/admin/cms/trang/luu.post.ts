/** Tạo / sửa trang cẩm nang. Body { id?, slug, tieuDe, nhom, icon, moTa, tuKhoa, thuTu, noiDung, trangThai }. */
export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_trang')
  const b = await bodyObj(event)
  const id = int(b.id)
  const slug = slugHopLe(b.slug)
  const du = {
    slug,
    tieu_de: chuoi(b.tieuDe, 'Tiêu đề', 200, 1),
    nhom: chuoi(b.nhom, 'Nhóm', 60) || 'Thế giới',
    icon: /^i-lucide-[a-z0-9-]+$/.test(str(b.icon)) ? str(b.icon) : 'i-lucide-file-text',
    mo_ta: chuoi(b.moTa, 'Mô tả', 500),
    tu_khoa: chuoi(b.tuKhoa, 'Từ khoá', 300),
    thu_tu: Math.max(-9999, Math.min(9999, int(b.thuTu, 100))),
    noi_dung: chuoi(b.noiDung, 'Nội dung', 300_000),
    trang_thai: int(b.trangThai, 1) === 1 ? 1 : 0,
  }
  const trung = await qv<number>(dbGame(), 'SELECT id FROM web_trang WHERE slug = ? AND id <> ? LIMIT 1', [slug, id])
  if (trung) throw apiLoi(409, `Đã có trang khác dùng đường dẫn "${slug}".`)
  let cu: unknown = null
  let moiId = id
  if (id > 0) {
    cu = await q1(dbGame(), 'SELECT * FROM web_trang WHERE id = ? LIMIT 1', [id])
    if (!cu) throw apiLoi(404, 'Không tìm thấy trang.')
    await exec(dbGame(), 'UPDATE web_trang SET slug=?, tieu_de=?, nhom=?, icon=?, mo_ta=?, tu_khoa=?, thu_tu=?, noi_dung=?, trang_thai=?, tac_gia=?, sua_luc=NOW() WHERE id=?',
      [du.slug, du.tieu_de, du.nhom, du.icon, du.mo_ta, du.tu_khoa, du.thu_tu, du.noi_dung, du.trang_thai, admin.username, id])
  }
  else {
    const r = await exec(dbGame(), 'INSERT INTO web_trang (slug, tieu_de, nhom, icon, mo_ta, tu_khoa, thu_tu, noi_dung, trang_thai, tac_gia, sua_luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())',
      [du.slug, du.tieu_de, du.nhom, du.icon, du.mo_ta, du.tu_khoa, du.thu_tu, du.noi_dung, du.trang_thai, admin.username])
    moiId = Number(r.insertId)
  }
  xoaDemCms()
  await ghiThayDoi(event, id > 0 ? 'cms_sua_trang' : 'cms_tao_trang', `trang:${slug}`, cu, du)
  return { ok: true, id: moiId }
})
