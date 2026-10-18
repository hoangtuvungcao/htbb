/**
 * Bật / tắt bảo trì đăng nhập bằng tệp bao_tri.txt trong thư mục game (HTBB_SERVER_DIR).
 * Định dạng + kiểm dữ liệu: shared/utils/baoTri.ts (thuần, có test).
 *
 * Ghi nguyên tử: ghi tệp tạm cùng thư mục rồi rename (game đọc mỗi 5 giây, không bao giờ thấy
 * tệp ghi dở). Chỉ ghi khi thư mục có server.ini (đúng thư mục làm việc của game) và web có
 * quyền ghi. Trên VPS mặc định web KHÔNG ghi được: deploy/htbb-web.service đặt
 * ReadOnlyPaths=/opt/htbb (và chạy bằng user htbb-web) — khi đó trang hiện lệnh làm tay.
 */
import { constants } from 'node:fs'
import { access, readFile, rename, stat, unlink, writeFile } from 'node:fs/promises'
import { join } from 'node:path'
import { randomBytes } from 'node:crypto'
import type { NoiDungBaoTri } from '../../shared/utils/baoTri'

export function duongDanBaoTri(): { thuMuc: string, tep: string } {
  const thuMuc = useHtbbConfig().serverDir
  return { thuMuc, tep: join(thuMuc, BAO_TRI_TEN_TEP) }
}

async function co(p: string, mode = constants.F_OK): Promise<boolean> {
  try { await access(p, mode); return true } catch { return false }
}

export async function trangThaiBaoTriTep() {
  const { thuMuc, tep } = duongDanBaoTri()
  const coServerIni = await co(join(thuMuc, 'server.ini'))
  const ghiDuoc = coServerIni && await co(thuMuc, constants.W_OK)
  let coTep = false
  let noiDung: NoiDungBaoTri | null = null
  let sua: string | null = null
  try {
    const s = await stat(tep)
    if (s.isFile()) {
      coTep = true
      sua = s.mtime.toISOString()
      noiDung = s.size > 64 * 1024 ? { cau: '(tệp quá lớn)', cho: [] } : baoTriDoc(await readFile(tep, 'utf8'))
    }
  } catch { /* chưa có tệp = đang mở */ }
  return { thuMuc, tep, coServerIni, ghiDuoc, coTep, noiDung, sua }
}

/** Ghi (bật) hoặc xoá (tắt). Ném lỗi 409 khi không ghi được — trang sẽ chỉ cách làm tay. */
export async function ghiBaoTriTep(nd: NoiDungBaoTri | null): Promise<void> {
  const { thuMuc, tep } = duongDanBaoTri()
  if (!(await co(join(thuMuc, 'server.ini')))) {
    throw apiLoi(409, 'Thư mục game cấu hình cho web (HTBB_SERVER_DIR) không có server.ini — web không chắc đây là thư mục game nên không ghi. Làm tay theo hướng dẫn trên trang.')
  }
  if (!(await co(thuMuc, constants.W_OK))) {
    throw apiLoi(409, 'Web không có quyền ghi vào thư mục game. Làm tay theo hướng dẫn trên trang.')
  }
  if (!nd) {
    try { await unlink(tep) } catch (e: any) { if (e?.code !== 'ENOENT') throw e }
    return
  }
  const tam = join(thuMuc, `.${BAO_TRI_TEN_TEP}.${process.pid}.${randomBytes(4).toString('hex')}.tmp`)
  try {
    await writeFile(tam, baoTriDung(nd), { encoding: 'utf8', mode: 0o644, flag: 'wx' })
    await rename(tam, tep)
  } catch (e) {
    await unlink(tam).catch(() => {})
    throw e
  }
}

/** Cờ bao_tri + tuổi của mẫu tob_chi_so_may_chu mới nhất (game tự báo). */
export async function baoTriTheoGame(): Promise<{ co: boolean, baoTri: boolean | null, cachGiay: number | null }> {
  if (!(await coBang(dbGame(), 'tob_chi_so_may_chu'))) return { co: false, baoTri: null, cachGiay: null }
  const r = await q1(dbGame(), 'SELECT bao_tri, UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(thoi_gian) AS cach FROM tob_chi_so_may_chu ORDER BY id DESC LIMIT 1')
  if (!r) return { co: true, baoTri: null, cachGiay: null }
  return { co: true, baoTri: Number(r.bao_tri) === 1, cachGiay: r.cach == null ? null : Number(r.cach) }
}
