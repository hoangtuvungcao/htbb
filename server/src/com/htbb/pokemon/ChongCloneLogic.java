package com.htbb.pokemon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Chong clone chuyen xu (chu game duyet 19/10/2026): "tao nhieu clone de giao dich xu qua thi sao?".
 *
 * Lop THUAN (khong CSDL, khong mang) - test/KiemChongClone.java. Ban keo noi vao game: {@link ChongClone}.
 *
 * Bon lop chan, ap cho MOI duong chuyen gia tri giua hai nguoi choi (giao dich truc tiep, cho ky gui, giao dich
 * thu cung lay xu, cho trang tri, cho sinh thai, thach dau cuoc xu):
 * <ol>
 *   <li>cap toi thieu cua CA HAI ben (chong_clone.cap_toi_thieu, mac dinh 20);</li>
 *   <li>tuoi tai khoan (gio tu luc dang ky, chong_clone.tuoi_tai_khoan_gio) va gio choi cua nhan vat
 *       (chong_clone.gio_choi_toi_thieu);</li>
 *   <li>moi nguoi nhan, moi ngay: toi da N nhan vat CUNG THIET BI hoac CUNG IP duoc chuyen cho ho
 *       (chong_clone.nv_cung_may_toi_da) - nhan vat thu N+1 tu cung may bi chan;</li>
 *   <li>gia ky gui phai nam trong [gia tham chieu x min%, max(gia tham chieu x lan, thu nhap gio x so gio)].</li>
 * </ol>
 * Cong them: moi lan chuyen ghi vao tob_log_chuyen_gia_tri, danh dau "nghi clone" khi mot nguoi nhan co tu
 * chong_clone.nghi_so_nguoi_gui nguoi gui khac nhau trong ngay, hai nguoi gui cung may/IP, hoac nguoi gui cung
 * may voi nguoi nhan. Trang /admin/kinh-te (muc "Nghi clone") doc bang nay.
 */
public final class ChongCloneLogic {

    private ChongCloneLogic() {
    }

    // ------------------------------------------------------------------ cau hinh

    public static final String K_BAT = "chong_clone.bat";
    public static final String K_CAP = "chong_clone.cap_toi_thieu";
    public static final String K_TUOI_TK_GIO = "chong_clone.tuoi_tai_khoan_gio";
    public static final String K_GIO_CHOI = "chong_clone.gio_choi_toi_thieu";
    public static final String K_CUNG_MAY = "chong_clone.nv_cung_may_toi_da";
    public static final String K_NGHI_SO_GUI = "chong_clone.nghi_so_nguoi_gui";
    public static final String K_GIA_LAN = "chong_clone.ky_gui_gia_toi_da_lan";
    public static final String K_GIA_GIO = "chong_clone.ky_gui_gia_toi_da_gio";
    public static final String K_GIA_MIN_PT = "chong_clone.ky_gui_gia_toi_thieu_phan_tram";
    public static final String K_BAN_NPC_DO_KHOA = "chong_clone.ban_npc_do_khoa";

    /** Anh chup cau hinh (bat bien). */
    public static final class CauHinh {
        public final boolean bat;
        public final int capToiThieu;
        public final int tuoiTaiKhoanGio;
        public final int gioChoiToiThieu;
        public final int nvCungMayToiDa;
        public final int nghiSoNguoiGui;
        public final int giaToiDaLan;
        public final int giaToiDaGio;
        public final int giaToiThieuPhanTram;
        public final boolean banNpcDoKhoa;

        public CauHinh(final boolean bat, final int capToiThieu, final int tuoiTaiKhoanGio, final int gioChoiToiThieu,
                final int nvCungMayToiDa, final int nghiSoNguoiGui, final int giaToiDaLan, final int giaToiDaGio,
                final int giaToiThieuPhanTram, final boolean banNpcDoKhoa) {
            this.bat = bat;
            this.capToiThieu = Math.max(0, capToiThieu);
            this.tuoiTaiKhoanGio = Math.max(0, tuoiTaiKhoanGio);
            this.gioChoiToiThieu = Math.max(0, gioChoiToiThieu);
            this.nvCungMayToiDa = Math.max(1, nvCungMayToiDa);
            this.nghiSoNguoiGui = Math.max(2, nghiSoNguoiGui);
            this.giaToiDaLan = Math.max(1, giaToiDaLan);
            this.giaToiDaGio = Math.max(1, giaToiDaGio);
            this.giaToiThieuPhanTram = Math.max(0, Math.min(100, giaToiThieuPhanTram));
            this.banNpcDoKhoa = banNpcDoKhoa;
        }

        /** Mac dinh san xuat (khop mac_dinh cua bang cau_hinh_danh_muc trong sql/htbb.sql). */
        public static CauHinh macDinh() {
            return new CauHinh(true, 20, 24, 10, 2, 3, 10, 200, 5, false);
        }
    }

    // ------------------------------------------------------------------ mot ben cua giao dich

    /** Mot ben (nguoi gui xu / nguoi nhan xu). tuoiTaiKhoanMs / phutChoi = -1 khi khong biet (khong chan). */
    public static final class Ben {
        public final int charId;
        public final String ten;
        public final int userId;
        public final int cap;
        public final String may;
        public final String ip;
        public final long tuoiTaiKhoanMs;
        public final long phutChoi;

        public Ben(final int charId, final String ten, final int userId, final int cap, final String may, final String ip,
                final long tuoiTaiKhoanMs, final long phutChoi) {
            this.charId = charId;
            this.ten = ten == null ? "" : ten;
            this.userId = userId;
            this.cap = cap;
            this.may = sach(may);
            this.ip = sach(ip);
            this.tuoiTaiKhoanMs = tuoiTaiKhoanMs;
            this.phutChoi = phutChoi;
        }

        static String sach(final String s) {
            if (s == null) {
                return "";
            }
            final String t = s.trim();
            return t.length() > 64 ? t.substring(0, 64) : t;
        }
    }

    // ------------------------------------------------------------------ chan theo cap / tuoi

    /**
     * Ly do {@code b} KHONG duoc chuyen/nhan xu voi nguoi khac, null neu duoc.
     * @param laMinh true = loi nhan cho chinh nguoi do ("Bạn cần..."), false = noi ve nguoi kia.
     */
    public static String lyDoChan(final CauHinh c, final Ben b, final boolean laMinh) {
        return lyDoChan(c, b, laMinh, null);
    }

    /** Cum tu viec bi chan mac dinh (khong biet viec cu the): giu nguyen chu cu - noi goi .replace() theo chuoi nay. */
    public static final String VIEC_MAC_DINH_CAP = "giao dịch / mua bán xu với người chơi khác";
    public static final String VIEC_MAC_DINH_TUOI = "giao dịch xu";

    /**
     * Nhu tren, {@code viec} = viec dang bi chan, ghi DUNG vao loi nhan ("giao dịch với người chơi khác" cho giao dich
     * truc tiep - ca vat pham lan xu; cong kiem 25/10/2026: giao dich chi co thuoc van bao "giao dịch / mua bán xu").
     * null = chu mac dinh cu.
     */
    public static String lyDoChan(final CauHinh c, final Ben b, final boolean laMinh, final String viec) {
        if (c == null || !c.bat || b == null) {
            return null;
        }
        final String vCap = viec == null ? VIEC_MAC_DINH_CAP : viec;
        final String vTuoi = viec == null ? VIEC_MAC_DINH_TUOI : viec;
        final String ai = laMinh ? "Bạn" : ("Người chơi " + b.ten);
        if (b.cap < c.capToiThieu) {
            return ai + " cần đạt cấp " + c.capToiThieu + " mới " + vCap + " được.";
        }
        if (c.tuoiTaiKhoanGio > 0 && b.tuoiTaiKhoanMs >= 0 && b.tuoiTaiKhoanMs < c.tuoiTaiKhoanGio * 3600000L) {
            final long conPhut = (c.tuoiTaiKhoanGio * 3600000L - b.tuoiTaiKhoanMs + 59999L) / 60000L;
            return (laMinh ? "Tài khoản của bạn" : "Tài khoản của " + b.ten) + " cần tạo đủ " + c.tuoiTaiKhoanGio
                    + " giờ mới " + vTuoi + " được (còn " + gioPhut(conPhut) + ").";
        }
        if (c.gioChoiToiThieu > 0 && b.phutChoi >= 0 && b.phutChoi < c.gioChoiToiThieu * 60L) {
            return ai + " cần chơi đủ " + c.gioChoiToiThieu + " giờ mới " + vTuoi + " được (còn "
                    + gioPhut(c.gioChoiToiThieu * 60L - b.phutChoi) + ").";
        }
        return null;
    }

    /** Cum tu viec theo kieu chuyen (ChongClone.KIEU_*), null = khong biet (chu mac dinh). */
    public static String viec(final String kieu) {
        if (kieu == null) {
            return null;
        }
        switch (kieu) {
            case "giao_dich":
                return "giao dịch với người chơi khác";
            case "ky_gui":
                return "mua bán ở chợ ký gửi";
            case "thu_cung":
                return "giao dịch thú cưng với người chơi khác";
            case "cho_trang_tri":
            case "cho_sinh_thai":
                return "mua bán với người chơi khác ở chợ";
            case "thach_dau":
                return "thách đấu cược xu";
            case "vong_quay":
                return "tham gia vòng quay";
            case "trao_doi_pokemon":
                return "trao đổi Pokémon với người chơi khác";
            default:
                return null;
        }
    }

    static String gioPhut(final long phut) {
        final long p = Math.max(1L, phut);
        if (p < 60L) {
            return p + " phút";
        }
        return (p / 60L) + " giờ" + (p % 60L == 0L ? "" : " " + (p % 60L) + " phút");
    }

    // ------------------------------------------------------------------ gia ky gui

    /** Gia rao toi da: max(giaThamChieu x lan, xuMoiGio x gio). */
    public static long giaToiDa(final CauHinh c, final long giaThamChieu, final long xuMoiGio) {
        final long a = giaThamChieu > 0 ? nhanKep(giaThamChieu, c.giaToiDaLan) : 0L;
        final long b = nhanKep(Math.max(1L, xuMoiGio), c.giaToiDaGio);
        return Math.min(Integer.MAX_VALUE, Math.max(a, b));
    }

    /** Gia rao toi thieu: giaThamChieu x min% (0 khi khong co gia tham chieu). */
    public static long giaToiThieu(final CauHinh c, final long giaThamChieu) {
        if (giaThamChieu <= 0) {
            return 1L;
        }
        return Math.max(1L, giaThamChieu * c.giaToiThieuPhanTram / 100L);
    }

    /** Loi nhan khi gia rao (moi don vi) sai, null neu hop le. */
    public static String kiemGiaKyGui(final CauHinh c, final long gia, final long giaThamChieu, final long xuMoiGio) {
        if (gia <= 0) {
            return "Không thể bán với giá này.";
        }
        if (c == null || !c.bat) {
            return null;
        }
        final long max = giaToiDa(c, giaThamChieu, xuMoiGio);
        if (gia > max) {
            return "Giá rao quá cao: tối đa " + max + " xu cho món này (chống chuyển xu qua chợ).";
        }
        final long min = giaToiThieu(c, giaThamChieu);
        if (gia < min) {
            return "Giá rao quá thấp: tối thiểu " + min + " xu cho món này.";
        }
        return null;
    }

    static long nhanKep(final long a, final long b) {
        if (a <= 0 || b <= 0) {
            return 0L;
        }
        if (a > Long.MAX_VALUE / b) {
            return Long.MAX_VALUE;
        }
        return a * b;
    }

    // ------------------------------------------------------------------ so ngay: ai chuyen cho ai

    /** Ket qua ghi mot lan chuyen. */
    public static final class KetQuaGhi {
        /** Danh dau nghi clone? */
        public final boolean nghi;
        /** Ly do nghi (rong neu khong nghi). */
        public final String lyDo;

        KetQuaGhi(final List<String> ds) {
            this.nghi = !ds.isEmpty();
            final StringBuilder b = new StringBuilder();
            for (final String s : ds) {
                b.append(b.length() == 0 ? "" : "; ").append(s);
            }
            this.lyDo = b.toString();
        }
    }

    /**
     * So trong ngay (bo nho, mat khi khoi dong lai - ChongClone nap lai tu tob_log_chuyen_gia_tri luc khoi dong).
     * Khoa: nguoi nhan -> (nguoi gui -> ben gui).
     */
    public static final class SoNgay {
        private String ngay = "";
        private final Map<Integer, LinkedHashMap<Integer, Ben>> theoNguoiNhan = new HashMap<Integer, LinkedHashMap<Integer, Ben>>();

        private void doiNgay(final String n) {
            if (!n.equals(ngay)) {
                ngay = n;
                theoNguoiNhan.clear();
            }
        }

        /** Loi nhan chan (cho nguoi gui) neu them gui -> nhan vuot gioi han cung may / IP, null neu duoc. */
        public synchronized String kiem(final CauHinh c, final String n, final Ben gui, final Ben nhan) {
            doiNgay(n);
            if (c == null || !c.bat || gui == null || nhan == null) {
                return null;
            }
            final LinkedHashMap<Integer, Ben> ds = theoNguoiNhan.get(Integer.valueOf(nhan.charId));
            if (ds == null || ds.containsKey(Integer.valueOf(gui.charId))) {
                return null;
            }
            // Dem rieng theo thiet bi va theo IP: nguoi thu N+1 cung thiet bi HOAC cung IP bi chan.
            int cungMay = 1;
            int cungIp = 1;
            for (final Ben b : ds.values()) {
                if (!gui.may.isEmpty() && gui.may.equals(b.may)) {
                    cungMay++;
                }
                if (!gui.ip.isEmpty() && gui.ip.equals(b.ip)) {
                    cungIp++;
                }
            }
            final int cung = Math.max(cungMay, cungIp);
            if (cung > c.nvCungMayToiDa) {
                return "Hôm nay " + nhan.ten + " đã nhận giao dịch từ " + (cung - 1)
                        + " Pokémon dùng chung thiết bị / mạng với bạn (tối đa " + c.nvCungMayToiDa
                        + " mỗi ngày). Thử lại vào ngày mai.";
            }
            return null;
        }

        /** Ghi mot lan chuyen da xong; tra ve co nghi clone khong. */
        public synchronized KetQuaGhi ghi(final CauHinh c, final String n, final Ben gui, final Ben nhan) {
            doiNgay(n);
            final List<String> nghi = new ArrayList<String>();
            if (gui == null || nhan == null) {
                return new KetQuaGhi(nghi);
            }
            LinkedHashMap<Integer, Ben> ds = theoNguoiNhan.get(Integer.valueOf(nhan.charId));
            if (ds == null) {
                ds = new LinkedHashMap<Integer, Ben>();
                theoNguoiNhan.put(Integer.valueOf(nhan.charId), ds);
            }
            ds.put(Integer.valueOf(gui.charId), gui);
            final int nguong = c == null ? 3 : c.nghiSoNguoiGui;
            if (ds.size() >= nguong) {
                nghi.add(ds.size() + " người gửi khác nhau trong ngày");
            }
            int cung = 0;
            for (final Ben b : ds.values()) {
                if (b.charId != gui.charId && cungMayHoacIp(b, gui)) {
                    cung++;
                }
            }
            if (cung > 0) {
                nghi.add((cung + 1) + " người gửi chung thiết bị/IP");
            }
            if (!gui.may.isEmpty() && gui.may.equals(nhan.may)) {
                nghi.add("người gửi cùng thiết bị với người nhận");
            }
            return new KetQuaGhi(nghi);
        }

        /** So nguoi gui khac nhau cua nguoi nhan trong ngay (test / trang quan tri). */
        public synchronized int soNguoiGui(final String n, final int nhanId) {
            doiNgay(n);
            final LinkedHashMap<Integer, Ben> ds = theoNguoiNhan.get(Integer.valueOf(nhanId));
            return ds == null ? 0 : ds.size();
        }
    }

    static boolean cungMayHoacIp(final Ben a, final Ben b) {
        return (!a.may.isEmpty() && a.may.equals(b.may)) || (!a.ip.isEmpty() && a.ip.equals(b.ip));
    }
}
