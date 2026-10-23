/**
 * Luật chủ game (0.9.8): chữ người chơi / khách thấy KHÔNG nhắc tên game khác ("NRO", "NSO", "HSO", "Làng Lá", "TeaMobi",
 * "phong cách NRO"...). Quét: trang web (web/app/pages + components, trừ /giay-phep và khu admin), dữ liệu web
 * (web/shared/data), bài tin / dữ liệu hiển thị trong sql/htbb.sql (data_*, web_tin_tuc - bỏ danh sách từ cấm chat /
 * tên cấm đăng ký), chuỗi giao diện game (chuỗi "..." trong client_jar/src, server/src) và bảng ghi công trong game.
 * Chú thích mã / tài liệu nội bộ được giữ.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, readdirSync, statSync } from 'node:fs'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { htbbSql } from './htbbSql.ts'

const goc = (p: string) => fileURLToPath(new URL(p, import.meta.url))
const CAM = /(^|[^A-Za-zÀ-ỹ])(NRO|NSO|HSO|TeaMobi|Teamobi)([^A-Za-zÀ-ỹ]|$)|Làng Lá|phong cách NRO|Ngọc Rồng Online|Ninja School/

function tep(thu: string, duoi: RegExp, bo: (p: string) => boolean = () => false): string[] {
  const ra: string[] = []
  for (const t of readdirSync(thu)) {
    const p = join(thu, t)
    if (bo(p)) continue
    if (statSync(p).isDirectory()) ra.push(...tep(p, duoi, bo))
    else if (duoi.test(t)) ra.push(p)
  }
  return ra
}

// Bỏ chú thích (dòng //, khối sao, thẻ HTML) - chỉ xét chữ hiển thị.
function boChuThich(s: string): string {
  return s.replace(/<!--[\s\S]*?-->/g, '').replace(/\/\*[\s\S]*?\*\//g, '').replace(/(^|[^:"'`])\/\/[^\n]*/g, '$1')
}

function loiTrong(files: string[], chi = (s: string) => s): string[] {
  const loi: string[] = []
  for (const f of files) {
    const s = chi(boChuThich(readFileSync(f, 'utf8')))
    s.split('\n').forEach((d, i) => {
      if (CAM.test(d)) loi.push(`${f.replace(goc('../..'), '')}:${i + 1}: ${d.trim().slice(0, 120)}`)
    })
  }
  return loi
}

test('trang web công khai không nhắc tên game khác (trừ /giay-phep)', () => {
  const files = tep(goc('../app'), /\.(vue|ts)$/, p => /[\\/]admin[\\/]?/.test(p.slice(goc('../app').length)) || /giay-phep\.vue$/.test(p))
  const loi = loiTrong(files)
  assert.deepEqual(loi, [], loi.join('\n'))
})

test('dữ liệu web (shared/data) không nhắc tên game khác', () => {
  const files = tep(goc('../shared/data'), /\.(json|ts)$/, p => /quy-tac-dang-ky\.json$/.test(p))
  assert.deepEqual(loiTrong(files), [])
})

test('bài tin + dữ liệu game hiển thị trong SQL không nhắc tên game khác', () => {
  const sql = htbbSql().split('\n')
  const loi = sql.filter(d => d.startsWith('INSERT INTO') && !/`(data_tu_cam|tu_cam|bad_word|data_chat_cam)/.test(d)
    && !/'team0bi'|'teamobi'/.test(d) && CAM.test(d)).map(d => d.slice(0, 120))
  assert.deepEqual(loi, [], loi.join('\n'))
})

test('chuỗi giao diện game (client + máy chủ) và ghi công không nhắc tên game khác', () => {
  const chuoi = (s: string) => (s.match(/"(?:[^"\\\n]|\\.)*"/g) ?? []).join('\n')
  const files = [...tep(goc('../../client_jar/src'), /\.java$/), ...tep(goc('../../server/src'), /\.java$/)]
  const loi = loiTrong(files, chuoi)
  assert.deepEqual(loi, [], loi.join('\n'))
  assert.ok(!CAM.test(readFileSync(goc('../../client_jar/res/GHI_CONG.txt'), 'utf8')), 'GHI_CONG.txt (bảng ghi công trong game)')
})
