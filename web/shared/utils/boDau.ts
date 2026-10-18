/** Tách khỏi pokedex.ts để dùng ở thanh tìm kiếm mà không kéo pokedex.json vào mọi trang. */
/** Bỏ dấu tiếng Việt để tìm kiếm. */
export function boDau(s: string): string {
  return s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').replace(/Đ/g, 'D').toLowerCase()
}
