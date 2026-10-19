/**
 * NPC công khai cho Cẩm nang › Bản đồ: đọc THẲNG bảng data_npc của game (cùng nguồn với game server, sửa ở
 * /admin/ten-hien). Gộp NPC trùng chức năng (Golem bán hàng 1..5 -> một dòng), đếm số bản đồ có NPC đó.
 * null = chưa có bảng / CSDL lỗi -> trang dùng danh sách tĩnh. Đệm 5 phút.
 */
export interface NpcCongKhai { ten: string, chucVu: string, dex: number, soBanDo: number }

export default defineEventHandler(async () => quaDem('npc-cong-khai', 300, async (): Promise<NpcCongKhai[] | null> => {
  try {
    if (!(await coBang(dbGame(), BANG_TEN_NPC))) return null
    const rows = await q<{ ten: string, chuc_vu: string, dex: number, ban_do: string }>(dbGame(), `SELECT ten, chuc_vu, dex, ban_do FROM ${BANG_TEN_NPC} ORDER BY type`)
    const gop = new Map<string, NpcCongKhai & { bd: Set<string> }>()
    for (const r of rows) {
      const ten = String(r.ten ?? '').replace(/\s+\d+$/, '').trim()
      if (!ten) continue
      const khoa = `${ten}|${r.chuc_vu}`
      const g = gop.get(khoa) ?? { ten, chucVu: String(r.chuc_vu ?? ''), dex: Number(r.dex) || 0, soBanDo: 0, bd: new Set<string>() }
      for (const b of String(r.ban_do ?? '').split(',').map(x => x.trim()).filter(Boolean)) g.bd.add(b)
      gop.set(khoa, g)
    }
    return [...gop.values()].map(({ bd, ...g }) => ({ ...g, soBanDo: bd.size }))
  }
  catch { return null }
}))
