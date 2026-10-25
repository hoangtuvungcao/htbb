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
import {
  KHOA_HAN_MUC_GIO, KHOA_HAN_MUC_NGAY, hanMucHieuLuc, kiemTraDangKy, tenBiCam, thongBaoDangKy, vuotHanMuc, type QuyTacDangKy,
} from '../shared/dang-ky/kiem-tra.ts'
import { DANH_MUC_TT } from '../shared/utils/cauHinhTrungTam.gen.ts'
import { giuCho, traCho } from '../server/utils/chanSpam.ts'
import { phanNangCap } from './htbbSql.ts'

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

// ---------------------------------------------------------------- hạn mức chỉnh được qua cau_hinh

const docWeb = (f: string) => readFileSync(fileURLToPath(new URL(`../${f}`, import.meta.url)), 'utf8')

test('hạn mức: cau_hinh ghi đè, vắng / hỏng / ≤ 0 lùi về JSON', () => {
  const json = { moiGio: qt.hanMuc.moiGio, moiNgay: qt.hanMuc.moiNgay }
  assert.deepEqual(hanMucHieuLuc(qt), json)
  assert.deepEqual(hanMucHieuLuc(qt, undefined, null), json)
  assert.deepEqual(hanMucHieuLuc(qt, '', '  '), json)
  assert.deepEqual(hanMucHieuLuc(qt, 'abc', '-5'), json)
  assert.deepEqual(hanMucHieuLuc(qt, '0', '1.5'), json)
  assert.deepEqual(hanMucHieuLuc(qt, '6', '30'), { moiGio: 6, moiNgay: 30 })
  assert.deepEqual(hanMucHieuLuc(qt, ' 6 ', 30), { moiGio: 6, moiNgay: 30 })
  assert.deepEqual(hanMucHieuLuc(qt, '6'), { moiGio: 6, moiNgay: json.moiNgay })
})

test('hạn mức: open test 6 / giờ, 30 / ngày thay cho 3 / 10', () => {
  const ot = hanMucHieuLuc(qt, '6', '30')
  // CGNAT: 9 tài khoản / 24 giờ, 2 trong giờ — JSON chặn (đúng lỗi người chơi gặp), open test cho qua
  assert.equal(vuotHanMuc(qt, 2, 9, 1), 'quaNgay')
  assert.equal(vuotHanMuc(qt, 2, 9, 0, ot), null)
  assert.equal(vuotHanMuc(qt, 5, 20, 0, ot), null)
  assert.equal(vuotHanMuc(qt, 6, 20, 0, ot), 'quaGio')
  assert.equal(vuotHanMuc(qt, 5, 20, 1, ot), 'quaGio')
  assert.equal(vuotHanMuc(qt, 0, 30, 0, ot), 'quaNgay')
  assert.equal(vuotHanMuc(qt, 0, 29, 1, ot), 'quaNgay')
  // script tạo hàng loạt vẫn bị chặn
  assert.equal(vuotHanMuc(qt, 100, 100, 0, ot), 'quaNgay')
  // vắng tham số = JSON (như trước)
  assert.equal(vuotHanMuc(qt, 3, 3, 0), 'quaGio')
})

test('hạn mức: khoá cau_hinh trong danh mục, chỉnh trên web, mặc định = JSON, cả web lẫn game đọc', () => {
  for (const [khoa, macDinh] of [[KHOA_HAN_MUC_GIO, qt.hanMuc.moiGio], [KHOA_HAN_MUC_NGAY, qt.hanMuc.moiNgay]] as const) {
    const k = DANH_MUC_TT.find(x => x.khoa === khoa)
    assert.ok(k, khoa)
    assert.equal(k.macDinh, String(macDinh), `${khoa}: mặc định phải bằng hanMuc của quy-tac-dang-ky.json`)
    assert.equal(k.kieu, 'so')
    assert.equal(k.nhom, 'bao_tri') // cùng nhóm dang_ky.web.bat
    assert.equal(k.lop, undefined) // chỉnh trên web
    assert.equal(k.dungO, 'ca_hai') // web (docNho bỏ khoá chỉ 'server') + máy chủ game
    assert.equal(k.apDung, 'ngay')
    assert.ok((k.min ?? 0) >= 1)
  }
  const web = docWeb('server/utils/dangKy.ts')
  assert.ok(web.includes('chGiaTri(khoa)') && web.includes('KHOA_HAN_MUC_GIO') && web.includes('KHOA_HAN_MUC_NGAY'))
  assert.ok(/dangKyVuotHanMuc\(gio, ngay, dangChay, await dangKyHanMuc\(\)\)/.test(web))
  const java = fileURLToPath(new URL('../../server/src/real/cmd/DangKyTrongGame.java', import.meta.url))
  if (existsSync(java)) {
    const j = readFileSync(java, 'utf8')
    assert.ok(j.includes('CauHinhTrungTam.chuoi(QuyTacDangKy.KHOA_HAN_MUC_GIO)'))
    assert.ok(j.includes('CauHinhTrungTam.chuoi(QuyTacDangKy.KHOA_HAN_MUC_NGAY)'))
    assert.ok(readFileSync(fileURLToPath(new URL('../../server/src/data/QuyTacDangKy.java', import.meta.url)), 'utf8')
      .includes(`KHOA_HAN_MUC_GIO = "${KHOA_HAN_MUC_GIO}"`))
  }
})

test('hạn mức: khối SQL open test chỉ thêm khi chưa có dòng (không đè giá trị admin)', () => {
  const b = phanNangCap()
  const i = b.indexOf('-- >>> mo_han_muc_dang_ky.sql')
  assert.ok(i > 0)
  const khoi = b.slice(i)
  assert.match(khoi, /INSERT IGNORE INTO `cau_hinh`[^;]*\('dang_ky\.toi_da_gio', '6'[^;]*\('dang_ky\.toi_da_ngay', '30'/)
  assert.ok(!/ON DUPLICATE KEY|UPDATE `?cau_hinh`? SET|REPLACE INTO/i.test(khoi.split('\n-- >>> ')[0]!))
})

test('câu báo vượt hạn mức: ngắn, chỉ cách xử lý (Wi-Fi khác / hỗ trợ), không nói "từ địa chỉ này"', () => {
  for (const ma of ['quaGio', 'quaNgay']) {
    const c = thongBaoDangKy(qt, ma)
    assert.ok(c.includes('mạng di động') && c.includes('Wi-Fi khác') && c.includes('liên hệ hỗ trợ'), c)
    assert.ok(c.length <= 160, `${ma}: ${c.length} ký tự`)
  }
  assert.notEqual(thongBaoDangKy(qt, 'quaGio'), thongBaoDangKy(qt, 'quaNgay'))
})

test('câu báo hạn mức theo THIẾT BỊ (đăng ký trong game, mã máy) riêng, không nói mạng di động / Wi-Fi', () => {
  assert.equal(thongBaoDangKy(qt, 'mayQuaNgay'), 'Thiết bị này đã tạo đủ tài khoản hôm nay. Thử lại vào ngày mai hoặc liên hệ hỗ trợ.')
  for (const ma of ['mayQuaGio', 'mayQuaNgay']) {
    const c = thongBaoDangKy(qt, ma)
    assert.ok(c.startsWith('Thiết bị') && c.includes('liên hệ hỗ trợ') && !c.includes('mạng di động') && !c.includes('Wi-Fi'), c)
    assert.ok(c.length <= 160, `${ma}: ${c.length} ký tự`)
  }
  assert.notEqual(thongBaoDangKy(qt, 'mayQuaGio'), thongBaoDangKy(qt, 'mayQuaNgay'))
})

test('lần lỗi không tính vào hạn mức: chỗ giữ được trả, lần bấm lại ngay không bị chặn', () => {
  const khoa = `dang_ky|thu-${Date.now()}`
  // lần 1: giữ chỗ rồi lỗi (trùng tên / lỗi CSDL) -> trả
  assert.equal(giuCho(khoa), 0)
  traCho(khoa)
  // lần 2 ngay sau đó: không còn lượt "đang chạy" nào -> đếm như chưa có gì
  assert.equal(giuCho(khoa), 0)
  traCho(khoa)
  // nếu KHÔNG trả, lần sau thấy 1 lượt đang chạy (lỗi cũ: 2 tài khoản trong giờ + 1 chỗ giữ = chạm 3)
  giuCho(khoa)
  assert.equal(giuCho(khoa), 1)
  traCho(khoa); traCho(khoa)
  assert.equal(vuotHanMuc(qt, 2, 2, 1), 'quaGio')
  assert.equal(vuotHanMuc(qt, 2, 2, 0), null)

  // register.post.ts: nhập liệu kiểm TRƯỚC hạn mức; mọi đường không tạo được tài khoản trả chỗ giữ (finally)
  const r = docWeb('server/api/auth/register.post.ts')
  assert.ok(r.indexOf('dangKyKiemTra(d)') < r.indexOf('dangKyBiChan(event)'), 'kiểm nhập liệu trước hạn mức')
  assert.match(r, /finally \{\s*if \(!daGhi\) dangKyTraCho\(event\)/)
  assert.ok(r.indexOf('daGhi = true') < r.indexOf("ghiLanLam(event, 'dang_ky')"))
  assert.ok(r.indexOf("ghiLanLam(event, 'dang_ky')") > r.indexOf('DANG_KY_INSERT_SQL'), 'chỉ ghi lượt sau khi INSERT thành công')
  assert.ok(docWeb('server/utils/dangKy.ts').includes("traCho(`dang_ky|${ipKhach(event)}`)"))
})
