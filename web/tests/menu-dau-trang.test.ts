/** Menu đầu trang công khai (shared/utils/menuDauTrang.ts + mặc định trong cmsMacDinh.ts). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { TOI_DA_MUC_CHINH, chiaDauTrang, chuanHoaDauTrang, soMucVua } from '../shared/utils/menuDauTrang.ts'
import { CMS_MAC_DINH, hopNhatCms } from '../shared/utils/cmsMacDinh.ts'

test('mặc định: tối đa 6–7 thứ hiện trên thanh (mục chính + Thêm + nút Tải game)', () => {
  const c = chiaDauTrang(chuanHoaDauTrang(CMS_MAC_DINH.menu.dauTrang))
  assert.ok(c.chinh.length + (c.them.length ? 1 : 0) + (c.nut ? 1 : 0) <= 7)
  assert.equal(c.nut?.to, '/download')
  assert.deepEqual(c.them.map(m => m.to).sort(), ['/gioi-thieu', '/news'])
  // Mục tài khoản không nằm trong menu (chúng ở menu ảnh đại diện)
  for (const to of ['/naptien', '/thanh-vien', '/kichhoat', '/admin', '/logout']) {
    assert.ok(!CMS_MAC_DINH.menu.dauTrang.some(m => m.to === to), to)
  }
})

test('menu cũ đã lưu (không có vi): Tải game thành nút, Giới thiệu / Tin tức vào Thêm', () => {
  const cu = [
    { label: 'Giới thiệu', to: '/gioi-thieu', icon: 'i-lucide-info' },
    { label: 'Tân thủ', to: '/huong-dan' },
    { label: 'Tin tức', to: '/news' },
    { label: 'Tải game', to: '/download' },
  ]
  const ds = chuanHoaDauTrang(cu)
  assert.deepEqual(ds.map(m => m.vi), ['them', 'chinh', 'them', 'nut'])
  const hop = hopNhatCms({ menu: { dauTrang: cu } })
  assert.deepEqual(hop.menu.dauTrang.map(m => m.vi), ['them', 'chinh', 'them', 'nut'])
})

test('nắn: bỏ mục rỗng / sai kiểu, vi lạ -> chinh, chỉ một nút nổi bật', () => {
  const ds = chuanHoaDauTrang([
    { label: 'A', to: '/a', vi: 'nut' },
    { label: 'B', to: '/b', vi: 'nut' },
    { label: '', to: '/c', vi: 'chinh' },
    { label: 'D', to: '/d', vi: 'la' },
    null, 5, 'x',
  ])
  assert.deepEqual(ds.map(m => `${m.label}:${m.vi}`), ['A:nut', 'B:chinh', 'D:chinh'])
  assert.deepEqual(chuanHoaDauTrang('khong phai mang'), [])
  // CSDL lưu menu rỗng -> vẫn có menu mặc định
  assert.ok(hopNhatCms({ menu: { dauTrang: [] } }).menu.dauTrang.length > 0)
})

test(`quá ${TOI_DA_MUC_CHINH} mục chính: phần dư dồn vào Thêm`, () => {
  const ds = Array.from({ length: TOI_DA_MUC_CHINH + 2 }, (_, i) => ({ label: `M${i}`, to: `/m${i}`, vi: 'chinh' as const }))
  const c = chiaDauTrang(ds)
  assert.equal(c.chinh.length, TOI_DA_MUC_CHINH)
  assert.equal(c.them.length, 2)
})

test('soMucVua: đủ chỗ hiện hết, thiếu chỗ thì chừa chỗ cho Thêm', () => {
  assert.equal(soMucVua([100, 100, 100], 300, 80, false), 3)
  assert.equal(soMucVua([100, 100, 100], 300, 80, true), 2) // có mục "Thêm" sẵn: 300 + 80 > 300
  assert.equal(soMucVua([100, 100, 100], 299, 80, false), 2) // 80 + 100 + 100 = 280
  assert.equal(soMucVua([100, 100, 100], 150, 80, false), 0)
  assert.equal(soMucVua([], 0, 80, true), 0)
})
