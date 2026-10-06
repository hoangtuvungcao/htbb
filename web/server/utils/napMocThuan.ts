/**
 * NẠP MỐC — các hàm thuần (không đụng database, không dùng auto-import của Nuxt):
 * sinh mã đơn, dựng URL QR VietQR, đọc payload webhook SePay và quyết định có
 * xác nhận đơn hay không. Phần chạm database nằm ở napMoc.ts.
 */
import { randomInt, createHash, timingSafeEqual } from 'node:crypto'

/** Bỏ ký tự dễ nhầm (0/O, 1/I) — người chơi có khi phải gõ tay nội dung CK. */
export const NAP_MOC_KY_TU = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'

/** Sinh mã đơn ngẫu nhiên dùng làm nội dung chuyển khoản, ví dụ KPX7F3AQ2. */
export function napMocTaoMaDon(): string {
  let code = ''
  for (let i = 0; i < 8; i++) {
    code += NAP_MOC_KY_TU[randomInt(0, NAP_MOC_KY_TU.length)]
  }
  return `KP${code}`
}

/**
 * URL ảnh QR VietQR (tương thích quét bằng app TPBank) cho 1 số tiền + nội dung CK.
 * Ảnh do dịch vụ công khai img.vietqr.io dựng, không cần đăng ký.
 */
export function napMocVietQrUrl(bankBin: string, accountNo: string, accountName: string, amount: number, maDon: string): string {
  const path = `${bankBin}-${encodeURIComponent(accountNo)}-compact2.png`
  const query = new URLSearchParams({
    amount: String(Math.trunc(amount)),
    addInfo: maDon,
    accountName,
  }).toString()
  return `https://img.vietqr.io/image/${path}?${query}`
}

/**
 * Đọc số tiền trong payload một cách khoan dung: số thì cắt phần thập phân, chuỗi
 * thì lấy phần số nguyên ở đầu ("49000.00" -> 49000), còn lại là 0 (sẽ bị coi là
 * "amount too low" chứ không bao giờ là đủ tiền).
 */
export function napMocSoNguyen(v: unknown): number {
  if (typeof v === 'number') return Number.isFinite(v) ? Math.trunc(v) : 0
  if (typeof v === 'string') {
    const m = v.match(/^\s*([+-]?\d+)/)
    return m ? Number(m[1]) : 0
  }
  return 0
}

/** Đọc một trường văn bản trong payload: chuỗi giữ nguyên, số đổi ra chuỗi, còn lại rỗng. */
export function napMocChuoi(v: unknown): string {
  if (typeof v === 'string') return v
  if (typeof v === 'number') return String(v)
  return ''
}

/** Tìm mã đơn trong nội dung chuyển khoản (ngân hàng hay chèn thêm chữ trước/sau). */
export function napMocTimMaDon(content: string): string | null {
  const m = content.match(/KP[A-Z0-9]{6,10}/i)
  return m ? m[0].toUpperCase() : null
}

/** Lấy API key từ header `Authorization: Apikey <key>` của SePay. */
export function napMocDocApiKey(authHeader: string | null | undefined): string | null {
  const m = (authHeader ?? '').match(/apikey\s+(\S+)/i)
  return m ? m[1]! : null
}

/**
 * So sánh khoá trong thời gian hằng. Băm cả hai về cùng độ dài trước để
 * timingSafeEqual không ném lỗi và không lộ độ dài khoá thật.
 */
export function napMocSoKhoa(that: string, goiLen: string): boolean {
  const a = createHash('sha256').update(that, 'utf8').digest()
  const b = createHash('sha256').update(goiLen, 'utf8').digest()
  return timingSafeEqual(a, b) && that.length === goiLen.length
}

export type NapMocWebhookPhanTich =
  | { loai: 'tra_loi', success: boolean, message: string }
  | { loai: 'doi_chieu', maDon: string, amount: number }

/**
 * Phần đầu của webhook (sau khi đã qua kiểm API key): đọc payload, bỏ qua tiền ra,
 * tìm mã đơn trong nội dung + mã tham chiếu SePay.
 */
export function napMocPhanTichWebhook(raw: string): NapMocWebhookPhanTich {
  let data: unknown
  try {
    data = JSON.parse(raw)
  } catch {
    data = null
  }
  if (data === null || typeof data !== 'object') {
    return { loai: 'tra_loi', success: false, message: 'invalid payload' }
  }
  const d = data as Record<string, unknown>
  if (d.transferType !== 'in') {
    return { loai: 'tra_loi', success: true, message: 'ignored (not incoming)' }
  }
  const amount = napMocSoNguyen(d.transferAmount ?? 0)
  const content = `${napMocChuoi(d.content)} ${napMocChuoi(d.code)}`
  const maDon = napMocTimMaDon(content)
  if (!maDon) {
    return { loai: 'tra_loi', success: false, message: 'no order code found in content' }
  }
  return { loai: 'doi_chieu', maDon, amount }
}

/**
 * Phần sau: đã tìm được đơn (hoặc không). capNhat = được phép chuyển sang
 * da_thanh_toan (câu UPDATE vẫn tự chặn trùng bằng WHERE trang_thai='cho_thanh_toan').
 * Chuyển thiếu tiền thì KHÔNG xác nhận — admin xử lý tay sau khi đối chiếu.
 */
export function napMocQuyetDinhWebhook(
  don: { trang_thai: string, amount: number | string } | null,
  maDon: string,
  amount: number,
): { capNhat: boolean, success: boolean, message: string } {
  if (!don) return { capNhat: false, success: false, message: `order not found: ${maDon}` }
  if (don.trang_thai !== 'cho_thanh_toan') return { capNhat: false, success: true, message: 'already processed' }
  if (amount < Number(don.amount)) return { capNhat: false, success: false, message: 'amount too low' }
  return { capNhat: true, success: true, message: `confirmed ${maDon}` }
}
