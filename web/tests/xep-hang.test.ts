/** Bảng xếp hạng — phần thuần (shared/utils/xepHangThuan.ts). Chạy: npm test. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import {
  XH_CAI_DAT_MAC_DINH, XH_DANH_MUC, xhBacPvp, xhBang, xhBangDuocHien, xhCapKyNang, xhChonBoLoc, xhChuanHoaCaiDat,
  xhDauTuanGame, xhDocSinhThai, xhGopDem, xhKhopLuatTen, xhLuatTen, xhMuaPvp, xhSoHuyHieu, xhSqlLoaiTru, xhTachMau,
  xhTenGiaoThe, xhVungTuInfotop,
} from '../shared/utils/xepHangThuan.ts'

test('danh mục: mã duy nhất, mỗi bảng có bảng CSDL', () => {
  const ma = XH_DANH_MUC.map(b => b.ma)
  assert.equal(new Set(ma).size, ma.length)
  for (const b of XH_DANH_MUC) assert.ok(b.bang.length > 0, b.ma)
})

test('không còn bảng "Top ủng hộ" (board_created là hàng chờ quà tạo nhân vật, không phải ủng hộ)', () => {
  assert.equal(XH_DANH_MUC.some(b => (b.ma as string) === 'ung-ho'), false)
  assert.equal(XH_DANH_MUC.some(b => b.bang.includes('board_created')), false)
  const cd = xhChuanHoaCaiDat({ hienUngHo: true, bang: [{ ma: 'ung-ho', hien: true }] })
  assert.equal(cd.bang.some(b => (b.ma as string) === 'ung-ho'), false)
  assert.equal('hienUngHo' in cd, false)
  for (const b of cd.bang) assert.equal(xhBangDuocHien(cd, b.ma), b.hien)
})

test('chuẩn hoá cài đặt: dữ liệu hỏng không làm vỡ, số bị kẹp, bảng thiếu thêm cuối', () => {
  assert.deepEqual(xhChuanHoaCaiDat(null), XH_CAI_DAT_MAC_DINH)
  const cd = xhChuanHoaCaiDat({ soDong: 99999, moiTrang: -3, lamMoiGiay: 'x', bang: [{ ma: 'pvp', hien: false }, { ma: 'pvp' }, { ma: 'la' }], mocChiemGym: 'bậy' })
  assert.equal(cd.soDong, 200)
  assert.equal(cd.moiTrang, 5)
  assert.equal(cd.lamMoiGiay, 60)
  assert.equal(cd.bang[0]!.ma, 'pvp')
  assert.equal(cd.bang[0]!.hien, false)
  assert.equal(cd.bang.length, XH_DANH_MUC.length)
  assert.equal(cd.mocChiemGym, XH_CAI_DAT_MAC_DINH.mocChiemGym)
})

test('mẫu loại trừ: hợp lệ, tiền tố, báo mẫu sai', () => {
  const r = xhTachMau('Tai0*\nbot_1, bot_1;  \n*\nxấu tên')
  assert.deepEqual(r.mau, ['tai0*', 'bot_1'])
  assert.deepEqual(r.loi, ['*', 'xấu tên'])
  const l = xhLuatTen(r.mau)
  assert.ok(xhKhopLuatTen('TAI007', l))
  assert.ok(xhKhopLuatTen('bot_1', l))
  assert.ok(!xhKhopLuatTen('bot_12', l))
})

test('SQL loại trừ: luôn có ban = 0, tham số hoá tên, id là số', () => {
  const s = xhSqlLoaiTru('a', xhLuatTen(['gm', 'tai0*']), [3, 5, Number.NaN])
  assert.match(s.sql, /^a\.ban = 0/)
  assert.match(s.sql, /LOWER\(a\.charname\) NOT IN \(\?\)/)
  assert.match(s.sql, /SUBSTR\(LOWER\(a\.charname\), 1, 4\) <> \?/)
  assert.match(s.sql, /a\.userId NOT IN \(3,5\)/)
  assert.deepEqual(s.params, ['gm', 'tai0'])
})

test('công thức chép từ game', () => {
  assert.equal(xhBacPvp(1199), 0)
  assert.equal(xhBacPvp(1200), 1)
  assert.equal(xhBacPvp(1600), 3)
  const kt = new Date(2026, 0, 1).getTime()
  assert.equal(xhMuaPvp(kt, kt + 90 * 86_400_000), 0)
  assert.equal(xhMuaPvp(kt, kt + 91 * 86_400_000), 1)
  assert.equal(xhMuaPvp(0, Date.now()), 0)
  assert.equal(xhSoHuyHieu('a=1;gh=7;b=2'), 3)
  assert.equal(xhSoHuyHieu(''), 0)
  assert.equal(xhCapKyNang(0), 1)
  assert.equal(xhCapKyNang(44), 2) // round(40 × 1,105) = 44
  assert.equal(xhCapKyNang(1e12), 50)
  assert.deepEqual(xhDocSinhThai('v1|kn=10,0,5|al=a~b~c'), { exp: [10, 0, 5, 0, 0], album: 3 })
  assert.deepEqual(xhDocSinhThai('v2|kn=1'), { exp: [0, 0, 0, 0, 0], album: 0 })
  assert.equal(xhVungTuInfotop('2,5'), 2)
  assert.equal(xhVungTuInfotop('-1'), -1)
  assert.equal(xhTenGiaoThe('ash_12'), 'ash')
  assert.equal(xhTenGiaoThe('ash_ketchum'), 'ash_ketchum')
})

test('gộp đếm: số lần giảm dần, ai đạt trước đứng trước', () => {
  const r = xhGopDem([
    { ten: 'b_1', so: 2, cuoi: '2026-01-02', idCuoi: 5 },
    { ten: 'a_2', so: 3, cuoi: '2026-01-03', idCuoi: 6 },
    { ten: 'b_3', so: 1, cuoi: '2026-01-01', idCuoi: 1 },
  ], xhTenGiaoThe)
  assert.deepEqual(r.map(x => [x.ten, x.so]), [['b', 3], ['a', 3]])
})

test('đầu tuần game: thứ Hai 05:00', () => {
  const d = xhDauTuanGame(new Date(2026, 9, 18, 12)) // CN 18/10/2026
  assert.equal(d.getDay(), 1)
  assert.equal(d.getDate(), 12)
  assert.equal(d.getHours(), 5)
  // Thứ Hai 04:00 vẫn thuộc tuần trước
  assert.equal(xhDauTuanGame(new Date(2026, 9, 19, 4)).getDate(), 12)
})

test('bộ lọc: giá trị lạ về mặc định, mùa chỉ nhận số', () => {
  assert.deepEqual(xhChonBoLoc(xhBang('pokedex')!, { kieu: 'DROP TABLE' }), { kieu: 'da-ha' })
  assert.deepEqual(xhChonBoLoc(xhBang('pvp')!, { mua: '3' }), { mua: '3' })
  assert.deepEqual(xhChonBoLoc(xhBang('pvp')!, { mua: '1;--' }), { mua: '' })
})
