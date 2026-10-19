/**
 * Phân loại cấu hình (docs/cau-hinh.md, trường `lop` của tools/cauhinh/danh_muc.json):
 *   chinh    — hiện trên /admin/cau-hinh-he-thong, sửa được
 *   co_dinh  — ẩn, máy thật luôn chạy giá trị cố định (macDinh), API lưu từ chối
 *   luon_bat — ẩn, luôn "1"
 * Cùng luật với máy chủ game (server/test/KiemPhanLoaiCauHinh.java). Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { DANH_MUC_TT, NHOM_TT } from '../shared/utils/cauHinhTrungTam.gen.ts'
import { anTT, demLopTT, hieuLucWebTT, khoaTT, nhomTT, soSanhTT, suaDuocTT } from '../shared/utils/cauHinhTrungTam.ts'

const HTBB = fileURLToPath(new URL('../../', import.meta.url))
const k = (khoa: string) => {
  const r = khoaTT(khoa)
  assert.ok(r, `thiếu khoá ${khoa}`)
  return r
}

test('phân loại: ít khoá chỉnh trên web, phần lớn ẩn', () => {
  const d = demLopTT()
  assert.equal(d.chinh + d.co_dinh + d.luon_bat, DANH_MUC_TT.length)
  assert.ok(d.chinh >= 40 && d.chinh <= 70, `chỉnh trên web: ${d.chinh}`)
  assert.ok(d.co_dinh > 100, `cố định: ${d.co_dinh}`)
  assert.ok(d.luon_bat >= 15, `luôn bật: ${d.luon_bat}`)
})

test('luôn bật: tính năng cốt lõi, kiểu cờ, giá trị 1', () => {
  for (const x of DANH_MUC_TT.filter(x => x.lop === 'luon_bat')) {
    assert.equal(x.kieu, 'co', x.khoa)
    assert.equal(x.macDinh, '1', x.khoa)
  }
  for (const khoa of ['sv.lau_dai', 'sv.he_sinh_thai', 'sv.spam.bat', 'chi_so.bat', 'gym.bat', 'ham_nguc.bat', 'pvp.tinh_diem.bat',
    'tu_dong.bat', 'hoi_sinh.bat', 'sv.chiSoMayChu', 'kich_hoat.thanh_vien_la_kich_hoat', 'nap.hoan_tien.thu_cong']) {
    assert.equal(k(khoa).lop, 'luon_bat', khoa)
  }
})

test('cố định: nội bộ chống DoS / nhật ký / công thức kinh tế / luật an toàn bị ẩn', () => {
  for (const khoa of ['sv.maxConnPerIp', 'sv.connRateMax', 'sv.dosBanSec', 'sv.tranGoiThuong', 'sv.log.muc', 'sv.log.tat',
    'sv.spam.chat.gan', 'chi_so.tran_chi_mang', 'tu_dong.nhip_danh', 'thue.cho', 'phi.dich_chuyen_phan_tram_gio',
    'sv.tieuNguongMau', 'sv.testMode', 'sv.isSuKienTet2017', 'hoi_sinh.he_so_lap', 'sv.worldCupBatDau']) {
    assert.equal(k(khoa).lop, 'co_dinh', khoa)
    assert.ok(anTT(k(khoa)), khoa)
  }
  // Giá trị cố định = giá trị VPS đang chạy (19/10/2026) — khoá không làm đổi kinh tế
  assert.equal(k('sv.chiaXuRoi').macDinh, '30')
  assert.equal(k('sv.ExpQua').macDinh, '1')
  assert.equal(k('sv.expvantieu').macDinh, '1')
  assert.equal(k('sv.Luckyexp').macDinh, '1')
})

test('chỉnh trên web: những thứ người vận hành thật sự chỉnh vẫn hiện', () => {
  for (const khoa of ['sv.tileXu', 'sv.tileLuong', 'sv.tileExpLv50', 'bao_tri.bat', 'sv.gioBaoTri', 'sv.nap_tien', 'nap.luong.bat',
    'tran_tuan_vnd', 'thanh_vien.gia', 'thanh_vien.bat', 'kich_hoat.chan_dang_nhap', 'tu_dong.gio_moi_ngay',
    'hoi_sinh.xu_phan_tram_gio', 'hoi_sinh.luong_khoa', 'dang_ky.web.bat', 'sv.dangKyTrongGame', 'sv.website', 'sv.linkTaiGame',
    'giftcode.ngan_sach_thang', 'sv.gioChiemThanh', 'HTBB_NAP_BAT']) {
    assert.ok(!anTT(k(khoa)), khoa)
  }
  // Mặc định của tỉ lệ rơi = tinh chỉnh đang chạy (nút "Về mặc định" không làm nhảy kinh tế)
  assert.equal(k('sv.tileXu').macDinh, '4')
  assert.equal(k('sv.tilePotion').macDinh, '14')
})

test('nhóm trên trang: 6 nhóm, không khoá ẩn; nhóm chỉ có khoá ẩn bị đánh dấu an', () => {
  const trang = nhomTT()
  assert.deepEqual(trang.map(n => n.ma), ['kinh_te', 'su_kien', 'nap_tien', 'thanh_vien', 'tu_dong', 'bao_tri'])
  for (const n of trang) for (const x of n.khoa) assert.ok(!anTT(x), x.khoa)
  for (const n of NHOM_TT) {
    const coHien = DANH_MUC_TT.some(x => x.nhom === n.ma && !anTT(x))
    assert.equal(!!n.an, !coHien, n.ma)
  }
  // máy thử: hiện cả khoá ẩn
  assert.equal(nhomTT(DANH_MUC_TT, true).reduce((s, n) => s + n.khoa.length, 0), DANH_MUC_TT.length)
})

test('lưu: máy thật từ chối khoá ẩn, cho xoá dòng / đưa về giá trị cố định; máy thử cho sửa', () => {
  assert.equal(suaDuocTT(k('sv.lau_dai'), false).duoc, false)
  assert.equal(suaDuocTT(k('sv.lau_dai'), true).duoc, true)
  let r = soSanhTT({}, { 'sv.lau_dai': '0', 'sv.spam.chat.gan': '1/1', 'sv.tileXu': '5' })
  assert.ok(r.loi['sv.lau_dai'])
  assert.ok(r.loi['sv.spam.chat.gan'])
  assert.deepEqual(r.thayDoi.map(d => d.khoa), ['sv.tileXu'])
  r = soSanhTT({ 'sv.lau_dai': '0', 'sv.chiaXuRoi': '10' }, { 'sv.lau_dai': null, 'sv.chiaXuRoi': '30' })
  assert.deepEqual(r.loi, {})
  assert.deepEqual(r.thayDoi.map(d => d.khoa).sort(), ['sv.chiaXuRoi', 'sv.lau_dai'])
  r = soSanhTT({}, { 'sv.spam.chat.gan': '1/1' }, true)
  assert.deepEqual(r.loi, {})
})

test('giá trị hiệu lực phía web: khoá ẩn bỏ qua CSDL trên máy thật', () => {
  assert.equal(hieuLucWebTT(k('kich_hoat.thanh_vien_la_kich_hoat'), '0'), '1')
  assert.equal(hieuLucWebTT(k('nap.hoan_tien.thu_cong'), '0'), '1')
  assert.equal(hieuLucWebTT(k('kich_hoat.thanh_vien_la_kich_hoat'), '0', true), '0')
  assert.equal(hieuLucWebTT(k('nap.luong.bat'), '0'), '0')
  assert.equal(hieuLucWebTT(k('nap.luong.bat'), null), '1')
  assert.equal(hieuLucWebTT(k('thanh_vien.gia'), 'abc'), k('thanh_vien.gia').macDinh)
})

test('tệp sinh mang phân loại (SQL cho bảng cau_hinh_danh_muc / cau_hinh_nhom, tài liệu)', { skip: !existsSync(`${HTBB}sql/pokemon_cau_hinh_trung_tam.sql`) }, () => {
  const sql = readFileSync(`${HTBB}sql/pokemon_cau_hinh_trung_tam.sql`, 'utf8')
  assert.ok(sql.includes('`lop` VARCHAR(10)') && sql.includes('`an` TINYINT'))
  assert.ok(sql.includes('CREATE TABLE IF NOT EXISTS `cau_hinh_nhom`'))
  const j = JSON.parse(readFileSync(`${HTBB}tools/cauhinh/danh_muc.json`, 'utf8')) as { khoa: { khoa: string, lop?: string }[] }
  for (const x of j.khoa) assert.equal(k(x.khoa).lop, x.lop, x.khoa)
  const md = readFileSync(`${HTBB}docs/cau-hinh.md`, 'utf8')
  const d = demLopTT()
  assert.ok(md.includes(`| **Chỉnh trên web** (\`chinh\`) | ${d.chinh} |`))
  assert.ok(md.includes(`| **Cố định** (\`co_dinh\`) | ${d.co_dinh} |`))
})

test('trang cấu hình duy nhất: không còn /admin/cauhinh', { skip: !existsSync(`${HTBB}web/app/pages`) }, () => {
  assert.ok(!existsSync(`${HTBB}web/app/pages/admin/cauhinh.vue`))
  assert.ok(!existsSync(`${HTBB}web/server/api/admin/cauhinh`))
  assert.ok(existsSync(`${HTBB}web/app/pages/admin/cau-hinh-he-thong.vue`))
})
