/** Cúp PvP tuần (0.9.7): tuần cúp thứ Hai 05:00 giờ VN khớp server CupLogic, gộp đội / xếp hạng, tin tự động. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { dongCupTuDong, gopDoi, moTuanCup, tuanCup } from '../shared/utils/cupPvp.ts'
import { msTuChuoi, tinCanDang, vietTin, type SuKienLan } from '../shared/utils/suKienNam.ts'

const t = (s: string) => msTuChuoi(s)

test('tuần cúp đổi đúng thứ Hai 05:00 giờ Việt Nam (mốc tuần chung)', () => {
  const w = tuanCup(t('2026-10-19 05:00:00'))
  assert.equal(moTuanCup(w), t('2026-10-19 05:00:00'))
  assert.equal(tuanCup(t('2026-10-19 04:59:59')), w - 1)
  assert.equal(tuanCup(t('2026-10-19 04:00:00')), w - 1) // trước 0.9.14 đây đã là tuần mới
  assert.equal(tuanCup(t('2026-10-25 23:59:00')), w)
  assert.equal(tuanCup(t('2026-10-26 04:59:00')), w)
  assert.equal(tuanCup(t('2026-10-26 05:00:00')), w + 1)
  // tuần 0 = thứ Hai 05/01/1970 05:00 (cùng mốc server)
  assert.equal(moTuanCup(0), t('1970-01-05 05:00:00'))
})

test('gộp Cúp Đôi theo doi_id, xếp hạng chốt trước rồi điểm', () => {
  const ds = [
    dongCupTuDong({ char_id: 1, doi_id: 1, ten: 'Ash', cap: 30, diem: 6, thang: 2, hoa: 0, thua: 1, hang: 0 }),
    dongCupTuDong({ char_id: 2, doi_id: 1, ten: 'Pikachu', cap: 31, diem: 6, thang: 2, hoa: 0, thua: 1, hang: 0 }),
    dongCupTuDong({ char_id: 3, doi_id: 3, ten: 'Misty', cap: 28, diem: 9, thang: 3, hoa: 0, thua: 0, hang: 0 }),
  ]
  const g = gopDoi(ds)
  assert.equal(g.length, 2)
  assert.equal(g[0]!.ten, 'Misty')
  assert.equal(g[1]!.ten, 'Ash & Pikachu')
  assert.equal(g[1]!.cap, 31)
  const chot = gopDoi(ds.map(d => ({ ...d, hang: d.doiId === 1 ? 1 : 2 })))
  assert.equal(chot[0]!.doiId, 1)
})

test('tin cúp: báo trước 1 ngày, có link /cup, có tin kết thúc', () => {
  const cup: SuKienLan = { ma: 'moi:cuptuan', nam: 2026, lan: 44, ten: 'Cúp Nhí (tuần 44)', nhom: 'Cúp PvP', ngayLe: 'mỗi tuần',
    moTa: 'Cúp cho Pokémon cấp 20–30.', phanThuong: 'Vô địch: danh hiệu 28 ngày', mo: '2026-10-26 04:00:00', dong: '2026-11-02 04:00:00', ghiDe: 0 }
  assert.equal(tinCanDang(cup, t('2026-10-24 12:00:00')), null)
  assert.equal(tinCanDang(cup, t('2026-10-25 05:00:00'))?.kieu, 'sap')
  assert.equal(tinCanDang(cup, t('2026-10-26 04:00:00'))?.kieu, 'dang')
  assert.equal(tinCanDang(cup, t('2026-11-02 05:00:00'))?.kieu, 'ket')
  const tin = vietTin(cup, 'ket')
  assert.ok(tin.noiDung.includes('/cup'))
  assert.ok(tin.the.includes('cúp PvP'))
  assert.equal(tin.slug, 'su-kien-moi-cuptuan-2026-44-ket')
})
