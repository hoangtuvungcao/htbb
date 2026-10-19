/**
 * CMS: đọc/ghi nội dung website trong htbb_db (bảng tạo bởi tools/luu_tru_sinh_du_lieu/sql_nguon/web_cms.sql).
 *
 * Nguyên tắc: thiếu bảng / lỗi CSDL -> trang công khai vẫn chạy bằng nội dung mặc định
 * (shared/utils/cmsMacDinh.ts, tin tức tĩnh, dữ liệu CSV). Đọc công khai có bộ nhớ đệm ngắn,
 * mọi lần admin ghi gọi xoaDemCms() để trang thấy ngay.
 */
import { CMS_MAC_DINH, KHOA_CMS, chuanHoaCmsSite, chuanHoaMenu, hopNhatCms, lienKetAnToan, lienKetNgoaiThat, lienKetZalo, tronSau, HOST_ZALO } from '../../shared/utils/cmsMacDinh'
import type { CmsCongKhai, CmsMenu, CmsSite, KhoaCms } from '../../shared/utils/cmsMacDinh'
import { chuanHoaBanTai, loiBanTai } from '../../shared/utils/taiGame'

import type { H3Event } from 'h3'

export const BANG_CMS = ['web_cai_dat', 'web_tin_tuc', 'web_trang', 'web_faq', 'web_media', 'web_su_kien_lich',
  'web_admin_vai_tro', 'web_khoa_tai_khoan', 'web_admin_thay_doi'] as const

export async function coBangCms(bang: typeof BANG_CMS[number] = 'web_cai_dat'): Promise<boolean> {
  try { return await coBang(dbGame(), bang) }
  catch { return false }
}

export async function canBangCms(bang: typeof BANG_CMS[number]) {
  if (!(await coBangCms(bang))) {
    throw apiLoi(409, `Chưa có bảng ${bang}. Chạy file sql/nang_cap.sql vào database game rồi tải lại trang.`, { code: 'chua_co_bang' })
  }
}

// ---------------------------------------------------------------- bộ nhớ đệm đọc công khai

const dem = new Map<string, { luc: number, v: unknown }>()
export async function quaDem<T>(khoa: string, giay: number, fn: () => Promise<T>): Promise<T> {
  const c = dem.get(khoa)
  if (c && Date.now() - c.luc < giay * 1000) return c.v as T
  const v = await fn()
  dem.set(khoa, { luc: Date.now(), v })
  return v
}
export function xoaDemCms() {
  dem.clear()
}

// ---------------------------------------------------------------- cài đặt (khoá -> JSON)

export async function docCaiDatDb(): Promise<Partial<Record<KhoaCms, unknown>>> {
  if (!(await coBangCms('web_cai_dat'))) return {}
  try {
    const rows = await q<{ khoa: string, gia_tri: string }>(dbGame(), 'SELECT khoa, gia_tri FROM web_cai_dat')
    const ra: Partial<Record<KhoaCms, unknown>> = {}
    for (const r of rows) {
      if (!(KHOA_CMS as readonly string[]).includes(r.khoa)) continue
      try { ra[r.khoa as KhoaCms] = JSON.parse(r.gia_tri) }
      catch { /* JSON hỏng: bỏ qua, dùng mặc định */ }
    }
    return ra
  }
  catch { return {} }
}

/** Nội dung công khai đã trộn mặc định (đệm 15 giây). */
export function cmsCongKhai(): Promise<CmsCongKhai> {
  return quaDem('cong-khai', 15, async () => hopNhatCms(await docCaiDatDb()))
}

// ---------------------------------------------------------------- lịch sử thay đổi (trước/sau)

export async function ghiThayDoi(event: H3Event, hanhDong: string, doiTuong: string, truoc: unknown, sau: unknown) {
  const j = (v: unknown) => (v === undefined || v === null ? '' : (typeof v === 'string' ? v : JSON.stringify(v))).slice(0, 200_000)
  await ghiNhatKy(event, hanhDong, doiTuong.slice(0, 100), 'xem trước/sau ở Lịch sử thay đổi')
  try {
    if (!(await coBangCms('web_admin_thay_doi'))) return
    await exec(dbGame(),
      'INSERT INTO web_admin_thay_doi (admin_user, hanh_dong, doi_tuong, truoc, sau, ip) VALUES (?, ?, ?, ?, ?, ?)',
      [String(event.context.htbbAdmin?.username ?? '').slice(0, 50), hanhDong.slice(0, 50), doiTuong.slice(0, 150), j(truoc), j(sau), ipKhach(event)])
  }
  catch { /* nhật ký không được làm hỏng thao tác chính */ }
}

// ---------------------------------------------------------------- kiểm tra dữ liệu vào

export function slugHopLe(s: unknown): string {
  const v = (typeof s === 'string' ? s : '').trim().toLowerCase()
  if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(v) || v.length > 120) {
    throw apiLoi(400, 'Đường dẫn (slug) chỉ gồm chữ thường không dấu, số và dấu gạch ngang, tối đa 120 ký tự.')
  }
  return v
}

/** Chuỗi bắt buộc độ dài [min, max]. */
export function chuoi(v: unknown, ten: string, max: number, min = 0): string {
  const s = typeof v === 'string' ? v.trim() : ''
  if (s.length < min) throw apiLoi(400, `${ten}: bắt buộc nhập.`)
  if (s.length > max) throw apiLoi(400, `${ten}: tối đa ${max} ký tự.`)
  return s
}

/** 'YYYY-MM-DD HH:MM[:SS]' hoặc ISO -> 'YYYY-MM-DD HH:MM:SS' giờ máy chủ; rỗng -> null. */
export function ngayGio(v: unknown): string | null {
  const s = typeof v === 'string' ? v.trim() : ''
  if (s === '') return null
  const m = s.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{2}:\d{2})(?::(\d{2}))?/)
  if (!m) throw apiLoi(400, 'Ngày giờ không hợp lệ (dạng 2026-10-20 08:00).')
  return `${m[1]} ${m[2]}:${m[3] ?? '00'}`
}

export function gioHienTai(): string {
  const d = new Date()
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

/**
 * Nắn + kiểm một khối cài đặt trước khi lưu. Khối 'site': link Zalo phải là https trên host Zalo
 * (HOST_ZALO), link mạng xã hội phải là http(s) thật — sai thì báo lỗi rõ thay vì lặng lẽ bỏ.
 */
export function nanCaiDatLuu<K extends KhoaCms>(khoa: K, giaTri: unknown): CmsCongKhai[K] {
  if (khoa === 'taiGame') {
    const ban = chuanHoaBanTai((giaTri as { ban?: unknown } | null)?.ban)
    for (const b of ban) {
      const loi = loiBanTai(b)
      if (loi) throw apiLoi(400, loi)
    }
    return { ban } as CmsCongKhai[K]
  }
  const moi = tronSau(CMS_MAC_DINH[khoa], giaTri)
  if (khoa === 'menu') return chuanHoaMenu(moi as CmsMenu) as CmsCongKhai[K]
  if (khoa !== 'site') return moi
  const site = moi as CmsSite
  const urlZalo = site.hoTroZalo.url.trim()
  if (urlZalo !== '' && !lienKetZalo(urlZalo)) {
    throw apiLoi(400, `Link Zalo không hợp lệ: chỉ nhận https://${HOST_ZALO.join(', https://')} (vd. https://zalo.me/g/abcxyz).`)
  }
  for (const m of site.mangXaHoi) {
    const u = typeof m?.url === 'string' ? m.url.trim() : ''
    if (u !== '' && u !== 'https://' && u !== 'http://' && u !== '#' && !lienKetNgoaiThat(u)) {
      throw apiLoi(400, `Mạng xã hội "${String(m.ten ?? '').slice(0, 40)}": link phải bắt đầu bằng https:// và có tên miền.`)
    }
  }
  return chuanHoaCmsSite(site) as CmsCongKhai[K]
}

/** Đọc body JSON, chỉ nhận object. */
export async function bodyObj(event: H3Event): Promise<Record<string, unknown>> {
  const b = await readBody(event).catch(() => null)
  if (!b || typeof b !== 'object' || Array.isArray(b)) throw apiLoi(400, 'Dữ liệu gửi lên không hợp lệ.')
  return b as Record<string, unknown>
}
