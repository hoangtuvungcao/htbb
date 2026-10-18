/**
 * Phát quà đua top — phần THUẦN (không đụng CSDL).
 *
 * Nằm ở shared/utils nên chạy y hệt ở trình duyệt (bộ chọn, xem trước) lẫn máy chủ
 * (chốt chặn thật khi lưu).
 *
 * VÌ SAO CÓ TRANG NÀY (yêu cầu người dùng 19/09/2026): trước đây "đua top" chỉ có một
 * cờ bật/tắt sự kiện (SuKien.DUA_TOP_HOA_LU), không gắn với phần thưởng nào. Trang này
 * ghi các dòng "hạng X -> nhân vật Y -> phần thưởng Z" vào bảng `tob_top_thuong`; server
 * đọc bảng MỖI LẦN người chơi mở NPC "Tổng Quản" (real/cmd/RequestNPCInfoHandler.java,
 * case -10) — CHỈ nhân vật có tên trong bảng và CHƯA nhận mới thấy dòng menu "Nhận
 * thưởng đua top" (xem real/SuKienDuaTop.java).
 *
 * BỘ CHỌN PHẦN THƯỞNG dùng LẠI đúng danh mục + cú pháp chuỗi của giftcode
 * (shared/utils/thuong.ts: kiemTraItem(), dmTruongCuaLoai()) — server áp dụng bằng CHÍNH
 * hàm data.DatabaseGiftCode.applyItemString() mà giftcode đang dùng.
 */
import { kiemTraItem } from './thuong'
import type { TruongThuong } from './thuong'

export const DUA_TOP_SO_HANG = 10

/** Tên nhân vật hợp lệ: 2-50 ký tự gồm chữ (mọi bảng chữ, kể cả có dấu), số, dấu cách, gạch dưới. */
export const DUA_TOP_TEN_RE = /^[\p{L}0-9 _]{2,50}$/u

/**
 * Chỉ bỏ khoảng trắng ASCII ở hai đầu, KHÔNG dùng String.trim(): trim() của JS còn bỏ cả
 * khoảng trắng Unicode (NBSP...), còn server game (Java) thì không — nhãn lưu vào phải
 * khớp đúng cách server đọc lại.
 */
function catKhoangTrang(s: string): string {
  return s.replace(/^[ \t\n\r\0\x0B]+|[ \t\n\r\0\x0B]+$/g, '')
}

/**
 * Quà đua top CHỈ gồm những thứ "đáng công" (yêu cầu người dùng 19/09/2026) — bỏ hết
 * nhóm nguyên liệu (đá/ngọc GEM) và các nhóm không được nhắc tới. Whitelist các KHOÁ
 * NHÓM trong danh mục giftcode (gcDanhMuc()) — nhóm nào không có ở đây bị ẩn khỏi bộ chọn của TRANG NÀY,
 * KHÔNG đụng gì tới danh mục dùng chung (giftcode vẫn thấy đủ mọi nhóm như cũ).
 */
export const DUA_TOP_NHOM_CHO_PHEP: readonly string[] = [
  'trung_thu', // Trứng thú
  'trung_linh', // Trứng linh thú (rồng băng...)
  'trung_thuoc', // Trứng phượng hoàng / hoả kỳ lân / đương khang... (trứng "pet")
  'danh_hieu', // Danh hiệu
  'thoi_trang', // Áo/mặt nạ/nón/chổi thời trang - có danh sách id chọn thật
]

/**
 * Các loại phần thưởng KHÔNG có danh mục id (chỉ cần chọn "loại" rồi tự gõ số ngày/phút
 * trong ô đã chèn) mà trang này cho phép chèn nhanh — đúng các nhóm người dùng liệt kê.
 * Nguồn cú pháp/mô tả vẫn là loaiThuong() — chỉ lọc bớt danh sách hiện ra.
 */
export const DUA_TOP_MA_CHEN_NHANH: readonly string[] = [
  // ---- Thú cưỡi có ngày ----
  'PHT', 'PHDST', 'PHMT', 'PHKT', 'PHTT', 'PHBT', 'PHDS',
  'RONGBANG', 'SUTU', 'SUTUBANG', 'LANT', 'DBT', 'DK', 'DKB', 'DKT', 'TL', 'HKL',
  // ---- Vũ khí thời trang ----
  'VKTT', 'VKID', 'VKMAX',
  // ---- Vũ khí thần binh ----
  'VKTB', 'VKTBMAX', 'VKTBLV',
  // ---- Nguyệt linh trượng ----
  'NLT', 'NLTMAX',
  // ---- Mặt nạ theo hạn (khác 5 mặt ID_MAT_NA đã có danh sách chọn riêng) ----
  'MATNA2', 'MNT', 'MNC',
]

/** Một mục trong nhóm bộ chọn (giữ thứ tự bằng mảng). */
export interface DuaTopMuc {
  id: string
  ten: string
}

/** Một nhóm của bộ chọn vật phẩm trang đua top. */
export interface DuaTopNhom {
  ma: string
  ten: string
  loai: string
  ghiChu: string
  muc: DuaTopMuc[]
}

/**
 * "Mặt nạ" (MATNAID) và "Phi phong kháng" (PPRK/PPVK) không có trong danh mục giftcode dùng
 * chung vì mỗi loại chỉ có ĐÚNG MỘT lựa chọn ý nghĩa (5 mặt nạ cố định Char.ID_MAT_NA,
 * hoặc riêng id=750 cho kháng). Ghép tay vào bộ chọn của TRANG NÀY để admin chọn qua
 * dropdown thay vì gõ tay — dmTruongCuaLoai() đã có sẵn field cho 'PPRK'/'PPVK'/'MATNAID'.
 */
export function duaTopNhomTuVe(): DuaTopNhom[] {
  return [
    {
      ma: 'mat_na',
      ten: 'Mặt nạ',
      loai: 'MATNAID',
      ghiChu: 'Chọn ĐÚNG mẫu mặt nạ ở đây, rồi chọn RIÊNG hiệu ứng (option) ở ô bên dưới — '
        + 'hai thứ độc lập nhau (khác MATNA/MATNA2 vốn ép chung một chỉ số).',
      muc: [
        { id: '745', ten: 'Mặt nạ bí ngô' },
        { id: '746', ten: 'Mặt nạ Haunter' },
        { id: '747', ten: 'Mặt nạ Houndoom' },
        { id: '748', ten: 'Mặt nạ Yamask' },
        { id: '749', ten: 'Mặt nạ Gengar' },
      ],
    },
    {
      ma: 'pp_khang_ngau_nhien',
      ten: 'Ruy băng kháng — chỉ số ngẫu nhiên',
      loai: 'PPRK',
      ghiChu: 'Chỉ id=750 (Ruy Băng Hoenn) mới thật sự áp dụng được tham số Kháng.',
      muc: [{ id: '750', ten: 'Ruy Băng Hoenn' }],
    },
    {
      ma: 'pp_khang_max',
      ten: 'Ruy băng kháng — chỉ số max',
      loai: 'PPVK',
      ghiChu: 'Chỉ id=750 (Ruy Băng Hoenn) mới thật sự áp dụng được tham số Kháng.',
      muc: [{ id: '750', ten: 'Ruy Băng Hoenn' }],
    },
  ]
}

/**
 * Kiểm tra + chuẩn hoá chuỗi "cho người chơi tự chọn": "nhãn1=chuỗi1|nhãn2=chuỗi2|...".
 * Mỗi đoạn là MỘT lựa chọn hiện ra trong menu NPC — "chuỗi" theo đúng cú pháp phần
 * thưởng bình thường (qua kiemTraItem()), "nhãn" là tên hiển thị cho người chơi chọn.
 * Trả về [chuoiDaChuan, mangLoi] giống kiemTraItem().
 */
export function duaTopKiemTraItemChon(rawVao: string): [string, string[]] {
  const raw = catKhoangTrang(String(rawVao ?? ''))
  if (raw === '') return ['', []]
  const loi: string[] = []
  const phan: string[] = []
  for (const doanTho of raw.split('|')) {
    const doan = catKhoangTrang(doanTho)
    if (doan === '') continue
    const vt = doan.indexOf('=')
    if (vt === -1 || vt === 0 || vt === doan.length - 1) {
      loi.push(`"${doan}": phải đúng dạng "nhãn=phần thưởng" (có dấu "=").`)
      continue
    }
    const nhan = catKhoangTrang(doan.slice(0, vt))
    const chuoiTho = catKhoangTrang(doan.slice(vt + 1))
    if (nhan === '' || /[|=]/.test(nhan)) {
      loi.push(`"${doan}": nhãn không hợp lệ (không được rỗng hoặc chứa "|"/"=").`)
      continue
    }
    const [chuoi, loiItem] = kiemTraItem(chuoiTho)
    if (loiItem.length) {
      loi.push(`"${nhan}": ${loiItem.join(' | ')}`)
      continue
    }
    if (chuoi === '' || chuoi.includes(',')) {
      // Mỗi lựa chọn là MỘT phần thưởng duy nhất — tránh nhầm lẫn dấu phẩy (ngăn cách
      // nhiều phần thưởng trong "item" thường) với dấu "|" (ngăn cách các LỰA CHỌN).
      loi.push(`"${nhan}": mỗi lựa chọn chỉ được đúng MỘT phần thưởng (không dùng dấu phẩy).`)
      continue
    }
    phan.push(`${nhan}=${chuoi}`)
  }
  return [phan.join('|'), loi]
}

/** Một nhóm lựa chọn "người chơi tự chọn" khi hiển thị. */
export interface DuaTopNhomLuaChon {
  ten: string
  con: { nhan: string, chuoi: string }[]
}

/**
 * Gom cột item_chon theo "tên nhóm::nhãn con" — GIỐNG HỆT cách đọc của
 * real/SuKienDuaTop.java (phanCapItemChon): nhóm có >1 nhãn con là menu 2 bước thật sự
 * (chọn nhóm trước, rồi mới chọn chỉ số), nhóm có đúng 1 là áp dụng thẳng khi chọn.
 */
export function duaTopNhomLuaChon(rawVao: string): DuaTopNhomLuaChon[] {
  const raw = catKhoangTrang(String(rawVao ?? ''))
  if (raw === '') return []
  const nhom = new Map<string, { nhan: string, chuoi: string }[]>()
  const them = (ten: string, nhan: string, chuoi: string) => {
    const ds = nhom.get(ten) ?? []
    ds.push({ nhan, chuoi })
    nhom.set(ten, ds)
  }
  for (const doanTho of raw.split('|')) {
    const doan = catKhoangTrang(doanTho)
    if (doan === '') continue
    const vt = doan.indexOf('=')
    if (vt === -1) {
      them(doan, '', doan)
      continue
    }
    const nhanGoc = doan.slice(0, vt)
    const chuoi = doan.slice(vt + 1)
    const vt2 = nhanGoc.indexOf('::')
    const tenNhom = vt2 !== -1 ? nhanGoc.slice(0, vt2) : nhanGoc
    const nhanCon = vt2 !== -1 ? nhanGoc.slice(vt2 + 2) : nhanGoc
    them(tenNhom, nhanCon, chuoi)
  }
  return [...nhom].map(([ten, con]) => ({ ten, con }))
}

/** Bỏ dấu "|" / "=" trong nhãn để không phá định dạng "nhãn=chuỗi|nhãn=chuỗi". */
export function duaTopLamSachNhan(s: string): string {
  return String(s ?? '').replace(/[|=]/g, ' ').trim()
}

/** Nối một đoạn "nhãn=chuỗi" (hoặc nhiều đoạn đã nối bằng "|") vào cuối ô tự chọn. */
export function duaTopNoiLuaChon(cu: string, doan: string): string {
  if (doan === '') return cu
  const c = String(cu ?? '').trim().replace(/\|+\s*$/, '')
  return c === '' ? doan : `${c}|${doan}`
}

/**
 * "Thêm CẢ NHÓM vào cho người chơi tự chọn": sinh HẾT tổ hợp có thể có làm các lựa chọn.
 *   - "chiều" đầu tiên là id (mọi món trong nhóm, vd 5 mặt nạ, hoặc chỉ 1 id như Phi
 *     Phong Huyền Vũ) — LUÔN duyệt hết.
 *   - mỗi trường sau đó: nếu là ô "chọn" (dropdown) có NHIỀU HƠN 1 giá trị (vd "Kháng" 6
 *     lựa chọn, "Hệ" 2 lựa chọn) thì CŨNG duyệt hết — người chơi được tự chọn cả Kháng
 *     lẫn Hệ. Ô còn lại (số lượng/thời hạn, hoặc chọn chỉ có 1 giá trị) giữ NGUYÊN giá
 *     trị đang đặt trong bộ chọn.
 *   - "khongCheo" (vd ô "Khoá"): KHÔNG coi là thứ để người chơi tự chọn dù là dropdown
 *     nhiều giá trị — khoá hay không là admin đặt sẵn (yêu cầu người dùng 19/09/2026).
 * Kết quả là TÍCH DESCARTES của tất cả các chiều — vd Phi phong kháng: 1 id × 2 hệ ×
 * 6 kháng = 12 lựa chọn trong 1 lần bấm.
 *
 * Nhãn đầu tiên (id/vật phẩm) là NHÓM cấp 1; các nhãn còn lại (từ trường nhiều lựa chọn)
 * gộp thành nhãn con cấp 2, nối bằng "::" — server (SuKienDuaTop) tách theo dấu này để
 * hiện MENU 2 BƯỚC. Không có gì để chọn thêm thì không nối "::".
 *
 * Trả về null nếu một ô cần giữ nguyên đang để trống (chưa điền đủ thì không thêm gì).
 */
export function duaTopToHopCaNhom(
  nhom: Pick<DuaTopNhom, 'loai' | 'muc'>,
  truong: TruongThuong[],
  giaTriHienTai: Record<string, string>,
): string[] | null {
  if (!nhom.muc.length) return null
  type Chieu = { giaTri: string[], nhan: (string | null)[] }
  const chieu: Chieu[] = [{ giaTri: nhom.muc.map(m => m.id), nhan: nhom.muc.map(m => m.ten) }]
  for (const t of truong) {
    if (t.an) {
      chieu.push({ giaTri: [t.macDinh], nhan: [null] })
      continue
    }
    if (t.chon && !t.khongCheo && t.chon.length > 1) {
      chieu.push({ giaTri: t.chon.map(c => c.value), nhan: t.chon.map(c => c.label) })
      continue
    }
    const v = String(giaTriHienTai[t.ma] ?? '').trim()
    if (v === '') return null // chưa điền đủ ô, không làm gì
    chieu.push({ giaTri: [v], nhan: [null] })
  }

  let toHop: Chieu[] = [{ giaTri: [], nhan: [] }]
  for (const c of chieu) {
    const moi: Chieu[] = []
    for (const truoc of toHop) {
      for (let k = 0; k < c.giaTri.length; k++) {
        moi.push({ giaTri: [...truoc.giaTri, c.giaTri[k]!], nhan: [...truoc.nhan, c.nhan[k] ?? null] })
      }
    }
    toHop = moi
  }

  return toHop.map((h) => {
    const nhomLabel = duaTopLamSachNhan(h.nhan[0] || h.giaTri[0] || '')
    const conLai = h.nhan.slice(1).filter((x): x is string => !!x).map(duaTopLamSachNhan)
    const tenGop = conLai.length ? `${nhomLabel}::${conLai.join(' - ')}` : nhomLabel
    return `${tenGop}=${[nhom.loai, ...h.giaTri].join(':')}`
  })
}
