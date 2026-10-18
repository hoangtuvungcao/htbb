/**
 * Cấu hình website.
 *
 * Thứ tự ưu tiên (cao xuống thấp):
 *   1. Biến môi trường HTBB_* (file .env khi dev, EnvironmentFile của systemd khi chạy thật)
 *   2. server.ini của game server ở HTBB_SERVER_DIR — nguồn thật, tự dò
 *   3. Mặc định cho máy dev (root, không mật khẩu)
 *
 * Bí mật (mật khẩu DB, số tài khoản, API key) KHÔNG bao giờ nằm trong mã nguồn.
 */
import { kiemCauHinhNapThat } from './napTienThuan'
import { readFileSync, statSync, accessSync, constants } from 'node:fs'
import { resolve, join } from 'node:path'
import { createHash, randomBytes } from 'node:crypto'
import { TEN_SITE } from '../../shared/utils/tenSite.ts'
import { chuanHoaGocSite } from '../../shared/utils/gocSite.ts'

export interface HtbbDbIni {
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
export function htbbDocIni(duongDan: string): Record<string, string> {
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
 * máy khách, 'htbb'@'localhost' và 'htbb'@'127.0.0.1' là hai quyền khác nhau.
 */
export function htbbCauHinhDb(serverDir: string): HtbbDbIni {
  const goc = serverDir.replace(/[\\/]+$/, '')
  const game = htbbDocIni(join(goc, 'server.ini'))
  const login = htbbDocIni(join(goc, 'loginServer', 'server.ini'))
  const ra: HtbbDbIni = {
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

/**
 * Doc bien HTBB_*; chua co thi doc ten cu cung hau to (tien to truoc khi doi ten HTBB),
 * de may dang chay voi file env dat ten theo tien to cu khong hong. Giu MOT ban phat hanh roi bo.
 */
function env(ten: string): string | undefined {
  const v = process.env[ten]
  if (v !== undefined) return v
  return ten.startsWith('HTBB_') ? process.env['KPAH_' + ten.slice(5)] : undefined // TUONG-THICH-TEN-CU
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

export interface HtbbConfig {
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
    /** Địa chỉ gốc (HTBB_SITE_URL, mặc định http://localhost:3000) — canonical, sitemap, OG. */
    siteUrl: string
    /** Đã có tên miền thật -> cho lập chỉ mục. Trống/localhost/IP -> noindex + robots chặn hết. */
    choIndex: boolean
  }
  nap: {
    /**
     * Bật tính năng nạp tiền (HTBB_NAP_BAT). Mặc định TẮT trên nhánh pokemon/chuyen-doi:
     * asset PMDCollab là CC BY-NC 4.0 và Pokémon là IP của Nintendo — không được thu tiền
     * (quyết định Q10, HTBB/docs/pokemon/KE_HOACH_CHUYEN_DOI.md). Tắt thì /api/nap/** và
     * /api/xac-nhan-nap/** trả 403 (server/middleware/tat-nap.ts) và web ẩn trang Nạp.
     */
    bat: boolean
    bankBin: string
    accountNo: string
    accountName: string
    sepayApiKey: string
    adminNaptienPassword: string
    /** Nạp tiền (HTBB/docs/pokemon/NAP_TIEN.md): khoá ký webhook Casso v2 (HTBB_CASSO_WEBHOOK_SECRET). */
    cassoSecret: string
    /** Chế độ thử: bật /api/nap/webhook-mock + nút "Giả lập thanh toán" (HTBB_NAP_SANDBOX=1, KHÔNG bật trên máy thật). */
    sandbox: boolean
    /** Khoá ký webhook mock (HTBB_NAP_MOCK_SECRET). */
    mockSecret: string
    /** Chủ server xác nhận ĐÃ có giấy phép hình ảnh/IP (NAP_DA_CO_GIAY_PHEP=1). Thiếu thì admin thấy cảnh báo đỏ. */
    daCoGiayPhep: boolean
    /** Token SePay user API (HTBB_SEPAY_API_TOKEN) cho job dò giao dịch bị lỡ webhook. Trống = tắt job. */
    sepayApiToken: string
    /** Phút giữa hai lần dò SePay (2–5, mặc định 3) và số giờ nhìn lại (mặc định 6). */
    sepayDoPhut: number
    sepayDoGio: number
    /** HTBB_NAP_BAT người vận hành đặt (trước khi kiểm cấu hình). */
    yeuCauBat: boolean
    /** Lý do bị TẮT cưỡng bức (máy thật, cấu hình yếu) — rỗng = không bị chặn. */
    lyDoTat: string[]
    /**
     * Cổng thanh toán đủ an toàn để THU TIỀN trên máy này (máy dev: luôn true; máy thật: không có lỗi
     * cấu hình). Dùng chung cho nạp lượng và bán Thành viên (thanh_vien.ban_bat) — không có công tắc thứ hai.
     */
    congAnToan: boolean
    /** Lỗi cấu hình cổng thanh toán (kể cả khi HTBB_NAP_BAT tắt) — trang admin hiện. */
    loiCauHinh: string[]
  }
  admin: {
    accounts: string[]
    idleTimeout: number
    maxFail: number
    failWindow: number
    batBuoc2fa: boolean
    /** HTBB_ADMIN_IP_CHO_PHEP: IP/CIDR được gọi API quản trị; rỗng = không giới hạn. */
    ipChoPhep: string
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
let cache: HtbbConfig | null = null

export function useHtbbConfig(): HtbbConfig {
  if (cache) return cache

  // Mặc định: thư mục cha của thư mục chạy web (web nằm ngay trong thư mục game server).
  const serverDir = resolve(env('HTBB_SERVER_DIR') || resolve(process.cwd(), '..'))
  const ini = htbbCauHinhDb(serverDir)

  const userTuEnv = env('HTBB_DB_USER') !== undefined
  const host = env('HTBB_DB_HOST') ?? ini.host ?? '127.0.0.1'
  const port = env('HTBB_DB_PORT') ? envInt('HTBB_DB_PORT', 3306) : (ini.port ?? 3306)
  const user = env('HTBB_DB_USER') ?? ini.user ?? 'root'
  const pass = env('HTBB_DB_PASS') ?? ini.pass ?? ''
  const game = env('HTBB_DB_GAME') ?? ini.game ?? 'htbb_db'
  const account = env('HTBB_DB_ACCOUNT') ?? ini.account ?? 'account'

  let accountUser: string | null = null
  let accountPass: string | null = null
  if (env('HTBB_DB_ACCOUNT_USER') !== undefined) {
    accountUser = env('HTBB_DB_ACCOUNT_USER')!
    accountPass = env('HTBB_DB_ACCOUNT_PASS') ?? ''
  } else if (!userTuEnv && ini.accountUser !== null && ini.accountUser !== user) {
    // Khai tay HTBB_DB_USER thì dùng nó cho CẢ HAI database: người khai tay biết
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

  const admins = (env('HTBB_ADMIN_ACCOUNTS') ?? '')
    .split(',')
    .map(s => s.trim().toLowerCase())
    .filter(Boolean)

  // Khoá ký token: ưu tiên HTBB_SECRET. Khoá này bảo vệ CẢ đăng nhập admin, nên khi
  // chạy thật mà thiếu thì KHÔNG được suy ra từ thứ đoán được (mật khẩu DB thường
  // để trống, thư mục mặc định /opt/htbb ai cũng biết -> ai cũng tự ký được token
  // admin). Thay vào đó sinh khoá ngẫu nhiên cho tiến trình này: an toàn, chỉ có
  // điều mọi người bị đăng xuất mỗi lần khởi động lại. Khi dev thì suy ra cố định
  // để HMR không đá văng phiên đăng nhập.
  let secret = env('HTBB_SECRET') ?? ''
  if (secret === '') {
    if (import.meta.dev) {
      secret = createHash('sha256').update(`htbb-web-token|${pass}|${serverDir}`).digest('hex')
    } else {
      secret = randomBytes(32).toString('hex')
      console.warn('[htbb] Chưa đặt HTBB_SECRET: dùng khoá ngẫu nhiên tạm thời, mọi phiên đăng nhập sẽ mất khi khởi động lại. Đặt HTBB_SECRET (openssl rand -hex 32).')
    }
  } else if (secret.length < 32) {
    console.warn('[htbb] HTBB_SECRET quá ngắn (< 32 ký tự): nên dùng openssl rand -hex 32.')
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
      // Nguồn duy nhất: shared/utils/tenSite.ts (app/app.config.ts cũng đọc từ đó).
      name: TEN_SITE,
      tagline: 'Máy chủ private phi thương mại - bạn chính là Pokémon',
      linkAndroid: env('HTBB_LINK_ANDROID') || '#',
      linkPc: env('HTBB_LINK_PC') || '#',
      linkIos: env('HTBB_LINK_IOS') || '#',
      linkJar: env('HTBB_LINK_JAR') || '#',
      linkGroup: env('HTBB_LINK_GROUP') || '#',
      autoActivate: envBool('HTBB_AUTO_ACTIVATE', false),
      siteUrl: chuanHoaGocSite(env('HTBB_SITE_URL')).goc,
      choIndex: chuanHoaGocSite(env('HTBB_SITE_URL')).choIndex,
    },
    nap: {
      bat: envBool('HTBB_NAP_BAT', false),
      bankBin: env('HTBB_NAP_BANK_BIN') || '970423', // mặc định TPBank theo chuẩn VietQR
      // HTBB_NAP_ACCOUNT_* là tên mới; HTBB_TPBANK_* giữ cho cấu hình cũ.
      accountNo: (env('HTBB_NAP_ACCOUNT_NO') || env('HTBB_TPBANK_ACCOUNT_NO') || '0000000000').trim(),
      accountName: (env('HTBB_NAP_ACCOUNT_NAME') || env('HTBB_TPBANK_ACCOUNT_NAME') || 'NGUYEN VAN A').trim(),
      sepayApiKey: env('HTBB_SEPAY_WEBHOOK_APIKEY') || '',
      adminNaptienPassword: env('HTBB_ADMIN_NAPTIEN_PASSWORD') || 'doi-mat-khau-nay',
      cassoSecret: (env('HTBB_CASSO_WEBHOOK_SECRET') ?? '').trim(),
      sandbox: envBool('HTBB_NAP_SANDBOX', false),
      mockSecret: (env('HTBB_NAP_MOCK_SECRET') ?? '').trim(),
      daCoGiayPhep: (env('NAP_DA_CO_GIAY_PHEP') ?? '').trim() === '1',
      sepayApiToken: (env('HTBB_SEPAY_API_TOKEN') ?? '').trim(),
      sepayDoPhut: Math.min(5, Math.max(2, envInt('HTBB_SEPAY_DO_PHUT', 3))),
      sepayDoGio: Math.min(48, Math.max(1, envInt('HTBB_SEPAY_DO_GIO', 6))),
      yeuCauBat: envBool('HTBB_NAP_BAT', false),
      lyDoTat: [],
      congAnToan: true,
      loiCauHinh: [],
    },
    admin: {
      accounts: [...new Set(admins)],
      idleTimeout: envInt('HTBB_ADMIN_IDLE_TIMEOUT', 3600),
      maxFail: envInt('HTBB_ADMIN_MAX_FAIL', 8),
      failWindow: envInt('HTBB_ADMIN_FAIL_WINDOW', 900),
      batBuoc2fa: envBool('HTBB_ADMIN_2FA_BAT_BUOC', false),
      ipChoPhep: (env('HTBB_ADMIN_IP_CHO_PHEP') ?? '').trim(),
    },
    mail: {
      gmailClientId: (env('HTBB_GMAIL_CLIENT_ID') ?? '').trim(),
      gmailClientSecret: (env('HTBB_GMAIL_CLIENT_SECRET') ?? '').trim(),
      gmailRefreshToken: (env('HTBB_GMAIL_REFRESH_TOKEN') ?? '').trim(),
      gmailSender: (env('HTBB_GMAIL_SENDER') ?? '').trim(),
      fromName: env('HTBB_MAIL_FROM_NAME') || TEN_SITE,
    },
    secret,
    trustedProxies: (env('HTBB_TRUSTED_PROXIES') ?? '127.0.0.1,::1,::ffff:127.0.0.1')
      .split(',').map(s => s.trim()).filter(Boolean),
  }
  // FAIL CLOSED (máy thật): bật nạp với cấu hình yếu = TẮT, in rõ lý do. Máy dev: chỉ cảnh báo.
  {
    const loi = kiemCauHinhNapThat({
      secret: env('HTBB_SECRET') ?? '',
      adminNaptienPassword: cache.nap.adminNaptienPassword,
      accountNo: cache.nap.accountNo,
      accountName: cache.nap.accountName,
      bankBin: cache.nap.bankBin,
      sepayApiKey: cache.nap.sepayApiKey,
      sepayApiToken: cache.nap.sepayApiToken,
      sandbox: cache.nap.sandbox,
    })
    cache.nap.loiCauHinh = loi
    cache.nap.congAnToan = loi.length === 0 || !!import.meta.dev
    if (!cache.nap.yeuCauBat) {
      // Không bật nạp lượng: chỉ ghi nhận lỗi (bán Thành viên cũng đọc congAnToan).
    } else if (loi.length && !import.meta.dev) {
      cache.nap.bat = false
      cache.nap.lyDoTat = loi
      console.error(`[htbb] NAP TIEN BI TAT (fail closed) - cau hinh chua an toan:\n  - ${loi.join('\n  - ')}\n  Sua bien moi truong roi khoi dong lai. Xem HTBB/docs/pokemon/NAP_TIEN.md muc SePay.`)
    } else if (loi.length) {
      console.warn(`[htbb] (dev) cau hinh nap chua dat cho may that:\n  - ${loi.join('\n  - ')}`)
    }
  }
  return cache
}

/** Thông tin công khai được phép gửi xuống trình duyệt. */
export function publicSiteConfig() {
  const c = useHtbbConfig()
  return {
    name: c.site.name,
    tagline: c.site.tagline,
    linkAndroid: c.site.linkAndroid,
    linkPc: c.site.linkPc,
    linkIos: c.site.linkIos,
    linkJar: c.site.linkJar,
    linkGroup: c.site.linkGroup,
    autoActivate: c.site.autoActivate,
    napBat: c.nap.bat,
    siteUrl: c.site.siteUrl,
    choIndex: c.site.choIndex,
  }
}
