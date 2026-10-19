/**
 * ĐỐI CHIẾU SQL == JSON: các mục website nay đọc từ CSDL game lúc chạy (server/utils/duLieuSql.ts) phải ra
 * ĐÚNG y bản JSON đóng gói (dự phòng lúc build). Đọc dòng bảng từ sql/htbb_day_du.sql (tệp nạp CSDL production)
 * bằng shared/utils/sqlDump.ts rồi chạy chính các hàm biến đổi của shared/utils/duLieuSqlThuan.ts.
 * Không cần CSDL. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { docBangSql } from '../shared/utils/sqlDump.ts'
import {
  BANG_CAM_NANG, bangCapDoTuSql, bangCapTuSql, camNangDeTuSql, cheTaoTuSql, cuaHangTuSql, ghepCamNang, gymTuSql,
  kinhTeTuSql, lichBossTuSql, nangCapTuSql, nangLuongTuSql, nhanhEeveeTuSql, suKienTuSql, suKienVatPhamTuSql,
  trangBiGiftcodeTuSql, trangTriTuSql, type BangCamNang,
} from '../shared/utils/duLieuSqlThuan.ts'

const doc = (p: string) => readFileSync(fileURLToPath(new URL(p, import.meta.url)), 'utf8')
const SQL = doc('../../sql/htbb_day_du.sql')
const json = (p: string) => JSON.parse(doc(`../shared/data/${p}`))
const CN = json('cam-nang.json')
const ND = json('cam-nang-noi-dung.json')
const bang = (t: string) => docBangSql(SQL, t)

test('sqlDump: đọc được escape, NULL, nhiều câu INSERT', () => {
  const s = "CREATE TABLE `t` (\n  `a` int NOT NULL,\n  `b` varchar(9) DEFAULT NULL,\n  PRIMARY KEY (`a`)\n);\n"
    + "INSERT INTO `t` VALUES (1,'it\\'s'),(2,NULL);\nINSERT INTO `t` (`b`, `a`) VALUES ('x''y\\n', 3);\n"
  assert.deepEqual(docBangSql(s, 't'), [{ a: 1, b: 'it\'s' }, { a: 2, b: null }, { a: 3, b: 'x\'y\n' }])
  assert.deepEqual(docBangSql(`${s}DELETE FROM \`t\`;\n`, 't'), [])
})

test('bang-cap-do.json == data_cap_do_lau_dai.exp_cu', () => {
  const j = json('bang-cap-do.json')
  assert.deepEqual(bangCapDoTuSql(bang('data_cap_do_lau_dai')), { capDoToiDa: j.capDoToiDa, expMain: j.expMain, expRequire: j.expRequire })
})

test('giftcode-an-toan.json trangBi == data_noi_dung_trang_bi.id_item', () => {
  assert.deepEqual(trangBiGiftcodeTuSql(bang('data_noi_dung_trang_bi')), json('giftcode-an-toan.json').trangBi)
})

test('cam-nang.json: capDo.bang, capDo.nangLuong, kinhTe, trangBi, suKien', () => {
  assert.deepEqual(bangCapTuSql(bang('data_cap_do_lau_dai'), CN.capDo.bang), CN.capDo.bang)
  assert.deepEqual(nangLuongTuSql(bang('data_kinh_te_lau_dai')), CN.capDo.nangLuong)
  const kt = kinhTeTuSql(bang('data_kinh_te_lau_dai'))!
  assert.equal(Object.keys(kt).length, 6)
  for (const [k, v] of Object.entries(kt)) assert.equal(v, CN.kinhTe[k], k)
  const nc = nangCapTuSql(bang('data_nang_cap_lau_dai'), CN.trangBi.bac)!
  assert.deepEqual(nc.nangCap, CN.trangBi.nangCap)
  assert.deepEqual(nc.chiPhiNangCap, CN.trangBi.chiPhiNangCap)
  assert.deepEqual(nc.bac, CN.trangBi.bac)
  assert.deepEqual(suKienTuSql(bang('data_su_kien_nam')), CN.suKien)
})

test('cam-nang-noi-dung.json: cửa hàng, Gym, chế tạo, nhánh Eevee, trang trí, vật phẩm sự kiện, lịch boss', () => {
  assert.deepEqual(cuaHangTuSql(bang('data_noi_dung_cua_hang')), ND.cuaHang)
  assert.deepEqual(gymTuSql(bang('data_noi_dung_gym'), bang('data_nang_cap_lau_dai')), ND.gym)
  assert.deepEqual(cheTaoTuSql(bang('data_noi_dung_che_tao')), ND.cheTao)
  assert.deepEqual(nhanhEeveeTuSql(bang('data_noi_dung_chieu_nhanh_eevee')), ND.nhanhEevee)
  assert.deepEqual(trangTriTuSql(bang('data_noi_dung_trang_tri')), ND.trangTri)
  assert.deepEqual(suKienVatPhamTuSql(bang('data_noi_dung_su_kien_vat_pham')), ND.suKienVatPham)
  assert.deepEqual(lichBossTuSql(bang('data_noi_dung_boss_the_gioi')), ND.bossTheGioi.lich)
})

test('ghép đủ bảng = JSON y hệt; thiếu bảng = giữ JSON', () => {
  const b: BangCamNang = Object.fromEntries(BANG_CAM_NANG.map(t => [t, bang(t)]))
  // Thứ tự dòng CSDL không cố định: đảo ngược để chắc hàm tự sắp xếp.
  for (const k of Object.keys(b) as (keyof BangCamNang)[]) b[k] = [...b[k]!].reverse()
  const de = camNangDeTuSql(b, CN)
  assert.equal(de.nguon.length, 12)
  assert.deepEqual(ghepCamNang(CN, de.camNang), CN)
  assert.deepEqual(ghepCamNang(ND, de.noiDung), ND)
  const rong = camNangDeTuSql({}, CN)
  assert.deepEqual(rong.nguon, [])
  assert.deepEqual(ghepCamNang(CN, rong.camNang), CN)
  assert.equal(cuaHangTuSql([{ stt: 1, cua_hang: 'X', ten: 'Y', gia: 1, tien: 'tien_la', cap: 1, gioi_han: '' }]), null, 'tiền lạ -> giữ JSON')
})
