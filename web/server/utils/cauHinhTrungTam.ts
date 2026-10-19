/**
 * CẤU HÌNH TRUNG TÂM — phần chạm CSDL của web (docs/cau-hinh.md).
 *
 * Web và máy chủ game đọc/ghi CÙNG các khoá (bảng cau_hinh; hai khoá trần nạp nằm ở nap_cau_hinh của hệ nạp).
 * Mỗi lần lưu: kiểm (shared/utils/cauHinhTrungTam.ts) -> ghi trong MỘT transaction -> cau_hinh_lich_su (ai/khi
 * nào/cũ/mới/lý do) -> tăng cau_hinh_phien_ban. Máy chủ game hỏi phiên bản mỗi 5 giây, áp dụng, rồi ghi
 * cau_hinh_ap_dung (phiên bản + giờ áp dụng) và cau_hinh_hieu_luc (giá trị đang chạy từng khoá) — trang đọc lại
 * hai bảng đó để admin thấy "đã có hiệu lực".
 *
 * Danh mục khoá: cau_hinh_danh_muc + cau_hinh_nhom trong CSDL (server/utils/cauHinhDanhMucSql.ts), dự phòng bản sinh.
 *
 * Bí mật (HTBB_NAP_*, khoá webhook, mật khẩu) KHÔNG BAO GIỜ vào CSDL: khoá bang='env' chỉ hiển thị trạng thái.
 */
import type { H3Event } from 'h3'
import type { PoolConnection } from 'mysql2/promise'
import type { AdminUser } from './admin'

export const BANG_TT = {
  phienBan: 'cau_hinh_phien_ban',
  lichSu: 'cau_hinh_lich_su',
  apDung: 'cau_hinh_ap_dung',
  hieuLuc: 'cau_hinh_hieu_luc',
  khoiDongLai: 'cau_hinh_khoi_dong_lai',
} as const

/** Web được ghi khoá thử nghiệm (khoaProd) khi chạy dev hoặc HTBB_CHO_PHEP_THU_NGHIEM=1. Máy chủ game còn kiểm riêng. */
export function choPhepThuNghiemWeb(): boolean {
  return !!import.meta.dev || ((process.env.HTBB_CHO_PHEP_THU_NGHIEM ?? process.env.KPAH_CHO_PHEP_THU_NGHIEM) ?? '').trim() === '1' // TUONG-THICH-TEN-CU
}

// ---------------------------------------------------------------- tạo bảng (giống CauHinhTrungTam.taoBangNeuThieu)

let daTao = false
export async function chTaoBang(): Promise<void> {
  if (daTao) return
  const g = dbGame()
  await exec(g, "CREATE TABLE IF NOT EXISTS `cau_hinh` (`khoa` varchar(64) NOT NULL, `gia_tri` varchar(255) NOT NULL DEFAULT '', `sua_boi` varchar(50) DEFAULT NULL, `sua_luc` datetime DEFAULT NULL, PRIMARY KEY (`khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
  await exec(g, 'CREATE TABLE IF NOT EXISTS `cau_hinh_phien_ban` (`id` TINYINT NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0, `sua_luc` DATETIME NULL, `sua_boi` VARCHAR(50) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4')
  await exec(g, "CREATE TABLE IF NOT EXISTS `cau_hinh_lich_su` (`id` BIGINT NOT NULL AUTO_INCREMENT, `khoa` VARCHAR(64) NOT NULL, `bang` VARCHAR(16) NOT NULL DEFAULT 'cau_hinh', `gia_tri_cu` VARCHAR(255) NULL, `gia_tri_moi` VARCHAR(255) NULL, `nguoi_sua` VARCHAR(50) NULL, `nguon` VARCHAR(20) NOT NULL DEFAULT '', `ly_do` VARCHAR(255) NOT NULL DEFAULT '', `phien_ban` BIGINT NOT NULL DEFAULT 0, `luc` DATETIME NOT NULL, PRIMARY KEY (`id`), KEY `idx_chls_khoa` (`khoa`), KEY `idx_chls_luc` (`luc`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
  await exec(g, 'CREATE TABLE IF NOT EXISTS `cau_hinh_ap_dung` (`may_chu` VARCHAR(40) NOT NULL, `phien_ban` BIGINT NOT NULL DEFAULT 0, `ap_dung_luc` DATETIME NULL, `khoi_dong_luc` DATETIME NULL, `nhip_luc` DATETIME NULL, `so_khoa` INT NOT NULL DEFAULT 0, `so_loi` INT NOT NULL DEFAULT 0, `loi` TEXT NULL, `cho_khoi_dong_lai` TEXT NULL, `thay_doi_cuoi` TEXT NULL, PRIMARY KEY (`may_chu`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4')
  await exec(g, "CREATE TABLE IF NOT EXISTS `cau_hinh_hieu_luc` (`may_chu` VARCHAR(40) NOT NULL, `khoa` VARCHAR(64) NOT NULL, `gia_tri` VARCHAR(255) NULL, `nguon` VARCHAR(12) NOT NULL DEFAULT '', `cho_khoi_dong_lai` VARCHAR(255) NULL, `loi` VARCHAR(255) NULL, `cap_nhat_luc` DATETIME NULL, PRIMARY KEY (`may_chu`, `khoa`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
  await exec(g, "CREATE TABLE IF NOT EXISTS `cau_hinh_khoi_dong_lai` (`id` TINYINT NOT NULL, `trang_thai` VARCHAR(12) NOT NULL DEFAULT '', `giay` INT NOT NULL DEFAULT 60, `ly_do` VARCHAR(200) NOT NULL DEFAULT '', `yeu_cau_boi` VARCHAR(50) NULL, `yeu_cau_luc` DATETIME NULL, `bat_dau_luc` DATETIME NULL, `thoat_luc` DATETIME NULL, `xong_luc` DATETIME NULL, `may_chu` VARCHAR(40) NULL, PRIMARY KEY (`id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4")
  await exec(g, 'INSERT IGNORE INTO `cau_hinh_phien_ban` (`id`, `phien_ban`) VALUES (1, 1)')
  await exec(g, "INSERT IGNORE INTO `cau_hinh_khoi_dong_lai` (`id`, `trang_thai`) VALUES (1, '')")
  daTao = true
}

// ---------------------------------------------------------------- đọc nhanh cho nơi dùng (nhớ 5 giây)

let nho: { gt: Record<string, string>, luc: number } | null = null

async function docNho(): Promise<Record<string, string>> {
  if (nho && Date.now() - nho.luc < 5000) return nho.gt
  const gt: Record<string, string> = {}
  await napDanhMucTTServer()
  const can = danhMucTT().filter(k => k.bang === 'cau_hinh' && k.dungO !== 'server').map(k => k.khoa)
  if (can.length) {
    for (const r of await q<{ khoa: string, gia_tri: string }>(dbGame(), `SELECT khoa, gia_tri FROM cau_hinh WHERE khoa IN (${can.map(() => '?').join(',')})`, can)) {
      gt[r.khoa] = String(r.gia_tri ?? '')
    }
  }
  nho = { gt, luc: Date.now() }
  return gt
}

/**
 * Giá trị hiệu lực phía web của một khoá cau_hinh (CSDL hợp lệ > mặc định — web không thấy server.ini).
 * Khoá ẩn (cố định / luôn bật) trên máy thật: luôn giá trị cố định, không đọc CSDL (docs/cau-hinh.md).
 * Lỗi CSDL / giá trị sai: `khiLoi` (công tắc thu tiền truyền '0' = FAIL CLOSED).
 */
export async function chGiaTri(khoa: string, khiLoi?: string): Promise<string> {
  await napDanhMucTTServer()
  const k = khoaTT(khoa)
  if (!k) throw new Error(`Khoá không có trong danh mục: ${khoa}`)
  if (anTT(k) && !choPhepThuNghiemWeb()) return k.macDinh
  let tho: string | undefined
  try {
    tho = (await docNho())[khoa]
  } catch {
    return khiLoi ?? k.macDinh
  }
  if (tho === undefined) return k.macDinh
  const r = kiemGiaTriTT(k, tho)
  return r.ok ? r.giaTri : (khiLoi ?? k.macDinh)
}

export async function chCo(khoa: string, khiLoi: boolean): Promise<boolean> {
  return (await chGiaTri(khoa, khiLoi ? '1' : '0')) === '1'
}

/** Công tắc bán theo loại gói nạp (thanh_vien có công tắc riêng thanh_vien.ban_bat, kiểm ở tvBanDuoc). */
export const KHOA_BAN_THEO_LOAI: Record<string, string> = {
  luong: 'nap.luong.bat',
  the_thang: 'nap.the_thang.bat',
  ve_mua: 'nap.ve_mua.bat',
}

/** Loại gói này đang được bán không (fail closed khi đọc lỗi). */
export async function chBanLoai(loai: string): Promise<boolean> {
  const k = KHOA_BAN_THEO_LOAI[loai]
  return k ? chCo(k, false) : true
}

export function chXoaNho() {
  nho = null
}

// ---------------------------------------------------------------- trạng thái cho trang

export interface DongLuuTT { giaTri: string, suaBoi: string | null, suaLuc: string | null }

export async function chDocLuu(): Promise<Record<string, DongLuuTT>> {
  await napDanhMucTTServer()
  const ra: Record<string, DongLuuTT> = {}
  for (const r of await q<{ khoa: string, gia_tri: string, sua_boi: string | null, sua_luc: string | null }>(dbGame(), 'SELECT khoa, gia_tri, sua_boi, sua_luc FROM cau_hinh')) {
    ra[r.khoa] = { giaTri: String(r.gia_tri ?? ''), suaBoi: r.sua_boi, suaLuc: r.sua_luc }
  }
  const nap = danhMucTT().filter(k => k.bang === 'nap_cau_hinh').map(k => k.khoa)
  if (nap.length && await coBang(dbGame(), 'nap_cau_hinh')) {
    for (const r of await q<{ khoa: string, gia_tri: string, cap_nhat_luc: string | null }>(dbGame(), `SELECT khoa, gia_tri, cap_nhat_luc FROM nap_cau_hinh WHERE khoa IN (${nap.map(() => '?').join(',')})`, nap)) {
      ra[`nap_cau_hinh:${r.khoa}`] = { giaTri: String(r.gia_tri ?? ''), suaBoi: null, suaLuc: r.cap_nhat_luc }
    }
  }
  return ra
}

/** Giá trị đang lưu của khoá k (đúng bảng), null = không có dòng. */
export function giaTriLuu(luu: Record<string, DongLuuTT>, k: KhoaTT): string | null {
  const d = k.bang === 'nap_cau_hinh' ? luu[`nap_cau_hinh:${k.khoa}`] : luu[k.khoa]
  return d ? d.giaTri : null
}

/** Trạng thái biến môi trường (KHÔNG có bí mật): chỉ cờ bật/tắt + tên biến còn thiếu. */
export function chTrangThaiEnv(): Record<string, string> {
  const n = useHtbbConfig().nap
  return {
    HTBB_NAP_BAT: n.bat ? '1' : '0',
    NAP_DA_CO_GIAY_PHEP: n.daCoGiayPhep ? '1' : '0',
  }
}

export async function chPhienBan(ex: PoolConnection | null = null): Promise<{ phien_ban: number, sua_luc: string | null, sua_boi: string | null }> {
  const r = await q1<{ phien_ban: number, sua_luc: string | null, sua_boi: string | null }>(ex ?? dbGame(), 'SELECT phien_ban, sua_luc, sua_boi FROM cau_hinh_phien_ban WHERE id = 1')
  return r ? { ...r, phien_ban: Number(r.phien_ban) } : { phien_ban: 0, sua_luc: null, sua_boi: null }
}

// ---------------------------------------------------------------- ghi

export interface KetQuaLuuTT { phienBan: number, thayDoi: ThayDoiTT[], canKhoiDongLai: string[] }

/**
 * Lưu nhiều khoá (tất cả hoặc không). `thayDoiTho[k] === null` = xoá dòng (máy chủ về server.ini / mặc định).
 * `phienBanMongDoi`: phiên bản trang đang xem — khác phiên bản hiện tại (ai đó vừa lưu) thì 409, tránh đè nhau.
 */
export async function chLuu(event: H3Event, admin: AdminUser, thayDoiTho: Record<string, unknown>, lyDo: string,
  nguon: string, phienBanMongDoi?: number): Promise<KetQuaLuuTT> {
  await chTaoBang()
  await napDanhMucTTServer()
  const luu = await chDocLuu()
  const dangLuu: Record<string, string | null> = {}
  for (const khoa of Object.keys(thayDoiTho)) {
    const k = khoaTT(khoa)
    if (k) dangLuu[khoa] = giaTriLuu(luu, k)
  }
  const { thayDoi, loi } = soSanhTT(dangLuu, thayDoiTho, choPhepThuNghiemWeb())
  if (Object.keys(loi).length) throw apiLoi(400, Object.values(loi).join(' · '), { loi })
  if (!thayDoi.length) return { phienBan: (await chPhienBan()).phien_ban, thayDoi: [], canKhoiDongLai: [] }
  const ai = admin.username.slice(0, 50)
  const luc = gioDb()
  const pb = await transaction(dbGame(), async (conn) => {
    const hien = await chPhienBan(conn)
    if (phienBanMongDoi !== undefined && Number.isInteger(phienBanMongDoi) && phienBanMongDoi !== hien.phien_ban) {
      throw apiLoi(409, `Cấu hình vừa được ${hien.sua_boi ?? 'người khác'} lưu (phiên bản ${hien.phien_ban}) — tải lại trang rồi sửa tiếp.`, { code: 'lech_phien_ban' })
    }
    const moi = hien.phien_ban + 1
    for (const d of thayDoi) {
      const k = khoaTT(d.khoa)!
      if (k.bang === 'nap_cau_hinh') {
        if (d.moi === null) {
          await exec(conn, 'DELETE FROM nap_cau_hinh WHERE khoa = ?', [d.khoa])
        } else {
          await exec(conn, 'INSERT IGNORE INTO nap_cau_hinh (khoa, gia_tri, cap_nhat_luc) VALUES (?, ?, ?)', [d.khoa, d.moi, luc])
          await exec(conn, 'UPDATE nap_cau_hinh SET gia_tri = ?, cap_nhat_luc = ? WHERE khoa = ?', [d.moi, luc, d.khoa])
        }
      } else if (d.moi === null) {
        await exec(conn, 'DELETE FROM cau_hinh WHERE khoa = ?', [d.khoa])
      } else {
        await exec(conn, 'INSERT IGNORE INTO cau_hinh (khoa, gia_tri, sua_boi, sua_luc) VALUES (?, ?, ?, ?)', [d.khoa, d.moi, ai, luc])
        await exec(conn, 'UPDATE cau_hinh SET gia_tri = ?, sua_boi = ?, sua_luc = ? WHERE khoa = ?', [d.moi, ai, luc, d.khoa])
      }
      const an = (v: string | null) => (v === null ? null : (k.nhay && v !== '' ? '(ẩn)' : v))
      await exec(conn,
        'INSERT INTO cau_hinh_lich_su (khoa, bang, gia_tri_cu, gia_tri_moi, nguoi_sua, nguon, ly_do, phien_ban, luc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
        [d.khoa, k.bang, an(d.cu), an(d.moi), ai, nguon.slice(0, 20), lyDo.slice(0, 255), moi, luc])
    }
    await exec(conn, 'UPDATE cau_hinh_phien_ban SET phien_ban = ?, sua_luc = ?, sua_boi = ? WHERE id = 1', [moi, luc, ai])
    return moi
  })
  chXoaNho()
  // Thành viên: gói nạp THANH_VIEN đi theo giá / công tắc bán (cùng hàm của trang Thành viên — một nguồn).
  if (thayDoi.some(d => d.khoa === 'thanh_vien.gia' || d.khoa === 'thanh_vien.ban_bat')) {
    try {
      await tvLuuCauHinh(admin.username, await tvCauHinh(true))
    } catch (e: any) {
      console.error('[cau-hinh-tt] dong bo goi Thanh vien loi:', e?.message ?? e)
    }
  }
  await ghiThayDoi(event, 'sua_cau_hinh_tt', `cau_hinh:${thayDoi.map(d => d.khoa).join(',')}`.slice(0, 150),
    Object.fromEntries(thayDoi.map(d => [d.khoa, d.cu])), { ...Object.fromEntries(thayDoi.map(d => [d.khoa, d.moi])), _lyDo: lyDo, _phienBan: pb })
  return { phienBan: pb, thayDoi, canKhoiDongLai: thayDoi.filter(d => d.apDung === 'khoi_dong_lai').map(d => d.khoa) }
}

// ---------------------------------------------------------------- khởi động lại an toàn

export async function chKhoiDongLai(): Promise<Record<string, any> | null> {
  return q1(dbGame(), 'SELECT trang_thai, giay, ly_do, yeu_cau_boi, yeu_cau_luc, bat_dau_luc, thoat_luc, xong_luc, may_chu FROM cau_hinh_khoi_dong_lai WHERE id = 1')
}

/** Máy chủ game còn sống không: nhịp xác nhận cấu hình gần nhất (giây trước). null = chưa từng. */
export async function chGiayTuNhip(): Promise<number | null> {
  const r = await q1<{ nhip_luc: string | null }>(dbGame(), 'SELECT MAX(nhip_luc) AS nhip_luc FROM cau_hinh_ap_dung')
  if (!r?.nhip_luc) return null
  const t = new Date(String(r.nhip_luc).replace(' ', 'T')).getTime()
  return Number.isNaN(t) ? null : Math.max(0, Math.round((Date.now() - t) / 1000))
}
