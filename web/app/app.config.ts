export default defineAppConfig({
  kpah: {
    /**
     * Tên hiển thị của website (tiêu đề tab, đầu/chân trang). Tên tạm cho nhánh
     * pokemon/chuyen-doi; server dùng tên riêng ở server/utils/config.ts (site.name).
     */
    tenSite: 'KPAH Pokémon',
  },
  ui: {
    colors: {
      primary: 'gold',
      secondary: 'jade',
      neutral: 'ink',
    },
    button: {
      slots: {
        base: 'font-semibold tracking-wide',
      },
    },
  },
})
