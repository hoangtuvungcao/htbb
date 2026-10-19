/**
 * Nhật ký thao tác của chính các admin (bảng htbb_db.web_admin_log).
 *
 * Đây là thứ trả lời câu “ai đã khoá tài khoản này?”, “mã quà kia ở đâu ra?”.
 * CỐ Ý CHỈ CÓ API ĐỌC, không có API xoá: một cuốn nhật ký xoá được thì không còn
 * là bằng chứng. Muốn dọn dòng cũ thì chạy tay câu DELETE ghi trong
 * du_lieu/sql_nguon/them_bang_web_admin_log.sql.
 *
 *   GET /api/admin/nhatky?ai=&viec=&doi=&trang=1
 */
const MOI_TRANG = 50

export default defineAdminHandler(async (event) => {
  const c = useHtbbConfig()
  const goiY = {
    dbGame: c.db.game,
    maxFail: Math.trunc(c.admin.maxFail),
    failPhut: Math.trunc(c.admin.failWindow / 60),
  }

  if (!(await coBangNhatKy())) {
    return { coBang: false, ...goiY, dsViec: [], dong: [], conTrangSau: false, trang: 1, loiDoc: '' }
  }

  const query = getQuery(event)
  const ai = str(query.ai)
  const viec = str(query.viec)
  const doi = str(query.doi)
  const trang = Math.min(1_000_000, Math.max(1, int(query.trang, 1)))

  let dsViec: string[] = []
  let dong: Record<string, unknown>[] = []
  let conTrangSau = false
  let loiDoc = ''

  try {
    const ds = await q<{ hanh_dong: string }>(dbGame(), 'SELECT DISTINCT hanh_dong FROM web_admin_log ORDER BY hanh_dong')
    dsViec = ds.map(r => String(r.hanh_dong))

    const dieuKien: string[] = []
    const thamSo: string[] = []
    if (ai !== '') { dieuKien.push('admin_user = ?'); thamSo.push(ai) }
    if (viec !== '') { dieuKien.push('hanh_dong = ?'); thamSo.push(viec) }
    if (doi !== '') { dieuKien.push('doi_tuong = ?'); thamSo.push(doi) }
    const where = dieuKien.length ? `WHERE ${dieuKien.join(' AND ')}` : ''

    // Ghép LIMIT/OFFSET bằng số nguyên đã ép kiểu (trang đã chặn trên ở trên);
    // lấy dư 1 dòng để biết còn trang sau mà không phải COUNT(*).
    const lay = MOI_TRANG + 1
    const bo = (trang - 1) * MOI_TRANG
    dong = await q(
      dbGame(),
      `SELECT id, admin_user, hanh_dong, doi_tuong, chi_tiet, ip, thoi_gian
         FROM web_admin_log ${where} ORDER BY id DESC LIMIT ${lay} OFFSET ${bo}`,
      thamSo,
    )
    if (dong.length > MOI_TRANG) {
      conTrangSau = true
      dong.pop()
    }
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
  }

  return { coBang: true, ...goiY, ai, viec, doi, trang, dsViec, dong, conTrangSau, loiDoc }
})
