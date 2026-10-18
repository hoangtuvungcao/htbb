/**
 * Hồ sơ MỘT tài khoản cho Trung tâm người chơi. ?u=<tên tài khoản> hoặc ?u=#<id tài khoản>
 * Gồm: thông tin tài khoản, khoá đang hiệu lực + lịch sử khoá/cấm chat, máy & IP đăng nhập (kèm số tài
 * khoản khác cùng máy, máy bị cấm), danh sách nhân vật (loài, cấp, % EXP, tiền tệ, online, cấm chat, vị trí),
 * thư gần đây, quà riêng đã tặng, thao tác quản trị gần đây trên tài khoản / nhân vật này.
 * Chi tiết sâu của một nhân vật (túi đồ, Pokédex, nhiệm vụ, sinh thái...) ở nhan-vat.get.ts.
 * Vai trò "Chỉ xem": email/điện thoại bị che bớt, mã quà riêng bị che.
 */
import { hsAnEmail, hsAnMa, hsDocViTri, hsPhanTramCap, hsQuyetDinhOnline, HS_TIEN_TO_QUA_RIENG } from '../../../utils/hoSoNguoiChoi'
import { hsTenBanDo } from '../../../utils/tenBanDoNguoiChoi'
import { gcMoTa } from '../../../utils/giftcodeAnToan'

export default defineAdminHandler(async (event, admin) => {
  // ?u=<tên> hoặc ?u=#<id tài khoản> (trang Nhân vật chỉ biết userId)
  const raw = getQuery(event).u
  const theoId = typeof raw === 'string' && /^#\d{1,10}$/.test(raw.trim()) ? Number(raw.trim().slice(1)) : null
  const u = theoId === null ? ncTenTaiKhoan(raw) : String(raw).trim()
  const tk = await q1<Record<string, any>>(dbAccount(), `SELECT id, username, email, phone, regdate, ban, provider, fromgame FROM team_user WHERE ${theoId === null ? 'username' : 'id'} = ? LIMIT 1`, [theoId ?? u])
  if (!tk) throw apiLoi(404, `Không có tài khoản "${u}".`)
  const ten = String(tk.username)
  const sieu = admin.vaiTro === 'sieu_quan_tri'
  const chiXem = admin.vaiTro === 'xem'
  const anMaQua = (s: string) => (sieu ? s : s.replace(new RegExp(`\\b${HS_TIEN_TO_QUA_RIENG}[a-z0-9]{14}\\b`, 'g'), m => hsAnMa(m)))

  // ---- khoá / cấm chat
  const khoa = (await ncDocAnToan<Record<string, any>>('web_khoa_tai_khoan',
    'SELECT id, loai, ly_do, het_han, admin_user, tao_luc, mo_luc FROM web_khoa_tai_khoan WHERE username = ? ORDER BY id DESC LIMIT 50', [ten])) ?? []
  const khoaHienTai = khoa.find(k => k.loai === 'khoa' && !k.mo_luc) ?? null

  // ---- máy & IP
  const may = (await ncDocAnToan<Record<string, any>>('tob_may_dang_nhap',
    'SELECT ma_may, ip, lan_dau, lan_cuoi, so_lan FROM tob_may_dang_nhap WHERE username = ? ORDER BY lan_cuoi DESC LIMIT 50', [ten])) ?? []
  const dsMay = [...new Set(may.map(m => String(m.ma_may)))].filter(Boolean)
  const cungMay = new Map<string, number>()
  const mayCam = new Set<string>()
  if (dsMay.length) {
    const cho = dsMay.map(() => '?').join(',')
    for (const r of (await ncDocAnToan<{ ma_may: string, n: number }>('tob_may_dang_nhap',
      `SELECT ma_may, COUNT(DISTINCT username) AS n FROM tob_may_dang_nhap WHERE ma_may IN (${cho}) GROUP BY ma_may`, dsMay)) ?? []) {
      cungMay.set(String(r.ma_may), Math.max(0, Number(r.n) - 1))
    }
    for (const r of (await ncDocAnToan<{ ma_may: string }>('tob_cam_may', `SELECT ma_may FROM tob_cam_may WHERE ma_may IN (${cho})`, dsMay)) ?? []) mayCam.add(String(r.ma_may))
  }

  // ---- nhân vật
  const nv = await q<Record<string, any>>(dbGame(),
    `SELECT id, charname, lastLv, xp, \`class\`, dex, shiny, gold, luong, luonglock, kho_xu, kho_luong, kho_luong_khoa,
            ban, del, lastLog, totalTimePlay, idClan, pInfo FROM tob_char WHERE userId = ? ORDER BY lastLv DESC, id ASC`, [Number(tk.id)])
  const { tinHieu, viTri } = await ncTinHieuOnline()
  const expMain = capDoRaExp()
  const ids = nv.map(c => Number(c.id))
  const demDex = new Map<number, number>()
  if (ids.length) {
    for (const r of (await ncDocAnToan<{ id_char: number, n: number }>('tob_pokedex',
      `SELECT id_char, COUNT(*) AS n FROM tob_pokedex WHERE so_ha > 0 AND id_char IN (${ids.map(() => '?').join(',')}) GROUP BY id_char`, ids)) ?? []) {
      demDex.set(Number(r.id_char), Number(r.n))
    }
  }
  const nhanVat = nv.map((c) => {
    const onl = hsQuyetDinhOnline({ ...tinHieu, coTrongViTri: viTri.has(String(c.charname)) })
    const vtOn = viTri.get(String(c.charname))
    const vtLuu = hsDocViTri(c.pInfo)
    const camChatDen = docCamChat(c.pInfo)
    return {
      id: Number(c.id), charname: String(c.charname), lastLv: Number(c.lastLv), xp: String(c.xp ?? '0'),
      phanTramCap: hsPhanTramCap(c.xp, Number(c.lastLv), expMain),
      class: Number(c.class), loai: speciesName(c.class, c.lastLv, c.dex), dex: dexHienTai(c.class, c.lastLv, c.dex), shiny: Number(c.shiny) === 1,
      gold: c.gold, luong: c.luong, luonglock: c.luonglock, khoXu: c.kho_xu, khoLuong: c.kho_luong, khoLuongKhoa: c.kho_luong_khoa,
      ban: Number(c.ban), del: Number(c.del), lastLog: c.lastLog ? String(c.lastLog) : '', totalTimePlay: Number(c.totalTimePlay ?? 0),
      idClan: Number(c.idClan ?? -1),
      pokedexDaHa: demDex.get(Number(c.id)) ?? 0,
      camChatDen,
      dangCamChat: camChatDen !== '' && camChatDen > gioHienTai(),
      online: onl.online, onlineLyDo: onl.lyDo,
      viTri: vtOn
        ? { nguon: 'online', map: vtOn.map_id, ten: vtOn.map_ten || hsTenBanDo(vtOn.map_id), khu: vtOn.khu, x: vtOn.x, y: vtOn.y }
        : vtLuu ? { nguon: 'da_luu', map: vtLuu.map, ten: hsTenBanDo(vtLuu.map), khu: null, x: vtLuu.x, y: vtLuu.y } : null,
    }
  })

  // ---- thư gần đây
  const tenNv = nhanVat.map(c => c.charname)
  const thu = tenNv.length
    ? ((await ncDocAnToan<Record<string, any>>('tob_inbox',
        `SELECT id, charname, sender, info, dateSend FROM tob_inbox WHERE charname IN (${tenNv.map(() => '?').join(',')}) ORDER BY id DESC LIMIT 30`, tenNv)) ?? [])
        .map(t => ({ ...t, info: anMaQua(String(t.info ?? '')) }))
    : []

  // ---- quà riêng (giftcode cá nhân tạo từ trang này)
  const quaTho = (await ncDocAnToan<Record<string, any>>('tob_giftcode',
    "SELECT ma, phan_thuong, han, tong_luot, da_dung, ghi_chu, tao_boi, tao_luc FROM tob_giftcode WHERE ma LIKE ? AND ghi_chu LIKE ? ESCAPE '!' ORDER BY tao_luc DESC LIMIT 30",
    [`${HS_TIEN_TO_QUA_RIENG}%`, `%(TK ${ten.replace(/[!%_]/g, c => `!${c}`)})%`])) ?? []
  const quaRieng = []
  for (const g of quaTho) {
    const nhan = (await ncDocAnToan<Record<string, any>>('tob_giftcode_log', 'SELECT charname, luc FROM tob_giftcode_log WHERE ma = ? LIMIT 1', [g.ma]))?.[0] ?? null
    quaRieng.push({
      ma: sieu ? String(g.ma) : hsAnMa(String(g.ma)), moTa: (await gcMoTa(String(g.phan_thuong))).join(', '),
      han: g.han ? String(g.han) : '', daDung: Number(g.da_dung) >= Number(g.tong_luot), ghiChu: String(g.ghi_chu ?? ''),
      taoBoi: String(g.tao_boi ?? ''), taoLuc: g.tao_luc ? String(g.tao_luc) : '', nhanBoi: nhan ? String(nhan.charname) : '', nhanLuc: nhan?.luc ? String(nhan.luc) : '',
    })
  }

  // ---- thao tác quản trị gần đây trên tài khoản / nhân vật này (các route cũ ghi doi_tuong kiểu "ten", "tk:ten", "nv:ten")
  const doiTuong = [ten, `tk:${ten}`, ...tenNv.flatMap(n => [n, `nv:${n}`])].slice(0, 60)
  const nhatKy = (await ncDocAnToan<Record<string, any>>('web_admin_log',
    `SELECT id, admin_user, hanh_dong, doi_tuong, chi_tiet, thoi_gian FROM web_admin_log
      WHERE doi_tuong IN (${doiTuong.map(() => '?').join(',')}) AND hanh_dong <> 'loi_web' ORDER BY id DESC LIMIT 60`, doiTuong)) ?? []

  return {
    tk: {
      id: Number(tk.id), username: ten,
      email: chiXem ? hsAnEmail(tk.email) : String(tk.email ?? ''),
      phone: chiXem && tk.phone ? `${String(tk.phone).slice(0, 3)}•••••` : String(tk.phone ?? ''),
      regdate: tk.regdate ? String(tk.regdate) : '', ban: Number(tk.ban), fromgame: Number(tk.fromgame), provider: Number(tk.provider),
      laAdmin: laTaiKhoanAdmin(ten), laChinhMinh: ten.toLowerCase() === admin.username.toLowerCase(),
    },
    khoaHienTai,
    khoa,
    may: may.map(m => ({ ...m, cungMay: cungMay.get(String(m.ma_may)) ?? 0, biCam: mayCam.has(String(m.ma_may)) })),
    nhanVat,
    thu,
    quaRieng,
    nhatKy,
    mayChu: { giayNhipTim: tinHieu.giayNhipTim, soOnline: tinHieu.soOnlineNhipTim },
  }
})
