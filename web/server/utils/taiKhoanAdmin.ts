/**
 * Quản lý tài khoản trong khu quản trị: trang Tài khoản (khoá, kích hoạt, đổi mật
 * khẩu, reset, xoá một/nhiều tài khoản), Tạo hàng loạt và Cấm theo máy.
 *
 * KHOÁ TÀI KHOẢN nghĩa là đặt team_user.ban = 1. Đó chính là cột login server đọc
 * (`ban == 1 ? -2 : id`, mã -2 làm client hiện "Tài khoản của bạn hiện đang bị
 * khoá"). Khoá chỉ chặn LẦN ĐĂNG NHẬP SAU — người đang online vẫn chơi tiếp tới khi
 * thoát ra, vì web không nói chuyện được với tiến trình game đang chạy.
 *
 * XOÁ NHÂN VẬT không DELETE thẳng từ web: nhân vật có thể đang online (RAM của tiến
 * trình game), nên web chỉ chèn một dòng lệnh `xoa_nhan_vat` vào htbb_db.web_lenh_nhan_vat
 * (xem src/data/LenhWeb.java); server đọc mỗi giây, đuổi nhân vật ra (lưu bản cuối +
 * ngắt kết nối) rồi xoá thật cả tob_char lẫn dữ liệu con. Server đang tắt thì dòng lệnh
 * nằm chờ, không mất. Tài khoản chỉ bị xoá khi KHÔNG còn nhân vật nào.
 *
 * Ghi chú tên: Nitro tự import MỌI export trong server/utils, trùng tên là hỏng build,
 * nên mọi export ở đây đều mang tiền tố `tk`/`TK_`. Các hàm bảng lệnh nhân vật được
 * chép riêng (tkGuiLenhXoaNhanVat...) để không đụng file của khu Nhân vật.
 */
import type { Pool } from 'mysql2/promise'
import {
  validPassword, validUsername,
  USERNAME_MIN, USERNAME_MAX, PASSWORD_MIN, PASSWORD_MAX,
} from '../../shared/utils/htbb'

// ---------------------------------------------------------------------------
//  Danh sách / chi tiết
// ---------------------------------------------------------------------------

/** Số dòng mỗi trang của danh sách tài khoản. */
export const TK_MOI_TRANG = 30
/** Tìm kiếm chỉ trả về tối đa 50 dòng. */
export const TK_TIM_TOI_DA = 50

// ---------------------------------------------------------------------------
//  Xoá nhiều tài khoản
// ---------------------------------------------------------------------------

/** Chọn tối đa 50 tài khoản mỗi lần xoá. */
export const TK_XOA_NHIEU_TOI_DA = 50
/** Chuỗi phải gõ để xác nhận xoá nhiều tài khoản. */
export const TK_XAC_NHAN_XOA_NHIEU = 'XOA DA CHON'

/**
 * Kiểm và khử trùng danh sách ID đã chọn.
 * Ném Error có thông điệp tiếng Việt khi dữ liệu sai.
 * Chỉ nhận chuỗi chữ số dạng chuẩn (không "007", không "1 OR 1=1") hoặc số nguyên dương.
 */
export function tkIdsDaChon(raw: unknown): number[] {
  if (!Array.isArray(raw) || raw.length > TK_XOA_NHIEU_TOI_DA) {
    throw new Error(`Chọn từ 1 đến ${TK_XOA_NHIEU_TOI_DA} tài khoản mỗi lần.`)
  }
  const ids = new Set<number>()
  for (const v of raw) {
    const s = typeof v === 'number' ? (Number.isInteger(v) ? String(v) : '') : (typeof v === 'string' ? v : '')
    if (!/^\d+$/.test(s) || Number(s) <= 0 || String(Number(s)) !== s || !Number.isSafeInteger(Number(s))) {
      throw new Error('Danh sách tài khoản không hợp lệ.')
    }
    ids.add(Number(s))
  }
  if (ids.size === 0) throw new Error('Chưa chọn tài khoản nào.')
  return [...ids]
}

export interface TkKetQuaXoaNhieu {
  xoa: string[]
  cho: string[]
  bo_qua: string[]
  loi: string[]
}

/**
 * Mọi thao tác DB mà việc xoá nhiều tài khoản cần, gom về một chỗ để quy trình xoá
 * (tkXoaNhieuTaiKhoan) đọc như một kịch bản, không lẫn câu SQL.
 */
export interface TkKhoXoaTaiKhoan {
  timTaiKhoan(id: number): Promise<{ id: number, username: string } | null>
  dsNhanVat(userId: number): Promise<{ id: number, charname: string }[]>
  /** Đã có lệnh xoá đang chờ (trang_thai 0 hoặc 4) cho nhân vật này chưa. */
  coLenhXoaDangCho(charId: number): Promise<boolean>
  guiLenhXoa(charId: number, charname: string, admin: string): Promise<number>
  demNhanVat(userId: number): Promise<number>
  xoaKichHoat(username: string, userId: number): Promise<void>
  xoaTaiKhoan(userId: number, username: string): Promise<void>
  ghiNhatKy(hanhDong: string, doiTuong: string, chiTiet: string): Promise<void>
  laAdmin(username: string): boolean
}

/**
 * Xoá nhiều tài khoản theo cùng quy trình xoá nhân vật của server game.
 * KHÔNG chờ từng nhân vật (tránh treo request khi chọn nhiều tài khoản): tài khoản
 * còn nhân vật thì gửi lệnh rồi để "chờ"; bấm lại sau khi server xử lý xong sẽ hoàn tất.
 * Bấm lại không nhân đôi lệnh đang chờ.
 */
export async function tkXoaNhieuTaiKhoan(kho: TkKhoXoaTaiKhoan, ids: number[], admin: string): Promise<TkKetQuaXoaNhieu> {
  const kq: TkKetQuaXoaNhieu = { xoa: [], cho: [], bo_qua: [], loi: [] }
  for (const id of ids) {
    let ten = `#${id}`
    try {
      const tk = await kho.timTaiKhoan(id)
      if (!tk) {
        kq.bo_qua.push(`${ten} (không còn tồn tại)`)
        continue
      }
      ten = String(tk.username)
      if (kho.laAdmin(ten) || ten.toLowerCase() === admin.toLowerCase()) {
        kq.bo_qua.push(`${ten} (quản trị)`)
        continue
      }
      const nhanVat = await kho.dsNhanVat(id)
      for (const nv of nhanVat) {
        if (!(await kho.coLenhXoaDangCho(nv.id))) {
          await kho.guiLenhXoa(nv.id, nv.charname, admin)
        }
      }
      if ((await kho.demNhanVat(id)) > 0) {
        kq.cho.push(ten)
        await kho.ghiNhatKy('xoa_nhieu_tai_khoan_cho', ten, 'đã gửi lệnh xoá Pokémon; chưa xoá tài khoản')
        continue
      }
      await kho.xoaKichHoat(ten, id)
      await kho.xoaTaiKhoan(id, ten)
      kq.xoa.push(ten)
      await kho.ghiNhatKy('xoa_nhieu_tai_khoan', ten, 'đã xoá tài khoản sau khi kiểm tra không còn Pokémon')
    } catch {
      kq.loi.push(ten)
      try {
        await kho.ghiNhatKy('xoa_nhieu_tai_khoan_loi', ten, 'chưa hoàn tất xoá tài khoản')
      } catch { /* im lặng */ }
    }
  }
  return kq
}

/** Câu báo kết quả xoá nhiều; còn chờ/lỗi/bỏ qua thì là cảnh báo. */
export function tkMoTaKetQuaXoaNhieu(kq: TkKetQuaXoaNhieu): { loai: 'success' | 'warning', message: string } {
  let msg = `Đã xoá ${kq.xoa.length} tài khoản`
  if (kq.xoa.length) msg += `: ${kq.xoa.join(', ')}`
  msg += '.'
  if (kq.cho.length) {
    msg += ` Đang chờ xoá Pokémon: ${kq.cho.join(', ')}. Khi server xử lý xong, chọn lại các tài khoản này và bấm xoá để hoàn tất.`
  }
  if (kq.bo_qua.length) msg += ` Bỏ qua: ${kq.bo_qua.join(', ')}.`
  if (kq.loi.length) msg += ` Chưa hoàn tất do lỗi: ${kq.loi.join(', ')}. Có thể thử lại.`
  return { loai: (kq.cho.length || kq.loi.length || kq.bo_qua.length) ? 'warning' : 'success', message: msg }
}

/** Kho thật trên MySQL. */
export function tkKhoXoaMysql(ghi: (hanhDong: string, doiTuong: string, chiTiet: string) => Promise<void>): TkKhoXoaTaiKhoan {
  let daTaoBang = false
  return {
    timTaiKhoan: async id => q1<{ id: number, username: string }>(dbAccount(),
      'SELECT id, username FROM team_user WHERE id = ? LIMIT 1', [id]),
    dsNhanVat: async userId => q<{ id: number, charname: string }>(dbGame(),
      'SELECT id, charname FROM tob_char WHERE userId = ?', [userId]),
    coLenhXoaDangCho: async (charId) => {
      if (!daTaoBang) {
        await tkTaoBangLenhNhanVat(dbGame())
        daTaoBang = true
      }
      const v = await qv(dbGame(),
        'SELECT id FROM web_lenh_nhan_vat WHERE char_id = ? AND loai = ? AND trang_thai IN (0, 4) LIMIT 1',
        [charId, TK_LENH_LOAI_XOA_NHAN_VAT])
      return v !== null
    },
    guiLenhXoa: (charId, charname, admin) => tkGuiLenhXoaNhanVat(dbGame(), charId, charname, admin),
    demNhanVat: async userId => Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_char WHERE userId = ?', [userId]) ?? 0),
    xoaKichHoat: async (username, userId) => {
      await exec(dbGame(), 'DELETE FROM `5h_active` WHERE username = ? OR userID = ?', [username, userId])
    },
    xoaTaiKhoan: async (userId, username) => {
      await exec(dbAccount(), 'DELETE FROM team_user WHERE id = ? AND username = ?', [userId, username])
    },
    ghiNhatKy: ghi,
    laAdmin: laTaiKhoanAdmin,
  }
}

// ---------------------------------------------------------------------------
//  Bảng lệnh nhân vật (htbb_db.web_lenh_nhan_vat) — phần cần cho việc xoá
// ---------------------------------------------------------------------------

export const TK_LENH_LOAI_XOA_NHAN_VAT = 'xoa_nhan_vat'
const LENH_CHO = 0

/** Tên trạng thái lệnh để hiện cho người bấm (mã khớp hằng số trong LenhWeb.java). */
export function tkTenTrangThaiLenh(trangThai: number): string {
  switch (trangThai) {
    case 0: return 'chờ server'
    case 1: return 'đã xong (online)'
    case 2: return 'đã ghi CSDL (cách cũ)'
    case 3: return 'lỗi'
    case 4: return 'chờ vào game'
    case 5: return 'đã huỷ'
    default: return `trạng thái ${trangThai}`
  }
}

/**
 * Cùng định nghĩa với LenhWeb.taoBang() và tools/luu_tru_sinh_du_lieu/sql_nguon/them_bang_web_lenh_nhan_vat.sql. Chỉ
 * CREATE khi bảng chưa có: người dùng MySQL của web có thể không có quyền CREATE, lúc đó
 * bảng đã được server tạo sẵn thì vẫn phải chạy được. Bảng cũ thiếu cột tham_so thì thêm.
 */
export async function tkTaoBangLenhNhanVat(pool: Pool): Promise<void> {
  let co = false
  try {
    await pool.query('SELECT 1 FROM web_lenh_nhan_vat LIMIT 1')
    co = true
  } catch { co = false }
  if (co) {
    const cot = await q(pool, 'SHOW COLUMNS FROM `web_lenh_nhan_vat` LIKE \'tham_so\'')
    if (!cot.length) {
      await pool.query('ALTER TABLE `web_lenh_nhan_vat` ADD COLUMN `tham_so` TEXT NULL AFTER `phan_nghin`')
    }
    return
  }
  await pool.query(
    'CREATE TABLE IF NOT EXISTS `web_lenh_nhan_vat` ('
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
    + ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4',
  )
}

/** Chèn lệnh "xoá vĩnh viễn nhân vật". Trả về id dòng lệnh để đợi kết quả. */
export async function tkGuiLenhXoaNhanVat(pool: Pool, charId: number, charname: string, adminUser: string): Promise<number> {
  await tkTaoBangLenhNhanVat(pool)
  const r = await exec(pool,
    'INSERT INTO web_lenh_nhan_vat (loai, char_id, charname, admin_user) VALUES (?, ?, ?, ?)',
    [TK_LENH_LOAI_XOA_NHAN_VAT, charId, tkCatBot(charname, 50), tkCatBot(adminUser, 50)])
  return Number(r.insertId)
}

/**
 * Đợi server xử lý, tối đa `toiDaGiay` giây (hỏi lại mỗi 250 ms). Trả về dòng lệnh ở
 * trạng thái cuối cùng đọc được — vẫn có thể là "chờ" nếu server không trả lời kịp.
 */
export async function tkChoKetQuaLenh(pool: Pool, id: number, toiDaGiay = 4): Promise<{ id: number, trang_thai: number, ket_qua: string } | null> {
  const han = Date.now() + toiDaGiay * 1000
  let r: { id: number, trang_thai: number, ket_qua: string } | null = null
  do {
    r = await q1(pool, 'SELECT id, trang_thai, ket_qua FROM web_lenh_nhan_vat WHERE id = ? LIMIT 1', [id])
    if (r !== null && Number(r.trang_thai) !== LENH_CHO) return r
    await new Promise(res => setTimeout(res, 250))
  } while (Date.now() < han)
  return r
}

/**
 * Gửi lệnh xoá cho TỪNG nhân vật của tài khoản, đợi tối đa 2 giây/nhân vật để tổng thời
 * gian một yêu cầu web không quá lâu. Trả về số nhân vật ban đầu, số còn lại, và chi tiết
 * từng lệnh. Mọi bước đều idempotent: bấm lại sẽ tiếp tục.
 */
export async function tkXoaHetNhanVat(userId: number, adminName: string) {
  const dsNV = await q<{ id: number, charname: string }>(dbGame(), 'SELECT id, charname FROM tob_char WHERE userId = ?', [userId])
  const chiTietLenh: string[] = []
  for (const nv of dsNV) {
    const idLenh = await tkGuiLenhXoaNhanVat(dbGame(), Number(nv.id), String(nv.charname), adminName)
    const lenh = await tkChoKetQuaLenh(dbGame(), idLenh, 2)
    chiTietLenh.push(`${nv.charname}: ${tkTenTrangThaiLenh(Number(lenh?.trang_thai ?? -1))}`)
  }
  const soConLai = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_char WHERE userId = ?', [userId]) ?? 0)
  return { soBanDau: dsNV.length, soConLai, chiTietLenh }
}

// ---------------------------------------------------------------------------
//  Tạo hàng loạt
// ---------------------------------------------------------------------------

/** Số dòng tối đa một lượt nhập — chặn dán nhầm danh sách khổng lồ làm treo request. */
export const TK_TAO_HANG_LOAT_TOI_DA = 200

/** Sinh mật khẩu ngẫu nhiên khi người dùng để trống. Bỏ ký tự dễ nhầm (0/O, 1/I/l). */
export function tkTaoMatKhauNgauNhien(doDai = 10): string {
  const chars = 'abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789'
  const bytes = new Uint32Array(doDai)
  globalThis.crypto.getRandomValues(bytes)
  let mk = ''
  // Lấy mẫu bỏ phần dư (rejection sampling): `x % chars.length` trên toàn dải 32 bit sẽ
  // ưu tiên vài ký tự đầu; bỏ các giá trị rơi vào phần dư thì mọi ký tự đều đồng xác suất.
  const gioiHan = Math.floor(0x1_0000_0000 / chars.length) * chars.length
  for (let i = 0; i < doDai; i++) {
    let x = bytes[i]!
    while (x >= gioiHan) {
      const b = new Uint32Array(1)
      globalThis.crypto.getRandomValues(b)
      x = b[0]!
    }
    mk += chars[x % chars.length]
  }
  return mk
}

export interface TkHangTao {
  dong: number
  username: string
  matkhau: string
  trangthai: string
  loi: boolean
}

/** Tách ô nhập: mỗi dòng một tài khoản; bỏ dòng trống và dòng bắt đầu bằng '#'. */
export function tkTachDongTao(raw: string): string[] {
  return raw.trim().split(/\r\n|\r|\n/).map(d => d.trim()).filter(d => d !== '' && d[0] !== '#')
}

/**
 * Kiểm từng dòng (KHÔNG đụng DB): tên hợp lệ, trùng trong chính danh sách, mật khẩu.
 * Mỗi dòng xử lý ĐỘC LẬP — dòng hỏng chỉ bị bỏ qua. Dòng còn `loi === false` và
 * `trangthai === ''` là dòng sẵn sàng để kiểm trùng với CSDL rồi INSERT.
 */
export function tkChuanBiTao(dong: string[]): TkHangTao[] {
  const ra: TkHangTao[] = []
  const daGap = new Map<string, number>() // tên -> số dòng đầu tiên gặp
  dong.forEach((line, i) => {
    const soDong = i + 1
    const viTri = line.indexOf(':')
    const username = (viTri === -1 ? line : line.slice(0, viTri)).trim().toLowerCase()
    const matKhauNhap = viTri === -1 ? '' : line.slice(viTri + 1).trim()
    const hang: TkHangTao = { dong: soDong, username, matkhau: '', trangthai: '', loi: true }

    if (!validUsername(username)) {
      hang.trangthai = `Bỏ qua — tên phải ${USERNAME_MIN}-${USERNAME_MAX} ký tự, chỉ chữ thường và số`
      ra.push(hang)
      return
    }
    if (daGap.has(username)) {
      hang.trangthai = `Bỏ qua — trùng tên với dòng ${daGap.get(username)} ở trên`
      ra.push(hang)
      return
    }
    daGap.set(username, soDong)

    const matKhau = matKhauNhap !== '' ? matKhauNhap : tkTaoMatKhauNgauNhien()
    if (!validPassword(matKhau)) {
      hang.trangthai = `Bỏ qua — mật khẩu phải ${PASSWORD_MIN}-${PASSWORD_MAX} ký tự`
      ra.push(hang)
      return
    }
    hang.matkhau = matKhau
    hang.loi = false
    ra.push(hang)
  })
  return ra
}

/**
 * Câu INSERT GIỐNG HỆT đường đăng ký (register): PASSWORD() để khớp login server,
 * phone rỗng, ban=0, provider=0, fromgame=0. Tạo hàng loạt ghi email NULL (bổ sung
 * email hàng loạt không nằm trong phạm vi tính năng này).
 */
export const TK_SQL_INSERT_TEAM_USER
  = 'INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`) '
    + 'VALUES (?, PASSWORD(?), \'\', NOW(), 0, 0, 0, NULL)'

// ---------------------------------------------------------------------------
//  Cấm theo máy
// ---------------------------------------------------------------------------

/** Tên bảng — phải khớp hằng số trong src/data/CamTheoMay.java. */
export const TK_BANG_CAM_MAY = 'tob_cam_may'
export const TK_BANG_LICH_SU_MAY = 'tob_may_dang_nhap'

/** Mã máy hợp lệ hay không — cùng luật với CamTheoMay.lamSach() bên Java. */
export function tkMaMayHopLe(s: string): boolean {
  return s !== '' && Buffer.byteLength(s, 'utf8') <= 64 && /^[A-Za-z0-9_-]+$/.test(s)
}

/**
 * Cắt chuỗi về tối đa `soByte` byte UTF-8 mà không để lại ký tự dở dang (cột
 * VARCHAR(255) utf8mb4: 255 byte luôn ≤ 255 ký tự).
 */
export function tkCatBot(s: string, soByte: number): string {
  const buf = Buffer.from(s, 'utf8')
  if (buf.length <= soByte) return s
  let n = soByte
  // Lùi về đầu một ký tự: byte tiếp nối UTF-8 có dạng 10xxxxxx.
  while (n > 0 && (buf[n]! & 0xC0) === 0x80) n--
  return buf.subarray(0, n).toString('utf8')
}

/**
 * Máy này có tài khoản quản trị nào từng dùng không. Cấm nhầm một máy mà chính bạn
 * đang ngồi thì mất luôn đường vào game — và web không gỡ hộ kịp vì server chỉ đọc lại
 * danh sách sau 60 giây. Trả về tên admin đầu tiên tìm thấy, '' nếu không có.
 */
export async function tkAdminTungDungMay(maMay: string): Promise<string> {
  try {
    const ds = await q<{ username: string }>(dbGame(), `SELECT username FROM ${TK_BANG_LICH_SU_MAY} WHERE ma_may = ?`, [maMay])
    for (const r of ds) {
      if (laTaiKhoanAdmin(String(r.username))) return String(r.username)
    }
  } catch {
    // Bảng chưa có thì coi như không biết.
  }
  return ''
}

