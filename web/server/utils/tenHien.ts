/**
 * Tên NGƯỜI CHƠI THẤY trong CSDL game (P6): `data_npc` (tên NPC tĩnh theo type) và `data_map_ten` (tên bản đồ theo id).
 * Bảng có trong sql/htbb.sql (CSDL cũ: phần 5 của sql/htbb.sql). Game server đọc lúc bật máy và TỰ NẠP
 * LẠI trong ≤ 30 giây khi số dòng / `sua_luc` đổi (com.htbb.pokemon.TenHien) — không cần khởi động lại; người chơi
 * thấy tên bản đồ mới ở lần vào bản đồ kế tiếp. Tên NPC tĩnh còn một bản sao trong client (giao thức chưa gửi tên NPC
 * tĩnh): đổi tên NPC thì phải phát hành lại client mới hiện trong game.
 */
export const BANG_TEN_NPC = 'data_npc'
export const BANG_TEN_MAP = 'data_map_ten'

export interface DongTenNpc { type: number, ten: string, chuc_vu: string, pokemon: string, dex: number, ban_do: string, sua_boi: string, sua_luc: string | null }
export interface DongTenMap { map_id: number, ten: string, vung: string, loai: string, mo_ta: string, sua_boi: string, sua_luc: string | null }

export async function docTenHien(): Promise<{ coBang: boolean, npc: DongTenNpc[], map: DongTenMap[] }> {
  const pool = dbGame()
  if (!(await coBang(pool, BANG_TEN_NPC)) || !(await coBang(pool, BANG_TEN_MAP))) return { coBang: false, npc: [], map: [] }
  const npc = await q<DongTenNpc>(pool, `SELECT type, ten, chuc_vu, pokemon, dex, ban_do, sua_boi, sua_luc FROM ${BANG_TEN_NPC} ORDER BY type`)
  const map = await q<DongTenMap>(pool, `SELECT map_id, ten, vung, loai, mo_ta, sua_boi, sua_luc FROM ${BANG_TEN_MAP} ORDER BY map_id`)
  return { coBang: true, npc, map }
}

let boNho: { luc: number, ten: Map<number, string> } | null = null

/** Bảng tên bản đồ id -> tên từ data_map_ten (đệm 60 giây); rỗng nếu chưa có bảng / CSDL lỗi. */
export async function bangTenBanDoSql(): Promise<Map<number, string>> {
  if (!boNho || Date.now() - boNho.luc > 60_000) {
    const ten = new Map<number, string>()
    try {
      if (await coBang(dbGame(), BANG_TEN_MAP)) {
        for (const r of await q<{ map_id: number, ten: string }>(dbGame(), `SELECT map_id, ten FROM ${BANG_TEN_MAP}`)) ten.set(Number(r.map_id), String(r.ten))
      }
    } catch { /* CSDL lỗi: để rỗng, nơi gọi hiện "Bản đồ #id" */ }
    boNho = { luc: Date.now(), ten }
  }
  return boNho.ten
}

export function xoaBoNhoTenHien(): void {
  boNho = null
}
