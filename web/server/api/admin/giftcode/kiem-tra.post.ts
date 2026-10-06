/**
 * Kiểm tra + dịch thử một chuỗi phần thưởng (xem trước trên trang). CHỈ ĐỌC — không
 * ghi gì. Lúc lưu, luu.post.ts vẫn tự chạy lại kiemTraItem(); đây chỉ để xem trước.
 * Body: { item }
 */
export default defineAdminHandler(async (event) => {
  const b = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const tho = typeof b?.item === 'string' ? b.item.slice(0, 5000) : ''
  const [chuan, loi] = kiemTraItem(tho)
  // Chuỗi có ký tự lạ thì không dịch (kiemTraItem đã dừng ở bước lọc ký tự).
  const kyTuHopLe = chuan === '' || /^[A-Za-z0-9:,_\- ]+$/.test(chuan)
  const [moTa] = await gcMoTaNhieu([kyTuHopLe ? chuan : ''])
  return { chuan, loi, moTa: moTa ?? [] }
})
