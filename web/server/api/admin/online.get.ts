/**
 * Ai đang online, đang ở map nào — đọc bảng `tob_vi_tri_online`.
 *
 * Bảng đó do server tự ghi (src/data/GhiViTriOnline.java): mỗi 30 giây XOÁ HẾT rồi
 * ghi lại toàn bộ người chơi đang online + map/toạ độ hiện tại. Vì vậy trang này
 * KHÔNG có khái niệm "trang sau" hay "log lịch sử" — nó luôn hiện đúng bức ảnh mới
 * nhất, chỉ có thể trễ tối đa 30 giây so với thực tế.
 *
 * Bảng chưa có (server chưa khởi động lại lần nào từ khi có tính năng này) thì trả
 * bangChuaCo = true để trang hiện hướng dẫn thay vì lỗi khó hiểu.
 *
 * Cột client (server bản mới mới có — bản cũ thiếu cột thì trang hiện "?"):
 *   tai_khoan, client (pc / android / jar / khac), chinh_chu (1 = client HTBB chính thức: gửi
 *   mã máy lúc đăng nhập; 0 = client gốc TeaMobi / client ngoài), client_ban (jar: "v159"), platf.
 *   Xem data/GhiClient.java (repo HTBB).
 *
 * Query: ?locTen=&locMap= (gần đúng, LIKE), ?banNgoai=1 (chỉ người dùng client ngoài)
 */
const BANG_VI_TRI = 'tob_vi_tri_online'

async function boSungTaiKhoanClient(dong: DongViTri[]): Promise<void> {
  const thieuTk = [...new Set(dong.filter(r => !r.tai_khoan).map(r => String(r.charname)))]
  if (thieuTk.length > 0) {
    try {
      const nv = await q<{ charname: string, userID: number }>(dbGame(),
        `SELECT charname, userID FROM tob_char WHERE charname IN (${thieuTk.map(() => '?').join(',')})`, thieuTk)
      const ids = [...new Set(nv.map(r => Number(r.userID)).filter(n => n > 0))]
      const tk = ids.length
        ? await q<{ id: number, username: string }>(dbAccount(),
          `SELECT id, username FROM team_user WHERE id IN (${ids.map(() => '?').join(',')})`, ids)
        : []
      const tenTk = new Map(tk.map(r => [Number(r.id), String(r.username)]))
      const theoNv = new Map(nv.map(r => [String(r.charname).toLowerCase(), tenTk.get(Number(r.userID)) ?? '']))
      for (const r of dong) if (!r.tai_khoan) r.tai_khoan = theoNv.get(String(r.charname).toLowerCase()) ?? ''
    } catch { /* để trống */ }
  }
  const thieuClient = [...new Set(dong.filter(r => !r.client && r.tai_khoan).map(r => String(r.tai_khoan)))]
  if (thieuClient.length > 0) {
    try {
      const cl = await q<{ tai_khoan: string, loai: string, chinh_chu: number, ban_jar: number, platf: string }>(dbGame(),
        `SELECT tai_khoan, loai, chinh_chu, ban_jar, platf FROM tob_client_dang_nhap
          WHERE tai_khoan IN (${thieuClient.map(() => '?').join(',')}) ORDER BY lan_cuoi ASC`, thieuClient)
      const m = new Map(cl.map(r => [String(r.tai_khoan).toLowerCase(), r])) // dòng sau (mới hơn) đè dòng trước
      for (const r of dong) {
        const c = !r.client && r.tai_khoan ? m.get(String(r.tai_khoan).toLowerCase()) : undefined
        if (c) {
          r.client = c.loai
          r.chinh_chu = Number(c.chinh_chu)
          r.client_ban = Number(c.ban_jar) > 0 ? `v${c.ban_jar}` : ''
          r.platf = c.platf
        }
      }
    } catch { /* bảng chưa có: để trống */ }
  }
}

interface DongViTri {
  charname: string
  map_id: number
  map_ten: string
  khu: number
  x: number
  y: number
  cap_nhat_luc: string
  tai_khoan?: string
  client?: string
  chinh_chu?: number
  client_ban?: string
  platf?: string
}

export default defineAdminHandler(async (event) => {
  const query = getQuery(event)
  const locTen = str(query.locTen)
  const locMap = str(query.locMap)
  const banNgoai = str(query.banNgoai) === '1'

  let dong: DongViTri[] = []
  let loiDoc = ''
  let bangChuaCo = false
  try {
    const dieuKien: string[] = []
    const thamSo: string[] = []
    // gõ % hoặc _ là tìm đúng ký tự đó (không thành ký tự đại diện)
    const thoat = (v: string) => v.replace(/[!%_]/g, c => `!${c}`)
    if (locTen !== '') {
      dieuKien.push("charname LIKE ? ESCAPE '!'")
      thamSo.push(`%${thoat(locTen)}%`)
    }
    if (locMap !== '') {
      dieuKien.push("map_ten LIKE ? ESCAPE '!'")
      thamSo.push(`%${thoat(locMap)}%`)
    }
    const where = dieuKien.length ? `WHERE ${dieuKien.join(' AND ')}` : ''
    dong = await q<DongViTri>(
      dbGame(),
      // SELECT *: server bản cũ chưa có cột client vẫn đọc được
      `SELECT * FROM \`${BANG_VI_TRI}\` ${where} ORDER BY map_ten, charname LIMIT 2000`,
      thamSo,
    )
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
    bangChuaCo = e?.code === 'ER_NO_SUCH_TABLE' || /exist/i.test(loiDoc)
  }

  // Thiếu tài khoản (server bản cũ chưa ghi cột tai_khoan, hoặc dòng ghi trước lần đăng nhập đầu sau khi
  // khởi động lại): tra charname -> tob_char.userID -> account.team_user.username. Thiếu client: lấy loại
  // client của lần đăng nhập gần nhất trong tob_client_dang_nhap. Hỏng ở bước nào thì để trống, không lỗi.
  await boSungTaiKhoanClient(dong)

  const coCotClient = dong.length > 0 && dong.some(r => (r.client ?? '') !== '')
  // Đếm theo client / bản ngoài trên TOÀN BỘ người đang online (trước khi lọc bản ngoài)
  const theoClient = { pc: 0, android: 0, jar: 0, khac: 0, chinhChu: 0, banNgoai: 0 }
  for (const r of dong) {
    const k = (['pc', 'android', 'jar'].includes(String(r.client)) ? r.client : 'khac') as 'pc' | 'android' | 'jar' | 'khac'
    theoClient[k]++
    if (!r.client) continue // chưa biết client: không tính là bản ngoài
    if (Number(r.chinh_chu) === 1) theoClient.chinhChu++
    else theoClient.banNgoai++
  }
  if (banNgoai && coCotClient) dong = dong.filter(r => r.client && Number(r.chinh_chu) !== 1)

  // Gom theo map để hiện số người trên từng map ngay trên đầu, không phải đọc từng dòng.
  const dem = new Map<string, number>()
  for (const r of dong) {
    const ten = String(r.map_ten ?? '') !== '' ? String(r.map_ten) : `(map id ${r.map_id})`
    dem.set(ten, (dem.get(ten) ?? 0) + 1)
  }
  const theoMap = [...dem.entries()].map(([ten, sl]) => ({ ten, sl })).sort((a, b) => b.sl - a.sl)

  return { dong, theoMap, theoClient, coCotClient, bangChuaCo, loiDoc, locTen, locMap, banNgoai }
})
