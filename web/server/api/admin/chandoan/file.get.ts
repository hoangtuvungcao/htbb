/**
 * Xem phần CUỐI (~64 KB) của một file log của game server.
 *
 *   GET /api/admin/chandoan/file?ten=errorlog.txt[&thutu=cu]
 *
 * `ten` CHỈ dùng để TRA khoá: phải khớp CHÍNH XÁC một tên trong danh sách cố định
 * DOC_LOG_FILE, hoặc một file `<nhân vật>.txt` mà chính server vừa liệt kê được
 * trong thư mục. Đường dẫn đọc luôn được ghép từ KHOÁ đã khớp, nên
 * "../../.env", "/etc/passwd", "errorlog.txt\0..." đều không lọt.
 *
 * Mặc định MỚI NHẤT LÊN ĐẦU (đảo theo mục, giữ nguyên vết gọi Java).
 * `thutu=cu` để xem theo đúng thứ tự trong file.
 */
export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const ten = typeof query.ten === 'string' ? query.ten : ''
  const serverDir = useHtbbConfig().serverDir

  const luuHong = docLogFileLuuNhanVatHong(serverDir)
  if (!docLogDuocDoc(ten, DOC_LOG_FILE, luuHong)) {
    throw apiLoi(400, 'Tên file không nằm trong danh sách được phép đọc.')
  }

  const kq = docLogCuoiFile(docLogDuongDan(serverDir, ten))
  if (!kq) throw apiLoi(404, `Không đọc được file ${ten}.`)

  const moiTruoc = query.thutu !== 'cu'
  return {
    ten,
    noiDung: moiTruoc ? docLogDaoThuTu(kq.noiDung) : kq.noiDung,
    co: kq.co,
    cat: kq.cat,
    suaLuc: docLogNgayGio(kq.suaLuc),
    moiTruoc,
  }
})
