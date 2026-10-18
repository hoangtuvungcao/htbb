/**
 * Cấu hình trung tâm (web): danh mục sinh khớp tools/cauhinh/danh_muc.json, luật kiểm khớp máy chủ game
 * (bộ ví dụ dùng chung tools/cauhinh/vi_du_kiem.tsv — server/test/KiemCauHinhTrungTam.java chạy cùng bộ),
 * so sánh trước khi lưu, khoá env / thử nghiệm, tìm kiếm, phân quyền. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { DANH_MUC_TT, NHOM_TT } from '../shared/utils/cauHinhTrungTam.gen.ts'
import {
  hienThiTT, khoaTT, kiemGiaTriTT, kiemYeuCauKhoiDongLai, nhomTT, soSanhTT, suaDuocTT, timTT,
} from '../shared/utils/cauHinhTrungTam.ts'
import { kiemQuyenAdmin, timQuyTacQuyen } from '../shared/utils/quyenAdmin.ts'

/** Gốc monorepo (web/ nằm ngay dưới). */
const HTBB = fileURLToPath(new URL('../../', import.meta.url))
const k = (khoa: string) => {
  const r = khoaTT(khoa)
  assert.ok(r, `thiếu khoá ${khoa}`)
  return r
}

test('danh mục: khoá duy nhất, nhóm hợp lệ, mặc định hợp lệ theo chính luật', () => {
  const thay = new Set<string>()
  const nhom = new Set(NHOM_TT.map(n => n.ma))
  assert.ok(DANH_MUC_TT.length > 100)
  for (const x of DANH_MUC_TT) {
    assert.ok(!thay.has(x.khoa), `trùng ${x.khoa}`)
    thay.add(x.khoa)
    assert.ok(nhom.has(x.nhom), `${x.khoa}: nhóm lạ ${x.nhom}`)
    assert.ok(/^[A-Za-z0-9_.]{1,64}$/.test(x.khoa), x.khoa)
    if (x.kieu === 'chon') assert.ok(x.luaChon?.length, `${x.khoa}: chọn cần lựa chọn`)
    if (x.bang === 'env') assert.ok(x.chiDoc, `${x.khoa}: env phải chỉ đọc`)
    if (x.bang !== 'env') assert.ok(kiemGiaTriTT(x, x.macDinh).ok, `${x.khoa}: mặc định "${x.macDinh}" sai luật`)
    assert.ok(!x.khoa.startsWith('db.'), `${x.khoa}: khoá CSDL không được vào danh mục`)
  }
  for (const bat of ['sv.lau_dai', 'sv.he_sinh_thai', 'sv.nap_tien']) assert.equal(k(bat).apDung, 'khoi_dong_lai', bat)
  for (const ngay of ['sv.tileXu', 'thue.cho', 'nap.luong.bat', 'bao_tri.bat', 'thanh_vien.bat', 'sv.dangKyTrongGame']) assert.equal(k(ngay).apDung, 'ngay', ngay)
  assert.ok(k('sv.tua_nhanh_ngay').khoaProd && k('sv.testMode').khoaProd)
  assert.ok(k('sv.testCode').nhay)
})

test('bản sinh khớp tools/cauhinh/danh_muc.json', { skip: !existsSync(`${HTBB}tools/cauhinh/danh_muc.json`) }, () => {
  const j = JSON.parse(readFileSync(`${HTBB}tools/cauhinh/danh_muc.json`, 'utf8')) as { nhom: { ma: string }[], khoa: Record<string, any>[] }
  assert.deepEqual(DANH_MUC_TT.map(x => x.khoa), j.khoa.map(x => x.khoa), 'chạy python3 tools/cauhinh/sinh_cau_hinh.py')
  assert.deepEqual(NHOM_TT.map(x => x.ma), j.nhom.map(x => x.ma))
  for (const x of j.khoa) {
    const t = k(x.khoa)
    assert.equal(t.macDinh, x.macDinh ?? '', x.khoa)
    assert.equal(t.kieu, x.kieu, x.khoa)
    assert.equal(t.apDung, x.apDung ?? 'ngay', x.khoa)
    assert.equal(t.min, x.min, x.khoa)
    assert.equal(t.max, x.max, x.khoa)
  }
})

test('bộ ví dụ kiểm dùng chung với máy chủ game', { skip: !existsSync(`${HTBB}tools/cauhinh/vi_du_kiem.tsv`) }, () => {
  let n = 0
  for (const dong of readFileSync(`${HTBB}tools/cauhinh/vi_du_kiem.tsv`, 'utf8').split('\n')) {
    if (!dong.trim() || dong.startsWith('#')) continue
    const [khoa, giaTri, mong] = dong.split('\t')
    const r = kiemGiaTriTT(k(khoa!), giaTri ?? '')
    assert.equal(r.ok ? `ok:${r.giaTri}` : 'loi', mong, `${khoa} = "${giaTri}"`)
    n++
  }
  assert.ok(n > 30)
})

test('kiểm: kiểu không phải chuỗi, ký tự điều khiển, boolean', () => {
  assert.deepEqual(kiemGiaTriTT(k('sv.dangKyTrongGame'), true), { ok: true, giaTri: '1' })
  assert.deepEqual(kiemGiaTriTT(k('sv.tileXu'), 42), { ok: true, giaTri: '42' })
  assert.equal(kiemGiaTriTT(k('sv.tileXu'), null).ok, false)
  assert.equal(kiemGiaTriTT(k('sv.tileXu'), { a: 1 }).ok, false)
  assert.equal(kiemGiaTriTT(k('bao_tri.cau'), 'dòng 1\ndòng 2').ok, false)
  assert.equal(kiemGiaTriTT(k('bao_tri.cau'), 'x'.repeat(201)).ok, false)
  assert.equal(khoaTT('__proto__'), null)
  assert.equal(khoaTT('toString'), null)
})

test('so sánh trước khi lưu', () => {
  const dang = { 'sv.tileXu': '20', 'thue.cho': '6', 'sv.testMode': null as string | null }
  let r = soSanhTT(dang, { 'sv.tileXu': '20', 'thue.cho': ' 7 ' })
  assert.deepEqual(r.loi, {})
  assert.deepEqual(r.thayDoi.map(d => [d.khoa, d.cu, d.moi]), [['thue.cho', '6', '7']], 'không đổi thì bỏ, có chuẩn hoá')
  r = soSanhTT(dang, { 'sv.tileXu': null })
  assert.deepEqual(r.thayDoi.map(d => [d.khoa, d.moi]), [['sv.tileXu', null]], 'null = xoá dòng')
  r = soSanhTT(dang, { 'sv.tileXu': '500' })
  assert.ok(r.loi['sv.tileXu'] && !r.thayDoi.length)
  r = soSanhTT(dang, { HTBB_NAP_BAT: '1' })
  assert.ok(r.loi.HTBB_NAP_BAT, 'khoá env không sửa được')
  r = soSanhTT(dang, { 'khoa.la': '1' })
  assert.ok(r.loi['khoa.la'])
  r = soSanhTT(dang, { 'sv.testMode': '1' }, false)
  assert.ok(r.loi['sv.testMode'], 'thử nghiệm bị khoá ở máy thật')
  r = soSanhTT(dang, { 'sv.testMode': '0' }, false)
  assert.deepEqual(r.thayDoi.map(d => d.moi), ['0'], 'được đưa về mặc định')
  r = soSanhTT(dang, { 'sv.testMode': '1' }, true)
  assert.deepEqual(r.thayDoi.map(d => d.moi), ['1'], 'máy thử cho bật')
  r = soSanhTT({ 'sv.lau_dai': '0' }, { 'sv.lau_dai': '1' })
  assert.equal(r.thayDoi[0]!.apDung, 'khoi_dong_lai')
  assert.equal(suaDuocTT(k('NAP_DA_CO_GIAY_PHEP'), true).duoc, false)
})

test('tìm kiếm không dấu + nhóm theo thứ tự', () => {
  assert.ok(timTT('roi xu').some(x => x.khoa === 'sv.tileXu'))
  assert.ok(timTT('BẢO TRÌ').some(x => x.khoa === 'bao_tri.bat'))
  assert.ok(timTT('nap.the_thang').some(x => x.khoa === 'nap.the_thang.bat'))
  assert.equal(timTT('').length, DANH_MUC_TT.length)
  const g = nhomTT()
  assert.deepEqual(g.map(x => x.ma), NHOM_TT.map(x => x.ma).filter(m => g.some(y => y.ma === m)))
  assert.equal(g.reduce((t, x) => t + x.khoa.length, 0), DANH_MUC_TT.length)
})

test('hiển thị: nhạy cảm che, cờ bật/tắt, lựa chọn có nhãn', () => {
  assert.equal(hienThiTT(k('sv.testCode'), 'bimat'), '(ẩn)')
  assert.equal(hienThiTT(k('sv.dangKyTrongGame'), '1'), 'Bật')
  assert.equal(hienThiTT(k('sv.dangKyTuKichHoat'), 'web'), 'Theo web')
  assert.equal(hienThiTT(k('sv.tileXu'), null), '—')
})

test('yêu cầu khởi động lại', () => {
  assert.deepEqual(kiemYeuCauKhoiDongLai({ giay: 60, lyDo: ' áp dụng dài hạn ' }), { ok: true, giay: 60, lyDo: 'áp dụng dài hạn' })
  assert.equal(kiemYeuCauKhoiDongLai({ giay: 5, lyDo: 'abcdef' }).ok, false)
  assert.equal(kiemYeuCauKhoiDongLai({ giay: 60, lyDo: 'ab' }).ok, false)
  assert.equal(kiemYeuCauKhoiDongLai({ giay: 60.5, lyDo: 'abcdef' }).ok, false)
  assert.equal(kiemYeuCauKhoiDongLai(null).ok, false)
})

test('phân quyền: siêu quản trị ghi, Chỉ xem đọc, vai trò khác không thấy', () => {
  assert.ok(timQuyTacQuyen('/api/admin/cau-hinh-he-thong/luu'))
  assert.ok(kiemQuyenAdmin('/api/admin/cau-hinh-he-thong', 'GET', 'xem'))
  assert.equal(kiemQuyenAdmin('/api/admin/cau-hinh-he-thong/luu', 'POST', 'xem'), false)
  assert.equal(kiemQuyenAdmin('/api/admin/cau-hinh-he-thong/khoi-dong-lai', 'POST', 'ho_tro'), false)
  assert.equal(kiemQuyenAdmin('/api/admin/cau-hinh-he-thong', 'GET', 'bien_tap'), false)
  assert.ok(kiemQuyenAdmin('/api/admin/cau-hinh-he-thong/hoan-tac', 'POST', 'sieu_quan_tri'))
  const menu = readFileSync(fileURLToPath(new URL('../app/utils/adminMenu.ts', import.meta.url)), 'utf8')
  const iHt = menu.indexOf("nhom: 'Hệ thống'")
  assert.ok(iHt > 0 && menu.indexOf("to: '/admin/cau-hinh-he-thong'") > iHt, 'trang nằm trong nhóm Hệ thống')
})
