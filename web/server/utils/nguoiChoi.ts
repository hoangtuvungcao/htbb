/**
 * Trung tâm người chơi: tra cứu nhanh, khoá có thời hạn, cấm chat, gửi thư — CHỈ dùng cơ chế
 * game đã có:
 *   - Khoá tài khoản = account.team_user.ban (login server đọc). Thời hạn + lý do lưu thêm ở
 *     kpah_db.web_khoa_tai_khoan; plugin server/plugins/mo-khoa-het-han.ts tự mở khi hết hạn.
 *   - Cấm chat = trường thứ 92 (dayCamChat, 'yyyy-MM-dd HH:mm:ss') trong tob_char.pInfo — chính
 *     là trường lệnh GM "camchat" trong game ghi (MapChat.java). Nhân vật ĐANG ONLINE giữ bản
 *     trong RAM và sẽ ghi đè khi lưu, nên web chỉ cho đặt khi nhân vật offline.
 *   - Thư = một dòng tob_inbox (server đọc hộp thư lúc nhân vật vào game: MessageCreator getInbox).
 */
export const PINFO_CAM_CHAT = 92

/** Nhân vật đang online? (bảng tob_vi_tri_online được game ghi lại mỗi 30 giây) */
export async function dangOnline(charname: string): Promise<boolean | null> {
  try {
    if (!(await coBang(dbGame(), 'tob_vi_tri_online'))) return null
    const r = await q1<{ moi: string | null }>(dbGame(), 'SELECT MAX(cap_nhat_luc) AS moi FROM tob_vi_tri_online')
    const moi = r?.moi ? new Date(String(r.moi).replace(' ', 'T')) : null
    if (!moi || Date.now() - moi.getTime() > 120_000) return null
    return !!(await qv(dbGame(), 'SELECT 1 FROM tob_vi_tri_online WHERE charname = ? LIMIT 1', [charname]))
  }
  catch { return null }
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
