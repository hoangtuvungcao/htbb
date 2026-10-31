/**
 * Lịch sử chỉ số VPS từ `tob_chi_so_vps` (bộ lấy mẫu của web ghi mỗi phút, giữ 7 ngày) + tổng mạng / đĩa HÔM NAY.
 * Query: ?gio=6 (1..168). Gộp theo bucket để không quá ~720 điểm. Chỉ dòng của máy đang chạy web (cột `may`).
 * Bảng chưa có -> bangChuaCo = true (web tự tạo khi ghi phút đầu tiên; sql/htbb.sql cũng có).
 */
import os from 'node:os'
import { BANG_VPS } from '../../../utils/vpsThuan'

export default defineAdminHandler(async (event) => {
  const gio = Math.min(168, Math.max(1, Math.trunc(Number(getQuery(event).gio ?? 6)) || 6))
  const bucket = Math.max(60, Math.ceil((gio * 3600) / 720 / 60) * 60)
  const may = os.hostname().slice(0, 64)
  try {
    const dong = await q(dbGame(),
      `SELECT FLOOR(UNIX_TIMESTAMP(luc) / ?) * ? AS t, COUNT(*) AS n,
              AVG(cpu_tb) AS cpu_tb, MAX(cpu_max) AS cpu_max, AVG(tai_1) AS tai_1,
              MAX(ram_tong_mb) AS ram_tong_mb, MAX(ram_dung_mb) AS ram_dung_mb, MIN(ram_san_min_mb) AS ram_san_mb,
              MAX(swap_tong_mb) AS swap_tong_mb, MAX(swap_dung_mb) AS swap_dung_mb, AVG(swap_vao_s) AS swap_vao_s, AVG(swap_ra_s) AS swap_ra_s,
              SUM(dia_doc_b) AS dia_doc_b, SUM(dia_ghi_b) AS dia_ghi_b, SUM(mang_vao_b) AS mang_vao_b, SUM(mang_ra_b) AS mang_ra_b,
              AVG(mang_goi_vao_s) AS mang_goi_vao_s, AVG(mang_goi_ra_s) AS mang_goi_ra_s, MAX(dia_max_pt) AS dia_max_pt
         FROM \`${BANG_VPS}\`
        WHERE may = ? AND luc >= DATE_SUB(NOW(), INTERVAL ? HOUR)
        GROUP BY t ORDER BY t`,
      [bucket, bucket, may, gio])
    const homNay = await q1(dbGame(),
      `SELECT SUM(mang_vao_b) AS vao, SUM(mang_ra_b) AS ra, SUM(dia_doc_b) AS doc, SUM(dia_ghi_b) AS ghi, COUNT(*) AS phut
         FROM \`${BANG_VPS}\` WHERE may = ? AND luc >= CURDATE()`, [may])
    const n = (v: unknown) => (v === null || v === undefined ? null : Math.round(Number(v) * 100) / 100)
    const moiGiay = (v: unknown, r: Record<string, any>) => (v === null || v === undefined ? null : Math.round(Number(v) / (Number(r.n) * 60)))
    return {
      bangChuaCo: false,
      gio,
      bucketGiay: bucket,
      may,
      homNay: {
        mangVaoB: Number(homNay?.vao ?? 0), mangRaB: Number(homNay?.ra ?? 0),
        diaDocB: Number(homNay?.doc ?? 0), diaGhiB: Number(homNay?.ghi ?? 0), soPhut: Number(homNay?.phut ?? 0),
      },
      diem: dong.map(r => ({
        t: Number(r.t) * 1000,
        cpuTb: n(r.cpu_tb), cpuMax: n(r.cpu_max), tai1: n(r.tai_1),
        ramTongMb: Number(r.ram_tong_mb), ramDungMb: Number(r.ram_dung_mb), ramSanMb: Number(r.ram_san_mb),
        swapTongMb: n(r.swap_tong_mb), swapDungMb: n(r.swap_dung_mb), swapVaoS: n(r.swap_vao_s), swapRaS: n(r.swap_ra_s),
        docBs: moiGiay(r.dia_doc_b, r), ghiBs: moiGiay(r.dia_ghi_b, r), vaoBs: moiGiay(r.mang_vao_b, r), raBs: moiGiay(r.mang_ra_b, r),
        goiVaoS: n(r.mang_goi_vao_s), goiRaS: n(r.mang_goi_ra_s), diaMaxPt: n(r.dia_max_pt),
      })),
    }
  }
  catch (e: any) {
    if (e?.code === 'ER_NO_SUCH_TABLE' || /exist/i.test(String(e?.message ?? e))) {
      return { bangChuaCo: true, gio, bucketGiay: bucket, may, homNay: null, diem: [] }
    }
    throw e
  }
})
