/**
 * Phát quà đua top — phần ĐỌC/GHI CSDL.
 * Phần thuần (kiểm tra chuỗi tự chọn, nhóm cho phép, tổ hợp "thêm cả nhóm") nằm ở
 * shared/utils/duaTop.ts. Danh mục vật phẩm + bộ giải thích chuỗi dùng LẠI của trang
 * Giftcode (server/utils/giftcode.ts: gcDanhMuc(), gcMoTaNhieu()).
 *
 * Một hạng có thể được tạo NHIỀU LẦN qua các đợt đua top khác nhau — bảng không ràng
 * buộc UNIQUE theo vi_tri, dòng của đợt trước (đã nhận hay chưa) vẫn giữ lại để tra cứu
 * lịch sử, giống cách giftcode_log giữ lại sau khi giftcode bị xoá.
 */
import type { Pool } from 'mysql2/promise'

export const DUA_TOP_BANG = 'tob_top_thuong'

/**
 * Tự tạo bảng nếu chưa có — cùng định nghĩa với real/SuKienDuaTop.java:taoBang().
 * Tự thêm cột `item_chon` (19/09/2026) nếu bảng đã tạo từ trước khi có cột này.
 */
export async function duaTopTaoBang(game: Pool = dbGame()): Promise<void> {
  await exec(game,
    `CREATE TABLE IF NOT EXISTS \`${DUA_TOP_BANG}\` (`
    + '`id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,'
    + '`vi_tri` TINYINT NOT NULL,'
    + '`charname` VARCHAR(50) NOT NULL,'
    + '`item` TEXT NULL,'
    + '`item_chon` TEXT NULL,'
    + '`da_nhan` TINYINT NOT NULL DEFAULT 0,'
    + '`admin_user` VARCHAR(50) NOT NULL DEFAULT \'\','
    + '`tao_luc` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,'
    + '`nhan_luc` DATETIME NULL,'
    + 'PRIMARY KEY (`id`),'
    + 'KEY `idx_charname_da_nhan` (`charname`, `da_nhan`)'
    + ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4')
  const co = await qv(game,
    'SELECT COUNT(*) FROM information_schema.COLUMNS'
    + ` WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '${DUA_TOP_BANG}' AND COLUMN_NAME = 'item_chon'`)
  if (!Number(co)) {
    await exec(game, `ALTER TABLE \`${DUA_TOP_BANG}\` ADD COLUMN \`item_chon\` TEXT NULL AFTER \`item\``)
  }
}

export interface DuaTopDong {
  id: number
  vi_tri: number
  charname: string
  item: string
  item_chon: string
  admin_user: string
  tao_luc: string
  nhan_luc: string | null
  /** Bản dịch dễ đọc của `item` (mỗi phần thưởng một dòng). */
  moTa: string[]
  /** Các lựa chọn "người chơi tự chọn" đã gom nhóm. */
  luaChon: DuaTopNhomLuaChon[]
}

/** Đọc hai bảng "đang chờ nhận" và "đã nhận (100 dòng mới nhất)". */
export async function duaTopDocDanhSach(game: Pool = dbGame()): Promise<{ dangCho: DuaTopDong[], daNhan: DuaTopDong[] }> {
  const dangChoTho = await q(game, `SELECT * FROM \`${DUA_TOP_BANG}\` WHERE da_nhan = 0 ORDER BY vi_tri, id`)
  const daNhanTho = await q(game, `SELECT * FROM \`${DUA_TOP_BANG}\` WHERE da_nhan = 1 ORDER BY nhan_luc DESC, id DESC LIMIT 100`)

  const tatCa = [...dangChoTho, ...daNhanTho]
  let moTa: string[][] = []
  try {
    moTa = await gcMoTaNhieu(tatCa.map(r => String(r.item ?? '')), game)
  } catch {
    moTa = []
  }
  const dong = tatCa.map((r, i): DuaTopDong => ({
    id: Number(r.id),
    vi_tri: Number(r.vi_tri),
    charname: String(r.charname ?? ''),
    item: String(r.item ?? ''),
    item_chon: String(r.item_chon ?? ''),
    admin_user: String(r.admin_user ?? ''),
    tao_luc: String(r.tao_luc ?? ''),
    nhan_luc: r.nhan_luc == null ? null : String(r.nhan_luc),
    moTa: String(r.item ?? '').trim() === '' ? [] : (moTa[i] ?? []),
    luaChon: duaTopNhomLuaChon(String(r.item_chon ?? '')),
  }))
  return { dangCho: dong.slice(0, dangChoTho.length), daNhan: dong.slice(dangChoTho.length) }
}

/**
 * Danh mục của bộ chọn TRANG NÀY: các nhóm whitelist trong danh mục giftcode (giữ thứ
 * tự của danh mục dùng chung) + 3 nhóm tự vẽ (mặt nạ, phi phong kháng).
 */
export async function duaTopDanhMuc(game: Pool = dbGame()): Promise<DuaTopNhom[]> {
  const choPhep = new Set(DUA_TOP_NHOM_CHO_PHEP)
  let chung: DuaTopNhom[] = []
  try {
    chung = (await gcDanhMuc(game))
      .filter(n => choPhep.has(n.ma))
      .map(n => ({ ma: n.ma, ten: n.ten, loai: n.loai, ghiChu: n.ghiChu, muc: n.muc.map(m => ({ id: String(m.id), ten: m.ten })) }))
  } catch {
    chung = []
  }
  return [...chung, ...duaTopNhomTuVe()]
}

/** Bảng "Chèn nhanh": các loại trong DUA_TOP_MA_CHEN_NHANH, theo thứ tự của loaiThuong(). */
export function duaTopChenNhanh(): { loai: string, cuPhap: string, moTa: string, viDu: string }[] {
  const chon = new Set(DUA_TOP_MA_CHEN_NHANH)
  return Object.entries(loaiThuong())
    .filter(([loai]) => chon.has(loai))
    .map(([loai, [, cuPhap, moTa, viDu]]) => ({ loai, cuPhap, moTa, viDu }))
}
