/**
 * Dữ liệu trang "Cấm theo máy".
 * ?q= mã máy HOẶC tên tài khoản — một ô tìm cho cả hai chiều.
 */
export default defineAdminHandler(async (event) => {
  const qs = str(getQuery(event).q).slice(0, 64)
  const ra = {
    q: qs,
    coBang: true,
    loiDoc: '',
    dsCam: [] as { ma_may: string, ly_do: string, boi_ai: string, luc: string }[],
    tong: { may: 0, cam: 0, nhieu_tk: 0 },
    mayNhieuTk: [] as { ma_may: string, so_tk: number, gan_nhat: string, ip: string }[],
    tkCuaMay: [] as { username: string, ip: string, so_lan: number, lan_dau: string, lan_cuoi: string }[],
    mayCuaTk: [] as { ma_may: string, ip: string, so_lan: number, lan_dau: string, lan_cuoi: string }[],
    dangBiCam: false,
  }
  const g = dbGame()
  try {
    ra.dsCam = await q(g, `SELECT ma_may, ly_do, boi_ai, luc FROM ${TK_BANG_CAM_MAY} ORDER BY luc DESC LIMIT 200`)
    ra.tong.may = Number(await qv(g, `SELECT COUNT(DISTINCT ma_may) FROM ${TK_BANG_LICH_SU_MAY}`) ?? 0)
    ra.tong.cam = ra.dsCam.length
    ra.tong.nhieu_tk = Number(await qv(g,
      `SELECT COUNT(*) FROM (SELECT ma_may FROM ${TK_BANG_LICH_SU_MAY} GROUP BY ma_may HAVING COUNT(*) >= 5) t`) ?? 0)

    // Máy dùng nhiều tài khoản nhất — đây là chỗ kẻ lập hàng loạt tài khoản lộ ra.
    ra.mayNhieuTk = await q(g,
      `SELECT ma_may, COUNT(*) so_tk, MAX(lan_cuoi) gan_nhat, MAX(ip) ip
         FROM ${TK_BANG_LICH_SU_MAY}
        GROUP BY ma_may
       HAVING so_tk >= 2
        ORDER BY so_tk DESC, gan_nhat DESC
        LIMIT 50`)

    if (qs !== '') {
      // Gõ mã máy ra danh sách tài khoản, gõ tên tài khoản ra danh sách máy.
      ra.tkCuaMay = await q(g,
        `SELECT username, ip, so_lan, lan_dau, lan_cuoi FROM ${TK_BANG_LICH_SU_MAY}
          WHERE ma_may = ? ORDER BY lan_cuoi DESC LIMIT 200`, [qs])
      ra.mayCuaTk = await q(g,
        `SELECT ma_may, ip, so_lan, lan_dau, lan_cuoi FROM ${TK_BANG_LICH_SU_MAY}
          WHERE username = ? ORDER BY lan_cuoi DESC LIMIT 200`, [qs])
      ra.dangBiCam = (await qv(g, `SELECT 1 FROM ${TK_BANG_CAM_MAY} WHERE ma_may = ? LIMIT 1`, [qs])) !== null
    }
  } catch (e: any) {
    console.error('[cammay] doc', e?.code ?? e)
    ra.coBang = false
    ra.loiDoc = 'Không đọc được bảng tob_may_dang_nhap / tob_cam_may (server game tạo khi khởi động; xem log máy chủ web).'
  }
  return ra
})
