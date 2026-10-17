/**
 * Bảng xếp hạng công khai — các câu SQL và mốc đợt đua top.
 */

/** BXH cấp độ (theo kinh nghiệm). */
// Cột `dex` (loài Pokémon hiện tại) thêm bởi HTBB/sql/them_cot_pokemon.sql — PHẢI chạy
// migration đó trước khi deploy nhánh pokemon/chuyen-doi, không thì bảng này báo lỗi.
export const XEP_HANG_CAP_DO_SQL = 'SELECT charname, lastLv, class, dex FROM tob_char ORDER BY xp DESC, lastLv DESC LIMIT 10'

/** BXH nạp/ủng hộ (board_created). */
export const XEP_HANG_UNG_HO_SQL = 'SELECT username, luong FROM board_created ORDER BY luong DESC LIMIT 10'

/**
 * Mốc khởi đầu CỐ ĐỊNH của đợt đua top mới, cùng giờ với timeLog của server.
 * Không dùng ngày hiện tại động: điểm phải tiếp tục cộng dồn vào các ngày sau.
 * Reset 12/09/2026 theo yêu cầu người dùng: đợt đua trước (từ 08/09) bị xoá, đợt
 * mới tính từ mốc giờ này trở đi.
 */
export const XEP_HANG_THE_BAT_DAU = '2026-09-12 20:12:29'
export const XEP_HANG_THE_BAT_DAU_HIEN = 'Chiếm thành · Tính từ 20:12 ngày 12/09/2026.'

export type XepHangTheAction = 'gcok' | 'pcok'

export const XEP_HANG_THE: Array<{ title: string, action: XepHangTheAction }> = [
  { title: 'Top Giao Thẻ', action: 'gcok' },
  { title: 'Top Phá Thẻ', action: 'pcok' },
]

/**
 * gcok lưu charname kèm "_idClan"; pcok lưu tên người phá thẻ.
 * Biểu thức là HẰNG SỐ chọn theo action trong mã nguồn, không bao giờ lấy từ người dùng.
 */
export function xepHangTheTenExpr(action: XepHangTheAction): string {
  return action === 'gcok'
    ? 'LEFT(charname, CHAR_LENGTH(charname) - CHAR_LENGTH(SUBSTRING_INDEX(charname, \'_\', -1)) - 1)'
    : 'charname'
}

/** Cùng số lần: ai đạt mốc sớm hơn đứng trước; cùng giây thì theo ID log. */
export function xepHangTheSql(action: XepHangTheAction): string {
  return `SELECT ${xepHangTheTenExpr(action)} AS player_name, COUNT(*) AS total
             FROM tob_log_clan WHERE actioninfo = ? AND timeLog >= ?
             GROUP BY player_name ORDER BY total DESC, MAX(timeLog) ASC, MAX(id) ASC, player_name ASC LIMIT 10`
}

/** Che tên đăng nhập khi không tìm được nhân vật: "hoahv" -> "ho***". */
export function xepHangCheTen(username: string): string {
  return `${username.slice(0, 2)}***`
}

/**
 * Tên đăng nhập -> tên nhân vật nhiều kinh nghiệm nhất của tài khoản đó.
 * Hai bước vì team_user nằm ở DB account, tob_char ở DB game.
 * Khoá của Map là tên đăng nhập viết thường.
 */
export async function xepHangTenNhanVat(usernames: string[]): Promise<Map<string, string>> {
  const ra = new Map<string, string>()
  const ds = [...new Set(usernames.map(u => u.toLowerCase()).filter(Boolean))]
  if (ds.length === 0) return ra
  const tk = await q<{ id: number, username: string }>(
    dbAccount(),
    `SELECT id, username FROM team_user WHERE username IN (${ds.map(() => '?').join(',')})`,
    ds,
  )
  if (tk.length === 0) return ra
  const ids = tk.map(t => Number(t.id))
  const nv = await q<{ userId: number, charname: string }>(
    dbGame(),
    `SELECT userId, charname FROM tob_char WHERE userId IN (${ids.map(() => '?').join(',')}) ORDER BY xp DESC`,
    ids,
  )
  const tenTheoId = new Map<number, string>()
  for (const n of nv) {
    if (!tenTheoId.has(Number(n.userId))) tenTheoId.set(Number(n.userId), String(n.charname))
  }
  for (const t of tk) {
    const ten = tenTheoId.get(Number(t.id))
    if (ten) ra.set(String(t.username).toLowerCase(), ten)
  }
  return ra
}
