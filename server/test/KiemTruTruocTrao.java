import data.Animal;
import data.Pet;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import real.Char;
import real.QuanLyThuGame;
import real.TruTruocTrao;

/**
 * TRU TRUOC, TRAO SAU (29/10/2026, docs/kiem-thu/ma-tran-bot.md "Lo hong uu tien" #2 cho, #3 quy doi, #4 nang cap cu,
 * #5 ghep ngoc / che tao, #6 ban thu cho NPC, #9 kho do; them: giao dich pet popup 112, dau gia Market).
 *
 * LOI (xac nhan bang mo hinh duoi day, dung Char.subXu THAT): kieu cu "if (getxu() &lt; gia) bao; ... TRAO ...;
 * subXu(gia)" - subXu CHAN VE 0. Cac lenh cua cung mot nhan vat chay tren hai luong (luong phien: RealController.privateCmd;
 * luong ban do: con lai), nen mot lenh tieu xu xen vao giua kiem va tru -&gt; ket qua da trao = mien phi; o cho / giao dich
 * pet, nguoi ban con duoc cong du = xu SINH RA. Quy doi: quy cong {@code money} trong khi chi tru duoc it hon.
 *
 * SUA. real.TruTruocTrao: kiem-va-tru MOT buoc duoi Char.KHOA_TIEN (truXuNeuDu) TRUOC khi trao; trao hong / nem loi -&gt;
 * hoan dung so da tru. Ban thu cho NPC = luat Tha (QuanLyThuGame.rutDeBanNpc). Kho do 68/69 cung luong + synchronized.
 */
public final class KiemTruTruocTrao {

    private KiemTruTruocTrao() {
    }

    public static void chay() {
        Kiem.nhom("Tru truoc trao sau: cho / quy doi / ghep ngoc / che tao / nang cap cu / ban thu NPC / kho do");
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        final data.PetTemplate[] petCu = real.Map.petTemplate;
        try {
            if (real.Map.petTemplate == null) {
                real.Map.loadPetTemplate();
            }
            final real.GemTemplate[] g = new real.GemTemplate[QuanLyThuGame.ID_VE + 1];
            for (int i = 0; i < g.length; i++) {
                g[i] = new real.GemTemplate();
                g[i].id = (short) i;
                g[i].name = "Nguyên liệu " + i;
            }
            real.Map.gemTemplate = g;
            moHinhCu();
            voiPhi();
            choVaQuy();
            ngocNangCapCu();
            banThuNpc();
            petDangChao();
            maNguon();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        } finally {
            real.Map.gemTemplate = gemCu;
            real.Map.petTemplate = petCu;
        }
    }

    static Char nv(final long xu) {
        final Char p = new Char(null);
        p.charname = "kiem ttt";
        p.isBot = -1;
        p.setxu(xu);
        return p;
    }

    /** Mo hinh dung thu tu CU (kiem -&gt; trao -&gt; tru chan 0) voi mot lenh tieu xu xen giua - de chung minh lo hong. */
    static void moHinhCu() {
        final Char mua = nv(10000);
        final Char ban = nv(0);
        boolean nhanMon = false;
        if (mua.getxu() >= 10000) {                       // kiem
            ban.addXu(10000, "kiem", Char.XU_CHUYEN);       // cong nguoi ban truoc
            mua.subXu(10000, false, "xen vao: mua 100 binh thuoc", Char.XU_CHUYEN); // luong kia tieu het xu
            nhanMon = true;                                 // trao mon
            mua.subXu(10000, false, "kiem", Char.XU_CHUYEN); // tru gia - CHAN VE 0
        }
        final long tieuThat = 10000 + 10000;                // gia mon + so xu luong kia tieu
        Kiem.dung("MO HINH CU: nguoi mua nhan mon + tieu 10.000 o luong khac du chi co 10.000 (xu sinh ra)",
                nhanMon && mua.getxu() == 0 && ban.getxu() == 10000 && tieuThat > 10000);

        // thu tu MOI: tru truoc -> lenh xen vao thay het xu -> khong tieu duoc; mon chi trao khi da tra
        final Char mua2 = nv(10000);
        final Char ban2 = nv(0);
        final boolean tra = TruTruocTrao.truNguoiMuaCho(mua2, 10000);
        final boolean xen = mua2.truXuNeuDu(10000, false, "xen vao");
        if (tra) {
            ban2.addXu(10000, "kiem", Char.XU_CHUYEN);
        }
        Kiem.dung("THU TU MOI: tru nguoi mua truoc, lenh xen vao bi tu choi, tong xu bao toan",
                tra && !xen && mua2.getxu() == 0 && ban2.getxu() == 10000 && mua2.getxu() + ban2.getxu() == 10000);
    }

    static void voiPhi() throws Exception {
        final Char p = nv(5000);
        final AtomicInteger trao = new AtomicInteger();
        // trong luc "trao", mot lenh khac tieu xu: khong con xu de tieu (da tru phi)
        TruTruocTrao.KetQua kq = TruTruocTrao.voiPhi(p, 5000, "kiem", () -> {
            Kiem.sai("trong luc trao: lenh xen vao KHONG tieu duoc xu da tru", p.truXuNeuDu(1, false, "xen"));
            trao.incrementAndGet();
            return true;
        });
        Kiem.dung("voiPhi du xu: XONG, trao 1 lan, xu 5000 -> 0", kq == TruTruocTrao.KetQua.XONG && trao.get() == 1 && p.getxu() == 0);
        kq = TruTruocTrao.voiPhi(p, 5000, "kiem", () -> {
            trao.incrementAndGet();
            return true;
        });
        Kiem.dung("voiPhi thieu xu: THIEU_XU, KHONG trao", kq == TruTruocTrao.KetQua.THIEU_XU && trao.get() == 1 && p.getxu() == 0);
        p.setxu(7000);
        kq = TruTruocTrao.voiPhi(p, 5000, "kiem", () -> Boolean.FALSE);
        Kiem.dung("voiPhi trao hong: HONG, hoan du (7000)", kq == TruTruocTrao.KetQua.HONG && p.getxu() == 7000);
        boolean nem = false;
        try {
            TruTruocTrao.voiPhi(p, 5000, "kiem", () -> {
                throw new IllegalStateException("goi cut");
            });
        } catch (final IllegalStateException e) {
            nem = true;
        }
        Kiem.dung("voiPhi trao nem loi: loi di tiep, xu hoan du (7000)", nem && p.getxu() == 7000);
        Kiem.dung("voiPhi phi am: tu choi, khong doi xu",
                TruTruocTrao.voiPhi(p, -5, "kiem", () -> Boolean.TRUE) == TruTruocTrao.KetQua.THIEU_XU && p.getxu() == 7000);
    }

    static void choVaQuy() {
        final Char mua = nv(999);
        Kiem.sai("cho: thieu 1 xu -> khong tru", TruTruocTrao.truNguoiMuaCho(mua, 1000));
        Kiem.bang("cho: thieu 1 xu -> xu giu nguyen", 999L, mua.getxu());
        mua.setxu(1000);
        Kiem.dung("cho: vua du -> tru het", TruTruocTrao.truNguoiMuaCho(mua, 1000) && mua.getxu() == 0);
        Kiem.sai("cho: gia 0 / am -> tu choi", TruTruocTrao.truNguoiMuaCho(mua, 0) || TruTruocTrao.truNguoiMuaCho(mua, -1));
        TruTruocTrao.hoanNguoiMuaCho(mua, 1000);
        Kiem.bang("cho: mon het -> hoan dung gia", 1000L, mua.getxu());

        final Char g = nv(300000);
        Kiem.dung("gop quy: du -> tru dung so gop", TruTruocTrao.truGopQuy(g, 300000) && g.getxu() == 0);
        Kiem.sai("gop quy: lan hai (het xu) -> khong tru, quy khong duoc cong", TruTruocTrao.truGopQuy(g, 300000));
        Kiem.sai("gop quy: so am -> tu choi", TruTruocTrao.truGopQuy(nv(10), -5));
    }

    static void ngocNangCapCu() {
        final int[] th = new int[20];
        final int[] ban = new int[20];
        final int[] kh = new int[20];
        th[8] = 1;
        Kiem.dung("nang cap cu: 1 da thuong trong tui, o can 1 -> du", ngoc(th, ban, kh, new int[]{8}, 0));
        th[8] = 0;
        Kiem.sai("nang cap cu: da da BAN (78) sau khi them vao o -> KHONG du (truoc day van dap, mien phi)", ngoc(th, ban, kh, new int[]{8}, 0));
        th[8] = 1;
        ban[8] = 1;
        Kiem.sai("nang cap cu: da dang dat cho -> khong tinh", ngoc(th, ban, kh, new int[]{8}, 0));
        ban[8] = 0;
        th[3] = 1;
        Kiem.sai("nang cap cu: o co 2 vien cung loai ma tui chi 1 -> khong du", ngoc(th, ban, kh, new int[]{3, 3}, 0));
        kh[8] = 1;
        Kiem.dung("nang cap cu: kieu khoa (1) dem da khoa", ngoc(th, ban, kh, new int[]{8}, 1));
        kh[8] = 0;
        Kiem.sai("nang cap cu: kieu khoa (1) khong co da khoa", ngoc(th, ban, kh, new int[]{8}, 1));
        kh[3] = 1;
        Kiem.dung("nang cap cu: kieu tron (2) khoa + thuong", ngoc(th, ban, kh, new int[]{3, 3}, 2));
        Kiem.sai("nang cap cu: id ngoai bang -> khong du", ngoc(th, ban, kh, new int[]{99}, 0));
    }

    static boolean ngoc(final int[] th, final int[] ban, final int[] kh, final int[] ids, final int kieu) {
        try {
            final java.lang.reflect.Method m = Class.forName("real.MapEpNgoc").getDeclaredMethod("duNgoc", int[].class, int[].class, int[].class,
                    int[].class, int.class);
            m.setAccessible(true);
            return ((Boolean) m.invoke(null, th, ban, kh, ids, Integer.valueOf(kieu))).booleanValue();
        } catch (final Exception e) {
            Kiem.dung("goi duoc MapEpNgoc.duNgoc (" + e + ")", false);
            return false;
        }
    }

    static void banThuNpc() {
        final Char p = KiemQuanLyThu.nhanVat(915);
        final Animal a = KiemQuanLyThu.thuCuoi(p, 9, 1);
        final Pet t = KiemQuanLyThu.pet(p, 5);
        QuanLyThuGame.doiKhoa(p, 0, a.id, true);
        Kiem.dung("ban NPC: thu cuoi DA KHOA -> tu choi (truoc day -800 xoa luon)", QuanLyThuGame.rutDeBanNpc(p, a) != null && p.animal.contains(a));
        QuanLyThuGame.doiKhoa(p, 0, a.id, false);
        p.thuChoTang = a;
        Kiem.dung("ban NPC: thu dang cho tang -> tu choi", QuanLyThuGame.rutDeBanNpc(p, a) != null && p.animal.contains(a));
        p.thuChoTang = null;
        p.userTrade.add(KiemQuanLyThu.nhanVat(915));
        Kiem.dung("ban NPC: dang trao doi (66) -> tu choi", QuanLyThuGame.rutDeBanNpc(p, a) != null && p.animal.contains(a));
        p.userTrade.clear();
        p.nameTangPet = "nguoi mua";
        p.idPetTang = t.id;
        Kiem.dung("ban NPC (-801): pet dang chao giao dich (popup 111/112) -> tu choi", QuanLyThuGame.rutDeBanNpc(p, t) != null && p.pet.contains(t));
        p.nameTangPet = "";
        Kiem.bang("ban NPC: thu tu do -> rut duoc", null, QuanLyThuGame.rutDeBanNpc(p, a));
        Kiem.sai("ban NPC: da rut khoi danh sach", p.animal.contains(a));
        Kiem.dung("ban NPC lan hai (bam dup) -> 'khong con', khong xoa them", QuanLyThuGame.rutDeBanNpc(p, a) != null);
    }

    static void petDangChao() {
        final Char ban = KiemQuanLyThu.nhanVat(915);
        final Char mua = KiemQuanLyThu.nhanVat(915);
        final Pet t1 = KiemQuanLyThu.pet(ban, 5);
        final Pet t2 = KiemQuanLyThu.pet(ban, 5);
        ban.idPetTang = t1.id;
        ban.nameTangPet = mua.charname;
        Kiem.dung("pet dang chao: dung con", QuanLyThuGame.petDangChao(ban, mua) == t1);
        ban.pet.remove(t1);
        Kiem.bang("pet dang chao da bien mat -> null (vong lap cu tra con CUOI = ban nham con khac)", null, QuanLyThuGame.petDangChao(ban, mua));
        Kiem.dung("... con khac khong bi dong toi", ban.pet.contains(t2));
        ban.pet.add(t1);
        ban.nameTangPet = "nguoi khac";
        Kiem.bang("chao cho nguoi khac -> null", null, QuanLyThuGame.petDangChao(ban, mua));
    }

    static void maNguon() {
        final String om = doc("src/real/OfflineMap.java");
        final int mua = om.indexOf("public synchronized void buyItemOfUser(Char player)");
        final int tru = om.indexOf("TruTruocTrao.truNguoiMuaCho(player, gia)", mua);
        final int rut = om.indexOf("p.iItems.remove((Item)item)", mua);
        final int cong = om.indexOf("p.addXu(player.sellerInfo.priceSell - money", mua);
        Kiem.dung("cho (-32): tru nguoi mua TRUOC khi rut mon va cong nguoi ban", mua > 0 && tru > mua && tru < rut && rut < cong);
        Kiem.sai("cho: het subXu nguoi mua sau khi cong", om.contains("player.subXu(player.sellerInfo.priceSell"));
        Kiem.dung("cho ngoc: tru ngoc nguoi ban kiem-va-tru (truGemNeuDu), khong delGem sau",
                om.contains("p.truGemNeuDu(((GemSellEntry)item).idGemTemplate") && !om.contains("p.delGem(entry.idGemTemplate, entry.soLuong, false)"));
        final String mk = doc("src/real/Market.java");
        Kiem.sai("dau gia: het subXu sau khi trao / hoan", mk.contains("player.subXu(item.priceSell") || mk.contains("player.subXu(priceBid"));

        final String m = doc("src/real/Map.java");
        final int q = m.indexOf("if (idpopup == 114) {");
        final int tq = m.indexOf("TruTruocTrao.truGopQuy(player, (long) money)", q);
        final int cq = m.indexOf("clandata.addMoney2Clan((long) money)", q);
        Kiem.dung("gop quy: tru TRUOC khi cong quy", q > 0 && tq > q && tq < cq);
        Kiem.sai("gop quy: het subXu \"map 777\"", m.contains("player.subXu((long) money, false, \"map 777\")"));
        final int g = m.indexOf("if (idpopup == 112) {");
        final int tg = m.indexOf("player.truXuNeuDu(moneytrade, false, \"map 27\"", g);
        final int rg = m.indexOf("p.pet.remove(pet)", g);
        final int cg = m.indexOf("p.addXu(moneytrade * 90L / 100L", g);
        Kiem.dung("giao dich pet (112): tru nguoi mua -> rut pet -> cong nguoi ban", g > 0 && tg > g && tg < rg && rg < cg);

        final String en = doc("src/real/MapEpNgoc.java");
        final int e = en.indexOf("static void doFinishEpNgoc(");
        final int te = en.indexOf("player.truXuNeuDu((long) prize, true, \"map 76\")", e);
        final int re = en.indexOf("epNgocThanhCong(r.nextInt(100), gem.size())", e);
        Kiem.dung("ghep ngoc (-36): tru xu TRUOC khi tung / trao", e > 0 && te > e && te < re);
        Kiem.sai("ghep ngoc: het subXu \"map 76\" o cuoi", en.contains("player.subXu((long) prize, true, \"map 76\")"));
        final int f = en.indexOf("static void doFinishImbue(");
        final int dn = en.indexOf("duNgocNangCapCu(player)", f);
        final int tx = en.indexOf("player.truXuNeuDu((long) prizeImbue, true, \"map 72\")", f);
        final int tn = en.indexOf("truNgocNangCapCu(player)", f);
        final int lc = en.indexOf("map.calculatePercenUp(player, percent[item.plus])", f);
        Kiem.dung("nang cap cu (77, LauDai tat): dem ngoc -> tru xu -> tru ngoc -> moi tung", f > 0 && dn > f && dn < tx && tx < tn && tn < lc);

        final String ct = doc("src/real/MapCheTao.java");
        Kiem.dung("che tao cu (-52): TruTruocTrao.voiPhi, het subXu 5000 sau khi trao",
                ct.contains("TruTruocTrao.voiPhi(player, 5000L") && !ct.contains("player.subXu(5000L"));

        final String rc = doc("src/real/RealController.java");
        final String dm = doc("src/real/MapDoMenu.java");
        Kiem.dung("ban thu NPC (-800/-801): ca 3 nhanh qua QuanLyThuGame.rutDeBanNpc",
                rc.contains("QuanLyThuGame.rutDeBanNpc(p, anim)") && dm.contains("QuanLyThuGame.rutDeBanNpc(player, anim)")
                        && dm.contains("QuanLyThuGame.rutDeBanNpc(player, pet)") && !rc.contains("p.animal.remove(idMenu)")
                        && !dm.contains("player.animal.remove(idMenu)") && !dm.contains("player.pet.remove(idMenu)"));
        Kiem.dung("kho do: 69 nam trong privateCmd (cung luong phien voi 68)", rc.contains("70, 68, 69, 9,"));
        Kiem.dung("kho do: OfflineMap + Map 68/69 duoi synchronized (player), 69 kiem trao doi",
                om.contains("doGetItemFromBagKhoa(player, itemID);") && om.contains("doPutItem2BagKhoa(player, itemID);")
                        && m.contains("doGetItemFromBagKhoa(player, itemID);") && m.contains("doPutItem2BagKhoa(player, itemID);")
                        && count(om + m, "synchronized (player) {\n") >= 4 && count(om + m, "69 truoc day khong kiem trao doi") == 2);
    }

    static int count(final String s, final String k) {
        int n = 0;
        for (int i = s.indexOf(k); i >= 0; i = s.indexOf(k, i + 1)) {
            n++;
        }
        return n;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
