package real;

import data.DatabaseSuKien;

/**
 * LICH HEN MO/DONG SU KIEN + TU DONG KHOI DONG LAI
 *
 * Hai viec, chung mot vong lap vi chung noi duoi nhau: lich hen doi cot `bat`, ma doi
 * `bat` thi phai khoi dong lai may chu moi co hieu luc.
 *
 * VI SAO PHAI KHOI DONG LAI MOI CO HIEU LUC. Rat nhieu tac dung cua su kien duoc tinh
 * MOT LAN luc bat may chu roi khong hoi lai nua: Map.loadShop() dat SELL/NOT_SELL cho
 * tung mon theo Char.isSuKien*, Map.isSale, danh sach NPC, bang vat pham. Doi co giua
 * chung thi duoc nua nay hong nua kia - kho lan hon nhieu so voi khong doi gi. Nen day
 * khong phai su luoi: khoi dong lai la cach dung.
 *
 * -------------------------------------------------------------------------------------
 *  BA CAI CHOT AN TOAN - DUNG THAO NEU KHONG DOC KY
 * -------------------------------------------------------------------------------------
 *
 *  1. GOM THAY DOI ROI MOI KHOI DONG LAI (gomGiay, mac dinh 120s).
 *     htbb-game.service dat StartLimitBurst=5 / StartLimitIntervalSec=300: khoi dong qua
 *     5 lan trong 300 giay thi systemd cho unit vao trang thai "failed" va KHONG BAT LAI
 *     NUA - server nam han cho toi khi co nguoi ssh vao. Bat 5 su kien lien tay ma moi
 *     lan mot lan khoi dong lai la dung ngay cai bay do. Nen moi thay doi deu doi han
 *     them 2 phut, va ca chum thay doi chi ton MOT lan khoi dong lai.
 *
 *  2. YEN 3 PHUT DAU SAU KHI BAT MAY (YEN_SAU_KHOI_DONG_MS).
 *     Cong voi 2 phut gom va 1 phut bao truoc thi hai lan khoi dong lai lien tiep cach
 *     nhau it nhat 6 phut > cua so 300 giay cua systemd. Do la toan bo ly do cua con so
 *     3 phut nay; ha xuong la mat bien an toan.
 *
 *  3. CHI TINH THAY DOI CO DAU VET NGUOI SUA (soDongChoKhoiDongLai).
 *     Neu ghiAnhChup() that bai luc khoi dong, cot dang_bat ket lai o gia tri cu va phep
 *     so `bat <> dang_bat` se dung MAI MAI. Khong co chot nay thi may chu tu khoi dong
 *     lai vo han va roi thang vao chot 1. Xem soDongAnhChupHong().
 *
 * -------------------------------------------------------------------------------------
 *
 * Tat toan bo co che bang sv.suKienTuKhoiDongLai=0 trong server.ini. Luc do lich hen VAN
 * chay (van doi cot `bat`), chi la khong ai tu bat lai may - trang quan tri se hien
 * "cho khoi dong lai" va nguoi van hanh tu chon gio.
 */
public final class LichSuKien implements Runnable {

    // ============================ CAU HINH ============================

    /** sv.suKienTuKhoiDongLai - tu khoi dong lai khi co su kien doi trang thai. */
    public static volatile boolean tuKhoiDongLai = true;

    /** sv.suKienGomGiay - doi bao lau khong con thay doi moi khoi dong lai. */
    public static volatile int gomGiay = 120;

    /** sv.suKienBaoTruocGiay - bao trong game bao lau truoc khi tat may. */
    public static volatile int baoTruocGiay = 60;

    // ============================ HANG SO ============================

    /** Nhip kiem tra. 15 giay la du: lich hen tinh bang phut, khong phai giay. */
    public static final long CHU_KY_MS = 15000L;

    /** Khong lam gi trong 3 phut dau sau khi bat may - xem chot an toan so 2 o tren. */
    public static final long YEN_SAU_KHOI_DONG_MS = 180000L;

    /**
     * TRAN CUNG cua viec gom: du con thay doi moi, khong hoan qua 15 phut ke tu thay doi
     * dau tien cua dot nay.
     *
     * Khong co tran nay thi moi thay doi lai day han them 2 phut, va mot nguoi ngoi sua
     * lien tuc (hoac mot lich hen chay lien tiep) se hoan vo han - thiet lap khong bao gio
     * vao game ma tren trang quan tri van hien "server se tu khoi dong lai", tuc la hong
     * mot cach im lang. 15 phut la day cua truong hop xau nhat, khong phai gia tri thuong gap.
     */
    public static final long TOI_DA_GOM_MS = 900000L;

    /** Viec can lam sau moi nhip. */
    public static final int KHONG_LAM_GI = 0;
    public static final int BAO_TRUOC = 1;
    public static final int YEU_CAU_TAT = 2;

    // ============================ TRANG THAI ============================

    /**
     * Trang thai cua bo dem. De thanh mot lop rieng chu khong phai bien static de
     * nhip() thanh ham thuan, test goi duoc ma khong can CSDL lan dong ho that.
     */
    public static final class TrangThai {
        /** Moc het gio gom. 0 = dang khong cho gi. */
        public long hanGom;
        /** Tran cung: du con thay doi cung khong hoan qua moc nay. 0 = chua bat dau dot nao. */
        public long hanCung;
        /** Moc tat may. 0 = chua bao truoc. */
        public long hanTat;
        /** So dong cho o nhip truoc, de biet co thay doi moi hay khong. */
        public int soChoLanTruoc;
        /** Da bat co khoi dong lai roi - dung bat lai lan nua. */
        public boolean daYeuCau;

        public void thoi() {
            hanGom = 0L;
            hanCung = 0L;
            hanTat = 0L;
            soChoLanTruoc = 0;
            daYeuCau = false;
        }
    }

    private static final TrangThai TRANG_THAI = new TrangThai();
    private static volatile long batDauLuc;
    private static volatile boolean daBaoAnhChupHong;
    private static volatile boolean dangChay;

    private LichSuKien() {
    }

    // ============================ VONG LAP ============================

    /** Goi mot lan tu TeamServer, sau khi SuKien.nap() da chay xong. */
    public static void batDau() {
        if (dangChay) {
            return;
        }
        dangChay = true;
        batDauLuc = System.currentTimeMillis();
        final Thread t = new Thread(new LichSuKien(), "LichSuKien");
        t.setDaemon(true);
        t.start();
        System.out.println("LICH SU KIEN -> " + moTa());
    }

    public static String moTa() {
        if (!tuKhoiDongLai) {
            return "lich hen BAT, tu khoi dong lai TAT (sv.suKienTuKhoiDongLai=0)";
        }
        return "lich hen BAT, tu khoi dong lai BAT (gom " + gomGiay
                + "s, bao truoc " + baoTruocGiay + "s)";
    }

    @Override
    public void run() {
        while (true) {
            try {
                motNhip();
            } catch (final Throwable e) {
                // Nuot moi loi: thread nay chet la lich hen chet am tham, va khong ai
                // biet cho toi khi mot su kien khong tu mo dung ngay.
                System.out.println("[SUKIEN] loi trong vong lap lich: " + e);
            }
            try {
                Thread.sleep(CHU_KY_MS);
            } catch (final InterruptedException e) {
                return;
            }
        }
    }

    /** Ngay (yyyyMMdd, gio Viet Nam) da ghi lai tob_su_kien_lich lan cuoi. */
    private static volatile int ngayGhiLich = 0;

    /**
     * Lich nam (com.htbb.pokemon.LichNam): moi ngay ghi lai cac lan dien ra (nam truoc .. nam sau) vao
     * tob_su_kien_lich, moi nhip dat tob_su_kien.bat theo lich. Goi ca luc khoi dong (SuKien.nap) va moi nhip.
     * @return so su kien vua doi bat.
     */
    public static int dongBoLichNam() {
        final long bay = com.htbb.pokemon.DongHo.bay();
        final java.util.Calendar c = java.util.Calendar.getInstance(com.htbb.pokemon.LichNam.MUI_GIO);
        c.setTimeInMillis(bay);
        final int nam = c.get(java.util.Calendar.YEAR);
        final int ngay = nam * 10000 + (c.get(java.util.Calendar.MONTH) + 1) * 100 + c.get(java.util.Calendar.DAY_OF_MONTH);
        if (ngay != ngayGhiLich) {
            if (data.DatabaseLichNam.ghiLich(nam - 1, nam + 1) > 0) {
                ngayGhiLich = ngay;
            }
        }
        return data.DatabaseLichNam.dongBo(com.htbb.pokemon.LichNam.dsMa());
    }

    /**
     * Thay doi do LICH NAM (moc 04:00) cho bao tri hang ngay neu bao tri con <= {@link #HOAN_TOI_BAO_TRI_PHUT} phut:
     * khong khoi dong lai rieng. Thay doi cua admin (hoac lich khi bao tri con xa / da tat) van khoi dong lai
     * nhu cu (gom + bao truoc trong game).
     */
    public static final int HOAN_TOI_BAO_TRI_PHUT = 180;

    /** Ham thuan: so dong can tu khoi dong lai sau khi tru cac dong lich cho bao tri. */
    public static int soChoTuKhoiDong(final int soCho, final int soChoTheoLich, final int phutDenBaoTri) {
        if (phutDenBaoTri >= 0 && phutDenBaoTri <= HOAN_TOI_BAO_TRI_PHUT) {
            return Math.max(0, soCho - Math.max(0, soChoTheoLich));
        }
        return soCho;
    }

    /** Mot nhip: chay lich toi han, dem viec ton dong, roi quyet dinh. */
    static void motNhip() {
        final int theoLich = dongBoLichNam();
        if (theoLich > 0) {
            System.out.println("[SUKIEN] lich nam vua doi trang thai " + theoLich + " su kien (ap dung o lan khoi dong lai / bao tri ke tiep).");
        }
        final int daDoi = DatabaseSuKien.chayLichDenHan();
        if (daDoi > 0) {
            System.out.println("[SUKIEN] lich hen vua doi trang thai " + daDoi + " su kien.");
        }

        final int hong = DatabaseSuKien.soDongAnhChupHong();
        if (hong > 0 && !daBaoAnhChupHong) {
            daBaoAnhChupHong = true;
            System.out.println("[SUKIEN] CANH BAO: " + hong + " dong co bat <> dang_bat ma"
                    + " khong phai do ai sua. Anh chup luc khoi dong co the da that bai."
                    + " KHONG tu khoi dong lai vi ly do nay - xem lai log [SUKIEN] luc bat may.");
        }

        final int soCho = soChoTuKhoiDong(DatabaseSuKien.soDongChoKhoiDongLai(),
                DatabaseSuKien.soDongChoKhoiDongLaiTheoLich(), BaoTriDaily.phutDenBaoTri(new java.util.Date()));
        final int viec = nhip(TRANG_THAI, System.currentTimeMillis(), batDauLuc, soCho,
                tuKhoiDongLai, gomGiay, baoTruocGiay);

        if (viec == BAO_TRUOC) {
            System.out.println("[SUKIEN] " + soCho + " su kien cho hieu luc -> khoi dong lai sau "
                    + baoTruocGiay + "s.");
            BaoTriDaily.baoTruocBaoTri(baoTruocGiay);
        } else if (viec == YEU_CAU_TAT) {
            System.out.println("[SUKIEN] bat co khoi dong lai (5h_systems.restart).");
            DatabaseSuKien.yeuCauKhoiDongLai();
        }
    }

    // ============================ QUYET DINH ============================

    /**
     * Quyet dinh viec can lam o nhip nay. Ham THUAN - moi thu vao qua tham so, ra qua
     * gia tri tra ve va thay doi tren `t`. Nho vay test dien duoc moi kich ban thoi gian
     * ma khong phai cho that 3 phut.
     *
     * @param t          bo dem, bi sua tai cho
     * @param bayGio     System.currentTimeMillis()
     * @param batDauLuc  luc may chu bat dau chay
     * @param soCho      so su kien dang cho khoi dong lai
     * @param tuDong     co cho phep tu khoi dong lai khong
     */
    public static int nhip(final TrangThai t, final long bayGio, final long batDauLuc,
                           final int soCho, final boolean tuDong,
                           final int gomGiay, final int baoTruocGiay) {
        if (!tuDong || soCho <= 0) {
            // Het lech thi huy moi hen dang co. Truong hop that: admin bat nham roi tat
            // lai ngay - khong con gi de ap dung nen khong duoc lam phien nguoi choi.
            t.thoi();
            return KHONG_LAM_GI;
        }

        if (bayGio - batDauLuc < YEN_SAU_KHOI_DONG_MS) {
            // Chua du tuoi. KHONG dat han gom o day: dat bay gio thi dong ho gom chay
            // song song voi thoi gian yen va hai cai cong don lai khong con dam bao
            // khoang cach 6 phut giua hai lan khoi dong lai.
            return KHONG_LAM_GI;
        }

        if (t.hanTat > 0L) {
            // Da bao truoc roi - thay doi den sau khong duoc doi han nua, vi nguoi choi
            // da nghe "bao tri sau 1 phut". Dung loi ho.
            if (bayGio >= t.hanTat && !t.daYeuCau) {
                t.daYeuCau = true;
                return YEU_CAU_TAT;
            }
            return KHONG_LAM_GI;
        }

        if (t.hanGom == 0L || soCho != t.soChoLanTruoc) {
            if (t.hanCung == 0L) {
                t.hanCung = bayGio + TOI_DA_GOM_MS;
            }
            final long han = bayGio + gomGiay * 1000L;
            t.hanGom = han < t.hanCung ? han : t.hanCung;
            t.soChoLanTruoc = soCho;
            // KHONG return o day. Khi da cham tran cung thi hanGom <= bayGio, va neu
            // thoat som thi nhip sau lai vao dung nhanh nay (vi soCho van doi) - hoan vo
            // han, dung cai ma tran cung sinh ra de chan.
        }

        if (bayGio >= t.hanGom) {
            t.hanTat = bayGio + baoTruocGiay * 1000L;
            return BAO_TRUOC;
        }
        return KHONG_LAM_GI;
    }

    /** Cho test: tra bo dem ve trang thai ban dau. */
    public static TrangThai boDem() {
        return TRANG_THAI;
    }
}
