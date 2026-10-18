/**
 * Trung tâm người chơi: tra cứu nhanh, khoá có thời hạn, cấm chat, gửi thư, đưa về điểm an toàn,
 * tặng quà qua giftcode cá nhân — CHỈ dùng cơ chế game đã có:
 *   - Khoá tài khoản = account.team_user.ban (login server đọc). Thời hạn + lý do lưu thêm ở
 *     htbb_db.web_khoa_tai_khoan; plugin server/plugins/mo-khoa-het-han.ts tự mở khi hết hạn.
 *   - Cấm chat = trường thứ 92 (dayCamChat, 'yyyy-MM-dd HH:mm:ss') trong tob_char.pInfo — chính
 *     là trường lệnh GM "camchat" trong game ghi (MapChat.java). Nhân vật ĐANG ONLINE giữ bản
 *     trong RAM và sẽ ghi đè khi lưu, nên web chỉ cho đặt khi nhân vật offline.
 *   - Thư = một dòng tob_inbox (server đọc hộp thư lúc nhân vật vào game: MessageCreator getInbox).
 *   - Điểm an toàn = pInfo[8] (mapID) = 0 -> game tự đưa về thị trấn khởi đầu (hoSoNguoiChoi.ts).
 *   - Không có lệnh "đá khỏi game" / dịch chuyển trong LenhWeb.java -> web KHÔNG làm (không bịa cơ chế).
 * Phần thuần (giải mã hồ sơ, quyết định online...) ở server/utils/hoSoNguoiChoi.ts.
 */
import { hsQuyetDinhOnline, type HsTinHieuOnline } from './hoSoNguoiChoi'

export const PINFO_CAM_CHAT = 92

/** Tín hiệu online toàn máy chủ (một lần đọc, dùng cho nhiều nhân vật). */
export async function ncTinHieuOnline(): Promise<{ tinHieu: Omit<HsTinHieuOnline, 'coTrongViTri'>, viTri: Map<string, { map_id: number, map_ten: string, khu: number, x: number, y: number }> }> {
  const tinHieu: Omit<HsTinHieuOnline, 'coTrongViTri'> = { giayNhipTim: null, soOnlineNhipTim: null, giayViTri: null }
  const viTri = new Map<string, { map_id: number, map_ten: string, khu: number, x: number, y: number }>()
  try {
    if (await coBang(dbGame(), 'tob_chi_so_may_chu')) {
      const r = await q1<{ g: number | null, nhan_vat: number }>(dbGame(),
        'SELECT UNIX_TIMESTAMP() - UNIX_TIMESTAMP(thoi_gian) AS g, nhan_vat FROM tob_chi_so_may_chu ORDER BY id DESC LIMIT 1')
      if (r && r.g !== null && Number.isFinite(Number(r.g))) {
        tinHieu.giayNhipTim = Math.max(0, Number(r.g))
        tinHieu.soOnlineNhipTim = Number(r.nhan_vat)
      }
    }
  }
  catch { /* coi như không có nhịp tim */ }
  try {
    if (await coBang(dbGame(), 'tob_vi_tri_online')) {
      const rows = await q<{ charname: string, map_id: number, map_ten: string, khu: number, x: number, y: number, g: number | null }>(dbGame(),
        'SELECT charname, map_id, map_ten, khu, x, y, UNIX_TIMESTAMP() - UNIX_TIMESTAMP(cap_nhat_luc) AS g FROM tob_vi_tri_online LIMIT 5000')
      let moiNhat: number | null = null
      for (const r of rows) {
        viTri.set(String(r.charname), { map_id: Number(r.map_id), map_ten: String(r.map_ten ?? ''), khu: Number(r.khu), x: Number(r.x), y: Number(r.y) })
        const g = Number(r.g)
        if (Number.isFinite(g)) moiNhat = moiNhat === null ? g : Math.min(moiNhat, g)
      }
      tinHieu.giayViTri = moiNhat === null ? null : Math.max(0, moiNhat)
    }
  }
  catch { /* bảng lỗi: coi như không có */ }
  return { tinHieu, viTri }
}

/** Trạng thái online của MỘT nhân vật kèm lý do. */
export async function ncOnline(charname: string): Promise<{ online: boolean | null, lyDo: string }> {
  const { tinHieu, viTri } = await ncTinHieuOnline()
  return hsQuyetDinhOnline({ ...tinHieu, coTrongViTri: viTri.has(charname) })
}

/** Nhân vật đang online? true / false / null (không chắc). */
export async function dangOnline(charname: string): Promise<boolean | null> {
  return (await ncOnline(charname)).online
}

/** Ném 409 nếu nhân vật không CHẮC CHẮN đang offline (thao tác ghi thẳng tob_char). */
export async function ncCanOffline(charname: string, viec: string): Promise<string> {
  const t = await ncOnline(charname)
  if (t.online === true) throw apiLoi(409, `${charname} đang online — game giữ dữ liệu trong RAM và sẽ ghi đè thay đổi. Thử lại khi người chơi đã thoát (${viec}).`)
  if (t.online === null) throw apiLoi(409, `Không xác định được ${charname} có đang online không (${t.lyDo}). Để an toàn, chưa ${viec}; thử lại sau ít phút.`)
  return t.lyDo
}

export function docCamChat(pInfo: string | null | undefined): string {
  const f = String(pInfo ?? '').split(',')
  return (f[PINFO_CAM_CHAT] ?? '').trim()
}

/** Ghi trường cấm chat vào pInfo; trả pInfo mới, hoặc null nếu pInfo không đủ trường. */
export function datCamChat(pInfo: string, giaTri: string): string | null {
  const f = pInfo.split(',')
  if (f.length <= PINFO_CAM_CHAT) return null
  f[PINFO_CAM_CHAT] = giaTri
  return f.join(',')
}

export function gioSau(gio: number): string {
  const d = new Date(Date.now() + gio * 3600_000)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/** Tên tài khoản để tra (team_user.username varchar(20); tài khoản cũ có thể có ký tự ngoài luật đăng ký mới). */
export function ncTenTaiKhoan(v: unknown): string {
  const u = (typeof v === 'string' ? v : '').trim().toLowerCase()
  if (!/^[\p{L}\p{N}_.@-]{1,30}$/u.test(u)) throw apiLoi(400, 'Tên tài khoản không hợp lệ.')
  return u
}

/** id nhân vật (số nguyên dương) từ body/query. */
export function ncIdNhanVat(v: unknown): number {
  const id = int(v)
  if (!Number.isSafeInteger(id) || id <= 0) throw apiLoi(400, 'Mã Pokémon không hợp lệ.')
  return id
}

/** Đọc bảng có thể chưa tồn tại: lỗi -> []. */
export async function ncDocAnToan<T = Record<string, any>>(bang: string, sql: string, thamSo: unknown[] = []): Promise<T[] | null> {
  try {
    if (!(await coBang(dbGame(), bang))) return null
    return await q<T>(dbGame(), sql, thamSo) as T[]
  }
  catch (e: any) {
    console.error('[nguoi-choi]', bang, e?.code ?? e?.message ?? e)
    return null
  }
}

/** Tài khoản có quyền quản trị web (HTBB_ADMIN_ACCOUNTS hoặc có vai trò trong web_admin_vai_tro) — không khoá/cấm/tặng quà. */
export async function ncLaQuanTri(username: string): Promise<boolean> {
  if (!username) return false
  if (laTaiKhoanAdmin(username)) return true
  try { return (await vaiTroCua(username)) !== null }
  catch { return false }
}
