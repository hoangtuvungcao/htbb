/**
 * Đăng ký tài khoản — quy tắc nhập liệu và các câu SQL dùng chung.
 */
import type { H3Event } from 'h3'
import { QUY_TAC_DANG_KY, dangKyLoi, dangKyThongBao, dangKyVuotHanMuc } from '../../shared/utils/quyTacDangKy'

/**
 * Hạn mức tạo tài khoản theo IP — lấy từ shared/data/quy-tac-dang-ky.json (hanMuc), dùng CHUNG với
 * đăng ký trong game (HTBB/src/real/cmd/DangKyTrongGame.java). Người thật hiếm khi cần quá vài cái
 * (bạn bè chung nhà, chung quán net), còn script thì tạo hàng nghìn.
 *
 * Đếm GỘP cả hai đường: hành động 'dang_ky' (web) và 'dang_ky_game' (game ghi vào cùng bảng
 * web_chan_spam) — không thì kẻ phá được gấp đôi hạn mức bằng cách dùng cả hai.
 */
export const DANG_KY_TOI_DA = QUY_TAC_DANG_KY.hanMuc.moiGio
export const DANG_KY_TRONG_GIAY = 3600
export const DANG_KY_TOI_DA_NGAY = QUY_TAC_DANG_KY.hanMuc.moiNgay
export const DANG_KY_HANH_DONG = ['dang_ky', 'dang_ky_game'] as const

/**
 * true/ mã lỗi = ĐANG BỊ CHẶN. Giữ chỗ (giuCho) trước mọi await như biChanSpam, rồi đếm số tài
 * khoản IP này đã tạo trong 1 giờ / 24 giờ qua ở cả web lẫn game. Lỗi DB thì cho qua (như biChanSpam).
 */
export async function dangKyBiChan(event: H3Event): Promise<'quaGio' | 'quaNgay' | null> {
  const ip = ipKhach(event)
  const dangChay = giuCho(`dang_ky|${ip}`)
  const bo = demBoNho(`dang_ky|${ip}`)
  let gio = bo.gio
  let ngay = bo.ngay
  try {
    if (await coBang(dbGame(), 'web_chan_spam')) {
      const dem = async (giay: number) => Number(await qv<number>(
        dbGame(),
        `SELECT COUNT(*) FROM web_chan_spam
          WHERE ip = ? AND hanh_dong IN ('dang_ky', 'dang_ky_game')
            AND thoi_gian > DATE_SUB(NOW(), INTERVAL ${Math.trunc(giay)} SECOND)`,
        [ip],
      ) ?? 0)
      gio = await dem(DANG_KY_TRONG_GIAY)
      ngay = await dem(86400)
    }
  } catch {
    // Lỗi DB: vẫn còn chặn theo số lượt đang chạy / bộ nhớ
  }
  return dangKyVuotHanMuc(gio, ngay, dangChay)
}

/** Câu báo khi vượt hạn mức (giờ / ngày). */
export function dangKyLoiHanMuc(ma: 'quaGio' | 'quaNgay'): string {
  return dangKyThongBao(ma)
}

/**
 * Câu INSERT tạo tài khoản. Lưu mật khẩu bằng PASSWORD() để KHỚP với login server
 * của game. Trang Tạo hàng loạt của admin PHẢI dùng đúng 7 cột đầu y hệt câu này
 * (chỉ cột email được khác: ở đây là tham số ?, tạo hàng loạt ghi NULL) — hai nơi
 * tạo tài khoản không được lệch cột. Đăng ký trong game (HTBB real.cmd.DangKyTrongGame) dùng
 * đúng câu này. `fromgame` PHẢI là 0: CheckLoginSocket.jar trả cột này về làm "region" và server game
 * chỉ cho vào khi region là 0/9 — khác đi là tài khoản không bao giờ đăng nhập được.
 */
export const DANG_KY_INSERT_SQL
  = 'INSERT INTO team_user (`username`, `password`, `phone`, `regdate`, `ban`, `provider`, `fromgame`, `email`)'
    + ' VALUES (?, PASSWORD(?), \'\', NOW(), 0, 0, 0, ?)'

export const DANG_KY_TRUNG_TEN_SQL = 'SELECT 1 FROM team_user WHERE username = ? LIMIT 1'

// Kiểm tra email (dangKyEmailHopLe) nằm ở shared/utils/htbb.ts để form đăng ký
// kiểm tra trước đúng như server.

export interface DangKyInput {
  username: string
  password: string
  confirm: string
  email: string
}

/**
 * Chuẩn hoá dữ liệu form: username trim + chữ thường (login server chỉ nhận chữ
 * thường), email trim. Trường không phải chuỗi coi như rỗng.
 */
export function dangKyChuanHoa(body: Record<string, unknown> | null | undefined): DangKyInput {
  const s = (v: unknown) => (typeof v === 'string' ? v : '')
  return {
    username: s(body?.username).trim().toLowerCase(),
    password: s(body?.password),
    confirm: s(body?.confirm),
    email: s(body?.email).trim(),
  }
}

/**
 * Mọi lỗi nhập liệu, theo thứ tự các ô trên form. Rỗng = hợp lệ. Quy tắc (độ dài, ký tự, tên quản
 * trị cấm, từ ngữ cấm, email) ở shared/data/quy-tac-dang-ky.json — chung với đăng ký trong game.
 * Login server của game chỉ nhận mật khẩu chữ cái không dấu + số (CreateCharHandler.checkInfoLogin)
 * và bỏ qua "000000": đăng ký mật khẩu khác thì tài khoản tạo được nhưng không bao giờ vào được game.
 * Email bắt buộc trên web: là đường duy nhất để lấy lại mật khẩu (trang Quên mật khẩu).
 */
export function dangKyKiemTra(d: DangKyInput): string[] {
  return dangKyLoi(d, 'web').map(l => l.loi)
}
