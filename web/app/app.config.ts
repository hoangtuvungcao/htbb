import { TEN_SITE, TEN_SITE_VIET_TAT } from '#shared/utils/tenSite'

export default defineAppConfig({
  htbb: {
    /**
     * Tên hiển thị của website (tiêu đề tab, đầu/chân trang). Giá trị lấy từ
     * shared/utils/tenSite.ts — nguồn duy nhất, server cũng đọc từ đó (site.name).
     */
    tenSite: TEN_SITE,
    tenVietTat: TEN_SITE_VIET_TAT,
  },
  ui: {
    // Màu định nghĩa ở app/assets/css/main.css (@theme).
    colors: {
      primary: 'pokered',
      secondary: 'pokeblue',
      success: 'grass',
      info: 'pokeblue',
      warning: 'pikachu',
      error: 'red',
      neutral: 'navy',
    },
    button: {
      slots: {
        base: 'font-display font-bold tracking-wide rounded-md',
      },
    },
    // Menu ngang chỉ hiện từ xl (7 mục + tìm kiếm); dưới xl dùng nút mở menu trượt.
    header: {
      slots: {
        toggle: 'xl:hidden',
        content: 'xl:hidden',
        overlay: 'xl:hidden',
      },
    },
    badge: {
      slots: {
        base: 'font-bold rounded-md',
      },
    },
  },
})
