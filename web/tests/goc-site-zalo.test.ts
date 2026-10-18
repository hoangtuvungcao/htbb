/** Địa chỉ gốc (HTBB_SITE_URL -> noindex khi chưa có tên miền) + kiểm link Zalo / mạng xã hội. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { chuanHoaGocSite, urlTuyetDoi, GOC_SITE_MAC_DINH } from '../shared/utils/gocSite.ts'
import { lienKetZalo, anhTrongSite, lienKetNgoaiThat, hopNhatCms } from '../shared/utils/cmsMacDinh.ts'

test('chưa có tên miền -> localhost, noindex', () => {
  for (const v of [undefined, '', '   ', 'không phải url', 'ftp://a.vn', 'javascript:alert(1)']) {
    assert.deepEqual(chuanHoaGocSite(v), { goc: GOC_SITE_MAC_DINH, choIndex: false }, String(v))
  }
  for (const v of ['http://localhost:3000', 'http://127.0.0.1:3992', 'http://203.0.113.7', 'http://[::1]:3000', 'http://may-chu', 'https://web.local', 'http://a.localhost']) {
    assert.equal(chuanHoaGocSite(v).choIndex, false, v)
  }
})

test('tên miền thật -> origin không có / cuối, cho index', () => {
  assert.deepEqual(chuanHoaGocSite('https://example.org/'), { goc: 'https://example.org', choIndex: true })
  assert.deepEqual(chuanHoaGocSite('https://Vi-Du.VN/abc?x=1'), { goc: 'https://vi-du.vn', choIndex: true })
  assert.deepEqual(chuanHoaGocSite('vi-du.vn'), { goc: 'https://vi-du.vn', choIndex: true })
  assert.equal(chuanHoaGocSite('https://u:p@vi-du.vn').choIndex, false)
  assert.equal(urlTuyetDoi('https://vi-du.vn', '/news/a'), 'https://vi-du.vn/news/a')
  assert.equal(urlTuyetDoi('http://localhost:3000', '/og/htbb.png'), 'http://localhost:3000/og/htbb.png')
})

test('link Zalo: chỉ https + host Zalo', () => {
  assert.equal(lienKetZalo('https://zalo.me/g/abcxyz'), 'https://zalo.me/g/abcxyz')
  assert.equal(lienKetZalo(' https://oa.zalo.me/home '), 'https://oa.zalo.me/home')
  assert.equal(lienKetZalo('https://chat.zalo.me/?g=abc'), 'https://chat.zalo.me/?g=abc')
  for (const v of ['', '#', 'http://zalo.me/g/abc', 'https://zalo.me.evil.vn/g/a', 'https://evilzalo.me/g/a', 'https://zalo.me:8443/g/a',
    'https://a@zalo.me/g/a', 'javascript:alert(1)', 'https://zalo.me/', '//zalo.me/g/a', 'https://facebook.com/x', 42, null]) {
    assert.equal(lienKetZalo(v), '', String(v))
  }
})

test('ảnh QR chỉ trong site; mạng xã hội rỗng bị bỏ', () => {
  assert.equal(anhTrongSite('/uploads/ab12.png'), '/uploads/ab12.png')
  for (const v of ['//evil/x.png', 'https://evil/x.png', '/uploads/../x', 'javascript:x', '/a b.png']) assert.equal(anhTrongSite(v), '', v)
  for (const v of ['#', 'https://', 'http://', '', 'mailto:a@b.vn']) assert.equal(lienKetNgoaiThat(v), '', v)
  assert.equal(lienKetNgoaiThat('https://www.facebook.com/trang'), 'https://www.facebook.com/trang')
  const c = hopNhatCms({ site: { hoTroZalo: { url: 'http://zalo.me/g/a', anhQr: '//x' }, mangXaHoi: [{ ten: 'Facebook', url: 'https://', icon: 'i-lucide-facebook' }, { ten: 'Discord', url: '#', icon: 'x' }] } })
  assert.equal(c.site.hoTroZalo.url, '')
  assert.equal(c.site.hoTroZalo.anhQr, '')
  assert.deepEqual(c.site.mangXaHoi, [])
  const d = hopNhatCms({ site: { hoTroZalo: { url: 'https://zalo.me/g/abc', anhQr: '/uploads/qr.png' } } })
  assert.equal(d.site.hoTroZalo.url, 'https://zalo.me/g/abc')
  assert.equal(d.site.hoTroZalo.ten, 'Hỗ trợ qua Zalo')
})
