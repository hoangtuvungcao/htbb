package game.pokemon;

import game.model.MainChar;
import game.model.QuickSlot;
import game.networklogic.GameService;
import game.render.Canvas;
import game.render.screen.GameScr;
import java.io.DataInputStream;
import network.Message;

/**
 * TRANG CHIÊU (0.9.8): ô nhanh nhiều trang - mặc định 2 trang, "Thẻ mở trang chiêu 3" (lượng) hoặc xu ở Huấn luyện mở trang
 * 3 (tối đa 3). Mỗi trang 5 ô gán tự do chiêu / đánh thường / thuốc. Viên "1/2" cạnh cụm chiêu (HudRong "hud-doi-bo", phím 0
 * / PC "Đổi bộ chiêu") quay vòng các trang. Máy chủ giữ trang của nhân vật (gói -103 loại 1 hai chiều, client >= 908);
 * máy chủ cũ / chưa có dữ liệu: RMS như trước, client gửi lên khi máy chủ chưa có gì.
 *
 * <pre>
 * -103 S-&gt;C loại 1: u8 số trang, u8 trang đang dùng, 3 x i64 trang (5 ô x 12 bit: (kiểu+1) 3 bit | giá trị 8 bit | buff 1
 *                    bit, ô 0 ở bit thấp), i64 giá xu mở trang 3.
 * -103 C-&gt;S loại 1: u8 trang đang dùng, 3 x i64 trang.   loại 2: mua trang 3 bằng xu.
 * </pre>
 */
public final class TrangChieu {
    private TrangChieu() {
    }

    public static final int TOI_DA = 3;
    public static final int LENH = -103;
    /** số trang mở (2 / 3) */
    public static int so = 2;
    /** máy chủ có trang chiêu (đã nhận gói) */
    public static boolean coMayChu;
    public static long giaXu;
    private static long[] cho;
    private static int trangCho = -1;
    /** gói đã nhận (kiểm thử) */
    public static int soGoi;

    /** 12 bit một ô. */
    static int ma(QuickSlot q) {
        if (q == null || q.quickslotType < 0) {
            return 0;
        }
        int gt = q.quickslotType == QuickSlot.TYPE_POTION ? q.getPotionType() : q.getSkillType();
        return ((q.quickslotType + 1) & 7) << 9 | (gt & 255) << 1 | (q.isBuff ? 1 : 0);
    }

    public static long maTrang(QuickSlot[] t) {
        long v = 0;
        for (int i = 0; t != null && i < t.length && i < 5; i++) {
            v |= ((long) ma(t[i])) << (12 * i);
        }
        return v;
    }

    public static void giaiTrang(long v, QuickSlot[] t, int lop) {
        for (int i = 0; t != null && i < t.length && i < 5; i++) {
            int m = (int) ((v >> (12 * i)) & 0xFFF);
            int kieu = ((m >> 9) & 7) - 1;
            int gt = (m >> 1) & 255;
            boolean buff = (m & 1) != 0;
            if (kieu == QuickSlot.TYPE_POTION && MoTaVatPham.ganONhanh(gt)) {
                t[i].setIsPotion(gt);   // 0.9.14: chỉ bình HP / PP (sáo gọi thú, vé... không gán ô nhanh)
            } else if (kieu == QuickSlot.TYPE_SKILL || kieu == 0) {
                t[i].setIsSkill(gt, lop, buff);
            } else {
                t[i].setIsNothing();
            }
        }
    }

    /** MessageHandler: true = đã xử lý (-103 loại 1). */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            DataInputStream r = msg.reader();
            if (r.readUnsignedByte() != 1) {
                return true;
            }
            so = Math.max(2, Math.min(TOI_DA, r.readUnsignedByte()));
            trangCho = r.readUnsignedByte();
            cho = new long[]{r.readLong(), r.readLong(), r.readLong()};
            giaXu = r.available() >= 8 ? r.readLong() : 0;
            coMayChu = true;
            soGoi++;
            apDung();
        } catch (Throwable e) {
            // gói hỏng
        }
        return true;
    }

    /** Áp dữ liệu máy chủ lên ô nhanh (gọi lại khi MainChar.loadQuickSlot tạo bộ ô). */
    public static void apDung() {
        if (cho == null || MainChar.quickSlot == null) {
            return;
        }
        long[] c = cho;
        cho = null;
        if (c[0] == 0 && c[1] == 0 && c[2] == 0) {
            gui();   // máy chủ chưa có: gửi bộ ô đang có trên máy (RMS) lên
            return;
        }
        int lop = Canvas.gameScr != null && Canvas.gameScr.mainChar != null ? Canvas.gameScr.mainChar.clazz : 0;
        for (int i = 0; i < TOI_DA && i < MainChar.quickSlot.length; i++) {
            giaiTrang(c[i], MainChar.quickSlot[i], lop);
        }
        GameScr.indexTabSlot = Math.max(0, Math.min(so - 1, trangCho));
        game.render.screen.RMS.luuQuickSlotMay();
    }

    /** Gửi trang chiêu lên máy chủ (RMS.saveQuickSlot gọi sau mỗi lần đổi). */
    public static void gui() {
        if (!coMayChu || MainChar.quickSlot == null) {
            return;
        }
        try {
            Message m = new Message((byte) LENH);
            m.writer().writeByte(1);
            m.writer().writeByte(Math.max(0, Math.min(so - 1, GameScr.indexTabSlot)));
            for (int i = 0; i < TOI_DA; i++) {
                m.writer().writeLong(i < MainChar.quickSlot.length ? maTrang(MainChar.quickSlot[i]) : 0L);
            }
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // chưa nối
        }
    }

    /** Mua trang 3 bằng xu (máy chủ kiểm giá / số trang). */
    public static void muaXu() {
        try {
            Message m = new Message((byte) LENH);
            m.writer().writeByte(2);
            GameService.gI().session.sendMessage(m);
            m.cleanup();
        } catch (Throwable e) {
            // chưa nối
        }
    }

    /** Trang kế tiếp (quay vòng) - viên "1/2", phím 0, phím PC. */
    public static void tiep() {
        int n = Math.max(1, so);
        GameScr.indexTabSlot = (GameScr.indexTabSlot + 1) % n;
    }

    /** Nhãn viên đổi trang: "1/2", "2/3". */
    public static String nhan() {
        return (GameScr.indexTabSlot + 1) + "/" + Math.max(1, so);
    }
}
