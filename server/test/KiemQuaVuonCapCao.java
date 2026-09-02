import data.FruitTemplate;
import server.TeamServer;

/**
 * Qua kinh nghiem cua o vuon (FruitTemplate.initGift) voi nhan vat cap cao / cap thap.
 *
 * Su co 02/09/2026: nhan vat duoc web buff thang len cap 90, lan dang nhap ke tiep server nem
 * ArrayIndexOutOfBoundsException: 11 tai FruitTemplate.initGift (bang EXP chi co du lieu toi
 * hang 9 = cap 79..83, hang 10 rong). Loi noi trong SelectCharHandler -> initInfoFromDB ->
 * createFruit, bi Session.run nuot, nhan vat khong duoc dua vao ban do -> client thay map
 * trong, "khong hien thi nhan vat". Chi lo ra khi phai TAO O VUON MOI luc dang nhap (o vuon
 * chua luu du), nen nhan vat 90 cay dan khong gap, nhan vat nhay cap thi gap.
 */
public final class KiemQuaVuonCapCao {

    public static void chay() {
        Kiem.nhom("Qua vuon - cap cao / cap thap khong lam gay dang nhap (FruitTemplate.initGift)");

        // Tien de ve bang EXP: 11 hang, hang cuoi rong (giu cho), hang 0..9 co [min, max] tang.
        Kiem.bang("(tien de) bang EXP co 11 hang", 11, FruitTemplate.EXP.length);
        Kiem.bang("(tien de) hang 10 la mang rong - cong thuc goc se nem loi o day", 0, FruitTemplate.EXP[10].length);
        boolean hopLe = true;
        for (int i = 0; i <= 9; i++) {
            if (FruitTemplate.EXP[i].length != 2 || FruitTemplate.EXP[i][1] <= FruitTemplate.EXP[i][0]) {
                hopLe = false;
            }
        }
        Kiem.dung("hang 0..9 deu co [min, max] voi max > min (de nextInt(max - min) khong nem loi)", hopLe);

        // Cong thuc goc (lv - 34) / 5 giu nguyen trong khoang no dung.
        Kiem.bang("cap 34 -> hang 0", 0, FruitTemplate.hangExpTheoCap(34));
        Kiem.bang("cap 38 -> hang 0", 0, FruitTemplate.hangExpTheoCap(38));
        Kiem.bang("cap 39 -> hang 1", 1, FruitTemplate.hangExpTheoCap(39));
        Kiem.bang("cap 60 -> hang 5", 5, FruitTemplate.hangExpTheoCap(60));
        Kiem.bang("cap 79 -> hang 9", 9, FruitTemplate.hangExpTheoCap(79));
        Kiem.bang("cap 83 -> hang 9 (hang cuoi co du lieu)", 9, FruitTemplate.hangExpTheoCap(83));

        // Ngoai khoang: kep, khong nem.
        Kiem.bang("cap 84 (cong thuc goc -> hang 10 rong) -> kep ve 9", 9, FruitTemplate.hangExpTheoCap(84));
        Kiem.bang("cap 88 -> 9", 9, FruitTemplate.hangExpTheoCap(88));
        Kiem.bang("cap 89 (cong thuc goc -> 11, ngoai bang) -> 9", 9, FruitTemplate.hangExpTheoCap(89));
        Kiem.bang("cap 90 (dung su co) -> 9", 9, FruitTemplate.hangExpTheoCap(90));
        Kiem.bang("cap 150 -> 9", 9, FruitTemplate.hangExpTheoCap(150));
        Kiem.bang("cap 1 (cong thuc goc am) -> 0", 0, FruitTemplate.hangExpTheoCap(1));
        Kiem.bang("cap 29 (cong thuc goc -1) -> 0", 0, FruitTemplate.hangExpTheoCap(29));
        Kiem.bang("cap 33 -> 0", 0, FruitTemplate.hangExpTheoCap(33));

        // Chay that initGift voi qua kinh nghiem (type 0) o cac cap tung nem loi.
        final boolean expQuaCu = TeamServer.ExpQua;
        try {
            TeamServer.ExpQua = true;
            final int[] cac = {1, 29, 84, 88, 89, 90};
            for (final int lv : cac) {
                try {
                    final FruitTemplate f = new FruitTemplate();
                    f.type = 0;
                    f.initGift(lv);
                    final int hang = FruitTemplate.hangExpTheoCap(lv);
                    Kiem.dung("initGift(" + lv + ") khong nem loi va qua nam trong [min, max) cua hang " + hang,
                            f.gift >= FruitTemplate.EXP[hang][0] && f.gift < FruitTemplate.EXP[hang][1]);
                } catch (final Throwable t) {
                    Kiem.dung("initGift(" + lv + ") khong nem loi (" + t + ")", false);
                }
            }
        } finally {
            TeamServer.ExpQua = expQuaCu;
        }

        // Diem goi duy nhat van la createFruit luc dang nhap - noi da gay su co.
        Kiem.dung("Char.createFruit goi initGift(lvDetail.lv) - dung duong dang nhap da gay su co",
                doc("src/real/Char.java").contains("this.fruit[i].initGift(this.lvDetail.lv);"));
        Kiem.sai("khong con chi so bang tinh thang (lvChar - 34) / 5 ngoai ham kep",
                doc("src/data/FruitTemplate.java").replace("int id = (lvChar - 34) / 5;", "").contains("(lvChar - 34) / 5"));
    }

    private static String doc(final String duongDan) {
        try {
            return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(duongDan)),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (final java.io.IOException e) {
            Kiem.dung("doc duoc " + duongDan, false);
            return "";
        }
    }

    private KiemQuaVuonCapCao() {
    }
}
