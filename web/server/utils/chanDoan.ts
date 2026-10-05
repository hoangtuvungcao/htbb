/**
 * Chẩn đoán & Lỗi — gom mọi chỗ có thể hỏng vào một trang.
 *
 * Bốn nguồn lỗi của hệ thống này, và trang gom đủ cả bốn:
 *
 *   1. LỖI JAVA        server ghi vết gọi vào errorlog.txt (util.Logger.logError),
 *                      còn stdout/stderr thì systemd đổ vào log/game.out.
 *   2. LỖI TRONG GAME  server tự ghi vào tob_log_all_item / tob_log_all_gem với
 *                      cột aclog mang tên loại: loiinvent, hackc, ffbug...
 *   3. DỮ LIỆU LỆCH    không ai báo lỗi cả, phải tự đi soi: giftcode trùng tên,
 *                      nhân vật mồ côi tài khoản, cột inven quá nhỏ...
 *   4. LỖI TRANG WEB   ghi vào web_admin_log với hanh_dong = 'loi_web'
 *                      (defineAdminHandler tự ghi khi API admin gặp lỗi 5xx).
 *
 * Mỗi phép kiểm tra nằm trong try/catch riêng: một bảng thiếu trên máy chủ này
 * thì phép đó báo "không kiểm được", các phép còn lại vẫn chạy.
 *
 * Mọi hàm xuất ra đều mang tiền tố chanDoan để không đụng tên auto-import khác.
 */
import { existsSync } from 'node:fs'
import { join } from 'node:path'

export type ChanDoanMuc = 'ok' | 'canhbao' | 'loi' | 'khong_kiem_duoc'

export interface ChanDoanKetQua {
  ten: string
  muc: ChanDoanMuc
  mota: string
  cachSua: string
  /** [{ 'nhãn cột': giá trị }, ...] hiện kèm nếu có */
  bang: Array<Record<string, string | number>>
}

/** Xếp mức nặng lên trước — đọc từ trên xuống là đúng thứ tự ưu tiên. */
export const CHAN_DOAN_THU_TU: Record<ChanDoanMuc, number> = { loi: 0, canhbao: 1, khong_kiem_duoc: 2, ok: 3 }

/**
 * Các giá trị aclog đáng chú ý. Đối chiếu từng giá trị với chỗ ghi trong src/ chứ
 * đừng đoán theo tên.
 *
 * ĐÃ BỎ 'black': tên nghe như "danh sách đen" nhưng MapDoMenu.java:2791 ghi nó cho
 * mỗi lần MUA HÀNG dịp Black Friday ("mua <tên đồ> vĩnh viễn"). Để trong bảng này
 * là 7 lượt mua bình thường hiện lên thành 7 nghi vấn gian lận.
 */
export const CHAN_DOAN_LOAI_LOI: Readonly<Record<string, string>> = Object.freeze({
  loiinvent: 'Lưu hành trang thất bại — server tự khoá nhân vật',
  hackc: 'Nghi vấn gian lận — giao thẻ khi không ở đúng map',
  hackR: 'Nghi vấn gian lận — sửa đồ ngoài map offline',
  hackbitem: 'Nghi vấn gian lận vật phẩm — mua món không có trong gian hàng',
  hackchonchar: 'Nghi vấn gian lận — chọn nhân vật của tài khoản khác',
  ffbug: 'Lỗi đã biết bị khai thác — phi phong lỗi cũ còn trong túi',
  thugay: 'Tịch thu gậy Giáng Sinh tạo trước ngày vá lỗi',
  hklbug: 'Lỗi đã biết bị khai thác — vật phẩm thần thú',
  tichthuchoi: 'Tịch thu chổi đua top',
  timeout: 'Vật phẩm hết hạn sử dụng, server đã xoá',
})

const soVN = (n: unknown) => so(Number(n ?? 0))
const byteLen = (s: string) => Buffer.byteLength(s, 'utf8')

/** Chạy toàn bộ phép kiểm tra, đã xếp theo mức nặng. */
export async function chanDoanChayKiemTra(): Promise<ChanDoanKetQua[]> {
  const c = useKpahConfig()
  const DB_GAME = c.db.game
  const DB_ACCOUNT = c.db.account
  // Tên DB phải ghép thẳng vào câu SQL bắc cầu 2 database (không truyền qua `?`
  // được) nên lọc chỉ còn chữ, số, gạch dưới.
  const DB_GAME_TEN = DB_GAME.replace(/[^A-Za-z0-9_]/g, '')
  const DB_ACC_TEN = DB_ACCOUNT.replace(/[^A-Za-z0-9_]/g, '')
  const SERVER_DIR = c.serverDir

  const kiemTra: ChanDoanKetQua[] = []
  const them = (ten: string, muc: ChanDoanMuc, mota: string, cachSua = '', bang: ChanDoanKetQua['bang'] = []) => {
    kiemTra.push({ ten, muc, mota, cachSua, bang })
  }
  /** Bọc một phép kiểm tra: hỏng thì báo "không kiểm được" chứ không vỡ trang. */
  const thu = async (ten: string, viec: () => Promise<void> | void) => {
    try {
      await viec()
    } catch (e: any) {
      them(ten, 'khong_kiem_duoc', `Không chạy được phép kiểm tra này: ${e?.message ?? e}`)
    }
  }
  const game = dbGame()

  // --- 1. Sức chứa các cột hành trang -----------------------------------------
  let tranInven: number | null = null
  await thu('Sức chứa cột hành trang', async () => {
    const cot = await q(
      game,
      `SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH AS toi_da
         FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?
          AND COLUMN_NAME IN (?, ?, ?, ?)`,
      [DB_GAME_TEN, 'tob_char', 'inven', 'bag', 'tuido', 'equip'],
    )
    if (!cot.length) {
      them('Sức chứa cột hành trang', 'khong_kiem_duoc',
        'Không đọc được information_schema — tài khoản MySQL của web thiếu quyền?')
      return
    }
    const nho: string[] = []
    const bang: ChanDoanKetQua['bang'] = []
    for (const r of cot) {
      const toiDa = Math.trunc(Number(r.toi_da ?? 0))
      bang.push({ 'Cột': String(r.COLUMN_NAME), 'Kiểu': String(r.DATA_TYPE).toUpperCase(), 'Sức chứa': `${so(toiDa)} byte` })
      if (String(r.DATA_TYPE).toLowerCase() === 'text') nho.push(String(r.COLUMN_NAME))
      if (tranInven === null || toiDa < tranInven) tranInven = toiDa
    }
    if (nho.length) {
      them('Sức chứa cột hành trang', 'loi',
        `Các cột ${nho.join(', ')} vẫn là TEXT — trần 65.535 byte, tức khoảng 140 món. `
        + 'Vượt trần thì câu UPDATE lưu nhân vật thất bại, và khối bắt lỗi của server TỰ ĐẶT ban = 1: '
        + 'nhân vật biến mất khỏi màn chọn, người chơi tưởng bị xoá tài khoản. Chuyện này đã xảy ra thật '
        + 'ngày 28/07/2026 với nhân vật admin.',
        `ALTER TABLE ${DB_GAME}.tob_char MODIFY inven MEDIUMTEXT;\n`
        + `ALTER TABLE ${DB_GAME}.tob_char MODIFY bag MEDIUMTEXT;\n`
        + `ALTER TABLE ${DB_GAME}.tob_char MODIFY tuido MEDIUMTEXT;\n`
        + `ALTER TABLE ${DB_GAME}.tob_char MODIFY equip MEDIUMTEXT;`,
        bang)
    } else {
      them('Sức chứa cột hành trang', 'ok',
        'Cả bốn cột đều đã được nới rộng, không còn nguy cơ tràn ở mức vài trăm món.', '', bang)
    }
  })

  // --- 2. Nhân vật sắp tràn hành trang ----------------------------------------
  await thu('Nhân vật sắp đầy hành trang', async () => {
    // MEDIUMTEXT báo 16.777.215; TEXT 65.535. Không đọc được thì giả định TEXT.
    const tran = tranInven || 65535
    const nguong = Math.trunc(tran * 0.80)
    const dong = await q(
      game,
      `SELECT charname, LENGTH(inven) AS n, ban FROM tob_char
        WHERE LENGTH(inven) > ? ORDER BY n DESC LIMIT 20`,
      [nguong],
    )
    if (!dong.length) {
      them('Nhân vật sắp đầy hành trang', 'ok',
        `Không nhân vật nào dùng quá 80% sức chứa cột inven (${so(nguong)} byte).`)
      return
    }
    const bang = dong.map(d => ({
      'Nhân vật': String(d.charname),
      'Đã dùng': `${soVN(d.n)} / ${so(tran)} byte`,
      'Phần trăm': `${Math.round(Number(d.n) * 1000 / tran) / 10}%`,
      'Đang khoá': Number(d.ban) !== 0 ? 'CÓ' : '',
    }))
    them('Nhân vật sắp đầy hành trang', 'canhbao',
      `${dong.length} nhân vật đã dùng quá 80% sức chứa cột inven. Đầy là lần lưu kế tiếp hỏng `
      + 'và server tự khoá nhân vật.', '', bang)
  })

  // --- 3. Dấu vết lưu hành trang thất bại -------------------------------------
  await thu('Lỗi lưu hành trang (loiinvent)', async () => {
    const dong = await q(
      game,
      `SELECT charname, COUNT(*) AS n, MAX(timelog) AS lan_cuoi
         FROM tob_log_all_item WHERE aclog = 'loiinvent'
        GROUP BY charname ORDER BY lan_cuoi DESC LIMIT 20`,
    )
    if (!dong.length) {
      them('Lỗi lưu hành trang (loiinvent)', 'ok',
        'Server chưa từng ghi dòng loiinvent nào — chưa nhân vật nào lưu hành trang thất bại.')
      return
    }
    const bang = dong.map(d => ({ 'Nhân vật': String(d.charname), 'Số lần': soVN(d.n), 'Gần nhất': String(d.lan_cuoi ?? '') }))
    them('Lỗi lưu hành trang (loiinvent)', 'loi',
      `${dong.length} nhân vật từng lưu hành trang thất bại. Mỗi lần như vậy là nhân vật lùi về bản `
      + 'lưu thành công cuối cùng — phần chơi ở giữa mất thật, gỡ khoá không lấy lại được. Nhiều khả năng '
      + 'các nhân vật này đang bị ban = 1.',
      `UPDATE ${DB_GAME}.tob_char SET ban = 0 WHERE charname = 'ten_nhan_vat';`, bang)
  })

  // --- 4. Nhân vật đang bị khoá ------------------------------------------------
  await thu('Nhân vật đang bị khoá', async () => {
    const dong = await q(
      game,
      `SELECT charname, userId, lastLv, lastLog FROM tob_char
        WHERE ban <> 0 ORDER BY lastLog DESC LIMIT 20`,
    )
    if (!dong.length) {
      them('Nhân vật đang bị khoá', 'ok', 'Không nhân vật nào đang bị khoá.')
      return
    }
    const bang = dong.map(d => ({
      'Nhân vật': String(d.charname),
      'Tài khoản': `#${Math.trunc(Number(d.userId))}`,
      'Cấp': Math.trunc(Number(d.lastLv)),
      'Vào lần cuối': String(d.lastLog ?? ''),
    }))
    them('Nhân vật đang bị khoá', 'canhbao',
      `${dong.length} nhân vật đang bị khoá. Nhớ rằng không phải cái nào cũng do admin khoá — `
      + 'server tự đặt ban = 1 khi lưu nhân vật thất bại.', '', bang)
  })

  // --- 5. Giftcode trùng tên ---------------------------------------------------
  await thu('Giftcode trùng tên', async () => {
    const dong = await q(game, 'SELECT giftcode, COUNT(*) AS n FROM giftcode GROUP BY giftcode HAVING n > 1 ORDER BY n DESC')
    if (!dong.length) {
      them('Giftcode trùng tên', 'ok', 'Mỗi mã một tên, không có mã nào bị che khuất.')
      return
    }
    const bang = dong.map(d => ({ 'Mã': String(d.giftcode), 'Số dòng': Math.trunc(Number(d.n)) }))
    them('Giftcode trùng tên', 'loi',
      'Có mã bị đặt trùng tên. Câu truy vấn của server kết thúc bằng LIMIT 1 nên chỉ dòng ĐẦU TIÊN '
      + 'được dùng — những dòng sau vĩnh viễn không ai nhận được, dù bạn có sửa phần thưởng trong đó.',
      `SELECT id, giftcode, item FROM ${DB_GAME}.giftcode WHERE giftcode = 'ten_ma' ORDER BY id;`,
      bang)
  })

  // --- 6. Giftcode sai cú pháp phần thưởng -------------------------------------
  await thu('Cú pháp phần thưởng của các mã đang có', async () => {
    const dong = await q(game, 'SELECT id, giftcode, item, type, limit_use FROM giftcode ORDER BY id')
    const xau: ChanDoanKetQua['bang'] = []
    for (const d of dong) {
      const item = String(d.item ?? '')
      if (item === '' || item.toUpperCase() === 'NULL') continue
      // kiemTraItem(): shared/utils/thuong.ts (khu Giftcode) — cùng bộ kiểm với lúc tạo mã.
      const [, loi] = kiemTraItem(item)
      if (loi.length) {
        xau.push({
          'Mã': String(d.giftcode),
          'Còn dùng được': (Number(d.type) === 0 && Number(d.limit_use) !== 0) ? 'CÓ' : 'không',
          'Vấn đề': loi.join(' | '),
        })
      }
    }
    if (!xau.length) {
      them('Cú pháp phần thưởng của các mã đang có', 'ok',
        `Cả ${dong.length} mã đều dùng loại phần thưởng mà server hiểu, đủ tham số.`)
      return
    }
    them('Cú pháp phần thưởng của các mã đang có', 'loi',
      `${xau.length} mã có chuỗi phần thưởng sai. Hậu quả tuỳ kiểu sai: loại lạ thì server lặng lẽ bỏ `
      + 'qua (người chơi mất quà, không báo gì); thiếu tham số thì server ném lỗi GIỮA CHỪNG — người chơi '
      + 'đã nhận được nửa số quà nhưng dòng giftcode_log lẫn việc trừ lượt đều không chạy, nên họ nhận lại '
      + 'được vô hạn lần.',
      '', xau)
  })

  // --- 7. Kích hoạt trùng dòng -------------------------------------------------
  await thu('Bảng kích hoạt 5h_active', async () => {
    const dong = await q(game, 'SELECT username, COUNT(*) AS n FROM `5h_active` GROUP BY username HAVING n > 1 ORDER BY n DESC LIMIT 20')
    if (!dong.length) {
      them('Bảng kích hoạt 5h_active', 'ok', 'Không có tài khoản nào bị ghi kích hoạt nhiều lần.')
      return
    }
    const bang = dong.map(d => ({ 'Tài khoản': String(d.username), 'Số dòng': Math.trunc(Number(d.n)) }))
    them('Bảng kích hoạt 5h_active', 'canhbao',
      'Có tài khoản bị ghi kích hoạt nhiều lần. Không gây lỗi (server chỉ hỏi "có dòng nào không") '
      + 'nhưng là dấu hiệu ai đó bấm kích hoạt lặp, và làm số liệu thống kê lệch.',
      `DELETE a1 FROM ${DB_GAME}.\`5h_active\` a1\n`
      + `  JOIN ${DB_GAME}.\`5h_active\` a2\n`
      + '    ON a1.username = a2.username AND a1.id > a2.id;', bang)
  })

  // --- 8. Kích hoạt mồ côi (bắc cầu 2 database) --------------------------------
  await thu('Kích hoạt mồ côi', async () => {
    const dong = await q(
      game,
      `SELECT a.username FROM \`5h_active\` a
        WHERE NOT EXISTS (SELECT 1 FROM \`${DB_ACC_TEN}\`.team_user t WHERE t.username = a.username)
        LIMIT 20`,
    )
    if (!dong.length) {
      them('Kích hoạt mồ côi', 'ok', 'Mọi dòng kích hoạt đều trỏ tới một tài khoản có thật.')
      return
    }
    const bang = dong.map(d => ({ 'Tài khoản không còn tồn tại': String(d.username) }))
    them('Kích hoạt mồ côi', 'canhbao',
      `${dong.length} dòng kích hoạt trỏ tới tài khoản đã bị xoá. Vô hại, chỉ là rác — nhưng nếu sau `
      + 'này có người đăng ký TRÙNG TÊN cũ thì họ được kích hoạt sẵn mà không cần làm gì.',
      `DELETE a FROM ${DB_GAME}.\`5h_active\` a\n`
      + ` WHERE NOT EXISTS (SELECT 1 FROM ${DB_ACCOUNT}.team_user t WHERE t.username = a.username);`,
      bang)
  })

  // --- 9. Nhân vật mồ côi tài khoản --------------------------------------------
  await thu('Nhân vật mồ côi tài khoản', async () => {
    const dong = await q(
      game,
      `SELECT charname, userId, lastLv FROM tob_char c
        WHERE NOT EXISTS (SELECT 1 FROM \`${DB_ACC_TEN}\`.team_user t WHERE t.id = c.userId)
        ORDER BY lastLv DESC LIMIT 20`,
    )
    if (!dong.length) {
      them('Nhân vật mồ côi tài khoản', 'ok', 'Mọi nhân vật đều thuộc về một tài khoản có thật.')
      return
    }
    const bang = dong.map(d => ({
      'Nhân vật': String(d.charname),
      'Trỏ tới tài khoản': `#${Math.trunc(Number(d.userId))}`,
      'Cấp': Math.trunc(Number(d.lastLv)),
    }))
    them('Nhân vật mồ côi tài khoản', 'canhbao',
      `${dong.length} nhân vật thuộc về tài khoản không còn tồn tại — không ai đăng nhập vào được nữa. `
      + 'Thường là dấu vết của một lần xoá tài khoản mà quên xoá nhân vật.', '', bang)
  })

  // --- 10. Giá trị ban lạ ------------------------------------------------------
  await thu('Giá trị cột ban của tài khoản', async () => {
    const dong = await q(dbAccount(), 'SELECT ban, COUNT(*) AS n FROM team_user WHERE ban NOT IN (0, 1, 3) GROUP BY ban')
    if (!dong.length) {
      them('Giá trị cột ban của tài khoản', 'ok',
        'Chỉ có ba giá trị login server hiểu: 0 bình thường, 1 bị khoá, 3 chờ xác nhận email.')
      return
    }
    const bang = dong.map(d => ({ 'ban =': Math.trunc(Number(d.ban)), 'Số tài khoản': soVN(d.n) }))
    them('Giá trị cột ban của tài khoản', 'canhbao',
      'Có tài khoản mang giá trị ban mà login server không xử lý riêng. Nó rơi vào nhánh cuối và được '
      + 'coi như BÌNH THƯỜNG — nếu bạn đặt giá trị đó với ý định khoá thì tài khoản vẫn đăng nhập được.',
      `UPDATE ${DB_ACCOUNT}.team_user SET ban = 1 WHERE ban NOT IN (0, 1, 3);`, bang)
  })

  // --- 11. Bảng phụ có đủ không -----------------------------------------------
  await thu('Các bảng phụ', async () => {
    const can: Record<string, string> = {
      web_admin_log: 'nhật ký trang quản trị — thiếu thì không ghi lại được ai làm gì, '
        + 'và việc chặn dò mật khẩu chỉ đếm trong bộ nhớ tiến trình web (khởi động lại là mất)',
      tob_nap_moc: 'theo dõi mốc nạp của trang nạp tiền — thiếu thì trang nạp tiền hỏng',
      web_chan_spam: 'đếm số lần đăng ký / đăng nhập sai / tạo đơn theo IP — thiếu thì '
        + 'chỉ đếm trong bộ nhớ tiến trình web, khởi động lại là đếm lại từ đầu',
    }
    const thieu: ChanDoanKetQua['bang'] = []
    for (const [bang, viLy] of Object.entries(can)) {
      try {
        // Tên bảng là hằng số ngay phía trên.
        await game.query(`SELECT 1 FROM \`${bang}\` LIMIT 1`)
      } catch {
        thieu.push({ 'Bảng': bang, 'Dùng để': viLy })
      }
    }
    if (!thieu.length) {
      them('Các bảng phụ', 'ok', 'Có đủ web_admin_log và tob_nap_moc.')
      return
    }
    them('Các bảng phụ', 'canhbao', `Thiếu ${thieu.length} bảng.`,
      `mysql -u root ${DB_GAME} < sql/them_bang_web_admin_log.sql\n`
      + `mysql -u root ${DB_GAME} < sql/them_bang_web_chan_spam.sql\n`
      + '# tob_nap_moc nằm ở PHẦN 2I của vps/db_new_server.sql', thieu)
  })

  // --- 12. Cờ nghi vấn gian lận ------------------------------------------------
  await thu('Cờ nghi vấn trên nhân vật', async () => {
    const dong = await q(
      game,
      `SELECT charname, nhack, blacklist, lastLv, lastLog FROM tob_char
        WHERE nhack > 0 OR blacklist > 0 ORDER BY lastLog DESC LIMIT 20`,
    )
    if (!dong.length) {
      them('Cờ nghi vấn trên nhân vật', 'ok', 'Không nhân vật nào bị server đánh dấu nghi vấn.')
      return
    }
    const bang = dong.map(d => ({
      'Nhân vật': String(d.charname),
      nhack: Math.trunc(Number(d.nhack)),
      blacklist: Math.trunc(Number(d.blacklist)),
      'Vào lần cuối': String(d.lastLog ?? ''),
    }))
    them('Cờ nghi vấn trên nhân vật', 'canhbao',
      `${dong.length} nhân vật bị server đánh dấu. Đây chỉ là nghi vấn máy tự đặt, không phải kết luận `
      + '— soi thêm ở Log game trước khi xử lý.', '', bang)
  })

  // --- 13b. Thông tin database lấy từ đâu --------------------------------------
  await thu('Nguồn thông tin database', () => {
    const goc = SERVER_DIR.replace(/[\\/]+$/, '')
    const fGame = join(goc, 'server.ini')
    const fLogin = join(goc, 'loginServer', 'server.ini')
    const { nguon } = c.db
    // useKpahConfig() đọc hai file này MỘT LẦN lúc website khởi động — cái đáng
    // hỏi là "lúc đó có đọc được không", vì giá trị đang dùng đến từ lúc đó.
    const trangThai = (f: string, docDuoc: boolean) => {
      if (!existsSync(f)) return 'không có file'
      if (!docDuoc) return 'CÓ nhưng website KHÔNG đọc được'
      return 'đọc được'
    }
    const bang: ChanDoanKetQua['bang'] = [
      { 'Nguồn': 'server.ini', 'Đường dẫn': fGame, 'Tình trạng': trangThai(fGame, nguon.docDuocServerIni) },
      { 'Nguồn': 'loginServer/server.ini', 'Đường dẫn': fLogin, 'Tình trạng': trangThai(fLogin, nguon.docDuocLoginIni) },
    ]
    if (nguon.userTuEnv) {
      bang.push({ 'Nguồn': 'KPAH_DB_USER', 'Đường dẫn': 'biến môi trường', 'Tình trạng': 'đã đặt — ghi đè server.ini cho CẢ HAI database' })
    }
    // Giá trị đang thật sự dùng. KHÔNG in mật khẩu, chỉ in độ dài — vừa đủ để
    // biết nó có rỗng hay không mà không phơi ra màn hình.
    bang.push({
      'Nguồn': 'ĐANG DÙNG',
      'Đường dẫn': `${c.db.user}@${c.db.host}:${c.db.port} · mật khẩu ${byteLen(c.db.pass)} ký tự`,
      'Tình trạng': `DB game: ${DB_GAME} · DB tài khoản: ${DB_ACCOUNT}`
        + (c.db.accountUser !== null ? ` (dùng riêng ${c.db.accountUser})` : ''),
    })

    const khongDoc = (existsSync(fGame) && !nguon.docDuocServerIni) || (existsSync(fLogin) && !nguon.docDuocLoginIni)
    if (khongDoc) {
      them('Nguồn thông tin database', 'canhbao',
        'File cấu hình của game server CÓ ở đó nhưng website không có quyền đọc, nên web đang dùng giá trị '
        + 'khai trong biến môi trường KPAH_DB_* (hoặc mặc định). Hai bên lệch nhau lúc nào không hay: đổi mật khẩu '
        + 'database cho game server là website chết mà không rõ vì sao.',
        '# thêm người dùng chạy website (tiến trình Node) vào nhóm kpah\n'
        + 'usermod -a -G kpah <user-chay-website>\n'
        + `chmod 640 ${SERVER_DIR}/server.ini ${SERVER_DIR}/loginServer/server.ini\n`
        + '# rồi khởi động lại tiến trình website để đọc lại cấu hình', bang)
      return
    }
    if (!existsSync(fGame) && !existsSync(fLogin)) {
      them('Nguồn thông tin database', 'ok',
        'Không thấy server.ini nào — website không nằm cùng cây thư mục với game server, nên nó dùng '
        + 'giá trị khai trong biến môi trường KPAH_DB_* (hoặc mặc định). Bình thường nếu đây là máy chạy thử.', '', bang)
      return
    }
    them('Nguồn thông tin database', 'ok',
      'Đọc được cấu hình của chính game server, nên web và server luôn dùng cùng một thông tin '
      + 'database — đổi mật khẩu ở server.ini là web theo ngay (sau khi khởi động lại website), không phải sửa hai chỗ.'
      + (nguon.userTuEnv ? ' Riêng KPAH_DB_USER đang được đặt trong môi trường nên user/mật khẩu lấy từ đó.' : ''),
      '', bang)
  })

  // --- 13. Đọc được thư mục server không ---------------------------------------
  await thu('Đọc file log của server', () => {
    let coFile = 0
    const bang: ChanDoanKetQua['bang'] = []
    for (const ten of Object.keys(DOC_LOG_FILE)) {
      const tt = docLogTrangThai(docLogDuongDan(SERVER_DIR, ten))
      if (tt.co) {
        coFile++
        bang.push({
          'File': ten,
          'Cỡ': `${so(tt.kichThuoc)} byte`,
          'Sửa lần cuối': docLogNgayGio(tt.suaLuc),
          'Đọc được': tt.docDuoc ? 'có' : 'KHÔNG',
        })
      }
    }
    if (coFile === 0) {
      them('Đọc file log của server', 'canhbao',
        `Không thấy file log nào trong ${SERVER_DIR}. Nếu website không nằm cùng máy với game `
        + 'server thì đúng như vậy — đặt biến môi trường KPAH_SERVER_DIR nếu muốn đọc được.')
      return
    }
    them('Đọc file log của server', 'ok', `Thấy ${coFile} file log trong ${SERVER_DIR}.`, '', bang)
  })

  return kiemTra.sort((a, b) => CHAN_DOAN_THU_TU[a.muc] - CHAN_DOAN_THU_TU[b.muc])
}

/** Thống kê lỗi trong log game 90 ngày gần nhất. Bảng thiếu thì bỏ qua. */
export async function chanDoanThongKeLoi(): Promise<Array<{ bang: string, aclog: string, nghia: string, n: number, lanCuoi: string }>> {
  const ra: Array<{ bang: string, aclog: string, nghia: string, n: number, lanCuoi: string }> = []
  const loai = Object.keys(CHAN_DOAN_LOAI_LOI)
  const ph = loai.map(() => '?').join(',')
  for (const bangLog of ['tob_log_all_item', 'tob_log_all_gem']) {
    try {
      const dong = await q(
        dbGame(),
        `SELECT aclog, COUNT(*) AS n, MAX(timelog) AS lan_cuoi
           FROM \`${bangLog}\`
          WHERE aclog IN (${ph}) AND timelog > DATE_SUB(NOW(), INTERVAL 90 DAY)
          GROUP BY aclog ORDER BY n DESC`,
        loai,
      )
      for (const r of dong) {
        ra.push({
          bang: bangLog,
          aclog: String(r.aclog),
          nghia: CHAN_DOAN_LOAI_LOI[String(r.aclog)] ?? '',
          n: Math.trunc(Number(r.n)),
          lanCuoi: String(r.lan_cuoi ?? ''),
        })
      }
    } catch {
      // bảng không có trên máy chủ này -> bỏ qua
    }
  }
  return ra
}

/** Lỗi của chính trang web (hanh_dong = 'loi_web'). */
export async function chanDoanLoiWeb(): Promise<Record<string, unknown>[]> {
  try {
    return await q(
      dbGame(),
      `SELECT thoi_gian, admin_user, doi_tuong, chi_tiet FROM web_admin_log
        WHERE hanh_dong = 'loi_web' ORDER BY id DESC LIMIT 20`,
    )
  } catch {
    return []
  }
}

/** Danh sách file log (cố định) + file lưu nhân vật hỏng, kèm trạng thái. */
export function chanDoanDanhSachFile() {
  const serverDir = useKpahConfig().serverDir
  const files = Object.entries(DOC_LOG_FILE).map(([ten, mota]) => {
    const tt = docLogTrangThai(docLogDuongDan(serverDir, ten))
    return { ten, mota, co: tt.co, kichThuoc: tt.kichThuoc, suaLuc: tt.co ? docLogNgayGio(tt.suaLuc) : '', docDuoc: tt.docDuoc }
  })
  const luuHong = Object.entries(docLogFileLuuNhanVatHong(serverDir)).map(([ten, ms]) => {
    const tt = docLogTrangThai(docLogDuongDan(serverDir, ten))
    return { ten, kichThuoc: tt.kichThuoc, suaLuc: docLogNgayGio(ms), docDuoc: tt.docDuoc }
  })
  return { serverDir, files, luuHong }
}
