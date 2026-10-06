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

export function requireUser(event: H3Event): WebUser {
  const u = currentUser(event)
  if (!u) throw apiLoi(401, 'Bạn cần đăng nhập để tiếp tục.', { code: 'not_logged_in' })
  return u
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
