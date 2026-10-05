<template>
  <div class="relative min-h-dvh bg-pattern text-default">
    <a
      href="#main-content"
      class="sr-only focus:not-sr-only focus:fixed focus:left-4 focus:top-4 focus:z-[100] focus:rounded-md focus:bg-primary focus:px-4 focus:py-2 focus:text-inverted"
    >Đến nội dung chính</a>
    <SiteHeader />
    <main id="main-content" tabindex="-1" class="relative outline-none">
      <slot />
    </main>
    <SiteFooter />
  </div>
</template>
