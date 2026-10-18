/**
 * Chỉ số máy chủ game cho bảng điều khiển quản trị (sẵn sàng production, 17/10/2026).
 *
 * Đọc bảng `tob_chi_so_may_chu` do server tự ghi mỗi 30 giây (HTBB/src/server/ChiSoMayChu.java,
 * giữ 7 ngày): online, heap, thread, CPU tiến trình, tải máy, và 4 bộ đếm là SỐ TĂNG TRONG 30 GIÂY
 * (gói vào, gói bị bỏ vì ngập, kết nối bị cắt vì ngập, lỗi xử lý gói). Endpoint này tự đổi 4 bộ
 * đếm đó thành "mỗi giây" để trang vẽ thẳng.
 *
 * Query: ?gio=6 (số giờ gần nhất, 1..168, mặc định 6). Mẫu dày quá thì gộp theo phút để không
 * trả quá ~720 điểm.
 *
 * Bảng chưa có (server chưa chạy bản có ChiSoMayChu, hoặc sv.chiSoMayChu=0) -> bangChuaCo = true.
 * `songGanNhat`: mẫu mới nhất cách đây bao nhiêu giây — > 90 giây nghĩa là server chết hoặc TREO
 * (cùng ngưỡng với HTBB/vps/kiem-suc-khoe.sh).
 */
const BANG = 'tob_chi_so_may_chu'

interface Dong {
  thoi_gian: string
  ket_noi: number
  nhan_vat: number
  heap_mb: number
  heap_toi_da_mb: number
  so_thread: number
  cpu_phan_tram: number
  tai_may: number
  goi_vao: number
  goi_bo: number
  cat_ngap: number
  loi_xu_ly: number
  bao_tri: number
}

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const gio = Math.min(168, Math.max(1, Math.trunc(Number(query.gio ?? 6)) || 6))
  // ~120 mẫu/giờ; gộp theo bucket (giây) để không quá ~720 điểm.
  const bucket = Math.max(30, Math.ceil((gio * 3600) / 720 / 30) * 30)

  try {
    const dong = await q<Dong & { t: number }>(
      dbGame(),
      `SELECT FLOOR(UNIX_TIMESTAMP(thoi_gian) / ?) * ? AS t,
              MAX(thoi_gian) AS thoi_gian,
              MAX(ket_noi) AS ket_noi, MAX(nhan_vat) AS nhan_vat,
              MAX(heap_mb) AS heap_mb, MAX(heap_toi_da_mb) AS heap_toi_da_mb,
              MAX(so_thread) AS so_thread, AVG(cpu_phan_tram) AS cpu_phan_tram, AVG(tai_may) AS tai_may,
              SUM(goi_vao) AS goi_vao, SUM(goi_bo) AS goi_bo, SUM(cat_ngap) AS cat_ngap,
              SUM(loi_xu_ly) AS loi_xu_ly, MAX(bao_tri) AS bao_tri, COUNT(*) AS n
         FROM \`${BANG}\`
        WHERE thoi_gian >= DATE_SUB(NOW(), INTERVAL ? HOUR)
        GROUP BY t ORDER BY t`,
      [bucket, bucket, gio],
    )
    const moiNhat = await q1<{ cach: number }>(
      dbGame(),
      // UNIX_TIMESTAMP thay TIMESTAMPDIFF: NovaDB (máy dev) không hiểu TIMESTAMPDIFF(SECOND, ...)
      `SELECT UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(MAX(thoi_gian)) AS cach FROM \`${BANG}\``,
    )
    const giay = (r: Record<string, any>) => Math.max(1, Number(r.n ?? 1) * 30)
    return {
      bangChuaCo: false,
      gio,
      bucketGiay: bucket,
      songGanNhat: moiNhat?.cach == null ? null : Number(moiNhat.cach),
      diem: dong.map(r => ({
        luc: r.thoi_gian,
        ketNoi: Number(r.ket_noi),
        nhanVat: Number(r.nhan_vat),
        heapMb: Number(r.heap_mb),
        heapToiDaMb: Number(r.heap_toi_da_mb),
        thread: Number(r.so_thread),
        cpu: Math.round(Number(r.cpu_phan_tram)),
        taiMay: Math.round(Number(r.tai_may) * 100) / 100,
        goiVaoGiay: Math.round(Number(r.goi_vao) / giay(r)),
        goiBoGiay: Math.round((Number(r.goi_bo) / giay(r)) * 100) / 100,
        catNgap: Number(r.cat_ngap),
        loiXuLy: Number(r.loi_xu_ly),
        baoTri: Number(r.bao_tri) === 1,
      })),
    }
  } catch (e: any) {
    const loi = String(e?.message ?? e)
    if (e?.code === 'ER_NO_SUCH_TABLE' || /exist/i.test(loi)) {
      return { bangChuaCo: true, gio, bucketGiay: bucket, songGanNhat: null, diem: [] }
    }
    throw e
  }
})
