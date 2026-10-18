<template>
  <!-- Trang trí nền nhẹ cho các khối trang: vài Poké Ball mờ (tên giữ nguyên để không phải sửa nơi dùng). -->
  <div class="pointer-events-none absolute inset-0 -z-10 overflow-hidden" aria-hidden="true">
    <PokeBall mono class="absolute -left-24 top-10 size-72 -rotate-12 text-pokeblue-500/[.06] dark:text-white/[.04]" />
    <PokeBall mono class="absolute -right-16 bottom-0 size-56 rotate-12 text-pokered-500/[.07] dark:text-white/[.04]" />
  </div>
</template>
