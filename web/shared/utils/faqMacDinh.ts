/**
 * FAQ tĩnh (dùng khi bảng web_faq trống; admin bấm "Chép FAQ mặc định" để đưa vào CSDL rồi sửa).
 * Thẻ {nen_tang} / {nen_tang_sap} được trang FAQ thay bằng danh sách nền tảng lấy từ /admin/tai-game
 * (useBanTai) — admin bật iOS là câu trả lời tự đổi, kể cả câu đã chép vào CSDL.
 */
import { TEN_SITE } from './tenSite'
import raw from '../data/cam-nang.json'

const C = raw as { capDo: { capToiDa: number, tranCap: { cap: number }[], nangLuong: { phutNgay: number } } }

export const FAQ_MAC_DINH: { ten: string, hoi: { label: string, content: string }[] }[] = [
  {
    ten: 'Tài khoản & cài đặt',
    hoi: [
      { label: 'Chơi trên thiết bị nào?', content: 'Hiện có: {nen_tang}.{nen_tang_sap} Xem trang Tải game để chọn bản phù hợp và yêu cầu cấu hình.' },
      { label: 'Một tài khoản dùng được trên nhiều thiết bị không?', content: 'Có. Dữ liệu lưu trên máy chủ, bạn đăng nhập cùng một tài khoản trên mọi bản ({nen_tang}).' },
      { label: 'Quên mật khẩu thì làm sao?', content: 'Dùng trang "Quên mật khẩu" trên website nếu tài khoản có email. Ban quản trị không bao giờ hỏi mật khẩu của bạn.' },
      { label: 'Tài khoản có cần kích hoạt không?', content: 'Tuỳ cấu hình máy chủ. Nếu cần, đăng nhập website và vào trang Kích hoạt.' },
      { label: 'Liên hệ hỗ trợ ở đâu?', content: 'Kênh hỗ trợ chính thức là Zalo (nhóm / Official Account của máy chủ). Link và mã QR có ở trang Tải game (mục "Cần hỗ trợ cài đặt?"), ở chân trang và cuối trang câu hỏi này. Chỉ tin link Zalo hiện trên website; ban quản trị không bao giờ hỏi mật khẩu của bạn.' },
      { label: 'Tài khoản bị khoá thì khiếu nại thế nào?', content: 'Nhắn ban quản trị qua Zalo hỗ trợ, ghi rõ tên tài khoản và mô tả sự việc (không gửi mật khẩu). Khoá có thời hạn sẽ tự mở khi hết hạn.' },
    ],
  },
  {
    ten: 'Lối chơi',
    hoi: [
      { label: 'Tôi điều khiển một Pokémon hay huấn luyện Pokémon?', content: `Trong ${TEN_SITE} chính bạn là một Pokémon: chọn 1 trong 5 dòng khởi đầu, lên cấp và tiến hoá. Không có con người, không bắt Pokémon bằng bóng.` },
      { label: 'Nên chọn Pokémon khởi đầu nào?', content: 'Charmander cân bằng, Bulbasaur trâu và tấn công cao khi dồn điểm, Squirtle thủ chắc, Pichu đánh xa cơ động, Eevee linh hoạt với 8 nhánh tiến hoá. Không có lựa chọn sai — hãy chọn con bạn thích.' },
      { label: 'Có đổi được dòng Pokémon sau khi tạo không?', content: 'Không. Nhưng một tài khoản có thể tạo nhiều Pokémon để thử các dòng khác.' },
      { label: 'Vì sao tôi nhận ít EXP hơn sau vài giờ chơi?', content: `Đó là năng lượng huấn luyện: ${C.capDo.nangLuong.phutNgay / 60} giờ đầu mỗi ngày đủ 100%, sau đó giảm dần. Phần chưa dùng được tích lại cho hôm sau.` },
      { label: 'Trần cấp là gì?', content: `Cấp tối đa ${C.capDo.capToiDa} mở dần theo mùa (khai trương: ${C.capDo.tranCap[0]?.cap}). Chạm trần thì EXP chuyển thành điểm tinh thông.` },
      { label: 'Khắc hệ có giống bản gốc không?', content: 'Cùng 18 hệ và cùng quan hệ khắc hệ, nhưng hệ số được làm dịu (tối đa ×2, không miễn nhiễm) vì bạn không đổi được Pokémon giữa trận.' },
    ],
  },
  {
    ten: 'Chính sách',
    hoi: [
      { label: 'Có nạp tiền không?', content: 'Không. Đây là dự án fan phi thương mại; không bán vật phẩm, không nhận nạp. Ai nhận tiền nhân danh máy chủ đều là lừa đảo.' },
      { label: 'Có bị xoá dữ liệu khi cập nhật không?', content: 'Không có kế hoạch xoá. Nếu cần bảo trì lớn, ban quản trị sẽ thông báo trước trên trang Tin tức.' },
      { label: 'Dùng phần mềm thứ ba, bot có bị khoá?', content: 'Có. Bot, treo máy tự động, lợi dụng lỗi đều bị khoá tài khoản. Hệ thống năng lượng cũng làm bot không có lợi.' },
      { label: 'Website có theo dõi tôi không?', content: 'Không dùng công cụ theo dõi hay quảng cáo bên thứ ba. Xem Chính sách quyền riêng tư.' },
    ],
  },
]
