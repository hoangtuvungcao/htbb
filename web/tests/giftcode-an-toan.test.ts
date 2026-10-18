/**
 * Giftcode an toàn — danh sách trắng vật phẩm. Bộ ca `thuNghiem` trong shared/data/giftcode-an-toan.json (sinh bởi
 * HTBB/tools/pokemon/sinh_giftcode_an_toan.py); CHÍNH bộ đó chạy bên Java (HTBB/test/KiemItemMoCoi.java) nên hai bản
 * kiemVatPham không lệch được. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { kiemVatPhamGiftcode, type DuLieuGiftcodeAnToan } from '../shared/giftcode/kiem-vat-pham.ts'

const d = JSON.parse(readFileSync(fileURLToPath(new URL('../shared/data/giftcode-an-toan.json', import.meta.url)), 'utf8')) as
  DuLieuGiftcodeAnToan & { thuNghiem: { thuong: string, cap: number, capItem: Record<string, number>, ok: boolean }[] }

test('giftcode an toàn: bộ ca chung', () => {
  assert.ok(d.thuNghiem.length >= 10)
  for (const t of d.thuNghiem) {
    const loi = kiemVatPhamGiftcode(d, t.thuong, t.cap, id => t.capItem[String(id)] ?? (d.trangBi.includes(id) ? -1 : 1))
    assert.equal(loi === null, t.ok, `${t.thuong} (cấp ${t.cap}): ${loi}`)
  }
})

test('giftcode an toàn: danh sách đã sắp xếp (bản Java dùng tìm nhị phân)', () => {
  assert.deepEqual([...d.trangBi].sort((a, b) => a - b), d.trangBi)
  assert.deepEqual([...d.ngocCam].sort((a, b) => a - b), d.ngocCam)
})
