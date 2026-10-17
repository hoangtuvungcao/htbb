/**
 * MẬT KHẨU V2 (scrypt) — CHƯA NỐI VÀO LUỒNG ĐĂNG NHẬP. Mặc định TẮT (KPAH_MAT_KHAU_V2 trống).
 * Hiện tại web vẫn so/lưu bằng MySQL PASSWORD() ở cột team_user.password, y hệt login server.
 *
 * ── VÌ SAO CHƯA ĐỔI ──────────────────────────────────────────────────────────────────────────
 * Login server (CheckLoginSocket.jar, chỉ có bản jar) chạy
 *     SELECT * FROM team_user WHERE username = ? AND password = PASSWORD(?)
 * nên cột `password` BẮT BUỘC giữ hash PASSWORD() (SHA1(SHA1(pw)), không salt — dò bằng bảng
 * tra sẵn rất nhanh nếu lộ DB). Chừng nào jar còn cần cột này, hash yếu vẫn nằm trong DB và
 * thêm cột mạnh hơn KHÔNG làm DB an toàn hơn. Lợi ích thật chỉ đến ở giai đoạn 3 dưới đây.
 *
 * ── KẾ HOẠCH HAI CỘT ─────────────────────────────────────────────────────────────────────────
 * Giai đoạn 1 (làm được ngay, không ảnh hưởng game):
 *   ALTER TABLE account.team_user ADD COLUMN password_v2 VARCHAR(255) NULL;
 *   Bật KPAH_MAT_KHAU_V2=1 và nối: đăng ký / đổi mật khẩu / đặt lại mật khẩu ghi CẢ HAI cột
 *   (`password` = PASSWORD(?) như cũ, `password_v2` = bamMatKhauV2(pw)); đăng nhập web đúng
 *   PASSWORD() mà password_v2 còn NULL -> ghi bù password_v2 (nâng cấp dần).
 * Giai đoạn 2: khi phần lớn tài khoản đã có password_v2, web chỉ kiểm password_v2.
 * Giai đoạn 3 (cần thay login server, hoặc một lớp proxy đăng nhập tự viết đứng trước game
 *   server): login mới kiểm password_v2; sau đó đặt `password` = chuỗi ngẫu nhiên/không khớp
 *   cho mọi tài khoản -> hash yếu biến mất khỏi DB. Đây là bước duy nhất thật sự tăng an toàn.
 * Lưu ý: token đăng nhập (dauMatKhau) đang gắn với cột `password`; giai đoạn 3 phải chuyển
 *   sang gắn với password_v2 (mọi phiên cũ sẽ bị đăng xuất một lần — chấp nhận được).
 *
 * Dùng scrypt có sẵn trong node:crypto (không thêm thư viện native như argon2/bcrypt, để build
 * và deploy không đổi). Định dạng: scrypt$N$r$p$saltB64$hashB64.
 */
import { randomBytes, scrypt as scryptCb, timingSafeEqual } from 'node:crypto'

const N = 16384
const R = 8
const P = 1
const DO_DAI = 32

function scryptAsync(pw: string, salt: Buffer, n: number, r: number, p: number, len: number): Promise<Buffer> {
  return new Promise((ok, loi) => {
    scryptCb(pw, salt, len, { N: n, r, p, maxmem: 64 * 1024 * 1024 }, (e, k) => (e ? loi(e) : ok(k)))
  })
}

/** Bật lưu/kiểm mật khẩu v2 hay chưa (KPAH_MAT_KHAU_V2=1). Mặc định false. */
export function matKhauV2Bat(): boolean {
  return ['1', 'true', 'yes', 'on'].includes((process.env.KPAH_MAT_KHAU_V2 ?? '').trim().toLowerCase())
}

export async function bamMatKhauV2(matKhau: string): Promise<string> {
  const salt = randomBytes(16)
  const k = await scryptAsync(matKhau, salt, N, R, P, DO_DAI)
  return `scrypt$${N}$${R}$${P}$${salt.toString('base64')}$${k.toString('base64')}`
}

/** So mật khẩu với chuỗi do bamMatKhauV2 sinh. Chuỗi hỏng/lạ -> false (không ném). */
export async function kiemMatKhauV2(matKhau: string, chuoi: string | null | undefined): Promise<boolean> {
  const m = String(chuoi ?? '').match(/^scrypt\$(\d+)\$(\d+)\$(\d+)\$([A-Za-z0-9+/=]+)\$([A-Za-z0-9+/=]+)$/)
  if (!m) return false
  const [n, r, p] = [Number(m[1]), Number(m[2]), Number(m[3])]
  if (n < 1024 || n > 1 << 20 || r < 1 || r > 32 || p < 1 || p > 16) return false
  const muc = Buffer.from(m[5]!, 'base64')
  if (muc.length < 16 || muc.length > 64) return false
  try {
    const k = await scryptAsync(matKhau, Buffer.from(m[4]!, 'base64'), n, r, p, muc.length)
    return timingSafeEqual(k, muc)
  } catch {
    return false
  }
}
