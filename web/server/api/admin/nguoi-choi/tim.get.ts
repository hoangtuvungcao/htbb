/** Tìm tài khoản + nhân vật theo tên (gần đúng). ?q= */
export default defineAdminHandler(async (event) => {
  const tu = str(getQuery(event).q).toLowerCase()
  if (tu.length < 2) return { taiKhoan: [], nhanVat: [] }
  const like = `%${tu.replace(/[!%_]/g, c => `!${c}`)}%`
  const taiKhoan = await q(dbAccount(), "SELECT id, username, email, regdate, ban FROM team_user WHERE username LIKE ? ESCAPE '!' ORDER BY id DESC LIMIT 30", [like])
  const nhanVat = await q(dbGame(), "SELECT id, charname, userId, lastLv, class, dex, ban, lastLog FROM tob_char WHERE charname LIKE ? ESCAPE '!' ORDER BY lastLv DESC LIMIT 30", [like])
  const ids = [...new Set(nhanVat.map(n => Number(n.userId)))]
  const chu = ids.length ? await q<{ id: number, username: string }>(dbAccount(), `SELECT id, username FROM team_user WHERE id IN (${ids.map(() => '?').join(',')})`, ids) : []
  const tenChu = new Map(chu.map(c => [Number(c.id), c.username]))
  return { taiKhoan, nhanVat: nhanVat.map(n => ({ ...n, chu: tenChu.get(Number(n.userId)) ?? '' })) }
})
