/**
 * GỬI LỆNH CHO SERVER GAME qua bảng htbb_db.web_lenh_nhan_vat (xem src/data/LenhWeb.java).
 *
 * VÌ SAO KHÔNG GHI THẲNG tob_char NỮA. Nhân vật đang online giữ bản của nó trong RAM của
 * tiến trình game; lần tự lưu kế tiếp ghi đè lại dòng web vừa sửa — mất hẳn thay đổi.
 * Riêng cấp độ còn tệ hơn: lúc đăng nhập server đọc cấp/exp từ cột pInfo, KHÔNG đọc
 * lastLv/xp, nên ghi hai cột đó chưa bao giờ có tác dụng. Web không nhìn thấy RAM của
 * tiến trình game, nên thay vì đoán mò, web CHỈ chèn một dòng lệnh; server đọc mỗi giây.
 *
 * CÁC LOẠI LỆNH:
 *   dat_cap_do   : cap_do + phan_nghin. Online -> cộng exp qua đường lên cấp tự nhiên rồi
 *                  lưu ngay (1). Offline -> nằm chờ (4), server cộng ngay khi nhân vật vào
 *                  game. Chỉ NÂNG cấp; hạ cấp bị từ chối (3).
 *   giam_cap_do  : cap_do = số level cần giảm (trang Giảm Level).
 *   xoa_vat_pham : tham_so = "<cot>><mau>" (cột equip/inven/bag, mau = đúng chuỗi thô của
 *                  món đồ trong cột). Online -> tìm đúng món khớp từng ký tự trong RAM, gỡ
 *                  ra, lưu ngay (1). Offline -> server xoá mẩu khỏi cột trong CSDL, rồi để
 *                  dòng ở "chờ vào game" (4) để khi đăng nhập đối chiếu lại RAM một lần.
 *   xoa_nhan_vat : XOÁ VĨNH VIỄN (khác hẳn nút "Khoá" chỉ ẩn đi) — không DELETE thẳng từ web
 *                  vì nhân vật có thể đang online. Online -> server đuổi ra (lưu bản cuối +
 *                  ngắt kết nối) rồi xoá thật tob_char lẫn dữ liệu con; offline -> xoá ngay.
 *                  Luôn xong (1) hoặc lỗi (3) ở lần đọc đầu tiên.
 * choKetQuaLenh() đợi tối đa vài giây rồi đọc lại để báo cho người bấm. Server đang tắt thì
 * dòng lệnh vẫn nằm đó (trang_thai 0) và được xử lý khi server chạy lại — không mất lệnh.
 * Người quản trị huỷ được dòng còn chờ (trang_thai 5), server bỏ qua dòng đã huỷ.
 *
 * ĐƠN VỊ %: web nhập phần trăm (bước 0,1 — tối đa 99,9), bảng lưu PHẦN NGHÌN (0..999) — cùng
 * đơn vị với real.LevelDetail.percent bên Java, để server tính exp bằng đúng công thức của nó
 * (expMain[cap-1] + expRequire[cap] * phanNghin / 1000). Web KHÔNG tính exp nữa. Mọi chỗ hiện
 * % cho người dùng đều lấy từ phần nghìn đã lưu, để số báo ra đúng bằng số server dùng.
 *
 * Phần thuần (không đụng CSDL) nằm đầu file, phần đụng CSDL nằm cuối.
 */
import { q, q1, exec } from './db'
import { docChuoiVatPham } from './vatPham'

type Ex = Parameters<typeof q>[0]

export const LENH_BANG = 'web_lenh_nhan_vat'
export const LENH_LOAI_DAT_CAP_DO = 'dat_cap_do'
export const LENH_LOAI_GIAM_CAP_DO = 'giam_cap_do'
export const LENH_LOAI_XOA_VAT_PHAM = 'xoa_vat_pham'
export const LENH_LOAI_XOA_NHAN_VAT = 'xoa_nhan_vat'

export const LENH_CHO = 0
export const LENH_XONG_ONLINE = 1
/** Mã cũ, server không tạo mới nữa; giữ để đọc dòng cũ. */
export const LENH_XONG_OFFLINE = 2
export const LENH_LOI = 3
export const LENH_CHO_DANG_NHAP = 4
export const LENH_HUY = 5

/** Đợi server tối đa bấy nhiêu giây. Server đọc bảng mỗi 1 giây nên 4 giây là đủ. */
export const LENH_CHO_TOI_DA_GIAY = 4.0

/** Cột vật phẩm web được phép xoá — tên cột ghép thẳng vào SQL nên PHẢI qua danh sách này. */
export const LENH_COT_VAT_PHAM = ['equip', 'inven', 'bag'] as const
export type CotVatPham = typeof LENH_COT_VAT_PHAM[number]

export function laCotVatPham(cot: unknown): cot is CotVatPham {
  return typeof cot === 'string' && (LENH_COT_VAT_PHAM as readonly string[]).includes(cot)
}

export interface DongLenhNhanVat {
  id: number
  loai?: string
  char_id?: number
  charname?: string
  cap_do?: number
  phan_nghin?: number
  tham_so?: string | null
  admin_user?: string
  trang_thai: number
  ket_qua: string
  tao_luc?: string
  xu_ly_luc?: string | null
}

/** Làm tròn nửa ra xa số 0 (Math.round của JS làm tròn -0,5 lên thành 0, không dùng được). */
function lamTronXaKhong(x: number): number {
  return x < 0 ? -Math.round(-x) : Math.round(x)
}

/** % (0..99.9) -> phần nghìn (0..999), làm tròn, kẹp vào biên. */
export function phanTramRaPhanNghin(phanTram: number): number {
  // toFixed(9) khử sai số dấu phẩy động trước khi làm tròn: 0.15*10 = 1.4999999… sẽ
  // bị làm tròn thành 1 (0,1%) thay vì 2 (0,2%) như người bấm gõ.
  const pn = Math.trunc(lamTronXaKhong(Number((phanTram * 10).toFixed(9))))
  return Math.max(0, Math.min(999, pn))
}

/** Phần nghìn -> chuỗi % gọn ("12,5%", "0%") để hiện cho người dùng. */
export function phanNghinRaChuoi(phanNghin: number): string {
  const pn = Math.max(0, Math.min(999, Math.trunc(phanNghin)))
  if (pn % 10 === 0) return `${Math.trunc(pn / 10)}%`
  return `${String(pn / 10).replace('.', ',')}%`
}

/** Tên trạng thái để hiện trong bảng. */
export function tenTrangThaiLenh(trangThai: number): string {
  switch (trangThai) {
    case LENH_CHO: return 'chờ server'
    case LENH_XONG_ONLINE: return 'đã xong (online)'
    case LENH_XONG_OFFLINE: return 'đã ghi CSDL (cách cũ)'
    case LENH_LOI: return 'lỗi'
    case LENH_CHO_DANG_NHAP: return 'chờ vào game'
    case LENH_HUY: return 'đã huỷ'
    default: return `trạng thái ${trangThai}`
  }
}

/** Màu gợi ý cho giao diện: xanh = xong, vàng = đang chờ, đỏ = lỗi. */
export function mauTrangThaiLenh(trangThai: number): 'success' | 'warning' | 'error' | 'neutral' {
  switch (trangThai) {
    case LENH_XONG_ONLINE:
    case LENH_XONG_OFFLINE: return 'success'
    case LENH_CHO:
    case LENH_CHO_DANG_NHAP: return 'warning'
    case LENH_LOI: return 'error'
    default: return 'neutral'
  }
}

/** Ghép tham số cho lệnh xoá vật phẩm. ">" không bao giờ nằm trong một mẩu (nó là dấu ngăn). */
export function ghepThamSoXoaVatPham(cot: string, mau: string): string {
  return `${cot}>${mau}`
}

/** Tách tham số "<cot>><mau>" -> [cot, mau]; sai định dạng -> null. */
export function tachThamSoXoaVatPham(thamSo: string | null | undefined): [CotVatPham, string] | null {
  const s = String(thamSo ?? '')
  const vt = s.indexOf('>')
  if (vt === -1) return null
  const cot = s.slice(0, vt)
  const mau = s.slice(vt + 1)
  if (!laCotVatPham(cot) || mau === '') return null
  return [cot, mau]
}

/** Mô tả ngắn một dòng lệnh cho bảng "Lệnh đã gửi": "cấp 50 (12,5%)" / "xoá #123 +5 (inven)". */
export function moTaYeuCauLenh(l: { loai?: string, cap_do?: number, phan_nghin?: number, tham_so?: string | null }): string {
  const loai = String(l.loai ?? '')
  if (loai === LENH_LOAI_GIAM_CAP_DO) return `giảm ${Math.trunc(Number(l.cap_do ?? 0))} level`
  if (loai === LENH_LOAI_XOA_NHAN_VAT) return 'xoá vĩnh viễn Pokémon'
  if (loai === LENH_LOAI_XOA_VAT_PHAM) {
    const ts = tachThamSoXoaVatPham(l.tham_so ?? '')
    if (ts === null) return 'xoá vật phẩm (tham số hỏng)'
    let moTa = 'xoá vật phẩm'
    const vp = docChuoiVatPham(ts[1])[0]
    if (vp) moTa = `xoá #${vp.template}${vp.plus > 0 ? ` +${vp.plus}` : ''}`
    return `${moTa} (${ts[0]})`
  }
  const pn = Math.trunc(Number(l.phan_nghin ?? 0))
  return `cấp ${Math.trunc(Number(l.cap_do ?? 0))}${pn > 0 ? ` (${phanNghinRaChuoi(pn)})` : ''}`
}

/** Viết hoa chữ cái đầu (chỉ khi là chữ ASCII thường; chữ có dấu giữ nguyên). */
function ucfirst(s: string): string {
  return s.length > 0 && /[a-z]/.test(s[0]!) ? s[0]!.toUpperCase() + s.slice(1) : s
}

export type LoaiBao = 'success' | 'warning' | 'error'

/**
 * Câu báo cho người bấm, kèm loại thông báo ('success' / 'warning' / 'error').
 * muc = mô tả việc vừa yêu cầu, vd 'cấp 50 (12,5%)' hay 'xoá "Hoả kỳ lân +3"'.
 * Thuần tuý, không đụng CSDL — để kiểm thử được không cần MySQL.
 */
export function moTaKetQuaLenh(r: DongLenhNhanVat | null, ten: string, muc: string, loai = ''): [LoaiBao, string] {
  if (r === null) {
    return ['error', `Không đọc lại được lệnh vừa gửi cho ${ten}. Kiểm tra bảng web_lenh_nhan_vat.`]
  }
  const kq = String(r.ket_qua ?? '').trim()
  switch (Math.trunc(Number(r.trang_thai))) {
    case LENH_XONG_ONLINE:
      // xoa_nhan_vat luôn xong ở trạng thái này dù nhân vật đang online hay không (xem
      // LenhWeb.xuLyXoaNhanVat) — câu "đang online" chung sẽ sai nếu họ đang offline.
      if (loai === LENH_LOAI_XOA_NHAN_VAT) {
        return ['success', `Đã ${muc} cho ${ten}.${kq !== '' ? ` (${kq})` : ''}`]
      }
      return ['success', `Đã thực hiện NGAY cho ${ten} đang online: ${muc}${kq !== '' ? ` (${kq})` : ''}. Server đã lưu lại, không bị ghi đè nữa.`]
    case LENH_CHO_DANG_NHAP:
      return ['success', `${ten} đang offline. ${kq !== '' ? `${ucfirst(kq)}. ` : ''}`
        + `Lệnh ${muc} sẽ được server đối chiếu/thực hiện NGAY KHI Pokémon vào game. `
        + 'Không cần bấm lại — có thể huỷ ở bảng "Lệnh đã gửi" bên dưới nếu đổi ý.']
    case LENH_XONG_OFFLINE:
      return ['success', `${ten} đang offline, đã ghi ${muc} vào CSDL (cách cũ).`]
    case LENH_LOI:
      return ['error', `Server từ chối lệnh ${muc} cho ${ten}: ${kq !== '' ? kq : 'không rõ lý do'}.`]
    case LENH_HUY:
      return ['warning', `Lệnh ${muc} cho ${ten} đã bị huỷ.`]
    default:
      return ['warning', `Đã gửi lệnh ${muc} cho ${ten} (lệnh #${Math.trunc(Number(r.id))}) nhưng server game chưa trả lời sau ${LENH_CHO_TOI_DA_GIAY} giây — có thể server đang tắt. `
        + 'Lệnh vẫn nằm chờ và sẽ được thực hiện ngay khi server chạy lại; KHÔNG cần bấm lại. '
        + 'Theo dõi hoặc huỷ ở bảng "Lệnh đã gửi" bên dưới.']
  }
}

/** Lỗi nhập liệu của trang Giảm Level (để API trả 400 thay vì 500). */
export class LoiNhapGiamLevel extends Error {}

/** Chữ thường chỉ cho chữ ASCII, giữ nguyên chữ có dấu — dùng làm khoá so trùng tên. */
function thuongAscii(s: string): string {
  return s.replace(/[A-Z]/g, c => c.toLowerCase())
}

/** Mỗi dòng một tên, hoặc ngăn bằng dấu phẩy / chấm phẩy; tên trùng (không phân biệt hoa thường) chỉ giữ một. */
export function tachTenGiamLevel(text: string): string[] {
  // Map: thứ tự theo lần gặp ĐẦU của mỗi tên, giá trị là cách viết của lần gặp CUỐI.
  // (Tra tob_char không phân biệt hoa thường nên cách viết nào cũng ra cùng nhân vật.)
  const names = new Map<string, string>()
  for (const entry of text.split(/[,;\r\n]+/)) {
    const name = entry.trim()
    if (name === '') continue
    // Giới hạn 50 tính theo BYTE UTF-8 (tên tiếng Việt có dấu tốn 2–3 byte mỗi chữ).
    if (new TextEncoder().encode(name).length > 50) throw new LoiNhapGiamLevel('Tên Pokémon quá dài.')
    names.set(thuongAscii(name), name)
  }
  if (names.size === 0 || names.size > 100) throw new LoiNhapGiamLevel('Nhập từ 1 đến 100 tên Pokémon.')
  return [...names.values()]
}

// ---------------------------------------------------------------------------
//  Phần đụng CSDL
// ---------------------------------------------------------------------------

let coBangLenhDaBiet = false

/** Bảng lệnh đã có chưa? Có rồi thì nhớ luôn (bảng không tự mất). */
export async function coBangLenh(db: Ex): Promise<boolean> {
  if (coBangLenhDaBiet) return true
  try {
    await db.query('SELECT 1 FROM web_lenh_nhan_vat LIMIT 1')
    coBangLenhDaBiet = true
  } catch {
    return false
  }
  return true
}

let daKiemCotThamSo = false

/** Bảng tạo ở bản đầu chưa có cột tham_so -> thêm. Chỉ chạy ALTER khi thật sự thiếu. */
export async function damBaoCotThamSo(db: Ex): Promise<void> {
  if (daKiemCotThamSo) return
  daKiemCotThamSo = true
  const co = await q1(db, `SHOW COLUMNS FROM \`${LENH_BANG}\` LIKE 'tham_so'`)
  if (!co) {
    await exec(db, `ALTER TABLE \`${LENH_BANG}\` ADD COLUMN \`tham_so\` TEXT NULL AFTER \`phan_nghin\``)
  }
}

/**
 * Cùng định nghĩa với LenhWeb.taoBang() và du_lieu/sql_nguon/them_bang_web_lenh_nhan_vat.sql. Chỉ CREATE khi
 * bảng chưa có: người dùng MySQL của web có thể không có quyền CREATE, lúc đó bảng đã được
 * server tạo sẵn thì vẫn phải chạy được. Bảng có từ bản đầu (chưa có cột tham_so) thì thêm
 * cột — server cũng tự thêm lúc khởi động, ai chạy trước cũng được.
 */
export async function taoBangLenhNhanVat(db: Ex): Promise<void> {
  if (await coBangLenh(db)) {
    await damBaoCotThamSo(db)
    return
  }
  await exec(db,
    `CREATE TABLE IF NOT EXISTS \`${LENH_BANG}\` (`
    + '`id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,'
    + '`loai`       VARCHAR(30)  NOT NULL,'
    + '`char_id`    INT          NOT NULL,'
    + '`charname`   VARCHAR(50)  NOT NULL DEFAULT \'\','
    + '`cap_do`     INT          NOT NULL DEFAULT 0,'
    + '`phan_nghin` INT          NOT NULL DEFAULT 0,'
    + '`tham_so`    TEXT         NULL,'
    + '`admin_user` VARCHAR(50)  NOT NULL DEFAULT \'\','
    + '`trang_thai` TINYINT      NOT NULL DEFAULT 0,'
    + '`ket_qua`    VARCHAR(255) NOT NULL DEFAULT \'\','
    + '`tao_luc`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,'
    + '`xu_ly_luc`  DATETIME     NULL,'
    + 'PRIMARY KEY (`id`),'
    + 'KEY `idx_trang_thai` (`trang_thai`)'
    + ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4')
  coBangLenhDaBiet = true
  daKiemCotThamSo = true
}

/** Cắt chuỗi cho vừa cột VARCHAR(n) (n tính theo KÝ TỰ ở utf8mb4). */
export function catLenh(s: string, n: number): string {
  return [...String(s)].slice(0, n).join('')
}

/** Chèn lệnh "đặt cấp độ". Trả về id dòng lệnh để đợi kết quả. */
export async function guiLenhCapDo(db: Ex, charId: number, charname: string, capDo: number, phanNghin: number, adminUser: string): Promise<number> {
  await taoBangLenhNhanVat(db)
  const res = await exec(db,
    `INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, cap_do, phan_nghin, admin_user)
     VALUES (?, ?, ?, ?, ?, ?)`,
    [LENH_LOAI_DAT_CAP_DO, charId, catLenh(charname, 50), capDo, Math.max(0, Math.min(999, Math.trunc(phanNghin))), catLenh(adminUser, 50)])
  return Number(res.insertId)
}

/** Chèn lệnh "xoá vật phẩm" (mau = chuỗi thô đúng của món đồ trong cột). Trả về id dòng lệnh. */
export async function guiLenhXoaVatPham(db: Ex, charId: number, charname: string, cot: string, mau: string, adminUser: string): Promise<number> {
  if (!laCotVatPham(cot)) throw new Error('Cột vật phẩm không hợp lệ.')
  if (mau.trim() === '' || mau.includes('>')) throw new Error('Chuỗi vật phẩm không hợp lệ.')
  await taoBangLenhNhanVat(db)
  const res = await exec(db,
    `INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, tham_so, admin_user)
     VALUES (?, ?, ?, ?, ?)`,
    [LENH_LOAI_XOA_VAT_PHAM, charId, catLenh(charname, 50), ghepThamSoXoaVatPham(cot, mau), catLenh(adminUser, 50)])
  return Number(res.insertId)
}

/** Chèn lệnh "xoá vĩnh viễn nhân vật". Trả về id dòng lệnh để đợi kết quả. */
export async function guiLenhXoaNhanVat(db: Ex, charId: number, charname: string, adminUser: string): Promise<number> {
  await taoBangLenhNhanVat(db)
  const res = await exec(db,
    `INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, admin_user)
     VALUES (?, ?, ?, ?)`,
    [LENH_LOAI_XOA_NHAN_VAT, charId, catLenh(charname, 50), catLenh(adminUser, 50)])
  return Number(res.insertId)
}

/** Một dòng lệnh (id, trang_thai, ket_qua), hoặc null nếu không có. */
export async function docLenh(db: Ex, id: number): Promise<DongLenhNhanVat | null> {
  return q1<DongLenhNhanVat>(db, 'SELECT id, trang_thai, ket_qua FROM web_lenh_nhan_vat WHERE id = ? LIMIT 1', [id])
}

/**
 * Đợi server xử lý, tối đa toiDaGiay giây (hỏi lại mỗi 250 ms). Trả về dòng lệnh ở trạng
 * thái cuối cùng đọc được — vẫn có thể là LENH_CHO nếu server không trả lời kịp.
 */
export async function choKetQuaLenh(db: Ex, id: number, toiDaGiay = LENH_CHO_TOI_DA_GIAY): Promise<DongLenhNhanVat | null> {
  const han = Date.now() + toiDaGiay * 1000
  let r: DongLenhNhanVat | null = null
  do {
    r = await docLenh(db, id)
    if (r !== null && Number(r.trang_thai) !== LENH_CHO) return r
    await new Promise(ok => setTimeout(ok, 250))
  } while (Date.now() < han)
  return r
}

/** Các lệnh gần đây của một nhân vật, mới nhất trước. Bảng chưa có thì trả về rỗng. */
export async function docLenhCuaNhanVat(db: Ex, charId: number, toiDa = 10): Promise<DongLenhNhanVat[]> {
  if (!(await coBangLenh(db))) return []
  await damBaoCotThamSo(db)
  const gioiHan = Math.max(1, Math.trunc(toiDa))
  return q<DongLenhNhanVat>(db,
    `SELECT id, loai, cap_do, phan_nghin, tham_so, admin_user, trang_thai, ket_qua, tao_luc, xu_ly_luc
       FROM web_lenh_nhan_vat WHERE char_id = ? ORDER BY id DESC LIMIT ${gioiHan}`,
    [charId])
}

/**
 * Huỷ một dòng còn chờ (LENH_CHO / LENH_CHO_DANG_NHAP). Trả về true nếu huỷ được; false nếu
 * server đã xử lý xong trước đó (không huỷ được kết quả đã có).
 *
 * KHÔNG BAO GIỜ huỷ được lệnh xoa_nhan_vat (xem WHERE bên dưới). Lý do: đọc dòng còn chờ ở
 * Java (LenhWeb.xuLyMotVong) và UPDATE huỷ này chạy ở HAI TIẾN TRÌNH khác nhau, không khoá gì
 * cả — Java có thể đã đọc dòng (trang_thai vẫn là CHỜ trong bản chụp của nó) và đang XOÁ THẬT
 * giữa lúc admin bấm Huỷ. Với dat_cap_do/xoa_vat_pham, "huỷ trớ" như vậy chỉ là một lần lên
 * cấp/mất đồ đáng tiếc nhưng còn sửa được; với xoa_nhan_vat thì không — "huỷ" một lệnh ĐÃ XOÁ
 * XONG sẽ hiện "đã huỷ" trong khi nhân vật thật ra đã biến mất vĩnh viễn. Server luôn xong
 * lệnh xoa_nhan_vat ngay ở vòng đọc đầu tiên nên "huỷ" không có tình huống hợp lệ nào.
 */
export async function huyLenh(
  db: Ex,
  id: number,
  adminUser: string,
  gioiHan: { charId?: number, loai?: string } = {},
): Promise<boolean> {
  // gioiHan: mỗi trang chỉ huỷ được lệnh của chính nó (trang nhân vật -> đúng
  // nhân vật đó; trang Giảm/Nâng level -> đúng loại lệnh đó), để nhật ký không
  // ghi nhầm tên và không ai huỷ hộ lệnh của trang khác bằng một id tự gõ.
  let them = ''
  const ts: unknown[] = [LENH_HUY, catLenh(`huy boi ${adminUser}`, 255), id, LENH_CHO, LENH_CHO_DANG_NHAP, LENH_LOAI_XOA_NHAN_VAT]
  if (gioiHan.charId !== undefined) {
    them += ' AND char_id = ?'
    ts.push(gioiHan.charId)
  }
  if (gioiHan.loai !== undefined) {
    them += ' AND loai = ?'
    ts.push(gioiHan.loai)
  }
  const res = await exec(db,
    `UPDATE web_lenh_nhan_vat SET trang_thai = ?, ket_qua = ?, xu_ly_luc = NOW()
      WHERE id = ? AND trang_thai IN (?, ?) AND loai != ?${them}`,
    ts)
  return res.affectedRows > 0
}

/** Dòng lệnh đã chuẩn bị sẵn cho giao diện (mô tả, tên trạng thái, có huỷ được không). */
export function lenhChoGiaoDien(l: DongLenhNhanVat) {
  const tt = Math.trunc(Number(l.trang_thai))
  // tham_so (mẩu vật phẩm thô, có thể dài vài KB) không gửi xuống trình duyệt — mo_ta đã đủ.
  const { tham_so: _boQua, ...gon } = l
  return {
    ...gon,
    trang_thai: tt,
    mo_ta: moTaYeuCauLenh(l),
    ten_trang_thai: tenTrangThaiLenh(tt),
    mau: mauTrangThaiLenh(tt),
    // xoa_nhan_vat không bao giờ huỷ được (xem huyLenh) — không hiện nút để khỏi tưởng nhầm.
    huy_duoc: (tt === LENH_CHO || tt === LENH_CHO_DANG_NHAP) && String(l.loai ?? '') !== LENH_LOAI_XOA_NHAN_VAT,
  }
}
