/**
 * Danh mục cấu hình trung tâm lúc chạy đọc từ CSDL (cau_hinh_danh_muc + cau_hinh_nhom — nguồn duy nhất, bản chụp
 * sql/htbb_day_du.sql) phải ra ĐÚNG bản tĩnh shared/utils/cauHinhTrungTam.gen.ts (dự phòng lúc build).
 * Không cần CSDL. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { docBangSql } from '../shared/utils/sqlDump.ts'
import { DANH_MUC_TT, NHOM_TT } from '../shared/utils/cauHinhTrungTam.gen.ts'
import { danhMucTT, danhMucTuSqlTT, datDanhMucTT, dsNhomTT, khoaTT, phienBanDanhMucTT } from '../shared/utils/cauHinhTrungTam.ts'

const doc = (p: string) => readFileSync(fileURLToPath(new URL(p, import.meta.url)), 'utf8')

/** Dòng giống hệt bộ sinh SQL (gioi_han JSON: min/max/buoc/soPhanTu/mau/doDai, luaChon + luaChonTen, hoa). */
function dongGiaLap() {
  const khoa = DANH_MUC_TT.map((k, i) => {
    const g: Record<string, unknown> = {}
    for (const f of ['min', 'max', 'buoc', 'soPhanTu', 'mau', 'doDai'] as const) if (k[f] !== undefined) g[f] = k[f]
    if (k.luaChon) { g.luaChon = k.luaChon.map(x => x[0]); g.luaChonTen = k.luaChon.map(x => x[1]) }
    if (k.hoa) g.hoa = true
    return {
      khoa: k.khoa, nhom: k.nhom, ten: k.ten, mo_ta: k.moTa, kieu: k.kieu, mac_dinh: k.macDinh, gioi_han: JSON.stringify(g),
      ap_dung: k.apDung, nhay: k.nhay ? 1 : 0, khoa_prod: k.khoaProd ? 1 : 0, chi_doc: k.chiDoc ? 1 : 0, bang: k.bang,
      dung_o: k.dungO, nguon: k.nguon, thu_tu: i + 1, lop: k.lop ?? 'chinh',
    }
  })
  const nhom = NHOM_TT.map((n, i) => ({ ma: n.ma, ten: n.ten, mo_ta: n.moTa, thu_tu: i + 1, an: n.an ? 1 : 0 }))
  return { khoa, nhom }
}

test('danhMucTuSqlTT: dòng theo định dạng CSDL -> đúng bản sinh (kể cả thứ tự, lựa chọn có nhãn, hoa)', () => {
  const { khoa, nhom } = dongGiaLap()
  const dm = danhMucTuSqlTT([...khoa].reverse(), [...nhom].reverse())
  assert.ok(dm)
  assert.deepEqual(dm.ds, DANH_MUC_TT)
  assert.deepEqual(dm.nhom, NHOM_TT)
  // CSDL chưa chạy migration (không có thu_tu) / bảng trống -> null (giữ bản sinh)
  assert.equal(danhMucTuSqlTT(khoa.map(({ thu_tu: _bo, ...r }) => r), nhom), null)
  // Thiếu cột lop (CSDL trước 0.9.4) / lớp lạ -> null: không đoán 'chinh' cho khoá ẩn
  assert.equal(danhMucTuSqlTT(khoa.map(({ lop: _bo, ...r }) => r), nhom), null)
  assert.equal(danhMucTuSqlTT(khoa.map(r => ({ ...r, lop: 'la' })), nhom), null)
  // Lớp từ CSDL đi tới khoá: khoá ẩn vẫn ẩn khi danh mục đọc từ SQL
  assert.ok(dm.ds.some(k => k.lop === 'luon_bat') && dm.ds.some(k => k.lop === 'co_dinh'))
  assert.equal(danhMucTuSqlTT([], nhom), null)
  assert.equal(danhMucTuSqlTT(khoa, null), null)
})

test('datDanhMucTT: thay danh mục đang dùng, null = về bản sinh', () => {
  const pb = phienBanDanhMucTT()
  const ds = [{ ...DANH_MUC_TT[0]!, ten: 'Đổi tên thử' }]
  assert.equal(datDanhMucTT({ ds, nhom: NHOM_TT }), true)
  assert.equal(khoaTT(DANH_MUC_TT[0]!.khoa)?.ten, 'Đổi tên thử')
  assert.equal(khoaTT(DANH_MUC_TT[1]!.khoa), null)
  assert.ok(phienBanDanhMucTT() > pb)
  assert.equal(datDanhMucTT(null), true)
  assert.equal(danhMucTT(), DANH_MUC_TT)
  assert.equal(dsNhomTT(), NHOM_TT)
})

const SQL_DAY_DU = doc('../../sql/htbb_day_du.sql')

test('sql/htbb_day_du.sql: cau_hinh_danh_muc + cau_hinh_nhom == cauHinhTrungTam.gen.ts', () => {
  assert.ok(/CREATE TABLE[^;]*`cau_hinh_nhom`/.test(SQL_DAY_DU), 'bản đầy đủ phải có bảng cau_hinh_nhom')
  const dm = danhMucTuSqlTT(docBangSql(SQL_DAY_DU, 'cau_hinh_danh_muc'), docBangSql(SQL_DAY_DU, 'cau_hinh_nhom'))
  assert.ok(dm, 'đọc được danh mục từ SQL')
  assert.deepEqual(dm.nhom, NHOM_TT)
  assert.deepEqual(dm.ds, DANH_MUC_TT)
})
