import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Kiem thu NPC an cua admin (Map.NAME_NPC_AN = "ADMIN").
 *
 * VI SAO CAN TEST NAY: NPC do den client qua BA duong khac nhau trong
 * CharCapNhat.updateNearChar, va truoc day chi MOT duong duoc chan:
 *
 *   1) vong lam moi vi tri cac char da co trong nearChars      -> truoc day KHONG chan
 *   2) vong rieng duyet Map.npcVilage                          -> DA chan
 *   3) vong quet nguoi choi quanh day (getAllPlayerMap)        -> truoc day KHONG chan
 *
 * Duong (3) la cho lam lo: MapKhoiTao goi addPlayer() dua moi NPC trong npcVilage vao
 * map.allPlayers, nen vong quet nguoi choi cung nhat duoc NPC nay. Bo loc duy nhat o do
 * la `!p1.isAdmin`, ma bot nay khong bao gio duoc dat isAdmin (mac dinh false tu
 * CharKhoiTao) -> lot qua het, nguoi choi thuong nhin thay NPC dang le chi admin moi thay.
 *
 * Nen test khong chi kiem tra ham loc chay dung, ma con kiem tra no duoc GOI DU CHO.
 */
public final class KiemNpcAn {

    private static final String F_CAP_NHAT = "src/real/CharCapNhat.java";
    private static final String F_MAP = "src/real/Map.java";

    public static void chay() {
        Kiem.nhom("NPC an cua admin - ham loc");

        Kiem.sai("null khong phai NPC an (khong duoc nem NullPointerException)",
                real.Map.laNpcAnCuaAdmin(null));

        Kiem.bang("ten NPC an van la \"ADMIN\" chu hoa", "ADMIN", real.Map.NAME_NPC_AN);

        // Ten phai so sanh PHAN BIET HOA THUONG: nhan vat that ten "admin" chu thuong
        // KHONG duoc bi coi la NPC an, neu khong ho se tang hinh voi moi nguoi.
        final real.GemTemplate[] gemCu = buGemTemplate();
        try {
            final real.Char npc = taoChar("ADMIN");
            final real.Char nguoiThat = taoChar("admin");
            final real.Char nguoiKhac = taoChar("SONGVEDEM");
            Kiem.dung("dung duoc doi tuong Char de kiem tra that (khong bo qua)",
                    npc != null && nguoiThat != null && nguoiKhac != null);
            if (npc != null && nguoiThat != null && nguoiKhac != null) {
                Kiem.dung("Char ten \"ADMIN\" la NPC an", real.Map.laNpcAnCuaAdmin(npc));
                Kiem.sai("Char ten \"admin\" (chu thuong) KHONG phai NPC an",
                        real.Map.laNpcAnCuaAdmin(nguoiThat));
                Kiem.sai("nhan vat thuong KHONG phai NPC an",
                        real.Map.laNpcAnCuaAdmin(nguoiKhac));
            }
        } finally {
            real.Map.gemTemplate = gemCu;
        }

        Kiem.nhom("NPC an cua admin - da chan du ca ba duong gui chua");

        final String maCapNhat = doc(F_CAP_NHAT);
        final String maMap = doc(F_MAP);
        if (maCapNhat == null || maMap == null) {
            Kiem.dung("bo qua: khong doc duoc ma nguon (chay tu thu muc goc du an)", true);
            return;
        }

        Kiem.dung("Map co ham loc dung chung laNpcAnCuaAdmin",
                maMap.contains("public static boolean laNpcAnCuaAdmin"));

        // Ba duong gui actor trong updateNearChar deu phai hoi ham loc.
        final int soLanChan = dem(maCapNhat, "laNpcAnCuaAdmin");
        Kiem.dung("CharCapNhat chan du ca 3 duong gui actor (dang co " + soLanChan + ")",
                soLanChan >= 3);

        // Moi lan chan deu phai kem "!self.isAdmin" TREN CUNG MOT DONG: admin VAN phai
        // thay NPC nay de cham vao mo bang setting. Chan luon ca admin la mat duong vao
        // bang setting, ma loi do se khong ai phat hien ra cho den luc can dung.
        int chanCoNgoaiLe = 0;
        for (final String dong : maCapNhat.split("\n")) {
            if (dong.contains("laNpcAnCuaAdmin(") && dong.contains("!self.isAdmin")) {
                chanCoNgoaiLe++;
            }
        }
        Kiem.bang("moi lan chan deu chua ngoai le cho admin",
                Integer.valueOf(soLanChan), Integer.valueOf(chanCoNgoaiLe));

        // Chu thich tro toi ham da bi doi ten chinh la thu khien loi nay nap duoc lau:
        // ba cho ghi "xem Char.updateActorPos" trong khi ham do khong con ton tai.
        Kiem.sai("khong con chu thich tro toi Char.updateActorPos (ham da khong con)",
                maCapNhat.contains("Char.updateActorPos") || maMap.contains("Char.updateActorPos"));
    }

    /**
     * Dung mot Char toi gian.
     *
     * Constructor cua Char doc Map.gemTemplate, ma bang do chi duoc nap tu CSDL luc server
     * khoi dong nen chay ngoai server no la null -> NullPointerException. Bu tam mot bang
     * rong roi TRA LAI NGUYEN TRANG o cuoi (xem chay()), de khong lam sai lech test khac.
     */
    private static real.Char taoChar(final String ten) {
        try {
            final real.Char c = new real.Char(null);
            c.charname = ten;
            return c;
        } catch (final Throwable t) {
            return null;
        }
    }

    /** @return bang gemTemplate cu, de tra lai sau khi test xong. */
    private static real.GemTemplate[] buGemTemplate() {
        final real.GemTemplate[] cu = real.Map.gemTemplate;
        if (cu == null) {
            final real.GemTemplate[] tam = new real.GemTemplate[400];
            for (int i = 0; i < tam.length; i++) {
                tam[i] = new real.GemTemplate();
            }
            real.Map.gemTemplate = tam;
        }
        return cu;
    }

    private static int dem(final String trong, final String tim) {
        int n = 0;
        int i = trong.indexOf(tim);
        while (i >= 0) {
            n++;
            i = trong.indexOf(tim, i + tim.length());
        }
        return n;
    }

    private static String doc(final String duongDan) {
        final File f = new File(duongDan);
        if (!f.isFile()) {
            return null;
        }
        Reader r = null;
        try {
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            final StringBuilder sb = new StringBuilder();
            final char[] dem = new char[8192];
            int n;
            while ((n = r.read(dem)) > 0) {
                sb.append(dem, 0, n);
            }
            return sb.toString();
        } catch (final Exception e) {
            return null;
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (final Exception ignored) {
            }
        }
    }

    private KiemNpcAn() {
    }
}
