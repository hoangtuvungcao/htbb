/**
 * Kiểm thử quy tắc đăng ký dùng chung (web + game). Chạy: npm test  (node --test, Node ≥ 22.18).
 * Bộ ca `thuNghiem` nằm trong shared/data/quy-tac-dang-ky.json và CHÍNH bộ đó cũng chạy bên Java
 * (HTBB/test/KiemDangKyTrongGame.java) -> hai bản cài đặt không lệch được.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { createHash } from 'node:crypto'
import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { kiemTraDangKy, tenBiCam, thongBaoDangKy, vuotHanMuc, type QuyTacDangKy } from '../shared/dang-ky/kiem-tra.ts'

const duongJson = fileURLToPath(new URL('../shared/data/quy-tac-dang-ky.json', import.meta.url))
const raw = readFileSync(duongJson)
const qt = JSON.parse(raw.toString('utf8')) as QuyTacDangKy & {
  thuNghiem: { u: string, p: string, c: string, e: string, web: string[], game: string[] }[]
}

test('bộ ca chung: web', () => {
  for (const t of qt.thuNghiem) {
    const ma = kiemTraDangKy(qt, { username: t.u, password: t.p, confirm: t.c, email: t.e }, 'web').map(l => l.ma)
    assert.deepEqual(ma, t.web, `web: ${JSON.stringify(t)}`)
  }
})

test('bộ ca chung: game (email không bắt buộc)', () => {
  for (const t of qt.thuNghiem) {
    const ma = kiemTraDangKy(qt, { username: t.u, password: t.p, confirm: t.c, email: t.e }, 'game').map(l => l.ma)
    assert.deepEqual(ma, t.game, `game: ${JSON.stringify(t)}`)
  }
})

test('tên quản trị viết kiểu số (adm1n, 4dmin) vẫn bị chặn', () => {
  assert.equal(tenBiCam(qt, 'adm1n'), 'tenQuanTri')
  assert.equal(tenBiCam(qt, 'xx4dminxx'), 'tenQuanTri')
  assert.equal(tenBiCam(qt, 'pikachu'), null)
})

test('câu báo lỗi thay {min}/{max}', () => {
  assert.equal(thongBaoDangKy(qt, 'tenSai'), `Tên tài khoản phải từ ${qt.tenTaiKhoan.min}-${qt.tenTaiKhoan.max} ký tự, chỉ gồm chữ thường và số.`)
  assert.ok(!thongBaoDangKy(qt, 'matKhauSai').includes('{'))
})

test('hạn mức: 3/giờ, 10/ngày, tính cả lượt đang chạy', () => {
  assert.equal(qt.hanMuc.moiGio, 3)
  assert.equal(qt.hanMuc.moiNgay, 10)
  assert.equal(vuotHanMuc(qt, 0, 0, 0), null)
  assert.equal(vuotHanMuc(qt, 2, 2, 0), null)
  assert.equal(vuotHanMuc(qt, 3, 3, 0), 'quaGio')
  assert.equal(vuotHanMuc(qt, 2, 2, 1), 'quaGio')
  assert.equal(vuotHanMuc(qt, 0, 10, 0), 'quaNgay')
  assert.equal(vuotHanMuc(qt, 0, 9, 1), 'quaNgay')
})

test('bản Java sinh ra khớp đúng tệp JSON này (sha256)', (t) => {
  const java = fileURLToPath(new URL('../../server/src/data/QuyTacDangKyDuLieu.java', import.meta.url))
  if (!existsSync(java)) {
    t.skip('không có repo HTBB bên cạnh')
    return
  }
  const sha = createHash('sha256').update(raw).digest('hex')
  assert.ok(readFileSync(java, 'utf8').includes(`SHA256 = "${sha}"`),
    'HTBB/src/data/QuyTacDangKyDuLieu.java đã cũ: chạy node scripts/sinh-quy-tac-dang-ky-java.mjs')
})
