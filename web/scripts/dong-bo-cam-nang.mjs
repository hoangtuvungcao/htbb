#!/usr/bin/env node
/**
 * Đồng bộ dữ liệu CẨM NANG (wiki cơ chế) cho website từ repo game HTBB.
 *
 *   HTBB_DIR=.. node scripts/dong-bo-cam-nang.mjs            # chỉ dữ liệu
 *   HTBB_DIR=.. node scripts/dong-bo-cam-nang.mjs --anh-map  # + chép ảnh xem trước bản đồ
 *
 * Đọc (chỉ đọc, không sửa gì bên HTBB):
 *   du_lieu/bang_cap_do_lau_dai.csv   bảng EXP 1..100 + giờ mục tiêu
 *   du_lieu/nhiem_vu_moi.csv          nhiệm vụ (chính tuyến, phụ, ngày, tuần, Pokédex, đội...)
 *   du_lieu/su_kien_nam.csv           lịch 52 tuần (năm 1 + năm 2)
 *   du_lieu/thiet_ke_map.csv          danh sách bản đồ (dấu ";")
 *   du_lieu/bang_vat_pham.csv         đếm trang bị theo loại
 *   docs/thiet-ke-game.md       (nếu có) chỉ để ghi nhận đã có tài liệu thiết kế
 *   du_lieu/map_moi/<id>.png          (chỉ khi --anh-map) ảnh xem trước bản đồ mới
 *   src/com/htbb/pokemon/ThongSoLauDai.java  hằng số thiết kế dài hạn (sinh từ sinh_sql_lau_dai.py)
 *
 * Ghi:
 *   shared/data/cam-nang.json   (sắp xếp cố định, không dấu thời gian -> chạy lại ra y hệt)
 *   shared/data/cam-nang-noi-dung.json  (vật phẩm, cửa hàng, Gym, boss, PvP, sổ tay, hầm ngục, hệ sinh thái,
 *                               câu cá, cốt truyện — từ du_lieu/web/*, diem_cau_ca.csv + hằng Java)
 *   public/ban-do/<id>.webp     (chỉ khi --anh-map; thu nhỏ ≤ 640×480 bằng ImageMagick nếu có)
 *
 * Mọi con số trên các trang /cam-nang/* đọc từ JSON này — muốn đổi số thì đổi bên game
 * (tools/pokemon/sinh_sql_lau_dai.py ...) rồi chạy lại script. Không sửa tay JSON.
 * Không thêm thư viện: chỉ node:fs / node:path.
 */
import { spawnSync } from 'node:child_process'
import { copyFileSync, existsSync, mkdirSync, readFileSync, statSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const WEB = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const HTBB = resolve(WEB, process.env.HTBB_DIR ?? '..')
const DOCS = join(HTBB, 'du_lieu')
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

const JAVA = doc('server/src/com/htbb/pokemon/ThongSoLauDai.java')
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

const bangCap = docCsv('du_lieu/bang_cap_do_lau_dai.csv').map(r => ({
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

/** Ô trang bị: type trong data_item -> tên Pokémon (docs/kien-truc.md §6.4). */
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
const vatPham = docCsv('du_lieu/bang_vat_pham.csv')
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

const nv = docCsv('du_lieu/nhiem_vu_moi.csv')
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
  // Câu mục tiêu hiện trong game ("Đóng góp 345 điểm cho đội") — chuẩn hơn ghép soLuong + mucTieu.
  yeuCau: r.muc_tieu_text ?? '',
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

const suKien = docCsv('du_lieu/su_kien_nam.csv').map(r => ({
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

// Tuần hệ: CSV ghi chung "Quái hệ X ..." -> điền hệ của tuần (lấy từ tên năm 1 "Tuần hệ Băng").
for (const sk of suKien) {
  const he = sk.ten1.match(/hệ (\S+)/)?.[1]
  if (he && /hệ X\b/.test(sk.coChe)) sk.coChe = sk.coChe.replace(/hệ X\b/g, `hệ ${he}`)
}

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
  ['Le ket doi', 'Lễ kết đôi'], ['Loi dai', 'Sàn đấu'], ['Sanh cho', 'Sảnh chờ'], ['Thap luyen tap', 'Tháp luyện tập'],
  ['Khu mua ban', 'Khu mua bán'], ['Khu mo', 'Khu mỏ'], ['Nui Mat Trang', 'Núi Mặt Trăng'], ['Vung Safari', 'Vùng Safari'],
  ['Rung Viridian', 'Rừng Viridian'], ['Rung Co', 'Rừng Cổ'], ['Rung Ilex', 'Rừng Ilex'], ['Khoang rung nho', 'Khoảng rừng nhỏ'],
  ['Chien truong', 'Chiến trường'], ['Chan nui Silver', 'Chân núi Silver'], ['Nui Silver', 'Núi Silver'],
  ['Dong co phia tay', 'Đồng cỏ phía tây'], ['Tau S.S. Anne - dao', 'Tàu S.S. Anne - đảo'],
  [' - dong co', ' - đồng cỏ'], [' - dong bang', ' - đồng bằng'], [' - bo bien', ' - bờ biển'], [' - ho lon', ' - hồ lớn'],
  [' - rung lon', ' - rừng lớn'], [' - rung nho', ' - rừng nhỏ'], [' - rung', ' - rừng'], [' - dam lay', ' - đầm lầy'],
  [' - ho', ' - hồ'], [' (sa mac)', ' (sa mạc)'], [' (tuyet)', ' (tuyết)'], [' (nui)', ' (núi)'], [' (hang)', ' (hang)'], [' - tuyen duong', ' - tuyến đường'], [' - ven bien', ' - ven biển'], [' - doi da', ' - đồi đá'],
  ['Hang Darkrai', 'Hang Darkrai'], ['Hang Salamence', 'Hang Salamence'],
]
const coDau = s => DAU.reduce((t, [a, b]) => t.split(a).join(b), s)

/** Vùng của địa danh: lấy từ nhiệm vụ (dia_danh -> vung). */
const vungDiaDanh = new Map()
for (const r of nv) if (r.dia_danh && ['Kanto', 'Johto', 'Hoenn'].includes(r.vung) && !vungDiaDanh.has(r.dia_danh)) vungDiaDanh.set(r.dia_danh, r.vung)
vungDiaDanh.set('Pewter', 'Kanto').set('Blackthorn', 'Johto').set('Cianwood', 'Johto')

/** Bản đồ Hoenn (năm 2): tên có dấu lấy từ nguồn duy nhất BanDoHoenn.java. */
const TEN_HOENN = new Map([...doc('server/src/com/htbb/pokemon/BanDoHoenn.java').matchAll(/them\((\d+),\s*"\w+",\s*"([^"]+)"/g)].map(m => [Number(m[1]), m[2]]))
const banDoRaw = docCsv('du_lieu/thiet_ke_map.csv', ';')
if (ANH_MAP) mkdirSync(OUT_MAP, { recursive: true })
let anhGhi = 0
/**
 * Ảnh xem trước gốc (du_lieu/map_moi/<id>.png) tới 2000×2000 px, cả thư mục ~22 MB — quá nặng
 * cho web. Có ImageMagick (`convert`) thì thu về ảnh WebP tối đa 640×480 (~20–60 KB); không có thì
 * chép PNG gốc (cảnh báo). Kích thước ghi vào JSON để trang đặt width/height (không nhảy bố cục).
 */
const coConvert = spawnSync('convert', ['-version'], { encoding: 'utf8' }).status === 0
if (ANH_MAP && !coConvert) console.warn('Cảnh báo: không có ImageMagick `convert` — chép PNG gốc (nặng). Cài imagemagick rồi chạy lại.')
const kichThuoc = (f) => {
  const r = spawnSync('identify', ['-format', '%w %h', f], { encoding: 'utf8' })
  const [w, h] = (r.stdout ?? '').trim().split(' ').map(Number)
  return w && h ? { w, h } : null
}
const banDo = banDoRaw.map((r) => {
  const ten = coDau(r.ten)
  const goc = ten.split(/ - | \(|\s\d+$/)[0].trim()
  const id = Number(r.id)
  let anh = ''
  let kt = null
  const nguon = join(DOCS, 'map_moi', `${id}.png`)
  const webp = join(OUT_MAP, `${id}.webp`)
  const png = join(OUT_MAP, `${id}.png`)
  if (ANH_MAP && existsSync(nguon)) {
    if (coConvert) {
      if (!existsSync(webp) || statSync(webp).mtimeMs < statSync(nguon).mtimeMs) {
        const kq = spawnSync('convert', [nguon, '-resize', '640x480>', '-strip', '-quality', '80', `webp:${webp}`])
        if (kq.status !== 0) throw new Error(`convert ${nguon}: ${kq.stderr}`)
        anhGhi++
      }
    }
    else if (!existsSync(png) || !readFileSync(png).equals(readFileSync(nguon))) { copyFileSync(nguon, png); anhGhi++ }
  }
  if (existsSync(webp)) { anh = `/ban-do/${id}.webp`; kt = kichThuoc(webp) }
  else if (existsSync(png)) { anh = `/ban-do/${id}.png`; kt = kichThuoc(png) }
  const hoenn = TEN_HOENN.get(id)
  return { id, loai: r.loai, ten: hoenn ?? ten, vung: hoenn ? 'Hoenn' : (vungDiaDanh.get(goc) ?? vungDiaDanh.get(ten) ?? ''), anh, rong: kt?.w ?? 0, cao: kt?.h ?? 0 }
})
const tenBanDo = id => banDo.find(b => b.id === id)?.ten

// ---------------------------------------------------------------- nội dung chi tiết (cam-nang-noi-dung.json)
//
// File riêng, chỉ các trang cần mới import (vật phẩm, cửa hàng, Gym, boss, hệ sinh thái, sổ tay,
// cốt truyện...). Nguồn: du_lieu/web/*.csv + nhiem_vu_tom_tat.json (bản "cho web" do nhóm
// game xuất), du_lieu/diem_cau_ca.csv, và hằng số Java (ưu tiên code hơn tài liệu thiết kế).

const javaSrc = p => doc(p)
const hangJ = (src, ten, tenFile) => {
  const m = src.match(new RegExp(`\\b${ten}\\s*=\\s*(-?\\d+)L?(?:\\s*\\*\\s*(\\d+)L?)*(?:\\s*\\*\\s*(\\d+)L?)?;`))
  if (!m) throw new Error(`${tenFile}: thiếu ${ten}`)
  // hỗ trợ dạng "10L * 60L * 1000L"
  const bt = m[0].split('=')[1].replace(/[L;\s]/g, '')
  return bt.split('*').reduce((a, b) => a * Number(b), 1)
}
const mangJ = (src, ten, tenFile) => {
  const m = src.match(new RegExp(`\\b${ten}\\s*=\\s*\\{([\\s\\S]*?)\\};`))
  if (!m) throw new Error(`${tenFile}: thiếu mảng ${ten}`)
  return m[1]
}
const soJ = s => [...s.matchAll(/-?\d+/g)].map(x => Number(x[0]))
const chuoiJ = s => [...s.matchAll(/"([^"]*)"/g)].map(x => x[1])
const web = f => docCsv(`du_lieu/web/${f}`)
const soHoac = v => (v === '' || v == null || Number.isNaN(Number(v)) ? null : Number(v))

// --- vật phẩm tiêu hao & nguyên liệu (web/vat_pham.csv)
const vatPhamWeb = web('vat_pham.csv').map(r => ({
  ten: r['Tên'], nhom: r['Nhóm'], congDung: r['Công dụng'], cap: r['Cấp'],
  gia: soHoac(r['Giá']), tien: r['Tiền'], nguon: r['Nguồn'], gioiHan: r['Giới hạn'], sapCo: r['Sắp có'] === 'có',
}))

// --- cửa hàng (web/cua_hang.csv)
const cuaHang = []
for (const r of web('cua_hang.csv')) {
  const ten = r['Cửa hàng']
  if (!ten || ten === 'Cửa hàng') continue
  let c = cuaHang.find(x => x.ten === ten)
  if (!c) { c = { ten, hang: [] }; cuaHang.push(c) }
  c.hang.push({ vatPham: r['Vật phẩm'], gia: soHoac(r['Giá']), tien: r['Tiền'], cap: soHoac(r['Cấp']), gioiHan: r['Giới hạn'] })
}

// --- Gym (web/gym.csv + noi_dung_gym.csv cho chỉ số + GymLauDai.java cho luật)
const gymJava = javaSrc('server/src/real/GymLauDai.java')
const boss2 = javaSrc('server/src/com/htbb/pokemon/GiaiDoanBoss.java')
const gymNd = docCsv('du_lieu/noi_dung_gym.csv')
const TEN_BAC_TB = bacTen
const gym = web('gym.csv').map((r) => {
  const nd = gymNd.find(x => Number(x.stt) === Number(r.Gym)) ?? {}
  return {
    so: n(r.Gym), huyHieu: r['Huy hiệu'], noi: r['Nơi'], he: r['Hệ'], cap: n(r['Cấp']),
    truong: r['Trưởng Gym'].replace(/^Trưởng Gym [^:]+:\s*/, ''),
    hp: n(nd.hp), tanCong: n(nd.tan_cong), phongThu: n(nd.phong_thu),
    bacThuong: TEN_BAC_TB[n(nd.bac_thuong)] ?? '',
  }
})
const huyChuongTuan = Number(/^(\d+) Huy chương Gym/.exec(gymNd[0]?.thuong_tuan ?? '')?.[1] ?? 0)
const aoChoang = cuaHang.find(c => c.ten === 'Huy chương Gym')?.hang[0] ?? null
const gymLuat = {
  phutGioiHan: hangJ(gymJava, 'THOI_GIAN_MS', 'GymLauDai.java') / 60000,
  phutNoiGian: hangJ(boss2, 'NO_GYM_MS', 'GiaiDoanBoss.java') / 60000,
  huyChuongTuan,
  aoChoang: aoChoang ? { ten: aoChoang.vatPham.replace(/\s*\(.*\)$/, ''), gia: aoChoang.gia } : null,
}

// --- chế tạo, bộ trang bị, nhánh Eevee, trang trí, vật phẩm sự kiện
const cheTao = web('che_tao.csv').map(r => ({ ten: r['Thành phẩm'], cap: n(r['Cấp']), da: n(r['Đá nâng cấp']), phu: r['Nguyên liệu phụ'], xu: n(r.Xu), noi: r['Nơi chế tạo'] }))
const nhanhEevee = web('nhanh_eevee.csv').map(r => ({ ten: r['Nhánh'], he: r['Hệ'], vai: r['Vai trò'], chieu: r['Chiêu đặc trưng'], cap: n(r['Cấp']) }))
const trangTri = web('trang_tri.csv').map(r => ({ mua: r['Mùa'], loai: r['Loại'], ten: r['Tên'], gia: soHoac(r['Giá']), tien: r['Tiền'], nguon: r['Nguồn'] }))
const suKienVatPham = web('su_kien.csv').map(r => ({ tuan: n(r['Tuần']), ten: r['Sự kiện'], ten2: r['Năm 2'], vatPham: r['Vật phẩm'], toiDaTuan: soHoac(r['Tối đa/tuần']), doiLay: r['Đổi lấy'] }))

// --- boss thế giới (web/boss_the_gioi.csv + LauDaiJob.java)
const jobJava = javaSrc('server/src/real/LauDaiJob.java')
const bossTheGioi = {
  lich: web('boss_the_gioi.csv').map(r => ({ tuan: n(r['Tuần']), nam1: r['Boss năm 1'], nam2: r['Boss năm 2'], he: r['Hệ'], luotNgay: n(r['Lượt/ngày']) })),
  // "thu Bay 20:00; tuan dot kich them 12:30" — kiểm chuỗi để biết code còn đúng giờ này.
  gioThuBay: /thu Bay 20:00/.test(jobJava) ? '20:00' : '',
  gioDotKich: [...jobJava.matchAll(/phut == (\d+) \* 60 \+ (\d+)/g)].map(m => `${m[1]}:${m[2].padStart(2, '0')}`),
  phutNoiGian: hangJ(boss2, 'NO_THE_GIOI_MS', 'GiaiDoanBoss.java') / 60000,
}

// --- PvP mùa (ThangPvp.java). LUONG_BAC chưa được code nào dùng -> KHÔNG đưa lên web.
const pvpJava = javaSrc('server/src/com/htbb/pokemon/ThangPvp.java')
const tenBac = chuoiJ(mangJ(pvpJava, 'TEN_BAC', 'ThangPvp.java'))
const nguongBac = soJ(mangJ(pvpJava, 'NGUONG_BAC', 'ThangPvp.java'))
const pvp = {
  eloBanDau: hangJ(pvpJava, 'ELO_BAN_DAU', 'ThangPvp.java'),
  k: hangJ(pvpJava, 'K', 'ThangPvp.java'),
  tuanMoiMua: hangJ(pvpJava, 'TUAN_MOI_MUA', 'ThangPvp.java'),
  bac: tenBac.map((ten, i) => ({ ten, tuElo: i === 0 ? 0 : nguongBac[i - 1] })),
  daiCap: 10,
  phutCungCap: Number(/CHAN_LAP_MS = (\d+)L \* 60L \* 1000L/.exec(javaSrc('server/src/real/ThangPvpGame.java'))?.[1] ?? NaN),
}

// --- Sổ tay huấn luyện, Hầm ngục, quà hoạt động
const soTayJava = javaSrc('server/src/real/SoTayLauDai.java')
const cuonHut = javaSrc('server/src/com/htbb/pokemon/CuonHut.java')
const quaHd = javaSrc('server/src/com/htbb/pokemon/QuaHoatDongLogic.java')
const hnJava = javaSrc('server/src/real/HamNgucGame.java')
const hnExp = /expMoiGio\([^)]*\)\) \* (\d+)L \/ 100L \* Math\.min\((\d+), tang\)/.exec(hnJava)
const hnLk = /tang >= (\d+) \? (\d+) : 0/.exec(hnJava)
if (!hnExp || !hnLk) throw new Error('HamNgucGame.java: đổi công thức thưởng, sửa script')
const soTay = {
  muc: chuoiJ(/moMenu\(final Char p\)[\s\S]*?new String\[\]\{([\s\S]*?)\}/.exec(soTayJava)?.[1] ?? ''),
  quaHoatDong: {
    phut: soJ(mangJ(quaHd, 'MOC_PHUT', 'QuaHoatDongLogic.java')),
    luongKhoa: soJ(mangJ(quaHd, 'MOC_LUONG_KHOA', 'QuaHoatDongLogic.java')),
    da: soJ(mangJ(quaHd, 'MOC_DA', 'QuaHoatDongLogic.java')),
    xuPhanTramGio: soJ(mangJ(quaHd, 'MOC_XU_PHAN_TRAM_GIO', 'QuaHoatDongLogic.java')),
    nguyenLieu: soJ(mangJ(quaHd, 'MOC_NGUYEN_LIEU', 'QuaHoatDongLogic.java')),
    giayDungYen: hangJ(quaHd, 'KHOANG_TOI_DA_MS', 'QuaHoatDongLogic.java') / 1000,
    ngayThang: hangJ(quaHd, 'NGAY_THANG_CAN', 'QuaHoatDongLogic.java'),
  },
  suPham: {
    capThay: hangJ(cuonHut, 'SU_PHAM_CAP_THAY', 'CuonHut.java'),
    cachCap: hangJ(cuonHut, 'SU_PHAM_CACH_CAP', 'CuonHut.java'),
    expPhanTram: hangJ(cuonHut, 'SU_PHAM_EXP_PHAN_TRAM', 'CuonHut.java'),
    moc: soJ(mangJ(cuonHut, 'SU_PHAM_MOC', 'CuonHut.java')),
  },
}
if (soTay.muc.length < 5) throw new Error('SoTayLauDai.java: không đọc được menu')
const hamNguc = {
  tangToiDa: hangJ(cuonHut, 'HAM_NGUC_TANG_TOI_DA', 'CuonHut.java'),
  lechCap: Number(/Math\.abs\(p\.lvDetail\.lv - mt\.level\) > (\d+)/.exec(hnJava)?.[1] ?? NaN),
  expPhanTramGio: Number(hnExp[1]),
  tangDuExp: Number(hnExp[2]),
  tangLuongKhoa: Number(hnLk[1]),
  luongKhoa: Number(hnLk[2]),
}

// --- Hệ sinh thái (HeSinhThai.java, KyNangSong.java, VatLieuSinhThai.java, diem_cau_ca.csv)
const hst = javaSrc('server/src/com/htbb/pokemon/HeSinhThai.java')
const kns = javaSrc('server/src/com/htbb/pokemon/KyNangSong.java')
const vlst = javaSrc('server/src/com/htbb/pokemon/VatLieuSinhThai.java')
const H = t => hangJ(hst, t, 'HeSinhThai.java')
const tenVl = Object.fromEntries([...vlst.matchAll(/dn\((\d+),\s*"([^"]+)",\s*\w+,\s*(\d+)\)/g)].map(m => [Number(m[1]), { ten: m[2], gia: Number(m[3]) }]))
const cauCaBang = [...mangJ(kns, 'BANG_CAU_CA', 'KyNangSong.java').matchAll(/\{([^{}]*)\}/g)].map((m) => {
  const [ma, , trongSo, capKn, nuoc] = soJ(m[1])
  return { ten: tenVl[ma]?.ten ?? `#${ma}`, trongSo, capKyNang: capKn, nuoc: nuoc === 3 ? 'cả hai' : nuoc === 1 ? 'ngọt' : 'mặn', hoLon: (tenVl[ma]?.gia ?? 0) >= 100 }
})
const KY_NANG = chuoiJ(mangJ(kns, 'String\\[\\] TEN', 'KyNangSong.java'))
const maKn = Object.fromEntries([...kns.matchAll(/\b(CAU_CA|TRONG_TROT|KHAO_CO|NAU_AN|CHE_THUOC) = (\d+)/g)].map(m => [m[1], Number(m[2])]))
const congThuc = [...mangJ(kns, 'CONG_THUC', 'KyNangSong.java').matchAll(/new CongThuc\(\d+,\s*"([^"]+)",\s*(\w+),\s*(\d+),\s*new int\[\]\[\]\{(.*?)\},\s*([\w.]+),\s*(\d+),\s*(null|"[^"]*")\)/g)].map(m => ({
  ten: m[1], kyNang: KY_NANG[maKn[m[2]]] ?? m[2], cap: Number(m[3]),
  nguyenLieu: [...m[4].matchAll(/\{(\d+),\s*(\d+)\}/g)].map(x => `${x[2]} ${tenVl[Number(x[1])]?.ten ?? '?'}`),
}))
if (congThuc.length < 20) throw new Error('KyNangSong.CONG_THUC: không đọc được công thức')
const diemCau = docCsv('du_lieu/diem_cau_ca.csv')
const cauCaMap = []
for (const r of diemCau) {
  const id = Number(r.map_id)
  let m = cauCaMap.find(x => x.map === id)
  if (!m) { m = { map: id, ten: r.ten_map, ho: new Map(), diem: 0 }; cauCaMap.push(m) }
  m.ho.set(r.ho_id, r.loai_ho)
  m.diem++
}
const heSinhThai = {
  theLucNgay: H('THE_LUC_NGAY'), theLucToiDa: H('THE_LUC_TOI_DA'),
  tlCauCa: H('TL_CAU_CA'), tlDao: H('TL_DAO'), tlNau: H('TL_NAU'),
  capKyNangToiDa: H('CAP_KY_NANG_TOI_DA'), kyNang: KY_NANG,
  thayLoaiPhanTram: H('TI_LE_THAY_PHAN_TRAM'), bungPhatPhanTram: H('TI_LE_BUNG_PHAT_PHAN_TRAM'),
  thoiTietPhanTram: H('THOI_TIET_SAT_THUONG_PHAN_TRAM'), heSoSinh: H('HE_SO_SINH_HOP_THOI_TIET'),
  gioBungPhat: soJ(mangJ(hst, 'GIO_BUNG_PHAT', 'HeSinhThai.java')),
  gioDemTu: H('GIO_DEM_TU'), gioDemDen: H('GIO_DEM_DEN'),
  nhaTre: { buocTrung: H('BUOC_DE_TRUNG'), trungNgay: H('TRUNG_TOI_DA_NGAY'), trungTui: H('TRUNG_TOI_DA_TUI'), shinyMau: H('SHINY_TRUNG_MAU'), shinyPity: H('SHINY_TRUNG_PITY'), ivToiDa: H('IV_TOI_DA') },
  cho: { thue: H('THUE_CHO_PHAN_TRAM'), phiDang: H('PHI_DANG_PHAN_TRAM'), npcPhanTram: H('SAN_NPC_PHAN_TRAM'), giaToiDaLan: H('GIA_TOI_DA_LAN'), hanNgay: H('HAN_CHO_NGAY'), tinBan: H('TOI_DA_TIN_BAN'), don: H('TOI_DA_DON') },
  doVui: { gio: soJ(mangJ(hst, 'GIO_DO_VUI', 'HeSinhThai.java')).map(g => `${g}:${String(H('PHUT_DO_VUI')).padStart(2, '0')}`), giay: H('GIAY_TRA_LOI'), luongKhoa: H('THUONG_DO_VUI_LUONG_KHOA') },
  dua: { gio: `${H('GIO_DUA')}:00`, phutDangKy: H('PHUT_DANG_KY_DUA'), thuong: soJ(mangJ(hst, 'THUONG_DUA_LUONG_KHOA', 'HeSinhThai.java')), giayToiDa: H('DUA_TOI_DA_GIAY') },
  nghienCuuLuongKhoa: H('THUONG_NGHIEN_CUU_LUONG_KHOA'),
  vung: { luongKhoa: H('THUONG_VUNG_LUONG_KHOA'), phanNghin: H('CHI_SO_VUNG_PHAN_NGHIN'), tranPhanNghin: H('CHI_SO_VUNG_TRAN_PHAN_NGHIN') },
  meThuocNgay: hangJ(kns, 'ME_THUOC_TOI_DA_NGAY', 'KyNangSong.java'),
  album: (() => {
    const st = javaSrc('server/src/com/htbb/pokemon/SuuTamSinhThai.java')
    const moc = soJ(mangJ(st, 'MOC_ALBUM', 'SuuTamSinhThai.java'))
    const thuong = soJ(mangJ(st, 'THUONG_MOC_ALBUM', 'SuuTamSinhThai.java'))
    const danhHieu = chuoiJ(mangJ(st, 'DANH_HIEU_ALBUM', 'SuuTamSinhThai.java'))
    return { toiDa: hangJ(st, 'ALBUM_TOI_DA', 'SuuTamSinhThai.java'), loai: chuoiJ(mangJ(st, 'TEN_ANH', 'SuuTamSinhThai.java')), moc: moc.map((m, i) => ({ anh: m, luongKhoa: thuong[i], danhHieu: danhHieu[i] ?? '' })) }
  })(),
  cauCa: {
    bang: cauCaBang,
    tongDiem: diemCau.length,
    ban: cauCaMap.map(m => ({ map: m.map, ten: tenBanDo(m.map) ?? coDau(m.ten), vung: banDo.find(b => b.id === m.map)?.vung ?? '', soHo: m.ho.size, hoLon: [...m.ho.values()].filter(v => v === 'ho_lon').length, diem: m.diem })),
  },
  congThuc,
}

// --- cốt truyện & tóm tắt nhiệm vụ (web/nhiem_vu_tom_tat.json)
const nvTT = JSON.parse(doc('du_lieu/web/nhiem_vu_tom_tat.json'))
const cotTruyen = {
  tong: nvTT.tong,
  theoLoai: nvTT.theo_loai,
  chuong: nvTT.cot_truyen.map(c => ({ so: c.chuong, nam: c.nam, vung: c.vung, cung: c.cung, diaDanh: c.dia_danh, cap: c.cap, tomTat: c.tom_tat, buoc: c.buoc, huyHieu: c.huy_hieu })),
  chuoiPhu: nvTT.chuoi_phu.map(c => ({ noi: c.noi, ten: c.chuoi, nhanVat: c.nhan_vat, capTu: c.cap_tu, capDen: c.cap_den, soBuoc: c.so_buoc })),
  ngay: nvTT.bang_ngay,
  tuan: nvTT.tuan,
  huyenThoai: nvTT.huyen_thoai,
  an: { soLuong: nvTT.an?.so_luong ?? 0, goiY: nvTT.an?.goi_y ?? '' },
}
if (cotTruyen.tong !== nv.length) console.warn(`Cảnh báo: nhiem_vu_tom_tat.json có ${cotTruyen.tong} nhiệm vụ, nhiem_vu_moi.csv có ${nv.length}.`)

// Shiny khi hạ quái (RoiLauDai.shiny: 1/SHINY_MAU, chắc chắn ở lần SHINY_PITY) — chỉ ghi Pokédex/danh hiệu.
const shiny = { mauSo: hangJ(cuonHut, 'SHINY_MAU', 'CuonHut.java'), pity: hangJ(cuonHut, 'SHINY_PITY', 'CuonHut.java') }

const noiDung2 = { shiny, vatPham: vatPhamWeb, cuaHang, gym, gymLuat, cheTao, nhanhEevee, trangTri, suKienVatPham, bossTheGioi, pvp, soTay, hamNguc, heSinhThai, cotTruyen }
const OUT_ND = join(WEB, 'shared/data/cam-nang-noi-dung.json')
const nd2 = `${JSON.stringify(noiDung2, null, 1)}\n`
if (!existsSync(OUT_ND) || readFileSync(OUT_ND, 'utf8') !== nd2) writeFileSync(OUT_ND, nd2)
console.log(`cam-nang-noi-dung.json: ${vatPhamWeb.length} vật phẩm, ${cuaHang.reduce((s, c) => s + c.hang.length, 0)} món ở ${cuaHang.length} cửa hàng, ${gym.length} Gym, ${cheTao.length} công thức chế tạo, ${congThuc.length} công thức sinh thái, ${diemCau.length} điểm câu cá / ${cauCaMap.length} bản đồ, ${cotTruyen.chuong.length} chương cốt truyện, ${cotTruyen.chuoiPhu.length} chuỗi phụ.`)

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
