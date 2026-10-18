/**
 * Đưa nhân vật bị kẹt về điểm an toàn. Body { charId, lyDo (3–300 ký tự) }.
 *
 * Ghi vị trí đã lưu trong pInfo ([8] mapID = 0, [9] x = 0, [10] y = 0). Ở lần vào game kế tiếp, chính server
 * (real/CharNapDuLieu.initInfoFromDB, khoảng dòng 418-446) thấy mapID 0 nên đưa nhân vật về thị trấn khởi đầu
 * của vùng (mapstart[myCountry] = 70/1701, 0/301, 80/1901) tại một ô hồi sinh ngẫu nhiên của game; nhân vật chưa
 * có vùng (myCountry = -1) về 117/2641. Web không tự bịa toạ độ.
 *
 * CHỈ KHI OFFLINE: nhân vật online giữ vị trí trong RAM và ghi đè pInfo khi lưu; game không có lệnh dịch chuyển /
 * đá người chơi cho web gọi. UPDATE có điều kiện pInfo chưa đổi để không đè lần lưu vừa xảy ra.
 */
import { hsDatViTriAnToan, hsDocViTri } from '../../../utils/hoSoNguoiChoi'

export default defineAdminHandler(async (event) => {
  const b = await bodyObj(event)
  const id = ncIdNhanVat(b.charId)
  const lyDo = chuoi(b.lyDo, 'Lý do', 300, 3)
  const c = await q1<{ charname: string, pInfo: string }>(dbGame(), 'SELECT charname, pInfo FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, 'Không tìm thấy Pokémon.')
  const truoc = hsDocViTri(c.pInfo)
  const moi = hsDatViTriAnToan(c.pInfo)
  if (!truoc || moi === null) throw apiLoi(422, 'Dữ liệu Pokémon (pInfo) không đúng định dạng — không sửa.')
  // mapstart chỉ có 3 vùng (0..2); vùng lạ thì game sẽ lỗi khi nạp -> không đụng.
  if (truoc.quocGia < -1 || truoc.quocGia > 2) throw apiLoi(422, `Vùng của Pokémon (${truoc.quocGia}) nằm ngoài 0–2 — không đưa về điểm an toàn được, cần kiểm tra dữ liệu bằng tay.`)
  await ncCanOffline(String(c.charname), 'đưa về điểm an toàn')
  const r = await exec(dbGame(), 'UPDATE tob_char SET pInfo = ? WHERE id = ? AND pInfo = ?', [moi, id, c.pInfo])
  if (r.affectedRows !== 1) throw apiLoi(409, 'Dữ liệu Pokémon vừa thay đổi (có thể người chơi vừa vào game). Tải lại rồi thử lại.')
  await ghiThayDoi(event, 've_diem_an_toan', `nv:${c.charname}`, { viTri: truoc }, { viTri: hsDocViTri(moi), lyDo, ghiChu: 'game tự đặt thị trấn khởi đầu khi vào lại' })
  return { ok: true, message: `Đã đưa ${c.charname} về điểm an toàn. Lần vào game kế tiếp, Pokémon xuất hiện ở thị trấn khởi đầu của vùng.` }
})
