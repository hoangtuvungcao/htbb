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
 *   tai_khoan, client (pc / android / jar / khac), chinh_chu (1 = client NQSH-H2 chính thức: gửi
 *   mã máy lúc đăng nhập; 0 = client gốc TeaMobi / client ngoài), client_ban (jar: "v159"), platf.
 *   Xem data/GhiClient.java (repo KPAH).
 *
 * Query: ?locTen=&locMap= (gần đúng, LIKE), ?banNgoai=1 (chỉ người dùng client ngoài)
 */
const BANG_VI_TRI = 'tob_vi_tri_online'

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

  const coCotClient = dong.length > 0 && dong[0]!.client !== undefined
  // Đếm theo client / bản ngoài trên TOÀN BỘ người đang online (trước khi lọc bản ngoài)
  const theoClient = { pc: 0, android: 0, jar: 0, khac: 0, chinhChu: 0, banNgoai: 0 }
  for (const r of dong) {
    const k = (['pc', 'android', 'jar'].includes(String(r.client)) ? r.client : 'khac') as 'pc' | 'android' | 'jar' | 'khac'
    theoClient[k]++
    if (Number(r.chinh_chu) === 1) theoClient.chinhChu++
    else theoClient.banNgoai++
  }
  if (banNgoai && coCotClient) dong = dong.filter(r => Number(r.chinh_chu) !== 1)

  // Gom theo map để hiện số người trên từng map ngay trên đầu, không phải đọc từng dòng.
  const dem = new Map<string, number>()
  for (const r of dong) {
    const ten = String(r.map_ten ?? '') !== '' ? String(r.map_ten) : `(map id ${r.map_id})`
    dem.set(ten, (dem.get(ten) ?? 0) + 1)
  }
  const theoMap = [...dem.entries()].map(([ten, sl]) => ({ ten, sl })).sort((a, b) => b.sl - a.sl)

  return { dong, theoMap, theoClient, coCotClient, bangChuaCo, loiDoc, locTen, locMap, banNgoai }
})
