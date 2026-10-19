/**
 * Học chiêu theo loài (đề xuất 1): bảng `data_learnset` của game (nguồn duy nhất: sql/hoc_chieu_huan_luyen.sql / CSDL,
 * game server com.htbb.pokemon.Learnset). Mỗi loài một dòng cho mỗi ô chiêu nó học được: `nguon` 'cap' = tự học khi đủ
 * cấp (không tốn điểm), 'npc' = học ở NPC Blaziken bằng xu. Hàm thuần — dùng chung cho API, trang Pokédex và test.
 */
export interface DongLearnset {
  loai: number
  o: number
  dong: number
  ten: string
  he: number
  nguon: 'cap' | 'npc'
  capHoc: number
  giaXu: number
}

export type CachHoc = 'cap' | 'npc' | 'tien-hoa'

export interface ChieuCuaLoai {
  o: number
  ten: string
  he: number
  cach: CachHoc
  capHoc: number
  giaXu: number
  /** Loài học được chiêu này sớm nhất (khi `cach` = 'tien-hoa': phải tiến hoá thành loài này). */
  loaiHoc: number
}

/**
 * Chiêu của loài `dex` trong cả dòng: chiêu loài tự học / học ở NPC, và chiêu chỉ dạng tiến hoá học được (kèm loài +
 * cấp). Sắp theo cấp học rồi ô.
 */
export function chieuCuaLoai(tatCa: DongLearnset[], dex: number): ChieuCuaLoai[] {
  const cua = tatCa.find(r => r.loai === dex)
  if (!cua) return []
  const dong = tatCa.filter(r => r.dong === cua.dong)
  const theoO = new Map<number, ChieuCuaLoai>()
  for (const r of dong) {
    if (r.loai !== dex) continue
    theoO.set(r.o, { o: r.o, ten: r.ten, he: r.he, cach: r.nguon === 'npc' ? 'npc' : 'cap', capHoc: r.capHoc, giaXu: r.giaXu, loaiHoc: dex })
  }
  for (const r of dong) {
    if (r.loai === dex || theoO.has(r.o) && theoO.get(r.o)!.cach !== 'tien-hoa') continue
    const cu = theoO.get(r.o)
    if (!cu || r.capHoc < cu.capHoc) {
      theoO.set(r.o, { o: r.o, ten: r.ten, he: r.he, cach: 'tien-hoa', capHoc: r.capHoc, giaXu: r.giaXu, loaiHoc: r.loai })
    }
  }
  return [...theoO.values()].sort((a, b) => a.capHoc - b.capHoc || a.o - b.o)
}

/** Một hàng SQL (snake_case) -> DongLearnset; hàng hỏng = null. */
export function docDong(r: Record<string, unknown>): DongLearnset | null {
  const so = (k: string) => Number(r[k])
  const d: DongLearnset = {
    loai: so('loai'), o: so('o'), dong: so('dong'), ten: String(r.ten ?? '').trim(), he: so('he'),
    nguon: String(r.nguon ?? '').trim().toLowerCase() === 'npc' ? 'npc' : 'cap', capHoc: so('cap_hoc'), giaXu: so('gia_xu'),
  }
  return Number.isInteger(d.loai) && Number.isInteger(d.o) && d.ten ? d : null
}
