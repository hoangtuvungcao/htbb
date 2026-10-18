#!/usr/bin/env node
/**
 * Sinh ảnh chia sẻ mạng xã hội (Open Graph 1200×630) -> public/og/htbb.png
 *
 *   node scripts/sinh-anh-og.mjs            (cần google-chrome hoặc chromium trong PATH, hoặc đặt CHROME=...)
 *
 * Vẽ bằng HTML (scripts/og/og.html) + sprite thật trong public/pokemon/anim, chụp bằng Chrome headless.
 * Chạy lại khi đổi tên game (shared/utils/tenSite.ts) hoặc sprite khởi đầu.
 */
import { execFileSync } from 'node:child_process'
import { readFileSync, writeFileSync, mkdtempSync, existsSync, copyFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath, pathToFileURL } from 'node:url'

const WEB = resolve(dirname(fileURLToPath(import.meta.url)), '..')
// Ảnh OG chuẩn = key art chung của game (htbb-client/j2me/tools/tao_banner.py --web ghi thẳng public/og/htbb.png).
// Có sẵn bản đó thì chỉ chép lại; không có repo client mới vẽ bằng HTML như cũ.
const OG_KEYART = resolve(WEB, '../htbb-client/shared/banner/og_1200x630.png')
if (existsSync(OG_KEYART)) {
  copyFileSync(OG_KEYART, join(WEB, 'public/og/htbb.png'))
  console.log(`Đã chép ${OG_KEYART} -> public/og/htbb.png`)
  process.exit(0)
}
const ten = readFileSync(join(WEB, 'shared/utils/tenSite.ts'), 'utf8').match(/TEN_SITE\s*=\s*'([^']+)'/)?.[1] ?? 'Huyền Thoại Bảo Bối'
const anim = JSON.parse(readFileSync(join(WEB, 'shared/data/sprite-anim.json'), 'utf8'))
const sprite = (dex, s) => {
  const a = anim[dex].idle
  const url = pathToFileURL(join(WEB, `public/pokemon/anim/${dex}-idle.png`)).href
  return `<span class="sp" style="width:${a.w * s}px;height:${a.h * s}px;background-image:url(${url});background-size:${a.w * a.khung * s}px ${a.h * s}px"></span>`
}
const html = readFileSync(join(WEB, 'scripts/og/og.html'), 'utf8')
  .replace('{{TEN}}', ten)
  .replace('{{SPRITES}}', [4, 1, 172, 7, 133].map(d => sprite(d, 4)).join(''))
const tmp = mkdtempSync(join(tmpdir(), 'og-'))
const f = join(tmp, 'og.html')
writeFileSync(f, html)
const out = join(WEB, 'public/og/htbb.png')
const chrome = process.env.CHROME ?? 'google-chrome'
execFileSync(chrome, ['--headless=new', '--disable-gpu', '--no-sandbox', '--hide-scrollbars', '--window-size=1200,630', `--screenshot=${out}`, pathToFileURL(f).href], { stdio: 'ignore' })
console.log(`Đã ghi ${out}`)
