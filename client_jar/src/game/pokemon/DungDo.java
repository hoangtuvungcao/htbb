package game.pokemon;

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
        // trùng dòng mới nhất (bấm liên tục): chỉ làm mới thời gian
        if (CHU[0] != null && CHU[0].equals(s) && now - LUC[0] < SONG[0]) {
            LUC[0] = now;
            return;
        }
        for (int i = SO_DONG - 1; i > 0; i--) {
            CHU[i] = CHU[i - 1];
            LOAI[i] = LOAI[i - 1];
            LUC[i] = LUC[i - 1];
            SONG[i] = SONG[i - 1];
            DONG[i] = DONG[i - 1];
            RONG_NGAT[i] = RONG_NGAT[i - 1];
        }
        CHU[0] = s;
        LOAI[0] = loai;
        LUC[0] = now;
        // đọc kịp: 2,5 giây + 45 ms mỗi chữ, tối đa 7 giây
        SONG[0] = Math.min(7000, 2500 + s.length() * 45);
        DONG[0] = null;
        RONG_NGAT[0] = 0;
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

    /** Vẽ trên cùng (Canvas.paint, sau màn / hộp thoại / menu). */
    public static void ve(Graphics g) {
        if (CHU[0] == null && CHU[1] == null && CHU[2] == null) {
            return;
        }
        Font f = Font.borderFont;
        if (f == null) {
            return;
        }
        Canvas.resetTrans(g);
        long now = System.currentTimeMillis();
        int rongToiDa = Math.max(80, Canvas.w - 24);
        int y = Math.max(22, Canvas.h / 6);
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
            if (DONG[i] == null || RONG_NGAT[i] != rongToiDa) {
                try {
                    DONG[i] = f.splitFontBStrInLine(CHU[i], rongToiDa - 14);
                } catch (Throwable e) {
                    DONG[i] = new String[]{CHU[i]};
                }
                RONG_NGAT[i] = rongToiDa;
            }
            String[] d = DONG[i];
            int rong = 0;
            for (int k = 0; k < d.length; k++) {
                rong = Math.max(rong, f.getWidth(d[k]));
            }
            rong = Math.min(rongToiDa, rong + 14);
            int cao = d.length * 13 + 4;
            int x = Canvas.hw - rong / 2;
            int yy = y - (int) (tuoi < 160 ? (160 - tuoi) / 20 : 0);
            int tren, duoi;
            if (LOAI[i] == LOI) {
                tren = 0xB8322A;
                duoi = 0x7E1A16;
            } else if (LOAI[i] == TIN) {
                tren = 0x3A64D0;
                duoi = 0x24418C;
            } else {
                tren = 0x2E8A3A;
                duoi = 0x1E5E28;
            }
            GiaoDien.khoi(g, x, yy, rong, cao, 7, GiaoDien.VIEN);
            GiaoDien.khoiDoc(g, x + 1, yy + 1, rong - 2, cao - 2, 6, tren, duoi);
            for (int k = 0; k < d.length; k++) {
                f.drawString(g, d[k], Canvas.hw, yy + 2 + k * 13, Font.CENTER);
            }
            y = yy + cao + 3;
        }
    }
}
