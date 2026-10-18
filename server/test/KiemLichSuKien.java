import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import real.LichSuKien;
import real.LichSuKien.TrangThai;

/**
 * Kiem thu LICH HEN SU KIEN + TU KHOI DONG LAI (real/LichSuKien).
 *
 * Trong tam la ba cai chot an toan. Ca ba deu la loai loi KHONG the hien ra khi thu tay:
 * chung chi lo mat vao dung hom co nguoi bat nam su kien lien tay, hoac dung hom CSDL
 * chap chon - va hau qua la server nam han chu khong phai mot loi hien tren man hinh.
 *
 *   1. Gom thay doi -> mot chum thay doi chi ton MOT lan khoi dong lai.
 *   2. Yen 3 phut dau -> hai lan khoi dong lai cach nhau > cua so 300s cua systemd.
 *   3. Chi khoi dong lai vi thay doi CO DAU VET NGUOI SUA -> anh chup hong khong gay
 *      vong lap khoi dong vo han.
 *
 * LichSuKien.nhip() duoc viet thanh ham thuan chinh la de cho o day dien duoc moi kich
 * ban thoi gian ma khong phai cho that.
 */
public final class KiemLichSuKien {

    private static final long PHUT = 60000L;

    /** Da qua thoi gian yen dau, de moi phep thu khong phai lap lai doan nay. */
    private static final long DU_TUOI = LichSuKien.YEN_SAU_KHOI_DONG_MS + PHUT;

    private KiemLichSuKien() {
    }

    public static void chay() {
        Kiem.nhom("Lich su kien - gom thay doi va tu khoi dong lai");

        chotYenSauKhoiDong();
        chotGom();
        chotBaoTruocRoiTat();
        chotHuyKhiHetLech();
        chotTat();
        khoangCachHaiLanKhoiDong();

        Kiem.nhom("Lich su kien - cau SQL va trang quan tri");
        soiMaNguon();
    }

    // ==================== CHOT 2: YEN 3 PHUT DAU ====================

    private static void chotYenSauKhoiDong() {
        final TrangThai t = new TrangThai();
        // Ngay sau khi bat may da co viec cho san (admin doi luc server dang tat).
        Kiem.bang("vua bat may: chua lam gi du da co viec cho",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, 0L, 0L, 3, true, 120, 60)));
        Kiem.bang("2 phut sau khi bat may: van chua lam gi",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, 2 * PHUT, 0L, 3, true, 120, 60)));
        // Va quan trong: dong ho gom KHONG duoc chay ngam trong luc cho.
        Kiem.bang("thoi gian yen chua dat han gom", Long.valueOf(0L), Long.valueOf(t.hanGom));

        Kiem.bang("qua 3 phut moi bat dau tinh",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, DU_TUOI, 0L, 3, true, 120, 60)));
        Kiem.dung("luc do moi dat han gom", t.hanGom == DU_TUOI + 120000L);
    }

    // ==================== CHOT 1: GOM THAY DOI ====================

    private static void chotGom() {
        final TrangThai t = new TrangThai();
        long luc = DU_TUOI;

        LichSuKien.nhip(t, luc, 0L, 1, true, 120, 60);
        final long hanDau = t.hanGom;
        Kiem.dung("thay doi dau tien dat han gom 120s", hanDau == luc + 120000L);

        // Admin bat them mot su kien nua sau 100 giay -> han phai LUI RA, khong duoc giu
        // nguyen. Neu giu nguyen thi thay doi thu hai gan nhu khong duoc gom chung.
        luc += 100000L;
        Kiem.bang("them thay doi thu hai: chua tat may",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, luc, 0L, 2, true, 120, 60)));
        Kiem.dung("han gom bi day ra xa hon", t.hanGom == luc + 120000L);
        Kiem.dung("han moi that su muon hon han cu", t.hanGom > hanDau);

        // Toi day han CU da qua tu lau. Neu ma nguon so nham voi han cu thi day se bao truoc.
        Kiem.bang("qua han cu nhung chua qua han moi: van im",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, hanDau + 1000L, 0L, 2, true, 120, 60)));
    }

    // ==================== BAO TRUOC ROI MOI TAT ====================

    private static void chotBaoTruocRoiTat() {
        final TrangThai t = new TrangThai();
        final long batDau = DU_TUOI;

        LichSuKien.nhip(t, batDau, 0L, 1, true, 120, 60);
        Kiem.bang("het gio gom -> bao truoc",
                Integer.valueOf(LichSuKien.BAO_TRUOC),
                Integer.valueOf(LichSuKien.nhip(t, batDau + 120000L, 0L, 1, true, 120, 60)));
        Kiem.dung("dat han tat = bao truoc 60s", t.hanTat == batDau + 120000L + 60000L);

        Kiem.bang("dang dem nguoc: chua den gio thi khong tat",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, batDau + 150000L, 0L, 1, true, 120, 60)));

        // Nguoi choi da nghe "bao tri sau 1 phut". Admin doi them mot su kien nua thi
        // KHONG duoc dai han ra - hua roi thi phai giu.
        Kiem.bang("dang dem nguoc ma co thay doi moi: van khong dai han",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, batDau + 155000L, 0L, 9, true, 120, 60)));
        Kiem.dung("han tat giu nguyen", t.hanTat == batDau + 180000L);

        Kiem.bang("den gio -> yeu cau tat",
                Integer.valueOf(LichSuKien.YEU_CAU_TAT),
                Integer.valueOf(LichSuKien.nhip(t, batDau + 180000L, 0L, 1, true, 120, 60)));

        // Server mat vai giay moi doc duoc co. Trong luc do nhip van chay - khong duoc
        // bat co lai lan nua, khong thi moi 15 giay lai mot lenh UPDATE vo ich.
        Kiem.bang("da yeu cau roi thi khong yeu cau lai",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, batDau + 200000L, 0L, 1, true, 120, 60)));
    }

    // ==================== HUY KHI KHONG CON GI LECH ====================

    private static void chotHuyKhiHetLech() {
        final TrangThai t = new TrangThai();
        LichSuKien.nhip(t, DU_TUOI, 0L, 1, true, 120, 60);
        Kiem.dung("dang co hen", t.hanGom > 0L);

        // Admin bat nham roi tat lai ngay -> khong con gi de ap dung. Khong duoc lam
        // phien nguoi choi bang mot lan khoi dong lai khong doi gi ca.
        Kiem.bang("het lech -> khong lam gi",
                Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                Integer.valueOf(LichSuKien.nhip(t, DU_TUOI + 1000L, 0L, 0, true, 120, 60)));
        Kiem.bang("va huy luon hen dang co", Long.valueOf(0L), Long.valueOf(t.hanGom));

        // Ke ca khi da bao truoc roi ma admin kip tat lai - van huy.
        final TrangThai t2 = new TrangThai();
        LichSuKien.nhip(t2, DU_TUOI, 0L, 1, true, 120, 60);
        LichSuKien.nhip(t2, DU_TUOI + 120000L, 0L, 1, true, 120, 60);
        Kiem.dung("dang dem nguoc", t2.hanTat > 0L);
        LichSuKien.nhip(t2, DU_TUOI + 130000L, 0L, 0, true, 120, 60);
        Kiem.bang("tat lai kip thi huy ca dem nguoc",
                Long.valueOf(0L), Long.valueOf(t2.hanTat));
    }

    // ==================== TAT HAN CO CHE ====================

    private static void chotTat() {
        final TrangThai t = new TrangThai();
        for (long luc = DU_TUOI; luc < DU_TUOI + 20 * PHUT; luc += PHUT) {
            Kiem.bang("tu khoi dong lai TAT -> khong bao gio tat may (phut " + (luc / PHUT) + ")",
                    Integer.valueOf(LichSuKien.KHONG_LAM_GI),
                    Integer.valueOf(LichSuKien.nhip(t, luc, 0L, 5, false, 120, 60)));
        }
    }

    // ==================== CHOT 1 (DO THAT): CACH SYSTEMD ====================

    /**
     * Phep thu quan trong nhat cua ca tep nay.
     *
     * htbb-game.service: StartLimitBurst=5, StartLimitIntervalSec=300. Neu hai lan khoi
     * dong lai co the cach nhau duoi 300 giay thi mot nguoi bat vai su kien lien tay se
     * lam unit chet han. O day dien lai dung kich ban do: may chu vua bat lai xong (dong
     * ho ve 0) va admin doi tiep ngay lap tuc.
     */
    private static void khoangCachHaiLanKhoiDong() {
        final TrangThai t = new TrangThai();
        final long batDauLuc = 0L;
        long khiTat = -1L;

        // Admin doi lien tuc moi 15 giay, khong ngung nghi, ngay tu giay dau sau khi bat
        // may. Neu khong co TOI_DA_GOM_MS thi vong nay chay het 30 phut ma khong bao gio
        // tat - dung hong ma trang quan tri van hua "server se tu khoi dong lai".
        for (int i = 0; i < 120 && khiTat < 0; i++) {
            final long luc = i * 15000L;
            final int viec = LichSuKien.nhip(t, luc, batDauLuc, 1 + (i % 3), true, 120, 60);
            if (viec == LichSuKien.YEU_CAU_TAT) {
                khiTat = luc;
            }
        }

        Kiem.dung("doi lien tuc thi van co luc phai tat (tran cung)", khiTat > 0);
        // 3 phut yen + it nhat 2 phut gom + 1 phut bao truoc = 360 giay.
        Kiem.dung("lan tat dau tien cach luc bat may it nhat 360s (thuc te "
                        + (khiTat / 1000) + "s)",
                khiTat >= 360000L);
        Kiem.dung("va do la > cua so 300s cua systemd StartLimitIntervalSec",
                khiTat > 300000L);
        // Tran cung = 15 phut ke tu thay doi dau tien, cong 3 phut yen va 1 phut bao truoc.
        Kiem.dung("khong hoan qua 20 phut du bi doi lien tuc (thuc te "
                        + (khiTat / 1000) + "s)",
                khiTat <= 20 * PHUT);

        Kiem.dung("hang so yen sau khoi dong dung 3 phut",
                LichSuKien.YEN_SAU_KHOI_DONG_MS == 180000L);
        Kiem.dung("nhip kiem tra du day de khong dap CSDL",
                LichSuKien.CHU_KY_MS >= 10000L);
    }

    // ==================== SOI MA NGUON ====================

    private static void soiMaNguon() {
        final String db = doc("src/data/DatabaseSuKien.java");
        Kiem.dung("doc duoc DatabaseSuKien.java", !db.isEmpty());

        // Lich phai so bang NOW() cua CSDL. Dung dong ho cua JVM la lech mui gio thi lich
        // chay som/muon vai tieng ma khong ai hieu vi sao - va trang web thi so bang NOW().
        Kiem.dung("lich so moc bang NOW() cua CSDL", db.contains("<= NOW()"));
        Kiem.sai("khong dung System.currentTimeMillis de so moc lich",
                db.contains("currentTimeMillis") );

        // Moc da chay phai bi xoa, khong thi moi nhip 15 giay lai chay lai mot lan.
        Kiem.dung("moc da chay bi xoa", db.contains("= NULL"));

        // Dem dong cho khoi dong lai PHAI co dieu kien thoi gian - xem chot an toan 3.
        Kiem.dung("chi tinh thay doi sau lan khoi dong gan nhat",
                db.contains("sua_luc > may_chu_luc"));

        // Khong duoc tu tat may trong LichSuKien: duong tat da co san trong BaoTriDaily,
        // viet duong thu hai la hai luong cung luu du lieu mot luc.
        final String lich = doc("src/real/LichSuKien.java");
        Kiem.dung("doc duoc LichSuKien.java", !lich.isEmpty());
        Kiem.sai("LichSuKien KHONG tu goi System.exit", lich.contains("System.exit"));
        Kiem.dung("ma di qua co 5h_systems.restart", db.contains("5h_systems"));

        // Lich phai chay TRUOC khi doc cot `bat` luc khoi dong. Nguoc lai thi moc toi han
        // trong luc may chu tat se phai doi them mot vong khoi dong lai nua moi vao game.
        final String sk = doc("src/real/SuKien.java");
        final int viTriChayLich = sk.indexOf("chayLichDenHan()");
        final int viTriDoc = sk.indexOf("DatabaseSuKien.nap()");
        Kiem.dung("SuKien.nap co chay lich hen", viTriChayLich > 0);
        Kiem.dung("chay lich TRUOC khi doc cot bat",
                viTriChayLich > 0 && viTriDoc > 0 && viTriChayLich < viTriDoc);

        // TeamServer phai khoi dong lich SAU khi nap cong su kien.
        final String ts = doc("src/server/TeamServer.java");
        final int viTriNap = ts.indexOf("real.SuKien.nap()");
        final int viTriLich = ts.indexOf("real.LichSuKien.batDau()");
        Kiem.dung("TeamServer co goi LichSuKien.batDau()", viTriLich > 0);
        Kiem.dung("goi sau khi nap cong su kien", viTriNap > 0 && viTriNap < viTriLich);

        // Ba khoa cau hinh phai that su duoc doc, khong phai chi co trong tai lieu.
        final String ch = doc("src/server/CauHinh.java");
        for (final String k : new String[]{"sv.suKienTuKhoiDongLai", "sv.suKienGomGiay",
            "sv.suKienBaoTruocGiay"}) {
            Kiem.dung("CauHinh co khoa " + k, ch.contains("\"" + k + "\""));
        }
        Kiem.dung("gom giay bi kep trong khoang", ch.contains("trongKhoang"));

        // ---- Trang quan tri (HTBB-Web /admin/sukien; website PHP web/admin/sukien.php da bo) ----
        if (WebHtbb.boQua("lich su kien phia web (HTBB-Web)")) {
            return;
        }
        final String luu = WebHtbb.doc("server/api/admin/sukien/lich.post.ts");
        final String xoa = WebHtbb.doc("server/api/admin/sukien/xoa-lich.post.ts");
        final String logic = WebHtbb.doc("server/utils/suKien.ts");
        final String ds = WebHtbb.doc("server/api/admin/sukien/index.get.ts");
        Kiem.dung("trang web co viec luu lich", luu.contains("suKienKiemLich("));
        Kiem.dung("trang web co viec xoa lich", xoa.contains("SET mo_luc = NULL, dong_luc = NULL"));
        Kiem.dung("trang web ghi hai cot lich", luu.contains("SET mo_luc = ?, dong_luc = ?"));
        // Web va Java phai dung CHUNG mot luat "cho khoi dong lai", khong thi trang bao
        // mot dang con server lam mot neo.
        Kiem.dung("trang web dung dung luat cho khoi dong lai", logic.contains("d.sua_luc > d.may_chu_luc"));
        // Gio so sanh phai lay tu CSDL chu khong phai tu Node.
        Kiem.dung("trang web lay gio tu CSDL",
                WebHtbb.doc("server/utils/suKienDb.ts").contains("'SELECT NOW()'") && luu.contains("await suKienBayGio()"));
        Kiem.dung("trang web chan moc da qua", logic.contains("Mốc mở đã qua"));
        Kiem.dung("trang web chan moc dong truoc moc mo", logic.contains("phải sau mốc mở"));
        // Bang co the chua co hai cot lich (tao truoc 30/08/2026) - trang van phai dung duoc.
        Kiem.dung("trang web chiu duoc bang thieu cot lich", ds.contains("coCotLich"));
        // (Phep kiem lop CSS .lich-form/.lich-o cua admin.css PHP da bo: giao dien Nuxt dung
        // Nuxt UI, khong co tep CSS rieng cho trang nay - xem docs/pokemon/BO_WEB_PHP.md.)
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
