/**
 * Dữ liệu cho trang Tài khoản và trang Kích hoạt của người chơi đang đăng nhập.
 * -> { username, email, activated, chars: [{ charname, lastLv, xp, class, dex, xu, khoXu, luong, luongKhoa, khoLuong,
 *        khoLuongKhoa, mapTen, lanCuoi }] }  (xu / lượng: số đã lưu xuống CSDL - đang online thì cập nhật theo chu kỳ lưu)
 * dex: loài Pokémon hiện tại (cột tob_char.dex, cần HTBB/tools/luu_tru_sinh_du_lieu/sql_nguon/them_cot_pokemon.sql).
 * email: email lưu ở team_user (null = chưa có, chưa dùng được Quên mật khẩu).
 *
 * Chỉ đọc theo id/username lấy từ token, không bao giờ từ tham số người gọi gửi lên.
 * Kích hoạt chỉ do admin cấp; người chơi không tự gia hạn được, nên cố ý không có
 * API nào cho việc đó.
 */
export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const activated = await isActivated(u.username)

  // Danh sách nhân vật của tài khoản (phòng thủ nếu lỗi)
  let chars: Array<Record<string, unknown>> = []
  try {
    chars = await q(
      dbGame(),
      'SELECT c.charname, c.lastLv, c.xp, c.class, c.dex, c.gold, c.kho_xu, c.luong, c.luonglock, c.kho_luong, c.kho_luong_khoa,'
      + ' c.lastLog, m.ten AS map_ten FROM tob_char c LEFT JOIN data_map_ten m ON m.map_id = c.map WHERE c.userId = ? ORDER BY c.xp DESC',
      [Math.trunc(u.id)],
    )
  } catch {
    try {
      // CSDL cũ thiếu cột kho / bảng tên bản đồ: vẫn hiện danh sách cơ bản
      chars = await q(dbGame(), 'SELECT charname, lastLv, xp, class, dex FROM tob_char WHERE userId = ? ORDER BY xp DESC', [Math.trunc(u.id)])
    } catch {
      chars = []
    }
  }
  const so = (v: unknown) => Math.max(0, Math.trunc(Number(v) || 0))

  // Email của tài khoản (phòng thủ nếu lỗi: coi như chưa có)
  let email: string | null = null
  try {
    const v = await qv<string | null>(dbAccount(), 'SELECT email FROM team_user WHERE id = ? LIMIT 1', [Math.trunc(u.id)])
    email = typeof v === 'string' && v.trim() !== '' ? v.trim() : null
  } catch {
    email = null
  }

  return {
    username: u.username,
    email,
    activated,
    chars: chars.map(c => ({
      charname: String(c.charname),
      lastLv: Math.trunc(Number(c.lastLv) || 0),
      xp: Number(c.xp) || 0,
      class: Math.trunc(Number(c.class)),
      dex: dexHienTai(c.class as number, c.lastLv as number, c.dex as number),
      xu: so(c.gold),
      khoXu: so(c.kho_xu),
      luong: so(c.luong),
      luongKhoa: so(c.luonglock),
      khoLuong: so(c.kho_luong),
      khoLuongKhoa: so(c.kho_luong_khoa),
      mapTen: typeof c.map_ten === 'string' && c.map_ten ? c.map_ten : null,
      lanCuoi: c.lastLog instanceof Date ? gioDb(c.lastLog) : (typeof c.lastLog === 'string' ? c.lastLog : null),
    })),
  }
})
