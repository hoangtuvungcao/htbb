/**
 * Gửi yêu cầu đổi lượng THƯỜNG của một Pokémon: { charname, loai: 'khoa' | 'xu', so }.
 *
 * Web KHÔNG sửa số dư (lượng khoá nằm trong pInfo, game nạp / lưu nhân vật không khoá dòng — ghi tay sẽ bị đè hoặc
 * nhân đôi). Web chỉ chèn MỘT dòng vào web_lenh_nhan_vat; máy chủ game (data.LenhWeb -> real.DoiLuongGame) chốt dòng
 * rồi đổi trên RAM dưới khoá tiền: Pokémon đang online -> đổi ngay (≤ 1 giây); offline -> đổi ngay khi vào game (huỷ
 * được trước lúc đó). Mọi luật (đủ lượng, tỉ giá hôm nay, trần ngày tài khoản) game kiểm lại lúc đổi.
 */
import { LOAI_DOI, LENH_DOI_LUONG_XU, loiDoi, ngayVN, soNhan, type LoaiDoi } from '../../../shared/utils/doiLuong'
import { docDaDung, docTiGia, docYeuCau, guiLenhDoi, laDangCho } from '../../utils/doiLuongDb'
import { LENH_CHO, LENH_CHO_DANG_NHAP, LENH_LOI, LENH_XONG_ONLINE, choKetQuaLenh } from '../../utils/lenhNhanVat'

const DOI_TOI_DA = 20
const DOI_TRONG_GIAY = 3600

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const charname = str(body?.charname).trim().slice(0, 50)
  const loai = str(body?.loai) as LoaiDoi
  const so = int(body?.so, 0)
  if (!charname) throw apiLoi(400, 'Hãy chọn Pokémon.')
  if (loai !== 'khoa' && loai !== 'xu') throw apiLoi(400, 'Loại đổi không hợp lệ.')
  if (await biChanSpam(event, 'doi_luong', DOI_TOI_DA, DOI_TRONG_GIAY)) {
    throw apiLoi(429, 'Bạn gửi quá nhiều yêu cầu đổi. Thử lại sau một giờ.')
  }
  const db = dbGame()
  const nv = await q1<{ id: number, charname: string, luong: number }>(db,
    'SELECT id, charname, luong FROM tob_char WHERE userId = ? AND charname = ? AND del = 1 LIMIT 1', [Math.trunc(u.id), charname])
  if (!nv) throw apiLoi(404, 'Không tìm thấy Pokémon này trong tài khoản của bạn.')

  const ngay = ngayVN()
  const yeuCau = await docYeuCau(db, Math.trunc(u.id), 50)
  const tiLe = Math.max(0, Math.trunc(Number(await chGiaTri('doi_luong.ti_le')) || 0))
  const phanTramGio = Math.max(0, Math.trunc(Number(await chGiaTri('doi_luong_xu.phan_tram_gio')) || 0))
  const tranXu = Math.max(0, Math.trunc(Number(await chGiaTri('doi_luong_xu.tran_ngay')) || 0))
  const tiGia = phanTramGio > 0 && tranXu > 0 ? ((await docTiGia(db, ngay))?.giaTri ?? 0) : 0
  const choXu = yeuCau.filter(y => y.loai === LENH_DOI_LUONG_XU && laDangCho(y.trangThai)).reduce((s, y) => s + y.so, 0)
  const loi = loiDoi(loai, so, {
    luong: Math.max(0, Math.trunc(Number(nv.luong) || 0)),
    tiLe,
    tiGia,
    conXu: tranXu - (await docDaDung(db, Math.trunc(u.id), 'doi_luong_xu', ngay)) - choXu,
    dangCho: yeuCau.some(y => y.charname.toLowerCase() === String(nv.charname).toLowerCase() && laDangCho(y.trangThai)),
  })
  if (loi) throw apiLoi(400, loi)

  const id = await guiLenhDoi(db, LOAI_DOI[loai], nv.id, String(nv.charname), so, `nguoi_choi:${u.username}`)
  await ghiLanLam(event, 'doi_luong')
  const r = await choKetQuaLenh(db, id)
  const tt = Math.trunc(Number(r?.trang_thai ?? LENH_CHO))
  const ketQua = String(r?.ket_qua ?? '')
  const nhan = soNhan(loai, so, tiLe, tiGia)
  const moTa = loai === 'khoa' ? `${so.toLocaleString('vi-VN')} lượng → ${nhan.toLocaleString('vi-VN')} lượng khoá`
    : `${so.toLocaleString('vi-VN')} lượng → ${nhan.toLocaleString('vi-VN')} xu`
  if (tt === LENH_XONG_ONLINE) return { id, trangThai: tt, loai: 'success', thongBao: `Đã đổi ${moTa} cho ${nv.charname} (đang online).` }
  if (tt === LENH_LOI) return { id, trangThai: tt, loai: 'error', thongBao: `Không đổi được: ${ketQua || 'máy chủ từ chối'}.` }
  if (tt === LENH_CHO_DANG_NHAP) {
    return { id, trangThai: tt, loai: 'warning', thongBao: `${nv.charname} đang offline: yêu cầu ${moTa} sẽ được đổi ngay khi Pokémon vào game (có thể huỷ trước lúc đó).` }
  }
  return { id, trangThai: tt, loai: 'warning', thongBao: `Đã gửi yêu cầu ${moTa}. Máy chủ game chưa trả lời — yêu cầu vẫn chờ và sẽ được xử lý khi máy chủ chạy.` }
})
