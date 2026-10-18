/** Luật cảnh báo kinh tế + đọc log xu (shared/utils/kinhTeCanhBao.ts). */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { chiSoDanXuat, docLogXu, dongTruoc, nhanXuTheoNhanVat, phanTramDoi, timDotBien, tinhCanhBaoKinhTe, tongHopNguon, type NgayKinhTe } from '../shared/utils/kinhTeCanhBao.ts'

const ngay = (n: string, tongXu: number, them: Partial<NgayKinhTe> = {}): NgayKinhTe => ({
  ngay: n, soNguoi: 100, capTrungVi: 30, chiSoGia: 1000, tongXu, tongLuong: 1000, tongLuongKhoa: 0,
  cungTienThuc: 5, top1: 10, xuVao: 0, xuRa: 0, muc: 0, ...them,
})

test('đọc log nhanxu / truxu', () => {
  assert.deepEqual(docLogXu('100000 + 40000000 = 40100000 tu vi tri board_created db'), { truoc: 100000, delta: 40000000, sau: 40100000, nguon: 'board_created db' })
  assert.deepEqual(docLogXu('500 - 200 = 300 tu vi tri mua npc 12'), { truoc: 500, delta: -200, sau: 300, nguon: 'mua npc #' })
  assert.equal(docLogXu('40100000_0_0'), null)
  assert.deepEqual(tongHopNguon(['1 + 5 = 6 tu vi tri quai 3', '1 + 7 = 8 tu vi tri quai 9', '1 + 100 = 101 tu vi tri nap', 'rac']),
    [{ nguon: 'nap', soLan: 1, tong: 100 }, { nguon: 'quai #', soLan: 2, tong: 12 }])
})

test('phần trăm đổi và dòng 7 ngày trước', () => {
  assert.equal(phanTramDoi(100, 130), 30)
  assert.equal(phanTramDoi(0, 5), null)
  const ds = [ngay('2026-10-01', 100), ngay('2026-10-05', 120), ngay('2026-10-08', 150), ngay('2026-10-12', 200)]
  assert.equal(dongTruoc(ds, 7)?.ngay, '2026-10-05')
  assert.equal(dongTruoc(ds, 30), null)
  const c = chiSoDanXuat(ds)
  assert.equal(c.doiXu7, phanTramDoi(120, 200))
  assert.equal(c.xuMoiNguoi, 2)
})

test('luật cảnh báo', () => {
  const ds = [ngay('2026-10-01', 1000), ngay('2026-10-08', 1700, { xuVao: 200, xuRa: 20, top1: 65, muc: 2, cungTienThuc: 25 })]
  const cb = tinhCanhBaoKinhTe(ds, [], '2026-10-08')
  const ma = Object.fromEntries(cb.map(c => [c.ma, c.muc]))
  assert.equal(ma.muc_game, 'nghiem_trong')
  assert.equal(ma.xu_rong, 'nghiem_trong') // 180/1700 = 10,6%
  assert.equal(ma.top1, 'nghiem_trong')
  assert.equal(ma.cung_xu_7, 'nghiem_trong') // +70%
  assert.equal(ma.du_lieu_cu, undefined)
  assert.equal(cb[0]!.muc, 'nghiem_trong')
  // ổn định: không cảnh báo
  assert.deepEqual(tinhCanhBaoKinhTe([ngay('2026-10-01', 1000), ngay('2026-10-08', 1010)], [], '2026-10-08'), [])
  // dữ liệu cũ
  assert.equal(tinhCanhBaoKinhTe([ngay('2026-10-01', 1000)], [], '2026-10-08')[0]!.ma, 'du_lieu_cu')
  assert.equal(tinhCanhBaoKinhTe([], [], '2026-10-08')[0]!.muc, 'thong_tin')
})

test('đột biến một nhân vật', () => {
  const dong = [
    { ten: 'a', info: '0 + 30000 = 30000 tu vi tri x', t: -100 },      // 30 giờ ở giá 1000 -> cảnh báo
    { ten: 'b', info: '0 + 200000 = 200000 tu vi tri x', t: -100 },    // 200 giờ -> nghiêm trọng
    { ten: 'c', info: '0 + 12000 = 12000 tu vi tri x', t: -100 },      // 12 giờ, 24h trước 1000 -> gấp 12 lần
    { ten: 'c', info: '0 + 1000 = 1000 tu vi tri x', t: -90000 },
    { ten: 'd', info: '0 + 12000 = 12000 tu vi tri x', t: -100 },      // 12 giờ nhưng 24h trước cũng 10000 -> không
    { ten: 'd', info: '0 + 10000 = 10000 tu vi tri x', t: -90000 },
    { ten: 'e', info: '9 - 5 = 4 tu vi tri x', t: -100 },
    { ten: 'f', info: '0 + 999999 = 999999 tu vi tri x', t: -200000 }, // ngoài 48 giờ
  ]
  const ds = nhanXuTheoNhanVat(dong, 0)
  assert.deepEqual(ds.map(x => x.ten), ['b', 'a', 'c', 'd'])
  const db = Object.fromEntries(timDotBien(ds, 1000).map(x => [x.ten, x.muc]))
  assert.deepEqual(db, { b: 'nghiem_trong', a: 'canh_bao', c: 'canh_bao' })
})
