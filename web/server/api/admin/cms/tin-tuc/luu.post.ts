/**
 * Tạo / sửa bài. Body { id?, slug, tieuDe, tomTat, anhBia, loai, the, noiDung, trangThai: 0|1, dangLuc?, ghim }.
 * trangThai 1 + dangLuc trong tương lai = hẹn giờ (bài tự hiện khi tới giờ).
 */
import { CMS_MAC_DINH, KHOA_CMS, hopNhatCms, lienKetAnToan, tronSau } from '../../../../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, KhoaCms } from '../../../../../shared/utils/cmsMacDinh'

export default defineAdminHandler(async (event, admin) => {
  await canBangCms('web_tin_tuc')
  const b = await bodyObj(event)
  const id = int(b.id)
  const slug = slugHopLe(b.slug)
  const du = {
    slug,
    tieu_de: chuoi(b.tieuDe, 'Tiêu đề', 200, 1),
    tom_tat: chuoi(b.tomTat, 'Tóm tắt', 500),
    anh_bia: lienKetAnToan(chuoi(b.anhBia, 'Ảnh bìa', 300)),
    loai: chuoi(b.loai, 'Chuyên mục', 40) || 'Thông báo',
    the: chuoi(b.the, 'Thẻ', 300),
    noi_dung: chuoi(b.noiDung, 'Nội dung', 300_000),
    trang_thai: int(b.trangThai) === 1 ? 1 : 0,
    dang_luc: ngayGio(b.dangLuc) ?? (int(b.trangThai) === 1 ? gioHienTai() : null),
    ghim: b.ghim === true || int(b.ghim) === 1 ? 1 : 0,
  }
  const trung = await qv<number>(dbGame(), 'SELECT id FROM web_tin_tuc WHERE slug = ? AND id <> ? LIMIT 1', [slug, id])
  if (trung) throw apiLoi(409, `Đã có bài khác dùng đường dẫn "${slug}".`)
  let cu: unknown = null
  let moiId = id
  if (id > 0) {
    cu = await q1(dbGame(), 'SELECT * FROM web_tin_tuc WHERE id = ? LIMIT 1', [id])
    if (!cu) throw apiLoi(404, 'Không tìm thấy bài viết.')
    await exec(dbGame(),
      `UPDATE web_tin_tuc SET slug=?, tieu_de=?, tom_tat=?, anh_bia=?, loai=?, the=?, noi_dung=?, trang_thai=?, dang_luc=?, ghim=?, sua_luc=NOW() WHERE id=?`,
      [du.slug, du.tieu_de, du.tom_tat, du.anh_bia, du.loai, du.the, du.noi_dung, du.trang_thai, du.dang_luc, du.ghim, id])
  }
  else {
    const r = await exec(dbGame(),
      `INSERT INTO web_tin_tuc (slug, tieu_de, tom_tat, anh_bia, loai, the, noi_dung, trang_thai, dang_luc, ghim, tac_gia, tao_luc, sua_luc)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())`,
      [du.slug, du.tieu_de, du.tom_tat, du.anh_bia, du.loai, du.the, du.noi_dung, du.trang_thai, du.dang_luc, du.ghim, admin.username])
    moiId = Number(r.insertId)
  }
  xoaDemCms()
  await ghiThayDoi(event, id > 0 ? 'cms_sua_tin' : 'cms_tao_tin', `tin:${slug}`, cu, du)
  return { ok: true, id: moiId }
})
