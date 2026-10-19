#!/usr/bin/env node
/**
 * Kiểm MỌI trang công khai bằng Chrome headless (CDP qua WebSocket có sẵn của Node 22, không cần puppeteer).
 *
 *   node scripts/kiem-trang-cong-khai.mjs --goc http://127.0.0.1:3000 --ra /tmp/kiem-trang \
 *        [--tk nguoichoi --mk ...] [--rong 390,1280,1440,1920,2560] [--toi] [--anh] [--trang /,/huong-dan]
 *
 * Mỗi trang × mỗi bề rộng (sáng; thêm tối nếu có --toi), đăng nhập người chơi nếu có --tk/--mk. HỎNG khi:
 *   - console có lỗi (console.error, ngoại lệ chưa bắt, lỗi tải tài nguyên trừ 401/403 có chủ ý);
 *   - trang cuộn ngang (scrollWidth > innerWidth);
 *   - ảnh hỏng (<img> tải xong mà naturalWidth = 0);
 *   - khung thả xuống ở đầu trang (Cẩm nang ▾, Thêm ▾, menu ảnh đại diện) tràn khỏi màn hình hoặc có chữ bị
 *     cắt cụt (tràn mà không có dấu … / line-clamp).
 * Cảnh báo (không làm hỏng): chữ có độ tương phản < 3:1 so với nền đặc gần nhất.
 * --anh: chụp ảnh cả trang (tối đa 7000px) vào --ra. Mã thoát 1 nếu có lỗi.
 */
import { spawn } from 'node:child_process'
import { mkdirSync, writeFileSync, mkdtempSync, rmSync, readdirSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { tmpdir } from 'node:os'
import { fileURLToPath } from 'node:url'

const arg = (k, d) => { const i = process.argv.indexOf(`--${k}`); return i > 0 ? process.argv[i + 1] : d }
const co = k => process.argv.includes(`--${k}`)
const GOC = arg('goc', 'http://127.0.0.1:3000').replace(/\/$/, '')
const RA = arg('ra', join(tmpdir(), 'kiem-trang'))
const TK = arg('tk', '')
const MK = arg('mk', '')
const DS_RONG = arg('rong', '390,1280,1440,1920,2560').split(',').map(Number)
const CHROME = arg('chrome', '/usr/bin/google-chrome')
const ANH = co('anh')
const CHE_DO = co('toi') ? ['sang', 'toi'] : ['sang']
/** --rong-toi 390,1920: chế độ tối chỉ kiểm ở các bề rộng này (mặc định: mọi bề rộng). */
const DS_RONG_TOI = arg('rong-toi', '') ? arg('rong-toi', '').split(',').map(Number) : null
mkdirSync(RA, { recursive: true })
const ngu = ms => new Promise(r => setTimeout(r, ms))

// ---- danh sách trang: trang tĩnh + mọi trang cẩm nang + 1 Pokédex + 1 bài tin ----
const GOC_WEB = arg('web', join(dirname(fileURLToPath(import.meta.url)), '..'))
const camNang = readdirSync(join(GOC_WEB, 'app/pages/cam-nang')).filter(f => f.endsWith('.vue') && !f.startsWith('[') && f !== 'index.vue').map(f => '/cam-nang/' + f.slice(0, -4))
async function dsTrang() {
  const ds = ['/', '/huong-dan', '/download', '/cam-nang', ...camNang, '/pokedex', '/pokedex/1', '/pokedex/133', '/ranking', '/news',
    '/gioi-thieu', '/giay-phep', '/dieu-khoan', '/quyen-rieng-tu', '/dieu-khoan-thanh-toan', '/chinh-sach-hoan-tien',
    '/register', '/login', '/quen-mat-khau', '/account', '/kichhoat', '/naptien', '/thanh-vien', '/khong-co-trang-nay']
  try {
    const tin = await (await fetch(GOC + '/api/tin-tuc')).json()
    for (const t of tin.slice(0, 2)) ds.push('/news/' + t.slug)
  } catch {}
  const chon = arg('trang', '')
  return chon ? chon.split(',') : ds
}

async function dangNhap() {
  if (!TK) return null
  const r = await fetch(GOC + '/api/auth/login', { method: 'POST', headers: { 'content-type': 'application/json', origin: GOC }, body: JSON.stringify({ username: TK, password: MK }) })
  if (!r.ok) throw new Error(`đăng nhập: HTTP ${r.status}`)
  return r.json()
}

async function moChrome() {
  const thuMuc = mkdtempSync(join(tmpdir(), 'kiem-trang-chrome-'))
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
  const trang = (await (await fetch(`http://127.0.0.1:${port}/json`)).json()).find(x => x.type === 'page')
  const sock = new WebSocket(trang.webSocketDebuggerUrl)
  await new Promise(r => sock.addEventListener('open', r, { once: true }))
  let id = 0
  const cho = new Map()
  const nghe = []
  sock.addEventListener('message', (e) => {
    const m = JSON.parse(e.data)
    if (m.id && cho.has(m.id)) { cho.get(m.id)(m); cho.delete(m.id) }
    else if (m.method) for (const f of nghe) f(m)
  })
  const gui = (method, params = {}) => new Promise((ok, loi) => {
    const i = ++id
    cho.set(i, m => (m.error ? loi(new Error(`${method}: ${m.error.message}`)) : ok(m.result)))
    sock.send(JSON.stringify({ id: i, method, params }))
  })
  const dong = () => { try { sock.close() } catch {} p.kill('SIGKILL'); setTimeout(() => { try { rmSync(thuMuc, { recursive: true, force: true }) } catch {} }, 500) }
  return { gui, dong, nghe: f => nghe.push(f) }
}

/** Trong trang: cuộn ngang, ảnh hỏng, tương phản thấp. */
const KIEM_TRANG = `(async () => {
  // cuộn hết trang để ảnh lazy tải, rồi về đầu
  const H = document.documentElement.scrollHeight
  for (let y = 0; y < H; y += innerHeight) { scrollTo(0, y); await new Promise(r => setTimeout(r, 150)) }
  scrollTo(0, 0); await new Promise(r => setTimeout(r, 800))
  const loi = [], canh = []
  const sw = document.documentElement.scrollWidth
  if (sw > innerWidth + 1) {
    const thua = [...document.body.querySelectorAll('*')].filter(e => { const r = e.getBoundingClientRect(); return r.width > 0 && r.right > innerWidth + 1 })
      .filter(e => !thuaTrong(e)).slice(0, 4).map(moTa)
    loi.push('cuộn ngang ' + sw + '>' + innerWidth + ': ' + thua.join(' ; '))
  }
  function thuaTrong(e) { let p = e.parentElement; while (p && p !== document.body) { const s = getComputedStyle(p); if (/(hidden|auto|scroll|clip)/.test(s.overflowX)) return true; p = p.parentElement } return false }
  function moTa(e) { return e.tagName.toLowerCase() + (e.id ? '#' + e.id : '') + '.' + String(e.className && e.className.baseVal !== undefined ? e.className.baseVal : e.className).split(/\\s+/).slice(0, 3).join('.') + ' "' + (e.textContent || '').trim().slice(0, 30) + '"' }
  for (const img of document.images) if (img.complete && img.naturalWidth === 0 && img.getAttribute('src')) loi.push('ảnh hỏng: ' + img.getAttribute('src'))
  // tương phản: phần tử lá có chữ
  // Màu CSS bất kỳ (rgb, oklab, color-mix… của Tailwind 4) -> rgba qua canvas 1×1, rồi độ chói tương đối WCAG.
  const cv = document.createElement('canvas'); cv.width = cv.height = 1
  const cx = cv.getContext('2d', { willReadFrequently: true })
  const lum = (c) => {
    if (!c || c === 'transparent') return null
    cx.clearRect(0, 0, 1, 1); cx.fillStyle = '#000'; cx.fillStyle = c; cx.fillRect(0, 0, 1, 1)
    const [r, g, b, a8] = cx.getImageData(0, 0, 1, 1).data
    const a = a8 / 255
    if (a === 0) return { L: 0, a: 0 }
    return { L: [r, g, b].map(v => { v /= 255; return v <= 0.03928 ? v / 12.92 : ((v + 0.055) / 1.055) ** 2.4 }).reduce((s, v, i) => s + v * [0.2126, 0.7152, 0.0722][i], 0), a }
  }
  const toi = document.documentElement.classList.contains('dark')
  // nền đặc gần nhất; gặp ảnh nền / gradient thì bỏ qua (không đoán được)
  const nen = e => { let p = e; while (p) { const s = getComputedStyle(p); if (s.backgroundImage !== 'none' && !p.matches('body, html, .bg-pattern')) return null; const l = lum(s.backgroundColor); if (l && l.a > 0.9) return l; p = p.parentElement } return lum(toi ? '#0b1530' : '#ffffff') }
  // có ảnh / phần tử định vị tuyệt đối đè dưới chữ (chữ trên ảnh) -> bỏ qua
  const treAnh = e => { let p = e; for (let i = 0; p && i < 6; i++, p = p.parentElement) { if (p.querySelector(':scope > img, :scope > picture, :scope > video')) return true } return false }
  let dem = 0
  for (const e of document.body.querySelectorAll('p, span, a, h1, h2, h3, h4, li, td, th, label, button, div')) {
    if (dem > 1500) break
    if (![...e.childNodes].some(n => n.nodeType === 3 && n.textContent.trim().length > 1)) continue
    const r = e.getBoundingClientRect(); if (r.width < 2 || r.height < 2) continue
    const s = getComputedStyle(e); if (s.visibility === 'hidden' || Number(s.opacity) < 0.5) continue
    dem++
    if (s.webkitTextStroke && !/^0px/.test(s.webkitTextStrokeWidth || '0px')) continue // chữ viền (text-gilded)
    if (treAnh(e)) continue
    const c = lum(s.color), b = nen(e); if (!c || !b || c.a < 0.5) continue
    const ti = (Math.max(c.L, b.L) + 0.05) / (Math.min(c.L, b.L) + 0.05)
    if (ti < 3) canh.push('tương phản ' + ti.toFixed(2) + ': ' + moTa(e))
  }
  return { loi, canh: [...new Set(canh)].slice(0, 8), cao: H }
})()`

/** Các nút mở khung thả xuống ở đầu trang (mục menu có con + nút ảnh đại diện): tên + toạ độ tâm. */
const DS_NUT_MENU = `(() => [...document.querySelectorAll('header nav [data-navigation-menu-trigger], header button[aria-haspopup="menu"], header button[data-slot="toggle"]')]
  .filter(b => b.offsetParent && b.getBoundingClientRect().width > 0)
  .map(b => { const r = b.getBoundingClientRect(); return { ten: (b.getAttribute('aria-label') || b.textContent || '').trim().replace(/\\s+/g, ' ').slice(0, 30), x: r.left + r.width / 2, y: r.top + r.height / 2, menu: b.getAttribute('aria-haspopup') === 'menu' || b.dataset.slot === 'toggle' } }))()`

/** Khung thả xuống đang mở: tràn màn hình? chữ bị cắt cụt (tràn mà không có … / line-clamp)? */
const KIEM_KHUNG = nhan => `(() => {
  const loi = [], nhan = ${JSON.stringify(nhan)}
  const khung = [...document.querySelectorAll('header [data-slot="viewport"], [role="menu"], [role="dialog"]')].filter(k => k.getBoundingClientRect().height > 20)
  if (!khung.length) return ['mở "' + nhan + '": không thấy khung thả xuống']
  for (const k of khung) {
    const kr = k.getBoundingClientRect()
    if (kr.left < -1 || kr.right > innerWidth + 1 || kr.bottom > innerHeight + 1) loi.push('"' + nhan + '": khung tràn màn hình [' + Math.round(kr.left) + ',' + Math.round(kr.right) + '] rộng ' + innerWidth)
    for (const e of k.querySelectorAll('*')) {
      if (![...e.childNodes].some(n => n.nodeType === 3 && n.textContent.trim())) continue
      const r = e.getBoundingClientRect(); if (r.width <= 1 || r.height <= 1) continue // bỏ chữ sr-only
      const s = getComputedStyle(e)
      const coCham = s.textOverflow === 'ellipsis' || (s.webkitLineClamp && s.webkitLineClamp !== 'none')
      const t = e.textContent.trim().slice(0, 50)
      if (r.right > kr.right + 1 || r.left < kr.left - 1) loi.push('"' + nhan + '": chữ ra ngoài khung: ' + t)
      else if (!coCham && e.scrollWidth > e.clientWidth + 1 && /(hidden|clip)/.test(s.overflowX)) loi.push('"' + nhan + '": chữ bị cắt không có …: ' + t)
      else if (!coCham && e.scrollHeight > e.clientHeight + 2 && /(hidden|clip)/.test(s.overflowY)) loi.push('"' + nhan + '": chữ bị cắt dọc: ' + t)
      // tổ tiên cắt chữ (overflow hidden) mà chữ vượt mép tổ tiên
      let p = e.parentElement
      while (p && p !== k) {
        const ps = getComputedStyle(p)
        if (/(hidden|clip)/.test(ps.overflowX)) { const pr = p.getBoundingClientRect(); if (r.right > pr.right + 1 && !coCham) { loi.push('"' + nhan + '": chữ bị khung con cắt: ' + t); break } }
        p = p.parentElement
      }
    }
  }
  return loi
})()`

async function kiemMenu(gui, chup) {
  const loi = []
  const { result } = await gui('Runtime.evaluate', { expression: DS_NUT_MENU, returnByValue: true })
  for (const n of result.value ?? []) {
    const chuot = (type, x, y) => gui('Input.dispatchMouseEvent', { type, x, y, button: 'left', buttons: type === 'mousePressed' ? 1 : 0, clickCount: 1, pointerType: 'mouse' })
    await chuot('mouseMoved', n.x, n.y)
    if (n.menu) { await chuot('mousePressed', n.x, n.y); await chuot('mouseReleased', n.x, n.y) }
    await ngu(800)
    const k = await gui('Runtime.evaluate', { expression: KIEM_KHUNG(n.ten), returnByValue: true })
    loi.push(...(k.result.value ?? []))
    if (chup) await chup(n.ten)
    await gui('Input.dispatchKeyEvent', { type: 'keyDown', key: 'Escape', code: 'Escape', windowsVirtualKeyCode: 27 })
    await gui('Input.dispatchKeyEvent', { type: 'keyUp', key: 'Escape', code: 'Escape', windowsVirtualKeyCode: 27 })
    await chuot('mouseMoved', 5, 600)
    await ngu(500)
  }
  return loi
}

const ds = await dsTrang()
const dn = await dangNhap()
const { gui, dong, nghe } = await moChrome()
let loiConsole = []
nghe((m) => {
  if (m.method === 'Runtime.exceptionThrown') loiConsole.push('ngoại lệ: ' + (m.params.exceptionDetails.exception?.description || m.params.exceptionDetails.text).split('\n')[0])
  if (m.method === 'Runtime.consoleAPICalled' && m.params.type === 'error') loiConsole.push('console.error: ' + m.params.args.map(a => a.value ?? a.description ?? '').join(' ').slice(0, 160))
  if (m.method === 'Log.entryAdded' && m.params.entry.level === 'error') {
    const t = m.params.entry.text + ' ' + (m.params.entry.url ?? '')
    if (!/status of 40[13]/.test(t) && !(/status of 404/.test(t) && /khong-co-trang-nay/.test(t))) loiConsole.push('log: ' + t.slice(0, 160))
  }
})
const tong = { loi: 0, canh: 0 }
const baoCao = []
try {
  await gui('Page.enable'); await gui('Runtime.enable'); await gui('Log.enable')
  // "Giảm chuyển động": khối FxReveal hiện ngay (ảnh chụp không bị khoảng trống đang chờ hiệu ứng).
  await gui('Emulation.setEmulatedMedia', { features: [{ name: 'prefers-reduced-motion', value: 'reduce' }] })
  for (const cheDo of CHE_DO) {
    await gui('Page.navigate', { url: GOC + '/robots.txt' })
    await ngu(600)
    await gui('Runtime.evaluate', {
      expression: `localStorage.clear(); sessionStorage.clear(); localStorage.setItem('nuxt-color-mode', '${cheDo === 'toi' ? 'dark' : 'light'}');`
        + (dn ? `localStorage.setItem('htbb_user', ${JSON.stringify(JSON.stringify({ token: dn.token, info: dn.user }))});` : '') + '1',
    })
    for (const rong of DS_RONG) {
      if (cheDo === 'toi' && DS_RONG_TOI && !DS_RONG_TOI.includes(rong)) continue
      await gui('Emulation.setDeviceMetricsOverride', { width: rong, height: rong < 800 ? 844 : 1000, deviceScaleFactor: 1, mobile: rong < 800 })
      for (const duong of ds) {
        loiConsole = []
        await gui('Page.navigate', { url: GOC + duong })
        await ngu(2200)
        const { result } = await gui('Runtime.evaluate', { expression: KIEM_TRANG, returnByValue: true, awaitPromise: true })
        const kq = result.value ?? { loi: ['không chạy được kiểm'], canh: [] }
        if (cheDo === 'sang' && (duong === '/huong-dan' || duong === '/' || co('menu-moi-trang'))) {
          const chup = ANH
            ? async (ten) => {
              const a = await gui('Page.captureScreenshot', { format: 'png', clip: { x: 0, y: 0, width: rong, height: 720, scale: 1 } })
              writeFileSync(join(RA, `menu-${ten.replace(/[^\p{L}\d]+/gu, '-')}-${duong === '/' ? 'trang-chu' : duong.slice(1).replace(/\//g, '_')}-${rong}.png`), Buffer.from(a.data, 'base64'))
            }
            : null
          kq.loi.push(...await kiemMenu(gui, chup))
        }
        kq.loi.push(...new Set(loiConsole))
        if (ANH) {
          // chụp quá khung nhìn (không đổi cỡ khung nhìn: trang dùng 100svh sẽ bị kéo dài theo)
          const cao = Math.min(7000, kq.cao || 1000)
          const a = await gui('Page.captureScreenshot', { format: 'jpeg', quality: 70, captureBeyondViewport: true, clip: { x: 0, y: 0, width: rong, height: cao, scale: 1 } })
          const ten = (duong === '/' ? 'trang-chu' : duong.slice(1).replace(/\//g, '_')) + `-${rong}-${cheDo}.jpg`
          writeFileSync(join(RA, ten), Buffer.from(a.data, 'base64'))
        }
        tong.loi += kq.loi.length
        tong.canh += kq.canh.length
        const dong1 = `${kq.loi.length ? 'LOI' : 'ok '} ${cheDo} ${rong}px ${duong}`
        console.log(dong1)
        for (const l of kq.loi) console.log('    ✗ ' + l)
        for (const c of kq.canh) console.log('    ! ' + c)
        baoCao.push({ cheDo, rong, duong, ...kq })
      }
    }
  }
} finally {
  dong()
}
writeFileSync(join(RA, 'bao-cao.json'), JSON.stringify(baoCao, null, 1))
console.log(tong.loi ? `HONG: ${tong.loi} lỗi, ${tong.canh} cảnh báo` : `DAT: ${ds.length} trang × ${DS_RONG.length} bề rộng, ${tong.canh} cảnh báo tương phản`)
process.exit(tong.loi ? 1 : 0)
