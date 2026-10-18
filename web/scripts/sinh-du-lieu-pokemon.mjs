#!/usr/bin/env node
/**
 * Sinh dữ liệu Pokédex cho website từ repo game HTBB (nhánh pokemon/chuyen-doi).
 *
 *   HTBB_DIR=../HTBB node scripts/sinh-du-lieu-pokemon.mjs
 *
 * Đọc (chỉ đọc, không sửa gì bên HTBB):
 *   docs/pokemon/bang_quai.csv, bang_chieu.csv, bang_thu.csv, bang_npc.csv,
 *   bang_npc_su_kien.csv, bang_pet_cu.csv, kiem_ke_sprite.csv
 *   src/com/htbb/pokemon/{HePokemon,LoaiPokemon,DaTienHoa,Pokedex,TenMapPokemon,ChuPokemon}.java
 *   src/real/Map.java (bảng tên map), ClientUnity/.../Tilemap.cs (tên map client)
 *   sql/htbb_day_du.sql (data_monster: cấp, HP, exp; data_potion: tên thuốc thưởng)
 *   map/map<N> (lưới quái: 2 byte rộng/cao + rộng×cao byte id template; 0/255 = trống)
 *   ClientUnity/Assets/Resources/poke/<dddd>[s]_cd.bytes (chân dung PNG 40×40)
 *
 * Ghi:
 *   shared/data/pokedex.json      (sắp xếp cố định, không có dấu thời gian -> chạy lại ra y hệt)
 *   public/pokemon/<dex>[s].png   (chỉ ghi khi nội dung khác)
 *
 * Không thêm thư viện: chỉ dùng node:fs / node:path (Node 22).
 */
import { existsSync, mkdirSync, readFileSync, readdirSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const WEB = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const HTBB = resolve(WEB, process.env.HTBB_DIR ?? '../HTBB')
const OUT_JSON = join(WEB, 'shared/data/pokedex.json')
const OUT_ANH = join(WEB, 'public/pokemon')

if (!existsSync(join(HTBB, 'docs/pokemon/bang_quai.csv'))) {
  console.error(`Không thấy repo HTBB ở ${HTBB} (đặt HTBB_DIR).`)
  process.exit(1)
}

const doc = p => readFileSync(join(HTBB, p), 'utf8')

// ---------------------------------------------------------------- CSV / Java helpers

/** CSV đơn giản có hỗ trợ "..." (bang_npc có dấu phẩy trong ngoặc kép). */
function docCsv(p) {
  const dong = doc(p).replace(/\r/g, '').split('\n').filter(Boolean)
  const tach = (l) => {
    const ra = []
    let o = ''
    let trongNgoac = false
    for (let i = 0; i < l.length; i++) {
      const c = l[i]
      if (c === '"') {
        if (trongNgoac && l[i + 1] === '"') { o += '"'; i++ }
        else trongNgoac = !trongNgoac
      }
      else if (c === ',' && !trongNgoac) { ra.push(o); o = '' }
      else o += c
    }
    ra.push(o)
    return ra
  }
  const dau = tach(dong[0])
  return dong.slice(1).map((l) => {
    const cot = tach(l)
    return Object.fromEntries(dau.map((d, i) => [d, cot[i] ?? '']))
  })
}

/** Lấy khối `{ ... }` ngay sau `marker` (đếm ngoặc). */
function khoi(src, marker) {
  const i = src.indexOf(marker)
  if (i < 0) throw new Error(`Không thấy "${marker}"`)
  const a = src.indexOf('{', i + marker.length - 1)
  let sau = 0
  for (let j = a; j < src.length; j++) {
    if (src[j] === '{') sau++
    else if (src[j] === '}' && --sau === 0) return src.slice(a + 1, j)
  }
  throw new Error(`Khối "${marker}" không đóng`)
}
const chuoi = s => [...s.matchAll(/"((?:[^"\\]|\\.)*)"/g)].map(m => m[1])
const so = s => [...s.matchAll(/-?\d+/g)].map(m => Number(m[0]))
/** Các dòng con `{...}` của một mảng 2 chiều. */
const dongCon = s => [...s.matchAll(/\{([^{}]*)\}/g)].map(m => m[1])

// ---------------------------------------------------------------- Hệ + bảng khắc

const heJava = doc('src/com/htbb/pokemon/HePokemon.java')
const TEN_HE = chuoi(khoi(heJava, 'String[] TEN ='))
const TEN_HE_EN = chuoi(khoi(heJava, 'String[] TEN_EN ='))
const HANG_HE = Object.fromEntries([...heJava.matchAll(/static final byte (\w+) = (-?\d+);/g)].map(m => [m[1], Number(m[2])]))
const SO_HE = TEN_HE.length
if (SO_HE !== 18) throw new Error(`Cần 18 hệ, thấy ${SO_HE}`)
const KHAC = Array.from({ length: SO_HE }, () => Array(SO_HE).fill(100))
for (const m of heJava.matchAll(/\bdat\((\w+), (\d+), ([\w, ]+)\);/g)) {
  const ck = HANG_HE[m[1]]
  for (const p of m[3].split(',').map(s => s.trim())) KHAC[ck][HANG_HE[p]] = Number(m[2])
}
const LAM_DIU = {}
for (const m of khoi(heJava, 'int heSoTrongGame(').matchAll(/case (\d+):\s*return (\d+);/g)) LAM_DIU[m[1]] = Number(m[2])
LAM_DIU['100'] = Number(/default:\s*return (\d+);/.exec(khoi(heJava, 'int heSoTrongGame('))[1])

// ---------------------------------------------------------------- Người chơi (5 dòng)

const loaiJava = doc('src/com/htbb/pokemon/LoaiPokemon.java')
const HE_K = { K: -1 }
const giaTri = (t) => {
  t = t.trim()
  if (/^-?\d+$/.test(t)) return Number(t)
  if (t === 'K') return -1
  if (t === 'KHONG') return -1
  const m = /HePokemon\.(\w+)/.exec(t)
  if (m) return HANG_HE[m[1]]
  if (t in HE_K) return HE_K[t]
  throw new Error(`Không hiểu giá trị "${t}"`)
}
const DONG_NGUOI_CHOI = dongCon(khoi(loaiJava, 'int[][] DU_LIEU =')).map(r => r.split(',').map(giaTri))
  .map(([dex, he1, he2, cap, sau]) => ({ dex, he: [he1, he2].filter(h => h >= 0), capTienHoa: cap, sau: sau > 0 ? sau : 0 }))
const TEN_NGUOI_CHOI = Object.fromEntries(dongCon(khoi(loaiJava, 'String[][] TEN =')).map(r => chuoi(r)).map(([d, t]) => [Number(d), t]))
const KHOI_DAU_THEO_CLASS = so(khoi(loaiJava, 'int[] KHOI_DAU_THEO_CLASS ='))
const NHANH_EEVEE = so(khoi(loaiJava, 'int[] NHANH_EEVEE ='))
const CAP_NHANH_EEVEE = Number(/CAP_NHANH_EEVEE = (\d+)/.exec(loaiJava)[1])
const PT_GD = {
  2: Number(/PT_GIAI_DOAN_2 = (\d+)/.exec(loaiJava)[1]),
  3: Number(/PT_GIAI_DOAN_3 = (\d+)/.exec(loaiJava)[1]),
  eevee: Number(/PT_NHANH_EEVEE = (\d+)/.exec(loaiJava)[1]),
}

// ---------------------------------------------------------------- Đá tiến hoá, Pokédex mốc

const daJava = doc('src/com/htbb/pokemon/DaTienHoa.java')
const DA_ID = so(khoi(daJava, 'int[] ID_ITEM ='))
const DA_DEX = so(khoi(daJava, 'int[] DEX ='))
const DA_TEN = chuoi(khoi(daJava, 'String[] TEN ='))
const TI_LE_ROI = Number(/TI_LE_ROI_PHAN_NGHIN = (\d+)/.exec(daJava)[1])
const LECH_CAP = Number(/LECH_CAP_TOI_DA = (\d+)/.exec(daJava)[1])

const sql = doc('sql/htbb_day_du.sql')
/** Các bộ giá trị của `INSERT INTO \`bang\` VALUES (...),(...);` (chuỗi '...' có \'). */
function sqlDong(bang) {
  const m = new RegExp('INSERT INTO `' + bang + '` VALUES (.*);').exec(sql)
  if (!m) throw new Error(`SQL thiếu bảng ${bang}`)
  const s = m[1]
  const ra = []
  let dong = null
  let o = ''
  let trongChuoi = false
  for (let i = 0; i < s.length; i++) {
    const c = s[i]
    if (trongChuoi) {
      if (c === '\\') { o += s[++i] === 'n' ? '\n' : s[i]; continue }
      if (c === '\'') { trongChuoi = false; continue }
      o += c
      continue
    }
    if (c === '\'') trongChuoi = true
    else if (c === '(') { dong = []; o = '' }
    else if (c === ',' && dong) { dong.push(o); o = '' }
    else if (c === ')') { dong.push(o); ra.push(dong); dong = null; o = '' }
    else if (dong) o += c
  }
  return ra
}
const THUOC = Object.fromEntries(sqlDong('data_potion').map(r => [Number(r[0]), r[4].split(/[:.\n]/)[0].trim()]))

const pokedexJava = doc('src/com/htbb/pokemon/Pokedex.java')
const MOC = so(khoi(pokedexJava, 'int[] MOC ='))
const MOC_THUONG = MOC.map((moc) => {
  const m = new RegExp(`case ${moc}:\\s*return ([^;]+);`).exec(pokedexJava)
  const chuoiThuong = m[1].replace(/"\s*\+\s*da\s*\+\s*"/g, '{DA}').replace(/"/g, '')
  const thuong = chuoiThuong.split(',').map((p) => {
    const [loai, id, sl] = p.split(':')
    if (loai === 'PT') return { ten: THUOC[Number(id)] ?? `Thuốc #${id}`, soLuong: Number(sl) }
    if (id === '{DA}') return { ten: '1 đá tiến hoá Eevee ngẫu nhiên', soLuong: Number(sl) }
    return { ten: `Vật phẩm #${id}`, soLuong: Number(sl) }
  })
  return { moc, thuong }
})

// ---------------------------------------------------------------- Chiêu (bang_chieu.csv)

const CHIEU = docCsv('docs/pokemon/bang_chieu.csv')
const chieuTheoClass = {}
for (const r of CHIEU) {
  (chieuTheoClass[r.class] ??= []).push({ so: Number(r.so_hieu_skill), ten: r.ten_moi, he: Number(r.so_he) })
}

// ---------------------------------------------------------------- Tên map

const tenMapJava = doc('src/com/htbb/pokemon/TenMapPokemon.java')
const BANG_TEN_MAP = dongCon(khoi(tenMapJava, 'String[][] BANG =')).map(r => chuoi(r))
const HUONG = chuoi(khoi(tenMapJava, 'String[] HUONG ='))
const lower = s => s.toLocaleLowerCase('vi')
function doiTenMap(ten) {
  if (!ten) return ten
  const tra = t => BANG_TEN_MAP.find(([cu]) => lower(cu) === lower(t))?.[1]
  const nguyen = tra(ten.trim())
  if (nguyen) return nguyen
  for (const h of HUONG) {
    if (lower(ten).startsWith(lower(h))) {
      const sau = tra(ten.slice(h.length).trim())
      if (sau) return h + sau
    }
  }
  return ten
}
const mapJava = doc('src/real/Map.java')
const NAM_MAP = dongCon(khoi(mapJava, 'String[][] namMap =')).map(r => chuoi(r)[0])
const MAP_ID = dongCon(khoi(mapJava, 'short[][] mapID =')).map(r => so(r)[0])
const ID_ARR_MAP = so(khoi(mapJava, 'short[] idArrMap ='))
const tenMapServer = {}
MAP_ID.forEach((id, i) => {
  const t = NAM_MAP[ID_ARR_MAP[i]]
  if (t) tenMapServer[id] = t
})
const tenMapClient = {}
const tilemap = join(HTBB, 'ClientUnity/Assets/Scripts/Game/Tilemap.cs')
if (existsSync(tilemap)) {
  const src = khoi(readFileSync(tilemap, 'utf8'), 'void setNameMap(')
  for (const m of src.matchAll(/case (\d+):\s*mapName = "([^"]*)";/g)) tenMapClient[m[1]] = m[2]
}
const vietHoa = s => s.charAt(0).toLocaleUpperCase('vi') + s.slice(1)
/** Map không có trong hai bảng trên (tên lấy từ chú thích RealController.java). */
const TEN_MAP_THEM = { 28: 'Cánh Rừng Đông' }
function tenMap(id) {
  const cu = tenMapClient[id] || tenMapServer[id] || TEN_MAP_THEM[id]
  return cu ? vietHoa(doiTenMap(cu)) : `Khu vực ${id}`
}

// ---------------------------------------------------------------- Lưới quái trên map

/** Map có lớp riêng tự đọc lưới theo kiểu khác (vị trí, không phải quái cố định). */
const MAP_BO_QUA = new Set([40, 42])
const SPAWN = {} // idTemplate -> {mapId: soLuong}
const MAP_CO_QUAI = {}
for (const f of readdirSync(join(HTBB, 'map')).filter(f => /^map\d+$/.test(f))) {
  const id = Number(f.slice(3))
  if (MAP_BO_QUA.has(id)) continue
  const b = readFileSync(join(HTBB, 'map', f))
  const w = b[0]
  const h = b[1]
  for (let i = 0; i < w * h && 2 + i < b.length; i++) {
    let v = b[2 + i]
    if (v === 0 || v === 255) continue
    if (id === 17 && v > 81) v += 3 // MapNapTaiNguyen.loadMap
    ;(SPAWN[v] ??= {})[id] = (SPAWN[v][id] ?? 0) + 1
    ;(MAP_CO_QUAI[id] ??= new Set()).add(v)
  }
}
/** Boss không nằm trong lưới: nơi xuất hiện theo tên hang (TenMapPokemon) hoặc sự kiện. */
const NOI_BOSS = {
  39: 'Hang Thằn Lằn', 82: 'Hang Đại Bàng', 116: 'Hang Dracula', 117: 'Hang Dracula',
  91: 'Hang Mãng Xà', 92: 'Hang Mãng Xà',
}

// ---------------------------------------------------------------- Template quái (CSV + SQL)

const QUAI_CSV = docCsv('docs/pokemon/bang_quai.csv')
const QUAI_SQL = Object.fromEntries(sqlDong('data_monster').map(r => [Number(r[0]), {
  ten: r[1], he: Number(r[3]), cap: Number(r[4]), hp: Number(r[5]), exp: Number(r[10]), dex: Number(r[24]), shiny: Number(r[25]), he2: Number(r[26]),
}]))

// ---------------------------------------------------------------- Các bảng phụ

const THU = docCsv('docs/pokemon/bang_thu.csv')
const NPC = docCsv('docs/pokemon/bang_npc.csv')
const NPC_SK = docCsv('docs/pokemon/bang_npc_su_kien.csv')
const PET_CU = docCsv('docs/pokemon/bang_pet_cu.csv')
const KIEM_KE = docCsv('docs/pokemon/kiem_ke_sprite.csv')
const chuJava = doc('src/com/htbb/pokemon/ChuPokemon.java')
const TEN_NPC = chuoi(khoi(chuJava, 'String[] TEN_NPC ='))
const TEN_VUNG = chuoi(khoi(chuJava, 'String[] TEN_VUNG ='))
const TEN_DONG = chuoi(khoi(chuJava, 'String[] TEN_DONG ='))
const HUONG_DAN_GAME = chuoi(khoi(chuJava, 'String[] HUONG_DAN ='))

/**
 * Hệ chuẩn (Gen 6+) cho loài KHÔNG có hệ trong dữ liệu game (thú cưỡi, pet, NPC):
 * game chỉ lưu hệ cho quái (data_monster.he/he2) và 5 dòng người chơi (LoaiPokemon).
 * Thiếu loài nào ở đây script sẽ báo lỗi.
 */
const HE_CHUAN = {
  18: [0, 9], 29: [7], 35: [17], 36: [17], 41: [7, 9], 42: [7, 9], 52: [0], 53: [0], 68: [6], 100: [3],
  113: [0], 115: [0], 120: [2], 121: [2, 10], 129: [2], 131: [2, 5], 143: [0], 144: [5, 9], 146: [1, 9],
  147: [14], 148: [14], 149: [14, 9], 163: [0, 9], 164: [0, 9], 165: [11, 9], 169: [7, 9], 173: [17],
  178: [10, 9], 179: [3], 190: [0], 221: [5, 8], 225: [5, 9], 227: [16, 9], 234: [0], 245: [2], 250: [1, 9],
  262: [15], 270: [2, 4], 276: [0, 9], 282: [10, 17], 302: [15, 13], 330: [8, 14], 337: [12, 10], 352: [0],
  357: [4, 9], 370: [2], 385: [16, 10], 393: [2], 399: [0], 403: [3], 417: [3], 448: [6, 16], 473: [5, 8],
  585: [0, 4], 615: [5], 627: [0, 9], 628: [0, 9], 831: [0], 896: [5], 897: [13],
}

/** Họ tiến hoá chuẩn, chỉ gồm loài có trong game (dòng người chơi lấy từ LoaiPokemon). */
const HO_CHUAN = [
  [10, 12], [194, 195], [255, 256, 257], [23, 24], [43, 44, 45], [46, 47], [543, 544, 545], [92, 93, 94],
  [104, 105], [58, 59], [159, 160], [453, 454], [401, 402], [559, 560], [436, 437], [129, 130],
  [355, 356, 477], [403, 404, 405], [536, 537], [496, 497], [77, 78], [217, 901], [163, 164],
  [147, 148, 149], [173, 35, 36], [41, 42, 169], [627, 628], [120, 121], [221, 473], [52, 53],
]

// ---------------------------------------------------------------- Gom theo loài

/** @type {Map<number, any>} */
const LOAI = new Map()
const loai = (dex) => {
  if (!LOAI.has(dex)) {
    LOAI.set(dex, {
      dex, ten: '', he: null, vaiTro: new Set(), quai: [], thuCuoi: [], pet: [], npc: [], chieu: null, dong: null,
    })
  }
  return LOAI.get(dex)
}
const datTen = (dex, ten, uuTien) => {
  const l = loai(dex)
  if (!ten) return
  if (!l.ten || uuTien > (l._uuTienTen ?? -1)) { l.ten = ten; l._uuTienTen = uuTien }
}
const lamSachTen = t => t.replace(/^Trứng /, '').replace(/ cấp \d+$/, '').replace(/ Shiny$/, '').replace(/ (lửa|vàng)$/, '').trim()

// Dòng người chơi
const CLASS_THEO_DEX = {}
KHOI_DAU_THEO_CLASS.forEach((dexDau, cls) => {
  let d = dexDau
  const chuoiDex = []
  while (d > 0 && chuoiDex.length < 5) {
    chuoiDex.push(d)
    d = DONG_NGUOI_CHOI.find(x => x.dex === d)?.sau ?? 0
  }
  if (dexDau === 133) chuoiDex.push(...NHANH_EEVEE)
  for (const x of chuoiDex) CLASS_THEO_DEX[x] = cls
})
for (const d of DONG_NGUOI_CHOI) {
  const l = loai(d.dex)
  datTen(d.dex, TEN_NGUOI_CHOI[d.dex], 10)
  l.he = d.he
  l.vaiTro.add('khoi-dau')
  const cls = CLASS_THEO_DEX[d.dex]
  l.dong = { class: cls, ten: TEN_DONG[cls] ?? '', dexDau: KHOI_DAU_THEO_CLASS[cls] }
  l.chieu = (chieuTheoClass[cls] ?? []).map(c => ({ ...c }))
}

// Quái / boss / thú cưỡi (template)
for (const r of QUAI_CSV) {
  if (!r.dex) continue
  const id = Number(r.id_quai)
  const s = QUAI_SQL[id]
  const dex = Number(r.dex)
  const l = loai(dex)
  // Quái năm 2 (id 139+, tools/pokemon/sinh_noi_dung.py) có thể chưa nằm trong dump: lấy tên từ ghi_chu "... -> Tên".
  datTen(dex, (s?.ten ?? r.ghi_chu.split('->').pop().trim()).replace(/ Shiny$/, ''), 5)
  if (!l.he) l.he = [Number(r.he1), Number(r.he2)].filter(h => h >= 0)
  const phanLoai = r.phan_loai
  const shiny = Number(r.shiny) === 1
  if (phanLoai === 'thu_cuoi') {
    const ten = r.ghi_chu.split('->')[0].trim()
    const kieu = /quốc gia/.test(ten) ? 'Thú cưỡi vùng' : /bang/.test(ten) ? 'Thú cưỡi đội' : 'Thú cưỡi cá nhân'
    const mau = (/(trắng|xanh lá|xanh|đỏ|vàng|tím)$/.exec(ten)?.[1]) ?? ''
    l.thuCuoi.push({ ten: `${kieu}${mau ? ` (${mau})` : ''}`, shiny, nguon: 'template', id })
    l.vaiTro.add('thu-cuoi')
    continue
  }
  const noi = Object.entries(SPAWN[id] ?? {}).map(([m, n]) => ({ map: Number(m), ten: tenMap(Number(m)), soLuong: n }))
    .sort((a, b) => b.soLuong - a.soLuong || a.map - b.map)
  const ghiChuNoi = phanLoai === 'boss' && !noi.length ? (NOI_BOSS[id] ? doiTenMap(NOI_BOSS[id]) : 'Sự kiện / boss thế giới') : ''
  l.quai.push({
    id, cap: s?.cap ?? Number(r.cap), hp: s?.hp ?? Number(r.maxhp), exp: s?.exp ?? 0, shiny, boss: phanLoai === 'boss', noi, ghiChuNoi,
  })
  l.vaiTro.add(phanLoai === 'boss' ? 'boss' : 'quai')
}

// Thú cưỡi (linh thú) + pet theo item
for (const r of THU) {
  const dex = Number(r.dex)
  const l = loai(dex)
  datTen(dex, lamSachTen(r.ten_moi), 2)
  const shiny = Number(r.shiny) === 1
  if (r.loai === 'thu_cuoi') {
    l.thuCuoi.push({ ten: 'Thú cưỡi', shiny, nguon: 'linh-thu', id: Number(r.id) })
    l.vaiTro.add('thu-cuoi')
  }
  else {
    const cap = /cấp (\d+)$/.exec(r.ten_moi)?.[1]
    l.pet.push({ idItem: Number(r.id), ten: r.ten_moi, cap: cap ? Number(cap) : 0, trung: r.ten_moi.startsWith('Trứng '), shiny })
    l.vaiTro.add('pet')
  }
}
for (const r of PET_CU) {
  const dex = Number(r.dex)
  datTen(dex, r.ten_moi, 3)
  loai(dex).pet.push({ idItem: 0, ten: `${r.ten_moi} đi theo`, cap: 0, trung: false, shiny: Number(r.shiny) === 1 })
  loai(dex).vaiTro.add('pet')
}

// NPC
for (const r of NPC) {
  const dex = Number(r.dex)
  datTen(dex, r.pokemon, 4)
  const ten = TEN_NPC[Number(r.type)] ?? r.pokemon
  const l = loai(dex)
  if (!l.npc.some(n => n.ten === ten)) l.npc.push({ ten, suKien: false })
  l.vaiTro.add('npc')
}
for (const r of NPC_SK) {
  if (!r.dex) continue
  const dex = Number(r.dex)
  datTen(dex, r.ten_moi, 4)
  const l = loai(dex)
  if (!l.npc.some(n => n.suKien)) l.npc.push({ ten: `${r.ten_moi} (NPC sự kiện)`, suKien: true })
  l.vaiTro.add('npc')
}

// Hệ còn thiếu
for (const l of LOAI.values()) {
  if (!l.he || !l.he.length) {
    if (!HE_CHUAN[l.dex]) throw new Error(`Loài #${l.dex} ${l.ten} chưa có hệ (thêm vào HE_CHUAN)`)
    l.he = HE_CHUAN[l.dex]
  }
  if (!l.ten) throw new Error(`Loài #${l.dex} chưa có tên`)
}

// ---------------------------------------------------------------- Chân dung

const THU_MUC_ANH = join(HTBB, 'ClientUnity/Assets/Resources/poke')
const anhCo = new Set(readdirSync(THU_MUC_ANH).filter(f => /^\d{4}s?_cd\.bytes$/.test(f)).map(f => f.slice(0, -'_cd.bytes'.length)))
mkdirSync(OUT_ANH, { recursive: true })
let soAnhGhi = 0
let soAnh = 0
const anhCua = (dex, shiny) => {
  const ma = String(dex).padStart(4, '0') + (shiny ? 's' : '')
  if (!anhCo.has(ma)) return null
  const key = `${dex}${shiny ? 's' : ''}`
  const dich = join(OUT_ANH, `${key}.png`)
  const nd = readFileSync(join(THU_MUC_ANH, `${ma}_cd.bytes`))
  if (nd.subarray(1, 4).toString() !== 'PNG') throw new Error(`${ma}_cd.bytes không phải PNG`)
  soAnh++
  if (!existsSync(dich) || !readFileSync(dich).equals(nd)) { writeFileSync(dich, nd); soAnhGhi++ }
  return `/pokemon/${key}.png`
}

// ---------------------------------------------------------------- Tiến hoá

function tienHoa(dex) {
  // Dòng người chơi
  const dongNC = DONG_NGUOI_CHOI.find(d => d.dex === dex)
  if (dongNC) {
    const cls = CLASS_THEO_DEX[dex]
    const dau = KHOI_DAU_THEO_CLASS[cls]
    if (dau === 133) {
      return {
        kieu: 'eevee',
        buoc: [{ dex: 133, dieuKien: 'Khởi đầu' }],
        nhanh: NHANH_EEVEE.map((d) => {
          const i = DA_DEX.indexOf(d)
          return { dex: d, dieuKien: `${DA_TEN[i]} (từ cấp ${CAP_NHANH_EEVEE})` }
        }),
      }
    }
    const buoc = []
    let d = dau
    let dk = 'Khởi đầu'
    while (d > 0) {
      buoc.push({ dex: d, dieuKien: dk })
      const x = DONG_NGUOI_CHOI.find(y => y.dex === d)
      if (!x || !x.sau) break
      dk = `Cấp ${x.capTienHoa}`
      d = x.sau
    }
    return { kieu: 'cap', buoc, nhanh: [] }
  }
  const ho = HO_CHUAN.find(h => h.includes(dex))
  if (!ho) return null
  // Pet: tiến hoá theo cấp pet (mỗi cấp là một item khác)
  const capPet = (d) => {
    const ds = LOAI.get(d)?.pet.filter(p => p.cap > 0).map(p => p.cap) ?? []
    return ds.length ? Math.min(...ds) : 0
  }
  const laPet = ho.every(d => capPet(d) > 0)
  return {
    kieu: laPet ? 'pet' : 'ho',
    buoc: ho.map((d, i) => ({ dex: d, dieuKien: i === 0 ? (laPet ? 'Trứng / cấp 1' : 'Dạng đầu') : (laPet ? `Pet cấp ${capPet(d)}` : 'Dạng sau') })),
    nhanh: [],
  }
}

// ---------------------------------------------------------------- Xuất

const THU_TU_VAI_TRO = ['khoi-dau', 'quai', 'boss', 'thu-cuoi', 'pet', 'npc']
const dsLoai = [...LOAI.values()].sort((a, b) => a.dex - b.dex).map((l) => {
  const anh = anhCua(l.dex, false)
  const anhShiny = anhCua(l.dex, true)
  const th = tienHoa(l.dex)
  return {
    dex: l.dex,
    ten: l.ten,
    he: l.he,
    vaiTro: THU_TU_VAI_TRO.filter(v => l.vaiTro.has(v)),
    anh,
    anhShiny,
    giaiDoan: DONG_NGUOI_CHOI.some(d => d.dex === l.dex)
      ? (NHANH_EEVEE.includes(l.dex) ? 2 : (th?.buoc.findIndex(b => b.dex === l.dex) ?? 0) + 1)
      : 0,
    dong: l.dong,
    tienHoa: th,
    chieu: l.chieu,
    quai: l.quai.sort((a, b) => a.cap - b.cap || a.id - b.id),
    thuCuoi: l.thuCuoi.sort((a, b) => a.id - b.id),
    pet: l.pet.sort((a, b) => a.idItem - b.idItem || a.ten.localeCompare(b.ten)),
    npc: l.npc,
  }
})

// Loài hiện trên map (để trang hướng dẫn liệt kê theo khu vực)
const dsMap = Object.keys(MAP_CO_QUAI).map(Number).sort((a, b) => a - b).map((id) => {
  const loaiTrenMap = [...new Set([...MAP_CO_QUAI[id]].map(t => Number(QUAI_SQL[t]?.dex ?? 0)).filter(Boolean))]
  const cap = [...MAP_CO_QUAI[id]].map(t => QUAI_SQL[t]?.cap ?? 0).filter(Boolean)
  return { id, ten: tenMap(id), dex: loaiTrenMap.sort((a, b) => a - b), capThap: Math.min(...cap), capCao: Math.max(...cap) }
}).filter(m => m.dex.length)

const kiemKeDex = new Set(KIEM_KE.map(r => Number(r.dex)))
const thieu = [...kiemKeDex].filter(d => !LOAI.has(d))
if (thieu.length) console.warn(`Cảnh báo: dex có sprite nhưng không có vai trò: ${thieu.join(', ')}`)

const du = {
  he: TEN_HE.map((ten, id) => ({ id, ten, en: TEN_HE_EN[id] })),
  khacChinhXac: KHAC,
  lamDiu: LAM_DIU,
  stabPhanTram: Number(/PT_STAB = (\d+)/.exec(doc('src/com/htbb/pokemon/ChieuPokemon.java'))[1]),
  khoiDau: KHOI_DAU_THEO_CLASS.map((dex, cls) => ({ class: cls, dex, dong: TEN_DONG[cls] ?? '' })),
  capNhanhEevee: CAP_NHANH_EEVEE,
  phanTramGiaiDoan: PT_GD,
  daTienHoa: DA_ID.map((idItem, i) => ({ idItem, ten: DA_TEN[i], dex: DA_DEX[i] })),
  tiLeRoiDaPhanNghin: TI_LE_ROI,
  lechCapRoiDa: LECH_CAP,
  mocPokedex: MOC_THUONG,
  vung: TEN_VUNG,
  huongDanGame: HUONG_DAN_GAME,
  map: dsMap,
  loai: dsLoai,
}

mkdirSync(dirname(OUT_JSON), { recursive: true })
const json = JSON.stringify(du, null, 1) + '\n'
const cu = existsSync(OUT_JSON) ? readFileSync(OUT_JSON, 'utf8') : ''
if (cu !== json) writeFileSync(OUT_JSON, json)

const dem = v => dsLoai.filter(l => l.vaiTro.includes(v)).length
console.log(`Loài: ${dsLoai.length} (khởi đầu ${dem('khoi-dau')}, quái ${dem('quai')}, boss ${dem('boss')}, thú cưỡi ${dem('thu-cuoi')}, pet ${dem('pet')}, NPC ${dem('npc')})`)
console.log(`Template quái: ${dsLoai.reduce((n, l) => n + l.quai.length, 0)}, map có quái: ${dsMap.length}`)
console.log(`Chân dung: ${soAnh} (ghi mới/đổi ${soAnhGhi}); JSON ${cu === json ? 'không đổi' : 'đã ghi'} (${(json.length / 1024).toFixed(1)} KB)`)
