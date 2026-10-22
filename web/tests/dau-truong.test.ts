/** Đấu trường Bảo Bối 3v3 (D12): số liệu web khớp máy chủ DauTruongLogic, xếp bảng tuần / mùa, cẩm nang có trang. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { DAU_TRUONG, bacDauTruong, diemCong, dongMuaTu, dongTuanTu, tenBacDauTruong, xepMua, xepTuan } from '../shared/utils/dauTruong.ts'
import { DS_CAM_NANG } from '../shared/utils/danhMucCamNang.ts'

const java = (f: string) => readFileSync(new URL(`../../server/src/com/htbb/pokemon/${f}`, import.meta.url), 'utf8')
const hang = (src: string, ten: string) => Number(new RegExp(`\\b${ten} = (\\d+)`).exec(src)?.[1])

test('hằng số khớp com.htbb.pokemon.DauTruongLogic', () => {
  const s = java('DauTruongLogic.java')
  assert.equal(DAU_TRUONG.diemBanDau, hang(s, 'DIEM_BAN_DAU'))
  assert.equal(DAU_TRUONG.k, hang(s, 'K'))
  assert.equal(DAU_TRUONG.luotNgay, hang(s, 'LUOT_NGAY'))
  assert.equal(DAU_TRUONG.lamMoiNgay, hang(s, 'LAM_MOI_NGAY'))
  assert.equal(DAU_TRUONG.luongThang, hang(s, 'LUONG_THANG'))
  assert.equal(DAU_TRUONG.tranLuongTuan, hang(s, 'TRAN_LUONG_TUAN'))
  assert.equal(DAU_TRUONG.tranToiThieuMua, hang(s, 'TRAN_TOI_THIEU_MUA'))
  assert.equal(DAU_TRUONG.giayLuot, hang(s, 'GIAY_LUOT'))
  assert.equal(DAU_TRUONG.phanTramThu, hang(s, 'PHAN_TRAM_THU'))
  assert.ok(s.includes(`NGUONG_BAC = {${DAU_TRUONG.nguongBac.join(', ')}}`))
  for (const d of DAU_TRUONG.danhHieu) assert.ok(s.includes(String(d.id)), `danh hieu ${d.id}`)
  const t = java('DauTruongTran.java')
  assert.equal(DAU_TRUONG.luotToiDa, hang(t, 'LUOT_TOI_DA'))
  assert.equal(DAU_TRUONG.capChuanHoa, hang(t, 'CAP'))
  const du = java('DauTruongDuLieu.java')
  assert.ok(du.includes(`PP = {${DAU_TRUONG.vaiTro.map(v => v.pp).join(', ')}}`))
  assert.ok(du.includes(`CHINH_XAC = {${DAU_TRUONG.vaiTro.map(v => v.chinhXac).join(', ')}}`))
})

test('điểm Elo giống máy chủ (KiemDauTruong)', () => {
  assert.equal(diemCong(1000, 1000, 1), 1016)
  assert.equal(diemCong(1000, 1000, 0), 984)
  assert.equal(diemCong(1000, 1000, 1, 500), 1008)
  assert.equal(diemCong(1000, 1000, 1, 0), 1000)
  assert.equal(diemCong(1000, 1400, 1), 1029)
  assert.equal(diemCong(110, 110, 0), 100)
})

test('bậc theo điểm', () => {
  assert.deepEqual([800, 950, 1150, 1300, 1450, 1600].map(bacDauTruong), [0, 1, 2, 3, 4, 5])
  assert.equal(tenBacDauTruong(1200), 'Bạc')
})

test('xếp bảng mùa / tuần', () => {
  const m = xepMua([
    dongMuaTu({ id_char: 3, ten: 'C', diem: 1100, thang: 2, thua: 0, so_tran: 2 }),
    dongMuaTu({ id_char: 1, ten: 'A', diem: 1100, thang: 1, thua: 0, so_tran: 1 }),
    dongMuaTu({ id_char: 2, ten: '', diem: 1200, thang: 3, thua: 0, so_tran: 3 }),
    dongMuaTu({ id_char: 4, ten: 'D', diem: 1500, thang: 0, thua: 0, so_tran: 0 }),
  ])
  assert.deepEqual(m.map(d => d.idChar), [2, 1, 3])
  assert.equal(m[0]!.ten, '#2')
  const t = xepTuan([
    dongTuanTu({ cong: 5, ten: 'E', tran: 5, thang: 3, diem_doi: 20, luong: 9 }),
    dongTuanTu({ cong: 6, ten: 'F', tran: 3, thang: 3, diem_doi: 20, luong: 9 }),
    dongTuanTu({ cong: 7, ten: 'G', tran: 2, thang: 0, diem_doi: -30, luong: 0 }),
  ])
  assert.deepEqual(t.map(d => d.idChar), [6, 5, 7])
})

test('cẩm nang Đấu trường có trang + mục khớp WikiSection', () => {
  const p = DS_CAM_NANG.find(x => x.slug === 'dau-truong')
  assert.ok(p)
  const vue = readFileSync(new URL('../app/pages/cam-nang/dau-truong.vue', import.meta.url), 'utf8')
  for (const m of p!.muc ?? []) assert.ok(vue.includes(`id="${m.id}"`), m.id)
  const trang = readFileSync(new URL('../app/pages/dau-truong.vue', import.meta.url), 'utf8')
  assert.ok(trang.includes("'/api/dau-truong'"))
  const api = readFileSync(new URL('../server/api/dau-truong.get.ts', import.meta.url), 'utf8')
  assert.ok(!/\b(may|ip|user_id)\b/.test(api.replace(/\/\*\*[\s\S]*?\*\//, '')), 'API khong tra thiet bi / IP / tai khoan')
})
