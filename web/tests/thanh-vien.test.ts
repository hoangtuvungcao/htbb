/**
 * Thành viên HTBB — luật thuần phía web (server/utils/thanhVienThuan.ts) + loại gói 'thanh_vien' trong
 * danh mục nạp + quy tắc phân quyền. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  TV_CHUC_NANG, TV_CHUC_NANG_MAC_DINH, conHanTv, docCauHinhTv, hanMoiTv, hanSauThuHoiTv, kiemCauHinhTv, tachDanhSachTk,
} from '../server/utils/thanhVienThuan.ts'
import { kiemGoi, LOAI_GOI } from '../server/utils/napTienThuan.ts'
import { kiemQuyenAdmin } from '../shared/utils/quyenAdmin.ts'

test('mặc định TẮT, không bán, 20.000 đ, vĩnh viễn', () => {
  const c = docCauHinhTv({})
  assert.equal(c.bat, false)
  assert.equal(c.banBat, false)
  assert.equal(c.gia, 20000)
  assert.equal(c.thoiHanNgay, 0)
  assert.deepEqual(c.chucNang, TV_CHUC_NANG_MAC_DINH.split(','))
})

test('đọc cấu hình: mã lạ bị bỏ, số sai về mặc định', () => {
  const c = docCauHinhTv({ 'thanh_vien.bat': '1', 'thanh_vien.chuc_nang': 'giao_dich, hack;CHO', 'thanh_vien.gia': 'x', 'thanh_vien.mien_cap': '15' })
  assert.equal(c.bat, true)
  assert.deepEqual(c.chucNang, ['giao_dich', 'cho'])
  assert.equal(c.gia, 20000)
  assert.equal(c.mienCap, 15)
})

test('đủ 11 tính năng yêu cầu', () => {
  for (const m of ['giao_dich', 'chat_the_gioi', 'cho', 'cho_sinh_thai', 'chuyen_luong', 'bang', 'pvp_xep_hang', 'giftcode', 'qua_online', 'gui_thu', 'nhan_tang']) {
    assert.ok(TV_CHUC_NANG.some(c => c.ma === m), m)
  }
})

test('kiểm dữ liệu admin', () => {
  assert.equal(kiemCauHinhTv({ gia: 20000, thoiHanNgay: 0, mienCap: 0, chucNang: ['giao_dich'], thongBao: '' }), null)
  assert.ok(kiemCauHinhTv({ gia: 20500 }))
  assert.ok(kiemCauHinhTv({ gia: 5000 }))
  assert.ok(kiemCauHinhTv({ thoiHanNgay: -1 }))
  assert.ok(kiemCauHinhTv({ chucNang: ['hack'] }))
})

test('hạn: vĩnh viễn, gia hạn nối tiếp, thu hồi khi hoàn tiền', () => {
  const t = 1e12, d = 86400000
  assert.equal(conHanTv(0, t), true)
  assert.equal(conHanTv(-1, t), false)
  assert.equal(conHanTv(null, t), false)
  assert.equal(hanMoiTv(false, -1, t, 30), t + 30 * d)
  assert.equal(hanMoiTv(true, t + 10 * d, t, 30), t + 40 * d)
  assert.equal(hanMoiTv(true, 0, t, 30), 0)
  assert.equal(hanMoiTv(false, -1, t, 0), 0)
  assert.equal(hanSauThuHoiTv(t + 40 * d, t, 30), t + 10 * d)
  assert.equal(hanSauThuHoiTv(t + 10 * d, t, 30), -1)
  assert.equal(hanSauThuHoiTv(0, t, 0), -1)
  assert.equal(hanSauThuHoiTv(0, t, 30), 0)
})

test('danh sách tài khoản hàng loạt', () => {
  assert.deepEqual(tachDanhSachTk('Aa, bb\ncc;; aa  x  <script>'), ['aa', 'bb', 'cc'])
})

test('gói thanh_vien được danh mục nạp chấp nhận, không kèm lượng', () => {
  assert.ok((LOAI_GOI as readonly string[]).includes('thanh_vien'))
  assert.equal(kiemGoi('thanh_vien', 20000, 0), null)
  assert.ok(kiemGoi('thanh_vien', 20000, 200))
})

test('phân quyền: Hỗ trợ xem được, chỉ Siêu quản trị ghi, Chỉ xem / Biên tập không vào', () => {
  assert.equal(kiemQuyenAdmin('/api/admin/thanh-vien/danh-sach', 'GET', 'ho_tro'), true)
  assert.equal(kiemQuyenAdmin('/api/admin/thanh-vien/cap', 'POST', 'ho_tro'), false)
  assert.equal(kiemQuyenAdmin('/api/admin/thanh-vien/cau-hinh', 'POST', 'sieu_quan_tri'), true)
  assert.equal(kiemQuyenAdmin('/api/admin/thanh-vien/tong-quan', 'GET', 'xem'), false)
  assert.equal(kiemQuyenAdmin('/api/admin/thanh-vien/tong-quan', 'GET', 'bien_tap'), false)
})

test('kích hoạt: mặc định không chặn đăng nhập, khoá 6 tính năng, thành viên = đã kích hoạt', async () => {
  const { docCauHinhTv: doc, KH_CHUC_NANG_MAC_DINH } = await import('../server/utils/thanhVienThuan.ts')
  const c = doc({})
  assert.equal(c.khChanDangNhap, false)
  assert.deepEqual(c.khChucNang, KH_CHUC_NANG_MAC_DINH.split(','))
  assert.equal(c.khTvLaKh, true)
  const d = doc({ 'kich_hoat.chan_dang_nhap': '1', 'kich_hoat.chuc_nang': '', 'kich_hoat.thanh_vien_la_kich_hoat': '0' })
  assert.equal(d.khChanDangNhap, true)
  assert.deepEqual(d.khChucNang, [])
  assert.equal(d.khTvLaKh, false)
  assert.ok(kiemCauHinhTv({ khChucNang: ['hack'] }))
})
