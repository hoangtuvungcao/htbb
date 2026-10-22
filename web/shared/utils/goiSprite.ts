/**
 * Đọc gói sprite một loài `server/map/loai/<NNNN>.goi` (định dạng HTPK phiên bản 1, ghi bởi
 * tools/pokemon/loai/DongGoiLoai.java bằng DataOutputStream, big-endian):
 *   'H','T','P','K'; u8 phienBan = 1; i16 dex; u8 shiny; u8 n;
 *   n × (writeUTF tenTep [u16 độ dài + UTF-8 biến thể Java], i32 len, len byte)
 * Hàm thuần (không đọc đĩa) để dùng ở script sinh chân dung và test.
 */
export interface GoiSprite { dex: number, shiny: boolean, tep: Map<string, Uint8Array> }

export function docGoiSprite(buf: Uint8Array): GoiSprite {
  const dv = new DataView(buf.buffer, buf.byteOffset, buf.byteLength)
  let p = 0
  const can = (n: number) => {
    if (p + n > buf.length) throw new Error('gói sprite bị cụt')
  }
  can(9)
  if (buf[0] !== 0x48 || buf[1] !== 0x54 || buf[2] !== 0x50 || buf[3] !== 0x4b) throw new Error('sai mã HTPK')
  if (buf[4] !== 1) throw new Error(`phiên bản gói ${buf[4]} không hỗ trợ`)
  const dex = dv.getInt16(5)
  const shiny = buf[7] !== 0
  const n = buf[8]!
  p = 9
  const tep = new Map<string, Uint8Array>()
  const giaiMa = new TextDecoder('utf-8', { fatal: true })
  for (let i = 0; i < n; i++) {
    can(2)
    const dai = dv.getUint16(p)
    p += 2
    can(dai)
    const ten = giaiMa.decode(buf.subarray(p, p + dai))
    p += dai
    can(4)
    const len = dv.getInt32(p)
    p += 4
    if (len < 0) throw new Error('độ dài tệp âm')
    can(len)
    tep.set(ten, buf.subarray(p, p + len))
    p += len
  }
  if (p !== buf.length) throw new Error('thừa byte cuối gói')
  return { dex, shiny, tep }
}

/** Tên tệp chân dung 40×40 trong gói của loài `dex` (vd. "0016_c.png"). */
export function tenChanDung(dex: number): string {
  return `${String(dex).padStart(4, '0')}_c.png`
}
