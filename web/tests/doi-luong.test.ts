/** Đổi lượng trên /account: luật thuần (một chiều, chỉ lượng thường, trần lượng → xu), API chỉ chèn lệnh, không ghi tob_char. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { ngayVN, soNhan, loiDoi, giaTrungBinh, moTaLenhDoi, LOAI_DOI, LENH_DOI_LUONG_KHOA, LENH_DOI_LUONG_XU } from '../shared/utils/doiLuong.ts'

const ok = { luong: 100, tiLe: 1, tiGia: 120, conXu: 5000, dangCho: false }

test('ngày theo giờ Việt Nam (khớp máy chủ game)', () => {
  assert.equal(ngayVN(Date.parse('2026-10-31T17:30:00Z')), '2026-11-01')
  assert.equal(ngayVN(Date.parse('2026-10-31T16:59:00Z')), '2026-10-31')
})

test('số nhận: 1 lượng = tiLe lượng khoá, 1 lượng = tiGia xu', () => {
  assert.equal(soNhan('khoa', 10, 1, 120), 10)
  assert.equal(soNhan('xu', 10, 1, 120), 1200)
  assert.equal(soNhan('xu', 10, 1, 0), 0)
  assert.equal(soNhan('khoa', 0, 1, 120), 0)
})

test('kiểm yêu cầu: chỉ lượng thường, trần lượng → xu, một yêu cầu chờ mỗi Pokémon', () => {
  assert.equal(loiDoi('khoa', 10, ok), null)
  assert.equal(loiDoi('xu', 100, ok), null)
  assert.match(loiDoi('khoa', 101, ok)!, /Không đủ lượng thường/)
  assert.match(loiDoi('khoa', 101, ok)!, /Lượng khoá không dùng để đổi/)
  assert.match(loiDoi('xu', 50, { ...ok, conXu: 40 })!, /còn đổi được 40/)
  assert.match(loiDoi('xu', 5, { ...ok, tiGia: 0 })!, /Chưa có tỉ giá/)
  assert.match(loiDoi('khoa', 5, { ...ok, tiLe: 0 })!, /đang tắt/)
  assert.match(loiDoi('khoa', 5, { ...ok, dangCho: true })!, /đang có một yêu cầu/)
  assert.ok(loiDoi('khoa', 0, ok))
  assert.ok(loiDoi('khoa', -5, ok))
  assert.ok(loiDoi('khoa', 2_000_000, { ...ok, luong: 5_000_000 }))
})

test('giá trung bình + mô tả lệnh', () => {
  assert.equal(giaTrungBinh(12500, 100), 125)
  assert.equal(giaTrungBinh(0, 0), 0)
  assert.equal(moTaLenhDoi(LENH_DOI_LUONG_KHOA, 1500), '1.500 lượng → lượng khoá')
  assert.equal(moTaLenhDoi(LENH_DOI_LUONG_XU, 3), '3 lượng → xu')
  assert.deepEqual(LOAI_DOI, { khoa: 'doi_luong_khoa', xu: 'doi_luong_xu' })
})

test('API: chỉ chèn lệnh vào web_lenh_nhan_vat, không ghi số dư; huỷ chỉ dòng còn chờ của chính tài khoản', () => {
  const post = readFileSync(new URL('../server/api/doi-luong/index.post.ts', import.meta.url), 'utf8')
  const huy = readFileSync(new URL('../server/api/doi-luong/huy.post.ts', import.meta.url), 'utf8')
  const util = readFileSync(new URL('../server/utils/doiLuongDb.ts', import.meta.url), 'utf8')
  assert.ok(!/UPDATE\s+tob_char/i.test(post + util), 'web không được sửa tob_char')
  assert.ok(post.includes('guiLenhDoi(') && post.includes('requireUser(event)') && post.includes('userId = ? AND charname = ?'))
  assert.ok(util.includes('INSERT INTO web_lenh_nhan_vat'))
  assert.ok(huy.includes('c.userId = ?') && huy.includes('huyLenh('))
  const lenh = readFileSync(new URL('../server/utils/lenhNhanVat.ts', import.meta.url), 'utf8')
  assert.ok(lenh.includes('export const LENH_DANG_XU_LY = 6'), 'trạng thái game đã chốt')
  assert.ok(lenh.includes('WHERE id = ? AND trang_thai IN (?, ?)'), 'huỷ chỉ dòng CHỜ / CHỜ VÀO GAME')
})

test('trang /account có form đổi lượng, hiện tỉ giá chung + lượng khoá không lên sàn', () => {
  const vue = readFileSync(new URL('../app/components/tai-khoan/DoiLuong.vue', import.meta.url), 'utf8')
  const acc = readFileSync(new URL('../app/pages/account.vue', import.meta.url), 'utf8')
  assert.ok(acc.includes('<TaiKhoanDoiLuong'))
  assert.ok(vue.includes('áp dụng chung toàn máy chủ'))
  assert.ok(vue.includes('lượng khoá không bao giờ lên sàn'))
  assert.ok(vue.includes('/api/doi-luong'))
})
