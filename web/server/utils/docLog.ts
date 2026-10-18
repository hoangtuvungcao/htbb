/**
 * Đọc file log của game server cho trang Chẩn đoán: danh sách file được phép
 * đọc, tìm file lưu nhân vật hỏng, đọc phần cuối file, chia mục và đảo thứ tự.
 *
 * File này THUẦN Node (chỉ node:fs / node:path), không phụ thuộc gì vào Nuxt.
 *
 * AN TOÀN ĐƯỜNG DẪN: tên file KHÔNG BAO GIỜ lấy từ người dùng mà phải khớp
 * CHÍNH XÁC một khoá có sẵn — hoặc trong danh sách cố định DOC_LOG_FILE, hoặc
 * một tên `<nhân vật>.txt` mà chính server liệt kê được trong thư mục. Không có
 * bước đó thì `?ten=../../.env` sẽ đọc được mật khẩu database.
 */
import * as nodeFs from 'node:fs'
import { join } from 'node:path'

/**
 * Danh sách file log của server mà trang này được phép đọc. CỐ ĐỊNH.
 *
 * Đường dẫn tương đối so với HTBB_SERVER_DIR là đúng: util.Logger ghi bằng
 * `new FileWriter(tên, true)` với đường dẫn tương đối, nên nó rơi vào thư mục
 * làm việc của JVM — `WorkingDirectory=/opt/htbb` trong htbb-game.service.
 *
 * ĐÃ BỎ khỏi danh sách vì đối chiếu với src/ thì chúng KHÔNG BAO GIỜ sinh ra:
 *   offline.txt     là file ĐẦU VÀO gõ tay (Database.java:11715), không liên quan
 *                   gì tới "rớt kết nối"
 *   saitenchar4.txt nằm trong giveGemItem() — không ai gọi
 *
 * hack.txt / hackdo.txt thì NGƯỢC LẠI — trước đây rỗng vĩnh viễn vì chỗ ghi duy
 * nhất nằm trong Database.banchar(), một hàm không ai gọi. Nay đã nối thẳng vào
 * các phép phát hiện gian lận đang chạy thật (data.NghiVanGianLan).
 */
export const DOC_LOG_FILE: Readonly<Record<string, string>> = Object.freeze({
  'hack.txt': 'Nghi vấn gian lận — mỗi dòng: giờ | Pokémon | IP | loại | chi tiết',
  'hackdo.txt': 'Nghi vấn gian lận vật phẩm — cùng định dạng với hack.txt',
  'errorlog.txt': 'Vết gọi lỗi Java + cảnh báo chặn lệnh admin trái phép (AdminHandler)',
  'log/game.out': 'Toàn bộ stdout/stderr của server (chỉ có trên VPS Linux)',
  'log/login.out': 'stdout/stderr của login server — đăng nhập hỏng thì xem đây',
  'log/autodeploy.log': 'Nhật ký deploy tự động (vps/deploy-auto.sh)',
  'log/don-log.log': 'Nhật ký dọn log định kỳ (vps/don-log.sh)',
  'errorAnima.txt': 'Lỗi khi LƯU thú nuôi (chỉ đường lưu, không có đường nạp)',
  'errorlogPet.txt': 'Nạp dữ liệu thú nuôi hỏng (Pet.java:1208) — chuỗi lưu thú sai định dạng',
  'errorSellITem.txt': 'Đếm số lần bán vật phẩm hỏng — chỉ ghi chuỗi cố định, không có giờ / tên / vết gọi',
  'errorquayso.txt': 'Lỗi thủ tục sự kiện theo ngày (vòng quay và cả sự kiện khác)',
  'savechar.txt': 'Lỗi lưu Pokémon — CHỈ đường lưu tự động; đường lưu chính ghi ra <tên Pokémon>.txt',
  'matketinfo.txt': 'Chốt số mặt kê mỗi ngày và mỗi lần tắt server',
  'AdminChat.txt': 'Mọi câu chat của tài khoản admin — dấu vết lệnh GM',
  'logtopluong.txt': 'Pokémon nạp từ 10.000 lượng trở lên',
  'updateleaf.txt': 'Lỗi vòng lặp AI quái (MapVongLap)',
  'rUpdateleaf.txt': 'Lỗi vòng lặp AI quái (Region / RegionVantienTran)',
  'nhantieu.txt': 'Sổ giao hàng: nhận hàng (ghi lúc sang ngày mới)',
  'doitieu.txt': 'Sổ giao hàng: đổi hàng',
  'thetieu.txt': 'Sổ giao hàng: thế chỗ hàng',
  'tratieu.txt': 'Sổ giao hàng: trả hàng',
  'cuoptieu.txt': 'Sổ giao hàng: cướp hàng',
})

/** Ba file server ĐỌC ngược lại như dữ liệu đầu vào, không phải log. */
const DOC_LOG_KHONG_PHAI_LOG = ['trangan.txt', 'offline.txt', 'top.txt']

/** Ghép thư mục server với một tên ĐÃ ĐƯỢC KIỂM (không bao giờ gọi với tên thô từ request). */
export function docLogDuongDan(serverDir: string, ten: string): string {
  return join(serverDir.replace(/[\\/]+$/, ''), ten)
}

/**
 * Tên này có được phép đọc không — khớp CHÍNH XÁC một khoá có sẵn, không chuẩn
 * hoá, không trim, không đổi hoa/thường. Dùng hasOwn để `__proto__`,
 * `constructor`... không lọt qua đường prototype.
 */
export function docLogDuocDoc(ten: unknown, ...danhSach: Array<Readonly<Record<string, unknown>>>): ten is string {
  if (typeof ten !== 'string' || ten === '') return false
  return danhSach.some(ds => Object.prototype.hasOwnProperty.call(ds, ten))
}

export interface DocLogTrangThaiFile {
  co: boolean
  /** byte */
  kichThuoc: number
  /** mili giây */
  suaLuc: number
  docDuoc: boolean
}

export function docLogTrangThai(duongDan: string, fs: Pick<typeof nodeFs, 'statSync' | 'accessSync'> = nodeFs): DocLogTrangThaiFile {
  try {
    const st = fs.statSync(duongDan)
    if (!st.isFile()) return { co: false, kichThuoc: 0, suaLuc: 0, docDuoc: false }
    let docDuoc = true
    try {
      fs.accessSync(duongDan, nodeFs.constants.R_OK)
    } catch {
      docDuoc = false
    }
    return { co: true, kichThuoc: st.size, suaLuc: st.mtimeMs, docDuoc }
  } catch {
    return { co: false, kichThuoc: 0, suaLuc: 0, docDuoc: false }
  }
}

/**
 * Tìm các file `<tên nhân vật>.txt` do LƯU NHÂN VẬT THẤT BẠI sinh ra.
 *
 * `DatabaseNhanVat.java:770` ghi `Logger.logError(ex, p.charname + ".txt")` mỗi
 * khi đường lưu chính hỏng — tên file là dữ liệu chạy nên không thể liệt kê
 * trước trong DOC_LOG_FILE. Đây thường là vết duy nhất còn lại của sự cố "nhân
 * vật mất đồ / tụt cấp sau khi thoát game".
 *
 * An toàn: readdir chỉ trả về tên có thật trong thư mục, không nhận gì từ người
 * dùng. Lọc bỏ mọi tên đã biết để chỉ còn lại phần dư. Trả về { tên: suaLuc(ms) },
 * mới sửa nhất lên đầu.
 */
export function docLogFileLuuNhanVatHong(serverDir: string, fs: Pick<typeof nodeFs, 'readdirSync' | 'statSync'> = nodeFs): Record<string, number> {
  const daBiet = new Set([...Object.keys(DOC_LOG_FILE), ...DOC_LOG_KHONG_PHAI_LOG])
  const goc = serverDir.replace(/[\\/]+$/, '')
  const ds: Array<[string, number]> = []
  let ten: string[]
  try {
    ten = fs.readdirSync(goc)
  } catch {
    return {}
  }
  for (const t of ten) {
    // Chỉ đuôi .txt viết thường, chỉ file thường ở ngay thư mục gốc (không đi sâu
    // vào thư mục con) — đúng chỗ Logger của server ghi file <nhân vật>.txt.
    if (!t.endsWith('.txt') || daBiet.has(t)) continue
    try {
      const st = fs.statSync(join(goc, t))
      if (st.isFile()) ds.push([t, st.mtimeMs])
    } catch { /* bỏ qua */ }
  }
  ds.sort((a, b) => b[1] - a[1])
  return Object.fromEntries(ds)
}

export interface DocLogCuoiFile {
  noiDung: string
  /** cỡ cả file, byte */
  co: number
  /** true = file lớn hơn ngưỡng, chỉ có phần cuối */
  cat: boolean
  /** mili giây */
  suaLuc: number
}

type DocLogFs = Pick<typeof nodeFs, 'openSync' | 'fstatSync' | 'readSync' | 'closeSync'>

/**
 * Đọc phần CUỐI của một file, không nạp cả file vào bộ nhớ (game.out vài trăm MB).
 *
 * `log/game.out` trên VPS có thể lên hàng trăm MB: đọc cả file (readFileSync, hay
 * bất cứ cách nào cấp bộ đệm theo cỡ CẢ FILE) là phình bộ nhớ tiến trình web tới
 * mức chết. Ở đây chỉ đọc đúng `soByte` byte cuối bằng readSync tại vị trí
 * (co - soByte) — KHÔNG BAO GIỜ dùng readFileSync cho file log.
 *
 * File lớn hơn ngưỡng thì bỏ dòng đầu vì gần chắc chắn bị cắt giữa chừng (không có
 * dấu xuống dòng nào trong đoạn cuối thì ra chuỗi rỗng). Cắt ngay sau byte 
 nên
 * không bao giờ cắt đôi một ký tự UTF-8 nhiều byte.
 */
export function docLogCuoiFile(duongDan: string, soByte = 64000, fs: DocLogFs = nodeFs): DocLogCuoiFile | null {
  let fd: number
  try {
    fd = fs.openSync(duongDan, 'r')
  } catch {
    return null
  }
  try {
    const st = fs.fstatSync(fd)
    if (!st.isFile()) return null
    const co = st.size
    const cat = co > soByte
    const canDoc = Math.min(co, soByte)
    const buf = Buffer.alloc(canDoc)
    let da = 0
    const batDau = cat ? co - soByte : 0
    while (da < canDoc) {
      const n = fs.readSync(fd, buf, da, canDoc - da, batDau + da)
      if (n <= 0) break
      da += n
    }
    let phan = buf.subarray(0, da)
    if (cat) {
      const xuong = phan.indexOf(0x0A)
      phan = xuong === -1 ? phan.subarray(phan.length) : phan.subarray(xuong + 1)
    }
    return { noiDung: phan.toString('utf8'), co, cat, suaLuc: st.mtimeMs }
  } catch {
    return null
  } finally {
    try { fs.closeSync(fd) } catch { /* bỏ qua */ }
  }
}

/**
 * Chia log thành từng MỤC, để đảo ngược được mà không băm nát vết gọi Java.
 *
 * Đảo từng dòng một là hỏng: một vết gọi ngoại lệ dài chục dòng sẽ bị lộn ngược.
 * Hai cách nhận ra ranh giới mục:
 *   1. File có dấu thời gian đầu dòng (errorlog.txt, hack.txt...): mỗi dấu thời
 *      gian mở một mục mới (cần ít nhất 2 dòng như vậy — 1 dòng có thể trùng hợp).
 *   2. Không có dấu thời gian (log/game.out là stdout thô): dựa vào THỤT LỀ — mọi
 *      dòng trong vết gọi Java bắt đầu bằng tab (`\tat ...`). Dòng không thụt lề
 *      mở mục mới, dòng thụt lề dính vào mục đang mở.
 */
export function docLogChiaMuc(noiDung: string): string[] {
  if (noiDung === '') return []
  const dong = noiDung.split(/\r\n|\n|\r/)
  // Xuống dòng cuối file sinh ra một phần tử rỗng thừa — để nguyên thì mỗi lần đảo
  // lại thừa thêm một dòng trống ở đầu.
  if (dong.length > 1 && dong[dong.length - 1] === '') dong.pop()

  const mocGio = /^\d{4}[-/]\d{1,2}[-/]\d{1,2}[ T]\d{1,2}:\d{2}:\d{2}/
  let soMoc = 0
  for (const d of dong) {
    if (mocGio.test(d) && ++soMoc >= 2) break
  }
  const theoGio = soMoc >= 2

  const muc: string[] = []
  let dangGom: string | null = null
  for (const d of dong) {
    const moMucMoi = theoGio ? mocGio.test(d) : (d !== '' && d[0] !== ' ' && d[0] !== '\t')
    if (moMucMoi || dangGom === null) {
      if (dangGom !== null) muc.push(dangGom)
      dangGom = d
    } else {
      dangGom += `\n${d}`
    }
  }
  if (dangGom !== null) muc.push(dangGom)
  return muc
}

/** Mới nhất lên đầu, vết gọi trong từng mục vẫn giữ đúng thứ tự. */
export function docLogDaoThuTu(noiDung: string): string {
  const muc = docLogChiaMuc(noiDung)
  if (muc.length < 2) return noiDung
  return muc.reverse().join('\n')
}

/** Định dạng YYYY-MM-DD HH:mm:ss theo giờ máy chủ. */
export function docLogNgayGio(ms: number): string {
  const d = new Date(ms)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}
