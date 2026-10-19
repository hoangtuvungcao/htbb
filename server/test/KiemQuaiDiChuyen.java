import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.BanDoPokemon;
import com.htbb.pokemon.DiChuyenQuai;
import com.htbb.pokemon.DinhDangBanDo;

/**
 * Quai di chuyen theo CUNG luat va cham voi nguoi choi (bao 0.9.3 "quai di vao tuong / nuoc / ngoai mep map").
 *
 * VI SAO: Monster.canMove cu tra "o chan" va luon "khong chan" khi quai co muc tieu -> quai duoi nguoi choi nhay
 * xuyen tuong; client lang thang chi kiem 1 diem, ra mep map thi tran sang hang khac; noi suy L giua hai vi tri
 * may chu cat goc tuong. Test chay tren MOI ban do that (cMap + luoi quai map/map&lt;N&gt;):
 *  - moi diem sinh nam tren o quai di duoc (khong o trong / cong / chan), vung xich quanh diem sinh co cho di;
 *  - mo phong lang thang + duoi (DiChuyenQuai) tu moi diem sinh: moi vi tri di duoc, moi doan thang thong, trong xich;
 *  - quai duoi vong qua tuong (khong xuyen), khong co duong trong xich = bo muc tieu; diem sinh tren o chan -> o gan;
 *  - client: DuongDi.diQuai / buocQuai (bien dich tai cho) chan tuong, ra mep, vuc; Monster dung chung.
 */
public final class KiemQuaiDiChuyen {

    public static void chay() {
        Kiem.nhom("Quai di chuyen theo va cham (DiChuyenQuai, cMap, Monster client)");
        try {
            tongHop();
            tatCaBanDo();
            client();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    /** Luoi giong server (MapKhoiTao): type = kieu tile, o cong (MapNapTaiNguyen) = 2e9 + i. */
    static DiChuyenQuai.Luoi luoi(final int id) throws Exception {
        final DinhDangBanDo m = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + id).toPath()));
        final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + DinhDangBanDo.boTile(id) + ".type").toPath());
        final int[] type = new int[m.w * m.h];
        final short[] tile = new short[m.w * m.h];
        for (int i = 0; i < type.length; i++) {
            int t = m.tile[i];
            if (t == 254) {
                t = 255;
            }
            tile[i] = (short) t;
            if (t != 255 && t < k.length) {
                type[i] = k[t] & 0xFF;
            }
        }
        int i = 0;
        for (final DinhDangBanDo.Cong c : m.cong) {
            type[c.y * m.w + c.x] = DiChuyenQuai.CONG + i++;
        }
        return DiChuyenQuai.luoi(type, tile, m.w, m.h);
    }

    static DiChuyenQuai.Luoi tuChuoi(final String... hang) {
        final int w = hang[0].length(), h = hang.length;
        final int[] type = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                type[y * w + x] = hang[y].charAt(x) == '#' ? 2 : 0;
            }
        }
        return DiChuyenQuai.luoi(type, null, w, h);
    }

    static void tongHop() {
        final DiChuyenQuai.Luoi l = tuChuoi(
                "..........",
                "....#.....",
                "....#.....",
                "....#.....",
                "....#.....",
                "..........");
        Kiem.dung("ngoai mep map = chan", !DiChuyenQuai.diPx(l, -1, 5) && !DiChuyenQuai.diPx(l, 160, 5) && !DiChuyenQuai.diPx(l, 5, 96));
        Kiem.dung("o tuong = chan", !DiChuyenQuai.diPx(l, 4 * 16 + 3, 2 * 16 + 3));
        Kiem.sai("doan thang xuyen tuong khong thong", DiChuyenQuai.thong(l, 2 * 16 + 8, 2 * 16 + 8, 7 * 16 + 8, 2 * 16 + 8));
        // duoi: quai trai tuong, nguoi choi phai tuong -> vong qua dau/duoi tuong, khong o nao nam tren tuong
        int x = 2 * 16 + 8, y = 2 * 16 + 8;
        final int tx = 7 * 16 + 8, ty = 2 * 16 + 8;
        boolean xuyen = false, toi = false;
        for (int lan = 0; lan < 20 && !toi; lan++) {
            final int[] b = DiChuyenQuai.duoi(l, x, y, tx, ty, x, y, 200, DiChuyenQuai.BUOC_DUOI);
            if (b == null) {
                break;
            }
            if (b == DiChuyenQuai.DA_TOI) {
                toi = true;
                break;
            }
            if (!DiChuyenQuai.thong(l, x, y, b[0], b[1])) {
                xuyen = true;
            }
            x = b[0];
            y = b[1];
        }
        Kiem.dung("duoi vong qua tuong toi sat muc tieu", toi);
        Kiem.sai("duoi: khong buoc nao cat tuong", xuyen);
        // muc tieu bi tuong bao kin -> khong co duong -> null (bo muc tieu, ve nha)
        final DiChuyenQuai.Luoi kin = tuChuoi(
                "..........",
                "......###.",
                "......#.#.",
                "......#.#.",
                "......###.",
                "..........");
        Kiem.bang("muc tieu trong o kin: khong duong -> null", null,
                DiChuyenQuai.duoi(kin, 24, 24, 7 * 16 + 8, 2 * 16 + 8 + 16, 24, 24, 300, 96) == null ? null : "co duong");
        // xich: muc tieu ngoai xich -> khong toi duoc trong vung xich
        final DiChuyenQuai.Luoi rong = tuChuoi("........................................", "........................................");
        Kiem.dung("muc tieu ngoai xich 120 px -> null (bo, ve diem sinh)",
                DiChuyenQuai.duoi(rong, 8, 8, 39 * 16, 8, 8, 8, 120, 96) == null);
        // diem sinh tren tuong -> o di duoc gan nhat
        final int[] o = DiChuyenQuai.oGan(l, 4 * 16, 2 * 16, 8);
        Kiem.dung("diem sinh tren tuong -> o ke ben di duoc", o != null && DiChuyenQuai.diPx(l, o[0], o[1])
                && Math.abs((o[0] >> 4) - 4) + Math.abs((o[1] >> 4) - 2) == 1);
        final DiChuyenQuai.Luoi dac = tuChuoi("###", "###");
        Kiem.dung("khong co o di duoc: oGan null", DiChuyenQuai.oGan(dac, 16, 16, 8) == null);
        Kiem.dung("o trong (vuc, tile 255) = chan", !DiChuyenQuai.diO(new int[]{0}, new short[]{255}, 1, 1, 0, 0));
        Kiem.dung("o cong chuyen map = chan voi quai", !DiChuyenQuai.diO(new int[]{DiChuyenQuai.CONG + 3}, null, 1, 1, 0, 0));
        Kiem.dung("dung luat Map.canMove: (type & 2) chan, bit khac di duoc",
                !DiChuyenQuai.diO(new int[]{6}, null, 1, 1, 0, 0) && DiChuyenQuai.diO(new int[]{4}, null, 1, 1, 0, 0));
    }

    static void tatCaBanDo() throws Exception {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final String f : new File("map").list()) {
            if (f.matches("map\\d+")) {
                ids.add(Integer.parseInt(f.substring(3)));
            }
        }
        java.util.Collections.sort(ids);
        int soMap = 0, soQuai = 0, chan = 0, ket = 0, saiMoPhong = 0, lechCo = 0;
        final List<String> loi = new ArrayList<String>();
        final Random r = new Random(903);
        for (final int id : ids) {
            if (!new File(BanDoPokemon.THU_MUC_CMAP + id).isFile()) {
                continue;
            }
            final DinhDangBanDo.LuoiQuai q = DinhDangBanDo.LuoiQuai.doc(Files.readAllBytes(new File("map/map" + id).toPath()));
            final DiChuyenQuai.Luoi l = luoi(id);
            boolean coQuai = false;
            for (int y = 0; y < q.h; y++) {
                for (int x = 0; x < q.w; x++) {
                    if (!q.coQuai(x, y)) {
                        continue;
                    }
                    coQuai = true;
                    soQuai++;
                    if (q.w != l.w() || q.h != l.h()) {
                        lechCo++;
                    }
                    final int sx = x * 16, sy = y * 16;   // MapNapTaiNguyen.loadMap: new Monster(.., j * 16, i * 16, ..)
                    if (!DiChuyenQuai.diPx(l, sx, sy)) {
                        chan++;
                        if (loi.size() < 12) {
                            loi.add(id + ": diem sinh tren o chan " + x + "," + y);
                        }
                        continue;
                    }
                    // vung xich (120 px) co it nhat 3 o di duoc lien thong (lang thang / duoi duoc)
                    if (vungXich(l, x, y, 7) < 3) {
                        ket++;
                        if (loi.size() < 12) {
                            loi.add(id + ": diem sinh bi bao kin " + x + "," + y);
                        }
                    }
                    if (!moPhong(l, sx, sy, r)) {
                        saiMoPhong++;
                        if (loi.size() < 12) {
                            loi.add(id + ": mo phong ra o chan / xuyen tuong tu " + x + "," + y);
                        }
                    }
                }
            }
            if (coQuai) {
                soMap++;
            }
        }
        // diem sinh viet cung (map 28: MapNapTaiNguyen customMonsters)
        final String nap = new String(Files.readAllBytes(new File("src/real/MapNapTaiNguyen.java").toPath()), StandardCharsets.UTF_8);
        final int dau = nap.indexOf("customMonsters");
        final Matcher m = Pattern.compile("\\{\\s*(\\d+),\\s*(\\d+),\\s*(\\d+)\\s*\\}").matcher(nap.substring(dau, nap.indexOf("};", dau)));
        final DiChuyenQuai.Luoi l28 = luoi(28);
        int cung = 0, cungChan = 0;
        while (m.find()) {
            cung++;
            if (!DiChuyenQuai.diPx(l28, Integer.parseInt(m.group(2)) * 16, Integer.parseInt(m.group(3)) * 16)) {
                cungChan++;
                loi.add("28: diem sinh viet cung tren o chan " + m.group(2) + "," + m.group(3));
            }
        }
        System.out.println("    " + soMap + " ban do co quai, " + soQuai + " diem sinh");
        Kiem.dung("co ban do co quai (> 20)", soMap > 20);
        Kiem.dung("co diem sinh quai (> 500)", soQuai > 500);
        Kiem.bang("luoi quai cung co cMap", 0, lechCo);
        Kiem.bang("MOI diem sinh nam tren o quai di duoc", 0, chan);
        Kiem.bang("vung xich quanh diem sinh khong bi bao kin", 0, ket);
        Kiem.bang("mo phong lang thang + duoi tu moi diem sinh: khong o chan / xuyen tuong / ra ngoai xich", 0, saiMoPhong);
        Kiem.dung("diem sinh viet cung map 28 doc duoc", cung >= 8);
        Kiem.bang("diem sinh viet cung map 28 tren o di duoc", 0, cungChan);
        if (!loi.isEmpty()) {
            System.out.println("    vi du: " + loi);
        }
    }

    /** So o di duoc lien thong voi o sinh trong ban kinh r o (toi da 9). */
    static int vungXich(final DiChuyenQuai.Luoi l, final int ox, final int oy, final int r) {
        final java.util.ArrayDeque<int[]> q = new java.util.ArrayDeque<int[]>();
        final java.util.HashSet<Integer> da = new java.util.HashSet<Integer>();
        q.add(new int[]{ox, oy});
        da.add(oy * l.w() + ox);
        while (!q.isEmpty() && da.size() < 9) {
            final int[] c = q.poll();
            final int[][] k4 = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            for (final int[] d : k4) {
                final int x = c[0] + d[0], y = c[1] + d[1];
                if (Math.abs(x - ox) > r || Math.abs(y - oy) > r || !l.di(x, y) || !da.add(y * l.w() + x)) {
                    continue;
                }
                q.add(new int[]{x, y});
            }
        }
        return da.size();
    }

    /** 60 buoc lang thang (dmove 32) xen duoi mot "nguoi choi" o ngau nhien gan diem sinh; moi buoc phai hop le. */
    static boolean moPhong(final DiChuyenQuai.Luoi l, final int sx, final int sy, final Random r) {
        int x = sx, y = sy;
        for (int lan = 0; lan < 60; lan++) {
            int[] b;
            if (lan % 3 == 2) {
                final int tx = sx + r.nextInt(241) - 120, ty = sy + r.nextInt(241) - 120;
                if (!DiChuyenQuai.diPx(l, tx, ty)) {
                    continue;
                }
                b = DiChuyenQuai.duoi(l, x, y, tx, ty, sx, sy, 120, DiChuyenQuai.BUOC_DUOI);
                if (b == null) {
                    x = sx;   // xich: bo muc tieu, ve diem sinh
                    y = sy;
                    continue;
                }
                if (b == DiChuyenQuai.DA_TOI) {
                    continue;
                }
            } else {
                b = DiChuyenQuai.langThang(l, x, y, sx, sy, 32, 120, r);
                if (b == null) {
                    continue;
                }
            }
            if (!DiChuyenQuai.diPx(l, b[0], b[1]) || !DiChuyenQuai.thong(l, x, y, b[0], b[1])) {
                return false;
            }
            x = b[0];
            y = b[1];
            if (Math.abs(x - sx) >= 120 + 24 || Math.abs(y - sy) >= 120 + 24) {
                return false;
            }
        }
        return true;
    }

    /** Client: DuongDi.diQuai / buocQuai (thuan Java) bien dich tai cho, chay tren map that; Monster dung chung. */
    static void client() throws Exception {
        final String goc = new File("../client_jar").isDirectory() ? "../client_jar/" : null;
        if (goc == null) {
            System.out.println("   (bo qua phan client: khong co client_jar)");
            return;
        }
        final String mo = new String(Files.readAllBytes(new File(goc + "src/game/model/Monster.java").toPath()), StandardCharsets.UTF_8);
        Kiem.dung("Monster client: buoc theo va cham (buocToi / diQuai)", mo.contains("DuongDi.buocQuai(Tilemap.type, Tilemap.map")
                && mo.contains("DuongDi.diQuai(Tilemap.type, Tilemap.map"));
        Kiem.sai("Monster client: khong con kiem 1 diem tileTypeAtPixel (ra mep map tran hang)", mo.contains("Tilemap.tileTypeAtPixel("));
        Kiem.dung("Monster client: ket khi noi suy -> dat dung vi tri may chu", mo.contains("if (!buocToi(this.xTo, this.yTo)) {"));
        final String moi = new String(Files.readAllBytes(new File(goc + "src/game/model/MonsterNew.java").toPath()), StandardCharsets.UTF_8);
        Kiem.dung("MonsterNew: diem duoi lech tranh o chan", moi.contains("diemDuoi(this.target)"));
        final String sv = new String(Files.readAllBytes(new File("src/real/Monster.java").toPath()), StandardCharsets.UTF_8);
        Kiem.dung("server Monster: duoi theo DiChuyenQuai.duoi, kiem diem sinh", sv.contains("DiChuyenQuai.duoi(") && sv.contains("this.kiemDiemSinh();"));
        final javax.tools.JavaCompiler jc = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (jc == null) {
            System.out.println("   (bo qua phan chay DuongDi: khong co javac)");
            return;
        }
        final File ra = Files.createTempDirectory("kqdc").toFile();
        try {
            final int rc = jc.run(null, null, null, "-nowarn", "-encoding", "UTF-8", "-d", ra.getPath(), goc + "src/game/model/DuongDi.java");
            Kiem.bang("bien dich client DuongDi.java", 0, rc);
            if (rc != 0) {
                return;
            }
            final java.net.URLClassLoader cl = new java.net.URLClassLoader(new java.net.URL[]{ra.toURI().toURL()}, null);
            final Class<?> c = cl.loadClass("game.model.DuongDi");
            final java.lang.reflect.Method di = c.getMethod("diQuai", int[].class, short[].class, int.class, int.class, int.class, int.class);
            final java.lang.reflect.Method buoc = c.getMethod("buocQuai", int[].class, short[].class, int.class, int.class, int.class,
                    int.class, int.class, int.class, int.class);
            final java.lang.reflect.Field bx = c.getField("buocX"), by = c.getField("buocY");
            // map that: so khop luat client voi server tren MOI o cua vai map co quai
            int lech = 0, saiBuoc = 0;
            for (final int id : new int[]{1, 2, 9, 10, 17, 40, 44, 105}) {
                if (!new File(BanDoPokemon.THU_MUC_CMAP + id).isFile()) {
                    continue;
                }
                final DiChuyenQuai.Luoi l = luoi(id);
                final DinhDangBanDo m = DinhDangBanDo.doc(Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + id).toPath()));
                final byte[] k = Files.readAllBytes(new File(BanDoPokemon.THU_MUC_CMAP + DinhDangBanDo.boTile(id) + ".type").toPath());
                final int[] type = new int[m.w * m.h];
                final short[] tile = new short[m.w * m.h];
                for (int i = 0; i < type.length; i++) {
                    int t = m.tile[i] == 254 ? 255 : m.tile[i];
                    tile[i] = (short) t;
                    if (t != 255 && t < k.length) {
                        type[i] = k[t] & 0xFF;
                    }
                }
                int ic = 0;
                for (final DinhDangBanDo.Cong cg : m.cong) {
                    type[cg.y * m.w + cg.x] = DiChuyenQuai.CONG + ic++;   // Tilemap: o cong = 2e9 + i
                }
                final Random r = new Random(id);
                for (int y = -1; y <= m.h; y++) {
                    for (int x = -1; x <= m.w; x++) {
                        final boolean a = (Boolean) di.invoke(null, type, tile, m.w, m.h, x * 16 + 5, y * 16 + 5);
                        if (a != DiChuyenQuai.diPx(l, x * 16 + 5, y * 16 + 5)) {
                            lech++;
                        }
                    }
                }
                // buoc ngau nhien 2000 lan: khong bao gio buoc vao o chan
                int x = -1, y = -1;
                for (int t = 0; t < 4000 && x < 0; t++) {
                    final int px = r.nextInt(m.w * 16), py = r.nextInt(m.h * 16);
                    if (DiChuyenQuai.diPx(l, px, py)) {
                        x = px;
                        y = py;
                    }
                }
                for (int t = 0; t < 2000 && x >= 0; t++) {
                    final int tx = x + r.nextInt(97) - 48, ty = y + r.nextInt(97) - 48;
                    if ((Boolean) buoc.invoke(null, type, tile, m.w, m.h, x, y, tx, ty, 1 + r.nextInt(4))) {
                        x = bx.getInt(null);
                        y = by.getInt(null);
                        if (!DiChuyenQuai.diPx(l, x, y)) {
                            saiBuoc++;
                        }
                    }
                }
            }
            Kiem.bang("client DuongDi.diQuai = server DiChuyenQuai (moi o, ca mep ngoai)", 0, lech);
            Kiem.bang("client buocQuai khong buoc vao o chan (map that)", 0, saiBuoc);
            cl.close();
        } finally {
            for (final File f : new File(ra, "game/model").listFiles() == null ? new File[0] : new File(ra, "game/model").listFiles()) {
                f.delete();
            }
            new File(ra, "game/model").delete();
            new File(ra, "game").delete();
            ra.delete();
        }
    }
}
