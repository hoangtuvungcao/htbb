import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import real.TocDoDiChuyen;

/**
 * Di chuyen / duoi muc tieu (bao 0.9.2 "danh va tu dong danh di giat, khong duoi quai"):
 *  - server: lenh 4 kiem toc do bang quy quang duong theo thoi gian (real.TocDoDiChuyen), KHONG Thread.sleep
 *    trong handler; di dung toc do (ke ca jitter mang, goi don) khong bi tra vi tri; hack toc do van bi chan.
 *  - client: tim duong BFS (client_jar game.model.DuongDi, bien dich tai cho bang javac neu co JDK) dung luat
 *    tuong cua server (type & 2), dung tren map > 127 o; mo phong duoi quai theo duong + gui vi tri that moi
 *    250 ms qua bo kiem server -> 0 lan bi tra vi tri; cac cho noi trong GameScr / MainChar / Char / TuDong.
 */
public final class KiemDiChuyen {

    private KiemDiChuyen() {
    }

    public static void chay() {
        Kiem.nhom("Di chuyen + duoi muc tieu (TocDoDiChuyen, DuongDi, MainChar.setPosTo)");
        try {
            quyServer();
            handlerKhongNgu();
            client();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    // ------------------------------------------------------------------ server

    static void quyServer() {
        // di dung toc do 5 px / 40 ms, goi moi 250 ms +- 60 ms, 15% goi den don (cach 5 ms): khong bi tu choi
        final Random r = new Random(1);
        long quy = -1L;
        long tLan = 0L;
        double x = 0;
        int sx = 0;
        int tuChoi = 0;
        long t = 1000L;
        for (int i = 0; i < 2000; i++) {
            final long buoc = r.nextInt(100) < 15 ? 5 : 190 + r.nextInt(121);
            t += buoc;
            x += buoc * 5.0 / 40.0; // vi tri that cua client
            final int nx = (int) x;
            final long q = TocDoDiChuyen.nap(quy, t - tLan, 5, 50);
            final long con = TocDoDiChuyen.buoc(q, nx - sx, 0, 50);
            if (con < 0) {
                tuChoi++;
                quy = q;
            } else {
                quy = con;
                sx = nx;
            }
            tLan = t;
        }
        Kiem.bang("di dung toc do + jitter + goi don: 0 lan bi tra vi tri", 0, tuChoi);

        // hack toc do: 50 px moi 10 ms -> bi chan gan het, trung binh khong vuot 1,5 lan toc do
        quy = -1L;
        tLan = 0L;
        sx = 0;
        int nhan = 0;
        t = 1000L;
        for (int i = 0; i < 1000; i++) {
            t += 10;
            final long q = TocDoDiChuyen.nap(quy, t - tLan, 5, 50);
            final long con = TocDoDiChuyen.buoc(q, 50, 0, 50);
            if (con >= 0) {
                nhan++;
                sx += 50;
                quy = con;
            } else {
                quy = q;
            }
            tLan = t;
        }
        // 10 giay: toi da 100 (quy dau) + 1,5 * 125 px/s * 10 s = 1975 px
        Kiem.dung("hack 50 px / 10 ms: quang duong 10 s <= 2000 px (that: " + sx + ")", sx <= 2000);
        Kiem.dung("hack 50 px / 10 ms: phan lon goi bi tu choi (" + nhan + "/1000 nhan)", nhan < 60);
        // mot buoc nhay > 2 lan nguong luon bi chan, ke ca quy day
        Kiem.bang("nhay 101 px voi quy day: tu choi", -1L, TocDoDiChuyen.buoc(TocDoDiChuyen.nap(-1L, 0, 5, 50), 101, 0, 50));
        Kiem.dung("nhay 100 px sau khi dung yen: nhan (bang nguong cu x2)",
                TocDoDiChuyen.buoc(TocDoDiChuyen.nap(-1L, 0, 5, 50), 100, 0, 50) >= 0);
        // bi lam cham (nguong 25): quy nap cham theo
        Kiem.dung("nguong 25 (lam cham): 250 ms nap ~ 1/2 cua nguong 50",
                Math.abs(TocDoDiChuyen.nap(0L, 250, 5, 25) * 2 - TocDoDiChuyen.nap(0L, 250, 5, 50)) <= 2);
    }

    static void handlerKhongNgu() throws Exception {
        final String src = new String(Files.readAllBytes(Paths.get("src/real/CharCapNhat.java")), StandardCharsets.UTF_8);
        // sua-tuyen: than ham nam o doPlayerMoveThan (doPlayerMove boc try/finally de kiem buoc dia diem SAU khi cap nhat vi tri)
        final int a = src.indexOf("static void doPlayerMoveThan(Char self");
        final int b = src.indexOf("\n    }\n", a);
        final String than = a < 0 || b < 0 ? "" : src.substring(a, b);
        Kiem.dung("CharCapNhat.doPlayerMove tim thay", !than.isEmpty());
        Kiem.sai("doPlayerMove KHONG Thread.sleep (chan luong xu ly phien)", than.contains("Thread.sleep("));
        Kiem.dung("doPlayerMove dung TocDoDiChuyen.nap / buoc", than.contains("TocDoDiChuyen.nap(") && than.contains("TocDoDiChuyen.buoc("));
        Kiem.dung("nhan buoc: cap nhat moc thoi gian + quy", than.contains("self.timeMove = now;") && than.contains("self.quyDiChuyen ="));
    }

    // ------------------------------------------------------------------ client

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(Paths.get(p)), StandardCharsets.UTF_8);
    }

    static void client() throws Exception {
        final String goc = "../client_jar/";
        final String gs = doc(goc + "src/game/render/screen/GameScr.java");
        final String mc = doc(goc + "src/game/model/MainChar.java");
        final String ch = doc(goc + "src/game/model/Char.java");
        final String td = doc(goc + "shared/mod/game/render/screen/TuDong.java");
        Kiem.dung("GameScr.updateFindRoad dung DuongDi.tim (bo vong quet ca ban do)", gs.contains("DuongDi.tim(Tilemap.type, Tilemap.w, Tilemap.h"));
        Kiem.dung("GameScr.moveToRange -> duoiToi (duong / thang, khong gui diem dich)", gs.contains("duoiToi(attackedLiveActor, maxRange);"));
        Kiem.dung("doUseSkillToFocusActor giu duong dang duoi", gs.contains("laDuongDuoi(this.focusedActor)"));
        Kiem.dung("GameScr.capNhatDuoi: duoi tiep moi khung toi tam", gs.contains("public void capNhatDuoi()") && mc.contains("capNhatDuoi()"));
        Kiem.dung("MainChar.setPosTo: lech nho khi dang di thi giu, lech lon thi dat lai", mc.contains("public void setPosTo(short x, short y)") && mc.contains("LECH_LON"));
        Kiem.dung("MainChar.updateWayPointer: ma hoa DuongDi (map > 127 o)", mc.contains("DuongDi.maX(") && !mc.contains("(byte) (GameScr.xStart"));
        Kiem.dung("MainChar gui vi tri that khi dang di", mc.contains("this.stop = false;") && !mc.contains("Canvas.gameScr.sendActorMove((short) ((xx * 16)"));
        Kiem.dung("Char.update: tang toc duoi kip chi cho nhan vat khac", ch.contains("if (!isMainchar()) {"));
        Kiem.dung("Char.update: di thang (hai truc theo ti le), khong chu L", ch.contains("sy = (dy1 * sx) / dx1;"));
        Kiem.dung("TuDong.diToi: tuong chan thi di theo duong", td.contains("gs.diTheoDuong(x, y);"));

        // bien dich DuongDi.java (thuan Java) va chay that tren ban do server/cMap
        final javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.out.println("   (bo qua phan chay DuongDi: khong co javac)");
            return;
        }
        final File ra = Files.createTempDirectory("kiemduongdi").toFile();
        final int rc = jc.run(null, null, null, "-nowarn", "-encoding", "UTF-8", "-d", ra.getPath(), goc + "src/game/model/DuongDi.java");
        Kiem.bang("bien dich client DuongDi.java", 0, rc);
        if (rc != 0) {
            return;
        }
        final java.net.URLClassLoader cl = new java.net.URLClassLoader(new java.net.URL[]{ra.toURI().toURL()}, null);
        final Class<?> dd = cl.loadClass("game.model.DuongDi");
        final Method tim = dd.getMethod("tim", int[].class, int.class, int.class, int.class, int.class, int.class, int.class);
        final Method maX = dd.getMethod("maX", short.class);
        final Method maY = dd.getMethod("maY", short.class);
        final Method tuong = dd.getMethod("tuong", int[].class, int.class, int.class, int.class, int.class);

        // map 40 (150x100): duong tim duoc khong qua o tuong (&2), o lien tiep ke nhau, dung ca o x >= 128
        final int[] wh = new int[2];
        final int[] type = napBanDo(40, wh);
        final int w = wh[0], h = wh[1];
        final Random r = new Random(5);
        int ok = 0, sai = 0, quaTuong = 0, xa128 = 0;
        long nsMax = 0;
        for (int k = 0; k < 2000 && ok < 100; k++) {
            final int sx = k % 3 == 0 ? 110 + r.nextInt(w - 110) : r.nextInt(w), sy = r.nextInt(h);
            final int dx = Math.max(0, Math.min(w - 1, sx + r.nextInt(41) - 20)), dy = Math.max(0, Math.min(h - 1, sy + r.nextInt(41) - 20));
            if ((Boolean) tuong.invoke(null, type, w, h, sx, sy) || (Boolean) tuong.invoke(null, type, w, h, dx, dy)) {
                continue;
            }
            final long t0 = System.nanoTime();
            final short[] p = (short[]) tim.invoke(null, type, w, h, sx, sy, dx, dy);
            nsMax = Math.max(nsMax, System.nanoTime() - t0);
            if (p == null) {
                continue;
            }
            ok++;
            int px = sx, py = sy;
            for (int i = p.length - 1; i >= 0; i--) {
                final int x = (Integer) maX.invoke(null, p[i]), y = (Integer) maY.invoke(null, p[i]);
                if (Math.abs(x - px) + Math.abs(y - py) != 1) {
                    sai++;
                }
                if ((Boolean) tuong.invoke(null, type, w, h, x, y)) {
                    quaTuong++;
                }
                if (x >= 128) {
                    xa128++;
                }
                px = x;
                py = y;
            }
            if (px != dx || py != dy) {
                sai++;
            }
        }
        Kiem.dung("DuongDi tim duoc duong cho >= 50 cap diem (" + ok + ")", ok >= 50);
        Kiem.bang("DuongDi: o lien tiep ke nhau + ket thuc dung dich", 0, sai);
        Kiem.bang("DuongDi: khong di qua o tuong cua server (type & 2)", 0, quaTuong);
        Kiem.dung("DuongDi: co duong qua o x >= 128 (ma hoa moi, cu ep byte)", xa128 > 0);
        Kiem.dung("DuongDi: moi lan tim < 50 ms (that: " + nsMax / 1000000 + " ms)", nsMax < 50_000_000L);

        // mo phong duoi: client di theo duong 5 px / 40 ms, gui vi tri that moi 250 ms (+ jitter), server kiem
        // bang TocDoDiChuyen + canMove (type & 2) + quang duong theo o -> 0 lan bi tra vi tri
        int tuChoi = 0, goi = 0, toi = 0;
        for (int k = 0; k < 40; k++) {
            final int sx = r.nextInt(w), sy = r.nextInt(h);
            final int dx = Math.max(0, Math.min(w - 1, sx + r.nextInt(31) - 15)), dy = Math.max(0, Math.min(h - 1, sy + r.nextInt(31) - 15));
            if ((Boolean) tuong.invoke(null, type, w, h, sx, sy) || (Boolean) tuong.invoke(null, type, w, h, dx, dy)) {
                continue;
            }
            final short[] p = (short[]) tim.invoke(null, type, w, h, sx, sy, dx, dy);
            if (p == null) {
                continue;
            }
            int x = sx * 16 + 8, y = sy * 16 + 8, svx = x, svy = y;
            long t = 0, lucGui = 0, quy = -1L, tSv = 0;
            int i = p.length - 1;
            while (i >= 0 && t < 60000) {
                final int ox = (Integer) maX.invoke(null, p[i]) * 16 + 8, oy = (Integer) maY.invoke(null, p[i]) * 16 + 8;
                final int ddx = ox - x, ddy = oy - y;
                if (Math.abs(ddx) <= 5 && Math.abs(ddy) <= 5) {
                    i--; // nhan o ke truoc khi toi han (MainChar.updateWayPointer)
                }
                x += Integer.signum(ddx) * Math.min(5, Math.abs(ddx));
                y += Integer.signum(ddy) * Math.min(5, Math.abs(ddy));
                t += 40;
                if (t - lucGui >= 250 + r.nextInt(3) * 40) {
                    lucGui = t;
                    goi++;
                    final long q = TocDoDiChuyen.nap(quy, t - tSv, 5, 50);
                    final long con = TocDoDiChuyen.buoc(q, x - svx, y - svy, 50);
                    if (con < 0 || (Boolean) tuong.invoke(null, type, w, h, x >> 4, y >> 4)) {
                        tuChoi++;
                        quy = q;
                    } else {
                        quy = con;
                        svx = x;
                        svy = y;
                    }
                    tSv = t;
                }
            }
            toi++;
        }
        Kiem.dung("mo phong duoi: " + toi + " lan, " + goi + " goi vi tri", toi > 10 && goi > 50);
        Kiem.bang("mo phong duoi theo duong: server tra vi tri 0 lan", 0, tuChoi);
    }

    /** Doc type o cua map (giong bot / client: ma tile -> bang .type). */
    static int[] napBanDo(final int load, final int[] wh) throws Exception {
        final byte[] tt = Files.readAllBytes(Paths.get("cMap", (load < 110 ? "t" : (load > 200 ? "t_thanh" : "t_hang")) + ".type"));
        final byte[] b = Files.readAllBytes(Paths.get("cMap", String.valueOf(load)));
        final int w = b[0] & 255, h = b[1] & 255;
        final int[] ty = new int[w * h];
        for (int i = 0; i < w * h; i++) {
            final int t = b[2 + i] & 255;
            if (t != 254 && t != 255 && t < tt.length) {
                ty[i] = tt[t] & 255;
            }
        }
        wh[0] = w;
        wh[1] = h;
        return ty;
    }
}
