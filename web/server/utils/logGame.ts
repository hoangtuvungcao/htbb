/**
 * Các bảng log mà game server tự ghi trong htbb_db — cấu hình của trang Log game.
 *
 * Tên bảng và tên cột ở đây đều là HẰNG SỐ viết sẵn, không bao giờ lấy từ người
 * dùng — nên ghép thẳng vào câu SQL là an toàn. Riêng giá trị lọc thì luôn đi
 * qua tham số `?`.
 */

export interface LogGameBang {
  ten: string
  mota: string
  /** Mục "view" đọc từ MỘT BẢNG THẬT khác với khoá hiển thị/URL. */
  bangThat?: string
  /** Điều kiện cố định người dùng không bỏ được (vd Loại = sitemu). */
  locCoDinh?: Record<string, string>
  /** cột -> nhãn, đúng thứ tự hiện */
  cot: Record<string, string>
  loc: string | null
  loc2: string | null
  /** ORDER BY (hằng số) */
  sap: string
  /**
   * Bảng TÁCH THEO KỲ: server (Database.check()) ghi vào `<bảng>_<tuần>_<tháng>_<năm>` (tuan) hoặc
   * `<bảng>_<tháng><năm>` (thang); bảng không hậu tố chỉ là bảng mẫu, luôn rỗng. Xem logGameDsKy().
   */
  theoKy?: 'tuan' | 'thang'
}

export const LOG_GAME_BANG: Readonly<Record<string, LogGameBang>> = Object.freeze({
  tob_log_all_item: {
    ten: 'Vật phẩm',
    mota: 'Mọi thứ liên quan tới vật phẩm: nhặt, tạo, mất, lỗi lưu hành trang. '
      + 'Truy vì sao mất đồ: lọc Loại = matdo (nghiền, bán tự động, admin xoá, '
      + 'server xoá lúc nạp). Từ 02/09/2026 bảng CHỈ còn ghi các loại về mất đồ, gian lận '
      + 'và tài sản tăng bất thường - danh sách GIU ở src/data/LocLogVatPham.java.',
    cot: { id: 'ID', charname: 'Pokémon', aclog: 'Loại', info: 'Chi tiết', charname2: 'Liên quan', timelog: 'Lúc' },
    loc: 'charname', loc2: 'aclog', sap: 'id DESC',
  },
  tob_log_all_gem: {
    ten: 'Ngọc',
    mota: 'Khảm, gỡ, mua bán ngọc.',
    cot: { id: 'ID', charname: 'Pokémon', aclog: 'Loại', info: 'Chi tiết', charname2: 'Liên quan', timelog: 'Lúc' },
    loc: 'charname', loc2: 'aclog', sap: 'id DESC',
  },
  tob_log_buy_shop: {
    ten: 'Mua ở shop',
    mota: 'Giao dịch mua trong cửa hàng NPC. Từ 14/09/2026: những lần mua CÙNG món '
      + 'liên tiếp trong vòng 5 giây được gộp thành 1 dòng (xem cột Số lượng) — gói '
      + 'tin mua không mang số lượng nên mỗi lần bấm mua vốn dĩ là 1 gói tin riêng.',
    cot: { id: 'ID', charname: 'Pokémon', info: 'Món', soluong: 'Số lượng', price: 'Giá', money: 'Trả bằng', timeBuy: 'Lúc' },
    loc: 'charname', loc2: null, sap: 'id DESC',
  },
  tob_log_buy_store: {
    ten: 'Ký gửi qua NPC (Giáp/Ngưu)',
    mota: 'Người chơi ký gửi đồ/ngọc qua 10 NPC ký gửi trong thành (Golem 1..Golem 5, '
      + 'Tauros 1..Tauros 5), người khác tới mua. Bảng gốc tob_log_buy_store đã KHÔNG còn '
      + 'được server ghi từ lâu (chỉ còn dữ liệu cũ, nếu có) — mục này là view lọc sẵn '
      + 'Loại = sitemu trên bảng tob_log_all_item, nơi log thật đang ghi cả người bán '
      + 'VÀ người mua (xem src/data/Database.java saveLogSellItem()).',
    bangThat: 'tob_log_all_item',
    locCoDinh: { aclog: 'sitemu' },
    cot: { id: 'ID', charname: 'Người bán', charname2: 'Người mua', aclog: 'Loại', info: 'Chi tiết', timelog: 'Lúc' },
    loc: 'charname', loc2: null, sap: 'id DESC',
  },
  tob_log_clan: {
    ten: 'Đội',
    mota: 'Vào / ra đội, đóng góp, thay đổi chức vụ.',
    cot: { id: 'ID', charname: 'Pokémon', actioninfo: 'Việc', info: 'Chi tiết', timeLog: 'Lúc' },
    loc: 'charname', loc2: 'actioninfo', sap: 'id DESC',
  },
  tob_log_use_luong: {
    ten: 'Tiêu lượng theo món',
    mota: 'Cộng dồn theo tên món — mỗi món một dòng, không phải theo từng lần mua.',
    cot: { id: 'ID', name: 'Món', idtemplate: 'ID mẫu', soluong: 'Số lượng', solanmua: 'Lượt mua', totalmoney: 'Tổng tiền', moneytype: 'Loại tiền' },
    loc: 'name', loc2: null, sap: 'totalmoney DESC', theoKy: 'tuan',
  },
  tob_nap_xai_luong: {
    ten: 'Nạp / xài lượng',
    mota: 'Tổng đã nạp và đã tiêu của từng Pokémon (mỗi Pokémon một dòng).',
    cot: { charname: 'Pokémon', luongnap: 'Đã nạp', luongxai: 'Đã xài', tnap: 'Nạp lần cuối', txai: 'Xài lần cuối' },
    loc: 'charname', loc2: null, sap: 'luongnap DESC',
  },
  // tob_log_xoso (xổ số của bản gốc) đã bỏ: server không còn ghi bảng này.
})

/**
 * Các bảng theo kỳ có thật của một mục (tên bảng lấy từ information_schema, KHÔNG từ người dùng), mới nhất trước.
 * tuan: `<goc>_<tuần>_<tháng>_<năm>`; thang: `<goc>_<tháng><năm>` (tháng 1–2 chữ số, năm 4 chữ số).
 * Tên không đúng mẫu (kể cả bảng mẫu không hậu tố) bị loại.
 */
export function logGameDsKy(goc: string, kieu: 'tuan' | 'thang', tenBang: readonly string[]): { bang: string, nhan: string }[] {
  const ds: { bang: string, nhan: string, khoa: number }[] = []
  for (const t of tenBang) {
    if (!t.startsWith(`${goc}_`)) continue
    const duoi = t.slice(goc.length + 1)
    if (kieu === 'tuan') {
      const m = /^(\d{1,2})_(\d{1,2})_(\d{4})$/.exec(duoi)
      if (!m) continue
      const [w, th, n] = [Number(m[1]), Number(m[2]), Number(m[3])]
      if (th < 1 || th > 12) continue
      ds.push({ bang: t, nhan: `Tuần ${w} · ${String(th).padStart(2, '0')}/${n}`, khoa: n * 10000 + th * 100 + w })
    } else {
      const m = /^(\d{1,2})(\d{4})$/.exec(duoi)
      if (!m) continue
      const [th, n] = [Number(m[1]), Number(m[2])]
      if (th < 1 || th > 12) continue
      ds.push({ bang: t, nhan: `Tháng ${String(th).padStart(2, '0')}/${n}`, khoa: n * 100 + th })
    }
  }
  return ds.sort((a, b) => b.khoa - a.khoa).map(({ bang, nhan }) => ({ bang, nhan }))
}

export const LOG_GAME_MAC_DINH = 'tob_log_all_item'
export const LOG_GAME_MOI_TRANG = 50

/** Cột nào nên hiện dưới dạng số có dấu chấm ngăn cách. */
export const LOG_GAME_COT_SO: readonly string[] = ['price', 'soluong', 'solanmua', 'totalmoney', 'luongnap', 'luongxai', 'win', 'idtemplate']

/**
 * Dựng câu SELECT cho một bảng log. Phân trang cố ý KHÔNG đếm tổng số dòng: mấy
 * bảng này có thể lên hàng triệu dòng và một câu COUNT(*) kèm điều kiện sẽ treo
 * trang. Thay vào đó lấy dư một dòng để biết còn trang sau hay không.
 */
export function logGameCauSql(key: string, loc: string, loc2: string, trang: number, bangKy?: string): { bangThat: string, sql: string, thamSo: string[] } {
  // Khoá lạ (kể cả '__proto__') -> bảng mặc định. KHÔNG BAO GIỜ để khoá thô lọt vào FROM.
  const k = Object.prototype.hasOwnProperty.call(LOG_GAME_BANG, key) ? key : LOG_GAME_MAC_DINH
  const ch = LOG_GAME_BANG[k]!
  // bangKy chỉ nhận khi đúng mẫu theo kỳ của CHÍNH mục này (người gọi lấy từ logGameDsKy) — chặn lần nữa ở đây.
  const kyHopLe = ch.theoKy && bangKy && logGameDsKy(k, ch.theoKy, [bangKy]).length === 1
  const bangThat = kyHopLe ? bangKy! : (ch.bangThat ?? k)
  const dieuKien: string[] = []
  const thamSo: string[] = []
  for (const [cot, gt] of Object.entries(ch.locCoDinh ?? {})) {
    dieuKien.push(`\`${cot}\` = ?`)
    thamSo.push(gt)
  }
  if (ch.loc !== null && loc !== '') {
    dieuKien.push(`\`${ch.loc}\` = ?`)
    thamSo.push(loc)
  }
  if (ch.loc2 !== null && loc2 !== '') {
    dieuKien.push(`\`${ch.loc2}\` = ?`)
    thamSo.push(loc2)
  }
  const where = dieuKien.length ? `WHERE ${dieuKien.join(' AND ')}` : ''
  const cotSql = Object.keys(ch.cot).map(c => `\`${c}\``).join(', ')
  // LIMIT/OFFSET là số nguyên đã ép kiểu nên ghép thẳng vào câu SQL được.
  // Chặn trên để OFFSET không thành dạng 1e+30 (SQL hỏng).
  const t = Math.min(1_000_000, Math.max(1, Math.trunc(trang) || 1))
  const lay = LOG_GAME_MOI_TRANG + 1
  const bo = (t - 1) * LOG_GAME_MOI_TRANG
  return {
    bangThat,
    sql: `SELECT ${cotSql} FROM \`${bangThat}\` ${where} ORDER BY ${ch.sap} LIMIT ${lay} OFFSET ${bo}`,
    thamSo,
  }
}
