package com.htbb.pokemon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * KENH CHAT (0.9.8, ban client 908) - phan THUAN (khong CSDL, khong mang): cau hinh kenh, luat gui tung kenh, lich su
 * the gioi / bang trong bo nho, dong goi tin -124. Phan noi vao game: real.KenhChatGame.
 *
 * <p>Kenh: {@link #BAN_DO} (chat gan, lenh 27 cu), {@link #THE_GIOI}, {@link #BANG} (bang hoi = "doi" cu cua game,
 * lenh -18), {@link #DOI} (nhom / to doi - truoc 0.9.8 KHONG co chat), {@link #RIENG} (tin rieng, lenh -5 cu - truoc
 * 0.9.8 nguoi nhan offline thi tin MAT), {@link #HE_THONG} (chi client, khong gui duoc).
 *
 * <pre>
 * -124 S-&gt;C (chi client ban &gt;= {@link #BAN_CLIENT}):
 *   byte 0 cau hinh: byte phien ban, byte n kenh, n x (byte id, UTF ten, byte gui duoc, short cap toi thieu, int xu,
 *                    short giay cho), short do dai toi da
 *   byte 1 tin:      byte kenh, byte co (CO_*), long luc (ms), short id nguoi gui (-1), UTF ten gui, UTF doi phuong
 *                    (tin rieng: ten nguoi kia; con lai ""), UTF noi dung, UTF nhan (chuc vu bang, "" = khong)
 *   byte 2 ban be:   short n, n x (UTF ten, byte online, short cap)
 *   byte 3 chan:     short n, n x UTF ten
 *   byte 4 ket qua:  byte kenh, byte ma (KQ_*), short giay cho con lai, UTF ly do
 *   byte 5 ban online / offline: UTF ten, byte online
 *   byte 6 id theo ten: byte viec, UTF ten, short id (-1 = offline), byte cung ban do
 *   byte 7 doi:      UTF ten doi ("" = chua vao doi), short so dang online, short tong thanh vien
 * Chat chi trong PHIEN (chu game 22/10/2026, kieu Lang La): khong gui lai lich su khi vao game, tin rieng cho nguoi offline
 * KHONG gui (bao nguoi gui) - khong con hop thu tob_chat_cho.
 * -124 C-&gt;S: byte 0 xin lai (cau hinh + chan + ban be + lich su), 1 gui (byte kenh, UTF dich, UTF noi dung),
 *   2 chan (byte 1 chan / 0 bo, UTF ten), 3 xin ban be, 6 tra id (byte viec, UTF ten)
 * </pre>
 * Client cu (&lt; 908) van nhan dung goi cu: the gioi = -60 (chu chay), bang = -18, tin rieng = -5, chat doi = -5 tu
 * "Nhóm" (tra loi vao the "Nhóm" la chat doi).
 */
public final class KenhChatLogic {

    private KenhChatLogic() {
    }

    // ================================================================== hang so

    public static final int LENH = -124;
    /** Ban client dau tien biet lenh -124 (0.9.8). */
    public static final int BAN_CLIENT = 908;
    public static final int PHIEN_BAN = 1;

    public static final byte BAN_DO = 0, THE_GIOI = 1, BANG = 2, DOI = 3, RIENG = 4, HE_THONG = 5;
    public static final int SO_KENH = 6;
    public static final String[] TEN_KENH = {"Bản đồ", "Thế giới", "Đội", "Nhóm", "Bạn bè", "Hệ thống"};

    /** goi S-&gt;C */
    public static final byte G_CAU_HINH = 0, G_TIN = 1, G_BAN = 2, G_CHAN = 3, G_KET_QUA = 4, G_TRANG_THAI = 5, G_ID = 6, G_DOI = 7;
    /** goi C-&gt;S */
    public static final byte K_XIN = 0, K_GUI = 1, K_CHAN = 2, K_XIN_BAN = 3, K_TRA_ID = 6;

    /** co cua mot tin */
    public static final int CO_CUA_MINH = 1, CO_HOP_THU = 2, CO_LICH_SU = 4, CO_LOA = 8, CO_GM = 16;

    /** ma ket qua gui */
    public static final byte KQ_DA_GUI = 0, KQ_LOI = 1, KQ_CHO = 2, KQ_HOP_THU = 3;

    /** viec cua lenh tra id (menu ten nguoi gui) */
    public static final byte VIEC_MOI_DOI = 0, VIEC_KET_BAN = 1, VIEC_XEM = 2;

    /** Ten the tin rieng gia cho client cu: tra loi vao the nay = chat doi (ten nhan vat khong co dau, khong trung). */
    public static final String TEN_NHOM_CU = "Nhóm";

    // ================================================================== cau hinh (sv.chat.*, lop co_dinh)

    public static volatile int capGan = 5;
    public static volatile int capTheGioi = 10;
    public static volatile int xuTheGioi = 100;
    public static volatile int giayTheGioi = 30;
    public static volatile int capRieng = 3;
    public static volatile int doDai = 80;
    public static volatile int lichSu = 50;
    public static volatile int chanToiDa = 100;

    public static synchronized void docCauHinh(final Properties p) {
        capGan = so(p, "sv.chat.capGan", 5, 0, 200);
        capTheGioi = so(p, "sv.chat.capTheGioi", 10, 0, 200);
        xuTheGioi = so(p, "sv.chat.xuTheGioi", 100, 0, 100000000);
        giayTheGioi = so(p, "sv.chat.giayTheGioi", 30, 0, 3600);
        capRieng = so(p, "sv.chat.capRieng", 3, 0, 200);
        doDai = so(p, "sv.chat.doDai", 80, 10, 200);
        lichSu = so(p, "sv.chat.lichSu", 50, 0, 200);
        chanToiDa = so(p, "sv.chat.chanToiDa", 100, 0, 1000);
    }

    public static String moTa() {
        return "KENH CHAT -> ban do cap " + capGan + ", the gioi cap " + capTheGioi + " / " + xuTheGioi + " xu / "
                + giayTheGioi + "s, rieng cap " + capRieng + ", dai " + doDai + ", lich su " + lichSu + ", chan toi da " + chanToiDa;
    }

    private static int so(final Properties p, final String k, final int macDinh, final int min, final int max) {
        try {
            final String v = p == null ? null : p.getProperty(k);
            if (v == null || v.trim().isEmpty()) {
                return macDinh;
            }
            return Math.max(min, Math.min(max, Integer.parseInt(v.trim())));
        } catch (final NumberFormatException e) {
            return macDinh;
        }
    }

    /** Kiem thu: ve mac dinh + xoa lich su. */
    public static synchronized void datLaiChoKiemThu() {
        docCauHinh(new Properties());
        synchronized (LICH_SU_THE_GIOI) {
            LICH_SU_THE_GIOI.clear();
        }
        synchronized (LICH_SU_BANG) {
            LICH_SU_BANG.clear();
        }
    }

    // ================================================================== mot tin

    public static final class Tin {
        public final byte kenh;
        public final long luc;
        public final short idGui;
        public final String tenGui, doiPhuong, noiDung, nhan;
        public final int co;

        public Tin(final byte kenh, final long luc, final short idGui, final String tenGui, final String doiPhuong,
                final String noiDung, final String nhan, final int co) {
            this.kenh = kenh;
            this.luc = luc;
            this.idGui = idGui;
            this.tenGui = tenGui == null ? "" : tenGui;
            this.doiPhuong = doiPhuong == null ? "" : doiPhuong;
            this.noiDung = noiDung == null ? "" : noiDung;
            this.nhan = nhan == null ? "" : nhan;
            this.co = co;
        }

        public Tin voiCo(final int coMoi) {
            return new Tin(this.kenh, this.luc, this.idGui, this.tenGui, this.doiPhuong, this.noiDung, this.nhan, coMoi);
        }

        /** Dong cho client cu ("nhan ten: noi dung"). */
        public String dongCu() {
            return this.nhan + this.tenGui + ": " + this.noiDung;
        }
    }

    // ================================================================== lich su (bo nho)

    private static final List<Tin> LICH_SU_THE_GIOI = new ArrayList<Tin>();
    private static final java.util.Map<Integer, List<Tin>> LICH_SU_BANG = new java.util.HashMap<Integer, List<Tin>>();

    public static void ghiTheGioi(final Tin t) {
        synchronized (LICH_SU_THE_GIOI) {
            them(LICH_SU_THE_GIOI, t);
        }
    }

    public static List<Tin> lichSuTheGioi() {
        synchronized (LICH_SU_THE_GIOI) {
            return new ArrayList<Tin>(LICH_SU_THE_GIOI);
        }
    }

    public static void ghiBang(final int idBang, final Tin t) {
        synchronized (LICH_SU_BANG) {
            List<Tin> l = LICH_SU_BANG.get(Integer.valueOf(idBang));
            if (l == null) {
                l = new ArrayList<Tin>();
                LICH_SU_BANG.put(Integer.valueOf(idBang), l);
            }
            them(l, t);
        }
    }

    public static List<Tin> lichSuBang(final int idBang) {
        synchronized (LICH_SU_BANG) {
            final List<Tin> l = LICH_SU_BANG.get(Integer.valueOf(idBang));
            return l == null ? new ArrayList<Tin>() : new ArrayList<Tin>(l);
        }
    }

    private static void them(final List<Tin> l, final Tin t) {
        l.add(t);
        final int tran = Math.max(0, lichSu);
        while (l.size() > tran) {
            l.remove(0);
        }
    }

    // ================================================================== chu

    /** Chuan hoa noi dung: bo ky tu dieu khien, gop khoang trang, cat do dai. null = rong (khong gui). */
    public static String chuan(final String s) {
        if (s == null) {
            return null;
        }
        final StringBuilder b = new StringBuilder(s.length());
        boolean cach = false;
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (Character.isWhitespace(c) || Character.isSpaceChar(c) || Character.isISOControl(c)) {
                cach = b.length() > 0;
                continue;
            }
            final int loai = Character.getType(c);
            if (loai == Character.FORMAT || c == 0xFEFF) {
                continue;
            }
            if (cach) {
                b.append(' ');
                cach = false;
            }
            b.append(c);
        }
        String r = b.toString();
        if (r.length() > doDai) {
            r = r.substring(0, doDai).trim();
        }
        return r.isEmpty() || com.htbb.chongspam.ChongSpam.rong(r) ? null : r;
    }

    public static String khoaTen(final String ten) {
        // ten huan luyen vien AI client thay la "bot_ai: ten" -> khoa tra cuu la ten that
        return ten == null ? "" : HlvAi.boTienTo(ten).toLowerCase(Locale.ROOT);
    }

    // ================================================================== luat gui (thuan)

    /** Trang thai nguoi gui de kiem luat (lay tu Char o real.KenhChatGame). */
    public static final class NguoiGui {
        public int cap;
        public long xu;
        public boolean admin;
        /** con bi cam chat (GM / tu dong): ly do, null = khong */
        public String biCam;
        public boolean coBang, coDoi;
        /** lan gui the gioi thanh cong / thu gan nhat (ms, 0 = chua) */
        public long lucTheGioi;
    }

    /** Ket qua kiem luat: null = duoc gui. */
    public static final class Loi {
        public final byte ma;
        public final String lyDo;
        public final int giayCho;

        Loi(final byte ma, final String lyDo, final int giayCho) {
            this.ma = ma;
            this.lyDo = lyDo;
            this.giayCho = giayCho;
        }
    }

    /**
     * Luat gui cua kenh (truoc chong spam / tu xau): cam chat, cap, xu, gian cach the gioi, thanh vien bang / doi.
     * @return null = duoc gui.
     */
    public static Loi kiem(final byte kenh, final NguoiGui n, final long bayGio) {
        if (kenh < 0 || kenh >= SO_KENH || kenh == HE_THONG) {
            return new Loi(KQ_LOI, "Kênh này không gửi được.", 0);
        }
        if (n.biCam != null && !n.admin) {
            return new Loi(KQ_LOI, n.biCam, 0);
        }
        switch (kenh) {
            case BAN_DO:
                if (!n.admin && n.cap < capGan) {
                    return new Loi(KQ_LOI, "Cần cấp " + capGan + " để chat bản đồ.", 0);
                }
                return null;
            case THE_GIOI: {
                if (n.admin) {
                    return null;
                }
                if (n.cap < capTheGioi) {
                    return new Loi(KQ_LOI, "Cần cấp " + capTheGioi + " để chat thế giới.", 0);
                }
                final long cho = n.lucTheGioi <= 0L ? 0L : giayTheGioi * 1000L - (bayGio - n.lucTheGioi);
                if (cho > 0L) {
                    final int giay = (int) ((cho + 999L) / 1000L);
                    return new Loi(KQ_CHO, "Kênh thế giới: chờ " + giay + " giây nữa.", giay);
                }
                if (xuTheGioi > 0 && n.xu < xuTheGioi) {
                    return new Loi(KQ_LOI, "Cần " + xuTheGioi + " xu để chat thế giới.", 0);
                }
                return null;
            }
            case BANG:
                return n.coBang ? null : new Loi(KQ_LOI, "Bạn chưa vào đội.", 0);
            case DOI:
                return n.coDoi ? null : new Loi(KQ_LOI, "Bạn chưa vào nhóm.", 0);
            case RIENG:
                if (!n.admin && n.cap < capRieng) {
                    return new Loi(KQ_LOI, "Cần cấp " + capRieng + " để nhắn riêng.", 0);
                }
                return null;
            default:
                return null;
        }
    }

    /** Ly do chong spam (com.htbb.chongspam.ChongSpam.KetQua) cho goi ket qua. */
    public static String lyDoSpam(final com.htbb.chongspam.ChongSpam.KetQua kq) {
        if (kq == null) {
            return "Tin không gửi được.";
        }
        switch (kq) {
            case DANG_CAM:
                return "Bạn đang bị tạm cấm chat vì spam.";
            case QUANG_CAO:
                return "Không được gửi liên kết, số điện thoại hay quảng cáo.";
            case QUA_NHANH:
                return "Bạn chat quá nhanh, chờ một chút.";
            case TRUNG_LAP:
                return "Đừng gửi lặp lại cùng một nội dung.";
            case KENH_BAN:
                return "Kênh thế giới đang bận, thử lại sau ít giây.";
            default:
                return "Tin không gửi được.";
        }
    }

    // ================================================================== goi tin

    public static void viet(final DataOutputStream o, final Tin t, final int coThem) throws IOException {
        o.writeByte(G_TIN);
        o.writeByte(t.kenh);
        o.writeByte(t.co | coThem);
        o.writeLong(t.luc);
        o.writeShort(t.idGui);
        o.writeUTF(cat(t.tenGui, 40));
        o.writeUTF(cat(t.doiPhuong, 40));
        o.writeUTF(cat(t.noiDung, 400));
        o.writeUTF(cat(t.nhan, 20));
    }

    public static void vietCauHinh(final DataOutputStream o, final boolean[] guiDuoc) throws IOException {
        o.writeByte(G_CAU_HINH);
        o.writeByte(PHIEN_BAN);
        o.writeByte(SO_KENH);
        for (int i = 0; i < SO_KENH; i++) {
            o.writeByte(i);
            o.writeUTF(TEN_KENH[i]);
            o.writeByte(guiDuoc != null && i < guiDuoc.length && guiDuoc[i] ? 1 : 0);
            o.writeShort(i == BAN_DO ? capGan : i == THE_GIOI ? capTheGioi : i == RIENG ? capRieng : 0);
            o.writeInt(i == THE_GIOI ? xuTheGioi : 0);
            o.writeShort(i == THE_GIOI ? giayTheGioi : 0);
        }
        o.writeShort(doDai);
    }

    public static void vietKetQua(final DataOutputStream o, final byte kenh, final byte ma, final int giayCho, final String lyDo)
            throws IOException {
        o.writeByte(G_KET_QUA);
        o.writeByte(kenh);
        o.writeByte(ma);
        o.writeShort(Math.max(0, Math.min(Short.MAX_VALUE, giayCho)));
        o.writeUTF(cat(lyDo, 200));
    }

    /** Loai 7: UTF ten doi ("" = chua vao doi), short dang online, short tong thanh vien. */
    public static void vietDoi(final DataOutputStream o, final String tenDoi, final int online, final int tong) throws IOException {
        o.writeByte(G_DOI);
        o.writeUTF(cat(tenDoi == null ? "" : tenDoi, 40));
        o.writeShort(Math.max(0, Math.min(Short.MAX_VALUE, online)));
        o.writeShort(Math.max(0, Math.min(Short.MAX_VALUE, tong)));
    }

    public static void vietTen(final DataOutputStream o, final byte goi, final List<String> ten) throws IOException {
        o.writeByte(goi);
        final int n = Math.min(ten.size(), 500);
        o.writeShort(n);
        for (int i = 0; i < n; i++) {
            o.writeUTF(cat(ten.get(i), 40));
        }
    }

    static String cat(final String s, final int n) {
        if (s == null) {
            return "";
        }
        return s.length() > n ? s.substring(0, n) : s;
    }
}
