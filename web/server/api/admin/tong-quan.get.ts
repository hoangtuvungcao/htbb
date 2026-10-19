/**
 * Bảng tổng quan khu quản trị: thống kê tài khoản / nhân vật / online / giftcode
 * và các cảnh báo an toàn đăng nhập.
 *
 * Mọi con số đọc trực tiếp từ `account` và `htbb_db` ngay lúc gọi. Một câu đếm
 * hỏng (bảng chưa có) trả null để trang hiện "—" thay vì vỡ cả trang.
 */
import type { H3Event } from 'h3'
import type { Pool } from 'mysql2/promise'
import type { Row } from '../../utils/db'

/** Chạy một câu đếm, trả về null nếu bảng không tồn tại. NULL của SQL (MAX/AVG trên bảng rỗng) -> 0. */
async function dem(pool: Pool, sql: string, args: unknown[] = []): Promise<number | null> {
  try {
    return Math.trunc(Number((await qv(pool, sql, args)) ?? 0))
  } catch {
    return null
  }
}

async function dsach<T = Row>(pool: Pool, sql: string, args: unknown[] = []): Promise<T[]> {
  try {
    return await q<T>(pool, sql, args)
  } catch {
    return []
  }
}

/** 14 ngày tính tới `homNay` (YYYY-MM-DD), ngày không có dòng nào ra 0 thay vì bị khuyết cột. */
function chuoi14Ngay(homNay: string, rows: { d: string, n: number }[]): { nhan: string, gt: number }[] {
  const theoNgay = new Map<string, number>()
  for (const r of rows) theoNgay.set(String(r.d).slice(0, 10), Math.trunc(Number(r.n)))
  const [y, m, d] = homNay.split('-').map(Number) as [number, number, number]
  const out: { nhan: string, gt: number }[] = []
  for (let i = 13; i >= 0; i--) {
    const t = new Date(Date.UTC(y, m - 1, d - i))
    const ngay = t.toISOString().slice(0, 10)
    out.push({ nhan: `${ngay.slice(8, 10)}/${ngay.slice(5, 7)}`, gt: theoNgay.get(ngay) ?? 0 })
  }
  return out
}

/** Trang có đang chạy HTTPS không: socket TLS, cổng 443, hoặc proxy phía trước báo X-Forwarded-Proto: https. */
function chayHttps(event: H3Event): boolean {
  const sock = event.node.req.socket as { encrypted?: boolean, localPort?: number }
  const proto = String(getRequestHeader(event, 'x-forwarded-proto') ?? '').split(',')[0]!.trim().toLowerCase()
  return !!sock?.encrypted || sock?.localPort === 443 || proto === 'https'
}

const TEN_NHOM_CAP: Record<string, string> = {
  1: 'Dưới 20', 2: '20 – 39', 3: '40 – 59', 4: '60 – 69', 5: '70 – 79', 6: 'Từ 80',
}

// Cache 30 s theo admin: ~30 truy vấn (quét tob_log_online, đếm tob_char...) mỗi lần mở trang Tổng quan.
// ?moi=1 (nút Làm mới) bỏ qua cache.
const CACHE_TONG_QUAN_MS = 30_000
const cacheTongQuan = new Map<string, { luc: number, kq: unknown }>()

export default defineAdminHandler(async (event, admin) => {
  const c = cacheTongQuan.get(admin.username)
  if (c && Date.now() - c.luc < CACHE_TONG_QUAN_MS && str(getQuery(event).moi) !== '1') return c.kq
  const kq = await tinhTongQuan(event, admin)
  cacheTongQuan.set(admin.username, { luc: Date.now(), kq })
  return kq
})

async function tinhTongQuan(event: import('h3').H3Event, admin: AdminUser) {
  const cfg = useHtbbConfig()
  const acc = dbAccount()
  const game = dbGame()

  // Thẻ giải thích rõ ràng khi MySQL tắt hữu ích hơn trang lỗi chung.
  try {
    await acc.query('SELECT 1')
    await game.query('SELECT 1')
  } catch (e: any) {
    return { loiKetNoi: String(e?.message ?? e) }
  }

  // Lấy "hôm nay" theo đồng hồ của MySQL: web và MySQL có thể lệch múi giờ và
  // biểu đồ sẽ trống một cột đầu / cuối nếu trộn lẫn.
  let homNay: string
  try {
    homNay = String(await qv(acc, 'SELECT CURDATE()')).slice(0, 10)
  } catch {
    homNay = new Date().toISOString().slice(0, 10)
  }

  // ---- Tài khoản ----
  const [tkTong, tkKhoa, tkChoEmail, tkHomNay, tk7Ngay, tk30Ngay] = await Promise.all([
    dem(acc, 'SELECT COUNT(*) FROM team_user'),
    dem(acc, 'SELECT COUNT(*) FROM team_user WHERE ban = 1'),
    dem(acc, 'SELECT COUNT(*) FROM team_user WHERE ban = 3'),
    dem(acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= CURDATE()'),
    dem(acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)'),
    dem(acc, 'SELECT COUNT(*) FROM team_user WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 29 DAY)'),
  ])

  // Chưa kích hoạt: NovaDB dùng mỗi database một file, không hỗ trợ truy vấn chéo
  // `account`.`htbb_db`. Đối chiếu hai tập ở application layer thay vì để câu SQL lỗi rồi
  // mới rơi về phép trừ gần đúng (phép trừ sai khi có tài khoản bị xóa/kích hoạt mồ côi).
  let tkChuaKH: number | null = null
  let tkChuaKHUocLuong = false
  try {
    const taiKhoan = await q<{ username: string }>(acc, 'SELECT username FROM team_user')
    const daKichHoat = new Set((await q<{ username: string }>(game, 'SELECT DISTINCT username FROM `5h_active`'))
      .map(r => String(r.username).toLowerCase()))
    tkChuaKH = taiKhoan.filter(r => !daKichHoat.has(String(r.username).toLowerCase())).length
  } catch {
    // Bảng 5h_active có thể chưa tồn tại ở server mới; giữ fallback hiện tại.
    const daKH = await dem(game, 'SELECT COUNT(DISTINCT username) FROM `5h_active`')
    if (tkTong !== null && daKH !== null) {
      tkChuaKH = Math.max(0, tkTong - daKH)
      tkChuaKHUocLuong = true
    }
  }

  // ---- Nhân vật ----
  const [nvTong, nvSong, nvKhoa, nv24h, nv7Ngay, nv30Ngay] = await Promise.all([
    dem(game, 'SELECT COUNT(*) FROM tob_char'),
    dem(game, 'SELECT COUNT(*) FROM tob_char WHERE ban = 0 AND del = 1'),
    dem(game, 'SELECT COUNT(*) FROM tob_char WHERE ban <> 0'),
    dem(game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 1 DAY)'),
    dem(game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 7 DAY)'),
    dem(game, 'SELECT COUNT(*) FROM tob_char WHERE lastLog >= DATE_SUB(NOW(), INTERVAL 30 DAY)'),
  ])

  // ---- Giftcode ----
  const [gcTong, gcConDung, gcLuotTong] = await Promise.all([
    // Giftcode an toàn (tob_giftcode) — bảng giftcode cũ không còn được game đọc (sv.lau_dai luôn bật).
    dem(game, 'SELECT COUNT(*) FROM tob_giftcode'),
    dem(game, 'SELECT COUNT(*) FROM tob_giftcode WHERE bat = 1 AND (han IS NULL OR han > NOW()) AND da_dung < tong_luot'),
    dem(game, 'SELECT COUNT(*) FROM tob_giftcode_log'),
  ])

  // ---- Người chơi online ----
  // Bảng tob_log_online do chính server game ghi, 5 phút một mẫu (data/GhiSoOnline.java).
  // null = chưa có bảng; 0 = có bảng nhưng server chưa ghi mẫu nào. Hai tình huống này
  // cần hai lời hướng dẫn khác hẳn nhau nên phải phân biệt được.
  const onSoMau = await dem(game, 'SELECT COUNT(*) FROM tob_log_online')
  const onCoBang = onSoMau !== null

  const onMoiNhat = await dsach<{ thoi_gian: string, ket_noi: number, nhan_vat: number, phut_truoc: number }>(
    game,
    `SELECT thoi_gian, ket_noi, nhan_vat,
            TIMESTAMPDIFF(MINUTE, thoi_gian, NOW()) AS phut_truoc
       FROM tob_log_online ORDER BY id DESC LIMIT 1`,
  )
  const onGanNhat = onMoiNhat[0] ?? null
  // Mẫu cũ hơn 15 phút (3 chu kỳ) thì con số không còn là "đang online" nữa — nhiều
  // khả năng server đang tắt. Hiển thị phải nói ra điều đó chứ không đưa số cũ như số thật.
  const onCu = onGanNhat !== null && Number(onGanNhat.phut_truoc) > 15

  const [onDinhHomNay, onDinh7Ngay, onTbHomNay] = await Promise.all([
    dem(game, 'SELECT MAX(nhan_vat) FROM tob_log_online WHERE thoi_gian >= CURDATE()'),
    dem(game, `SELECT MAX(nhan_vat) FROM tob_log_online
                WHERE thoi_gian >= DATE_SUB(CURDATE(), INTERVAL 6 DAY)`),
    dem(game, 'SELECT ROUND(AVG(nhan_vat)) FROM tob_log_online WHERE thoi_gian >= CURDATE()'),
  ])

  // 24 giờ gần nhất, gom theo từng giờ và lấy ĐỈNH của giờ đó (không lấy trung bình:
  // đỉnh mới là con số dùng để tính tải máy chủ).
  const bdOnlineGio = (await dsach<{ nhan: string, n: number }>(
    game,
    `SELECT DATE_FORMAT(thoi_gian, '%Y-%m-%d %H') AS gio,
            DATE_FORMAT(thoi_gian, '%Hh')          AS nhan,
            MAX(nhan_vat)                          AS n
       FROM tob_log_online
      WHERE thoi_gian >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
      GROUP BY gio, nhan ORDER BY gio`,
  )).map(r => ({ nhan: String(r.nhan), gt: Math.trunc(Number(r.n)) }))

  // ---- Biểu đồ 14 ngày ----
  const bdDangKy = chuoi14Ngay(homNay, await dsach(acc,
    `SELECT DATE(regdate) AS d, COUNT(*) AS n FROM team_user
      WHERE regdate >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d`))
  const bdHoatDong = chuoi14Ngay(homNay, await dsach(game,
    `SELECT DATE(lastLog) AS d, COUNT(*) AS n FROM tob_char
      WHERE lastLog >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d`))
  // Đỉnh online mỗi ngày, 14 ngày.
  const bdOnlineNgay = chuoi14Ngay(homNay, await dsach(game,
    `SELECT DATE(thoi_gian) AS d, MAX(nhan_vat) AS n FROM tob_log_online
      WHERE thoi_gian >= DATE_SUB(CURDATE(), INTERVAL 13 DAY) GROUP BY d`))

  // ---- Phân bố nghề / cấp ----
  const phanBoNghe = (await dsach<{ c: number, n: number }>(game,
    `SELECT \`class\` AS c, COUNT(*) AS n FROM tob_char
      WHERE ban = 0 AND del = 1 GROUP BY \`class\` ORDER BY n DESC`))
    .map(r => ({ nhan: tenNghe(r.c), gt: Math.trunc(Number(r.n)) }))

  const phanBoCap = (await dsach<{ nhom: string, n: number }>(game,
    `SELECT CASE
        WHEN lastLv < 20 THEN '1'
        WHEN lastLv < 40 THEN '2'
        WHEN lastLv < 60 THEN '3'
        WHEN lastLv < 70 THEN '4'
        WHEN lastLv < 80 THEN '5'
        ELSE '6' END AS nhom, COUNT(*) AS n
      FROM tob_char WHERE ban = 0 AND del = 1 GROUP BY nhom ORDER BY nhom`))
    .map(r => ({ nhan: TEN_NHOM_CAP[String(r.nhom)] ?? '?', gt: Math.trunc(Number(r.n)) }))

  // ---- Bảng xếp hạng nhanh ----
  const [topXp, topLuong, topNap, tkMoi] = await Promise.all([
    dsach(game, `SELECT charname, lastLv, xp, \`class\`, dex FROM tob_char
                  WHERE ban = 0 AND del = 1 ORDER BY xp DESC LIMIT 10`),
    dsach(game, `SELECT charname, lastLv, luong, luonglock FROM tob_char
                  WHERE ban = 0 AND del = 1 ORDER BY luong DESC LIMIT 10`),
    dsach(game, `SELECT charname, luongnap, luongxai FROM tob_nap_xai_luong
                  WHERE luongnap > 0 ORDER BY luongnap DESC LIMIT 10`),
    // ---- Tài khoản mới nhất ----
    dsach(acc, `SELECT id, username, email, regdate, ban FROM team_user
                 ORDER BY regdate DESC LIMIT 8`),
  ])

  // ---- Thao tác admin gần nhất ----
  const nhatKyMoi = (await coBangNhatKy())
    ? await dsach(game, `SELECT admin_user, hanh_dong, doi_tuong, thoi_gian FROM web_admin_log
                          WHERE hanh_dong <> 'dang_nhap_that_bai' ORDER BY id DESC LIMIT 8`)
    : []

  // ---- Cảnh báo an toàn đăng nhập ----
  // Hai lỗ này không hiện ra ở đâu khác: trang vẫn chạy tốt, vẫn đăng nhập được,
  // vẫn đẹp. Chúng chỉ lộ ra vào hôm có người lấy được mật khẩu. Nên phải nói ngay
  // trên trang đầu, mỗi lần mở, cho tới khi được sửa.
  // Đọc web_admin_2fa hỏng thì chỉ bỏ phần cảnh báo này, không làm vỡ cả trang.
  let daBat2fa = false
  let conMaDuPhong = 0
  const chuaGhiDanh: string[] = []
  try {
    daBat2fa = await hbDaGhiDanh(admin.username)
    conMaDuPhong = daBat2fa ? await hbSoMaDuPhongConLai(admin.username) : 0
    // Ai trong HTBB_ADMIN_ACCOUNTS còn chưa ghi danh — để biết khi nào bật được
    // HTBB_ADMIN_2FA_BAT_BUOC mà không khoá ai ra ngoài.
    for (const ten of adminAccounts()) {
      if (!(await hbDaGhiDanh(ten))) chuaGhiDanh.push(ten)
    }
  } catch (e: any) {
    console.error('[htbb-admin] tong quan: khong doc duoc web_admin_2fa:', e?.message ?? e)
  }
  const batBuoc2fa = hbBatBuocTatCa()

  const sv = (r: Row, k: string) => Number(r[k] ?? 0)

  return {
    loiKetNoi: null,
    dbAccount: cfg.db.account,
    dbGame: cfg.db.game,
    baoMat: {
      chayHttps: chayHttps(event),
      daBat2fa,
      conMaDuPhong,
      batBuoc2fa,
      chuaGhiDanh,
      admin: admin.username,
    },
    online: {
      coBang: onCoBang,
      soMau: onSoMau,
      ganNhat: onGanNhat
        ? { nhanVat: Number(onGanNhat.nhan_vat), ketNoi: Number(onGanNhat.ket_noi), phutTruoc: Number(onGanNhat.phut_truoc), thoiGian: String(onGanNhat.thoi_gian) }
        : null,
      cu: onCu,
      dinhHomNay: onDinhHomNay,
      dinh7Ngay: onDinh7Ngay,
      tbHomNay: onTbHomNay,
      bdGio: bdOnlineGio,
      bdNgay: bdOnlineNgay,
    },
    taiKhoan: {
      tong: tkTong, khoa: tkKhoa, choEmail: tkChoEmail,
      homNay: tkHomNay, bayNgay: tk7Ngay, baMuoiNgay: tk30Ngay,
      chuaKichHoat: tkChuaKH, chuaKichHoatUocLuong: tkChuaKHUocLuong,
    },
    nhanVat: {
      tong: nvTong, song: nvSong, khoa: nvKhoa,
      h24: nv24h, bayNgay: nv7Ngay, baMuoiNgay: nv30Ngay,
    },
    giftcode: { tong: gcTong, conDung: gcConDung, luotTong: gcLuotTong },
    bdDangKy,
    bdHoatDong,
    phanBoNghe,
    phanBoCap,
    topXp: topXp.map(r => ({ charname: String(r.charname), lastLv: sv(r, 'lastLv'), xp: sv(r, 'xp'), nghe: speciesName(r.class, r.lastLv, r.dex) })),
    topLuong: topLuong.map(r => ({ charname: String(r.charname), lastLv: sv(r, 'lastLv'), luong: sv(r, 'luong'), luonglock: sv(r, 'luonglock') })),
    topNap: topNap.map(r => ({ charname: String(r.charname), luongnap: sv(r, 'luongnap'), luongxai: sv(r, 'luongxai') })),
    tkMoi: tkMoi.map(r => ({ id: sv(r, 'id'), username: String(r.username), email: String(r.email ?? ''), regdate: String(r.regdate ?? ''), ban: sv(r, 'ban') })),
    nhatKyMoi: nhatKyMoi.map(r => ({ admin_user: String(r.admin_user ?? ''), hanh_dong: String(r.hanh_dong ?? ''), doi_tuong: String(r.doi_tuong ?? ''), thoi_gian: String(r.thoi_gian ?? '') })),
  }
}
