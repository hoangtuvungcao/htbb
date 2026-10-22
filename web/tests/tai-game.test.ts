/** Bản tải game (shared/utils/taiGame.ts). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  DS_NEN_TANG, MA_NEN_TANG, banTaiMacDinh, banTaiSanSang, boDongTepPhu, chuKyTepHopLe, chuanHoaBanTai, hrefCaiDat,
  laThietBiIos, loiBanTai, nenTang, nenTangTheoUa, soBanHopLe, tenTepGameAnToan, urlTaiHopLe,
} from '../shared/utils/taiGame.ts'

test('chuẩn hoá: dữ liệu (không trangThai) -> đủ 5 nền tảng, giữ thứ tự đã lưu', () => {
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
  assert.ok(ds.some(b => b.ma === 'linux' && b.hien))
})

test('đúng 5 bản tải: Java J2ME, Windows, Linux, Android, iOS (chỉ iOS "Sắp ra mắt")', () => {
  assert.deepEqual(DS_NEN_TANG.map(n => n.ma), ['jar', 'windows', 'linux', 'android', 'ios'])
  assert.deepEqual([...MA_NEN_TANG], ['jar', 'windows', 'linux', 'android', 'ios'])
  assert.ok(DS_NEN_TANG.every(n => n.hien))
  assert.deepEqual(DS_NEN_TANG.filter(n => n.trangThai === 'sap-ra-mat').map(n => n.ma), ['ios'])
  assert.equal(nenTang('android')!.heDieuHanh, 'Android 8.0 trở lên') // client_apk/pokemon minSdk 26
  assert.deepEqual(nenTang('windows')!.duoi, ['exe'])
  assert.deepEqual(nenTang('linux')!.duoi, ['appimage'])
  assert.deepEqual(nenTang('jar')!.duoi, ['jar'])
  assert.deepEqual(nenTang('android')!.duoi, ['apk'])
  for (const ma of ['pc', 'pc-java', 'pc-jar', 'pc-linux', 'macos']) assert.equal(nenTang(ma), undefined, ma)
})

/** Giá trị CMS 'taiGame' đang chạy trên máy thật (19/10/2026, bản 0.9.4) — kiểu 6 thẻ cũ + tệp phụ. */
const CMS_094_CU = [
  { ma: 'android', ten: 'Android', url: '/tai/HTBB-0.9.4-opentest-android.apk', kieu: 'tep', phienBan: '0.9.4 (Open test)', nhatKy: 'Bản 0.9.4.', sha256: '79b4c2407fbe3032dae07ca1ba7222d4b221d2267902a4f24adde53ebea88ec6', trangThai: 'san-sang', hien: true },
  { ma: 'pc-java', ten: 'PC Windows (bản Java)', moTa: 'Bộ cài Windows có sẵn Java đi kèm', url: '/tai/HTBB-PC-Setup-0.9.4.exe', kieu: 'tep', phienBan: '0.9.4 (Open test)', capNhat: '2026-10-19', nhatKy: 'Bản 0.9.4.\\nKhông muốn cài đặt? Tải bản nén chạy ngay: /tai/HTBB-PC-Windows-0.9.4.zip', sha256: 'bee49ae5c503cc1f5718825bbdea75bb34eacc9a313186c94906662f73374c9c', trangThai: 'san-sang', hien: true },
  { ma: 'pc-jar', ten: 'PC Java (.jar)', url: '/tai/HTBB-PC-0.9.4.jar', kieu: 'tep', phienBan: '0.9.4 (Open test)', capNhat: '2026-10-19', nhatKy: 'Bản 0.9.4.\\nLinux có Java đi kèm: /tai/HTBB-PC-Linux-0.9.4.tar.gz', trangThai: 'san-sang', hien: true },
  { ma: 'jar', ten: 'Điện thoại Java (J2ME)', url: '/tai/HTBB-0.9.4-opentest-thuong.jar', kieu: 'tep', nhatKy: 'Bản 0.9.4.\\nMáy yếu dùng bản nhẹ (~2 MB): /tai/HTBB-0.9.4-opentest-nhe.jar', trangThai: 'san-sang', hien: true },
  { ma: 'pc', ten: 'PC Windows', url: '', kieu: 'tep', nhatKy: 'Đang hoàn thiện', trangThai: 'sap-ra-mat', hien: true },
  { ma: 'ios', ten: 'iOS', url: '', kieu: 'testflight', trangThai: 'sap-ra-mat', hien: true },
]

test('đổi mã cũ: pc-java -> windows, bỏ pc (Unity) và pc-jar, Linux lấy từ tệp phụ, xếp theo danh mục', () => {
  const ds = chuanHoaBanTai(CMS_094_CU)
  assert.deepEqual(ds.map(b => b.ma), ['jar', 'windows', 'linux', 'android', 'ios'])
  const w = ds.find(b => b.ma === 'windows')!
  assert.equal(w.url, '/tai/HTBB-PC-Setup-0.9.4.exe')
  assert.equal(w.ten, 'Windows')
  assert.equal(w.sha256, 'bee49ae5c503cc1f5718825bbdea75bb34eacc9a313186c94906662f73374c9c')
  assert.equal(w.trangThai, 'san-sang')
  assert.equal(w.nhatKy, 'Bản 0.9.4.') // dòng tệp phụ .zip bị bỏ
  const l = ds.find(b => b.ma === 'linux')!
  assert.equal(l.url, '/tai/HTBB-PC-Linux-0.9.4.tar.gz')
  assert.equal(l.trangThai, 'san-sang')
  assert.equal(l.phienBan, '0.9.4 (Open test)')
  const j = ds.find(b => b.ma === 'jar')!
  assert.equal(j.url, '/tai/HTBB-0.9.4-opentest-thuong.jar')
  assert.equal(j.nhatKy, 'Bản 0.9.4.') // bỏ jar nhẹ
  // Không còn link tệp phụ nào ở bất kỳ thẻ nào; mọi thẻ hợp lệ để lưu lại — trừ link Linux .tar.gz cũ:
  // từ 0.9.5 bản Linux chỉ là .AppImage, admin phải đổi sang tệp mới trước khi lưu.
  for (const b of ds) {
    assert.ok(!/\/tai\//.test(b.nhatKy), b.ma)
    if (b.ma === 'linux') assert.match(loiBanTai(b)!, /AppImage/)
    else assert.equal(loiBanTai(b), null, b.ma)
  }
  // Chuẩn hoá lần hai không đổi gì (dữ liệu mới giữ nguyên thứ tự admin đã xếp).
  assert.deepEqual(chuanHoaBanTai(JSON.parse(JSON.stringify(ds))), ds)
})

test('đổi mã cũ: tệp sai loại (pc-java là .zip) -> bỏ link, thẻ Windows chờ tệp .exe', () => {
  const ds = chuanHoaBanTai([{ ma: 'pc-java', url: '/tai/HTBB-PC-Windows-0.9.4.zip', trangThai: 'san-sang', hien: true }])
  const w = ds.find(b => b.ma === 'windows')!
  assert.equal(w.url, '')
  assert.equal(banTaiSanSang(w), false)
  assert.equal(ds.length, 5)
})

test('mỗi nền tảng một loại tệp', () => {
  const w = banTaiMacDinh(nenTang('windows')!)
  assert.equal(loiBanTai({ ...w, url: '/tai/HTBB-PC-Setup-0.9.5.exe' }), null)
  assert.match(loiBanTai({ ...w, url: '/tai/HTBB-PC-Windows-0.9.5.zip' })!, /\.exe/)
  const l = banTaiMacDinh(nenTang('linux')!)
  assert.equal(loiBanTai({ ...l, url: '/tai/HTBB-0.9.5-x86_64.AppImage' }), null)
  // Từ 0.9.5 bản Linux chỉ là .AppImage: link .tar.gz bị từ chối khi lưu.
  assert.match(loiBanTai({ ...l, url: 'https://cdn.vn/HTBB-PC-Linux-0.9.5.tar.gz?x=1' })!, /AppImage/)
  assert.match(loiBanTai({ ...l, url: '/tai/HTBB-PC-0.9.5.jar' })!, /AppImage/)
  const j = banTaiMacDinh(nenTang('jar')!)
  assert.equal(loiBanTai({ ...j, url: '/tai/HTBB-0.9.5-opentest-thuong.jar' }), null)
  assert.match(loiBanTai({ ...j, url: '/tai/a.apk' })!, /\.jar/)
  const a = banTaiMacDinh(nenTang('android')!)
  assert.match(loiBanTai({ ...a, url: '/tai/a.jar' })!, /\.apk/)
})

test('bỏ dòng tệp phụ trong "Có gì mới"', () => {
  assert.equal(boDongTepPhu('A\nB: /tai/x.zip\nC'), 'A\nC')
  assert.equal(boDongTepPhu('A\\nB: /tai/x.zip'), 'A')
  assert.equal(boDongTepPhu('Không có link'), 'Không có link')
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
  assert.equal(nenTangTheoUa('Mozilla/5.0 (Windows NT 10.0; Win64; x64)'), 'windows')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0)'), null) // chưa có bản macOS
  assert.equal(nenTangTheoUa('Mozilla/5.0 (X11; Linux x86_64)'), 'linux')
  assert.equal(nenTangTheoUa('Mozilla/5.0 (X11; CrOS x86_64 14541.0.0)'), null)
  assert.equal(nenTangTheoUa('Nokia6300/2.0 Profile/MIDP-2.0'), 'jar')
})

test('nhận diện iOS (kể cả iPad ở chế độ máy tính)', () => {
  const ipadDesktop = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15'
  assert.equal(laThietBiIos('Mozilla/5.0 (iPhone; CPU iPhone OS 15_8 like Mac OS X) AppleWebKit/605.1.15 Mobile/15E148'), true)
  assert.equal(laThietBiIos('Mozilla/5.0 (iPad; CPU OS 16_7 like Mac OS X) AppleWebKit/605.1.15'), true)
  assert.equal(laThietBiIos(ipadDesktop, 5), true)
  assert.equal(laThietBiIos(ipadDesktop, 0), false) // Mac thật
  assert.equal(laThietBiIos('Mozilla/5.0 (Linux; Android 13; SM-A505F)', 5), false)
  assert.equal(laThietBiIos('Mozilla/5.0 (Windows NT 10.0; Win64; x64)'), false)
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

test('số bản (BAN) từng nền tảng: máy chủ game đọc làm bản mới nhất', () => {
  const ds = chuanHoaBanTai([
    { ma: 'android', url: '/tai/HTBB-907.apk', ban: 907, phienBan: '0.9.7', trangThai: 'san-sang', hien: true },
    { ma: 'jar', url: '/tai/HTBB.jar', ban: '908', trangThai: 'san-sang', hien: true },
    { ma: 'windows', url: '/tai/a.exe', ban: -3 },
    { ma: 'linux', url: '/tai/a.AppImage', ban: 9.5 },
  ])
  const b = (ma: string) => ds.find(x => x.ma === ma)!
  assert.equal(b('android').ban, 907)
  assert.equal(b('jar').ban, 908, 'chuỗi số được nhận')
  assert.equal(b('windows').ban, 0, 'số âm -> 0')
  assert.equal(b('linux').ban, 0, 'số lẻ -> 0')
  assert.equal(b('ios').ban, 0, 'thiếu -> 0')
  assert.equal(banTaiMacDinh(nenTang('jar')!).ban, 0)
  // dữ liệu cũ đổi mã (pc-java -> windows) không mang số bản cũ sang
  assert.equal(chuanHoaBanTai([{ ma: 'pc-java', url: '/tai/a.exe', ban: 905 }]).find(x => x.ma === 'windows')!.ban, 0)
  assert.equal(soBanHopLe(' 907 '), 907)
  assert.equal(soBanHopLe('9a'), 0)
  assert.equal(soBanHopLe(1e9), 0)
  assert.equal(loiBanTai({ ...b('android'), ban: 1.5 }) !== null, true, 'lưu số lẻ bị từ chối')
  assert.equal(loiBanTai(b('android')), null)
})
