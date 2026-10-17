/**
 * Kích thước + thời lượng khung sprite PMD (shared/data/sprite-anim.json, sinh bởi
 * scripts/sinh-anim-khoi-dau.mjs). Ảnh dải khung ở public/pokemon/anim/<khoá>-<động tác>.png.
 */
import raw from '../data/sprite-anim.json'

export interface DaiKhung { w: number, h: number, khung: number, thoiLuong: number[], /** Điểm chân so với tâm khung (px gốc). */ chanX: number, chanY: number }
export type DongTac = 'idle' | 'walk' | 'walkR' | 'danhR'

export const ANIM = raw as Record<string, Partial<Record<DongTac, DaiKhung>>>

/** Khoá sprite của loài ("25" / "77s"): bản thường, không có thì bản shiny (và ngược lại khi muốn shiny). */
export function khoaSprite(dex: number, shiny = false): string | null {
  const a = shiny ? `${dex}s` : String(dex)
  const b = shiny ? String(dex) : `${dex}s`
  return ANIM[a] ? a : ANIM[b] ? b : null
}
