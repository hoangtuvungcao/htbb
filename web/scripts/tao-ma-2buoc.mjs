#!/usr/bin/env node
/**
 * ============================================================================
 *  GHI DANH XÁC THỰC HAI BƯỚC CHO MỘT TÀI KHOẢN ADMIN
 * ============================================================================
 *
 *  Chạy trên máy chủ qua SSH, trong thư mục website, bằng ĐÚNG người dùng và
 *  ĐÚNG biến môi trường của dịch vụ web (để chắc chắn đọc được cùng cấu hình —
 *  nếu lệnh này chạy được mà trang lại báo lỗi kết nối thì vấn đề nằm ở chỗ khác):
 *
 *      cd <thư mục KPAH-Web>
 *      sudo -u <người chạy web> node --env-file=<file EnvironmentFile của systemd> scripts/tao-ma-2buoc.mjs <tên>
 *
 *  Khi dev: `node scripts/tao-ma-2buoc.mjs <tên>` (tự đọc .env nếu có).
 *
 *  Cấu hình đọc y hệt server: server/utils/config.ts (KPAH_* > server.ini > mặc định).
 *  Thuật toán: server/utils/haiBuoc.ts (Node chạy thẳng tệp .ts nhờ type stripping).
 *
 *  ------------------------------------------------------------------------
 *   VÌ SAO ĐÂY LÀ LỆNH DÒNG LỆNH, KHÔNG PHẢI MỘT TRANG WEB
 *  ------------------------------------------------------------------------
 *  Nếu web có đường tạo khoá thì bất kỳ ai lấy được mật khẩu cũng tự sinh khoá
 *  hai bước cho tài khoản của bạn bằng điện thoại của nó — một cửa hậu tệ hơn cả
 *  việc không có 2FA. Tệp này nằm trong scripts/, không được Nuxt phục vụ, và web
 *  KHÔNG có API nào tạo hay đổi khoá trong web_admin_2fa (đăng nhập chỉ cập nhật
 *  bước đã dùng và gạch mã dự phòng) — giữ nguyên như vậy khi thêm API mới.
 *
 *  ------------------------------------------------------------------------
 *   SAU KHI GHI DANH XONG CHO CẢ ĐỘI
 *  ------------------------------------------------------------------------
 *  Đặt biến môi trường KPAH_ADMIN_2FA_BAT_BUOC=1 rồi khởi động lại web. Từ đó tài
 *  khoản admin nào chưa có khoá là KHÔNG vào được, kể cả đúng mật khẩu. Chưa đặt
 *  thì người chưa ghi danh vẫn vào được bằng mật khẩu — tức là kẻ trộm mật khẩu
 *  cũng vậy. **Đừng dừng ở bước ghi danh.**
 *
 *  Chữ in ra không dấu để đọc được trên mọi terminal SSH (kể cả bảng mã cũ).
 */
import { existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { createInterface } from 'node:readline/promises'

const GOC = join(dirname(fileURLToPath(import.meta.url)), '..')
process.chdir(GOC)
// Biến đã có trong môi trường (vd. --env-file) không bị .env ghi đè.
if (existsSync(join(GOC, '.env'))) {
  try { process.loadEnvFile(join(GOC, '.env')) } catch { /* bỏ qua */ }
}

const { useKpahConfig } = await import('../server/utils/config.ts')
const hb = await import('../server/utils/haiBuoc.ts')
const mysql = (await import('mysql2/promise')).default

const cfg = useKpahConfig()
const HB_BANG = hb.HB_BANG
const adminAccounts = () => cfg.admin.accounts
const laTaiKhoanAdmin = ten => adminAccounts().includes(ten.trim().toLowerCase())

let _game = null
let _acc = null
async function dbGame() {
  if (!_game) {
    _game = await mysql.createConnection({
      host: cfg.db.host, port: cfg.db.port, user: cfg.db.user, password: cfg.db.pass,
      database: cfg.db.game, charset: 'utf8mb4', dateStrings: true,
    })
  }
  return _game
}
async function dbAccount() {
  if (!_acc) {
    const rieng = cfg.db.accountUser !== null
    _acc = await mysql.createConnection({
      host: cfg.db.host, port: cfg.db.port,
      user: rieng ? cfg.db.accountUser : cfg.db.user,
      password: rieng ? (cfg.db.accountPass ?? '') : cfg.db.pass,
      database: cfg.db.account, charset: 'utf8mb4', dateStrings: true,
    })
  }
  return _acc
}
async function thoat(ma) {
  try { await _game?.end() } catch { /* bỏ qua */ }
  try { await _acc?.end() } catch { /* bỏ qua */ }
  process.exit(ma)
}

async function coBang(ten) {
  try {
    await (await dbGame()).query(`SELECT 1 FROM \`${ten}\` LIMIT 1`)
    return true
  } catch {
    return false
  }
}
async function hbDong(ten) {
  if (!(await coBang(HB_BANG))) return null
  const [rows] = await (await dbGame()).query(
    `SELECT admin_user, khoa_bimat, kich_hoat, buoc_da_dung, ma_du_phong, tao_luc, dung_luc
       FROM ${HB_BANG} WHERE admin_user = ? LIMIT 1`, [ten.toLowerCase()])
  return rows[0] ?? null
}
/** Ghi nhật ký khi chạy dòng lệnh: không có admin đăng nhập, không có IP. */
async function ghiNhatKy(hanhDong, doiTuong, chiTiet) {
  if (!(await coBang('web_admin_log'))) return
  try {
    await (await dbGame()).query(
      'INSERT INTO web_admin_log (admin_user, hanh_dong, doi_tuong, chi_tiet, ip) VALUES (?, ?, ?, ?, ?)',
      ['', hanhDong.slice(0, 50), doiTuong.slice(0, 100), chiTiet.slice(0, 1000), ''])
  } catch { /* im lặng */ }
}

const LENH = 'node scripts/tao-ma-2buoc.mjs'
const ten = String(process.argv[2] ?? '').trim().toLowerCase()

if (ten === '' || ten === '-h' || ten === '--help') {
  console.log(`
GHI DANH XAC THUC HAI BUOC (TOTP) CHO TRANG QUAN TRI
====================================================

  Cach dung:
    ${LENH} <ten tai khoan>

  <ten tai khoan> la ten DANG NHAP (cot team_user.username),
  khong phai ten nhan vat trong game.

  Xem ai da bat hai buoc:
    ${LENH} --danh-sach

  Go hai buoc cua mot tai khoan (mat dien thoai, het ma du phong):
    mysql -u root kpah_db
    DELETE FROM web_admin_2fa WHERE admin_user = 'ten';
`)
  process.exit(1)
}

// ---------------------------------------------------------------------------
//  Liệt kê
// ---------------------------------------------------------------------------
if (ten === '--danh-sach' || ten === '--list') {
  if (!(await coBang(HB_BANG))) {
    console.log(`Chua co bang ${HB_BANG} - chua ai ghi danh.`)
    await thoat(0)
  }
  const [dong] = await (await dbGame()).query(
    `SELECT admin_user, kich_hoat, tao_luc, dung_luc, ma_du_phong FROM ${HB_BANG} ORDER BY admin_user`)
  if (!dong.length) console.log('Bang co nhung chua ai ghi danh.')
  const cot = (s, n) => String(s).padEnd(n)
  console.log(`\n${cot('TAI KHOAN', 20)} ${cot('BAT', 8)} ${cot('GHI DANH LUC', 20)} ${cot('DUNG LAN CUOI', 20)} MA DU PHONG`)
  console.log('-'.repeat(92))
  for (const d of dong) {
    console.log(`${cot(d.admin_user, 20)} ${cot(Number(d.kich_hoat) === 1 ? 'co' : 'KHONG', 8)} ${cot(d.tao_luc ?? '-', 20)} ${cot(d.dung_luc ?? 'chua dung', 20)} ${hb.hbDemMaDuPhong(d.ma_du_phong)} con lai`)
  }
  console.log('')
  console.log(cfg.admin.batBuoc2fa
    ? 'KPAH_ADMIN_2FA_BAT_BUOC = 1  -> tai khoan CHUA ghi danh se KHONG vao duoc.'
    : 'KPAH_ADMIN_2FA_BAT_BUOC chua dat -> tai khoan chua ghi danh VAN vao duoc bang mat khau.\n'
      + '  Ghi danh xong cho ca doi thi dat bien moi truong:\n'
      + '      KPAH_ADMIN_2FA_BAT_BUOC=1')
  console.log('')
  await thoat(0)
}

// ---------------------------------------------------------------------------
//  Kiểm tra tài khoản có thật và có quyền admin
// ---------------------------------------------------------------------------
// Ghi danh cho một tên gõ sai chính tả sẽ tạo ra một dòng mà không đăng nhập nào
// đọc tới, và người ta sẽ ngồi thắc mắc vì sao quét mã xong vẫn không bị hỏi mã.
if (!laTaiKhoanAdmin(ten)) {
  console.log(`[DUNG LAI] '${ten}' khong nam trong KPAH_ADMIN_ACCOUNTS.`)
  console.log(`           Dang cho phep: ${adminAccounts().length ? adminAccounts().join(', ') : '(rong - khong ai vao duoc)'}`)
  console.log('           Them ten vao do truoc, roi chay lai lenh nay.')
  await thoat(1)
}

try {
  const [r] = await (await dbAccount()).query('SELECT username FROM team_user WHERE username = ? LIMIT 1', [ten])
  if (!r.length) {
    console.log(`[DUNG LAI] Khong co tai khoan game nao ten '${ten}' trong bang team_user.`)
    await thoat(1)
  }
} catch (e) {
  console.log(`[LOI] Khong doc duoc bang team_user: ${e?.message ?? e}`)
  console.log('      Kiem tra KPAH_DB_* / server.ini, hoac mo trang Chan doan cua khu quan tri.')
  await thoat(1)
}

// ---------------------------------------------------------------------------
//  Tạo bảng nếu chưa có
// ---------------------------------------------------------------------------
try {
  await (await dbGame()).query(
    `CREATE TABLE IF NOT EXISTS ${HB_BANG} (`
    + '`admin_user`   VARCHAR(50) NOT NULL,'
    + '`khoa_bimat`   VARCHAR(64) NOT NULL,'
    + '`kich_hoat`    TINYINT     NOT NULL DEFAULT 0,'
    + '`buoc_da_dung` BIGINT      NOT NULL DEFAULT 0,'
    + '`ma_du_phong`  TEXT        NULL,'
    + '`tao_luc`      DATETIME    NULL,'
    + '`dung_luc`     DATETIME    NULL,'
    + 'PRIMARY KEY (`admin_user`)'
    + ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4')
} catch (e) {
  console.log(`[LOI] Khong tao duoc bang ${HB_BANG}: ${e?.message ?? e}`)
  await thoat(1)
}

// ---------------------------------------------------------------------------
//  Cảnh báo nếu đã ghi danh rồi
// ---------------------------------------------------------------------------
const daCo = await hbDong(ten)
if (daCo !== null && Number(daCo.kich_hoat) === 1) {
  console.log('')
  console.log(`CANH BAO: '${ten}' DA bat hai buoc tu ${daCo.tao_luc ?? '?'}.`)
  console.log('          Tao khoa moi se LAM MAT hieu luc khoa cu va toan bo ma du phong cu.')
  console.log('          Dien thoai dang dung se khong con vao duoc, phai quet lai ma moi.\n')
  const rl = createInterface({ input: process.stdin, output: process.stdout })
  const traLoi = (await rl.question("Go dung chu 'DONG Y' de tiep tuc: ")).trim()
  rl.close()
  if (traLoi !== 'DONG Y') {
    console.log('Da huy, khong doi gi.')
    await thoat(1)
  }
  console.log('')
}

// ---------------------------------------------------------------------------
//  Sinh khoá + mã dự phòng
// ---------------------------------------------------------------------------
const khoa = hb.hbSinhKhoa()
const maDuPhong = hb.hbSinhMaDuPhong()

try {
  await (await dbGame()).query(
    `INSERT INTO ${HB_BANG} (admin_user, khoa_bimat, kich_hoat, buoc_da_dung, ma_du_phong, tao_luc)
     VALUES (?, ?, 1, 0, ?, NOW())
     ON DUPLICATE KEY UPDATE khoa_bimat = VALUES(khoa_bimat), kich_hoat = 1,
                             buoc_da_dung = 0, ma_du_phong = VALUES(ma_du_phong),
                             tao_luc = NOW(), dung_luc = NULL`,
    [ten, khoa, hb.hbBamMaDuPhong(maDuPhong)])
} catch (e) {
  console.log(`[LOI] Khong ghi duoc vao ${HB_BANG}: ${e?.message ?? e}`)
  await thoat(1)
}

await ghiNhatKy('2fa_ghi_danh', ten, 'tao khoa hai buoc moi qua dong lenh')

// ---------------------------------------------------------------------------
//  In ra
// ---------------------------------------------------------------------------
const vach = '='.repeat(68)
console.log(`${vach}
  DA BAT XAC THUC HAI BUOC CHO: ${ten}
${vach}

BUOC 1 - Mo ung dung xac thuc tren dien thoai
  Google Authenticator, Microsoft Authenticator, Authy, 2FAS... deu duoc.

BUOC 2 - Them tai khoan bang cach NHAP KHOA THU CONG
  Chon "Nhap khoa thiet lap" / "Enter setup key", roi go:

      Ten     : KPAH Admin (${ten})
      Khoa    : ${hb.hbKhoaDeDoc(khoa)}
      Loai    : Theo thoi gian (Time based)

  Khoang trang trong khoa chi de de doc, go lien hay co cach deu duoc.

  Hoac neu ung dung nhan duong dan otpauth:// thi dan nguyen dong nay:

      ${hb.hbUri(khoa, ten)}

BUOC 3 - LUU 8 MA DU PHONG NAY VAO CHO AN TOAN
  Moi ma dung DUNG MOT LAN, thay cho ma 6 so khi mat dien thoai.
  In ra giay hoac luu vao trinh quan ly mat khau - DUNG luu trong may chu.
  Sau khi dong cua so nay se KHONG XEM LAI DUOC (trong CSDL chi luu ban bam).
`)
maDuPhong.forEach((m, i) => console.log(`      ${i + 1}. ${m}`))
console.log(`
BUOC 4 - Thu dang nhap NGAY BAY GIO, o mot cua so trinh duyet khac
  Dang nhap thanh cong roi hay dong phien hien tai. Neu ung dung khong sinh
  dung ma, xoa khoa di roi lam lai:
      mysql -u root kpah_db
      DELETE FROM web_admin_2fa WHERE admin_user = '${ten}';
`)

if (!cfg.admin.batBuoc2fa) {
  console.log(`${vach}
  CON MOT BUOC NUA, DUNG QUEN
${vach}
  Hien tai tai khoan admin CHUA ghi danh van vao duoc chi bang mat khau -
  ke trom duoc mat khau cung vay. Ghi danh xong cho tat ca moi nguoi trong
  KPAH_ADMIN_ACCOUNTS roi dat bien moi truong cho dich vu web:

      KPAH_ADMIN_2FA_BAT_BUOC=1

  Xem ai con thieu:  ${LENH} --danh-sach
${vach}
`)
}

await thoat(0)
