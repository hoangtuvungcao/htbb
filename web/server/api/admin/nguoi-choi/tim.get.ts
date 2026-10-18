/**
 * Tìm người chơi. ?q= (2–64 ký tự) &kieu= tat_ca | tai_khoan | nhan_vat | email | ip | ma_may
 *   - tai_khoan / nhan_vat / email: gần đúng (LIKE); số thuần thì khớp thêm id tài khoản / id nhân vật.
 *   - ip: theo đầu chuỗi trong lịch sử đăng nhập tob_may_dang_nhap; ma_may: khớp đúng hoặc theo đầu chuỗi.
 *   - tat_ca: tài khoản + nhân vật + email, và IP nếu chuỗi trông như IP.
 * Trả về MỖI TÀI KHOẢN một dòng (tối đa 50) kèm nhân vật, cấp, online, khoá, và lý do khớp.
 */
import { hsAnEmail, hsChuanHoaTim, hsGiongIp, hsLike, hsQuyetDinhOnline } from '../../../utils/hoSoNguoiChoi'

const TOI_DA = 50

export default defineAdminHandler(async (event, admin) => {
  const { q: tu, kieu } = hsChuanHoaTim(getQuery(event).q, getQuery(event).kieu)
  if (tu.length < 2) return { q: tu, kieu, ketQua: [], gioiHan: false }
  const so = /^\d{1,10}$/.test(tu) ? Number(tu) : null
  const like = hsLike(tu.toLowerCase())
  const khop = new Map<string, Set<string>>() // username -> lý do
  const theoId = new Map<number, Set<string>>() // userId -> lý do
  const them = <K>(m: Map<K, Set<string>>, k: K, ly: string) => { if (!m.has(k)) m.set(k, new Set()); m.get(k)!.add(ly) }

  if (kieu === 'tat_ca' || kieu === 'tai_khoan') {
    const rows = await q<{ id: number, username: string }>(dbAccount(),
      `SELECT id, username FROM team_user WHERE username LIKE ? ESCAPE '!' ${so !== null ? 'OR id = ?' : ''} ORDER BY id DESC LIMIT ${TOI_DA}`,
      so !== null ? [like, so] : [like])
    for (const r of rows) them(theoId, Number(r.id), Number(r.id) === so ? 'ID tài khoản' : 'Tên tài khoản')
  }
  if (kieu === 'tat_ca' || kieu === 'email') {
    const rows = await q<{ id: number }>(dbAccount(), `SELECT id FROM team_user WHERE email LIKE ? ESCAPE '!' ORDER BY id DESC LIMIT ${TOI_DA}`, [like])
    for (const r of rows) them(theoId, Number(r.id), 'Email')
  }
  if (kieu === 'tat_ca' || kieu === 'nhan_vat') {
    const rows = await q<{ userId: number, charname: string, id: number }>(dbGame(),
      `SELECT id, userId, charname FROM tob_char WHERE charname LIKE ? ESCAPE '!' ${so !== null ? 'OR id = ?' : ''} ORDER BY lastLv DESC LIMIT ${TOI_DA}`,
      so !== null ? [like, so] : [like])
    for (const r of rows) them(theoId, Number(r.userId), Number(r.id) === so ? `ID Pokémon #${r.id}` : `Pokémon ${r.charname}`)
  }
  const timIp = kieu === 'ip' || (kieu === 'tat_ca' && hsGiongIp(tu))
  if (timIp || kieu === 'ma_may') {
    try {
      if (await coBang(dbGame(), 'tob_may_dang_nhap')) {
        const rows = timIp
          ? await q<{ username: string, ip: string }>(dbGame(), `SELECT username, ip FROM tob_may_dang_nhap WHERE ip LIKE ? ESCAPE '!' ORDER BY lan_cuoi DESC LIMIT 200`, [hsLike(tu, 'dau')])
          : await q<{ username: string, ma_may: string }>(dbGame(), `SELECT username, ma_may FROM tob_may_dang_nhap WHERE ma_may = ? OR ma_may LIKE ? ESCAPE '!' ORDER BY lan_cuoi DESC LIMIT 200`, [tu, hsLike(tu, 'dau')])
        for (const r of rows as Record<string, any>[]) them(khop, String(r.username).toLowerCase(), timIp ? `IP ${r.ip}` : `Mã máy ${String(r.ma_may).slice(0, 16)}…`)
      }
    }
    catch (e: any) { console.error('[nguoi-choi/tim] may', e?.code ?? e) }
  }

  // Gộp: tài khoản theo id + theo tên
  const ids = [...theoId.keys()].slice(0, TOI_DA)
  const tens = [...khop.keys()].slice(0, TOI_DA)
  const dk: string[] = []
  const ts: unknown[] = []
  if (ids.length) { dk.push(`id IN (${ids.map(() => '?').join(',')})`); ts.push(...ids) }
  if (tens.length) { dk.push(`username IN (${tens.map(() => '?').join(',')})`); ts.push(...tens) }
  const tk = dk.length
    ? await q<{ id: number, username: string, email: string | null, regdate: string | null, ban: number, fromgame: number }>(dbAccount(),
        `SELECT id, username, email, regdate, ban, fromgame FROM team_user WHERE ${dk.join(' OR ')} ORDER BY id DESC LIMIT ${TOI_DA}`, ts)
    : []

  const uids = tk.map(t => Number(t.id))
  const nv = uids.length
    ? await q<{ id: number, userId: number, charname: string, lastLv: number, class: number, dex: number, ban: number, lastLog: string | null }>(dbGame(),
        `SELECT id, userId, charname, lastLv, \`class\`, dex, ban, lastLog FROM tob_char WHERE userId IN (${uids.map(() => '?').join(',')}) ORDER BY lastLv DESC`, uids)
    : []
  const { tinHieu, viTri } = await ncTinHieuOnline()
  const anEmail = admin.vaiTro === 'xem'

  const ketQua = tk.map((t) => {
    const cua = nv.filter(n => Number(n.userId) === Number(t.id)).map((n) => {
      const on = hsQuyetDinhOnline({ ...tinHieu, coTrongViTri: viTri.has(String(n.charname)) }).online
      return {
        id: Number(n.id), charname: String(n.charname), lastLv: Number(n.lastLv), ban: Number(n.ban),
        loai: speciesName(n.class, n.lastLv, n.dex), dex: dexHienTai(n.class, n.lastLv, n.dex),
        lastLog: n.lastLog ? String(n.lastLog) : '', online: on,
      }
    })
    const lyDo = new Set([...(theoId.get(Number(t.id)) ?? []), ...(khop.get(String(t.username).toLowerCase()) ?? [])])
    return {
      id: Number(t.id),
      username: String(t.username),
      email: anEmail ? hsAnEmail(t.email) : String(t.email ?? ''),
      regdate: t.regdate ? String(t.regdate) : '',
      ban: Number(t.ban),
      tuGame: Number(t.fromgame) === 1,
      laAdmin: laTaiKhoanAdmin(String(t.username)),
      nhanVat: cua,
      capCao: cua.reduce((m, c) => Math.max(m, c.lastLv), 0),
      dangOnline: cua.some(c => c.online === true),
      lanCuoi: cua.reduce((m, c) => (c.lastLog > m ? c.lastLog : m), ''),
      khop: [...lyDo].slice(0, 4),
    }
  })
  return { q: tu, kieu, ketQua, gioiHan: ketQua.length >= TOI_DA }
})
