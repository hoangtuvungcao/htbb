/**
 * Đọc vật phẩm của nhân vật từ ba cột `equip`, `inven`, `bag` của bảng `tob_char`.
 *
 * KHÔNG CÓ BẢNG tob_item trên server này. Trang bị / hành trang / rương được
 * serialize thẳng vào ba cột nói trên (xem ghi chú ở sql/wipe_player_data.sql).
 *
 * ĐỊNH DẠNG (lấy từ chính bộ ghi và bộ nạp của server, không phải đoán):
 *
 *   cột       = nhiều vật phẩm, ngăn nhau bằng  ">"
 *   vật phẩm  = <phần dbInfo>  "|"  <phần thuộc tính>
 *   dbInfo    = 33 số/chuỗi ngăn nhau bằng ","
 *
 * Nguồn đối chiếu:
 *   ghi  : real/Item.java  getInfoSave() -> getDBInfo() + "|" + getAttribute()
 *   đọc  : real/Item.java  initInfoFromDB()   (thứ tự trường lấy từ đây)
 *   ghép : data/DatabaseNhanVat.java  saveChar*()
 *   tách : real/CharTrangBi.java  setitemInvenBag()
 *
 * HAI ĐIỀU DỄ SAI, đều lấy quy tắc từ bộ nạp của server:
 *
 *   1. Cột `inven` có phần "thời trang đang mặc" NỐI THÊM vào cuối
 *      (DatabaseNhanVat: invent.append(">").append(infomodel)). Chúng cùng định
 *      dạng nên không có ranh giới nào để cắt. Server phân biệt bằng
 *      `place == 4` (CharTrangBi:514) — ở đây làm y hệt.
 *
 *   2. Chuỗi có thể bắt đầu bằng ">" (khi nhân vật không có vật phẩm nào trong
 *      túi nhưng có thời trang). Server tự cắt dấu đó đi trước khi tách.
 *
 * Các hằng số VP_* phải khớp với Item.initInfoFromDB() bên Java: server thêm một
 * trường vào GIỮA getDBInfo() là mọi vị trí phía sau dịch một ô, trang admin hiện sai
 * toàn bộ mà không báo lỗi gì — đổi bên Java thì phải đối chiếu lại ở đây.
 */
import { q } from './db'

/** Vị trí các trường trong phần dbInfo — thứ tự lấy từ Item.initInfoFromDB(). */
export const VP_CLAZZ = 0
export const VP_TEMPLATE = 1
export const VP_PLACE = 2
export const VP_DURABLE = 3
export const VP_MDURABLE = 4
export const VP_PLUS = 5
export const VP_LEVEL = 6
export const VP_MINUTE_EXIST = 10
export const VP_COLOR = 11
export const VP_LOCK = 14
export const VP_DATE_CREATE = 19
export const VP_TINH_LUYEN = 30

/** place == 4 nghĩa là thời trang đang mặc, không phải đồ trong túi. */
export const VP_PLACE_THOI_TRANG = 4

export interface VatPhamNhanVat {
  /** Vị trí mẩu này trong mảng split('>') của chuỗi gốc (sau khi cắt '>' đầu chuỗi). */
  _i: number
  template: number
  place: number
  plus: number
  level: number
  mau: number
  khoa: boolean
  tinh_luyen: number
  thue_phut: number
  ngay_tao: string
}

/**
 * Trường có phải số không (nhận cả dạng thập phân / số mũ như "1.0", "1e3"; sau đó cắt
 * phần lẻ). Trường hỏng/không phải số thì dùng giá trị mặc định thay vì làm hỏng cả món.
 */
function laChuoiSo(s: string): boolean {
  return /^[+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?$/.test(s)
}

/**
 * Tách một cột thành danh sách vật phẩm.
 *
 * Bỏ qua (không làm hỏng cả danh sách) những mẩu không đọc được: dữ liệu cũ từ
 * các phiên bản trước có thể thiếu trường ở cuối, và chính server cũng bọc mỗi
 * vật phẩm trong try/catch riêng khi nạp.
 */
export function docChuoiVatPham(chuoiVao: string | null | undefined): VatPhamNhanVat[] {
  if (chuoiVao === null || chuoiVao === undefined) return []
  let chuoi = chuoiVao.trim()
  // Server cắt dấu ">" đầu chuỗi trước khi tách (CharTrangBi:358).
  if (chuoi !== '' && chuoi[0] === '>') chuoi = chuoi.slice(1)
  if (chuoi === '') return []

  const ra: VatPhamNhanVat[] = []
  // '_i' = vị trí mẩu này trong mảng split('>') (sau khi đã cắt '>' đầu chuỗi giống
  // server) — dùng để XOÁ đúng mẩu, vì đây là CHUỖI THÔ duy nhất còn giữ dữ liệu đầy
  // đủ (các trường sau dấu '|' bị bỏ qua ở đây). Đánh số trên TOÀN BỘ mảng, kể cả mẩu
  // bị bỏ qua bên dưới, để vị trí luôn khớp với chuỗi gốc.
  chuoi.split('>').forEach((mau, i) => {
    if (mau.trim() === '') return
    const phan = mau.split('|')
    const t = phan[0]!.split(',')
    // Thiếu tới cả id template thì không còn gì để hiển thị.
    if (t.length <= VP_TEMPLATE) return
    const lay = (k: number, macDinh = 0): number => {
      const v = t[k]
      if (v === undefined) return macDinh
      const s = v.trim()
      return laChuoiSo(s) ? Math.trunc(Number(s)) : macDinh
    }
    ra.push({
      _i: i,
      template: lay(VP_TEMPLATE, -1),
      place: lay(VP_PLACE, -1),
      plus: lay(VP_PLUS),
      level: lay(VP_LEVEL),
      mau: lay(VP_COLOR),
      khoa: lay(VP_LOCK) === 1,
      tinh_luyen: lay(VP_TINH_LUYEN),
      thue_phut: lay(VP_MINUTE_EXIST),
      ngay_tao: t[VP_DATE_CREATE] !== undefined ? t[VP_DATE_CREATE]!.trim() : '',
    })
  })
  return ra
}

/**
 * Xoá đúng một vật phẩm khỏi chuỗi thô (equip/inven/bag), theo vị trí '_i' trả về
 * từ docChuoiVatPham().
 *
 * Giữ NGUYÊN phần thô của các vật phẩm còn lại (không parse rồi build lại từ các
 * trường đã rút gọn — sẽ làm hỏng dữ liệu các item khác). Chỉ cắt bỏ đúng một mẩu.
 * Web hiện KHÔNG tự ghi cột (đi qua lệnh xoa_vat_pham cho server game, vì nhân vật
 * online sẽ lưu đè); hàm này chỉ giữ lại cho công cụ sửa dữ liệu thủ công khi server tắt.
 */
export function xoaVatPhamTheoViTri(chuoiVao: string | null | undefined, viTri: number): string {
  let chuoi = String(chuoiVao ?? '').trim()
  if (chuoi !== '' && chuoi[0] === '>') chuoi = chuoi.slice(1)
  const mau = chuoi.split('>')
  if (!Number.isInteger(viTri) || viTri < 0 || viTri >= mau.length) {
    throw new Error(`Không tìm thấy vật phẩm ở vị trí ${viTri} để xoá.`)
  }
  mau.splice(viTri, 1)
  // Server tự cắt dấu '>' đầu chuỗi nếu có (CharTrangBi:358) nên luôn ghi bản không có.
  return mau.join('>')
}

/** Tách cột `inven` thành hai phần: đồ trong túi và thời trang đang mặc. */
export function tachTuiVaThoiTrang(inven: string | null | undefined): { tui: VatPhamNhanVat[], thoi_trang: VatPhamNhanVat[] } {
  const tui: VatPhamNhanVat[] = []
  const thoiTrang: VatPhamNhanVat[] = []
  for (const vp of docChuoiVatPham(inven)) {
    if (vp.place === VP_PLACE_THOI_TRANG) thoiTrang.push(vp)
    else tui.push(vp)
  }
  return { tui, thoi_trang: thoiTrang }
}

/** Tên màu vật phẩm — hằng số lấy từ real/Item.java (COLOR_WHITE = 0 …). */
export function tenMauVatPham(mau: number): string {
  const ten: Record<number, string> = {
    0: 'Trắng',
    1: 'Xanh lá',
    2: 'Đỏ',
    3: 'Xanh dương',
    4: 'Hồng',
    5: 'Vàng',
  }
  return ten[mau] ?? `màu ${mau}`
}

/** Tra tên vật phẩm theo id template, một câu cho cả danh sách. Lỗi -> {}. */
export async function tenVatPhamTheoId(game: Parameters<typeof q>[0], ids: number[]): Promise<Record<number, string>> {
  const ds = [...new Set(ids.filter(i => Number.isInteger(i) && i >= 0))]
  if (ds.length === 0) return {}
  try {
    const cho = ds.map(() => '?').join(',')
    const rows = await q<{ id: number, name: string }>(game, `SELECT id, name FROM data_item WHERE id IN (${cho})`, ds)
    const ra: Record<number, string> = {}
    for (const r of rows) ra[Number(r.id)] = String(r.name)
    return ra
  } catch {
    return {}
  }
}
