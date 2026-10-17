/** Hồ sơ một tài khoản: thông tin, lịch sử khoá, IP/máy đăng nhập, các nhân vật (cấp, loài, xu, lượng, Pokédex, túi đồ, cấm chat). ?u= */
export default defineAdminHandler(async (event) => {
  const u = str(getQuery(event).u).toLowerCase()
  const tk = await q1<Record<string, any>>(dbAccount(), 'SELECT id, username, email, phone, regdate, ban FROM team_user WHERE username = ? LIMIT 1', [u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  const khoa = (await coBangCms('web_khoa_tai_khoan'))
    ? await q(dbGame(), 'SELECT * FROM web_khoa_tai_khoan WHERE username = ? ORDER BY id DESC LIMIT 30', [u]) : []
  let may: Record<string, any>[] = []
  try { may = await q(dbGame(), 'SELECT ip, lan_dau, lan_cuoi, so_lan FROM tob_may_dang_nhap WHERE username = ? ORDER BY lan_cuoi DESC LIMIT 20', [u]) }
  catch { may = [] }
  const nv = await q<Record<string, any>>(dbGame(),
    'SELECT id, charname, lastLv, class, dex, shiny, gold, luong, luonglock, kho_xu, kho_luong, ban, lastLog, totalTimePlay, pInfo, equip, inven, bag FROM tob_char WHERE userId = ? ORDER BY lastLv DESC', [Number(tk.id)])
  const nhanVat = []
  for (const c of nv) {
    let dex = 0
    try { dex = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_pokedex WHERE id_char = ? AND so_ha > 0', [c.id])) }
    catch { dex = 0 }
    const tui = tachTuiVaThoiTrang(c.inven)
    nhanVat.push({
      id: c.id, charname: c.charname, lastLv: c.lastLv, class: c.class, dex: c.dex, shiny: c.shiny, loai: speciesName(c.class, c.lastLv, c.dex), dexHienTai: dexHienTai(c.class, c.lastLv, c.dex),
      gold: c.gold, luong: c.luong, luonglock: c.luonglock, khoXu: c.kho_xu, khoLuong: c.kho_luong, ban: c.ban, lastLog: c.lastLog, totalTimePlay: c.totalTimePlay,
      pokedexDaHa: dex, camChatDen: docCamChat(c.pInfo), online: await dangOnline(String(c.charname)),
      soVatPham: { mac: docChuoiVatPham(c.equip).length, tui: tui.tui.length, thoiTrang: tui.thoi_trang.length, ruong: docChuoiVatPham(c.bag).length },
    })
  }
  return { tk: { ...tk, laAdmin: laTaiKhoanAdmin(String(tk.username)) }, khoa, may, nhanVat }
})
