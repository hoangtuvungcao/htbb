/** Hàng đợi tải ảnh Pokédex (shared/utils/hangDoiAnh.ts): giới hạn song song, thử lại lùi dần, gộp cùng URL. npm test. */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { taoHangDoiAnh, thoiGianCho } from '../shared/utils/hangDoiAnh.ts'

const tick = () => new Promise<void>(r => setTimeout(r, 1))

test('không quá 6 ảnh tải cùng lúc với 200 ảnh xếp hàng', async () => {
  let dang = 0
  let cao = 0
  const q = taoHangDoiAnh({
    toiDa: 6,
    tai: async () => {
      dang++
      cao = Math.max(cao, dang)
      await tick()
      dang--
    },
  })
  await Promise.all(Array.from({ length: 200 }, (_, i) => q.tai(`/pokemon/anim/${i}-idle.png`)))
  assert.equal(cao, 6)
  assert.equal(q.dangTai(), 0)
  assert.equal(q.dangCho(), 0)
  assert.ok(q.daCo('/pokemon/anim/199-idle.png'))
})

test('máy chủ trả 429 khi > 6 yêu cầu song song: mọi ảnh vẫn tải xong (không vượt ngưỡng)', async () => {
  let dang = 0
  let bi429 = 0
  const q = taoHangDoiAnh({
    toiDa: 6,
    ngu: async () => {},
    tai: async () => {
      dang++
      try {
        if (dang > 6) {
          bi429++
          throw new Error('429')
        }
        await tick()
      } finally {
        dang--
      }
    },
  })
  await Promise.all(Array.from({ length: 120 }, (_, i) => q.tai(`/a/${i}.png`)))
  assert.equal(bi429, 0)
})

test('lỗi tạm (429) thì thử lại theo lùi dần, lỗi mãi thì bỏ sau thuLai lần', async () => {
  const cho: number[] = []
  let lan = 0
  const q = taoHangDoiAnh({
    thuLai: 3,
    goc: 400,
    ngu: async (ms) => { cho.push(ms) },
    tai: async () => {
      lan++
      if (lan < 3) throw new Error('429')
    },
  })
  await q.tai('/x.png')
  assert.equal(lan, 3)
  assert.deepEqual(cho, [400, 800])
  const hong = taoHangDoiAnh({ thuLai: 2, ngu: async () => {}, tai: async () => { throw new Error('404') } })
  await assert.rejects(hong.tai('/y.png'))
  assert.equal(hong.dangTai(), 0)
  assert.equal(thoiGianCho(1), 400)
  assert.equal(thoiGianCho(4), 3200)
  assert.equal(thoiGianCho(10), 8000)
})

test('cùng URL chỉ tải một lần', async () => {
  let n = 0
  const q = taoHangDoiAnh({ tai: async () => { n++; await tick() } })
  await Promise.all([q.tai('/z.png'), q.tai('/z.png'), q.tai('/z.png')])
  await q.tai('/z.png')
  assert.equal(n, 1)
})

test('lưới Pokédex + sprite dùng hàng đợi trễ (không gắn ảnh nền trước khi tải xong)', async () => {
  const { readFileSync } = await import('node:fs')
  const sprite = readFileSync(new URL('../app/components/poke/Sprite.vue', import.meta.url), 'utf8')
  assert.ok(sprite.includes('useAnhTre(el'), 'PokeSprite dùng useAnhTre')
  assert.ok(sprite.includes('backgroundImage: daTai.value ?'), 'ảnh nền chỉ gắn khi đã tải')
  const avatar = readFileSync(new URL('../app/components/poke/Avatar.vue', import.meta.url), 'utf8')
  assert.ok(avatar.includes('v-if="anh && daTai"'), 'PokeAvatar chỉ gắn src khi đã tải')
  const dex = readFileSync(new URL('../app/pages/pokedex/index.vue', import.meta.url), 'utf8')
  assert.ok(dex.includes('const MOI_TRANG = 60') && dex.includes(' tinh class="w-full"'), 'Pokédex 60 thẻ / trang, khung đầu đứng yên')
  const c = readFileSync(new URL('../app/composables/useAnhTre.ts', import.meta.url), 'utf8')
  assert.ok(c.includes('IntersectionObserver') && c.includes('toiDa: 6'), 'tải khi vào khung nhìn, 6 song song')
})
