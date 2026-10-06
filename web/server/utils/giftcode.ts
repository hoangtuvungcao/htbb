/**
 * Phần ĐỌC CSDL của danh mục giftcode và bộ giải thích chuỗi phần thưởng cho bảng
 * danh sách. Phần thuần nằm ở server/utils/giftcodeDanhMuc.ts.
 *
 * Mọi hàm đọc ở đây KHÔNG BAO GIỜ ném lỗi ra ngoài: đọc một bảng hỏng chỉ làm nhóm đó
 * biến mất khỏi bộ chọn / dòng mô tả hiện xấu, không làm vỡ cả trang.
 */
import type { Pool } from 'mysql2/promise'
import {
  DM_TYPE_NHOM_RIENG, dmDungDanhMuc, dmMoTaChuoiThuong, dmThuocTuDong,
  type DongDataItem, type DongTen, type NguonTenGiftcode, type NhomDanhMucGiftcode,
} from './giftcodeDanhMuc'

async function gcAnLoi<T>(fn: () => Promise<T>, macDinh: T): Promise<T> {
  try {
    return await fn()
  } catch {
    return macDinh
  }
}

/**
 * Đọc data_item. `chiLay` rỗng = lấy hết.
 * Ép về số nguyên rồi nhúng thẳng: đây là mảng do MÃ NGUỒN tạo (hằng số type), không
 * phải dữ liệu người gửi.
 */
export function gcDocVatPham(game: Pool, chiLay: readonly number[] = []): Promise<DongDataItem[]> {
  let sql = 'SELECT id, name, type, level FROM data_item'
  if (chiLay.length) sql += ` WHERE type IN (${chiLay.map(n => Math.trunc(Number(n))).join(',')})`
  sql += ' ORDER BY type, id'
  return gcAnLoi(() => q<DongDataItem>(game, sql), [])
}

/**
 * Đọc data_potion, CHỈ id trong dải hợp lệ và có tên. Dòng không tên là ô trống
 * trong bảng — phát ra thì người chơi nhận món không tên (xem LoginHandler.POTION_SHOP).
 */
export function gcDocThuoc(game: Pool): Promise<DongTen[]> {
  return gcAnLoi(() => q<DongTen>(game,
    `SELECT id, name FROM data_potion
      WHERE name IS NOT NULL AND name <> ''
        AND id >= 0 AND id < ${Math.trunc(DM_SO_O_THUOC)}
      ORDER BY id`), [])
}

/**
 * "Thời trang" KHÔNG PHẢI một data_item.type — là cột atb9 (1 hoặc 2,
 * Map.isModelClothes()), rải trên type 0 (áo), 1 (mặt nạ/nón), 2 (nón), 13 (chổi bay).
 *
 * CỐ Ý BỎ type 22/23/24 dù cũng có atb9=1 (Thẻ mua bán, Xa tiêu lệnh, Lệnh bài trấn
 * yêu): đối chiếu dữ liệu thật thì atb9=1 ở đó chỉ là cách né bị nhân hệ số theo class
 * — xếp vào đây sẽ làm người chọn "Thời trang" phát nhầm một thẻ giao dịch.
 * CỐ Ý BỎ type 29 (Cánh, atb9=1) — Cánh đã có nhóm riêng.
 *
 * Loại thưởng: TT -> Char.createThoiTrang() — đã đọc toàn thân hàm, có null-guard và
 * nhánh riêng cho Cánh / mặt nạ / chổi, an toàn với mọi id trong nhóm này.
 */
export function gcDocThoiTrang(game: Pool): Promise<DongTen[]> {
  return gcAnLoi(() => q<DongTen>(game,
    `SELECT id, name FROM data_item
      WHERE atb9 IN (1, 2) AND type IN (0, 1, 2, 13)
      ORDER BY type, id`), [])
}

/** Thần khí: chỉ đúng 3 id (890/891/892) — mang type 4 nên không nằm trong các type ở trên. */
export function gcDocThanKhi(game: Pool): Promise<DongTen[]> {
  return gcAnLoi(() => q<DongTen>(game, 'SELECT id, name FROM data_item WHERE id IN (890, 891, 892)'), [])
}

/** Toàn bộ danh mục cho bộ chọn. */
export async function gcDanhMuc(game: Pool = dbGame()): Promise<NhomDanhMucGiftcode[]> {
  const [vatPhamChon, vatPhamTatCa, thuoc, thoiTrang, thanKhi] = await Promise.all([
    gcDocVatPham(game, DM_TYPE_NHOM_RIENG),
    gcDocVatPham(game),
    gcDocThuoc(game),
    gcDocThoiTrang(game),
    gcDocThanKhi(game),
  ])
  return dmDungDanhMuc({ vatPhamChon, vatPhamTatCa, thuoc, thoiTrang, thanKhi })
}

/**
 * Giải thích nhiều chuỗi phần thưởng một lượt (cột "Phần thưởng" của hai bảng).
 *
 * Làm hai lượt để không phải tra `data_item` từng id một: lượt 1 chạy bộ giải thích
 * với nguồn "ghi lại" để biết cần những id nào, rồi đọc MỘT câu
 * `WHERE id IN (?, ?, ...)` (tham số hoá), lượt 2 chạy thật với tên đã nạp.
 */
export async function gcMoTaNhieu(chuoi: string[], game: Pool = dbGame()): Promise<string[][]> {
  const canId = new Set<number>()
  let canThuoc = false
  const ghiLai: NguonTenGiftcode = {
    tenItem: (id) => { canId.add(id); return null },
    thuoc: () => { canThuoc = true; return new Map() },
  }
  for (const c of chuoi) {
    try { dmMoTaChuoiThuong(ghiLai, c) } catch { /* bỏ qua */ }
  }

  const tenItem = new Map<number, string>()
  const ids = [...canId].filter(n => Number.isSafeInteger(n))
  if (ids.length) {
    const dong = await gcAnLoi(() => q<DongTen>(game,
      `SELECT id, name FROM data_item WHERE id IN (${ids.map(() => '?').join(',')})`, ids), [])
    for (const d of dong) tenItem.set(Number(d.id), dmMotDong(d.name))
  }
  const thuoc = new Map<number, string>()
  if (canThuoc) {
    for (const m of dmThuocTuDong(await gcDocThuoc(game))) thuoc.set(m.id, m.ten)
  }
  const nguon: NguonTenGiftcode = {
    tenItem: id => tenItem.get(id) ?? null,
    thuoc: () => thuoc,
  }
  return chuoi.map((c) => {
    try {
      return dmMoTaChuoiThuong(nguon, c)
    } catch {
      return []
    }
  })
}

/**
 * Đọc số nguyên rộng tay cho trường của biểu mẫu giftcode: lấy phần số nguyên ở đầu
 * chuỗi ("12abc" -> 12, "abc" -> 0, "" -> 0). Ô số để trống gửi lên "" -> 0.
 */
export function gcSoNguyen(v: unknown): number {
  if (typeof v === 'number') return Number.isFinite(v) ? Math.trunc(v) : 0
  if (typeof v !== 'string') return 0
  const m = v.trim().match(/^[+-]?\d+/)
  return m ? Number(m[0]) : 0
}

/** Cột INT(11) của bảng giftcode. */
export const GC_INT_MIN = -2147483648
export const GC_INT_MAX = 2147483647
