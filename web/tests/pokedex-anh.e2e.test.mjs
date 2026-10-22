/**
 * E2E (Playwright): trang /pokedex qua một proxy GIẢ LẬP TƯỜNG LỬA - quá 6 yêu cầu ảnh /pokemon/* đang chạy cùng lúc thì
 * trả 429 (như Nova WAF production). Mọi thẻ đang thấy trong khung nhìn phải tải xong ảnh (data-anh="da-tai"), proxy ghi
 * nhận số yêu cầu song song cao nhất <= 6.
 *
 * Cần: `npm run build` (web/.output) + gói `playwright` (hoặc `playwright-core` + Chrome ở /usr/bin/google-chrome).
 * Thiếu thứ nào thì phép kiểm BỎ QUA kèm lý do (máy CI không có trình duyệt vẫn qua `npm test`).
 *   npm i -D playwright && npx playwright install chromium && npm run build && node --test tests/pokedex-anh.e2e.test.mjs
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { existsSync } from 'node:fs'
import { spawn } from 'node:child_process'
import http from 'node:http'

const OUTPUT = new URL('../.output/server/index.mjs', import.meta.url)

async function layPlaywright() {
  for (const ten of ['playwright', 'playwright-core']) {
    try {
      return await import(ten)
    } catch {
      // thử gói sau
    }
  }
  return null
}

function cho(ms) {
  return new Promise(r => setTimeout(r, ms))
}

async function moCong(cong, het = 30000) {
  const t0 = Date.now()
  while (Date.now() - t0 < het) {
    try {
      await new Promise((ok, loi) => http.get(`http://127.0.0.1:${cong}/`, r => { r.resume(); ok() }).on('error', loi))
      return true
    } catch {
      await cho(300)
    }
  }
  return false
}

test('Pokédex: ảnh thẻ trong khung nhìn tải xong khi tường lửa chặn > 6 yêu cầu song song', async (t) => {
  const pw = await layPlaywright()
  if (!pw) {
    t.skip('không có gói playwright / playwright-core (npm i -D playwright)')
    return
  }
  if (!existsSync(OUTPUT)) {
    t.skip('chưa build web (.output) - chạy npm run build trước')
    return
  }
  const congWeb = 39700 + Math.floor(Math.random() * 200)
  const congProxy = congWeb + 1
  const web = spawn(process.execPath, [OUTPUT.pathname], {
    env: { ...process.env, PORT: String(congWeb), NITRO_PORT: String(congWeb), HOST: '127.0.0.1', HTBB_DB_HOST: '127.0.0.1', HTBB_DB_PORT: '1' },
    stdio: 'ignore',
  })
  let dangAnh = 0
  let caoNhat = 0
  let bi429 = 0
  const proxy = http.createServer((req, res) => {
    const laAnh = req.url.startsWith('/pokemon/')
    if (laAnh) {
      if (dangAnh >= 6) {
        bi429++
        res.writeHead(429, { 'retry-after': '1' })
        res.end('Too Many Requests')
        return
      }
      dangAnh++
      caoNhat = Math.max(caoNhat, dangAnh)
    }
    const p = http.request({ host: '127.0.0.1', port: congWeb, path: req.url, method: req.method, headers: req.headers }, (r) => {
      res.writeHead(r.statusCode, r.headers)
      r.pipe(res)
      r.on('end', () => { if (laAnh) setTimeout(() => dangAnh--, 60) })   // giữ chỗ thêm 60 ms như mạng thật
    })
    p.on('error', () => { if (laAnh) dangAnh--; res.writeHead(502); res.end() })
    req.pipe(p)
  }).listen(congProxy, '127.0.0.1')
  let trinh
  try {
    assert.ok(await moCong(congWeb), 'web .output không lên')
    try {
      trinh = await pw.chromium.launch({ headless: true })
    } catch {
      try {
        trinh = await pw.chromium.launch({ headless: true, executablePath: '/usr/bin/google-chrome' })
      } catch (e) {
        t.skip(`không mở được trình duyệt: ${String(e).slice(0, 120)}`)
        return
      }
    }
    const trang = await trinh.newPage({ viewport: { width: 1280, height: 900 } })
    await trang.goto(`http://127.0.0.1:${congProxy}/pokedex`, { waitUntil: 'networkidle' })
    // mọi thẻ đang thấy: chờ ảnh tải xong (có thử lại khi 429)
    const ok = await trang.waitForFunction(() => {
      const ds = [...document.querySelectorAll('[aria-label="Danh sách Pokémon"] [data-anh]')]
      const thay = ds.filter((e) => {
        const r = e.getBoundingClientRect()
        return r.bottom > 0 && r.top < window.innerHeight
      })
      return thay.length > 0 && thay.every(e => e.getAttribute('data-anh') === 'da-tai')
    }, null, { timeout: 45000 }).then(() => true, () => false)
    assert.ok(ok, 'thẻ trong khung nhìn chưa tải xong ảnh')
    assert.ok(caoNhat <= 6, `ảnh song song cao nhất ${caoNhat} > 6`)
    t.diagnostic(`anh song song cao nhat ${caoNhat}, 429 cua proxy ${bi429}`)
  } finally {
    if (trinh) await trinh.close()
    proxy.close()
    web.kill()
  }
})
