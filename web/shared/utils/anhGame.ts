/**
 * Ảnh chụp THẬT trong game (bản 0.9.8, client PC 1920×1080 - cùng mã jar / APK, tắt lớp gỡ lỗi; giao diện khung gỗ cổ điển) —
 * public/anh-game/<ma>-<rộng>.webp,
 * mỗi ảnh hai cỡ 1280 / 640 px để trình duyệt tự chọn (srcset). Thêm ảnh: chụp lại bằng client_pc/test/ChupTrongGame,
 * đổi sang WebP (chất lượng ~78) ở hai cỡ rồi thêm một dòng ở đây.
 */
/** Bản game của bộ ảnh hiện tại — đổi khi chụp lại. */
export const BAN_ANH_GAME = '0.9.8'

export interface AnhGame { ma: string, tieuDe: string, moTa: string }

export const ANH_GAME: Record<string, AnhGame> = {
  'the-gioi': { ma: 'the-gioi', tieuDe: 'Thế giới mở', moTa: 'Thị trấn, đường đi, NPC và Pokémon hoang dã — camera cố định, ai cũng thấy cùng một khung cảnh.' },
  'chien-dau': { ma: 'chien-dau', tieuDe: 'Đánh thời gian thực', moTa: 'Chạm để chọn mục tiêu, chiêu + đánh thường, nút thuốc HP / PP, khắc hệ hiện ngay trên đầu quái.' },
  'thu-cuoi': { ma: 'thu-cuoi', tieuDe: 'Thú cưỡi & pet', moTa: 'Hồ sơ → Thú: cưỡi Rapidash, Tauros chạy nhanh hơn, chọn pet nhỏ lẽo đẽo theo sau.' },
  'tu-dong': { ma: 'tu-dong', tieuDe: 'Tự động đánh', moTa: 'Bảng cài đặt: thứ tự chiêu, ngưỡng thuốc HP / PP, vùng đánh, về hồi phục, tự hồi sinh, nhặt đồ.' },
  'khoi-dau': { ma: 'khoi-dau', tieuDe: 'Chọn Pokémon khởi đầu', moTa: 'Charmander, Bulbasaur, Squirtle, Pichu hay Eevee — chính bạn là Pokémon đó.' },
  'ky-nang': { ma: 'ky-nang', tieuDe: 'Kỹ năng', moTa: 'Học và nâng chiêu bằng điểm kỹ năng, gán vào ô nhanh.' },
  'trang-bi': { ma: 'trang-bi', tieuDe: 'Pokémon & trang bị', moTa: 'Ô trang bị, chỉ số thật, EXP và Tiềm năng.' },
  'cua-hang': { ma: 'cua-hang', tieuDe: 'Poké Mart', moTa: 'Mua thuốc hồi máu, Ether bằng xu — icon vật phẩm thật.' },
  'cua-hang-do': { ma: 'cua-hang-do', tieuDe: 'Cửa hàng trang bị', moTa: 'So sánh chỉ số với món đang mặc trước khi mua.' },
  'pokedex': { ma: 'pokedex', tieuDe: 'Pokédex', moTa: 'Lọc theo hệ, xem loài đã gặp / đã hạ.' },
  'menu': { ma: 'menu', tieuDe: 'Menu chính', moTa: 'Chạm Poké Ball ở đáy màn: Pokémon, Pokédex, Cửa hàng, Đội/Bạn bè, Sự kiện, cùng Hệ sinh thái, Thư/Quà, Xếp hạng, Cài đặt.' },
  'thu-qua': { ma: 'thu-qua', tieuDe: 'Thư / Quà & giftcode', moTa: 'Hộp thư và ô Nhập giftcode ở cùng một chỗ.' },
  'npc': { ma: 'npc', tieuDe: 'NPC ngoài trời', moTa: 'Y tá Chansey ngoài thị trấn: hồi phục Pokémon, mua thuốc — thẻ chức năng mở ngay cạnh NPC.' },
  'tien-hoa': { ma: 'tien-hoa', tieuDe: 'Tiến hoá', moTa: 'Đủ cấp là tiến hoá ngay trên bản đồ.' },
  'doi': { ma: 'doi', tieuDe: 'Đội & bạn bè', moTa: 'Lập đội đi săn, đánh boss cùng nhau.' },
  // 0.9.8 (giao diện khung gỗ cổ điển)
  'thu-phuc': { ma: 'thu-phuc', tieuDe: 'Thu phục Pokémon', moTa: 'Đánh yếu Pokémon hoang dã (dấu Poké Ball, vòng màu bậc) rồi chạm nút bóng cạnh cụm chiêu — nhãn ghi bóng sẽ ném và số còn.' },
  'hop-pc': { ma: 'hop-pc', tieuDe: 'Hộp PC', moTa: 'Pokémon đã bắt, Kẹo loài, tiến hoá và bạn đồng hành đi theo.' },
  'ho-so-tu-do': { ma: 'ho-so-tu-do', tieuDe: 'Tủ đồ trang trí', moTa: 'Hồ sơ → Ngoại hình: mặc thử trang phục, hào quang, skin thú và chọn dòng chỉ số của ô.' },
  'chat': { ma: 'chat', tieuDe: 'Chat nhiều kênh', moTa: 'Bản đồ, Thế giới, Đội, Nhóm, Bạn bè và tin riêng hai chiều trong một bảng — chỉ trong phiên đang chơi.' },
  'pho-ban': { ma: 'pho-ban', tieuDe: 'Phó bản Hầm ngục', moTa: 'Phòng riêng 5 tầng cho nhóm, boss ở tầng cuối.' },
}

/**
 * Ảnh CHƯA chụp (chưa có public/anh-game/<ma>-640|1280.webp) -> tạm dùng ảnh có sẵn. Khi thêm tệp ảnh thật thì
 * xoá dòng tương ứng (tests/he-thong-098.test.ts kiểm: ảnh không có trong bảng này phải có đủ hai tệp). 0.9.8: đã đủ.
 */
export const ANH_TAM: Record<string, string> = {}
const tep = (ma: string) => ANH_TAM[ma] ?? ma

/** srcset 640/1280 của một ảnh. */
export function srcsetAnhGame(ma: string): string {
  return `/anh-game/${tep(ma)}-640.webp 640w, /anh-game/${tep(ma)}-1280.webp 1280w`
}

/** Ảnh nhỏ (cỡ 640). */
export function srcAnhGameNho(ma: string): string {
  return `/anh-game/${tep(ma)}-640.webp`
}

/** Ảnh mặc định (cỡ 1280). */
export function srcAnhGame(ma: string): string {
  return `/anh-game/${tep(ma)}-1280.webp`
}

/** Icon vật phẩm thật (atlas 16×16 của game, server/map/icon_pokemon/<n>.png chép sang public/vat-pham/). */
export const ICON_VAT_PHAM = {
  thuoc: 172, berry: 286, daTienHoa: 189, daNangCap: 296, bua: 405, ngoc: 294, trung: 96, thuCuoi: 62, manh: 277, ve: 123, trangTri: 13, nhiemVu: 12,
} as const

export function iconVatPham(n: number): string {
  return `/vat-pham/${n}.png`
}

/**
 * Hình đại diện THẬT cho từng trang cẩm nang (thẻ ở /cam-nang, khung thả xuống Cẩm nang ▾): chân dung PMD của
 * Pokémon gắn với chủ đề (NPC / boss trong game), hoặc icon vật phẩm thật. Trang mới chưa có thì dùng icon Lucide.
 */
export const HINH_CAM_NANG: Record<string, { dex?: number, vp?: number }> = {
  'he-khac-che': { dex: 6 }, 'chi-so-sat-thuong': { dex: 68 }, 'chieu-thuc': { dex: 25 }, 'gym': { dex: 76 },
  'cap-do': { dex: 149 }, 'tu-dong': { vp: ICON_VAT_PHAM.ve }, 'so-tay': { dex: 178 }, 'tien-hoa': { dex: 133 },
  'trang-bi': { vp: ICON_VAT_PHAM.daNangCap }, 'vat-pham': { vp: ICON_VAT_PHAM.thuoc }, 'cua-hang': { dex: 352 },
  'kinh-te': { dex: 52 }, 'pokedex-shiny': { vp: ICON_VAT_PHAM.ngoc }, 'thu-cuoi-pet': { dex: 78 }, 'doi-su-pham': { dex: 214 },
  'pvp': { dex: 448 }, 'boss-dot-kich': { dex: 384 }, 'ham-nguc': { dex: 94 }, 'su-kien': { vp: ICON_VAT_PHAM.trangTri },
  'nhiem-vu': { dex: 143 }, 'ban-do': { dex: 18 }, 'he-sinh-thai': { dex: 1 }, 'cau-ca': { dex: 129 }, 'tai-khoan': { dex: 113 },
  'faq': { vp: ICON_VAT_PHAM.nhiemVu },
  // 0.9.8
  'thu-phuc': { dex: 25 }, 'danh-hieu': { vp: ICON_VAT_PHAM.trangTri }, 'trao-doi-cho': { dex: 52 },
  'kenh-chat': { dex: 441 }, 'ket-doi': { dex: 370 }, 'thoi-tiet': { dex: 351 },
}
