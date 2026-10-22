/**
 * Hàng đợi tải ảnh có GIỚI HẠN SONG SONG + thử lại lùi dần (phần thuần, không DOM — app/composables/useAnhTre.ts nối
 * vào IntersectionObserver + new Image()).
 *
 * Vì sao: lưới Pokédex 60 thẻ / trang từng bắn ~200 yêu cầu /pokemon/anim/*.png cùng lúc; tường lửa (WAF) trước máy
 * chủ trả 429 cho phần lớn, ảnh nền CSS không tự thử lại nên thẻ chỉ còn bóng. Ở đây: tối đa `toiDa` ảnh đang tải,
 * lỗi (429 / mạng) thì chờ `goc × 2^lần` ms (trần `tran`) rồi thử lại, tối đa `thuLai` lần; cùng URL chỉ tải một lần.
 */
export interface TuyChonHangDoi {
  /** Số ảnh tải cùng lúc tối đa (mặc định 6). */
  toiDa?: number
  /** Số lần thử lại sau lần đầu (mặc định 5). */
  thuLai?: number
  /** Chờ trước lần thử lại đầu (ms, mặc định 400), mỗi lần sau gấp đôi. */
  goc?: number
  /** Trần chờ (ms, mặc định 8000). */
  tran?: number
  /** Tải một URL: resolve khi xong, reject khi lỗi (trình duyệt: new Image()). */
  tai: (url: string) => Promise<void>
  /** Ngủ (ms) — thay được trong kiểm thử. */
  ngu?: (ms: number) => Promise<void>
}

export interface HangDoiAnh {
  /** Xếp URL vào hàng; Promise xong khi ảnh đã tải (hoặc reject sau lần thử cuối). */
  tai: (url: string) => Promise<void>
  /** Số đang tải (kiểm thử / gỡ lỗi). */
  dangTai: () => number
  /** Số đang chờ trong hàng. */
  dangCho: () => number
  /** URL đã tải xong (cache). */
  daCo: (url: string) => boolean
}

/** Thời gian chờ trước lần thử thứ `lan` (1..): goc × 2^(lan-1), trần `tran`. */
export function thoiGianCho(lan: number, goc = 400, tran = 8000): number {
  return Math.min(tran, goc * 2 ** Math.max(0, lan - 1))
}

export function taoHangDoiAnh(o: TuyChonHangDoi): HangDoiAnh {
  const toiDa = Math.max(1, o.toiDa ?? 6)
  const thuLai = Math.max(0, o.thuLai ?? 5)
  const goc = o.goc ?? 400
  const tran = o.tran ?? 8000
  const ngu = o.ngu ?? ((ms: number) => new Promise<void>(r => setTimeout(r, ms)))
  const xong = new Set<string>()
  const hua = new Map<string, Promise<void>>()
  const cho: (() => void)[] = []
  let dang = 0

  /** Lấy một chỗ tải (chờ nếu đã đủ `toiDa`). */
  function layCho(): Promise<void> {
    if (dang < toiDa) {
      dang++
      return Promise.resolve()
    }
    return new Promise<void>(r => cho.push(() => { dang++; r() }))
  }
  function traCho() {
    dang--
    const tiep = cho.shift()
    if (tiep) tiep()
  }

  async function taiMot(url: string): Promise<void> {
    for (let lan = 0; ; lan++) {
      await layCho()
      try {
        await o.tai(url)
        traCho()
        xong.add(url)
        return
      } catch (e) {
        traCho()   // không giữ chỗ trong lúc chờ lùi: ảnh khác vẫn tải được
        if (lan >= thuLai) throw e
        await ngu(thoiGianCho(lan + 1, goc, tran))
      }
    }
  }

  return {
    tai(url: string) {
      let p = hua.get(url)
      if (!p) {
        p = taiMot(url)
        hua.set(url, p)
        p.catch(() => hua.delete(url))   // hỏng hẳn: lần xuất hiện sau thử lại từ đầu
      }
      return p
    },
    dangTai: () => dang,
    dangCho: () => cho.length,
    daCo: (url: string) => xong.has(url),
  }
}
