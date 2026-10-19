/**
 * Menu đầu trang công khai (sửa ở /admin/menu, lưu trong web_cai_dat khoá 'menu').
 *
 * Mỗi mục có một VỊ TRÍ:
 *   'chinh' — hiện thẳng trên thanh (tối đa vài mục; không đủ chỗ thì tự dồn vào "Thêm ▾");
 *   'them'  — luôn nằm trong "Thêm ▾";
 *   'nut'   — nút nổi bật bên phải menu (Tải game). Chỉ một mục; mục 'nut' thứ hai coi như 'chinh'.
 * Mục tài khoản (Nạp / Thành viên / Kích hoạt / Quản trị / Đăng xuất) KHÔNG nằm ở đây: chúng ở menu
 * ảnh đại diện (SiteHeader), tự hiện theo trạng thái đăng nhập.
 *
 * Menu đã lưu từ trước (không có trường `vi`) được nắn khi đọc: /download -> nút, /gioi-thieu và /news
 * -> Thêm, còn lại -> chính. Nhờ vậy dòng cũ trong CSDL vẫn hiện gọn, không cần chạy SQL.
 */
export type ViTriMenu = 'chinh' | 'them' | 'nut'

export interface MucMenu { label: string, to: string, icon?: string, vi?: ViTriMenu }

export const VI_TRI_MENU: { value: ViTriMenu, label: string }[] = [
  { value: 'chinh', label: 'Thanh menu' },
  { value: 'them', label: 'Trong "Thêm ▾"' },
  { value: 'nut', label: 'Nút nổi bật' },
]

/** Số mục 'chinh' tối đa hiện trên thanh dù màn rộng tới đâu (phần dư vào "Thêm ▾"). */
export const TOI_DA_MUC_CHINH = 5

const VAO_THEM_CU = ['/gioi-thieu', '/news']
const NUT_CU = ['/download']

/** Nắn danh sách mục: thêm `vi` cho dòng cũ, bỏ mục rỗng, chỉ giữ một nút nổi bật. */
export function chuanHoaDauTrang(ds: unknown): MucMenu[] {
  if (!Array.isArray(ds)) return []
  const coViTri = ds.some(m => m && typeof m === 'object' && 'vi' in m)
  let daCoNut = false
  const ra: MucMenu[] = []
  for (const m of ds) {
    if (!m || typeof m !== 'object') continue
    const o = m as Record<string, unknown>
    const label = typeof o.label === 'string' ? o.label.trim().slice(0, 40) : ''
    const to = typeof o.to === 'string' ? o.to.trim().slice(0, 300) : ''
    if (label === '' || to === '') continue
    let vi: ViTriMenu = o.vi === 'them' || o.vi === 'nut' || o.vi === 'chinh' ? o.vi : 'chinh'
    if (!coViTri) vi = NUT_CU.includes(to) ? 'nut' : (VAO_THEM_CU.includes(to) ? 'them' : 'chinh')
    if (vi === 'nut') {
      if (daCoNut) vi = 'chinh'
      daCoNut = true
    }
    const muc: MucMenu = { label, to, vi }
    if (typeof o.icon === 'string' && o.icon.trim() !== '') muc.icon = o.icon.trim().slice(0, 60)
    ra.push(muc)
  }
  return ra
}

/** Chia mục theo vị trí (giữ thứ tự). */
export function chiaDauTrang(ds: MucMenu[]): { chinh: MucMenu[], them: MucMenu[], nut: MucMenu | null } {
  const chinh = ds.filter(m => (m.vi ?? 'chinh') === 'chinh')
  return {
    chinh: chinh.slice(0, TOI_DA_MUC_CHINH),
    them: [...chinh.slice(TOI_DA_MUC_CHINH), ...ds.filter(m => m.vi === 'them')],
    nut: ds.find(m => m.vi === 'nut') ?? null,
  }
}

/**
 * Bao nhiêu mục 'chinh' (theo thứ tự) vừa chỗ trống `rongCho` px. `rongMuc` = bề rộng đo được của
 * từng mục; `rongThem` = bề rộng nút "Thêm ▾"; `luonCoThem` = đằng nào cũng có "Thêm ▾" (có mục 'them').
 * Không vừa hết thì chừa chỗ cho "Thêm ▾" để các mục bị dồn có chỗ hiện.
 */
export function soMucVua(rongMuc: number[], rongCho: number, rongThem: number, luonCoThem: boolean): number {
  const tong = rongMuc.reduce((a, b) => a + b, 0)
  if (tong + (luonCoThem ? rongThem : 0) <= rongCho) return rongMuc.length
  let dung = rongThem
  let n = 0
  for (const r of rongMuc) {
    if (dung + r > rongCho) break
    dung += r
    n++
  }
  return n
}
