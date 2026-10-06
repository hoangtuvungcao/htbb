/**
 * Cấm / bỏ cấm một mã máy.
 * Body: { viec: 'cam' | 'bo_cam', ma_may, ly_do? }
 *
 * MÃ MÁY là SystemInfo.deviceUniqueIdentifier do client Unity tự khai (không phải MAC):
 * chặn được người phá lười, không chặn được người biết sửa client; cài lại ứng dụng có
 * thể sinh mã mới; máy dùng chung thì cấm một người là chặn cả nhóm.
 *
 * Lệnh có hiệu lực CHẬM NHẤT 60 GIÂY: server giữ danh sách cấm trong RAM và làm mới mỗi
 * phút (CamTheoMay.CHU_KY_LAM_MOI_MS) — cố ý, để một trận dò mật khẩu không thành một
 * trận dội database.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const viec = str(body?.viec)
  const maMay = str(body?.ma_may)

  if (!tkMaMayHopLe(maMay)) throw apiLoi(400, 'Mã máy không hợp lệ.')
  if (viec !== 'cam' && viec !== 'bo_cam') throw apiLoi(400, 'Không hiểu thao tác.')

  if (viec === 'cam') {
    // Cấm nhầm một máy mà chính bạn đang ngồi thì mất luôn đường vào game — và web không
    // gỡ hộ kịp vì server chỉ đọc lại danh sách sau 60 giây. Chặn ngay tại đây.
    const chuAdmin = await tkAdminTungDungMay(maMay)
    if (chuAdmin !== '') {
      throw apiLoi(403, `Máy này đã từng đăng nhập tài khoản quản trị (${chuAdmin}) nên không cấm được — cấm nhầm là bạn tự khoá mình.`)
    }
  }

  try {
    if (viec === 'cam') {
      const lyDo = tkCatBot(str(body?.ly_do), 255)
      await exec(dbGame(),
        `INSERT INTO ${TK_BANG_CAM_MAY} (ma_may, ly_do, boi_ai)
         VALUES (?, ?, ?)
         ON DUPLICATE KEY UPDATE ly_do = VALUES(ly_do), boi_ai = VALUES(boi_ai)`,
        [maMay, lyDo, admin.username])
      await ghiNhatKy(event, 'cam_may', maMay, lyDo)
      return { loai: 'success', message: `Đã cấm máy ${maMay}. Có hiệu lực trong vòng 60 giây.` }
    }
    await exec(dbGame(), `DELETE FROM ${TK_BANG_CAM_MAY} WHERE ma_may = ?`, [maMay])
    await ghiNhatKy(event, 'bo_cam_may', maMay, '')
    return { loai: 'success', message: `Đã bỏ cấm máy ${maMay}. Có hiệu lực trong vòng 60 giây.` }
  } catch (e: any) {
    throw apiLoi(422, `Không thao tác được: ${e?.message ?? e}`)
  }
})
