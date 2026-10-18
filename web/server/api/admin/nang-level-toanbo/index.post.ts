/**
 * Xếp hàng lệnh đưa toàn bộ nhân vật (del = 1) lên đúng cấp đích. Body: { cap_do_dich }.
 * Không UPDATE thẳng tob_char (xem server/utils/nangLevelToanBo.ts và lenhNhanVat.ts).
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  // Chỉ nhận số nguyên viết đúng dạng (không nhận "5.0", "5abc"), trong khoảng cho phép.
  const tho = str(body?.cap_do_dich)
  const capDich = /^[+-]?\d+$/.test(tho) ? Number(tho) : Number.NaN
  if (!Number.isInteger(capDich) || capDich < 1 || capDich > CAPDO_TOI_DA) {
    throw apiLoi(400, `Cấp độ đích phải là số nguyên từ 1 đến ${CAPDO_TOI_DA}.`)
  }

  const db = dbGame()
  await taoBangLenhNhanVat(db)
  const dsNV = await q<{ id: number, charname: string }>(db, 'SELECT id, charname FROM tob_char WHERE del = 1')

  if (dsNV.length === 0) {
    return { loai: 'success' as const, thongBao: 'Không có Pokémon nào đang hoạt động — không có gì để nâng level.' }
  }

  await transaction(db, async (conn) => {
    for (const nv of dsNV) {
      await exec(conn,
        `INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, phan_nghin, tham_so, admin_user)
         VALUES (?, ?, ?, ?, 0, ?, ?)`,
        [LENH_LOAI_DAT_CAP_DO, Number(nv.id), catLenh(String(nv.charname), 50),
          capDich, TANG_LEVEL_TOANBO_DANH_DAU, catLenh(admin.username, 50)])
    }
  })

  await ghiNhatKy(event, 'nang_level_toanbo', '', `đã xếp hàng đợi đưa ${dsNV.length} Pokémon lên cấp ${capDich}`)
  return {
    loai: 'success' as const,
    thongBao: `Đã xếp hàng đợi đưa ${dsNV.length} Pokémon lên cấp ${capDich}`
      + ' (0%). Pokémon đang online sẽ được nâng NGAY; offline sẽ được nâng khi vào game. '
      + 'Server game sẽ tự xử lý dần ở nền — tải lại trang này để xem tiến độ.',
  }
})
