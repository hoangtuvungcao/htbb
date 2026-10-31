import java.util.ArrayDeque;
import java.util.Locale;
import java.util.Random;

/**
 * Cau chat cua Huan luyen vien AI: ngan hang cau tieng Viet tu nhien theo ngu canh (dang lam gi) + tra loi chao / hoi.
 * Khong bao gio ru giao dich / xin do (khong giong lua dao). Khong lap nguyen van (nho 40 cau gan nhat CHUNG moi bot).
 * Nhip chat do may chu chan nhu moi nguoi choi (ChongSpam); dich vu chi chat o luc doi viec + khi duoc goi.
 */
final class HlvAiChat {

    private HlvAiChat() {
    }

    static final String[] CHAO = {"chào cả nhà", "hello ae", "hi mọi người", "tối nay đông vui ghê", "có ai online không nè",
        "chào buổi sáng mọi người", "vào game điểm danh nào", "ae khoẻ không", "hôm nay server vui ghê", "lại là mình đây"};
    static final String[] CAY = {"map này quái ra nhanh ghê", "ai cày chung không", "lên cấp {cap} rồi, mệt mà vui",
        "đang cày ở {map}", "sắp lên cấp rồi, cố thêm chút", "quái ở đây đánh đau quá", "nay EXP lên nhanh thật",
        "cày xong đợt này đi ngủ", "rớt được món ngon nè", "bình máu hết sạch luôn"};
    static final String[] GYM = {"sắp đánh Gym rồi hồi hộp", "Gym này khó thật sự", "vừa lấy được huy hiệu mới", "ai có mẹo đánh Gym không",
        "thua Gym rồi, cày thêm đã"};
    static final String[] PVP = {"ai solo không", "PK nhẹ nhàng thôi nha", "vừa thua một trận, đau quá", "đấu trường hôm nay căng ghê",
        "thắng liền 2 trận rồi", "bảng xếp hạng đổi liên tục luôn"};
    static final String[] BUON = {"giá chợ dạo này ổn", "vừa bán được món đồ", "chợ sinh thái nhiều hàng ghê", "vườn nhà mình ra quả rồi",
        "câu cá cả buổi được con to", "nhà trẻ sắp nở trứng rồi"};
    static final String[] XA_HOI = {"chúc mọi người chơi vui", "có ai biết đường tới {map} không", "đội mình còn chỗ, ai vào chơi chung",
        "nay vui quá ae ơi", "ai cùng làm nhiệm vụ không", "mới vào game được mấy hôm", "game này chơi cuốn ghê"};
    static final String[] THUONG = {"treo máy chút đi ăn cơm", "hôm nay câu được cá to", "Pokédex mình được {dex} loài rồi",
        "vừa bắt được một con hiếm", "về làng hồi máu đã", "lag nhẹ hay sao ấy", "buồn ngủ quá mà vẫn muốn chơi"};
    static final String[] TAM_BIET = {"mình off nha, mai gặp", "đi ngủ đây, bye ae", "off đi học bài đây", "tạm biệt mọi người"};

    static final String[] TRA_CHAO = {"chào bạn", "hi bạn", "chào nha", "hello", "chào bạn, chơi vui nhé"};
    static final String[] TRA_CAM_ON = {"không có gì", "ok bạn", "hihi", "có gì đâu"};
    static final String[] TRA_DUONG = {"bạn mở Sổ tay xem bản đồ nhé", "đi qua cổng rồi hỏi Arcanine ở làng ấy", "mình cũng đang tìm đường nè"};
    static final String[] TRA_CAP = {"mình cấp {cap} nè", "cấp {cap} thôi, còn gà lắm", "mới {cap} à"};
    static final String[] TRA_BOT = {"mình là huấn luyện viên AI của server nè, chơi cùng cho vui", "đúng rồi, mình là bot AI, cứ chơi bình thường nha"};
    static final String[] TRA_KHAC = {"ok", "uh", "vậy à", "hay đó", "ừa", ":D"};

    /** Cau gan day (chung moi bot) - khong lap nguyen van. */
    private static final ArrayDeque<String> GAN = new ArrayDeque<>();

    static synchronized String chon(String[] bang, Random r, int cap, String map, int dex) {
        for (int lan = 0; lan < 8; lan++) {
            String c = bang[r.nextInt(bang.length)].replace("{cap}", String.valueOf(cap)).replace("{map}", map == null || map.isEmpty() ? "bãi cỏ" : map)
                    .replace("{dex}", String.valueOf(Math.max(1, dex)));
            if (GAN.contains(c)) continue;
            GAN.addLast(c);
            while (GAN.size() > 40) GAN.removeFirst();
            return c;
        }
        return null;
    }

    static String[] theoViec(String viec) {
        if (viec == null) return XA_HOI;
        switch (viec) {
            case "cay": case "nhiem_vu": case "treo": case "pho_ban": case "boss": return CAY;
            case "gym": return GYM;
            case "pk": case "dau_truong": return PVP;
            case "cho": case "sinh_thai": case "vuon": case "nha_tre": case "giao_dich": return BUON;
            case "bat": case "su_kien": return THUONG;
            default: return XA_HOI;
        }
    }

    /** Tra loi mot cau nguoi khac noi voi minh (null = khong tra loi). {@code rieng} = tin rieng (luon tra loi). */
    static String traLoi(String noiDung, boolean rieng, Random r, int cap) {
        String t = BotChucNang.khongDau(noiDung).toLowerCase(Locale.ROOT);
        String[] bang = null;
        if (t.contains("bot") || t.contains("ai that") || t.contains("nguoi that")) bang = TRA_BOT;
        else if (t.contains("chao") || t.startsWith("hi") || t.contains("hello") || t.contains("xin chao")) bang = TRA_CHAO;
        else if (t.contains("cam on") || t.contains("thank") || t.contains("tks")) bang = TRA_CAM_ON;
        else if (t.contains("duong") || t.contains("o dau") || t.contains("di sao")) bang = TRA_DUONG;
        else if (t.contains("cap may") || t.contains("lv may") || t.contains("level")) bang = TRA_CAP;
        else if (rieng) bang = TRA_KHAC;
        if (bang == null) return null;
        return bang[r.nextInt(bang.length)].replace("{cap}", String.valueOf(cap));
    }
}
