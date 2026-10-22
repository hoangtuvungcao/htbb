/** Pokédex đầy đủ 1.025 loài — phần thuần (shared/utils/loai.ts, loaiDuLieu.ts, goiSprite.ts). Chạy: npm test. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, readdirSync } from 'node:fs'
import {
  bacLoai, docSoDex, dungChuoiTienHoa, heCuaLoai, locLoai, moTaTienHoa, phanTramChiSo, tongChiSo,
  type CanhTienHoa, type LoaiTomTat, type ThanhVienHo,
} from '../shared/utils/loai.ts'
import { ketQuaChiTietLoai, laThieuBang, taiDanhSachLoai, type Truy } from '../shared/utils/loaiDuLieu.ts'
import { docGoiSprite, tenChanDung } from '../shared/utils/goiSprite.ts'

const tv = (dex: number, tu = 0, giaiDoan = 1): ThanhVienHo => ({ dex, ten: `L${dex}`, he: [0], giaiDoan, bat: true, tienHoaTu: tu })
const c = (tu: number, sang: number, coChe = 'cap', cap = 16, dieuKien = ''): CanhTienHoa =>
  ({ tu, sang, kieu: coChe === 'cap' ? 'cap' : 'da', coChe, cap, tenVatPham: '', ngayDem: 0, keo: 25, dieuKien })

test('số Pokédex: chỉ nguyên 1..1025', () => {
  assert.equal(docSoDex('25'), 25)
  assert.equal(docSoDex(['133']), 133)
  assert.equal(docSoDex('1025'), 1025)
  for (const x of ['0', '1026', '-1', '2.5', 'abc', '', '25a', ' ', '99999', undefined, null]) assert.equal(docSoDex(x), null, String(x))
})

test('hệ: he2 = -1 là đơn hệ, trùng he1 bỏ', () => {
  assert.deepEqual(heCuaLoai(4, 7), [4, 7])
  assert.deepEqual(heCuaLoai(0, -1), [0])
  assert.deepEqual(heCuaLoai(3, 3), [3])
})

test('chuỗi tiến hoá: Eevee 133 -> 8 nhánh cùng tầng', () => {
  const nhanh = [134, 135, 136, 196, 197, 470, 471, 700]
  const t = dungChuoiTienHoa([tv(133), ...nhanh.map(d => tv(d, 133, 2))], nhanh.map(d => c(133, d, 'vat_pham', 20)), 133)
  assert.equal(t.length, 2)
  assert.deepEqual(t[0]!.map(n => n.dex), [133])
  assert.deepEqual(t[1]!.map(n => n.dex), nhanh)
  assert.ok(t[1]!.every(n => n.vao?.tu === 133))
})

test('chuỗi tiến hoá: Tyrogue 236 -> 106 / 107 / 237; Oddish 43 -> 44 -> 45 / 182', () => {
  const t = dungChuoiTienHoa([tv(236, 0, 0), tv(106, 236), tv(107, 236), tv(237, 236)],
    [c(236, 237, 'keo'), c(236, 106, 'keo'), c(236, 107, 'keo')], 236)
  assert.deepEqual(t.map(x => x.map(n => n.dex)), [[236], [106, 107, 237]])
  const o = dungChuoiTienHoa([tv(43), tv(44, 43, 2), tv(45, 44, 3), tv(182, 44, 3)],
    [c(43, 44, 'cap', 21), c(44, 45, 'vat_pham', 36, 'Dùng Đá Lá'), c(44, 182, 'vat_pham', 36, 'Dùng Đá Mặt Trời')], 43)
  assert.deepEqual(o.map(x => x.map(n => n.dex)), [[43], [44], [45, 182]])
  assert.equal(o[2]![1]!.vao?.dieuKien, 'Dùng Đá Mặt Trời')
})

test('chuỗi tiến hoá: cạnh ra ngoài họ bị bỏ, vòng không treo', () => {
  const t = dungChuoiTienHoa([tv(1), tv(2, 1, 2)], [c(1, 2), c(2, 1), c(2, 999)], 1)
  assert.deepEqual(t.map(x => x.map(n => n.dex)), [[1], [2]])
  assert.deepEqual(dungChuoiTienHoa([tv(131)], [], 131).map(x => x.length), [1])
})

test('mô tả tiến hoá: điều kiện gốc + cách trong game', () => {
  assert.equal(moTaTienHoa(c(1, 2, 'cap', 16, 'Cấp 16')), 'Cấp 16')
  assert.equal(moTaTienHoa(c(1, 2, 'cap', 16)), 'Cấp 16')
  assert.match(moTaTienHoa(c(64, 65, 'trao_doi', 36, 'Trao đổi')), /Trao đổi · trong game: từ cấp 36, 25 Kẹo/)
})

test('bậc, chỉ số, lọc', () => {
  assert.equal(bacLoai(4).ma, 'T4')
  assert.equal(bacLoai(99).ma, 'T1')
  assert.equal(tongChiSo({ hp: 45, tanCong: 49, phongThu: 49, tanCongDb: 65, phongThuDb: 65, tocDo: 45 }), 318)
  assert.equal(phanTramChiSo(255), 100)
  assert.equal(phanTramChiSo(0), 2)
  const ds: LoaiTomTat[] = [
    { dex: 1, ten: 'Bulbasaur', he: [4, 7], bac: 4, theHe: 1, bat: true, nhom: 0 },
    { dex: 25, ten: 'Pikachu', he: [3], bac: 1, theHe: 1, bat: true, nhom: 0 },
    { dex: 252, ten: 'Treecko', he: [4], bac: 4, theHe: 3, bat: false, nhom: 0 },
  ]
  const L = (o: Partial<Parameters<typeof locLoai>[1]>) => locLoai(ds, { tim: '', he: -1, theHe: 0, bac: 0, trangThai: 'tat-ca', ...o }).map(x => x.dex)
  assert.deepEqual(L({ he: 4 }), [1, 252])
  assert.deepEqual(L({ trangThai: 'chua-mo' }), [252])
  assert.deepEqual(L({ trangThai: 'da-mo', bac: 4 }), [1])
  assert.deepEqual(L({ theHe: 3 }), [252])
  assert.deepEqual(L({ tim: 'pika' }), [25])
  assert.deepEqual(L({ tim: '#025' }), [25])
})

/** CSDL giả: trả dòng theo bảng trong câu SQL. */
function truyGia(bang: Record<string, Record<string, any>[]>, thieu: string[] = []): Truy & { cau: string[] } {
  const cau: string[] = []
  const f = (async (sql: string, ts: unknown[] = []) => {
    cau.push(sql)
    for (const t of thieu) {
      if (sql.includes(t)) throw Object.assign(new Error(`Table 'htbb_db.${t}' doesn't exist`), { code: 'ER_NO_SUCH_TABLE', errno: 1146 })
    }
    assert.ok(!/\$\{|'\d/.test(sql), 'SQL phải dùng tham số ?')
    const m = /FROM (\w+)/.exec(sql)!
    const rows = bang[m[1]!] ?? []
    if (m[1] === 'data_loai' && /WHERE dex = \?/.test(sql)) return rows.filter(r => r.dex === ts[0])
    if (m[1] === 'data_loai' && /dex_goc = \?/.test(sql)) return rows.filter(r => r.dex_goc === ts[0] || r.dex === ts[1])
    if (m[1] === 'data_loai_tien_hoa') return rows.filter(r => ts.includes(r.tu_dex))
    return rows
  }) as Truy & { cau: string[] }
  f.cau = cau
  return f
}

const dongLoai = (dex: number, goc: number, tu = 0) => ({
  dex, ten: `L${dex}`, he1: 4, he2: -1, hp: 45, tan_cong: 49, phong_thu: 49, tan_cong_db: 65, phong_thu_db: 65, toc_do: 45,
  tong_chi_so: 318, nhom: 0, dex_goc: goc, tien_hoa_tu: tu, giai_doan: 1, bac: 4, goc_bat: 60, bat_duoc: 1, chi_su_kien: 0,
  nhom_trung1: 1, nhom_trung2: 7, moi_truong: 'dong_co', ti_le_cai: 1, chieu_cao: 7, can_nang: 69, the_he: 1, bat: 1, mo_ta: '',
})

test('API chi tiết: dex sai -> 400 (không chạm CSDL); không có -> 404; thiếu bảng -> 404', async () => {
  const truy = truyGia({})
  for (const x of ['0', '1026', 'abc', '1;DROP']) assert.equal((await ketQuaChiTietLoai(x, truy)).ma, 400)
  assert.equal(truy.cau.length, 0)
  assert.equal((await ketQuaChiTietLoai('5', truy)).ma, 404)
  const kq = await ketQuaChiTietLoai('5', truyGia({}, ['data_loai']))
  assert.equal(kq.ma, 404)
  assert.match(kq.thongBao!, /chưa có dữ liệu/)
})

test('API chi tiết: ghép họ, chiêu, vùng, nhóm trứng, sprite', async () => {
  const truy = truyGia({
    data_loai: [dongLoai(1, 1), dongLoai(2, 1, 1), dongLoai(3, 1, 2)],
    data_loai_tien_hoa: [{ tu_dex: 1, sang_dex: 2, kieu: 'cap', co_che: 'cap', cap: 16, ten_vat_pham: '', ngay_dem: 0, keo: 50, dieu_kien: 'Cấp 16' },
      { tu_dex: 2, sang_dex: 3, kieu: 'cap', co_che: 'cap', cap: 32, ten_vat_pham: '', ngay_dem: 0, keo: 100, dieu_kien: 'Cấp 32' }],
    data_loai_chieu: [{ o: 0, ma_chieu: 'grass.xa', vai: 'stab', cap_hoc: 1, ten: 'Hút năng lượng', he: 4, kieu: 'dac_biet', vai_tro: 'xa' }],
    data_loai_vung: [{ khu_vuc: 'Kanto', vung: 2, cap_min: 30, cap_max: 52, trong_so: 100, ngay_dem: 0, thoi_tiet: -1, bac_gap: 4 }],
    data_nhom_trung_ten: [{ id: 1, ten: 'Quái vật' }, { id: 7, ten: 'Thực vật' }],
    data_loai_sprite: [{ trang_thai: 2, chan_dung: 1, trong_jar: 1, goi_may_chu: 1, ghi_cong: 'Ai đó' }],
  })
  const kq = await ketQuaChiTietLoai('2', truy)
  assert.equal(kq.ma, 200)
  const d = kq.duLieu!
  assert.equal(d.loai.dex, 2)
  assert.equal(d.loai.dexGoc, 1)
  assert.deepEqual(d.loai.nhomTrung, ['Quái vật', 'Thực vật'])
  assert.deepEqual(d.ho.thanhVien.map(t => t.dex), [1, 2, 3])
  assert.equal(d.ho.canh.length, 2)
  assert.deepEqual(dungChuoiTienHoa(d.ho.thanhVien, d.ho.canh, d.loai.dexGoc).map(t => t.map(n => n.dex)), [[1], [2], [3]])
  assert.equal(d.chieu[0]!.ten, 'Hút năng lượng')
  assert.equal(d.vung[0]!.khuVuc, 'Kanto')
  assert.equal(d.sprite?.ghiCong, 'Ai đó')
  // bảng phụ chưa có (CSDL cũ) vẫn trả được loài
  const kq2 = await ketQuaChiTietLoai('2', truyGia({ data_loai: [dongLoai(2, 2)] }, ['data_loai_sprite', 'data_nhom_trung_ten']))
  assert.equal(kq2.ma, 200)
  assert.equal(kq2.duLieu!.sprite, null)
  assert.deepEqual(kq2.duLieu!.loai.nhomTrung, ['Nhóm 1', 'Nhóm 7'])
})

test('API danh sách: ánh xạ dòng', async () => {
  const ds = await taiDanhSachLoai(truyGia({ data_loai: [dongLoai(1, 1), { ...dongLoai(252, 252), he2: 7, bat: 0, the_he: 3 }] }))
  assert.deepEqual(ds[1], { dex: 252, ten: 'L252', he: [4, 7], bac: 4, theHe: 3, bat: false, nhom: 0 })
  assert.ok(laThieuBang({ code: 'ER_NO_SUCH_TABLE' }))
  assert.ok(!laThieuBang({ code: 'ECONNREFUSED' }))
})

function goi(dex: number, tep: [string, Uint8Array][]): Uint8Array {
  const phan: number[] = [0x48, 0x54, 0x50, 0x4b, 1, (dex >> 8) & 255, dex & 255, 0, tep.length]
  for (const [ten, b] of tep) {
    const t = new TextEncoder().encode(ten)
    phan.push(t.length >> 8, t.length & 255, ...t, 0, 0, (b.length >> 8) & 255, b.length & 255, ...b)
  }
  return new Uint8Array(phan)
}

test('gói sprite HTPK: đọc đúng, từ chối gói hỏng', () => {
  const g = docGoiSprite(goi(16, [['0016.dl', new Uint8Array([1, 2])], [tenChanDung(16), new Uint8Array([0x89, 0x50])]]))
  assert.equal(g.dex, 16)
  assert.equal(g.shiny, false)
  assert.deepEqual([...g.tep.keys()], ['0016.dl', '0016_c.png'])
  assert.deepEqual([...g.tep.get('0016_c.png')!], [0x89, 0x50])
  const ok = goi(1, [['0001.dl', new Uint8Array([9])]])
  assert.throws(() => docGoiSprite(ok.subarray(0, ok.length - 1)), /cụt/)
  const sai = ok.slice()
  sai[0] = 0
  assert.throws(() => docGoiSprite(sai), /HTPK/)
  const pb = ok.slice()
  pb[4] = 2
  assert.throws(() => docGoiSprite(pb), /phiên bản/)
  assert.throws(() => docGoiSprite(new Uint8Array([...ok, 0])), /thừa/)
})

test('gói sprite thật trong repo (server/map/loai): đọc được, có chân dung PNG', () => {
  const thuMuc = new URL('../../server/map/loai/', import.meta.url)
  let tep: string[] = []
  try {
    tep = readdirSync(thuMuc).filter(f => /^\d{4}\.goi$/.test(f))
  }
  catch { return }
  for (const f of tep.slice(0, 40)) {
    const g = docGoiSprite(new Uint8Array(readFileSync(new URL(f, thuMuc))))
    assert.equal(g.dex, Number(f.slice(0, 4)))
    const p = g.tep.get(tenChanDung(g.dex))
    assert.ok(p && p[0] === 0x89 && p[1] === 0x50, f)
  }
})
