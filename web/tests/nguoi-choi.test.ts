/**
 * Trung tâm người chơi — phần thuần (server/utils/hoSoNguoiChoi.ts): giải mã hồ sơ game, vị trí an toàn,
 * quyết định online, kiểm dữ liệu vào, mã quà riêng. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  hsAnEmail, hsAnMa, hsBacPvp, hsCapKyNang, hsChuanHoaTim, hsDatViTriAnToan, hsDocViTri, hsExpLenCapKyNang,
  hsGiaiMaLauDai, hsGiaiMaSinhThai, hsGiongIp, hsGiongMaMay, hsLike, hsNoiDungThuQua, hsPhanTramCap, hsQuyetDinhOnline,
  hsSoGio, hsTachNhiemVuCu, hsTaoMaQuaRieng, HS_MAP_AN_TOAN,
} from '../server/utils/hoSoNguoiChoi.ts'
import { hsTenBanDo } from '../server/utils/tenBanDoNguoiChoi.ts'

// pInfo thật lấy từ CSDL thử (tai003): [8]=2 (map), [9]=248, [10]=584, [39]=1 (vùng)
const PINFO = '0,1,tai003,1,798976,25,1200,200,2,248,584,0,0,0,0,0,1,0,1,1,-5,0,0,-1,0,1,0,1,1,2,0,0,260917,1,1,1,0,15503,64,1,0,0,0,20'

test('vị trí: đọc pInfo', () => {
  assert.deepEqual(hsDocViTri(PINFO), { map: 2, x: 248, y: 584, quocGia: 1 })
  assert.equal(hsDocViTri('1,2,3'), null)
  assert.equal(hsDocViTri(null), null)
  assert.equal(hsDocViTri('0,1,a,1,1,1,1,1,x,1,1'), null)
})

test('vị trí: đưa về điểm an toàn chỉ đổi đúng 3 trường', () => {
  const moi = hsDatViTriAnToan(PINFO)!
  const a = PINFO.split(','), b = moi.split(',')
  assert.equal(a.length, b.length)
  assert.equal(b[8], String(HS_MAP_AN_TOAN))
  assert.equal(b[9], '0')
  assert.equal(b[10], '0')
  for (let i = 0; i < a.length; i++) if (i < 8 || i > 10) assert.equal(b[i], a[i], `trường ${i}`)
  assert.equal(hsDatViTriAnToan('hỏng'), null)
})

test('hồ sơ lâu dài: giải mã như HoSoLauDai.giaiMa', () => {
  const h = hsGiaiMaLauDai('nn=20261018;nm=7968;np=180;vc=4;vt=2962;sd=1;pe=1250;pw=3;pl=1;d1=1;d2=1;nc=1;nq=1;gh=5;t12=2;t13=0;rac;x=abc')
  assert.equal(h.nangLuong.ngay, 20261018)
  assert.equal(h.nangLuong.msDaDung, 7968)
  assert.equal(h.veChuong, 4)
  assert.equal(h.pvp.elo, 1250)
  assert.equal(h.pvp.thang, 3)
  assert.equal(h.gym.soHuyHieu, 2)
  assert.equal(h.soMonDangNangCap, 1)
  assert.equal(h.nhiemVu.daChuyenChuoiMoi, true)
  // mặc định khi thiếu khoá
  const r = hsGiaiMaLauDai('')
  assert.equal(r.pvp.elo, 1000)
  assert.equal(r.nangLuong.phut100, 180)
  assert.equal(r.veChuong, 0)
})

test('bậc PvP theo Elo', () => {
  assert.equal(hsBacPvp(1000), 'Poké Ball')
  assert.equal(hsBacPvp(1200), 'Great Ball')
  assert.equal(hsBacPvp(1599), 'Ultra Ball')
  assert.equal(hsBacPvp(1600), 'Master Ball')
})

test('kỹ năng sống: cấp từ EXP như KyNangSong', () => {
  assert.equal(hsExpLenCapKyNang(1), 44)
  assert.equal(hsCapKyNang(0), 1)
  assert.equal(hsCapKyNang(43), 1)
  assert.equal(hsCapKyNang(44), 2)
  assert.equal(hsCapKyNang(1e12), 50)
})

test('hồ sơ hệ sinh thái: giải mã chuỗi v1', () => {
  const s = hsGiaiMaSinhThai('v1|ng=20261018|tl=150|kn=44,0,100,0,5|tui=11:3,12:2|tr=1:1:2:3:0:10,4:5:5:5:1:0|nt=25,133,40,0,0,0|gi=1:3.4.5|nc=25:3,1:1|al=a~b~c|vx=5|dem=0,0,0,0,0|bf=0,0|tt=|mg=1,0,0,77|dh=0~3~99~0')
  assert.equal(s.hopLe, true)
  assert.equal(s.theLuc, 150)
  assert.deepEqual(s.kyNang.map(k => k.cap), [2, 1, 3, 1, 1]) // 100 EXP = 44 + 49 -> cấp 3
  assert.equal(s.tongVatLieu, 5)
  assert.equal(s.soLoaiVatLieu, 2)
  assert.equal(s.soTrung, 2)
  assert.equal(s.nhaTre.pet2, 133)
  assert.equal(s.soLoaiNghienCuu, 2)
  assert.equal(s.soAnhAlbum, 3)
  assert.equal(s.soVungHoanThanh, 2)
  assert.equal(s.thanThien, 77)
  assert.deepEqual(s.danhHieu, ['Nhiếp ảnh tập sự', 'Huyền thoại ống kính'])
  const rong = hsGiaiMaSinhThai('v0|ng=1')
  assert.equal(rong.hopLe, false)
  assert.equal(rong.theLuc, 200)
})

test('nhiệm vụ cũ: tách isfinish', () => {
  assert.deepEqual(hsTachNhiemVuCu('120,0,0'), [{ id: 120, trangThai: '0', chinh: true }])
  assert.deepEqual(hsTachNhiemVuCu('0,0,0'), [])
  assert.deepEqual(hsTachNhiemVuCu('5,1,1|hong|7,0,0'), [{ id: 5, trangThai: '1', chinh: false }, { id: 7, trangThai: '0', chinh: true }])
  assert.deepEqual(hsTachNhiemVuCu(null), [])
})

test('% vào cấp', () => {
  const bang = [0, 200, 800, 2000]
  assert.equal(hsPhanTramCap(100, 1, bang), 50)
  assert.equal(hsPhanTramCap('800', 3, bang), 0)
  assert.equal(hsPhanTramCap(1999, 3, bang), 99.92)
  assert.equal(hsPhanTramCap(10, 4, bang), null)
  assert.equal(hsPhanTramCap('x', 1, bang), null)
})

test('online: quyết định theo nhịp tim + vị trí', () => {
  const co = (t: Partial<Parameters<typeof hsQuyetDinhOnline>[0]>) =>
    hsQuyetDinhOnline({ giayNhipTim: null, soOnlineNhipTim: null, giayViTri: null, coTrongViTri: false, ...t }).online
  assert.equal(co({ giayViTri: 20, coTrongViTri: true }), true)
  assert.equal(co({ giayViTri: 20, coTrongViTri: false }), false)
  assert.equal(co({ giayNhipTim: 30, soOnlineNhipTim: 0 }), false) // máy chủ chạy, không ai online (bảng vị trí rỗng)
  assert.equal(co({ giayNhipTim: 30, soOnlineNhipTim: 5 }), null) // có người online nhưng vị trí chưa có -> không chắc
  assert.equal(co({ giayNhipTim: 30, soOnlineNhipTim: 5, giayViTri: 500, coTrongViTri: false }), null)
  assert.equal(co({ giayNhipTim: 3600, soOnlineNhipTim: 5, giayViTri: 3600, coTrongViTri: true }), false) // máy chủ tắt
  assert.equal(co({}), null)
})

test('dữ liệu vào: tìm, số giờ, LIKE', () => {
  assert.deepEqual(hsChuanHoaTim('  abc ', 'ip'), { q: 'abc', kieu: 'ip' })
  assert.equal(hsChuanHoaTim('x', 'DROP').kieu, 'tat_ca')
  assert.equal(hsChuanHoaTim(['a'], 'email').q, '')
  assert.equal(hsChuanHoaTim('a'.repeat(100), 'tat_ca').q.length, 64)
  assert.equal(hsGiongIp('192.168.'), true)
  assert.equal(hsGiongIp('10.0.0.1'), true)
  assert.equal(hsGiongIp('hoahv'), false)
  assert.equal(hsGiongIp('2001:db8'), true)
  assert.equal(hsSoGio(24, 100), 24)
  assert.equal(hsSoGio('48', 100), 48)
  assert.equal(hsSoGio(-1, 100), null)
  assert.equal(hsSoGio(1.5, 100), null)
  assert.equal(hsSoGio('1e3', 2000), null)
  assert.equal(hsSoGio(101, 100), null)
  assert.equal(hsLike('a%b_c!'), '%a!%b!_c!!%')
  assert.equal(hsLike('10.0', 'dau'), '10.0%')
})

test('che email / mã', () => {
  assert.equal(hsAnEmail('hoahv0903@gmail.com'), 'ho•••••03@gmail.com')
  assert.equal(hsAnEmail('ab@x.vn'), 'a•••@x.vn')
  assert.equal(hsAnEmail(''), '')
  assert.equal(hsAnEmail('khongphaiemail'), '•••')
  assert.equal(hsAnMa('qrabcdefghijklmn'), 'qrabcd••••••')
})

test('mã quà riêng: đúng định dạng giftcode an toàn, không trùng', () => {
  const ds = new Set<string>()
  for (let i = 0; i < 200; i++) {
    const b = new Uint8Array(14)
    globalThis.crypto.getRandomValues(b)
    const ma = hsTaoMaQuaRieng(b)
    assert.match(ma, /^qr[a-z0-9]{14}$/)
    ds.add(ma)
  }
  assert.equal(ds.size, 200)
})

test('thư báo quà ≤ 800 ký tự và luôn có mã', () => {
  const s = hsNoiDungThuQua(['10 lượng khoá'], 'qrabc', '2026-11-01 00:00:00', 'Chúc mừng')
  assert.ok(s.includes('qrabc') && s.includes('Chúc mừng'))
  const dai = hsNoiDungThuQua(Array(200).fill('1 × Thuốc hồi máu cực mạnh (khoá)'), 'qrxyz', '2026-11-01 00:00:00', 'x'.repeat(200))
  assert.ok(dai.length <= 800)
  assert.ok(dai.includes('qrxyz'))
})

test('tên bản đồ', () => {
  assert.equal(hsTenBanDo(0), 'Pewter')
  assert.equal(hsTenBanDo(105), 'Sân nhà khởi đầu')
  assert.equal(hsTenBanDo(301), 'Bản đồ #301')
})

test('tìm "Tất cả" nhận ra mã máy client HTBB (j2me-…), bỏ chuỗi quá ngắn / ký tự lạ', () => {
  assert.equal(hsGiongMaMay('j2me-abcdefghijklmnopqrstuvwx'), true)
  assert.equal(hsGiongMaMay('abcdef0123456789'), true)
  assert.equal(hsGiongMaMay('abc'), false)
  assert.equal(hsGiongMaMay('1.2.3.4'), false)
  assert.equal(hsGiongMaMay('có dấu cách'), false)
  assert.equal(hsGiongMaMay('x'.repeat(65)), false)
})
