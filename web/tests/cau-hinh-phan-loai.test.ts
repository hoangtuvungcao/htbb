/**
 * Phân loại cấu hình (docs/cau-hinh.md, cột `lop` của bảng cau_hinh_danh_muc trong sql/htbb.sql):
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
import { docBangSql } from '../shared/utils/sqlDump.ts'
import { anTT, demLopTT, hieuLucWebTT, khoaTT, nhomTT, soSanhTT, suaDuocTT } from '../shared/utils/cauHinhTrungTam.ts'
import { phanDayDu, phanNangCap } from './htbbSql.ts'

const HTBB = fileURLToPath(new URL('../../', import.meta.url))
const k = (khoa: string) => {
  const r = khoaTT(khoa)
  assert.ok(r, `thiếu khoá ${khoa}`)
  return r
}

test('phân loại: ít khoá chỉnh trên web, phần lớn ẩn', () => {
  const d = demLopTT()
  assert.equal(d.chinh + d.co_dinh + d.luon_bat, DANH_MUC_TT.length)
  // <= 72: 0.9.6 thêm 3 khoá chỉnh của thẻ đặt tên Pokémon (giá + mức + trọng số mức)
  // <= 78: quản lý thú thêm 6 khoá chỉnh (thu.*), open test thêm 2 khoá hạn mức đăng ký (dang_ky.toi_da_gio / _ngay —
  // chủ game chỉnh trên web khi người chơi dùng chung mạng di động bị chặn). Cùng trần với server/test/KiemPhanLoaiCauHinh.java.
  // <= 81: thêm 3 khoá Về làng miễn phí (ve_lang.moi_ngay / thang_gym / toi_da — chủ game chỉnh lượt trên web).
  // <= 95: thêm nhóm hlv_ai 14 khoá (Huấn luyện viên AI: số lượng / lịch / tỉ lệ / cấp / bản đồ / hoạt động + 6 luật chống lạm phát).
  // <= 97: thêm hlv_ai.doi_bot_toi_da + hlv_ai.tien_to_ten.
  // <= 102: đổi lượng (chủ game 31/10/2026): doi_luong.ti_le, doi_luong_xu.phan_tram_gio / tran_ngay, san_luong.phi_phan_tram / tran_ngay.
  // <= 105: thêm 3 khoá exp.nang_luong_{thanh_vien,the_thang,ve_mua}_phut (thưởng năng lượng theo gói, chủ game 31/10/2026).
  // <= 106: thêm tang_toc.bua_ngay_toi_da (Bùa thể lực không giới hạn, admin đặt lại trần).
  // <= 108: thêm tang_toc.bua_exp_phan_tram / bua_xu_phan_tram (bậc bùa 50% / 50%).
  // <= 109: thêm hlv_ai.cap_theo_nguoi.
  // <= 114: PK 3 vs 3 buổi tối (chủ game 05/11/2026): pk3v3.bat / gio_mo / gio_dong / tran_ngay / luong_thang.
  assert.ok(d.chinh >= 40 && d.chinh <= 114, `chỉnh trên web: ${d.chinh}`)
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
    'hoi_sinh.xu_phan_tram_gio', 'hoi_sinh.luong_khoa', 'dang_ky.web.bat', 'sv.dangKyTrongGame', 'sv.website', 'sv.linkTaiGame', 'sv.banToiThieu', 'sv.banMoiNhat',
    'giftcode.ngan_sach_thang', 'sv.gioChiemThanh', 'HTBB_NAP_BAT']) {
    assert.ok(!anTT(k(khoa)), khoa)
  }
  // Mặc định của tỉ lệ rơi = tinh chỉnh đang chạy (nút "Về mặc định" không làm nhảy kinh tế)
  assert.equal(k('sv.tileXu').macDinh, '4')
  assert.equal(k('sv.tilePotion').macDinh, '14')
})

test('nhóm trên trang: 8 nhóm (thêm Chống clone 19/10/2026; Huấn luyện viên AI 31/10/2026; Thẻ đặt tên Pokémon 0.9.8 chỉ có khoá ẩn), không khoá ẩn; nhóm chỉ có khoá ẩn bị đánh dấu an', () => {
  const trang = nhomTT()
  assert.deepEqual(trang.map(n => n.ma), ['kinh_te', 'su_kien', 'nap_tien', 'thanh_vien', 'chong_clone', 'tu_dong', 'bao_tri', 'hlv_ai'])
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

test('CSDL mang phân loại (cột lop / an của cau_hinh_danh_muc / cau_hinh_nhom, tài liệu)', () => {
  for (const [f, sql] of [['sql/htbb.sql phần A', phanDayDu()], ['sql/htbb.sql phần B', phanNangCap()]] as const) {
    assert.ok(/`lop` varchar\(10\)/i.test(sql) && /`an` tinyint/i.test(sql), `${f}: cột lop / an`)
    assert.ok(/CREATE TABLE[^;]*`cau_hinh_nhom`/.test(sql), `${f}: bảng cau_hinh_nhom`)
  }
  const dong = docBangSql(phanDayDu(), 'cau_hinh_danh_muc')
  assert.equal(dong.length, DANH_MUC_TT.length)
  for (const x of dong) assert.equal(k(String(x.khoa)).lop ?? 'chinh', x.lop, String(x.khoa))
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
