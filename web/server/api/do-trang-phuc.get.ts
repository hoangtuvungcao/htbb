/**
 * Danh mục đồ họ mới + trang phục (Cẩm nang /cam-nang/do-moi, /cam-nang/trang-phuc).
 * Đọc 5 bảng CSDL do sql/them_do_trang_phuc.sql tạo (chỉ dòng bat = 1, không dữ liệu người chơi); CSDL chưa có bảng /
 * lỗi kết nối -> danh mục sinh sẵn shared/data/danh-muc-do-trang-phuc.json (cùng bộ sinh tools/do_trang_phuc/sinh.py).
 * Nhớ đệm 5 phút.
 */
import macDinh from '../../shared/data/danh-muc-do-trang-phuc.json'
import { hangThanhDanhMuc } from '../../shared/utils/doTrangPhuc.mjs'

const BANG = ['data_trang_bi_ho', 'data_trang_bi_ho_thuong', 'data_trang_bi_moi', 'data_trang_phuc_o', 'data_trang_phuc']

export default defineEventHandler(async () => quaDem('do-trang-phuc', 300, async () => {
  try {
    const g = dbGame()
    for (const b of BANG) {
      if (!(await coBang(g, b))) return { ...macDinh, nguonDuLieu: 'mac_dinh' }
    }
    const [ho, thuong, moi, o, trangPhuc] = await Promise.all([
      q<Record<string, unknown>>(g, 'SELECT ma, ten, loai, he, he_khac, vung, bac_tu, bac_den, mo_ta, thu_tu, bat FROM data_trang_bi_ho'),
      q<Record<string, unknown>>(g, 'SELECT ma_ho, so_mon, chi_so, phan_nghin FROM data_trang_bi_ho_thuong'),
      q<Record<string, unknown>>(g, 'SELECT id_item, ma_ho, type, bac, ten, icon, nguon FROM data_trang_bi_moi'),
      q<Record<string, unknown>>(g, 'SELECT o, ten, phan_nghin, dong, thu_tu FROM data_trang_phuc_o'),
      q<Record<string, unknown>>(g, 'SELECT ma, o, so, ten, do_hiem, icon, nguon, tham_so, gia_luong, mo_ta, thu_tu, bat FROM data_trang_phuc'),
    ])
    if (!moi.length && !trangPhuc.length) return { ...macDinh, nguonDuLieu: 'mac_dinh' }
    const dm = hangThanhDanhMuc({ ho, thuong, moi, o, trangPhuc },
      { nganSachDanhHieu: macDinh.nganSachDanhHieu, tranTong: macDinh.tranTong })
    return { ...dm, nguonDuLieu: 'csdl' }
  }
  catch {
    return { ...macDinh, nguonDuLieu: 'mac_dinh' }
  }
}))
