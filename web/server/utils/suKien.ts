/**
 * Bật / tắt sự kiện trong game (bảng kpah_db.tob_su_kien) — phần logic thuần, không
 * đụng CSDL (truy vấn nằm ở suKienDb.ts).
 *
 * TRƯỚC NGÀY 30/08/2026 mỗi sự kiện được mở bằng cách so ngày hệ thống với một mốc
 * GHI CỨNG trong src/real/Char.java (toàn 2015–2024, nên đều tắt vĩnh viễn). Nay mỗi sự
 * kiện là một dòng trong `tob_su_kien`, bật lúc nào cũng được.
 *
 * AI GHI CỘT NÀO — trang web và game server không đè lên nhau:
 *
 *     Trang web ghi   :  bat, sua_boi, sua_luc, mo_luc, dong_luc
 *     Game server ghi :  ten, nhom, mo_ta, dang_bat, may_chu_luc
 *
 * Ngoại lệ duy nhất: khi một mốc trong `mo_luc`/`dong_luc` tới giờ thì `real/LichSuKien`
 * bên game ghi `bat` thay người, đặt `sua_boi='lich hen'` rồi xoá mốc đã chạy.
 *
 * TỰ KHỞI ĐỘNG LẠI. Đổi `bat` xong không phải tự restart: game server gom các thay đổi
 * trong `sv.suKienGomGiay` giây, báo trong game `sv.suKienBaoTruocGiay` giây, rồi bật cờ
 * `5h_systems.restart` để BaoTriDaily lưu dữ liệu và thoát — systemd bật lại sau 10 giây.
 * Web KHÔNG cần sudo/ssh, chỉ ghi CSDL. Tắt cơ chế đó bằng `sv.suKienTuKhoiDongLai=0`.
 *
 * Danh sách sự kiện KHÔNG nằm trong mã web: mỗi lần khởi động, server tự đăng danh mục
 * lên bảng (data/DatabaseSuKien.ghiAnhChup). Thêm sự kiện mới trong Java là tự hiện ở đây.
 *
 * VÌ SAO PHẢI KHỞI ĐỘNG LẠI: nhiều tác dụng của sự kiện được tính MỘT LẦN lúc khởi động —
 * bảng shop (Map.loadShop đặt SELL/NOT_SELL theo Char.isSuKien*), Map.isSale, danh sách
 * NPC, bảng vật phẩm. Đổi cờ giữa chừng thì nửa này hỏng nửa kia. So `sua_luc` với
 * `may_chu_luc` là cách biết có cần nhắc hay không.
 */

/** Phải khớp hằng số BANG trong src/data/DatabaseSuKien.java. */
export const SU_KIEN_BANG = 'tob_su_kien'

/** Mã của cổng tổng — phải khớp SuKien.TAT_CA bên Java. */
export const SU_KIEN_MA_CONG_TONG = 'tatca'

export interface SuKienDong {
  ma: string
  bat: number
  ten: string | null
  nhom: string | null
  mo_ta: string | null
  dang_bat: number
  sua_boi: string | null
  sua_luc: string | null
  may_chu_luc: string | null
  mo_luc: string | null
  dong_luc: string | null
}

function laNgayHopLe(nam: number, thang: number, ngay: number): boolean {
  if (thang < 1 || thang > 12 || ngay < 1 || nam < 1) return false
  const soNgay = new Date(Date.UTC(nam, thang, 0)).getUTCDate()
  return ngay <= soNgay
}

/**
 * Đổi giá trị ô <input type="datetime-local"> thành 'Y-m-d H:i:s', hoặc null nếu bỏ
 * trống. Trả về false khi không đọc được — người gọi phải phân biệt false với null.
 *
 * Trình duyệt gửi "2026-10-25T00:00" (có khi kèm ":ss"). Nhận cả dạng có dấu cách để
 * dán tay từ SQL vào cũng chạy. Dùng chung cho cả trang Bật/tắt server (giờ hẹn bật).
 */
export function suKienDocMoc(thoVao: unknown): string | null | false {
  if (thoVao === null || thoVao === undefined) return null
  if (typeof thoVao !== 'string') return false
  let tho = thoVao.trim()
  if (tho === '') return null
  tho = tho.replace(/T/g, ' ')
  const k = tho.match(/^(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2})(?::(\d{2}))?$/)
  if (!k) return false
  if (!laNgayHopLe(Number(k[1]), Number(k[2]), Number(k[3])) || Number(k[4]) > 23 || Number(k[5]) > 59) {
    return false
  }
  return `${k[1]}-${k[2]}-${k[3]} ${k[4]}:${k[5]}:${k[6] ?? '00'}`
}

/** Đổi 'Y-m-d H:i:s' của CSDL về dạng ô datetime-local ('Y-m-dTH:i'). */
export function suKienMocChoO(dbVal: string | null | undefined): string {
  if (dbVal === null || dbVal === undefined || dbVal === '') return ''
  return dbVal.replace(' ', 'T').slice(0, 16)
}

/**
 * Kiểm tra cặp mốc hẹn trước khi lưu. Trả về thông báo lỗi, hoặc null nếu hợp lệ.
 * `bayGio` PHẢI là NOW() của CSDL (xem suKienBayGio trong API) — LichSuKien so mốc bằng
 * NOW() của MySQL, web và MySQL có thể khác múi giờ.
 */
export function suKienKiemLich(mo: string | null, dong: string | null, bayGio: string): string | null {
  // Chặn mốc đã qua. Không phải để cầu toàn: mốc quá khứ sẽ chạy ngay ở nhịp kiểm tra kế
  // tiếp (LichSuKien so `<= NOW()`), nên đặt nhầm ngày năm ngoái là sự kiện bật/tắt tức
  // thì kèm một lần khởi động lại không ai ngờ tới.
  if (mo !== null && mo <= bayGio) {
    return `Mốc mở đã qua (${mo} ≤ ${bayGio}). Muốn bật ngay thì dùng nút Bật.`
  }
  if (dong !== null && dong <= bayGio) {
    return `Mốc đóng đã qua (${dong} ≤ ${bayGio}). Muốn tắt ngay thì dùng nút Tắt.`
  }
  // Đóng phải sau mở. Bên Java chạy mở trước rồi mới chạy đóng, nên nếu để đóng ≤ mở thì
  // khi cả hai cùng quá hạn kết quả luôn là TẮT — đúng luật nhưng chắc chắn không phải ý
  // người đặt. Chặn ngay từ đây cho rõ.
  if (mo !== null && dong !== null && dong <= mo) {
    return 'Mốc đóng phải sau mốc mở.'
  }
  return null
}

export interface SuKienPhanLoai {
  /** Lệch và do người sửa sau lần khởi động gần nhất — đang chờ khởi động lại. */
  choKhoiDong: SuKienDong[]
  /** Lệch nhưng sua_luc cũ hơn lần khởi động — ảnh chụp lúc bật máy bị hỏng. */
  anhChupHong: SuKienDong[]
  lichSapToi: { ma: string, ten: string, ve: 'mo' | 'dong', moc: string }[]
  mayChuLuc: string | null
  soDangBat: number
  soSeBat: number
  /** Cổng tổng đang tắt (chỉ đúng khi có dòng cổng tổng). */
  congTongTat: boolean
}

/**
 * Phân loại các dòng đọc được: đang chờ khởi động lại, ảnh chụp hỏng, lịch sắp tới, số đếm.
 *
 * Điều kiện "chờ khởi động lại" phải khớp TỪNG CHỮ với
 * `DatabaseSuKien.soDongChoKhoiDongLai()` bên Java: lệch, **và** thay đổi xảy ra sau lần
 * khởi động gần nhất. Nếu chỉ so `bat<>dang_bat` thì khi ảnh chụp lúc khởi động bị hỏng
 * (server ghi `dang_bat` thất bại), trang sẽ báo "chờ khởi động lại" mãi mãi trong khi
 * server cố tình không làm gì — và không ai hiểu vì sao.
 */
export function suKienPhanLoai(dong: SuKienDong[]): SuKienPhanLoai {
  let mayChuLuc: string | null = null
  for (const d of dong) {
    if (d.may_chu_luc !== null && (mayChuLuc === null || d.may_chu_luc > mayChuLuc)) {
      mayChuLuc = d.may_chu_luc
    }
  }

  const choKhoiDong: SuKienDong[] = []
  const anhChupHong: SuKienDong[] = []
  for (const d of dong) {
    if (Number(d.bat) === Number(d.dang_bat)) continue
    const doAiSua = d.sua_luc !== null
      && (d.may_chu_luc === null || d.sua_luc > d.may_chu_luc)
    if (doAiSua) choKhoiDong.push(d)
    else anhChupHong.push(d)
  }

  // Các sự kiện có mốc hẹn, sắp theo mốc gần nhất.
  const lichSapToi: SuKienPhanLoai['lichSapToi'] = []
  for (const d of dong) {
    for (const [ve, moc] of [['mo', d.mo_luc], ['dong', d.dong_luc]] as const) {
      if (moc !== null && moc !== '') {
        lichSapToi.push({ ma: d.ma, ten: String(d.ten ?? ''), ve, moc: String(moc) })
      }
    }
  }
  lichSapToi.sort((a, b) => (a.moc < b.moc ? -1 : a.moc > b.moc ? 1 : 0))

  let soDangBat = 0
  let soSeBat = 0
  let congTongTat = false
  for (const d of dong) {
    if (Number(d.dang_bat) === 1) soDangBat++
    if (Number(d.bat) === 1) soSeBat++
    if (d.ma === SU_KIEN_MA_CONG_TONG) congTongTat = Number(d.bat) !== 1
  }

  return { choKhoiDong, anhChupHong, lichSapToi, mayChuLuc, soDangBat, soSeBat, congTongTat }
}

/**
 * Gom theo nhóm. SELECT không có ORDER BY thì MySQL trả theo khoá chính, nên sắp nhóm
 * theo tên cho ổn định, trong nhóm giữ thứ tự trả về.
 *
 * Riêng nhóm chứa CỔNG TỔNG luôn đứng đầu: phải bật cổng tổng trước thì phần lớn sự kiện
 * mới có tác dụng, và lời nhắc trên trang chỉ người dùng tới "nhóm đầu tiên". Sắp thuần
 * theo tên thì nhóm khác (vd. "Bảng xếp hạng") sẽ chen lên trước.
 */
export function suKienTheoNhom(dong: SuKienDong[]): { nhom: string, ds: SuKienDong[] }[] {
  const m = new Map<string, SuKienDong[]>()
  let nhomCongTong: string | null = null
  for (const d of dong) {
    const k = String(d.nhom ?? '')
    if (!m.has(k)) m.set(k, [])
    m.get(k)!.push(d)
    if (d.ma === SU_KIEN_MA_CONG_TONG) nhomCongTong = k
  }
  return [...m.keys()]
    .sort((a, b) => {
      if (a === nhomCongTong) return -1
      if (b === nhomCongTong) return 1
      return a < b ? -1 : a > b ? 1 : 0
    })
    .map(nhom => ({ nhom, ds: m.get(nhom)! }))
}

export interface SuKienCauHinhKhoiDong {
  /** null = không đọc được server.ini (web có thể nằm khác máy game server). */
  tuKhoiDongLai: boolean | null
  gomGiay: number
  baoTruocGiay: number
}

/**
 * Đọc số nguyên đúng như game server đọc (Integer.parseInt sau khi trim): cả chuỗi phải
 * là một số nguyên 32 bit, sai định dạng hay thiếu khoá thì về mặc định. Đọc khác đi thì
 * trang sẽ hiện một con số khác với con số server thật sự dùng.
 */
function soNguyenNhuServer(v: string | undefined, macDinh: number): number {
  if (v === undefined) return macDinh
  const t = v.trim()
  if (!/^[+-]?\d+$/.test(t)) return macDinh
  const n = Number(t)
  return n >= -2147483648 && n <= 2147483647 ? n : macDinh
}

/**
 * Cấu hình tự khởi động lại, đọc từ server.ini của game server (đã parse).
 * `ini` rỗng = không đọc được — nói thẳng là không biết, không đoán bừa là "đang bật".
 * Kẹp y hệt CauHinh.apDungLichSuKien để con số hiện ra đúng cái server dùng.
 */
export function suKienCauHinhKhoiDong(ini: Record<string, string>): SuKienCauHinhKhoiDong {
  if (!ini || Object.keys(ini).length === 0) {
    return { tuKhoiDongLai: null, gomGiay: 120, baoTruocGiay: 60 }
  }
  return {
    tuKhoiDongLai: (ini['sv.suKienTuKhoiDongLai'] ?? '1') !== '0',
    gomGiay: Math.max(30, Math.min(3600, soNguyenNhuServer(ini['sv.suKienGomGiay'], 120))),
    baoTruocGiay: Math.max(10, Math.min(600, soNguyenNhuServer(ini['sv.suKienBaoTruocGiay'], 60))),
  }
}
