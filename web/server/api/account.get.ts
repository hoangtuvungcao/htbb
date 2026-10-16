/**
 * Dữ liệu cho trang Tài khoản và trang Kích hoạt của người chơi đang đăng nhập.
 * -> { username, email, activated, chars: [{ charname, lastLv, xp, class, dex }] }
 * dex: loài Pokémon hiện tại (cột tob_char.dex, cần KPAH/sql/them_cot_pokemon.sql).
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
  let chars: Array<{ charname: string, lastLv: number, xp: number, class: number, dex: number }> = []
  try {
    chars = await q(
      dbGame(),
      'SELECT charname, lastLv, xp, class, dex FROM tob_char WHERE userId = ? ORDER BY xp DESC',
      [Math.trunc(u.id)],
    )
  } catch {
    chars = []
  }

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
      dex: dexHienTai(c.class, c.lastLv, c.dex),
    })),
  }
})
