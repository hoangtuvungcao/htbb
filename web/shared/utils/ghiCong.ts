/**
 * Đọc file ghi công sprite (public/pokemon/GHI_CONG.txt, chép từ
 * KPAH/ClientUnity/Assets/Resources/poke/GHI_CONG.txt — do DongGoiSprite sinh ra).
 *
 * Định dạng:
 *   vài dòng đầu = lời dẫn
 *   == 0005        <- bắt đầu một loài (4 chữ số dex, hậu tố "s" = bản shiny)
 *   thoi_diem<TAB>nguoi_ve<TAB>trang_thai<TAB>giay_phep<TAB>dong_tac
 *
 * Người vẽ có thể là mention Discord dạng <@!123> hoặc <@123> -> hiện "Discord #123".
 */

export interface DongGhiCong {
  thoiDiem: string
  nguoiVe: string
  trangThai: string
  giayPhep: string
  dongTac: string[]
}

export interface LoaiGhiCong {
  /** Mã thư mục gốc, vd. "0005" hoặc "0077s". */
  ma: string
  dex: number
  shiny: boolean
  dong: DongGhiCong[]
}

export interface GhiCong {
  loiDan: string[]
  loai: LoaiGhiCong[]
}

/** "<@!123>" / "<@123>" -> "Discord #123"; tên thường giữ nguyên. */
export function tenNguoiVe(s: string): string {
  const m = s.trim().match(/^<@!?(\d+)>$/)
  return m ? `Discord #${m[1]}` : s.trim()
}

/** Tên giấy phép dễ đọc. */
export function tenGiayPhep(s: string): string {
  switch (s) {
    case 'CC_BY-NC_4': return 'CC BY-NC 4.0'
    case 'PMDCollab_1': return 'PMDCollab v1'
    case 'PMDCollab_2': return 'PMDCollab v2'
    case 'Unspecified': return 'Không ghi'
    default: return s.replace(/_/g, ' ')
  }
}

export function docGhiCong(noiDung: string): GhiCong {
  const ra: GhiCong = { loiDan: [], loai: [] }
  let hienTai: LoaiGhiCong | null = null
  for (const dongTho of noiDung.split(/\r?\n/)) {
    const d = dongTho.trimEnd()
    if (d.trim() === '') continue
    const dau = d.match(/^==\s*(\d+)(s?)\s*$/)
    if (dau) {
      hienTai = { ma: `${dau[1]}${dau[2]}`, dex: Number(dau[1]), shiny: dau[2] === 's', dong: [] }
      ra.loai.push(hienTai)
      continue
    }
    if (!hienTai) {
      ra.loiDan.push(d.trim())
      continue
    }
    const cot = d.split('\t')
    if (cot.length < 2) continue
    hienTai.dong.push({
      thoiDiem: (cot[0] ?? '').trim(),
      nguoiVe: tenNguoiVe(cot[1] ?? ''),
      trangThai: (cot[2] ?? '').trim(),
      giayPhep: (cot[3] ?? '').trim(),
      dongTac: (cot[4] ?? '').split(',').map(x => x.trim()).filter(Boolean),
    })
  }
  return ra
}
