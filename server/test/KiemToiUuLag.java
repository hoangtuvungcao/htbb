/**
 * Kiem lag 10/2026 (docs: bao cao toi-uu-lag). Bat bien cua cac sua:
 *  - InfoClientConnect: lan chon nhan vat DAU TIEN cua mot IP khong bi cho 2 s (truoc day timeSelectChar = luc tao muc
 *    IP nen moi lan dang nhap moi tu IP khong qua proxy tin cay mat 2 s); lan thu hai trong 5 s van bi cho nhu cu.
 *  - Session: gom ghi socket chi khi ket noi dang dong (GOM_GUI_NGUONG lan ghi / 100 ms), tre toi da GOM_GUI_MS.
 *  - Client: Font giu bo chu ghep qua drawString; Session_ME ghi MOT khung / goi va danh thuc luong gui ngay.
 */
public final class KiemToiUuLag {
    private KiemToiUuLag() {
    }

    static String doc(String p) throws Exception {
        return new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(p)), "UTF-8");
    }

    public static void chay() {
        Kiem.nhom("Toi uu lag (chon nhan vat lan dau, gom ghi socket, cap phat moi khung client)");
        try {
            server.InfoClientConnect ic = new server.InfoClientConnect();
            Kiem.dung("IP moi: lan chon nhan vat dau KHONG bi cho", ic.canSelectChar());
            Kiem.dung("IP: chon lan hai trong 5 s van bi cho (luat cu)", !ic.canSelectChar());
            ic.timeSelectChar = System.currentTimeMillis() - 6000L;
            Kiem.dung("IP: sau 5 s chon lai duoc", ic.canSelectChar());

            Kiem.bang("gom ghi: mac dinh 10 ms", Integer.valueOf(10), Integer.valueOf(io.Session.GOM_GUI_MS));
            Kiem.bang("gom ghi: chi khi >= 8 lan ghi / 100 ms", Integer.valueOf(8), Integer.valueOf(io.Session.GOM_GUI_NGUONG));
            String sv = doc("src/io/Session.java");
            Kiem.dung("gom ghi: chi cho khi ket noi dang dong", sv.contains("soGhiCuaSo >= GOM_GUI_NGUONG && cho > 0 && cho <= GOM_GUI_MS"));

            String f = doc("../client_jar/src/game/render/screen/Font.java");
            int tao = f.indexOf("this.boGhep = new java.util.Hashtable();\n        }\n        java.util.Hashtable boCu = this.boGhep;");
            Kiem.dung("client Font: tao bo ghep TRUOC khi giu boCu (khong vut bo dem moi lan ve)", tao > 0);
            String sm = doc("../client_jar/src/network/Session_ME.java");
            Kiem.dung("client Session_ME: mot lan ghi moi goi", sm.contains("this.dos.write(khung);") && !sm.contains("this.dos.writeByte(byte1);"));
            Kiem.dung("client Session_ME: AddMessage danh thuc luong gui", sm.contains("this.notify();") && sm.contains("this.wait(10L);"));
            String cd = doc("../client_jar/src/game/pokemon/ChuDe.java");
            Kiem.dung("client ChuDe.cat: nho nhieu ket qua", cd.contains("CAT_SO = 24"));
            for (String p : new String[]{"../client_apk/pokemon/src/main/java/javax/microedition/io/Connector.java", "../client_pc/src/javax/microedition/io/Connector.java"}) {
                Kiem.dung("socket client co dem doc: " + p, doc(p).contains("new java.io.BufferedInputStream(s.getInputStream(), 8192)"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemToiUuLag loi: " + e, false);
        }
    }
}
