/**
 * NẠP TIỀN — CHẾ ĐỘ DEMO (HTBB_NAP_DEMO=1): hàm THUẦN (không database, không auto-import của Nuxt).
 *
 * Mục đích: cho chủ server XEM luồng nạp lượng + mua Thành viên trên máy thật mà KHÔNG thu được tiền.
 *   - Số tài khoản chỉ để minh hoạ (có chữ "x", VD 0123456xxx) — không chuyển khoản được.
 *   - Không có mã VietQR thật (trình duyệt vẽ hình giả + chữ DEMO), không nút mở app ngân hàng.
 *   - Đơn demo chỉ nằm trong BỘ NHỚ tiến trình web (mất khi khởi động lại), mã "DM" + 8 ký tự —
 *     không bao giờ khớp mã đơn thật "HT..." (timMaDon), không vào bảng nap_*, không cộng gì trong game.
 *   - Webhook SePay/Casso/mock và trang xác nhận tay đều bị từ chối khi đang demo.
 *   - Demo và nạp thật LOẠI TRỪ nhau: cấu hình thật hợp lệ (hoặc đang bật) thì demo bị từ chối, giữ chế độ thật.
 *
 * Phần chạm Nuxt (đọc token, đọc gói từ DB) ở napDemo.ts. Thiết kế: docs/van-hanh.md mục 9.6.
 */
import { randomInt } from 'node:crypto'

/** Số tài khoản minh hoạ mặc định khi chưa đặt HTBB_NAP_ACCOUNT_NO. */
export const DEMO_SO_TK_MAC_DINH = '0123456xxx'
export const DEMO_TEN_TK_MAC_DINH = 'DEMO HTBB'
/** Chữ banner bắt buộc trên mọi trang nạp / Thành viên khi demo. */
export const DEMO_BANNER = 'BẢN DEMO — không chuyển tiền. Số tài khoản chỉ để minh hoạ.'
/** Mã đơn demo: tiền tố riêng, KHÁC "HT" của đơn thật. */
export const DEMO_TIEN_TO = 'DM'
const KY_TU = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
export const DEMO_GIO_SONG = 24
export const DEMO_DON_MOI_TK = 20
export const DEMO_DON_TOI_DA = 5000

// ---------------------------------------------------------------- số tài khoản

/** Số tài khoản minh hoạ: 6–19 ký tự chỉ gồm số và chữ x, có ít nhất một chữ x (VD 0123456xxx). */
export function laSoTaiKhoanDemo(s: string | null | undefined): boolean {
  const v = String(s ?? '').trim()
  return /^[0-9xX]{6,19}$/.test(v) && /x/i.test(v)
}

/** Trông như số tài khoản THẬT (toàn chữ số, không phải mặc định 0000000000) -> không được dùng cho demo. */
export function laSoTaiKhoanThat(s: string | null | undefined): boolean {
  const v = String(s ?? '').replace(/\s+/g, '')
  return /^\d{6,19}$/.test(v) && v !== '0000000000'
}

// ---------------------------------------------------------------- quyết định chế độ

export interface DauVaoCheDo {
  /** HTBB_NAP_BAT. */
  yeuCauBat: boolean
  /** HTBB_NAP_DEMO=1. */
  yeuCauDemo: boolean
  /** Lỗi của kiemCauHinhNapThat() — rỗng = cấu hình thật hợp lệ. */
  loiThat: string[]
  /** Máy dev (import.meta.dev). */
  dev: boolean
  accountNo: string
  accountName: string
}

export interface CheDoNap {
  /** Nạp THẬT đang bật. */
  bat: boolean
  /** Chế độ DEMO đang bật (không bao giờ cùng lúc với bat). */
  demo: boolean
  /** Cổng thanh toán đủ an toàn để THU TIỀN thật (bán Thành viên thật cũng đọc cờ này). Demo = luôn false. */
  congAnToan: boolean
  /** Lý do nạp thật bị tắt cưỡng bức (HTBB_NAP_BAT=true nhưng cấu hình yếu). */
  lyDoTat: string[]
  /** Lý do HTBB_NAP_DEMO=1 bị từ chối (rỗng = không yêu cầu demo hoặc demo đã bật). */
  lyDoTuChoiDemo: string[]
  /** Số / tên tài khoản hiển thị khi demo. */
  soTkDemo: string
  tenTkDemo: string
  /** Dòng log khởi động: [mức, nội dung]. */
  log: ['error' | 'warn' | 'info', string][]
}

/**
 * Một nguồn duy nhất cho "nạp thật / demo / tắt". config.ts gọi lúc khởi động.
 *   - Không yêu cầu demo: giữ y nguyên luật cũ (fail closed trên máy thật, máy dev chỉ cảnh báo).
 *   - Yêu cầu demo nhưng cấu hình thật HỢP LỆ, hoặc nạp thật đang bật (máy dev): TỪ CHỐI demo, giữ chế độ thật.
 *   - Yêu cầu demo nhưng HTBB_NAP_ACCOUNT_NO là số tài khoản thật: TỪ CHỐI demo (không bao giờ khoe số thật trong bản demo).
 *   - Còn lại: demo bật, nạp thật TẮT, congAnToan = false (mọi đường thu tiền thật vẫn đóng).
 */
export function quyetDinhCheDoNap(v: DauVaoCheDo): CheDoNap {
  const hopLe = v.loiThat.length === 0
  const thatBat = v.yeuCauBat && (hopLe || v.dev)
  const ra: CheDoNap = {
    bat: thatBat,
    demo: false,
    congAnToan: hopLe || v.dev,
    lyDoTat: v.yeuCauBat && !hopLe && !v.dev ? [...v.loiThat] : [],
    lyDoTuChoiDemo: [],
    soTkDemo: '',
    tenTkDemo: '',
    log: [],
  }
  if (ra.lyDoTat.length) {
    ra.log.push(['error', `[htbb] NAP TIEN BI TAT (fail closed) - cau hinh chua an toan:\n  - ${ra.lyDoTat.join('\n  - ')}\n  Sua bien moi truong roi khoi dong lai. Xem docs/van-hanh.md.`])
  } else if (v.yeuCauBat && !hopLe) {
    ra.log.push(['warn', `[htbb] (dev) cau hinh nap chua dat cho may that:\n  - ${v.loiThat.join('\n  - ')}`])
  }
  if (!v.yeuCauDemo) return ra

  if (hopLe || thatBat) {
    ra.lyDoTuChoiDemo.push(hopLe
      ? 'cấu hình nạp thật đang hợp lệ (đủ số TK, SePay, mật khẩu...) — demo và thật không chạy cùng lúc'
      : 'HTBB_NAP_BAT=true đang bật nạp thật (máy dev) — demo và thật không chạy cùng lúc')
  }
  const soTk = (v.accountNo ?? '').trim()
  if (laSoTaiKhoanThat(soTk)) {
    ra.lyDoTuChoiDemo.push('HTBB_NAP_ACCOUNT_NO là số tài khoản thật — demo chỉ nhận số minh hoạ có chữ x (VD 0123456xxx)')
  }
  if (ra.lyDoTuChoiDemo.length) {
    ra.log.push(['error', `[htbb] HTBB_NAP_DEMO=1 BI TU CHOI, giu nguyen che do ${thatBat ? 'nap THAT' : 'hien tai'}:\n  - ${ra.lyDoTuChoiDemo.join('\n  - ')}`])
    return ra
  }
  const ten = (v.accountName ?? '').trim()
  ra.bat = false
  ra.demo = true
  ra.congAnToan = false
  ra.soTkDemo = laSoTaiKhoanDemo(soTk) ? soTk : DEMO_SO_TK_MAC_DINH
  ra.tenTkDemo = ten === '' || ten === 'NGUYEN VAN A' ? DEMO_TEN_TK_MAC_DINH : ten
  ra.log.push(['warn', `[htbb] NAP TIEN CHE DO DEMO: khong thu tien, webhook bi tu choi, don chi o bo nho. TK minh hoa ${ra.soTkDemo}.`])
  return ra
}

// ---------------------------------------------------------------- ai được xem

/** HTBB_NAP_DEMO_CHI_ADMIN=1 (mặc định): chỉ tài khoản người chơi có tên trong HTBB_ADMIN_ACCOUNTS. 0: mọi người. */
export function demoDuocXem(demo: boolean, chiAdmin: boolean, username: string | null | undefined, admins: string[]): boolean {
  if (!demo) return false
  if (!chiAdmin) return true
  const u = String(username ?? '').trim().toLowerCase()
  return u !== '' && admins.map(a => a.trim().toLowerCase()).includes(u)
}

export type KetQuaDuongDanDemo = 'bo_qua' | 'tu_choi_webhook' | 'tu_choi_xac_nhan' | 'demo' | 'chan'

/**
 * server/middleware/tat-nap.ts: xử lý một đường dẫn khi đang demo.
 *   bo_qua           không phải API nạp, hoặc không demo (luật cũ xử lý)
 *   tu_choi_webhook  /api/nap/webhook-* : 403 + ghi log, KHÔNG ghi nhận tiền
 *   tu_choi_xac_nhan /api/xac-nhan-nap/* : 403 (không xác nhận tay khi demo)
 *   demo             người xem được demo -> cho vào handler (nhánh demo)
 *   chan             người khác -> 403 như khi nạp tắt
 */
export function demoXetDuongDan(duongDan: string, demo: boolean, duocXem: boolean): KetQuaDuongDanDemo {
  const p = (duongDan.split('?')[0] ?? '').toLowerCase()
  if (!p.startsWith('/api/nap/') && !p.startsWith('/api/xac-nhan-nap/')) return 'bo_qua'
  if (!demo) return 'bo_qua'
  if (p.startsWith('/api/nap/webhook')) return 'tu_choi_webhook'
  if (p.startsWith('/api/xac-nhan-nap/')) return 'tu_choi_xac_nhan'
  return duocXem ? 'demo' : 'chan'
}

// ---------------------------------------------------------------- gói minh hoạ (khi chưa có bảng nap_goi)

export interface GoiDemo { ma: string, ten: string, loai: string, gia_vnd: number, luong: number, mo_ta: string }

export const GOI_DEMO: GoiDemo[] = [
  { ma: 'L20', ten: '200 lượng', loai: 'luong', gia_vnd: 20000, luong: 200, mo_ta: '' },
  { ma: 'L50', ten: '500 lượng', loai: 'luong', gia_vnd: 50000, luong: 500, mo_ta: '' },
  { ma: 'L100', ten: '1.000 lượng', loai: 'luong', gia_vnd: 100000, luong: 1000, mo_ta: '' },
  { ma: 'L200', ten: '2.000 lượng', loai: 'luong', gia_vnd: 200000, luong: 2000, mo_ta: '' },
  { ma: 'L500', ten: '5.000 lượng', loai: 'luong', gia_vnd: 500000, luong: 5000, mo_ta: '' },
  { ma: 'THE_THANG', ten: 'Thẻ tháng', loai: 'the_thang', gia_vnd: 50000, luong: 0, mo_ta: '20 lượng mỗi ngày trong 30 ngày (nhận ở NPC).' },
  { ma: 'VE_MUA', ten: 'Vé mùa cao cấp', loai: 've_mua', gia_vnd: 99000, luong: 0, mo_ta: 'Mở nhánh thưởng cao cấp của mùa hiện tại.' },
]

// ---------------------------------------------------------------- kho đơn demo (bộ nhớ)

export interface DonDemo {
  ma_don: string
  user_id: number
  username: string
  ma_goi: string
  loai: string
  ten_goi: string
  so_tien: number
  luong: number
  charname: string
  trang_thai: 'cho_thanh_toan' | 'da_cong'
  tao_luc: string
  thanh_toan_luc: string | null
  cong_luc: string | null
  /** Luôn true — mọi chỗ hiển thị biết đây là đơn minh hoạ. */
  demo: true
  khoa_idem: string | null
  taoMs: number
  gia_lap_boi: string
}

function hai(n: number): string {
  return n < 10 ? `0${n}` : String(n)
}

function gio(d: Date): string {
  return `${d.getFullYear()}-${hai(d.getMonth() + 1)}-${hai(d.getDate())} ${hai(d.getHours())}:${hai(d.getMinutes())}:${hai(d.getSeconds())}`
}

export function taoMaDonDemo(): string {
  let s = DEMO_TIEN_TO
  for (let i = 0; i < 8; i++) s += KY_TU[randomInt(0, KY_TU.length)]
  return s
}

export function laMaDonDemo(ma: string | null | undefined): boolean {
  return /^DM[A-HJ-NP-Z2-9]{8}$/.test(String(ma ?? ''))
}

export interface HitWebhookDemo { luc: string, duongDan: string, ip: string }

/** Kho đơn demo trong bộ nhớ: không chạm database, sống DEMO_GIO_SONG giờ, có trần số đơn. */
export function taoKhoDemo(bayGio: () => number = Date.now) {
  const don = new Map<string, DonDemo>()
  const webhook: HitWebhookDemo[] = []

  function don_dep() {
    const han = bayGio() - DEMO_GIO_SONG * 3600_000
    for (const [k, d] of don) if (d.taoMs < han) don.delete(k)
    while (don.size > DEMO_DON_TOI_DA) {
      const dau = don.keys().next().value
      if (dau === undefined) break
      don.delete(dau)
    }
  }

  return {
    tao(u: { id: number, username: string }, goi: GoiDemo, charname: string, khoaIdem: string | null): DonDemo {
      don_dep()
      if (khoaIdem) {
        for (const d of don.values()) if (d.user_id === u.id && d.khoa_idem === khoaIdem) return d
      }
      const cua = [...don.values()].filter(d => d.user_id === u.id)
      if (cua.length >= DEMO_DON_MOI_TK) don.delete(cua[0]!.ma_don)
      let ma = taoMaDonDemo()
      while (don.has(ma)) ma = taoMaDonDemo()
      const ms = bayGio()
      const d: DonDemo = {
        ma_don: ma, user_id: u.id, username: u.username, ma_goi: goi.ma, loai: goi.loai, ten_goi: goi.ten,
        so_tien: goi.gia_vnd, luong: goi.luong, charname, trang_thai: 'cho_thanh_toan', tao_luc: gio(new Date(ms)),
        thanh_toan_luc: null, cong_luc: null, demo: true, khoa_idem: khoaIdem, taoMs: ms, gia_lap_boi: '',
      }
      don.set(ma, d)
      return d
    },
    lay(ma: string): DonDemo | null {
      don_dep()
      return don.get(ma) ?? null
    },
    cuaToi(userId: number, loai?: string): DonDemo[] {
      don_dep()
      return [...don.values()].filter(d => d.user_id === userId && (!loai || d.loai === loai)).reverse()
    },
    tatCa(): DonDemo[] {
      don_dep()
      return [...don.values()].reverse()
    },
    /** Admin "Giả lập thanh toán": chỉ đổi trạng thái HIỂN THỊ của đơn demo. Không cộng gì ở đâu cả. */
    giaLap(ma: string, admin: string): DonDemo | null {
      const d = don.get(ma)
      if (!d) return null
      if (d.trang_thai === 'cho_thanh_toan') {
        const luc = gio(new Date(bayGio()))
        d.trang_thai = 'da_cong'
        d.thanh_toan_luc = luc
        d.cong_luc = luc
        d.gia_lap_boi = admin
      }
      return d
    },
    ghiWebhook(duongDan: string, ip: string): void {
      webhook.unshift({ luc: gio(new Date(bayGio())), duongDan: duongDan.slice(0, 120), ip: ip.slice(0, 64) })
      webhook.length = Math.min(webhook.length, 50)
    },
    webhook(): HitWebhookDemo[] {
      return [...webhook]
    },
  }
}

export type KhoDemo = ReturnType<typeof taoKhoDemo>

/** Dữ liệu đơn demo trả cho chủ đơn (giống napDonCongKhai + cờ demo). */
export function donDemoCongKhai(d: DonDemo) {
  return {
    ma_don: d.ma_don, ma_goi: d.ma_goi, loai: d.loai, ten_goi: d.ten_goi, so_tien: d.so_tien, luong: d.luong,
    charname: d.charname, trang_thai: d.trang_thai, tao_luc: d.tao_luc, thanh_toan_luc: d.thanh_toan_luc, cong_luc: d.cong_luc, demo: true as const,
  }
}
