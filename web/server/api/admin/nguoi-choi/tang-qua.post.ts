/**
 * TẶNG QUÀ cho một nhân vật — CHỈ siêu quản trị (shared/utils/quyenAdmin.ts), và CHỈ qua đường giftcode an toàn:
 * web KHÔNG ghi vật phẩm / lượng vào tob_char (nhân vật online sẽ ghi đè, và đó là đường in tiền không kiểm soát).
 *
 * Cách làm: tạo MỘT mã cá nhân trong tob_giftcode (1 lượt, có hạn, mã ngẫu nhiên "qr" + 14 ký tự không dò được) rồi
 * gửi mã vào hộp thư (tob_inbox) của đúng nhân vật đó. Người chơi tự nhập mã trong game; server game kiểm lại toàn
 * bộ luật khi phát (real/GiftcodeAnToan.nhap: danh sách trắng, trần giá trị, ngân sách tháng, tối đa mã/tháng/nhân
 * vật, mỗi tài khoản 1 lần/mã). Kiểm tra ở web dùng ĐÚNG các hàm của /api/admin/giftcode-moi/luu
 * (gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcKiemVatPham) — không có bộ luật thứ hai.
 *
 * Giới hạn: game không gắn mã với tài khoản — ai biết mã đều nhập được (vì vậy mã chỉ nằm trong thư của người nhận và
 * bị che trong nhật ký / với vai trò khác); chỉ hoạt động khi máy chủ bật sv.lau_dai=1 (hệ giftcode an toàn).
 *
 * Body { charId, phanThuong ("LUONGKHOA:n,GEM:id:n,ITEM:id:n"), lyDo (3–200), soNgay (1–90, mặc định 30), loiNhan? (≤ 200) }
 */
import { randomBytes } from 'node:crypto'
import { gcChuanHoaThuong, gcGiaTri, gcGioiHan, gcKiemVatPham, gcMoTa, gcThangNay } from '../../../utils/giftcodeAnToan'
import { hsAnMa, hsGiaiMaLauDai, hsNoiDungThuQua, hsSoGio, hsTaoMaQuaRieng, HS_TIEN_TO_QUA_RIENG } from '../../../utils/hoSoNguoiChoi'

/** Mỗi siêu quản trị tạo tối đa bấy nhiêu quà riêng / 24 giờ (chặn bấm nhầm hàng loạt / tài khoản admin bị chiếm). */
const TOI_DA_MOI_NGAY = 20
/** Một nhân vật giữ tối đa bấy nhiêu mã quà riêng chưa dùng, còn hạn. */
const TOI_DA_CHO_DUNG = 3

export default defineAdminHandler(async (event, admin) => {
  if (!(await coBang(dbGame(), 'tob_giftcode'))) throw apiLoi(409, 'Chưa có bảng tob_giftcode (chạy sql/htbb.sql).')
  const b = await bodyObj(event)
  const id = ncIdNhanVat(b.charId)
  const lyDo = chuoi(b.lyDo, 'Lý do', 200, 3)
  const loiNhan = chuoi(b.loiNhan, 'Lời nhắn', 200)
  const soNgay = b.soNgay === undefined || b.soNgay === '' ? 30 : hsSoGio(b.soNgay, 90)
  if (soNgay === null || soNgay < 1) throw apiLoi(400, 'Hạn dùng mã: 1–90 ngày.')
  if (typeof b.phanThuong !== 'string' || b.phanThuong.length > 500) throw apiLoi(400, 'Phần thưởng không hợp lệ.')

  const c = await q1<{ id: number, charname: string, userId: number, lastLv: number }>(dbGame(), 'SELECT id, charname, userId, lastLv FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, 'Không tìm thấy Pokémon.')
  const chu = String(await qv(dbAccount(), 'SELECT username FROM team_user WHERE id = ? LIMIT 1', [c.userId]) ?? '')
  if (!chu) throw apiLoi(409, 'Pokémon không còn tài khoản sở hữu.')
  if (chu.toLowerCase() === admin.username.toLowerCase() || await ncLaQuanTri(chu)) throw apiLoi(403, 'Không tặng quà cho Pokémon của tài khoản quản trị (kể cả của chính mình).')

  // ---- cùng luật với giftcode-moi/luu
  const chuan = gcChuanHoaThuong(b.phanThuong)
  if (!chuan) throw apiLoi(400, 'Phần thưởng chỉ được gồm LUONGKHOA:n, GEM:id:n, ITEM:id:n (n ≤ 10, vật phẩm khoá).')
  const gioiHan = await gcGioiHan()
  const giaTri = gcGiaTri(chuan)
  if (giaTri > gioiHan.giaTriToiDa) throw apiLoi(400, `Giá trị quà ${giaTri} vượt trần ${gioiHan.giaTriToiDa} (lượng khoá + số đá + 5 × số vật phẩm).`)
  const cap = Math.max(1, Math.min(100, Math.trunc(Number(c.lastLv) || 1)))
  const loiVatPham = await gcKiemVatPham(chuan, cap)
  if (loiVatPham) throw apiLoi(400, loiVatPham)

  // ---- ngân sách tháng (server game cũng chặn lúc nhập; báo sớm ở đây)
  const thang = gcThangNay()
  const daDung = Number((await ncDocAnToan<{ s: number | null }>('tob_giftcode_log', 'SELECT SUM(gia_tri) AS s FROM tob_giftcode_log WHERE thang = ?', [thang]))?.[0]?.s ?? 0)
  if (daDung + giaTri > gioiHan.nganSachThang) throw apiLoi(409, `Ngân sách giftcode tháng này còn ${Math.max(0, gioiHan.nganSachThang - daDung)} — không đủ cho quà giá trị ${giaTri}.`)

  // ---- chống lạm dụng
  const homNay = Number(await qv(dbGame(), "SELECT COUNT(*) FROM tob_giftcode WHERE ma LIKE ? AND tao_boi = ? AND tao_luc > DATE_SUB(NOW(), INTERVAL 1 DAY)", [`${HS_TIEN_TO_QUA_RIENG}%`, admin.username]) ?? 0)
  if (homNay >= TOI_DA_MOI_NGAY) throw apiLoi(429, `Mỗi quản trị tạo tối đa ${TOI_DA_MOI_NGAY} quà riêng trong 24 giờ.`)
  const tenEsc = chu.replace(/[!%_]/g, x => `!${x}`)
  const choDung = Number(await qv(dbGame(),
    "SELECT COUNT(*) FROM tob_giftcode WHERE ma LIKE ? AND ghi_chu LIKE ? ESCAPE '!' AND da_dung < tong_luot AND (han IS NULL OR han > NOW())",
    [`${HS_TIEN_TO_QUA_RIENG}%`, `%(TK ${tenEsc})%`]) ?? 0)
  if (choDung >= TOI_DA_CHO_DUNG) throw apiLoi(409, `Tài khoản ${chu} đã có ${choDung} quà riêng chưa dùng — chờ người chơi nhập hoặc hết hạn.`)

  // ---- giới hạn mã/tháng của nhân vật (chỉ cảnh báo: mã còn hạn sang tháng sau vẫn nhập được)
  let canhBao = ''
  const ld = (await ncDocAnToan<{ du_lieu: string }>('tob_char_lau_dai', 'SELECT du_lieu FROM tob_char_lau_dai WHERE id_char = ? LIMIT 1', [id]))?.[0]
  if (ld) {
    const h = hsGiaiMaLauDai(ld.du_lieu)
    if (h.giftcode.thang === thang && h.giftcode.dem >= gioiHan.toiDaThang) canhBao = ` Lưu ý: Pokémon đã nhập đủ ${gioiHan.toiDaThang} mã trong tháng này — phải đợi sang tháng sau mới nhập được.`
  }

  // ---- tạo mã (thử lại nếu trùng)
  const han = gioSau(soNgay * 24)
  // tob_giftcode / tob_inbox là utf8 (3 byte): emoji → '?' để MariaDB strict không báo lỗi 1366 (chuoiDb.ts)
  const ghiChu = chuUtf8mb3(`Quà riêng: ${c.charname} (TK ${chu}) — ${lyDo}`.slice(0, 255))
  let ma = ''
  for (let lan = 0; lan < 5 && !ma; lan++) {
    const thu = hsTaoMaQuaRieng(randomBytes(14))
    try {
      await exec(dbGame(), 'INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu, tao_boi, tao_luc) VALUES (?, ?, ?, 1, 0, ?, 1, ?, ?, NOW())',
        [thu, chuan, han, cap, ghiChu, admin.username])
      ma = thu
    }
    catch (e: any) {
      if (e?.code !== 'ER_DUP_ENTRY' && !/UNIQUE|duplicate/i.test(String(e?.message))) throw e
    }
  }
  if (!ma) throw apiLoi(500, 'Không tạo được mã quà, thử lại.')

  const moTa = await gcMoTa(chuan)
  const thuGui = await exec(dbGame(), 'INSERT INTO tob_inbox (charname, sender, info, dateSend) VALUES (?, ?, ?, NOW())',
    [c.charname, 'Ban quản trị', chuUtf8mb3(hsNoiDungThuQua(moTa, ma, han, loiNhan))])
  await ghiThayDoi(event, 'tang_qua', `nv:${c.charname}`, null,
    { ma: hsAnMa(ma), phanThuong: chuan, moTa, giaTri, han, lyDo, loiNhan, idThu: Number(thuGui.insertId), taiKhoan: chu })
  return { ok: true, ma, giaTri, han, message: `Đã tạo mã quà riêng (giá trị ${giaTri}) và gửi vào hộp thư của ${c.charname}. Mã dùng 1 lần, hạn tới ${han}.${canhBao}` }
})
