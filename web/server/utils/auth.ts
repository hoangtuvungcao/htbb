/**
 * Đăng nhập người chơi trên website: token 'user' trong header Authorization.
 */
import type { H3Event } from 'h3'

/**
 * Phiên người chơi sống 7 ngày kể từ lúc đăng nhập (hạn cứng, không gia hạn trượt):
 * người chơi không phải đăng nhập lại mỗi lần mở trình duyệt, nhưng token lộ ra
 * ngoài cũng không dùng được mãi.
 */
export const USER_TOKEN_HAN = 7 * 86400

export interface WebUser {
  id: number
  username: string
}

export function currentUser(event: H3Event): WebUser | null {
  const p = docToken(bearer(event), 'user')
  return p ? { id: p.id, username: p.u } : null
}

/** Token người chơi kèm dấu mật khẩu hiện tại (đăng nhập / đổi mật khẩu cấp lại). */
export function taoTokenNguoiChoi(id: number, username: string, hashMatKhau: unknown): string {
  return taoToken('user', id, username, USER_TOKEN_HAN, { pw: dauMatKhau(hashMatKhau) })
}

/**
 * Bắt buộc đăng nhập. Ngoài chữ ký + hạn, kiểm thêm trong DB: tài khoản bị khoá (ban) hoặc đã đổi / quên
 * mật khẩu (dấu mật khẩu khác) -> phiên hết hiệu lực ngay. Trước đây token sống đủ 7 ngày dù đã đổi mật khẩu
 * hay bị khoá (kẻ lấy được token vẫn vào được sau khi chủ tài khoản đổi mật khẩu).
 */
export async function requireUser(event: H3Event): Promise<WebUser> {
  const p = docToken(bearer(event), 'user')
  if (!p) throw apiLoi(401, 'Bạn cần đăng nhập để tiếp tục.', { code: 'not_logged_in' })
  let row: { ban: number, password: string } | null
  try {
    row = await q1(dbAccount(), 'SELECT ban, `password` FROM team_user WHERE id = ? LIMIT 1', [Math.trunc(p.id)])
  } catch (e: any) {
    console.error('requireUser db:', e?.code ?? e)
    throw apiLoi(503, 'Chưa kiểm tra được phiên đăng nhập. Vui lòng thử lại sau ít phút.')
  }
  if (!row || Number(row.ban) === 1 || p.pw !== dauMatKhau(row.password)) {
    throw apiLoi(401, 'Phiên đăng nhập đã hết hiệu lực. Hãy đăng nhập lại.', { code: 'not_logged_in' })
  }
  return { id: p.id, username: p.u }
}

/** Tài khoản còn hạn kích hoạt trong 5h_active hay không. */
export async function isActivated(username: string): Promise<boolean> {
  try {
    const v = await qv(
      dbGame(),
      `SELECT 1 FROM \`5h_active\`
        WHERE username = ? AND time_end <> '-1'
          AND CAST(time_end AS UNSIGNED) > UNIX_TIMESTAMP()
        LIMIT 1`,
      [username],
    )
    return v !== null
  } catch {
    return false
  }
}

/** Gia hạn tài khoản 30 ngày; nếu còn hạn thì cộng nối tiếp từ hạn cũ. */
export async function activateAccount(userId: number, username: string): Promise<boolean> {
  try {
    await transaction(dbGame(), async (conn) => {
      const cu = Number(await qv(
        conn,
        `SELECT MAX(CASE WHEN time_end <> '-1' THEN CAST(time_end AS UNSIGNED) ELSE 0 END)
           FROM \`5h_active\` WHERE userID = ? OR username = ? FOR UPDATE`,
        [userId, username],
      ) ?? 0)
      const batDau = Math.max(nowSec(), cu)
      const timeEnd = batDau + 30 * 86400
      await exec(conn, 'DELETE FROM `5h_active` WHERE userID = ? OR username = ?', [userId, username])
      await exec(conn, 'INSERT INTO `5h_active` (userID, username, time_end) VALUES (?, ?, ?)', [userId, username, String(timeEnd)])
    })
    return true
  } catch {
    return false
  }
}
