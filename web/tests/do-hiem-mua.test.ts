/**
 * 0.9.7 đề xuất 4 + 8: màu độ hiếm MỘT nguồn (tools/cauhinh/do_hiem.json -> shared/utils/doHiem.gen.ts, cùng màu game)
 * và tin tự động của Ngày cộng đồng hằng tháng (nhóm 'Ngày cộng đồng', báo trước 3 ngày, mô tả dòng nổi bật).
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { BAC_DANH_HIEU, BAC_HIEM, BAC_TRANG_BI, BAC_VAT_PHAM, bacDanhHieu, bacHiem, bacVatPham } from '../shared/utils/doHiem.gen.ts'
import { laNgayCongDong, laTuanPokemon, msTuChuoi, giaiDoan, tinCanDang, vietTin, type SuKienLan } from '../shared/utils/suKienNam.ts'

const json = JSON.parse(readFileSync(new URL('../../tools/cauhinh/do_hiem.json', import.meta.url), 'utf8')) as {
  bac: Array<{ ma: string, ten: string, mau: string, nhat: string, sang: string, vienDong: boolean }>
  mauVatPham: Record<string, string>
  danhHieu: string[]
}

test('doHiem.gen.ts khớp do_hiem.json (chạy python3 tools/cauhinh/sinh_do_hiem.py)', () => {
  assert.deepEqual(BAC_HIEM.map(b => ({ ma: b.ma, ten: b.ten, mau: b.mau, nhat: b.nhat, sang: b.sang, vienDong: b.vienDong })), json.bac.map(b => ({
    ma: b.ma, ten: b.ten, mau: b.mau, nhat: b.nhat, sang: b.sang, vienDong: b.vienDong,
  })))
  const vi = new Map(json.bac.map((b, i) => [b.ma, i]))
  for (const [k, v] of Object.entries(json.mauVatPham)) assert.equal(BAC_VAT_PHAM[Number(k)], vi.get(v), `colorName ${k}`)
  assert.deepEqual([...BAC_DANH_HIEU], json.danhHieu.map(m => vi.get(m)))
  assert.equal(BAC_TRANG_BI.length, 11)
})

test('đúng danh sách độ hiếm của đặc tả: Thường trắng · Hiếm lá · Quý dương · Sử thi tím · Huyền thoại cam (viền động) · Trang trí vàng', () => {
  assert.deepEqual(BAC_HIEM.map(b => b.ten), ['Thường', 'Hiếm', 'Quý', 'Sử thi', 'Huyền thoại', 'Trang trí'])
  assert.equal(BAC_HIEM.filter(b => b.vienDong).map(b => b.ma).join(), 'huyen_thoai')
  const sac = (h: string) => {
    const v = Number.parseInt(h.slice(1), 16)
    const r = (v >> 16) & 255, g = (v >> 8) & 255, b = v & 255
    return { r, g, b }
  }
  const hiem = sac(bacHiem(1).mau), quy = sac(bacHiem(2).mau), su = sac(bacHiem(3).mau), ht = sac(bacHiem(4).mau)
  assert.ok(hiem.g > hiem.r && hiem.g > hiem.b, 'Hiếm xanh lá')
  assert.ok(quy.b > quy.r && quy.b > quy.g, 'Quý xanh dương')
  assert.ok(su.b > su.g && su.r > su.g, 'Sử thi tím')
  assert.ok(ht.r > ht.g && ht.g > ht.b, 'Huyền thoại cam')
})

test('ánh xạ theo giá trị thật: Xanh 110% = Hiếm, Đỏ 120% = Quý, Hoàn mỹ = Sử thi; danh hiệu Hiếm = xanh lá như đồ', () => {
  assert.equal(bacVatPham(3).ten, 'Hiếm')
  assert.equal(bacVatPham(2).ten, 'Quý')
  assert.equal(bacVatPham(1).ten, 'Sử thi')
  assert.equal(bacVatPham(99).ten, 'Thường')
  assert.equal(bacDanhHieu(1).mau, bacVatPham(3).mau)
  assert.equal(bacDanhHieu(3).ten, 'Huyền thoại')
})

test('xếp hạng tô chip danh hiệu bằng màu độ hiếm chung', () => {
  const v = readFileSync(new URL('../app/pages/ranking.vue', import.meta.url), 'utf8')
  assert.match(v, /bacDanhHieu\(d\.danhHieu\.doHiem\)/)
  const b = readFileSync(new URL('../app/components/site/RankBoard.vue', import.meta.url), 'utf8')
  assert.match(b, /r\.danhHieu\.mau/)
})

const congDong: SuKienLan = {
  ma: 'moi:congdong', nam: 2026, lan: 10, ten: 'Ngày cộng đồng', nhom: 'Ngày cộng đồng', ngayLe: 'Chủ nhật hằng tháng',
  moTa: 'Dòng Venipede xuất hiện dày (40% lần hồi sinh) trên các tuyến của nó trong giờ vàng 14:00–17:00; hạ Venipede +20% EXP, shiny x5.',
  phanThuong: 'Danh hiệu khi hạ 30 Venipede trong giờ vàng; shiny x5; +20% EXP',
  mo: '2026-11-15 04:00:00', dong: '2026-11-16 04:00:00', ghiDe: 0,
}

test('Ngày cộng đồng: báo trước 3 ngày như lễ hội, có tin kết thúc, tin nêu dòng nổi bật và giờ vàng', () => {
  assert.equal(laNgayCongDong(congDong), true)
  assert.equal(laTuanPokemon(congDong), false)
  assert.equal(giaiDoan(congDong, msTuChuoi('2026-11-12 04:00:00')), 'sap')
  assert.equal(giaiDoan(congDong, msTuChuoi('2026-11-15 12:00:00')), 'dang')
  assert.equal(tinCanDang(congDong, msTuChuoi('2026-11-16 05:00:00'))?.kieu, 'ket')
  const tin = vietTin(congDong, 'sap')
  assert.match(tin.noiDung, /Venipede/)
  assert.match(tin.noiDung, /14:00–17:00/)
  assert.match(tin.the, /ngày cộng đồng/)
  assert.equal(tin.slug, 'su-kien-moi-congdong-2026-10-sap')
})

test('cẩm nang sự kiện có mục Ngày cộng đồng, lịch lễ hội không lẫn 12 ngày cộng đồng', () => {
  const v = readFileSync(new URL('../app/pages/cam-nang/su-kien.vue', import.meta.url), 'utf8')
  assert.match(v, /id="ngay-cong-dong"/)
  assert.match(v, /!laNgayCongDong\(l\)/)
  const h = readFileSync(new URL('../app/pages/cam-nang/he-sinh-thai.vue', import.meta.url), 'utf8')
  assert.match(h, /Gió mạnh/)
  assert.match(h, /4 khung 6 giờ/)
})
