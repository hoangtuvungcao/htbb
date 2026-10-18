/** Whitelist sửa lẻ cau_hinh + danh mục dữ liệu game (chỉ tên SQL an toàn). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { DANH_MUC_CAU_HINH, khoaSuaLeDuoc, kiemGiaTriKhoa } from '../shared/utils/cauHinhDanhMuc.ts'
import { DS_DU_LIEU_GAME, TAB_DU_LIEU_GAME, dungSqlBoDuLieu, laTenSqlAnToan, timBoDuLieu } from '../server/utils/duLieuGameDanhMuc.ts'

test('chỉ khoá trong whitelist được sửa lẻ', () => {
  assert.equal(khoaSuaLeDuoc('thue.cho'), true)
  assert.equal(khoaSuaLeDuoc('sv.adminAccounts'), false)
  assert.equal(khoaSuaLeDuoc('sv.tieuNguongMau'), false)
  assert.equal(khoaSuaLeDuoc('db.password'), false)
  assert.equal(khoaSuaLeDuoc('__proto__'), false)
  assert.equal(khoaSuaLeDuoc('toString'), false)
  for (const [k, m] of Object.entries(DANH_MUC_CAU_HINH)) {
    if (!m.suaLe) continue
    assert.ok(m.kieu === 'so' || m.kieu === 'co', `${k}: kiểu ${m.kieu} chưa hỗ trợ sửa lẻ`)
    if (m.kieu === 'so') assert.ok(m.min !== undefined && m.max !== undefined && m.min <= m.max, `${k}: thiếu khoảng`)
    assert.ok(!k.startsWith('db.') && !k.startsWith('lau_dai.'), k)
  }
})

test('kiểm giá trị', () => {
  assert.deepEqual(kiemGiaTriKhoa('thue.cho', '6'), { ok: true, giaTri: '6' })
  assert.deepEqual(kiemGiaTriKhoa('thue.cho', 10), { ok: true, giaTri: '10' })
  assert.equal(kiemGiaTriKhoa('thue.cho', '51').ok, false)
  assert.equal(kiemGiaTriKhoa('thue.cho', '-1').ok, false)
  assert.equal(kiemGiaTriKhoa('thue.cho', '6.5').ok, false)
  assert.equal(kiemGiaTriKhoa('thue.cho', '06').ok, false)
  assert.equal(kiemGiaTriKhoa('thue.cho', '').ok, false)
  assert.equal(kiemGiaTriKhoa('thue.cho', '1e1').ok, false)
  assert.deepEqual(kiemGiaTriKhoa('sv.testMode', true), { ok: true, giaTri: '1' })
  assert.equal(kiemGiaTriKhoa('sv.testMode', '2').ok, false)
  assert.equal(kiemGiaTriKhoa('sv.maxCharPerAccount', '4').ok, false)
  assert.equal(kiemGiaTriKhoa('sv.adminAccounts', 'x').ok, false)
  assert.equal(kiemGiaTriKhoa(5, '1').ok, false)
})

test('danh mục dữ liệu game: tên SQL an toàn, mã duy nhất, thẻ hợp lệ', () => {
  const ma = new Set<string>()
  const tab = new Set(TAB_DU_LIEU_GAME.map(t => t.ma))
  for (const b of DS_DU_LIEU_GAME) {
    assert.ok(!ma.has(b.ma), `trùng mã ${b.ma}`)
    ma.add(b.ma)
    assert.ok(tab.has(b.tab), `${b.ma}: thẻ ${b.tab}`)
    assert.match(b.ma, /^[a-z0-9-]+$/)
    if (b.loai !== 'sql') continue
    for (const t of [b.bang!, ...b.cot.map(c => c.key), ...(b.tim ?? []), ...(b.sapXep ? [b.sapXep] : [])]) assert.ok(laTenSqlAnToan(t), `${b.ma}: ${t}`)
    for (const t of b.tim ?? []) assert.ok(b.cot.some(c => c.key === t), `${b.ma}: cột tìm ${t} không nằm trong cot`)
  }
  for (const t of tab) assert.ok(DS_DU_LIEU_GAME.some(b => b.tab === t), `thẻ ${t} trống`)
  assert.equal(timBoDuLieu('../tob_char'), null)
  assert.equal(timBoDuLieu('tob_char'), null)
})

test('SQL dựng ra: tìm qua tham số, kẹp limit', () => {
  const b = timBoDuLieu('quai')!
  const s = dungSqlBoDuLieu(b, "x' OR 1=1 -- %", 999999, -5)
  assert.ok(!s.sql.includes("x'"))
  assert.match(s.sql, /LIMIT 5000 OFFSET 0$/)
  assert.deepEqual(s.thamSo, ["%x' OR 1=1 -- %", "%x' OR 1=1 -- %"])
  assert.equal(dungSqlBoDuLieu(b, '', 10, 20).thamSo.length, 0)
  assert.match(dungSqlBoDuLieu(b, '', 10, 20).sql, /LIMIT 10 OFFSET 20$/)
})
