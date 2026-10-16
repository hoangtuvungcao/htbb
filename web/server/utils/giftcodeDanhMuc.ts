/**
 * ============================================================================
 *  DANH MỤC PHẦN THƯỞNG cho bộ chọn của trang Giftcode — PHẦN THUẦN
 * ============================================================================
 *  Tệp này KHÔNG đụng CSDL: nó nhận các dòng đã đọc (data_item / data_potion) rồi dựng
 *  danh mục, nên logic dựng nhóm tách hẳn khỏi chuyện kết nối. Phần đọc CSDL nằm ở
 *  server/utils/giftcode.ts.
 *
 *  VÌ SAO CÓ BỘ CHỌN. Ô "Phần thưởng vật phẩm" trước đây là ô trống, phải tự nhớ id —
 *  mà id nằm rải ở NĂM nguồn khác nhau:
 *      data_item        -> ITEM, và cột atb9 lọc ra "thời trang" -> TT
 *      data_potion      -> PT
 *      Map.gemTemplate  (270 viên, GHI CỨNG TRONG JAVA)              -> GEM
 *      Map.petTemplate  (31 con, GHI CỨNG TRONG JAVA, id = CHỈ SỐ MẢNG) -> PET / PETV
 *      DanhHieu.getName (48 tên, GHI CỨNG TRONG JAVA trong một switch) -> DH
 *
 *  BỘ CHỌN LÀ BỘ SINH CHUỖI, KHÔNG PHẢI ĐƯỜNG ĐI RIÊNG. Nó chỉ ghép chuỗi rồi chèn vào
 *  chính ô văn bản cũ; chuỗi đó vẫn đi qua kiemTraItem() (shared/utils/thuong.ts) ở
 *  máy chủ y như khi gõ tay. Không thể tạo ra một loại dữ liệu sai mới, và người quen
 *  gõ tay / dán từ mã cũ vẫn làm được như trước.
 *
 *  QUÀ GIFTCODE LÀ QUÀ KHOÁ:
 *    ITEM   - khoá sẵn, không tham số nào mở được (Char.createItemSoLuong: lock = 1).
 *    TT     - có chọn, mặc định khoá (1 = khoá, khác chiều dấu của GEM).
 *    GEM    - có chọn, mặc định khoá (doAddGemItem(id, sl, khoa < 0)).
 *    PT     - không có khái niệm khoá (ô thuốc chỉ là con số trong mảng potions).
 *    TK/TKMAX - KHÔNG BAO GIỜ khoá được: Char.createThanKhi() đặt cứng newItem.lock = 0.
 *
 *  CÁC BẪY ĐÃ ĐỌC TỪ src/data/DatabaseGiftCode.java:
 *   1. GEM thiếu phần thứ tư thì hành vi đến từ một khối catch (khoá = true) — bộ chọn
 *      LUÔN ghi đủ bốn phần: GEM:108:5:-1.
 *   2. Trứng phượng hoàng dùng PT, KHÔNG dùng EGG (EGG chỉ hiểu 4 giá trị, không nhánh
 *      else, không làm mới hành trang).
 *   3. PT ghi thẳng vào mảng potions (162 ô) không kiểm biên — id ngoài dải ném lỗi GIỮA
 *      lúc phát quà. Nên danh mục thuốc chỉ lấy id 0..161 và có tên.
 *
 *  Cố ý CHƯA đưa vào bộ chọn (vẫn gõ tay): AOTT (12 id ghi cứng — dùng TT an toàn hơn),
 *  TKF/TKMAXF (quy tắc dòng ẩn theo màu quá rối), PPRK/PPVK chọn kháng (chỉ có tác dụng
 *  thật với đúng id 750).
 */
import {
  DM_SO_O_THUOC, dmCoTu, dmDonViThoiHanLoai, dmIdCanh, dmIdRac, dmLoaiTypeDoChe,
  dmMotDong, dmTenLoaiVatPham, loaiThuong,
} from '../../shared/utils/thuong'
import { danhMucNgoc } from './giftcodeNgoc.gen'
import { danhMucThu } from './giftcodeThu.gen'
import { danhMucDanhHieu } from './giftcodeDanhHieu.gen'

/**
 * Một mục trong nhóm: id (hoặc chỉ số) + nhãn. Dùng MẢNG chứ không dùng object có khoá
 * số: object JS luôn sắp khoá số tăng dần, làm mất thứ tự type-rồi-id đã chọn.
 */
export interface MucDanhMucGiftcode {
  id: number
  ten: string
}

export interface NhomDanhMucGiftcode {
  /** khoá nhóm: nlsc6, trung_thu, thoi_trang, vat_pham... */
  ma: string
  /** nhãn hiện trong ô "Nhóm" */
  ten: string
  /** mã loại phần thưởng sẽ sinh ra */
  loai: string
  /** câu giải thích hiện dưới ô chọn */
  ghiChu: string
  muc: MucDanhMucGiftcode[]
}

export interface DongDataItem { id: number, name: string | null, type: number, level?: number | null }
export interface DongTen { id: number, name: string | null }

/**
 * dmVatPham(): từ các dòng `SELECT id, name, type, level FROM data_item [WHERE type IN ..]
 * ORDER BY type, id`, trả về Map<type, mục[]> — giữ thứ tự type rồi id.
 *
 * Loại rác (dmIdRac) NGAY TẠI ĐÂY — đây là điểm chung của mọi nhóm vật phẩm, kể cả
 * nhóm bắt hết "Vật phẩm khác", nên lọc một lần ở đây là chặn được ở mọi nơi.
 *
 * Tên TRÙNG THẬT trong đúng tập đang lấy (vd "Phi phong Lăng Vân" 584/596/597, level
 * 35/95/100) được gắn "(Lv N)" — CHỈ gắn cho tên trùng, không gắn tràn lan: phần lớn
 * vật phẩm khác có level 1 hoặc dùng chung một level, gắn vào chỉ là nhiễu.
 */
export function dmVatPhamTuDong(dong: DongDataItem[]): Map<number, MucDanhMucGiftcode[]> {
  const idRac = new Set(dmIdRac())
  const demTen = new Map<string, number>()
  for (const d of dong) {
    const ten = dmMotDong(d.name)
    demTen.set(ten, (demTen.get(ten) ?? 0) + 1)
  }
  const ra = new Map<number, MucDanhMucGiftcode[]>()
  for (const d of dong) {
    const id = Math.trunc(Number(d.id))
    if (idRac.has(id)) continue
    let ten = dmMotDong(d.name)
    if ((demTen.get(ten) ?? 0) > 1) {
      const level = Math.trunc(Number(d.level ?? 0))
      ten += level > 0 ? ` (Lv ${level})` : ` (id ${id})`
    }
    const type = Math.trunc(Number(d.type))
    if (!ra.has(type)) ra.set(type, [])
    ra.get(type)!.push({ id, ten })
  }
  return ra
}

/** dmThuoc(): dòng data_potion đã lọc (có tên, id 0..DM_SO_O_THUOC-1) -> mục. */
export function dmThuocTuDong(dong: DongTen[]): MucDanhMucGiftcode[] {
  // Lọc lại cả ở đây (SQL đã lọc) để hàm thuần tự đứng vững được.
  return dong
    .filter(d => d.name !== null && d.name !== '' && d.id >= 0 && d.id < DM_SO_O_THUOC)
    .map(d => ({ id: Math.trunc(Number(d.id)), ten: dmMotDong(d.name) }))
}

export interface NguonDanhMucGiftcode {
  /** dmVatPham(game, [21, 26, 29, 30, 19, 27, 25, 28]) — dòng thô */
  vatPhamChon: DongDataItem[]
  /** dmVatPham(game) — mọi dòng data_item (cho nhóm bắt hết) */
  vatPhamTatCa: DongDataItem[]
  /** data_potion có tên, id trong dải */
  thuoc: DongTen[]
  /** data_item WHERE atb9 IN (1, 2) AND type IN (0, 1, 2, 13) ORDER BY type, id */
  thoiTrang: DongTen[]
  /** data_item WHERE id IN (890, 891, 892) */
  thanKhi: DongTen[]
}

/** Các type vật phẩm mà các nhóm riêng dùng (một câu đọc chung cho tất cả các nhóm đó). */
export const DM_TYPE_NHOM_RIENG = [21, 26, 29, 30, 19, 27, 25, 28] as const

/**
 * Dựng toàn bộ danh mục cho bộ chọn. Thứ tự ở đây là thứ tự hiện ra: nhóm hay dùng
 * lên trước, các nhóm "tất cả" xuống cuối.
 */
export function dmDungDanhMuc(nguon: NguonDanhMucGiftcode): NhomDanhMucGiftcode[] {
  const ngoc = danhMucNgoc()
  const idNgoc = Object.keys(ngoc).map(Number).sort((a, b) => a - b)
  const nhomNgoc = new Map<string, MucDanhMucGiftcode[]>()
  for (const id of idNgoc) {
    const g = ngoc[id]!
    if (g.nhom !== '') {
      if (!nhomNgoc.has(g.nhom)) nhomNgoc.set(g.nhom, [])
      nhomNgoc.get(g.nhom)!.push({ id, ten: g.ten })
    }
  }

  const dm: NhomDanhMucGiftcode[] = []

  // ---- Ngọc nguyên liệu: NLSC / NLCC theo cấp ----
  // Người vận hành gọi là "NLSC 6" / "NLCC 6"; id thật 73/80/87/94/101 và
  // 108/115/122/129/136 — không ai nhớ.
  for (const [tt, tenDai] of [['NLSC', 'Nguyên liệu sơ cấp'], ['NLCC', 'Nguyên liệu cao cấp']] as const) {
    for (let cap = 1; cap <= 6; cap++) {
      const ma = tt + cap
      const muc = nhomNgoc.get(ma)
      if (!muc || muc.length === 0) continue
      dm.push({
        ma: ma.toLowerCase(),
        ten: `${tenDai} cấp ${cap} (${ma})`,
        loai: 'GEM',
        ghiChu: `Năm hệ của ${ma}. Không bán trong cửa hàng NPC.`,
        muc,
      })
    }
  }

  // ---- Ngọc khác không bán trong cửa hàng ----
  const ngocKhac = idNgoc.filter(id => !ngoc[id]!.ban && ngoc[id]!.nhom === '').map(id => ({ id, ten: ngoc[id]!.ten }))
  if (ngocKhac.length) {
    dm.push({
      ma: 'ngoc_khong_ban',
      ten: 'Ngọc khác — không bán trong cửa hàng',
      loai: 'GEM',
      ghiChu: 'Những viên người chơi không mua được, chỉ nhận qua sự kiện hoặc giftcode.',
      muc: ngocKhac,
    })
  }

  // ---- Trứng ----
  // Ba họ trứng nằm ở ba nguồn khác nhau -> ba nhóm rõ ràng; gộp thì không biết món
  // đang chọn sẽ sinh ra ITEM hay PT.
  const vp = dmVatPhamTuDong(nguon.vatPhamChon)

  // Nhánh hoa đào là data_item 835, khác Hoa đào trong ô potion.
  const hoaDao = (vp.get(28) ?? []).find(m => m.id === 835)
  if (hoaDao) {
    dm.push({
      ma: 'nhanh_hoa_dao',
      ten: 'Nhánh hoa đào',
      loai: 'ITEM',
      ghiChu: 'Max chỉ số: sát thương chuẩn 24,1%; chỉ số vũ khí tăng 50,1%; tỷ lệ gây sát thương chuẩn 29,0%. Vật phẩm khóa, vĩnh viễn.',
      muc: [hoaDao],
    })
  }

  if ((vp.get(26) ?? []).length) {
    dm.push({ ma: 'trung_thu', ten: 'Trứng thú', loai: 'ITEM', ghiChu: 'data_item type 26 — trứng nở ra thú nuôi.', muc: vp.get(26)! })
  }

  // Trứng linh thú nằm lẫn trong type 21 (Hộ vệ lệnh). Lọc theo TÊN chứ không ghi cứng
  // id: thêm trứng mới là tự hiện. 883-886 đã bị dmIdRac() loại từ trước — còn đúng 887.
  const trungLinh = (vp.get(21) ?? []).filter(m => dmCoTu(m.ten, 'rứng'))
  if (trungLinh.length) {
    dm.push({ ma: 'trung_linh', ten: 'Trứng thú cưỡi', loai: 'ITEM', ghiChu: 'Nằm lẫn trong data_item type 21, lọc theo tên.', muc: trungLinh })
  }

  const thuoc = dmThuocTuDong(nguon.thuoc)
  const trungThuoc = thuoc.filter(m => dmCoTu(m.ten, 'rứng'))
  if (trungThuoc.length) {
    dm.push({
      ma: 'trung_thuoc',
      ten: 'Trứng phượng hoàng / hoả kỳ lân / đương khang…',
      loai: 'PT',
      ghiChu: 'Mấy loại trứng này KHÔNG phải vật phẩm trong hành trang — trong '
        + 'game chúng là ô thuốc (data_item không có dòng nào tên phượng '
        + 'hoàng cả). Nên phải dùng loại PT, không phải ITEM.',
      muc: trungThuoc,
    })
  }

  // ---- Cánh: 5 món, TÁCH RIÊNG khỏi type 29 ----
  // ĐÃ SỬA: loại này TỪNG là ITEM — sai. Cánh không có chỉ số trong cột atb; chỉ số
  // THẬT chỉ được gán khi tạo qua Char.createThoiTrang() (loại TT, gọi createOptionCanh),
  // KHÔNG qua Char.createItemSoLuong() (loại ITEM). Phát Cánh bằng ITEM ra món 0 chỉ số.
  const idCanh = new Set(dmIdCanh())
  const muaCanh = (vp.get(29) ?? []).filter(m => idCanh.has(m.id))
  if (muaCanh.length) {
    dm.push({
      ma: 'canh',
      ten: 'Cánh',
      loai: 'TT',
      ghiChu: 'Trang trí cánh — hệ riêng (Char.wModel.CanhModel), không phải mảnh '
        + 'cải trang dù cùng nằm chung data_item type 29. Dùng TT (không phải '
        + 'ITEM) vì chỉ số của cánh chỉ được gán đúng qua đường này.',
      muc: muaCanh,
    })
  }

  // ---- Thời trang: cột atb9 IN (1,2), KHÔNG phải type — xem dmThoiTrang ở giftcode.ts ----
  const thoiTrang = nguon.thoiTrang.map(d => ({ id: Math.trunc(Number(d.id)), ten: dmMotDong(d.name) }))
  if (thoiTrang.length) {
    dm.push({
      ma: 'thoi_trang',
      ten: `Thời trang (${thoiTrang.length})`,
      loai: 'TT',
      ghiChu: 'Áo / mặt nạ / nón / chổi bay thời trang — cột atb9, không phải type. '
        + 'Gõ vào ô tìm để lọc theo tên hoặc id.',
      muc: thoiTrang,
    })
  }

  // ---- Thú nuôi (Map.petTemplate, KHÔNG PHẢI data_item) ----
  // id ở đây là CHỈ SỐ MẢNG (0..30), Char.createPet()/createPetVinhVien() đọc thẳng
  // không kiểm biên.
  const thu = danhMucThu()
  const mucThu = Object.keys(thu).map(Number).sort((a, b) => a - b).map(id => ({ id, ten: thu[id]! }))
  if (mucThu.length) {
    dm.push({
      ma: 'thu_vinh_vien',
      ten: 'Thú nuôi — vĩnh viễn',
      loai: 'PETV',
      ghiChu: `Chỉ số ở đây là THỨ TỰ trong danh sách (0–${mucThu.length - 1}), không phải id vật phẩm. Chỉ số 5 và 28 trùng tên `
        + '"Lồng đèn hoa sen" — đúng là hai con khác nhau trong game, không '
        + 'phải lỗi hiển thị.',
      muc: mucThu,
    })
    dm.push({
      ma: 'thu_thoi_han',
      ten: 'Thú nuôi — có thời hạn',
      loai: 'PET',
      ghiChu: 'Cùng danh sách với "Thú nuôi — vĩnh viễn", nhưng có thêm số ngày.',
      muc: mucThu,
    })
  }

  // ---- Thần khí: chỉ đúng 3 id (890/891/892), xem src/ThanKhi/ThoRenThanBi.java ----
  const thanKhi = nguon.thanKhi.map(d => ({ id: Math.trunc(Number(d.id)), ten: dmMotDong(d.name) }))
  if (thanKhi.length) {
    // CẢNH BÁO CHÍNH SÁCH: Char.createThanKhi() đặt CỨNG newItem.lock = 0, bỏ qua tham số
    // khoá. Thần khí phát qua TK/TKMAX LUÔN giao dịch được — hành vi của máy chủ, không
    // phải thiếu sót của bộ chọn.
    const ghiChuThanKhi = 'Chỉ đúng 3 id này được hệ thần khí nhận diện. '
      + 'LƯU Ý: khác mọi loại khác trong bộ chọn, thần khí phát qua giftcode '
      + 'KHÔNG BAO GIỜ bị khoá — máy chủ luôn tạo ở trạng thái giao dịch được, '
      + 'không có cách nào đặt khoá qua giftcode.'
    dm.push({ ma: 'than_khi', ten: 'Thần khí', loai: 'TK', ghiChu: ghiChuThanKhi, muc: thanKhi })
    dm.push({ ma: 'than_khi_max', ten: 'Thần khí — tối đa chỉ số', loai: 'TKMAX', ghiChu: ghiChuThanKhi, muc: thanKhi })
  }

  // ---- Danh hiệu ----
  const danhHieu = danhMucDanhHieu()
  const idDh = Object.keys(danhHieu).map(Number).sort((a, b) => a - b)
  if (idDh.length) {
    // id 0 KHÔNG phải một danh hiệu thật — thêm tay làm mục đầu, ghi rõ nó là gì.
    const muc: MucDanhMucGiftcode[] = [{
      id: 0,
      ten: 'Ngẫu nhiên — chỉ ra một trong 34 danh hiệu KHÔNG đánh dấu "(không random được)" bên dưới',
    }]
    for (const id of idDh) {
      const d = danhHieu[id]!
      muc.push({ id, ten: d.ten + (d.ngauNhien ? '' : ' (không random được)') })
    }
    dm.push({
      ma: 'danh_hieu',
      ten: 'Danh hiệu',
      loai: 'DH',
      ghiChu: 'Chỉ 34/48 danh hiệu random được bằng cách gõ id 0 — 14 danh hiệu '
        + 'đánh dấu "(không random được)" PHẢI chọn đúng tên ở đây, gõ id=0 sẽ '
        + 'không bao giờ ra chúng.',
      muc,
    })
  }

  // ---- Các nhóm hay phát khác ----
  // Skin linh thú đi loại DB (có ô thời hạn phút, qua GiftCode.AddItem) chứ không phải
  // ITEM: trước 12/09/2026 nhóm này sinh "ITEM:911:1" -> Kim Cang Long 0 chỉ số và không
  // đặt được hạn. Java đã vá để ITEM cũng roll, nhưng ITEM vẫn không có thời hạn.
  const ghiChuSkin = 'Đổi hình thú cưỡi. 5 skin đầu (905–909) chỉ số cố định theo hệ; '
    + 'Kim Cang Long (911) roll ngẫu nhiên 2–3 dòng (bạo kích / sát thương chuẩn / tăng công) '
    + 'lúc phát. Thời hạn tính bằng PHÚT, 0 = vĩnh viễn (1 ngày = 1440, 7 ngày = 10080).'
  const nhomKhac: [number, string, string, string, string][] = [
    [29, 'manh_caitrang', 'Mảnh cải trang', 'Ghép thành bộ cải trang Bát Giới / Sa Tăng.', 'ITEM'],
    [30, 'skin_thu', 'Skin thú cưỡi', ghiChuSkin, 'DB'],
    [19, 'phi_phong', 'Phi phong', 'Loại thưởng ITEM cho phi phong thường; '
      + 'muốn chọn kháng thì dùng PPRK và gõ tay.', 'ITEM'],
    [27, 'thu_nuoi', 'Thú nuôi', 'Thú nuôi dạng vật phẩm trong hành trang.', 'ITEM'],
    [25, 'hoa_ky_lan', 'Hoả kỳ lân', '', 'ITEM'],
  ]
  for (const [type, ma, ten, ghiChu, loai] of nhomKhac) {
    let muc = vp.get(type) ?? []
    // Bỏ 5 món Cánh đã tách ra nhóm riêng — còn đúng 24 mảnh cải trang thật (848–871).
    if (type === 29) muc = muc.filter(m => !idCanh.has(m.id))
    if (muc.length) dm.push({ ma, ten, loai, ghiChu, muc })
  }

  // ---- Thuốc / bình ----
  if (thuoc.length) {
    dm.push({
      ma: 'thuoc',
      ten: 'Thuốc / bình (tất cả)',
      loai: 'PT',
      ghiChu: `Chỉ id 0–${DM_SO_O_THUOC - 1} và có tên. Id ngoài dải làm server ném lỗi giữa lúc phát quà.`,
      muc: thuoc,
    })
  }

  // ---- Bắt hết: mọi vật phẩm KHÔNG PHẢI đồ chế ----
  // Để cuối vì dài, nhưng phải có: thiếu nó thì người vận hành lại quay về gõ id tay.
  const boDiType = new Set(dmLoaiTypeDoChe())
  const tenLoai = dmTenLoaiVatPham()
  const mucVp: MucDanhMucGiftcode[] = []
  for (const [type, ds] of dmVatPhamTuDong(nguon.vatPhamTatCa)) {
    if (boDiType.has(type)) continue
    for (const m of ds) {
      // Cánh mượn chung type 29 với mảnh cải trang — nhãn riêng để không lặp lại lỗi cũ.
      const nhan = idCanh.has(m.id) ? 'Cánh' : (tenLoai[type] ?? `type ${type}`)
      mucVp.push({ id: m.id, ten: `[${nhan}] ${m.ten}` })
    }
  }
  if (mucVp.length) {
    dm.push({
      ma: 'vat_pham',
      ten: `Vật phẩm khác (${mucVp.length})`,
      loai: 'ITEM',
      ghiChu: 'Đã bỏ đồ chế ở thợ rèn thần bí (vũ khí, trang bị, trang bị thú, '
        + 'ngọc trang bị). Cần phát mấy món đó thì gõ tay ITEM:id:số lượng. '
        + 'Gõ vào ô tìm để lọc theo tên hoặc id.',
      muc: mucVp,
    })
  }

  const mucNgoc = idNgoc.map(id => ({ id, ten: ngoc[id]!.ten + (ngoc[id]!.ban ? '' : ' — không bán') }))
  dm.push({
    ma: 'ngoc',
    ten: `Tất cả ngọc (${mucNgoc.length})`,
    loai: 'GEM',
    ghiChu: 'Tên ngọc không có trong CSDL — bảng này sinh từ mã nguồn, xem '
      + 'tools/SinhDanhMucNgoc.java.',
    muc: mucNgoc,
  })

  return dm
}

// ============================================================================
//  GIẢI THÍCH CHUỖI PHẦN THƯỞNG THÀNH VĂN BẢN ĐỌC ĐƯỢC
// ============================================================================
// Cột "Phần thưởng" in chuỗi thô kèm bản dịch từng đoạn ra tên thật, dùng CHÍNH các
// danh mục của bộ chọn — nên tên hiện ra ở đây LUÔN khớp tên trong bộ chọn.
//
// CHỈ ĐỂ HIỂN THỊ, KHÔNG DÙNG ĐỂ KIỂM TRA HAY GHI XUỐNG: kiemTraItem() vẫn là nơi duy
// nhất chặn dữ liệu sai; đọc sai một đoạn khó hiểu chỉ làm dòng đó hiển thị xấu.
//
// Phủ hết các loại trong loaiThuong(), độ chi tiết không đều vì bằng chứng không đều:
//   - loại có danh mục riêng -> TRA RA TÊN THẬT;
//   - loại chỉ nhận thời hạn -> in mô tả có sẵn + số ngày/phút ĐÃ XÁC MINH đơn vị;
//   - loại còn lại -> in số thô, KHÔNG bịa đơn vị chưa xác minh.

/** Nguồn tên cho bộ giải thích. Phía máy chủ nạp sẵn trước (xem gcMoTaNhieu). */
export interface NguonTenGiftcode {
  /** tên data_item theo id (đã qua dmMotDong), null nếu không có */
  tenItem: (id: number) => string | null
  /** danh mục thuốc (dmThuoc) — id => tên */
  thuoc: () => Map<number, string>
}

/** Các loại tra thẳng data_item bằng id thật. */
export const DM_LOAI_TRA_DATA_ITEM = [
  'ITEM', 'DB', 'TT', 'AOTT', 'AOSHOP', 'TRANGPHUC', 'MATNA2', 'MATNAID', 'HLT', 'VKID',
  'PPR', 'PPV', 'PPRK', 'PPVK', 'PPLUCK', 'HOVELAN', 'CHOI', 'TK', 'TKMAX', 'TKF', 'TKMAXF',
] as const

/** Ánh xạ EGG -> Char.createTrungPhuongHoang(): CHỈ 4 giá trị này có nghĩa. */
export const DM_EGG_TEN: Record<number, string> = {
  16: 'Phượng hoàng',
  17: 'Trứng phượng hoàng băng',
  18: 'Trứng Hoả kỳ lân',
  19: 'Sách kỹ năng pet (KHÔNG phải trứng)',
}

/** Thân thật của dmTenTheoLoai(). */
export function dmTenTheoLoaiKhongCache(nguon: NguonTenGiftcode, loai: string, id: number): string | null {
  if ((DM_LOAI_TRA_DATA_ITEM as readonly string[]).includes(loai)) return nguon.tenItem(id)
  switch (loai) {
    case 'PT':
      return nguon.thuoc().get(id) ?? null
    case 'GEM':
      return danhMucNgoc()[id]?.ten ?? null
    case 'PET': case 'PETV':
      // CHỈ SỐ MẢNG, không phải id vật phẩm.
      return danhMucThu()[id] ?? null
    case 'DH':
      if (id === 0) return 'Ngẫu nhiên (một trong 34 danh hiệu cũ)'
      return danhMucDanhHieu()[id]?.ten ?? null
    case 'DANHHIEU2':
      return danhMucDanhHieu()[id]?.ten ?? null
    case 'CAITRANG':
      // Chỉ 903/904 làm việc — xem doUsePotion/Char.createItemCaiTrang.
      if (id === 903) return 'Cải Trang Bát Giới'
      if (id === 904) return 'Cải Trang Sa Tăng'
      return null
    case 'MATNA':
      // option là CHỈ SỐ 0..4 vào Char.ID_MAT_NA = {745..749}, KHÔNG PHẢI id vật phẩm.
      if (id === -1) return 'Ngẫu nhiên'
      if (id >= 0 && id <= 4) return nguon.tenItem(745 + id)
      return null
    case 'EGG':
      return DM_EGG_TEN[id] ?? null
  }
  return null
}

/**
 * Tra một id ra tên. null nếu loại đó không có id để tra hoặc không tìm thấy.
 * Chỉ nhận chuỗi số nguyên (cho phép dấu '-' ở đầu); chuỗi lạ thì không tra.
 */
export function dmTenTheoLoai(nguon: NguonTenGiftcode, loai: string, idRaw: string): string | null {
  const boDau = idRaw.replace(/^-+/, '')
  if (idRaw === '' || idRaw === '-' || !/^[0-9]+$/.test(boDau)) return null
  // "--5" qua được bước bỏ dấu ở trên nhưng không phải một số -> coi như 0.
  const id = /^-?[0-9]+$/.test(idRaw) ? Number(idRaw) : 0
  return dmTenTheoLoaiKhongCache(nguon, loai, id)
}

/**
 * Tách một chuỗi phần thưởng đầy đủ thành các dòng mô tả. KHÔNG BAO GIỜ ném lỗi — dữ
 * liệu cũ có thể có đoạn hỏng; gặp đoạn không hiểu thì in nguyên văn kèm dấu hỏi.
 */
export function dmMoTaChuoiThuong(nguon: NguonTenGiftcode, chuoiDayDu: string): string[] {
  const chuoi = String(chuoiDayDu ?? '').trim()
  if (chuoi === '') return []
  const bangLoai = loaiThuong()
  const donViThoiHan = dmDonViThoiHanLoai()
  const ra: string[] = []

  for (const doanTho of chuoi.split(',')) {
    const doan = doanTho.trim()
    if (doan === '') continue
    const tach = doan.split(':').map(s => s.trim())
    const loai = tach[0]!.toUpperCase()
    const dong0 = Object.prototype.hasOwnProperty.call(bangLoai, loai) ? bangLoai[loai] : undefined
    if (!dong0) {
      ra.push(`? "${doan}" (không rõ loại)`)
      continue
    }
    const [, cuPhap, moTa] = dong0
    // Tên tham số theo đúng thứ tự ghi trong cú pháp, bỏ phần đầu (tên loại).
    const tenThamSo = cuPhap.split(':').slice(1)

    // Vị trí đầu là id NẾU cú pháp gọi nó là một biến thể của "id" — quy ước xuyên suốt
    // bảng loaiThuong(). MATNA dùng "option", EGG dùng "loai" — liệt kê thẳng.
    const tenDau = tenThamSo[0] ?? ''
    let laThamSoId = tenDau !== '' && tenDau !== '_' && tenDau.toLowerCase().includes('id')
    laThamSoId = laThamSoId || tenDau === 'option' || loai === 'EGG'

    let tenThat: string | null = null
    if (laThamSoId && tach[1] !== undefined) {
      try {
        tenThat = dmTenTheoLoai(nguon, loai, tach[1])
      } catch {
        tenThat = null
      }
    }

    // Có tên thật thì DÙNG TÊN LÀM TIÊU ĐỀ — mô tả trong loaiThuong() viết cho bảng tham
    // khảo (nhiều loại dài cả đoạn cảnh báo), nhét vào đây thành dòng dài không ai đọc.
    let dong: string
    if (tenThat !== null) dong = tenThat
    else if (laThamSoId && tach[1] !== undefined) dong = `${moTa} (id ${tach[1]} — không tìm thấy tên)`
    else dong = moTa

    const phanConLai: string[] = []
    tenThamSo.forEach((ten, i) => {
      const viTri = i + 1 // +1 vì tach[0] là tên loại
      if (ten === '_' || tach[viTri] === undefined) return
      if (i === 0 && laThamSoId) return // đã dùng ở trên
      const giaTri = tach[viTri]!
      if (ten === 'thoiHan' && donViThoiHan[loai] !== undefined) {
        phanConLai.push(giaTri === '0' ? 'vĩnh viễn' : `${giaTri} ${donViThoiHan[loai]}`)
      } else if (ten === 'soNgay') {
        // Chính cái tên "số ngày" đã nói rõ đơn vị.
        phanConLai.push(giaTri === '0' ? 'vĩnh viễn' : `${giaTri} ngày`)
      } else if (ten === 'soLuong') {
        phanConLai.push(`số lượng ${giaTri}`)
      } else if (ten === 'khoa') {
        // Quy ước dấu khác nhau giữa các loại (GEM: âm là khoá; TT: 1 là khoá) — không
        // đoán, chỉ in nguyên giá trị kèm nhãn.
        phanConLai.push(`khoá=${giaTri}`)
      } else {
        phanConLai.push(`${ten}=${giaTri}`)
      }
    })
    if (phanConLai.length) dong += ` (${phanConLai.join(', ')})`
    ra.push(dong)
  }
  return ra
}
