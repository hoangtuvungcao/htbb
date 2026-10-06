/**
 * Xoá cả hai mốc lịch hẹn của một sự kiện.
 * Body: { ma }
 */
export default defineAdminHandler(async (event) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const ma = str(body?.ma)
  const ten = await suKienTenTheoMa(ma)

  await exec(dbGame(), `UPDATE ${SU_KIEN_BANG}
                           SET mo_luc = NULL, dong_luc = NULL
                         WHERE ma = ?`, [ma])
  await ghiNhatKy(event, 'su_kien_xoa_lich', ma, ten)
  return { ok: true, message: `Đã xoá lịch hẹn của ${ten}.` }
})
