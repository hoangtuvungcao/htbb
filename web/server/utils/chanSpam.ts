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
 *
 * GỬI SONG SONG: trước đây đếm-rồi-mới-ghi nên 500 request cùng lúc đều qua bước đếm trước khi lần sai đầu
 * tiên kịp ghi (dò mật khẩu / tạo tài khoản vượt hạn mức). Giờ mỗi lần gọi biChanSpam GIỮ CHỖ trong bộ nhớ
 * GIU_CHO_MS (Node một tiến trình, phép tăng là nguyên tử): các lượt đang chạy cũng tính vào hạn mức.
 */
import type { H3Event } from 'h3'

const HAN_GIU = 86400
const boNho = new Map<string, number[]>()

/** Lượt đang chạy (đã qua kiểm tra, chưa chắc đã ghi) theo khoá, tự trả sau GIU_CHO_MS. */
const GIU_CHO_MS = 10_000
const dangChay = new Map<string, number>()

/** Giữ một chỗ cho khoá, trả số lượt đang chạy TRƯỚC lượt này. Đồng bộ (không await) nên không đua được. */
export function giuCho(khoa: string): number {
  const truoc = dangChay.get(khoa) ?? 0
  dangChay.set(khoa, truoc + 1)
  setTimeout(() => {
    const n = (dangChay.get(khoa) ?? 1) - 1
    if (n <= 0) dangChay.delete(khoa)
    else dangChay.set(khoa, n)
  }, GIU_CHO_MS).unref?.()
  return truoc
}

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
  // Giữ chỗ TRƯỚC mọi await (xem đầu file)
  const dangChayTruoc = giuCho(`${hanhDong}|${ip}`)
  if (dangChayTruoc >= soLanToiDa) return true
  if (!(await coBang(dbGame(), 'web_chan_spam'))) {
    const moc = nowSec() - trongGiay
    const n = (boNho.get(`${hanhDong}|${ip}`) ?? []).filter(t => t > moc).length
    return n + dangChayTruoc >= soLanToiDa
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
    return Number(n ?? 0) + dangChayTruoc >= soLanToiDa
  } catch {
    // Lỗi DB: vẫn còn chặn theo số lượt đang chạy ở trên
    return false
  }
}
