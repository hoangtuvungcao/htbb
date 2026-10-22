package game.pokemon;

import game.model.Char;
import game.model.MainChar;
import game.render.Canvas;
import java.io.DataInputStream;
import network.Message;

/**
 * KHO TRẠNG THÁI người chơi phía client (0.9.3) - một nơi nhận MỌI gói đổi trạng thái của nhân vật mình (túi 16, HP /
 * PP 22 / 60 qua MauPP, thông tin 3, trang bị 37 / pet, gói -105 của server) và ghi thẳng vào các trường Char /
 * MainChar mà HUD, Hành trang, Cửa hàng, màn Nhân vật đọc MỖI khung vẽ -> giá trị mới hiện ngay khung kế tiếp sau
 * khi gói được xử lý, không chờ gói định kỳ hay mở lại bảng.
 *
 * - {@link #phienBan} tăng mỗi lần trạng thái đổi ({@link #doi}); bảng có danh sách dựng sẵn (Hành trang jar) so phiên
 *   bản để dựng lại. {@link #doiCuoi} = các phần vừa đổi ({@link #HP}, {@link #TIEN}, {@link #TUI}...).
 * - Gói -105 (server real.HanhDongVatPham, gửi NGAY sau mỗi thao tác vật phẩm, kể cả khi bị từ chối): trạng thái NHỎ
 *   ĐẦY ĐỦ của server = nguồn sự thật: HP / PP / max, xu, lượng, lượng khoá, cấp, EXP, số bình từng loại. Áp đè mọi
 *   thay đổi client đoán trước (trừ bình, cộng HP khi bấm bình, trừ xu khi bấm mua) -> bị từ chối thì tự trả lại.
 *
 * Định dạng -105: byte phiên bản (1), byte kiểu thao tác, int hp, int maxhp, int mp, int maxmp, long xu, int lượng,
 * int lượng khoá, short cấp, long exp, byte n, (byte id bình, int số) x n, short số món trong túi, short số món đang mặc;
 * 0.9.7 (máy chủ mới NỐI THÊM, máy chủ cũ không có - đọc khi còn byte): short oDaMo (sức chứa hành trang), short
 * oToiDaVe (trần mở bằng vé / xu), byte oMoiVe (+ô mỗi vé), int giaXuBuocKe (giá xu lần mở kế, -1 = hết), int soVe,
 * short idVe (id nguyên liệu của vé), byte loaiVe (1 = nguyên liệu). Lệnh mở rộng: {@link #LENH_MO_RONG} (-107).
 */
public final class TrangThaiNguoiChoi {
    public static final int LENH = -105;
    public static final int HP = 1, TIEN = 2, TUI = 4, TRANG_BI = 8, PET = 16, EXP = 32, TAT_CA = 63;
    /** kiểu thao tác (khớp server HanhDongVatPham) */
    public static final int KHAC = 0, MUA = 1, BAN = 2, DUNG = 3, MAC = 4, THAO = 5, VUT = 6, TACH = 7, GIAO_DICH = 8, TANG = 9;

    /** tăng mỗi lần trạng thái đổi */
    public static volatile int phienBan;
    /** các phần vừa đổi (OR dồn, bảng đọc xong tự xoá nếu muốn) */
    public static volatile int doiCuoi;

    /** Trạng thái server ở gói -105 cuối (kiểm thử / chẩn đoán). */
    public static int soGoi, kieuCuoi = -1, hpMay, maxhpMay, mpMay, maxmpMay, luongMay, luongKhoaMay, capMay, soTuiMay, soMacMay;
    public static long xuMay, expMay;
    /** 0.9.7 hành trang: sức chứa (-1 = máy chủ cũ chưa gửi), trần mở bằng vé / xu, ô mỗi vé, giá xu lần kế, số vé, id vé */
    public static int oDaMo = -1, oToiDaVe = -1, oMoiVe = 10, giaXuMoRong = -1, soVe, idVe = -1, loaiVe = -1;
    /** C->S mở rộng hành trang: byte kieu 0 hỏi trạng thái, 1 dùng vé, 2 mua bằng xu */
    public static final int LENH_MO_RONG = -107;

    /** Sức chứa hành trang (ô): gói -105 mới, không có thì 42 x số trang (gói thông tin 3). */
    public static int sucChua(MainChar mc) {
        if (oDaMo > 0) {
            return oDaMo;
        }
        return 42 * Math.max(1, mc == null ? 1 : mc.limItemBag);
    }

    /** Trần ô hiện ở lưới (ô khoá cuối lưới tới đây). */
    public static int tran(MainChar mc) {
        return Math.max(sucChua(mc), oToiDaVe > 0 ? oToiDaVe : sucChua(mc));
    }

    /** Gửi -107 (kiểu 0 hỏi, 1 dùng vé, 2 mua xu). */
    public static void guiMoRong(int kieu) {
        try {
            game.networklogic.GameService.gI().moRongTui(kieu);
        } catch (Throwable e) {
            // chưa nối máy chủ
        }
    }
    public static final int[] binhMay = new int[256];

    private TrangThaiNguoiChoi() {
    }

    /** Một phần trạng thái vừa đổi (gói tin vừa áp vào Char): tăng phiên bản, xin vẽ lại ngay. */
    public static void doi(int phan) {
        phienBan++;
        doiCuoi |= phan;
        try {
            if (Canvas.instance != null) {
                Canvas.instance.repaint();
            }
        } catch (Throwable e) {
            // chưa có màn
        }
    }

    /** true = đã xử lý (gói -105). Gọi ở đầu MessageHandler.onMessage. */
    public static boolean nhan(Message msg) {
        if (msg.command != LENH) {
            return false;
        }
        try {
            MainChar mc = Canvas.gameScr == null ? null : Canvas.gameScr.mainChar;
            apDung(msg.reader(), mc);
        } catch (Throwable e) {
            // gói hỏng: bỏ qua
        }
        return true;
    }

    /** Đọc gói -105 và áp lên nhân vật mc (mc null: chỉ ghi bản server - kiểm thử). */
    public static void apDung(DataInputStream r, Char mc) throws java.io.IOException {
        r.readByte();   // phiên bản gói
        kieuCuoi = r.readByte();
        hpMay = r.readInt();
        maxhpMay = r.readInt();
        mpMay = r.readInt();
        maxmpMay = r.readInt();
        xuMay = r.readLong();
        luongMay = r.readInt();
        luongKhoaMay = r.readInt();
        capMay = r.readShort();
        expMay = r.readLong();
        for (int i = 0; i < binhMay.length; i++) {
            binhMay[i] = 0;
        }
        int n = r.readByte();
        for (int i = 0; i < n; i++) {
            int id = r.readUnsignedByte();
            int so = r.readInt();
            if (id < binhMay.length) {
                binhMay[id] = so;
            }
        }
        soTuiMay = r.readShort();
        soMacMay = r.readShort();
        if (r.available() >= 2 + 2 + 1 + 4 + 4 + 2 + 1) {
            int dm = r.readShort();
            int td = r.readShort();
            int mv = r.readByte();
            int gx = r.readInt();
            int sv = r.readInt();
            int iv = r.readShort();
            int lv = r.readByte();
            if (oDaMo != dm || soVe != sv || giaXuMoRong != gx) {
                doiCuoi |= TUI;
            }
            oDaMo = dm;
            oToiDaVe = td;
            oMoiVe = mv;
            giaXuMoRong = gx;
            soVe = sv;
            idVe = iv;
            loaiVe = lv;
        }
        soGoi++;
        if (mc == null) {
            return;
        }
        int phan = 0;
        if (mc.maxhp != maxhpMay || mc.hp != hpMay || mc.maxmp != maxmpMay || mc.mp != mpMay) {
            phan |= HP;
        }
        mc.maxhp = maxhpMay;
        mc.maxmp = maxmpMay;
        MauPP.datHp(mc, hpMay);
        MauPP.datPp(mc, mpMay);
        if (mc.charXu != xuMay || mc.luong != luongMay || mc.luongKhoa != luongKhoaMay) {
            phan |= TIEN;
        }
        mc.charXu = xuMay;
        mc.luong = luongMay;
        mc.luongKhoa = luongKhoaMay;
        if (capMay > 0 && capMay <= 127 && mc.level != capMay) {
            mc.level = (byte) capMay;
            phan |= EXP;
        }
        // bình: số server là tuyệt đối (trả lại bình client đã trừ trước khi server từ chối)
        if (mc.potions != null) {
            for (int i = 0; i < mc.potions.length; i++) {
                int so = i < binhMay.length ? binhMay[i] : 0;
                if (mc.potions[i] != so) {
                    mc.potions[i] = so;
                    phan |= TUI;
                }
                try {
                    if (MainChar.listPotion != null && i < MainChar.listPotion.length && MainChar.listPotion[i] != null) {
                        MainChar.listPotion[i].number = so;
                    }
                } catch (Throwable e) {
                    // bảng bình chưa nạp
                }
            }
        }
        doi(phan == 0 ? 0 : phan);
    }

    /** Client khớp bản server ở gói -105 cuối chưa (kiểm thử / F9). */
    public static boolean khop(Char mc) {
        if (mc == null || soGoi == 0) {
            return false;
        }
        if (mc.hp != hpMay || mc.maxhp != maxhpMay || mc.mp != mpMay || mc.maxmp != maxmpMay || mc.charXu != xuMay
                || mc.luong != luongMay || mc.luongKhoa != luongKhoaMay) {
            return false;
        }
        if (mc.potions != null) {
            for (int i = 0; i < mc.potions.length && i < binhMay.length; i++) {
                if (mc.potions[i] != binhMay[i]) {
                    return false;
                }
            }
        }
        return true;
    }
}
