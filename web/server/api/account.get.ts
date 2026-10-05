/**
 * Dữ liệu cho trang Tài khoản và trang Kích hoạt của người chơi đang đăng nhập.
 * -> { username, activated, chars: [{ charname, lastLv, xp, class }] }
 *
 * Chỉ đọc theo id/username lấy từ token, không bao giờ từ tham số người gọi gửi lên.
 * Kích hoạt chỉ do admin cấp; người chơi không tự gia hạn được, nên cố ý không có
 * API nào cho việc đó.
 */
export default defineEventHandler(async (event) => {
  const u = requireUser(event)
  const activated = await isActivated(u.username)

  // Danh sách nhân vật của tài khoản (phòng thủ nếu lỗi)
  let chars: Array<{ charname: string, lastLv: number, xp: number, class: number }> = []
  try {
    chars = await q(
      dbGame(),
      'SELECT charname, lastLv, xp, class FROM tob_char WHERE userId = ? ORDER BY xp DESC',
      [Math.trunc(u.id)],
    )
  } catch {
    chars = []
  }

  return {
    username: u.username,
    activated,
    chars: chars.map(c => ({
      charname: String(c.charname),
      lastLv: Math.trunc(Number(c.lastLv) || 0),
      xp: Number(c.xp) || 0,
      class: Math.trunc(Number(c.class)),
    })),
  }
})
