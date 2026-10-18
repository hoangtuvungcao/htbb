/** Kiểu dữ liệu tin tức trả về từ /api/tin-tuc (dùng chung trang & admin). */
import type { NewsArticle } from './news'

export interface TinTomTatWeb {
  slug: string
  tieuDe: string
  tomTat: string
  anhBia: string
  loai: string
  the: string[]
  ngay: string
  ghim: boolean
}
export interface TinChiTietWeb extends TinTomTatWeb {
  html: string
  muc: { id: string, ten: string }[]
  tinh?: NewsArticle
}
