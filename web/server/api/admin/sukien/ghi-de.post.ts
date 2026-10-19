/**
 * Ghi đè MỘT lần diễn ra (một năm) của sự kiện theo lịch năm. Body { ma, nam, lan, kieu, mo?, dong? }:
 *   kieu 'theo_lich' — về lại giờ của lịch;
 *        'tat'       — tắt năm nay (không bật, không đăng tin);
 *        'doi'       — đổi giờ mở / đóng (mo, dong: 'YYYY-MM-DDTHH:mm');
 *        'bat_ngay'  — mở ngay bây giờ, đóng theo `dong` (bỏ trống = giờ đóng hiện tại của lần đó).
 * Máy chủ game áp dụng trong ≤ 15 giây (nhịp LichSuKien) rồi tự khởi động lại như khi bật tay (gom + báo trong game).
 */
export default defineAdminHandler(async (event, admin) => {
  const b = await bodyObj(event)
  const ma = str(b.ma)
  const nam = int(b.nam)
  const lan = int(b.lan)
  const kieu = str(b.kieu)
  if (!['theo_lich', 'tat', 'doi', 'bat_ngay'].includes(kieu)) throw apiLoi(400, 'Không hiểu thao tác.')
  const dong0 = await q1<Record<string, unknown>>(dbGame(),
    `SELECT ten, lich_mo, lich_dong, mo_luc, dong_luc FROM ${SU_KIEN_LICH_BANG} WHERE ma = ? AND nam = ? AND lan = ? LIMIT 1`,
    [ma, nam, lan])
  if (!dong0) throw apiLoi(404, 'Không có lần diễn ra này trong lịch (máy chủ game chưa ghi lịch?).')
  const ten = String(dong0.ten ?? ma)
  const bayGio = await suKienBayGio()
  let mo = String(dong0.mo_luc)
  let dong = String(dong0.dong_luc)
  let ghiDe = 2
  if (kieu === 'theo_lich') {
    mo = String(dong0.lich_mo)
    dong = String(dong0.lich_dong)
    ghiDe = 0
  }
  else if (kieu === 'tat') {
    ghiDe = 1
  }
  else {
    const moMoi = kieu === 'bat_ngay' ? bayGio.slice(0, 16) + ':00' : suKienDocMoc(b.mo ?? '')
    const dongMoi = str(b.dong) ? suKienDocMoc(b.dong) : (kieu === 'bat_ngay' ? dong : false)
    if (!moMoi || !dongMoi) throw apiLoi(400, 'Mốc thời gian không đọc được. Định dạng: 2026-10-25 04:00.')
    if (String(dongMoi) <= String(moMoi)) throw apiLoi(400, 'Giờ đóng phải sau giờ mở.')
    if (String(dongMoi) <= bayGio) throw apiLoi(400, 'Giờ đóng đã qua.')
    mo = String(moMoi)
    dong = String(dongMoi)
  }
  await exec(dbGame(),
    `UPDATE ${SU_KIEN_LICH_BANG} SET ghi_de = ?, mo_luc = ?, dong_luc = ?, sua_boi = ?, sua_luc = NOW()
      WHERE ma = ? AND nam = ? AND lan = ?`, [ghiDe, mo, dong, admin.username, ma, nam, lan])
  await ghiNhatKy(event, 'su_kien_ghi_de', `${ma}:${nam}:${lan}`, `${ten} | ${kieu} | ${mo} -> ${dong}`)
  xoaDemCms()
  const viec = { theo_lich: 'về lại giờ theo lịch', tat: 'tắt năm nay', doi: `đổi giờ: ${mo.slice(0, 16)} → ${dong.slice(0, 16)}`, bat_ngay: `mở ngay tới ${dong.slice(0, 16)}` }[kieu]
  return { ok: true, message: `${ten} ${nam}: ${viec}. Máy chủ game áp dụng trong vài giây (tự khởi động lại nếu cần).` }
})
