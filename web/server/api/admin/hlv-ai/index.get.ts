/**
 * Huấn luyện viên AI (bot lấp máy chủ) cho trang /admin/hlv-ai: cấu hình nhóm hlv_ai (giá trị đang lưu + mặc định),
 * danh sách bot (tob_hlv_ai_trang_thai - dịch vụ bot ghi mỗi phút), kinh tế bot theo ngày + ảnh chụp 10 phút
 * (máy chủ game ghi: real.HlvAiGame), chuyển giá trị bot ↔ người gần đây. Lưu cấu hình: /api/admin/cau-hinh-he-thong/luu.
 */
export default defineAdminHandler(async () => {
  const game = dbGame()
  const danhMuc = await q<{ khoa: string, mac_dinh: string }>(game,
    `SELECT khoa, mac_dinh FROM cau_hinh_danh_muc WHERE nhom = 'hlv_ai' ORDER BY thu_tu`).catch(() => [])
  const luu = await q<{ khoa: string, gia_tri: string, sua_boi: string | null, sua_luc: string | null }>(game,
    `SELECT khoa, gia_tri, sua_boi, sua_luc FROM cau_hinh WHERE khoa LIKE 'hlv\\_ai.%'`).catch(() => [])
  const cauHinh: Record<string, { giaTri: string, macDinh: string, daLuu: boolean }> = {}
  for (const d of danhMuc) cauHinh[d.khoa] = { giaTri: String(d.mac_dinh ?? ''), macDinh: String(d.mac_dinh ?? ''), daLuu: false }
  for (const l of luu) {
    if (cauHinh[l.khoa]) cauHinh[l.khoa] = { ...cauHinh[l.khoa]!, giaTri: String(l.gia_tri ?? ''), daLuu: true }
  }
  const coTrangThai = await coBang(game, 'tob_hlv_ai_trang_thai')
  const coNgay = await coBang(game, 'tob_hlv_ai_kinh_te_ngay')
  const bot = coTrangThai
    ? await q(game, `SELECT tai_khoan, char_id, ten, lop, tinh_cach, cap, cap_muc_tieu, map_id, ten_map, hoat_dong, online, xu,
        vao_luc, ra_luc, cap_nhat FROM tob_hlv_ai_trang_thai ORDER BY online DESC, cap DESC, tai_khoan LIMIT 600`)
    : []
  const ngay = coNgay ? (await q(game, `SELECT * FROM tob_hlv_ai_kinh_te_ngay ORDER BY ngay DESC LIMIT 60`)).reverse() : []
  const moc = coNgay ? (await q(game, `SELECT * FROM tob_hlv_ai_moc WHERE luc >= NOW() - INTERVAL 3 DAY ORDER BY luc DESC LIMIT 432`)).reverse() : []
  const chuyen = coNgay
    ? await q(game, `SELECT luc, huong, kieu, bot_ten, nguoi_ten, xu, gia_tri, bi_chan, chi_tiet FROM tob_hlv_ai_chuyen ORDER BY id DESC LIMIT 60`)
    : []
  const dongNay = Date.now()
  return {
    coBang: coTrangThai && coNgay,
    cauHinh,
    bot: bot.map(r => ({
      taiKhoan: String(r.tai_khoan), charId: Number(r.char_id), ten: tenHienThiHlvAi(String(r.ten ?? ''), true), lop: Number(r.lop),
      tinhCach: String(r.tinh_cach), cap: Number(r.cap), capMucTieu: Number(r.cap_muc_tieu), mapId: Number(r.map_id),
      tenMap: String(r.ten_map ?? ''), hoatDong: String(r.hoat_dong ?? ''), xu: Number(r.xu),
      // dịch vụ ghi mỗi phút: quá 3 phút không cập nhật = dịch vụ đã dừng -> coi như offline
      online: Number(r.online) === 1 && r.cap_nhat != null && dongNay - new Date(r.cap_nhat).getTime() < 180000,
      vaoLuc: r.vao_luc ? String(r.vao_luc) : null, raLuc: r.ra_luc ? String(r.ra_luc) : null, capNhat: r.cap_nhat ? String(r.cap_nhat) : null,
    })),
    ngay: ngay.map(r => ({
      ngay: String(r.ngay).slice(0, 10), xuTao: Number(r.xu_tao), xuCat: Number(r.xu_cat), xuDot: Number(r.xu_dot), xuTieu: Number(r.xu_tieu),
      xuNhanNguoi: Number(r.xu_nhan_nguoi), xuTraNguoi: Number(r.xu_tra_nguoi), giaTriChoNguoi: Number(r.gia_tri_cho_nguoi),
      soChuyen: Number(r.so_chuyen), soChan: Number(r.so_chan), vatHuy: Number(r.vat_huy), tongXuBot: Number(r.tong_xu_bot),
      tongXuNguoi: Number(r.tong_xu_nguoi), soBot: Number(r.so_bot), soBotOnline: Number(r.so_bot_online),
    })),
    moc: moc.map(r => ({
      luc: String(r.luc), tongXuBot: Number(r.tong_xu_bot) + Number(r.kho_xu_bot), tongXuNguoi: Number(r.tong_xu_nguoi) + Number(r.kho_xu_nguoi),
      soBotOnline: Number(r.so_bot_online), soNguoiOnline: Number(r.so_nguoi_online),
    })),
    chuyen: chuyen.map(r => ({
      luc: String(r.luc), huong: Number(r.huong), kieu: String(r.kieu), bot: tenHienThiHlvAi(String(r.bot_ten), true), nguoi: String(r.nguoi_ten),
      xu: Number(r.xu), giaTri: Number(r.gia_tri), biChan: Number(r.bi_chan) === 1, chiTiet: String(r.chi_tiet ?? ''),
    })),
  }
})
