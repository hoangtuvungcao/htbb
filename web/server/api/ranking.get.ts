/**
 * Bảng vàng anh hùng. Công khai, không cần đăng nhập.
 *
 * Mỗi bảng tự báo lỗi riêng (`error: true`) để một bảng hỏng không kéo sập cả
 * trang. Không bao giờ đưa chi tiết lỗi DB ra ngoài. Chỉ trả đúng các cột hiển thị
 * (tên nhân vật, cấp, môn phái, số lượng) — không id, tên đăng nhập, email hay IP.
 */
import type { Pool } from 'mysql2/promise'

// Cache 2 phút (stale-while-revalidate): trang công khai, không đăng nhập — trước đây mỗi lượt xem chạy lại
// toàn bộ truy vấn xếp hạng (có GROUP BY cả bảng tob_log_clan), ai cũng dùng được để làm nặng CSDL.
export default defineCachedEventHandler(async () => {
  // Kết nối một lần cho mỗi request; không thử lại DB đang hỏng cho từng bảng.
  let rankDb: Pool | null = null
  try {
    rankDb = dbGame()
    await rankDb.query('SELECT 1')
  } catch (e: any) {
    console.error('Ranking database unavailable:', e?.code ?? e)
    rankDb = null
  }

  // BXH cấp độ (theo kinh nghiệm)
  let topLevel: Array<{ charname: string, lastLv: number, class: number }> = []
  let topLevelError = !rankDb
  if (rankDb) {
    try {
      const rows = await q(rankDb, XEP_HANG_CAP_DO_SQL)
      topLevel = rows.map(r => ({ charname: String(r.charname), lastLv: Math.trunc(Number(r.lastLv) || 0), class: Math.trunc(Number(r.class)) }))
    } catch { topLevelError = true }
  }

  // BXH nạp/ủng hộ (board_created). board_created chỉ có TÊN ĐĂNG NHẬP — không
  // bao giờ đưa tên đăng nhập ra trang công khai (lộ ra là nửa đường để dò mật
  // khẩu). Hiện tên nhân vật cấp cao nhất của tài khoản đó; chưa có nhân vật thì
  // che bớt tên.
  let topNap: Array<{ name: string, luong: number }> = []
  let topNapError = !rankDb
  if (rankDb) {
    try {
      const rows = await q(rankDb, XEP_HANG_UNG_HO_SQL)
      const tenHien = await xepHangTenNhanVat(rows.map(r => String(r.username)))
      topNap = rows.map(r => ({
        name: tenHien.get(String(r.username).toLowerCase()) ?? xepHangCheTen(String(r.username)),
        luong: Math.trunc(Number(r.luong) || 0),
      }))
    } catch { topNapError = true }
  }

  const cardRanks = []
  for (const r of XEP_HANG_THE) {
    const rank = { title: r.title, action: r.action, rows: [] as Array<{ player_name: string, total: number }>, error: false }
    if (!rankDb) {
      rank.error = true
    } else {
      try {
        const rows = await q(rankDb, xepHangTheSql(r.action), [r.action, XEP_HANG_THE_BAT_DAU])
        rank.rows = rows.map(x => ({ player_name: String(x.player_name ?? ''), total: Math.trunc(Number(x.total) || 0) }))
      } catch (e: any) {
        rank.error = true
        console.error(`Card ranking ${r.action}:`, e?.message ?? e)
      }
    }
    cardRanks.push(rank)
  }

  return {
    topLevel,
    topLevelError,
    topNap,
    topNapError,
    cardRanks,
    cardRankNote: XEP_HANG_THE_BAT_DAU_HIEN,
  }
}, { name: 'xep-hang', maxAge: 120, swr: true })
