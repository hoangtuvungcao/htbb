#!/usr/bin/env node
/**
 * Đồng bộ dữ liệu CẨM NANG (wiki cơ chế) cho website từ repo game HTBB.
 *
 *   HTBB_DIR=../HTBB node scripts/dong-bo-cam-nang.mjs            # chỉ dữ liệu
 *   HTBB_DIR=../HTBB node scripts/dong-bo-cam-nang.mjs --anh-map  # + chép ảnh xem trước bản đồ
 *
 * Đọc (chỉ đọc, không sửa gì bên HTBB):
 *   docs/pokemon/bang_cap_do_lau_dai.csv   bảng EXP 1..100 + giờ mục tiêu
 *   docs/pokemon/nhiem_vu_moi.csv          nhiệm vụ (chính tuyến, phụ, ngày, tuần, Pokédex, đội...)
 *   docs/pokemon/su_kien_nam.csv           lịch 52 tuần (năm 1 + năm 2)
 *   docs/pokemon/thiet_ke_map.csv          danh sách bản đồ (dấu ";")
 *   docs/pokemon/bang_vat_pham.csv         đếm trang bị theo loại
 *   docs/pokemon/THIET_KE_LAU_DAI.md       (nếu có) chỉ để ghi nhận đã có tài liệu thiết kế
 *   docs/pokemon/map_moi/<id>.png          (chỉ khi --anh-map) ảnh xem trước bản đồ mới
 *   src/com/htbb/pokemon/ThongSoLauDai.java  hằng số thiết kế dài hạn (sinh từ sinh_sql_lau_dai.py)
 *
 * Ghi:
 *   shared/data/cam-nang.json   (sắp xếp cố định, không dấu thời gian -> chạy lại ra y hệt)
 *   public/ban-do/<id>.png      (chỉ khi --anh-map; chỉ ghi khi nội dung khác)
 *
 * Mọi con số trên các trang /cam-nang/* đọc từ JSON này — muốn đổi số thì đổi bên game
 * (tools/pokemon/sinh_sql_lau_dai.py ...) rồi chạy lại script. Không sửa tay JSON.
 * Không thêm thư viện: chỉ node:fs / node:path.
 */
import { copyFileSync, existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const WEB = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const HTBB = resolve(WEB, process.env.HTBB_DIR ?? '../HTBB')
const DOCS = join(HTBB, 'docs/pokemon')
const OUT_JSON = join(WEB, 'shared/data/cam-nang.json')
const OUT_MAP = join(WEB, 'public/ban-do')
const ANH_MAP = process.argv.includes('--anh-map')

if (!existsSync(join(DOCS, 'nhiem_vu_moi.csv'))) {
  console.error(`Không thấy ${DOCS}/nhiem_vu_moi.csv (đặt HTBB_DIR).`)
  process.exit(1)
}

const doc = p => readFileSync(join(HTBB, p), 'utf8')

// ---------------------------------------------------------------- CSV

function tachDong(l, sep) {
  const ra = []
  let o = ''
  let q = false
  for (let i = 0; i < l.length; i++) {
    const c = l[i]
    if (c === '"') {
      if (q && l[i + 1] === '"') { o += '"'; i++ }
      else q = !q
    }
    else if (c === sep && !q) { ra.push(o); o = '' }
    else o += c
  }
  ra.push(o)
  return ra
}

/** CSV có ngoặc kép; bỏ dòng chú thích "#"; dòng trong ngoặc kép có xuống dòng được nối lại. */
function docCsv(p, sep = ',') {
  const raw = doc(p).replace(/\r/g, '')
  const dong = []
  let buf = ''
  for (const l of raw.split('\n')) {
    buf = buf ? `${buf}\n${l}` : l
    if ((buf.match(/"/g)?.length ?? 0) % 2 === 0) { dong.push(buf); buf = '' }
  }
  const sach = dong.filter(l => l.trim() && !l.startsWith('#'))
  const dau = tachDong(sach[0], sep)
  return sach.slice(1).map((l) => {
    const c = tachDong(l, sep)
    return Object.fromEntries(dau.map((d, i) => [d, c[i] ?? '']))
  })
}

const n = s => (s === '' || s == null ? null : Number(s))

// ---------------------------------------------------------------- ThongSoLauDai.java

const JAVA = doc('src/com/htbb/pokemon/ThongSoLauDai.java')
function hang(ten) {
  const m = JAVA.match(new RegExp(`\\b${ten}\\s*=\\s*(-?\\d+)L?;`))
  if (!m) throw new Error(`ThongSoLauDai: thiếu ${ten}`)
  return Number(m[1])
}
function mang(ten) {
  const m = JAVA.match(new RegExp(`\\b${ten}\\s*=\\s*\\{([\\s\\S]*?)\\};`))
  if (!m) throw new Error(`ThongSoLauDai: thiếu mảng ${ten}`)
  return m[1]
}
const mang1 = ten => [...mang(ten).matchAll(/-?\d+/g)].map(x => Number(x[0]))
const mang2 = ten => [...mang(ten).matchAll(/\{([^{}]*)\}/g)].map(x => [...x[1].matchAll(/-?\d+/g)].map(y => Number(y[0])))
const mangChuoi = ten => [...mang(ten).matchAll(/"([^"]*)"/g)].map(x => x[1])

// ---------------------------------------------------------------- cấp độ

const bangCap = docCsv('docs/pokemon/bang_cap_do_lau_dai.csv').map(r => ({
  cap: n(r.cap),
  expLenCap: n(r.exp_len_cap),
  gioTichLuy: n(r.gio_tich_luy),
  gioCap: n(r.gio_cap),
  quaiMoiGio: n(r.quai_moi_gio),
  expMoiQuai: n(r.mob_exp),
  xuMoiQuai: n(r.xu_moi_quai),
  xuMoiGio: n(r.xu_moi_gio),
}))

const tranNgay = mang1('TRAN_CAP_NGAY')
const tranGiaTri = mang1('TRAN_CAP_GIA_TRI')

const capDo = {
  capToiDa: hang('CAP_TOI_DA'),
  tranCap: tranNgay.map((ngay, i) => ({ ngay, cap: tranGiaTri[i] })),
  bang: bangCap,
  nangLuong: {
    phutNgay: hang('NANG_LUONG_PHUT_NGAY'),
    phutTichToiDa: hang('NANG_LUONG_PHUT_TICH_TOI_DA'),
    bacMet: mang2('BAC_MET').map(([phut, tocDo]) => ({ phut, tocDo })),
  },
  batKip: { heSoPhanTram: hang('HE_SO_BAT_KIP'), khoangCap: hang('KHOANG_BAT_KIP') },
  tanThu: { duoiCap: hang('CAP_TAN_THU'), heSoPhanTram: hang('HE_SO_TAN_THU_PHAN_TRAM') },
  tinhThong: {
    phanTramCap: hang('TINH_THONG_PHAN_TRAM_CAP'),
    diemMoiMua: hang('TINH_THONG_DIEM_MOI_MUA'),
    toiDa: hang('TINH_THONG_TOI_DA'),
    phanNghinMoiDiem: hang('TINH_THONG_PHAN_NGHIN_MOI_DIEM'),
  },
}

// ---------------------------------------------------------------- trang bị

const bacTen = mangChuoi('BAC_TB_TEN')
const bacCap = mang1('BAC_TB_CAP')
const bacNgay = mang1('BAC_TB_NGAY_MO')
const bacGia = mang1('BAC_TB_GIA')
const tiLe = mang1('TI_LE_NC')
const da = mang1('DA_NC')
const bua = mang1('BUA_BAO_HO_LUONG_KHOA')
const chiSo = mang1('CHI_SO_NC_PHAN_TRAM')
const chiPhi = mang2('CHI_PHI_NC').map(r => r.slice(1))

/** Ô trang bị: type trong data_item -> tên Pokémon (KE_HOACH_CHUYEN_DOI.md §6.4). */
const O_TRANG_BI = [
  { type: [0], ten: 'Khăn quàng', vai: 'Thân (HP, phòng thủ)' },
  { type: [1], ten: 'Đai', vai: 'Thân (HP, phòng thủ)' },
  { type: [2], ten: 'Băng đô', vai: 'Đầu (phòng thủ)' },
  { type: [3, 4, 5, 6, 7], ten: 'Vật phẩm cầm theo hệ', vai: 'Tấn công — đúng dòng: Than lửa (Charmander), Hạt giống thần (Bulbasaur), Khăn lụa (Eevee), Nước thần (Squirtle), Nam châm (Pichu)' },
  { type: [8], ten: 'Huy hiệu ×2', vai: 'Hai ô nhẫn: chỉ số phụ' },
  { type: [9], ten: 'Vòng cổ', vai: 'Chỉ số phụ' },
  { type: [10], ten: 'Lông vũ tốc độ', vai: 'Tốc độ chạy, né' },
  { type: [11], ten: 'Găng đấm', vai: 'Chí mạng, tấn công' },
  { type: [12], ten: 'Phiến đá hệ', vai: 'Tăng sức mạnh theo hệ' },
  { type: [19], ten: 'Ruy băng', vai: 'Ô trang trí có chỉ số nhỏ (không vẽ lên thân)' },
]
const vatPham = docCsv('docs/pokemon/bang_vat_pham.csv')
const demType = {}
for (const r of vatPham) {
  if (!r.ten_moi || r.ten_moi.startsWith('(')) continue
  const t = Number(r.type)
  demType[t] = (demType[t] ?? 0) + 1
}
const BO_TB = [...(readFileSync(join(HTBB, 'tools/pokemon/sinh_sql_lau_dai.py'), 'utf8')
  .match(/BO_TB\s*=\s*\[(.*?)\]\s*#/s)?.[1] ?? '')
  .matchAll(/\((\d+),\s*"([^"]+)",\s*(\d+)\)/g)].map(m => ({ soMon: Number(m[1]), chiSo: m[2], phanTram: Number(m[3]) }))

const trangBi = {
  soO: hang('SO_O_TRANG_BI'),
  o: O_TRANG_BI.map(o => ({ ten: o.ten, vai: o.vai, loai: o.type.length > 1 ? 'vat-cam' : o.type[0] === 8 ? 'huy-hieu' : o.type[0] === 19 ? 'ruy-bang' : 'mac', soMon: o.type.reduce((s, t) => s + (demType[t] ?? 0), 0) })),
  bac: bacTen.map((ten, i) => ({ bac: i, ten, cap: bacCap[i], ngayMo: bacNgay[i], gia: bacGia[i] })),
  soMonMuaMoiBac: hang('SO_MON_MUA_MOI_BAC'),
  nangCap: tiLe.map((t, i) => ({ plus: i + 1, tiLe: t, da: da[i], buaLuongKhoa: bua[i], chiSoPhanTram: chiSo[i] })),
  chiPhiNangCap: chiPhi,
  pityChia: hang('PITY_CHIA'),
  matCapTu: hang('MAT_CAP_TU'),
  giuCapKhiDoiBac: hang('GIU_CAP_KHI_DOI_BAC'),
  daRoiPhanNghin: hang('DA_ROI_PHAN_NGHIN'),
  daTuPhanRa: hang('DA_TU_PHAN_RA'),
  boTrangBi: BO_TB,
  ngocHe: {
    capToiDa: hang('NGOC_HE_CAP_TOI_DA'),
    giaGioPhanTram: hang('NGOC_HE_GIA_GIO_PHAN_TRAM'),
    tangPhanTram: hang('NGOC_HE_TANG_PHAN_TRAM'),
  },
}

// ---------------------------------------------------------------- kinh tế

const kinhTe = {
  tiLeRoiXu: hang('TI_LE_ROI_XU'),
  thueCho: hang('THUE_CHO_PHAN_TRAM'),
  phiDangBan: hang('PHI_DANG_BAN_PHAN_TRAM'),
  tranGiaoDichGio: hang('TRAN_GIAO_DICH_TRUC_TIEP_GIO'),
  tieuHaoPhanTram: hang('TIEU_HAO_PHAN_TRAM'),
  suaDoPhanTram: hang('SUA_DO_PHAN_TRAM'),
  dichChuyenLanNgay: hang('DICH_CHUYEN_LAN_NGAY'),
  dichChuyenPhanTramGio: hang('DICH_CHUYEN_PHAN_TRAM_GIO'),
  suKienPhanXu: hang('SU_KIEN_PHAN_XU'),
  giftcodeToiDaThang: hang('GIFTCODE_TOI_DA_THANG'),
  trangTri: { phanNghinNgay: hang('TRANG_TRI_PHAN_NGHIN_NGAY'), nguongGio: hang('TRANG_TRI_NGUONG_GIO') },
  lichDangNhap: mang1('LICH_DANG_NHAP'),
  luongXepHangTuan: mang2('LUONG_XEP_HANG_TUAN').map(([top, luong]) => ({ top, luong })),
}

// ---------------------------------------------------------------- nhiệm vụ

const nv = docCsv('docs/pokemon/nhiem_vu_moi.csv')
const demLoai = {}
for (const r of nv) demLoai[r.loai] = (demLoai[r.loai] ?? 0) + 1

/** Cung chính tuyến: gom theo cột `cung` ("K1 Khởi hành từ Pallet"). */
const cung = []
for (const r of nv.filter(r => r.loai === 'chinh')) {
  const [ma, ...tenCung] = r.cung.split(' ')
  let c = cung.find(x => x.ma === ma)
  if (!c) {
    c = { ma, ten: tenCung.join(' '), vung: r.vung, nam: n(r.nam), chuong: [] }
    cung.push(c)
  }
  const soChuong = n(r.chuong)
  let ch = c.chuong.find(x => x.so === soChuong)
  if (!ch) {
    // Tên chương = tên bước đầu ("Lời nhờ: <tên chương>") hoặc tên dàn ý (năm 2).
    ch = { so: soChuong, ten: r.ten.replace(/^Lời nhờ:\s*/, ''), diaDanh: r.nam === '1' ? r.dia_danh : '', cap: n(r.cap_toi_thieu), soBuoc: 0, ngayMo: n(r.ngay_mo) ?? 0 }
    c.chuong.push(ch)
  }
  ch.soBuoc++
}
for (const c of cung) {
  c.capTu = Math.min(...c.chuong.map(x => x.cap))
  c.capDen = Math.max(...c.chuong.map(x => x.cap))
  if (c.nam === 2) for (const ch of c.chuong) ch.soBuoc = 0
}

const nhom = (loai, f) => {
  const ra = []
  for (const r of nv.filter(r => r.loai === loai)) {
    const k = f(r)
    let g = ra.find(x => x.ten === k)
    if (!g) { g = { ten: k, vung: r.vung, capTu: n(r.cap_toi_thieu), soBuoc: 0 }; ra.push(g) }
    g.soBuoc++
    g.capTu = Math.min(g.capTu, n(r.cap_toi_thieu))
  }
  return ra
}

// Chỉ giữ trường người chơi cần đọc (JSON này được đóng gói vào trang công khai — không mang
// mã nội bộ, ghi chú thiết kế, id bảng...).
const motDong = r => ({
  ten: r.ten, cap: n(r.cap_toi_thieu), npc: r.ten_npc, diaDanh: r.dia_danh,
  mucTieu: r.muc_tieu_ten, soLuong: n(r.so_luong), lapLai: r.lap_lai,
})

const nhiemVu = {
  demLoai,
  gioiHanNgay: hang('GIOI_HAN_NGAY'),
  gioiHanTuan: hang('GIOI_HAN_TUAN'),
  veChuongTuan: hang('VE_CHUONG_TUAN'),
  veChuongToiDa: hang('VE_CHUONG_TOI_DA'),
  veChuongBanDau: hang('VE_CHUONG_BAN_DAU'),
  huongDan: nv.filter(r => r.loai === 'huong_dan').map(motDong),
  cung,
  phu: nhom('phu', r => r.cung),
  tuyen: nhom('tuyen', r => r.cung.replace(/^Tuyến\s+/, '')),
  hamNguc: nv.filter(r => r.loai === 'hang_bi_an').map(motDong),
  suPham: nv.filter(r => r.loai === 'su_pham').map(motDong),
  doi: nv.filter(r => r.loai === 'doi').map(motDong),
  quayLai: nv.filter(r => r.loai === 'quay_lai').map(motDong),
  thanhTuu: nv.filter(r => r.loai === 'thanh_tuu').map(r => ({ ten: r.ten, soLuong: n(r.so_luong), kieu: r.kieu_muc_tieu.replace(/^thanh_tuu_/, ''), luongKhoa: n(r.luong_khoa), luong: n(r.luong) })),
  ngay: [...new Set(nv.filter(r => r.loai === 'ngay').map(r => r.ten))],
  tuan: [...new Set(nv.filter(r => r.loai === 'tuan').map(r => r.ten))],
  pokedexSoLoai: new Set(nv.filter(r => r.loai === 'pokedex').map(r => r.muc_tieu_id)).size,
}

// ---------------------------------------------------------------- sự kiện năm

const suKien = docCsv('docs/pokemon/su_kien_nam.csv').map(r => ({
  tuan: n(r.tuan),
  thang: n(r.thang),
  ten1: r.ten_nam1.replace(/\s*\(tuần \d\/\d\)$/, '').replace(/\s*\(theo âm lịch: tuần [\d-]+\)/, ''),
  ten2: r.ten_nam2,
  phan: r.ten_nam1.match(/\(tuần (\d\/\d)\)$/)?.[1] ?? '',
  loai: r.loai,
  coChe: r.co_che,
  soNgay: n(r.so_ngay),
  phanThuong: r.phan_thuong === '—' ? '' : r.phan_thuong,
  mocGiuChan: r.moc_giu_chan,
}))

// "Như tuần 3 + đá tiến hoá ×2" -> chép cơ chế của tuần 3 cho người đọc khỏi phải tra.
for (const sk of suKien) {
  const m = sk.coChe.match(/^Như tuần (\d+)(.*)$/)
  if (!m) continue
  const goc = suKien.find(x => x.tuan === Number(m[1]))
  if (goc && !/^Như tuần/.test(goc.coChe)) sk.coChe = `${goc.coChe}${m[2]}`
}

// ---------------------------------------------------------------- bản đồ

/** Tên có dấu cho tên ASCII trong thiet_ke_map.csv (địa danh Pokémon giữ nguyên). */
const DAU = [
  ['Trung tam Pokemon', 'Trung tâm Pokémon'], ['Poke Mart', 'Poké Mart'], ['Cua hang', 'Cửa hàng'],
  ['Nha kho', 'Nhà kho'], ['Xuong che tao', 'Xưởng chế tạo'], ['Nha (', 'Nhà ('], ['San nha khoi dau', 'Sân nhà khởi đầu'],
  ['Ben tau', 'Bến tàu'], ['Thuyen', 'Thuyền'], ['Hang dong', 'Hang động'], ['Hang Toi', 'Hang Tối'],
  ['Hang trong', 'Hang trống'], ['Hang nho', 'Hang nhỏ'], ['Hang co', 'Hang cổ'], ['Nguc toi', 'Ngục tối'],
  ['Ho Phan No (hang)', 'Hồ Phẫn Nộ (hang)'], ['Duong Victory', 'Đường Victory'], ['Cao nguyen', 'Cao nguyên'],
  ['Dau truong La Ma', 'Đấu trường La Mã'], ['Dau truong', 'Đấu trường'], ['Lanh dia doi', 'Lãnh địa đội'],
  ['Le cuoi', 'Lễ cưới'], ['Loi dai', 'Lôi đài'], ['Sanh cho', 'Sảnh chờ'], ['Thap luyen tap', 'Tháp luyện tập'],
  ['Khu mua ban', 'Khu mua bán'], ['Khu mo', 'Khu mỏ'], ['Nui Mat Trang', 'Núi Mặt Trăng'], ['Vung Safari', 'Vùng Safari'],
  ['Rung Viridian', 'Rừng Viridian'], ['Rung Co', 'Rừng Cổ'], ['Rung Ilex', 'Rừng Ilex'], ['Khoang rung nho', 'Khoảng rừng nhỏ'],
  ['Chien truong', 'Chiến trường'], ['Chan nui Silver', 'Chân núi Silver'], ['Nui Silver', 'Núi Silver'],
  ['Dong co phia tay', 'Đồng cỏ phía tây'], ['Tau S.S. Anne - dao', 'Tàu S.S. Anne - đảo'],
  [' - dong co', ' - đồng cỏ'], [' - dong bang', ' - đồng bằng'], [' - bo bien', ' - bờ biển'], [' - ho lon', ' - hồ lớn'],
  [' - rung lon', ' - rừng lớn'], [' - rung nho', ' - rừng nhỏ'], [' - rung', ' - rừng'], [' - dam lay', ' - đầm lầy'],
  [' - ho', ' - hồ'], [' - tuyen duong', ' - tuyến đường'], [' - ven bien', ' - ven biển'], [' - doi da', ' - đồi đá'],
  ['Hang Darkrai', 'Hang Darkrai'], ['Hang Salamence', 'Hang Salamence'],
]
const coDau = s => DAU.reduce((t, [a, b]) => t.split(a).join(b), s)

/** Vùng của địa danh: lấy từ nhiệm vụ (dia_danh -> vung). */
const vungDiaDanh = new Map()
for (const r of nv) if (r.dia_danh && ['Kanto', 'Johto', 'Hoenn'].includes(r.vung) && !vungDiaDanh.has(r.dia_danh)) vungDiaDanh.set(r.dia_danh, r.vung)
vungDiaDanh.set('Pewter', 'Kanto').set('Blackthorn', 'Johto').set('Cianwood', 'Johto')

const banDoRaw = docCsv('docs/pokemon/thiet_ke_map.csv', ';')
if (ANH_MAP) mkdirSync(OUT_MAP, { recursive: true })
let anhGhi = 0
const banDo = banDoRaw.map((r) => {
  const ten = coDau(r.ten)
  const goc = ten.split(/ - | \(|\s\d+$/)[0].trim()
  const id = Number(r.id)
  let anh = false
  const nguon = join(DOCS, 'map_moi', `${id}.png`)
  if (ANH_MAP && existsSync(nguon)) {
    const dich = join(OUT_MAP, `${id}.png`)
    if (!existsSync(dich) || !readFileSync(dich).equals(readFileSync(nguon))) { copyFileSync(nguon, dich); anhGhi++ }
    anh = true
  }
  else if (existsSync(join(OUT_MAP, `${id}.png`))) anh = true
  return { id, loai: r.loai, ten, vung: vungDiaDanh.get(goc) ?? vungDiaDanh.get(ten) ?? '', anh }
})

// ---------------------------------------------------------------- ghi

const ra = {
  capDo,
  trangBi,
  kinhTe,
  nhiemVu,
  suKien,
  banDo,
}
const noiDung = `${JSON.stringify(ra, null, 1)}\n`
mkdirSync(dirname(OUT_JSON), { recursive: true })
if (!existsSync(OUT_JSON) || readFileSync(OUT_JSON, 'utf8') !== noiDung) writeFileSync(OUT_JSON, noiDung)
console.log(`cam-nang.json: ${bangCap.length} cấp, ${cung.length} cung, ${suKien.length} tuần sự kiện, ${banDo.length} bản đồ, ${nv.length} nhiệm vụ.`)
if (ANH_MAP) console.log(`Ảnh bản đồ: ghi ${anhGhi} file vào ${OUT_MAP}`)
