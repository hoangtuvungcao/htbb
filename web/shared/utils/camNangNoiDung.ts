/**
 * Nội dung chi tiết cho cẩm nang (vật phẩm, cửa hàng, Gym, boss thế giới, PvP mùa, Sổ tay huấn luyện,
 * Hầm ngục, hệ sinh thái, câu cá, cốt truyện). Sinh bởi `node scripts/dong-bo-cam-nang.mjs` từ
 * HTBB/docs/pokemon/web/*.csv, nhiem_vu_tom_tat.json, diem_cau_ca.csv và hằng số Java. KHÔNG sửa tay.
 *
 * Tách riêng khỏi camNang.ts để chỉ trang nào dùng mới tải (Vite gom JSON này thành chunk riêng).
 */
import raw from '../data/cam-nang-noi-dung.json'

export interface VatPhamWeb { ten: string, nhom: string, congDung: string, cap: string, gia: number | null, tien: string, nguon: string, gioiHan: string, sapCo: boolean }
export interface HangCuaHang { vatPham: string, gia: number | null, tien: string, cap: number | null, gioiHan: string }
export interface GymInfo { so: number, huyHieu: string, noi: string, he: string, cap: number, truong: string, hp: number | null, tanCong: number | null, phongThu: number | null, bacThuong: string }
export interface ChuongCotTruyen { so: number, nam: number, vung: string, cung: string, diaDanh: string, cap: number, tomTat: string, buoc: string[], huyHieu: string }

export interface CamNangNoiDung {
  /** Shiny khi hạ quái: 1/mauSo mỗi lần hạ, chắc chắn ở lần thứ `pity` (không cộng sức mạnh). */
  shiny: { mauSo: number, pity: number }
  vatPham: VatPhamWeb[]
  cuaHang: { ten: string, hang: HangCuaHang[] }[]
  gym: GymInfo[]
  gymLuat: { phutGioiHan: number, phutNoiGian: number, huyChuongTuan: number, aoChoang: { ten: string, gia: number | null } | null }
  cheTao: { ten: string, cap: number, da: number, phu: string, xu: number, noi: string }[]
  nhanhEevee: { ten: string, he: string, vai: string, chieu: string, cap: number }[]
  trangTri: { mua: string, loai: string, ten: string, gia: number | null, tien: string, nguon: string }[]
  suKienVatPham: { tuan: number, ten: string, ten2: string, vatPham: string, toiDaTuan: number | null, doiLay: string }[]
  bossTheGioi: { lich: { tuan: number, nam1: string, nam2: string, he: string, luotNgay: number }[], gioThuBay: string, gioDotKich: string[], phutNoiGian: number }
  pvp: { eloBanDau: number, k: number, tuanMoiMua: number, bac: { ten: string, tuElo: number }[], daiCap: number, phutCungCap: number }
  soTay: {
    muc: string[]
    quaHoatDong: { phut: number[], luongKhoa: number[], da: number[], xuPhanTramGio: number[], nguyenLieu: number[], giayDungYen: number, ngayThang: number }
    suPham: { capThay: number, cachCap: number, expPhanTram: number, moc: number[] }
  }
  hamNguc: { tangToiDa: number, lechCap: number, expPhanTramGio: number, tangDuExp: number, tangLuongKhoa: number, luongKhoa: number }
  heSinhThai: {
    theLucNgay: number
    theLucToiDa: number
    tlCauCa: number
    tlDao: number
    tlNau: number
    capKyNangToiDa: number
    kyNang: string[]
    thayLoaiPhanTram: number
    bungPhatPhanTram: number
    thoiTietPhanTram: number
    heSoSinh: number
    gioBungPhat: number[]
    gioDemTu: number
    gioDemDen: number
    nhaTre: { buocTrung: number, trungNgay: number, trungTui: number, shinyMau: number, shinyPity: number, ivToiDa: number }
    cho: { thue: number, phiDang: number, npcPhanTram: number, giaToiDaLan: number, hanNgay: number, tinBan: number, don: number }
    doVui: { gio: string[], giay: number, luongKhoa: number }
    dua: { gio: string, phutDangKy: number, thuong: number[], giayToiDa: number }
    nghienCuuLuongKhoa: number
    vung: { luongKhoa: number, phanNghin: number, tranPhanNghin: number }
    meThuocNgay: number
    album: { toiDa: number, loai: string[], moc: { anh: number, luongKhoa: number, danhHieu: string }[] }
    cauCa: {
      bang: { ten: string, trongSo: number, capKyNang: number, nuoc: string, hoLon: boolean }[]
      tongDiem: number
      ban: { map: number, ten: string, vung: string, soHo: number, hoLon: number, diem: number }[]
    }
    congThuc: { ten: string, kyNang: string, cap: number, nguyenLieu: string[] }[]
  }
  cotTruyen: {
    tong: number
    theoLoai: Record<string, number>
    chuong: ChuongCotTruyen[]
    chuoiPhu: { noi: string, ten: string, nhanVat: string, capTu: number, capDen: number, soBuoc: number }[]
    ngay: string[]
    tuan: string[]
    huyenThoai: string[]
    an: { soLuong: number, goiY: string }
  }
}

export const CAM_NANG_ND = raw as unknown as CamNangNoiDung

/** "1.250 xu" / "5 lượng khoá" / "Không bán". */
export function giaHienThi(gia: number | null, tien: string): string {
  if (gia == null || !tien || tien === 'không bán') return 'Không bán'
  return `${gia.toLocaleString('vi-VN')} ${tien}`
}
