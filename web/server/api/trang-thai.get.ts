/**
 * Trạng thái máy chủ công khai cho trang chủ: số người online + máy chủ còn sống không.
 *
 * Đọc bảng `tob_vi_tri_online` (game server xoá & ghi lại mỗi 30 giây — xem
 * server/api/admin/online.get.ts). Bảng được ghi trong 2 phút gần đây => máy chủ đang chạy.
 * Không đọc được CSDL => `song: null` (website vẫn hiện "chưa rõ" thay vì lỗi).
 * Kết quả giữ 30 giây trong bộ nhớ để không truy vấn CSDL mỗi lượt xem trang.
 */
interface TrangThai { song: boolean | null, online: number | null, capNhat: string | null }

let cache: { luc: number, kq: TrangThai } | null = null

export default defineEventHandler(async (): Promise<TrangThai> => {
  if (cache && Date.now() - cache.luc < 30_000) return cache.kq
  let kq: TrangThai = { song: null, online: null, capNhat: null }
  // Nguồn ngoài (cài ở /admin/cai-dat): chỉ lấy hai trường song/online.
  const url = (await cmsCongKhai()).site.trangThaiUrl
  if (/^https?:\/\//i.test(url)) {
    try {
      const r = await $fetch<{ song?: unknown, online?: unknown }>(url, { timeout: 3000 })
      kq = { song: typeof r?.song === 'boolean' ? r.song : null, online: Number.isFinite(Number(r?.online)) ? Math.max(0, Math.trunc(Number(r?.online))) : null, capNhat: null }
    }
    catch { /* giữ "chưa rõ" */ }
    cache = { luc: Date.now(), kq }
    return kq
  }
  try {
    const r = await q1<{ n: number, moi: string | Date | null }>(
      dbGame(),
      'SELECT COUNT(*) AS n, MAX(cap_nhat_luc) AS moi FROM `tob_vi_tri_online`',
    )
    const moi = r?.moi ? new Date(r.moi) : null
    const song = moi ? Date.now() - moi.getTime() < 120_000 : false
    kq = { song, online: song ? Number(r?.n ?? 0) : 0, capNhat: moi ? moi.toISOString() : null }
  }
  catch {
    // CSDL chưa cấu hình / bảng chưa có: giữ "chưa rõ"
  }
  cache = { luc: Date.now(), kq }
  return kq
})
