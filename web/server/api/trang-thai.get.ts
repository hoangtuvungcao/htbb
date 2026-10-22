/**
 * Trạng thái máy chủ công khai cho trang chủ: số người online + máy chủ còn sống không.
 *
 * Ưu tiên nhịp tim `tob_chi_so_may_chu` (game ghi mỗi 30 giây, kể cả khi 0 người online);
 * dự phòng: bảng `tob_vi_tri_online` (game server xoá & ghi lại mỗi 30 giây — xem
 * server/api/admin/online.get.ts). Bảng được ghi trong 2 phút gần đây => máy chủ đang chạy.
 * Không đọc được CSDL => `song: null` (website vẫn hiện "chưa rõ" thay vì lỗi).
 * Kết quả giữ 30 giây trong bộ nhớ để không truy vấn CSDL mỗi lượt xem trang.
 */
interface TrangThai { song: boolean | null, online: number | null, capNhat: string | null }

let cache: { luc: number, kq: TrangThai } | null = null

/** Trang công khai chỉ cần Online / Bảo trì + số người: không trả thời điểm nhịp tim nội bộ. */
export default defineEventHandler(async (event): Promise<{ song: boolean | null, online: number | null }> => {
  setHeader(event, 'Cache-Control', 'public, max-age=30')
  const { song, online } = await layTrangThai()
  return { song, online }
})

async function layTrangThai(): Promise<TrangThai> {
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
  // Nhịp tim tob_chi_so_may_chu (server ghi mỗi 30 giây kể cả khi không ai online).
  // Tuổi mẫu tính trong CSDL để không lệch múi giờ giữa MariaDB và Node.
  try {
    if (await coBang(dbGame(), 'tob_chi_so_may_chu')) {
      const r = await q1<{ cach: number | null, nv: number, luc: string | Date | null }>(
        dbGame(),
        'SELECT UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(thoi_gian) AS cach, nhan_vat AS nv, thoi_gian AS luc FROM `tob_chi_so_may_chu` ORDER BY id DESC LIMIT 1',
      )
      if (r && r.cach != null) {
        const song = Number(r.cach) < 120
        kq = { song, online: song ? Math.max(0, Number(r.nv ?? 0)) : 0, capNhat: r.luc ? new Date(Date.now() - Number(r.cach) * 1000).toISOString() : null }
        cache = { luc: Date.now(), kq }
        return kq
      }
    }
  }
  catch { /* thử nguồn cũ bên dưới */ }
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
}
