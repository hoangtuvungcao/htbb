/**
 * Nội dung tin tức dùng chung cho danh sách và trang chi tiết.
 *
 * Đây là NGUỒN GỐC của tin tức: thêm/sửa bài trực tiếp trong file này.
 *
 * Khoá của object là slug trên đường dẫn /news/<slug> — chỉ dùng chữ thường, số và
 * gạch ngang, và đừng đổi slug của bài đã đăng (link cũ sẽ thành 404).
 * Thứ tự khoá = thứ tự hiển thị (mới nhất trước).
 */

import { TEN_SITE } from './tenSite'

export interface NewsSection {
  title: string
  paragraphs?: string[]
  steps?: string[]
}

export interface NewsArticle {
  date: string
  category: string
  title: string
  body: string
  sections: NewsSection[]
  notice: string
}

export const NEWS: Record<string, NewsArticle> = {
  "khai-mo-may-chu-moi": {
    "date": "20/07",
    "category": "Thông báo",
    "title": "Khai mở máy chủ mới",
    "body": `Chào mừng bạn đến với thế giới ${TEN_SITE}, nơi chính bạn là một Pokémon. Chuẩn bị tài khoản và bắt đầu chuyến phiêu lưu cùng bạn bè.`,
    "sections": [
      {
        "title": "Một hành trình mới đang chờ",
        "paragraphs": [
          `Ở ${TEN_SITE}, bạn không huấn luyện Pokémon mà hoá thân thành một Pokémon: lên cấp, tiến hoá, gặp gỡ bạn bè và khám phá vùng đất mới. Hãy chuẩn bị tài khoản và phiên bản game phù hợp trước khi bắt đầu.`
        ]
      },
      {
        "title": "Chuẩn bị trước khi vào game",
        "steps": [
          "Tạo tài khoản tại trang Đăng ký và ghi nhớ thông tin đăng nhập.",
          "Mở trang Tải game, chọn phiên bản phù hợp với thiết bị của bạn.",
          "Sau khi cài đặt, đăng nhập và làm theo hướng dẫn trong game để chọn Pokémon khởi đầu."
        ]
      },
      {
        "title": "Chọn Pokémon khởi đầu",
        "paragraphs": [
          "Năm Pokémon khởi đầu gồm Charmander (Lửa), Bulbasaur (Cỏ/Độc), Squirtle (Nước), Pichu (Điện) và Eevee (Thường). Mỗi Pokémon tiến hoá theo cấp; riêng Eevee được tự chọn 1 trong 8 hướng tiến hoá ở cấp 25."
        ]
      }
    ],
    "notice": "Lịch khai mở cụ thể và chính sách quà tân thủ chưa được cập nhật trong bài viết này. Hãy kiểm tra thông báo từ ban quản trị trước khi tham gia."
  },
  "su-kien-he-ruc-lua": {
    "date": "18/07",
    "category": "Sự kiện",
    "title": "Sự kiện Hè rực lửa",
    "body": "Theo dõi thông báo sự kiện và hướng dẫn tham gia trong game.",
    "sections": [
      {
        "title": "Thông tin sự kiện",
        "paragraphs": [
          "Đây là trang theo dõi thông tin cho sự kiện Hè rực lửa. Thời gian tổ chức, điều kiện tham gia và danh sách phần thưởng chưa được cập nhật."
        ]
      },
      {
        "title": "Trước khi tham gia",
        "steps": [
          "Đọc thông báo mới nhất trong game và từ ban quản trị.",
          "Kiểm tra điều kiện tham gia, thời hạn và cách nhận thưởng khi thể lệ được công bố.",
          "Nếu có nội dung chưa rõ, truy cập trang Tải game để tìm kênh hỗ trợ cộng đồng."
        ]
      }
    ],
    "notice": "Chưa có thể lệ chi tiết được xác nhận trên website. Không sử dụng bài viết này làm căn cứ về mức nạp hoặc phần thưởng."
  },
  "huong-dan-tan-thu": {
    "date": "15/07",
    "category": "Hướng dẫn",
    "title": "Hướng dẫn tân thủ",
    "body": "Từ tạo tài khoản đến chọn Pokémon khởi đầu: những bước đầu tiên trong thế giới Pokémon.",
    "sections": [
      {
        "title": "1. Tạo tài khoản",
        "paragraphs": [
          "Mở trang Đăng ký, điền tên tài khoản, mật khẩu và xác nhận lại mật khẩu. Thực hiện theo yêu cầu hiển thị ngay bên dưới các ô nhập.",
          "Sau khi đăng ký, đọc thông báo kết quả để biết tài khoản đã được kích hoạt hay cần liên hệ quản trị viên."
        ]
      },
      {
        "title": "2. Tải và cài đặt game",
        "paragraphs": [
          "Mở trang Tải game và chọn nền tảng phù hợp với thiết bị. Nếu phiên bản hiện “Đang cập nhật bản tải”, hãy chờ đường dẫn được bổ sung hoặc xem kênh hỗ trợ.",
          "Làm theo hướng dẫn đi kèm bản tải để hoàn tất cài đặt."
        ]
      },
      {
        "title": "3. Bắt đầu hành trình",
        "paragraphs": [
          "Đăng nhập vào game bằng tài khoản vừa tạo. Chọn một trong năm Pokémon khởi đầu: Charmander, Bulbasaur, Squirtle, Pichu hoặc Eevee.",
          "Làm quen với các hướng dẫn trong game, khám phá bản đồ và kết nối với những người chơi khác."
        ]
      },
      {
        "title": "4. Quản lý tài khoản",
        "paragraphs": [
          "Đăng nhập website để xem thông tin tài khoản và Pokémon của bạn. Khi dùng máy tính chung, hãy mở trang Đăng xuất và xác nhận để kết thúc phiên đăng nhập trên website."
        ]
      }
    ],
    "notice": "Không chia sẻ mật khẩu với người khác. Nếu cần hỗ trợ, mô tả lỗi bạn gặp mà không gửi thông tin đăng nhập."
  }
}

/** Danh sách [slug, bài] theo đúng thứ tự trong nguồn. */
export function newsList(): Array<[string, NewsArticle]> {
  return Object.entries(NEWS)
}

/** Bài theo slug, hoặc null (trang chi tiết hiện 404). */
export function newsBySlug(slug: unknown): NewsArticle | null {
  return typeof slug === 'string' && Object.prototype.hasOwnProperty.call(NEWS, slug) ? NEWS[slug]! : null
}
