/**
 * Huấn luyện viên AI (bot lấp máy chủ): tên hiển thị "bot_ai: <tên>" ở mọi nơi người chơi thấy tên (giống máy chủ game,
 * com.htbb.pokemon.HlvAi). Tên thật chỉ có [0-9a-zA-Z] nên tiền tố không trùng tên người chơi nào.
 */
export const TIEN_TO_HLV_AI = 'bot_ai: '

export function tenHienThiHlvAi(ten: string, laBot: boolean): string {
  if (!laBot || ten.startsWith(TIEN_TO_HLV_AI)) return ten
  return TIEN_TO_HLV_AI + ten
}
