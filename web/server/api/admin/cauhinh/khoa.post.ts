/**
 * Sửa LẺ một khoá cau_hinh trong whitelist (DANH_MUC_CAU_HINH[khoa].suaLe, shared/utils/cauHinhDanhMuc.ts).
 * Body: { khoa: string, giaTri: string | number | boolean }
 * Kiểm kiểu + khoảng chặt (ngoài khoảng = 400, không kẹp ngầm). Server game đọc lại sau ≤ 10 giây.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = (await readBody<Record<string, unknown>>(event).catch(() => null)) ?? {}
  const kq = kiemGiaTriKhoa(body.khoa, body.giaTri)
  if (!kq.ok) throw apiLoi(400, kq.loi)
  const khoa = String(body.khoa)
  const truoc = await cauHinhDocKhoa([khoa])
  if (truoc[khoa] === kq.giaTri) return { ok: true, message: 'Giá trị không đổi.' }
  await cauHinhLuu({ [khoa]: kq.giaTri }, admin.username)
  await ghiThayDoi(event, 'sua_cau_hinh', `cau_hinh:${khoa}`, truoc, { [khoa]: kq.giaTri })
  return { ok: true, message: `Đã lưu ${DANH_MUC_CAU_HINH[khoa]!.ten}. Server áp dụng trong tối đa 10 giây.` }
})
