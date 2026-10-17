#!/usr/bin/env node
/**
 * Cắt dải hoạt ảnh (đứng + đi, hướng DOWN) của 5 Pokémon khởi đầu cho hero trang chủ.
 *
 *   KPAH_DIR=../KPAH node scripts/sinh-anim-khoi-dau.mjs
 *
 * Đọc (chỉ đọc): ClientUnity/Assets/Resources/poke/<dddd>_anh.bytes (PNG atlas RGBA)
 *               + <dddd>_dl.bytes (dữ liệu khung, định dạng ở KE_HOACH_CHUYEN_DOI.md §7.1).
 * Ghi: public/pokemon/anim/<dex>-<dongTac>.png (một hàng khung nằm ngang)
 *      public/pokemon/anim/<dex>.json        ({ idle|walk: { w, h, khung, thoiLuong[] (1/60 s) } })
 *
 * Không thêm thư viện: tự giải/nén PNG bằng node:zlib (Node 22).
 */
import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { deflateSync, inflateSync } from 'node:zlib'

const WEB = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const KPAH = resolve(WEB, process.env.KPAH_DIR ?? '../KPAH')
const POKE = join(KPAH, 'ClientUnity/Assets/Resources/poke')
const OUT = join(WEB, 'public/pokemon/anim')

/** Charmander, Bulbasaur, Eevee, Squirtle, Pichu (DEX_KHOI_DAU). */
const KHOI_DAU = [4, 1, 133, 7, 172]
/** Động tác cần lấy: chỉ số trong _dl.bytes (0 dung, 1 chay). */
const DONG_TAC = { idle: 0, walk: 1 }
/** Hướng DOWN = hàng 0 trong atlas. */
const HUONG = 0

if (!existsSync(POKE)) {
  console.error(`Không thấy ${POKE} (đặt KPAH_DIR).`)
  process.exit(1)
}
mkdirSync(OUT, { recursive: true })

// ---------------------------------------------------------------- PNG

function docPng(buf) {
  if (buf.readUInt32BE(0) !== 0x89504E47) throw new Error('Không phải PNG')
  let p = 8
  let w = 0, h = 0, kieu = 0, bit = 0, xenKe = 0
  const idat = []
  while (p < buf.length) {
    const len = buf.readUInt32BE(p)
    const loai = buf.toString('ascii', p + 4, p + 8)
    const data = buf.subarray(p + 8, p + 8 + len)
    if (loai === 'IHDR') {
      w = data.readUInt32BE(0); h = data.readUInt32BE(4); bit = data[8]; kieu = data[9]; xenKe = data[12]
    } else if (loai === 'IDAT') idat.push(data)
    else if (loai === 'IEND') break
    p += 12 + len
  }
  if (kieu !== 6 || bit !== 8 || xenKe) throw new Error(`Chỉ hỗ trợ RGBA 8-bit không xen kẽ (kiểu ${kieu}, ${bit} bit)`)
  const raw = inflateSync(Buffer.concat(idat))
  const bpp = 4, stride = w * bpp
  const px = Buffer.alloc(h * stride)
  for (let y = 0; y < h; y++) {
    const f = raw[y * (stride + 1)]
    const src = raw.subarray(y * (stride + 1) + 1, (y + 1) * (stride + 1))
    const o = y * stride
    for (let x = 0; x < stride; x++) {
      const a = x >= bpp ? px[o + x - bpp] : 0
      const b = y ? px[o - stride + x] : 0
      const c = x >= bpp && y ? px[o - stride + x - bpp] : 0
      let v = src[x]
      if (f === 1) v += a
      else if (f === 2) v += b
      else if (f === 3) v += (a + b) >> 1
      else if (f === 4) {
        const pp = a + b - c, pa = Math.abs(pp - a), pb = Math.abs(pp - b), pc = Math.abs(pp - c)
        v += pa <= pb && pa <= pc ? a : pb <= pc ? b : c
      }
      px[o + x] = v & 255
    }
  }
  return { w, h, px }
}

const CRC = new Int32Array(256).map((_, n) => {
  let c = n
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1
  return c
})
function crc32(b) {
  let c = -1
  for (const x of b) c = CRC[(c ^ x) & 255] ^ (c >>> 8)
  return (c ^ -1) >>> 0
}
function khoi(loai, data) {
  const len = Buffer.alloc(4); len.writeUInt32BE(data.length)
  const td = Buffer.concat([Buffer.from(loai, 'ascii'), data])
  const crc = Buffer.alloc(4); crc.writeUInt32BE(crc32(td))
  return Buffer.concat([len, td, crc])
}
function ghiPng(w, h, px) {
  const raw = Buffer.alloc(h * (w * 4 + 1))
  for (let y = 0; y < h; y++) px.copy(raw, y * (w * 4 + 1) + 1, y * w * 4, (y + 1) * w * 4)
  const ihdr = Buffer.alloc(13)
  ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4); ihdr[8] = 8; ihdr[9] = 6
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A]),
    khoi('IHDR', ihdr), khoi('IDAT', deflateSync(raw, { level: 9 })), khoi('IEND', Buffer.alloc(0)),
  ])
}

// ---------------------------------------------------------------- dữ liệu khung

function docDl(buf) {
  let p = 0
  const u8 = () => buf[p++]
  const u16 = () => { const v = buf.readUInt16BE(p); p += 2; return v }
  const phienBan = u8()
  const so = u8()
  const ds = []
  for (let i = 0; i < so; i++) {
    const w = u16(), h = u16(), khung = u8()
    const thoiLuong = Array.from({ length: khung }, u8)
    const yAtlas = u16()
    const xAtlas = phienBan >= 2 ? u16() : 0
    p += 8 // dxChan[4], dyChan[4]
    ds.push({ w, h, khung, thoiLuong, xAtlas, yAtlas })
  }
  return ds
}

// ---------------------------------------------------------------- chạy

let ghi = 0
for (const dex of KHOI_DAU) {
  const ma = String(dex).padStart(4, '0')
  const atlas = docPng(readFileSync(join(POKE, `${ma}_anh.bytes`)))
  const dl = docDl(readFileSync(join(POKE, `${ma}_dl.bytes`)))
  const json = {}
  for (const [ten, i] of Object.entries(DONG_TAC)) {
    const d = dl[i]
    const W = d.w * d.khung, H = d.h
    const px = Buffer.alloc(W * H * 4)
    const y0 = d.yAtlas + HUONG * d.h
    for (let y = 0; y < H; y++) {
      const tu = ((y0 + y) * atlas.w + d.xAtlas) * 4
      atlas.px.copy(px, y * W * 4, tu, tu + W * 4)
    }
    const png = ghiPng(W, H, px)
    const dich = join(OUT, `${dex}-${ten}.png`)
    if (!existsSync(dich) || !readFileSync(dich).equals(png)) { writeFileSync(dich, png); ghi++ }
    json[ten] = { w: d.w, h: d.h, khung: d.khung, thoiLuong: d.thoiLuong }
  }
  const dichJson = join(OUT, `${dex}.json`)
  const noiDung = `${JSON.stringify(json)}\n`
  if (!existsSync(dichJson) || readFileSync(dichJson, 'utf8') !== noiDung) { writeFileSync(dichJson, noiDung); ghi++ }
  console.log(`#${dex}: idle ${json.idle.w}×${json.idle.h}×${json.idle.khung}, walk ${json.walk.w}×${json.walk.h}×${json.walk.khung}`)
}
console.log(`Xong. Ghi ${ghi} file vào ${OUT}`)
