/**
 * Gửi lệnh giảm cùng số level cho một hoặc nhiều nhân vật (trang /admin/giam-level).
 * Body: { ten_nhan_vat: "ten1\nten2,ten3", so_cap }.
 *
 * Tìm đủ TẤT CẢ tên trước; thiếu một tên là không gửi lệnh nào (tránh gửi nửa chừng).
 * Mọi lệnh chèn trong MỘT transaction. Server game (LenhWeb) tự xử lý: cấp thấp nhất là 1,
 * exp về 0% cấp mới, không đủ cấp thì báo lỗi riêng cho dòng đó; nhân vật offline được
 * giảm khi vào game.
 */
export default defineAdminHandler(async (event, admin) => {
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))

  // Chỉ nhận số nguyên viết đúng dạng (không nhận "5.0", "5abc"), trong khoảng cho phép.
  const soCapTho = str(body?.so_cap)
  const soCap = /^[+-]?\d+$/.test(soCapTho) ? Number(soCapTho) : Number.NaN
  if (!Number.isInteger(soCap) || soCap < 1 || soCap > CAPDO_TOI_DA) {
    throw apiLoi(400, `Số level phải là số nguyên từ 1 đến ${CAPDO_TOI_DA}.`)
  }

  let names: string[]
  try {
    names = tachTenGiamLevel(typeof body?.ten_nhan_vat === 'string' ? body.ten_nhan_vat.trim() : '')
  } catch (e) {
    if (e instanceof LoiNhapGiamLevel) throw apiLoi(400, e.message)
    throw e
  }

  const db = dbGame()
  await taoBangLenhNhanVat(db)
  const players = new Map<number, { id: number, charname: string }>()
  const missing: string[] = []
  for (const name of names) {
    const player = await q1<{ id: number, charname: string }>(db,
      'SELECT id, charname FROM tob_char WHERE charname = ? AND del = 1 LIMIT 1', [name])
    if (!player) missing.push(name)
    else players.set(Number(player.id), player)
  }
  if (missing.length > 0) throw apiLoi(400, `Chưa gửi lệnh. Không tìm thấy: ${missing.join(', ')}`)

  await transaction(db, async (conn) => {
    for (const player of players.values()) {
      await exec(conn,
        'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, admin_user) VALUES (?, ?, ?, ?, ?)',
        [LENH_LOAI_GIAM_CAP_DO, Number(player.id), player.charname, soCap, admin.username])
    }
  })

  await ghiNhatKy(event, 'giam_level_hang_loat', [...players.values()].map(p => p.charname).join(', '),
    `Gửi giảm ${soCap} level/Pokémon`)

  return {
    loai: 'success' as const,
    thongBao: `Đã gửi ${players.size} lệnh giảm ${soCap} level. Xem kết quả bên dưới; không gửi lại các lệnh đang chờ.`,
  }
})
