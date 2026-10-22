package game.pokemon;

import game.render.Canvas;

/**
 * PHÂN LOẠI THÔNG BÁO (0.9.6.1) - một chỗ quyết định MỖI thông báo hiện thế nào, dùng chung J2ME / APK / PC Java.
 *
 * <pre>
 * Loại        Ví dụ                                   Cách hiện                                 Thời gian
 * KET_QUA     câu được, nhận đồ, mua / bán xong        thẻ nhanh xanh lá (icon vật phẩm / Pokémon) 2,5 s + 60 ms/chữ, tối đa 6 s
 * TIN         gặp Pokémon, nhắc nhở, tin của bạn       thẻ nhanh xanh dương                       3 s + 60 ms/chữ, tối đa 8 s
 * LOI         không đủ xu, hết thể lực, không thể...   thẻ nhanh đỏ + tiếng cảnh báo              4 s + 40 ms/chữ, tối đa 6,5 s
 * HIEM        đồ hiếm, shiny, lên cấp, danh hiệu       thẻ VÀNG nổi bật, lấp lánh                 5 s + 60 ms/chữ, tối đa 8 s
 * SU_KIEN     boss xuất hiện / bị hạ, tin toàn máy chủ biểu ngữ trên cùng (đỏ boss / vàng / teal) 6 s + 40 ms/chữ, tối đa 8 s
 * HOP         bảo trì, mất kết nối, khoá tài khoản,    hộp thoại chặn (bấm OK) - CHỈ những việc
 *             tin dài / nhiều dòng người chơi tự mở   cần đọc kỹ / quyết định
 * </pre>
 * Thẻ nhanh không chặn chơi, không lấy phím; chạm vào thẻ = tắt sớm; tối đa 3 thẻ cùng lúc, phần còn lại XẾP HÀNG (không
 * đè nhau, không biến mất trước khi đọc kịp - xem {@link DungDo}). Máy chủ mới gửi loại rõ ràng (lệnh -104, xem
 * server com.htbb.thongbao.ThongBao); câu cũ (hộp 37, chữ chạy -26, Canvas.startOKDlg của client) được phân loại theo
 * nội dung ở {@link #phanLoai}.
 */
public final class ThongBao {
    public static final int KET_QUA = 0, LOI = 1, TIN = 2, HIEM = 3, SU_KIEN = 4, HOP = 5;
    public static final int HINH_KHONG = 0, HINH_ICON = 1, HINH_POKEMON = 2, HINH_BIEU_TUONG = 3;

    /** Thời gian tối thiểu của MỌI thẻ (đọc kịp câu ngắn nhất). */
    public static final int TOI_THIEU = 2500;
    static final int[] GOC = {2500, 4000, 3000, 5000, 6000};
    static final int[] MOI_CHU = {60, 40, 60, 60, 40};
    static final int[] TRAN = {6000, 6500, 8000, 8000, 8000};

    /** Đếm cho kiểm thử: số lần thông báo đi mỗi đường (thẻ theo loại / hộp). */
    public static final int[] DEM = new int[6];
    /** Câu dài hơn chừng này (hoặc từ 3 dòng) là nội dung để ĐỌC: giữ hộp thoại. */
    static final int DAI_HOP = 170;

    private ThongBao() {
    }

    /** Số chữ đếm thời gian (bỏ xuống dòng, khoảng trắng thừa). */
    static int soChu(String s) {
        if (s == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '\n') {
                n++;
            }
        }
        return n;
    }

    /** Thời gian hiện (ms) theo loại và độ dài - không bao giờ dưới {@link #TOI_THIEU}. */
    public static int thoiGian(int loai, String s) {
        int k = loai < 0 || loai >= GOC.length ? TIN : loai;
        int t = GOC[k] + MOI_CHU[k] * soChu(s);
        return Math.max(TOI_THIEU, Math.min(TRAN[k], t));
    }

    /**
     * Thời gian ĐỌC tối thiểu khi có thẻ khác đang chờ (hàng đợi đầy thì thẻ cũ nhường chỗ sau chừng này, không sớm hơn).
     */
    public static int docToiThieu(int loai, String s) {
        int t = Math.max(TOI_THIEU, 1500 + 50 * soChu(s));
        if (loai == HIEM || loai == SU_KIEN) {
            t = Math.max(t, 4000);
        }
        return Math.min(thoiGian(loai, s), t);
    }

    // ---------------------------------------------------------------- phân loại câu cũ

    static boolean co(String t, String k) {
        return t.indexOf(k) >= 0;
    }

    /** Chữ thường kiểu Việt (CLDC toLowerCase chỉ đổi ASCII: Đ / Ă / Â... đổi tay những chữ hay đứng đầu câu). */
    static String thuong(String s) {
        String t = s.toLowerCase();
        StringBuffer b = null;
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            char d = c;
            switch (c) {
                case 'Đ': d = 'đ'; break;
                case 'Ă': d = 'ă'; break;
                case 'Â': d = 'â'; break;
                case 'Ê': d = 'ê'; break;
                case 'Ô': d = 'ô'; break;
                case 'Ơ': d = 'ơ'; break;
                case 'Ư': d = 'ư'; break;
                case 'Ố': d = 'ố'; break;
                case 'Ấ': d = 'ấ'; break;
                case 'Ở': d = 'ở'; break;
                default: break;
            }
            if (d != c) {
                if (b == null) {
                    b = new StringBuffer(t);
                }
                b.setCharAt(i, d);
            }
        }
        return b == null ? t : b.toString();
    }

    static int soDong(String s) {
        int n = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                n++;
            }
        }
        return n;
    }

    /** Nghiêm trọng / cần đọc kỹ: phải là hộp thoại. */
    static boolean canHop(String s, String t) {
        if (soDong(s) >= 3 || soChu(s) > DAI_HOP) {
            return true;
        }
        return (co(t, "bảo trì") && (co(t, "máy chủ") || co(t, "server") || co(t, "phút") || co(t, "giây")))
                || co(t, "mất kết nối") || co(t, "ngắt kết nối") || co(t, "kết nối lại")
                || co(t, "đăng nhập ở nơi khác") || co(t, "đăng nhập từ nơi khác") || co(t, "nơi khác đăng nhập")
                || co(t, "tài khoản") && (co(t, "bị khoá") || co(t, "bị khóa") || co(t, "bị cấm"))
                || co(t, "phiên bản mới") || co(t, "cập nhật phiên bản") || co(t, "khởi động lại");
    }

    static boolean laBoss(String t) {
        return co(t, "boss") || co(t, "trùm");
    }

    static boolean laHiem(String t) {
        return co(t, "shiny") || co(t, "hiếm") || co(t, "huyền thoại") || co(t, "lên cấp") || co(t, "tiến hoá")
                || co(t, "tiến hóa") || co(t, "chúc mừng") || co(t, "danh hiệu") || co(t, "đạt mốc") || co(t, "vô địch")
                || co(t, "trúng") || co(t, "vĩnh viễn");
    }

    static boolean laLoi(String t) {
        return t.startsWith("không ") || t.startsWith("chưa ") || t.startsWith("bạn phải") || t.startsWith("bạn cần")
                || t.startsWith("bạn không") || t.startsWith("bạn chưa") || t.startsWith("cần ") || t.startsWith("chỉ ")
                || t.startsWith("hết ") || t.startsWith("thiếu ") || t.startsWith("phải ") || t.startsWith("vui lòng")
                || t.startsWith("lỗi") || t.startsWith("có lỗi")
                || co(t, "không đủ") || co(t, "không thể") || co(t, "đã đầy") || co(t, "thất bại") || co(t, "không hợp lệ")
                || co(t, "không tìm thấy") || co(t, "không được phép") || co(t, "đang tạm khoá") || co(t, "đang tạm khóa")
                || co(t, "không còn") || co(t, "chưa đủ") || co(t, "chưa đạt") || co(t, "không trúng");
    }

    static boolean laKetQua(String t) {
        return co(t, "nhận được") || co(t, "câu được") || co(t, "đào được") || co(t, "đã mua") || co(t, "đã bán")
                || co(t, "thành công") || co(t, "hoàn thành") || co(t, "đã dùng") || co(t, "nhặt") || co(t, "đã nhận")
                || co(t, "chế xong") || co(t, "đổi được") || co(t, "+");
    }

    /** Loại của một câu máy chủ cũ (hộp 37 / Canvas.startOKDlg). */
    public static int phanLoai(String s) {
        if (s == null) {
            return TIN;
        }
        String t = thuong(s.trim());
        if (canHop(s, t)) {
            return HOP;
        }
        if (laBoss(t) && (co(t, "xuất hiện") || co(t, "hạ gục") || co(t, "tiêu diệt") || co(t, "đã rời"))) {
            return SU_KIEN;
        }
        if (laLoi(t)) {
            return LOI;
        }
        if (laHiem(t)) {
            return HIEM;
        }
        if (laKetQua(t)) {
            return KET_QUA;
        }
        return TIN;
    }

    /**
     * Câu chữ chạy (-26): tin TOÀN MÁY CHỦ (boss, sự kiện, người khác được thưởng) = biểu ngữ trên; tin RIÊNG của bạn
     * (đồ hết hạn, hiệu ứng, "Bạn ...") = thẻ nhanh.
     */
    public static int phanLoaiChay(String s) {
        if (s == null) {
            return TIN;
        }
        String t = thuong(s.trim());
        if (soDong(s) >= 3 || soChu(s) > DAI_HOP + 60) {
            return HOP;
        }
        if (t.startsWith("bạn ") || co(t, " của bạn") || co(t, "hết hạn") || co(t, "hết tác dụng") || co(t, "đã hết")
                || t.startsWith("thời gian sử dụng") || t.startsWith("kích hoạt") || t.startsWith("bị giảm")
                || co(t, "tinh thông +") || co(t, "sát thương") || co(t, "khoá khi mặc") || t.startsWith("lịch đăng nhập")
                || t.startsWith("không thể") || t.startsWith("thời gian sử dụng") || co(t, "đã hết hạn")) {
            // 0.9.8: tin riêng của bạn (máy chủ cũ gửi -26) - thẻ nhanh, KHÔNG biểu ngữ "TOÀN MÁY CHỦ"
            int k = phanLoai(s);
            return k == SU_KIEN || k == HOP ? TIN : k;
        }
        return SU_KIEN;
    }

    // ---------------------------------------------------------------- đường hiện

    /** Đang ở trong game (màn thế giới / bảng của game): thông báo đi thẻ nhanh; ngoài game (đăng nhập...) giữ hộp. */
    public static boolean trongGame() {
        return Canvas.gameScr != null && Canvas.gameScr.mainChar != null && Canvas.currentScreen != null
                && (Canvas.currentScreen == Canvas.gameScr || Canvas.currentScreen instanceof ManBang
                || Canvas.currentScreen.getClass().getName().startsWith("game.pokemon.Man"));
    }

    /** Hiện một thông báo đã biết loại (máy chủ mới / client). */
    public static void hien(String s, int loai, int hinh, int id) {
        if (s == null || s.trim().length() == 0) {
            return;
        }
        if (loai == HOP) {
            DEM[HOP]++;
            hop(s);
            return;
        }
        DEM[loai < 0 || loai > SU_KIEN ? TIN : loai]++;
        DungDo.them(s, loai, hinh, id, false);
    }

    public static void hien(String s, int loai) {
        hien(s, loai, HINH_KHONG, 0);
    }

    static boolean dangMoHop;

    /** Hộp thoại chặn (OK) - chỉ cho loại HOP. */
    static void hop(String s) {
        dangMoHop = true;
        try {
            Canvas.startOKDlg(s);
        } finally {
            dangMoHop = false;
        }
    }

    /** Hộp 37 của máy chủ (không đường dẫn) khi đang trong game. */
    public static void tuMayChu(String s) {
        hien(s, phanLoai(s));
    }

    /** Chữ chạy -26 (GameScr.onMSGServer) khi đang trong game. */
    public static void tuChuChay(String s) {
        hien(s, phanLoaiChay(s));
    }

    /**
     * Canvas.startOKDlg(câu, false) của client: trong game và không phải loại HOP thì thành thẻ nhanh (true = đã hiện,
     * không mở hộp).
     */
    public static boolean tuHopOK(String s) {
        if (dangMoHop || s == null || !trongGame()) {
            return false;
        }
        int k = phanLoai(s);
        if (k == HOP) {
            DEM[HOP]++;
            return false;
        }
        hien(s, k);
        return true;
    }

    // ---------------------------------------------------------------- câu cá: kết quả hiện lúc kéo cần

    /** Thông báo -104 "Câu được ..." đang giữ chờ hoạt cảnh cá cắn (chữ, loại, hình). */
    static String choCau;
    static int choCauLoai, choCauHinh, choCauId;

    static void giuCau(String s, int loai, int hinh, int id) {
        choCau = s;
        choCauLoai = loai;
        choCauHinh = hinh;
        choCauId = id;
    }

    /**
     * SinhThai.ketThucCau: hiện kết quả câu (thẻ nhanh, không chặn). Câu kiểu cũ "Câu được: 1 X (bậc 1). Gặp Y! Thể lực
     * còn N." (máy chủ cũ) được tách thành kết quả + lần gặp riêng.
     */
    public static void xongCau(String s) {
        if (s == null) {
            return;
        }
        if (s.equals(choCau)) {
            String c = choCau;
            choCau = null;
            DEM[choCauLoai < 0 || choCauLoai > SU_KIEN ? TIN : choCauLoai]++;
            DungDo.them(c, choCauLoai, choCauHinh, choCauId, true);
            return;
        }
        String[] r = tachCauCu(s);
        DEM[KET_QUA]++;
        DungDo.them(r[0], KET_QUA, HINH_BIEU_TUONG, BieuTuong.NGUYEN_LIEU, true);
        if (r[1] != null) {
            DEM[TIN]++;
            DungDo.them(r[1], TIN, HINH_BIEU_TUONG, BieuTuong.DEX, false);
        }
    }

    /** "Câu được: 1 Vảy Magikarp (bậc 1). Gặp Magikarp! Thể lực còn 176." -> {"Câu được Vảy Magikarp ×1\nThể lực còn 176", "Bạn vừa gặp Magikarp!"}. */
    public static String[] tachCauCu(String s) {
        String[] r = new String[2];
        if (!s.startsWith("Câu được: ")) {
            r[0] = s;
            return r;
        }
        String t = s.substring("Câu được: ".length());
        String theLuc = null;
        int iTl = t.indexOf(" Thể lực còn ");
        if (iTl >= 0) {
            theLuc = t.substring(iTl + 1).trim();
            if (theLuc.endsWith(".")) {
                theLuc = theLuc.substring(0, theLuc.length() - 1);
            }
            t = t.substring(0, iTl).trim();
        }
        int iGap = t.indexOf(". Gặp ");
        if (iGap >= 0) {
            String gap = t.substring(iGap + ". Gặp ".length()).trim();
            if (gap.endsWith("!") || gap.endsWith(".")) {
                gap = gap.substring(0, gap.length() - 1);
            }
            r[1] = "Bạn vừa gặp " + gap + "!";
            t = t.substring(0, iGap);
        }
        int iBac = t.indexOf(" (bậc ");
        if (iBac >= 0) {
            t = t.substring(0, iBac);
        }
        int sp = t.indexOf(' ');
        String so = sp > 0 ? t.substring(0, sp) : "1";
        boolean laSo = so.length() > 0;
        for (int i = 0; i < so.length(); i++) {
            if (!Character.isDigit(so.charAt(i))) {
                laSo = false;
            }
        }
        String ten = laSo && sp > 0 ? t.substring(sp + 1).trim() : t.trim();
        r[0] = "Câu được " + ten + " ×" + (laSo ? so : "1") + (theLuc == null ? "" : "\n" + theLuc);
        return r;
    }
}
