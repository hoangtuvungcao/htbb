/**
 * Cấu hình server (bảng htbb_db.cau_hinh).
 *
 * 32 setting server.ini nạp lại được lúc đang chạy, xem src/server/CauHinh.java.
 *
 * TRƯỚC 09/09/2026: chỉ sửa được bằng cách SSH vào VPS sửa tay server.ini, hoặc vào
 * game gõ chat "setting" (real/AdminMenu.java). NAY: trang này, bảng setting trong
 * game, và server game đọc/ghi CHUNG một bảng SQL. Server tự đọc lại bảng mỗi 10 giây
 * (server.CauHinh.batDau()) nên sửa ở đây thì server áp dụng trong tối đa 10 giây —
 * KHÔNG cần restart.
 *
 * KHÔNG PHẢI TOÀN BỘ server.ini nằm ở đây — chỉ đúng các khoá đã có sẵn cơ chế nạp lại
 * (xem CauHinh.java). Khoá kết nối CSDL/mở cổng (db.host, sv.port, ...) vẫn đọc một lần
 * lúc khởi động, buộc phải sửa qua server.ini + restart như trước. Trang này KHÔNG ghi
 * server.ini, chỉ ghi bảng cau_hinh — server.ini là tệp trên VPS game, web không có
 * quyền (và không nên có quyền) sửa nó.
 *
 * Phần đầu tệp là hàm thuần (dựng/kẹp giá trị); phần cuối đụng tới CSDL.
 */

export const BANG_CAU_HINH = 'cau_hinh'

/** Phải khớp K_* trong src/server/CauHinh.java. Đây cũng là WHITELIST khoá được ghi. */
export const CAU_HINH_K = {
  TILE_XU: 'sv.tileXu',
  TILE_LUONG: 'sv.tileLuong',
  TILE_SUKIEN: 'sv.tileItemSuKien',
  TILE_POTION: 'sv.tilePotion',
  TILE_NGOC: 'sv.tileNgoc',
  TILE_ITEM: 'sv.tileItem',
  CHIA_XU: 'sv.chiaXuRoi',
  TILE_EXP_LV50: 'sv.tileExpLv50',
  TILE_EXP_DUOI_LV50: 'sv.tileExpDuoiLv50',
  TIEU_NGUONG_MAU: 'sv.tieuNguongMau',
  TIEU_HE_SO_EXP: 'sv.tieuHeSoExp',
  TIEU_PC_EXP_THEO_MAU: 'sv.tieuPcExpTheoMau',
  TIEU_PERMILLE_CAO_CAP: 'sv.tieuPermilleCaoCap',
  DOI_TIEU_NGUONG_MAU_XU: 'sv.doiTieuNguongMauXu',
  DOI_TIEU_NGUONG_MAU_LUONG: 'sv.doiTieuNguongMauLuong',
  GIO_CT: 'sv.gioChiemThanh',
  PHUT_CT: 'sv.phutChiemThanh',
  NGAY_CT: 'sv.ngayChiemThanh',
  SK_TU_KHOI_DONG_LAI: 'sv.suKienTuKhoiDongLai',
  SK_GOM_GIAY: 'sv.suKienGomGiay',
  SK_BAO_TRUOC_GIAY: 'sv.suKienBaoTruocGiay',
  TAO_TEN_GM: 'sv.choPhepTaoTenGM',
  MAX_CHAR: 'sv.maxCharPerAccount',
  ADMIN_ACCOUNTS: 'sv.adminAccounts',
  WEBSITE: 'sv.website',
  LINK_TAI_GAME: 'sv.linkTaiGame',
  TEST_MODE: 'sv.testMode',
  TEST_CODE: 'sv.testCode',
  EXP_VANTIEU: 'sv.expvantieu',
  LUCKY_EXP: 'sv.Luckyexp',
  EXP_QUA: 'sv.ExpQua',
} as const

const K = CAU_HINH_K

/** Nhóm khoá của từng thẻ trên trang — dùng cho dòng "sửa lần cuối bởi...". */
export const CAU_HINH_NHOM: Record<string, string[]> = {
  drop: [K.TILE_XU, K.TILE_LUONG, K.TILE_SUKIEN, K.TILE_POTION, K.TILE_NGOC, K.TILE_ITEM, K.CHIA_XU],
  expQuai: [K.TILE_EXP_LV50, K.TILE_EXP_DUOI_LV50],
  tieu: [K.TIEU_NGUONG_MAU, K.TIEU_HE_SO_EXP, K.TIEU_PC_EXP_THEO_MAU, K.TIEU_PERMILLE_CAO_CAP, K.DOI_TIEU_NGUONG_MAU_XU, K.DOI_TIEU_NGUONG_MAU_LUONG],
  chiemThanh: [K.GIO_CT, K.PHUT_CT, K.NGAY_CT],
  lichHen: [K.SK_TU_KHOI_DONG_LAI, K.SK_GOM_GIAY, K.SK_BAO_TRUOC_GIAY],
  expPhu: [K.EXP_VANTIEU, K.LUCKY_EXP, K.EXP_QUA],
  khac: [K.MAX_CHAR, K.ADMIN_ACCOUNTS, K.WEBSITE, K.LINK_TAI_GAME, K.TEST_MODE, K.TEST_CODE, K.TAO_TEN_GM],
}

/** Tên 7 thứ trong tuần dùng cho checkbox + Java Date.toString(), đúng thứ tự Thứ Hai trước. */
export const CAU_HINH_CAC_THU: Record<string, string> = {
  Mon: 'Thứ Hai', Tue: 'Thứ Ba', Wed: 'Thứ Tư', Thu: 'Thứ Năm',
  Fri: 'Thứ Sáu', Sat: 'Thứ Bảy', Sun: 'Chủ Nhật',
}

// ---------------------------------------------------------------------------
//  Hàm thuần
// ---------------------------------------------------------------------------

/**
 * Làm tròn nửa xa số 0, giữ `chuSo` chữ số thập phân. Làm tròn qua chuỗi mũ thập phân
 * ("1.005e2") để tránh sai số nhị phân kiểu Math.round(1.005 * 100) = 100.
 *
 * Số rất nhỏ/rất lớn tự in ra dạng mũ ("1e-7"): phải cộng vào số mũ sẵn có chứ không
 * nối thêm "e…" lần nữa ("1e-7e3" = NaN — sẽ làm ngưỡng màu tiêu lưu thành "NaN"
 * khi admin gõ % cực nhỏ như 0.0000000001).
 */
function dichMu(x: number, k: number): number {
  const [m, e] = String(x).split('e')
  return Number(`${m}e${Number(e ?? 0) + k}`)
}
export function cauHinhRound(x: number, chuSo = 0): number {
  if (!Number.isFinite(x)) return x
  const dau = x < 0 ? -1 : 1
  const r = dichMu(Math.round(dichMu(Math.abs(x), chuSo)), -chuSo)
  return dau * r
}

/** Chuỗi thô từ body JSON: số hoặc chuỗi, đã trim. Kiểu khác -> ''. */
function tho(v: unknown): string {
  if (typeof v === 'number' && Number.isFinite(v)) return String(v)
  return typeof v === 'string' ? v.trim() : ''
}

/** Số nguyên viết chuẩn: dấu tuỳ chọn, không số 0 đứng đầu ("007" bị coi là sai). */
function laSoNguyen(s: string): boolean {
  return /^[+-]?(0|[1-9]\d*)$/.test(s)
}

/** Số thực: 1, 1.5, .5, 1., 1e3... */
function laSoThuc(s: string): boolean {
  return /^[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?$/.test(s)
}

/** int, kẹp trong khoảng, mặc định khi không đọc được (ô bỏ trống / gõ chữ). */
export function cauHinhSoKep(v: unknown, min: number, max: number, macDinh: number): number {
  const s = tho(v)
  if (!laSoNguyen(s)) return macDinh
  const n = Number(s)
  if (!Number.isSafeInteger(n)) return macDinh
  return Math.max(min, Math.min(max, n))
}

/** float, kẹp trong khoảng, mặc định khi không đọc được. Cho phép số lẻ (vd 0.001). */
export function cauHinhSoThapPhan(v: unknown, min: number, max: number, macDinh: number): number {
  const s = tho(v)
  if (!laSoThuc(s)) return macDinh
  const n = Number(s)
  if (!Number.isFinite(n)) return macDinh
  return Math.max(min, Math.min(max, n))
}

/** Công tắc bật/tắt: chỉ true / 1 / '1' là bật, mọi thứ khác (thiếu, rỗng, 'true'...) là tắt. */
export function cauHinhCo(v: unknown): boolean {
  return v === true || v === 1 || v === '1'
}

/**
 * Đổi mảng 5 % (Trắng/Xanh/Đỏ/Hoàn Mỹ/Vàng) sang 5 ngưỡng CỘNG DỒN trên thang 100000
 * (phần còn lại là Tím). Dùng chung cho cả "nhận tiêu mới" và "đổi tiêu" (xu/lượng).
 */
export function cauHinhPhanTramSangNguong(phanTramMau: number[]): number[] {
  const nguong: number[] = []
  let congDon = 0
  for (const pt of phanTramMau) {
    congDon += Math.trunc(cauHinhRound(pt * 1000))
    nguong.push(Math.min(100000, congDon))
  }
  return nguong
}

/** Chiều ngược lại: trả về [% từng màu..., % Tím]. */
export function cauHinhNguongSangPhanTram(nguong: number[]): number[] {
  const phanTram: number[] = []
  let truocDo = 0
  for (const ng of nguong) {
    phanTram.push(cauHinhRound((ng - truocDo) / 1000, 3))
    truocDo = ng
  }
  phanTram.push(cauHinhRound((100000 - truocDo) / 1000, 3)) // Tím, phần tử cuối
  return phanTram
}

/** null nếu tổng hợp lệ (<=100%); ngược lại trả về thông báo lỗi. */
export function cauHinhLoiTongQua100(phanTramMau: number[], ten: string): string | null {
  const tong = cauHinhRound(phanTramMau.reduce((a, b) => a + b, 0), 3)
  if (tong > 100) {
    return `Tổng % của ${ten} đang vượt quá 100% (${tong}%) — không còn chỗ cho Tím. `
      + 'Giảm bớt một trong 5 màu rồi lưu lại.'
  }
  return null
}

/** Số nguyên đầu chuỗi của một phần tử CSV ("12abc" -> 12); không đọc được -> 0. */
function soNguyenDau(s: string): number {
  const n = Number.parseInt(s.trim(), 10)
  return Number.isFinite(n) ? n : 0
}

/** Đọc một khoá dạng CSV "a,b,c" thành int[]; sai số phần tử thì dùng macDinh. */
export function cauHinhMangTu(gt: Record<string, string>, k: string, macDinh: number[]): number[] {
  const raw = (k in gt ? String(gt[k]) : '').trim()
  if (raw === '') return macDinh
  const phan = raw.split(',').map(soNguyenDau)
  return phan.length === macDinh.length ? phan : macDinh
}

/** Số thực đầu chuỗi: "2.5abc" -> 2.5, "abc" -> 0. */
function soThucDau(s: string): number {
  const n = Number.parseFloat(s.trim())
  return Number.isFinite(n) ? n : 0
}

/** Cắt chuỗi tối đa n ký tự theo code point — cột varchar đếm theo ký tự, và không cắt đôi chữ có dấu / emoji. */
export function cauHinhCat(s: string, n: number): string {
  return Array.from(s).slice(0, n).join('')
}

/** Chi tiết ghi nhật ký: các khoá => giá trị vừa ghi, dạng JSON (giữ nguyên chữ có dấu). */
export function cauHinhJson(m: Record<string, string | number>): string {
  return JSON.stringify(m)
}

export interface CauHinhLuuKetQua {
  /** khoá => giá trị sẽ ghi (đã kẹp/kiểm). */
  m: Record<string, string | number>
  /** doi_tuong ghi nhật ký. */
  doiTuong: string
  /** Câu báo thành công. */
  thongBao: string
}

/**
 * Dựng bộ khoá => giá trị cho một thao tác lưu (mỗi thẻ trên trang là một `viec`).
 * Mọi số đều bị kẹp vào khoảng an toàn — server game đọc thẳng các giá trị này.
 * Ném Error(message) khi dữ liệu sai; khi đó KHÔNG ghi khoá nào.
 */
export function cauHinhDungThaoTac(viec: string, b: Record<string, unknown>): CauHinhLuuKetQua {
  const p = (ten: string) => b[ten]
  const ap = 'Server áp dụng trong tối đa 10 giây.'
  switch (viec) {
    case 'luu_drop':
      return {
        m: {
          [K.TILE_XU]: cauHinhSoKep(p('tileXu'), 0, 100, 0),
          [K.TILE_LUONG]: cauHinhSoKep(p('tileLuong'), 0, 100, 0),
          [K.TILE_SUKIEN]: cauHinhSoKep(p('tileItemSuKien'), 0, 100, 0),
          [K.TILE_POTION]: cauHinhSoKep(p('tilePotion'), 0, 100, 0),
          [K.TILE_NGOC]: cauHinhSoKep(p('tileNgoc'), 0, 100, 0),
          [K.TILE_ITEM]: cauHinhSoKep(p('tileItem'), 0, 100, 0),
          [K.CHIA_XU]: cauHinhSoKep(p('chiaXuRoi'), 1, 1000000, 10),
        },
        doiTuong: 'ti_le_drop',
        thongBao: `Đã lưu tỷ lệ rơi đồ. ${ap}`,
      }

    case 'luu_exp_quai':
      return {
        m: {
          [K.TILE_EXP_LV50]: cauHinhSoKep(p('tileExpLv50'), 0, 100, 10),
          [K.TILE_EXP_DUOI_LV50]: cauHinhSoKep(p('tileExpDuoiLv50'), 0, 100, 50),
        },
        doiTuong: 'ti_le_exp_quai',
        thongBao: `Đã lưu tỉ lệ EXP quái. ${ap}`,
      }

    case 'luu_tieu': {
      // Người dùng nhập % riêng cho 5 màu đầu (Trắng..Vàng) — Tím là PHẦN CÒN LẠI, không
      // nhập. Cộng dồn thành ngưỡng (thang 100000) từ 5 % này LUÔN tăng dần vì là tổng
      // đơn điệu của số không âm — chỉ cần chặn tổng 5 màu không vượt 100%.
      const mac = (ds: number[], tien: string) => ds.map((d, i) => cauHinhSoThapPhan(p(`${tien}${i}`), 0, 100, d))
      const phanTramMau = mac([60.189, 29.2, 10.3, 0.3, 0.01], 'tieuMau')
      let loi = cauHinhLoiTongQua100(phanTramMau, 'Trắng+Xanh+Đỏ+Hoàn Mỹ+Vàng (nhận hàng mới)')
      if (loi) throw new Error(loi)
      const nguong = cauHinhPhanTramSangNguong(phanTramMau)

      // Đổi tiêu (reroll trả phí) — bảng RIÊNG cho xu và lượng, cùng định dạng.
      const phanTramDoiXu = mac([50.545, 35, 13.4, 1, 0.05], 'doiTieuXuMau')
      loi = cauHinhLoiTongQua100(phanTramDoiXu, 'Trắng+Xanh+Đỏ+Hoàn Mỹ+Vàng (đổi hàng bằng xu)')
      if (loi) throw new Error(loi)
      const nguongDoiXu = cauHinhPhanTramSangNguong(phanTramDoiXu)

      const phanTramDoiLuong = mac([22.89, 30, 44, 3, 0.1], 'doiTieuLuongMau')
      loi = cauHinhLoiTongQua100(phanTramDoiLuong, 'Trắng+Xanh+Đỏ+Hoàn Mỹ+Vàng (đổi hàng bằng lượng)')
      if (loi) throw new Error(loi)
      const nguongDoiLuong = cauHinhPhanTramSangNguong(phanTramDoiLuong)

      const heSoChung = cauHinhSoThapPhan(p('tieuHeSoChung'), 0, 10, 2.0)
      const heSoMau = mac([0.5, 1.0, 1.5, 2.5, 2.5, 5.0], 'tieuHeSoMau')
      const pcExp = heSoMau.map(h => Math.trunc(cauHinhRound(h * 100)))
      pcExp.push(0) // phần tử thứ 7 — không màu nào đọc tới, giữ 0 cho đủ độ dài

      const permille: number[] = []
      for (const khung of [0, 1, 2]) {
        for (const mau of [0, 1, 2]) {
          const pc = cauHinhSoThapPhan(p(`tieuPhanTramCaoCap${khung}_${mau}`), 0, 100, 0)
          permille.push(Math.trunc(cauHinhRound(pc * 10)))
        }
      }

      return {
        m: {
          [K.TIEU_NGUONG_MAU]: nguong.join(','),
          [K.TIEU_HE_SO_EXP]: Math.trunc(cauHinhRound(heSoChung * 100)),
          [K.TIEU_PC_EXP_THEO_MAU]: pcExp.join(','),
          [K.TIEU_PERMILLE_CAO_CAP]: permille.join(','),
          [K.DOI_TIEU_NGUONG_MAU_XU]: nguongDoiXu.join(','),
          [K.DOI_TIEU_NGUONG_MAU_LUONG]: nguongDoiLuong.join(','),
        },
        doiTuong: 'ti_le_tieu',
        thongBao: `Đã lưu tỉ lệ Giao Hàng. ${ap}`,
      }
    }

    case 'luu_chiemthanh': {
      const gio = tho(p('gioChiemThanh'))
      const ngayCuThe = tho(p('ngayCuThe'))
      if (ngayCuThe !== '' && !/^\d{4}-\d{2}-\d{2}$/.test(ngayCuThe)) {
        throw new Error('Ngày cụ thể phải đúng định dạng yyyy-MM-dd.')
      }
      let ngay: string
      if (ngayCuThe !== '') {
        // Ngày cụ thể ghi đè mọi lựa chọn thứ trong tuần — chỉ áp dụng ĐÚNG một lần.
        ngay = ngayCuThe
      } else {
        // Chỉ nhận đúng 7 mã thứ, theo đúng thứ tự Thứ Hai trước (bất kể client gửi thứ tự nào).
        const chon = Array.isArray(p('cacThu')) ? (p('cacThu') as unknown[]) : []
        ngay = Object.keys(CAU_HINH_CAC_THU).filter(thu => chon.includes(thu)).join(',')
      }
      return {
        m: {
          [K.GIO_CT]: gio === '' ? -1 : cauHinhSoKep(gio, -1, 23, -1),
          [K.PHUT_CT]: cauHinhSoKep(p('phutChiemThanh'), 0, 59, 0),
          [K.NGAY_CT]: cauHinhCat(ngay, 60),
        },
        doiTuong: 'gio_chiem_thanh',
        thongBao: `Đã lưu giờ chiếm Gym. ${ap}`,
      }
    }

    case 'luu_lichhen':
      return {
        m: {
          [K.SK_TU_KHOI_DONG_LAI]: cauHinhCo(p('tuKhoiDongLai')) ? '1' : '0',
          [K.SK_GOM_GIAY]: cauHinhSoKep(p('gomGiay'), 30, 3600, 120),
          [K.SK_BAO_TRUOC_GIAY]: cauHinhSoKep(p('baoTruocGiay'), 10, 600, 60),
        },
        doiTuong: 'lich_hen_su_kien',
        thongBao: `Đã lưu lịch hẹn sự kiện. ${ap}`,
      }

    case 'luu_khac':
      return {
        m: {
          [K.TAO_TEN_GM]: cauHinhCo(p('choPhepTaoTenGM')) ? '1' : '0',
          [K.MAX_CHAR]: cauHinhSoKep(p('maxCharPerAccount'), 1, 3, 3),
          [K.ADMIN_ACCOUNTS]: cauHinhCat(tho(p('adminAccounts')), 255),
          [K.WEBSITE]: cauHinhCat(tho(p('website')), 255),
          [K.LINK_TAI_GAME]: cauHinhCat(tho(p('linkTaiGame')), 255),
          [K.TEST_MODE]: cauHinhCo(p('testMode')) ? '1' : '0',
          // Chỉ hạ chữ ASCII (không đụng chữ có dấu): mã test là chuỗi ASCII gõ trong game.
          [K.TEST_CODE]: cauHinhCat(tho(p('testCode')), 50).replace(/[A-Z]/g, c => c.toLowerCase()),
        },
        doiTuong: 'khac',
        thongBao: `Đã lưu. ${ap}`,
      }

    case 'luu_exp_phu':
      return {
        m: {
          [K.EXP_VANTIEU]: cauHinhCo(p('expVantieu')) ? '1' : '0',
          [K.LUCKY_EXP]: cauHinhCo(p('luckyExp')) ? '1' : '0',
          [K.EXP_QUA]: cauHinhCo(p('expQua')) ? '1' : '0',
        },
        doiTuong: 'exp_phu',
        thongBao: `Đã lưu. ${ap}`,
      }

    default:
      throw new Error('Không hiểu thao tác.')
  }
}

/** Giá trị hiện tại của một khoá, hoặc mặc định nếu chưa có (server chưa khởi động lần nào). */
function giaTri(gt: Record<string, string>, k: string, macDinh = ''): string {
  return k in gt ? gt[k]! : macDinh
}

/**
 * Giá trị điền sẵn cho các ô của trang. Khoá chưa có trong bảng (server chưa khởi động
 * lần nào) thì dùng mặc định của CauHinh.java. Ngưỡng cộng dồn được đổi ngược về % từng
 * màu để admin nhập; hệ số lưu ×100 / phần nghìn được đổi về số thập phân dễ đọc.
 */
export function cauHinhGiaTriForm(gt: Record<string, string>) {
  const tieuNguong = cauHinhMangTu(gt, K.TIEU_NGUONG_MAU, [60189, 89389, 99689, 99989, 99999])
  const tieuPc = cauHinhMangTu(gt, K.TIEU_PC_EXP_THEO_MAU, [50, 100, 150, 250, 250, 500, 0])
  const tieuPer = cauHinhMangTu(gt, K.TIEU_PERMILLE_CAO_CAP, [50, 70, 90, 35, 55, 75, 5, 25, 45])
  const doiXu = cauHinhMangTu(gt, K.DOI_TIEU_NGUONG_MAU_XU, [50545, 85545, 98945, 99945, 99995])
  const doiLuong = cauHinhMangTu(gt, K.DOI_TIEU_NGUONG_MAU_LUONG, [22890, 52890, 96890, 99890, 99990])

  const pcMau = cauHinhNguongSangPhanTram(tieuNguong)
  const pcDoiXu = cauHinhNguongSangPhanTram(doiXu)
  const pcDoiLuong = cauHinhNguongSangPhanTram(doiLuong)

  const caoCap: Record<string, number> = {}
  for (const khung of [0, 1, 2]) {
    for (const mau of [0, 1, 2]) {
      caoCap[`tieuPhanTramCaoCap${khung}_${mau}`] = cauHinhRound(tieuPer[khung * 3 + mau]! / 10, 1)
    }
  }

  const ngayHienTai = giaTri(gt, K.NGAY_CT, '')
  const laNgayCuThe = /^\d{4}-\d{2}-\d{2}$/.test(ngayHienTai)
  const thuDaChon = laNgayCuThe ? [] : ngayHienTai.split(',').map(s => s.trim()).filter(Boolean)

  return {
    drop: {
      tileXu: giaTri(gt, K.TILE_XU, '0'),
      tileLuong: giaTri(gt, K.TILE_LUONG, '0'),
      tileItemSuKien: giaTri(gt, K.TILE_SUKIEN, '0'),
      tilePotion: giaTri(gt, K.TILE_POTION, '0'),
      tileNgoc: giaTri(gt, K.TILE_NGOC, '0'),
      tileItem: giaTri(gt, K.TILE_ITEM, '0'),
      chiaXuRoi: giaTri(gt, K.CHIA_XU, '10'),
    },
    expQuai: {
      tileExpLv50: giaTri(gt, K.TILE_EXP_LV50, '10'),
      tileExpDuoiLv50: giaTri(gt, K.TILE_EXP_DUOI_LV50, '50'),
    },
    tieu: {
      tieuMau: pcMau.slice(0, 5),
      doiTieuXuMau: pcDoiXu.slice(0, 5),
      doiTieuLuongMau: pcDoiLuong.slice(0, 5),
      tieuHeSoChung: cauHinhRound(soThucDau(giaTri(gt, K.TIEU_HE_SO_EXP, '200')) / 100, 2),
      tieuHeSoMau: tieuPc.slice(0, 6).map(v => cauHinhRound(v / 100, 2)),
      caoCap,
    },
    chiemThanh: {
      gioChiemThanh: soNguyenDau(giaTri(gt, K.GIO_CT, '-1')) < 0 ? '' : giaTri(gt, K.GIO_CT),
      phutChiemThanh: giaTri(gt, K.PHUT_CT, '0'),
      cacThu: thuDaChon,
      ngayCuThe: laNgayCuThe ? ngayHienTai : '',
    },
    lichHen: {
      tuKhoiDongLai: giaTri(gt, K.SK_TU_KHOI_DONG_LAI, '1') === '1',
      gomGiay: giaTri(gt, K.SK_GOM_GIAY, '120'),
      baoTruocGiay: giaTri(gt, K.SK_BAO_TRUOC_GIAY, '60'),
    },
    expPhu: {
      expVantieu: giaTri(gt, K.EXP_VANTIEU, '0') === '1',
      luckyExp: giaTri(gt, K.LUCKY_EXP, '0') === '1',
      expQua: giaTri(gt, K.EXP_QUA, '0') === '1',
    },
    khac: {
      maxCharPerAccount: giaTri(gt, K.MAX_CHAR, '3'),
      adminAccounts: giaTri(gt, K.ADMIN_ACCOUNTS),
      website: giaTri(gt, K.WEBSITE),
      linkTaiGame: giaTri(gt, K.LINK_TAI_GAME),
      testCode: giaTri(gt, K.TEST_CODE),
      choPhepTaoTenGM: giaTri(gt, K.TAO_TEN_GM, '0') === '1',
      testMode: giaTri(gt, K.TEST_MODE, '0') === '1',
    },
  }
}

/** Mốc "sửa bởi X lúc Y" mới nhất trong một nhóm khoá, hoặc null (chưa từng sửa qua CSDL). */
export function cauHinhSuaMoiNhat(
  suaBoi: Record<string, [string, string]>,
  khoas: string[],
): { boi: string, luc: string } | null {
  let moiNhat: [string, string] | null = null
  for (const k of khoas) {
    const s = suaBoi[k]
    if (s && s[1] !== '' && (moiNhat === null || s[1] > moiNhat[1])) moiNhat = s
  }
  return moiNhat ? { boi: moiNhat[0], luc: moiNhat[1] } : null
}

// ---------------------------------------------------------------------------
//  CSDL
// ---------------------------------------------------------------------------

/** Tạo bảng nếu server game chưa từng khởi động (đúng định nghĩa CauHinh.taoBangNeuThieu). */
export async function cauHinhTaoBangNeuThieu(): Promise<void> {
  await exec(
    dbGame(),
    `CREATE TABLE IF NOT EXISTS \`${BANG_CAU_HINH}\` (`
    + '`khoa` varchar(64) NOT NULL,'
    + '`gia_tri` varchar(255) NOT NULL DEFAULT \'\','
    + '`sua_boi` varchar(50) DEFAULT NULL,'
    + '`sua_luc` datetime DEFAULT NULL,'
    + 'PRIMARY KEY (`khoa`)'
    + ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  )
}

/**
 * Ghi nhiều khoá trong một lượt, sua_boi = admin đang đăng nhập.
 *
 * Cả nhóm ghi trong MỘT transaction: server game đọc lại bảng mỗi 10 giây, nếu ghi lẻ
 * từng khoá mà đứt giữa chừng thì server có thể nạp nửa nhóm mới nửa nhóm cũ (vd ngưỡng
 * màu tiêu mới nhưng hệ số EXP cũ).
 */
export async function cauHinhLuu(capGiaTri: Record<string, string | number>, adminTen: string): Promise<void> {
  const choPhep = new Set<string>(Object.values(CAU_HINH_K))
  for (const khoa of Object.keys(capGiaTri)) {
    // Chốt an toàn: chỉ ghi khoá có trong whitelist CAU_HINH_K (thẻ nhóm) hoặc khoá sửa lẻ
    // của DANH_MUC_CAU_HINH (shared/utils/cauHinhDanhMuc.ts).
    if (!choPhep.has(khoa) && !khoaSuaLeDuoc(khoa)) throw new Error(`Khoá cấu hình không được phép: ${khoa}`)
  }
  await cauHinhTaoBangNeuThieu()
  await transaction(dbGame(), async (conn) => {
    for (const [khoa, giaTriMoi] of Object.entries(capGiaTri)) {
      await exec(
        conn,
        `INSERT INTO \`${BANG_CAU_HINH}\` (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, NOW())
         ON DUPLICATE KEY UPDATE gia_tri = VALUES(gia_tri), sua_boi = VALUES(sua_boi), sua_luc = VALUES(sua_luc)`,
        [khoa, String(giaTriMoi), adminTen.slice(0, 50)],
      )
    }
  })
}

/** Đọc toàn bộ bảng: khoá => giá trị, khoá => [sua_boi, sua_luc]. */
export async function cauHinhDocBang(): Promise<{ gt: Record<string, string>, suaBoi: Record<string, [string, string]> }> {
  await cauHinhTaoBangNeuThieu()
  const gt: Record<string, string> = {}
  const suaBoi: Record<string, [string, string]> = {}
  const rows = await q(dbGame(), `SELECT khoa, gia_tri, sua_boi, sua_luc FROM \`${BANG_CAU_HINH}\``)
  for (const r of rows) {
    gt[r.khoa] = String(r.gia_tri ?? '')
    suaBoi[r.khoa] = [String(r.sua_boi ?? ''), String(r.sua_luc ?? '')]
  }
  return { gt, suaBoi }
}

/** Giá trị hiện tại của các khoá (khoá chưa có -> null) — dùng làm "trước" cho lịch sử thay đổi. */
export async function cauHinhDocKhoa(khoas: string[]): Promise<Record<string, string | null>> {
  const ra: Record<string, string | null> = Object.fromEntries(khoas.map(k => [k, null]))
  if (!khoas.length) return ra
  await cauHinhTaoBangNeuThieu()
  const rows = await q(dbGame(), `SELECT khoa, gia_tri FROM \`${BANG_CAU_HINH}\` WHERE khoa IN (${khoas.map(() => '?').join(',')})`, khoas)
  for (const r of rows) ra[r.khoa] = String(r.gia_tri ?? '')
  return ra
}
