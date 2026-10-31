/**
 * DANH SÁCH MÁY CHỦ cho client game (GET /server.txt và link danh sách cũ của client đã phát hành,
 * kể cả tiền tố /kpah — phục vụ ở server/middleware/duong-dan-cu.ts, xem duongDanCu.ts).
 * File THUẦN (không import) để npm test kiểm trực tiếp.
 *
 * Client Unity (ServerListScr.linkGetHost = "http://" + SERVER_HOST + "/server.txt") tải tệp
 * này lúc mở game. Trước 19/10/2026 nó là tệp tĩnh HTBB/web/server.txt của website PHP cũ
 * (đã bỏ — docs/kien-truc.md); nay Nuxt phục vụ từ biến môi trường, để không
 * ghi cứng IP/tên miền nào trong mã nguồn.
 *
 *   HTBB_SERVER_LIST (dự phòng: KPAH_SERVER_LIST) — mỗi máy chủ một dòng "Tên:host:cổng:loại",  // TUONG-THICH-TEN-CU
 *   vd. "Server 1:203.0.113.10:19129:0". Nhiều máy chủ: ngăn bằng chuỗi "\n" (hai ký tự,
 *   vì tệp env / GitHub vars không chứa được xuống dòng thật) hoặc xuống dòng thật.
 *
 * Dòng sai định dạng bị BỎ (không gửi rác cho client — client tách bằng ":" và đọc thẳng
 * cổng là số). Không còn dòng nào hợp lệ -> null (trả 503).
 *
 *   HTBB_SERVER_LIST_CU — riêng cho link danh sách cũ của client đã phát hành (duongDanCu.ts),
 *   nếu cần khác /server.txt. Không đặt thì dùng chung HTBB_SERVER_LIST. Tên biến cũ vẫn đọc
 *   (dự phòng) ở bienDanhSachClientCu.
 */
const DONG_HOP_LE = /^[^:\r\n]+:[A-Za-z0-9.\-]+:\d{1,5}:\d+$/

export function docDanhSachMayChu(tho: string | undefined | null): string | null {
  if (tho === undefined || tho === null) return null
  const dong = String(tho)
    .split(/\\n|\r?\n/)
    .map(s => s.trim())
    .filter(s => DONG_HOP_LE.test(s) && Number(s.split(':')[2]) <= 65535)
  return dong.length > 0 ? dong.join('\n') + '\n' : null
}

/** Giá trị biến môi trường: HTBB_SERVER_LIST trước, KPAH_SERVER_LIST sau (giai đoạn đổi tên). */  // TUONG-THICH-TEN-CU
export function bienDanhSachMayChu(env: Record<string, string | undefined>): string | undefined {
  const moi = env.HTBB_SERVER_LIST
  if (moi !== undefined && moi.trim() !== '') return moi
  return env.KPAH_SERVER_LIST  // TUONG-THICH-TEN-CU
}

/** Link danh sách cũ (client cũ): HTBB_SERVER_LIST_CU nếu có, không thì như /server.txt. */
export function bienDanhSachClientCu(env: Record<string, string | undefined>): string | undefined {
  // TUONG-THICH-TEN-CU: hai tên biến sau là tên cũ trong file env / GitHub vars đã đặt trên VPS - vẫn đọc dự phòng.
  for (const k of ['HTBB_SERVER_LIST_CU', 'HTBB_NQSH_LIST', 'KPAH_NQSH_LIST']) {
    const v = env[k]
    if (v !== undefined && v.trim() !== '') return v
  }
  return bienDanhSachMayChu(env)
}
