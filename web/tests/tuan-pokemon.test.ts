/** "Tuần Pokémon" trên web phải trùng tuần máy chủ đang chạy (LichNam: tuần ISO, đổi tuần 04:00 thứ Hai giờ VN). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { tuanTrongNam } from '../shared/utils/danhMucCamNang.ts'

// Giờ Việt Nam -> Date (UTC+7)
const vn = (s: string) => new Date(`${s}+07:00`)

test('tuần ISO theo giờ Việt Nam', () => {
  assert.equal(tuanTrongNam(vn('2026-10-19T12:00:00')), 43) // thứ Hai 19/10/2026 = tuần ISO 43
  assert.equal(tuanTrongNam(vn('2026-10-25T23:00:00')), 43) // Chủ nhật cùng tuần
  assert.equal(tuanTrongNam(vn('2026-01-01T12:00:00')), 1) // thứ Năm 1/1/2026 -> tuần 1
})

test('đổi tuần lúc 04:00 thứ Hai, không phải nửa đêm', () => {
  assert.equal(tuanTrongNam(vn('2026-10-26T03:59:00')), 43)
  assert.equal(tuanTrongNam(vn('2026-10-26T04:00:00')), 44)
})

test('tuần ISO 53 và đầu năm thuộc năm trước gộp vào tuần 52', () => {
  assert.equal(tuanTrongNam(vn('2026-12-31T12:00:00')), 52) // 2026 có tuần ISO 53
  assert.equal(tuanTrongNam(vn('2027-01-02T12:00:00')), 52) // vẫn thuộc tuần 53 của 2026
  assert.equal(tuanTrongNam(vn('2027-01-04T05:00:00')), 1)
})
