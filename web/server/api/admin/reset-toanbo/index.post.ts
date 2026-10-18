/**
 * RESET TOÀN SERVER — body: { xac_nhan: 'XOA TOAN BO' }. KHÔNG THỂ HOÀN TÁC.
 * Phải gõ đúng cụm xác nhận (kiểm ở đây, không chỉ ở giao diện). Tài khoản quản trị
 * được loại trừ. Xem server/utils/resetToanBo.ts.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  if (str(body?.xac_nhan) !== RESET_TOANBO_CUM_TU) {
    throw apiLoi(400, `Gõ đúng cụm "${RESET_TOANBO_CUM_TU}" để xác nhận.`)
  }

  const idsAdmin = await resetIdTaiKhoanAdmin()
  const [dk, th] = resetDieuKienNgoaiAdmin(idsAdmin)

  const db = dbGame()
  const dsNV = await q<{ id: number, charname: string, userId: number }>(db, `SELECT id, charname, userId FROM tob_char${dk}`, th)

  if (dsNV.length === 0) {
    return { loai: 'success' as const, thongBao: 'Không có Pokémon nào ngoài tài khoản quản trị — không có gì để reset.' }
  }

  await taoBangLenhNhanVat(db)
  const taiKhoan = new Set<number>()
  await transaction(db, async (conn) => {
    for (const nv of dsNV) {
      await exec(conn,
        'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, admin_user) VALUES (?, ?, ?, ?)',
        [LENH_LOAI_XOA_NHAN_VAT, Number(nv.id), catLenh(String(nv.charname), 50), catLenh(admin.username, 50)])
      taiKhoan.add(Math.trunc(Number(nv.userId)))
    }

    // Đưa tài khoản về CHƯA KÍCH HOẠT (xoá khỏi 5h_active), giống hệt thao tác "Huỷ kích
    // hoạt" ở trang Tài khoản nhưng áp dụng hàng loạt. Không đụng team_user nên tài khoản
    // vẫn đăng nhập web/game bình thường, chỉ phải "Kích hoạt ngay" lại trước khi chơi tiếp.
    const idsTaiKhoan = [...taiKhoan]
    const cho = idsTaiKhoan.map(() => '?').join(',')
    await exec(conn, `DELETE FROM \`5h_active\` WHERE userID IN (${cho})`, idsTaiKhoan)
  })

  await ghiNhatKy(event, 'reset_toanbo', '', `đã xếp hàng đợi xoá ${dsNV.length} Pokémon và huỷ kích hoạt ${taiKhoan.size}`
    + ` tài khoản (loại trừ ${idsAdmin.length} tài khoản quản trị)`)
  return {
    loai: 'success' as const,
    thongBao: `Đã xếp hàng đợi lệnh xoá cho ${dsNV.length} Pokémon và huỷ kích hoạt ${taiKhoan.size}`
      + ' tài khoản (phải "Kích hoạt ngay" lại mới vào game được). Server game sẽ tự xử lý xoá Pokémon dần ở nền'
      + ' (khoảng 1 Pokémon/giây) — tải lại trang này để xem tiến độ.',
  }
})
