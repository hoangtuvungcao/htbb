/**
 * GỬI MAIL — phần THUẦN (không auto-import của Nuxt, không đọc cấu hình): dựng thư MIME và một
 * máy khách SMTP tối giản chỉ dùng node:net / node:tls. guiMail.ts chọn đường gửi (Gmail API hay
 * SMTP) và lấy cấu hình; tests/gui-mail.test.ts chạy thẳng tệp này với máy chủ SMTP giả.
 *
 * Vì sao tự viết SMTP mà không thêm nodemailer: chỉ cần một việc (đăng nhập bằng mật khẩu ứng dụng
 * Gmail rồi gửi một thư đã dựng sẵn), luồng hội thoại cố định ~8 lệnh; tự viết ~200 dòng, không kéo
 * thêm phụ thuộc vào bản build Nitro, và kiểm soát được việc KHÔNG ghi mật khẩu ra log.
 *
 * Luồng: kết nối (cổng 465 = TLS ngay từ đầu; cổng khác = kết nối thường rồi BẮT BUỘC STARTTLS,
 * không bao giờ gửi AUTH khi chưa mã hoá) -> 220 -> EHLO -> AUTH PLAIN (hoặc LOGIN nếu máy chủ chỉ
 * có LOGIN) -> MAIL FROM -> RCPT TO -> DATA (nhân đôi dấu chấm đầu dòng) -> QUIT.
 *
 * KHÔNG BAO GIỜ đưa mật khẩu / chuỗi AUTH vào thông báo lỗi hay log: lỗi chỉ mang tên bước + mã trả
 * lời + dòng chữ máy chủ trả về.
 */
import { randomBytes } from 'node:crypto'
import { connect as ketNoiNet, isIP, type Socket } from 'node:net'
import { connect as ketNoiTls, type ConnectionOptions, type TLSSocket } from 'node:tls'

export interface ThuGui {
  to: string
  subject: string
  text: string
  html: string
}

/** Mật khẩu ứng dụng Google hiện dạng "abcd efgh ijkl mnop": bỏ mọi khoảng trắng. */
export function chuanHoaMatKhauUngDung(s: string): string {
  return s.replace(/\s+/g, '')
}

// ---------------------------------------------------------------------------
//  Dựng thư MIME
// ---------------------------------------------------------------------------

/** Bỏ CR/LF: giá trị đưa vào header mà có xuống dòng là chèn được header lạ. */
export function motDongMail(s: string): string {
  return s.replace(/[\r\n]+/g, ' ').trim()
}

/**
 * Mã hoá RFC 2047 cho header có chữ không phải ASCII. Một "encoded-word" tối đa 75
 * ký tự, nên chia chuỗi thành nhiều đoạn (không cắt đôi ký tự UTF-8), nối bằng
 * xuống dòng + khoảng trắng (folding). Chuỗi ASCII thuần giữ nguyên.
 */
export function maHoaHeaderMail(s: string): string {
  // eslint-disable-next-line no-control-regex
  if (/^[\x20-\x7E]*$/.test(s)) return s
  const doan: string[] = []
  let hienTai = ''
  for (const kyTu of s) {
    // 45 byte UTF-8 -> 60 ký tự base64, cộng 12 ký tự "=?UTF-8?B?" "?=" = 72 < 75.
    if (Buffer.byteLength(hienTai + kyTu, 'utf8') > 45) {
      doan.push(hienTai)
      hienTai = ''
    }
    hienTai += kyTu
  }
  if (hienTai !== '') doan.push(hienTai)
  return doan.map(d => `=?UTF-8?B?${Buffer.from(d, 'utf8').toString('base64')}?=`).join('\r\n ')
}

/** Nội dung base64, ngắt dòng 76 ký tự theo chuẩn MIME. */
function base64Dong(s: string): string {
  return (Buffer.from(s, 'utf8').toString('base64').match(/.{1,76}/g) ?? []).join('\r\n')
}

/** Thư dạng RFC 5322 (CRLF), multipart/alternative text + html, UTF-8 + base64 -> toàn ASCII. */
export function dungThuMime(thu: ThuGui, tenNguoiGui: string, diaChiGui: string): string {
  const ranh = `htbb_${randomBytes(12).toString('hex')}`
  const tenGui = motDongMail(tenNguoiGui).replace(/["\\]/g, '')
  const tu = /^[\x20-\x7E]*$/.test(tenGui) ? `"${tenGui}"` : maHoaHeaderMail(tenGui)
  const diaChi = motDongMail(diaChiGui)
  const tenMien = diaChi.includes('@') ? diaChi.slice(diaChi.lastIndexOf('@') + 1).replace(/[^A-Za-z0-9.-]/g, '') : ''
  const dong = [
    `From: ${tu} <${diaChi}>`,
    `To: ${motDongMail(thu.to)}`,
    `Subject: ${maHoaHeaderMail(motDongMail(thu.subject))}`,
    `Date: ${new Date().toUTCString().replace('GMT', '+0000')}`,
    `Message-ID: <${randomBytes(16).toString('hex')}@${tenMien || 'htbb.local'}>`,
    'MIME-Version: 1.0',
    `Content-Type: multipart/alternative; boundary="${ranh}"`,
    '',
    `--${ranh}`,
    'Content-Type: text/plain; charset="UTF-8"',
    'Content-Transfer-Encoding: base64',
    '',
    base64Dong(thu.text),
    `--${ranh}`,
    'Content-Type: text/html; charset="UTF-8"',
    'Content-Transfer-Encoding: base64',
    '',
    base64Dong(thu.html),
    `--${ranh}--`,
    '',
  ]
  return dong.join('\r\n')
}

/**
 * Thân lệnh DATA: chuẩn hoá xuống dòng về CRLF, dòng bắt đầu bằng "." thì thêm một "." (RFC 5321
 * 4.5.2), kết thúc bằng CRLF.CRLF.
 */
export function chamDuLieuSmtp(thu: string): string {
  let s = thu.replace(/\r\n|\r|\n/g, '\r\n')
  if (!s.endsWith('\r\n')) s += '\r\n'
  s = s.replace(/^\./gm, '..')
  return `${s}.\r\n`
}

// ---------------------------------------------------------------------------
//  Đọc trả lời SMTP (nhiều dòng "250-..." ... "250 ...")
// ---------------------------------------------------------------------------

export interface TraLoiSmtp {
  ma: number
  dong: string[]
}

/** Một dòng trả lời quá dài / máy chủ gửi rác: dừng thay vì nhớ mãi. */
const TOI_DA_BO_DEM = 64 * 1024

/**
 * Tách các trả lời TRỌN VẸN khỏi bộ đệm. Trả về các trả lời + phần còn dư (dòng chưa hết).
 * Ném lỗi khi dòng sai cú pháp (không phải 3 chữ số + ' ' / '-') hay mã đổi giữa chừng.
 */
export function tachTraLoiSmtp(boDem: string): { traLoi: TraLoiSmtp[], du: string } {
  const traLoi: TraLoiSmtp[] = []
  let dangGom: string[] = []
  let maGom = 0
  let viTri = 0
  let batDauTraLoi = 0
  for (;;) {
    const het = boDem.indexOf('\n', viTri)
    if (het < 0) break
    const dong = boDem.slice(viTri, het).replace(/\r$/, '')
    viTri = het + 1
    const m = /^(\d{3})([ -])(.*)$/.exec(dong) ?? /^(\d{3})()$/.exec(dong)
    if (!m) throw new Error(`smtp: tra loi sai cu phap`)
    const ma = Number(m[1])
    if (dangGom.length > 0 && ma !== maGom) throw new Error('smtp: ma tra loi nhieu dong khong khop')
    maGom = ma
    dangGom.push(m[3] ?? '')
    if (m[2] !== '-') {
      traLoi.push({ ma, dong: dangGom })
      dangGom = []
      batDauTraLoi = viTri
    }
  }
  return { traLoi, du: boDem.slice(batDauTraLoi) }
}

/** Đọc trả lời theo thứ tự trên một socket (đổi được socket khi nâng cấp STARTTLS). */
class DocTraLoi {
  private boDem = ''
  private hang: TraLoiSmtp[] = []
  private cho: { xong: (t: TraLoiSmtp) => void, loi: (e: Error) => void }[] = []
  private loi: Error | null = null
  private socket: Socket | null = null
  private readonly nghe = {
    data: (b: Buffer) => this.nhan(b),
    error: (e: Error) => this.hong(new Error(`smtp: loi ket noi (${(e as NodeJS.ErrnoException).code ?? e.message})`)),
    close: () => this.hong(new Error('smtp: may chu dong ket noi')),
  }

  gan(socket: Socket): void {
    this.go()
    this.socket = socket
    socket.on('data', this.nghe.data)
    socket.on('error', this.nghe.error)
    socket.on('close', this.nghe.close)
  }

  /** Thôi nghe socket hiện tại (trước khi bọc TLS). */
  go(): void {
    const s = this.socket
    if (!s) return
    s.off('data', this.nghe.data)
    s.off('error', this.nghe.error)
    s.off('close', this.nghe.close)
    this.socket = null
  }

  /** Còn dữ liệu chưa đọc (máy chủ gửi lố trước STARTTLS = tấn công chèn lệnh). */
  conDu(): boolean {
    return this.boDem !== '' || this.hang.length > 0
  }

  private nhan(b: Buffer): void {
    if (this.loi) return
    this.boDem += b.toString('latin1')
    if (this.boDem.length > TOI_DA_BO_DEM) {
      this.hong(new Error('smtp: tra loi qua dai'))
      return
    }
    try {
      const { traLoi, du } = tachTraLoiSmtp(this.boDem)
      this.boDem = du
      for (const t of traLoi) {
        const c = this.cho.shift()
        if (c) c.xong(t)
        else this.hang.push(t)
      }
    } catch (e) {
      this.hong(e as Error)
    }
  }

  hong(e: Error): void {
    if (this.loi) return
    this.loi = e
    for (const c of this.cho.splice(0)) c.loi(e)
  }

  doc(): Promise<TraLoiSmtp> {
    const t = this.hang.shift()
    if (t) return Promise.resolve(t)
    if (this.loi) return Promise.reject(this.loi)
    return new Promise((xong, loi) => this.cho.push({ xong, loi }))
  }
}

// ---------------------------------------------------------------------------
//  Gửi qua SMTP
// ---------------------------------------------------------------------------

export interface CauHinhSmtp {
  host: string
  port: number
  user: string
  /** Mật khẩu ứng dụng (đã bỏ khoảng trắng). */
  pass: string
  /** Tên máy khai trong EHLO. */
  tenMay?: string
  /** Chờ mỗi trả lời tối đa bao lâu (mặc định 15 giây). */
  hetGioMs?: number
  /** Cả phiên tối đa bao lâu (mặc định 60 giây). */
  hetGioTongMs?: number
  /** TLS ngay từ đầu (mặc định: cổng 465). false = kết nối thường + STARTTLS bắt buộc. */
  tlsNgam?: boolean
  /** Thêm tuỳ chọn TLS (test: ca/servername). */
  tls?: ConnectionOptions
}

export interface PhongBiSmtp {
  from: string
  to: string[]
  /** Thư đã dựng (dungThuMime). */
  thu: string
}

/** Lỗi SMTP: có mã trả lời (nếu có) — KHÔNG chứa mật khẩu / chuỗi AUTH. */
export class LoiSmtp extends Error {
  ma: number | null
  constructor(message: string, ma: number | null = null) {
    super(message)
    this.name = 'LoiSmtp'
    this.ma = ma
  }
}

/** Địa chỉ trong MAIL FROM / RCPT TO: một dòng, không có <>, không khoảng trắng. */
function diaChiLenh(s: string): string {
  const d = motDongMail(s)
  if (d === '' || /[\s<>]/.test(d)) throw new LoiSmtp('smtp: dia chi khong hop le')
  return d
}

/** Chữ máy chủ trả về, cắt ngắn để ghi log. */
function tomTat(t: TraLoiSmtp): string {
  return t.dong.join(' | ').replace(/[^\x20-\x7E]/g, '?').slice(0, 200)
}

export async function guiSmtp(ch: CauHinhSmtp, pb: PhongBiSmtp): Promise<void> {
  const hetGio = ch.hetGioMs ?? 15_000
  const hetGioTong = ch.hetGioTongMs ?? 60_000
  const ngamTls = ch.tlsNgam ?? ch.port === 465
  const tenMay = (ch.tenMay ?? 'localhost').replace(/[^A-Za-z0-9.-]/g, '') || 'localhost'
  const from = diaChiLenh(pb.from)
  const to = pb.to.map(diaChiLenh)
  if (to.length === 0) throw new LoiSmtp('smtp: khong co nguoi nhan')

  const tuyChonTls = (): ConnectionOptions => ({
    host: ch.host,
    ...(isIP(ch.host) === 0 ? { servername: ch.host } : {}),
    minVersion: 'TLSv1.2',
    ...ch.tls,
  })

  const doc = new DocTraLoi()
  let socket: Socket | TLSSocket
  let tongHetGio: NodeJS.Timeout | null = null

  const moKetNoi = () => new Promise<Socket>((xong, loi) => {
    const s: Socket = ngamTls
      ? ketNoiTls({ port: ch.port, ...tuyChonTls() })
      : ketNoiNet({ host: ch.host, port: ch.port })
    const sanSang = ngamTls ? 'secureConnect' : 'connect'
    const hong = (e: Error) => loi(new LoiSmtp(`smtp: khong ket noi duoc ${ch.host}:${ch.port} (${(e as NodeJS.ErrnoException).code ?? e.message})`))
    s.once('error', hong)
    s.once(sanSang, () => {
      s.off('error', hong)
      xong(s)
    })
    s.setTimeout(hetGio, () => s.destroy(new Error('ETIMEDOUT')))
  })

  /** Chờ một trả lời, quá hạn thì huỷ kết nối. */
  const cho = async (buoc: string): Promise<TraLoiSmtp> => {
    let hen: NodeJS.Timeout | null = null
    try {
      return await Promise.race([
        doc.doc(),
        new Promise<never>((_, loi) => {
          hen = setTimeout(() => {
            const e = new LoiSmtp(`smtp: het gio cho tra loi (${buoc})`)
            doc.hong(e)
            loi(e)
          }, hetGio)
        }),
      ])
    } catch (e) {
      if (e instanceof LoiSmtp) throw e
      throw new LoiSmtp(`${(e as Error).message} (${buoc})`)
    } finally {
      if (hen) clearTimeout(hen)
    }
  }

  /** Gửi một lệnh (dongLog: cách lệnh hiện trong lỗi — không bao giờ là chuỗi AUTH). */
  const lenh = async (dongGui: string, buoc: string, maMong: number[]): Promise<TraLoiSmtp> => {
    socket.write(`${dongGui}\r\n`)
    const t = await cho(buoc)
    if (!maMong.includes(t.ma)) throw new LoiSmtp(`smtp: ${buoc} bi tu choi ${t.ma} ${tomTat(t)}`, t.ma)
    return t
  }

  const ehlo = async (): Promise<Set<string>> => {
    const t = await lenh(`EHLO ${tenMay}`, 'EHLO', [250])
    const mo = new Set<string>()
    for (const d of t.dong.slice(1)) mo.add(d.trim().toUpperCase())
    return mo
  }

  socket = await moKetNoi()
  try {
    await Promise.race([
      (async () => {
        socket.setTimeout(0)
        doc.gan(socket)
        const chao = await cho('chao')
        if (chao.ma !== 220) throw new LoiSmtp(`smtp: may chu khong san sang ${chao.ma} ${tomTat(chao)}`, chao.ma)

        let mo = await ehlo()
        if (!ngamTls) {
          if (![...mo].some(x => x === 'STARTTLS')) throw new LoiSmtp('smtp: may chu khong ho tro STARTTLS (khong gui mat khau qua ket noi khong ma hoa)')
          await lenh('STARTTLS', 'STARTTLS', [220])
          if (doc.conDu()) throw new LoiSmtp('smtp: du lieu la truoc STARTTLS')
          doc.go()
          const tho = socket
          tho.on('error', () => {})
          socket = await new Promise<TLSSocket>((xong, loi) => {
            const s = ketNoiTls({ socket: tho, ...tuyChonTls() })
            s.once('secureConnect', () => {
              s.off('error', loi)
              xong(s)
            })
            s.once('error', (e: Error) => loi(new LoiSmtp(`smtp: bat TLS that bai (${(e as NodeJS.ErrnoException).code ?? e.message})`)))
          })
          doc.gan(socket)
          mo = await ehlo()
        }

        const auth = [...mo].find(x => x.startsWith('AUTH ') || x.startsWith('AUTH='))
        const cachAuth = new Set((auth ?? '').slice(5).split(/\s+/).filter(Boolean))
        if (cachAuth.has('PLAIN') || !cachAuth.has('LOGIN')) {
          const chuoi = Buffer.from(`\0${ch.user}\0${ch.pass}`, 'utf8').toString('base64')
          await lenh(`AUTH PLAIN ${chuoi}`, 'AUTH PLAIN', [235])
        } else {
          await lenh('AUTH LOGIN', 'AUTH LOGIN', [334])
          await lenh(Buffer.from(ch.user, 'utf8').toString('base64'), 'AUTH LOGIN (ten)', [334])
          await lenh(Buffer.from(ch.pass, 'utf8').toString('base64'), 'AUTH LOGIN (mat khau)', [235])
        }

        await lenh(`MAIL FROM:<${from}>`, 'MAIL FROM', [250])
        for (const r of to) await lenh(`RCPT TO:<${r}>`, 'RCPT TO', [250, 251])
        await lenh('DATA', 'DATA', [354])
        socket.write(chamDuLieuSmtp(pb.thu))
        const xong = await cho('gui thu')
        if (xong.ma !== 250) throw new LoiSmtp(`smtp: thu bi tu choi ${xong.ma} ${tomTat(xong)}`, xong.ma)

        // QUIT: thư đã nhận, lỗi ở đây không quan trọng.
        await lenh('QUIT', 'QUIT', [221]).catch(() => {})
      })(),
      new Promise<never>((_, loi) => {
        tongHetGio = setTimeout(() => {
          const e = new LoiSmtp('smtp: qua thoi gian toi da cua phien')
          doc.hong(e)
          loi(e)
        }, hetGioTong)
      }),
    ])
  } catch (e) {
    // Cố chào QUIT cho lịch sự rồi đóng; không chờ.
    try {
      if (!socket.destroyed) socket.write('QUIT\r\n')
    } catch { /* bỏ qua */ }
    throw e
  } finally {
    if (tongHetGio) clearTimeout(tongHetGio)
    doc.go()
    // Lỗi đến sau khi đã xong (ECONNRESET khi đóng...) không được làm sập tiến trình.
    socket.on('error', () => {})
    socket.end()
    socket.destroy()
  }
}
