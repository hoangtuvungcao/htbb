/**
 * Chi tiết sâu MỘT nhân vật cho hồ sơ người chơi. ?id=<id nhân vật>
 * Túi đồ (đang mặc / thời trang / hành trang / rương), Pokédex (đã hạ, shiny, mốc thưởng), nhiệm vụ (chuỗi cũ
 * tob_char_quest + nhiệm vụ lâu dài tob_nhiem_vu_lau_dai), hồ sơ lâu dài (tob_char_lau_dai), hồ sơ hệ sinh thái
 * (tob_hst_nhan_vat), PvP mùa, Hầm Ngục, sư phạm, lệnh web đã gửi. Bảng nào chưa có thì phần đó = null.
 */
import { hsBacPvp, hsGiaiMaLauDai, hsGiaiMaSinhThai, hsTachNhiemVuCu } from '../../../utils/hoSoNguoiChoi'
import type { VatPhamNhanVat } from '../../../utils/vatPham'

const TEN_LOAI_NV: Record<string, string> = {
  huong_dan: 'Hướng dẫn', chinh: 'Cốt truyện', phu: 'Phụ', ngay: 'Hằng ngày', tuan: 'Hằng tuần', pokedex: 'Pokédex',
  doi: 'Đội', su_pham: 'Sư phạm', hang_bi_an: 'Hang bí ẩn', quay_lai: 'Quay lại', su_kien: 'Sự kiện',
  huyen_thoai: 'Huyền thoại', an: 'Ẩn', thanh_tuu: 'Thành tựu',
}

export default defineAdminHandler(async (event, admin) => {
  const id = ncIdNhanVat(getQuery(event).id)
  const c = await q1<Record<string, any>>(dbGame(), 'SELECT id, charname, userId, lastLv, `class`, dex, equip, inven, bag FROM tob_char WHERE id = ? LIMIT 1', [id])
  if (!c) throw apiLoi(404, `Không tìm thấy Pokémon #${id}.`)

  // ---- vật phẩm (cùng cách đọc với /api/admin/nhanvat/[id])
  const mac = docChuoiVatPham(c.equip ?? null)
  const tach = tachTuiVaThoiTrang(c.inven ?? null)
  const ruong = docChuoiVatPham(c.bag ?? null)
  const tenVP = await tenVatPhamTheoId(dbGame(), [...mac, ...tach.tui, ...tach.thoi_trang, ...ruong].map(v => v.template))
  const kem = (ds: VatPhamNhanVat[]) => ds.map(v => ({ ...v, ten: tenVP[v.template] ?? null, ten_mau: tenMauVatPham(v.mau) }))

  // ---- Pokédex
  const dexRows = await ncDocAnToan<{ dex: number, so_ha: number, lan_dau: string | null }>('tob_pokedex',
    'SELECT dex, so_ha, lan_dau FROM tob_pokedex WHERE id_char = ? ORDER BY dex', [id])
  const shinyRows = await ncDocAnToan<{ dex: number, so_shiny: number }>('tob_pokedex_shiny', 'SELECT dex, so_shiny FROM tob_pokedex_shiny WHERE id_char = ? ORDER BY dex', [id])
  const thuongRows = await ncDocAnToan<{ moc: number, thoi_gian: string | null }>('tob_pokedex_thuong', 'SELECT moc, thoi_gian FROM tob_pokedex_thuong WHERE id_char = ? ORDER BY moc', [id])
  const shinyMap = new Map((shinyRows ?? []).map(r => [Number(r.dex), Number(r.so_shiny)]))
  const pokedex = dexRows === null ? null : {
    daHa: dexRows.filter(r => Number(r.so_ha) > 0).length,
    soShiny: (shinyRows ?? []).filter(r => Number(r.so_shiny) > 0).length,
    tongLoai: POKEDEX.loai.length,
    moc: POKEDEX.mocPokedex.map(m => ({ moc: m.moc, thuong: m.thuong.map(t => `${t.soLuong} × ${t.ten}`).join(', '), daNhan: (thuongRows ?? []).some(t => Number(t.moc) === m.moc) })),
    ds: dexRows.map(r => ({ dex: Number(r.dex), ten: loaiTheoDex(Number(r.dex))?.ten ?? `#${r.dex}`, soHa: Number(r.so_ha), lanDau: r.lan_dau ? String(r.lan_dau) : '', shiny: shinyMap.get(Number(r.dex)) ?? 0 })),
  }

  // ---- nhiệm vụ
  const qc = (await ncDocAnToan<{ isfinish: string }>('tob_char_quest', 'SELECT isfinish FROM tob_char_quest WHERE id_char = ? LIMIT 1', [id]))?.[0] ?? null
  const cu = hsTachNhiemVuCu(qc?.isfinish)
  const tenCu = new Map<number, string>()
  if (cu.length) {
    const qi = cu.map(x => x.id)
    for (const bang of ['data_quest_moi', 'data_quest']) {
      for (const r of (await ncDocAnToan<{ id: number, name: string }>(bang, `SELECT id, name FROM ${bang} WHERE id IN (${qi.map(() => '?').join(',')})`, qi)) ?? []) {
        if (!tenCu.has(Number(r.id))) tenCu.set(Number(r.id), String(r.name))
      }
    }
  }
  const lauDaiRows = await ncDocAnToan<{ ma: string, tien_do: number, trang_thai: number, tuan: number }>('tob_nhiem_vu_lau_dai',
    'SELECT ma, tien_do, trang_thai, tuan FROM tob_nhiem_vu_lau_dai WHERE id_char = ? LIMIT 2000', [id])
  const maNv = [...new Set((lauDaiRows ?? []).map(r => String(r.ma)))]
  const moTaNv = new Map<string, { ten: string, loai: string, soLuong: number }>()
  for (let i = 0; i < maNv.length; i += 500) {
    const phan = maNv.slice(i, i + 500)
    for (const r of (await ncDocAnToan<{ ma: string, ten: string, loai: string, so_luong: number }>('data_quest_lau_dai',
      `SELECT ma, ten, loai, so_luong FROM data_quest_lau_dai WHERE ma IN (${phan.map(() => '?').join(',')})`, phan)) ?? []) {
      moTaNv.set(String(r.ma), { ten: String(r.ten), loai: String(r.loai), soLuong: Number(r.so_luong) })
    }
  }
  const lauDai = (lauDaiRows ?? []).map((r) => {
    const m = moTaNv.get(String(r.ma))
    return { ma: String(r.ma), ten: m?.ten ?? String(r.ma), loai: TEN_LOAI_NV[m?.loai ?? ''] ?? (m?.loai || '—'), tienDo: Number(r.tien_do), canDat: m?.soLuong ?? 0, trangThai: Number(r.trang_thai), tuan: Number(r.tuan) }
  })
  const demTheoLoai: Record<string, { xong: number, dangLam: number }> = {}
  for (const n of lauDai) {
    const d = (demTheoLoai[n.loai] ??= { xong: 0, dangLam: 0 })
    if (n.trangThai >= 1) d.xong++
    else d.dangLam++
  }
  const nhiemVu = {
    cu: cu.map(x => ({ ...x, ten: tenCu.get(x.id) ?? `Nhiệm vụ #${x.id}` })),
    coBangLauDai: lauDaiRows !== null,
    lauDai: lauDai.sort((a, b) => a.trangThai - b.trangThai || a.ma.localeCompare(b.ma)).slice(0, 300),
    tongLauDai: lauDai.length,
    demTheoLoai,
  }

  // ---- hồ sơ lâu dài + hệ sinh thái
  const ld = (await ncDocAnToan<{ du_lieu: string, cap_nhat: string }>('tob_char_lau_dai', 'SELECT du_lieu, cap_nhat FROM tob_char_lau_dai WHERE id_char = ? LIMIT 1', [id]))
  const hoSoLauDai = ld === null ? null : ld[0] ? { ...hsGiaiMaLauDai(ld[0].du_lieu), capNhat: String(ld[0].cap_nhat ?? ''), tho: chiTho(admin.vaiTro, ld[0].du_lieu) } : { ...hsGiaiMaLauDai(''), capNhat: '', tho: '' }
  const st = await ncDocAnToan<{ du_lieu: string, cap_nhat: number }>('tob_hst_nhan_vat', 'SELECT du_lieu, cap_nhat FROM tob_hst_nhan_vat WHERE id_char = ? LIMIT 1', [id])
  const sinhThai = st === null ? null : st[0] ? { ...hsGiaiMaSinhThai(st[0].du_lieu), capNhat: Number(st[0].cap_nhat) > 0 ? new Date(Number(st[0].cap_nhat)).toISOString() : '' } : { ...hsGiaiMaSinhThai(''), capNhat: '' }

  // ---- PvP, Hầm Ngục, sư phạm
  const pvpRows = await ncDocAnToan<{ mua: number, elo: number, thang: number, thua: number }>('tob_pvp_mua', 'SELECT mua, elo, thang, thua FROM tob_pvp_mua WHERE id_char = ? ORDER BY mua DESC LIMIT 20', [id])
  const muaHienTai = pvpRows === null ? null : Number((await ncDocAnToan<{ m: number | null }>('tob_pvp_mua', 'SELECT MAX(mua) AS m FROM tob_pvp_mua'))?.[0]?.m ?? 0)
  const pvp = pvpRows === null ? null : {
    muaHienTai,
    hienTai: pvpRows.find(r => Number(r.mua) === muaHienTai) ?? null,
    ds: pvpRows.map(r => ({ mua: Number(r.mua), elo: Number(r.elo), thang: Number(r.thang), thua: Number(r.thua), bac: hsBacPvp(Number(r.elo)) })),
  }
  const hnRows = await ncDocAnToan<{ ngay: number, tang: number, ghi_luc: string | null }>('tob_ham_nguc', 'SELECT ngay, tang, ghi_luc FROM tob_ham_nguc WHERE id_char = ? ORDER BY tang DESC, ngay DESC LIMIT 10', [id])
  const hamNguc = hnRows === null ? null : { tangCao: hnRows[0] ? Number(hnRows[0].tang) : 0, ngayCao: hnRows[0] ? Number(hnRows[0].ngay) : 0, ds: hnRows }
  const laTro = await ncDocAnToan<{ id_thay: number, bat_dau: string | null, moc: string }>('tob_su_pham', 'SELECT id_thay, bat_dau, moc FROM tob_su_pham WHERE id_tro = ? LIMIT 1', [id])
  const laThay = await ncDocAnToan<{ id_tro: number, bat_dau: string | null, moc: string }>('tob_su_pham', 'SELECT id_tro, bat_dau, moc FROM tob_su_pham WHERE id_thay = ? ORDER BY bat_dau DESC LIMIT 50', [id])
  let suPham = null
  if (laTro !== null) {
    const idCan = [...(laTro ?? []).map(r => Number(r.id_thay)), ...(laThay ?? []).map(r => Number(r.id_tro))]
    const tenNv = new Map<number, string>()
    if (idCan.length) for (const r of await q<{ id: number, charname: string }>(dbGame(), `SELECT id, charname FROM tob_char WHERE id IN (${idCan.map(() => '?').join(',')})`, idCan)) tenNv.set(Number(r.id), String(r.charname))
    suPham = {
      thay: laTro[0] ? { id: Number(laTro[0].id_thay), ten: tenNv.get(Number(laTro[0].id_thay)) ?? `#${laTro[0].id_thay}`, batDau: String(laTro[0].bat_dau ?? ''), moc: String(laTro[0].moc ?? '') } : null,
      tro: (laThay ?? []).map(r => ({ id: Number(r.id_tro), ten: tenNv.get(Number(r.id_tro)) ?? `#${r.id_tro}`, batDau: String(r.bat_dau ?? ''), moc: String(r.moc ?? '') })),
    }
  }

  // ---- lệnh web đã gửi
  let lenh: ReturnType<typeof lenhChoGiaoDien>[] = []
  try { lenh = (await docLenhCuaNhanVat(dbGame(), id)).map(lenhChoGiaoDien) }
  catch { lenh = [] }

  return {
    id, charname: String(c.charname), userId: Number(c.userId),
    vatPham: { mac: kem(mac), thoiTrang: kem(tach.thoi_trang), tui: kem(tach.tui), ruong: kem(ruong) },
    pokedex, nhiemVu, hoSoLauDai, sinhThai, pvp, hamNguc, suPham, lenh,
  }
})

/** Chuỗi thô chỉ cho siêu quản trị / hỗ trợ (để đối chiếu khi báo lỗi). */
function chiTho(vaiTro: string, s: string): string {
  return vaiTro === 'xem' ? '' : String(s ?? '').slice(0, 2000)
}
