/**
 * Danh mục đồ họ + trang phục (shared/data/danh-muc-do-trang-phuc.json, sinh bởi tools/do_trang_phuc/sinh.py):
 * hình dạng, icon có thật, mọi món thuộc một bộ có thật, ngân sách trang trí = trần tổng, ô nào cũng có món miễn phí,
 * và hàm ánh xạ hàng CSDL -> danh mục (API /api/do-trang-phuc) cho cùng kết quả với JSON. JS thuần: chạy được bằng
 * `node --test tests/do-trang-phuc.test.mjs` trên Node 20 (không cần bỏ kiểu TypeScript).
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { ID_ANH_DAU, hangThanhDanhMuc, moTaNguon, phanTram, tongNganSach } from '../shared/utils/doTrangPhuc.mjs'

const WEB = new URL('..', import.meta.url).pathname
const dm = JSON.parse(readFileSync(`${WEB}shared/data/danh-muc-do-trang-phuc.json`, 'utf8'))

test('628 món đồ họ, id riêng, trong dải 1400..2099', () => {
  assert.equal(dm.do.length, 628)
  const id = new Set(dm.do.map(d => d.id))
  assert.equal(id.size, dm.do.length)
  for (const d of dm.do) assert.ok(d.id >= 1400 && d.id <= 2099, String(d.id))
})

test('mọi món thuộc một bộ có thật; 18 bộ hệ + 3 bộ vùng', () => {
  const bo = new Set(dm.bo.map(b => b.ma))
  assert.equal(dm.bo.filter(b => b.loai === 'he').length, 18)
  assert.equal(dm.bo.filter(b => b.loai === 'vung').length, 3)
  for (const d of dm.do) assert.ok(bo.has(d.maHo), d.maHo)
  for (const b of dm.bo) assert.ok(dm.do.some(d => d.maHo === b.ma), `bộ rỗng ${b.ma}`)
})

test('icon có thật cho mọi món (đồ + trang trí), không trùng số', () => {
  const so = [...dm.do.map(d => d.icon), ...dm.trangTri.map(t => t.icon)]
  assert.equal(new Set(so).size, so.length)
  for (const n of so) assert.ok(existsSync(`${WEB}public/icon-do/${n}.png`), `thiếu icon ${n}`)
})

test('thưởng bộ trong trần: ST hệ <= 5%, kháng <= 3%, bộ vùng mỗi dòng <= 1%', () => {
  for (const b of dm.bo) {
    const tong = {}
    for (const t of b.thuong) tong[t.chiSo] = (tong[t.chiSo] ?? 0) + t.phanNghin
    if (b.loai === 'he') {
      assert.ok((tong.st_he ?? 0) <= 50, b.ma)
      assert.ok((tong.khang_he_khac ?? 0) <= 30, b.ma)
    }
    else {
      for (const v of Object.values(tong)) assert.ok(v <= 10, b.ma)
    }
  }
})

test('ngân sách trang trí: 1,5 + 1 + 0,5 + 1 + danh hiệu 1 = trần 5%', () => {
  const pn = Object.fromEntries(dm.oTrangTri.map(o => [o.o, o.phanNghin]))
  assert.deepEqual(pn, { trang_phuc: 15, hao_quang: 10, skin_thu: 5, the_dong_hanh: 10 })
  assert.equal(dm.nganSachDanhHieu, 10)
  assert.equal(tongNganSach(dm), dm.tranTong)
  assert.equal(dm.tranTong, 50)
  assert.equal(phanTram(15), '1,5')
})

test('ô nào cũng có món miễn phí; món cửa hàng có giá, món khác không giá', () => {
  for (const o of dm.oTrangTri) {
    assert.ok(dm.trangTri.some(t => t.o === o.o && t.nguon === 'mien_phi'), o.o)
  }
  for (const t of dm.trangTri) {
    if (t.nguon === 'cua_hang') assert.ok(t.giaLuong > 0, t.ma)
    else assert.equal(t.giaLuong, 0, t.ma)
    assert.notEqual(moTaNguon(t), '—', t.ma)
  }
  assert.match(moTaNguon(dm.trangTri.find(t => t.ma === 'tp_tan_thu')), /cấp 5/)
  assert.match(moTaNguon(dm.trangTri.find(t => t.ma === 'tp_valentine')), /Valentine/)
})

test('ánh xạ hàng CSDL -> danh mục khớp JSON (API dùng khi đã chạy sql/htbb.sql (khối them_do_trang_phuc.sql))', () => {
  const ho = dm.bo.map((b, i) => ({
    ma: b.ma, ten: b.ten, loai: b.loai, he: b.loai === 'he' ? dm.he.indexOf(b.he) : -1,
    he_khac: b.loai === 'he' ? dm.he.indexOf(b.heKhac) : -1, vung: b.vung ?? '', bac_tu: b.bacTu ?? 2, bac_den: b.bacDen ?? 10,
    mo_ta: '', thu_tu: i + 1, bat: 1,
  }))
  ho.push({ ma: 'an_di', ten: 'Bộ tắt', loai: 'he', he: 0, he_khac: 0, vung: '', bac_tu: 0, bac_den: 0, mo_ta: '', thu_tu: 99, bat: 0 })
  const thuong = dm.bo.flatMap(b => b.thuong.map(t => ({ ma_ho: b.ma, so_mon: t.soMon, chi_so: t.chiSo, phan_nghin: t.phanNghin })))
  const moi = dm.do.map(d => ({ id_item: d.id, ma_ho: d.maHo, type: d.type, bac: d.bac, ten: d.ten, icon: ID_ANH_DAU + d.icon, nguon: d.nguon.join(',') }))
  moi.push({ id_item: 2098, ma_ho: 'an_di', type: 0, bac: 0, ten: 'Món của bộ tắt', icon: ID_ANH_DAU, nguon: '' })
  const o = dm.oTrangTri.map((x, i) => ({ o: x.o, ten: x.ten, phan_nghin: x.phanNghin, dong: x.dong.map(d => `${d.ma}:${d.ten}`).join(','), thu_tu: i + 1 }))
  const trangPhuc = dm.trangTri.map((t, i) => ({ ma: t.ma, o: t.o, so: t.so, ten: t.ten, do_hiem: t.doHiem, icon: ID_ANH_DAU + t.icon,
    nguon: t.nguon, tham_so: t.thamSo, gia_luong: t.giaLuong, mo_ta: t.moTa, thu_tu: i + 1, bat: 1 }))
  trangPhuc.push({ ma: 'tp_tat', o: 'trang_phuc', so: 200, ten: 'Tắt', do_hiem: 0, icon: ID_ANH_DAU, nguon: 'cua_hang', tham_so: '', gia_luong: 1, mo_ta: '', thu_tu: 999, bat: 0 })
  const ra = hangThanhDanhMuc({ ho, thuong, moi, o, trangPhuc }, { nganSachDanhHieu: 10, tranTong: 50 })
  assert.equal(ra.bo.length, dm.bo.length)
  assert.deepEqual(ra.do, dm.do)
  assert.deepEqual(ra.oTrangTri, dm.oTrangTri)
  assert.deepEqual(ra.trangTri, dm.trangTri)
  assert.deepEqual(ra.bo.map(b => [b.ma, b.loai, b.he ?? b.vung, b.thuong]), dm.bo.map(b => [b.ma, b.loai, b.he ?? b.vung, b.thuong]))
  assert.equal(tongNganSach(ra), 50)
})

test('trang cẩm nang đăng ký trong danh mục', () => {
  const s = readFileSync(`${WEB}shared/utils/danhMucCamNang.ts`, 'utf8')
  for (const slug of ['do-moi', 'trang-phuc']) {
    assert.ok(s.includes(`slug: '${slug}'`), slug)
    assert.ok(existsSync(`${WEB}app/pages/cam-nang/${slug}.vue`), slug)
  }
})
