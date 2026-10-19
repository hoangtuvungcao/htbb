/**
 * Phần dùng chung của các API trang Nhân vật (/api/admin/nhanvat/...).
 *
 * KHOÁ NHÂN VẬT là đặt tob_char.ban = 1 — đúng cột mà bảng điều khiển trong game
 * dùng (AdminPanel.handleBanAccount). Hệ quả trong game:
 *   · Nhân vật BIẾN MẤT khỏi màn hình chọn nhân vật (câu lấy danh sách lọc
 *     `AND ban=0 AND del=1`), người chơi sẽ tưởng bị xoá tài khoản.
 *   · Bị loại khỏi mọi bảng xếp hạng (các câu top đều có `and ban=0`).
 *   · Tài khoản VẪN đăng nhập được, chỉ là không còn nhân vật để vào.
 * Muốn chặn cả tài khoản thì khoá ở trang Tài khoản (team_user.ban).
 *
 * ĐẶT CẤP ĐỘ — ĐI QUA SERVER GAME, KHÔNG GHI THẲNG tob_char.
 * Khoá nhân vật AN TOÀN khi người chơi đang online vì cột `ban` không nằm trong câu
 * UPDATE lưu nhân vật định kỳ (DatabaseNhanVat.java). `lastLv` và `xp` THÌ KHÁC: hai
 * cột này nằm ngay trong câu lưu định kỳ đó — UPDATE thẳng chúng thì với nhân vật đang
 * online, dữ liệu trong RAM bị lưu đè lại ngay lần tự lưu kế tiếp, không một cảnh báo
 * nào (đã từng xảy ra). Vì vậy web chỉ CHÈN MỘT DÒNG LỆNH vào web_lenh_nhan_vat
 * (server/utils/lenhNhanVat.ts + src/data/LenhWeb.java); server tự biết online/offline.
 */
import { q1 } from './db'

/** Cột của danh sách và trang chi tiết. KHÔNG gồm equip/inven/bag (TEXT rất dài). */
export const NV_COT = `id, charname, userId, lastLv, \`class\`, dex, xp, gold, luong, luonglock, ban, del,
        lastLog, idserver, idClan, nhack, blacklist, tichluy, topNap`

export interface NhanVatDong {
  id: number
  charname: string
  userId: number
  lastLv: number
  class: number
  /** Loài Pokémon (0 = suy ra từ class + cấp). Cột thêm bởi HTBB/tools/luu_tru_sinh_du_lieu/sql_nguon/them_cot_pokemon.sql. */
  dex: number
  xp: number | string
  gold: number
  luong: number
  luonglock: number
  ban: number
  del: number
  lastLog: string
  idserver: number
  idClan: number
  nhack: number
  blacklist: number
  tichluy: number
  topNap: number
}

/** Tên tài khoản sở hữu một nhân vật, để không lỡ tay khoá/xoá nhân vật của admin. */
export async function nvChuNhan(userId: number): Promise<string> {
  try {
    const r = await q1<{ username: string }>(dbAccount(), 'SELECT username FROM team_user WHERE id = ? LIMIT 1', [userId])
    return String(r?.username ?? '')
  } catch {
    return ''
  }
}

/** Đọc nhân vật (id, charname, userId, ban) cho các thao tác. Không có -> lỗi 404. */
export async function nvLayDeThaoTac(id: number): Promise<{ id: number, charname: string, userId: number, ban: number }> {
  const nv = await q1<{ id: number, charname: string, userId: number, ban: number }>(
    dbGame(), 'SELECT id, charname, userId, ban FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!nv) throw apiLoi(404, `Không tìm thấy Pokémon #${id}.`)
  return nv
}

/** id nhân vật lấy từ route /api/admin/nhanvat/<id>/... */
export function nvIdTuRoute(event: import('h3').H3Event): number {
  const id = int(getRouterParam(event, 'id'))
  if (id <= 0) throw apiLoi(404, `Không tìm thấy Pokémon #${id}.`)
  return id
}
