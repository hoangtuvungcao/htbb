/**
 * Kiểm một tên NPC / bản đồ trước khi ghi vào CSDL (trang /admin/ten-hien). Cùng luật với server test KiemTenHien:
 * có dấu tiếng Việt, hoặc chỉ gồm danh từ riêng trong RIENG_TEN / số (vd "Tauros 1", "Pewter"); không chứa từ kiếm hiệp cũ.
 * File thuần (không import gì) — tests/ten-hien.test.ts.
 */
export const TU_CAM_TEN = [
  'giang hồ', 'võ lâm', 'môn phái', 'bang hội', 'bang chủ', 'lãnh thổ', 'linh thú', 'xa phu', 'đại hiệp', 'tiền bối',
  'cao thủ', 'kiếm khách', 'kiếm hiệp', 'kiếm sư', 'tướng quân', 'thống lĩnh', 'lôi đài', 'đức vua', 'nhà vua',
  'thần tài', 'dì út', 'bà tám', 'anh bảy', 'hắc ngưu', 'thiết bì', 'phú ông', 'giáp sư', 'bội châu', 'lộc phát',
  'kim hoa', 'bảo ngọc', 'an tâm', 'ông nội', 'hoa tiêu', 'dao châu', 'tiên du', 'sơn nam', 'kỳ bố', 'phù liệt',
  'hàm tử', 'thạch giang', 'tử quan', 'trường giang', 'lộc trĩ', 'sơn lâm', 'phong châu', 'bình lục', 'bảo thái',
  'hầm tối', 'hồi hồ', 'siêu loại', 'cẩm khê', 'bạch hạc', 'tế giang', 'đỗ đông', 'bình kiều', 'dương đông', 'lâm tây',
  'chiến hạm', 'bến bắc', 'bến đông', 'bến tây', 'bến nam',
]

/** Danh từ riêng (Pokémon, địa danh) được viết không dấu. Tên không dấu khác phải thêm chữ có dấu ("Golem bán hàng 1"). */
export const RIENG_TEN = new Set([
  'pewter', 'pallet', 'viridian', 'vermilion', 'cerulean', 'lavender', 'celadon', 'saffron', 'fuchsia', 'cinnabar', 'new',
  'bark', 'cherrygrove', 'goldenrod', 'ecruteak', 'violet', 's.s.', 'anne', 'azalea', 'olivine', 'mahogany', 'ilex',
  'silver', 'blackthorn', 'cianwood', 'indigo', 'victory', 'safari', 'littleroot', 'petalburg', 'rustboro', 'dewford',
  'slateport', 'route', 'mauville', 'lavaridge', 'chimney', 'fortree', 'lilycove', 'pyre', 'mossdeep', 'sootopolis', 'ever',
  'grande', 'granite', 'meteor', 'hoenn', 'kanto', 'johto', 'zapdos', 'darkrai', 'salamence', 'chansey', 'kecleon',
  'machamp', 'bronzong', 'arcanine', 'xatu', 'persian', 'pidgeot', 'snorlax', 'kangaskhan', 'lapras', 'golem', 'tauros',
  'blaziken', 'meowth', 'torkoal', 'sableye', 'heracross', "farfetch'd", 'gardevoir', 'delibird', 'abomasnow', 'sudowoodo',
  'lucario', 'litleo', 'khu', 'nam', 'hang',
])

const DAU = /[àáảãạăằắẳẵặâầấẩẫậèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđ]/i

/** Lỗi (chuỗi tiếng Việt) hoặc null nếu tên hợp lệ. */
export function kiemTenHien(ten: string): string | null {
  const t = ten.trim()
  if (!t) return 'Tên không được để trống.'
  if (t.length > 64) return 'Tên dài quá 64 ký tự.'
  if (/[\u0000-\u001f<>]/.test(t)) return 'Tên có ký tự không cho phép.'
  const thuong = t.toLocaleLowerCase('vi')
  const cam = TU_CAM_TEN.find(w => new RegExp(`(^|[^\\p{L}])${w}($|[^\\p{L}])`, 'u').test(thuong))
  if (cam) return `Tên chứa từ cũ kiếm hiệp "${cam}".`
  if (DAU.test(t)) return null
  // không dấu: chỉ chấp nhận khi mọi từ là danh từ riêng đã biết (Pokémon / địa danh) hoặc số
  const tu = t.split(/[\s()/,-]+/).filter(Boolean)
  const sai = tu.find(w => !/^\d+x?$/.test(w) && !RIENG_TEN.has(w.toLowerCase()))
  if (sai) return `Tên "${t}" thiếu dấu tiếng Việt (từ "${sai}").`
  return null
}
