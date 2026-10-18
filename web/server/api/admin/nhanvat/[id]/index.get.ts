/**
 * Chi tiết một nhân vật + ba cột vật phẩm + lệnh đã gửi. Cả hai lối vào của trang
 * (?id= và tìm ra đúng một người) đều gọi API này.
 */
import type { NhanVatDong } from '../../../../utils/nhanVatAdmin'
import type { VatPhamNhanVat } from '../../../../utils/vatPham'

export default defineAdminHandler(async (event) => {
  const id = nvIdTuRoute(event)
  const ct = await q1<NhanVatDong>(dbGame(), `SELECT ${NV_COT} FROM tob_char WHERE id = ? LIMIT 1`, [id])
  if (!ct) throw apiLoi(404, `Không tìm thấy Pokémon #${id}.`)

  const chuTk = await nvChuNhan(Number(ct.userId))

  // Ba cột này là TEXT dài (cả hành trang serialize vào đó) nên chỉ lấy ở trang chi tiết
  // một nhân vật, KHÔNG thêm vào NV_COT — NV_COT còn dùng cho câu liệt kê.
  let vpRow: { equip?: string | null, inven?: string | null, bag?: string | null } = {}
  let loiVatPham = ''
  try {
    vpRow = (await q1(dbGame(), 'SELECT equip, inven, bag FROM tob_char WHERE id = ? LIMIT 1', [id])) ?? {}
  } catch (e: any) {
    loiVatPham = String(e?.message ?? e)
  }

  const vpMac = docChuoiVatPham(vpRow.equip ?? null)
  const vpTach = tachTuiVaThoiTrang(vpRow.inven ?? null)
  const vpTui = vpTach.tui
  const vpThoiTrang = vpTach.thoi_trang
  const vpRuong = docChuoiVatPham(vpRow.bag ?? null)

  const moiId: number[] = []
  for (const nhom of [vpMac, vpTui, vpThoiTrang, vpRuong]) for (const v of nhom) moiId.push(v.template)
  const tenVP = await tenVatPhamTheoId(dbGame(), moiId)

  const kem = (ds: VatPhamNhanVat[]) => ds.map(v => ({
    ...v,
    ten: tenVP[v.template] ?? null,
    ten_mau: tenMauVatPham(v.mau),
  }))

  // Lệnh đã gửi cho nhân vật này (mới nhất trước). Bảng chưa có -> rỗng, không lỗi.
  let lenh: ReturnType<typeof lenhChoGiaoDien>[] = []
  try {
    lenh = (await docLenhCuaNhanVat(dbGame(), id)).map(lenhChoGiaoDien)
  } catch {
    lenh = []
  }

  return {
    ct,
    chuTk,
    chuLaAdmin: chuTk !== '' && laTaiKhoanAdmin(chuTk),
    capDoToiDa: await capDoToiDa(),
    loiVatPham,
    vatPham: {
      mac: kem(vpMac),
      thoiTrang: kem(vpThoiTrang),
      tui: kem(vpTui),
      ruong: kem(vpRuong),
    },
    lenh,
  }
})
