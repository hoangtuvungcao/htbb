/**
 * Bảng chiêu theo loài cho trang Pokédex: đọc THẲNG `data_learnset` của game (cùng nguồn với game server), mọi loài cùng
 * dòng với `dex` (để trang hiện "khi tiến hoá thành …"). null = chưa có bảng / CSDL lỗi / loài không phải Pokémon người
 * chơi -> trang dùng danh sách chiêu tĩnh. Đệm 5 phút.
 */
import { docDong, type DongLearnset } from '../../../shared/utils/learnset'

export default defineEventHandler(async (event): Promise<DongLearnset[] | null> => {
  const dex = Number(getRouterParam(event, 'dex'))
  if (!Number.isInteger(dex) || dex <= 0 || dex > 2000) return null
  return quaDem(`learnset-${dex}`, 300, async () => {
    try {
      if (!(await coBang(dbGame(), 'data_learnset'))) return null
      const rows = await q<Record<string, unknown>>(dbGame(),
        'SELECT loai, o, dong, ten, he, nguon, cap_hoc, gia_xu FROM data_learnset'
        + ' WHERE dong = (SELECT dong FROM data_learnset WHERE loai = ? LIMIT 1) ORDER BY loai, o', [dex])
      const ds = rows.map(docDong).filter((d): d is DongLearnset => d !== null)
      return ds.length ? ds : null
    }
    catch { return null }
  })
})
