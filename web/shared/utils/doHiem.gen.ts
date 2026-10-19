/**
 * SINH TU tools/cauhinh/do_hiem.json boi tools/cauhinh/sinh_do_hiem.py - KHONG SUA TAY.
 * Do hiem chung (cung mau voi game: server com.htbb.pokemon.DoHiemSinh, client game.pokemon.DoHiemSinh).
 */

export interface BacHiem { id: number, ma: string, ten: string, mau: string, nhat: string, sang: string, vienDong: boolean }

export const BAC_HIEM: readonly BacHiem[] = [
  { id: 0, ma: "thuong", ten: "Thường", mau: "#5F6B7A", nhat: "#EEF1F5", sang: "#F2F4F7", vienDong: false },
  { id: 1, ma: "hiem", ten: "Hiếm", mau: "#23803A", nhat: "#E2F5E5", sang: "#7EE08A", vienDong: false },
  { id: 2, ma: "quy", ten: "Quý", mau: "#2366C0", nhat: "#E3EEFC", sang: "#8CC8FF", vienDong: false },
  { id: 3, ma: "su_thi", ten: "Sử thi", mau: "#7B45D6", nhat: "#EEE8FC", sang: "#CDB8FF", vienDong: false },
  { id: 4, ma: "huyen_thoai", ten: "Huyền thoại", mau: "#B95A0E", nhat: "#FFEEDB", sang: "#FFC46B", vienDong: true },
  { id: 5, ma: "trang_tri", ten: "Trang trí", mau: "#8A6A00", nhat: "#FFF5D0", sang: "#FFE070", vienDong: false },
]

/** Item.colorName -> id bac (ngoai bang = 0 Thuong). */
export const BAC_VAT_PHAM: readonly number[] = [0, 3, 2, 1, 3, 5, 4, 1]
/** data_danh_hieu.do_hiem (0..3) -> id bac. */
export const BAC_DANH_HIEU: readonly number[] = [0, 1, 3, 4]
export const BAC_TRANG_BI: readonly { ten: string, mau: string }[] = [
  { ten: "Sơ cấp", mau: "#9AA0A8" },
  { ten: "Đồng", mau: "#C88048" },
  { ten: "Bạc", mau: "#D0D8E0" },
  { ten: "Vàng", mau: "#F8D030" },
  { ten: "Bạch kim", mau: "#E4F4FF" },
  { ten: "Kim cương", mau: "#78D8F8" },
  { ten: "Chuyên gia", mau: "#52C866" },
  { ten: "Tinh anh", mau: "#A45CE8" },
  { ten: "Huyền thoại", mau: "#F08A28" },
  { ten: "Thần thoại", mau: "#F84868" },
  { ten: "Tối thượng", mau: "#FFFFFF" },
]

export function bacHiem(id: number): BacHiem {
  return BAC_HIEM[id] ?? BAC_HIEM[0]!
}

export function bacVatPham(colorName: number): BacHiem {
  return bacHiem(BAC_VAT_PHAM[colorName] ?? 0)
}

export function bacDanhHieu(doHiem: number): BacHiem {
  return bacHiem(BAC_DANH_HIEU[doHiem] ?? 0)
}
