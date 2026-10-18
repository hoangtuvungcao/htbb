/**
 * Phân quyền khu quản trị: liệt kê MỌI file route trong server/api/admin và đòi
 *   1) mỗi route (trừ 3 route đăng nhập công khai) đi qua defineAdminHandler — nơi gọi kiemQuyen;
 *   2) mỗi route khớp một quy tắc tường minh trong shared/utils/quyenAdmin.ts;
 *   3) các bất biến của ma trận (Chỉ xem không ghi được gì, Biên tập chỉ ghi nội dung web, …).
 * Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readdirSync, readFileSync, statSync } from 'node:fs'
import { join, relative } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
  DS_VAI_TRO_QUYEN, QUY_TAC_QUYEN, kiemQuyenAdmin, timQuyTacQuyen, type VaiTroQuyen,
} from '../shared/utils/quyenAdmin.ts'

const GOC = fileURLToPath(new URL('../server/api/admin', import.meta.url))

interface Route { file: string, path: string, method: string }

function lietKe(dir: string): string[] {
  return readdirSync(dir).flatMap((t) => {
    const p = join(dir, t)
    return statSync(p).isDirectory() ? lietKe(p) : [p]
  })
}

const ROUTES: Route[] = lietKe(GOC).filter(f => f.endsWith('.ts')).map((file) => {
  const rel = relative(GOC, file).replace(/\\/g, '/').replace(/\.ts$/, '')
  const m = rel.match(/^(.*?)(?:\.(get|post|put|patch|delete|head))?$/)!
  let duong = m[1]!.replace(/(^|\/)index$/, '')
  duong = duong.replace(/\[\.\.\.[^\]]+\]/g, 'a/b').replace(/\[[^\]]+\]/g, '123')
  return { file: rel, path: `/api/admin${duong ? `/${duong}` : ''}`, method: (m[2] ?? 'get').toUpperCase() }
})

/** Route đăng nhập: chưa có phiên nên không thể dùng defineAdminHandler (có chặn dò sai riêng). */
const CONG_KHAI = new Set(['auth/login.post', 'auth/status.get', 'auth/verify-2fa.post'])

const laGhi = (m: string) => !['GET', 'HEAD'].includes(m)

test('có route để kiểm', () => {
  assert.ok(ROUTES.length > 50, `chỉ thấy ${ROUTES.length} route`)
})

test('mọi route admin (trừ đăng nhập) đi qua defineAdminHandler', () => {
  for (const r of ROUTES) {
    if (CONG_KHAI.has(r.file)) continue
    const src = readFileSync(join(GOC, `${r.file}.ts`), 'utf8')
    assert.match(src, /export default defineAdminHandler\(/, `${r.file} không dùng defineAdminHandler`)
  }
})

test('mọi route admin khớp một quy tắc quyền tường minh', () => {
  const thieu = ROUTES.filter(r => !timQuyTacQuyen(r.path)).map(r => `${r.method} ${r.path} (${r.file})`)
  assert.deepEqual(thieu, [], `Thêm quy tắc vào shared/utils/quyenAdmin.ts cho:\n${thieu.join('\n')}`)
})

test('mọi quy tắc đều được dùng ít nhất bởi một route (không để quy tắc mồ côi)', () => {
  const dung = new Set(ROUTES.map(r => timQuyTacQuyen(r.path)!.mau))
  const thua = QUY_TAC_QUYEN.map(q => q.mau).filter(m => !dung.has(m))
  assert.deepEqual(thua, [])
})

test('siêu quản trị gọi được mọi route', () => {
  for (const r of ROUTES) assert.ok(kiemQuyenAdmin(r.path, r.method, 'sieu_quan_tri'), `${r.method} ${r.path}`)
})

test('Chỉ xem: không ghi được gì ngoài phiên của mình', () => {
  for (const r of ROUTES.filter(x => laGhi(x.method) && !x.path.startsWith('/api/admin/auth/'))) {
    assert.equal(kiemQuyenAdmin(r.path, r.method, 'xem'), false, `xem ghi được ${r.method} ${r.path}`)
  }
})

test('Biên tập: chỉ ghi nội dung web', () => {
  for (const r of ROUTES.filter(x => laGhi(x.method) && !x.path.startsWith('/api/admin/auth/'))) {
    const duoc = kiemQuyenAdmin(r.path, r.method, 'bien_tap')
    const laNoiDung = r.path.startsWith('/api/admin/cms/') && r.path !== '/api/admin/cms/dong-bo'
    assert.equal(duoc, laNoiDung, `bien_tap ${duoc ? 'ghi được' : 'không ghi được'} ${r.method} ${r.path}`)
  }
})

test('Hỗ trợ: chỉ ghi vào người chơi, không có thao tác phá huỷ / phát quà / hàng loạt', () => {
  const choPhep = ['/api/admin/nguoi-choi/', '/api/admin/taikhoan/', '/api/admin/nhanvat/', '/api/admin/cammay']
  const cam = [/\/nhanvat\/[^/]+\/xoa$/, /\/nhanvat\/[^/]+\/cap-do$/, /\/taikhoan\/xoa-nhieu$/, /\/nguoi-choi\/tang-qua$/]
  for (const r of ROUTES.filter(x => laGhi(x.method) && !x.path.startsWith('/api/admin/auth/'))) {
    const duoc = kiemQuyenAdmin(r.path, r.method, 'ho_tro')
    const mongDoi = choPhep.some(p => r.path.startsWith(p)) && !cam.some(c => c.test(r.path))
    assert.equal(duoc, mongDoi, `ho_tro ${duoc ? 'ghi được' : 'không ghi được'} ${r.method} ${r.path}`)
  }
})

test('chỉ siêu quản trị: phân quyền, tệp chẩn đoán, nạp tiền (cả đọc)', () => {
  const vai: VaiTroQuyen[] = ['bien_tap', 'ho_tro', 'xem']
  for (const r of ROUTES.filter(x => /^\/api\/admin\/(phan-quyen|chandoan\/file|naptien)/.test(x.path))) {
    for (const v of vai) assert.equal(kiemQuyenAdmin(r.path, r.method, v), false, `${v} gọi được ${r.method} ${r.path}`)
  }
})

test('mặc định đóng: route lạ bị cấm với mọi vai trò trừ siêu quản trị', () => {
  for (const v of DS_VAI_TRO_QUYEN.filter(x => x !== 'sieu_quan_tri')) {
    assert.equal(kiemQuyenAdmin('/api/admin/khong-ton-tai', 'GET', v), false)
    assert.equal(kiemQuyenAdmin('/api/admin/khong-ton-tai/x', 'POST', v), false)
  }
})

test('khớp mẫu: * một đoạn, ** phần còn lại, bỏ query', () => {
  assert.equal(timQuyTacQuyen('/api/admin/nhanvat/5/xoa')?.mau, '/api/admin/nhanvat/*/xoa')
  assert.equal(timQuyTacQuyen('/api/admin/nhanvat/5/khoa')?.mau, '/api/admin/nhanvat/**')
  assert.equal(timQuyTacQuyen('/api/admin/nhanvat?q=a')?.mau, '/api/admin/nhanvat/**')
  assert.equal(timQuyTacQuyen('/api/admin/cms/thay-doi')?.mau, '/api/admin/cms/thay-doi')
  assert.equal(timQuyTacQuyen('/api/admin/cammayx'), null)
})
