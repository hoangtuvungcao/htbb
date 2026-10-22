package game.pokemon;

import game.model.IAction;
import game.model.ItemInInventory;
import game.model.ItemTemplate;
import game.model.MainChar;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.Res;
import game.render.screen.Font;
import javax.microedition.lcdui.Graphics;
import network.Message;

/**
 * Dùng vật phẩm (hành trang WindowInfoScr / ShopNew, ô nhanh): gửi lệnh và hiện KẾT QUẢ thành thông báo nhanh.
 *
 * Lệnh -104 server -> client (HTBB real.DungVatPham, com.htbb.thongbao.ThongBao): byte loại (ThongBao.KET_QUA / LOI /
 * TIN / HIEM / SU_KIEN), UTF nội dung (tối đa 2 dòng: ý chính '\n' thông tin phụ), byte kiểu thao tác, rồi (0.9.6.1)
 * byte hình + id (icon vật phẩm / huy hiệu Pokémon / BieuTuong). Loại, thời gian hiện, hàng đợi: xem {@link ThongBao}.
 * Server chỉ gửi -104 cho client bản >= 902 (gửi số bản lúc đăng nhập), bản cũ nhận hộp thoại OK như trước.
 * Thông báo vẽ trên MỌI màn (Canvas.paint gọi {@link #ve}), kể cả lúc đang mở hành trang, và không bị "Tiện ích -
 * Thông báo: tắt" nuốt như hộp thoại OK. Dùng chung jar / APK / PC Java (port từ client_jar/src).
 */
public final class DungDo {
    public static final int LENH = -104;
    public static final int OK = 0, LOI = 1, TIN = 2;

    /** Đếm chẩn đoán (kiểm thử / F9 bản PC). */
    public static int soNhan, soGui;
    /** Kiểu thao tác của thông báo cuối (-1 = server cũ không gửi) và nội dung - kiểm thử. */
    public static int kieuCuoi = -1;
    public static String chuCuoi;

    private DungDo() {
    }

    // ---------------------------------------------------------------- mạng

    /** true = đã xử lý (lệnh -104). Gọi ở đầu MessageHandler.onMessage. */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            int loai = msg.reader().readByte();
            String s = msg.reader().readUTF();
            // 0.9.3: byte kiểu thao tác (TrangThaiNguoiChoi.MUA / BAN / DUNG / MAC...) - server 0.9.2 không gửi
            int k = -1;
            try {
                if (msg.reader().available() > 0) {
                    k = msg.reader().readByte();
                }
            } catch (Throwable e) {
                k = -1;
            }
            // 0.9.6.1: hình kèm (ThongBao.HINH_*): icon vật phẩm / huy hiệu Pokémon (short) / BieuTuong (byte)
            int hinh = 0, id = 0;
            try {
                if (msg.reader().available() > 0) {
                    hinh = msg.reader().readByte();
                    if (hinh == ThongBao.HINH_ICON || hinh == ThongBao.HINH_POKEMON) {
                        id = msg.reader().readShort();
                    } else if (hinh == ThongBao.HINH_BIEU_TUONG) {
                        id = msg.reader().readByte();
                    }
                }
            } catch (Throwable e) {
                hinh = 0;
            }
            kieuCuoi = k;
            chuCuoi = s;
            soNhan++;
            if (loai == TIN && k == TrangThaiNguoiChoi.DUNG && game.render.screen.GameScr.autoFight) {
                return true;   // tự đánh tự uống bình: thanh HP đã lên, không phủ thông báo mỗi bình
            }
            // kết quả câu cá: giữ đến lúc kéo cần (SinhThai.ketThucCau -> ThongBao.xongCau)
            if (s.startsWith("Câu được") && SinhThai.chanThongBao(s)) {
                ThongBao.giuCau(s, loai, hinh, id);
                return true;
            }
            if (loai == TIN && hinh == 0) {
                int kk = ThongBao.phanLoai(s);   // câu cũ đổi từ hộp 37: phân loại theo nội dung
                if (kk == ThongBao.HOP && ThongBao.trongGame()) {
                    ThongBao.hien(s, ThongBao.HOP);
                    return true;
                }
                loai = kk == ThongBao.HOP ? TIN : kk;
            }
            ThongBao.hien(s, loai, hinh, id);
            if (loai == LOI) {
                AmThanh.phat(AmThanh.CANH_BAO);
            } else if (loai == OK || loai == ThongBao.HIEM) {
                AmThanh.phat(AmThanh.XAC_NHAN);
            }
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    /**
     * Bấm "Sử dụng" một món trong hành trang. Chỉ chặn ở client những gì chắc chắn sai (đang bán, sai giới tính);
     * cấp độ / hệ / điều kiện khác để server quyết (server luôn trả lời lý do).
     */
    public static void dungItem(ItemInInventory item) {
        if (item == null) {
            return;
        }
        if (item.isSelling) {
            loi("Vật phẩm đang được bán trong gian hàng.");
            return;
        }
        MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
        ItemTemplate it = Res.getItem(item.charClazz, item.idTemplate);
        if (it != null && mc != null && it.gender != 0 && mc.gender != 0 && it.gender != mc.gender) {
            loi(it.gender == 1 ? "Vật phẩm này chỉ dành cho Pokémon đực." : "Vật phẩm này chỉ dành cho Pokémon cái.");
            return;
        }
        soGui++;
        GameService.gI().useItem(item.itemID);
    }

    /** Tháo một món trang bị đang mặc (server: lệnh 29 với id món đang mặc = tháo về hành trang). */
    public static void thao(ItemInInventory item) {
        if (item == null) {
            return;
        }
        soGui++;
        GameService.gI().useItem(item.itemID);
    }

    // ---------------------------------------------------------------- thông báo nhanh

    public static void bao(String s) {
        bao(s, OK);
    }

    public static void loi(String s) {
        bao(s, LOI);
    }

    /** Thông báo nhanh của client (loại OK / LOI / TIN); TIN được phân loại lại theo nội dung (lỗi, hiếm, kết quả). */
    public static void bao(String s, int loai) {
        if (s == null) {
            return;
        }
        if (loai == TIN) {
            int k = ThongBao.phanLoai(s);
            if (k == LOI || k == ThongBao.HIEM || k == OK) {
                loai = k;
            }
        }
        them(s, loai, 0, 0, false);
    }

    // ---------------------------------------------------------------- hàng đợi (0.9.6.1)

    /** Một thông báo: chữ (dòng 1 = ý chính, dòng 2 sau '\n' = thông tin phụ), loại ThongBao.*, hình kèm, thời gian. */
    static final class Tin {
        String chu, chinh, phu;
        int loai, hinh, hinhId;
        long luc;
        int song, doc;
        int dem = 1;
        /** biểu ngữ: -1 chưa tính, 0 toàn máy chủ, 1 boss, 2 tin vui */
        int kieuBieu = -1;
        /** dòng ý chính đã ngắt theo bề ngang (tính lúc vẽ) */
        String[] dong;
        int rongNgat;

        Tin(String s, int loai, int hinh, int id) {
            chu = s;
            int n = s.indexOf('\n');
            chinh = n < 0 ? s : s.substring(0, n).trim();
            phu = n < 0 ? null : s.substring(n + 1).trim().replace('\n', ' ');
            if (phu != null && phu.length() == 0) {
                phu = null;
            }
            this.loai = loai;
            this.hinh = hinh;
            hinhId = id;
            song = ThongBao.thoiGian(loai, s);
            doc = ThongBao.docToiThieu(loai, s);
        }

        int con(long now) {
            return (int) (song - (now - luc));
        }
    }

    /** Thẻ đang hiện (0 = mới nhất, trên cùng) - tối đa 3; còn lại chờ trong CHO (không đè, không mất). */
    static final int SO_DONG = 3, CHO_TOI_DA = 12, CHO_BIEU_TOI_DA = 6;
    static final Tin[] HIEN = new Tin[SO_DONG];
    static final java.util.Vector CHO = new java.util.Vector();
    /** Biểu ngữ trên cùng (boss / toàn máy chủ): một cái hiện, còn lại chờ. */
    static Tin bieu;
    static final java.util.Vector CHO_BIEU = new java.util.Vector();
    /** Hai thẻ vào cách nhau ít nhất chừng này (lần lượt trượt vào, không chớp cùng lúc). */
    static final int GIAN_CACH = 350;
    private static long dayCuoi, veCuoi;

    /**
     * Thêm một thông báo vào hàng. loai = ThongBao.KET_QUA / LOI / TIN / HIEM / SU_KIEN. dau = chen đầu hàng chờ (kết
     * quả câu cá: hiện trước lần gặp Pokémon đến cùng lúc).
     */
    public static void them(String s, int loai, int hinh, int id, boolean dau) {
        if (s == null) {
            return;
        }
        s = s.trim();
        if (s.length() == 0) {
            return;
        }
        if (loai < 0 || loai > ThongBao.SU_KIEN) {
            loai = TIN;
        }
        long now = System.currentTimeMillis();
        if (loai == ThongBao.SU_KIEN) {
            if (bieu != null && bieu.chu.equals(s) && bieu.con(now) > 0) {
                bieu.dem++;
                bieu.luc = now;
                return;
            }
            if (gopTrong(CHO_BIEU, s)) {
                return;
            }
            CHO_BIEU.addElement(new Tin(s, loai, hinh, id));
            if (CHO_BIEU.size() > CHO_BIEU_TOI_DA) {
                CHO_BIEU.removeElementAt(0);
            }
            dayHang(now);
            return;
        }
        // trùng một thẻ đang hiện (bấm liên tục / máy chủ gửi lặp): gộp "×n", đưa lên đầu, làm mới thời gian
        for (int i = 0; i < SO_DONG; i++) {
            Tin t = HIEN[i];
            if (t != null && t.chu.equals(s) && t.con(now) > 0) {
                t.dem++;
                t.luc = now;
                for (int k = i; k > 0; k--) {
                    HIEN[k] = HIEN[k - 1];
                }
                HIEN[0] = t;
                return;
            }
        }
        if (gopTrong(CHO, s)) {
            return;
        }
        Tin t = new Tin(s, loai, hinh, id);
        if (dau) {
            CHO.insertElementAt(t, 0);
        } else {
            CHO.addElement(t);
        }
        if (CHO.size() > CHO_TOI_DA) {
            CHO.removeElementAt(dau ? 1 : 0);   // hàng quá dài (máy chủ dội tin): bỏ tin chờ cũ nhất
        }
        dayHang(now);
    }

    private static boolean gopTrong(java.util.Vector v, String s) {
        for (int i = 0; i < v.size(); i++) {
            Tin t = (Tin) v.elementAt(i);
            if (t.chu.equals(s)) {
                t.dem++;
                return true;
            }
        }
        return false;
    }

    /**
     * Kết quả (không phải lỗi) chờ trong lúc câu cá: giữ đến khi kéo cần xong. Trừ lời nhắc của chính lần câu
     * (SinhThai.laNhacCau: "Cá cắn câu! Kéo cần ngay!") - phải hiện NGAY vì người chơi chỉ có ~1,5 giây để kéo.
     */
    static boolean giuLai(Tin t) {
        return t.loai != LOI && SinhThai.dangCau() && !SinhThai.laNhacCau(t.chu);
    }

    /** Hết giờ thì tắt; có chỗ thì đưa tin chờ vào; hàng chờ đầy chỗ thì thẻ cũ nhất ĐÃ ĐỌC ĐỦ (doc) nhường chỗ. */
    static void dayHang(long now) {
        int n = 0;
        for (int i = 0; i < SO_DONG; i++) {
            Tin t = HIEN[i];
            HIEN[i] = null;
            if (t != null && t.con(now) > 0) {
                HIEN[n++] = t;
            }
        }
        while (CHO.size() > 0) {
            Tin c = (Tin) CHO.elementAt(0);
            if (giuLai(c)) {
                break;
            }
            if (n > 0 && now - dayCuoi < GIAN_CACH) {
                break;
            }
            if (n == SO_DONG) {
                Tin cu = HIEN[SO_DONG - 1];
                if (now - cu.luc < cu.doc) {
                    break;
                }
                HIEN[SO_DONG - 1] = null;
                n--;
            }
            for (int k = n; k > 0; k--) {
                HIEN[k] = HIEN[k - 1];
            }
            CHO.removeElementAt(0);
            c.luc = now;
            HIEN[0] = c;
            n++;
            dayCuoi = now;
        }
        if (bieu != null && bieu.con(now) <= 0) {
            bieu = null;
        }
        if (CHO_BIEU.size() > 0 && (bieu == null || now - bieu.luc >= bieu.doc)) {
            bieu = (Tin) CHO_BIEU.elementAt(0);
            CHO_BIEU.removeElementAt(0);
            bieu.luc = now;
        }
    }

    /** Biểu ngữ (thông báo máy chủ cũ): phân loại theo nội dung như hộp 37. */
    public static void bieuNgu(String s) {
        ThongBao.tuMayChu(s);
    }

    /** Xoá mọi thông báo đang hiện / chờ (đăng xuất / kiểm thử). */
    public static void xoaHet() {
        for (int i = 0; i < SO_DONG; i++) {
            HIEN[i] = null;
        }
        CHO.removeAllElements();
        CHO_BIEU.removeAllElements();
        bieu = null;
        dayCuoi = 0;
        veCuoi = 0;
    }

    /** Còn thông báo đang hiện / chờ (để màn tự vẽ lại). */
    public static boolean dangHien() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < SO_DONG; i++) {
            if (HIEN[i] != null && HIEN[i].con(now) > 0) {
                return true;
            }
        }
        return CHO.size() > 0 || CHO_BIEU.size() > 0 || bieu != null && bieu.con(now) > 0;
    }

    // ---- truy vấn cho kiểm thử (client_pc/test/KiemThongBao)
    public static int soHien() {
        int n = 0;
        for (int i = 0; i < SO_DONG; i++) {
            if (HIEN[i] != null) {
                n++;
            }
        }
        return n;
    }

    public static int soCho() {
        return CHO.size();
    }

    public static int soChoBieuNgu() {
        return CHO_BIEU.size();
    }

    /** Chữ thẻ thứ i (0 = mới nhất) / biểu ngữ (i = -1); null = trống. */
    public static String chuHien(int i) {
        Tin t = i < 0 ? bieu : i < SO_DONG ? HIEN[i] : null;
        return t == null ? null : t.chu;
    }

    public static int loaiHien(int i) {
        Tin t = i < 0 ? bieu : i < SO_DONG ? HIEN[i] : null;
        return t == null ? -1 : t.loai;
    }

    public static int songHien(int i) {
        Tin t = i < 0 ? bieu : i < SO_DONG ? HIEN[i] : null;
        return t == null ? -1 : t.song;
    }

    /** Kiểm thử: lùi đồng hồ mọi thẻ đi ms (như đã hiện lâu chừng ấy) rồi đẩy hàng. */
    public static void troiGio(int ms) {
        for (int i = 0; i < SO_DONG; i++) {
            if (HIEN[i] != null) {
                HIEN[i].luc -= ms;
            }
        }
        if (bieu != null) {
            bieu.luc -= ms;
        }
        dayCuoi -= ms;
        dayHang(System.currentTimeMillis());
    }

    private static String sucKhoe;
    private static long sucKhoeLuc, sucKhoeHien = -1;
    static final long GIO = 3600000L;

    /** Dòng nhắc sức khoẻ (máy chủ gửi khi vào game + định kỳ): hiện mờ 6 giây lúc vào game, rồi tối đa mỗi giờ. */
    public static void sucKhoe(String s) {
        long now = System.currentTimeMillis();
        if (sucKhoeHien >= 0 && now - sucKhoeHien < GIO) {
            return;
        }
        sucKhoe = s;
        sucKhoeHien = now;
        sucKhoeLuc = now;
    }

    /** Câu máy chủ (-26) là lời nhắc sức khoẻ / độ tuổi? */
    public static boolean laSucKhoe(String s) {
        return s != null && (s.indexOf("phút mỗi ngày") >= 0 || s.indexOf("sức khoẻ") >= 0 || s.indexOf("sức khỏe") >= 0);
    }

    private static final IAction[] BO_QUA = new IAction[SO_DONG];
    private static final IAction[] XEM = new IAction[SO_DONG];
    private static final IAction BO_BIEU = new IAction() {
        public void perform() {
            bieu = null;
            dayHang(System.currentTimeMillis());
        }
    };

    static {
        for (int i = 0; i < SO_DONG; i++) {
            final int k = i;
            BO_QUA[i] = new IAction() {
                public void perform() {
                    HIEN[k] = null;
                    dayHang(System.currentTimeMillis());
                }
            };
            XEM[i] = new IAction() {
                public void perform() {
                    Tin t = HIEN[k];
                    HIEN[k] = null;
                    if (t != null) {
                        HopThongBao.bao(t.loai == LOI ? "Không thực hiện được" : "Thông báo", t.chu);
                    }
                }
            };
        }
    }

    /** Màu nhấn theo loại (đĩa biểu tượng, vạch thời gian). */
    static int mauLoai(Tin t, int ic) {
        if (t.loai == LOI) {
            return ChuDe.DO;
        }
        if (t.loai == ThongBao.HIEM) {
            return ChuDe.VANG_TOI;
        }
        if (ic == BieuTuong.CANH_BAO) {
            return ChuDe.CAM;
        }
        if (t.loai == TIN || ic == BieuTuong.CAMERA) {
            return ChuDe.XANH;
        }
        return ic == BieuTuong.XU ? ChuDe.VANG_TOI : ChuDe.LA;
    }

    /** Vẽ trên cùng (Canvas.paint, sau màn / hộp thoại / menu): biểu ngữ + thẻ thông báo GIỮA TRÊN (không che nhân vật). */
    public static void ve(Graphics g) {
        veSucKhoe(g);
        long now = System.currentTimeMillis();
        long dt = veCuoi == 0 ? 0 : now - veCuoi;
        veCuoi = now;
        boolean theGioi = Canvas.currentScreen == Canvas.gameScr;
        // KHÔNG đếm giờ lúc bị che (hộp thoại đang mở / khung không được vẽ): mở lại vẫn còn đủ thời gian để đọc
        boolean che = Canvas.currentDialog != null || dt > 600;
        if (dt > 0) {
            for (int i = 0; i < SO_DONG; i++) {
                if (HIEN[i] != null && che) {
                    HIEN[i].luc += dt;
                }
            }
            if (bieu != null && (che || !theGioi)) {
                bieu.luc += dt;
            }
        }
        dayHang(now);
        if (HIEN[0] == null && bieu == null) {
            return;
        }
        if (Canvas.currentDialog != null) {
            return;   // hộp thoại đang mở: không đè lên đầu hộp (thông báo dừng đồng hồ, hiện tiếp khi đóng hộp)
        }
        Canvas.resetTrans(g);
        int lopCu = VungCham.lopDangVe();
        VungCham.lop(VungCham.NOI);
        boolean sangCu = Font.nenSang;
        // giữa trên, dưới hàng nút / khung mục tiêu của HUD; tránh thẻ trạng thái (trái) và nút (phải)
        int rongToiDa = Math.max(170, Math.min(ChuDe.dt() ? 400 : 360, Canvas.w - 2 * 140));
        int y = Bang.anTren + 40;
        if (theGioi) {
            // màn hẹp (4:3...): thẻ trạng thái / nút HUD có thể chạm dải giữa - xuống dưới chúng
            int rongMax = Math.max(rongToiDa, Math.max(200, Math.min(ChuDe.dt() ? 460 : 420, Canvas.w - 2 * 140)));
            int d = VungCham.duoiVung("hud-", Canvas.hw - rongMax / 2, Canvas.hw + rongMax / 2, y + 30);
            if (d >= 0 && d + 4 > y) {
                y = d + 4;
            }
        }
        if (theGioi && bieu != null) {
            y = veBieuNgu(g, bieu, y, now) + 6;
        }
        // màn chơi: giữa trên, chạm = tắt / "Xem"; đang mở bảng: sát đáy, KHÔNG nhận chạm (không che tab / nút của
        // bảng), tự tắt
        int[] cao = CAO;
        int tong = 0;
        for (int i = 0; i < SO_DONG; i++) {
            if (HIEN[i] != null) {
                cao[i] = caoThe(HIEN[i], rongToiDa, theGioi);
                tong += cao[i] + 4;
            }
        }
        if (!theGioi) {
            y = Canvas.h - Bang.anDuoi - 10 - tong;
        }
        for (int i = 0; i < SO_DONG; i++) {
            if (HIEN[i] != null) {
                y = veThe(g, HIEN[i], i, y, cao[i], rongToiDa, theGioi, now) + 4;
            }
        }
        Font.nenSang = sangCu;
        VungCham.lop(lopCu);
    }

    private static final int[] CAO = new int[SO_DONG];
    /** độ đậm bóng (alpha) dưới thẻ hiếm / biểu ngữ - màu bóng là token ChuDe.BONG */
    static final int BONG_THE = 0x20000000, BONG_BIEU = 0x30000000;

    static int caoThe(Tin t, int rongToiDa, boolean theGioi) {
        int vai = t.phu != null ? ChuDe.NHAN : ChuDe.THUONG;
        int wChu = rongToiDa - 40;
        if (t.dong == null || t.rongNgat != rongToiDa) {
            t.dong = ChuDe.ngat(t.chinh, vai, wChu, 3);
            t.rongNgat = rongToiDa;
        }
        int soDong = Math.min(2, t.dong.length);
        int lh = ChuDe.cao(vai);
        int h = soDong * lh + (t.phu != null ? ChuDe.cao(ChuDe.PHU) + 1 : 0) + 12;
        if (t.loai == ThongBao.HIEM) {
            h += 4;
        }
        return Math.max(VungCham.MIN, h);
    }

    /** Một thẻ: viên trắng (hiếm: vàng nhạt viền vàng, lấp lánh), đĩa màu loại + hình, 1-2 dòng chính + dòng phụ, vạch thời gian. */
    static int veThe(Graphics g, Tin t, int i, int y, int cao, int rongToiDa, boolean theGioi, long now) {
        long tuoi = now - t.luc;
        int vai = t.phu != null ? ChuDe.NHAN : ChuDe.THUONG;
        int lh = ChuDe.cao(vai);
        String[] d = t.dong;
        boolean xem = d.length > 2 && theGioi;
        int soDong = Math.min(2, d.length);
        int wXem = xem ? ChuDe.rong("Xem", ChuDe.NHAN) + 14 : 0;
        String sl = t.dem > 1 ? "×" + t.dem : null;
        int wSl = sl == null ? 0 : ChuDe.rong(sl, ChuDe.NHAN) + 8;
        int dkd = Math.min(cao - 8, 34);
        int rong = 0;
        for (int k = 0; k < soDong; k++) {
            rong = Math.max(rong, ChuDe.rong(d[k], vai));
        }
        if (t.phu != null) {
            rong = Math.max(rong, ChuDe.rong(t.phu, ChuDe.PHU));
        }
        rong = Math.min(rongToiDa, rong + dkd + 22 + wXem + wSl);
        int x = Canvas.hw - rong / 2;
        int yy = y - (int) (tuoi < 160 ? (160 - tuoi) / 20 : 0);
        int ic = t.hinh == ThongBao.HINH_BIEU_TUONG ? t.hinhId : bieuTuong(t.chu, t.loai);
        if (t.hinh == 0 && t.loai == ThongBao.HIEM) {
            ic = BieuTuong.SU_KIEN;   // ngôi sao
        }
        int mau = mauLoai(t, ic);
        boolean hiem = t.loai == ThongBao.HIEM;
        int r = soDong == 1 && t.phu == null ? cao / 2 : Math.min(cao / 2, 14);
        // nền thẻ = thành phần thẻ chung của giao diện (ChuDe.the) - đổi giao diện thì thẻ đổi theo; chỉ màu là token
        // 0.9.7 (NRO): thẻ thường = bảng tối chỉ vàng viền màu loại (chữ giấy da); thẻ hiếm = thanh giấy da khung vàng
        KhungNro.bong(g, x, yy, rong, cao);
        if (hiem) {
            if (!KhungNro.ve(g, KhungNroBang.HANG_CHON, x, yy, rong, cao)) {
                GiaoDien.khoi(g, x, yy, rong, cao, 2, ChuDe.VANG);
            }
        } else {
            g.setColor(mau);
            g.fillRect(x - 1, yy - 1, rong + 2, cao + 2);
            if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, yy, rong, cao)) {
                GiaoDien.khoi(g, x, yy, rong, cao, 2, 0x2B1A10);
            }
        }
        Font.nenSang = hiem;
        int mauChu = hiem ? ChuDe.CHU : 0xF6E6C0, mauPhu = hiem ? ChuDe.CHU_PHU : ChuDe.VANG;
        int xd = x + 5, yd = yy + (cao - dkd) / 2;
        veHinh(g, t, ic, mau, xd, yd, dkd);
        int xc = xd + dkd + 7;
        int caoChu = soDong * lh + (t.phu != null ? ChuDe.cao(ChuDe.PHU) + 1 : 0);
        int yt = yy + (cao - caoChu) / 2;
        int wd = rong - (xc - x) - 8 - wXem - wSl;
        for (int k = 0; k < soDong; k++) {
            String dk = k == soDong - 1 && xem ? ChuDe.cat(d[k] + "…", vai, wd) : ChuDe.cat(d[k], vai, wd);
            ChuDe.chu(g, dk, xc, yt + k * lh, vai, mauChu, 0);
        }
        if (t.phu != null) {
            ChuDe.chu(g, ChuDe.cat(t.phu, ChuDe.PHU, wd), xc, yt + soDong * lh + 1, ChuDe.PHU, mauPhu, 0);
        }
        if (sl != null) {
            ChuDe.chuGiua(g, sl, x + rong - wXem - 6, yy, cao, ChuDe.NHAN, mauPhu, 1);
        }
        // vạch thời gian còn lại (mảnh, dưới đáy thẻ): biết thẻ còn bao lâu
        int con = t.con(now);
        if (rong > 60 && con > 0) {
            int wv = (rong - 2 * r) * con / Math.max(1, t.song);
            g.setColor(GiaoDien.pha(mau, ChuDe.NEN, 1, 2));
            g.fillRect(x + r, yy + cao - 3, Math.max(1, wv), 2);
        }
        if (hiem) {
            veLapLanh(g, x, yy, rong, cao, now);
        }
        if (theGioi) {
            VungCham.them(g, x, yy, rong - wXem, cao, BO_QUA[i], "thong-bao-" + i);
        }
        if (xem) {
            int xx = x + rong - wXem - 4;
            ChuDe.chuGiua(g, "Xem", xx + wXem / 2, yy, cao, ChuDe.NHAN, hiem ? ChuDe.CHINH_TOI : ChuDe.VANG, 2);
            VungCham.them(g, xx, yy, wXem + 4, cao, XEM[i], "thong-bao-xem-" + i);
        }
        return yy + cao;
    }

    /** Hình đầu thẻ: icon vật phẩm / huy hiệu Pokémon / đĩa màu + biểu tượng trắng. */
    static void veHinh(Graphics g, Tin t, int ic, int mau, int x, int y, int k) {
        int cx = x + k / 2, cy = y + k / 2;
        if (t.hinh == ThongBao.HINH_POKEMON && t.hinhId > 0 && HuyHieuPoke.ve(g, t.hinhId, false, cx, cy, k)) {
            return;
        }
        // 0.9.7: ô hộp gỗ (icon vật phẩm / icon pixel đủ màu)
        if (!KhungNro.ve(g, KhungNroBang.O, x, y, k, k)) {
            GiaoDien.khoiPixel(g, x, y, k, k, mau);
        }
        if (t.hinh == ThongBao.HINH_ICON && t.hinhId > 0) {
            IconVatPham.ve(g, (short) t.hinhId, cx, cy, k - 6, BieuTuong.QUA);
            return;
        }
        if (t.hinh == ThongBao.HINH_POKEMON) {
            ic = BieuTuong.DEX;
        }
        BieuTuong.ve(g, ic, x + 3, y + 3, k - 6, mau, 0x2E1E14);
    }

    /** Ba đốm sáng nhấp nháy quanh thẻ hiếm. */
    static void veLapLanh(Graphics g, int x, int y, int w, int h, long now) {
        int[][] diem = {{w - 10, 3}, {w / 2 + 6, h - 2}, {6, 2}};
        for (int i = 0; i < diem.length; i++) {
            int pha = (int) ((now / 90 + i * 5) % 14);
            int s = pha < 7 ? pha / 2 : (13 - pha) / 2;
            if (s <= 0) {
                continue;
            }
            int cx = x + diem[i][0], cy = y + diem[i][1];
            g.setColor(ChuDe.NEN);
            g.fillRect(cx - s, cy, 2 * s + 1, 1);
            g.fillRect(cx, cy - s, 1, 2 * s + 1);
            g.setColor(ChuDe.VANG);
            g.fillRect(cx - 1, cy - 1, 3, 3);
        }
    }

    /** Biểu ngữ trên cùng: dải màu đậm (boss đỏ / chúc mừng vàng / toàn máy chủ teal), nhãn nhỏ + 1-2 dòng chữ trắng. */
    static int veBieuNgu(Graphics g, Tin t, int y, long now) {
        if (t.kieuBieu < 0) {
            String tt = ThongBao.thuong(t.chu);
            t.kieuBieu = ThongBao.laBoss(tt) ? 1 : ThongBao.laHiem(tt) ? 2 : 0;
        }
        boolean boss = t.kieuBieu == 1;
        boolean vui = t.kieuBieu == 2;
        // màu = token chủ đề (boss DO_TOI / tin vui VANG_TOI / toàn máy chủ CHINH_TOI), đáy dải tối dần về BONG
        int nen = boss ? ChuDe.DO_TOI : vui ? ChuDe.VANG_TOI : ChuDe.CHINH_TOI;
        int nen2 = GiaoDien.pha(nen, ChuDe.BONG, 1, 4);
        int chuNhat = 0xFFE89A;
        String nhan = boss ? "BOSS" : vui ? "TIN NỔI BẬT" : "TOÀN MÁY CHỦ";
        int ic = boss ? BieuTuong.HUY_HIEU : BieuTuong.SU_KIEN;
        int rongToiDa = Math.max(200, Math.min(ChuDe.dt() ? 460 : 420, Canvas.w - 2 * 140));
        int wChu = rongToiDa - 52;
        if (t.dong == null || t.rongNgat != rongToiDa) {
            t.dong = ChuDe.ngat(t.chinh, ChuDe.THUONG, wChu, 2);
            t.rongNgat = rongToiDa;
        }
        int lh = ChuDe.cao(ChuDe.THUONG), lhN = ChuDe.cao(ChuDe.NHAN_NHO), lhP = ChuDe.cao(ChuDe.PHU);
        int soDong = Math.max(1, t.dong.length);
        int cao = Math.max(VungCham.MIN, lhN + soDong * lh + (t.phu != null ? lhP + 1 : 0) + 12);
        int rong = ChuDe.rong(nhan, ChuDe.NHAN_NHO);
        for (int k = 0; k < t.dong.length; k++) {
            rong = Math.max(rong, ChuDe.rong(t.dong[k], ChuDe.THUONG));
        }
        if (t.phu != null) {
            rong = Math.max(rong, ChuDe.rong(t.phu, ChuDe.PHU));
        }
        rong = Math.min(rongToiDa, rong + 52);
        int x = Canvas.hw - rong / 2;
        long tuoi = now - t.luc;
        int yy = y - (int) (tuoi < 220 ? (220 - tuoi) / 10 : 0);
        // 0.9.7 (NRO): biểu ngữ = khối nút lớn (boss đỏ / tin vui cam / toàn máy chủ xanh) viền vàng, ô icon hộp gỗ
        g.setColor(ChuDe.VANG);
        g.fillRect(x - 2, yy - 2, rong + 4, cao + 4);
        if (!KhungNro.ve(g, boss ? KhungNroBang.NUT_DO : vui ? KhungNroBang.NUT_CAM : KhungNroBang.NUT_XANH, x, yy, rong, cao)) {
            GiaoDien.khoiDoc(g, x, yy, rong, cao, 3, nen, nen2);
        }
        int k = Math.min(cao - 12, 30);
        int xd = x + 8, yd = yy + (cao - k) / 2;
        if (!KhungNro.ve(g, KhungNroBang.O_CHON, xd, yd, k, k)) {
            GiaoDien.khoi(g, xd, yd, k, k, 2, ChuDe.NEN);
        }
        BieuTuong.ve(g, ic, xd + 3, yd + 3, k - 6, nen, 0x2E1E14);
        Font.nenSang = false;
        int xc = xd + k + 8;
        int wd = rong - (xc - x) - 8;
        int yt = yy + 6;
        ChuDe.chu(g, nhan + (t.dem > 1 ? "  ×" + t.dem : ""), xc, yt, ChuDe.NHAN_NHO, chuNhat, 0);
        yt += lhN;
        for (int i = 0; i < t.dong.length; i++) {
            ChuDe.chuVien(g, ChuDe.cat(t.dong[i], ChuDe.THUONG, wd), xc, yt + i * lh, ChuDe.THUONG, 0xFFFFFF, ChuDe.VIEN, 0);
        }
        if (t.phu != null) {
            ChuDe.chu(g, ChuDe.cat(t.phu, ChuDe.PHU, wd), xc, yt + soDong * lh + 1, ChuDe.PHU, chuNhat, 0);
        }
        int con = t.con(now);
        if (con > 0) {
            g.setColor(ChuDe.NEN);
            g.fillRect(x + 12, yy + cao - 3, Math.max(1, (rong - 24) * con / Math.max(1, t.song)), 2);
        }
        VungCham.them(g, x, yy, rong, cao, BO_BIEU, "thong-bao-bieu-ngu");
        return yy + cao;
    }

    /** Biểu tượng thông báo theo nội dung / loại. */
    static int bieuTuong(String s, int loai) {
        if (loai == LOI) {
            return BieuTuong.LOI;
        }
        String t = s.toLowerCase();
        if (t.indexOf("album") >= 0 || t.indexOf("chụp") >= 0 || t.indexOf("sưu tầm") >= 0 || t.indexOf("bộ sưu tập") >= 0) {
            return BieuTuong.CAMERA;
        }
        if (t.indexOf("nhiệm vụ") >= 0) {
            return BieuTuong.NHIEM_VU;
        }
        if (t.indexOf(" xu") >= 0 || t.startsWith("xu") || t.indexOf("lượng") >= 0 || t.indexOf("tiền") >= 0) {
            return BieuTuong.XU;
        }
        if (t.indexOf("không") >= 0 || t.indexOf("chưa") >= 0 || t.indexOf("hết") >= 0 || t.indexOf("đầy") >= 0) {
            return BieuTuong.CANH_BAO;
        }
        if (t.indexOf("nhận") >= 0 || t.indexOf("dùng") >= 0 || t.indexOf("mặc") >= 0 || t.indexOf("tháo") >= 0 || t.indexOf("nhặt") >= 0) {
            return BieuTuong.HOP_DO;
        }
        return loai == TIN ? BieuTuong.TIN : BieuTuong.HOP_DO;
    }

    /** Dòng sức khoẻ: chữ nhỏ trắng trên viên mờ, giữa đáy (phía trên thanh chiêu, không che nút), 6 giây rồi tắt. */
    static void veSucKhoe(Graphics g) {
        if (sucKhoe == null) {
            return;
        }
        long tuoi = System.currentTimeMillis() - sucKhoeLuc;
        if (tuoi > 6000) {
            sucKhoe = null;
            return;
        }
        if (Canvas.currentScreen != Canvas.gameScr) {
            return;
        }
        Canvas.resetTrans(g);
        int a = tuoi > 5000 ? (int) (0x60 * (6000 - tuoi) / 1000) : 0x60;
        int w = Math.min(Canvas.w - 2 * 120, ChuDe.rong(sucKhoe, ChuDe.PHU) + 16);
        String t = ChuDe.cat(sucKhoe, ChuDe.PHU, w - 12);
        int h = ChuDe.cao(ChuDe.PHU) + 4;
        int y = Canvas.h - Bang.anDuoi - h - 4;
        KieuO.phu(g, Canvas.hw - w / 2, y, w, h, a << 24 | 0x1B0E06);
        ChuDe.chuGiua(g, t, Canvas.hw, y, h, ChuDe.PHU, a > 0x30 ? 0xFFFFFF : 0xC8CDD8, 2);
    }
}
