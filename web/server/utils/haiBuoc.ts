/**
 * ============================================================================
 *  XÁC THỰC HAI BƯỚC (TOTP) CHO TRANG QUẢN TRỊ
 * ============================================================================
 *
 *  Cài đặt theo RFC 6238 (TOTP) + RFC 4226 (HOTP), thuật toán SHA1, 6 chữ số,
 *  bước 30 giây — đúng mặc định của Google Authenticator / Authy / 2FAS.
 *  Không dùng thư viện ngoài (kể cả bcrypt — xem phần MÃ DỰ PHÒNG bên dưới).
 *
 *  Phần thuần (TOTP, Base32, bcrypt) CHỈ import 'node:crypto', không dùng
 *  auto-import của Nuxt, để lệnh ghi danh scripts/tao-ma-2buoc.mjs import thẳng
 *  được bằng Node. Phần LƯU TRỮ ở cuối tệp dùng dbGame()/q1/exec auto-import —
 *  chỉ gọi được bên trong server Nitro.
 *
 *  ------------------------------------------------------------------------
 *   VÌ SAO GHI DANH PHẢI LÀM QUA SSH, KHÔNG PHẢI TRÊN WEB
 *  ------------------------------------------------------------------------
 *  Nếu để người đã gõ đúng mật khẩu tự ghi danh trên web, thì kẻ trộm được mật
 *  khẩu cũng tự ghi danh được — nó quét mã QR bằng điện thoại CỦA NÓ rồi vào
 *  bình thường. Lúc đó 2FA không chặn ai cả, chỉ thêm một bước cho chủ nhà. Cái
 *  duy nhất làm 2FA có nghĩa là **khoá bí mật không bao giờ đi qua đường mà kẻ
 *  tấn công đang đứng**. Nên khoá được sinh bằng lệnh chạy trên máy chủ qua SSH
 *  (`node scripts/tao-ma-2buoc.mjs <tên>`), và web KHÔNG có đường nào tạo hay
 *  đổi khoá. Đừng bao giờ thêm API gọi hbSinhKhoa().
 *
 *  ------------------------------------------------------------------------
 *   HAI MỨC BẮT BUỘC
 *  ------------------------------------------------------------------------
 *   - Tài khoản ĐÃ ghi danh: luôn bị hỏi mã, không có cách bỏ qua.
 *   - Tài khoản CHƯA ghi danh: mặc định vẫn vào được (kèm cảnh báo đỏ), để lần
 *     cập nhật đầu tiên không khoá bạn ra khỏi chính trang của mình. Ghi danh
 *     xong cho cả đội thì đặt biến môi trường HTBB_ADMIN_2FA_BAT_BUOC=1.
 *     Từ đó tài khoản nào chưa có
 *     khoá là KHÔNG vào được, kể cả đúng mật khẩu. Đừng dừng ở bước ghi danh.
 *
 *  ------------------------------------------------------------------------
 *   MẤT ĐIỆN THOẠI THÌ SAO
 *  ------------------------------------------------------------------------
 *   1. Dùng một trong các mã dự phòng in ra lúc ghi danh (mỗi mã một lần).
 *   2. Hết mã dự phòng thì vào máy chủ bằng SSH và xoá khoá đi:
 *          DELETE FROM web_admin_2fa WHERE admin_user = 'tên';
 *      rồi ghi danh lại. Có SSH là luôn còn đường về — đó là chốt cuối, và nó
 *      cũng là lý do KHÔNG cần cửa hậu nào trên web.
 */
import { createHmac, randomBytes, randomInt, timingSafeEqual } from 'node:crypto'
import { TEN_SITE_VIET_TAT } from '../../shared/utils/tenSite.ts'

/** Tên hiện trong ứng dụng xác thực (issuer). Đổi thì mục cũ đã quét vẫn giữ nhãn cũ, mã vẫn đúng. */
export const HB_TEN_HE_THONG = `${TEN_SITE_VIET_TAT} Admin`

/** Số chữ số của mã. 6 = mặc định của mọi ứng dụng authenticator. */
export const HB_SO_CHU_SO = 6

/** Độ dài một bước thời gian (giây). */
export const HB_BUOC_GIAY = 30

/**
 * Cho phép lệch mấy bước về hai phía.
 *
 * 1 = ±30 giây. Cần có vì đồng hồ điện thoại và đồng hồ máy chủ gần như không
 * bao giờ khớp tuyệt đối. Đừng nâng lên cao: mỗi bước cho thêm là nhân đôi số
 * mã đang hợp lệ cùng lúc, tức là giảm đúng bấy nhiêu công sức đoán mò.
 */
export const HB_LECH_BUOC = 1

/** Số mã dự phòng sinh ra lúc ghi danh. */
export const HB_SO_MA_DU_PHONG = 8

/** Tên bảng lưu khoá (htbb_db). */
export const HB_BANG = 'web_admin_2fa'

// ===========================================================================
//  BASE32 (RFC 4648) — định dạng mà ứng dụng authenticator nhận
// ===========================================================================

const HB_BANG_CHU_BASE32 = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'

/** Mã hoá byte thô thành Base32 (không có dấu '=' đệm). */
export function hbMaHoaBase32(tho: Uint8Array | string): string {
  const buf = typeof tho === 'string' ? Buffer.from(tho, 'latin1') : Buffer.from(tho)
  if (buf.length === 0) return ''
  let bit = ''
  for (const b of buf) bit += b.toString(2).padStart(8, '0')
  // Đệm cho đủ nhóm 5 bit. Đệm bằng 0 ở CUỐI là đúng chuẩn.
  bit = bit.padEnd(Math.ceil(bit.length / 5) * 5, '0')
  let ra = ''
  for (let i = 0; i < bit.length; i += 5) ra += HB_BANG_CHU_BASE32[parseInt(bit.slice(i, i + 5), 2)]
  return ra
}

/**
 * Giải Base32 về byte thô. Trả về null nếu có ký tự không hợp lệ.
 *
 * Nhận cả chữ thường và cả dấu cách / gạch ngang, vì khoá được in ra theo nhóm
 * 4 ký tự cho dễ đọc và người ta hay dán lại nguyên cả khoảng trắng.
 */
export function hbGiaiBase32(ma: string): Buffer | null {
  // Chỉ bỏ khoảng trắng ASCII và '-' (viết rõ từng ký tự, không dùng \s vì \s
  // của JS còn khớp cả khoảng trắng Unicode).
  let s = ma.replace(/[ \t\n\v\f\r-]+/g, '').replace(/[a-z]/g, c => c.toUpperCase())
  s = s.replace(/=+$/, '')
  if (s === '') return null
  let bit = ''
  for (const ch of s) {
    const viTri = HB_BANG_CHU_BASE32.indexOf(ch)
    if (viTri === -1) return null
    bit += viTri.toString(2).padStart(5, '0')
  }
  // Bỏ phần bit lẻ ở cuối: đó là bit đệm, không thuộc dữ liệu.
  const n = Math.floor(bit.length / 8)
  const ra = Buffer.alloc(n)
  for (let i = 0; i < n; i++) ra[i] = parseInt(bit.slice(i * 8, i * 8 + 8), 2)
  return ra
}

// ===========================================================================
//  TOTP
// ===========================================================================

/** Bước thời gian hiện tại. Nhận mốc thời gian (giây) tuỳ chọn để thử với giờ cố định. */
export function hbBuoc(luc?: number | null): number {
  return Math.floor((luc ?? Math.floor(Date.now() / 1000)) / HB_BUOC_GIAY)
}

/**
 * Sinh mã cho một bước cụ thể. `khoaTho` là khoá đã giải Base32 (byte thô).
 *
 * Đây là HOTP của RFC 4226: HMAC-SHA1 rồi "dynamic truncation" — lấy 4 bit
 * cuối làm chỉ số, đọc 4 byte từ đó, xoá bit dấu, chia lấy dư 10^6.
 */
export function hbMaCuaBuoc(khoaTho: Uint8Array | string, buoc: number): string {
  const khoa = typeof khoaTho === 'string' ? Buffer.from(khoaTho, 'latin1') : khoaTho
  // Bộ đếm 8 byte, unsigned 64-bit big-endian — đúng như RFC 4226 quy định.
  const dem = Buffer.alloc(8)
  dem.writeBigUInt64BE(BigInt(buoc))
  const bam = createHmac('sha1', khoa).update(dem).digest()
  const chiSo = bam[19]! & 0x0f
  const so = ((bam[chiSo]! & 0x7f) << 24)
    | ((bam[chiSo + 1]! & 0xff) << 16)
    | ((bam[chiSo + 2]! & 0xff) << 8)
    | (bam[chiSo + 3]! & 0xff)
  return String(so % 10 ** HB_SO_CHU_SO).padStart(HB_SO_CHU_SO, '0')
}

/** So sánh thời gian hằng: không để lộ qua thời gian phản hồi là khớp được mấy ký tự đầu. */
function hbBangNhau(a: string, b: string): boolean {
  const x = Buffer.from(a)
  const y = Buffer.from(b)
  return x.length === y.length && timingSafeEqual(x, y)
}

/**
 * Kiểm tra một mã người dùng gõ vào.
 *
 * @param khoaBase32 khoá bí mật dạng Base32
 * @param maGoVao    mã 6 số người dùng gõ
 * @param buocDaDung bước cao nhất đã dùng rồi (chống dùng lại)
 * @param luc        mốc thời gian (giây), null = bây giờ
 * @returns bước khớp, hoặc null nếu không khớp
 */
export function hbKiemMa(khoaBase32: string, maGoVao: string, buocDaDung = 0, luc: number | null = null): number | null {
  const ma = maGoVao.replace(/\D+/g, '')
  if (ma.length !== HB_SO_CHU_SO) return null
  const khoaTho = hbGiaiBase32(khoaBase32)
  if (khoaTho === null || khoaTho.length === 0) return null
  const buocGio = hbBuoc(luc)
  for (let d = -HB_LECH_BUOC; d <= HB_LECH_BUOC; d++) {
    const buoc = buocGio + d
    // CHỐNG DÙNG LẠI. Không có dòng này thì ai đọc lén được mã (nhìn qua vai,
    // hoặc bắt được gói tin) vẫn dùng lại được trong 90 giây tiếp theo.
    if (buoc <= buocDaDung) continue
    // So sánh thời gian hằng, không rò rỉ số ký tự khớp đầu.
    if (hbBangNhau(hbMaCuaBuoc(khoaTho, buoc), ma)) return buoc
  }
  return null
}

/**
 * Sinh khoá bí mật mới: 20 byte ngẫu nhiên (160 bit, đúng cỡ SHA1).
 * CHỈ lệnh ghi danh qua SSH (scripts/tao-ma-2buoc.mjs) được gọi hàm này.
 */
export function hbSinhKhoa(): string {
  return hbMaHoaBase32(randomBytes(20))
}

/** Chia khoá thành nhóm 4 ký tự cho người gõ tay đỡ sai. */
export function hbKhoaDeDoc(khoaBase32: string): string {
  return (khoaBase32.match(/.{1,4}/g) ?? []).join(' ').trim()
}

/** Mã hoá URL theo RFC 3986: mọi thứ trừ A-Z a-z 0-9 - _ . ~ (encodeURIComponent còn để lọt ! ' ( ) *). */
function hbRawUrlEncode(s: string): string {
  return encodeURIComponent(s).replace(/[!'()*]/g, c => `%${c.charCodeAt(0).toString(16).toUpperCase()}`)
}

/**
 * URI otpauth:// để ứng dụng authenticator quét hoặc nhận qua dán.
 *
 * `issuer` vào cả nhãn và tham số là cố ý — cách cũ (chỉ nhãn) không phải app
 * nào cũng đọc đúng, và trong app sẽ hiện thành "HTBB Admin (tên)" cho dễ tìm.
 */
export function hbUri(khoaBase32: string, tenTaiKhoan: string, tenHeThong = HB_TEN_HE_THONG): string {
  const nhan = `${hbRawUrlEncode(tenHeThong)}:${hbRawUrlEncode(tenTaiKhoan)}`
  return `otpauth://totp/${nhan}?secret=${khoaBase32}&issuer=${hbRawUrlEncode(tenHeThong)}`
    + `&algorithm=SHA1&digits=${HB_SO_CHU_SO}&period=${HB_BUOC_GIAY}`
}

// ===========================================================================
//  BCRYPT ($2y$) — băm mã dự phòng
// ===========================================================================
//
//  Mã dự phòng được lưu dạng băm bcrypt "$2y$10$..." (crypt_blowfish của Solar
//  Designer). Các dòng đã có trong web_admin_2fa phải tiếp tục dùng được, và dự
//  án KHÔNG thêm thư viện npm, nên bcrypt được cài lại ở đây:
//   - khoá = byte của mật khẩu + NUL, lặp vòng, dùng đúng 72 byte đầu;
//   - $2a$ / $2b$ / $2y$ cho kết quả như nhau với mật khẩu ASCII (mã dự phòng
//     chỉ gồm a-z 2-9 và '-'); $2x$ (lỗi mở rộng dấu cũ) không hỗ trợ;
//   - kiểm tra = băm lại với cùng chuỗi cài đặt (cost + muối) rồi so cả chuỗi
//     bằng thời gian hằng.
//
//  Bảng P/S khởi tạo của Blowfish là các chữ số thập lục phân của π. Thay vì chép
//  tay 1042 hằng số (dễ sai một chữ mà không ai thấy), ta TÍNH π bằng công thức
//  Machin trên BigInt một lần lúc dùng lần đầu, rồi tự đối chiếu vài hằng số mốc
//  đã công bố (P[0]=243F6A88, S0[0]=D1310BA6, S3[255]=3AC372E6). Lệch là ném lỗi
//  ngay — thà không đăng nhập được bằng mã dự phòng còn hơn băm sai lặng lẽ.

const BC_SO_TU = 18 + 4 * 256

let bcGoc: Uint32Array | null = null

/** 1042 từ 32-bit đầu tiên của phần thập phân π (hệ 16): P[18] rồi S0..S3. */
function bcHangSoPi(): Uint32Array {
  if (bcGoc) return bcGoc
  const soChuSo = BC_SO_TU * 8
  const du = 16 // chữ số dự phòng chống sai số làm tròn
  // Dùng BigInt(...) thay vì literal "1n": bản build của Nitro hạ cú pháp về
  // es2019 và esbuild cảnh báo literal BigInt ở mức đó.
  const [K0, K1, K2, K4, K5, K16, K239] = [0, 1, 2, 4, 5, 16, 239].map(BigInt) as [bigint, bigint, bigint, bigint, bigint, bigint, bigint]
  const thang = K1 << BigInt(4 * (soChuSo + du))
  const arctanNghich = (x: bigint): bigint => {
    // arctan(1/x) = Σ (-1)^k / ((2k+1) x^(2k+1))
    const x2 = x * x
    let luyThua = thang / x
    let tong = K0
    let k = K0
    while (luyThua !== K0) {
      const s = luyThua / (K2 * k + K1)
      tong = k % K2 === K0 ? tong + s : tong - s
      luyThua /= x2
      k++
    }
    return tong
  }
  const pi = K16 * arctanNghich(K5) - K4 * arctanNghich(K239)
  const hex = pi.toString(16) // "3243f6a88..."
  const phanLe = hex.slice(1, 1 + soChuSo)
  const ra = new Uint32Array(BC_SO_TU)
  for (let i = 0; i < BC_SO_TU; i++) ra[i] = parseInt(phanLe.slice(i * 8, i * 8 + 8), 16) >>> 0
  if (ra[0] !== 0x243F6A88 || ra[18] !== 0xD1310BA6 || ra[BC_SO_TU - 1] !== 0x3AC372E6) {
    throw new Error('bcrypt: bảng hằng số Blowfish tính ra sai')
  }
  bcGoc = ra
  return ra
}

const BC_ITOA64 = './ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'

function bcMaHoa64(src: Uint8Array, len: number): string {
  let ra = ''
  let i = 0
  while (i < len) {
    let c1 = src[i++]!
    ra += BC_ITOA64[c1 >> 2]
    c1 = (c1 & 0x03) << 4
    if (i >= len) { ra += BC_ITOA64[c1]; break }
    let c2 = src[i++]!
    c1 |= c2 >> 4
    ra += BC_ITOA64[c1]
    c1 = (c2 & 0x0f) << 2
    if (i >= len) { ra += BC_ITOA64[c1]; break }
    c2 = src[i++]!
    c1 |= c2 >> 6
    ra += BC_ITOA64[c1]
    ra += BC_ITOA64[c2 & 0x3f]
  }
  return ra
}

function bcGiai64(s: string, len: number): Uint8Array | null {
  const ra = new Uint8Array(len)
  let i = 0
  let o = 0
  const lay = (): number => {
    const v = i < s.length ? BC_ITOA64.indexOf(s[i]!) : -1
    i++
    return v
  }
  while (o < len) {
    const c1 = lay(); const c2 = lay()
    if (c1 < 0 || c2 < 0) return null
    ra[o++] = ((c1 << 2) | ((c2 & 0x30) >> 4)) & 0xff
    if (o >= len) break
    const c3 = lay()
    if (c3 < 0) return null
    ra[o++] = (((c2 & 0x0f) << 4) | ((c3 & 0x3c) >> 2)) & 0xff
    if (o >= len) break
    const c4 = lay()
    if (c4 < 0) return null
    ra[o++] = (((c3 & 0x03) << 6) | c4) & 0xff
  }
  return ra
}

/** Lõi bcrypt: trả về 23 byte băm. */
function bcLoi(matKhau: Uint8Array, muoi: Uint8Array, cost: number): Uint8Array {
  const g = bcHangSoPi()
  const P = new Uint32Array(g.subarray(0, 18))
  const S = new Uint32Array(g.subarray(18))

  const F = (x: number) => ((((S[x >>> 24]! + S[256 + ((x >>> 16) & 0xff)]!) >>> 0) ^ S[512 + ((x >>> 8) & 0xff)]!) + S[768 + (x & 0xff)]!) >>> 0
  const lr = new Uint32Array(2)
  const maHoa = () => {
    let l = lr[0]!
    let r = lr[1]!
    for (let i = 0; i < 16; i += 2) {
      l = (l ^ P[i]!) >>> 0
      r = (r ^ F(l)) >>> 0
      r = (r ^ P[i + 1]!) >>> 0
      l = (l ^ F(r)) >>> 0
    }
    lr[0] = (r ^ P[17]!) >>> 0
    lr[1] = (l ^ P[16]!) >>> 0
  }

  // Khoá: byte mật khẩu + NUL, lặp vòng, đúng 18 từ (72 byte) như BF_set_key.
  const khoa = new Uint8Array(matKhau.length + 1)
  khoa.set(matKhau)
  const tuKhoa = new Uint32Array(18)
  let vt = 0
  for (let i = 0; i < 18; i++) {
    let t = 0
    for (let j = 0; j < 4; j++) {
      t = ((t << 8) | khoa[vt]!) >>> 0
      vt = (vt + 1) % khoa.length
    }
    tuKhoa[i] = t
  }
  const tuMuoi = new Uint32Array(4)
  for (let i = 0; i < 4; i++) tuMuoi[i] = ((muoi[i * 4]! << 24) | (muoi[i * 4 + 1]! << 16) | (muoi[i * 4 + 2]! << 8) | muoi[i * 4 + 3]!) >>> 0

  const moRong = (coMuoi: boolean) => {
    for (let i = 0; i < 18; i++) P[i] = (P[i]! ^ tuKhoa[i]!) >>> 0
    lr[0] = 0; lr[1] = 0
    let m = 0
    for (let i = 0; i < 18; i += 2) {
      if (coMuoi) { lr[0] = (lr[0]! ^ tuMuoi[m]!) >>> 0; lr[1] = (lr[1]! ^ tuMuoi[m + 1]!) >>> 0; m ^= 2 }
      maHoa()
      P[i] = lr[0]!; P[i + 1] = lr[1]!
    }
    for (let i = 0; i < 1024; i += 2) {
      if (coMuoi) { lr[0] = (lr[0]! ^ tuMuoi[m]!) >>> 0; lr[1] = (lr[1]! ^ tuMuoi[m + 1]!) >>> 0; m ^= 2 }
      maHoa()
      S[i] = lr[0]!; S[i + 1] = lr[1]!
    }
  }
  const moRongMuoi = () => {
    // Expand(state, 0, salt): XOR P với muối rồi mã hoá dây chuyền không muối.
    for (let i = 0; i < 18; i++) P[i] = (P[i]! ^ tuMuoi[i % 4]!) >>> 0
    lr[0] = 0; lr[1] = 0
    for (let i = 0; i < 18; i += 2) { maHoa(); P[i] = lr[0]!; P[i + 1] = lr[1]! }
    for (let i = 0; i < 1024; i += 2) { maHoa(); S[i] = lr[0]!; S[i + 1] = lr[1]! }
  }

  moRong(true)
  const vong = 2 ** cost
  for (let n = 0; n < vong; n++) {
    moRong(false)
    moRongMuoi()
  }

  // "OrpheanBeholderScryDoubt", mã hoá 64 lần.
  const ct = new Uint32Array([0x4f727068, 0x65616e42, 0x65686f6c, 0x64657253, 0x63727944, 0x6f756274])
  for (let n = 0; n < 64; n++) {
    for (let b = 0; b < 6; b += 2) {
      lr[0] = ct[b]!; lr[1] = ct[b + 1]!
      maHoa()
      ct[b] = lr[0]!; ct[b + 1] = lr[1]!
    }
  }
  const ra = new Uint8Array(24)
  for (let i = 0; i < 6; i++) {
    ra[i * 4] = ct[i]! >>> 24; ra[i * 4 + 1] = (ct[i]! >>> 16) & 0xff
    ra[i * 4 + 2] = (ct[i]! >>> 8) & 0xff; ra[i * 4 + 3] = ct[i]! & 0xff
  }
  return ra.subarray(0, 23)
}

/** Băm bcrypt với chuỗi cài đặt có sẵn ("$2y$10$" + 22 ký tự muối). Cài đặt hỏng -> null. */
function bcCrypt(matKhau: string, caiDat: string): string | null {
  const m = caiDat.match(/^\$2([aby])\$(\d\d)\$([./A-Za-z0-9]{22})/)
  if (!m) return null
  const cost = Number(m[2])
  if (cost < 4 || cost > 31) return null
  const muoi = bcGiai64(m[3]!, 16)
  if (!muoi) return null
  const bam = bcLoi(Buffer.from(matKhau, 'utf8'), muoi, cost)
  // Chuẩn hoá ký tự muối cuối như crypt_blowfish (chỉ 2 bit của nó có nghĩa).
  return `$2${m[1]}$${m[2]}$${bcMaHoa64(muoi, 16)}${bcMaHoa64(bam, 23)}`
}

/** Băm mới: bcrypt $2y$, cost 10, muối ngẫu nhiên 16 byte. */
export function hbBcryptHash(matKhau: string, cost = 10): string {
  const c = String(cost).padStart(2, '0')
  return bcCrypt(matKhau, `$2y$${c}$${bcMaHoa64(randomBytes(16), 16)}`)!
}

/** Kiểm tra một chuỗi với chuỗi băm bcrypt đã lưu. */
export function hbBcryptVerify(matKhau: string, bam: string): boolean {
  if (bam.length !== 60) return false
  const tinh = bcCrypt(matKhau, bam)
  return tinh !== null && hbBangNhau(tinh, bam)
}

// ===========================================================================
//  MÃ DỰ PHÒNG
// ===========================================================================

/** Sinh danh sách mã dự phòng dạng "abcd-efgh" (dễ đọc, không có ký tự dễ lẫn). */
export function hbSinhMaDuPhong(soLuong = HB_SO_MA_DU_PHONG): string[] {
  // Bỏ 0/o/1/l/i để không ai gõ nhầm khi đang cuống vì mất điện thoại.
  const chu = 'abcdefghjkmnpqrstuvwxyz23456789'
  const ra: string[] = []
  for (let i = 0; i < soLuong; i++) {
    let ma = ''
    for (let j = 0; j < 8; j++) {
      if (j === 4) ma += '-'
      ma += chu[randomInt(0, chu.length)]
    }
    ra.push(ma)
  }
  return ra
}

/** Băm danh sách mã dự phòng để lưu. Không bao giờ lưu dạng đọc được. */
export function hbBamMaDuPhong(ma: string[]): string {
  return ma.map(m => hbBcryptHash(m)).join('\n')
}

/** Bỏ khoảng trắng ASCII + NUL ở hai đầu (không đụng khoảng trắng Unicode). */
function hbCatKhoangTrang(s: string): string {
  return s.replace(/^[ \t\n\r\0\x0B]+|[ \t\n\r\0\x0B]+$/g, '')
}

/**
 * Thử một mã dự phòng. Trả về chuỗi băm CÒN LẠI nếu khớp, null nếu không.
 * Mã đã dùng bị gỡ khỏi danh sách — mỗi mã đúng một lần.
 */
export function hbDungMaDuPhong(chuoiBam: string, maGoVao: string): string | null {
  const ma = hbCatKhoangTrang(maGoVao).replace(/[A-Z]/g, c => c.toLowerCase())
  if (ma === '' || chuoiBam === '') return null
  // Mã dự phòng luôn có dạng "xxxx-xxxx". Thứ gì khác (nhất là mã 6 số gõ sai)
  // không thể khớp, nên bỏ qua luôn thay vì băm bcrypt 8 lần — mỗi lần ~0,1 giây
  // CPU chặn cả server, và ai đang dò mã sẽ được đẩy việc đó lên máy chủ miễn phí.
  if (!/^[a-z0-9]{4}-[a-z0-9]{4}$/.test(ma)) return null
  const conLai: string[] = []
  let khop = false
  for (const dong of chuoiBam.split('\n')) {
    const bam = hbCatKhoangTrang(dong)
    if (bam === '') continue
    // Chỉ gỡ MỘT mã dù có trùng: khớp rồi thì phần còn lại giữ nguyên hết.
    if (!khop && hbBcryptVerify(ma, bam)) {
      khop = true
      continue
    }
    conLai.push(bam)
  }
  return khop ? conLai.join('\n') : null
}

/** Đếm số mã còn lại trong một chuỗi băm. */
export function hbDemMaDuPhong(chuoiBam: string | null | undefined): number {
  if (!chuoiBam) return 0
  return chuoiBam.split('\n').filter(b => hbCatKhoangTrang(b) !== '').length
}

// ===========================================================================
//  LƯU TRỮ (bảng htbb_db.web_admin_2fa) — chỉ chạy trong server Nitro
// ===========================================================================

export interface HbDong {
  admin_user: string
  khoa_bimat: string
  kich_hoat: number
  buoc_da_dung: number
  ma_du_phong: string | null
  tao_luc: string | null
  dung_luc: string | null
}

let hbBangDaCo = false

/**
 * Bảng đã tồn tại chưa.
 *
 * CHỈ trả false khi MySQL nói rõ "không có bảng" (chưa ai từng ghi danh). Mọi
 * lỗi khác (MySQL tắt, mất kết nối, hết quyền) đều NÉM RA: nếu coi chúng là
 * "không có bảng" thì một lần MySQL trục trặc là mọi tài khoản đã ghi danh vào
 * được chỉ bằng mật khẩu. Cũng vì thế chỉ NHỚ kết quả "có bảng", không nhớ
 * "không có".
 */
export async function hbCoBang(): Promise<boolean> {
  if (hbBangDaCo) return true
  try {
    await dbGame().query(`SELECT 1 FROM ${HB_BANG} LIMIT 1`)
  } catch (e: any) {
    if (e?.code === 'ER_NO_SUCH_TABLE' || e?.errno === 1146) return false
    throw e
  }
  hbBangDaCo = true
  return true
}

/**
 * Dòng 2FA của một tài khoản, hoặc null nếu chưa ghi danh.
 * Lỗi cơ sở dữ liệu được NÉM RA (xem hbCoBang): người gọi phải từ chối đăng nhập
 * chứ không được hiểu nhầm thành "chưa bật hai bước".
 */
export async function hbDong(tenTaiKhoan: string): Promise<HbDong | null> {
  if (!(await hbCoBang())) return null
  const d = await q1<HbDong>(
    dbGame(),
    `SELECT admin_user, khoa_bimat, kich_hoat, buoc_da_dung,
            ma_du_phong, tao_luc, dung_luc
       FROM ${HB_BANG} WHERE admin_user = ? LIMIT 1`,
    [tenTaiKhoan.toLowerCase()],
  )
  return d ?? null
}

/** Tài khoản này có bắt buộc phải gõ mã hai bước không. */
export async function hbDaGhiDanh(tenTaiKhoan: string): Promise<boolean> {
  const d = await hbDong(tenTaiKhoan)
  return d !== null && Number(d.kich_hoat) === 1
}

/** Có bắt buộc MỌI tài khoản admin phải có 2FA không (HTBB_ADMIN_2FA_BAT_BUOC). */
export function hbBatBuocTatCa(): boolean {
  return useHtbbConfig().admin.batBuoc2fa
}

/**
 * Ghi lại bước vừa dùng để không ai dùng lại được mã đó.
 *
 * Trả về false khi một yêu cầu khác vừa dùng chính bước này (hoặc bước mới hơn)
 * trước mình — hai yêu cầu gửi song song cùng một mã thì chỉ một cái được vào:
 * điều kiện `buoc_da_dung < ?` biến lệnh UPDATE thành phép "so rồi đổi" nguyên tử.
 *
 * Lỗi ghi được NÉM RA chứ không cho vào: không ghi được bước đã dùng nghĩa là mã
 * này còn dùng lại được thêm tới 90 giây, đúng cái lỗ mà bước này sinh ra để bịt.
 */
export async function hbGhiBuocDaDung(tenTaiKhoan: string, buoc: number): Promise<boolean> {
  // `buoc_da_dung = GREATEST(...)`: hai phiên đăng nhập gần nhau có thể ghi
  // lệch thứ tự, và lùi con số này lại là mở lại đúng cái cửa vừa đóng.
  const kq = await exec(
    dbGame(),
    `UPDATE ${HB_BANG}
        SET buoc_da_dung = GREATEST(buoc_da_dung, ?),
            dung_luc = NOW()
      WHERE admin_user = ? AND buoc_da_dung < ?`,
    [buoc, tenTaiKhoan.toLowerCase(), buoc],
  )
  return kq.affectedRows > 0
}

/**
 * Ghi lại danh sách mã dự phòng còn lại sau khi dùng một mã.
 * Chỉ ghi khi danh sách trong bảng vẫn đúng là bản vừa đọc (`chuoiBamCu`) — nếu
 * không, một yêu cầu khác vừa dùng mã trước, trả về false.
 * Lỗi ghi được NÉM RA: không gạch được mã khỏi danh sách thì mã "dùng một lần"
 * thành dùng mãi, nên thà từ chối lượt đăng nhập này.
 */
export async function hbGhiMaDuPhong(tenTaiKhoan: string, chuoiBamConLai: string, chuoiBamCu: string): Promise<boolean> {
  const kq = await exec(
    dbGame(),
    `UPDATE ${HB_BANG}
        SET ma_du_phong = ?, dung_luc = NOW()
      WHERE admin_user = ? AND ma_du_phong = ?`,
    [chuoiBamConLai, tenTaiKhoan.toLowerCase(), chuoiBamCu],
  )
  return kq.affectedRows > 0
}

/** Số mã dự phòng còn lại. */
export async function hbSoMaDuPhongConLai(tenTaiKhoan: string): Promise<number> {
  const d = await hbDong(tenTaiKhoan)
  return d === null ? 0 : hbDemMaDuPhong(d.ma_du_phong)
}

// ===========================================================================
//  ĐĂNG NHẬP QUẢN TRỊ (dùng chung cho login.post.ts và verify-2fa.post.ts)
// ===========================================================================

/**
 * Phiên "đã đúng mật khẩu, đang chờ mã hai bước" sống được bao lâu (giây).
 *
 * Ngắn là cố ý. Trạng thái này nằm giữa hai cánh cửa: mật khẩu đã đúng rồi. Để
 * nó treo cả tiếng trên một máy công cộng là để sẵn nửa lượt đăng nhập cho người
 * ngồi vào sau. Token 'cho2fa' KHÔNG phải token admin: requireAdmin() chỉ nhận
 * loại 'admin', nên mọi API khác vẫn trả 401.
 */
export const HB_CHO_2FA_HAN = 300

/** Đóng phiên chờ và mở phiên admin thật: ghi nhật ký 'dang_nhap' rồi cấp token admin. */
export async function hbVaoDuoc(event: import('h3').H3Event, tk: { id: number, username: string }, cach: string) {
  const admin = { id: Number(tk.id), username: String(tk.username), vaiTro: (await vaiTroCua(String(tk.username))) ?? 'xem' as const }
  // ghiNhatKy() lấy tên người làm từ context: gắn admin vào trước để dòng
  // 'dang_nhap' có cột admin_user là chính người vừa vào.
  event.context.htbbAdmin = admin
  await ghiNhatKy(event, 'dang_nhap', admin.username, `vào trang quản trị (${cach})`)
  return { token: await taoTokenAdmin(admin.id, admin.username), admin }
}
