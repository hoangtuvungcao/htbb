/**
 * GIFTCODE AN TOÀN — danh sách trắng vật phẩm. Lõi thuần, KHÔNG import gì (chạy được bằng `node --test`).
 * Dữ liệu: shared/data/giftcode-an-toan.json (SINH bởi HTBB/tools/pokemon/sinh_giftcode_an_toan.py).
 * Bản Java dịch 1-1: HTBB/src/com/htbb/pokemon/GiftcodeAnToanLogic.kiemVatPham — sửa một bên thì sửa cả bên kia;
 * cả hai chạy chung bộ ca `thuNghiem` của JSON (npm test / HTBB test/KiemItemMoCoi.java).
 *   ITEM: tiêu hao / nguyên liệu / tín vật (dải vatPham) hoặc trang bị catalog có cấp món ≤ cấp người nhận
 *         (web: cap_toi_thieu của mã; server game: cấp nhân vật lúc nhập).
 *   GEM : ngọc có thật (0..ngocToiDa) và không nằm trong ngocCam (bán bằng lượng / đã nghỉ hưu).
 */
export interface DuLieuGiftcodeAnToan {
  vatPham: [number, number][]
  trangBi: number[]
  ngocToiDa: number
  ngocCam: number[]
}

/** `chuan` = chuỗi ĐÃ chuẩn hoá (gcChuanHoaThuong). `capMon(id)` = data_item.level, -1 nếu không có. null = hợp lệ. */
export function kiemVatPhamGiftcode(d: DuLieuGiftcodeAnToan, chuan: string, capNguoiNhan: number,
  capMon: (id: number) => number): string | null {
  for (const phan of chuan.split(',')) {
    const t = phan.split(':')
    const id = t.length > 1 ? Number(t[1]) : -1
    if (t[0] === 'ITEM') {
      if (d.vatPham.some(([a, b]) => id >= a && id <= b)) continue
      if (d.trangBi.includes(id)) {
        const cap = capMon(id)
        if (cap < 0) return `Vật phẩm #${id} không tồn tại.`
        if (cap > capNguoiNhan) return `Trang bị #${id} (cấp ${cap}) cao hơn cấp người nhận (${capNguoiNhan}).`
        continue
      }
      return `Vật phẩm #${id} không được phát bằng giftcode (chỉ tiêu hao, nguyên liệu, tín vật, trang bị thường).`
    }
    else if (t[0] === 'GEM') {
      if (!(id >= 0) || id > d.ngocToiDa || d.ngocCam.includes(id)) {
        return `Ngọc #${id} không được phát bằng giftcode (không tồn tại hoặc bán bằng lượng).`
      }
    }
  }
  return null
}
