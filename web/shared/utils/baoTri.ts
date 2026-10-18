/**
 * Tệp bảo trì đăng nhập `bao_tri.txt` (cạnh server.ini, thư mục làm việc của game).
 *
 * Định dạng do HTBB/src/server/ChiSoMayChu.java#docTepBaoTri quy định — hàm ở đây PHẢI đọc
 * y hệt bên Java:
 *   - Dòng đầu (đã trim) = câu thông báo gửi người chơi. Rỗng, hoặc chính nó là dòng "cho:"
 *     -> game dùng câu mặc định CAU_BAO_TRI_MAC_DINH.
 *   - Mọi dòng bắt đầu bằng "cho:" (không phân biệt hoa thường) = danh sách tài khoản VẪN được
 *     đăng nhập, tách bằng dấu phẩy / khoảng trắng, game hạ chữ thường.
 *   - Có tệp = bảo trì (kể cả đọc lỗi). Xoá tệp = mở lại. Game kiểm tra mỗi 5 giây, KHÔNG đá
 *     người đang chơi.
 *
 * File thuần (không import gì): dùng được ở server, ở trang và trong tests/bao-tri.test.ts.
 */

export const BAO_TRI_TEN_TEP = 'bao_tri.txt'
export const BAO_TRI_CAU_MAC_DINH = 'Máy chủ đang bảo trì, vui lòng quay lại sau. Theo dõi trang chủ để biết giờ mở lại.'
export const BAO_TRI_CAU_TOI_DA = 200
export const BAO_TRI_SO_TK_TOI_DA = 50
/** Tên tài khoản được phép ghi vào dòng "cho:" — chặt hơn game để không chèn được ký tự lạ. */
export const BAO_TRI_MAU_TAI_KHOAN = /^[a-z0-9_]{3,20}$/

export interface NoiDungBaoTri {
  /** Câu thông báo ('' = game dùng câu mặc định). */
  cau: string
  /** Tài khoản vẫn đăng nhập được (chữ thường). */
  cho: string[]
}

/** Đọc nội dung tệp đúng như game đọc. */
export function baoTriDoc(noiDung: string): NoiDungBaoTri {
  const dong = noiDung.replace(/^\uFEFF/, '').split(/\r?\n/)
  // Files.readAllLines: tệp rỗng -> 0 dòng; split('') -> [''] — cùng kết quả cau = ''.
  let cau = (dong[0] ?? '').trim()
  if (cau.toLowerCase().startsWith('cho:')) cau = ''
  const cho: string[] = []
  for (const d of dong) {
    const t = d.trim()
    if (!t.toLowerCase().startsWith('cho:')) continue
    for (const ten of t.slice(4).split(/[,\s]+/)) {
      const v = ten.toLowerCase()
      if (v && !cho.includes(v)) cho.push(v)
    }
  }
  return { cau, cho }
}

/** Câu game sẽ thật sự hiện cho người chơi. */
export function baoTriCauHienThi(cau: string): string {
  return cau.trim() === '' ? BAO_TRI_CAU_MAC_DINH : cau.trim()
}

/** Dựng nội dung tệp (đã kiểm). Luôn kết thúc bằng xuống dòng. */
export function baoTriDung(nd: NoiDungBaoTri): string {
  const dong = [nd.cau]
  if (nd.cho.length) dong.push(`cho: ${nd.cho.join(', ')}`)
  return `${dong.join('\n')}\n`
}

export type KetQuaKiemBaoTri =
  | { ok: true, bat: false }
  | { ok: true, bat: true, noiDung: NoiDungBaoTri }
  | { ok: false, loi: string }

/**
 * Kiểm body của POST bật/tắt bảo trì: { bat: boolean, cau?: string, cho?: string[] | string }.
 * Không sửa ngầm dữ liệu sai — báo lỗi để admin sửa.
 */
export function baoTriKiem(body: unknown): KetQuaKiemBaoTri {
  const b = (body && typeof body === 'object' ? body : {}) as Record<string, unknown>
  if (typeof b.bat !== 'boolean') return { ok: false, loi: 'Thiếu lựa chọn bật hay tắt bảo trì.' }
  if (!b.bat) return { ok: true, bat: false }

  if (b.cau !== undefined && typeof b.cau !== 'string') return { ok: false, loi: 'Câu thông báo không hợp lệ.' }
  const cau = String(b.cau ?? '').trim()
  if (/[\r\n\u2028\u2029]/.test(cau)) return { ok: false, loi: 'Câu thông báo phải nằm trên một dòng (không xuống dòng).' }
  // eslint-disable-next-line no-control-regex
  if (/[\u0000-\u001f\u007f]/.test(cau)) return { ok: false, loi: 'Câu thông báo có ký tự điều khiển.' }
  if (Array.from(cau).length > BAO_TRI_CAU_TOI_DA) return { ok: false, loi: `Câu thông báo tối đa ${BAO_TRI_CAU_TOI_DA} ký tự.` }
  // Game coi dòng đầu bắt đầu bằng "cho:" là danh sách tài khoản chứ không phải câu thông báo.
  if (cau.toLowerCase().startsWith('cho:')) return { ok: false, loi: 'Câu thông báo không được bắt đầu bằng "cho:".' }

  let ds: unknown[]
  if (b.cho === undefined || b.cho === null || b.cho === '') ds = []
  else if (Array.isArray(b.cho)) ds = b.cho
  else if (typeof b.cho === 'string') ds = b.cho.split(/[,\s]+/)
  else return { ok: false, loi: 'Danh sách tài khoản không hợp lệ.' }

  const cho: string[] = []
  for (const x of ds) {
    if (typeof x !== 'string') return { ok: false, loi: 'Danh sách tài khoản không hợp lệ.' }
    const t = x.trim().toLowerCase()
    if (t === '') continue
    if (!BAO_TRI_MAU_TAI_KHOAN.test(t)) {
      return { ok: false, loi: `Tên tài khoản "${t.slice(0, 30)}" không hợp lệ (3–20 ký tự a-z, 0-9, _).` }
    }
    if (!cho.includes(t)) cho.push(t)
  }
  if (cho.length > BAO_TRI_SO_TK_TOI_DA) return { ok: false, loi: `Tối đa ${BAO_TRI_SO_TK_TOI_DA} tài khoản được vào khi bảo trì.` }
  return { ok: true, bat: true, noiDung: { cau, cho } }
}

/** Lệnh shell để admin tự làm tay trên VPS (khi web không ghi được thư mục game). */
export function baoTriLenhTay(thuMuc: string, nd: NoiDungBaoTri | null): string {
  const tep = `${thuMuc.replace(/\/+$/, '')}/${BAO_TRI_TEN_TEP}`
  if (!nd) return `sudo rm -f ${tep}`
  const q = (s: string) => `'${s.replace(/'/g, `'\\''`)}'`
  const dong = baoTriDung(nd).replace(/\n$/, '').split('\n')
  return `printf '%s\\n' ${dong.map(q).join(' ')} | sudo -u htbb tee ${tep} >/dev/null`
}
