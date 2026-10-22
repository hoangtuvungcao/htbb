/** Vùng hoang dã xoay vòng (loai-bo-sung): cùng công thức chu kỳ với máy chủ (LoaiDuLieu.chuKy), xác định theo ngày. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { cacTuanToi, chuKyXoay, homNayMayChu, ngayEpoch, ngayTuEpoch, type LichVungHoangDa } from '../shared/utils/vungHoangDa.ts'

const goc = ngayEpoch('2026-10-26')

test('ngày gốc (thứ Hai) là chu kỳ 0, cả tuần giữ nguyên', () => {
  for (let i = 0; i < 7; i++) assert.equal(chuKyXoay(goc + i, goc, 6), 0)
  assert.equal(chuKyXoay(goc + 7, goc, 6), 1)
  assert.equal(chuKyXoay(goc + 6 * 7, goc, 6), 0)
})

test('trước ngày gốc vẫn xác định (floorMod)', () => {
  assert.equal(chuKyXoay(goc - 1, goc, 6), 5)
  assert.equal(chuKyXoay(goc - 7, goc, 6), 5)
  assert.equal(chuKyXoay(goc - 8, goc, 6), 4)
})

test('không có lịch -> -1', () => {
  assert.equal(chuKyXoay(goc, goc, 0), -1)
})

test('giờ máy chủ UTC+7: 17:00 UTC Chủ nhật đã là thứ Hai ở máy chủ', () => {
  const bay = Date.UTC(2026, 9, 25, 17, 30)
  assert.equal(homNayMayChu(bay), goc)
})

test('tuần này + 2 tuần tới bắt đầu từ thứ Hai', () => {
  const lich: LichVungHoangDa = { ngayGoc: '2026-10-26', tuan: [0, 1, 2, 3, 4, 5].map(i => ({ chuKy: i, ten: `T${i}`, moTa: '', theHe: 4 + i })), banDo: [], soLoai: {} }
  const t = cacTuanToi(lich, goc + 10, 3)
  assert.deepEqual(t.map(x => x.chuKy), [1, 2, 3])
  assert.equal(ngayTuEpoch(t[0]!.batDau), '02/11')
})
