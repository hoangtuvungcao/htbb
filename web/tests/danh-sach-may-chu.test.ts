/**
 * /server.txt + /NQSH2.txt (danh sách máy chủ cho client game), link cũ /kpah/… /*.php của
 * website PHP đã bỏ, và các tệp JSON danh mục do công cụ Java
 * của repo game sinh (shared/data/danh-muc-*.json, bang-cap-do.json). Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { bienDanhSachMayChu, bienDanhSachNqsh, docDanhSachMayChu } from '../server/utils/danhSachMayChu.ts'
import { xuLyDuongDanCu } from '../server/utils/duongDanCu.ts'

test('server.txt: một máy chủ', () => {
  assert.equal(docDanhSachMayChu('Server 1:203.0.113.10:19129:0'), 'Server 1:203.0.113.10:19129:0\n')
})

test('server.txt: nhiều máy chủ ngăn bằng "\\n" chữ hoặc xuống dòng thật', () => {
  const ra = 'Server 1:203.0.113.10:19129:0\nServer 2:sv.example.vn:19130:1\n'
  assert.equal(docDanhSachMayChu('Server 1:203.0.113.10:19129:0\\nServer 2:sv.example.vn:19130:1'), ra)
  assert.equal(docDanhSachMayChu('Server 1:203.0.113.10:19129:0\r\nServer 2:sv.example.vn:19130:1\n'), ra)
})

test('server.txt: bỏ dòng sai định dạng; không còn gì -> null', () => {
  assert.equal(docDanhSachMayChu('rac\nServer 1:1.2.3.4:19129:0\nS:1.2.3.4:99999:0\nS:1.2.3.4:abc:0'), 'Server 1:1.2.3.4:19129:0\n')
  assert.equal(docDanhSachMayChu(''), null)
  assert.equal(docDanhSachMayChu(undefined), null)
  assert.equal(docDanhSachMayChu('chi:co:ba'), null)
})

test('server.txt: HTBB_SERVER_LIST trước, KPAH_SERVER_LIST dự phòng', () => {  // TUONG-THICH-TEN-CU
  assert.equal(bienDanhSachMayChu({ HTBB_SERVER_LIST: 'a', KPAH_SERVER_LIST: 'b' }), 'a')  // TUONG-THICH-TEN-CU
  assert.equal(bienDanhSachMayChu({ HTBB_SERVER_LIST: ' ', KPAH_SERVER_LIST: 'b' }), 'b')  // TUONG-THICH-TEN-CU
  assert.equal(bienDanhSachMayChu({}), undefined)
})

test('NQSH2.txt: HTBB_NQSH_LIST riêng nếu có, không thì dùng chung HTBB_SERVER_LIST', () => {
  assert.equal(bienDanhSachNqsh({ HTBB_NQSH_LIST: 'n', HTBB_SERVER_LIST: 's' }), 'n')
  assert.equal(bienDanhSachNqsh({ KPAH_NQSH_LIST: 'k', HTBB_SERVER_LIST: 's' }), 'k')  // TUONG-THICH-TEN-CU
  assert.equal(bienDanhSachNqsh({ HTBB_SERVER_LIST: 's' }), 's')
  assert.equal(bienDanhSachNqsh({}), undefined)
})

test('link cũ: danh sách máy chủ trả thẳng nội dung (không chuyển hướng), có/không tiền tố /kpah', () => {
  for (const d of ['/server.txt', '/kpah/server.txt']) assert.deepEqual(xuLyDuongDanCu(d), { loai: 'danh_sach', bien: 'server' })
  for (const d of ['/NQSH2.txt', '/server/NQSH2.txt', '/kpah/NQSH2.txt', '/kpah/server/NQSH2.txt', '/NQSH2.txt?x=1']) {
    assert.deepEqual(xuLyDuongDanCu(d), { loai: 'danh_sach', bien: 'nqsh' }, d)
  }
})

test('link cũ: trang .php -> 301 sang trang Nuxt, giữ query string', () => {
  const c = (d: string, m = 'GET') => xuLyDuongDanCu(d, m)
  assert.deepEqual(c('/kpah/register.php'), { loai: 'chuyen', dich: '/register', ma: 301 })
  assert.deepEqual(c('/register.php?ref=a'), { loai: 'chuyen', dich: '/register?ref=a', ma: 301 })
  assert.deepEqual(c('/kpah/index.php'), { loai: 'chuyen', dich: '/', ma: 301 })
  assert.deepEqual(c('/kpah/kichhoat.php?ma=x'), { loai: 'chuyen', dich: '/kichhoat?ma=x', ma: 301 })
  assert.deepEqual(c('/kpah/admin_naptien.php'), { loai: 'chuyen', dich: '/xac-nhan-nap', ma: 301 })
  assert.deepEqual(c('/kpah/admin/giam_level.php'), { loai: 'chuyen', dich: '/admin/giam-level', ma: 301 })
  assert.deepEqual(c('/admin/nhanvat.php?id=5'), { loai: 'chuyen', dich: '/admin/nhanvat?id=5', ma: 301 })
  assert.deepEqual(c('/kpah/admin/khong_co.php'), { loai: 'chuyen', dich: '/admin', ma: 301 })
  assert.deepEqual(c('/kpah/khong_co.php'), { loai: 'chuyen', dich: '/', ma: 301 })
  // POST (webhook SePay cũ) -> 308 giữ method + body
  assert.deepEqual(c('/kpah/webhook_sepay.php', 'POST'), { loai: 'chuyen', dich: '/api/nap/webhook-sepay', ma: 308 })
})

test('link cũ: /kpah/<khác> -> bỏ tiền tố; tệp nội bộ PHP -> trang chủ; đường dẫn Nuxt thường không đụng', () => {
  assert.deepEqual(xuLyDuongDanCu('/kpah'), { loai: 'chuyen', dich: '/', ma: 301 })
  assert.deepEqual(xuLyDuongDanCu('/kpah/'), { loai: 'chuyen', dich: '/', ma: 301 })
  assert.deepEqual(xuLyDuongDanCu('/kpah/admin/'), { loai: 'chuyen', dich: '/admin', ma: 301 })
  assert.deepEqual(xuLyDuongDanCu('/kpah/assets/app.css'), { loai: 'chuyen', dich: '/', ma: 301 })
  assert.deepEqual(xuLyDuongDanCu('/kpah/inc/db.php'), { loai: 'chuyen', dich: '/', ma: 301 })
  for (const d of ['/', '/register', '/admin', '/admin/nhanvat', '/api/nap/webhook-sepay', '/kpahx', '/robots.txt', '/server.txt.bak']) {  // TUONG-THICH-TEN-CU
    assert.equal(xuLyDuongDanCu(d), null, d)
  }
})

const docJson = (ten: string) => JSON.parse(readFileSync(fileURLToPath(new URL(`../shared/data/${ten}`, import.meta.url)), 'utf8'))

test('danh mục sinh từ Java: đúng hình dạng mà server/utils/*.gen.ts và capDo.ts đọc', () => {
  const thu = docJson('danh-muc-thu.json').thu as Record<string, string>
  assert.equal(Object.keys(thu).length, 31)
  assert.deepEqual(Object.keys(thu).map(Number), [...Array(31).keys()], 'khoá = chỉ số mảng 0..30 liên tục')
  assert.equal(thu['5'], thu['28'], 'chỉ số 5 và 28 trùng tên thật trong Java')

  const ngoc = docJson('danh-muc-ngoc.json').ngoc as Record<string, { ten: string, ban: boolean, nhom: string }>
  assert.equal(Object.keys(ngoc).length, 270)
  for (const v of Object.values(ngoc)) {
    assert.equal(typeof v.ten, 'string'); assert.equal(typeof v.ban, 'boolean'); assert.match(v.nhom, /^(|NL[SC]C[1-6])$/)
  }
  assert.equal(Object.values(ngoc).filter(v => v.nhom !== '').length, 60)

  const dh = docJson('danh-muc-danh-hieu.json').danhHieu as Record<string, { ten: string, ngauNhien: boolean }>
  assert.equal(Object.values(dh).filter(v => v.ngauNhien).length, 34)
  assert.ok(!('8999' in dh), 'danh hiệu TEST không được lọt vào')

  const cd = docJson('bang-cap-do.json')
  assert.equal(cd.capDoToiDa, 90)
  assert.equal(cd.expMain.length, cd.capDoToiDa + 1)
  assert.equal(cd.expRequire.length, cd.expMain.length + 1)
  for (const v of [...cd.expMain, ...cd.expRequire]) assert.ok(Number.isSafeInteger(v) && v >= 0)
})
