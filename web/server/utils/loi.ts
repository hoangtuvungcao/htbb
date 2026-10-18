/**
 * Lỗi trả về cho client. Client đọc `error.data.message` (xem app/composables/useApi.ts).
 *
 * KHÔNG BAO GIỜ đưa chi tiết lỗi database ra cho người chưa đăng nhập: thông báo
 * lỗi của driver MySQL có thể chứa host, tên database và tên người dùng DB.
 */
export function apiLoi(statusCode: number, message: string, extra: Record<string, unknown> = {}) {
  return createError({
    statusCode,
    statusMessage: statusCode >= 500 ? 'Server Error' : 'Request Error',
    message,
    data: { message, ...extra },
  })
}

/** Đọc một chuỗi đã trim từ body/query; không phải chuỗi thì trả mặc định. */
export function str(v: unknown, macDinh = ''): string {
  return typeof v === 'string' ? v.trim() : (typeof v === 'number' ? String(v) : macDinh)
}

/** Đọc số nguyên; không hợp lệ thì trả mặc định. */
export function int(v: unknown, macDinh = 0): number {
  if (typeof v === 'number' && Number.isFinite(v)) return Math.trunc(v)
  if (typeof v === 'string' && /^-?\d+$/.test(v.trim())) return Number(v.trim())
  return macDinh
}
