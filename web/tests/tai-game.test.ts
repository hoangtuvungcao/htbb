/** Bản tải game (shared/utils/taiGame.ts). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  DS_NEN_TANG, banTaiMacDinh, banTaiSanSang, chuKyTepHopLe, chuanHoaBanTai, hrefCaiDat, loiBanTai, nenTang,
  nenTangTheoUa, tenTepGameAnToan, urlTaiHopLe,
} from '../shared/utils/taiGame.ts'

test('chuẩn hoá: dữ liệu cũ (4 nền tảng, không trangThai) -> đủ 6 nền tảng, giữ thứ tự đã lưu', () => {
  const cu = [
    { ma: 'jar', ten: 'Java', url: 'https://x.vn/a.jar', hien: true },
    { ma: 'android', ten: 'Android', url: '', hien: true },
    { ma: 'ios', url: '#', hien: false },
    { ma: 'la' }, { ma: 'jar', ten: 'trùng' },
  ]
  const ds = chuanHoaBanTai(cu)
  assert.deepEqual(ds.map(b => b.ma).slice(0, 3), ['jar', 'android', 'ios'])
  assert.equal(ds.length, DS_NEN_TANG.length)
  assert.equal(ds[0]!.trangThai, 'san-sang')
  assert.equal(ds[1]!.trangThai, 'san-sang') // mặc định của Android
  assert.equal(ds[2]!.url, '')
  assert.equal(ds[2]!.kieu, 'testflight')
  assert.ok(ds.some(b => b.ma === 'pc-linux' && !b.hien))
})

test('mặc định có thẻ iOS "Sắp ra mắt" đang hiện', () => {
  const ios = banTaiMacDinh(nenTang('ios')!)
  assert.equal(ios.hien, true)
  assert.equal(ios.trangThai, 'sap-ra-mat')
  assert.equal(banTaiSanSang(ios), false)
})

test('link: https, đường dẫn nội bộ; chặn javascript:, //, user:pass, ..', () => {
  assert.equal(urlTaiHopLe('/tai/htbb-1.0.apk'), '/tai/htbb-1.0.apk')
  assert.equal(urlTaiHopLe('https://cdn.vn/a.zip'), 'https://cdn.vn/a.zip')
  for (const u of ['javascript:alert(1)', '//evil.vn/a', 'https://a:b@x.vn/', '/tai/../etc', 'ftp://x/a', 'http://x.vn/a']) assert.equal(urlTaiHopLe(u), '', u)
})

test('kiểm theo kiểu link', () => {
  const ios = { ...banTaiMacDinh(nenTang('ios')!) }
  assert.equal(loiBanTai({ ...ios, url: 'https://testflight.apple.com/join/abc' }), null)
  assert.match(loiBanTai({ ...ios, url: 'https://evil.vn/join/abc' })!, /testflight/)
  assert.match(loiBanTai({ ...ios, kieu: 'tep' })!, /kiểu link/)
  assert.match(loiBanTai({ ...ios, kieu: 'ipa-manifest', url: '/tai/m.plist' })!, /https/)
  assert.match(loiBanTai({ ...ios, sha256: 'xyz' })!, /SHA-256/)
  const m = hrefCaiDat({ kieu: 'ipa-manifest', url: 'https://x.vn/m.plist' })
  assert.equal(m, 'itms-services://?action=download-manifest&url=https%3A%2F%2Fx.vn%2Fm.plist')
})

test('nhận diện thiết bị', () => {
  assert.equal(nenTangTheoUa('Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)'), 'ios')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (Linux; Android 13; SM-A505F)'), 'android')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (Windows NT 10.0; Win64; x64)'), 'pc')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0)'), 'macos')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (X11; Linux x86_64)'), 'pc-linux')
  assert.equal(nenTangTheoUa('Nokia6300/2.0 Profile/MIDP-2.0'), 'jar')
})

test('tên tệp + chữ ký byte', () => {
  assert.equal(tenTepGameAnToan('../../HTBB Bảo Bối 1.0.apk'), 'HTBB-Bao-Boi-1.0.apk')
  assert.equal(tenTepGameAnToan('x.sh'), null)
  assert.equal(tenTepGameAnToan('.hidden.zip'), 'hidden.zip')
  const pk = new Uint8Array([0x50, 0x4B, 0x03, 0x04, 1])
  assert.ok(chuKyTepHopLe('apk', pk))
  assert.ok(!chuKyTepHopLe('exe', pk))
  assert.ok(chuKyTepHopLe('exe', new Uint8Array([0x4D, 0x5A])))
  assert.ok(chuKyTepHopLe('plist', new TextEncoder().encode('<?xml version="1.0"?>')))
  assert.ok(!chuKyTepHopLe('plist', new Uint8Array([0x3C, 0, 1])))
})
