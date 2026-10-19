/**
 * Báo cáo "Nghi clone" (/admin/kinh-te): ai nhận xu / giá trị từ nhiều Pokémon khác nhau, nhất là Pokémon dùng chung
 * thiết bị / IP. Nguồn: tob_log_chuyen_gia_tri (máy chủ game ghi mọi lần chuyển giữa hai người chơi — giao dịch, ký
 * gửi, thú cưng lấy xu, chợ trang trí, chợ sinh thái — kể cả lần bị chặn; com.htbb.pokemon.ChongClone).
 * ?ngay=7 (1..90). Chỉ đọc.
 */
const BANG = 'tob_log_chuyen_gia_tri'

export default defineAdminHandler(async (event) => {
  const ngay = Math.min(90, Math.max(1, int(getQuery(event).ngay, 7)))
  if (!(await coBang(dbGame(), BANG))) return { coBang: false, ngay, nguoiNhan: [], chiTiet: [], mayChung: [] }
  const nguoiNhan = await q<Record<string, unknown>>(dbGame(),
    `SELECT nhan_id AS id, MAX(nhan_ten) AS ten, MAX(nhan_cap) AS cap,
            COUNT(DISTINCT CASE WHEN bi_chan = 0 THEN gui_id END) AS soNguoiGui,
            SUM(CASE WHEN bi_chan = 0 THEN 1 ELSE 0 END) AS soLan,
            SUM(CASE WHEN bi_chan = 0 THEN xu ELSE 0 END) AS tongXu,
            SUM(nghi) AS soNghi, SUM(bi_chan) AS soBiChan,
            COUNT(DISTINCT NULLIF(gui_may, '')) AS soMay, COUNT(DISTINCT NULLIF(gui_ip, '')) AS soIp,
            SUM(CASE WHEN gui_may <> '' AND gui_may = nhan_may THEN 1 ELSE 0 END) AS cungMayNguoiNhan,
            MAX(luc) AS lanCuoi
       FROM ${BANG}
      WHERE luc >= NOW() - INTERVAL ? DAY
      GROUP BY nhan_id
     HAVING soNguoiGui >= 2 OR soNghi > 0 OR soBiChan > 0
      ORDER BY soNghi DESC, soNguoiGui DESC, tongXu DESC
      LIMIT 200`, [ngay])
  const mayChung = await q<Record<string, unknown>>(dbGame(),
    `SELECT gui_may AS may, COUNT(DISTINCT gui_id) AS soPokemon, COUNT(DISTINCT gui_tk) AS soTaiKhoan,
            COUNT(DISTINCT nhan_id) AS soNguoiNhan, SUM(CASE WHEN bi_chan = 0 THEN xu ELSE 0 END) AS tongXu,
            GROUP_CONCAT(DISTINCT gui_ten ORDER BY gui_ten SEPARATOR ', ') AS dsGui,
            GROUP_CONCAT(DISTINCT nhan_ten ORDER BY nhan_ten SEPARATOR ', ') AS dsNhan
       FROM ${BANG}
      WHERE luc >= NOW() - INTERVAL ? DAY AND gui_may <> ''
      GROUP BY gui_may
     HAVING soPokemon >= 2
      ORDER BY soPokemon DESC, tongXu DESC
      LIMIT 100`, [ngay])
  const chiTiet = await q<Record<string, unknown>>(dbGame(),
    `SELECT id, luc, kieu, gui_ten AS gui, gui_cap AS guiCap, gui_may AS guiMay, gui_ip AS guiIp,
            nhan_ten AS nhan, nhan_cap AS nhanCap, xu, chi_tiet AS chiTiet, bi_chan AS biChan, nghi, ly_do AS lyDo
       FROM ${BANG}
      WHERE luc >= NOW() - INTERVAL ? DAY AND (nghi = 1 OR bi_chan = 1)
      ORDER BY id DESC
      LIMIT 300`, [ngay])
  const n = (v: unknown) => Number(v) || 0
  return {
    coBang: true,
    ngay,
    nguoiNhan: nguoiNhan.map(r => ({
      id: n(r.id), ten: String(r.ten ?? ''), cap: n(r.cap), soNguoiGui: n(r.soNguoiGui), soLan: n(r.soLan), tongXu: n(r.tongXu),
      soNghi: n(r.soNghi), soBiChan: n(r.soBiChan), soMay: n(r.soMay), soIp: n(r.soIp), cungMayNguoiNhan: n(r.cungMayNguoiNhan),
      lanCuoi: r.lanCuoi == null ? null : String(r.lanCuoi),
    })),
    mayChung: mayChung.map(r => ({
      may: String(r.may ?? ''), soPokemon: n(r.soPokemon), soTaiKhoan: n(r.soTaiKhoan), soNguoiNhan: n(r.soNguoiNhan),
      tongXu: n(r.tongXu), dsGui: String(r.dsGui ?? ''), dsNhan: String(r.dsNhan ?? ''),
    })),
    chiTiet: chiTiet.map(r => ({
      id: n(r.id), luc: String(r.luc ?? ''), kieu: String(r.kieu ?? ''), gui: String(r.gui ?? ''), guiCap: n(r.guiCap),
      guiMay: String(r.guiMay ?? ''), guiIp: String(r.guiIp ?? ''), nhan: String(r.nhan ?? ''), nhanCap: n(r.nhanCap),
      xu: n(r.xu), chiTiet: String(r.chiTiet ?? ''), biChan: n(r.biChan), nghi: n(r.nghi), lyDo: String(r.lyDo ?? ''),
    })),
  }
})
