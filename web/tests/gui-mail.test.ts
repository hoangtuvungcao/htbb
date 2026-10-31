/**
 * Kiểm thử máy khách SMTP tối giản (server/utils/mailThuan.ts) với máy chủ SMTP GIẢ chạy trên 127.0.0.1
 * — không ra mạng. Chứng chỉ TLS tự ký sinh tạm bằng openssl (không có openssl thì bỏ qua phần TLS).
 */
import { test, before, after } from 'node:test'
import assert from 'node:assert/strict'
import { execFileSync } from 'node:child_process'
import { mkdtempSync, readFileSync, rmSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { createServer as taoMayNet, type AddressInfo, type Server, type Socket } from 'node:net'
import { createServer as taoMayTls, TLSSocket } from 'node:tls'
import {
  chamDuLieuSmtp, chuanHoaMatKhauUngDung, dungThuMime, guiSmtp, maHoaHeaderMail, tachTraLoiSmtp, type CauHinhSmtp,
} from '../server/utils/mailThuan.ts'

let key = ''
let cert = ''
let thuMucTam = ''

before(() => {
  thuMucTam = mkdtempSync(join(tmpdir(), 'htbb-smtp-'))
  try {
    execFileSync('openssl', [
      'req', '-x509', '-newkey', 'rsa:2048', '-nodes', '-days', '2', '-subj', '/CN=localhost',
      '-addext', 'subjectAltName=DNS:localhost',
      '-keyout', join(thuMucTam, 'k.pem'), '-out', join(thuMucTam, 'c.pem'),
    ], { stdio: 'ignore' })
    key = readFileSync(join(thuMucTam, 'k.pem'), 'utf8')
    cert = readFileSync(join(thuMucTam, 'c.pem'), 'utf8')
  } catch {
    key = ''
  }
})
after(() => {
  if (thuMucTam) rmSync(thuMucTam, { recursive: true, force: true })
})

// ---------------------------------------------------------------- máy chủ SMTP giả

interface KichBan {
  /** Dòng EHLO trả về (không gồm dòng đầu). */
  moRong?: string[]
  /** Trả lời cho AUTH (mặc định 235). */
  maAuth?: number
  /** Chào không gửi gì (thử hết giờ). */
  imLang?: boolean
  /** Gửi lố một trả lời trước khi bật TLS (chèn lệnh STARTTLS). */
  choLoTruocTls?: boolean
}

interface MayGia {
  port: number
  nhatKy: string[]
  duLieu: string[]
  dong: () => Promise<void>
}

function phucVu(socketDau: Socket, kb: KichBan, nhatKy: string[], duLieu: string[], tlsNgam: boolean): void {
  let s: Socket = socketDau
  let boDem = ''
  let dangData = false
  let data = ''
  let choAuthLogin = 0
  let daTls = tlsNgam
  const gui = (x: string) => s.write(`${x}\r\n`)

  const xuLy = (dong: string) => {
    if (dangData) {
      if (dong === '.') {
        dangData = false
        duLieu.push(data)
        data = ''
        gui('250 2.0.0 OK queued')
      } else {
        data += `${dong}\r\n`
      }
      return
    }
    nhatKy.push(dong)
    if (choAuthLogin > 0) {
      choAuthLogin--
      gui(choAuthLogin > 0 ? '334 UGFzc3dvcmQ6' : `${kb.maAuth ?? 235} 2.7.0 done`)
      return
    }
    const lenh = dong.split(' ')[0]!.toUpperCase()
    if (lenh === 'EHLO') {
      const mo = kb.moRong ?? (daTls ? ['AUTH LOGIN PLAIN XOAUTH2', '8BITMIME'] : ['STARTTLS', '8BITMIME'])
      s.write(['250-smtp.gia chao ban', ...mo.map((m, i) => `250${i === mo.length - 1 ? ' ' : '-'}${m}`)].join('\r\n') + '\r\n')
    } else if (lenh === 'STARTTLS') {
      gui('220 2.0.0 Ready to start TLS' + (kb.choLoTruocTls ? '\r\n250 lo' : ''))
      const tho = s
      tho.removeAllListeners('data')
      const t = new TLSSocket(tho, { isServer: true, key, cert })
      s = t
      daTls = true
      t.on('data', nhan)
      t.on('error', () => {})
    } else if (lenh === 'AUTH') {
      if (dong.toUpperCase().startsWith('AUTH LOGIN')) {
        choAuthLogin = 2
        gui('334 VXNlcm5hbWU6')
      } else {
        gui(`${kb.maAuth ?? 235} 2.7.0 Accepted`)
      }
    } else if (lenh === 'MAIL' || lenh === 'RCPT') {
      gui('250 2.1.0 OK')
    } else if (lenh === 'DATA') {
      dangData = true
      gui('354 Go ahead')
    } else if (lenh === 'QUIT') {
      gui('221 2.0.0 bye')
      s.end()
    } else {
      gui('502 5.5.1 Unrecognized')
    }
  }
  const nhan = (b: Buffer) => {
    boDem += b.toString('latin1')
    let i: number
    while ((i = boDem.indexOf('\r\n')) >= 0) {
      const dong = boDem.slice(0, i)
      boDem = boDem.slice(i + 2)
      xuLy(dong)
    }
  }
  s.on('data', nhan)
  s.on('error', () => {})
  if (!kb.imLang) gui('220 smtp.gia ESMTP san sang')
}

async function moMayGia(kb: KichBan, tlsNgam: boolean): Promise<MayGia> {
  const nhatKy: string[] = []
  const duLieu: string[] = []
  const socketMo = new Set<Socket>()
  const may: Server = tlsNgam
    ? taoMayTls({ key, cert }, s => phucVu(s, kb, nhatKy, duLieu, true))
    : taoMayNet(s => phucVu(s, kb, nhatKy, duLieu, false))
  may.on(tlsNgam ? 'secureConnection' : 'connection', (s: Socket) => {
    socketMo.add(s)
    s.on('close', () => socketMo.delete(s))
  })
  await new Promise<void>(r => may.listen(0, '127.0.0.1', r))
  return {
    port: (may.address() as AddressInfo).port,
    nhatKy,
    duLieu,
    dong: () => new Promise<void>((r) => {
      for (const s of socketMo) s.destroy()
      may.close(() => r())
    }),
  }
}

function cauHinh(port: number, them: Partial<CauHinhSmtp> = {}): CauHinhSmtp {
  return {
    host: '127.0.0.1',
    port,
    user: 'htbb.test@gmail.com',
    pass: 'abcdefghijklmnop',
    hetGioMs: 2000,
    tls: { servername: 'localhost', ca: cert },
    ...them,
  }
}

const THU = (from: string) => dungThuMime(
  { to: 'nguoichoi@example.com', subject: 'Mã xác nhận đặt lại mật khẩu', text: 'Mã: ABCD\n.dong bat dau bang cham', html: '<b>ABCD</b>' },
  'Huyền Thoại Bảo Bối',
  from,
)

// ---------------------------------------------------------------- hàm thuần

test('tách trả lời nhiều dòng, giữ phần chưa trọn', () => {
  const { traLoi, du } = tachTraLoiSmtp('250-smtp.gmail.com\r\n250-AUTH LOGIN PLAIN\r\n250 SMTPUTF8\r\n220 xin')
  assert.deepEqual(traLoi, [{ ma: 250, dong: ['smtp.gmail.com', 'AUTH LOGIN PLAIN', 'SMTPUTF8'] }])
  assert.equal(du, '220 xin')
  const hai = tachTraLoiSmtp('250 OK\r\n354 Go\r\n')
  assert.deepEqual(hai.traLoi.map(t => t.ma), [250, 354])
  assert.equal(hai.du, '')
  // Dòng nhiều phần chưa có dòng kết thúc -> chưa trả về gì, giữ nguyên bộ đệm.
  const doDang = tachTraLoiSmtp('250-a\r\n250-b\r\n')
  assert.equal(doDang.traLoi.length, 0)
  assert.equal(doDang.du, '250-a\r\n250-b\r\n')
  assert.throws(() => tachTraLoiSmtp('xin chao\r\n'))
  assert.throws(() => tachTraLoiSmtp('250-a\r\n251 b\r\n'))
})

test('mật khẩu ứng dụng: bỏ khoảng trắng nhóm 4 ký tự', () => {
  assert.equal(chuanHoaMatKhauUngDung(' abcd efgh\tijkl  mnop \n'), 'abcdefghijklmnop')
})

test('nhân đôi dấu chấm đầu dòng, chuẩn CRLF, kết thúc CRLF.CRLF', () => {
  assert.equal(chamDuLieuSmtp('a\n.b\r\n..c\nd'), 'a\r\n..b\r\n...c\r\nd\r\n.\r\n')
  assert.equal(chamDuLieuSmtp('.\r\n'), '..\r\n.\r\n')
})

test('header tiếng Việt mã hoá RFC 2047, ASCII giữ nguyên; thư không chèn được header', () => {
  assert.equal(maHoaHeaderMail('Hello'), 'Hello')
  assert.match(maHoaHeaderMail('Mã xác nhận'), /^=\?UTF-8\?B\?[A-Za-z0-9+/=]+\?=$/)
  const thu = dungThuMime({ to: 'a@b.c\r\nBcc: x@y.z', subject: 'x', text: 't', html: 'h' }, 'Tên', 'gui@gmail.com')
  assert.ok(!/^Bcc:/m.test(thu))
  assert.match(thu, /^From: =\?UTF-8\?B\?.+\?= <gui@gmail\.com>$/m)
  assert.match(thu, /^Message-ID: <[0-9a-f]{32}@gmail\.com>$/m)
  // eslint-disable-next-line no-control-regex
  assert.ok(/^[\x00-\x7F]*$/.test(thu), 'thư toàn ASCII')
})

// ---------------------------------------------------------------- hội thoại SMTP

test('cổng TLS ngầm (465): EHLO -> AUTH PLAIN -> MAIL/RCPT/DATA -> QUIT', async (t) => {
  if (!key) return t.skip('không có openssl')
  const may = await moMayGia({}, true)
  try {
    const thu = THU('htbb.test@gmail.com')
    // Máy giả nghe cổng ngẫu nhiên -> ép TLS ngầm như cổng 465 bằng tlsNgam.
    await guiSmtp({ ...cauHinh(may.port), tlsNgam: true }, { from: 'htbb.test@gmail.com', to: ['nguoichoi@example.com'], thu })
    assert.equal(may.nhatKy[0]!.startsWith('EHLO '), true)
    const auth = may.nhatKy.find(d => d.startsWith('AUTH PLAIN '))!
    assert.equal(Buffer.from(auth.slice(11), 'base64').toString(), '\0htbb.test@gmail.com\0abcdefghijklmnop')
    assert.deepEqual(may.nhatKy.slice(2), ['MAIL FROM:<htbb.test@gmail.com>', 'RCPT TO:<nguoichoi@example.com>', 'DATA', 'QUIT'])
    assert.equal(may.duLieu.length, 1)
    // Máy chủ bỏ dấu chấm nhân đôi -> nhận lại đúng thư gốc.
    const nhan = may.duLieu[0]!.split('\r\n').map(d => (d.startsWith('..') ? d.slice(1) : d)).join('\r\n')
    assert.equal(nhan, thu.endsWith('\r\n') ? thu : `${thu}\r\n`)
  } finally {
    await may.dong()
  }
})

test('cổng thường (587): STARTTLS rồi EHLO lại, sau đó mới AUTH', async (t) => {
  if (!key) return t.skip('không có openssl')
  const may = await moMayGia({}, false)
  try {
    await guiSmtp(cauHinh(may.port), { from: 'a@gmail.com', to: ['b@example.com'], thu: THU('a@gmail.com') })
    assert.equal(may.nhatKy[1], 'STARTTLS')
    assert.equal(may.nhatKy[2]!.startsWith('EHLO '), true, 'EHLO lại sau STARTTLS')
    assert.ok(may.nhatKy.some(d => d.startsWith('AUTH PLAIN ')))
  } finally {
    await may.dong()
  }
})

test('AUTH LOGIN khi máy chủ chỉ quảng bá LOGIN', async (t) => {
  if (!key) return t.skip('không có openssl')
  const may = await moMayGia({ moRong: ['AUTH LOGIN'] }, true)
  try {
    await guiSmtp({ ...cauHinh(may.port), tlsNgam: true }, { from: 'a@gmail.com', to: ['b@example.com'], thu: THU('a@gmail.com') })
    const i = may.nhatKy.indexOf('AUTH LOGIN')
    assert.ok(i > 0)
    assert.equal(Buffer.from(may.nhatKy[i + 1]!, 'base64').toString(), 'htbb.test@gmail.com')
    assert.equal(Buffer.from(may.nhatKy[i + 2]!, 'base64').toString(), 'abcdefghijklmnop')
  } finally {
    await may.dong()
  }
})

test('cổng thường mà máy chủ không có STARTTLS: KHÔNG gửi mật khẩu', async () => {
  const may = await moMayGia({ moRong: ['AUTH LOGIN PLAIN'] }, false)
  try {
    await assert.rejects(
      guiSmtp(cauHinh(may.port), { from: 'a@gmail.com', to: ['b@example.com'], thu: 'x' }),
      /STARTTLS/,
    )
    assert.ok(!may.nhatKy.some(d => d.startsWith('AUTH')))
  } finally {
    await may.dong()
  }
})

test('chèn dữ liệu trước STARTTLS bị từ chối', async (t) => {
  if (!key) return t.skip('không có openssl')
  const may = await moMayGia({ choLoTruocTls: true }, false)
  try {
    await assert.rejects(
      guiSmtp(cauHinh(may.port), { from: 'a@gmail.com', to: ['b@example.com'], thu: 'x' }),
      /truoc STARTTLS/,
    )
  } finally {
    await may.dong()
  }
})

test('sai mật khẩu (535): lỗi có mã, KHÔNG chứa mật khẩu hay chuỗi AUTH', async (t) => {
  if (!key) return t.skip('không có openssl')
  const may = await moMayGia({ maAuth: 535 }, true)
  try {
    const ch = { ...cauHinh(may.port), tlsNgam: true }
    const chuoiAuth = Buffer.from(`\0${ch.user}\0${ch.pass}`).toString('base64')
    await assert.rejects(
      guiSmtp(ch, { from: 'a@gmail.com', to: ['b@example.com'], thu: 'x' }),
      (e: any) => {
        assert.equal(e.ma, 535)
        assert.ok(!String(e.message).includes(ch.pass))
        assert.ok(!String(e.message).includes(chuoiAuth))
        assert.ok(!String(e.stack).includes(ch.pass))
        return true
      },
    )
    assert.ok(!may.nhatKy.some(d => d.startsWith('MAIL')))
  } finally {
    await may.dong()
  }
})

test('máy chủ im lặng: hết giờ, không treo', async () => {
  const may = await moMayGia({ imLang: true }, false)
  try {
    const bd = Date.now()
    await assert.rejects(
      guiSmtp(cauHinh(may.port, { hetGioMs: 300 }), { from: 'a@gmail.com', to: ['b@example.com'], thu: 'x' }),
      /het gio/,
    )
    assert.ok(Date.now() - bd < 2000)
  } finally {
    await may.dong()
  }
})

test('không kết nối được: báo lỗi gọn', async () => {
  const may = await moMayGia({}, false)
  const port = may.port
  await may.dong()
  await assert.rejects(
    guiSmtp(cauHinh(port), { from: 'a@gmail.com', to: ['b@example.com'], thu: 'x' }),
    /khong ket noi duoc/,
  )
})

test('địa chỉ có xuống dòng / dấu <> bị từ chối trước khi kết nối', async () => {
  await assert.rejects(guiSmtp(cauHinh(1), { from: 'a@gmail.com\r\nRCPT TO:<x@y>', to: ['b@example.com'], thu: 'x' }), /dia chi/)
  await assert.rejects(guiSmtp(cauHinh(1), { from: 'a@gmail.com', to: ['<b@example.com>'], thu: 'x' }), /dia chi/)
})
