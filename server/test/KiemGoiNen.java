import io.GoiNen;

/**
 * Kiem io.GoiNen (nen goi lon cho client co kha nang): boc -> mo lai dung tung byte, goi nho / da nen khong boc,
 * khung nen > 32767 byte dung tien to -128 + 4 byte do dai nhu Message.toByteArray, va client giai nen dung dinh dang
 * (doc dung ma network.Session_ME / GiaiNenZip).
 */
public final class KiemGoiNen {
    private KiemGoiNen() {
    }

    static byte[] khung(int cmd, byte[] d) {
        final boolean lon = d.length > 32767;
        final byte[] k = new byte[(lon ? 6 : 3) + d.length];
        int i = 0;
        if (lon) k[i++] = -128;
        k[i++] = (byte) cmd;
        if (lon) {
            k[i++] = (byte) (d.length >>> 24);
            k[i++] = (byte) (d.length >>> 16);
        }
        k[i++] = (byte) (d.length >>> 8);
        k[i++] = (byte) d.length;
        System.arraycopy(d, 0, k, i, d.length);
        return k;
    }

    /** Doc khung ngoai (nhu Session_ME.readMessage, chua ma hoa) -> {cmd, du lieu}. */
    static Object[] docKhung(byte[] k) {
        int i = 0;
        int c = k[i++];
        int n;
        if (c == -128) {
            c = k[i++];
            n = ((k[i] & 255) << 24) | ((k[i + 1] & 255) << 16) | ((k[i + 2] & 255) << 8) | (k[i + 3] & 255);
            i += 4;
        } else {
            n = ((k[i] & 255) << 8) | (k[i + 1] & 255);
            i += 2;
        }
        return new Object[]{Integer.valueOf(c), java.util.Arrays.copyOfRange(k, i, i + n)};
    }

    static byte[] giai(byte[] nen) throws Exception {
        final java.util.zip.Inflater f = new java.util.zip.Inflater();
        f.setInput(nen);
        final java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        final byte[] b = new byte[4096];
        while (!f.finished()) {
            final int c = f.inflate(b);
            if (c == 0 && f.needsInput()) throw new java.io.IOException("hong");
            o.write(b, 0, c);
        }
        f.end();
        return o.toByteArray();
    }

    public static void chay() {
        Kiem.nhom("io.GoiNen - nen goi lon cho client co kha nang");
        try {
            // du lieu giong mau vat pham: chu + so lap lai
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3000; i++) sb.append("Bình thuốc hồi máu cấp ").append(i % 50).append(';');
            final byte[] d = sb.toString().getBytes("UTF-8");
            final byte[] goc = khung(25, d);
            final byte[] nen = GoiNen.boc(goc);
            Kiem.dung("goi chu " + goc.length + " B duoc nen", nen != null && nen.length < goc.length / 2);
            final Object[] ngoai = docKhung(nen);
            Kiem.bang("khung ngoai la lenh -127", Integer.valueOf(-127), ngoai[0]);
            final byte[] trong = giai((byte[]) ngoai[1]);
            Kiem.dung("giai nen ra DUNG khung goc tung byte", java.util.Arrays.equals(goc, trong));
            final Object[] tk = docKhung(trong);
            Kiem.bang("khung trong la lenh 25", Integer.valueOf(25), tk[0]);

            // goi nho: khong boc
            Kiem.dung("goi < nguong khong nen", GoiNen.boc(khung(4, new byte[100])) == null);
            // du lieu ngau nhien (anh PNG): nen khong loi -> khong boc
            final byte[] nn = new byte[20000];
            new java.util.Random(7).nextBytes(nn);
            Kiem.dung("du lieu khong nen duoc (anh) -> giu nguyen", GoiNen.boc(khung(-51, nn)) == null);
            // khung lon > 32767 byte sau nen: tien to -128
            final byte[] to = new byte[300000];
            final java.util.Random r = new java.util.Random(3);
            for (int i = 0; i < to.length; i++) to[i] = (byte) (r.nextInt(16) + 'a');
            final byte[] goc2 = khung(26, to);
            final byte[] nen2 = GoiNen.boc(goc2);
            Kiem.dung("khung 300 KB nen > 32767 B dung tien to -128", nen2 != null && nen2[0] == -128 && nen2[1] == -127);
            Kiem.dung("khung 300 KB giai ra dung", java.util.Arrays.equals(goc2, giai((byte[]) docKhung(nen2)[1])));
            // tat bang sv.nenGoi=0
            GoiNen.BAT = false;
            Kiem.dung("sv.nenGoi=0 -> khong nen", GoiNen.boc(goc) == null);
            GoiNen.BAT = true;

            // client: cung dinh dang (Session_ME doc -127, GiaiNenZip giai, AutoGuard bao kha nang)
            final String sm = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../client_jar/src/network/Session_ME.java")), "UTF-8");
            Kiem.dung("client Session_ME mo goi -127", sm.contains("GOI_NEN = -127") && sm.contains("moGoiNen"));
            final String ag = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("../client_jar/shared/mod/game/render/screen/AutoGuard.java")), "UTF-8");
            Kiem.dung("client gui kha nang sau so ban", ag.indexOf("Session_ME.khaNang()") > ag.indexOf("writeInt(PhienBan.BAN)"));
            Kiem.dung("PC + APK co GiaiNenZip", new java.io.File("../client_pc/src/network/GiaiNenZip.java").isFile()
                    && new java.io.File("../client_apk/pokemon/src/main/java/network/GiaiNenZip.java").isFile());
            final String lh = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get("src/real/cmd/LoginHandler.java")), "UTF-8");
            Kiem.dung("server doc kha nang SAU so ban (client cu khong gui -> 0)", lh.indexOf("session.khaNang = dis.readInt()") > lh.indexOf("banClient = dis.readInt()"));
        } catch (Exception e) {
            Kiem.dung("KiemGoiNen loi: " + e, false);
        }
    }
}
