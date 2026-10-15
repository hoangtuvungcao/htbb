/**
 * Người chơi dùng client nào (PC / Android / jar).
 *
 * Hai bảng, đều do server game ghi (repo KPAH):
 *   tob_client_dang_nhap — data/GhiClient.java: mỗi lần ĐĂNG NHẬP THÀNH CÔNG cập nhật một dòng cho
 *     (tài khoản, loại client): số lần, lần đầu, lần cuối, tên máy (platf), phiên bản, số bản jar.
 *   tob_log_online       — data/GhiSoOnline.java: 5 phút một mẫu, có thêm cột pc / android / jar / khac
 *     (số nhân vật đang trong game theo loại client).
 * Loại client lấy theo byte firmware trong gói đăng nhập (jar 0, Android 1, PC 2/4) — PC và Android
 * đều gửi platf "nokia/1/1" nên không dùng platf để phân loại.
 *
 * Bản chính thức hay bản ngoài: cột chinh_chu (1 = client NQSH-H2 của mình — gửi mã máy lúc đăng nhập;
 * 0 = client gốc TeaMobi / client lạ). Danh sách banNgoai để admin soát và khoá tài khoản.
 *
 * Tài khoản bot thử tải (tbot001…, user123…, kiemtrabot) bị ẩn khi ?anBot=1 (mặc định).
 */
const BANG_CLIENT = 'tob_client_dang_nhap'
const LOAI = ['pc', 'android', 'jar', 'khac'] as const
type Loai = typeof LOAI[number]
const LA_BOT = /^(tbot\d+|user\d+|kiemtrabot)$/i

interface DongClient {
  tai_khoan: string
  loai: Loai
  platf: string
  phien_ban: string
  ban_jar: number
  chinh_chu: number
  so_lan: number
  lan_dau: string
  lan_cuoi: string
  phut_truoc: number
}

export default defineAdminHandler(async (event) => {
  const anBot = str(getQuery(event).anBot, '1') !== '0'
  const game = dbGame()

  // ---- Đang online theo loại (mẫu mới nhất của tob_log_online) ----
  let online: Record<Loai, number> & { thoi_gian: string, phut_truoc: number } | null = null
  let dinh24h: Record<Loai, number> | null = null
  let onlineChuaCoCot = false
  try {
    const r = await q1<any>(game, `SELECT thoi_gian, pc, android, jar, khac,
        TIMESTAMPDIFF(MINUTE, thoi_gian, NOW()) AS phut_truoc FROM tob_log_online ORDER BY id DESC LIMIT 1`)
    if (r) {
      online = {
        thoi_gian: String(r.thoi_gian), phut_truoc: Number(r.phut_truoc),
        pc: Number(r.pc), android: Number(r.android), jar: Number(r.jar), khac: Number(r.khac),
      }
    }
    const d = await q1<any>(game, `SELECT MAX(pc) AS pc, MAX(android) AS android, MAX(jar) AS jar, MAX(khac) AS khac
        FROM tob_log_online WHERE thoi_gian >= DATE_SUB(NOW(), INTERVAL 24 HOUR)`)
    if (d) {
      dinh24h = { pc: Number(d.pc ?? 0), android: Number(d.android ?? 0), jar: Number(d.jar ?? 0), khac: Number(d.khac ?? 0) }
    }
  } catch {
    // Bảng có từ trước nhưng chưa có cột pc/android/jar: server game chưa chạy bản mới
    onlineChuaCoCot = true
  }

  // ---- Tài khoản theo loại client ----
  let dong: DongClient[] = []
  let bangChuaCo = false
  let loiDoc = ''
  try {
    dong = await q<DongClient>(game, `SELECT tai_khoan, loai, platf, phien_ban, ban_jar, chinh_chu, so_lan, lan_dau, lan_cuoi,
        TIMESTAMPDIFF(MINUTE, lan_cuoi, NOW()) AS phut_truoc FROM \`${BANG_CLIENT}\` ORDER BY lan_cuoi DESC`)
  } catch (e: any) {
    loiDoc = String(e?.message ?? e)
    bangChuaCo = e?.code === 'ER_NO_SUCH_TABLE' || /exist/i.test(loiDoc)
  }
  const soBotAn = anBot ? dong.filter(r => LA_BOT.test(r.tai_khoan)).length : 0
  if (anBot) dong = dong.filter(r => !LA_BOT.test(r.tai_khoan))

  // Số tài khoản KHÁC NHAU đã vào bằng từng loại trong 1 / 7 / 30 ngày và từ trước tới nay.
  // Một tài khoản dùng cả PC lẫn Android được đếm ở cả hai cột.
  const MOC = [{ key: 'ngay1', phut: 24 * 60 }, { key: 'ngay7', phut: 7 * 24 * 60 }, { key: 'ngay30', phut: 30 * 24 * 60 }]
  const theoLoai = LOAI.map((loai) => {
    const ds = dong.filter(r => r.loai === loai)
    const kq: Record<string, number | string> = { loai, tatCa: ds.length, banNgoai: ds.filter(r => Number(r.chinh_chu) !== 1).length }
    for (const m of MOC) kq[m.key] = ds.filter(r => Number(r.phut_truoc) <= m.phut).length
    return kq as { loai: Loai, tatCa: number, banNgoai: number, ngay1: number, ngay7: number, ngay30: number }
  })
  const tongTaiKhoan = new Set(dong.map(r => r.tai_khoan)).size

  // jar: tên máy + số bản đang dùng (30 ngày) — biết còn bao nhiêu người chưa lên bản mới
  const dem = (ds: string[]) => {
    const m = new Map<string, number>()
    for (const k of ds) m.set(k, (m.get(k) ?? 0) + 1)
    return [...m.entries()].map(([ten, sl]) => ({ ten, sl })).sort((a, b) => b.sl - a.sl)
  }
  const jar30 = dong.filter(r => r.loai === 'jar' && Number(r.phut_truoc) <= 30 * 24 * 60)
  const jarTheoMay = dem(jar30.map(r => r.platf || '(không rõ)'))
  const jarTheoBan = dem(jar30.map(r => (Number(r.ban_jar) > 0 ? `v${r.ban_jar}` : 'bản cũ (không gửi số bản)')))

  // Tài khoản từng vào bằng client KHÔNG phải bản của mình (mới nhất trước)
  const banNgoai = dong.filter(r => Number(r.chinh_chu) !== 1)

  return {
    online, dinh24h, onlineChuaCoCot, banNgoai,
    theoLoai, tongTaiKhoan, jarTheoMay, jarTheoBan,
    ganDay: dong.slice(0, 200),
    anBot, soBotAn, bangChuaCo, loiDoc,
  }
})
