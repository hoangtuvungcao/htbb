/**
 * Huấn luyện viên AI (bot lấp máy chủ): tên hiển thị = tiền tố (khoá hlv_ai.tien_to_ten, mặc định "ai ") + tên, giống máy
 * chủ game (com.htbb.pokemon.HlvAi). Tiền tố gõ tự do 1–12 ký tự, có thể dính liền tên, nhưng phải có chữ AI hoặc BOT;
 * không dùng ":" (chat kiểu cũ tách tên ở ": "). Giá trị sai -> mặc định.
 */
export const TIEN_TO_HLV_AI_MAC_DINH = 'ai '
/** Cùng mẫu với danh mục cấu hình và máy chủ (HlvAi.MAU_TIEN_TO). */
export const MAU_TIEN_TO_HLV_AI = /^(?=.*([Aa][Ii]|[Bb][Oo][Tt]))[^:\u0000-\u001F]{1,12}$/
export const LOI_TIEN_TO_HLV_AI = 'Tiền tố phải có chữ AI hoặc BOT để người chơi nhận ra Huấn luyện viên AI'

/** null = hợp lệ, chuỗi = lý do (hiện trên trang quản trị). */
export function loiTienToHlvAi(t: string | null | undefined): string | null {
  const s = String(t ?? '')
  if (!s.trim()) return LOI_TIEN_TO_HLV_AI
  if (s.length > 12) return 'Tiền tố tối đa 12 ký tự'
  if (s.includes(':')) return 'Tiền tố không dùng dấu ":"'
  return MAU_TIEN_TO_HLV_AI.test(s) ? null : LOI_TIEN_TO_HLV_AI
}

export function chuanTienToHlvAi(cauHinh: string | null | undefined): string {
  return loiTienToHlvAi(cauHinh) === null ? String(cauHinh) : TIEN_TO_HLV_AI_MAC_DINH
}

export function tenHienThiHlvAi(ten: string, laBot: boolean, tienTo?: string | null): string {
  if (!laBot) return ten
  const t = chuanTienToHlvAi(tienTo)
  return ten.startsWith(t) ? ten : t + ten
}
