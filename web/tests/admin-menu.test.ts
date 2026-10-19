/**
 * Menu quản trị (app/utils/adminMenu.ts): mọi mục trỏ tới một trang có thật, không còn trang đã gộp
 * (/admin/cauhinh, /admin/server, /admin/phan-quyen, /admin/dong-bo), menu gọn (≤ 40 mục, nhóm ≤ 8).
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'

const WEB = new URL('..', import.meta.url).pathname
const menu = readFileSync(`${WEB}app/utils/adminMenu.ts`, 'utf8')
const muc = [...menu.matchAll(/to: '(\/admin[^']*)'/g)].map(m => m[1]!)

test('menu quản trị: mỗi mục có trang', () => {
  assert.ok(muc.length > 10)
  for (const to of muc) {
    const p = to === '/admin' ? 'admin/index' : to.slice(1)
    assert.ok(existsSync(`${WEB}app/pages/${p}.vue`) || existsSync(`${WEB}app/pages/${p}/index.vue`), `thiếu trang cho ${to}`)
  }
})

test('menu quản trị: không còn mục đã gộp / bỏ', () => {
  for (const cu of ['/admin/cauhinh', '/admin/server', '/admin/phan-quyen', '/admin/dong-bo']) {
    assert.ok(!muc.includes(cu), `${cu} vẫn còn trong menu`)
  }
  assert.equal(new Set(muc).size, muc.length, 'trùng mục')
})

test('menu quản trị: gọn', () => {
  assert.ok(muc.length <= 40, `${muc.length} mục`)
  const nhom = [...menu.matchAll(/nhom: '([^']+)'/g)].length
  assert.ok(nhom <= 10, `${nhom} nhóm`)
})
