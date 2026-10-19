/** Lịch sự kiện năm: giờ Việt Nam, giai đoạn, thời điểm đăng tin tự động, nội dung tin, trang chủ. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import {
  BAO_TRUOC_NGAY, SU_KIEN_PHU, chuoiTuMs, dangVaSapToi, demNguoc, giaiDoan, gioDangTin, gioDep, khoangThoiGian,
  msTuChuoi, slugTin, suKienKem, suKienLanTuDong, tinCanDang, vietTin, type SuKienLan,
} from '../shared/utils/suKienNam.ts'

const H = 3_600_000
const NGAY = 24 * H
const t = (s: string) => msTuChuoi(s)

const halloween: SuKienLan = {
  ma: 'halloween', nam: 2026, lan: 0, ten: 'Halloween', nhom: 'Lễ hội', ngayLe: '31/10',
  moTa: 'Bí ngô, mặt nạ, hộp quà Halloween và boss Darkrai.', phanThuong: 'Hộp quà Halloween, mặt nạ, bí ngô',
  mo: '2026-10-18 04:00:00', dong: '2026-11-03 04:00:00', ghiDe: 0,
}
const demHalloween: SuKienLan = { ...halloween, ma: 'demhalloween', ten: 'Đêm Halloween', moTa: 'Thưởng riêng ngày 31/10.', mo: '2026-10-31 04:00:00', dong: '2026-11-01 04:00:00' }
const tuanHe: SuKienLan = { ...halloween, ma: 'moi:tuanhe', ten: 'Tuần hệ', nhom: 'Tuần Pokémon', lan: 5, mo: '2026-10-12 04:00:00', dong: '2026-10-19 04:00:00' }

test('giờ Việt Nam UTC+7, đọc / ghi chuỗi CSDL', () => {
  assert.equal(t('2026-10-18 04:00:00'), Date.UTC(2026, 9, 17, 21, 0, 0))
  assert.equal(chuoiTuMs(t('2026-02-17 04:00:00')), '2026-02-17 04:00:00')
  assert.equal(t('2026-10-18T04:00'), t('2026-10-18 04:00:00'))
  assert.ok(Number.isNaN(t('hôm qua')))
  assert.equal(gioDep(t('2026-10-18 04:00:00')), '04:00 Chủ nhật 18/10/2026')
  assert.equal(khoangThoiGian(halloween), 'từ 04:00 Chủ nhật 18/10/2026 đến 04:00 Thứ Ba 03/11/2026')
})

test('giai đoạn: xa → sắp (3 ngày trước) → đang → kết thúc (2 ngày) → xa', () => {
  assert.equal(giaiDoan(halloween, t('2026-10-15 03:59:00')), 'xa')
  assert.equal(giaiDoan(halloween, t('2026-10-15 04:00:00')), 'sap')
  assert.equal(giaiDoan(halloween, t('2026-10-18 03:59:59')), 'sap')
  assert.equal(giaiDoan(halloween, t('2026-10-18 04:00:00')), 'dang')
  assert.equal(giaiDoan(halloween, t('2026-11-03 03:59:59')), 'dang')
  assert.equal(giaiDoan(halloween, t('2026-11-03 04:00:00')), 'ket')
  assert.equal(giaiDoan(halloween, t('2026-11-05 04:00:00')), 'xa')
  assert.equal(giaiDoan({ ...halloween, ghiDe: 1 }, t('2026-10-20 12:00:00')), 'xa', 'admin tắt năm nay')
  assert.equal(BAO_TRUOC_NGAY, 3)
})

test('đăng tin đúng mốc: một tin của giai đoạn hiện tại', () => {
  assert.equal(tinCanDang(halloween, t('2026-10-14 12:00:00')), null)
  assert.deepEqual(tinCanDang(halloween, t('2026-10-15 04:05:00')), { kieu: 'sap', han: t('2026-10-15 04:00:00') })
  assert.deepEqual(tinCanDang(halloween, t('2026-10-18 04:10:00')), { kieu: 'dang', han: t('2026-10-18 04:00:00') })
  assert.deepEqual(tinCanDang(halloween, t('2026-11-03 04:00:00')), { kieu: 'ket', han: t('2026-11-03 04:00:00') })
  assert.equal(tinCanDang(halloween, t('2026-11-06 00:00:00')), null, 'quá hạn tin kết thúc: không đăng tin cũ')
  // Sự kiện phụ không có tin riêng; tuần Pokémon báo trước 1 ngày, không có tin kết thúc.
  assert.equal(tinCanDang(demHalloween, t('2026-10-31 05:00:00')), null)
  assert.equal(tinCanDang(tuanHe, t('2026-10-10 12:00:00')), null)
  assert.deepEqual(tinCanDang(tuanHe, t('2026-10-11 04:00:00')), { kieu: 'sap', han: t('2026-10-11 04:00:00') })
  assert.equal(tinCanDang(tuanHe, t('2026-10-19 05:00:00')), null)
  assert.equal(tinCanDang({ ...halloween, ghiDe: 1 }, t('2026-10-18 05:00:00')), null)
})

test('giờ đăng: đúng mốc nếu trễ ít, trễ quá 24 giờ thì lấy giờ đăng thật', () => {
  const han = t('2026-10-18 04:00:00')
  assert.equal(gioDangTin(han, han + 10 * 60_000), han)
  assert.equal(gioDangTin(han, han + 23 * H), han)
  assert.equal(gioDangTin(han, han + 25 * H), han + 25 * H)
})

test('nội dung tin: tiêu đề, thời gian, mô tả, phần thưởng, sự kiện đi kèm, slug cố định', () => {
  const sap = vietTin(halloween, 'sap', [demHalloween])
  assert.equal(sap.tieuDe, 'Sắp diễn ra: Halloween')
  assert.equal(sap.slug, 'su-kien-halloween-2026-sap')
  assert.equal(sap.loai, 'Sự kiện')
  assert.match(sap.noiDung, /từ 04:00 Chủ nhật 18\/10\/2026 đến 04:00 Thứ Ba 03\/11\/2026/)
  assert.match(sap.noiDung, /16 ngày/)
  assert.match(sap.noiDung, /Hộp quà Halloween, mặt nạ, bí ngô/)
  assert.match(sap.noiDung, /Đêm Halloween/)
  assert.ok(sap.tomTat.length <= 500)
  const dang = vietTin(halloween, 'dang')
  assert.equal(dang.tieuDe, 'Đang diễn ra: Halloween')
  assert.match(dang.tomTat, /đang diễn ra tới 04:00 Thứ Ba 03\/11\/2026/)
  const ket = vietTin(halloween, 'ket')
  assert.equal(ket.tieuDe, 'Đã kết thúc: Halloween')
  assert.equal(ket.slug, 'su-kien-halloween-2026-ket')
  assert.equal(slugTin(tuanHe, 'dang'), 'su-kien-moi-tuanhe-2026-5-dang')
  assert.match(slugTin({ ma: 'Tết:ABC', nam: 2027, lan: 0 }, 'sap'), /^su-kien-[a-z0-9-]+-2027-sap$/)
  assert.deepEqual(suKienKem(halloween, [halloween, demHalloween, tuanHe]).map(k => k.ma), ['demhalloween'])
})

test('trang chủ: đang diễn ra (sắp hết trước) + sắp tới trong 30 ngày, bỏ sự kiện phụ / bị tắt', () => {
  const noel: SuKienLan = { ...halloween, ma: 'noel', ten: 'Giáng sinh (Noel)', mo: '2026-12-18 04:00:00', dong: '2026-12-28 04:00:00' }
  const nhaGiao: SuKienLan = { ...halloween, ma: 'sinhnhat2011', ten: 'Ngày Nhà giáo Việt Nam 20/11', mo: '2026-11-17 04:00:00', dong: '2026-11-24 04:00:00' }
  const tat: SuKienLan = { ...nhaGiao, ma: 'blackfriday', ghiDe: 1, mo: '2026-11-26 04:00:00', dong: '2026-12-01 04:00:00' }
  const pv = { ...halloween, ma: 'moi:2010', ten: 'Phụ nữ Việt Nam 20/10', mo: '2026-10-17 04:00:00', dong: '2026-10-23 04:00:00' }
  const r = dangVaSapToi([noel, halloween, demHalloween, nhaGiao, tat, pv], t('2026-10-19 15:00:00'), 30)
  assert.deepEqual(r.dang.map(l => l.ma), ['moi:2010', 'halloween'])
  assert.deepEqual(r.sap.map(l => l.ma), ['sinhnhat2011'])
})

test('đếm ngược', () => {
  assert.equal(demNguoc(3 * NGAY + 4 * H), '3 ngày 4 giờ')
  assert.equal(demNguoc(5 * H + 12 * 60_000), '5 giờ 12 phút')
  assert.equal(demNguoc(8 * 60_000), '8 phút')
  assert.equal(demNguoc(0), 'ngay bây giờ')
})

test('đọc dòng CSDL', () => {
  const l = suKienLanTuDong({ ma: 'trungthu', nam: '2027', lan: '0', ten: 'Trung Thu', nhom: 'Lễ hội', ngay_le: '15/8 âm lịch', mo_ta: 'x', phan_thuong: 'y', mo_luc: '2027-09-05 04:00:00', dong_luc: '2027-09-19 04:00:00', ghi_de: '2' })
  assert.equal(l.nam, 2027)
  assert.equal(l.ngayLe, '15/8 âm lịch')
  assert.equal(l.ghiDe, 2)
  assert.equal(l.mo, '2027-09-05 04:00:00')
})

test('mã sự kiện phụ khớp lịch năm của máy chủ game (com.htbb.pokemon.LichNam)', () => {
  const java = readFileSync(new URL('../../server/src/com/htbb/pokemon/LichNam.java', import.meta.url), 'utf8')
  for (const [phu, chinh] of Object.entries(SU_KIEN_PHU)) {
    assert.ok(java.includes(`"${phu}"`), `${phu} có trong LichNam`)
    assert.ok(java.includes(`"${chinh}"`), `${chinh} có trong LichNam`)
  }
  assert.ok(java.includes('"Tuần Pokémon"'), 'nhóm "Tuần Pokémon" khớp laTuanPokemon')
})
