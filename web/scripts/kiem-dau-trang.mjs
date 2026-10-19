#!/usr/bin/env node
/**
 * Kiểm đầu trang (SiteHeader) bằng Chrome headless — không cần puppeteer (CDP qua WebSocket có sẵn của Node 22).
 *
 *   node scripts/kiem-dau-trang.mjs --goc http://127.0.0.1:3000 --ra /tmp/dau-trang \
 *        [--tk admin --mk ...] [--rong 1024,1280,1440,1920,2560] [--chrome /usr/bin/google-chrome]
 *
 * Với mỗi bề rộng: chụp ảnh phần đầu trang (chưa đăng nhập, và đã đăng nhập nếu có --tk/--mk: đăng nhập người chơi
 * + admin qua API rồi đặt token vào localStorage/sessionStorage như trang /login làm) và KIỂM:
 *   - không hai phần tử tương tác nào trong <header> (logo, mục menu, nút) chồng lên nhau;
 *   - không phần tử nào tràn ra ngoài khung header / bị che khuất (elementFromPoint ở tâm phải là chính nó);
 *   - header chỉ cao một hàng (≤ 96px).
 * Mã thoát 1 nếu có lỗi. Chạy với server thật (`npm run build && node .output/server/index.mjs`) hoặc `nuxt dev`.
 */
import { spawn } from 'node:child_process'
import { mkdirSync, writeFileSync, mkdtempSync, rmSync } from 'node:fs'
import { join } from 'node:path'
import { tmpdir } from 'node:os'

const arg = (k, d) => { const i = process.argv.indexOf(`--${k}`); return i > 0 ? process.argv[i + 1] : d }
const GOC = arg('goc', 'http://127.0.0.1:3000').replace(/\/$/, '')
const RA = arg('ra', join(tmpdir(), 'kiem-dau-trang'))
const TK = arg('tk', '')
const MK = arg('mk', '')
const DS_RONG = arg('rong', '1024,1280,1440,1920,2560').split(',').map(Number)
const CHROME = arg('chrome', '/usr/bin/google-chrome')
const TRANG = arg('trang', '/')
const CAO = Number(arg('cao', '140'))
/** --mo-menu: bấm nút tài khoản rồi chụp thêm (xem menu thả xuống). */
const MO_MENU = process.argv.includes('--mo-menu')
mkdirSync(RA, { recursive: true })

const ngu = ms => new Promise(r => setTimeout(r, ms))

async function dangNhap() {
  if (!TK) return null
  const goi = async (duong) => {
    const r = await fetch(GOC + duong, { method: 'POST', headers: { 'content-type': 'application/json', origin: GOC }, body: JSON.stringify({ username: TK, password: MK }) })
    if (!r.ok) throw new Error(`${duong}: HTTP ${r.status} ${await r.text()}`)
    return r.json()
  }
  const nc = await goi('/api/auth/login')
  let ad = null
  try { ad = await goi('/api/admin/auth/login') } catch (e) { console.warn('admin login:', e.message) }
  return { nc, ad }
}

async function moChrome() {
  const thuMuc = mkdtempSync(join(tmpdir(), 'kiem-dau-trang-chrome-'))
  const p = spawn(CHROME, ['--headless=new', '--disable-gpu', '--no-first-run', '--no-default-browser-check', '--hide-scrollbars',
    `--user-data-dir=${thuMuc}`, '--remote-debugging-port=0', 'about:blank'], { stdio: ['ignore', 'ignore', 'pipe'] })
  const ws = await new Promise((ok, loi) => {
    let buf = ''
    const t = setTimeout(() => loi(new Error('chrome không mở được cổng gỡ lỗi')), 20000)
    p.stderr.on('data', (d) => {
      buf += d
      const m = buf.match(/DevTools listening on (ws:\/\/\S+)/)
      if (m) { clearTimeout(t); ok(m[1]) }
    })
  })
  const port = new URL(ws).port
  const ds = await (await fetch(`http://127.0.0.1:${port}/json`)).json()
  const trang = ds.find(x => x.type === 'page')
  const sock = new WebSocket(trang.webSocketDebuggerUrl)
  await new Promise(r => sock.addEventListener('open', r, { once: true }))
  let id = 0
  const cho = new Map()
  sock.addEventListener('message', (e) => {
    const m = JSON.parse(e.data)
    if (m.id && cho.has(m.id)) { cho.get(m.id)(m); cho.delete(m.id) }
  })
  const gui = (method, params = {}) => new Promise((ok, loi) => {
    const i = ++id
    cho.set(i, m => (m.error ? loi(new Error(`${method}: ${m.error.message}`)) : ok(m.result)))
    sock.send(JSON.stringify({ id: i, method, params }))
  })
  const dong = () => { try { sock.close() } catch {} p.kill('SIGKILL'); setTimeout(() => { try { rmSync(thuMuc, { recursive: true, force: true }) } catch {} }, 500) }
  return { gui, dong }
}

/** Chạy trong trang: liệt kê phần tử tương tác của header, tìm chồng lấn / tràn / bị che. */
const KIEM_JS = `(() => {
  const h = document.querySelector('header')
  if (!h) return { loi: ['không thấy <header>'] }
  const hr = h.getBoundingClientRect()
  const vis = el => { const s = getComputedStyle(el); const r = el.getBoundingClientRect(); return s.visibility !== 'hidden' && s.display !== 'none' && r.width > 1 && r.height > 1 }
  const ds = [...h.querySelectorAll('a, button, input, img')].filter(vis)
    .filter(el => !ds0(el))
  function ds0(el) { // bỏ phần tử nằm trong phần tử tương tác khác (img trong a)
    let p = el.parentElement
    while (p && p !== h) { if (p.matches('a, button')) return true; p = p.parentElement }
    return false
  }
  const loi = []
  const ten = el => (el.getAttribute('aria-label') || el.textContent || el.getAttribute('alt') || el.tagName).trim().replace(/\\s+/g, ' ').slice(0, 40)
  const rs = ds.map(el => ({ el, r: el.getBoundingClientRect(), t: ten(el) }))
  for (let i = 0; i < rs.length; i++) {
    const a = rs[i]
    if (a.r.left < hr.left - 1 || a.r.right > hr.right + 1 || a.r.right > window.innerWidth + 1) loi.push('tràn ngang: ' + a.t)
    const cx = Math.min(window.innerWidth - 1, Math.max(0, a.r.left + a.r.width / 2)), cy = a.r.top + a.r.height / 2
    const tai = document.elementFromPoint(cx, cy)
    if (tai && !a.el.contains(tai) && !tai.contains(a.el)) loi.push('bị che: ' + a.t + ' (bởi ' + ten(tai) + ')')
    for (let j = i + 1; j < rs.length; j++) {
      const b = rs[j]
      if (a.el.contains(b.el) || b.el.contains(a.el)) continue
      const x = Math.min(a.r.right, b.r.right) - Math.max(a.r.left, b.r.left)
      const y = Math.min(a.r.bottom, b.r.bottom) - Math.max(a.r.top, b.r.top)
      if (x > 1 && y > 1) loi.push('chồng: ' + a.t + ' ↔ ' + b.t)
    }
  }
  if (hr.height > 96) loi.push('header cao ' + Math.round(hr.height) + 'px (hơn một hàng)')
  return { loi, muc: rs.map(x => x.t), cao: Math.round(hr.height) }
})()`

const dn = await dangNhap()
const { gui, dong } = await moChrome()
let tongLoi = 0
try {
  await gui('Page.enable')
  await gui('Runtime.enable')
  for (const kieu of dn ? ['khach', 'dang-nhap'] : ['khach']) {
    // đặt / xoá phiên trên đúng origin
    await gui('Page.navigate', { url: GOC + '/robots.txt' })
    await ngu(800)
    const js = kieu === 'khach'
      ? 'localStorage.clear(); sessionStorage.clear(); 1'
      : `localStorage.setItem('htbb_user', ${JSON.stringify(JSON.stringify({ token: dn.nc.token, info: dn.nc.user }))});`
        + (dn.ad ? `sessionStorage.setItem('htbb_admin', ${JSON.stringify(JSON.stringify({ token: dn.ad.token, info: dn.ad.admin }))});` : '') + '1'
    await gui('Runtime.evaluate', { expression: js })
    for (const rong of DS_RONG) {
      await gui('Emulation.setDeviceMetricsOverride', { width: rong, height: 900, deviceScaleFactor: 1, mobile: false })
      await gui('Page.navigate', { url: GOC + TRANG })
      await ngu(rong === DS_RONG[0] ? 6000 : 3500)
      const { result } = await gui('Runtime.evaluate', { expression: KIEM_JS, returnByValue: true })
      const kq = result.value ?? { loi: ['không chạy được kiểm'] }
      const anh = await gui('Page.captureScreenshot', { format: 'png', clip: { x: 0, y: 0, width: rong, height: CAO, scale: 1 } })
      const tep = join(RA, `dau-trang-${kieu}-${rong}.png`)
      writeFileSync(tep, Buffer.from(anh.data, 'base64'))
      tongLoi += kq.loi.length
      console.log(`${kq.loi.length ? 'LOI' : 'ok '} ${kieu} ${rong}px cao=${kq.cao} [${(kq.muc ?? []).join(' | ')}] -> ${tep}`)
      for (const l of kq.loi) console.log('    - ' + l)
      if (MO_MENU && kieu === 'dang-nhap') {
        await gui('Runtime.evaluate', { expression: `(() => { const b = [...document.querySelectorAll('header button')].find(x => (x.getAttribute('aria-label') || '').startsWith('Tài khoản') || ['Open menu', 'Mở menu'].includes(x.getAttribute('aria-label') || '') && x.offsetParent); b && b.dispatchEvent(new PointerEvent('pointerdown', { bubbles: true, button: 0, pointerType: 'mouse' })); b && b.click(); return 1 })()` })
        await ngu(1200)
        const anh2 = await gui('Page.captureScreenshot', { format: 'png', clip: { x: 0, y: 0, width: rong, height: 520, scale: 1 } })
        writeFileSync(join(RA, `dau-trang-${kieu}-${rong}-menu.png`), Buffer.from(anh2.data, 'base64'))
      }
    }
  }
} finally {
  dong()
}
console.log(tongLoi ? `HONG: ${tongLoi} lỗi` : 'DAT: đầu trang không chồng lấn ở mọi bề rộng')
process.exit(tongLoi ? 1 : 0)
