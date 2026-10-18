/** Tệp bao_tri.txt: đọc phải khớp HTBB/src/server/ChiSoMayChu.java#docTepBaoTri; kiểm body POST. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { baoTriDoc, baoTriDung, baoTriKiem, baoTriCauHienThi, baoTriLenhTay, BAO_TRI_CAU_MAC_DINH } from '../shared/utils/baoTri.ts'

test('đọc giống game: dòng đầu là câu, các dòng cho: là tài khoản', () => {
  assert.deepEqual(baoTriDoc('Bảo trì 30 phút\ncho: Admin, gm_1  tai001\ncho:x2\n'), { cau: 'Bảo trì 30 phút', cho: ['admin', 'gm_1', 'tai001', 'x2'] })
  assert.deepEqual(baoTriDoc(''), { cau: '', cho: [] })
  assert.deepEqual(baoTriDoc('CHO: abc\n'), { cau: '', cho: ['abc'] })
  assert.deepEqual(baoTriDoc('\uFEFF  Câu  \r\n'), { cau: 'Câu', cho: [] })
  assert.equal(baoTriCauHienThi(''), BAO_TRI_CAU_MAC_DINH)
})

test('dựng rồi đọc lại ra y hệt', () => {
  const nd = { cau: 'Nâng cấp máy chủ, mở lại 15:00', cho: ['admin', 'tai001'] }
  assert.deepEqual(baoTriDoc(baoTriDung(nd)), nd)
  assert.equal(baoTriDung({ cau: '', cho: [] }), '\n')
  assert.deepEqual(baoTriDoc(baoTriDung({ cau: '', cho: ['gm_a'] })), { cau: '', cho: ['gm_a'] })
})

test('kiểm body', () => {
  assert.deepEqual(baoTriKiem({ bat: false }), { ok: true, bat: false })
  assert.equal(baoTriKiem({}).ok, false)
  assert.equal(baoTriKiem(null).ok, false)
  assert.equal(baoTriKiem({ bat: 'true' }).ok, false)
  assert.deepEqual(baoTriKiem({ bat: true, cau: ' Bảo trì ', cho: 'Admin, tai001 admin' }), { ok: true, bat: true, noiDung: { cau: 'Bảo trì', cho: ['admin', 'tai001'] } })
  assert.equal(baoTriKiem({ bat: true, cau: 'a\nb' }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cau: 'a\u2028b' }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cau: 'x'.repeat(201) }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cau: 'ệ'.repeat(200) }).ok, true)
  assert.equal(baoTriKiem({ bat: true, cau: 'cho: hack' }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: ['ab'] }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: ['a'.repeat(21)] }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: ['tên-có-dấu'] }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: ['abc;rm'] }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: [1] }).ok, false)
  assert.equal(baoTriKiem({ bat: true, cho: Array.from({ length: 51 }, (_, i) => `tk${String(i).padStart(3, '0')}`) }).ok, false)
})

test('lệnh làm tay trích dẫn an toàn', () => {
  assert.equal(baoTriLenhTay('/opt/htbb/', null), 'sudo rm -f /opt/htbb/bao_tri.txt')
  const l = baoTriLenhTay('/opt/htbb', { cau: "It's ok", cho: ['admin'] })
  assert.equal(l, `printf '%s\\n' 'It'\\''s ok' 'cho: admin' | sudo -u htbb tee /opt/htbb/bao_tri.txt >/dev/null`)
})
