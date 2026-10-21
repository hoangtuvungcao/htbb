/**
 * Học chiêu theo loài trên Pokédex: hàm thuần shared/utils/learnset.ts với dữ liệu đầu thật (phần hoc_chieu_huan_luyen.sql của sql/nang_cap.sql)
 * (cùng nguồn với game server), API đọc data_learnset, trang Pokédex hiện bảng chiêu theo loài.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { chieuCuaLoai, docDong, type DongLearnset } from '../shared/utils/learnset.ts'

const WEB = new URL('..', import.meta.url).pathname
const sql = readFileSync(`${WEB}../sql/nang_cap.sql`, 'utf8').split('\n-- >>> hoc_chieu_huan_luyen.sql\n')[1].split('\n-- >>> ')[0]

function dongSql(): DongLearnset[] {
  const a = sql.indexOf('INSERT INTO data_learnset')
  const b = sql.indexOf('ON DUPLICATE KEY', a)
  return sql.slice(a, b).split('\n').filter(l => l.startsWith('(')).map((l) => {
    const m = /^\((\d+),(\d+),(\d+),'([^']*)',(\d+),'([^']*)','([^']*)',(\d+),(\d+),'([^']*)'\)/.exec(l)!
    return docDong({ loai: m[1], o: m[2], dong: m[3], ten: m[4], he: m[5], nguon: m[7], cap_hoc: m[8], gia_xu: m[9] })!
  })
}

const ds = dongSql()

test('dữ liệu đầu đọc được (175 dòng, 21 loài)', () => {
  assert.equal(ds.length, 175)
  assert.equal(new Set(ds.map(d => d.loai)).size, 21)
})

test('Charmander: chiêu tự học + chiêu khi tiến hoá', () => {
  const c = chieuCuaLoai(ds, 4)
  assert.equal(c.length, 9)
  assert.deepEqual(c.slice(0, 4).map(x => [x.ten, x.cach, x.capHoc]), [['Cào', 'cap', 1], ['Than hồng', 'cap', 4], ['Vũ điệu kiếm', 'cap', 6], ['Nanh lửa', 'cap', 7]])
  const sp = c.find(x => x.o === 6)!
  assert.equal(sp.cach, 'tien-hoa')
  assert.equal(sp.loaiHoc, 6)
  assert.equal(sp.capHoc, 36)
})

test('Charizard: chiêu NPC có giá', () => {
  const c = chieuCuaLoai(ds, 6)
  const npc = c.filter(x => x.cach === 'npc')
  assert.deepEqual(npc.map(x => [x.ten, x.giaXu]), [['Súng phun lửa', 60000], ['Vuốt rồng', 120000], ['Đại hỏa', 300000]])
  assert.equal(c.filter(x => x.cach === 'tien-hoa').length, 0)
})

test('nhánh Eevee đổi tên + hệ ô 2', () => {
  const v = chieuCuaLoai(ds, 134).find(x => x.o === 2)!
  assert.equal(v.ten, 'Súng nước')
  assert.equal(v.he, 2)
  assert.equal(chieuCuaLoai(ds, 133).find(x => x.o === 2)!.ten, 'Cắn')
})

test('API + trang Pokédex dùng bảng chiêu theo loài', () => {
  assert.ok(existsSync(`${WEB}server/api/learnset/[dex].get.ts`))
  const trang = readFileSync(`${WEB}app/pages/pokedex/[dex].vue`, 'utf8')
  assert.match(trang, /\/api\/learnset\//)
  assert.match(trang, /chieuCuaLoai/)
})
