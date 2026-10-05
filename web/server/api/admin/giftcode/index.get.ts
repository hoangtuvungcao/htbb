/**
 * Danh sách mã giftcode + số lần đã phát mỗi mã + bản dịch phần thưởng.
 *
 * Bốn điều server game làm, quyết định mọi thứ trên trang này (DatabaseGiftCode.java:301-600):
 *   1. Câu tìm mã là `select * from giftcode where giftcode='...' and type = 0`
 *      -> type khác 0 là mã vô hình với người chơi.
 *   2. `limit_use` là số lượt CÒN LẠI. Bằng 0 thì báo hết lượt. Chỉ trừ khi `limit >= 1`,
 *      nên số âm là không giới hạn thật sự (không trừ tiếp).
 *   3. Chặn nhận lại tính theo TÀI KHOẢN (giftcode_log.userId, sửa 09/09/2026) — một tài
 *      khoản chỉ nhận một mã đúng một lần dù có bao nhiêu nhân vật.
 *   4. Cột `expire` chưa bao giờ được đọc. Muốn hết hạn thì tự đặt limit_use = 0.
 */
export default defineAdminHandler(async () => {
  const game = dbGame()
  const dsMa = await q(game, 'SELECT id, giftcode, xu, luong, luongLock, item, limit_use, type FROM giftcode ORDER BY id DESC')
  const demNhan: Record<string, number> = {}
  for (const r of await q(game, 'SELECT giftcode, COUNT(*) AS n FROM giftcode_log GROUP BY giftcode')) {
    demNhan[String(r.giftcode ?? '').toLowerCase()] = Number(r.n)
  }
  // Không bao giờ ném lỗi ra bảng: một mã cũ cú pháp lạ chỉ hiện "? ..." ở đúng chỗ của nó.
  const moTa = await gcMoTaNhieu(dsMa.map(r => String(r.item ?? '')))
  return {
    dsMa: dsMa.map((r, i) => ({
      id: Number(r.id),
      giftcode: String(r.giftcode ?? ''),
      xu: Number(r.xu ?? 0),
      luong: Number(r.luong ?? 0),
      luongLock: Number(r.luongLock ?? 0),
      item: String(r.item ?? ''),
      limit_use: Number(r.limit_use ?? 0),
      type: Number(r.type ?? 0),
      daPhat: demNhan[String(r.giftcode ?? '').toLowerCase()] ?? 0,
      moTa: moTa[i] ?? [],
    })),
  }
})
