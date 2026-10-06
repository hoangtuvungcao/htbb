#!/usr/bin/env node
/**
 * CHỈ DÙNG KHI DEV: in ra một token đăng nhập để gọi thử API bằng curl.
 *
 *   node scripts/dev-token.mjs admin admin 1     # token admin cho tài khoản "admin" id 1
 *   node scripts/dev-token.mjs user hoahv 3      # token người chơi
 *
 * Đọc KPAH_SECRET từ .env. Thuật toán giống hệt server/utils/token.ts.
 * Không chạy được nếu không có KPAH_SECRET — tránh vô tình dùng trên máy thật.
 */
import { createHmac } from 'node:crypto'
import { readFileSync } from 'node:fs'

let secret = process.env.KPAH_SECRET
if (!secret) {
  try {
    const m = readFileSync(new URL('../.env', import.meta.url), 'utf8').match(/^KPAH_SECRET=(.+)$/m)
    secret = m?.[1]?.trim()
  } catch { /* không có .env */ }
}
if (!secret) {
  console.error('Thiếu KPAH_SECRET (đặt trong .env).')
  process.exit(1)
}
const [k = 'admin', u = 'admin', id = '1', han = '3600'] = process.argv.slice(2)
const iat = Math.floor(Date.now() / 1000)
const du = Buffer.from(JSON.stringify({ k, id: Number(id), u, iat, exp: iat + Number(han) }), 'utf8').toString('base64url')
const sig = createHmac('sha256', secret).update(`${k}|${du}`).digest('base64url')
console.log(`${du}.${sig}`)
