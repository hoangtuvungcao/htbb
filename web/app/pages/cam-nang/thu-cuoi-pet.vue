<script setup lang="ts">
/** Thú cưỡi & pet — pokedex.json (bang_thu.csv, bang_pet_cu.csv, PetPokemon). */
const L = (dex: number) => loaiTheoDex(dex)
const thuCuoi = POKEDEX.loai.filter(l => l.vaiTro.includes('thu-cuoi'))
const chuoiPet = POKEDEX.loai
  .filter(l => l.tienHoa?.kieu === 'pet' && l.tienHoa.buoc[0]?.dex === l.dex)
  .map(l => l.tienHoa!.buoc.map(b => ({ ...b, loai: L(b.dex) })))
const petKhac = POKEDEX.loai.filter(l => l.vaiTro.includes('pet') && l.tienHoa?.kieu !== 'pet')
</script>

<template>
  <WikiShell slug="thu-cuoi-pet">
    <WikiSection id="thu-cuoi" title="Thú cưỡi" :lead="`${thuCuoi.length} loài Pokémon cưỡi được: thú cưỡi cá nhân, thú cưỡi của đội và của vùng. Cưỡi giúp di chuyển nhanh; đồ thú cưỡi (yên, giáp) cộng thêm chỉ số. Bấm phím * → Xuống thú để xuống.`">
      <ul class="grid grid-cols-2 gap-3 sm:grid-cols-4 lg:grid-cols-6">
        <li v-for="t in thuCuoi" :key="t.dex">
          <NuxtLink :to="`/pokedex/${t.dex}`" class="group block overflow-hidden rounded-2xl glass text-center transition hover:-translate-y-0.5">
            <PokeSan :dex="t.dex" :he="t.he" :size="100" class="w-full" />
            <span class="block py-2 text-sm font-bold text-highlighted group-hover:text-primary">{{ t.ten }}</span>
          </NuxtLink>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="pet" title="Pet" lead="Pet là Pokémon nhỏ đi theo bạn, cộng chỉ số phụ. Pet nở từ trứng Pokémon, nâng cấp pet bằng nguyên liệu và tiến hoá dần theo cấp pet.">
      <ul class="grid gap-3 md:grid-cols-2">
        <li v-for="(c, i) in chuoiPet" :key="i" class="flex flex-wrap items-center gap-2 rounded-2xl glass p-3">
          <template v-for="(b, j) in c" :key="b.dex">
            <UIcon v-if="j > 0" name="i-lucide-chevron-right" class="size-4 text-dimmed" aria-hidden="true" />
            <NuxtLink :to="`/pokedex/${b.dex}`" class="flex items-center gap-2 text-sm hover:text-primary">
              <PokeAvatar :dex="b.dex" class="size-9" />
              <span>
                <span class="font-semibold text-highlighted">{{ b.loai?.ten }}</span>
                <span class="block text-[11px] text-muted">{{ b.dieuKien }}</span>
              </span>
            </NuxtLink>
          </template>
        </li>
      </ul>
      <h3 class="mt-8 font-display text-xl font-bold text-highlighted">
        Pet khác ({{ petKhac.length }} loài)
      </h3>
      <ul class="mt-3 flex flex-wrap gap-1.5">
        <li v-for="p in petKhac" :key="p.dex">
          <NuxtLink :to="`/pokedex/${p.dex}`" class="flex items-center gap-1.5 rounded-full bg-elevated py-1 pl-1 pr-3 text-xs font-semibold hover:text-primary">
            <PokeAvatar :dex="p.dex" class="size-6" /> {{ p.ten }}
          </NuxtLink>
        </li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
