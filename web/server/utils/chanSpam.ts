/**
 * CHẶN LÀM HÀNG LOẠT (bảng kpah_db.web_chan_spam), đếm theo IP + hành động.
 *
 * Đây mới là chỗ giới hạn số lần: một script cứ gửi form lặp lại bao nhiêu lần
 * cũng được, chỉ bộ đếm theo IP mới chặn được nó.
 *
 * Thiếu bảng thì lùi về đếm trong bộ nhớ của tiến trình Node (không phụ thuộc
 * cookie, nhưng mất khi khởi động lại). Đếm hỏng vì lỗi database thì CHO QUA:
 * thà bỏ lọt còn hơn chặn oan người chơi thật.
 *
 * biChanSpam() chỉ đếm, không sửa gì. Dọn rác nằm ở ghiLanLam() theo hạn giữ cố
 * định 24 giờ.
 */
import type { H3Event } from 'h3'

const HAN_GIU = 86400
const boNho = new Map<string, number[]>()

export async function ghiLanLam(event: H3Event, hanhDong: string): Promise<void> {
  const ip = ipKhach(event)
  if (!(await coBang(dbGame(), 'web_chan_spam'))) {
    const key = `${hanhDong}|${ip}`
    const moc = nowSec() - HAN_GIU
    const ds = (boNho.get(key) ?? []).filter(t => t > moc)
    ds.push(nowSec())
    boNho.set(key, ds)
    // Dọn các IP đã hết hạn giữ để bộ nhớ không phình mãi khi bị quét từ nhiều IP.
    if (boNho.size > 5000) {
      for (const [k, v] of boNho) {
        if (!v.some(t => t > moc)) boNho.delete(k)
      }
    }
    return
  }
  try {
    await exec(dbGame(), 'INSERT INTO web_chan_spam (ip, hanh_dong) VALUES (?, ?)', [ip, hanhDong.slice(0, 30)])
  } catch { /* im lặng */ }
}

/** true = ĐANG BỊ CHẶN */
export async function biChanSpam(event: H3Event, hanhDong: string, soLanToiDa: number, trongGiay: number): Promise<boolean> {
  const ip = ipKhach(event)
  if (!(await coBang(dbGame(), 'web_chan_spam'))) {
    const moc = nowSec() - trongGiay
    const n = (boNho.get(`${hanhDong}|${ip}`) ?? []).filter(t => t > moc).length
    return n >= soLanToiDa
  }
  try {
    // Số giây ghép thẳng bằng số nguyên đã ép kiểu (MySQL không nhận INTERVAL '3600' SECOND).
    const giay = Math.trunc(trongGiay)
    const n = await qv<number>(
      dbGame(),
      `SELECT COUNT(*) FROM web_chan_spam
        WHERE ip = ? AND hanh_dong = ?
          AND thoi_gian > DATE_SUB(NOW(), INTERVAL ${giay} SECOND)`,
      [ip, hanhDong],
    )
    return Number(n ?? 0) >= soLanToiDa
  } catch {
    return false
  }
}
