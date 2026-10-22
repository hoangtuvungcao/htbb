#!/usr/bin/env node
/**
 * Sinh chân dung 40×40 cho Pokédex đầy đủ (trang /pokedex, /pokedex/[dex]).
 *
 *   node scripts/sinh-chan-dung-loai.mjs        (chạy ở web/, Node 22)
 *
 * Nguồn (không tải mạng):
 *  1. gói sprite tải theo nhu cầu ../server/map/loai/<NNNN>.goi (HTPK v1, tools/pokemon/loai/DongGoiLoai.java):
 *     lấy tệp <NNNN>_c.png -> public/pokemon/loai/<dex>.png;
 *  2. loài khác: chân dung đã có public/pokemon/<dex>.png (sinh bởi sinh-du-lieu-pokemon.mjs).
 * Ra shared/data/chan-dung-loai.json: { "<dex>": "<url>" } — loài không có chân dung không có khoá (trang vẽ bóng).
 * Sprite / chân dung: PMDCollab SpriteCollab, CC BY-NC 4.0 (ghi công: server/map/loai/GHI_CONG.txt, /giay-phep).
 */
import { readdirSync, readFileSync, writeFileSync, mkdirSync, existsSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import { docGoiSprite, tenChanDung } from '../shared/utils/goiSprite.ts'

const WEB = join(dirname(fileURLToPath(import.meta.url)), '..')
const GOI = join(WEB, '..', 'server', 'map', 'loai')
const RA = join(WEB, 'public', 'pokemon', 'loai')
const CU = join(WEB, 'public', 'pokemon')
const JSON_RA = join(WEB, 'shared', 'data', 'chan-dung-loai.json')

mkdirSync(RA, { recursive: true })
const bang = {}
let tuGoi = 0
let loi = 0
if (existsSync(GOI)) {
  for (const f of readdirSync(GOI).filter(x => /^\d{4}\.goi$/.test(x)).sort()) {
    try {
      const g = docGoiSprite(new Uint8Array(readFileSync(join(GOI, f))))
      const png = g.tep.get(tenChanDung(g.dex))
      if (!png || png[0] !== 0x89 || png[1] !== 0x50) throw new Error('không có chân dung PNG')
      writeFileSync(join(RA, `${g.dex}.png`), png)
      bang[g.dex] = `/pokemon/loai/${g.dex}.png`
      tuGoi++
    }
    catch (e) {
      loi++
      console.error(`LOI ${f}: ${e.message}`)
    }
  }
}
let tuCu = 0
for (const f of readdirSync(CU)) {
  const m = /^(\d+)\.png$/.exec(f)
  if (!m) continue
  const dex = Number(m[1])
  if (dex >= 1 && dex <= 1025 && !bang[dex]) {
    bang[dex] = `/pokemon/${dex}.png`
    tuCu++
  }
}
const sx = Object.fromEntries(Object.entries(bang).sort((a, b) => Number(a[0]) - Number(b[0])))
writeFileSync(JSON_RA, JSON.stringify(sx) + '\n')
console.log(`Chân dung: ${tuGoi} từ gói máy chủ, ${tuCu} có sẵn, tổng ${Object.keys(sx).length} loài -> shared/data/chan-dung-loai.json`)
process.exit(loi ? 1 : 0)
