package real;

import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.pokemon.ChongClone;
import com.htbb.pokemon.ThachDauLogic;
import com.htbb.pokemon.VongQuayLogic;
import data.Database;
import data.DatabaseGioiHanQua;
import data.DatabaseVongQuay;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ky quy + chia tien thach dau cuoc xu (loi dai 1-1 = arena kieu 3, nhom = kieu 4). Luat: {@link ThachDauLogic}.
 *
 * Luong: Map.doPopup popup 10 (1-1) / MapXuLyLenh.doAcceptChallengeParty (nhom) goi {@link #kyQuy} TRUOC khi tao arena;
 * ArenaData (ket thuc tran: thang / hoa) goi {@link #ketThuc} - MOT lan moi tran (remove nguyen tu). Nguoi nhan offline
 * -> tob_vong_quay_cho_nhan (cong khi dang nhap, real.VongQuay.khiDangNhap). May tat giua tran -> {@link #napLucKhoiDong}
 * hoan du cho ca hai ben.
 */
public final class ThachDauCuoc {

    private ThachDauCuoc() {
    }

    /** Mot khoan ky quy: ben 0 / ben 1 (nguoi tra cuoc: chinh nguoi choi 1-1, doi truong neu nhom). */
    public static final class KyQuy {
        public final int idArena;
        public final int loai;
        public final String ten0;
        public final int id0;
        public final String ten1;
        public final int id1;
        public final long cuoc;

        KyQuy(final int idArena, final int loai, final String ten0, final int id0, final String ten1, final int id1, final long cuoc) {
            this.idArena = idArena;
            this.loai = loai;
            this.ten0 = ten0;
            this.id0 = id0;
            this.ten1 = ten1;
            this.id1 = id1;
            this.cuoc = cuoc;
        }
    }

    static final ConcurrentHashMap<Integer, KyQuy> DANG_GIU = new ConcurrentHashMap<Integer, KyQuy>();
    private static final Object KHOA = new Object();

    public static ThachDauLogic.CauHinh cauHinh() {
        final ThachDauLogic.CauHinh d = ThachDauLogic.CauHinh.macDinh();
        try {
            return new ThachDauLogic.CauHinh(
                    CauHinhTrungTam.so("thach_dau.bat", d.bat ? 1 : 0) == 1,
                    (int) CauHinhTrungTam.so("thach_dau.cap_toi_thieu", d.capToiThieu),
                    CauHinhTrungTam.so("thach_dau.cuoc_toi_thieu", d.cuocToiThieu),
                    CauHinhTrungTam.so("thach_dau.cuoc_toi_da", d.cuocToiDa),
                    (int) CauHinhTrungTam.so("thach_dau.cuoc_toi_da_gio", d.cuocToiDaGio),
                    (int) CauHinhTrungTam.so("thach_dau.phi_phan_tram", d.phiPhanTram));
        } catch (final Throwable t) {
            return d;
        }
    }

    static int cap(final Char p) {
        return p == null || p.lvDetail == null ? 0 : p.lvDetail.lv;
    }

    /** Cap hieu luc: max(cap thach dau, cap chong clone). */
    static int capHieuLuc(final ThachDauLogic.CauHinh c) {
        final com.htbb.pokemon.ChongCloneLogic.CauHinh cc = ChongClone.cauHinh();
        return cc.bat ? Math.max(c.capToiThieu, cc.capToiThieu) : c.capToiThieu;
    }

    /**
     * Loi nhan neu cuoc {@code so} khong hop le cho nhung nguoi {@code ds} (moi nguoi trong tran), null neu duoc.
     * Muc toi da theo cap THAP NHAT trong {@code ds}.
     */
    public static String kiemCuoc(final long so, final Char... ds) {
        final ThachDauLogic.CauHinh c0 = cauHinh();
        final ThachDauLogic.CauHinh c = new ThachDauLogic.CauHinh(c0.bat, capHieuLuc(c0), c0.cuocToiThieu, c0.cuocToiDa,
                c0.cuocToiDaGio, c0.phiPhanTram);
        int thap = Integer.MAX_VALUE;
        for (final Char p : ds) {
            if (p != null) {
                thap = Math.min(thap, cap(p));
            }
        }
        if (thap == Integer.MAX_VALUE) {
            thap = 0;
        }
        long gio = 1000L;
        try {
            gio = com.htbb.pokemon.LauDai.xuMoiGio(Math.max(1, thap));
        } catch (final Throwable ignored) {
        }
        return ThachDauLogic.kiemCuoc(c, so, thap, gio);
    }

    /** Mo ta muc cuoc cho o nhap (vd "1.000 – 43.120"). */
    public static String moTaMuc(final Char p) {
        final ThachDauLogic.CauHinh c = cauHinh();
        long gio = 1000L;
        try {
            gio = com.htbb.pokemon.LauDai.xuMoiGio(Math.max(1, cap(p)));
        } catch (final Throwable ignored) {
        }
        return VongQuayLogic.so(c.cuocToiThieu) + " – " + VongQuayLogic.so(ThachDauLogic.cuocToiDa(c, gio));
    }

    /**
     * KY QUY nguyen tu: tru {@code cuoc} cua ca a va b duoi mot khoa; ben b thieu -> hoan a. Ghi bang ky quy truoc khi
     * tra ve. @return null = da giu (tao arena {@code idArena} ngay sau), khac = loi nhan (khong tru gi).
     */
    public static String kyQuy(final Char a, final Char b, final long cuoc, final int loai, final int idArena) {
        if (a == null || b == null || cuoc <= 0L) {
            return "Số tiền cược không hợp lệ.";
        }
        synchronized (KHOA) {
            if (DANG_GIU.containsKey(Integer.valueOf(idArena))) {
                return "Trận đấu đã tồn tại.";
            }
            if (!a.truXuNeuDu(cuoc, false, "thach dau ky quy")) {
                return a.charname + " không đủ " + VongQuayLogic.so(cuoc) + " xu, trận đấu đã huỷ.";
            }
            if (!b.truXuNeuDu(cuoc, false, "thach dau ky quy")) {
                a.addXu(cuoc, "thach dau hoan ky quy (doi thu thieu xu)");
                return b.charname + " không đủ " + VongQuayLogic.so(cuoc) + " xu, trận đấu đã huỷ.";
            }
            DANG_GIU.put(Integer.valueOf(idArena), new KyQuy(idArena, loai, a.charname, a.charDBID, b.charname, b.charDBID, cuoc));
        }
        DatabaseGioiHanQua.ghiKyQuy(idArena, loai, a.charname, a.charDBID, b.charname, b.charDBID, cuoc);
        luu(a);
        luu(b);
        try {
            a.sendMessage(MessageCreator.createCharInventoryMessage(a, 0));
            b.sendMessage(MessageCreator.createCharInventoryMessage(b, 0));
        } catch (final Exception ignored) {
        }
        nhatKy(a.charname, "ky quy thach dau " + idArena + " " + a.charname + " vs " + b.charname + " " + cuoc + " xu moi ben", "challen");
        return null;
    }

    public static boolean coKyQuy(final int idArena) {
        return DANG_GIU.containsKey(Integer.valueOf(idArena));
    }

    /**
     * Ket thuc tran {@code idArena}: doiThang 0 / 1 = ben 0 / ben 1 thang (doi 0 = team1 = nguoi moi), khac = hoa.
     * Goi nhieu lan an toan (chi lan dau co tac dung). @return phan chia, null neu tran khong co ky quy.
     */
    public static ThachDauLogic.Chia ketThuc(final int idArena, final int doiThang) {
        final KyQuy k = DANG_GIU.remove(Integer.valueOf(idArena));
        if (k == null) {
            return null;
        }
        final ThachDauLogic.Chia ch = ThachDauLogic.chia(cauHinh(), k.cuoc, doiThang);
        tra(k.ten0, k.id0, ch.tra0, doiThang == 0 ? "thang thach dau" : "hoan thach dau (hoa)");
        tra(k.ten1, k.id1, ch.tra1, doiThang == 1 ? "thang thach dau" : "hoan thach dau (hoa)");
        DatabaseGioiHanQua.xoaKyQuy(idArena);
        if (doiThang == 0 || doiThang == 1) {
            final Char thua = CharManager.instance.getCharByCharName(doiThang == 0 ? k.ten1 : k.ten0);
            if (thua != null) {
                try {
                    ChongClone.ghi(thua, doiThang == 0 ? k.id0 : k.id1, doiThang == 0 ? k.ten0 : k.ten1,
                            ChongClone.KIEU_THACH_DAU, k.cuoc, "thach dau " + idArena + " phi " + ch.phi);
                } catch (final Throwable ignored) {
                }
            }
        }
        nhatKy(doiThang == 1 ? k.ten1 : k.ten0,
                "ket thuc thach dau " + idArena + " kieu " + k.loai + " thang " + doiThang + " cuoc " + k.cuoc + " tra " + ch.tra0
                        + "/" + ch.tra1 + " phi " + ch.phi, "garena");
        return ch;
    }

    /** May vua khoi dong: tran dang danh luc tat -> hoan du ca hai ben (qua cho_nhan). */
    public static void napLucKhoiDong() {
        try {
            final List<Object[]> ds = DatabaseGioiHanQua.napKyQuy();
            for (final Object[] d : ds) {
                final long cuoc = ((Long) d[3]).longValue();
                DatabaseVongQuay.congChoNhan((String) d[1], cuoc, 0L, "hoan ky quy thach dau " + d[0] + " (may khoi dong lai)");
                DatabaseVongQuay.congChoNhan((String) d[2], cuoc, 0L, "hoan ky quy thach dau " + d[0] + " (may khoi dong lai)");
                DatabaseGioiHanQua.xoaKyQuy(((Integer) d[0]).intValue());
                System.out.println("[THACH DAU] hoan ky quy " + d[0] + ": " + d[1] + ", " + d[2] + " moi ben " + cuoc);
            }
        } catch (final Throwable t) {
            System.out.println("[THACH DAU] napLucKhoiDong: " + t);
        }
    }

    static void tra(final String ten, final int id, final long so, final String lyDo) {
        if (so <= 0L || ten == null) {
            return;
        }
        final Char c = CharManager.instance.getCharByCharName(ten);
        if (c != null && (id <= 0 || c.charDBID == id)) {
            c.addXu(so, lyDo);
            try {
                c.sendMessage(MessageCreator.createCharInventoryMessage(c, 0));
            } catch (final Exception ignored) {
            }
            luu(c);
        } else {
            DatabaseVongQuay.congChoNhan(ten, so, 0L, lyDo);
        }
    }

    static void nhatKy(final String ten, final String noiDung, final String loai) {
        try {
            Database.instance.saveOrtherLog("", ten, noiDung, loai);
        } catch (final Throwable ignored) {
        }
    }

    static void luu(final Char p) {
        try {
            Database.instance.saveCharAuto(p);
        } catch (final Throwable ignored) {
        }
    }
}
