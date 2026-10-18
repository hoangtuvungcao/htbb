/**
 * ĐƯỜNG DẪN CỦA WEBSITE PHP CŨ (HTBB/web, đã bỏ 19/10/2026 — docs/kien-truc.md).
 * File THUẦN (không import) để npm test kiểm trực tiếp; middleware duong-dan-cu.ts dùng nó.
 *
 * Website PHP được phục vụ ở CẢ gốc (DocumentRoot /opt/htbb/web) lẫn /kpah/ (symlink
 * /var/www/html/kpah), nên link cũ có hai dạng: /register.php và /kpah/register.php.
 *
 *   1. DANH SÁCH MÁY CHỦ — client đã phát hành tải trực tiếp, KHÔNG chuyển hướng (client cũ
 *      có thể không theo 301): /server.txt (client Unity), /server/NQSH2.txt và /NQSH2.txt
 *      (bản thật trên PHP; /server.txt chỉ là symlink tới nó), kèm tiền tố /htbb. Trả luôn
 *      nội dung — xem danhSachMayChu.ts.
 *   2. Trang .php cũ -> 301 sang trang Nuxt tương ứng (giữ query string). Không phải GET/HEAD
 *      (vd SePay còn POST webhook_sepay.php) -> 308 để giữ method và body.
 *   3. /kpah hay /kpah/<gì đó khác> -> 301 về trang tương ứng bỏ tiền tố, không có thì trang chủ.
 */
export type KetQuaDuongDanCu =
  | { loai: 'danh_sach', bien: 'server' | 'nqsh' }
  | { loai: 'chuyen', dich: string, ma: 301 | 308 }
  | null

/** Trang PHP (bỏ ".php", đã bỏ tiền tố /kpah) -> trang Nuxt. */
const TRANG_PHP: Readonly<Record<string, string>> = Object.freeze({
  'index': '/',
  'register': '/register',
  'login': '/login',
  'logout': '/logout',
  'account': '/account',
  'kichhoat': '/kichhoat',
  'naptien': '/naptien',
  'order_status': '/naptien',
  'admin_naptien': '/xac-nhan-nap',
  'webhook_sepay': '/api/nap/webhook-sepay',
  'admin/index': '/admin',
  'admin/dangxuat': '/admin',
  'admin/tao_ma_2buoc': '/admin',
})

/** Trang admin/<ten>.php có trang Nuxt cùng tên (gạch dưới -> gạch ngang). */
const TRANG_ADMIN = new Set([
  'cammay', 'cauhinh', 'chandoan', 'dua-top', 'giam-level', 'giftcode', 'log', 'nang-level-toanbo',
  'nhanvat', 'nhatky', 'online', 'reset-toanbo', 'server', 'sukien', 'taikhoan', 'tao-hang-loat',
])

const DANH_SACH: Readonly<Record<string, 'server' | 'nqsh'>> = Object.freeze({
  '/server.txt': 'server',
  '/server/NQSH2.txt': 'nqsh',
  '/NQSH2.txt': 'nqsh',
})

export function xuLyDuongDanCu(duong: string, method = 'GET'): KetQuaDuongDanCu {
  const hoi = duong.indexOf('?')
  const path = hoi === -1 ? duong : duong.slice(0, hoi)
  const query = hoi === -1 ? '' : duong.slice(hoi)
  const coTienTo = path === '/kpah' || path.startsWith('/kpah/')
  const p = coTienTo ? (path.slice('/kpah'.length) || '/') : path

  const ds = DANH_SACH[p]
  if (ds) return { loai: 'danh_sach', bien: ds }

  const ma: 301 | 308 = method === 'GET' || method === 'HEAD' ? 301 : 308
  const php = /^\/((?:admin\/)?[A-Za-z0-9_]+)\.php$/.exec(p)
  if (php) {
    const ten = php[1]!
    let dich = TRANG_PHP[ten]
    if (dich === undefined && ten.startsWith('admin/')) {
      const a = ten.slice('admin/'.length).replace(/_/g, '-')
      dich = TRANG_ADMIN.has(a) ? `/admin/${a}` : '/admin'
    }
    return { loai: 'chuyen', dich: (dich ?? '/') + query, ma }
  }
  if (coTienTo) {
    // /kpah/, /kpah/admin/, /kpah/assets/... -> bỏ tiền tố; thư mục admin cũ -> /admin.
    let dich = p.replace(/\/+$/, '') || '/'
    if (dich.startsWith('/assets/') || dich.startsWith('/admin/assets/') || dich.startsWith('/inc/')
      || dich.startsWith('/admin/inc/') || dich.startsWith('/admin/test/')) dich = '/'
    return { loai: 'chuyen', dich: dich + query, ma }
  }
  return null
}
