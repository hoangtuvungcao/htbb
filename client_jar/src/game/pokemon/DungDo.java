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
 * Lệnh -104 server -> client (HTBB real.DungVatPham): byte loại (0 xong, 1 không dùng được, 2 tin), UTF nội dung.
 * Server chỉ gửi -104 cho client bản >= 902 (gửi số bản lúc đăng nhập), bản cũ nhận hộp thoại OK như trước.
 * Thông báo vẽ trên MỌI màn (Canvas.paint gọi {@link #ve}), kể cả lúc đang mở hành trang, và không bị "Tiện ích -
 * Thông báo: tắt" nuốt như hộp thoại OK. Dùng chung jar / APK / PC Java (port từ client_jar/src).
 */
public final class DungDo {
    public static final int LENH = -104;
    public static final int OK = 0, LOI = 1, TIN = 2;

    private static final int SO_DONG = 3;
    private static final String[] CHU = new String[SO_DONG];
    private static final int[] LOAI = new int[SO_DONG];
    private static final long[] LUC = new long[SO_DONG];
    private static final int[] SONG = new int[SO_DONG];
    /** Dòng đã ngắt theo bề ngang màn (tính lúc vẽ, Font cần màn đã dựng). */
    private static final String[][] DONG = new String[SO_DONG][];
    private static final int[] RONG_NGAT = new int[SO_DONG];
    /** số lần gộp (cùng chữ còn đang hiện) */
    private static final int[] DEM = new int[SO_DONG];

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
            kieuCuoi = k;
            chuCuoi = s;
            soNhan++;
            if (loai == TIN && k == TrangThaiNguoiChoi.DUNG && game.render.screen.GameScr.autoFight) {
                return true;   // tự đánh tự uống bình: thanh HP đã lên, không phủ thông báo mỗi bình
            }
            bao(s, loai);
            if (loai == LOI) {
                AmThanh.phat(AmThanh.CANH_BAO);
            } else if (loai == OK) {
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

    public static void bao(String s, int loai) {
        if (s == null) {
            return;
        }
        s = s.trim();
        if (s.length() == 0) {
            return;
        }
        long now = System.currentTimeMillis();
        // trùng một dòng đang hiện (bấm liên tục / server gửi lặp): gộp thành "×n", đưa lên đầu, làm mới thời gian
        int dem = 1;
        int tu = SO_DONG - 1;
        for (int i = 0; i < SO_DONG; i++) {
            if (CHU[i] != null && CHU[i].equals(s) && now - LUC[i] < SONG[i]) {
                dem = DEM[i] + 1;
                tu = i;
                break;
            }
        }
        for (int i = tu; i > 0; i--) {
            CHU[i] = CHU[i - 1];
            LOAI[i] = LOAI[i - 1];
            LUC[i] = LUC[i - 1];
            SONG[i] = SONG[i - 1];
            DONG[i] = DONG[i - 1];
            RONG_NGAT[i] = RONG_NGAT[i - 1];
            DEM[i] = DEM[i - 1];
        }
        DEM[0] = dem;
        CHU[0] = s;
        LOAI[0] = loai;
        LUC[0] = now;
        // đọc kịp: 2,5 giây + 45 ms mỗi chữ, tối đa 7 giây
        SONG[0] = Math.min(7000, 2500 + s.length() * 45);
        DONG[0] = null;
        RONG_NGAT[0] = 0;
    }

    /** Xoá mọi thông báo đang hiện (đăng xuất / kiểm thử). */
    public static void xoaHet() {
        for (int i = 0; i < SO_DONG; i++) {
            CHU[i] = null;
            DONG[i] = null;
            DEM[i] = 0;
        }
    }

    /** Còn thông báo đang hiện (để màn tự vẽ lại). */
    public static boolean dangHien() {
        long now = System.currentTimeMillis();
        for (int i = 0; i < SO_DONG; i++) {
            if (CHU[i] != null && now - LUC[i] <= SONG[i]) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ biểu ngữ / dòng sức khoẻ (0.9.5)
    /** Biểu ngữ (thông báo máy chủ dài, vd "Tài khoản chưa kích hoạt..."): không chặn, 2 dòng + "Xem" mở đủ. */
    public static void bieuNgu(String s) {
        bao(s, TIN);
        if (CHU[0] != null) {
            SONG[0] = Math.max(SONG[0], 9000);
        }
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

    static {
        for (int i = 0; i < SO_DONG; i++) {
            final int k = i;
            BO_QUA[i] = new IAction() {
                public void perform() {
                    CHU[k] = null;
                    DONG[k] = null;
                }
            };
            XEM[i] = new IAction() {
                public void perform() {
                    String t = CHU[k];
                    int l = LOAI[k];
                    CHU[k] = null;
                    DONG[k] = null;
                    if (t != null) {
                        HopThongBao.bao(l == LOI ? "Không thực hiện được" : "Thông báo", t);
                    }
                }
            };
        }
    }

    /** Vẽ trên cùng (Canvas.paint, sau màn / hộp thoại / menu): thẻ thông báo GIỮA TRÊN (không che nhân vật). */
    public static void ve(Graphics g) {
        veSucKhoe(g);
        if (CHU[0] == null && CHU[1] == null && CHU[2] == null) {
            return;
        }
        if (Canvas.currentDialog != null) {
            return;   // hộp thoại đang mở: không đè lên đầu hộp (thông báo vẫn đếm giờ)
        }
        Canvas.resetTrans(g);
        int lopCu = VungCham.lopDangVe();
        VungCham.lop(VungCham.NOI);
        long now = System.currentTimeMillis();
        // giữa trên, dưới hàng nút / khung mục tiêu của HUD; tránh thẻ trạng thái (trái) và nút (phải)
        int rongToiDa = Math.max(160, Math.min(ChuDe.dt() ? 380 : 340, Canvas.w - 2 * 150));
        // màn chơi: giữa trên (dưới hàng nút HUD), chạm = tắt / "Xem"; đang mở bảng: sát đáy, KHÔNG nhận chạm (không
        // che tab / nút của bảng), tự tắt
        boolean theGioi = Canvas.currentScreen == Canvas.gameScr;
        int y = Bang.anTren + 40;
        int lh = ChuDe.cao(ChuDe.THUONG);
        if (!theGioi) {
            int tong = 0;
            for (int i = 0; i < SO_DONG; i++) {
                if (CHU[i] != null) {
                    tong += Math.max(VungCham.MIN, 2 * lh + 10) + 4;
                }
            }
            y = Canvas.h - Bang.anDuoi - 10 - tong;
        }
        for (int i = 0; i < SO_DONG; i++) {
            if (CHU[i] == null) {
                continue;
            }
            long tuoi = now - LUC[i];
            if (tuoi > SONG[i]) {
                CHU[i] = null;
                DONG[i] = null;
                continue;
            }
            int wChu = rongToiDa - 34;
            if (DONG[i] == null || RONG_NGAT[i] != rongToiDa) {
                DONG[i] = ChuDe.ngat(CHU[i], ChuDe.THUONG, wChu, 3);
                RONG_NGAT[i] = rongToiDa;
            }
            String[] d = DONG[i];
            // dài hơn 2 dòng: biểu ngữ 2 dòng + "Xem"
            boolean xem = d.length > 2 && theGioi;
            int soDong = Math.min(2, d.length);
            int wXem = xem ? ChuDe.rong("Xem", ChuDe.NHAN) + 14 : 0;
            int rong = 0;
            for (int k = 0; k < soDong; k++) {
                rong = Math.max(rong, ChuDe.rong(d[k], ChuDe.THUONG));
            }
            rong = Math.min(rongToiDa, rong + 30 + wXem);
            int cao = Math.max(VungCham.MIN, soDong * lh + 10);
            int x = Canvas.hw - rong / 2;
            int yy = y - (int) (tuoi < 160 ? (160 - tuoi) / 20 : 0);
            int ic = cao - 8;
            int loaiIc = bieuTuong(CHU[i], LOAI[i]);
            int mau = LOAI[i] == LOI ? ChuDe.DO : loaiIc == BieuTuong.CANH_BAO ? ChuDe.CAM : LOAI[i] == TIN || loaiIc == BieuTuong.CAMERA ? ChuDe.XANH
                    : loaiIc == BieuTuong.XU ? 0xD99A00 : ChuDe.LA;
            String sl = DEM[i] > 1 ? " ×" + DEM[i] : null;
            int wSl = sl == null ? 0 : ChuDe.rong(sl, ChuDe.NHAN) + 4;
            rong = Math.min(rongToiDa, rong + ic - 8 + wSl);
            x = Canvas.hw - rong / 2;
            // 0.9.7 (NRO): bảng tối chỉ vàng, viền màu theo loại (lỗi đỏ, cảnh báo cam, tin xanh, xu vàng, nhận xanh lá),
            // ô icon pixel trái, chữ giấy da
            KhungNro.bong(g, x, yy, rong, cao);
            g.setColor(mau);
            g.fillRect(x - 1, yy - 1, rong + 2, cao + 2);
            if (!KhungNro.ve(g, KhungNroBang.TOOLTIP, x, yy, rong, cao)) {
                GiaoDien.khoi(g, x, yy, rong, cao, 2, 0x2B1A10);
            }
            Font.nenSang = false;
            int dkd = cao - 8;
            if (!KhungNro.ve(g, KhungNroBang.O, x + 4, yy + 4, dkd, dkd)) {
                GiaoDien.khoiPixel(g, x + 4, yy + 4, dkd, dkd, mau);
            }
            BieuTuong.ve(g, loaiIc, x + 4, yy + 4, dkd, mau, 0x2E1E14);
            int xc = x + 10 + dkd;
            int yt = yy + (cao - soDong * lh) / 2;
            int wd = rong - (xc - x) - 6 - wXem - wSl;
            for (int k = 0; k < soDong; k++) {
                String dk = k == soDong - 1 && xem ? ChuDe.cat(d[k] + "…", ChuDe.THUONG, wd) : ChuDe.cat(d[k], ChuDe.THUONG, wd);
                ChuDe.chu(g, dk, xc, yt + k * lh, ChuDe.THUONG, 0xF6E6C0, 0);
            }
            if (sl != null) {
                ChuDe.chu(g, sl, x + rong - wXem - 6, yt, ChuDe.NHAN, ChuDe.VANG, 1);
            }
            if (theGioi) {
                VungCham.them(g, x, yy, rong - wXem, cao, BO_QUA[i], "thong-bao-" + i);
            }
            if (xem) {
                int xx = x + rong - wXem - 4;
                ChuDe.chuGiua(g, "Xem", xx + wXem / 2, yy, cao, ChuDe.NHAN, ChuDe.VANG, 2);
                VungCham.them(g, xx, yy, wXem + 4, cao, XEM[i], "thong-bao-xem-" + i);
            }
            y = yy + cao + 4;
        }
        VungCham.lop(lopCu);
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
