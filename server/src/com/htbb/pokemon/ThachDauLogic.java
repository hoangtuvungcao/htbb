package com.htbb.pokemon;

/**
 * Thach dau cuoc xu (loi dai 1-1 va nhom) - lop THUAN, test/KiemThachDauCuoc.java; noi vao game: real.ThachDauCuoc.
 *
 * Truoc (chu game duyet sua 19/10/2026): 1-1 kiem so du roi moi tru rieng (subXu chan ve 0) - mot ben tieu xu o giua
 * van lam nguoi thang nhan du 1,8 lan; muc 100.000 - 10.000.000 xu; thang offline mat trang. Nhom: tru cuoc hai doi
 * truong nhung arena kieu 4 KHONG tra ai (ca hai cuoc bien mat). Ham Map.doChallenge (khong goi) tra 2 lan, khong thue.
 *
 * Sau:
 * <ul>
 *   <li>KY QUY luc chap nhan: tru ca hai ben trong mot khoa (truXuNeuDu, ben sau thieu -> hoan ben truoc, huy tran).</li>
 *   <li>Ket thuc: co nguoi thang -> nguoi thang nhan 2 x cuoc - phi (phi = HO HUT, cung muc vong quay / thue cho);
 *       hoa -> hoan du moi ben (khong co chuyen xu nen khong thu phi). Offline -> tra khi dang nhap. Khoi dong lai giua
 *       tran -> hoan du (bang ky quy).</li>
 *   <li>Muc cuoc theo dai cap: [cuocToiThieu, min(cuocToiDa, cuocToiDaGio x thu nhap 1 gio o cap THAP NHAT)].</li>
 *   <li>Cong chong clone ca hai ben (da co) + cap toi thieu.</li>
 * </ul>
 */
public final class ThachDauLogic {

    private ThachDauLogic() {
    }

    public static final class CauHinh {
        public final boolean bat;
        public final int capToiThieu;
        public final long cuocToiThieu;
        public final long cuocToiDa;
        public final int cuocToiDaGio;
        public final int phiPhanTram;

        public CauHinh(final boolean bat, final int capToiThieu, final long cuocToiThieu, final long cuocToiDa,
                final int cuocToiDaGio, final int phiPhanTram) {
            this.bat = bat;
            this.capToiThieu = Math.max(0, capToiThieu);
            this.cuocToiThieu = Math.max(1L, cuocToiThieu);
            this.cuocToiDa = Math.max(this.cuocToiThieu, cuocToiDa);
            this.cuocToiDaGio = Math.max(1, cuocToiDaGio);
            // 5..50%: duoi 5% thua co y thanh duong chuyen xu re hon cho (6% + 1%).
            this.phiPhanTram = Math.max(5, Math.min(50, phiPhanTram));
        }

        public static CauHinh macDinh() {
            return new CauHinh(true, 40, 1000L, 100000L, 2, 10);
        }
    }

    /** Cuoc toi da cho tran co nguoi cap thap nhat {@code capThapNhat} (xuMoiGio = thu nhap 1 gio o cap do). */
    public static long cuocToiDa(final CauHinh c, final long xuMoiGioCapThap) {
        final long theoGio = Math.max(1L, xuMoiGioCapThap) * c.cuocToiDaGio;
        return Math.max(c.cuocToiThieu, Math.min(c.cuocToiDa, theoGio));
    }

    /** Loi nhan neu cuoc {@code so} khong hop le, null neu duoc. */
    public static String kiemCuoc(final CauHinh c, final long so, final int capThapNhat, final long xuMoiGioCapThap) {
        if (c == null || !c.bat) {
            return "Thách đấu cược xu đang đóng.";
        }
        if (capThapNhat < c.capToiThieu) {
            return "Cả hai bên cần đạt cấp " + c.capToiThieu + " mới thách đấu cược xu.";
        }
        final long max = cuocToiDa(c, xuMoiGioCapThap);
        if (so < c.cuocToiThieu || so > max) {
            return "Chỉ cược được từ " + VongQuayLogic.so(c.cuocToiThieu) + " đến " + VongQuayLogic.so(max) + " xu (theo cấp thấp nhất trong trận).";
        }
        return null;
    }

    /** Ket qua chia tien mot tran. */
    public static final class Chia {
        /** tien tra cho ben 0 / ben 1 */
        public final long tra0;
        public final long tra1;
        public final long phi;
        public final long vao;

        Chia(final long tra0, final long tra1, final long phi, final long vao) {
            this.tra0 = tra0;
            this.tra1 = tra1;
            this.phi = phi;
            this.vao = vao;
        }
    }

    /**
     * Chia tien: {@code doiThang} 0 / 1 = ben thang, khac = hoa / huy (hoan du). Bat bien: tra0 + tra1 + phi == 2 x cuoc.
     */
    public static Chia chia(final CauHinh c, final long cuoc, final int doiThang) {
        final long vao = 2L * Math.max(0L, cuoc);
        if (doiThang != 0 && doiThang != 1) {
            return new Chia(vao / 2L, vao / 2L, 0L, vao);
        }
        final long phi = vao * c.phiPhanTram / 100L;
        final long tra = vao - phi;
        return doiThang == 0 ? new Chia(tra, 0L, phi, vao) : new Chia(0L, tra, phi, vao);
    }
}
