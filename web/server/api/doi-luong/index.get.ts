/**
 * Tình trạng đổi lượng cho trang /account (người chơi đang đăng nhập): tỉ lệ lượng → lượng khoá, tỉ giá lượng → xu hôm
 * nay (chung toàn máy chủ), trần ngày còn lại của TÀI KHOẢN, sổ lệnh sàn (chỉ xem — đặt lệnh ở NPC Tổng Quản trong game),
 * và các yêu cầu đổi gần đây. Chỉ đọc theo id lấy từ token.
 */
import { LENH_DOI_LUONG_XU, giaTrungBinh, moTaLenhDoi, ngayVN } from '../../../shared/utils/doiLuong'
import { docDaDung, docSoLenh, docTiGia, docYeuCau, laDangCho } from '../../utils/doiLuongDb'
import { tenTrangThaiLenh } from '../../utils/lenhNhanVat'

export default defineEventHandler(async (event) => {
  const u = await requireUser(event)
  const userId = Math.trunc(u.id)
  const ngay = ngayVN()
  const so = async (k: string) => Math.max(0, Math.trunc(Number(await chGiaTri(k)) || 0))
  const [tiLe, phanTramGio, tranXu, phiSan, tranSan] = await Promise.all([
    so('doi_luong.ti_le'), so('doi_luong_xu.phan_tram_gio'), so('doi_luong_xu.tran_ngay'), so('san_luong.phi_phan_tram'), so('san_luong.tran_ngay'),
  ])
  const db = dbGame()
  let tiGia: Awaited<ReturnType<typeof docTiGia>> = null
  let daDoiXu = 0
  let daDatSan = 0
  let yeuCau: Awaited<ReturnType<typeof docYeuCau>> = []
  let san: Awaited<ReturnType<typeof docSoLenh>> = null
  try {
    ;[tiGia, daDoiXu, daDatSan, yeuCau, san] = await Promise.all([
      docTiGia(db, ngay), docDaDung(db, userId, 'doi_luong_xu', ngay), docDaDung(db, userId, 'san_luong', ngay), docYeuCau(db, userId), docSoLenh(db),
    ])
  } catch (e: any) {
    console.error('[doi-luong] doc loi:', e?.code ?? e)
  }
  // Yêu cầu lượng → xu đang chờ cũng giữ chỗ trong trần hiển thị (game kiểm lại lúc đổi).
  const choXu = yeuCau.filter(y => y.loai === LENH_DOI_LUONG_XU && laDangCho(y.trangThai)).reduce((s, y) => s + y.so, 0)
  const tiGiaHienTai = phanTramGio > 0 && tranXu > 0 ? (tiGia?.giaTri ?? 0) : 0
  return {
    ngay,
    tiLe,
    tiGia: tiGia && tiGiaHienTai > 0 ? { ...tiGia, phanTramGio } : null,
    tranXu,
    conXu: Math.max(0, tranXu - daDoiXu - choXu),
    phiSan,
    tranSan,
    conSan: Math.max(0, tranSan - daDatSan),
    san: san ? { ...san, giaTb24h: giaTrungBinh(san.tongXu24h, san.tongLuong24h) } : null,
    yeuCau: yeuCau.map(y => ({ ...y, moTa: moTaLenhDoi(y.loai, y.so), tenTrangThai: tenTrangThaiLenh(y.trangThai), dangCho: laDangCho(y.trangThai) })),
  }
})
