/**
 * Ai đang online, đang ở map nào — đọc bảng `tob_vi_tri_online`.
 *
 * Bảng đó do server tự ghi (src/data/GhiViTriOnline.java): mỗi 30 giây XOÁ HẾT rồi
 * ghi lại toàn bộ người chơi đang online + map/toạ độ hiện tại. Vì vậy trang này
 * KHÔNG có khái niệm "trang sau" hay "log lịch sử" — nó luôn hiện đúng bức ảnh mới
 * nhất, chỉ có thể trễ tối đa 30 giây so với thực tế.
 *
 * Bảng chưa có (server chưa khởi động lại lần nào từ khi có tính năng này) thì trả
 * bangChuaCo = true để trang hiện hướng dẫn thay vì lỗi khó hiểu.
 *
 * Query: ?locTen=&locMap= (gần đúng, LIKE)
 */
const BANG_VI_TRI = 'tob_vi_tri_online'

interface DongViTri {
  charname: string
  map_id: number
  map_ten: string
  khu: number
  x: number
  y: number
  cap_nhat_luc: string
}

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const locTen = str(query.locTen)
  const locMap = str(query.locMap)

  let dong: DongViTri[] = []
  let loiDoc = ''
  let bangChuaCo = false
  try {
    const dieuKien: string[] = []
    const thamSo: string[] = []
    if (locTen !== '') {
      dieuKien.push('charname LIKE ?')
      thamSo.push(`%${locTen}%`)
    }
    if (locMap !== '') {
      dieuKien.push('map_ten LIKE ?')
      thamSo.push(`%${locMap}%`)
    }
    const where = dieuKien.length ? `WHERE ${dieuKien.join(' AND ')}` : ''
    dong = await q<DongViTri>(
      dbGame(),
      `SELECT charname, map_id, map_ten, khu, x, y, cap_nhat_luc FROM \`${BANG_VI_TRI}\` ${where} ORDER BY map_ten, charname LIMIT 2000`,
      thamSo,
    )
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
    bangChuaCo = e?.code === 'ER_NO_SUCH_TABLE' || /exist/i.test(loiDoc)
  }

  // Gom theo map để hiện số người trên từng map ngay trên đầu, không phải đọc từng dòng.
  const dem = new Map<string, number>()
  for (const r of dong) {
    const ten = String(r.map_ten ?? '') !== '' ? String(r.map_ten) : `(map id ${r.map_id})`
    dem.set(ten, (dem.get(ten) ?? 0) + 1)
  }
  const theoMap = [...dem.entries()].map(([ten, sl]) => ({ ten, sl })).sort((a, b) => b.sl - a.sl)

  return { dong, theoMap, bangChuaCo, loiDoc, locTen, locMap }
})
