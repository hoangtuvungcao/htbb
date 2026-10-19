/** Tiền tệ: số gọn (cùng quy ước client game.pokemon.TienTe.gon), nhận loại tiền từ nhãn, ảnh biểu tượng có đủ. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync } from 'node:fs'
import { soGon, loaiTienTuNhan, TIEN_TE, type LoaiTien } from '../shared/utils/tienTe.ts'

test('số gọn kiểu Việt Nam', () => {
  assert.equal(soGon(0), '0')
  assert.equal(soGon(999), '999')
  assert.equal(soGon(9999), '9.999')
  assert.equal(soGon(12500), '12,5 N')
  assert.equal(soGon(12000), '12 N')
  assert.equal(soGon(250000), '250 N')
  assert.equal(soGon(1250000), '1,25 Tr')
  assert.equal(soGon(1999999), '1,99 Tr')
  assert.equal(soGon(1050000), '1,05 Tr')
  assert.equal(soGon(250000000), '250 Tr')
  assert.equal(soGon(3200000000), '3,2 Tỉ')
  assert.equal(soGon(-12500), '-12,5 N')
})

test('nhận loại tiền từ nhãn cột', () => {
  assert.equal(loaiTienTuNhan('Xu'), 'xu')
  assert.equal(loaiTienTuNhan('Lượng'), 'luong')
  assert.equal(loaiTienTuNhan('Lượng khoá'), 'luong_khoa')
  assert.equal(loaiTienTuNhan('Lượng khóa'), 'luong_khoa')
  assert.equal(loaiTienTuNhan('Cấp'), null)
})

test('mỗi loại tiền có biểu tượng 1x / 2x', () => {
  for (const l of Object.keys(TIEN_TE) as LoaiTien[]) {
    for (const c of [32, 64]) assert.ok(existsSync(new URL(`../public/tien/${l}_${c}.png`, import.meta.url)), `${l}_${c}`)
  }
})
