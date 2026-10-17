/**
 * Danh sách / tìm nhân vật cho trang /admin/nhanvat.
 *   ?q=<chữ>  : tìm theo tên (LIKE), tối đa 50, cấp cao trước
 *   ?q=<số>   : userId (xem hết nhân vật của một tài khoản) hoặc id nhân vật
 *   (trống)   : 30 nhân vật vào gần nhất, phân trang ?trang=
 * Tìm ra ĐÚNG MỘT nhân vật thì trang tự mở chi tiết (motId) — thiếu bước này thì bấm
 * tên nhân vật ở trang Log (liên kết ?q=ten) sẽ dừng lại ở bảng một dòng không có vật
 * phẩm và người xem tưởng trang không hề có phần đó.
 */
import type { NhanVatDong } from '../../../utils/nhanVatAdmin'

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const tim = str(query.q)
  const trang = Math.min(1_000_000, Math.max(1, int(query.trang, 1)))
  const moi = 30
  let ds: NhanVatDong[] = []
  let conTrangSau = false

  if (tim !== '') {
    if (/^\d+$/.test(tim)) {
      // Số: hiểu là userId (xem hết nhân vật của một tài khoản) hoặc id nhân vật.
      const so = int(tim)
      ds = await q<NhanVatDong>(dbGame(),
        `SELECT ${NV_COT} FROM tob_char WHERE userId = ? OR id = ?
          ORDER BY lastLv DESC LIMIT 50`, [so, so])
    } else {
      ds = await q<NhanVatDong>(dbGame(),
        `SELECT ${NV_COT} FROM tob_char WHERE charname LIKE ?
          ORDER BY lastLv DESC LIMIT 50`, [`%${tim}%`])
    }
  } else {
    // Lấy dư 1 dòng để biết còn trang sau hay không — khỏi phải COUNT(*) cả bảng mỗi lần.
    const lay = Math.trunc(moi + 1)
    const bo = Math.trunc((trang - 1) * moi)
    ds = await q<NhanVatDong>(dbGame(), `SELECT ${NV_COT} FROM tob_char ORDER BY lastLog DESC LIMIT ${lay} OFFSET ${bo}`)
    if (ds.length > moi) {
      conTrangSau = true
      ds.pop()
    }
  }

  return {
    q: tim,
    trang,
    conTrangSau,
    ds,
    motId: tim !== '' && ds.length === 1 ? ds[0]!.id : null,
  }
})
