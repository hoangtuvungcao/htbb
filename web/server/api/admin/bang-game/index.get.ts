/**
 * Xem CHỈ ĐỌC các bảng kết quả của game (chiếm thành tuần, chiến trường, quà online hằng ngày...).
 * Danh sách trắng cố định — chỉ hiện bảng thật sự có trong CSDL. Bảng mới của giai đoạn 2
 * (quà online, chiếm thành tuần) thêm tên vào BANG khi server tạo xong.
 * ?bang= tên bảng (phải nằm trong danh sách) -> 200 dòng mới nhất.
 */
const BANG: { ten: string, moTa: string }[] = [
  { ten: 'tob_chiemthanh', moTa: 'Chiếm thành — đội đang giữ thành' },
  { ten: 'tob_chiem_thanh_tuan', moTa: 'Chiếm thành theo tuần (giai đoạn 2)' },
  { ten: 'tob_chien_truong', moTa: 'Chiến trường' },
  { ten: 'tob_qua_ct_top', moTa: 'Quà top chiến trường' },
  { ten: 'tob_qua_online', moTa: 'Quà online hằng ngày (giai đoạn 2)' },
  { ten: 'tob_thuong_online_ngay', moTa: 'Quà online hằng ngày (giai đoạn 2)' },
  { ten: 'tob_diem_danh', moTa: 'Điểm danh hằng ngày' },
  { ten: 'tob_top_thuong', moTa: 'Thưởng bảng xếp hạng' },
  { ten: 'giftcode_log', moTa: 'Nhật ký nhận giftcode' },
]
export default defineAdminHandler(async (event) => {
  const co: typeof BANG = []
  for (const b of BANG) if (await coBang(dbGame(), b.ten)) co.push(b)
  const ten = str(getQuery(event).bang)
  if (!ten) return { bang: co }
  if (!co.some(b => b.ten === ten)) throw apiLoi(404, 'Không có bảng này.')
  const rows = await q(dbGame(), `SELECT * FROM \`${ten}\` LIMIT 200`)
  // Không trả cột mật khẩu / dữ liệu nhân vật thô nếu lỡ có.
  const an = /pass|pinfo|equip|inven|bag/i
  return { bang: co, dong: rows.map(r => Object.fromEntries(Object.entries(r).filter(([k]) => !an.test(k)).map(([k, v]) => [k, typeof v === 'string' && v.length > 200 ? `${v.slice(0, 200)}…` : v]))) }
})
