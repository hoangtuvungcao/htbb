import com.htbb.pokemon.ThachDauLogic;
import com.htbb.pokemon.ThachDauLogic.CauHinh;
import com.htbb.pokemon.ThachDauLogic.Chia;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import real.Char;
import real.CharManager;
import real.ThachDauCuoc;

/**
 * Thach dau cuoc xu (loi dai 1-1, nhom) - chu game duyet 19/10/2026: ky quy nguyen tu hai ben, hoan khi huy / hoa /
 * khoi dong lai, phi ben thang (ho hut), muc cuoc theo dai cap, cong chong clone; nhom khong con mat ca hai cuoc.
 */
public final class KiemThachDauCuoc {

    private KiemThachDauCuoc() {
    }

    public static void chay() {
        Kiem.nhom("Thach dau cuoc xu - ky quy, chia tien (vao = ra + phi), muc theo cap, hoa hoan du, nhom het mat cuoc");
        chiaTien();
        mucCuoc();
        kyQuy();
        quetMa();
    }

    static void chiaTien() {
        final CauHinh c = CauHinh.macDinh();
        boolean baoToan = true;
        for (long cuoc = 1000; cuoc <= 100000; cuoc += 777) {
            for (final int thang : new int[]{0, 1, -1, 2}) {
                final Chia ch = ThachDauLogic.chia(c, cuoc, thang);
                baoToan &= ch.tra0 + ch.tra1 + ch.phi == 2 * cuoc && ch.vao == 2 * cuoc;
            }
        }
        Kiem.dung("moi muc cuoc / ket qua: tong vao (2 x cuoc) = tra hai ben + phi", baoToan);
        final Chia t = ThachDauLogic.chia(c, 10000, 0);
        Kiem.bang("ben 0 thang 2 x 10.000: nhan 18.000", 18000L, t.tra0);
        Kiem.bang("ben thua nhan 0", 0L, t.tra1);
        Kiem.bang("phi 10% = 2.000 xu (ho hut)", 2000L, t.phi);
        Kiem.bang("ben 1 thang: ben 1 nhan", 18000L, ThachDauLogic.chia(c, 10000, 1).tra1);
        final Chia h = ThachDauLogic.chia(c, 10000, -1);
        Kiem.dung("hoa: hoan du moi ben, khong phi", h.tra0 == 10000 && h.tra1 == 10000 && h.phi == 0);
        Kiem.bang("phi 0% bi kep len 5%", 5, new CauHinh(true, 40, 1000, 100000, 2, 0).phiPhanTram);
        Kiem.bang("phi 90% bi kep xuong 50%", 50, new CauHinh(true, 40, 1000, 100000, 2, 90).phiPhanTram);
        Kiem.dung("phi mac dinh >= thue cho 7%", c.phiPhanTram >= 7);
    }

    static void mucCuoc() {
        final CauHinh c = CauHinh.macDinh();
        final long g40 = com.htbb.pokemon.LauDai.xuMoiGio(40);
        final long g90 = com.htbb.pokemon.LauDai.xuMoiGio(90);
        Kiem.bang("cap 40: toi da 2 gio thu nhap", 2 * g40, ThachDauLogic.cuocToiDa(c, g40));
        Kiem.bang("cap 90: kep tran tuyet doi 100.000", 100000L, ThachDauLogic.cuocToiDa(c, g90));
        Kiem.bang("cap 40, 1.000 xu: duoc", null, ThachDauLogic.kiemCuoc(c, 1000, 40, g40));
        Kiem.dung("999 xu bi chan", ThachDauLogic.kiemCuoc(c, 999, 40, g40) != null);
        Kiem.dung("cap 40 dat qua 2 gio bi chan", ThachDauLogic.kiemCuoc(c, 2 * g40 + 1, 40, g40) != null);
        Kiem.dung("muc cu 10.000.000 bi chan", ThachDauLogic.kiemCuoc(c, 10000000, 100, g90) != null);
        final String cap = ThachDauLogic.kiemCuoc(c, 1000, 39, g40);
        Kiem.dung("cap 39 bi chan (can 40)", cap != null && cap.contains("40"));
        Kiem.dung("tat: bi chan", ThachDauLogic.kiemCuoc(new CauHinh(false, 40, 1000, 100000, 2, 10), 1000, 50, g40) != null);
    }

    static Char nv(final String ten, final short id, final int dbid, final long xu) {
        final Char p = new Char(null);
        p.charname = ten;
        p.isBot = -1;
        p.id = id;
        p.userID = -dbid;
        p.charDBID = dbid;
        p.setxu(xu);
        return p;
    }

    static void kyQuy() {
        Char a = null;
        Char b = null;
        Char c = null;
        try {
            a = nv("kiem td a", (short) -31001, 931001, 50000);
            b = nv("kiem td b", (short) -31002, 931002, 50000);
            c = nv("kiem td c", (short) -31003, 931003, 5000);
        } catch (final Throwable t) {
            Kiem.dung("tao duoc Char(null) (" + t + ")", false);
            return;
        }
        final CharManager cm = CharManager.instance;
        try {
            cm.put(a);
            cm.put(b);
            cm.put(c);
            final long truoc = a.getxu() + b.getxu();
            Kiem.bang("ky quy 20.000 moi ben: duoc", null, ThachDauCuoc.kyQuy(a, b, 20000, 3, 1900000001));
            Kiem.dung("ky quy: tru ca hai ben", a.getxu() == 30000 && b.getxu() == 30000);
            Kiem.dung("ky quy dang giu", ThachDauCuoc.coKyQuy(1900000001));
            final Chia ch = ThachDauCuoc.ketThuc(1900000001, 0);
            Kiem.dung("ket thuc ben 0 thang: ben 0 nhan 36.000", ch != null && a.getxu() == 30000 + 36000 && b.getxu() == 30000);
            Kiem.bang("tong xu hai ben = truoc - phi (khong in xu)", truoc - 4000L, a.getxu() + b.getxu());
            Kiem.bang("ket thuc lan hai: khong tra them", null, ThachDauCuoc.ketThuc(1900000001, 0));
            Kiem.bang("tong khong doi sau lan hai", truoc - 4000L, a.getxu() + b.getxu());

            final long a0 = a.getxu();
            final String thieu = ThachDauCuoc.kyQuy(a, c, 20000, 3, 1900000002);
            Kiem.dung("doi thu thieu xu: huy, bao loi", thieu != null && thieu.contains("không đủ"));
            Kiem.dung("doi thu thieu xu: hoan ben da tru, ben thieu khong bi tru", a.getxu() == a0 && c.getxu() == 5000);
            Kiem.sai("doi thu thieu xu: khong giu ky quy", ThachDauCuoc.coKyQuy(1900000002));

            final long b0 = b.getxu();
            Kiem.bang("ky quy de hoa", null, ThachDauCuoc.kyQuy(a, b, 10000, 4, 1900000003));
            ThachDauCuoc.ketThuc(1900000003, -1);
            Kiem.dung("hoa (nhom kieu 4): hoan du hai ben - het mat ca hai cuoc", a.getxu() == a0 && b.getxu() == b0);

            // nguyen tu: nhieu tran cung luc dung het so du cua a - chi mot tran duoc giu
            a.setxu(10000);
            final AtomicInteger duoc = new AtomicInteger();
            final Thread[] th = new Thread[8];
            final Char fa = a;
            final Char fb = b;
            for (int i = 0; i < th.length; i++) {
                final int id = 1900000010 + i;
                th[i] = new Thread(() -> {
                    if (ThachDauCuoc.kyQuy(fa, fb, 10000, 3, id) == null) {
                        duoc.incrementAndGet();
                    }
                });
            }
            for (final Thread t : th) {
                t.start();
            }
            for (final Thread t : th) {
                try {
                    t.join();
                } catch (final InterruptedException ignored) {
                }
            }
            Kiem.bang("8 tran cung luc voi 10.000 xu cua a: dung 1 tran duoc ky quy", 1, duoc.get());
            Kiem.bang("so du a sau do = 0, khong am", 0L, a.getxu());
            for (int i = 0; i < th.length; i++) {
                ThachDauCuoc.ketThuc(1900000010 + i, -1);
            }
            Kiem.bang("hoa tran do: a nhan lai 10.000", 10000L, a.getxu());

            // nguoi thang offline: khong cong vao doi tuong trong bo nho (tra qua cho_nhan khi dang nhap)
            Kiem.bang("ky quy (thang offline)", null, ThachDauCuoc.kyQuy(a, b, 5000, 3, 1900000020));
            cm.remove(a);
            final long aOff = a.getxu();
            final Chia off = ThachDauCuoc.ketThuc(1900000020, 0);
            Kiem.dung("thang offline: van chia (tra qua tob_vong_quay_cho_nhan), khong cong truc tiep", off != null && off.tra0 == 9000 && a.getxu() == aOff);
        } finally {
            cm.remove(a);
            cm.remove(b);
            cm.remove(c);
        }
    }

    static void quetMa() {
        final String ad = doc("src/real/ArenaData.java");
        Kiem.dung("ArenaData.onDraw: hoan qua ThachDauCuoc.ketThuc(idArena, -1)", ad.contains("ThachDauCuoc.ketThuc(this.idArena, -1)"));
        Kiem.dung("ArenaData.addGift: chia qua ThachDauCuoc.ketThuc(idArena, twin) cho kieu 3 va 4",
                ad.contains("ThachDauCuoc.ketThuc(this.idArena, twin)") && ad.contains("this.typeArena == 3 || this.typeArena == 4"));
        Kiem.sai("ArenaData: het addXu(this.moneyThachdau...) truc tiep", ad.contains("addXu(this.moneyThachdau"));
        final String map = doc("src/real/Map.java");
        final int a = map.indexOf("} else if (idpopup == 10 && idActor == player.id) {");
        final String p10 = a < 0 ? "" : map.substring(a, Math.min(map.length(), a + 5000));
        Kiem.dung("popup 10 (1-1): ky quy TRUOC khi tao arena", p10.indexOf("ThachDauCuoc.kyQuy(") >= 0
                && p10.indexOf("ThachDauCuoc.kyQuy(") < p10.indexOf("new ArenaData()"));
        Kiem.dung("popup 10: muc theo cap + cong chong clone", p10.contains("ThachDauCuoc.kiemCuoc(cuocTran, px, player)") && p10.contains("chanCaHai(px, player"));
        Kiem.sai("popup 10: het subXu rieng tung ben", p10.contains("subXu((long) px.moneyThachdau"));
        Kiem.sai("Map.doChallenge (tra 2 lan, khong thue) da bo", map.contains("private void doChallenge("));
        final String xl = doc("src/real/MapXuLyLenh.java");
        final int b = xl.indexOf("static void doAcceptChallengeParty(");
        final String nhom = b < 0 ? "" : xl.substring(b, Math.min(xl.length(), b + 6000));
        Kiem.dung("nhom: ky quy qua ThachDauCuoc.kyQuy(p, player, cuoc, 4, idTran)", nhom.contains("ThachDauCuoc.kyQuy(p, player, cuoc, 4, idTran)"));
        Kiem.sai("nhom: het truXuNeuDu rieng (tien vao arena khong ai nhan)", nhom.contains("truXuNeuDu(cuoc, false, \"map 32\")"));
        final String nt = doc("src/real/MapNhapText.java");
        Kiem.sai("MapNhapText: het muc 100.000 - 10.000.000", nt.contains("moneyxxxx > 10000000") || nt.contains("player.moneyThachdau <= 10000000"));
        Kiem.dung("MapNhapText / MapXaHoi: muc theo cap", nt.contains("ThachDauCuoc.kiemCuoc(moneyxxxx, player)")
                && doc("src/real/MapXaHoi.java").contains("ThachDauCuoc.kiemCuoc(player.moneyThachdau, moiNguoi"));
        Kiem.dung("khoi dong: hoan ky quy con lai", doc("src/data/Database.java").contains("real.ThachDauCuoc.napLucKhoiDong()"));
        final String dm = DayDu.danhMucJson(); // danh muc trong CSDL (ban chup sql/htbb_day_du.sql)
        Kiem.dung("danh muc: thach_dau.phi_phan_tram = 10, cuoc_toi_da = 100000", dm.contains("\"khoa\": \"thach_dau.phi_phan_tram\"")
                && dm.matches("(?s).*\"thach_dau.cuoc_toi_da\"[^}]*\"macDinh\": \"100000\".*"));
        Kiem.dung("sql/nang_cap.sql (phan 9): bang ky quy + danh muc", doc("../sql/nang_cap.sql").contains("tob_thach_dau_ky_quy")
                && doc("../sql/nang_cap.sql").contains("'thach_dau.phi_phan_tram'"));
    }

    static String doc(final String tep) {
        try {
            return new String(Files.readAllBytes(Paths.get(tep)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }
}
