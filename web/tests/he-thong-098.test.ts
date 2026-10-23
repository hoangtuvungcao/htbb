/**
 * Số liệu trang cẩm nang 0.9.8 (shared/utils/heThong098.ts) == CSDL game (sql/htbb.sql, phần A): bảng data_* và giá trị
 * mặc định của danh mục cấu hình. Đổi số trong game mà quên sửa trang -> test hỏng. Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync, existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { docBangSql } from '../shared/utils/sqlDump.ts'
import { phanDayDu } from './htbbSql.ts'
import {
  BAC_LOAI_BAT, BAT, BONG, CHAT, HOANG_DA, CHO_POKEMON, CHUYEN_DOI, DANH_HIEU, KET_DOI, NGAM_TAY, NGAY_CONG_DONG, PHO_BAN, TAN_THU,
  THOI_TIET, TRAO_DOI,
} from '../shared/utils/heThong098.ts'
import { DS_CAM_NANG } from '../shared/utils/danhMucCamNang.ts'
import { ANH_GAME, ANH_TAM } from '../shared/utils/anhGame.ts'

const goc = (p: string) => fileURLToPath(new URL(p, import.meta.url))
const SQL = phanDayDu()
const bang = (t: string) => docBangSql(SQL, t)
const cauHinh = new Map(bang('cau_hinh_danh_muc').map(r => [String(r.khoa), String(r.mac_dinh)]))
const ch = (k: string) => Number(cauHinh.get(k))

test('Poké Ball / Great / Ultra == data_bat_bong', () => {
  const b = bang('data_bat_bong').sort((x, y) => Number(x.thu_tu) - Number(y.thu_tu))
  assert.deepEqual(b.map(r => ({ ma: r.ma, ten: r.ten, heSo: r.he_so_phan_tram, tran: r.tran_phan_nghin, giaPhanNghinGio: r.gia_phan_nghin_gio, capMo: r.cap_mo })),
    BONG.map(x => ({ ...x })))
})

test('bậc loài: tên + tỉ lệ gốc phổ biến nhất == data_bat_loai', () => {
  const loai = bang('data_bat_loai').filter(r => Number(r.bat_duoc) === 1 && Number(r.ti_le_phan_nghin) > 0)
  for (const b of BAC_LOAI_BAT) {
    const dem = new Map<number, number>()
    for (const r of loai.filter(r => Number(r.do_hiem) === b.bac)) dem.set(Number(r.ti_le_phan_nghin), (dem.get(Number(r.ti_le_phan_nghin)) ?? 0) + 1)
    const pho = [...dem.entries()].sort((x, y) => y[1] - x[1])[0]![0]
    assert.equal(pho, b.tiLeGoc, b.ten)
  }
})

test('chuyển đổi con trùng == data_bat_chuyen_doi', () => {
  assert.deepEqual(bang('data_bat_chuyen_doi').map(r => ({ bac: r.do_hiem, keo: r.keo, xuPhanNghinGio: r.xu_phan_nghin_gio, luongKhoa: r.luong_khoa })),
    CHUYEN_DOI.map(x => ({ ...x })))
})

test('thu phục / trao đổi / chợ Pokémon / phó bản / chat / ngắm tay == cau_hinh_danh_muc', () => {
  const ds: [string, number][] = [
    ['bat.nguong_hp_phan_tram', BAT.nguongHpPhanTram], ['bat.tran_ngay', BAT.tranNgay], ['bat.tran_nem_ngay', BAT.tranNemNgay],
    ['bat.cach_ms', BAT.cachMs], ['bat.so_lan_bo_chay', BAT.soLanBoChay], ['bat.giu_moi_loai', BAT.giuMoiLoai],
    ['bat.chuyen_doi.tran_xu_phan_nghin_gio', BAT.tranXuChuyenDoiPhanNghinGio], ['bat.chuyen_doi.tran_luong_ngay', BAT.tranLuongChuyenDoiNgay],
    ['trao_doi.cap_toi_thieu', TRAO_DOI.cap], ['trao_doi.phi_phan_nghin_gio', TRAO_DOI.phiPhanNghinGio],
    ['trao_doi.phi_tran_phan_nghin_gio', TRAO_DOI.phiTranPhanNghinGio], ['trao_doi.khoa_gio', TRAO_DOI.khoaGio],
    ['trao_doi.tran_ngay', TRAO_DOI.tranNgay], ['trao_doi.cach_giay', TRAO_DOI.cachGiay],
    ['cho_pokemon.cap_toi_thieu', CHO_POKEMON.cap], ['cho_pokemon.han_gio', CHO_POKEMON.hanGio], ['cho_pokemon.thue_phan_tram', CHO_POKEMON.thuePhanTram],
    ['cho_pokemon.phi_dang_phan_tram', CHO_POKEMON.phiDangPhanTram], ['cho_pokemon.he_so_3_sao', CHO_POKEMON.heSo3Sao],
    ['cho_pokemon.he_so_sac_mau', CHO_POKEMON.heSoSacMau], ['cho_pokemon.khoa_gio', CHO_POKEMON.khoaGio], ['cho_pokemon.mua_ngay', CHO_POKEMON.muaNgay],
    ['cho_pokemon.toi_da_dang', CHO_POKEMON.toiDaDang],
    ['pho_ban.cap_toi_thieu', PHO_BAN.cap], ['pho_ban.luot_ngay', PHO_BAN.luotNgay], ['pho_ban.phut_toi_da', PHO_BAN.phutToiDa],
    ['sv.chat.capGan', CHAT.capGan], ['sv.chat.capTheGioi', CHAT.capTheGioi], ['sv.chat.xuTheGioi', CHAT.xuTheGioi],
    ['sv.chat.giayTheGioi', CHAT.giayTheGioi], ['sv.chat.capRieng', CHAT.capRieng], ['sv.chat.doDai', CHAT.doDai],
    ['sv.chat.chanToiDa', CHAT.chanToiDa],
    ['pvp.ngam_tay.bu_tre_ms', NGAM_TAY.buTreMs], ['pvp.ngam_tay.giay_giao_chien', NGAM_TAY.giayGiaoChien],
    ['danh_hieu.tran_phan_nghin', DANH_HIEU.tranPhanNghin],
    ['loai.doi_thuong_phan_tram', HOANG_DA.t12PhanTram], ['loai.gap_t3', HOANG_DA.gapT3], ['loai.gap_t4', HOANG_DA.gapT4],
    ['loai.gap_t5', HOANG_DA.gapT5],
  ]
  for (const [k, v] of ds) assert.equal(ch(k), v, k)
})

test('phó bản theo thứ == data_pho_ban_ngay, số tầng == data_pho_ban_tang', () => {
  assert.deepEqual(bang('data_pho_ban_ngay').map(r => ({ thu: r.thu, ten: r.ten })), PHO_BAN.ngay.map(x => ({ ...x })))
  assert.equal(bang('data_pho_ban_tang').length, PHO_BAN.soTang)
})

test('ngắm tay: số chiêu == data_chieu_ban', () => {
  assert.equal(bang('data_chieu_ban').length, NGAM_TAY.soChieu)
})

test('thời tiết == data_thoi_tiet, ngày cộng đồng == data_ngay_cong_dong', () => {
  assert.deepEqual(bang('data_thoi_tiet').map(r => ({ ten: r.ten, goiY: r.goi_y })), THOI_TIET.loai.map(x => ({ ...x })))
  for (const r of bang('data_ngay_cong_dong')) {
    assert.equal(r.gio_bat_dau, NGAY_CONG_DONG.gioBatDau)
    assert.equal(r.so_gio, NGAY_CONG_DONG.soGio)
    assert.equal(r.ti_le, NGAY_CONG_DONG.tiLePhanTram)
    assert.equal(r.exp_phan_tram, NGAY_CONG_DONG.expPhanTram)
    assert.equal(r.so_ha, NGAY_CONG_DONG.soHa)
  }
})

test('danh hiệu: bậc, màu, số mẫu == data_danh_hieu / data_danh_hieu_do_hiem', () => {
  const dh = bang('data_danh_hieu')
  const bac = bang('data_danh_hieu_do_hiem')
  assert.deepEqual(bac.map(r => ({ ten: r.ten, mau: r.mau, so: dh.filter(d => d.do_hiem === r.id).length })), DANH_HIEU.bac.map(x => ({ ...x })))
})

test('kết đôi: 15 nhóm trứng == data_nhom_trung_ten; tân thủ == data_quest_moi 901..910', () => {
  assert.equal(bang('data_nhom_trung_ten').length, KET_DOI.soNhomTrung)
  const n = bang('data_nhom_trung_ten')
  assert.deepEqual(n.filter(r => Number(r.ket_doi_duoc) === 1).map(r => r.ten), [...KET_DOI.nhom])
  assert.deepEqual(n.filter(r => Number(r.ket_doi_duoc) === 0).map(r => r.ten), [KET_DOI.khongKetDoi])
  const q = bang('data_quest_moi').filter(r => Number(r.id) >= 901 && Number(r.id) <= 910).sort((a, b) => Number(a.id) - Number(b.id))
  assert.deepEqual(q.map(r => ({ ten: r.name, viec: r.scontent })), TAN_THU.map(x => ({ ...x })))
})

test('trang cẩm nang 0.9.8 có tệp .vue', () => {
  for (const s of ['thu-phuc', 'trao-doi-cho', 'thoi-tiet', 'danh-hieu', 'kenh-chat', 'ket-doi']) {
    assert.ok(DS_CAM_NANG.some(p => p.slug === s), `DS_CAM_NANG thiếu ${s}`)
    assert.ok(existsSync(goc(`../app/pages/cam-nang/${s}.vue`)), `thiếu app/pages/cam-nang/${s}.vue`)
  }
})

test('ảnh trong game: mỗi ảnh có đủ hai cỡ (trừ ảnh 0.9.8 đang tạm dùng ảnh khác)', () => {
  for (const ma of Object.keys(ANH_GAME)) {
    const t = ANH_TAM[ma] ?? ma
    for (const c of [640, 1280]) assert.ok(existsSync(goc(`../public/anh-game/${t}-${c}.webp`)), `thiếu public/anh-game/${t}-${c}.webp`)
    if (ANH_TAM[ma]) assert.ok(!existsSync(goc(`../public/anh-game/${ma}-1280.webp`)), `đã có ảnh ${ma}: xoá dòng '${ma}' khỏi ANH_TAM`)
  }
})
