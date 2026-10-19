/**
 * Cấm / bỏ cấm một mã máy.
 * Body: { viec: 'cam' | 'bo_cam', ma_may, ly_do? }
 *
 * MÃ MÁY là chuỗi ngẫu nhiên client HTBB sinh một lần và tự khai (AutoGuard.maMay, không phải MAC):
 * chặn được người phá lười, không chặn được người biết sửa client; cài lại ứng dụng có
 * thể sinh mã mới; máy dùng chung thì cấm một người là chặn cả nhóm.
 *
 * Lệnh có hiệu lực CHẬM NHẤT 60 GIÂY: server giữ danh sách cấm trong RAM và làm mới mỗi
 * phút (CamTheoMay.CHU_KY_LAM_MOI_MS) — cố ý, để một trận dò mật khẩu không thành một
 * trận dội database.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await bodyObj(event)
  const viec = str(body?.viec)
  const maMay = str(body?.ma_may)

  if (!tkMaMayHopLe(maMay)) throw apiLoi(400, 'Mã máy không hợp lệ.')
  if (viec !== 'cam' && viec !== 'bo_cam') throw apiLoi(400, 'Không hiểu thao tác.')
  const lyDo = viec === 'cam' ? tkCatBot(chuoi(body?.ly_do, 'Lý do', 255, 3), 255) : ''

  if (viec === 'cam') {
    // Cấm nhầm một máy mà chính bạn đang ngồi thì mất luôn đường vào game — và web không
    // gỡ hộ kịp vì server chỉ đọc lại danh sách sau 60 giây. Chặn ngay tại đây.
    const chuAdmin = await tkAdminTungDungMay(maMay)
    if (chuAdmin !== '') {
      throw apiLoi(403, `Máy này đã từng đăng nhập tài khoản quản trị (${chuAdmin}) nên không cấm được — cấm nhầm là bạn tự khoá mình.`)
    }
  }

  let truoc: Record<string, unknown> | null = null
  try {
    truoc = await q1(dbGame(), `SELECT ma_may, ly_do, boi_ai, luc FROM ${TK_BANG_CAM_MAY} WHERE ma_may = ? LIMIT 1`, [maMay])
    if (viec === 'cam') {
      await exec(dbGame(),
        `INSERT INTO ${TK_BANG_CAM_MAY} (ma_may, ly_do, boi_ai)
         VALUES (?, ?, ?)
         ON DUPLICATE KEY UPDATE ly_do = VALUES(ly_do), boi_ai = VALUES(boi_ai)`,
        [maMay, lyDo, admin.username])
    } else {
      if (!truoc) return { loai: 'success', message: `Máy ${maMay} không bị cấm.` }
      await exec(dbGame(), `DELETE FROM ${TK_BANG_CAM_MAY} WHERE ma_may = ?`, [maMay])
    }
  } catch (e: any) {
    console.error('[cammay]', e?.code ?? e)
    throw apiLoi(422, 'Không thao tác được (lỗi cơ sở dữ liệu — xem log máy chủ web; bảng tob_cam_may có thể chưa được tạo).')
  }
  await ghiThayDoi(event, viec === 'cam' ? 'cam_may' : 'bo_cam_may', `may:${maMay}`.slice(0, 100), truoc, viec === 'cam' ? { ma_may: maMay, ly_do: lyDo, boi_ai: admin.username } : null)
  return { loai: 'success', message: viec === 'cam' ? `Đã cấm máy ${maMay}. Có hiệu lực trong vòng 60 giây.` : `Đã bỏ cấm máy ${maMay}. Có hiệu lực trong vòng 60 giây.` }
})
