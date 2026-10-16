import { TEN_SITE, TEN_SITE_VIET_TAT } from '#shared/utils/tenSite'

export default defineAppConfig({
  kpah: {
    /**
     * Tên hiển thị của website (tiêu đề tab, đầu/chân trang). Giá trị lấy từ
     * shared/utils/tenSite.ts — nguồn duy nhất, server cũng đọc từ đó (site.name).
     */
    tenSite: TEN_SITE,
    tenVietTat: TEN_SITE_VIET_TAT,
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
