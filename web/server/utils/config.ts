/**
 * Cấu hình website.
 *
 * Thứ tự ưu tiên (cao xuống thấp):
 *   1. Biến môi trường KPAH_* (file .env khi dev, EnvironmentFile của systemd khi chạy thật)
 *   2. server.ini của game server ở KPAH_SERVER_DIR — nguồn thật, tự dò
 *   3. Mặc định cho máy dev (root, không mật khẩu)
 *
 * Bí mật (mật khẩu DB, số tài khoản, API key) KHÔNG bao giờ nằm trong mã nguồn.
 */
import { readFileSync, statSync, accessSync, constants } from 'node:fs'
import { resolve, join } from 'node:path'
import { createHash, randomBytes } from 'node:crypto'

export interface KpahDbIni {
  host: string | null
  port: number | null
  user: string | null
  pass: string | null
  game: string | null
  account: string | null
  accountUser: string | null
  accountPass: string | null
}

/** Đọc file .ini kiểu `khoa=gia tri`. File không có / không đọc được -> {}. */
export function kpahDocIni(duongDan: string): Record<string, string> {
  let noiDung: string
  try {
    noiDung = readFileSync(duongDan, 'utf8')
  } catch {
    return {}
  }
  const ra: Record<string, string> = {}
  for (const dongTho of noiDung.split(/\r?\n/)) {
    const d = dongTho.trim()
    if (d === '' || d[0] === '#' || d[0] === ';') continue
    const viTri = d.indexOf('=')
    if (viTri === -1) continue
    ra[d.slice(0, viTri).trim()] = d.slice(viTri + 1).trim()
  }
  return ra
}

/**
 * Rút thông tin kết nối từ hai file ini của game server.
 * Giữ nguyên chữ "localhost" (không đổi sang 127.0.0.1): MySQL phân quyền theo
 * máy khách, 'kpah'@'localhost' và 'kpah'@'127.0.0.1' là hai quyền khác nhau.
 */
export function kpahCauHinhDb(serverDir: string): KpahDbIni {
  const goc = serverDir.replace(/[\\/]+$/, '')
  const game = kpahDocIni(join(goc, 'server.ini'))
  const login = kpahDocIni(join(goc, 'loginServer', 'server.ini'))
  const ra: KpahDbIni = {
    host: null, port: null, user: null, pass: null,
    game: null, account: null, accountUser: null, accountPass: null,
  }
  const hostIni = game['db.host']
  if (hostIni) {
    let host = hostIni
    const i = host.indexOf(':')
    if (i !== -1) {
      const cong = host.slice(i + 1)
      host = host.slice(0, i)
      if (/^\d+$/.test(cong)) ra.port = Number(cong)
    }
    if (host !== '') ra.host = host
  }
  if (game['db.user']) ra.user = game['db.user']
  if (game['db.password'] !== undefined) ra.pass = game['db.password']
  if (game['db.name']) ra.game = game['db.name']

  // jdbc:mysql://localhost:3306/account?useUnicode=true
  const url = login['db.url']
  if (url) {
    const m = url.match(/\/([A-Za-z0-9_]+)(?:\?|$)/)
    if (m) ra.account = m[1]!
  }
  if (login['db.user']) {
    ra.accountUser = login['db.user']
    ra.accountPass = login['db.password'] ?? ''
  }
  return ra
}

function env(ten: string): string | undefined {
  const v = process.env[ten]
  return v === undefined ? undefined : v
}

function envBool(ten: string, macDinh: boolean): boolean {
  const v = env(ten)
  if (v === undefined || v === '') return macDinh
  return ['1', 'true', 'yes', 'on'].includes(v.toLowerCase())
}

function envInt(ten: string, macDinh: number): number {
  const v = env(ten)
  if (v === undefined || v === '' || !/^-?\d+$/.test(v)) return macDinh
  return Number(v)
}

export interface KpahConfig {
  serverDir: string
  db: {
    host: string
    port: number
    user: string
    pass: string
    game: string
    account: string
    /** Login server có thể dùng người dùng DB khác game server. null = dùng chung user/pass. */
    accountUser: string | null
    accountPass: string | null
    /** Nguồn thông tin DB — cho trang Chẩn đoán. */
    nguon: { userTuEnv: boolean, docDuocServerIni: boolean, docDuocLoginIni: boolean }
  }
  site: {
    name: string
    tagline: string
    linkAndroid: string
    linkPc: string
    linkIos: string
    linkJar: string
    linkGroup: string
    autoActivate: boolean
  }
  nap: {
    bankBin: string
    accountNo: string
    accountName: string
    sepayApiKey: string
    adminNaptienPassword: string
  }
  admin: {
    accounts: string[]
    idleTimeout: number
    maxFail: number
    failWindow: number
    batBuoc2fa: boolean
  }
  /**
   * Gửi email qua Gmail API (OAuth2 refresh token) — dùng cho Quên mật khẩu và
   * thông báo đổi mật khẩu. Thiếu một trong bốn giá trị đầu = chưa bật gửi mail.
   * Cách lấy: scripts/lay-gmail-refresh-token.mjs, README mục "Gửi email (Gmail API)".
   */
  mail: {
    gmailClientId: string
    gmailClientSecret: string
    gmailRefreshToken: string
    /** Địa chỉ Gmail gửi đi (chính tài khoản đã cấp quyền). */
    gmailSender: string
    /** Tên hiển thị ở ô "Người gửi". */
    fromName: string
  }
  /** Khoá ký token đăng nhập (HMAC). */
  secret: string
  /** Đọc IP thật từ X-Forwarded-For khi kết nối đến từ proxy tin cậy (Apache/Nginx cùng máy). */
  trustedProxies: string[]
}

/** Đọc một lần mỗi tiến trình: đổi biến môi trường thì khởi động lại dịch vụ. */
let cache: KpahConfig | null = null

export function useKpahConfig(): KpahConfig {
  if (cache) return cache

  // Mặc định: thư mục cha của thư mục chạy web (web nằm ngay trong thư mục game server).
  const serverDir = resolve(env('KPAH_SERVER_DIR') || resolve(process.cwd(), '..'))
  const ini = kpahCauHinhDb(serverDir)

  const userTuEnv = env('KPAH_DB_USER') !== undefined
  const host = env('KPAH_DB_HOST') ?? ini.host ?? '127.0.0.1'
  const port = env('KPAH_DB_PORT') ? envInt('KPAH_DB_PORT', 3306) : (ini.port ?? 3306)
  const user = env('KPAH_DB_USER') ?? ini.user ?? 'root'
  const pass = env('KPAH_DB_PASS') ?? ini.pass ?? ''
  const game = env('KPAH_DB_GAME') ?? ini.game ?? 'kpah_db'
  const account = env('KPAH_DB_ACCOUNT') ?? ini.account ?? 'account'

  let accountUser: string | null = null
  let accountPass: string | null = null
  if (env('KPAH_DB_ACCOUNT_USER') !== undefined) {
    accountUser = env('KPAH_DB_ACCOUNT_USER')!
    accountPass = env('KPAH_DB_ACCOUNT_PASS') ?? ''
  } else if (!userTuEnv && ini.accountUser !== null && ini.accountUser !== user) {
    // Khai tay KPAH_DB_USER thì dùng nó cho CẢ HAI database: người khai tay biết
    // mình muốn gì, đừng lặng lẽ trộn với user của loginServer/server.ini.
    accountUser = ini.accountUser
    accountPass = ini.accountPass ?? ''
  }

  const coTheDoc = (f: string) => {
    try {
      statSync(f)
      accessSync(f, constants.R_OK)
      return true
    } catch {
      return false
    }
  }

  const admins = (env('KPAH_ADMIN_ACCOUNTS') ?? '')
    .split(',')
    .map(s => s.trim().toLowerCase())
    .filter(Boolean)

  // Khoá ký token: ưu tiên KPAH_SECRET. Khoá này bảo vệ CẢ đăng nhập admin, nên khi
  // chạy thật mà thiếu thì KHÔNG được suy ra từ thứ đoán được (mật khẩu DB thường
  // để trống, thư mục mặc định /opt/kpah ai cũng biết -> ai cũng tự ký được token
  // admin). Thay vào đó sinh khoá ngẫu nhiên cho tiến trình này: an toàn, chỉ có
  // điều mọi người bị đăng xuất mỗi lần khởi động lại. Khi dev thì suy ra cố định
  // để HMR không đá văng phiên đăng nhập.
  let secret = env('KPAH_SECRET') ?? ''
  if (secret === '') {
    if (import.meta.dev) {
      secret = createHash('sha256').update(`kpah-web-token|${pass}|${serverDir}`).digest('hex')
    } else {
      secret = randomBytes(32).toString('hex')
      console.warn('[kpah] Chưa đặt KPAH_SECRET: dùng khoá ngẫu nhiên tạm thời, mọi phiên đăng nhập sẽ mất khi khởi động lại. Đặt KPAH_SECRET (openssl rand -hex 32).')
    }
  } else if (secret.length < 32) {
    console.warn('[kpah] KPAH_SECRET quá ngắn (< 32 ký tự): nên dùng openssl rand -hex 32.')
  }

  cache = {
    serverDir,
    db: {
      host, port, user, pass, game, account, accountUser, accountPass,
      nguon: {
        userTuEnv,
        docDuocServerIni: coTheDoc(join(serverDir, 'server.ini')),
        docDuocLoginIni: coTheDoc(join(serverDir, 'loginServer', 'server.ini')),
      },
    },
    site: {
      name: 'KPAH - Khí Phách Anh Hùng',
      tagline: 'Máy chủ private - Hồi ức tuổi thơ',
      linkAndroid: env('KPAH_LINK_ANDROID') || '#',
      linkPc: env('KPAH_LINK_PC') || '#',
      linkIos: env('KPAH_LINK_IOS') || '#',
      linkJar: env('KPAH_LINK_JAR') || '#',
      linkGroup: env('KPAH_LINK_GROUP') || '#',
      autoActivate: envBool('KPAH_AUTO_ACTIVATE', false),
    },
    nap: {
      bankBin: '970423', // TPBank theo chuẩn VietQR
      accountNo: env('KPAH_TPBANK_ACCOUNT_NO') || '0000000000',
      accountName: env('KPAH_TPBANK_ACCOUNT_NAME') || 'NGUYEN VAN A',
      sepayApiKey: env('KPAH_SEPAY_WEBHOOK_APIKEY') || '',
      adminNaptienPassword: env('KPAH_ADMIN_NAPTIEN_PASSWORD') || 'doi-mat-khau-nay',
    },
    admin: {
      accounts: [...new Set(admins)],
      idleTimeout: envInt('KPAH_ADMIN_IDLE_TIMEOUT', 3600),
      maxFail: envInt('KPAH_ADMIN_MAX_FAIL', 8),
      failWindow: envInt('KPAH_ADMIN_FAIL_WINDOW', 900),
      batBuoc2fa: envBool('KPAH_ADMIN_2FA_BAT_BUOC', false),
    },
    mail: {
      gmailClientId: (env('KPAH_GMAIL_CLIENT_ID') ?? '').trim(),
      gmailClientSecret: (env('KPAH_GMAIL_CLIENT_SECRET') ?? '').trim(),
      gmailRefreshToken: (env('KPAH_GMAIL_REFRESH_TOKEN') ?? '').trim(),
      gmailSender: (env('KPAH_GMAIL_SENDER') ?? '').trim(),
      fromName: env('KPAH_MAIL_FROM_NAME') || 'Khí Phách Anh Hùng',
    },
    secret,
    trustedProxies: (env('KPAH_TRUSTED_PROXIES') ?? '127.0.0.1,::1,::ffff:127.0.0.1')
      .split(',').map(s => s.trim()).filter(Boolean),
  }
  return cache
}

/** Thông tin công khai được phép gửi xuống trình duyệt. */
export function publicSiteConfig() {
  const c = useKpahConfig()
  return {
    name: c.site.name,
    tagline: c.site.tagline,
    linkAndroid: c.site.linkAndroid,
    linkPc: c.site.linkPc,
    linkIos: c.site.linkIos,
    linkJar: c.site.linkJar,
    linkGroup: c.site.linkGroup,
    autoActivate: c.site.autoActivate,
  }
}
