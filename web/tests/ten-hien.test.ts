/**
 * Tên NPC / bản đồ (bảng SQL data_npc / data_map_ten): luật kiểm tên ở trang /admin/ten-hien
 * (shared/utils/kiemTenHien.ts) chấp nhận MỌI tên trong dữ liệu đầu sql/pokemon_ten_hien.sql và chặn tên không dấu /
 * tên kiếm hiệp cũ. Trang + API + quyền có thật.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { kiemTenHien } from '../shared/utils/kiemTenHien.ts'

const WEB = new URL('..', import.meta.url).pathname
const sql = readFileSync(`${WEB}../sql/pokemon_ten_hien.sql`, 'utf8')

function tenTrongBang(bang: string): string[] {
  const a = sql.indexOf('INSERT INTO `' + bang + '`')
  const b = sql.indexOf('ON DUPLICATE KEY', a)
  return sql.slice(a, b).split('\n').filter(l => l.startsWith('(')).map(l => /^\(\d+,'((?:[^']|'')*)'/.exec(l)![1]!.replace(/''/g, "'"))
}

test('mọi tên trong dữ liệu đầu hợp lệ', () => {
  const npc = tenTrongBang('data_npc')
  const map = tenTrongBang('data_map_ten')
  assert.equal(npc.length, 32)
  assert.ok(map.length >= 130)
  for (const t of [...npc, ...map]) assert.equal(kiemTenHien(t), null, t)
})

test('chặn tên không dấu và tên kiếm hiệp cũ', () => {
  assert.match(kiemTenHien('Ba Ngoai') ?? '', /thiếu dấu/)
  assert.match(kiemTenHien('Ong Snorlax') ?? '', /thiếu dấu/)
  assert.match(kiemTenHien('Bến bắc') ?? '', /kiếm hiệp/)
  assert.match(kiemTenHien('Dao Châu') ?? '', /kiếm hiệp/)
  assert.ok(kiemTenHien(''))
  assert.equal(kiemTenHien('Bà Ngoại'), null)
  assert.equal(kiemTenHien('Tauros 1'), null)
  assert.equal(kiemTenHien('Bến tàu phía bắc'), null)
})

test('trang, API, quyền', () => {
  assert.ok(existsSync(`${WEB}app/pages/admin/ten-hien.vue`))
  assert.ok(existsSync(`${WEB}server/api/admin/ten-hien/index.get.ts`))
  assert.ok(existsSync(`${WEB}server/api/admin/ten-hien/sua.post.ts`))
  assert.ok(readFileSync(`${WEB}shared/utils/quyenAdmin.ts`, 'utf8').includes("'/api/admin/ten-hien/**'"))
  assert.ok(readFileSync(`${WEB}server/api/admin/ten-hien/sua.post.ts`, 'utf8').includes('ghiNhatKy('))
})
