/**
 * Chế độ DEMO nạp tiền / Thành viên (HTBB_NAP_DEMO=1) — luật thuần ở server/utils/napDemoThuan.ts.
 * Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  DEMO_DON_MOI_TK, DEMO_SO_TK_MAC_DINH, DEMO_TEN_TK_MAC_DINH, GOI_DEMO,
  demoDuocXem, demoXetDuongDan, laMaDonDemo, laSoTaiKhoanDemo, laSoTaiKhoanThat, quyetDinhCheDoNap, taoKhoDemo, taoMaDonDemo,
} from '../server/utils/napDemoThuan.ts'
import { kiemCauHinhNapThat, kiemGoi, timMaDon } from '../server/utils/napTienThuan.ts'

const CAU_HINH_THAT = {
  secret: 'a1b2c3d4e5f60718293a4b5c6d7e8f90a1b2c3d4e5f60718',
  adminNaptienPassword: 'Qz8!mK2pLx7wR4',
  accountNo: '19038475620',
  accountName: 'NGUYEN VAN B',
  bankBin: '970422',
  sepayApiKey: 'f3a9c1e7b5d2084f6a1c3e5b7d9f0a2c',
  sepayApiToken: '',
  sandbox: false,
}

function cheDo(p: Partial<Parameters<typeof quyetDinhCheDoNap>[0]> & { accountNo?: string }) {
  const accountNo = p.accountNo ?? '0123456xxx'
  const accountName = p.accountName ?? 'DEMO HTBB'
  return quyetDinhCheDoNap({
    yeuCauBat: false,
    yeuCauDemo: true,
    dev: false,
    loiThat: kiemCauHinhNapThat({ ...CAU_HINH_THAT, accountNo, accountName }),
    accountNo,
    accountName,
    ...p,
  })
}

// ---------------------------------------------------------------- số tài khoản minh hoạ

test('số TK minh hoạ có chữ x: chỉ hợp lệ cho demo, cấu hình thật luôn từ chối', () => {
  assert.equal(laSoTaiKhoanDemo('0123456xxx'), true)
  assert.equal(laSoTaiKhoanDemo('xxxxxxxx'), true)
  assert.equal(laSoTaiKhoanDemo('0123456789'), false)
  assert.equal(laSoTaiKhoanDemo('12x'), false)
  assert.equal(laSoTaiKhoanDemo('0123 456xxx'), false)
  assert.equal(laSoTaiKhoanThat('0123456789'), true)
  assert.equal(laSoTaiKhoanThat('0000000000'), false)
  assert.equal(laSoTaiKhoanThat('0123456xxx'), false)
  // Cấu hình THẬT không bao giờ nhận số có chữ x -> nạp thật không bật được với số minh hoạ.
  const loi = kiemCauHinhNapThat({ ...CAU_HINH_THAT, accountNo: '0123456xxx' })
  assert.ok(loi.some(l => l.includes('HTBB_NAP_ACCOUNT_NO')))
  assert.deepEqual(kiemCauHinhNapThat(CAU_HINH_THAT), [])
})

test('demo bật với số minh hoạ: nạp thật TẮT, cổng không an toàn để thu tiền', () => {
  const c = cheDo({})
  assert.equal(c.demo, true)
  assert.equal(c.bat, false)
  assert.equal(c.congAnToan, false)
  assert.equal(c.soTkDemo, '0123456xxx')
  assert.equal(c.tenTkDemo, 'DEMO HTBB')
  assert.deepEqual(c.lyDoTuChoiDemo, [])
})

test('demo chưa đặt số TK / tên: dùng số và tên minh hoạ mặc định', () => {
  const c = cheDo({ accountNo: '0000000000', accountName: 'NGUYEN VAN A' })
  assert.equal(c.demo, true)
  assert.equal(c.soTkDemo, DEMO_SO_TK_MAC_DINH)
  assert.equal(c.tenTkDemo, DEMO_TEN_TK_MAC_DINH)
})

test('demo KHÔNG nhận số tài khoản thật (dù cấu hình thật chưa đủ)', () => {
  const c = cheDo({ accountNo: '19038475620', loiThat: ['HTBB_SEPAY_WEBHOOK_APIKEY trống/yếu'] })
  assert.equal(c.demo, false)
  assert.ok(c.lyDoTuChoiDemo.some(l => l.includes('số tài khoản thật')))
  assert.ok(c.log.some(([m, d]) => m === 'error' && d.includes('HTBB_NAP_DEMO=1 BI TU CHOI')))
})

// ---------------------------------------------------------------- loại trừ với nạp thật

test('nạp thật hợp lệ + HTBB_NAP_DEMO=1: từ chối demo, ghi log, giữ chế độ THẬT', () => {
  const c = quyetDinhCheDoNap({
    yeuCauBat: true, yeuCauDemo: true, dev: false, loiThat: kiemCauHinhNapThat(CAU_HINH_THAT),
    accountNo: CAU_HINH_THAT.accountNo, accountName: CAU_HINH_THAT.accountName,
  })
  assert.equal(c.bat, true)
  assert.equal(c.demo, false)
  assert.equal(c.congAnToan, true)
  assert.ok(c.lyDoTuChoiDemo.length > 0)
  assert.ok(c.log.some(([m, d]) => m === 'error' && d.includes('nap THAT')))
})

test('cấu hình thật hợp lệ (dù HTBB_NAP_BAT=false) cũng thắng demo — Thành viên thật có thể đang bán', () => {
  const c = quyetDinhCheDoNap({
    yeuCauBat: false, yeuCauDemo: true, dev: false, loiThat: [], accountNo: CAU_HINH_THAT.accountNo, accountName: 'X',
  })
  assert.equal(c.demo, false)
  assert.equal(c.congAnToan, true)
})

test('máy dev bật nạp thật + demo: thật thắng', () => {
  const c = cheDo({ yeuCauBat: true, dev: true })
  assert.equal(c.bat, true)
  assert.equal(c.demo, false)
})

test('HTBB_NAP_BAT=true nhưng cấu hình thật hỏng + demo: thật fail closed, demo chạy', () => {
  const c = cheDo({ yeuCauBat: true })
  assert.equal(c.bat, false)
  assert.equal(c.demo, true)
  assert.ok(c.lyDoTat.length > 0)
})

test('không yêu cầu demo: giữ nguyên luật fail closed cũ', () => {
  const tat = quyetDinhCheDoNap({ yeuCauBat: true, yeuCauDemo: false, dev: false, loiThat: ['x'], accountNo: '', accountName: '' })
  assert.deepEqual([tat.bat, tat.demo, tat.congAnToan, tat.lyDoTat], [false, false, false, ['x']])
  const dev = quyetDinhCheDoNap({ yeuCauBat: true, yeuCauDemo: false, dev: true, loiThat: ['x'], accountNo: '', accountName: '' })
  assert.deepEqual([dev.bat, dev.demo, dev.congAnToan, dev.lyDoTat], [true, false, true, []])
  const thuong = quyetDinhCheDoNap({ yeuCauBat: false, yeuCauDemo: false, dev: false, loiThat: [], accountNo: '', accountName: '' })
  assert.deepEqual([thuong.bat, thuong.demo, thuong.congAnToan], [false, false, true])
})

// ---------------------------------------------------------------- ai được xem

test('HTBB_NAP_DEMO_CHI_ADMIN=1: chỉ tài khoản trong HTBB_ADMIN_ACCOUNTS (đã đăng nhập)', () => {
  const admins = ['chuserver', 'hoahv']
  assert.equal(demoDuocXem(true, true, 'HoaHV', admins), true)
  assert.equal(demoDuocXem(true, true, 'nguoichoi', admins), false)
  assert.equal(demoDuocXem(true, true, null, admins), false)
  assert.equal(demoDuocXem(true, true, '', admins), false)
  assert.equal(demoDuocXem(true, true, 'hoahv', []), false)
})

test('HTBB_NAP_DEMO_CHI_ADMIN=0: mọi người thấy demo; demo tắt: không ai thấy', () => {
  assert.equal(demoDuocXem(true, false, null, []), true)
  assert.equal(demoDuocXem(true, false, 'nguoichoi', ['hoahv']), true)
  assert.equal(demoDuocXem(false, false, 'hoahv', ['hoahv']), false)
  assert.equal(demoDuocXem(false, true, 'hoahv', ['hoahv']), false)
})

// ---------------------------------------------------------------- đường dẫn (server/middleware/tat-nap.ts)

test('đang demo: mọi webhook bị từ chối, kể cả người được xem; xác nhận tay bị từ chối', () => {
  for (const p of ['/api/nap/webhook-sepay', '/api/nap/webhook-casso', '/api/nap/webhook-mock', '/api/nap/Webhook-SePay?x=1']) {
    assert.equal(demoXetDuongDan(p, true, true), 'tu_choi_webhook', p)
    assert.equal(demoXetDuongDan(p, true, false), 'tu_choi_webhook', p)
  }
  assert.equal(demoXetDuongDan('/api/xac-nhan-nap/xac-nhan', true, true), 'tu_choi_xac_nhan')
  assert.equal(demoXetDuongDan('/api/xac-nhan-nap/login', true, true), 'tu_choi_xac_nhan')
})

test('đang demo: API nạp khác chỉ mở cho người được xem', () => {
  assert.equal(demoXetDuongDan('/api/nap/tao-don', true, true), 'demo')
  assert.equal(demoXetDuongDan('/api/nap/don?ma_don=DMABCDEFGH', true, true), 'demo')
  assert.equal(demoXetDuongDan('/api/nap/tao-don', true, false), 'chan')
  assert.equal(demoXetDuongDan('/api/nap/goi', true, false), 'chan')
})

test('không demo / không phải API nạp: để luật cũ xử lý', () => {
  assert.equal(demoXetDuongDan('/api/nap/webhook-sepay', false, false), 'bo_qua')
  assert.equal(demoXetDuongDan('/api/thanh-vien/mua', true, true), 'bo_qua')
  assert.equal(demoXetDuongDan('/api/admin/naptien/xac-nhan', true, true), 'bo_qua')
})

// ---------------------------------------------------------------- đơn demo (bộ nhớ)

test('mã đơn demo không bao giờ khớp mã đơn thật (webhook / sao kê không xác nhận được)', () => {
  for (let i = 0; i < 500; i++) {
    const ma = taoMaDonDemo()
    assert.ok(laMaDonDemo(ma), ma)
    assert.equal(timMaDon(ma), null, ma)
    assert.equal(timMaDon(`chuyen khoan ${ma} nap`), null, ma)
  }
  assert.equal(laMaDonDemo('HTABCDEFGH'), false)
})

test('kho demo: tạo, idempotent, giả lập chỉ đổi hiển thị, hết hạn 24 giờ', () => {
  let bayGio = 1_800_000_000_000
  const kho = taoKhoDemo(() => bayGio)
  const u = { id: 7, username: 'hoahv' }
  const goi = GOI_DEMO[0]!
  const d = kho.tao(u, goi, 'Pika', 'khoa-idem-0001')
  assert.equal(d.demo, true)
  assert.equal(d.trang_thai, 'cho_thanh_toan')
  assert.equal(d.so_tien, goi.gia_vnd)
  assert.equal(kho.tao(u, goi, 'Pika', 'khoa-idem-0001').ma_don, d.ma_don)
  assert.notEqual(kho.tao(u, goi, 'Pika', null).ma_don, d.ma_don)
  assert.equal(kho.cuaToi(8).length, 0)
  const g = kho.giaLap(d.ma_don, 'admin1')!
  assert.equal(g.trang_thai, 'da_cong')
  assert.equal(g.gia_lap_boi, 'admin1')
  assert.equal(kho.giaLap('DMZZZZZZZZ', 'admin1'), null)
  bayGio += 25 * 3600_000
  assert.equal(kho.lay(d.ma_don), null)
})

test('kho demo: trần đơn mỗi tài khoản, ghi webhook bị từ chối', () => {
  const kho = taoKhoDemo()
  const u = { id: 1, username: 'a' }
  for (let i = 0; i < DEMO_DON_MOI_TK + 5; i++) kho.tao(u, GOI_DEMO[1]!, 'X', null)
  assert.equal(kho.cuaToi(1).length, DEMO_DON_MOI_TK)
  kho.ghiWebhook('/api/nap/webhook-sepay', '1.2.3.4')
  assert.equal(kho.webhook()[0]!.duongDan, '/api/nap/webhook-sepay')
})

test('gói minh hoạ đúng luật gói thật (tỷ giá cố định, không kèm lượng cho thẻ/vé)', () => {
  for (const g of GOI_DEMO) assert.equal(kiemGoi(g.loai, g.gia_vnd, g.luong), null, g.ma)
})
