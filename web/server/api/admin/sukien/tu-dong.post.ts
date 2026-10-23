/**
 * Bật / tắt "Tự động theo lịch năm" cho một sự kiện. Body { ma, tuDong: 0 | 1 }.
 * 1 = máy chủ game tự bật / tắt theo tob_su_kien_lich (mặc định); 0 = thủ công (bật / tắt / hẹn giờ tay).
 */
export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const ma = str(b.ma)
  const tuDong = int(b.tuDong) === 1 ? 1 : 0
  const ten = await suKienTenTheoMa(ma)
  if (!(await coCotTuDong())) throw apiLoi(409, 'Bảng tob_su_kien chưa có cột tu_dong. Chạy sql/htbb.sql hoặc khởi động lại máy chủ game bản mới.')
  if (tuDong === 1 && !(await laMaLichNam(ma))) throw apiLoi(400, `${ten} không có lịch năm — chỉ bật / tắt tay được.`)
  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG} SET tu_dong = ? WHERE ma = ?`, [tuDong, ma])
  await ghiNhatKy(event, 'su_kien_tu_dong', ma, `${ten} -> ${tuDong ? 'tự động' : 'thủ công'}`)
  return {
    ok: true,
    message: tuDong ? `${ten}: tự động theo lịch năm (máy chủ game đặt lại bật / tắt trong vài giây).` : `${ten}: chuyển sang thủ công.`,
  }
})
